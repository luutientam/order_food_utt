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
            val strPrice: String = "" + food.price + AppConfig.CURRENCY
            holder.mItemFoodGridBinding.tvPriceSale.text = strPrice
        } else {
            holder.mItemFoodGridBinding.tvSaleOff.visibility = View.VISIBLE
            holder.mItemFoodGridBinding.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + food.sale + "%"
            holder.mItemFoodGridBinding.tvSaleOff.text = strSale
            val strOldPrice: String = "" + food.price + AppConfig.CURRENCY
            holder.mItemFoodGridBinding.tvPrice.text = strOldPrice
            holder.mItemFoodGridBinding.tvPrice.paintFlags = holder.mItemFoodGridBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = "" + food.realPrice + AppConfig.CURRENCY
            holder.mItemFoodGridBinding.tvPriceSale.text = strRealPrice
        }
        holder.mItemFoodGridBinding.tvFoodName.text = food.name
        holder.mItemFoodGridBinding.layoutItem.setOnClickListener { iOnClickFoodItemListener.onClickItemFood(food) }
    }

    override fun getItemCount(): Int {
        return mListFoods?.size ?: 0
    }

    class FoodGridViewHolder(val mItemFoodGridBinding: ItemFoodGridBinding) : RecyclerView.ViewHolder(mItemFoodGridBinding.root)
}