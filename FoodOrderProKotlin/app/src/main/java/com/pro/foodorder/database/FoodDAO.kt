package com.pro.foodorder.database

import androidx.room.*
import com.pro.foodorder.model.Food

@Dao
interface FoodDAO {
    @Insert
    fun insertFood(food: Food?)

    @get:Query("SELECT * FROM food")
    val listFoodCart: MutableList<Food>?

    @Query("SELECT * FROM food WHERE id=:id")
    fun checkFoodInCart(id: Long): MutableList<Food>?

    @Delete
    fun deleteFood(food: Food?)

    @Update
    fun updateFood(food: Food?)

    @Query("DELETE from food")
    fun deleteAllFood()
}