package com.utt.foodorderapp.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.OrderAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.data.repository.ReviewRepository
import com.utt.foodorderapp.databinding.ActivityOrderHistoryBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.model.Review
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.presentation.order.OrderViewModel

class OrderHistoryActivity : BaseActivity() {

    private var mActivityOrderHistoryBinding: ActivityOrderHistoryBinding? = null
    private var mListOrder: MutableList<Order>? = null
    private var mOrderAdapter: OrderAdapter? = null
    private val orderViewModel: OrderViewModel by viewModels()
    private val reviewRepository = ReviewRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityOrderHistoryBinding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(mActivityOrderHistoryBinding!!.root)
        initToolbar()
        initView()
        observeViewModel()
        getListOrders()
    }

    private fun initToolbar() {
        mActivityOrderHistoryBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityOrderHistoryBinding!!.toolbar.imgCart.visibility = View.GONE
        mActivityOrderHistoryBinding!!.toolbar.tvTitle.text = getString(R.string.order_history)
        mActivityOrderHistoryBinding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(this)
        mActivityOrderHistoryBinding!!.rcvOrderHistory.layoutManager = linearLayoutManager
    }

    private fun getListOrders() {
        orderViewModel.observeOrders { order ->
            val strEmail = user!!.email
            strEmail.equals(order.email, ignoreCase = true)
        }
    }

    private fun onClickCancelOrder(order: Order) {
        if (!order.isCancelableByUser()) {
            showToastMessage(this, getString(R.string.msg_order_cannot_cancel))
            return
        }
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_cancel_order))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    updateOrderStatus(order, Order.STATUS_CANCEL)
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun onClickTrackOrder(order: Order) {
        val intent = Intent(this, OrderTrackingActivity::class.java)
        intent.putExtra(OrderTrackingActivity.EXTRA_ORDER_ID, order.id)
        startActivity(intent)
    }

    private fun onClickRateOrder(order: Order) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rate_order, null, false)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val edtComment = view.findViewById<EditText>(R.id.edt_review_comment)

        reviewRepository.findReviewForOrder(order.id) { existing ->
            if (existing != null) {
                ratingBar.rating = existing.rating.toFloat()
                edtComment.setText(existing.comment ?: "")
            }
        }

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.rating_dialog_title))
                .setView(view)
                .setPositiveButton(getString(R.string.action_submit_rating)) { _, _ ->
                    submitReview(order, ratingBar.rating.toInt().coerceIn(1, 5), edtComment.text.toString().trim())
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun submitReview(order: Order, rating: Int, comment: String) {
        val review = Review()
        review.id = System.currentTimeMillis()
        review.orderId = order.id
        review.userEmail = user?.email
        review.foodId = 0
        review.restaurantId = 0
        review.rating = rating
        review.comment = comment
        review.createdAt = System.currentTimeMillis()
        reviewRepository.submitReview(review) { error ->
            if (error == null) {
                showToastMessage(this, getString(R.string.msg_rating_success))
            } else {
                showToastMessage(this, getString(R.string.msg_rating_failed))
            }
        }
    }

    private fun updateOrderStatus(order: Order, status: Int) {
        orderViewModel.updateOrderStatus(order.id, status) { isSuccess ->
            if (isSuccess) {
                showToastMessage(this, getString(R.string.msg_order_cancel_success))
            } else {
                showToastMessage(this, getString(R.string.msg_order_cannot_cancel))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOrderAdapter != null) {
            mOrderAdapter!!.release()
        }
    }

    private fun observeViewModel() {
        orderViewModel.ordersState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Unit
                is UiState.Error -> Unit
                is UiState.Success -> {
                    mListOrder = state.data.toMutableList()
                    mOrderAdapter = OrderAdapter(this@OrderHistoryActivity, mListOrder,
                            object : OrderAdapter.IOrderActionListener {
                                override fun cancelOrder(order: Order) {
                                    onClickCancelOrder(order)
                                }

                                override fun trackOrder(order: Order) {
                                    onClickTrackOrder(order)
                                }

                                override fun rateOrder(order: Order) {
                                    onClickRateOrder(order)
                                }
                            })
                    mActivityOrderHistoryBinding!!.rcvOrderHistory.adapter = mOrderAdapter
                }
            }
        }
    }
}
