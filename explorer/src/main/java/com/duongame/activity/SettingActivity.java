package com.duongame.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.duongame.AnalyticsApplication;
import com.duongame.R;


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
public class SettingActivity extends BaseActivity {
    Switch nightMode;
    Switch thumbnail;
    Switch japaneseDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();
        initToolbar();

        initUI();
    }

    void initUI() {
        nightMode = findViewById(R.id.night_mode);
        thumbnail = findViewById(R.id.thumbnail);
        japaneseDirection = findViewById(R.id.japanese_direction);

        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalyticsApplication application = (AnalyticsApplication)getApplication();
                if(application != null) {
                    application.setNightMode(isChecked);
                }
            }
        });

        thumbnail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalyticsApplication application = (AnalyticsApplication)getApplication();
                if(application != null) {
                    application.setThumbnail(isChecked);
                }
            }
        });

        japaneseDirection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalyticsApplication application = (AnalyticsApplication)getApplication();
                if(application != null) {
                    application.setJapaneseDirection(isChecked);
                }
            }
        });

    }

    public static Intent getLocalIntent(Context context) {
        final Intent intent = new Intent(context, SettingActivity.class);
        return intent;
    }

    private void initToolbar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initContentView() {
        setContentView(R.layout.activity_setting);
    }
}
