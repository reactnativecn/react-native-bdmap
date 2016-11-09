package cn.reactnative.baidumap;

import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.baidu.mapapi.map.MapView;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tdzl2003 on 4/23/16.
 */
public class BDMapManager extends ViewGroupManager<MapView>  {
    @Override
    public String getName() {
        return "RCTBDMapView";
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "topLoad", MapBuilder.of("registrationName", "onLoad"),
                "topRegionChange", MapBuilder.of("registrationName", "onRegionChange"),
                "topRegionChangeComplete", MapBuilder.of("registrationName", "onRegionChangeComplete")
        );
    }
    @Override
    protected MapView createViewInstance(final ThemedReactContext reactContext) {
        final MapView ret = new MapView(reactContext);
        new BDMapExtraData(reactContext, ret);
        return ret;
    }

    @Override
    public void onDropViewInstance(MapView view) {
        BDMapExtraData.getExtraData(view).onDropViewInstance();
    }

    @ReactProp(name="showsUserLocation")
    public void setEnableMyLocation(MapView view, boolean enabled) {
        view.getMap().setMyLocationEnabled(enabled);
    }

    @ReactProp(name="region")
    public void setRegion(MapView view, ReadableMap region) {
        if (region == null) {
            return;
        }
        double longitude = region.getDouble("longitude");
        double latitude = region.getDouble("latitude");
        double longitudeDelta = region.hasKey("longitudeDelta") ? region.getDouble("longitudeDelta") / 2 : 0;
        double latitudeDelta = region.hasKey("latitudeDelta") ? region.getDouble("latitudeDelta") / 2 : 0;

        view.getMap().animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                .include(new LatLng(latitude - latitudeDelta, longitude - longitudeDelta))
                .include(new LatLng(latitude + latitudeDelta, longitude + longitudeDelta))
                .build()));
    }

    @ReactProp(name="traceData")
    public void setTraceData(MapView view, ReadableArray arr) {
        BDMapExtraData.getExtraData(view).setTraceData(arr);
    }

    @ReactProp(name="annotations")
    public void setAnnotations(MapView view, ReadableArray arr) {
        BDMapExtraData.getExtraData(view).setAnnotations(arr);
    }
}
