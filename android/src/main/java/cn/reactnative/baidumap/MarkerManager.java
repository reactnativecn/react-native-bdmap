package cn.reactnative.baidumap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by tdzl2003 on 4/23/16.
 */
public class MarkerManager extends SimpleViewManager<MarkerFakeView> {
    private Map<String, Integer> mResourceDrawableIdMap = new HashMap<>();

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "topPress", MapBuilder.of("registrationName", "onPress")
        );
    }

    public int getResourceDrawableId(Context context, @Nullable String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        name = name.toLowerCase().replace("-", "_");
        if (mResourceDrawableIdMap.containsKey(name)) {
            return mResourceDrawableIdMap.get(name);
        }
        int id = context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());
        mResourceDrawableIdMap.put(name, id);
        return id;
    }

    public @Nullable Bitmap getResourceBitmap(Context context, @Nullable String name) {
        int resId = getResourceDrawableId(context, name);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), resId);
        return icon;
    }

    public MarkerManager(){
    }

    final static int COMMAND_DISPLAY = 1;

    @Override
    public String getName() {
        return "RCTBDMapMarker";
    }

    @Override
    public Map<String,Integer> getCommandsMap() {
        return MapBuilder.of(
                "display",
                COMMAND_DISPLAY
                );
    }

    @Override
    public void receiveCommand(
            MarkerFakeView view,
            int commandType,
            @Nullable ReadableArray args) {
        switch (commandType) {
            case COMMAND_DISPLAY: {
                view.display();
                return;
            }
        }
    }

    @Override
    protected MarkerFakeView createViewInstance(final ThemedReactContext reactContext) {
        final MarkerFakeView ret = new MarkerFakeView(reactContext);
        return ret;
    }

    @ReactProp(name="location")
    public void setLocation(MarkerFakeView view, ReadableMap value)
    {
        double longitude = value.getDouble("longitude");
        double latitude = value.getDouble("latitude");
        LatLng position = new LatLng(latitude, longitude);
        view.setLocation(position);
    }

    @ReactProp(name="image")
    public void setIcon(MarkerFakeView view, String icon)
    {
        view.setIcon(icon, this);
    }

    @ReactProp(name="title")
    public void setTitle(MarkerFakeView view, String title)
    {
        view.setTitle(title);
    }
}
