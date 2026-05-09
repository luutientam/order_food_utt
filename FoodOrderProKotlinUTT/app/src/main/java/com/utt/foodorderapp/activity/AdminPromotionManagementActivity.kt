package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.PromotionAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.ActivityAdminPromotionManagementBinding
import com.utt.foodorderapp.model.Promotion
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import java.util.Locale

class AdminPromotionManagementActivity : BaseActivity() {

    private var binding: ActivityAdminPromotionManagementBinding? = null
    private var listPromotion: MutableList<Promotion> = ArrayList()
    private var listener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPromotionManagementBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initToolbar()
        initData()
        initListener()
    }

    private fun initToolbar() {
        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.manage_promotion)
        binding!!.toolbar.imgBack.setOnClickListener { finish() }
    }

    private fun initData() {
        binding!!.rcvPromotion.layoutManager = LinearLayoutManager(this)
        binding!!.rcvPromotion.adapter = PromotionAdapter(listPromotion, object : PromotionAdapter.IPromotionAction {
            override fun onToggle(promotion: Promotion) {
                val code = promotion.code ?: return
                promotion.isActive = !promotion.isActive
                ControllerApplication[this@AdminPromotionManagementActivity].promotionDatabaseReference
                        .child(code).setValue(promotion)
            }

            override fun onDelete(promotion: Promotion) {
                val code = promotion.code ?: return
                ControllerApplication[this@AdminPromotionManagementActivity].promotionDatabaseReference
                        .child(code).removeValue()
            }
        })
        subscribePromotions()
    }

    private fun initListener() {
        binding!!.tvAddPromotion.setOnClickListener {
            val code = binding!!.edtCode.text.toString().trim().uppercase(Locale.getDefault())
            val title = binding!!.edtTitle.text.toString().trim()
            val discountPercent = binding!!.edtDiscountPercent.text.toString().toIntOrNull() ?: 0
            val minOrder = binding!!.edtMinOrder.text.toString().toIntOrNull() ?: 0
            val maxDiscount = binding!!.edtMaxDiscount.text.toString().toIntOrNull() ?: 0
            if (isEmpty(code) || isEmpty(title)) {
                showToastMessage(this, getString(R.string.msg_promotion_code_required))
                return@setOnClickListener
            }
            if (discountPercent <= 0 || discountPercent > 100) {
                showToastMessage(this, getString(R.string.msg_promotion_invalid_percent))
                return@setOnClickListener
            }
            val promotion = Promotion(code, title, discountPercent, minOrder, maxDiscount, true)
            ControllerApplication[this].promotionDatabaseReference.child(code).setValue(promotion)
            binding!!.edtCode.setText("")
            binding!!.edtTitle.setText("")
            binding!!.edtDiscountPercent.setText("")
            binding!!.edtMinOrder.setText("")
            binding!!.edtMaxDiscount.setText("")
        }
    }

    private fun subscribePromotions() {
        listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val promotion = snapshot.getValue(Promotion::class.java) ?: return
                listPromotion.add(0, promotion)
                binding!!.rcvPromotion.adapter?.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val promotion = snapshot.getValue(Promotion::class.java) ?: return
                for (i in listPromotion.indices) {
                    if (listPromotion[i].code == promotion.code) {
                        listPromotion[i] = promotion
                        break
                    }
                }
                binding!!.rcvPromotion.adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val promotion = snapshot.getValue(Promotion::class.java) ?: return
                listPromotion.removeAll { it.code == promotion.code }
                binding!!.rcvPromotion.adapter?.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ControllerApplication[this].promotionDatabaseReference.addChildEventListener(listener!!)
    }

    override fun onDestroy() {
        if (listener != null) {
            ControllerApplication[this].promotionDatabaseReference.removeEventListener(listener!!)
        }
        super.onDestroy()
    }
}
