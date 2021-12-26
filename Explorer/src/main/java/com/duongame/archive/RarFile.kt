package com.duongame.archive

import java.util.*

/**
 * Created by namjungsoo on 2018-01-23.
 */
class RarFile(rarPath: String?) : ArchiveFile {
    var rar: Unrar = Unrar(rarPath)
    override val headers: ArrayList<ArchiveHeader>?
        get() {
            val headers = rar.headers ?: return null
            val newHeaders = ArrayList<ArchiveHeader>()
            for (header in headers) {
                newHeaders.add(ArchiveHeader(header.fileName, header.size))
            }
            return newHeaders
        }

    override fun extractFile(fileName: String, destPath: String): Boolean {
        return rar.extractFile(fileName, destPath, null)
    }

    override fun extractAll(destPath: String): Boolean {
        return rar.extractAll(destPath, null)
    }

    override fun destroy() {}
    override val isEncryped: Boolean
        get() = false

    override fun setPassword(password: String) {}
    override fun setFileNameCharset(charset: String) {}

}