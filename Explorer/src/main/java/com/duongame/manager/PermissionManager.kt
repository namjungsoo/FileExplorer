package com.duongame.manager

import android.Manifest
import com.duongame.manager.PermissionManager
import android.app.Activity
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Created by js296 on 2017-07-09.
 */
object PermissionManager {
    private const val PERMISSION_STORAGE = 1
    const val PERMISSION_CONTACTS = 2
    var isStoragePermissions = false
    var isContactsPermissions = false

    fun checkContactsPermission(context: Activity?): Boolean {
        if (isContactsPermissions) return true
        if (context == null) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // get accounts 권한이 없으면 요청하자
            if (context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(
                    arrayOf(Manifest.permission.GET_ACCOUNTS),
                    PERMISSION_CONTACTS
                )
                isContactsPermissions = false
                return false
            } else {
                isContactsPermissions = true
            }
        } else {
            isContactsPermissions = true
        }
        return true
    }

    fun checkStoragePermissions(context: Activity?): Boolean {
        if (isStoragePermissions) return true
        if (context == null) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_STORAGE
                )
                isStoragePermissions = false
                return false
            } else {
                isStoragePermissions = true
            }
        } else {
            isStoragePermissions = true
        }
        return true
    }

    fun onRequestStoragePermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val read = Manifest.permission.READ_EXTERNAL_STORAGE
        val write = Manifest.permission.WRITE_EXTERNAL_STORAGE
        var readEnable = false
        var writeEnable = false
        for (i in permissions.indices) {
            if (read == permissions[i] && grantResults[i] == 0) readEnable = true
            if (write == permissions[i] && grantResults[i] == 0) writeEnable = true
        }
        if (readEnable && writeEnable) {
            // 최초 이므로 무조건 null
            isStoragePermissions = true
        }
    }

    fun onRequestContactsPermissionsResult(
        requestCode: Int,
        permissions: Array<String>?,
        grantResults: IntArray?
    ) {
        if (requestCode != PERMISSION_CONTACTS) return
        if (permissions == null || permissions.size == 0) return
        if (grantResults == null || grantResults.size == 0) return
        if (Manifest.permission.GET_ACCOUNTS == permissions[0] &&
            grantResults[0] == 0
        ) isContactsPermissions = true
    }
}