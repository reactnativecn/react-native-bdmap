package cn.reactnative.baidumap;

import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tdzl2003 on 4/24/16.
 */
public class BDMapExtraData implements LifecycleEventListener, BaiduMap.OnMapLoadedCallback, BaiduMap.OnMapStatusChangeListener, BaiduMap.OnMarkerClickListener {
    private MapView view;
    private ReactContext reactContext;

    private static Map<MapView, BDMapExtraData> extraDataMap = new HashMap<>();

    public BDMapExtraData(ReactContext context, MapView view) {
        this.view = view;
        this.reactContext = context;

        extraDataMap.put(view, this);

        view.getMap().setOnMapLoadedCallback(this);
        view.getMap().setOnMapStatusChangeListener(this);
        view.getMap().setOnMarkerClickListener(this);
        reactContext.addLifecycleEventListener(this);
    }


    public static BDMapExtraData getExtraData(MapView view){
        return extraDataMap.get(view);
    }

    public void onDropViewInstance() {
        reactContext.removeLifecycleEventListener(this);
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                extraDataMap.remove(view);
            }
        });
    }

    /* 应用生命周期跟踪 */

    @Override
    public void onHostResume() {
        view.onResume();
    }

    @Override
    public void onHostPause() {
        view.onPause();
    }

    @Override
    public void onHostDestroy() {
        view.onDestroy();
    }

    /* 地图位置监听 */
    private WritableMap transformMapStatus(MapStatus mapStatus) {
        WritableMap ret = Arguments.createMap();
        LatLng southwest = mapStatus.bound.southwest;
        LatLng northeast = mapStatus.bound.northeast;

        ret.putDouble("latitude", (northeast.latitude + southwest.latitude) / 2);
        ret.putDouble("longitude", (northeast.longitude + southwest.longitude) / 2);
        ret.putDouble("latitudeDelta", (northeast.latitude - southwest.latitude));
        ret.putDouble("longitudeDelta", (northeast.longitude - southwest.longitude));
        return ret;
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                view.getId(),
                "topRegionChange",
                transformMapStatus(mapStatus));
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                view.getId(),
                "topRegionChange",
                transformMapStatus(mapStatus));
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                view.getId(),
                "topRegionChangeComplete",
                transformMapStatus(mapStatus));
    }

    /* 标注物等内容处理 */
    List<View> children = new ArrayList<View>();
    public void addView(View child, int index) {
        children.add(index, child);
        if (child instanceof OverlayFakeView) {
            OverlayFakeView fakeChild = (OverlayFakeView)child;
            fakeChild.setMap(view.getMap());
        } else if (child instanceof InfoWindowFakeView) {
            InfoWindowFakeView fakeChild = (InfoWindowFakeView)child;
            fakeChild.map = view.getMap();
            fakeChild.maybeUpdate();
        }
    }

    public int getChildCount() {
        return children.size();
    }

    public View getChildAt(int index) {
        return children.get(index);
    }

    public void removeViewAt(int index) {
        View child = children.remove(index);
        if (child instanceof OverlayFakeView) {
            OverlayFakeView fakeChild = (OverlayFakeView)child;
            fakeChild.setMap(null);
//            fakeChild.overlay.remove();
        } else if (child instanceof InfoWindowFakeView) {
            InfoWindowFakeView fakeChild = (InfoWindowFakeView)child;
            fakeChild.destroy();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MarkerFakeView view = (MarkerFakeView)OverlayFakeView.getFakeView(marker);
        if (view != null) {
            view.onMarkerClick();
        }
        return true;
    }

    @Override
    public void onMapLoaded() {
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                view.getId(),
                "topLoad",
                null);
    }

    private Overlay trace;
    public void setTraceData(ReadableArray traceData) {
        if (trace != null) {
            trace.remove();
            trace = null;
        }
        if (traceData != null && traceData.size() >= 2) {
            List<LatLng> pts = new ArrayList<LatLng>();
            for (int i = 0; i < traceData.size(); i++){
                ReadableArray item = traceData.getArray(i);
                pts.add(new LatLng(item.getDouble(1), item.getDouble(0)));
            }
            trace = view.getMap().addOverlay(new PolylineOptions().points(pts).color(0xffff00ff).width(5));
        }
    }
}
