package com.duongame.archive

import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.FileHeader
import java.util.*

/**
 * Created by namjungsoo on 2018-01-28.
 */
class Zip4jFile(zipPath: String) : ArchiveFile {
    var file: ZipFile = ZipFile(zipPath)

    override val isEncryped: Boolean = file.isEncrypted

    override fun setPassword(password: String) {
        file.setPassword(password)
    }

    override fun setFileNameCharset(charset: String) {
        file.setFileNameCharset(charset)
    }

    override val headers: ArrayList<ArchiveHeader>?
        get() {
            try {
            val headers = file.fileHeaders as List<FileHeader>? ?: return null
            val newHeaders = ArrayList<ArchiveHeader>()
            for (header in headers) {
                newHeaders.add(ArchiveHeader(header.fileName, header.uncompressedSize))
            }
            return newHeaders
        } catch (e: ZipException) {
            e.printStackTrace()
            return null
        }
    }

    override fun extractFile(fileName: String, destPath: String): Boolean {
        try {
            file.extractFile(fileName, destPath)
            return true
        } catch (e: ZipException) {
            e.printStackTrace()
        }
        return false
    }

    override fun extractAll(destPath: String): Boolean {
        try {
            file.extractAll(destPath)
            return true
        } catch (e: ZipException) {
            e.printStackTrace()
        }
        return false
    }

    override fun destroy() {}
}