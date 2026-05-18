package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.model.Category

/**
 * Hiển thị danh sách category dạng chip ngang.
 * categoryId = 0 nghĩa là "Tất cả".
 */
class CategoryChipAdapter(
        private val items: List<Category>,
        private val onSelect: (Category) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.VH>() {

    private var selectedId: Long = 0L

    fun setSelected(id: Long) {
        selectedId = id
        notifyDataSetChanged()
    }

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_chip, parent, false) as TextView
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.text.context
        holder.text.text = item.name ?: ""
        val isSelected = item.id == selectedId
        holder.text.setBackgroundResource(
                if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        holder.text.setTextColor(
                ContextCompat.getColor(ctx, if (isSelected) R.color.white else R.color.textColorPrimary)
        )
        holder.text.setOnClickListener {
            if (selectedId != item.id) {
                selectedId = item.id
                notifyDataSetChanged()
                onSelect(item)
            }
        }
    }
}
