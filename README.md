## 安装

首先安装rnpm

```
npm install -g rnpm
```

```
npm install react-native-bdmap
react-native link react-native-bdmap
```

此时应看到输出

```
rnpm-link info Linking react-native-bdmap android dependency
rnpm-link info Android module react-native-bdmap has been successfully linked
rnpm-link info Linkng react-native-bdmap ios dependency
rnpm-link info iOS module react-native-bdmap has been successfully linked
```

为成功

Android: 入口代码

在`android/app/src/main/你的包名/MainActivity.java`中增加如下代码:

```java
import android.os.Bundle;
import com.baidu.mapapi.SDKInitializer;

public class MainActivity extends ReactActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplicationContext());
    }
    ...
}
```

Android: 添加混淆规则:

在`android/app/proguard-rules.pro`尾部,增加如下内容:

```
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}    
-dontwarn com.baidu.**
```

Android: 配置API Key

在`android/app/build.gradle`中, `defaultConfig`内,增加如下代码:

```
    defaultConfig {
        // 增加下面3行:
        manifestPlaceholders = [
                BAIDU_MAP_API_KEY: "Android端ApiKey"   //在此修改百度地图的APIKEY
        ]
    }
```

iOS: 配置Info.plist

确保Info.plist中有以下内容

```
    <key>NSAppTransportSecurity</key>
    <dict>
        <key>NSAllowsArbitraryLoads</key>
        <true/>
    </dict>
    <key>BDMapApiKey</key>
    <string>iOS端ApiKey</string>
    <key>CFBundleDisplayName</key>
    <string>应用显示名称</string>
```

如果只有应用在前台时定位,增加以下内容

```
    <key>NSLocationWhenInUseUsageDescription</key>
    <string>关于本应用为何要用此功能的描述</string>
```

如果应用在后台时也需要定位,增加以下内容

```
    <key>NSLocationAlwaysUsageDescription</key>
    <string>关于本应用为何要用此功能的描述</string>
```

iOS: 添加依赖

将`node_modules/react-native-baidu-map/ios/SDK/`下所有.framework文件拖入工程

并将`node_modules/react-native-baidu-map/ios/SDK/BaiduMapAPI_Map.framework/Resources/mapapi.bundle`拖入工程

在Build Settings中的Framework Search Paths中,增加:

`$(SRCROOT)/../node_modules/react-native-bdmap/ios/SDK`

在Build Phases中的Link Binary With Libraries中,增加:

* CoreTelephony.framework
* libc++.tbd
* libstdc++.6.0.9.tbd
* libsqlite3.tbd

## API

#### getDistance({latitude, longitude}, {latitude, longitude}) => Promise<number>

提供经纬度,获取两点之间的实际地理距离,返回单位是米

#### getLocation([options]) => Promise<position>

获取当前的位置.position包含如下字段:

* code: 错误码,参见[错误码 - Android定位SDK](http://lbsyun.baidu.com/index.php?title=android-locsdk/guide/ermsg)
* latitude
* longitude
* speed: 移动速度,可能为空
* altitude: 当前高度,可能为空
* address: 当前地址名字,可能为空

#### watchPosition(success[, error[, options]]) => watchId

持续监听当前的位置.success回调会被多次调用,其参数意义同`getLocation()`的返回值

#### clearWatch(watchId)

清除一个位置监听器

#### stopObserving()

清除所有位置监听器

#### geoReverse({latitude, longitude}) => Promise<object>

逆地理编码. 返回编码信息以及Poi信息

#### poiSearch({latitude, longitude}, {keyword, sortMode, radius, pageIndex, pageCapacity}) => Promise<object>

* keyword: 搜索关键字
* sortMode: 'composite': 综合排序 'nearToFar': 由近到远排序
* radius: 搜索半径
* pageIndex: 分页页码
* pageCapacity: 分页每页个数

## class BDMapView extends React.Component

地图组件.

### 属性列表

#### 包含View的所有属性

#### blurMarkerWhileMove: bool

是否在移动位置的时候取消聚焦标记物

#### region: {latitude, longitude, latitudeDelta, longitudeDelta}

当前显示的区域.改变这个属性的值将触发视野的变化.

区域使用中心的坐标和要显示的范围来定义。

注意: 这类似TextInput等Controlled Component,如果你指定了这个值,
并且没有随onRegionChange改变这个值,用户将无法移动地图

典型写法:

```
    state = {};
    render() {
      return <MapView 
        region={this.state.region} 
        onRegionChange={region=>this.setState({region})}
        />
    }
```

#### defaultRegion: {latitude, longitude, latitudeDelta, longitudeDelta}

初始化地图的时候显示的区域.没指定region时才会生效.改变这个属性没有任何作用.

#### annotations: [{latitude, longitude, onPress, onFocus, onBlur, id, icon}]

标记物列表
id, latitude, longitude是必填项.

icon={require('./xxx.png')} 可以指定标记物的图片

当此属性改变时,也会通过React的dom diff机制进行,不必担心带来大幅创建View的开销

#### onRegionChange: func(region)

在用户拖拽地图的时候持续调用此回调函数。

#### onRegionChangeComplete: func(region)

当用户停止拖拽地图之后，调用此回调函数一次。
