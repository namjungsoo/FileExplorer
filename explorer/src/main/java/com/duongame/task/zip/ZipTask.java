package com.duongame.task.zip;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;

import com.duongame.R;
import com.duongame.adapter.ExplorerItem;
import com.duongame.dialog.ZipDialog;
import com.duongame.file.FileExplorer;
import com.duongame.file.FileHelper;
import com.duongame.file.SdCardExplorer;
import com.duongame.helper.ToastHelper;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.duongame.file.FileHelper.BLOCK_SIZE;

/**
 * Created by namjungsoo on 2017-12-31.
 */

public class ZipTask extends AsyncTask<Void, FileHelper.Progress, Boolean> {
    private ArrayList<ExplorerItem> fileList;
    private ArrayList<ExplorerItem> zipList;

    private WeakReference<ZipDialog> dialogWeakReference;
    private WeakReference<Activity> activityWeakReference;

    private DialogInterface.OnDismissListener onDismissListener;
    private String path;
    private int type;

    public ZipTask(Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    // 압축할 파일의 목록
    public void setFileList(ArrayList<ExplorerItem> fileList) {
        this.fileList = fileList;
    }

    // 압축 대상 파일의 패스
    public void setPath(String path) {
        this.path = path;
        type = FileHelper.getCompressType(path);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Dialog의 초기화를 진행한다.
        dialogWeakReference = new WeakReference<>(new ZipDialog());
        ZipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ZipTask.this.cancel(true);
                }
            });
            dialog.setOnDismissListener(onDismissListener);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.show(activity.getFragmentManager(), "zip");
            }
        }
    }

    void updateProgress(int i, String name, int percent) {
        FileHelper.Progress progress = new FileHelper.Progress();
        progress.index = i;
        progress.fileName = name;
        //progress.percent = i * 100 / zipList.count();
        progress.percent = percent;
        publishProgress(progress);
    }

    boolean archiveTar() {
        String tar = path.replace(".gz", "");
        TarArchiveOutputStream stream = null;
        try {
            stream = new TarArchiveOutputStream(new FileOutputStream(tar));
            byte[] buf = new byte[BLOCK_SIZE];

            for (int i = 0; i < zipList.size(); i++) {

                TarArchiveEntry entry = new TarArchiveEntry(zipList.get(i).name);
                File src = new File(zipList.get(i).path);
                FileInputStream inputStream = new FileInputStream(zipList.get(i).path);

                long srcLength = src.length();
                long totalRead = 0;
                int nRead = 0;

                entry.setSize(srcLength);
                stream.putArchiveEntry(entry);

                while ((nRead = inputStream.read(buf)) > 0) {
                    stream.write(buf, 0, nRead);

                    totalRead += nRead;
                    long percent = totalRead * 100 / srcLength;
                    updateProgress(i, zipList.get(i).name, (int) percent);
                }

                stream.closeArchiveEntry();
            }

            stream.finish();
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //region Tar를 먼저 수행하는 압축
    boolean archiveGzip() {
        if (!archiveTar())
            return false;

        String tar = path.replace(".gz", "");

        try {
            // gzip 압축
            File src = new File(tar);
            FileInputStream inputStream = new FileInputStream(src);
            byte[] buf = new byte[BLOCK_SIZE];

            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
            GzipCompressorOutputStream stream = new GzipCompressorOutputStream(outputStream);

            long srcLength = src.length();
            long totalRead = 0;
            int nRead = 0;
            while ((nRead = inputStream.read(buf)) > 0) {
                stream.write(buf, 0, nRead);

                totalRead += nRead;
                long percent = totalRead * 100 / srcLength;
                updateProgress(zipList.size(), path, (int) percent);
            }

            stream.close();
            inputStream.close();

            // tar 삭제
            src.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    boolean archiveBzip2() {
        if (!archiveTar())
            return false;

        String tar = path.replace(".bz2", "");

        try {
            // gzip 압축
            File src = new File(tar);
            FileInputStream inputStream = new FileInputStream(src);
            byte[] buf = new byte[BLOCK_SIZE];

            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
            BZip2CompressorOutputStream stream = new BZip2CompressorOutputStream(outputStream);

            long srcLength = src.length();
            long totalRead = 0;
            int nRead = 0;
            while ((nRead = inputStream.read(buf)) > 0) {
                stream.write(buf, 0, nRead);

                totalRead += nRead;
                long percent = totalRead * 100 / srcLength;
                updateProgress(zipList.size(), path, (int) percent);
            }

            stream.close();
            inputStream.close();

            // tar 삭제
            src.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    //endregion

    // 현재 지원 안됨
    boolean archive7z() {
        return true;
    }

    boolean archiveZip() {
        try {
            File src = new File(path);

            ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(src));
//            ZipArchiveOutputStream stream = new ZipArchiveOutputStream(src);

            byte[] buf = new byte[BLOCK_SIZE];

            for (int i = 0; i < zipList.size(); i++) {

                ZipEntry entry = new ZipEntry(zipList.get(i).name);
//                ZipArchiveEntry entry = new ZipArchiveEntry(zipList.get(i).name);

                FileInputStream inputStream = new FileInputStream(zipList.get(i).path);

                stream.putNextEntry(entry);
//                stream.putArchiveEntry(entry);

                long srcLength = src.length();
                long totalRead = 0;
                int nRead = 0;
                while ((nRead = inputStream.read(buf)) > 0) {
                    stream.write(buf, 0, nRead);

                    totalRead += nRead;
                    long percent = totalRead * 100 / srcLength;
                    updateProgress(i, zipList.get(i).name, (int) percent);
                }

                stream.closeEntry();
//                stream.closeArchiveEntry();
            }

            stream.finish();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void makeZipList() {
        zipList = new ArrayList<>();

        // fileList에는 선택된 파일만 들어옴
        for (int i = 0; i < fileList.size(); i++) {
            ExplorerItem item = fileList.get(i);

            if (item.type == ExplorerItem.FILETYPE_FOLDER) {
                // 폴더의 경우 하위 모든 아이템을 찾은뒤에 더한다.
                FileExplorer explorer = new SdCardExplorer();
                FileExplorer.Result result = explorer.setRecursiveDirectory(true)
                        .setHiddenFile(true)
                        .setExcludeDirectory(false)
                        .setImageListEnable(false)
                        .search(item.path);

                // 폴더 하위 파일의 경우에는 폴더 이름과 파일명을 적어줌
                if (result != null && result.fileList != null) {
                    for (int j = 0; j < result.fileList.size(); j++) {

                        // 선택된 폴더의 최상위 폴더의 폴더명을 제외한 나머지가 name임
                        ExplorerItem subItem = result.fileList.get(j);
                        if (subItem.path.startsWith(item.path)) {
                            subItem.name = subItem.path.replace(FileHelper.getParentPath(item.path) + "/", "");
                        }
                    }
                    zipList.addAll(result.fileList);
                }

                // 폴더 자기 자신도 더함
                // zip은 더하지 않음
//                zipList.add(item);
            } else {
                zipList.add(item);
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // 선택된 파일이 폴더인 경우에 전체 폴더 확장을 해야한다.
        makeZipList();

        switch (type) {
            case ExplorerItem.COMPRESSTYPE_ZIP:
                return archiveZip();
            // 구현이 완료될때까지 막아둠
//            case SEVENZIP:
//                return archive7z();
            case ExplorerItem.COMPRESSTYPE_GZIP:
                return archiveGzip();
            case ExplorerItem.COMPRESSTYPE_BZIP2:
                return archiveBzip2();
            case ExplorerItem.COMPRESSTYPE_TAR:
                return archiveTar();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(FileHelper.Progress... values) {
        FileHelper.Progress progress = values[0];

        ZipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.getFileName().setText(progress.fileName);
            dialog.getEachProgress().setProgress(progress.percent);

            // 들어온 퍼센트를 바로 토탈로 표시한다.
            //int totalPercent = progress.percent;
            int size;
            if (type == ExplorerItem.COMPRESSTYPE_GZIP || type == ExplorerItem.COMPRESSTYPE_BZIP2) {
                size = zipList.size() + 1;
            } else {
                size = zipList.size();
            }

            int totalPercent = (progress.index * 100) / size;
            dialog.getTotalProgress().setProgress(totalPercent);

            Activity activity = activityWeakReference.get();
            if (activity != null) {
                dialog.getEachText().setVisibility(View.VISIBLE);
                dialog.getEachText().setText(String.format(activity.getString(R.string.each_text), progress.percent));
                dialog.getTotalText().setVisibility(View.VISIBLE);
                dialog.getTotalText().setText(String.format(activity.getString(R.string.total_text), progress.index + 1, size, totalPercent));
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            if (result)
                ToastHelper.success(activity, R.string.toast_file_zip);
            else
                ToastHelper.error(activity, R.string.toast_cancel);
        }

        ZipDialog dialog = dialogWeakReference.get();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
