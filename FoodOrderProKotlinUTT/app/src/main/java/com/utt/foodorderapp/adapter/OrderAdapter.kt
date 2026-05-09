package com.utt.foodorderapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.OrderAdapter.OrderViewHolder
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ItemOrderBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.utils.DateTimeUtils.convertTimeStampToDate

class OrderAdapter(private var mContext: Context?,
                   private val mListOrder: List<Order>?) : RecyclerView.Adapter<OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemOrderBinding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return OrderViewHolder(itemOrderBinding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = mListOrder!![position]
        if (order.isCompleted) {
            holder.mItemOrderBinding.layoutItem.setBackgroundColor(mContext!!.resources.getColor(R.color.black_overlay))
        } else {
            holder.mItemOrderBinding.layoutItem.setBackgroundColor(mContext!!.resources.getColor(R.color.white))
        }
        holder.mItemOrderBinding.tvId.text = order.id.toString()
        holder.mItemOrderBinding.tvName.text = order.name
        holder.mItemOrderBinding.tvPhone.text = order.phone
        holder.mItemOrderBinding.tvAddress.text = order.address
        holder.mItemOrderBinding.tvMenu.text = order.foods
        holder.mItemOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strAmount: String = "" + order.amount + AppConfig.CURRENCY
        holder.mItemOrderBinding.tvTotalAmount.text = strAmount
        var paymentMethod = ""
        if (AppConfig.TYPE_PAYMENT_CASH == order.payment) {
            paymentMethod = AppConfig.PAYMENT_METHOD_CASH
        }
        holder.mItemOrderBinding.tvPayment.text = paymentMethod
    }

    override fun getItemCount(): Int {
        return mListOrder?.size ?: 0
    }

    fun release() {
        mContext = null
    }

    class OrderViewHolder(val mItemOrderBinding: ItemOrderBinding) : RecyclerView.ViewHolder(mItemOrderBinding.root)
}