package cn.reactnative.baidumap;

import android.app.Activity;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.poi.BaiduMapPoiSearch;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

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

    @ReactMethod
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
                        .getJSModule(RCTNativeAppEventEmitter.class)
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
                .getJSModule(RCTNativeAppEventEmitter.class)
                .emit("BDMapLocation", map);

        if (!isListening){
            mClient.stop();
        }
    }

    @ReactMethod
    public void poiSearch(final ReadableMap loc, final ReadableMap map, final Promise promise) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PoiSearch search = PoiSearch.newInstance();
                PoiNearbySearchOption option = new PoiNearbySearchOption();
                option.location(readLatLng(loc));
                if (map.hasKey("keyword")){
                    option.keyword(map.getString("keyword"));
                }
                if (map.hasKey("sortMode")) {
                    option.sortType("nearToFar".equals(map.getString("sortMode")) ? PoiSortType.distance_from_near_to_far : PoiSortType.comprehensive);
                }
                if (map.hasKey("radius")){
                    option.radius(map.getInt("radius"));
                }
                if (map.hasKey("pageIndex")){
                    option.pageNum(map.getInt("pageIndex"));
                }
                if (map.hasKey("pageCapacity")){
                    option.pageCapacity(map.getInt("pageCapacity"));
                }

                search.searchNearby(option);
                search.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
                    @Override
                    public void onGetPoiResult(PoiResult result) {
                        if (result.error != SearchResult.ERRORNO.NO_ERROR){
                            promise.reject(result.error.toString(), "Search failed");
                            return;
                        }

                        WritableMap map = Arguments.createMap();
                        map.putInt("totalPages", result.getTotalPageNum());
                        map.putInt("totalCount", result.getTotalPoiNum());
                        map.putArray("pois", poiListToArray(result.getAllPoi()));

                        promise.resolve(map);
                    }

                    @Override
                    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

                    }
                });
            }
        });
    }

    private WritableArray poiListToArray(List<PoiInfo> list){
        WritableArray poiList = Arguments.createArray();
        for (PoiInfo p : list){
            WritableMap poi = Arguments.createMap();

            poi.putString("name", p.name);
            poi.putString("uid", p.uid);
            poi.putString("address", p.address);
            poi.putString("city", p.city);
            poi.putString("phoneNum", p.phoneNum);
            poi.putString("postCode", p.postCode);
            poi.putInt("type", p.type != null ? p.type.getInt() : 0);
            //poi.putInt("type", p.type.getInt());


            WritableMap location = Arguments.createMap();
            location.putDouble("latitude", p.location.latitude);
            location.putDouble("longitude", p.location.longitude);
            poi.putMap("location", location);

            poiList.pushMap(poi);
        }
        return poiList;
    }

    @ReactMethod
    public void geoReverse(final ReadableMap map, final Promise promise) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GeoCoder coder = GeoCoder.newInstance();
                ReverseGeoCodeOption option = new ReverseGeoCodeOption();
                option.location(readLatLng(map));
                coder.reverseGeoCode(option);
                coder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                        // Ignore
//                        Log.i("onGetGeoCodeResult",geoCodeResult.toString());
                    }

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                        if (reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                            promise.reject(reverseGeoCodeResult.error.toString(), "Search failed");
                            return;
                        }

                        WritableMap map = Arguments.createMap();
                        map.putString("formattedAddress", reverseGeoCodeResult.getAddress());
                        map.putString("business", reverseGeoCodeResult.getBusinessCircle());

                        ReverseGeoCodeResult.AddressComponent ac = reverseGeoCodeResult.getAddressDetail();
                        WritableMap addrComp = Arguments.createMap();
                        addrComp.putString("city", ac.city);
                        addrComp.putString("streetNumber", ac.streetNumber);
                        addrComp.putString("street", ac.street);
                        addrComp.putString("district", ac.district);
                        addrComp.putString("province", ac.province);
                        map.putMap("addressComponent", addrComp);

                        map.putArray("pois", poiListToArray(reverseGeoCodeResult.getPoiList()));

                        promise.resolve(map);
                    }
                });
            }
        });
    }
}
