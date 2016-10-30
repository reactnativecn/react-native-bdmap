package cn.reactnative.baidumap;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by tdzl2003 on 4/24/16.
 */
public class InfoWindowFakeView extends ViewGroup implements InfoWindow.OnInfoWindowClickListener {
    BaiduMap map;

    View children;
    InfoWindow info;
    LatLng position;
    int yOffset = 0;

    public InfoWindowFakeView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //dummy
    }

    void maybeUpdate()
    {
        if (map != null)
        {
            if (info != null)
            {
                map.hideInfoWindow();
                info = null;
            }
            if (position != null && children != null)
            {
                BitmapDescriptor bmp = fromView(children);
//                info = new InfoWindow(, position, yOffset);
                info = new InfoWindow(bmp, position, yOffset, this);
                map.showInfoWindow(info);
            }
        }
    }

    public static BitmapDescriptor fromView(View var0) {
        if(var0 == null) {
            return null;
        } else {
            var0.measure(MeasureSpec.makeMeasureSpec(1000, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(1000, MeasureSpec.AT_MOST));
            var0.layout(0, 0, var0.getMeasuredWidth(), var0.getMeasuredHeight());
            var0.buildDrawingCache();
            Bitmap var1 = var0.getDrawingCache();
            BitmapDescriptor var2 = BitmapDescriptorFactory.fromBitmap(var1);
            if(var1 != null) {
                var1.recycle();
            }

            var0.destroyDrawingCache();
            return var2;
        }
    }

    void destroy()
    {
        if (info != null)
        {
            map.hideInfoWindow();
            info = null;
        }
        map = null;
    }

    @Override
    public void onInfoWindowClick() {

    }
}
