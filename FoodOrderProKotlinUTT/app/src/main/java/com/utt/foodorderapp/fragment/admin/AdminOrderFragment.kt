package com.utt.foodorderapp.fragment.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.AdminMainActivity
import com.utt.foodorderapp.adapter.AdminOrderAdapter
import com.utt.foodorderapp.adapter.AdminOrderAdapter.IUpdateStatusListener
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.FragmentAdminOrderBinding
import com.utt.foodorderapp.fragment.BaseFragment
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.presentation.order.OrderViewModel
import java.util.*

class AdminOrderFragment : BaseFragment() {

    private var mFragmentAdminOrderBinding: FragmentAdminOrderBinding? = null
    private var mListOrder: MutableList<Order>? = null
    private var mAdminOrderAdapter: AdminOrderAdapter? = null
    private var mBookingChildEventListener: ChildEventListener? = null
    private var mBookingDatabaseReference: DatabaseReference? = null
    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        mFragmentAdminOrderBinding = FragmentAdminOrderBinding.inflate(inflater, container, false)
        initView()
        getListOrders()
        return mFragmentAdminOrderBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.order))
        }
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminOrderBinding!!.rcvOrder.layoutManager = linearLayoutManager
        mListOrder = ArrayList()
        mAdminOrderAdapter = AdminOrderAdapter(activity, mListOrder, object : IUpdateStatusListener {
            override fun updateStatus(order: Order) {
                handleUpdateStatusOrder(order)
            }

            override fun cancelOrder(order: Order) {
                updateOrderStatus(order, Order.STATUS_CANCEL)
            }
        })
        mFragmentAdminOrderBinding!!.rcvOrder.adapter = mAdminOrderAdapter
    }

    private fun getListOrders() {
        val currentActivity = activity ?: return
        mBookingDatabaseReference = ControllerApplication[currentActivity].bookingDatabaseReference
        mBookingChildEventListener?.let { listener ->
            mBookingDatabaseReference?.removeEventListener(listener)
        }
        mBookingChildEventListener = object : ChildEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order == null || mListOrder == null || mAdminOrderAdapter == null) {
                            return
                        }
                        mListOrder!!.add(0, order)
                        mAdminOrderAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order == null || mListOrder == null || mListOrder!!.isEmpty() || mAdminOrderAdapter == null) {
                            return
                        }
                        for (i in mListOrder!!.indices) {
                            if (order.id == mListOrder!![i].id) {
                                mListOrder!![i] = order
                                break
                            }
                        }
                        mAdminOrderAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val order = dataSnapshot.getValue(Order::class.java)
                        if (order == null || mListOrder == null || mListOrder!!.isEmpty() || mAdminOrderAdapter == null) {
                            return
                        }
                        for (orderObject in mListOrder!!) {
                            if (order.id == orderObject.id) {
                                mListOrder!!.remove(orderObject)
                                break
                            }
                        }
                        mAdminOrderAdapter!!.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
        mBookingDatabaseReference?.addChildEventListener(mBookingChildEventListener!!)
    }

    private fun handleUpdateStatusOrder(order: Order) {
        val nextStatus = when (order.getStatusValue()) {
            Order.STATUS_NEW -> Order.STATUS_PREPARING
            else -> -1
        }
        if (nextStatus == -1) {
            showToastMessage(activity, getString(R.string.msg_order_cannot_update_status))
            return
        }
        updateOrderStatus(order, nextStatus)
    }

    private fun updateOrderStatus(order: Order, status: Int) {
        orderViewModel.updateOrderStatus(order.id, status) { isSuccess ->
            if (isSuccess) {
                showToastMessage(activity, getString(R.string.msg_order_update_status_success))
            } else {
                showToastMessage(activity, getString(R.string.msg_order_cannot_update_status))
            }
        }
    }

    override fun onDestroyView() {
        mBookingChildEventListener?.let { listener ->
            mBookingDatabaseReference?.removeEventListener(listener)
        }
        mBookingChildEventListener = null
        mBookingDatabaseReference = null
        if (mAdminOrderAdapter != null) {
            mAdminOrderAdapter!!.release()
        }
        mAdminOrderAdapter = null
        mFragmentAdminOrderBinding = null
        super.onDestroyView()
    }
}