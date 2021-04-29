package com.duongame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anjlab.android.iab.v3.SkuDetails
import com.duongame.R
import kotlinx.android.synthetic.main.item_donate.view.*

class DonateAdapter(private val items: List<SkuDetails>?) : RecyclerView.Adapter<DonateAdapter.DonateViewHolder>() {
    var onClick: ((id: String) -> Unit)? = null

    fun setOnClickCallback(onClick: (id: String) -> Unit) {
        this.onClick = onClick
    }

    // 클릭 콜백
    class DonateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donate, parent, false)
        return DonateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonateViewHolder, position: Int) {
        holder.itemView.title.text = items?.get(position)?.title
        holder.itemView.execute.setOnClickListener {
            onClick?.let {
                items?.get(position)?.productId?.let { it1 -> it(it1) }
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }
}