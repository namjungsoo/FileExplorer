package com.duongame.explorer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duongame.explorer.R;
import com.duongame.explorer.bitmap.BitmapCache;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.BitmapMsg;
import com.duongame.explorer.view.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.duongame.explorer.bitmap.BitmapCache.getThumbnail;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public abstract class ExplorerAdapter extends BaseAdapter implements AbsListView.OnScrollListener, View.OnTouchListener {
    private final static String TAG = "ExplorerAdapter";
    protected ArrayList<ExplorerItem> fileList;
    protected Activity context;

    protected HashMap<ImageView, AsyncTask> taskMap = new HashMap<ImageView, AsyncTask>();

    private Handler mainHandler;
    private Thread thread;

    private static int LOAD_BITMAP = 0;
    private static int LOAD_DRAWABLE = 1;

    private Queue<BitmapMsg> messageQueue = new ConcurrentLinkedQueue<>();

    private int lastScrollState = SCROLL_STATE_IDLE;
    private LoaderRunnable loaderRunnable = new LoaderRunnable();
    private int firstVisibleItem;
    private int visibleItemCount;

    class LoaderRunnable implements Runnable {
        private Object mPauseLock;
        private boolean mPaused;
        private boolean mFinished;

        public LoaderRunnable() {
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
        }

        @Override
        public void run() {
            while (!mFinished) {
                // Do stuff.

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                // 큐에 있는것을 꺼내자
                BitmapMsg msg = messageQueue.poll();

                // 꺼내서 로딩함
                handleBitmapMsg(msg);
            }
        }

        /**
         * Call this on pause.
         */
        public void onPause() {
            Log.d(TAG, "onPause");
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        /**
         * Call this on resume.
         */
        public void onResume() {
            Log.d(TAG, "onResume");
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }
    }


    public void onScrollStateChanged(AbsListView view, int scrollState) {
        lastScrollState = scrollState;
//        Log.d(TAG, "lastScrollState=" + lastScrollState);

        // 지금 idle이면 queue에 있는것을 전부 handler로 밀어 넣는다.
        if (lastScrollState == SCROLL_STATE_IDLE) {
            // 대기 중인거 중에서 position 범위에 안맞는건 삭제하자
            final ArrayList<BitmapMsg> removeList = new ArrayList<>();
            for (BitmapMsg msg : messageQueue) {
                if (msg.position >= firstVisibleItem && msg.position < firstVisibleItem + visibleItemCount) {

                } else {
                    removeList.add(msg);
                }
            }

            for (BitmapMsg msg : removeList) {
                messageQueue.remove(msg);
            }

            loaderRunnable.onResume();
        } else {
            loaderRunnable.onPause();
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount = visibleItemCount;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            loaderRunnable.onPause();
        }
        return false;
    }

    public ExplorerAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        this.context = context;
        this.fileList = fileList;

        // 받은 메세지로 imageview에 bitmap을 셋팅
        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final BitmapMsg bitmapMsg = (BitmapMsg) msg.obj;
                if (bitmapMsg == null)
                    return;

                if (bitmapMsg.imageView == null)
                    return;

                if (msg.arg1 == LOAD_BITMAP) {
                    if (bitmapMsg.bitmap == null)
                        return;

                    bitmapMsg.imageView.setImageBitmap(bitmapMsg.bitmap);
                } else if (msg.arg1 == LOAD_DRAWABLE) {
                    if (bitmapMsg.drawable == null)
                        return;

                    bitmapMsg.imageView.setImageDrawable(bitmapMsg.drawable);
                }
            }
        };

        thread = new Thread(loaderRunnable);
        thread.start();

        Log.d(TAG, "Thread Start");
    }

    private void handleBitmapMsg(BitmapMsg bitmapMsg) {
        if (bitmapMsg == null)
            return;

        switch (bitmapMsg.type) {
            case APK: {
                Message mainMsg = new Message();
                mainMsg.arg1 = LOAD_DRAWABLE;
                mainMsg.obj = bitmapMsg;
                bitmapMsg.drawable = BitmapLoader.loadApkThumbnailDrawable(ExplorerAdapter.this.context, bitmapMsg.path);

                mainHandler.sendMessage(mainMsg);
            }
            break;
            case PDF: {
                Message mainMsg = new Message();
                mainMsg.arg1 = LOAD_BITMAP;
                mainMsg.obj = bitmapMsg;
                bitmapMsg.bitmap = BitmapLoader.loadPdfThumbnailBitmap(ExplorerAdapter.this.context, bitmapMsg.path, bitmapMsg.imageView);

                mainHandler.sendMessage(mainMsg);
            }
            break;
            case IMAGE: {
                Message mainMsg = new Message();
                mainMsg.arg1 = LOAD_BITMAP;
                mainMsg.obj = bitmapMsg;
                bitmapMsg.bitmap = BitmapLoader.loadImageThumbnailBitmap(ExplorerAdapter.this.context, bitmapMsg.path, bitmapMsg.imageView);

                mainHandler.sendMessage(mainMsg);
            }
            break;
            case VIDEO: {
                Message mainMsg = new Message();
                mainMsg.arg1 = LOAD_BITMAP;
                mainMsg.obj = bitmapMsg;
                bitmapMsg.bitmap = BitmapLoader.loadVideoThumbnailBitmap(ExplorerAdapter.this.context, bitmapMsg.path, bitmapMsg.imageView);

                mainHandler.sendMessage(mainMsg);
            }
            break;
            case ZIP: {
                Message mainMsg = new Message();
                mainMsg.arg1 = LOAD_BITMAP;
                mainMsg.obj = bitmapMsg;
                bitmapMsg.bitmap = BitmapLoader.loadZipThumbnailBitmap(ExplorerAdapter.this.context, bitmapMsg.path, bitmapMsg.imageView);

                mainHandler.sendMessage(mainMsg);
            }
            break;
        }
    }

    @Override
    public int getCount() {
        if (fileList == null)
            return 0;

        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    protected static class ViewHolder {
        public ImageView small_icon;
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public ExplorerItem.FileType type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflateLayout(parent);

            viewHolder = new ViewHolder();
            initViewHolder(viewHolder, convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExplorerItem item = fileList.get(position);

        setViewHolder(viewHolder, item);
        setIcon(viewHolder, item, position);
//        Log.d(TAG, "getView position="+position);

        return convertView;
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
        loaderRunnable.onResume();
    }

    void setIconImage(final ViewHolder viewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            BitmapMsg bitmapMsg = new BitmapMsg();
            bitmapMsg.type = ExplorerItem.FileType.IMAGE;
            bitmapMsg.path = item.path;
            bitmapMsg.imageView = viewHolder.icon;
            bitmapMsg.position = position;

            messageQueue.add(bitmapMsg);
        } else {
            viewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconPdf(final ViewHolder viewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            BitmapMsg bitmapMsg = new BitmapMsg();
            bitmapMsg.type = ExplorerItem.FileType.PDF;
            bitmapMsg.path = item.path;
            bitmapMsg.imageView = viewHolder.icon;
            bitmapMsg.position = position;

            messageQueue.add(bitmapMsg);
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
        }

    }

    void setIconZip(final ViewHolder viewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            BitmapMsg bitmapMsg = new BitmapMsg();
            bitmapMsg.type = ExplorerItem.FileType.ZIP;
            bitmapMsg.path = item.path;
            bitmapMsg.imageView = viewHolder.icon;
            bitmapMsg.position = position;

            messageQueue.add(bitmapMsg);
        } else {
            viewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconApk(final ViewHolder viewHolder, ExplorerItem item, int position) {
        //TODO: 동적으로 읽기
        Drawable drawable = BitmapCache.getDrawable(item.path);
        if (drawable == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            BitmapMsg bitmapMsg = new BitmapMsg();
            bitmapMsg.type = ExplorerItem.FileType.APK;
            bitmapMsg.path = item.path;
            bitmapMsg.imageView = viewHolder.icon;
            bitmapMsg.position = position;

            messageQueue.add(bitmapMsg);
        } else {
            viewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconVideo(final ViewHolder viewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            viewHolder.icon.setImageResource(R.drawable.file);

            BitmapMsg bitmapMsg = new BitmapMsg();
            bitmapMsg.type = ExplorerItem.FileType.VIDEO;
            bitmapMsg.path = item.path;
            bitmapMsg.imageView = viewHolder.icon;
            bitmapMsg.position = position;

            messageQueue.add(bitmapMsg);
        } else {// 로딩된 비트맵을 셋팅
            viewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIcon(final ViewHolder viewHolder, ExplorerItem item, int position) {
        if (item.type == ExplorerItem.FileType.IMAGE) {
            setIconImage(viewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.VIDEO) {
            setIconVideo(viewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.ZIP) {
            setIconZip(viewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.PDF) {
            setIconPdf(viewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.APK) {
            setIconApk(viewHolder, item, position);
        } else {
            // 이전 타입과 다르게 새 타입이 들어왔다면 업데이트 한다.
            //if (viewHolder.type != item.type) {
            setTypeIcon(item.type, viewHolder.icon);
            //}
        }

        viewHolder.type = item.type;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    void setTypeIcon(ExplorerItem.FileType type, ImageView icon) {
        switch (type) {
            case AUDIO:
            case FILE:
                icon.setImageBitmap(BitmapCache.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                icon.setImageBitmap(BitmapCache.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            case TEXT:
                icon.setImageBitmap(BitmapCache.getResourceBitmap(context.getResources(), R.drawable.text));
                break;
            default:
                return;
        }
    }

    public void stopAllTasks() {
        for (AsyncTask task : taskMap.values()) {
            task.cancel(true);
        }
        taskMap.clear();
    }

    public abstract void initViewHolder(ViewHolder viewHolder, View convertView);

    public abstract void setViewHolder(ViewHolder viewHolder, ExplorerItem item);

    public abstract View inflateLayout(ViewGroup parent);
}
