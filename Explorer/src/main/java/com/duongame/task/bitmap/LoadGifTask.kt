package com.duongame.task.bitmap

import android.os.AsyncTask
import java.io.*

/**
 * Created by namjungsoo on 2017. 1. 28..
 */
// AsyncTask:
//  parameter
//  progress
//  result
class LoadGifTask(private val listener: LoadGifListener?) : AsyncTask<String?, Int?, Void?>() {
    interface LoadGifListener {
        fun onSuccess(data: ByteArray?)
        fun onFail()
    }

    private lateinit var data: ByteArray
    protected override fun doInBackground(vararg params: String?): Void? {
        val path = params[0]
        try {
            val file = File(path)
            data = ByteArray(file.length().toInt())
            val fis = FileInputStream(file)
            val dis = DataInputStream(fis)
            dis.readFully(data)
            dis.close()
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            listener?.onFail()
        } catch (e: IOException) {
            e.printStackTrace()
            listener?.onFail()
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        listener?.onSuccess(data)
    }

    companion object {
        @Throws(IOException::class)
        fun loadGif(path: String?): ByteArray {
            val file = File(path)
            val data = ByteArray(file.length().toInt())
            val fis = FileInputStream(file)
            val dis = DataInputStream(fis)
            dis.readFully(data)
            dis.close()
            fis.close()
            return data
        }
    }
}