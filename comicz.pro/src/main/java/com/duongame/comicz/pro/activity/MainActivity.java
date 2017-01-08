package com.duongame.comicz.pro.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.duongame.comicz.adapter.ComicPagerAdapter;
import com.duongame.comicz.db.BookDB;
import com.duongame.comicz.pro.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.helper.PositionManager;
import com.duongame.explorer.helper.PreferenceHelper;
import com.duongame.explorer.helper.ShortcutManager;
import com.duongame.explorer.helper.ToastHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static int PERMISSION_STORAGE = 1;

    private ViewPager pager;
    private ComicPagerAdapter adapter;
    private TabLayout tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ShortcutManager.checkShortcut(this);
        initTabs();

//        getSupportActionBar().setHideOnContentScrollEnabled(true);
        getSupportActionBar().setElevation(0.0f);
    }

    private void initTabs() {
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new ComicPagerAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(adapter);
        tab = (TabLayout) findViewById(R.id.tab);
        tab.setupWithViewPager(pager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (checkStoragePermissions()) {
            final String lastPath = PreferenceHelper.getLastPath(MainActivity.this);
            final int position = PreferenceHelper.getLastPosition(MainActivity.this);
            final int top = PreferenceHelper.getLastTop(MainActivity.this);

            Log.d(TAG, "onCreate path=" + lastPath + " position=" + position + " top=" + top);

            PositionManager.setPosition(lastPath, position);
            PositionManager.setTop(lastPath, top);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String read = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String write = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        boolean readEnable = false;
        boolean writeEnable = false;

        for (int i = 0; i < permissions.length; i++) {
            if (read.equals(permissions[i]) && grantResults[i] == 0)
                readEnable = true;
            if (write.equals(permissions[i]) && grantResults[i] == 0)
                writeEnable = true;
        }

        if (readEnable && writeEnable) {
            // 최초 이므로 무조건 null
        }
    }

    @Override
    public void onBackPressed() {
        final int position = pager.getCurrentItem();
        final BaseFragment fragment = (BaseFragment) adapter.getItem(position);
        if (fragment != null) {
            fragment.onBackPressed();
        }
    }

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_clear_cache) {
            clearHistory();
            clearCache();

            ToastHelper.showToast(this, "캐쉬 파일을 삭제하였습니다.");
        }

        if (id == R.id.action_clear_history) {

            ToastHelper.showToast(this, "최근파일 목록을 삭제하였습니다.");
        }

        return super.onOptionsItemSelected(item);
    }

    void clearCache() {
        BitmapCacheManager.recycleThumbnail();
        BitmapCacheManager.recyclePage();
        BitmapCacheManager.recycleBitmap();

        final File file = getFilesDir();
        deleteRecursive(file);

        final BaseFragment fragment = (BaseFragment) adapter.getItem(0);
        fragment.refresh();
    }

    void clearHistory() {
        BookDB.clearBooks(this);

        final BaseFragment fragment = (BaseFragment) adapter.getItem(1);
        fragment.refresh();
    }

    void deleteRecursive(File fileOrDirectory) {
        if(fileOrDirectory.getAbsolutePath().endsWith("instant-run"))
            return;

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
