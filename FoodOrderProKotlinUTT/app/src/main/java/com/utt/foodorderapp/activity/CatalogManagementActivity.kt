package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.SimpleCatalogAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.ActivityCatalogManagementBinding
import com.utt.foodorderapp.model.Category
import com.utt.foodorderapp.model.Restaurant
import com.utt.foodorderapp.utils.StringUtil

class CatalogManagementActivity : BaseActivity() {

    private var binding: ActivityCatalogManagementBinding? = null
    private var restaurants: MutableList<Restaurant> = ArrayList()
    private var categories: MutableList<Category> = ArrayList()
    private var restaurantListener: ChildEventListener? = null
    private var categoryListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogManagementBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initToolbar()
        initData()
        initListener()
    }

    private fun initToolbar() {
        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.manage_catalog)
        binding!!.toolbar.imgBack.setOnClickListener { finish() }
    }

    private fun initData() {
        binding!!.rcvRestaurants.layoutManager = LinearLayoutManager(this)
        binding!!.rcvCategories.layoutManager = LinearLayoutManager(this)
        binding!!.rcvRestaurants.adapter = SimpleCatalogAdapter(
                restaurants,
                { item -> item.name ?: "" },
                { item -> deleteRestaurant(item) }
        )
        binding!!.rcvCategories.adapter = SimpleCatalogAdapter(
                categories,
                { item -> item.name ?: "" },
                { item -> deleteCategory(item) }
        )
        subscribeRestaurants()
        subscribeCategories()
    }

    private fun initListener() {
        binding!!.tvAddRestaurant.setOnClickListener {
            val name = binding!!.edtRestaurantName.text.toString().trim()
            if (StringUtil.isEmpty(name)) return@setOnClickListener
            val id = System.currentTimeMillis()
            val restaurant = Restaurant(id, name, "", "", "", true)
            ControllerApplication[this].restaurantDatabaseReference.child(id.toString()).setValue(restaurant)
            binding!!.edtRestaurantName.setText("")
        }
        binding!!.tvAddCategory.setOnClickListener {
            val name = binding!!.edtCategoryName.text.toString().trim()
            if (StringUtil.isEmpty(name)) return@setOnClickListener
            val id = System.currentTimeMillis()
            val category = Category(id, name, true)
            ControllerApplication[this].categoryDatabaseReference.child(id.toString()).setValue(category)
            binding!!.edtCategoryName.setText("")
        }
    }

    private fun subscribeRestaurants() {
        restaurantListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(Restaurant::class.java) ?: return
                restaurants.add(0, data)
                binding!!.rcvRestaurants.adapter?.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(Restaurant::class.java) ?: return
                for (i in restaurants.indices) {
                    if (restaurants[i].id == data.id) {
                        restaurants[i] = data
                        break
                    }
                }
                binding!!.rcvRestaurants.adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val data = snapshot.getValue(Restaurant::class.java) ?: return
                restaurants.removeAll { it.id == data.id }
                binding!!.rcvRestaurants.adapter?.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ControllerApplication[this].restaurantDatabaseReference.addChildEventListener(restaurantListener!!)
    }

    private fun subscribeCategories() {
        categoryListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(Category::class.java) ?: return
                categories.add(0, data)
                binding!!.rcvCategories.adapter?.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(Category::class.java) ?: return
                for (i in categories.indices) {
                    if (categories[i].id == data.id) {
                        categories[i] = data
                        break
                    }
                }
                binding!!.rcvCategories.adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val data = snapshot.getValue(Category::class.java) ?: return
                categories.removeAll { it.id == data.id }
                binding!!.rcvCategories.adapter?.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ControllerApplication[this].categoryDatabaseReference.addChildEventListener(categoryListener!!)
    }

    private fun deleteRestaurant(restaurant: Restaurant) {
        ControllerApplication[this].restaurantDatabaseReference.child(restaurant.id.toString()).removeValue()
        showToastMessage(this, getString(R.string.delete))
    }

    private fun deleteCategory(category: Category) {
        ControllerApplication[this].categoryDatabaseReference.child(category.id.toString()).removeValue()
        showToastMessage(this, getString(R.string.delete))
    }

    override fun onDestroy() {
        if (restaurantListener != null) {
            ControllerApplication[this].restaurantDatabaseReference.removeEventListener(restaurantListener!!)
        }
        if (categoryListener != null) {
            ControllerApplication[this].categoryDatabaseReference.removeEventListener(categoryListener!!)
        }
        super.onDestroy()
    }
}
