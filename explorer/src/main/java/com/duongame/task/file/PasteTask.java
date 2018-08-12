package com.duongame.task.file;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.OverwriteDialog;
import com.duongame.dialog.PasteDialog;
import com.duongame.file.FileExplorer;
import com.duongame.file.FileHelper;
import com.duongame.file.LocalExplorer;
import com.duongame.helper.JLog;
import com.duongame.helper.ToastHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import static com.duongame.file.FileHelper.BLOCK_SIZE;

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

    private boolean cancelled;
    private HashMap<String, String> srcNewFolderMap;

    private ArrayList<String> createdPathList;

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
            srcNewFolderMap = new HashMap<>();
        }
    }

    public void setIsCut(boolean cut) {
        this.cut = cut;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

//        JLog.w("PasteTask", "onPreExecute");
        createdPathList = new ArrayList<>();
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

    FileExplorer.Result searchFile(String path) {
        FileExplorer explorer = new LocalExplorer();
        FileExplorer.Result result = explorer.setRecursiveDirectory(true)
                .setHiddenFile(true)
                .setExcludeDirectory(false)
                .setImageListEnable(false)
                .search(path);
        return result;
    }

    void prepareFolder(FileExplorer.Result result, String path) {
        // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
        if (result != null && result.fileList != null) {
            for (int j = 0; j < result.fileList.size(); j++) {
                prepareLocalPathToName(result, path, j);
            }
            // 7ztest/_DSC5307.jpg
            pasteList.addAll(result.fileList);
        }
    }

    void prepareLocalPathToName(FileExplorer.Result result, String path, int j) {
        // 선택된 폴더의 최상위 폴더의 폴더명을 제외한 나머지가 name임
        ExplorerItem subItem = result.fileList.get(j);
        if (!subItem.path.startsWith(path))
            return;

        // 상대 패스를 만들어 줌
        // 이거는 Download
        // item.path는 7ztest
        String parentPath = FileHelper.getParentPath(path);

        // 이 결과를 이름에 박아둠
        subItem.name = subItem.path.replace(parentPath + "/", "");
    }

    void preparePasteList() {
        pasteList = new ArrayList<>();

        // fileList에는 선택된 파일만 들어옴
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            if (item.type == ExplorerItem.FILETYPE_FOLDER) {

                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                FileExplorer.Result result = searchFile(item.path);
                for (int j = 0; j < result.fileList.size(); j++) {
                    JLog.e("TAG", "fileList j=" + j + " " + result.fileList.get(j).path);
                }
                prepareFolder(result, item.path);

                // 폴더 자기 자신도 더함
                // 폴더를 나중에 더할 경우 폴더를 복사할때 에러가 발생함
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
                    cancelled = true;
                    return false;
                } else {
                    if (!process(i, pasteList.get(i).path)) {
                        return false;
                    }
                }
            } catch (SecurityException e) {
                // 지울수 없는 파일
                return false;
            } catch (InterruptedException e) {
                // 지울수 없는 파일
                return false;
            }
        }
        return true;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        preparePasteList();
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
        progress.percent = percent;

        publishProgress(progress);
    }

    void processInternal(int i, File src, File dest) throws IOException {
        if (cut) {
            if (src.isDirectory()) {
                // src가 디렉토리 이면, 이미 하위 파일은 폴더를 생성하고 옮겼으므로 delete해준다.
                JLog.e("TAG", "delete folder path=" + src.getPath());
                src.delete();
                //FileUtils.forceDelete(src);
                JLog.e("TAG", "complete delete folder path=" + src.getPath());
            } else {
                JLog.e("TAG", "move folder path=" + src.getPath());
                // 이동한다.
                FileUtils.moveFile(src, dest);
                updateProgress(i, 100);
            }
        } else {
            // 복사한다.
//            JLog.e("TAG", "processInternal copy");

            FileInputStream inputStream = new FileInputStream(src);
            FileOutputStream outputStream = new FileOutputStream(dest);

            byte[] buf = new byte[BLOCK_SIZE];
            int nRead = 0;
            long totalRead = 0;

            long srcLength = src.length();

            while ((nRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, nRead);
                totalRead += nRead;

                long percent = totalRead * 100 / srcLength;
                JLog.e("TAG", "percent=" + percent + " totalRead=" + totalRead + " srcLength=" + srcLength);

                updateProgress(i, (int) percent);
            }

            outputStream.close();
            inputStream.close();
        }
    }

    boolean checkOverwrite(File dest, String destPath) throws InterruptedException {
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
        return true;
    }

    void processParentFolder(String destPath) {
        // 대상 폴더의 상위폴더까지 무조건 생성
        String parentPath = FileHelper.getParentPath(destPath);
        File parent = new File(parentPath);
        parent.mkdirs();
    }

    String getRelativePath(String srcPath) {
        return srcPath.replace(capturePath + "/", "");// 상대적인 패스
    }

    String processDestPath(String relativePath) {
        // 원본의 패스를 없애고 파일명과 확장자만 얻어옴
        //String relativePath = srcPath.replace(capturePath + "/", "");// 상대적인 패스

        // makeCopy일때 상대패스가 폴더를 포함하면
        if (makeCopy) {// 이때는 pastePath와 capturePath가 같다.
            if (relativePath.contains("/")) {
                // 첫번째 폴더명과 이후의 패스를 분리함
                String subPath = relativePath.substring(relativePath.indexOf("/") + 1);

                // 첫번째 패스만을 골라낸다.
                String copyPath = capturePath + "/" + relativePath.substring(0, relativePath.indexOf("/"));

                String newPath;
                if (srcNewFolderMap.containsKey(copyPath)) {
                    newPath = srcNewFolderMap.get(copyPath);
                } else {
                    // 첫번째 패스의 새로운 패스를 찾는다.
                    newPath = FileHelper.getNewFileName(copyPath);
                    srcNewFolderMap.put(copyPath, newPath);
                }

                // 새로운 패스 + 이후의 패스
                return newPath + "/" + subPath;
            } else {
                return FileHelper.getNewFileName(pastePath + "/" + relativePath);
            }
        } else {
            return pastePath + "/" + relativePath;// 대상 패스 + 상대적인 패스
        }
    }

    String getTopPath(String relativePath) {
        int index = relativePath.indexOf("/");
        if (index == -1)
            return null;
        else
            return relativePath.substring(0, index);
    }

    // srcPath는 pasteList로부터 가져온 패스의 원본
    boolean process(int i, String srcPath) throws InterruptedException {
        File src = new File(srcPath);

        String relativePath = getRelativePath(srcPath);
        String destPath = processDestPath(relativePath);
        //String destPath = processDestPath(srcPath);

        // top의 폴더가 이미 있을 경우에는 폴더를 새로 만들어줌
        // 그리고 이하의 모든 패스를 변경하여야 함
        String topPath = getTopPath(relativePath);
        if (topPath != null) {
            String pasteTopPath = pastePath + "/" + topPath;
            File top = new File(pasteTopPath);
            if (top.exists() && top.isDirectory()) {
            } else {
                // 존재하지 않으면 내가 만들었다고 체크해둠
                createdPathList.add(pasteTopPath);
            }
        }

        // 이건 그냥 모든 폴더를 만들어 주는 것
        processParentFolder(destPath);

        // 내가 만든 폴더에 속하면 overwrite는 테스트 하지 않음
        File dest = new File(destPath);

        // top path가 아닌 하위 폴더는 관여 안함
        if (dest.isDirectory() && !destPath.equals(topPath)) {

        } else {
            if (createdPathList.indexOf(destPath) == -1) {
                if (!checkOverwrite(dest, destPath))
                    return false;
            }
        }

        // skip이 아니면, 위에서 이미 지웠으니 무조건 overwrite이다.
        if (!skip) {
            try {
                processInternal(i, src, dest);
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
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index + 1, size, totalPercent));
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
                    ToastHelper.info(activity, R.string.toast_file_paste_cut);
                else
                    ToastHelper.info(activity, R.string.toast_file_paste_copy);
            } else {
                if (cancelled)
                    ToastHelper.error(activity, R.string.toast_cancel);
                else
                    ToastHelper.error(activity, R.string.toast_error);
            }
        }

        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
