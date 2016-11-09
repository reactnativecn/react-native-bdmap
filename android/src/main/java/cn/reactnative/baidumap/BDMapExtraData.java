package cn.reactnative.baidumap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
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
import com.facebook.react.bridge.ReadableMap;
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

    private BitmapDescriptor defaultIcon;

    public BDMapExtraData(ReactContext context, MapView view) {
        this.view = view;
        this.reactContext = context;

        extraDataMap.put(view, this);

        view.getMap().setOnMapLoadedCallback(this);
        view.getMap().setOnMapStatusChangeListener(this);
        view.getMap().setOnMarkerClickListener(this);
        reactContext.addLifecycleEventListener(this);
        defaultIcon = drawableToBitmap(reactContext.getResources().getDrawable(R.drawable.bmap_default_icon));
    }

    static BitmapDescriptor drawableToBitmap(Drawable drawable) // drawable 转换成bitmap
    {
        int width = drawable.getIntrinsicWidth();// 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中

        return BitmapDescriptorFactory.fromBitmap(bitmap);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
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

    private HashMap<String, MarkerData> annotations = new HashMap<String, MarkerData>();

    private void updateMarkerInfo(MarkerData marker, ReadableMap map) {
        double longitude = map.getDouble("longitude");
        double latitude = map.getDouble("latitude");
        LatLng position = new LatLng(latitude, longitude);
        marker.setLocation(position);

        if (map.hasKey("iconUrl")) {
            marker.setIcon(reactContext, map.getString("iconUrl"), defaultIcon);
        }
    }

    public void setAnnotations(ReadableArray newData) {
        HashMap<String, MarkerData> newAnnos = new HashMap<String, MarkerData>();

        for (int i = 0; i < newData.size(); i++) {
            ReadableMap map = newData.getMap(i);
            String key = map.getString("id");

            MarkerData marker = annotations.get(key);


            if (marker == null) {
                marker = new MarkerData(defaultIcon);
                annotations.put(key, marker);
                updateMarkerInfo(marker, map);
                marker.createMarker(view);
            } else {
                updateMarkerInfo(marker, map);
            }

            newAnnos.put(key, marker);
        }

        ArrayList<String> removedKeys = new ArrayList<>();
        for (Map.Entry<String, MarkerData> entry:annotations.entrySet()) {
            if (!newAnnos.containsKey(entry.getKey())) {
                removedKeys.add(entry.getKey());
            }
        }

        for (String key : removedKeys) {
            MarkerData data = annotations.get(key);
            data.destroyMarker();
            annotations.remove(key);
        }
    }
}
