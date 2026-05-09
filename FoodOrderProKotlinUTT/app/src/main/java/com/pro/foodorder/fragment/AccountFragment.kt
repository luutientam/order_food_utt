package com.pro.foodorder.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.pro.foodorder.R
import com.pro.foodorder.activity.ChangePasswordActivity
import com.pro.foodorder.activity.MainActivity
import com.pro.foodorder.activity.OrderHistoryActivity
import com.pro.foodorder.activity.SignInActivity
import com.pro.foodorder.constant.GlobalFunction.startActivity
import com.pro.foodorder.databinding.FragmentAccountBinding
import com.pro.foodorder.prefs.DataStoreManager.Companion.user

class AccountFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAccountBinding = FragmentAccountBinding.inflate(inflater, container, false)
        fragmentAccountBinding.tvEmail.text = user!!.email
        fragmentAccountBinding.layoutSignOut.setOnClickListener { onClickSignOut() }
        fragmentAccountBinding.layoutChangePassword.setOnClickListener { onClickChangePassword() }
        fragmentAccountBinding.layoutOrderHistory.setOnClickListener { onClickOrderHistory() }
        return fragmentAccountBinding.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.account))
        }
    }

    private fun onClickOrderHistory() {
        startActivity(activity!!, OrderHistoryActivity::class.java)
    }

    private fun onClickChangePassword() {
        startActivity(activity!!, ChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) {
            return
        }
        FirebaseAuth.getInstance().signOut()
        user = null
        startActivity(activity!!, SignInActivity::class.java)
        activity!!.finishAffinity()
    }
}