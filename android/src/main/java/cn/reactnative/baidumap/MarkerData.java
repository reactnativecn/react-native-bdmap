package cn.reactnative.baidumap;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
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

/**
 * Created by tdzl2003 on 11/9/16.
 */
public class MarkerData {
    private MarkerOptions options = new MarkerOptions();
    private Marker marker;

    private String iconUrl;

    public MarkerData(BitmapDescriptor defaultIcon) {
        options.icon(defaultIcon);
    }

    public void setLocation(LatLng position) {
        options.position(position);
        if (marker != null){
            marker.setPosition(position);
        }
    }

    public void createMarker(MapView view) {
        if (this.marker == null) {
            marker = (Marker) view.getMap().addOverlay(options);
        }
    }

    public void destroyMarker() {
        if (this.marker != null) {
            marker.remove();
            marker = null;
        }
    }

    public void setIcon(Context context, final String iconUrl, BitmapDescriptor defaultIcon) {
        if (iconUrl != null &&
                !iconUrl.equals(this.iconUrl)) {
            this.iconUrl = iconUrl;
            Uri uri = null;

            uri = Uri.parse(iconUrl);
            // Verify scheme is set, so that relative uri (used by static resources) are not handled.
            if (uri.getScheme() == null) {
                uri = getResourceDrawableUri(context, iconUrl);
            }

            this._getImage(uri, null, new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap bitmap) {
                    if (!iconUrl.equals(MarkerData.this.iconUrl)) {
                        return;
                    }
                    BitmapDescriptor bmp = BitmapDescriptorFactory.fromBitmap(bitmap);
                    options.icon(bmp);
                    if (marker != null) {
                        marker.setIcon(bmp);
                    }
                }
            });
        } else if (this.iconUrl != null && iconUrl == null){
            this.iconUrl = null;
            options.icon(defaultIcon);
            if (marker != null) {
                marker.setIcon(defaultIcon);
            }
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
