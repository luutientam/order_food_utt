@file:Suppress("DEPRECATION")

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
import com.utt.foodorderapp.activity.AdminSystemReportActivity
import com.utt.foodorderapp.activity.AdminUserManagementActivity
import com.utt.foodorderapp.activity.AdminUserStatsActivity
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
        fragmentAdminAccountBinding.tvEmail.text = user?.email ?: ""
        fragmentAdminAccountBinding.layoutUpdateProfile.setOnClickListener { onClickUpdateProfile() }
        fragmentAdminAccountBinding.layoutManageAccounts.setOnClickListener { onClickManageAccounts() }
        fragmentAdminAccountBinding.layoutManageCatalog.setOnClickListener { onClickManageCatalog() }
        fragmentAdminAccountBinding.layoutManagePromotion.setOnClickListener { onClickManagePromotion() }
        fragmentAdminAccountBinding.layoutReport.setOnClickListener { onClickReport() }
        fragmentAdminAccountBinding.layoutUserStats.setOnClickListener {
            val act = activity ?: return@setOnClickListener
            startActivity(act, AdminUserStatsActivity::class.java)
        }
        fragmentAdminAccountBinding.layoutSystemReport.setOnClickListener {
            val act = activity ?: return@setOnClickListener
            startActivity(act, AdminSystemReportActivity::class.java)
        }
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
        val act = activity ?: return
        startActivity(act, AdminReportActivity::class.java)
    }

    private fun onClickUpdateProfile() {
        val act = activity ?: return
        startActivity(act, UpdateProfileActivity::class.java)
    }

    private fun onClickManageAccounts() {
        val act = activity ?: return
        startActivity(act, AdminUserManagementActivity::class.java)
    }

    private fun onClickManageCatalog() {
        val act = activity ?: return
        startActivity(act, CatalogManagementActivity::class.java)
    }

    private fun onClickManagePromotion() {
        val act = activity ?: return
        startActivity(act, AdminPromotionManagementActivity::class.java)
    }

    private fun onClickChangePassword() {
        val act = activity ?: return
        startActivity(act, ChangePasswordActivity::class.java)
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