package com.duongame.archive

import com.hzy.lib7z.Z7Extractor
import java.util.*

/**
 * Created by namjungsoo on 2018-01-23.
 */
class SevenFile(z7Path: String?) : ArchiveFile {
    var z7Extractor: Z7Extractor = Z7Extractor(z7Path)
    override val headers: ArrayList<ArchiveHeader>?
        get() {
            val headers = z7Extractor.headers ?: return null
            val newHeaders = ArrayList<ArchiveHeader>()
            for (header in headers) {
                newHeaders.add(ArchiveHeader(header.fileName, header.size))
            }
            return newHeaders
        }

    override fun extractFile(fileName: String, destPath: String): Boolean {
        return z7Extractor.extractFile(fileName, destPath, null)
    }

    override fun extractAll(destPath: String): Boolean {
        return z7Extractor.extractAll(destPath, null)
    }

    override fun destroy() {
        z7Extractor.destroy()
    }

    override val isEncryped: Boolean
        get() = false

    override fun setPassword(password: String) {}
    override fun setFileNameCharset(charset: String) {}

}