package com.duongame.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.duongame.R;
import com.duongame.helper.AppHelper;

/**
 * Created by namjungsoo on 2017-12-23.
 */

public class SortDialog extends DialogFragment {
    private static String TAG = SortDialog.class.getSimpleName();
    int sortType;
    int sortDir;

    public interface OnSortListener {
        void onSort(int type, int dir);
    }

    OnSortListener onSortListener;

    public void setTypeAndDirection(int sortType, int sortDir) {
        this.sortType = sortType;
        this.sortDir = sortDir;
    }

    public void setOnSortListener(OnSortListener onSortListener) {
        this.onSortListener = onSortListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        Activity activity = getActivity();
        if(activity == null)
            return null;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_sort, null, false);
        initUI(view);

        builder.setTitle(AppHelper.getAppName(activity))
                .setIcon(AppHelper.getIconResId(activity))
                .setMessage(R.string.msg_file_sort)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인이므로 pref에 저장하고 소팅을 새로 해줌
//                            Timber.e("ok");
                        if (onSortListener != null) {
                            onSortListener.onSort(sortType, sortDir);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                            Timber.e("cancel");
                    }
                });

        return builder.create();
    }

    // pref에서 초기값을 읽어서 셋팅한다.
    // 값이 변했을 경우 적용해준다.
    void initUI(View view) {
        RadioGroup type = view.findViewById(R.id.sort_type);
        RadioButton[] types = new RadioButton[4];
        types[0] = view.findViewById(R.id.sort_name);
        types[1] = view.findViewById(R.id.sort_ext);
        types[2] = view.findViewById(R.id.sort_date);
        types[3] = view.findViewById(R.id.sort_size);

        RadioGroup direction = view.findViewById(R.id.sort_direction);
        RadioButton[] dirs = new RadioButton[2];
        dirs[0] = view.findViewById(R.id.sort_accending);
        dirs[1] = view.findViewById(R.id.sort_descending);

        types[sortType].setChecked(true);
        dirs[sortDir].setChecked(true);

        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sort_name:
                        sortType = 0;
                        break;
                    case R.id.sort_ext:
                        sortType = 1;
                        break;
                    case R.id.sort_date:
                        sortType = 2;
                        break;
                    case R.id.sort_size:
                        sortType = 3;
                        break;
                }
            }
        });

        direction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sort_accending:
                        sortDir = 0;
                        break;
                    case R.id.sort_descending:
                        sortDir = 1;
                        break;
                }
            }
        });
    }
}
