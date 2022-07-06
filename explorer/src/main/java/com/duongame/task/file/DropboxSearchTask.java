package com.duongame.task.file;

import android.os.AsyncTask;
import android.view.MenuItem;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.duongame.App;
import com.duongame.R;
import com.duongame.activity.main.BaseMainActivity;
import com.duongame.adapter.ExplorerItem;
import com.duongame.cloud.dropbox.DropboxClientFactory;
import com.duongame.file.FileExplorer;
import com.duongame.file.FileHelper;
import com.duongame.fragment.ExplorerFragment;
import com.duongame.helper.ToastHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.duongame.adapter.ExplorerItem.FILETYPE_PDF;
import static com.duongame.adapter.ExplorerItem.FILETYPE_TEXT;
import static com.duongame.adapter.ExplorerItem.FILETYPE_ZIP;

public class DropboxSearchTask extends AsyncTask<String, Void, FileExplorer.Result> {
    private WeakReference<ExplorerFragment> fragmentWeakReference;
    private String path;

    public DropboxSearchTask(ExplorerFragment fragment) {
        fragmentWeakReference = new WeakReference<>(fragment);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();// AsyncTask는 아무것도 안함

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        fragment.setCanClick(false);// 이제부터 클릭할수 없음
    }

    ExplorerItem createFolder(FolderMetadata metadata) {
        ExplorerItem item = new ExplorerItem(metadata.getPathDisplay(), metadata.getName(), null, 0, ExplorerItem.FILETYPE_FOLDER);
        item.metadata = metadata;
        return item;
    }

    ExplorerItem createFile(FileMetadata metadata) {
        // 압축파일일 경우 책(ZIP)으로 셋팅함
        // 추가적으로 TXT, PDF를 지원
        int type = FileHelper.getFileType(metadata.getName());

        switch (type) {
            case FILETYPE_ZIP:
            case FILETYPE_TEXT:
            case FILETYPE_PDF:
                break;
            default:
                type = ExplorerItem.FILETYPE_FILE;
                break;
        }

        ExplorerItem item = new ExplorerItem(metadata.getPathDisplay(), metadata.getName(), metadata.getServerModified().toString(), metadata.getSize(), type);
        item.metadata = metadata;
        return item;
    }

    @Override
    protected FileExplorer.Result doInBackground(String... strings) {
        path = strings[0];
        if (path == null)
            path = "/";
//            ExplorerFragment fragment = fragmentWeakReference.get();
//            if (fragment == null)
//                return null;

        if (path.equals("/"))
            path = path.replace("/", "");

        try {
            DbxClientV2 client = DropboxClientFactory.getClient();
            DbxUserFilesRequests requests = client.files();
            ListFolderResult listFolderResult = requests.listFolder(path);
            ArrayList<ExplorerItem> fileList = new ArrayList<>();

            if (listFolderResult != null) {
                List<Metadata> entries = listFolderResult.getEntries();
                if (entries != null) {
                    for (int i = 0; i < entries.size(); i++) {
                        Metadata metadata = entries.get(i);
                        if (metadata == null)
                            continue;

                        ExplorerItem item = null;
                        if (metadata instanceof FileMetadata) {
                            item = createFile((FileMetadata) metadata);
                        } else if (metadata instanceof FolderMetadata) {
                            item = createFolder((FolderMetadata) metadata);
                        } else
                            continue;

                        // 패스를 찾았다. 리스트에 더해주자.
                        fileList.add(item);
                        Timber.e("i=" + i + " " + item.toString());
                    }
                }

                FileExplorer.Result result = new FileExplorer.Result();
                result.fileList = fileList;
                return result;
            }
        } catch (DbxException e) {
            Timber.e(e.getLocalizedMessage());
        } catch (IllegalStateException e) {
            Timber.e(e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(FileExplorer.Result result) {
        super.onPostExecute(result);// AsyncTask는 아무것도 안함

        if (result == null) {
            onExit();
            return;
        }

        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        try {
            //FIX: Index Out of Bound
            // 쓰레드에서 메인쓰레드로 옮김
            fragment.setFileList(result.fileList);
            App.getInstance(fragment.getActivity()).setFileList(result.fileList);
            App.getInstance(fragment.getActivity()).setImageList(result.imageList);
            fragment.getAdapter().setFileList(fragment.getFileList());

            fragment.getAdapter().notifyDataSetChanged();

            // 성공했을때 현재 패스를 업데이트
            // 드롭박스에서만 앞에 /를 붙여준다.
            if (path.length() == 0)
                path = "/";

            App.getInstance(fragment.getActivity()).setLastPath(path);
            fragment.getTextPath().setText(path);
            fragment.getTextPath().requestLayout();

            fragment.setCanClick(true);

        } catch (NullPointerException e) {

        }
    }

    private void onExit() {
        ExplorerFragment fragment = fragmentWeakReference.get();
        if (fragment == null)
            return;

        // UI 업데이트
        fragment.updateDropboxUI(false);
        fragment.setCanClick(true);

        // 에러 메세지
        ToastHelper.showToast(fragment.getContext(), R.string.toast_error);

        BaseMainActivity activity = (BaseMainActivity) fragment.getActivity();
        if (activity == null)
            return;

        // 로그아웃 처리
        MenuItem item = activity.getGoogleDriveMenuItem();
        activity.logoutGoogleDrive(item);
    }
}
