package com.duongame.explorer.adapter;

import android.app.Activity;
import android.graphics.pdf.PdfRenderer;

/**
 * Created by namjungsoo on 2016-12-25.
 */

public class PdfPagerAdapter extends ExplorerPagerAdapter {
    private Activity context;
    private PdfRenderer renderer;

    public PdfPagerAdapter(Activity context) {
        super(context);
    }

    public void setRenderer(PdfRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void stopAllTasks() {

    }
}
