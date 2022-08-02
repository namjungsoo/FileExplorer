package com.duongame.helper

import android.os.Environment

/**
 * Created by namjungsoo on 2017-01-22.
 */
object ExtSdCardHelper {
    @JvmStatic
    val externalSdCardPath: String?
        get() {
            val root = Environment.getExternalStorageDirectory() ?: return null
            val parent = root.parentFile ?: return null
            val storage = parent.parentFile ?: return null
            val files = storage.listFiles() ?: return null
            for (file in files) {
                val path = file.name
                if (path == "emulated") continue
                if (path == "self") continue
                return file.absolutePath
            }
            return null
        }
}