package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.EarningsAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.databinding.ActivityShipperEarningsBinding
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.prefs.DataStoreManager
import java.util.Calendar

/**
 * Hiển thị thu nhập của shipper hôm nay / tuần này / tháng này
 * và danh sách đơn đã giao thành công.
 */
class ShipperEarningsActivity : BaseActivity() {

    private var binding: ActivityShipperEarningsBinding? = null
    private val items = mutableListOf<Order>()
    private var adapter: EarningsAdapter? = null
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShipperEarningsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.earnings_title)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = EarningsAdapter(items)
        binding!!.rcvEarnings.layoutManager = LinearLayoutManager(this)
        binding!!.rcvEarnings.adapter = adapter

        loadEarnings()
    }

    private fun loadEarnings() {
        val uid = DataStoreManager.user?.uid ?: return
        val ref = ControllerApplication[this].bookingDatabaseReference
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mine = ArrayList<Order>()
                for (child in snapshot.children) {
                    val order = child.getValue(Order::class.java) ?: continue
                    if (order.shipperId == uid && order.getStatusValue() == Order.STATUS_SUCCESS) {
                        mine.add(order)
                    }
                }
                mine.sortByDescending { it.id }
                items.clear()
                items.addAll(mine)
                adapter?.notifyDataSetChanged()

                renderSummary(mine)
                val isEmpty = mine.isEmpty()
                binding?.tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding?.rcvEarnings?.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener!!)
    }

    private fun renderSummary(orders: List<Order>) {
        val now = Calendar.getInstance()
        val startOfToday = startOfDay(now)
        val startOfWeek = startOfWeek(now)
        val startOfMonth = startOfMonth(now)

        var today = 0L
        var week = 0L
        var month = 0L
        for (o in orders) {
            val amount = o.amount.toLong()
            if (o.id >= startOfMonth) month += amount
            if (o.id >= startOfWeek) week += amount
            if (o.id >= startOfToday) today += amount
        }
        binding?.tvToday?.text = "$today${AppConfig.CURRENCY}"
        binding?.tvWeek?.text = "$week${AppConfig.CURRENCY}"
        binding?.tvMonth?.text = "$month${AppConfig.CURRENCY}"
    }

    private fun startOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun startOfWeek(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
        return c.timeInMillis
    }

    private fun startOfMonth(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.set(Calendar.DAY_OF_MONTH, 1)
        return c.timeInMillis
    }

    override fun onDestroy() {
        listener?.let { ControllerApplication[this].bookingDatabaseReference.removeEventListener(it) }
        listener = null
        super.onDestroy()
    }
}
