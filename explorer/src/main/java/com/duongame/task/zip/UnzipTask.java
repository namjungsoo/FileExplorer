package com.duongame.task.zip;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.UnzipDialog;
import com.duongame.helper.FileHelper;
import com.duongame.helper.ToastHelper;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

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
    void updateProgress(int i, int j, int size, String name) {
        FileHelper.Progress progress = new FileHelper.Progress();
        progress.index = j;
        progress.percent = 100;
        progress.fileName = name;
        progress.size = size;
        publishProgress(progress);
    }

    boolean unarchiveRar(ExplorerItem item, int i) throws IOException {
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
//                archive.extractFile(header, os);
//                os.close();
//
//
//                header = archive.nextFileHeader();
//            }
//        } catch (RarException e) {
//            e.printStackTrace();
//            return false;
//        }
        return true;
    }

    boolean unarchiveGzip(ExplorerItem item, int i) throws IOException {
        String tar;
        if (item.path.toLowerCase().endsWith(".tgz")) {
            tar = item.path.replace(".tgz", ".tar");
        } else {
            tar = item.path.replace(".gz", "");
        }

        GzipCompressorInputStream stream = new GzipCompressorInputStream(new FileInputStream(item.path));
        FileOutputStream outputStream = new FileOutputStream(tar);
        IOUtils.copy(stream, outputStream);

        if (tar.toLowerCase().endsWith(".tar")) {
            if (!unarchiveTar(tar, i))
                return false;
            // tar 파일 삭제
            new File(tar).delete();
        }
        return true;
    }

    boolean unarchiveBzip2(ExplorerItem item, int i) throws IOException {
        String tar;
        if (item.path.toLowerCase().endsWith(".tbz2")) {
            tar = item.path.replace(".tbz2", ".tar");
        } else {
            tar = item.path.replace(".bz2", "");
        }

        BZip2CompressorInputStream stream = new BZip2CompressorInputStream(new FileInputStream(item.path));
        FileOutputStream outputStream = new FileOutputStream(tar);
        IOUtils.copy(stream, outputStream);

        if (tar.toLowerCase().endsWith(".tar")) {
            if (!unarchiveTar(tar, i))
                return false;

            // tar 파일 삭제
            new File(tar).delete();
        }

        return true;
    }

    boolean unarchiveTar(String path, int i) throws IOException {
        TarArchiveInputStream stream;
        TarArchiveEntry entry;

        stream = new TarArchiveInputStream(new FileInputStream(path));
        entry = (TarArchiveEntry) stream.getNextEntry();
        int size = 0;

        // 파일 갯수를 세기
        while (entry != null) {
            if (isCancelled())
                return false;

            size++;
            entry = (TarArchiveEntry) stream.getNextEntry();
        }
        stream.close();


        stream = new TarArchiveInputStream(new FileInputStream(path));
        entry = (TarArchiveEntry) stream.getNextEntry();

        int j = 0;
        while (entry != null) {
            if (isCancelled())
                return false;

            updateProgress(i, j, size, entry.getName());

            // 하위 폴더가 없을때 만들어줌
            String target = path + "/" + entry.getName();
            new File(target).getParentFile().mkdirs();

            // 파일 복사 부분
            FileOutputStream outputStream = new FileOutputStream(target);
            IOUtils.copy(stream, outputStream);
            outputStream.close();


            entry = (TarArchiveEntry) stream.getNextEntry();
            j++;
        }

        stream.close();

        return true;
    }

    boolean unarchive7z(ExplorerItem item, int i) throws IOException {
        SevenZFile sevenZFile;
        SevenZArchiveEntry entry;

        sevenZFile = new SevenZFile(new File(item.path));
        entry = sevenZFile.getNextEntry();
        int size = 0;

        while (entry != null) {
            if (isCancelled())
                return false;

            size++;
            entry = sevenZFile.getNextEntry();
        }
        sevenZFile.close();

        sevenZFile = new SevenZFile(new File(item.path));
        entry = sevenZFile.getNextEntry();

        int j = 0;
        while (entry != null) {
            if (isCancelled())
                return false;

            updateProgress(i, j, size, entry.getName());

            // 하위 폴더가 없을때 만들어줌
            String target = path + "/" + entry.getName();
            new File(target).getParentFile().mkdirs();


            // 파일 복사 부분
            FileOutputStream outputStream = new FileOutputStream(target);
            byte[] content = new byte[(int) entry.getSize()];
            sevenZFile.read(content, 0, content.length);
            outputStream.write(content);
            outputStream.close();


            entry = sevenZFile.getNextEntry();
            j++;
        }

        sevenZFile.close();
        return true;
    }

    boolean unarchiveZip(ExplorerItem item, int i) throws IOException {
        ZipArchiveInputStream stream;
        ZipEntry entry;

        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
        entry = (ZipEntry) stream.getNextEntry();
        int size = 0;

        // 파일 갯수를 세기
        while (entry != null) {
            if (isCancelled())
                return false;

            size++;
            entry = (ZipEntry) stream.getNextEntry();
        }
        stream.close();

        stream = new ZipArchiveInputStream(new FileInputStream(item.path));
        entry = (ZipEntry) stream.getNextEntry();

        int j = 0;
        while (entry != null) {
            if (isCancelled())
                return false;

            updateProgress(i, j, size, entry.getName());

            // 하위 폴더가 없을때 만들어줌
            String target = path + "/" + entry.getName();
            new File(target).getParentFile().mkdirs();


            // 파일 복사 부분 
            FileOutputStream outputStream = new FileOutputStream(target);
            IOUtils.copy(stream, outputStream);
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
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            try {
                ExplorerItem.CompressType type = FileHelper.getCompressType(item.path);
                switch (type) {
                    case ZIP:
                        if (!unarchiveZip(item, i))
                            return false;
                        break;
                    case SEVENZIP:
                        if (!unarchive7z(item, i))
                            return false;
                        break;
                    case GZIP:
                        if (!unarchiveGzip(item, i))
                            return false;
                        break;
                    case BZIP2:
                        if (!unarchiveBzip2(item, i))
                            return false;
                        break;
                    case RAR:
                        if (!unarchiveRar(item, i))
                            return false;
                        break;
                    case TAR:
                        if (!unarchiveTar(item.path, i))
                            return false;
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //TODO: ZIP 현재는 파일 한개씩 밖에 풀수가 없다.
    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];

        UnzipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(progress.fileName);

            dialog.getEachProgress().setProgress(100);

            int totalPercent = progress.index * 100 / progress.size;
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), 100));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index, progress.size, totalPercent));
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
