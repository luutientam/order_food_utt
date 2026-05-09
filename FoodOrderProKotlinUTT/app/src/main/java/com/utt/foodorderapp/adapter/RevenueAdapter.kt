package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.adapter.RevenueAdapter.RevenueViewHolder
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ItemRevenueBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.utils.DateTimeUtils.convertTimeStampToDate_2

class RevenueAdapter(private val mListOrder: List<Order>?) : RecyclerView.Adapter<RevenueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevenueViewHolder {
        val itemRevenueBinding = ItemRevenueBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return RevenueViewHolder(itemRevenueBinding)
    }

    override fun onBindViewHolder(holder: RevenueViewHolder, position: Int) {
        val order = mListOrder!![position]
        holder.mItemRevenueBinding.tvId.text = order.id.toString()
        holder.mItemRevenueBinding.tvDate.text = convertTimeStampToDate_2(order.id)
        val strAmount: String = "" + order.amount + AppConfig.CURRENCY
        holder.mItemRevenueBinding.tvTotalAmount.text = strAmount
    }

    override fun getItemCount(): Int {
        return mListOrder?.size ?: 0
    }

    class RevenueViewHolder(val mItemRevenueBinding: ItemRevenueBinding) : RecyclerView.ViewHolder(mItemRevenueBinding.root)
}