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
        val rating: TextView = view.findViewById(R.id.tv_restaurant_rating)
        val distance: TextView = view.findViewById(R.id.tv_restaurant_distance)
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

        // Rating & distance derived from id so each restaurant looks distinct
        // (no rating/distance field on the model yet).
        val rating = 4.5 + (item.id % 5) / 10.0
        holder.rating.text = String.format("%.1f", rating)
        val distanceKm = 0.4 + (item.id * 13 % 80) / 10.0
        holder.distance.text = String.format("%.1f km", distanceKm)
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
