package com.duongame.archive

import java.util.*

/**
 * Created by namjungsoo on 2018-01-23.
 */
interface ArchiveFile {
    val headers: ArrayList<ArchiveHeader>?
    fun extractFile(fileName: String, destPath: String): Boolean
    fun extractAll(destPath: String): Boolean
    fun destroy()
    val isEncryped: Boolean
    fun setPassword(password: String)
    fun setFileNameCharset(charset: String)
}