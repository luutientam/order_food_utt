package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.utils.DateTimeUtils.convertTimeStampToDate
import com.utt.foodorderapp.utils.MoneyUtils

class EarningsAdapter(
        private val items: MutableList<Order>
) : RecyclerView.Adapter<EarningsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.tv_order_id)
        val amount: TextView = view.findViewById(R.id.tv_amount)
        val address: TextView = view.findViewById(R.id.tv_address)
        val date: TextView = view.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_earning, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = items[position]
        holder.id.text = "#${order.id}"
        holder.amount.text = "${MoneyUtils.format(order.amount)}"
        holder.address.text = order.address ?: ""
        holder.date.text = convertTimeStampToDate(order.id)
    }
}
