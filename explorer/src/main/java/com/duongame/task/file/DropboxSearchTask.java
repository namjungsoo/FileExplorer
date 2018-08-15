package com.duongame.task.file;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.duongame.adapter.ExplorerItem;
import com.duongame.cloud.dropbox.DropboxClientFactory;
import com.duongame.file.FileExplorer;
import com.duongame.fragment.ExplorerFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
        return item;
    }

    ExplorerItem createFile(FileMetadata metadata) {
        ExplorerItem item = new ExplorerItem(metadata.getPathDisplay(), metadata.getName(), metadata.getServerModified().toString(), metadata.getSize(), ExplorerItem.FILETYPE_FILE);
        return item;
    }

    @Override
    protected FileExplorer.Result doInBackground(String... strings) {
        path = strings[0];
        if (path == null)
            path = "";
//            ExplorerFragment fragment = fragmentWeakReference.get();
//            if (fragment == null)
//                return null;

        DbxClientV2 client = DropboxClientFactory.getClient();
        try {
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
                        Log.e("Jungsoo", "i=" + i + " " + item.toString());
                    }
                }

                FileExplorer.Result result = new FileExplorer.Result();
                result.fileList = fileList;
                return result;
            }
        } catch (DbxException e) {
            Log.e("Jungsoo", e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(FileExplorer.Result result) {
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

        // 성공했을때 현재 패스를 업데이트
        fragment.getApplication().setLastPath(path);
        fragment.getTextPath().setText(path);
        fragment.getTextPath().requestLayout();

        fragment.setCanClick(true);
    }
}
