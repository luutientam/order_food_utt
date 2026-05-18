package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.model.Address

class AddressAdapter(
        private val items: MutableList<Address>,
        private val listener: Listener
) : RecyclerView.Adapter<AddressAdapter.VH>() {

    interface Listener {
        fun onSelect(address: Address)
        fun onEdit(address: Address)
        fun onDelete(address: Address)
        fun onSetDefault(address: Address)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.tv_label)
        val badge: TextView = view.findViewById(R.id.tv_default_badge)
        val recipient: TextView = view.findViewById(R.id.tv_recipient)
        val fullAddress: TextView = view.findViewById(R.id.tv_full_address)
        val edit: TextView = view.findViewById(R.id.tv_edit)
        val delete: TextView = view.findViewById(R.id.tv_delete)
        val setDefault: TextView = view.findViewById(R.id.tv_set_default)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.label.text = item.label ?: "—"
        holder.badge.visibility = if (item.isDefault) View.VISIBLE else View.GONE
        holder.recipient.text = "${item.recipientName ?: ""}  ${item.phone ?: ""}"
        holder.fullAddress.text = item.fullAddress ?: ""

        holder.setDefault.visibility = if (item.isDefault) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { listener.onSelect(item) }
        holder.edit.setOnClickListener { listener.onEdit(item) }
        holder.delete.setOnClickListener { listener.onDelete(item) }
        holder.setDefault.setOnClickListener { listener.onSetDefault(item) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Address>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
