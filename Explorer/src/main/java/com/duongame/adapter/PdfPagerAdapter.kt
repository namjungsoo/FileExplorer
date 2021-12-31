package com.duongame.adapter

import android.app.Service
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import com.duongame.R
import com.duongame.activity.viewer.PagerActivity
import com.duongame.attacher.ImageViewAttacher
import com.duongame.helper.PreferenceHelper.nightMode
import com.duongame.listener.PagerOnTouchListener

/**
 * Created by namjungsoo on 2016-12-25.
 */
class PdfPagerAdapter(activity: PagerActivity) : ViewerPagerAdapter() {
    private var renderer: PdfRenderer? = null

    //    private Bitmap bitmap;
    var mPagerOnTouchListener: PagerOnTouchListener = PagerOnTouchListener(activity)

    fun setRenderer(renderer: PdfRenderer?) {
        this.renderer = renderer
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflator = container.context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflator.inflate(R.layout.page_viewer, container, false) as ViewGroup
        val imageView = rootView.findViewById<View>(R.id.image_viewer) as ImageView
        container.addView(rootView)
        val width = container.width
        val height = container.height
        if (width == 0 || height == 0) {
            container.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val width = container.width
                    val height = container.height
                    loadPage(position, imageView, width, height)
                    container.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
            })
        } else {
            loadPage(position, imageView, width, height)
        }
        return rootView
    }

    private fun loadPage(position: Int, imageView: ImageView?, width: Int, height: Int) {
        if (imageView != null) {
            val context = imageView.context
            val item = imageList?.get(position) ?: return
            var bitmap: Bitmap?
            if (renderer != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val page = renderer!!.openPage(position)

                    // 종횡비를 계산하자.
                    val pdfRatio = page.height.toFloat() / page.width.toFloat()
                    val screenRatio = height.toFloat() / width.toFloat()
                    var newWidth: Int
                    var newHeight: Int
                    if (pdfRatio > screenRatio) { // pdf가 더 길쭉 하면, screen의 height에 맞춘다.
                        newWidth = (height / pdfRatio).toInt()
                        newHeight = height
                    } else { // screen이 더 길쭉하면 screen의 width에 맞춘다.
                        newWidth = width
                        newHeight = (width * pdfRatio).toInt()
                    }

                    //FIX: PDF OOM
                    while (true) {
                        try {
                            bitmap =
                                Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                            break
                        } catch (e: OutOfMemoryError) {
                            newWidth /= 2
                            newHeight /= 2
                        }
                    }
                    page.render(bitmap!!, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    imageView.setImageBitmap(bitmap)

                    //TODO: 무조건 해주지 않으면 안된다. 알파로 처리되어 있기 때문이다. (RENDER_MODE_FOR_DISPLAY)
                    try {
                        if (nightMode) {
                            imageView.setBackgroundColor(Color.BLACK)
                        } else {
                            imageView.setBackgroundColor(Color.WHITE)
                        }
                    } catch (e: NullPointerException) {
                    }
                    updateColorFilter(imageView)

                    // 이미지 확대 축소
                    // 및 matrix을 사용하여 화면 가운데 정렬
                    item.attacher = ImageViewAttacher(imageView)
                    item.attacher?.mPagerOnTouchListener = mPagerOnTouchListener
                }
            }
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        val item = imageList?.get(position)
        val rootView = `object` as ViewGroup
        val imageView = rootView.findViewById<ImageView>(R.id.image_viewer)
        imageView.setImageBitmap(null)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {}
    override fun stopAllTasks() {
        // 아무것도 안함
        // task를 사용안함
    }
}