package com.utt.foodorderapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.ChangePasswordActivity
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.activity.OrderHistoryActivity
import com.utt.foodorderapp.activity.SignInActivity
import com.utt.foodorderapp.activity.UpdateProfileActivity
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.FragmentAccountBinding
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user

class AccountFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAccountBinding = FragmentAccountBinding.inflate(inflater, container, false)
        fragmentAccountBinding.tvEmail.text = user!!.email
        fragmentAccountBinding.layoutUpdateProfile.setOnClickListener { onClickUpdateProfile() }
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

    private fun onClickUpdateProfile() {
        startActivity(activity!!, UpdateProfileActivity::class.java)
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