package com.duongame.task.file;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.PasteDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.FileSearcher;
import com.duongame.helper.ToastHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-12-29.
 */

public class PasteTask extends AsyncTask<Void, PasteTask.Progress, Void> {
    class Progress {
        int percent;
        int index;
    }

    private ArrayList<ExplorerItem> fileList;
    private ArrayList<ExplorerItem> pasteList;

    private String capturePath;
    private String pastePath;
    private boolean cut;

    // 생성자에서 사용
    private WeakReference<PasteDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    private DialogInterface.OnDismissListener onDismissListener;

    public PasteTask(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
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
    }

    public void setIsCut(boolean cut) {
        this.cut = cut;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialogWeakReference = new WeakReference<PasteDialog>(new PasteDialog());
        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog = new PasteDialog();

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

    @Override
    protected Void doInBackground(Void... voids) {
        pasteList = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);
            if (!item.selected)
                continue;

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

                pasteList.add(item);
            } else {
                pasteList.add(item);
            }
        }

        for (int i = 0; i < pasteList.size(); i++) {
            // 파일을 하나하나 지운다.
            try {
                if (isCancelled()) {
                    break;
                } else {
                    if (!work(pasteList.get(i).path)) {
                        Activity activity = activityWeakReference.get();
                        if (activity != null) {
                            ToastHelper.showToast(activity, R.string.toast_cancel);
                        }
                        return null;
                    }

                    Progress progress = new Progress();
                    progress.index = i;
                    progress.percent = 100;
                    publishProgress(progress);
                }
            } catch (SecurityException e) {
                // 지울수 없는 파일
                Activity activity = activityWeakReference.get();
                if (activity != null) {
                    ToastHelper.showToast(activity, R.string.toast_error);
                }
                return null;
            } catch (InterruptedException e) {
                // 지울수 없는 파일
                Activity activity = activityWeakReference.get();
                if (activity != null) {
                    ToastHelper.showToast(activity, R.string.toast_error);
                }
                return null;
            }
        }
        return null;
    }

    void alertOverwrite(String path) {
    }

    void workDirect(File src, File dest) {
        if (cut) {
            // 이동한다.
            try {
                FileUtils.moveFile(src, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 복사한다.
            // progress를 기입해야 한다.
        }
    }

    boolean work(String srcPath) throws InterruptedException {
        File src = new File(srcPath);
        String bodyPath = srcPath.replace(capturePath, "");
        String destPath = pastePath + bodyPath;

        // 대상 폴더의 상위폴더까지 무조건 생성
        String parentPath = FileHelper.getParentPath(destPath);
        File parent = new File(parentPath);
        parent.mkdirs();

        File dest = new File(destPath);

        // 대상 파일이 있는 경우 팝업을 물어보고 지우거나/스킵하거나/덮어씌운다.
//        if (!applyAll && dest.exists()) {
//            // 팝업을 띄운다.
//            // 그리고 대기한다.
//            alertOverwrite(destPath);
//            wait();
//
//            if (cancel) {
//                return false;
//            }
//
//            if (!skip) {
//                // 파일을 지워 주어야 함
//                dest.delete();
//            }
//        }
//
//        // skip이 아니면, 위에서 이미 지웠으니 무조건 overwrite이다.
//        if (!skip) {
//            workDirect(src, dest);
//        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        Progress progress = values[0];

        String name = pasteList.get(progress.index).name;
        int size = pasteList.size();
        float total = ((float) progress.index + 1) / size;
        int percent = (int) (total * 100);

        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(name);

            dialog.getEachProgress().setProgress(progress.percent);
            dialog.getTotalProgress().setProgress(percent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index + 1, size, percent));
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), 100));
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            ToastHelper.showToast(activity, R.string.toast_file_delete);
        }
        PasteDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
