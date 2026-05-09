package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ItemPromotionBinding
import com.utt.foodorderapp.model.Promotion

class PromotionAdapter(
        private val items: MutableList<Promotion>,
        private val listener: IPromotionAction
) : RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder>() {

    interface IPromotionAction {
        fun onToggle(promotion: Promotion)
        fun onDelete(promotion: Promotion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        val binding = ItemPromotionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PromotionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        val promotion = items[position]
        holder.binding.tvCode.text = promotion.code
        holder.binding.tvInfo.text = "${promotion.title} - ${promotion.discountPercent}% | Min ${promotion.minOrderAmount} | Max ${promotion.maxDiscountAmount}"
        holder.binding.tvToggle.text = if (promotion.isActive) holder.itemView.context.getString(R.string.action_disable) else holder.itemView.context.getString(R.string.action_enable)
        holder.binding.tvToggle.setOnClickListener { listener.onToggle(promotion) }
        holder.binding.tvDelete.setOnClickListener { listener.onDelete(promotion) }
    }

    override fun getItemCount(): Int = items.size

    class PromotionViewHolder(val binding: ItemPromotionBinding) : RecyclerView.ViewHolder(binding.root)
}
