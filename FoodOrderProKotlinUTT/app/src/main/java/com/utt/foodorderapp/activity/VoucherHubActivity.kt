package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.VoucherAdapter
import com.utt.foodorderapp.databinding.ActivityVoucherHubBinding
import com.utt.foodorderapp.model.Promotion

class VoucherHubActivity : BaseActivity() {

    private var binding: ActivityVoucherHubBinding? = null
    private val items = mutableListOf<Promotion>()
    private var adapter: VoucherAdapter? = null
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoucherHubBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.voucher_hub_title)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = VoucherAdapter(items)
        binding!!.rcvVouchers.layoutManager = LinearLayoutManager(this)
        binding!!.rcvVouchers.adapter = adapter

        loadVouchers()
    }

    private fun loadVouchers() {
        val ref = ControllerApplication[this].promotionDatabaseReference
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Promotion>()
                for (child in snapshot.children) {
                    val item = child.getValue(Promotion::class.java) ?: continue
                    if (item.isActive) list.add(item)
                }
                list.sortByDescending { it.discountPercent }
                adapter?.update(list)
                binding?.tvEmpty?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding?.rcvVouchers?.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener!!)
    }

    override fun onDestroy() {
        listener?.let { ControllerApplication[this].promotionDatabaseReference.removeEventListener(it) }
        listener = null
        super.onDestroy()
    }
}
