package com.duongame.fileexplorer.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.duongame.fileexplorer.ExplorerFileItem;
import com.duongame.fileexplorer.ExplorerSearcher;
import com.duongame.fileexplorer.R;
import com.duongame.fileexplorer.bitmap.BitmapCacheManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016-11-16.
 */

public class ViewerActivity extends AppCompatActivity {
    ViewPager pager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        BitmapCacheManager.recycleBitmap();

        pager = (ViewPager)findViewById(R.id.pager);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        ArrayList<ExplorerFileItem> imageList = ExplorerSearcher.getImageList();
        pagerAdapter.setImageList(imageList);
        pager.setAdapter(pagerAdapter);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String name = extras.getString("name");

            int item = 0;
            for(int i=0; i<imageList.size(); i++) {
                if(imageList.get(i).name.equals(name)) {
                    item = i;
                }
            }
            pager.setCurrentItem(item);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapCacheManager.recycleBitmap();
    }

    private static Bitmap decodeBitmapAndRemoveSides(String path, String prev, String next, ImageView imageView) {
        Bitmap bitmap = BitmapCacheManager.getBitmap(path);
        if(bitmap == null) {
            bitmap = BitmapFactory.decodeFile(path);
            BitmapCacheManager.setBitmap(path, bitmap, imageView);
        }

        return bitmap;
    }

    public static class RemoveBitmapTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            return null;
        }
    }

    public static class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public LoadBitmapTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            String prev = params[1];
            String next = params[2];

            return decodeBitmapAndRemoveSides(path, prev, next, imageViewReference.get());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
            Log.d("LoadBitmapTask", "onPostExecute");
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<ExplorerFileItem> imageList;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setImageList(ArrayList<ExplorerFileItem> imageList) {
            this.imageList = imageList;
        }
        @Override
        public Fragment getItem(int position) {
            // 해당하는 page의 Fragment를 생성합니다.
            String prev = null, next = null;
            if(position > 1) {
                prev = imageList.get(position-1).path;
            }

            if(position < imageList.size()-1) {
                next = imageList.get(position+1).path;
            }

            return PageFragment.create(position, imageList.get(position).path, prev, next);
        }

        @Override
        public int getCount() {
            return imageList.size();  // 총 5개의 page를 보여줍니다.
        }

    }

    public static class PageFragment extends Fragment {
        private String path, prev, next;

        public static PageFragment create(int position, String path, String prev, String next) {
            PageFragment fragment = new PageFragment();
            Bundle args = new Bundle();
            args.putString("path", path);
            args.putString("prev", prev);
            args.putString("next", next);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            path = getArguments().getString("path");
            prev = getArguments().getString("prev");
            next = getArguments().getString("next");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d("PageFragment", "onDestroy");

            BitmapCacheManager.removeBitmapImage(path);
            BitmapCacheManager.removeBitmap(path);

            // 나중에 쓰레드로 지우면 OOM 발생
//            RemoveBitmapTask task = new RemoveBitmapTask();
//            task.execute(path);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_page, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.image_viewer);

            Log.d("tag", "path="+path +" prev="+prev + " next="+next);

            // 로딩은 동적으로
            LoadBitmapTask task = new LoadBitmapTask(imageView);
            task.execute(path, prev, next);

            return rootView;
        }
    }
}
