package com.duongame.manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * Created by js296 on 2017-07-09.
 */

public class PermissionManager {
    public final static int PERMISSION_STORAGE = 1;
    public final static int PERMISSION_CONTACTS = 2;

    private static boolean isStoragePermissions = false;
    private static boolean isContactsPermissions = false;

    public static boolean checkStoragePermissions() {
        return isStoragePermissions;
    }

    public static boolean checkContactsPermission() {
        return isContactsPermissions;
    }

    public static boolean checkContactsPermission(Activity context) {
        if (isContactsPermissions)
            return true;

        if (context == null)
            return true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // get accounts 권한이 없으면 요청하자
            if (context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        PERMISSION_CONTACTS);
                isContactsPermissions = false;
                return false;
            } else {
                isContactsPermissions = true;
            }
        } else {
            isContactsPermissions = true;
        }
        return true;
    }

    public static boolean checkStoragePermissions(Activity context) {
        if (isStoragePermissions)
            return true;

        if (context == null)
            return true;

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

    public static void onRequestContactsPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode != PERMISSION_CONTACTS)
            return;

        if(permissions == null || permissions.length == 0)
            return;

        if(grantResults == null || grantResults.length == 0)
            return;

        if(Manifest.permission.GET_ACCOUNTS.equals(permissions[0]) &&
                grantResults[0] == 0)
            isContactsPermissions = true;
    }
}
