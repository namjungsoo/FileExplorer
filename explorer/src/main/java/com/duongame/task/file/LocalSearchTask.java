package com.duongame.task.file;

import android.os.AsyncTask;
import android.view.View;

import com.duongame.adapter.ExplorerItem;
import com.duongame.file.FileExplorer;
import com.duongame.file.FileHelper;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.JLog;
import com.duongame.manager.PermissionManager;

import java.lang.ref.WeakReference;
import java.util.Comparator;

import static android.view.View.GONE;

public class LocalSearchTask extends AsyncTask<String, Void, FileExplorer.Result> {
    WeakReference<ExplorerFragment> fragmentWeakReference;
    boolean pathChanged;
    String path;
    Comparator<ExplorerItem> comparator;

    public LocalSearchTask(ExplorerFragment fragment, boolean pathChanged) {
        this.pathChanged = pathChanged;
        fragmentWeakReference = new WeakReference<>(fragment);

        JLog.e("Jungsoo", "LocalSearchTask begin");
    }

    void updateComparator() {
        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        if (fragment.getSortDirection() == 0) {// ascending
            switch (fragment.getSortType()) {
                case 0:
                    comparator = new FileHelper.NameAscComparator();
                    break;
                case 1:
                    comparator = new FileHelper.ExtAscComparator();
                    break;
                case 2:
                    comparator = new FileHelper.DateAscComparator();
                    break;
                case 3:
                    comparator = new FileHelper.SizeAscComparator();
                    break;
            }
        } else {
            switch (fragment.getSortType()) {// descending
                case 0:
                    comparator = new FileHelper.NameDescComparator();
                    break;
                case 1:
                    comparator = new FileHelper.ExtDescComparator();
                    break;
                case 2:
                    comparator = new FileHelper.DateDescComparator();
                    break;
                case 3:
                    comparator = new FileHelper.SizeDescComparator();
                    break;
            }
        }
    }

    @Override
    protected FileExplorer.Result doInBackground(String... params) {
        JLog.e("Jungsoo", "LocalSearchTask doInBackground begin");
        path = params[0];
        updateComparator();

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return null;

        FileExplorer.Result result = fragment.getFileExplorer()
                .setRecursiveDirectory(false)
                .setExcludeDirectory(false)
                .setComparator(comparator)
                .setHiddenFile(false)
                .setImageListEnable(true)
                .search(path);

        if (result == null) {
            result = new FileExplorer.Result();
        }
//            fragment.fileResult = result;

        JLog.e("Jungsoo", "LocalSearchTask doInBackground end");
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();// AsyncTask는 아무것도 안함

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        fragment.setCanClick(false);// 이제부터 클릭할수 없음
    }

    @Override
    protected void onPostExecute(FileExplorer.Result result) {
        JLog.e("Jungsoo", "LocalSearchTask onPostExecute begin");
        super.onPostExecute(result);// AsyncTask는 아무것도 안함

        if (isCancelled())
            return;

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        //FIX: Index Out of Bound
        // 쓰레드에서 메인쓰레드로 옮김
        fragment.setFileList(result.fileList);
        fragment.getApplication().setImageList(result.imageList);
        fragment.getAdapter().setFileList(fragment.getFileList());

        fragment.getAdapter().notifyDataSetChanged();

        // SearchTask가 resume
        if (pathChanged) {
            synchronized (fragment) {
                if (fragment.getFileList() != null && fragment.getFileList().size() > 0) {
                    fragment.getCurrentView().scrollToPosition(0);
                    fragment.getCurrentView().invalidate();
                }
            }
        }

        // 성공했을때 현재 패스를 업데이트
        fragment.getApplication().setLastPath(path);
        fragment.getTextPath().setText(path);
        fragment.getTextPath().requestLayout();

        if (fragment.getSwitcherContents() != null) {
            if (fragment.getFileList() == null || fragment.getFileList().size() <= 0) {
                fragment.getSwitcherContents().setDisplayedChild(1);

                // 퍼미션이 있으면 퍼미션 버튼을 보이지 않게 함
                if (PermissionManager.checkStoragePermissions()) {
                    fragment.getPermissionButton().setVisibility(GONE);
                    fragment.getTextNoFiles().setVisibility(View.VISIBLE);
                } else {
                    fragment.getPermissionButton().setVisibility(View.VISIBLE);
                    fragment.getTextNoFiles().setVisibility(GONE);
                }
            } else {
                fragment.getSwitcherContents().setDisplayedChild(0);
            }
        }

        fragment.setCanClick(true);
        JLog.e("Jungsoo", "LocalSearchTask onPostExecute end");
    }
}