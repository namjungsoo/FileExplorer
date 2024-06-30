package com.duongame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.duongame.R

class DonateAdapter(private val items: List<SkuDetails>) : RecyclerView.Adapter<DonateAdapter.DonateViewHolder>() {
    var onClick: ((skuDetails: SkuDetails) -> Unit)? = null

    fun setOnClickCallback(onClick: (skuDetails: SkuDetails) -> Unit) {
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
        val title = items[position].title.substring(0, items[position].title.indexOf('('))
        holder.itemView.findViewById<TextView>(R.id.title).text = title
        holder.itemView.findViewById<Button>(R.id.execute).setOnClickListener {
            onClick?.let { callback ->
                callback(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}