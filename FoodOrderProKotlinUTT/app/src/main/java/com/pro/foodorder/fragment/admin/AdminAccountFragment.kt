package com.pro.foodorder.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.pro.foodorder.R
import com.pro.foodorder.activity.AdminMainActivity
import com.pro.foodorder.activity.AdminReportActivity
import com.pro.foodorder.activity.ChangePasswordActivity
import com.pro.foodorder.activity.SignInActivity
import com.pro.foodorder.constant.GlobalFunction.startActivity
import com.pro.foodorder.databinding.FragmentAdminAccountBinding
import com.pro.foodorder.fragment.BaseFragment
import com.pro.foodorder.prefs.DataStoreManager.Companion.user

class AdminAccountFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAdminAccountBinding = FragmentAdminAccountBinding.inflate(inflater, container, false)
        fragmentAdminAccountBinding.tvEmail.text = user!!.email
        fragmentAdminAccountBinding.layoutReport.setOnClickListener { onClickReport() }
        fragmentAdminAccountBinding.layoutSignOut.setOnClickListener { onClickSignOut() }
        fragmentAdminAccountBinding.layoutChangePassword.setOnClickListener { onClickChangePassword() }
        return fragmentAdminAccountBinding.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.account))
        }
    }

    private fun onClickReport() {
        startActivity(activity!!, AdminReportActivity::class.java)
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