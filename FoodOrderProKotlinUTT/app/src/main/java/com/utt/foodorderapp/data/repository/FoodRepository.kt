package com.utt.foodorderapp.data.repository

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.database.FoodDatabase
import com.utt.foodorderapp.model.Food
import java.util.Locale

class FoodRepository {

    fun observeFoods(query: String, callback: (List<Food>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = ArrayList<Food>()
                val searchKey = query.lowercase(Locale.getDefault()).trim()
                for (child in snapshot.children) {
                    val item = child.getValue(Food::class.java) ?: continue
                    if (searchKey.isEmpty()) {
                        result.add(0, item)
                        continue
                    }
                    val name = (item.name ?: "").lowercase(Locale.getDefault())
                    val category = (item.categoryName ?: "").lowercase(Locale.getDefault())
                    val restaurant = (item.restaurantName ?: "").lowercase(Locale.getDefault())
                    if (name.contains(searchKey) || category.contains(searchKey) || restaurant.contains(searchKey)) {
                        result.add(0, item)
                    }
                }
                callback(result)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ControllerApplication.getInstance().foodDatabaseReference.addValueEventListener(listener)
        return listener
    }

    fun removeObserveFoods(listener: ValueEventListener?) {
        if (listener == null) return
        ControllerApplication.getInstance().foodDatabaseReference.removeEventListener(listener)
    }

    fun getCart(context: Context): MutableList<Food> {
        return FoodDatabase.getInstance(context)!!.foodDAO()!!.listFoodCart ?: ArrayList()
    }

    fun saveCartItem(context: Context, food: Food) {
        FoodDatabase.getInstance(context)!!.foodDAO()!!.insertFood(food)
    }

    fun updateCartItem(context: Context, food: Food) {
        FoodDatabase.getInstance(context)!!.foodDAO()!!.updateFood(food)
    }

    fun removeCartItem(context: Context, food: Food) {
        FoodDatabase.getInstance(context)!!.foodDAO()!!.deleteFood(food)
    }

    fun clearCart(context: Context) {
        FoodDatabase.getInstance(context)!!.foodDAO()!!.deleteAllFood()
    }
}
