package com.duongame.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.duongame.App.Companion.instance
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.adapter.SearchRecyclerAdapter
import com.duongame.databinding.FragmentSearchBinding
import com.duongame.db.BookLoader.load
import com.duongame.file.LocalExplorer
import com.duongame.view.DividerItemDecoration
import java.lang.ref.WeakReference
import java.util.*


/**
 * Created by namjungsoo on 2016. 12. 30..
 */
class SearchFragment : BaseFragment() {
    private var fileList: ArrayList<ExplorerItem> = arrayListOf()
    private lateinit var binding: FragmentSearchBinding

    internal class SearchTask(fragment: SearchFragment, var keyword: String, var ext: ArrayList<String>) :
        AsyncTask<Void?, Void?, Boolean>() {
        var fragmentWeakReference: WeakReference<SearchFragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: Void?): Boolean {
            val fragment = fragmentWeakReference.get() ?: return false
            val activity = fragment.activity ?: return false
            fragment.fileResult = fragment.fileExplorer?.apply {
                isRecursiveDirectory = true
                isHiddenFile = true
                isExcludeDirectory = false
                isImageListEnable = false
            }?.search(instance.initialPath)

            fragment.fileList = fragment.fileResult?.fileList ?: arrayListOf()
            return fragment.fileList.size > 0
        }

        override fun onPostExecute(result: Boolean) {
            val fragment = fragmentWeakReference.get() ?: return
            val activity = fragment.activity ?: return
            if (result) {
                val adapter = SearchRecyclerAdapter(fragment.fileList)
                adapter.setOnItemClickListener(object : SearchRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val fragment = fragmentWeakReference.get() ?: return
                        val item = fragment.fileList[position]
                        val activity = fragment.activity ?: return
                        load(activity, item, false)
                    }
                })
                fragment.binding.recyclerSearch.adapter = adapter
            } else {
                fragment.binding.switcherContents.displayedChild = 1
            }
            fragment.binding.progressSearch.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity ?: return null
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.recyclerSearch.layoutManager = LinearLayoutManager(activity)
        binding.recyclerSearch.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL_LIST
            )
        )
        binding.btnSearch.setOnClickListener(View.OnClickListener {
            val activity = getActivity() ?: return@OnClickListener
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editKeyword.windowToken, 0)

            // 대문자 PDF, ZIP, TXT를 소문자로 수정
            val ext = binding.spinnerType.selectedItem.toString().lowercase(Locale.getDefault())
            val exts = ext.split(",".toRegex()).toTypedArray()
            val extList = ArrayList<String>()
            for (i in exts.indices) {
                extList.add("." + exts[i])
            }
            val keyword = binding.editKeyword.text.toString()
            val task = SearchTask(this@SearchFragment, keyword, extList)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            binding.progressSearch.visibility = View.VISIBLE
            binding.switcherContents.displayedChild = 0
        })
        fileExplorer = LocalExplorer()
        return binding.root
    }

    override fun onRefresh() {
        // 현재는 무조건 결과 없음을 리턴함
        if (binding.recyclerSearch.adapter != null && binding.recyclerSearch.adapter?.itemCount ?: 0 > 0) {
            binding.switcherContents.displayedChild = 0
        } else {
            binding.switcherContents.displayedChild = 1
        }
    }
}