package com.duongame.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.duongame.App;
import com.duongame.R;
import com.duongame.activity.viewer.PagerActivity;
import com.duongame.attacher.ImageViewAttacher;
import com.duongame.listener.PagerOnTouchListener;

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
        final ViewGroup rootView = (ViewGroup) context.getLayoutInflater().inflate(R.layout.page_viewer, container, false);
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
        if (imageView != null) {
            final ExplorerItem item = imageList.get(position);
            if (item == null)
                return;

            Bitmap bitmap;
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

                    //FIX: PDF OOM
                    while (true) {
                        try {
                            bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
                            break;
                        } catch (OutOfMemoryError e) {
                            newWidth /= 2;
                            newHeight /= 2;
                        }
                    }
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                    imageView.setImageBitmap(bitmap);

                    //TODO: 무조건 해주지 않으면 안된다. 알파로 처리되어 있기 때문이다. (RENDER_MODE_FOR_DISPLAY)
                    try {
                        if (App.getInstance(context).isNightMode()) {
                            imageView.setBackgroundColor(Color.BLACK);
                        } else {
                            imageView.setBackgroundColor(Color.WHITE);
                        }
                    } catch (NullPointerException e) {

                    }

                    updateColorFilter(imageView);

                    // 이미지 확대 축소
                    // 및 matrix을 사용하여 화면 가운데 정렬
                    item.attacher = new ImageViewAttacher(imageView);
                    item.attacher.setActivity(context);
                }
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);

        final ExplorerItem item = imageList.get(position);
        if (item == null)
            return;

        final ViewGroup rootView = (ViewGroup) object;
        final ImageView imageView = rootView.findViewById(R.id.image_viewer);

        imageView.setImageBitmap(null);
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, Object object) {
    }

    @Override
    public void stopAllTasks() {
        // 아무것도 안함
        // task를 사용안함
    }
}
