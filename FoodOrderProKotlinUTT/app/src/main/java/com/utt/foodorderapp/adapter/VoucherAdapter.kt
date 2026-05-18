package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.model.Promotion

class VoucherAdapter(
        private val items: MutableList<Promotion>
) : RecyclerView.Adapter<VoucherAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val percent: TextView = view.findViewById(R.id.tv_percent)
        val code: TextView = view.findViewById(R.id.tv_code)
        val title: TextView = view.findViewById(R.id.tv_title)
        val condition: TextView = view.findViewById(R.id.tv_condition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_voucher, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context
        holder.percent.text = "${item.discountPercent}%"
        holder.code.text = item.code ?: ""
        holder.title.text = item.title ?: ""
        val minOrder = if (item.minOrderAmount > 0)
            ctx.getString(R.string.voucher_min_order, "${item.minOrderAmount}${AppConfig.CURRENCY}")
        else ctx.getString(R.string.voucher_no_min)
        val maxDiscount = if (item.maxDiscountAmount > 0)
            ctx.getString(R.string.voucher_max_discount, "${item.maxDiscountAmount}${AppConfig.CURRENCY}")
        else ""
        holder.condition.text = if (maxDiscount.isEmpty()) minOrder else "$minOrder · $maxDiscount"
    }

    fun update(newItems: List<Promotion>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
