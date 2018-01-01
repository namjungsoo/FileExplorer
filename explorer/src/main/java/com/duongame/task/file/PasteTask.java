package com.duongame.task.file;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.OverwriteDialog;
import com.duongame.dialog.PasteDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.ToastHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-12-29.
 */

public class PasteTask extends AsyncTask<Void, FileHelper.Progress, Boolean> {
    private ArrayList<ExplorerItem> fileList;
    private ArrayList<ExplorerItem> pasteList;

    private String capturePath;
    private String pastePath;
    private boolean cut;
    private boolean makeCopy;

    // 생성자에서 사용
    private WeakReference<PasteDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    private DialogInterface.OnDismissListener onDismissListener;

    private final Object lock;
    private boolean applyAll, skip, cancel;

    public PasteTask(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
        lock = new Object();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    // 삭제할 파일과 폴더를 입력해야 한다.
    // 폴더 삭제도 가능한지 확인해봐야 한다.
    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    public void setPath(String capturePath, String pastePath) {
        this.capturePath = capturePath;
        this.pastePath = pastePath;
        if (capturePath.equals(pastePath)) {
            makeCopy = true;
        }
    }

    public void setIsCut(boolean cut) {
        this.cut = cut;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

//        JLog.w("PasteTask", "onPreExecute");
        dialogWeakReference = new WeakReference<PasteDialog>(new PasteDialog());
        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
//            JLog.w("PasteTask", "dialog.hashCode " + dialog.hashCode());

            // 확인 버튼이 눌려쥐면 task를 종료한다.
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PasteTask.this.cancel(true);
                }
            });
            dialog.setIsCut(cut);
            dialog.setOnDismissListener(onDismissListener);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "paste");
            }
        }
    }

    void makePasteList() {
        pasteList = new ArrayList<>();

        // fileList에는 선택된 파일만 들어옴
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            if (item.type == ExplorerItem.FileType.FOLDER) {
                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                FileSearcher searcher = new FileSearcher();
                FileSearcher.Result result = searcher.setRecursiveDirectory(true)
                        .setHiddenFile(true)
                        .setExcludeDirectory(false)
                        .setImageListEnable(false)
                        .search(item.path);

                // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
                if (result != null && result.fileList != null) {
                    for (int j = 0; j < result.fileList.size(); j++) {

                        // 선택된 폴더의 최상위 폴더의 폴더명을 적어줌
                        ExplorerItem subItem = result.fileList.get(j);
                        if (subItem.path.startsWith(item.path)) {
                            subItem.name = subItem.path.replace(FileHelper.getParentPath(item.path) + "/", "");
                        }
                    }
                    pasteList.addAll(result.fileList);
                }

                // 폴더 자기 자신도 더함
                pasteList.add(item);
            } else {
                pasteList.add(item);
            }
        }
    }

    boolean processTask() {
        for (int i = 0; i < pasteList.size(); i++) {
            // 파일을 하나하나 지운다.
            try {
                if (isCancelled()) {
                    return false;
                } else {
                    if (!work(i, pasteList.get(i).path)) {
                        return false;
                    }
                }
            } catch (SecurityException e) {
                // 지울수 없는 파일
                Activity activity = activityWeakReference.get();
                if (activity != null) {
                    ToastHelper.showToast(activity, R.string.toast_error);
                }
                return false;
            } catch (InterruptedException e) {
                // 지울수 없는 파일
                Activity activity = activityWeakReference.get();
                if (activity != null) {
                    ToastHelper.showToast(activity, R.string.toast_error);
                }
                return false;
            }
        }
        return true;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        makePasteList();
        return processTask();
    }

    void alertOverwrite(final String path) {
        final Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OverwriteDialog dialog = new OverwriteDialog();
                    dialog.setPath(path);
                    dialog.setLock(lock);
                    dialog.setOnFinishListener(new OverwriteDialog.OnFinishListener() {
                        @Override
                        public void onFinish(boolean applyAll, boolean skip, boolean cancel) {
                            PasteTask.this.applyAll = applyAll;
                            PasteTask.this.skip = skip;
                            PasteTask.this.cancel = cancel;
                        }
                    });
                    dialog.show(activity.getFragmentManager(), "overwrite");
                }
            });
        }
    }

    void updateProgress(int i, int percent) {
        // progress를 기입해야 한다.
        FileHelper.Progress progress = new FileHelper.Progress();
        progress.index = i;
        progress.percent = 100;

        publishProgress(progress);
    }

    void workDirect(int i, File src, File dest) throws IOException {
        if (cut) {
            // 이동한다.
            FileUtils.moveFile(src, dest);
            updateProgress(i, 100);
        } else {
            // 복사한다.
//            JLog.e("TAG", "workDirect copy");
            FileInputStream inputStream = new FileInputStream(src);
            FileChannel inputChannel = inputStream.getChannel();

            FileOutputStream outputStream = new FileOutputStream(dest);
            FileChannel outputChannel = outputStream.getChannel();

//            ReadableCallbackByteChannel readableCallbackByteChannel = new ReadableCallbackByteChannel(inputChannel, src.length(), i);
//            JLog.e("TAG", "workDirect transferFrom");
//            outputChannel.transferFrom(readableCallbackByteChannel, 0, src.length());

//            JLog.e("TAG", "maxMemory=" + Runtime.getRuntime().maxMemory() + " freeMemory=" + Runtime.getRuntime().freeMemory());
            WritableCallbackByteChannel writableCallbackByteChannel = new WritableCallbackByteChannel(outputChannel, src.length(), i);
//            JLog.e("TAG", "workDirect transferTo");
            long position = 0;
            final long blockSize = (4 * 1024 * 1024);
            while (inputChannel.transferTo(position, blockSize, writableCallbackByteChannel) > 0) {
                position += blockSize;
            }
        }
    }

    boolean work(int i, String srcPath) throws InterruptedException {
        File src = new File(srcPath);

        // 원본의 패스를 없애고 파일명과 확장자만 얻어옴
        String bodyPath = srcPath.replace(capturePath, "");
        String destPath = pastePath + bodyPath;

        // 대상 폴더의 상위폴더까지 무조건 생성
        String parentPath = FileHelper.getParentPath(destPath);
        File parent = new File(parentPath);
        parent.mkdirs();

        // 사본 생성 모드이면 새로운 파일 이름을 얻음
        if (makeCopy)
            destPath = FileHelper.getNewFileName(destPath);

        File dest = new File(destPath);

        // 대상 파일이 있는 경우 팝업을 물어보고 지우거나/스킵하거나/덮어씌운다.
        if (!applyAll && dest.exists()) {
            // 팝업을 띄운다.
            // 그리고 대기한다.
            alertOverwrite(destPath);
            synchronized (lock) {
                lock.wait();
            }

            if (cancel) {
                return false;
            }

            if (!skip) {
                // 파일을 지워 주어야 함
                dest.delete();
            }
        }

        // skip이 아니면, 위에서 이미 지웠으니 무조건 overwrite이다.
        if (!skip) {
            try {
                workDirect(i, src, dest);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

//    class ReadableCallbackByteChannel implements ReadableByteChannel {
//        ReadableByteChannel rbc;
//        long sizeRead;
//        long size;
//        int index;
//
//        ReadableCallbackByteChannel(ReadableByteChannel rbc, long size, int i) {
//            this.rbc = rbc;
//            this.size = size;
//            index = i;
//        }
//
//        @Override
//        public int read(ByteBuffer src) throws IOException {
//            int n;
//            int percent;
//            if ((n = rbc.read(src)) > 0) {
//                sizeRead += n;
//                percent = (int) (sizeRead * 100 / size);
//                JLog.e("TAG", "WritableCallbackByteChannel write i=" + index + " percent=" + percent);
//
//                // callback 영역
//                Progress progress = new Progress();
//                progress.index = index;
//                progress.percent = percent;
//
//                publishProgress(progress);
//            }
//            return n;
//        }
//
//        @Override
//        public boolean isOpen() {
//            return rbc.isOpen();
//        }
//
//        @Override
//        public void close() throws IOException {
//            rbc.close();
//        }
//    }

    class WritableCallbackByteChannel implements WritableByteChannel {

        private WritableByteChannel wbc;
        private long sizeWritten;
        private long size;
        private int index;

        WritableCallbackByteChannel(WritableByteChannel wbc, long size, int i) {
            this.wbc = wbc;
            this.size = size;
            index = i;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            int n;
            int percent;
            if ((n = wbc.write(src)) > 0) {
                sizeWritten += n;
                percent = (int) (sizeWritten * 100 / size);
//                JLog.e("TAG", "WritableCallbackByteChannel write i=" + index + " percent=" + percent);

                // callback 영역
                FileHelper.Progress progress = new FileHelper.Progress();
                progress.index = index;
                progress.percent = percent;

                publishProgress(progress);
            }
            return n;
        }

        @Override
        public boolean isOpen() {
            return wbc.isOpen();
        }

        @Override
        public void close() throws IOException {
            wbc.close();
        }
    }

    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];
//        JLog.w("PasteTask", "onProgressUpdate " + progress.index);

        String name = pasteList.get(progress.index).name;
        int size = pasteList.size();
        int totalPercent = progress.index * 100 / size;

        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
//            JLog.w("PasteTask", "dialog.hashCode " + dialog.hashCode());

            dialog.getFileName().setText(name);

            dialog.getEachProgress().setProgress(progress.percent);
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), progress.percent));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index, size, totalPercent));
            }
        } else {
//            JLog.e("PasteTask", "dialog is null");
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

//        JLog.w("PasteTask", "onPostExecute");
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            if (result) {
                if (cut)
                    ToastHelper.showToast(activity, R.string.toast_file_paste_cut);
                else
                    ToastHelper.showToast(activity, R.string.toast_file_paste_copy);
            } else {
                ToastHelper.showToast(activity, R.string.toast_cancel);
            }
        }

        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
