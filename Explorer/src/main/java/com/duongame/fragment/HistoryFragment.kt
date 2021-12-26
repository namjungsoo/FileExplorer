package com.duongame.fragment

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.ViewSwitcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duongame.R
import com.duongame.adapter.HistoryRecyclerAdapter
import com.duongame.db.Book
import com.duongame.db.BookDB.Companion.clearBook
import com.duongame.db.BookDB.Companion.getBooks
import com.duongame.db.BookLoader.loadContinue
import com.duongame.helper.PreferenceHelper.hideCompleted
import com.duongame.view.DividerItemDecoration
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by namjungsoo on 2016. 12. 30..
 */
class HistoryFragment : BaseFragment() {
    private var switcherContents: ViewSwitcher? = null
    private var recyclerAdapter: HistoryRecyclerAdapter? = null
    private var bookList: ArrayList<Book> = arrayListOf()
    private var isHideCompleted = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity ?: return null
        val rootView = inflater.inflate(R.layout.fragment_history, container, false) as ViewGroup
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_history)
        isHideCompleted = hideCompleted
        val switchHide = rootView.findViewById<Switch>(R.id.switch_hide)
        switchHide.isChecked = isHideCompleted
        switchHide.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            isHideCompleted = isChecked
            val activity = getActivity() ?: return@OnCheckedChangeListener
            hideCompleted = isHideCompleted
            onRefresh()
        })
        recyclerAdapter = HistoryRecyclerAdapter(this, null)
        recyclerAdapter?.setOnItemClickListener(object :
            HistoryRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val book = bookList[position]
                val activity = getActivity() ?: return
                loadContinue(activity, book)
            }
        })
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL_LIST
            )
        )
        switcherContents = rootView.findViewById(R.id.switcher_contents)
        return rootView
    }

    internal class RefreshTask(fragment: HistoryFragment) : AsyncTask<Void?, Void?, Void?>() {
        var fragmentWeakReferencea: WeakReference<HistoryFragment> = WeakReference(fragment)

        protected override fun doInBackground(vararg params: Void?): Void? {
            var fragment = fragmentWeakReferencea.get() ?: return null
            val activity = fragment.activity ?: return null

            fragment.bookList = ArrayList()
            val historyList = getBooks(activity)

            fragment = fragmentWeakReferencea.get() ?: return null

            val deleteList = ArrayList<String>()
            for (i in historyList.indices) {
                val book = historyList[i]
                try {
                    // 파일이 존재하지 않으면 삭제 리스트에 넣고 패스함
                    val file = File(book.path)
                    if (!file.exists()) {
                        deleteList.add(book.path)
                        continue
                    }
                } catch (e: Exception) {
                }
                if (fragment.isHideCompleted) {
                    if (book.percent < 100) {
                        fragment.bookList.add(book)
                    }
                } else {
                    fragment.bookList.add(book)
                }
            }

            // 삭제 리스트에 있는 모든 파일을 지운다.
            for (i in deleteList.indices) {
                clearBook(activity, deleteList[i])
            }

//            if (fragment.recyclerAdapter != null) {
//                fragment.recyclerAdapter.setBookList(fragment.bookList);
//            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            val fragment = fragmentWeakReferencea.get() ?: return
            fragment.recyclerAdapter?.bookList = fragment.bookList
            fragment.recyclerAdapter?.notifyDataSetChanged()

            // 결과가 있을때 없을때를 구분해서 SWICTH 함
            if (fragment.bookList.size > 0) {
                fragment.switcherContents?.displayedChild = 0
            } else {
                fragment.switcherContents?.displayedChild = 1
            }
        }

    }

    override fun onRefresh() {
        val task = RefreshTask(this)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        onRefresh()
    }

    companion object {
        private const val DEBUG = false
    }
}