package com.duongame.task.zip;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.UnzipDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.JLog;
import com.duongame.helper.ToastHelper;
import com.hzy.lib7z.ExtractCallback;
import com.hzy.lib7z.Z7Extractor;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

import static com.duongame.helper.FileHelper.BLOCK_SIZE;

/**
 * Created by namjungsoo on 2017-12-31.
 */

/*
6가지 파일을 테스트 해야 한다.
--
zip
tar/tar.gz/tar.bz2
gz
bz2
7z
rar
 */
public class UnzipTask extends AsyncTask<Void, FileHelper.Progress, Boolean> {
    private ArrayList<ExplorerItem> fileList;

    private WeakReference<UnzipDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    // 생성자에서 사용
    private DialogInterface.OnDismissListener onDismissListener;

    private String path;
    private boolean z7success;
    private int count;

    public UnzipTask(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // 폴더를 생성
        if (path != null) {
            File file = new File(path);
            if (!file.exists())
                file.mkdirs();
        }

        // Dialog의 초기화를 진행한다.
        dialogWeakReference = new WeakReference<>(new UnzipDialog());
        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UnzipTask.this.cancel(true);
                }
            });
            dialog.setOnDismissListener(onDismissListener);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "unzip");
            }
        }
    }

    //TODO: ZIP 현재는 1개 파일만 선택해서 압축을 풀으므로 i는 무시된다.
    void updateProgress(int j, int count, String name, int percent) {
        FileHelper.Progress progress = new FileHelper.Progress();
        progress.index = j;
        progress.percent = percent;
        progress.fileName = name;
        progress.count = count;
        publishProgress(progress);
    }

//    boolean unarchiveRar(ExplorerItem item, int i) throws IOException {
//        // 현재 동작안되고 다운됨
//        try {
//            Archive archive;
//            FileHeader header;
//
//            // 파일 갯수 세기
//            archive = new Archive(new File(item.path));
//            header = archive.nextFileHeader();
//            int size = 0;
//
//            while (header != null) {
//                if (isCancelled())
//                    return false;
//
//                size++;
//                header = archive.nextFileHeader();
//            }
//            archive.close();
//
//
//            archive = new Archive(new File(item.path));
//            header = archive.nextFileHeader();
//
//            int j = 0;
//            while (header != null) {
//                if (isCancelled())
//                    return false;
//
//                updateProgress(i, j, size, header.getFileNameString());
//
//                // 하위 폴더가 없을때 만들어줌
//                String target = path + "/" + header.getFileNameString();
//                new File(target).getParentFile().mkdirs();
//
//
//                // 파일 복사 부분
//                FileOutputStream os = new FileOutputStream(target);
//                archive.extractAll(header, os);
//                os.close();
//
//
//                header = archive.nextFileHeader();
//            }
//        } catch (RarException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

    boolean unarchiveGzip(ExplorerItem item) throws IOException {
        String tar;
        byte[] buf = new byte[BLOCK_SIZE];
        if (item.path.toLowerCase().endsWith(".tgz")) {
            tar = item.path.replace(".tgz", ".tar");
        } else {
            tar = item.path.replace(".gz", "");
        }
        File src = new File(item.path);
        GzipCompressorInputStream stream = new GzipCompressorInputStream(new FileInputStream(src));
        FileOutputStream outputStream = new FileOutputStream(tar);

        long srcLength = src.length();
        long totalRead = 0;
        int nRead = 0;
        while ((nRead = stream.read(buf)) > 0) {
            outputStream.write(buf, 0, nRead);

            totalRead += nRead;
            long percent = totalRead * 100 / srcLength;
            updateProgress(0, count + 1, tar, (int) percent);
        }

        if (tar.toLowerCase().endsWith(".tar")) {
            if (!unarchiveTar(tar))
                return false;

            // tar 파일 삭제
            new File(tar).delete();
        }
        return true;
    }

    boolean unarchiveBzip2(ExplorerItem item) throws IOException {
        byte[] buf = new byte[BLOCK_SIZE];
        String tar;
        if (item.path.toLowerCase().endsWith(".tbz2")) {
            tar = item.path.replace(".tbz2", ".tar");
        } else {
            tar = item.path.replace(".bz2", "");
        }

        File src = new File(item.path);
        BZip2CompressorInputStream stream = new BZip2CompressorInputStream(new FileInputStream(src));
        FileOutputStream outputStream = new FileOutputStream(tar);

        long srcLength = src.length();
        long totalRead = 0;
        int nRead = 0;
        while ((nRead = stream.read(buf)) > 0) {
            outputStream.write(buf, 0, nRead);

            totalRead += nRead;
            long percent = totalRead * 100 / srcLength;
            updateProgress(0, count + 1, tar, (int) percent);
        }

        if (tar.toLowerCase().endsWith(".tar")) {
            if (!unarchiveTar(tar))
                return false;

            // tar 파일 삭제
            new File(tar).delete();
        }

        return true;
    }

    boolean unarchiveTar(String tar) throws IOException {
        TarArchiveInputStream stream;
        TarArchiveEntry entry;
        byte[] buf = new byte[BLOCK_SIZE];

        // 파일 갯수를 위해서 두번 오픈한다.
        stream = new TarArchiveInputStream(new FileInputStream(tar));
        entry = (TarArchiveEntry) stream.getNextEntry();
        count = 0;

        // 파일 갯수를 세기
        while (entry != null) {
            if (isCancelled())
                return false;

            count++;
            entry = (TarArchiveEntry) stream.getNextEntry();
        }
        stream.close();

        stream = new TarArchiveInputStream(new FileInputStream(tar));
        entry = (TarArchiveEntry) stream.getNextEntry();

        int j = 0;
        while (entry != null) {
            if (isCancelled())
                return false;


            // 하위 폴더가 없을때 만들어줌
            String target = path + "/" + entry.getName();
            new File(target).getParentFile().mkdirs();

            // 파일 복사 부분
            FileOutputStream outputStream = new FileOutputStream(target);
            long srcLength = entry.getSize();
            long totalRead = 0;
            int nRead = 0;
            while ((nRead = stream.read(buf)) > 0) {
                outputStream.write(buf, 0, nRead);

                totalRead += nRead;
                long percent = totalRead * 100 / srcLength;
                updateProgress(j, count + 1, entry.getName(), (int) percent);
            }
            outputStream.close();

            entry = (TarArchiveEntry) stream.getNextEntry();
            j++;
        }

        stream.close();

        return true;
    }

    boolean unarchive7z(ExplorerItem item) throws IOException {
        // un7z를 사용함
        Z7Extractor extractor = new Z7Extractor();
        ArrayList<String> headers = extractor.getHeaders(item.path);

        extractor.extractAll(item.path, path, new ExtractCallback() {
            int count;
            int j;

            @Override
            public void onStart() {

            }

            @Override
            public void onGetFileNum(int fileNum) {
                count = fileNum;
                JLog.e("TAG", "7z fileNum=" + count);
            }

            @Override
            public void onProgress(String name, long size) {
                JLog.e("TAG", "7z onProgress name=" + name + " count=" + size + " j=" + j);
                updateProgress(j, count, name, 100);
                j++;
            }

            @Override
            public void onError(int errorCode, String message) {
                z7success = false;
            }

            @Override
            public void onSucceed() {
                z7success = true;
            }
        });

        return z7success;
    }

    boolean unarchiveZip(ExplorerItem item) throws IOException {
        ZipArchiveInputStream stream;
        ZipEntry entry;

        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
        entry = (ZipEntry) stream.getNextEntry();
        count = 0;

        // 파일 갯수를 세기
        while (entry != null) {
            if (isCancelled())
                return false;

            count++;
            entry = (ZipEntry) stream.getNextEntry();
        }
        stream.close();

        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
        entry = (ZipEntry) stream.getNextEntry();

        int j = 0;
        byte[] buf = new byte[BLOCK_SIZE];
        while (entry != null) {
            if (isCancelled())
                return false;


            // 하위 폴더가 없을때 만들어줌
            String target = path + "/" + entry.getName();
            new File(target).getParentFile().mkdirs();


            // 파일 복사 부분 
            FileOutputStream outputStream = new FileOutputStream(target);

            long srcLength = entry.getSize();
            long totalRead = 0;
            int nRead = 0;
            while ((nRead = stream.read(buf)) > 0) {
                outputStream.write(buf, 0, nRead);

                totalRead += nRead;
                long percent = totalRead * 100 / srcLength;

                updateProgress(j, count, entry.getName(), (int) percent);
            }

            outputStream.close();


            entry = (ZipEntry) stream.getNextEntry();
            j++;
        }

        stream.close();
        return true;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // 파일의 갯수만큼 루프를 돌아서 unzip 해준다.
//        for (int i = 0; i < fileList.size(); i++) {
        ExplorerItem item = fileList.get(0);

        try {
            ExplorerItem.CompressType type = FileHelper.getCompressType(item.path);
            switch (type) {
                case ZIP:
                    if (!unarchiveZip(item))
                        return false;
                    break;
                case SEVENZIP:
                    if (!unarchive7z(item))
                        return false;
                    break;
                case GZIP:
                    if (!unarchiveGzip(item))
                        return false;
                    break;
                case BZIP2:
                    if (!unarchiveBzip2(item))
                        return false;
                    break;
                //TODO: 현재는 풀수 없다.
//                    case RAR:
//                        if (!unarchiveRar(item))
//                            return false;
//                        break;
                case TAR:
                    if (!unarchiveTar(item.path))
                        return false;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
//        }
        return true;
    }

    //TODO: ZIP 현재는 파일 한개씩 밖에 풀수가 없다.
    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];

        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(progress.fileName);
            dialog.getEachProgress().setProgress(progress.percent);

            int totalPercent = progress.index * 100 / progress.count;
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), progress.percent));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index + 1, progress.count, totalPercent));
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Activity activity = activityWeakReference.get();
        if (activity != null) {
            if (result)
                ToastHelper.showToast(activity, R.string.toast_file_unzip);
            else
                ToastHelper.showToast(activity, R.string.toast_cancel);
        }

        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
