package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.RevenueAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.showDatePicker
import com.utt.foodorderapp.databinding.ActivityAdminReportBinding
import com.utt.foodorderapp.listener.IGetDateListener
import com.utt.foodorderapp.listener.IOnSingleClickListener
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.model.User
import com.utt.foodorderapp.utils.DateTimeUtils.convertDate2ToTimeStamp
import com.utt.foodorderapp.utils.DateTimeUtils.convertTimeStampToDate_2
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import java.util.*

class AdminReportActivity : AppCompatActivity() {

    private var mActivityAdminReportBinding: ActivityAdminReportBinding? = null
    private var mRevenueValueEventListener: ValueEventListener? = null
    private var mBookingDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminReportBinding = ActivityAdminReportBinding.inflate(layoutInflater)
        setContentView(mActivityAdminReportBinding!!.root)
        initToolbar()
        initListener()
        loadSummary()
        getListRevenue()
    }

    private fun initToolbar() {
        mActivityAdminReportBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminReportBinding!!.toolbar.imgCart.visibility = View.GONE
        mActivityAdminReportBinding!!.toolbar.tvTitle.text = getString(R.string.revenue)
        mActivityAdminReportBinding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initListener() {
        mActivityAdminReportBinding!!.tvDateFrom.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                showDatePicker(this@AdminReportActivity,
                        mActivityAdminReportBinding!!.tvDateFrom.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        mActivityAdminReportBinding!!.tvDateFrom.text = date
                        getListRevenue()
                    }
                })
            }
        })
        mActivityAdminReportBinding!!.tvDateTo.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                showDatePicker(this@AdminReportActivity,
                        mActivityAdminReportBinding!!.tvDateTo.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        mActivityAdminReportBinding!!.tvDateTo.text = date
                        getListRevenue()
                    }
                })
            }
        })
    }

    private fun getListRevenue() {
        mBookingDatabaseReference = ControllerApplication[this].bookingDatabaseReference
        mRevenueValueEventListener?.let { listener ->
            mBookingDatabaseReference?.removeEventListener(listener)
        }
        mRevenueValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list: MutableList<Order> = ArrayList()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)!!
                    if (canAddOrder(order)) {
                        list.add(0, order)
                    }
                }
                handleDataHistories(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        mBookingDatabaseReference?.addValueEventListener(mRevenueValueEventListener!!)
    }

    private fun canAddOrder(order: Order?): Boolean {
        if (order == null) {
            return false
        }
        if (order.getStatusValue() != Order.STATUS_SUCCESS) {
            return false
        }
        val strDateFrom = mActivityAdminReportBinding!!.tvDateFrom.text.toString()
        val strDateTo = mActivityAdminReportBinding!!.tvDateTo.text.toString()
        if (isEmpty(strDateFrom) && isEmpty(strDateTo)) {
            return true
        }
        val strDateOrder = convertTimeStampToDate_2(order.id)
        val longOrder = convertDate2ToTimeStamp(strDateOrder).toLong()
        if (isEmpty(strDateFrom) && !isEmpty(strDateTo)) {
            val longDateTo = convertDate2ToTimeStamp(strDateTo).toLong()
            return longOrder <= longDateTo
        }
        if (!isEmpty(strDateFrom) && isEmpty(strDateTo)) {
            val longDateFrom = convertDate2ToTimeStamp(strDateFrom).toLong()
            return longOrder >= longDateFrom
        }
        val longDateTo = convertDate2ToTimeStamp(strDateTo).toLong()
        val longDateFrom = convertDate2ToTimeStamp(strDateFrom).toLong()
        return longOrder in longDateFrom..longDateTo
    }

    private fun handleDataHistories(list: List<Order>?) {
        if (list == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(this)
        mActivityAdminReportBinding!!.rcvOrderHistory.layoutManager = linearLayoutManager
        val revenueAdapter = RevenueAdapter(list)
        mActivityAdminReportBinding!!.rcvOrderHistory.adapter = revenueAdapter

        // Calculate total
        val strTotalValue: String = "" + getTotalValues(list) + AppConfig.CURRENCY
        mActivityAdminReportBinding!!.tvTotalValue.text = strTotalValue
    }

    private fun getTotalValues(list: List<Order>?): Int {
        if (list == null || list.isEmpty()) {
            return 0
        }
        var total = 0
        for (order in list) {
            total += order.amount
        }
        return total
    }

    private fun loadSummary() {
        val app = ControllerApplication[this]
        app.userDatabaseReference.get().addOnSuccessListener { snapshot ->
            var totalUsers = 0
            var totalShippers = 0
            for (child in snapshot.children) {
                val user = child.getValue(User::class.java) ?: continue
                totalUsers++
                if (user.role == User.ROLE_SHIPPER) {
                    totalShippers++
                }
            }
            mActivityAdminReportBinding!!.tvTotalUsers.text = "${getString(R.string.report_total_users)}: $totalUsers"
            mActivityAdminReportBinding!!.tvTotalShippers.text = "${getString(R.string.report_total_shippers)}: $totalShippers"
        }
        app.bookingDatabaseReference.get().addOnSuccessListener { snapshot ->
            mActivityAdminReportBinding!!.tvTotalOrders.text = "${getString(R.string.report_total_orders)}: ${snapshot.childrenCount}"
        }
        app.restaurantDatabaseReference.get().addOnSuccessListener { snapshot ->
            mActivityAdminReportBinding!!.tvTotalRestaurants.text = "${getString(R.string.report_total_restaurants)}: ${snapshot.childrenCount}"
        }
    }

    override fun onDestroy() {
        mRevenueValueEventListener?.let { listener ->
            mBookingDatabaseReference?.removeEventListener(listener)
        }
        mRevenueValueEventListener = null
        mBookingDatabaseReference = null
        super.onDestroy()
    }
}