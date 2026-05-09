package com.utt.foodorderapp.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.AdminMainActivity
import com.utt.foodorderapp.activity.AdminPromotionManagementActivity
import com.utt.foodorderapp.activity.AdminReportActivity
import com.utt.foodorderapp.activity.AdminUserManagementActivity
import com.utt.foodorderapp.activity.CatalogManagementActivity
import com.utt.foodorderapp.activity.ChangePasswordActivity
import com.utt.foodorderapp.activity.SignInActivity
import com.utt.foodorderapp.activity.UpdateProfileActivity
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.FragmentAdminAccountBinding
import com.utt.foodorderapp.fragment.BaseFragment
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user

class AdminAccountFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAdminAccountBinding = FragmentAdminAccountBinding.inflate(inflater, container, false)
        fragmentAdminAccountBinding.tvEmail.text = user!!.email
        fragmentAdminAccountBinding.layoutUpdateProfile.setOnClickListener { onClickUpdateProfile() }
        fragmentAdminAccountBinding.layoutManageAccounts.setOnClickListener { onClickManageAccounts() }
        fragmentAdminAccountBinding.layoutManageCatalog.setOnClickListener { onClickManageCatalog() }
        fragmentAdminAccountBinding.layoutManagePromotion.setOnClickListener { onClickManagePromotion() }
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

    private fun onClickUpdateProfile() {
        startActivity(activity!!, UpdateProfileActivity::class.java)
    }

    private fun onClickManageAccounts() {
        startActivity(activity!!, AdminUserManagementActivity::class.java)
    }

    private fun onClickManageCatalog() {
        startActivity(activity!!, CatalogManagementActivity::class.java)
    }

    private fun onClickManagePromotion() {
        startActivity(activity!!, AdminPromotionManagementActivity::class.java)
    }

    private fun onClickChangePassword() {
        startActivity(activity!!, ChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) {
            return
        }
        val currentActivity = activity ?: return
        FirebaseAuth.getInstance().signOut()
        val googleClient = GoogleSignIn.getClient(currentActivity, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleClient.signOut().addOnCompleteListener {
            user = null
            startActivity(currentActivity, SignInActivity::class.java)
            currentActivity.finishAffinity()
        }
    }
}