package cn.reactnative.baidumap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
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
public class MarkerFakeView extends OverlayFakeView{
    public MarkerFakeView(Context context) {
        super(context);
        options = new MarkerOptions();
    }

    MarkerOptions getOptions() {
        return (MarkerOptions)options;
    }

    Marker getMarker() {
        return (Marker)overlay;
    }

    private boolean shouldDisplay = false;
    @Override
    public void display() {
        if (this.getOptions().getIcon() != null) {
            super.display();
        } else {
            shouldDisplay = true;
        }
    }

    private String lastIcon;

    public void setIcon(final String imageUrl, MarkerManager manager) {
        Uri uri = null;

        uri = Uri.parse(imageUrl);
        // Verify scheme is set, so that relative uri (used by static resources) are not handled.
        if (uri.getScheme() == null) {
            uri = getResourceDrawableUri(getContext(), imageUrl);
        }

        lastIcon = imageUrl;

        this._getImage(uri, null, new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                BitmapDescriptor bmp = BitmapDescriptorFactory.fromBitmap(bitmap);
                getOptions().icon(bmp);
                if (overlay != null) {
                    getMarker().setIcon(bmp);
                }
                if (shouldDisplay) {
                    display();
                    shouldDisplay = false;
                }
            }
        });
    }

    public void onMarkerClick() {
        ReactContext reactContext = (ReactContext)this.getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                this.getId(),
                "topPress",
                null);
    }

    public void setLocation(LatLng position) {
        getOptions().position(position);

        if (overlay != null) {
            getMarker().setPosition(position);
        }
    }

    public void setTitle(String title) {
        getOptions().title(title);
        if (overlay != null) {
            getMarker().setTitle(title);
        }
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

    private void _getImage(Uri uri, ResizeOptions resizeOptions, final ImageCallback imageCallback) {
        BaseBitmapDataSubscriber dataSubscriber = new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                bitmap = bitmap.copy(bitmap.getConfig(), true);
                imageCallback.invoke(bitmap);
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                imageCallback.invoke(null);
            }
        };

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (resizeOptions != null) {
            builder = builder.setResizeOptions(resizeOptions);
        }
        ImageRequest imageRequest = builder.build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    private static Uri getResourceDrawableUri(Context context, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        name = name.toLowerCase().replace("-", "_");
        int resId = context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());

        if (resId == 0) {
            return null;
        } else {
            return new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
        }
    }
}
