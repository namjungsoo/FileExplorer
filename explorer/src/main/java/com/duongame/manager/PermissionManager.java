package com.duongame.manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Created by js296 on 2017-07-09.
 */

public class PermissionManager {
    private final static int PERMISSION_STORAGE = 1;
    private static boolean isStoragePermissions = false;

    public static boolean checkStoragePermissions() {
        return isStoragePermissions;
    }

    public static boolean checkStoragePermissions(Activity context) {
        if (isStoragePermissions == true)
            return isStoragePermissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_STORAGE);
                isStoragePermissions = false;
                return false;
            } else {
                isStoragePermissions = true;
            }
        } else {
            isStoragePermissions = true;
        }
        return true;
    }

    public static void onRequestStoragePermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
            isStoragePermissions = true;
        }
    }
}
