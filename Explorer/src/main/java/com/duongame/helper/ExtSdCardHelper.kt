package com.duongame.helper

import android.os.Environment

/**
 * Created by namjungsoo on 2017-01-22.
 */
object ExtSdCardHelper {
    val externalSdCardPath: String
        get() {
            val root = Environment.getExternalStorageDirectory() ?: return ""
            val parent = root.parentFile ?: return ""
            val storage = parent.parentFile ?: return ""
            val files = storage.listFiles() ?: return ""
            for (file in files) {
                val path = file.name
                if (path == "emulated") continue
                if (path == "self") continue
                return file.absolutePath
            }
            return ""
        }
}