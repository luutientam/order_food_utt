package com.utt.foodorderapp.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ItemShipperOrderBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.utils.MoneyUtils

class ShipperOrderAdapter(
        private val isPickupMode: Boolean,
        private val orders: MutableList<Order>,
        private val listener: IShipperOrderListener
) : RecyclerView.Adapter<ShipperOrderAdapter.ShipperOrderViewHolder>() {

    interface IShipperOrderListener {
        fun onPrimaryAction(order: Order)
        fun onSecondaryAction(order: Order)
        fun onContactCustomer(order: Order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipperOrderViewHolder {
        val binding = ItemShipperOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShipperOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShipperOrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context
        holder.binding.tvInfo.text = "#${order.id} - ${order.name} - ${order.phone}\n${order.address}\n${order.foods}\n${context.getString(R.string.shipper_long_press_contact)}"
        holder.binding.tvStatus.text = getStatusText(holder, order)
        holder.binding.tvStatus.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, getStatusColor(order)))
        holder.binding.tvInfo.setOnLongClickListener {
            listener.onContactCustomer(order)
            true
        }
        if (isPickupMode) {
            holder.binding.tvActionPrimary.visibility = View.VISIBLE
            holder.binding.tvActionSecondary.visibility = View.VISIBLE
            holder.binding.tvActionPrimary.text = holder.itemView.context.getString(R.string.assign_shipper)
            holder.binding.tvActionSecondary.text = holder.itemView.context.getString(R.string.action_cancel)
            holder.binding.tvActionPrimary.setOnClickListener { listener.onPrimaryAction(order) }
            holder.binding.tvActionSecondary.setOnClickListener { listener.onSecondaryAction(order) }
            return
        }
        // Đơn đã huỷ: chỉ hiển thị trạng thái "Đã huỷ", ẩn nút thao tác vì shipper không thể xử lý tiếp
        if (order.getStatusValue() != Order.STATUS_DELIVERING) {
            holder.binding.tvActionPrimary.visibility = View.GONE
            holder.binding.tvActionSecondary.visibility = View.GONE
            return
        }
        holder.binding.tvActionPrimary.visibility = View.VISIBLE
        holder.binding.tvActionSecondary.visibility = View.VISIBLE
        holder.binding.tvActionPrimary.text = holder.itemView.context.getString(R.string.status_success)
        holder.binding.tvActionSecondary.text = holder.itemView.context.getString(R.string.status_fail)
        holder.binding.tvActionPrimary.setOnClickListener { listener.onPrimaryAction(order) }
        holder.binding.tvActionSecondary.setOnClickListener { listener.onSecondaryAction(order) }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    private fun getStatusText(holder: ShipperOrderViewHolder, order: Order): String {
        val context = holder.itemView.context
        val statusText = when (order.getStatusValue()) {
            Order.STATUS_NEW -> context.getString(R.string.status_new)
            Order.STATUS_PREPARING -> context.getString(R.string.status_preparing)
            Order.STATUS_DELIVERING -> context.getString(R.string.status_process)
            Order.STATUS_SUCCESS -> context.getString(R.string.status_success)
            Order.STATUS_CANCEL -> context.getString(R.string.status_cancel)
            else -> context.getString(R.string.status_fail)
        }
        return "$statusText - ${MoneyUtils.format(order.amount)}"
    }

    private fun getStatusColor(order: Order): Int {
        return when (order.getStatusValue()) {
            Order.STATUS_NEW -> R.color.statusNew
            Order.STATUS_PREPARING -> R.color.statusPreparing
            Order.STATUS_DELIVERING -> R.color.statusDelivering
            Order.STATUS_SUCCESS -> R.color.statusSuccess
            Order.STATUS_CANCEL -> R.color.statusCancel
            Order.STATUS_FAIL -> R.color.statusFail
            else -> R.color.statusNew
        }
    }

    class ShipperOrderViewHolder(val binding: ItemShipperOrderBinding) : RecyclerView.ViewHolder(binding.root)
}
