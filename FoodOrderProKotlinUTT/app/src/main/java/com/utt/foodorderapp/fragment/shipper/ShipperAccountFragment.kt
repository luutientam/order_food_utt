@file:Suppress("DEPRECATION")

package com.utt.foodorderapp.fragment.shipper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.ChangePasswordActivity
import com.utt.foodorderapp.activity.ShipperEarningsActivity
import com.utt.foodorderapp.activity.ShipperMainActivity
import com.utt.foodorderapp.activity.SignInActivity
import com.utt.foodorderapp.activity.UpdateProfileActivity
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.FragmentAccountBinding
import com.utt.foodorderapp.fragment.BaseFragment
import com.utt.foodorderapp.prefs.DataStoreManager

class ShipperAccountFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentAccountBinding.inflate(inflater, container, false)
        binding.tvEmail.text = DataStoreManager.user?.email
        binding.layoutOrderHistory.visibility = View.GONE
        binding.layoutRestaurantList.visibility = View.GONE
        binding.layoutAddressBook.visibility = View.GONE
        binding.layoutVoucherHub.visibility = View.GONE
        binding.layoutEarnings.visibility = View.VISIBLE
        binding.layoutEarnings.setOnClickListener {
            startActivity(requireActivity(), ShipperEarningsActivity::class.java)
        }
        binding.layoutUpdateProfile.setOnClickListener { startActivity(requireActivity(), UpdateProfileActivity::class.java) }
        binding.layoutChangePassword.setOnClickListener { startActivity(requireActivity(), ChangePasswordActivity::class.java) }
        binding.layoutSignOut.setOnClickListener {
            val currentActivity = requireActivity()
            FirebaseAuth.getInstance().signOut()
            val googleClient = GoogleSignIn.getClient(currentActivity, GoogleSignInOptions.DEFAULT_SIGN_IN)
            googleClient.signOut().addOnCompleteListener {
                DataStoreManager.user = null
                startActivity(currentActivity, SignInActivity::class.java)
                currentActivity.finishAffinity()
            }
        }
        return binding.root
    }

    override fun initToolbar() {
        (activity as? ShipperMainActivity)?.setToolBar(getString(R.string.account))
    }
}
