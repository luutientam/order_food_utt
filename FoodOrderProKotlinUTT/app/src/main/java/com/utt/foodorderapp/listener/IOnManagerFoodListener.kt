package com.utt.foodorderapp.listener

import com.utt.foodorderapp.model.Food

interface IOnManagerFoodListener {
    fun onClickUpdateFood(food: Food?)
    fun onClickDeleteFood(food: Food?)
}