package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.ActivityUpdateProfileBinding
import com.utt.foodorderapp.prefs.DataStoreManager

class UpdateProfileActivity : BaseActivity() {

    private var binding: ActivityUpdateProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initToolbar()
        val user = DataStoreManager.user
        binding!!.edtName.setText(user?.name ?: "")
        binding!!.edtPhone.setText(user?.phone ?: "")
        binding!!.tvSave.setOnClickListener { saveProfile() }
    }

    private fun initToolbar() {
        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.account)
        binding!!.toolbar.imgBack.setOnClickListener { finish() }
    }

    private fun saveProfile() {
        val user = DataStoreManager.user ?: return
        val userId = user.uid ?: return
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = binding!!.edtName.text.toString().trim()
        map["phone"] = binding!!.edtPhone.text.toString().trim()
        ControllerApplication[this].userDatabaseReference.child(userId).updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
            user.name = map["name"] as String
            user.phone = map["phone"] as String
            DataStoreManager.user = user
            showToastMessage(this, getString(R.string.action_edit))
            finish()
        }
    }
}
