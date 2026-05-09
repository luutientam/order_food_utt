package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ItemShipperOrderBinding
import com.utt.foodorderapp.model.Order

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
        holder.binding.tvInfo.text = "#${order.id} - ${order.name} - ${order.phone}\n${order.address}\n${order.foods}\n(Nhấn giữ để liên hệ)"
        holder.binding.tvStatus.text = getStatusText(holder, order)
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
        return "$statusText - ${order.amount}${AppConfig.CURRENCY}"
    }

    class ShipperOrderViewHolder(val binding: ItemShipperOrderBinding) : RecyclerView.ViewHolder(binding.root)
}
