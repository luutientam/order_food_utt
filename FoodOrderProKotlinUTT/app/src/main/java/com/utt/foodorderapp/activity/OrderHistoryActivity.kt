package com.utt.foodorderapp.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.OrderAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.ActivityOrderHistoryBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.presentation.order.OrderViewModel

class OrderHistoryActivity : BaseActivity() {

    private var mActivityOrderHistoryBinding: ActivityOrderHistoryBinding? = null
    private var mListOrder: MutableList<Order>? = null
    private var mOrderAdapter: OrderAdapter? = null
    private val orderViewModel: OrderViewModel by viewModels()

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
        mActivityOrderHistoryBinding!!.toolbar.imgBack.setOnClickListener { onBackPressed() }
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
                            object : OrderAdapter.ICancelOrderListener {
                                override fun cancelOrder(order: Order) {
                                    onClickCancelOrder(order)
                                }
                            })
                    mActivityOrderHistoryBinding!!.rcvOrderHistory.adapter = mOrderAdapter
                }
            }
        }
    }
}