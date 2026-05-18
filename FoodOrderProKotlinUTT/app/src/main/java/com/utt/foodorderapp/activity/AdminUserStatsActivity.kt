package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ActivityAdminUserStatsBinding
import com.utt.foodorderapp.model.User

/**
 * Thống kê người dùng theo role + tỷ lệ hoạt động.
 */
class AdminUserStatsActivity : BaseActivity() {

    private var binding: ActivityAdminUserStatsBinding? = null
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserStatsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.stats_user_title)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadStats()
    }

    private fun loadStats() {
        val ref = ControllerApplication[this].userDatabaseReference
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0
                var active = 0
                var locked = 0
                var customer = 0
                var shipper = 0
                var admin = 0
                for (child in snapshot.children) {
                    val u = child.getValue(User::class.java) ?: continue
                    u.resolveRole()
                    total++
                    if (u.isActive) active++ else locked++
                    when (u.role) {
                        User.ROLE_CUSTOMER -> customer++
                        User.ROLE_SHIPPER -> shipper++
                        User.ROLE_ADMIN -> admin++
                    }
                }
                val b = binding ?: return
                b.tvTotalUsers.text = total.toString()
                b.tvActiveUsers.text = active.toString()
                b.tvCountCustomer.text = customer.toString()
                b.tvCountShipper.text = shipper.toString()
                b.tvCountAdmin.text = admin.toString()
                b.tvCountLocked.text = locked.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener!!)
    }

    override fun onDestroy() {
        listener?.let {
            ControllerApplication[this].userDatabaseReference.removeEventListener(it)
        }
        listener = null
        super.onDestroy()
    }
}
