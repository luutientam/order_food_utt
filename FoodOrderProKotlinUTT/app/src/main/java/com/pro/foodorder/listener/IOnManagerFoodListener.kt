package com.pro.foodorder.listener

import com.pro.foodorder.model.Food

interface IOnManagerFoodListener {
    fun onClickUpdateFood(food: Food?)
    fun onClickDeleteFood(food: Food?)
}