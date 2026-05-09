package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.databinding.ItemCatalogSimpleBinding

class SimpleCatalogAdapter<T>(
        private val data: MutableList<T>,
        private val getName: (T) -> String,
        private val onDelete: (T) -> Unit
) : RecyclerView.Adapter<SimpleCatalogAdapter<T>.CatalogViewHolder>() {

    inner class CatalogViewHolder(val binding: ItemCatalogSimpleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val binding = ItemCatalogSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatalogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        val item = data[position]
        holder.binding.tvName.text = getName(item)
        holder.binding.tvDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
