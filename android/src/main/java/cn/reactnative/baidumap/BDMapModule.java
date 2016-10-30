package cn.reactnative.baidumap;

import android.app.Activity;
import android.os.AsyncTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tdzl2003 on 3/31/16.
 */
public class BDMapModule extends ReactContextBaseJavaModule implements BDLocationListener {

    LocationClient mClient = null;
    public BDMapModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RCTBaiduMap";
    }

    @Override
    public void initialize() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _createClient();
                mClient.setLocOption(createDefaultOption());
            }
        });
    }

    private LocationClientOption createDefaultOption() {
        LocationClientOption option = new LocationClientOption();

        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        option.setEnableSimulateGps(true);
        return option;
    }

    @Override
    public void onCatalystInstanceDestroy() {
    }

    @ReactMethod
    public void setOptions(final ReadableMap options, final Promise promise) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (options != null) {
                    LocationClientOption option = createDefaultOption();

                    if (options.hasKey("mode")) {
                        option.setLocationMode(LocationClientOption.LocationMode.valueOf(options.getString("mode")));
                    }
                    if (options.hasKey("scanSpan")) {
                        option.setScanSpan(options.getInt("scanSpan"));
                    }
                    mClient.setLocOption(option);
                }

                promise.resolve(null);
            }
        });
    }

    private LatLng readLatLng(ReadableMap pos) {
        return new LatLng(pos.getDouble("latitude"), pos.getDouble("longitude"));
    }

    public void getDistance(ReadableMap pos1, ReadableMap pos2, Promise promise) {
        promise.resolve(DistanceUtil.getDistance(readLatLng(pos1), readLatLng(pos2)));
    }

    @ReactMethod
    public void startWatch() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isListening = true;
                _start();
            }
        });
    }

    @ReactMethod
    public void stopWatch() {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isListening = false;
            }
        });
    }

    private void _start() {
        if (!mClient.isStarted()){
            mClient.start();
        }
    }

    private void _createClient() {
        if (mClient == null) {
            mClient = new LocationClient(this.getReactApplicationContext().getApplicationContext());
            mClient.registerLocationListener(this);
        }
    }

    boolean isListening;

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        UiThreadUtil.assertOnUiThread();

        WritableMap map = Arguments.createMap();
        int code = bdLocation.getLocType();
        map.putInt("code", code);
        switch (code) {
            case 61:case 65:case 66:case 67:case 68:case 161:
                break;
            default:
                getReactApplicationContext()
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("BDMapLocationError", map);
                return;
        }

        map.putDouble("latitude", bdLocation.getLatitude());
        map.putDouble("longitude", bdLocation.getLongitude());

        if (bdLocation.hasAddr()) {
            map.putString("address", bdLocation.getAddrStr());
        }
        if (bdLocation.hasAltitude()) {
            map.putDouble("altitude", bdLocation.getAltitude());
        }
        if (bdLocation.hasSpeed()) {
            map.putDouble("speed", bdLocation.getSpeed());
        }

        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("BDMapLocation", map);

        if (!isListening){
            mClient.stop();
        }
    }
}
