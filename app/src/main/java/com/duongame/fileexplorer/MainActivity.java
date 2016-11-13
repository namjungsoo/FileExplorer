package com.duongame.fileexplorer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ExplorerAdapter adapter;
    ExplorerSearcher searcher;
    ArrayList<ExplorerFileItem> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileList = new ArrayList<>();
        searcher = new ExplorerSearcher();

        ListView listView = (ListView)findViewById(R.id.list_explorer);
        fileList = searcher.search(null);
//        fileList.add(new ExplorerFileItem("파일명이 졸라 길때를 테스트 해보자.", "2016-11-05", "1.0MB", ExplorerFileItem.FileType.DIRECTORY));
//        fileList.add(new ExplorerFileItem("file2", "2016-11-05 11:10 AM", "1.0MB", ExplorerFileItem.FileType.DIRECTORY));
//        fileList.add(new ExplorerFileItem("file3", "2016-11-05 11:10 AM", "1.0MB", ExplorerFileItem.FileType.DIRECTORY));

        adapter = new ExplorerAdapter(this, fileList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExplorerFileItem item = fileList.get(position);
                if(item.type == ExplorerFileItem.FileType.DIRECTORY) {
                    String newPath = searcher.getLastPath() + "/" + item.name;
                    fileList = searcher.search(newPath);
                    adapter.setFileList(fileList);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_file);
                if(checkBox != null) {
                    checkBox.setVisibility(View.VISIBLE);
                }

                return true;
            }
        });

        adapter.notifyDataSetChanged();
    }
}
