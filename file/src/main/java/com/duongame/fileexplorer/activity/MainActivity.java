package com.duongame.fileexplorer.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.duongame.comicz.db.BookDB;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.fragment.BaseFragment;
import com.duongame.explorer.helper.ToastHelper;
import com.duongame.explorer.manager.ExplorerManager;
import com.duongame.fileexplorer.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        if (!ExplorerManager.isInitialPath()) {
            final BaseFragment fragment = (BaseFragment)getSupportFragmentManager().getFragments().get(0);
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
        BitmapCache.recycleThumbnail();
        BitmapCache.recyclePage();
        BitmapCache.recycleBitmap();

        final File file = getFilesDir();
        deleteRecursive(file);

        final BaseFragment fragment = (BaseFragment)getSupportFragmentManager().getFragments().get(0);
        fragment.onRefresh();
    }

    void clearHistory() {
        BookDB.clearBooks(this);

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
