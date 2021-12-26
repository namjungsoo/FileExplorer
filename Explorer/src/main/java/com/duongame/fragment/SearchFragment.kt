package com.duongame.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duongame.App.Companion.instance
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.duongame.adapter.SearchRecyclerAdapter
import com.duongame.file.LocalExplorer
import com.duongame.view.DividerItemDecoration
import java.lang.ref.WeakReference
import java.util.*
import com.duongame.db.BookLoader.load


/**
 * Created by namjungsoo on 2016. 12. 30..
 */
class SearchFragment : BaseFragment() {
    private var switcherContents: ViewSwitcher? = null
    private var recyclerView: RecyclerView? = null
    private var spinnerType: Spinner? = null
    private var editKeyword: EditText? = null
    private var progressBar: ProgressBar? = null
    private var fileList: ArrayList<ExplorerItem> = arrayListOf()
    private var adapter: SearchRecyclerAdapter? = null

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
                fragment.adapter = SearchRecyclerAdapter(fragment.fileList)
                fragment.adapter?.setOnItemClickListener(object : SearchRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val fragment = fragmentWeakReference.get() ?: return
                        val item = fragment.fileList[position]
                        val activity = fragment.activity ?: return
                        load(activity, item, false)
                    }
                })
            } else {
                fragment.switcherContents!!.displayedChild = 1
            }
            fragment.progressBar!!.visibility = View.GONE
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity ?: return null
        val rootView = inflater.inflate(R.layout.fragment_search, container, false) as ViewGroup
        switcherContents = rootView.findViewById(R.id.switcher_contents)
        recyclerView = rootView.findViewById(R.id.recycler_search)
        recyclerView?.setLayoutManager(LinearLayoutManager(activity))
        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL_LIST
            )
        )
        spinnerType = rootView.findViewById(R.id.spinner_type)
        editKeyword = rootView.findViewById(R.id.edit_keyword)
        progressBar = rootView.findViewById(R.id.progress_search)
        val buttonSearch = rootView.findViewById<Button>(R.id.btn_search)
        buttonSearch.setOnClickListener(View.OnClickListener {
            val activity = getActivity() ?: return@OnClickListener
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editKeyword?.windowToken, 0)

            // 대문자 PDF, ZIP, TXT를 소문자로 수정
            val ext = spinnerType?.selectedItem.toString().lowercase(Locale.getDefault())
            val exts = ext.split(",".toRegex()).toTypedArray()
            val extList = ArrayList<String>()
            for (i in exts.indices) {
                extList.add("." + exts[i])
            }
            val keyword = editKeyword?.text.toString()
            val task = SearchTask(this@SearchFragment, keyword, extList)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            progressBar?.visibility = View.VISIBLE
            switcherContents?.displayedChild = 0
        })
        fileExplorer = LocalExplorer()
        return rootView
    }

    override fun onRefresh() {
        // 현재는 무조건 결과 없음을 리턴함
        if (switcherContents != null) {
            if (recyclerView!!.adapter != null && recyclerView!!.adapter!!.itemCount > 0) {
                switcherContents!!.displayedChild = 0
            } else {
                switcherContents!!.displayedChild = 1
            }
        }
    }
}