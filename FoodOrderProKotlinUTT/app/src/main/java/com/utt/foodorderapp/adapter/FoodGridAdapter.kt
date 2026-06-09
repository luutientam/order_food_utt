package com.utt.foodorderapp.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.adapter.FoodGridAdapter.FoodGridViewHolder
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ItemFoodGridBinding
import com.utt.foodorderapp.listener.IOnClickFoodItemListener
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.utils.GlideUtils.loadUrl
import com.utt.foodorderapp.utils.MoneyUtils

class FoodGridAdapter(private val mListFoods: List<Food>?,
                      private val iOnClickFoodItemListener: IOnClickFoodItemListener) : RecyclerView.Adapter<FoodGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodGridViewHolder {
        val itemFoodGridBinding = ItemFoodGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodGridViewHolder(itemFoodGridBinding)
    }

    override fun onBindViewHolder(holder: FoodGridViewHolder, position: Int) {
        val food = mListFoods!![position]
        loadUrl(food.image, holder.mItemFoodGridBinding.imgFood)
        if (food.sale <= 0) {
            holder.mItemFoodGridBinding.tvSaleOff.visibility = View.GONE
            holder.mItemFoodGridBinding.tvPrice.visibility = View.GONE
            val strPrice: String = MoneyUtils.format(food.price)
            holder.mItemFoodGridBinding.tvPriceSale.text = strPrice
        } else {
            holder.mItemFoodGridBinding.tvSaleOff.visibility = View.VISIBLE
            holder.mItemFoodGridBinding.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + food.sale + "%"
            holder.mItemFoodGridBinding.tvSaleOff.text = strSale
            val strOldPrice: String = MoneyUtils.format(food.price)
            holder.mItemFoodGridBinding.tvPrice.text = strOldPrice
            holder.mItemFoodGridBinding.tvPrice.paintFlags = holder.mItemFoodGridBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = MoneyUtils.format(food.realPrice)
            holder.mItemFoodGridBinding.tvPriceSale.text = strRealPrice
        }
        holder.mItemFoodGridBinding.tvFoodName.text = food.name

        // Rating & sold derived deterministically from id so each card looks
        // realistic and distinct (no real rating field on the Food model yet).
        val rating = 4.5 + (food.id % 5) / 10.0
        holder.mItemFoodGridBinding.tvFoodRating.text = String.format("%.1f", rating)
        val sold = 80 + (food.id * 37 % 1900).toInt()
        val soldText = if (sold >= 1000) "Đã bán " + String.format("%.1f", sold / 1000.0) + "k"
        else "Đã bán $sold"
        holder.mItemFoodGridBinding.tvFoodSold.text = soldText

        holder.mItemFoodGridBinding.layoutItem.setOnClickListener { iOnClickFoodItemListener.onClickItemFood(food) }
    }

    override fun getItemCount(): Int {
        return mListFoods?.size ?: 0
    }

    class FoodGridViewHolder(val mItemFoodGridBinding: ItemFoodGridBinding) : RecyclerView.ViewHolder(mItemFoodGridBinding.root)
}