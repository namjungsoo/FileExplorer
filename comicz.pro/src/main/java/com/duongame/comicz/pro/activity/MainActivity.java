package com.duongame.comicz.pro.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duongame.comicz.pro.R;
import com.duongame.explorer.adapter.ComicPagerAdapter;
import com.duongame.explorer.fragment.ExplorerFragment;
import com.duongame.explorer.helper.ExplorerSearcher;
import com.duongame.explorer.helper.PositionManager;
import com.duongame.explorer.helper.PreferenceHelper;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static int PERMISSION_STORAGE = 1;

    ExplorerFragment fragment;
    ViewPager pager;
//    ComicPagerAdapter adapter;
    FragmentStatePagerAdapter adapter;
    TabLayout tab;

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // 해당하는 page의 Fragment를 생성합니다.
            //return PageFragment.create(position);

            //return new PageFragment();
            return new ExplorerFragment();
        }

        @Override
        public int getCount() {
            return 1;  // 총 5개의 page를 보여줍니다.
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = String.valueOf(position);
            Log.d("tag", "title="+title);
            return title;
        }

    }

    public static class PageFragment extends Fragment {
        private int mPageNumber;
        ViewGroup rootView;

//        public static PageFragment create(int pageNumber) {
//            PageFragment fragment = new PageFragment();
//            Bundle args = new Bundle();
//            args.putInt("page", pageNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }

//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
////            mPageNumber = getArguments().getInt("page");
//        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = (ViewGroup) inflater.inflate(R.layout.page_fragment, container, false);
            ((TextView) rootView.findViewById(R.id.number)).setText(mPageNumber + "");

//            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    rootView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d(TAG, "onGlobalLayout "+rootView.getWidth() + " " + rootView.getHeight());
//                        }
//                    });
//                }
//            });
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initTabs();

        // 단일 프라그먼트
//        FragmentManager fm = getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        fragment = new ExplorerFragment();
//        ft.replace(android.R.id.content, fragment);
//        ft.commit();
    }

    private void initTabs() {
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new ComicPagerAdapter(getSupportFragmentManager(), this);
//        adapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
//        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                Log.d(TAG, "onPageSelected=" + position);
//                fragment = (ExplorerFragment) adapter.getItem(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

//        ArrayList<Fragment> fragmentList = new ArrayList<>();
//        fragmentList.add(new ExplorerFragment());
//        adapter.setFragmentList(fragmentList);
//        adapter.notifyDataSetChanged();

//        fragment = (ExplorerFragment) adapter.getItem(0);
//
        tab = (TabLayout) findViewById(R.id.tab);
        tab.setupWithViewPager(pager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 동적 생성한다.
//        FragmentManager fm = getSupportFragmentManager();
//        fragment = (ExplorerFragment)fm.findFragmentById(R.id.fragment_explorer);

        if (checkStoragePermissions()) {
            final String lastPath = PreferenceHelper.getLastPath(MainActivity.this);
            final int position = PreferenceHelper.getLastPosition(MainActivity.this);
            final int top = PreferenceHelper.getLastTop(MainActivity.this);

            Log.d(TAG, "onCreate path=" + lastPath + " position=" + position + " top=" + top);

            PositionManager.setPosition(lastPath, position);
            PositionManager.setTop(lastPath, top);

            if (fragment != null)
                fragment.updateFileList(lastPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String read = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String write = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        boolean readEnable = false;
        boolean writeEnable = false;

        for (int i = 0; i < permissions.length; i++) {
            if (read.equals(permissions[i]) && grantResults[i] == 0)
                readEnable = true;
            if (write.equals(permissions[i]) && grantResults[i] == 0)
                writeEnable = true;
        }

        if (readEnable && writeEnable) {
            // 최초 이므로 무조건 null
            if (fragment != null)
                fragment.updateFileList(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (!ExplorerSearcher.isInitialPath()) {
            if (fragment != null)
                fragment.gotoUpDirectory();
        }
    }

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

}
