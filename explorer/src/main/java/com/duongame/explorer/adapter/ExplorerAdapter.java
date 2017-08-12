package com.duongame.explorer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duongame.R;
import com.duongame.explorer.bitmap.BitmapCacheManager;
import com.duongame.explorer.bitmap.BitmapLoader;
import com.duongame.explorer.bitmap.BitmapMessage;
import com.duongame.explorer.view.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.duongame.explorer.adapter.ExplorerItem.FileType.APK;
import static com.duongame.explorer.adapter.ExplorerItem.FileType.VIDEO;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getDrawable;
import static com.duongame.explorer.bitmap.BitmapCacheManager.getThumbnail;

/**
 * Created by namjungsoo on 2016-11-06.
 */

//public abstract class ExplorerAdapter extends BaseAdapter implements AbsListView.OnScrollListener, View.OnTouchListener {
public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder> implements RecyclerView.OnItemTouchListener {
    private final static String TAG = "ExplorerAdapter";
    private final static boolean DEBUG = false;
    private final static boolean USE_THREAD = false;

    protected ArrayList<ExplorerItem> fileList;

    // file path와 file item을 묶어 놓음
    protected HashMap<String, ExplorerItem> fileMap;

    protected Activity context;

//    protected HashMap<ImageView, AsyncTask> taskMap = new HashMap<ImageView, AsyncTask>();

    private Handler mainHandler;
    private Thread thread;

    private final static int LOAD_BITMAP = 0;
    private final static int LOAD_DRAWABLE = 1;

    private Queue<BitmapMessage> messageQueue = new ConcurrentLinkedQueue<>();

    private int lastScrollState = SCROLL_STATE_IDLE;
    private LoaderRunnable loaderRunnable = new LoaderRunnable();
    private int firstVisibleItem;
    private int visibleItemCount;

    private int idle_firstVisibleItem;
    private int idle_visibleItemCount;

    private final static int SCROLL_DIRECTION_NEXT = 0;
    private final static int SCROLL_DIRECTION_PREV = 1;
    private int scrollDirection = SCROLL_DIRECTION_NEXT;

    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (USE_THREAD)
                loaderRunnable.onPause();
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

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
//                Log.d(TAG, "run");

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                // 큐에 있는것을 꺼내자
                BitmapMessage msg = messageQueue.poll();

                // msg가 null이면 읽을게 더이상 없다는 이야기므로 prev, next중에서 하나를 읽자
                if (msg == null) {

                    if (scrollDirection == SCROLL_DIRECTION_PREV) {
//                        Log.w(TAG, "SCROLL_DIRECTION_PREV");

                        if (firstVisibleItem >= 0) {// 다 읽지 않았을때 앞으로 가서 읽는다.
                            for (int i = firstVisibleItem - 1; i >= 0; i--) {
                                // 읽었는지 체크해보고 하나만 읽는다.
                                final ExplorerItem item = fileList.get(i);

                                // 없을 경우 로딩해준다.
                                if (!checkBodInCache(item)) {
//                                    Log.d(TAG, "SCROLL_DIRECTION_PREV loadThumbnail i=" + i + " " + item.path);
                                    loadThumbnail(item.type, item.path);
                                    break;
                                }
                            }
                        }
                    } else if (scrollDirection == SCROLL_DIRECTION_NEXT) {
//                        Log.w(TAG, "SCROLL_DIRECTION_NEXT");

                        if (firstVisibleItem + visibleItemCount < fileList.size()) {// 다 읽지 않았을때 앞으로 가서 읽는다.
                            for (int i = firstVisibleItem + visibleItemCount; i < fileList.size(); i++) {
                                // 읽었는지 체크해보고 하나만 읽는다.
                                final ExplorerItem item = fileList.get(i);

                                // 없을 경우 로딩해준다.
                                if (!checkBodInCache(item)) {
//                                    Log.d(TAG, "SCROLL_DIRECTION_NEXT loadThumbnail i=" + i + " " + item.path);
                                    loadThumbnail(item.type, item.path);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // 꺼내서 로딩함
                    handleBitmapMessage(msg);
                }
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

        public boolean isPaused() {
            return mPaused;
        }
    }

    // 썸네일이 있는 파일만 true
    private boolean hasThumbnail(ExplorerItem item) {
        switch (item.type) {
            case APK:
            case ZIP:
            case PDF:
            case IMAGE:
            case VIDEO:
                return true;
            default:
                return false;
        }
    }

    private boolean checkBodInCache(ExplorerItem item) {
        if (item.type == APK) {
            Drawable drawable = BitmapCacheManager.getDrawable(item.path);
            if (drawable != null)
                return true;
        } else {
            if (hasThumbnail(item)) {
                Bitmap bitmap = BitmapCacheManager.getThumbnail(item.path);
                if (bitmap != null)
                    return true;
            } else {
                return true;
            }
        }
        return false;
    }

    public void scrollStateChanged(RecyclerView recyclerView, int scrollState) {
        lastScrollState = scrollState;
        firstVisibleItem = getFirstVisibleItem(recyclerView);
        visibleItemCount = getVisibleItemCount(recyclerView);
//        Log.d("TAG", "scrollStateChanged lastScrollState=" + lastScrollState + " firstVisibleItem=" + firstVisibleItem + " visibleItemCount=" + visibleItemCount);


//        Log.d(TAG, "lastScrollState=" + lastScrollState);

//        Log.d(TAG, "onScrollStateChanged first=" + firstVisibleItem + " count=" + visibleItemCount);

        // 지금 idle이면 queue에 있는것을 전부 handler로 밀어 넣는다.
        if (lastScrollState == SCROLL_STATE_IDLE) {
//            Log.d(TAG, "SCROLL_STATE_IDLE first=" + firstVisibleItem + " count=" + visibleItemCount);

            // 처음인 경우
            if (idle_firstVisibleItem == 0 && idle_visibleItemCount == 0) {
            } else {
//                Log.d(TAG, "first=" + firstVisibleItem + " idle_first=" + idle_firstVisibleItem);
                if (idle_firstVisibleItem > firstVisibleItem) {
                    scrollDirection = SCROLL_DIRECTION_PREV;
                } else {
                    scrollDirection = SCROLL_DIRECTION_NEXT;
                }
//                Log.d(TAG, "scrollDirection=" + scrollDirection);
            }
            idle_firstVisibleItem = firstVisibleItem;
            idle_visibleItemCount = visibleItemCount;

            // 대기 중인거 중에서 position 범위에 안맞는건 삭제하자
            final ArrayList<BitmapMessage> removeList = new ArrayList<>();
            for (BitmapMessage msg : messageQueue) {
                if (msg.position >= firstVisibleItem && msg.position < firstVisibleItem + visibleItemCount) {

                } else {
                    removeList.add(msg);
                }
            }

            for (BitmapMessage msg : removeList) {
                messageQueue.remove(msg);
            }

            if (USE_THREAD)
                loaderRunnable.onResume();
        } else {
//            loaderRunnable.onPause();
        }
    }

//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
////        Log.d(TAG, "onScroll first=" + firstVisibleItem + " count=" + visibleItemCount);
//        this.firstVisibleItem = firstVisibleItem;
//        this.visibleItemCount = visibleItemCount;
//    }

//    public boolean onTouch(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            loaderRunnable.onPause();
//        }
//        return false;
//    }

    public ExplorerAdapter(Activity context, ArrayList<ExplorerItem> fileList) {
        this.context = context;
        this.fileList = fileList;

        // 받은 메세지로 imageview에 bitmap을 셋팅
        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final BitmapMessage bitmapMessage = (BitmapMessage) msg.obj;
                if (bitmapMessage == null) {
                    Log.e(TAG, "bitmapMessage == null");
                    return;
                }

                if (bitmapMessage.imageView == null) {
                    Log.e(TAG, "bitmapMessage.imageView == null");
                    return;
                }

                // 여기서 파일리스트에서 찾아보자
//                if(loaderRunnable.isPaused())
//                    return;
                if (fileMap == null) {
                    Log.e(TAG, "fileMap == null");
                    return;
                }

                if (!fileMap.containsKey(bitmapMessage.path)) {
                    Log.e(TAG, "!fileMap.containsKey(bitmapMessage.path)");
                    return;
                }

                if (fileMap.get(bitmapMessage.path).imageViewRef == null) {
                    Log.e(TAG, "imageViewRef == null");
                    return;
                }

                if (fileMap.get(bitmapMessage.path).imageViewRef.get() != bitmapMessage.imageView) {
                    Log.e(TAG, "imageViewRef.get() != bitmapMessage.imageView");
                    return;
                }

                if (msg.arg1 == LOAD_BITMAP) {
                    if (bitmapMessage.bitmap == null) {
                        Log.e(TAG, "bitmap == null");
                        return;
                    }

                    bitmapMessage.imageView.setImageBitmap(bitmapMessage.bitmap);
                } else if (msg.arg1 == LOAD_DRAWABLE) {
                    if (bitmapMessage.drawable == null) {
                        Log.e(TAG, "drawable == null");
                        return;
                    }

                    bitmapMessage.imageView.setImageDrawable(bitmapMessage.drawable);
                }
            }
        };

        if (USE_THREAD) {
            thread = new Thread(loaderRunnable);
            thread.start();
        }

        Log.d(TAG, "Thread Start");
    }

    class BitmapOrDrawable {
        public Bitmap bitmap;
        public Drawable drawable;
    }

    private BitmapOrDrawable loadThumbnail(ExplorerItem.FileType type, String path) {
        BitmapOrDrawable bod = new BitmapOrDrawable();

        switch (type) {
            case APK:
                bod.drawable = BitmapLoader.loadApkThumbnailDrawable(ExplorerAdapter.this.context, path);
                break;
            case PDF:
                bod.bitmap = BitmapLoader.loadPdfThumbnailBitmap(ExplorerAdapter.this.context, path);
                break;
            case IMAGE:
                bod.bitmap = BitmapLoader.loadImageThumbnailBitmap(ExplorerAdapter.this.context, path);
                break;
            case VIDEO:
                bod.bitmap = BitmapLoader.loadVideoThumbnailBitmap(ExplorerAdapter.this.context, path);
                break;
            case ZIP:
                bod.bitmap = BitmapLoader.loadZipThumbnailBitmap(ExplorerAdapter.this.context, path);
                break;
        }

        return bod;
    }

    private void handleBitmapMessage(BitmapMessage bitmapMessage) {
        if (bitmapMessage == null)
            return;

        Message mainMsg = new Message();
        mainMsg.obj = bitmapMessage;
        mainMsg.arg1 = LOAD_BITMAP;
        BitmapOrDrawable bod = loadThumbnail(bitmapMessage.type, bitmapMessage.path);

        switch (bitmapMessage.type) {
            case APK: {
                mainMsg.arg1 = LOAD_DRAWABLE;
                bitmapMessage.drawable = bod.drawable;
            }
            break;
            case PDF:
            case IMAGE:
            case VIDEO:
            case ZIP:
                bitmapMessage.bitmap = bod.bitmap;
                break;
        }

        mainHandler.sendMessage(mainMsg);
    }

//    @Override
//    public int getCount() {
//        if (fileList == null)
//            return 0;
//
//        return fileList.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }

    protected static class ExplorerViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconSmall;
        public RoundedImageView icon;
        public TextView name;
        public TextView date;
        public TextView size;
        public ExplorerItem.FileType type;
        public int position;

        public ExplorerViewHolder(View itemView) {
            super(itemView);
            icon = (RoundedImageView) itemView.findViewById(R.id.file_icon);
            name = (TextView) itemView.findViewById(R.id.text_name);
            date = (TextView) itemView.findViewById(R.id.text_date);
            size = (TextView) itemView.findViewById(R.id.text_size);
            iconSmall = (ImageView) itemView.findViewById(R.id.file_small_icon);
        }
    }

    @Override
    public ExplorerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflateLayout(parent);
        ExplorerViewHolder holder = new ExplorerViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ExplorerViewHolder holder, int position) {
//        Log.e("TAG", "onBindViewHolder " + position);
        //holder.mTextView.setText(String.valueOf(item[position]));
        bindViewHolderExplorer(holder, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder.position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (fileList == null)
            return 0;

        return fileList.size();
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ExplorerViewHolder viewHolder;
//
//        if (convertView == null) {
//            convertView = inflateLayout(parent);
//
//            viewHolder = new ExplorerViewHolder();
//            initViewHolder(viewHolder, convertView);
//
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ExplorerViewHolder) convertView.getTag();
//        }
//
//        ExplorerItem item = fileList.get(position);
//        item.imageViewRef = new WeakReference<ImageView>(viewHolder.icon);
//
//        setViewHolder(viewHolder, item);
//
//        setDefaultIcon(item.type, viewHolder.icon);
//        setIcon(viewHolder, item, position);
////        Log.d(TAG, "getView position="+position);
//
//        return convertView;
//    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
        fileMap = new HashMap<>();
        for (ExplorerItem item : fileList) {
            fileMap.put(item.path, item);
        }

        initVisibleInfo();

        // SearchTask가 resume
//        loaderRunnable.onResume();
    }

    private void initVisibleInfo() {
        firstVisibleItem = 0;
        visibleItemCount = 0;
        idle_firstVisibleItem = 0;
        idle_visibleItemCount = 0;
    }

    void setIconImage(final ExplorerViewHolder explorerViewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            if (USE_THREAD) {
                BitmapMessage bitmapMessage = new BitmapMessage();
                bitmapMessage.type = ExplorerItem.FileType.IMAGE;
                bitmapMessage.path = item.path;
                bitmapMessage.imageView = explorerViewHolder.icon;
                bitmapMessage.position = position;

                messageQueue.add(bitmapMessage);
            } else {
                // Glide로 읽자
                // 값이 0임
//                int width = explorerViewHolder.icon.getWidth();
//                int height = explorerViewHolder.icon.getHeight();
                //.override(explorerViewHolder.icon.getWidth(), explorerViewHolder.icon.getHeight())
                Glide.with(context).load(new File(item.path))
                        .centerCrop().into(explorerViewHolder.icon);
            }
        } else {
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }

    }

    void setIconPdf(final ExplorerViewHolder explorerViewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            BitmapMessage bitmapMessage = new BitmapMessage();
            bitmapMessage.type = ExplorerItem.FileType.PDF;
            bitmapMessage.path = item.path;
            bitmapMessage.imageView = explorerViewHolder.icon;
            bitmapMessage.position = position;

            messageQueue.add(bitmapMessage);
        } else {// 로딩된 비트맵을 셋팅
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }

    }

    void setIconZip(final ExplorerViewHolder explorerViewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            if(USE_THREAD) {
                BitmapMessage bitmapMessage = new BitmapMessage();
                bitmapMessage.type = ExplorerItem.FileType.ZIP;
                bitmapMessage.path = item.path;
                bitmapMessage.imageView = explorerViewHolder.icon;
                bitmapMessage.position = position;

                messageQueue.add(bitmapMessage);
            }
            else {
                final String image = BitmapLoader.getZipThumbnailFileName(context, item.path);
                if(image != null) {
                    Glide.with(context).load(new File(image))
                            .centerCrop().into(explorerViewHolder.icon);
                }
            }
        } else {
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIconApk(final ExplorerViewHolder explorerViewHolder, ExplorerItem item, int position) {
        Drawable drawable = getDrawable(item.path);
        if (drawable == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            if(USE_THREAD) {
                BitmapMessage bitmapMessage = new BitmapMessage();
                bitmapMessage.type = ExplorerItem.FileType.APK;
                bitmapMessage.path = item.path;
                bitmapMessage.imageView = explorerViewHolder.icon;
                bitmapMessage.position = position;

                messageQueue.add(bitmapMessage);
            } else {
                BitmapOrDrawable bod = loadThumbnail(item.type, item.path);
                explorerViewHolder.icon.setImageDrawable(bod.drawable);

//                Glide.with(context)
//                        .using(new PassThroughModelLoader<PackageInfo>(), PackageInfo.class)
//                        .from(PackageInfo.class)
//                        .as(Drawable.class)
//                        .decoder(new ApplicationIconDecoder(context))
//                        .diskCacheStrategy(DiskCacheStrategy.NONE) // cannot disk cache ApplicationInfo, nor Drawables
//                        .load(context.getPackageManager().getPackageArchiveInfo(item.path, 0))
//                        .into(explorerViewHolder.icon);
            }
        } else {
            explorerViewHolder.icon.setImageDrawable(drawable);
        }
    }

    void setIconVideo(final ExplorerViewHolder explorerViewHolder, ExplorerItem item, int position) {
        final Bitmap bitmap = getThumbnail(item.path);
        if (bitmap == null) {
            explorerViewHolder.icon.setImageResource(R.drawable.file);

            BitmapMessage bitmapMessage = new BitmapMessage();
            bitmapMessage.type = VIDEO;
            bitmapMessage.path = item.path;
            bitmapMessage.imageView = explorerViewHolder.icon;
            bitmapMessage.position = position;

            messageQueue.add(bitmapMessage);
        } else {// 로딩된 비트맵을 셋팅
            explorerViewHolder.icon.setImageBitmap(bitmap);
        }
    }

    void setIcon(final ExplorerViewHolder explorerViewHolder, ExplorerItem item, int position) {
        if (item.type == ExplorerItem.FileType.IMAGE) {
            setIconImage(explorerViewHolder, item, position);
        } else if (item.type == VIDEO) {
            setIconVideo(explorerViewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.ZIP) {
            setIconZip(explorerViewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.PDF) {
            setIconPdf(explorerViewHolder, item, position);
        } else if (item.type == ExplorerItem.FileType.APK) {
            setIconApk(explorerViewHolder, item, position);
        } else {
            // 이전 타입과 다르게 새 타입이 들어왔다면 업데이트 한다.
            //if (explorerViewHolder.type != item.type) {
            setTypeIcon(item.type, explorerViewHolder.icon);
            //}
        }

        explorerViewHolder.type = item.type;
    }

    // 비용이 많이 드는 작업임
//    public static Bitmap drawableToBitmap(Drawable drawable) {
//        Bitmap bitmap = null;
//
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            if (bitmapDrawable.getBitmap() != null) {
//                return bitmapDrawable.getBitmap();
//            }
//        }
//
//        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
//            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
//        } else {
//            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        }
//
//        final Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }

    void setDefaultIcon(ExplorerItem.FileType type, ImageView icon) {
        switch (type) {
            case DIRECTORY:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            default:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
        }
    }

    void setTypeIcon(ExplorerItem.FileType type, ImageView icon) {
        switch (type) {
            case AUDIO:
            case FILE:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.file));
                break;
            case DIRECTORY:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.directory));
                break;
            case TEXT:
                icon.setImageBitmap(BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.text));
                break;
            default:
                return;
        }
    }

    public void resumeThread() {
        if (USE_THREAD)
            loaderRunnable.onResume();
    }

    public void pauseThread() {
        Log.d(TAG, "pauseThread");
        if (USE_THREAD)
            loaderRunnable.onPause();
        messageQueue.clear();

        // SearchTask가 resume
//        loaderRunnable.onResume();

        // 현재 태스크는 사용하고 있지 않음
        // 1 쓰레드로 사용중
//        for (AsyncTask task : taskMap.values()) {
//            task.cancel(true);
//            Log.d(TAG, "task.cancel");
//        }
//        taskMap.clear();
    }

//    public abstract void initViewHolder(ExplorerViewHolder viewHolder, View convertView);

//    public abstract void setViewHolder(ExplorerViewHolder viewHolder, ExplorerItem item);

    public void bindViewHolderExplorer(ExplorerViewHolder viewHolder, int position) {
    }

    public View inflateLayout(ViewGroup parent) {
        return null;
    }

    public int getFirstVisibleItem(RecyclerView recyclerView) {
        return 0;
    }

    public int getVisibleItemCount(RecyclerView recyclerView) {
        return 0;
    }


//    class PassThroughModelLoader<T> implements ModelLoader<T, T> {
//        @Override public DataFetcher<T> getResourceFetcher(final T model, int width, int height) {
//            return new DataFetcher<T>() {
//                @Override public T loadData(Priority priority) throws Exception { return model; }
//                @Override public void cleanup() { }
//                @Override public String getId() { return "PassThroughDataFetcher"; }
//                @Override public void cancel() { }
//            };
//        }
//    }
//
//    class ApplicationIconDecoder implements ResourceDecoder<PackageInfo, Drawable> {
//        private final Context context;
//        public ApplicationIconDecoder(Context context) { this.context = context; }
//        @Override public Resource<Drawable> decode(PackageInfo source, int width, int height) throws IOException {
//            //Drawable icon = context.getPackageManager().getApplicationIcon(source);
//            Drawable icon = source.applicationInfo.loadIcon(context.getPackageManager());
//            return new DrawableResource<Drawable>(icon) {
//                @Override public int getSize() { // best effort
//                    if (drawable instanceof BitmapDrawable) {
//                        return Util.getBitmapByteSize(((BitmapDrawable)drawable).getBitmap());
//                    } else {
//                        return 1;
//                    }
//                }
//                @Override public void recycle() { /* not from our pool */ }
//            };
//        }
//        @Override public String getId() { return "ApplicationInfoToDrawable"; }
//    }
}
