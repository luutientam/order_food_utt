package com.utt.foodorderapp.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.RestaurantAdapter
import com.utt.foodorderapp.databinding.ActivityRestaurantListBinding
import com.utt.foodorderapp.model.Restaurant

class RestaurantListActivity : BaseActivity() {

    private var binding: ActivityRestaurantListBinding? = null
    private val items = mutableListOf<Restaurant>()
    private var adapter: RestaurantAdapter? = null
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantListBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.restaurant_list_title)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = RestaurantAdapter(items,
                onClick = { restaurant ->
                    val intent = Intent(this, RestaurantFoodsActivity::class.java)
                    intent.putExtra(RestaurantFoodsActivity.EXTRA_RESTAURANT_ID, restaurant.id)
                    intent.putExtra(RestaurantFoodsActivity.EXTRA_RESTAURANT_NAME, restaurant.name ?: "")
                    startActivity(intent)
                },
                onCallClick = { restaurant ->
                    val phone = restaurant.phone?.trim().orEmpty()
                    if (phone.isNotEmpty()) {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                    }
                })
        binding!!.rcvRestaurants.layoutManager = LinearLayoutManager(this)
        binding!!.rcvRestaurants.adapter = adapter

        loadRestaurants()
    }

    private fun loadRestaurants() {
        val ref = ControllerApplication[this].restaurantDatabaseReference
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Restaurant>()
                for (child in snapshot.children) {
                    val item = child.getValue(Restaurant::class.java) ?: continue
                    if (item.isActive) list.add(item)
                }
                adapter?.update(list)
                val isEmpty = list.isEmpty()
                binding?.tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding?.rcvRestaurants?.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener!!)
    }

    override fun onDestroy() {
        listener?.let {
            ControllerApplication[this].restaurantDatabaseReference.removeEventListener(it)
        }
        listener = null
        super.onDestroy()
    }
}
