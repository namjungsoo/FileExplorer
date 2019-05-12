package com.duongame.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.duongame.BuildConfig;
import com.duongame.MainApplication;
import com.duongame.R;
import com.duongame.bitmap.BitmapCacheManager;
import com.duongame.db.BookDB;
import com.duongame.helper.AlertHelper;
import com.duongame.helper.AppHelper;
import com.duongame.helper.JLog;
import com.duongame.helper.PreferenceHelper;
import com.duongame.helper.ToastHelper;
import com.duongame.manager.AdBannerManager;
import com.duongame.manager.AdRewardManager;
import com.google.android.gms.ads.AdView;

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
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();
        initToolbar();

        initUI();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            ViewGroup vg = (ViewGroup) adView.getParent();
            if (vg != null) {
                vg.removeView(adView);
            }
            adView.removeAllViews();
            adView.destroy();
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adView != null) {
            adView.resume();
            // 광고 리워드 제거 시간 중인가?
            if(isAdRemoveReward()) {
                adView.setVisibility(View.GONE);
            } else {
                adView.setVisibility(View.VISIBLE);
            }
        }
    }

    void clearHistory() {
        BookDB.clearBooks(SettingsActivity.this);

        ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_history));
    }

    void clearCache() {
        BitmapCacheManager.removeAllThumbnails();
        BitmapCacheManager.removeAllPages();
        BitmapCacheManager.removeAllBitmaps();

        final File file = getFilesDir();
        deleteRecursive(file);

        ToastHelper.showToast(this, getResources().getString(R.string.msg_clear_cache));
    }

    void showLicense() {
        if (BuildConfig.SHOW_AD && !isAdRemoveReward()) {
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
        if(!AppHelper.isComicz(this)) {
            findViewById(R.id.layout_japanese_direction).setVisibility(View.GONE);
            findViewById(R.id.layout_action_clear_history).setVisibility(View.GONE);
        }

        Button adRemove = findViewById(R.id.action_ad_remove);
        adRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdRewardManager.show(SettingsActivity.this);
            }
        });

        Button donate = findViewById(R.id.action_ad_donate);
        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, DonateActivity.class);
                startActivity(intent);
            }
        });

        Switch nightMode = findViewById(R.id.night_mode);
        Switch thumbnailDisabled = findViewById(R.id.thumbnail_disabled);
        Switch japaneseDirection = findViewById(R.id.japanese_direction);
        Switch pagingAnimationDisabled = findViewById(R.id.paging_animation_disabled);

        // 자동 페이징 시간 설정
        SeekBar autoPagingTime = findViewById(R.id.seek_time);
        final TextView autoPagingTimeValue = findViewById(R.id.auto_paging_time_value);
        int time = PreferenceHelper.getAutoPagingTime(this);
        autoPagingTime.setMax(10);

        autoPagingTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                JLog.e(TAG, "change track " + seekBar.getProgress());
                autoPagingTimeValue.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                JLog.e(TAG, "start track " + seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                JLog.e(TAG, "stop track " + seekBar.getProgress());
                PreferenceHelper.setAutoPagingTime(SettingsActivity.this, seekBar.getProgress());
            }
        });
        autoPagingTimeValue.setText(String.valueOf(time));
        autoPagingTime.setProgress(time);

        try {
            MainApplication application = MainApplication.getInstance(this);
            nightMode.setChecked(application.isNightMode());
            thumbnailDisabled.setChecked(application.isThumbnailDisabled());
            japaneseDirection.setChecked(application.isJapaneseDirection());
            pagingAnimationDisabled.setChecked(application.isPagingAnimationDisabled());
        } catch (NullPointerException e) {

        }

        // viewer
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    MainApplication.getInstance(SettingsActivity.this).setNightMode(isChecked);
                } catch (NullPointerException e) {

                }
            }
        });

        thumbnailDisabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    MainApplication.getInstance(SettingsActivity.this).setThumbnailDisabled(isChecked);
                } catch (NullPointerException e) {

                }
            }
        });

        japaneseDirection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    MainApplication.getInstance(SettingsActivity.this).setJapaneseDirection(isChecked);
                } catch (NullPointerException e) {

                }
            }
        });

        pagingAnimationDisabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    MainApplication.getInstance(SettingsActivity.this).setPagingAnimationDisabled(isChecked);
                } catch (NullPointerException e) {

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

        View view = findViewById(R.id.pro_purchase);
        if (!AppHelper.isPro(this)) {
            view.setVisibility(View.VISIBLE);

            String packageName = getApplicationContext().getPackageName();
            final String proPackageName = packageName.replace(".free", ".pro");

            // pro 구매하기
            // pro version일때는 숨겨야 함
            Button purchase = findViewById(R.id.purchase);
            purchase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppHelper.launchMarket(SettingsActivity.this, proPackageName);
                }
            });
        }
    }

    public static Intent getLocalIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        // 로고 버튼
        actionBar.setDisplayShowHomeEnabled(true);

        // Up 버튼
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initContentView() {
        if (BuildConfig.SHOW_AD) {
            AdBannerManager.initBannerAd(this, 2);

            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View activityView = inflater.inflate(R.layout.activity_settings, null, true);

            final RelativeLayout layout = new RelativeLayout(this);
            layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            adView = AdBannerManager.getAdBannerView(2);

            // adview layout params
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adView.setLayoutParams(params);

            AdBannerManager.requestAd(2);

            // mainview layout params
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ABOVE, adView.getId());
            activityView.setLayoutParams(params);

            layout.addView(adView);
            layout.addView(activityView);

            setContentView(layout);
        } else {
            setContentView(R.layout.activity_settings);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
