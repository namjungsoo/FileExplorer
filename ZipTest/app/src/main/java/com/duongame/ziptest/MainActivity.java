package com.duongame.ziptest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.duongame.ziptest.compress.RarFile;
import com.duongame.ziptest.compress.Z7File;
import com.duongame.ziptest.compress.ZipApacheFile;
import com.duongame.ziptest.compress.ZipJavaFile;
import com.duongame.ziptest.compress.common.ArchiveHeader;
import com.duongame.ziptest.util.PermissionActivity;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends PermissionActivity {

    String download = "/storage/emulated/0/Download/";
    String bookzip = "주방의 마법사 01.zip";
    String bookrar = "주방의 마법사 01-rar.rar";
    String book7z = "주방의 마법사 01-7z.7z";

    int count = 1;

    void zip4j() {
        long begin, end, delta, accum;
        try {
            ZipFile file = new ZipFile(download + bookzip);
            begin = System.currentTimeMillis();
            List<FileHeader> headers = file.getFileHeaders();
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "zip4j getFileHeaders=" + delta);

            accum = 0;
            new File(download + "Out").mkdirs();
            for (int i = 0; i < headers.size(); i++) {
                //for (int i = 0; i < count; i++) {
                begin = System.currentTimeMillis();
                file.extractFile(headers.get(i).getFileName(), download + "Out");
                end = System.currentTimeMillis();
                delta = end - begin;
                accum += delta;
                Log.e("ZIP", "zip4j extractFile i=" + i + " " + delta);
            }
            Log.e("ZIP", "zip4j accum=" + accum);

        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    void zipApache() {
        long begin, end, delta, accum;
        try {
            ZipApacheFile file = new ZipApacheFile(download + bookzip);
            begin = System.currentTimeMillis();
            ArrayList<ArchiveHeader> headers = file.getHeaders();
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "zipApache getFileHeaders=" + delta);

            accum = 0;
            new File(download + "Out").mkdirs();
            for (int i = 0; i < headers.size(); i++) {
                //for (int i = 0; i < count; i++) {
                begin = System.currentTimeMillis();
                file.extractFile(headers.get(i).getName(), download + "Out");
                end = System.currentTimeMillis();
                delta = end - begin;
                accum += delta;
                Log.e("ZIP", "zipApache extractFile i=" + i + " " + delta);
            }
            Log.e("ZIP", "zipApache accum=" + accum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void zipJava() {
        long begin, end, delta, accum;
        try {
            ZipJavaFile file = new ZipJavaFile(download + bookzip);
            begin = System.currentTimeMillis();
            ArrayList<ArchiveHeader> headers = file.getHeaders();
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "zipJava getFileHeaders=" + delta);

            accum = 0;
            new File(download + "Out").mkdirs();
            for (int i = 0; i < headers.size(); i++) {
                //for (int i = 0; i < count; i++) {
                begin = System.currentTimeMillis();
                file.extractFile(headers.get(i).getName(), download + "Out");
                end = System.currentTimeMillis();
                delta = end - begin;
                accum += delta;
                Log.e("ZIP", "zipJava extractFile i=" + i + " " + delta);
            }
            Log.e("ZIP", "zipJava accum=" + accum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void z7() {
        long begin, end, delta, accum;
        Log.e("ZIP", "z7");
        try {
            Z7File file = new Z7File(download + book7z);
            begin = System.currentTimeMillis();
            ArrayList<ArchiveHeader> headers = file.getHeaders();
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "z7 getFileHeaders=" + delta);

            accum = 0;
            new File(download + "Out").mkdirs();
            for (int i = 0; i < headers.size(); i++) {
                //for (int i = 0; i < count; i++) {
                begin = System.currentTimeMillis();
                file.extractFile(headers.get(i).getName(), download + "Out");
                end = System.currentTimeMillis();
                delta = end - begin;
                accum += delta;
                Log.e("ZIP", "z7 extractFile i=" + i + " " + delta);
            }
            Log.e("ZIP", "z7 accum=" + accum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void rar() {
        long begin, end, delta, accum;
        try {
            RarFile file = new RarFile(download + bookrar);
            begin = System.currentTimeMillis();
            ArrayList<ArchiveHeader> headers = file.getHeaders();
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "rar getFileHeaders=" + delta);

            accum = 0;
            new File(download + "Out").mkdirs();
            for (int i = 0; i < headers.size(); i++) {
                //for (int i = 0; i < count; i++) {
                begin = System.currentTimeMillis();
                file.extractFile(headers.get(i).getName(), download + "Out");
                end = System.currentTimeMillis();
                delta = end - begin;
                accum += delta;
                Log.e("ZIP", "rar extractFile i=" + i + " " + delta);
            }
            Log.e("ZIP", "rar accum=" + accum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void zip4j_a() {
        long begin, end, delta, accum;
        try {
            ZipFile file = new ZipFile(download + bookzip);
            file.setRunInThread(true);
            begin = System.currentTimeMillis();
            file.extractAll(download + "Out");

            ProgressMonitor monitor = file.getProgressMonitor();
            while (monitor.getState() == ProgressMonitor.STATE_BUSY) {
                if (monitor.getCurrentOperation() == ProgressMonitor.OPERATION_EXTRACT) {
                    Log.e("ZIP", "zip4j extractAll filename=" + monitor.getFileName() + monitor.getPercentDone());
                }
            }

            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "zip4j extractAll=" + delta);

        } catch (ZipException e) {
            e.printStackTrace();
        }

    }

    void rar_a() {
        long begin, end, delta, accum;
        try {
            RarFile file = new RarFile(download + bookrar);
            begin = System.currentTimeMillis();
            file.extractAll(download + "Out");
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "rar extractAll=" + delta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void z7_a() {
        long begin, end, delta, accum;
        try {
            Z7File file = new Z7File(download + book7z);
            begin = System.currentTimeMillis();
            file.extractAll(download + "Out");
            end = System.currentTimeMillis();
            delta = end - begin;
            Log.e("ZIP", "z7 extractAll=" + delta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zip4j();
            }
        });
        Button btn11 = findViewById(R.id.btn11);
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zipApache();
            }
        });
        Button btn12 = findViewById(R.id.btn12);
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zipJava();
            }
        });
        Button btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rar();
            }
        });
        Button btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                z7();
            }
        });


        Button btn4 = findViewById(R.id.btn4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zip4j_a();
            }
        });
        Button btn5 = findViewById(R.id.btn5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rar_a();
            }
        });
        Button btn6 = findViewById(R.id.btn6);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                z7_a();
            }
        });
    }
}
