package com.duongame.viewer.listener;

import com.duongame.viewer.activity.PagerActivity;

/**
 * Created by namjungsoo on 2017-01-22.
 */

public class PagerOnTouchListener extends BaseOnTouchListener {
    PagerActivity activity;

    public PagerOnTouchListener(PagerActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected boolean handleActionUp() {
        if (!isBeingDragged && activity.getPagerIdle()) {

            // 터치 영역을 확인하여 좌/중/우를 확인하자.
            int width = activity.getPager().getWidth();
            int height = activity.getPager().getHeight();
//                            Log.d(TAG, "width="+width + " height="+height);

            int left = width / 4;
            int right = width * 3 / 4;

            if (lastMotionPt.x < left) {
                int page = activity.getPager().getCurrentItem();
                if (page > 0)
                    activity.getPager().setCurrentItem(page - 1, true);
            } else if (lastMotionPt.x > right) {
                //int total_file = pager.getChildCount();
                int count = activity.getPagerAdapter().getCount();
                int page = activity.getPager().getCurrentItem();
                if (page < count + 1)
                    activity.getPager().setCurrentItem(page + 1, true);
            } else {
                activity.setFullscreen(!activity.getFullscreen());
            }
            return true;
        } else {
            isBeingDragged = false;
        }
        return false;
    }
}
