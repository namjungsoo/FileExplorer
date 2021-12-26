package com.duongame.adapter

import com.duongame.helper.PreferenceHelper.nightMode
import com.duongame.activity.viewer.PagerActivity
import androidx.viewpager.widget.PagerAdapter
import com.duongame.adapter.ExplorerItem
import android.graphics.ColorMatrix
import com.duongame.helper.PreferenceHelper
import android.graphics.ColorMatrixColorFilter
import android.view.View
import android.widget.ImageView
import java.lang.NullPointerException
import java.util.ArrayList

/**
 * Created by namjungsoo on 2016-11-17.
 */
//TODO: Zip파일 양면 읽기용으로 상속받아야 함. Pdf 파일 버전으로 따로 만들어야 함.
//TODO: 나중에 FragmentStatePagerAdapter로 변경해야 함
abstract class ViewerPagerAdapter : PagerAdapter() {
    var imageList: ArrayList<ExplorerItem> = arrayListOf()
    var exifRotation = true
    abstract fun stopAllTasks()

    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    private val colorMatrix: ColorMatrix?
        get() {
            var colorMatrix = ColorMatrix(
                floatArrayOf(
                    1f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    1f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    1f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    1f,
                    0f
                )
            )
            return try {
                if (nightMode) {
                    colorMatrix = ColorMatrix(
                        floatArrayOf(
                            -1f,
                            0f,
                            0f,
                            0f,
                            192f,
                            0f,
                            -1f,
                            0f,
                            0f,
                            192f,
                            0f,
                            0f,
                            -1f,
                            0f,
                            192f,
                            0f,
                            0f,
                            0f,
                            1f,
                            0f
                        )
                    )
                }
                colorMatrix
            } catch (e: NullPointerException) {
                null
            }
        }

    fun updateColorFilter(imageView: ImageView?) {
        if (imageView == null) return
        imageView.colorFilter = ColorMatrixColorFilter(colorMatrix!!)
    }
}