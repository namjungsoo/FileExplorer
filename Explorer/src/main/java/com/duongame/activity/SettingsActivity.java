package com.duongame.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.duongame.AnalyticsApplication;
import com.duongame.BuildConfig;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookDB;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.manager.AdBannerManager;

import java.io.File;


// 셋팅을 전부 관장하는 액티비티이다.
//
// 앞으로 해야할일
// 1. 타이틀바 텍스트
// 2. 하단 광고

// 필요한 설정 내용
// 1. 좌우 기본값
// 2. 텍스트 폰트 변경
// 3. 캐쉬 초기화
// 4. 캐쉬 위치(SD카드)

// 5. 패스 워드
// 6. ZIP 파일 인코딩
// 7. 이미지 프로세싱
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();
        initToolbar();

        initUI();
    }

    void clearHistory() {
        BookDB.clearBooks(SettingsActivity.this);

        ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_history));
    }

    void clearCache() {
        BitmapCacheManager.removeAllThumbnails();
        BitmapCacheManager.removeAllPages();
        BitmapCacheManager.removeAllBitmaps();
        BitmapCacheManager.removeAllDrawables();

        clearHistory();

        final File file = getFilesDir();
        deleteRecursive(file);

        ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_cache));
    }

    void showLicense() {
        if (BuildConfig.SHOW_AD) {
            AlertHelper.showAlertWithAd(this,
                    AppHelper.getAppName(this),
                    "Icon license: designed by Smashicons from Flaticon",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, null, true);
            AdBannerManager.initPopupAd(this);// 항상 초기화 해주어야 함
        } else {
            AlertHelper.showAlert(this,
                    AppHelper.getAppName(this),
                    "Icon license: designed by Smashicons from Flaticon",
                    null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, null, true);

        }
    }

    void initUI() {
        Switch nightMode = findViewById(R.id.night_mode);
        Switch thumbnail = findViewById(R.id.thumbnail);
        Switch japaneseDirection = findViewById(R.id.japanese_direction);

        nightMode.setChecked(application.isNightMode());
        thumbnail.setChecked(application.isThumbnail());
        japaneseDirection.setChecked(application.isJapaneseDirection());
        
        // viewer
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalyticsApplication application = (AnalyticsApplication) getApplication();
                if (application != null) {
                    application.setNightMode(isChecked);
                }
            }
        });

        thumbnail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalyticsApplication application = (AnalyticsApplication) getApplication();
                if (application != null) {
                    if (isChecked) {
                        clearCache();
                    }
                    application.setThumbnail(isChecked);
                }
            }
        });

        japaneseDirection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalyticsApplication application = (AnalyticsApplication) getApplication();
                if (application != null) {
                    application.setJapaneseDirection(isChecked);
                }
            }
        });

        // history
        Button cache = findViewById(R.id.action_clear_cache);
        cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCache();
            }
        });

        Button history = findViewById(R.id.action_clear_history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        // system
        Button license = findViewById(R.id.action_license);
        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 라이센스 팝업 띄우기
                showLicense();
            }
        });

        TextView version = findViewById(R.id.version);
        version.setText("v" + BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE);
    }

    public static Intent getLocalIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private void initToolbar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initContentView() {
        setContentView(R.layout.activity_settings);
    }
}
