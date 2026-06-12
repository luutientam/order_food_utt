package com.utt.foodorderapp.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.AdminFoodAdapter.AdminFoodViewHolder
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ItemAdminFoodBinding
import com.utt.foodorderapp.listener.IOnManagerFoodListener
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.utils.GlideUtils.loadUrl
import com.utt.foodorderapp.utils.MoneyUtils

class AdminFoodAdapter(private val mListFoods: List<Food>?,
                       val iOnManagerFoodListener: IOnManagerFoodListener) : RecyclerView.Adapter<AdminFoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFoodViewHolder {
        val itemAdminFoodBinding = ItemAdminFoodBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminFoodViewHolder(itemAdminFoodBinding)
    }

    override fun onBindViewHolder(holder: AdminFoodViewHolder, position: Int) {
        val food = mListFoods!![position]
        loadUrl(food.image, holder.mItemAdminFoodBinding.imgFood)
        if (food.sale <= 0) {
            holder.mItemAdminFoodBinding.tvSaleOff.visibility = View.GONE
            holder.mItemAdminFoodBinding.tvPrice.visibility = View.GONE
            val strPrice: String = MoneyUtils.format(food.price)
            holder.mItemAdminFoodBinding.tvPriceSale.text = strPrice
        } else {
            holder.mItemAdminFoodBinding.tvSaleOff.visibility = View.VISIBLE
            holder.mItemAdminFoodBinding.tvPrice.visibility = View.VISIBLE
            val context = holder.itemView.context
            val strSale = context.getString(R.string.food_sale_off, food.sale)
            holder.mItemAdminFoodBinding.tvSaleOff.text = strSale
            val strOldPrice: String = MoneyUtils.format(food.price)
            holder.mItemAdminFoodBinding.tvPrice.text = strOldPrice
            holder.mItemAdminFoodBinding.tvPrice.paintFlags = holder.mItemAdminFoodBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = MoneyUtils.format(food.realPrice)
            holder.mItemAdminFoodBinding.tvPriceSale.text = strRealPrice
        }
        holder.mItemAdminFoodBinding.tvFoodName.text = food.name
        holder.mItemAdminFoodBinding.tvFoodDescription.text = food.description
        if (food.isPopular) {
            holder.mItemAdminFoodBinding.tvPopular.text =
                    holder.itemView.context.getString(R.string.food_has_discount)
        } else {
            holder.mItemAdminFoodBinding.tvPopular.text =
                    holder.itemView.context.getString(R.string.food_no_discount)
        }
        holder.mItemAdminFoodBinding.imgEdit.setOnClickListener { iOnManagerFoodListener.onClickUpdateFood(food) }
        holder.mItemAdminFoodBinding.imgDelete.setOnClickListener { iOnManagerFoodListener.onClickDeleteFood(food) }
    }

    override fun getItemCount(): Int {
        return mListFoods?.size ?: 0
    }

    class AdminFoodViewHolder(val mItemAdminFoodBinding: ItemAdminFoodBinding) : RecyclerView.ViewHolder(mItemAdminFoodBinding.root)
}