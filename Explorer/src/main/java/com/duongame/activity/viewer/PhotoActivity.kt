package com.duongame.activity.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.duongame.App.Companion.instance
import com.duongame.activity.viewer.PhotoActivity
import com.duongame.adapter.ExplorerItem
import com.duongame.adapter.PhotoPagerAdapter
import com.duongame.adapter.ViewerPagerAdapter
import com.duongame.file.FileHelper.getMinimizedSize

/**
 * Created by namjungsoo on 2016-11-18.
 */
class PhotoActivity : PagerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPagerAdapter()
        processIntent()
    }

    override fun createPagerAdapter(): ViewerPagerAdapter? {
        // 이때는 애니메이션을 한다.
        // 그런데 애니메이션이 없으면 안해야 한다.
        return PhotoPagerAdapter(this,true)
    }

    private fun initPagerAdapter() {
        try {
            val imageList = instance.imageList ?: return
            pagerAdapter?.imageList = imageList
            pager?.adapter = pagerAdapter
            seekPage?.max = imageList.size
        } catch (e: NullPointerException) {
        }
    }

    protected fun processIntent() {
        try {
            val imageList = instance.imageList ?: return
            val intent = intent
            val extras = intent.extras
            if (extras != null) {
                name = extras.getString("name").toString()
                path = extras.getString("path").toString()
                size = extras.getLong("size")
                textName?.text = name

                //FIX:
                // 이미지 리스트가 없거나 사이즈가 0이라면 리턴한다.
                if (imageList.size == 0) {
                    finish()
                }

                // 이미지 파일 리스트에서 현재 위치를 찾자
                var current = 0
                for (i in imageList.indices) {
                    if (imageList[i].name == name) {
                        current = i
                        break
                    }
                }
                pager?.currentItem = current
                updateName(current)
                updateInfo(current)
                updateScrollInfo(current)
            }
        } catch (e: NullPointerException) {
        }
    }

    override fun updateInfo(position: Int) {
        if (pager?.currentItem == position) {
            val item = pagerAdapter?.imageList?.get(position) ?: return
            textInfo?.text = "" + item.width + " x " + item.height
            textSize?.text = getMinimizedSize(item.size)
        }
    }

    companion object {
        private const val TAG = "PhotoActivity"
        @JvmStatic
        fun getLocalIntent(context: Context?, item: ExplorerItem): Intent {
            val intent = Intent(context, PhotoActivity::class.java)
            // 풀패스에서 폴더만 떼옴
            intent.putExtra("path", item.path.substring(0, item.path.lastIndexOf('/')))
            intent.putExtra("name", item.name)
            intent.putExtra("size", item.size)
            return intent
        }
    }
}