package com.pro.foodorder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.foodorder.R
import com.pro.foodorder.adapter.AdminOrderAdapter.AdminOrderViewHolder
import com.pro.foodorder.constant.Constant
import com.pro.foodorder.databinding.ItemAdminOrderBinding
import com.pro.foodorder.model.Order
import com.pro.foodorder.utils.DateTimeUtils.convertTimeStampToDate

class AdminOrderAdapter(private var mContext: Context?, private val mListOrder: List<Order>?,
                        private val mIUpdateStatusListener: IUpdateStatusListener) : RecyclerView.Adapter<AdminOrderViewHolder>() {

    interface IUpdateStatusListener {
        fun updateStatus(order: Order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val itemAdminOrderBinding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return AdminOrderViewHolder(itemAdminOrderBinding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = mListOrder!![position]
        if (order.isCompleted) {
            holder.mItemAdminOrderBinding.layoutItem.setBackgroundColor(mContext!!.resources.getColor(R.color.black_overlay))
        } else {
            holder.mItemAdminOrderBinding.layoutItem.setBackgroundColor(mContext!!.resources.getColor(R.color.white))
        }
        holder.mItemAdminOrderBinding.chbStatus.isChecked = order.isCompleted
        holder.mItemAdminOrderBinding.tvId.text = order.id.toString()
        holder.mItemAdminOrderBinding.tvEmail.text = order.email
        holder.mItemAdminOrderBinding.tvName.text = order.name
        holder.mItemAdminOrderBinding.tvPhone.text = order.phone
        holder.mItemAdminOrderBinding.tvAddress.text = order.address
        holder.mItemAdminOrderBinding.tvMenu.text = order.foods
        holder.mItemAdminOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strAmount: String = "" + order.amount + Constant.CURRENCY
        holder.mItemAdminOrderBinding.tvTotalAmount.text = strAmount
        var paymentMethod = ""
        if (Constant.TYPE_PAYMENT_CASH == order.payment) {
            paymentMethod = Constant.PAYMENT_METHOD_CASH
        }
        holder.mItemAdminOrderBinding.tvPayment.text = paymentMethod
        holder.mItemAdminOrderBinding.chbStatus.setOnClickListener { mIUpdateStatusListener.updateStatus(order) }
    }

    override fun getItemCount(): Int {
        return mListOrder?.size ?: 0
    }

    fun release() {
        mContext = null
    }

    class AdminOrderViewHolder(val mItemAdminOrderBinding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(mItemAdminOrderBinding.root)
}