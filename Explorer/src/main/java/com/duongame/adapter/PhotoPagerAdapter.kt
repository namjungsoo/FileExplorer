package com.duongame.adapter

import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import com.duongame.R
import com.duongame.activity.viewer.PagerActivity
import com.duongame.attacher.ImageViewAttacher
import com.duongame.bitmap.BitmapCacheManager.removeBitmap
import com.duongame.bitmap.BitmapCacheManager.removePage
import com.duongame.file.FileHelper.isGifImage
import com.duongame.listener.PagerOnTouchListener
import com.duongame.task.bitmap.LoadBitmapTask
import com.duongame.task.bitmap.LoadGifTask
import com.duongame.task.bitmap.LoadGifTask.LoadGifListener
import com.felipecsl.gifimageview.library.GifImageView
import timber.log.Timber

/**
 * Created by namjungsoo on 2016-12-25.
 */
class PhotoPagerAdapter(val activity: PagerActivity, var useGifAni: Boolean) : ViewerPagerAdapter() {
    private val taskList = ArrayList<AsyncTask<*, *, *>>()
    private var lastPosition = -1
    var mPagerOnTouchListener: PagerOnTouchListener = PagerOnTouchListener(activity)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        Timber.e("instantiateItem $position")
        val rootView = LayoutInflater.from(container.context).inflate(R.layout.page_viewer, container, false) as FrameLayout
        val imageView = rootView.findViewById<ImageView>(R.id.image_viewer)

//        rootView.setBaseOnTouchListener(mPagerOnTouchListener);
        container.addView(rootView)
        val width = container.width
        val height = container.height
        if (width == 0 || height == 0) {
            container.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val width = container.width
                    val height = container.height
                    loadCurrentBitmap(position, imageView, width, height)
                    container.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
            })
        } else {
            loadCurrentBitmap(position, imageView, width, height)
        }
        return rootView
    }

    private fun loadCurrentBitmap(position: Int, imageView: ImageView, width: Int, height: Int) {
        val item = imageList[position]

        // GIF는 여기서 읽지 않는다.
        // useGifAni: 애니메이션이 있을때는 외부에서 쓰레드를 통해서 렌더링 하므로 여기서는 미리 gif를 로딩해 놓지 않는다.
        if (useGifAni && isGifImage(item.path)) {
//            Glide.with(context).load(new File(item.path)).into(imageView);
            // 일단 애니메이션이 있는지를 체크해보고 없으면 내가 로딩하자
            return
        }
        val loadingByOther = LoadBitmapTask.isCurrentLoadingBitmap(item.path)
        if (!loadingByOther) LoadBitmapTask.setCurrentLoadingBitmap(item.path)
        val task = LoadBitmapTask(
            activity,
            imageView,
            width,
            height,
            exifRotation,
            position,
            loadingByOther
        )

        // THREAD_POOL을 사용하는 이유는 압축을 풀면서 동적으로 로딩을 해야 하기 때문이다.
        // 그런데 양쪽 페이지로 되어 있는 만화 같은 경우 하나의 PNG를 읽으면 양쪽 페이지가 나오는데
        // 두개의 쓰레드가 경쟁할때가 있다.
        // 이를 위해서 쓰레드풀을 분리해야할 수 있다.
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item)

        // viewer_page layout에 장착한다.
//        imageView.setOnTouchListener(mPagerOnTouchListener);
        item.attacher = ImageViewAttacher(imageView)
        //item.attacher!!.setActivity(activity)
        item.attacher?.mPagerOnTouchListener = mPagerOnTouchListener
        taskList.add(task)
        updateColorFilter(imageView)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Timber.e("destroyItem position=$position")
        // 공통 사항
        container.removeView(`object` as View)

        // 이미지뷰에서 bitmap을 끊어주자.
        val rootView = `object` as ViewGroup ?: return
        val imageView = rootView.findViewById<ImageView>(R.id.image_viewer) ?: return
        imageView.setImageBitmap(null)

        //FIX: OOM
        // 현재 위치에 해당하는 bitmap을 찾아서 캐쉬 삭제 해주자.
        deleteItemBitmapCache(position)
    }

    private fun deleteItemBitmapCache(position: Int) {
        try {
            val item = imageList[position] ?: return

            // bitmap부터 체크
            if (item.side == ExplorerItem.SIDE_ALL) {
                removeBitmap(item.path)
            } else {
                removePage(item.path)
            }
        } catch (e: Exception) {
        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
//        Timber.e("setPrimaryItem " + position);
        val width = container.width
        val height = container.height
        if (position != lastPosition) {
            lastPosition = position

            // preload bitmap task
            if (width == 0 || height == 0) {
                container.viewTreeObserver.addOnGlobalLayoutListener(object :
                    OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val width = container.width
                        val height = container.height

                        //FIX: OOM
//                        preloadAndRemoveNearBitmap(position, width, height);
                        container.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                })
            } else {
                //FIX: OOM
//                preloadAndRemoveNearBitmap(position, width, height);
            }
            updateGifImage(`object`, position)
        }
    }

    private fun updateGifImage(`object`: Any, position: Int) {
        // GIF 이미지일 경우
        // 메모리에 사라졌다가 재 로딩일 경우에 애니메이션이 잘 안된다.
        val rootView = `object` as ViewGroup ?: return
        val pagerActivity = activity
        val imageView = rootView.findViewById<GifImageView>(R.id.image_viewer)
        if (!useGifAni) return
        if (imageList[position].path.endsWith(".gif")) {
            val task = LoadGifTask(object : LoadGifListener {
                override fun onSuccess(data: ByteArray?) {
                    // 기존 GIF가 있으면 가져와서 stop해줌
                    pagerActivity.stopGifAnimation()

                    //imageView.stopAnimation();
                    imageView.setBytes(data)
                    imageView.startAnimation()

                    // 성공이면 imageView를 저장해 놓음
                    pagerActivity.gifImageView = imageView
                    val item = imageList[position]
                    if (item != null) {
                        item.width = imageView.gifWidth
                        item.height = imageView.gifHeight
                    }
                    activity.updateInfo(position)
                }

                override fun onFail() {
                    // 기존 GIF가 있으면 가져와서 stop해줌
                    pagerActivity.stopGifAnimation()
                    pagerActivity.gifImageView = null
                }
            })
            task.execute(imageList[position].path)
        } else { // GIF가 아니면
            // 기존 GIF가 있으면 가져와서 stop해줌
            pagerActivity.stopGifAnimation()
            pagerActivity.gifImageView = null
        }
    }

    override fun stopAllTasks() {
        for (task in taskList) {
            task.cancel(true)
        }
        taskList.clear()
    }

    companion object {
        private const val TAG = "PhotoPagerAdapter"
    }

}