package com.duongame.ziptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by namjungsoo on 2018-01-26.
 */

/*
AndroidManifest.xml
    </application>

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 */
public class PermissionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionManager.checkStoragePermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestStoragePermissionsResult(requestCode, permissions, grantResults);
    }

}
