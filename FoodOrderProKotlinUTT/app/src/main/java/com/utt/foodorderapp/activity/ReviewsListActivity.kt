package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.ReviewListAdapter
import com.utt.foodorderapp.data.repository.ReviewRepository
import com.utt.foodorderapp.databinding.ActivityReviewsListBinding
import com.utt.foodorderapp.model.Review

/**
 * Hiển thị danh sách đánh giá của 1 món hoặc 1 nhà hàng.
 * Truyền EXTRA_FOOD_ID hoặc EXTRA_RESTAURANT_ID (khác 0).
 */
class ReviewsListActivity : BaseActivity() {

    private var binding: ActivityReviewsListBinding? = null
    private val items = mutableListOf<Review>()
    private var adapter: ReviewListAdapter? = null
    private val repository = ReviewRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsListBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val foodId = intent.getLongExtra(EXTRA_FOOD_ID, 0L)
        val restaurantId = intent.getLongExtra(EXTRA_RESTAURANT_ID, 0L)
        val titleRes = if (foodId != 0L) R.string.reviews_food_title else R.string.reviews_restaurant_title

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(titleRes)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = ReviewListAdapter(items)
        binding!!.rcvReviews.layoutManager = LinearLayoutManager(this)
        binding!!.rcvReviews.adapter = adapter

        if (foodId != 0L) {
            repository.observeReviewsForFood(foodId) { render(it) }
        } else if (restaurantId != 0L) {
            repository.observeReviewsForRestaurant(restaurantId) { render(it) }
        } else {
            finish()
        }
    }

    private fun render(list: List<Review>) {
        items.clear()
        items.addAll(list)
        adapter?.notifyDataSetChanged()

        val isEmpty = list.isEmpty()
        binding?.tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding?.layoutSummary?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding?.rcvReviews?.visibility = if (isEmpty) View.GONE else View.VISIBLE

        if (!isEmpty) {
            val avg = list.sumOf { it.rating }.toFloat() / list.size
            binding?.tvAvgRating?.text = String.format("%.1f", avg)
            binding?.ratingAvgBar?.rating = avg
            binding?.tvTotalReviews?.text = getString(R.string.reviews_total_count, list.size)
        }
    }

    companion object {
        const val EXTRA_FOOD_ID = "EXTRA_FOOD_ID"
        const val EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID"
    }
}
