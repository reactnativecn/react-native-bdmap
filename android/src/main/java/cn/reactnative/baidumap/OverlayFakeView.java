package cn.reactnative.baidumap;

import android.content.Context;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tdzl2003 on 4/24/16.
 */
public class OverlayFakeView extends View{
    OverlayOptions options;
    Overlay overlay = null;
    BaiduMap map = null;

    public OverlayFakeView(Context context) {
        super(context);
    }

    static Map<Overlay, OverlayFakeView> fakeViewMap = new HashMap<>();

    public void setMap(BaiduMap map) {
        this.map = map;

        // 这里不添加marker,等command再添加.
        if (map == null && this.overlay != null) {
            this.destroyOverlay();
        }
    }

    public void display() {
        if (map != null && this.overlay == null) {
            this.createOverlay();
        }
    }

    protected void createOverlay() {
        this.overlay = map.addOverlay(this.options);
        fakeViewMap.put(overlay, this);
    }

    protected void destroyOverlay() {
        fakeViewMap.remove(overlay);
        overlay.remove();
        overlay = null;
    }

    public static OverlayFakeView getFakeView(Overlay overlay) {
        return fakeViewMap.get(overlay);
    }

}
