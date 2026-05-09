package com.utt.foodorderapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.AdminOrderAdapter.AdminOrderViewHolder
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ItemAdminOrderBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.utils.DateTimeUtils.convertTimeStampToDate

class AdminOrderAdapter(private var mContext: Context?, private val mListOrder: List<Order>?,
                        private val mIUpdateStatusListener: IUpdateStatusListener) : RecyclerView.Adapter<AdminOrderViewHolder>() {

    interface IUpdateStatusListener {
        fun updateStatus(order: Order)
        fun cancelOrder(order: Order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val itemAdminOrderBinding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return AdminOrderViewHolder(itemAdminOrderBinding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = mListOrder!![position]
        holder.mItemAdminOrderBinding.layoutItem.setBackgroundColor(holder.itemView.context.resources.getColor(R.color.white))
        holder.mItemAdminOrderBinding.tvId.text = order.id.toString()
        holder.mItemAdminOrderBinding.tvEmail.text = order.email
        holder.mItemAdminOrderBinding.tvName.text = order.name
        holder.mItemAdminOrderBinding.tvPhone.text = order.phone
        holder.mItemAdminOrderBinding.tvAddress.text = order.address
        holder.mItemAdminOrderBinding.tvMenu.text = order.foods
        holder.mItemAdminOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strAmount: String = "" + order.amount + AppConfig.CURRENCY
        val amountDisplay = if (order.discountAmount > 0) {
            "$strAmount (giam ${order.discountAmount}${AppConfig.CURRENCY})"
        } else {
            strAmount
        }
        holder.mItemAdminOrderBinding.tvTotalAmount.text = amountDisplay
        val paymentMethod = if (AppConfig.TYPE_PAYMENT_ONLINE == order.payment) {
            AppConfig.PAYMENT_METHOD_ONLINE
        } else {
            AppConfig.PAYMENT_METHOD_CASH
        }
        val paymentStatus = if (order.paymentStatus == Order.PAYMENT_STATUS_PAID) {
            holder.itemView.context.getString(R.string.payment_status_paid)
        } else {
            holder.itemView.context.getString(R.string.payment_status_unpaid)
        }
        holder.mItemAdminOrderBinding.tvPayment.text = "$paymentMethod - $paymentStatus"
        val tvStatus = holder.mItemAdminOrderBinding.root.findViewById<TextView>(R.id.tv_status)
        val layoutTransaction = holder.mItemAdminOrderBinding.root.findViewById<View>(R.id.layout_transaction)
        val tvTransaction = holder.mItemAdminOrderBinding.root.findViewById<TextView>(R.id.tv_transaction)
        val tvShipperLocation = holder.mItemAdminOrderBinding.root.findViewById<TextView>(R.id.tv_shipper_location)
        val tvNextStatus = holder.mItemAdminOrderBinding.root.findViewById<TextView>(R.id.tv_next_status)
        val tvCancelOrder = holder.mItemAdminOrderBinding.root.findViewById<TextView>(R.id.tv_cancel_order)
        val currentStatus = order.getStatusValue()
        tvStatus.text = buildStatusText(order)
        if (AppConfig.TYPE_PAYMENT_ONLINE == order.payment && !order.paymentTransactionId.isNullOrEmpty()) {
            layoutTransaction.visibility = View.VISIBLE
            tvTransaction.text = order.paymentTransactionId
        } else {
            layoutTransaction.visibility = View.GONE
            tvTransaction.text = ""
        }
        tvShipperLocation.text = getShipperLocationText(order)
        val canMoveNext = currentStatus == Order.STATUS_NEW
        tvNextStatus.visibility = if (canMoveNext) View.VISIBLE else View.GONE
        tvNextStatus.setOnClickListener {
            if (canMoveNext) {
                mIUpdateStatusListener.updateStatus(order)
            }
        }
        val canCancel = currentStatus == Order.STATUS_NEW
                || currentStatus == Order.STATUS_PREPARING
                || currentStatus == Order.STATUS_DELIVERING
        tvCancelOrder.visibility = if (canCancel) View.VISIBLE else View.GONE
        tvCancelOrder.setOnClickListener {
            if (canCancel) {
                mIUpdateStatusListener.cancelOrder(order)
            }
        }
    }

    override fun getItemCount(): Int {
        return mListOrder?.size ?: 0
    }

    fun release() {
        mContext = null
    }

    private fun getStatusText(order: Order): String {
        val context = mContext ?: return Order.STATUS_NEW.toString()
        return when (order.getStatusValue()) {
            Order.STATUS_NEW -> context.getString(R.string.status_new)
            Order.STATUS_PREPARING -> context.getString(R.string.status_preparing)
            Order.STATUS_DELIVERING -> context.getString(R.string.status_process)
            Order.STATUS_SUCCESS -> context.getString(R.string.status_success)
            Order.STATUS_CANCEL -> context.getString(R.string.status_cancel)
            Order.STATUS_FAIL -> context.getString(R.string.status_fail)
            else -> context.getString(R.string.status_new)
        }
    }

    private fun buildStatusText(order: Order): String {
        val baseStatus = getStatusText(order)
        if (order.getStatusValue() != Order.STATUS_FAIL || order.issueNote.isNullOrBlank()) {
            return baseStatus
        }
        return "$baseStatus\nSự cố: ${order.issueNote}"
    }

    private fun getShipperLocationText(order: Order): String {
        val context = mContext ?: return "${order.shipperLat}, ${order.shipperLng}"
        if (order.shipperLat == 0.0 && order.shipperLng == 0.0) {
            return context.getString(R.string.shipper_location_unknown)
        }
        return "${order.shipperLat}, ${order.shipperLng}"
    }

    class AdminOrderViewHolder(val mItemAdminOrderBinding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(mItemAdminOrderBinding.root)
}