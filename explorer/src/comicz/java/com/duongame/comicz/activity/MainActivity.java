package com.duongame.comicz.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.duongame.R;
import com.duongame.comicz.adapter.ComicPagerAdapter;
import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.helper.ShortcutHelper;
import com.duongame.explorer.helper.ToastHelper;
import com.duongame.explorer.manager.PermissionManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private ViewPager pager;
    private ComicPagerAdapter adapter;
    private TabLayout tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ShortcutHelper.checkShortcut(this);
        initTabs();

        getSupportActionBar().setElevation(0.0f);

        //TEST
//        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestStoragePermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        final int position = pager.getCurrentItem();
        final BaseFragment fragment = (BaseFragment) adapter.getItem(position);
        if (fragment != null) {
            fragment.onBackPressed();
        }
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
            clearHistory();

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
        fragment.onRefresh();
    }

    void clearHistory() {
        BookDB.clearBooks(this);

        final BaseFragment fragment = (BaseFragment) adapter.getItem(1);
        fragment.onRefresh();
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.getAbsolutePath().endsWith("instant-run"))
            return;

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
