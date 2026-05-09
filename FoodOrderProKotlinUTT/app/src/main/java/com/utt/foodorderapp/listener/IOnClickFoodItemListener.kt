package com.utt.foodorderapp.listener

import com.utt.foodorderapp.model.Food

interface IOnClickFoodItemListener {
    fun onClickItemFood(food: Food)
}