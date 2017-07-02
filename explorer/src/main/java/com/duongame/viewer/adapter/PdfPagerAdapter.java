package com.duongame.viewer.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.duongame.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.viewer.activity.PagerActivity;
import com.duongame.viewer.listener.PagerOnTouchListener;

/**
 * Created by namjungsoo on 2016-12-25.
 */

public class PdfPagerAdapter extends ViewerPagerAdapter {
    private final static String TAG = "PdfPagerAdapter";

    private PdfRenderer renderer;
//    private Bitmap bitmap;
    PagerOnTouchListener mPagerOnTouchListener;

    public PdfPagerAdapter(PagerActivity context) {
        super(context);
        mPagerOnTouchListener = new PagerOnTouchListener(context);
    }

    public void setRenderer(PdfRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
//        Log.d(TAG, "instantiateItem position=" + position);
        final ViewGroup rootView = (ViewGroup) context.getLayoutInflater().inflate(R.layout.viewer_page, container, false);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

        container.addView(rootView);

        final int width = container.getWidth();
        final int height = container.getHeight();

        if (width == 0 || height == 0) {
            container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final int width = container.getWidth();
                    final int height = container.getHeight();
//                    Log.d(TAG, "onGlobalLayout width=" + width + " height=" + height);

                    loadPage(position, imageView, width, height);
                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            loadPage(position, imageView, width, height);
        }

        return rootView;
    }

    private void loadPage(int position, ImageView imageView, int width, int height) {
//        Log.d(TAG, "loadPage position=" + position);

        if (imageView != null) {
            final ExplorerItem item = imageList.get(position);
            if (item == null)
                return;

//            Bitmap bitmap = BitmapCacheManager.getBitmap(item.path);
//            if (bitmap != null) {
//                imageView.setImageBitmap(bitmap);
//                imageView.setBackgroundColor(Color.WHITE);
//                Log.w(TAG, "loadPage getBitmap ok");
//                return;
//            }

            final Bitmap bitmap;
            if (renderer != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final PdfRenderer.Page page = renderer.openPage(position);

                    // 종횡비를 계산하자.
                    float pdfRatio = (float) page.getHeight() / (float) page.getWidth();
                    float screenRatio = (float) height / (float) width;
                    int newWidth, newHeight;

                    if (pdfRatio > screenRatio) {// pdf가 더 길쭉 하면, screen의 height에 맞춘다.
                        newWidth = (int) (height / pdfRatio);
                        newHeight = height;
                    } else {// screen이 더 길쭉하면 screen의 width에 맞춘다.
                        newWidth = width;
                        newHeight = (int) (width * pdfRatio);
                    }

                    bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();

                    imageView.setImageBitmap(bitmap);

                    // 무조건 해주지 않으면 안된다. 알파로 처리되어 있기 때문이다. (RENDER_MODE_FOR_DISPLAY)
                    imageView.setBackgroundColor(Color.WHITE);

                    imageView.setOnTouchListener(mPagerOnTouchListener);
                    //imageView.setBackgroundColor(context.getResources().getColor(R.color.colorGreyBackground));

//                    final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams )imageView.getLayoutParams();
//                    params.width = newWidth;
//                    params.height = newHeight;
//                    params.gravity = Gravity.CENTER;
//
//                    imageView.setLayoutParams(params);
//                    imageView.requestLayout();

//                    BitmapCacheManager.setBitmap(item.path, bitmap);
                }
            }
        } else {
            Log.e(TAG, "imageView is null");
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);

        final ExplorerItem item = imageList.get(position);
        if (item == null)
            return;

//        Log.w(TAG, "destroyItem position="+position + " path=" + item.path);
        final ViewGroup rootView = (ViewGroup) object;
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

        //TODO: 이부분 살펴봐야함
        final Drawable d = imageView.getDrawable();
        if(d instanceof BitmapDrawable) {
            Bitmap b = ((BitmapDrawable)d).getBitmap();
            b.recycle();
//            Log.w(TAG, "recycle");
        }

        imageView.setImageBitmap(null);
//        BitmapCacheManager.removeBitmap(item.path);
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
//        Log.d(TAG, "setPrimaryItem position=" + position);
    }

    @Override
    public void stopAllTasks() {
        // 아무것도 안함
        // task를 사용안함
    }
}
