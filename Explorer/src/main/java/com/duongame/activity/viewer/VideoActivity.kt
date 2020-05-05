package com.duongame.activity.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.duongame.R
import com.duongame.adapter.ExplorerItem

class VideoActivity : BaseViewerActivity() {
    fun getLocalIntent(context: Context?, item: ExplorerItem): Intent? {
        val intent = Intent(context, PhotoActivity::class.java)
        // 풀패스에서 폴더만 떼옴
        intent.putExtra("path", item.path.substring(0, item.path.lastIndexOf('/')))
        intent.putExtra("name", item.name)
        intent.putExtra("size", item.size)
        return intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        contentViewResId = R.layout.activity_video

        super.onCreate(savedInstanceState)
    }
}