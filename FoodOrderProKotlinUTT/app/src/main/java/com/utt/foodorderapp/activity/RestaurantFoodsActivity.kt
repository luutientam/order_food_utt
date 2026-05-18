package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.FoodGridAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction
import com.utt.foodorderapp.databinding.ActivityRestaurantFoodsBinding
import com.utt.foodorderapp.listener.IOnClickFoodItemListener
import com.utt.foodorderapp.model.Food

/**
 * Hiển thị danh sách món của 1 nhà hàng cụ thể.
 * Mở từ RestaurantListActivity.
 */
class RestaurantFoodsActivity : BaseActivity() {

    private var binding: ActivityRestaurantFoodsBinding? = null
    private val items = mutableListOf<Food>()
    private var adapter: FoodGridAdapter? = null
    private var listener: ValueEventListener? = null
    private var restaurantId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantFoodsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        restaurantId = intent.getLongExtra(EXTRA_RESTAURANT_ID, 0L)
        val name = intent.getStringExtra(EXTRA_RESTAURANT_NAME) ?: getString(R.string.restaurant_foods_title)
        if (restaurantId == 0L) {
            finish()
            return
        }

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = name
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = FoodGridAdapter(items, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                val bundle = Bundle()
                bundle.putSerializable(AppConfig.KEY_INTENT_FOOD_OBJECT, food)
                GlobalFunction.startActivity(this@RestaurantFoodsActivity, FoodDetailActivity::class.java, bundle)
            }
        })
        binding!!.rcvFoods.layoutManager = GridLayoutManager(this, 2)
        binding!!.rcvFoods.adapter = adapter

        loadFoods()
    }

    private fun loadFoods() {
        val ref = ControllerApplication[this].foodDatabaseReference
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Food>()
                for (child in snapshot.children) {
                    val food = child.getValue(Food::class.java) ?: continue
                    if (food.restaurantId == restaurantId) list.add(food)
                }
                items.clear()
                items.addAll(list)
                adapter?.notifyDataSetChanged()
                val isEmpty = list.isEmpty()
                binding?.tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding?.rcvFoods?.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener!!)
    }

    override fun onDestroy() {
        listener?.let {
            ControllerApplication[this].foodDatabaseReference.removeEventListener(it)
        }
        listener = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID"
        const val EXTRA_RESTAURANT_NAME = "EXTRA_RESTAURANT_NAME"
    }
}
