package com.duongame.task.file;

import android.os.AsyncTask;
import android.view.View;

import com.duongame.MainApplication;
import com.duongame.adapter.ExplorerItem;
import com.duongame.db.ExplorerItemDB;
import com.duongame.db.ExplorerItemDao;
import com.duongame.file.FileExplorer;
import com.duongame.file.FileHelper;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.manager.PermissionManager;

import java.lang.ref.WeakReference;
import java.util.Comparator;

import timber.log.Timber;

import static android.view.View.GONE;

public class LocalSearchTask extends AsyncTask<String, Void, FileExplorer.Result> {
    private WeakReference<ExplorerFragment> fragmentWeakReference;

    // 기본값 false
    private boolean pathChanged;
    private boolean disableUpdateCanClick;
    private String path;
    private Comparator<ExplorerItem> comparator;

    public LocalSearchTask(ExplorerFragment fragment, boolean pathChanged, boolean disableUpdateCanClick) {
        this(fragment, pathChanged);
        this.disableUpdateCanClick = disableUpdateCanClick;
    }

    public LocalSearchTask(ExplorerFragment fragment, boolean pathChanged) {
        this.pathChanged = pathChanged;
        fragmentWeakReference = new WeakReference<>(fragment);

        Timber.e("LocalSearchTask begin");
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
        Timber.e("LocalSearchTask doInBackground begin");
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
                .setVideoListEnable(true)
                .setAudioListEnable(true)
                .search(path);

        if (result == null) {
            result = new FileExplorer.Result();
        }
//            fragment.fileResult = result;

        Timber.e("LocalSearchTask doInBackground end");
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();// AsyncTask는 아무것도 안함

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        if (!disableUpdateCanClick) {
            fragment.setCanClick(false);// 이제부터 클릭할수 없음. 프로그래스바 표시
        }
    }

    @Override
    protected void onPostExecute(FileExplorer.Result result) {
        Timber.e("LocalSearchTask onPostExecute begin");
        super.onPostExecute(result);// AsyncTask는 아무것도 안함

        if (isCancelled())
            return;

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        try {
            //FIX: Index Out of Bound
            // 쓰레드에서 메인쓰레드로 옮김
            fragment.setFileList(result.fileList);
            MainApplication.getInstance(fragment.getActivity()).setFileList(result.fileList);
            MainApplication.getInstance(fragment.getActivity()).setImageList(result.imageList);
            MainApplication.getInstance(fragment.getActivity()).setVideoList(result.videoList);
            MainApplication.getInstance(fragment.getActivity()).setAudioList(result.audioList);
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
            MainApplication.getInstance(fragment.getActivity()).setLastPath(path);
            fragment.getTextPath().setText(path);
            fragment.getTextPath().requestLayout();

            if (fragment.getSwitcherContents() != null) {
                if (fragment.getFileList() == null || fragment.getFileList().size() <= 0) {
                    fragment.getSwitcherContents().setDisplayedChild(1);

                    // 퍼미션이 있으면 퍼미션 버튼을 보이지 않게 함
                    if (PermissionManager.checkStoragePermissions(fragment.getActivity())) {
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
            new Thread(() -> {
                // 결과가 왔으므로 DB에 저장해 준다.
                Timber.e("LocalSearchTask doInBackground DB begin");
                ExplorerItemDao dao = ExplorerItemDB.Companion.getInstance(fragment.getContext()).getDb().explorerItemDao();
                dao.deleteAll();
                if (result.fileList != null) {
                    for(ExplorerItem item : result.fileList) {
                        Timber.e(item.toString());
                    }
                    dao.insertItems(result.fileList);
                }
                Timber.e("LocalSearchTask doInBackground DB end");
            }).start();
            Timber.e("LocalSearchTask onPostExecute end");
        } catch (NullPointerException e) {

        }
    }
}