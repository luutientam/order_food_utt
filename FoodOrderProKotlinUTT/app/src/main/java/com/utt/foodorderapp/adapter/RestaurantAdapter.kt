package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.model.Restaurant
import com.utt.foodorderapp.utils.GlideUtils

class RestaurantAdapter(
        private val items: MutableList<Restaurant>,
        private val onClick: (Restaurant) -> Unit,
        private val onCallClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.img_restaurant)
        val name: TextView = view.findViewById(R.id.tv_restaurant_name)
        val address: TextView = view.findViewById(R.id.tv_restaurant_address)
        val phone: TextView = view.findViewById(R.id.tv_restaurant_phone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name ?: ""
        holder.address.text = item.address ?: ""
        holder.phone.text = item.phone ?: ""
        if (!item.image.isNullOrEmpty()) {
            GlideUtils.loadUrl(item.image, holder.image)
        }
        holder.itemView.setOnClickListener { onClick(item) }
        holder.phone.setOnClickListener { onCallClick(item) }
    }

    fun update(newItems: List<Restaurant>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
