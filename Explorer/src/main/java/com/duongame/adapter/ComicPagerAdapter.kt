package com.duongame.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.duongame.R
import com.duongame.fragment.BaseFragment
import com.duongame.fragment.ExplorerFragment
import com.duongame.fragment.HistoryFragment
import com.duongame.fragment.SearchFragment

/**
 * Created by namjungsoo on 2016. 12. 30..
 */
class ComicPagerAdapter(fm: FragmentManager, private val context: Activity) : FragmentPagerAdapter(fm) {
    private val PAGE_COUNT = 3
    private val fragmentMap = HashMap<Int, BaseFragment>()
    private var lastPosition = -1
    override fun getCount(): Int {
        //TODO: 나중에 동적으로 수정해야 한다.
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        var fragment: BaseFragment? = null
        fragment = fragmentMap[position]
        if (fragment == null) {
            when (position) {
                0 -> {
                    fragment = ExplorerFragment()
                    fragmentMap[position] = fragment
                }
                1 -> {
                    fragment = HistoryFragment()
                    fragmentMap[position] = fragment
                }
                2 -> {
                    fragment = SearchFragment()
                    fragmentMap[position] = fragment
                }
            }
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return context.resources.getString(R.string.explorer)
            1 -> return context.resources.getString(R.string.history)
            2 -> return context.resources.getString(R.string.search)
        }
        return ""
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (position != lastPosition) {
            lastPosition = position
            if (fragmentMap.containsKey(position)) {
                val fragment = fragmentMap[position]
                fragment!!.onRefresh()
            }
        }
    }
    //    @Override
    //    public boolean isViewFromObject(View view, Object object) {
    //        return view == object;
    //    }
    companion object {
        private const val DEBUG = false
        private const val TAG = "ComicPagerAdapter"
    }
}