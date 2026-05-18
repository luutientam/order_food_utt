package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ActivityAdminSystemReportBinding
import com.utt.foodorderapp.model.Order

/**
 * Báo cáo hoạt động hệ thống: phân bố đơn theo trạng thái,
 * tổng số đơn, tỷ lệ thành công.
 */
class AdminSystemReportActivity : BaseActivity() {

    private var binding: ActivityAdminSystemReportBinding? = null
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSystemReportBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.system_report_title)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadReport()
    }

    private fun loadReport() {
        val ref = ControllerApplication[this].bookingDatabaseReference
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val counts = IntArray(6) // 0..5
                var total = 0
                for (child in snapshot.children) {
                    val order = child.getValue(Order::class.java) ?: continue
                    val status = order.getStatusValue()
                    if (status in 0..5) counts[status]++
                    total++
                }
                renderRows(counts, total)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener!!)
    }

    private fun renderRows(counts: IntArray, total: Int) {
        val b = binding ?: return
        bindRow(b.rowStatusNew.root, getString(R.string.status_new), counts[Order.STATUS_NEW], total, R.color.statusNew)
        bindRow(b.rowStatusPreparing.root, getString(R.string.status_preparing), counts[Order.STATUS_PREPARING], total, R.color.statusPreparing)
        bindRow(b.rowStatusDelivering.root, getString(R.string.status_process), counts[Order.STATUS_DELIVERING], total, R.color.statusDelivering)
        bindRow(b.rowStatusSuccess.root, getString(R.string.status_success), counts[Order.STATUS_SUCCESS], total, R.color.statusSuccess)
        bindRow(b.rowStatusCancel.root, getString(R.string.status_cancel), counts[Order.STATUS_CANCEL], total, R.color.statusCancel)
        bindRow(b.rowStatusFail.root, getString(R.string.status_fail), counts[Order.STATUS_FAIL], total, R.color.statusFail)
        b.tvTotalOrders.text = total.toString()
        val successRate = if (total > 0) counts[Order.STATUS_SUCCESS] * 100 / total else 0
        b.tvSuccessRate.text = "$successRate%"
    }

    private fun bindRow(view: View, label: String, count: Int, total: Int, colorRes: Int) {
        view.findViewById<View>(R.id.dot_status).setBackgroundColor(ContextCompat.getColor(this, colorRes))
        view.findViewById<TextView>(R.id.tv_label).text = label
        view.findViewById<TextView>(R.id.tv_count).text = count.toString()
        val percent = if (total > 0) count * 100 / total else 0
        view.findViewById<TextView>(R.id.tv_percent).text = "$percent%"
    }

    override fun onDestroy() {
        listener?.let {
            ControllerApplication[this].bookingDatabaseReference.removeEventListener(it)
        }
        listener = null
        super.onDestroy()
    }
}
