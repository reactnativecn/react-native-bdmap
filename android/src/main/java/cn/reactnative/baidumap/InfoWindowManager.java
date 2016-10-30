package cn.reactnative.baidumap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by tdzl2003 on 4/23/16.
 */
public class InfoWindowManager extends ViewGroupManager<InfoWindowFakeView> {
    public InfoWindowManager(){
    }

    @Override
    public String getName() {
        return "RCTBDMapInfoWindow";
    }


    @Override
    protected InfoWindowFakeView createViewInstance(final ThemedReactContext reactContext) {
        final InfoWindowFakeView ret = new InfoWindowFakeView(reactContext);
        return ret;
    }

    @ReactProp(name="location")
    public void setLocation(InfoWindowFakeView view, ReadableMap value)
    {
        double longitude = value.getDouble("longitude");
        double latitude = value.getDouble("latitude");
        LatLng position = new LatLng(latitude, longitude);
        view.position = position;
        view.maybeUpdate();
    }

    @ReactProp(name="yOffset")
    public void setYOffset(InfoWindowFakeView view, int value)
    {
        view.yOffset = value;
        view.maybeUpdate();
    }

    @Override
    public void addView(InfoWindowFakeView parent, View child, int index) {
        parent.children = child;
        parent.maybeUpdate();
    }

    @Override
    public int getChildCount(InfoWindowFakeView parent) {
        return parent.info == null ? 0 : 1;
    }

    @Override
    public View getChildAt(InfoWindowFakeView parent, int index) {
        if (index == 0) {
            return parent.children;
        }
        return null;
    }

    @Override
    public void removeViewAt(InfoWindowFakeView parent, int index) {
        parent.children = null;
        parent.maybeUpdate();
    }

    @Override
    public boolean needsCustomLayoutForChildren() {
        return true;
    }
}
