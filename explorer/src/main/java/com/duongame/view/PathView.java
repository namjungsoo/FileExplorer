package com.duongame.view;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class PathView extends HorizontalScrollView {
    public PathView(Context context) {
        super(context);
    }

    public PathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private String path;

    public interface OnPathClickListener {
        void onClickPath(String path);
    }

    public void setOnPathClickListener() {

    }

    public void setPath(String path) {
        if (TextUtils.isEmpty(path))
            return;

        if (this.path.equals(path))
            return;

        this.path = path;

        updateAllChildViews();
    }

    private void updateAllChildViews() {
        // 모두 삭제한 후에
        removeAllViews();

        // 새로 등록해준다
        String[] paths = path.split("/");

    }

    public String getPath() {
        return path;
    }
}
