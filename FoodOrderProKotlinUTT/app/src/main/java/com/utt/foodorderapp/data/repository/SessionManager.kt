package com.utt.foodorderapp.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.activity.SignInActivity
import com.utt.foodorderapp.constant.GlobalFunction
import com.utt.foodorderapp.model.User
import com.utt.foodorderapp.prefs.DataStoreManager

/**
 * Đồng bộ profile user trong DataStoreManager với Firebase.
 * Mục đích: phát hiện role bị đổi, tài khoản bị khóa,
 * hoặc profile bị admin xóa giữa phiên.
 */
object SessionManager {

    private val userRepository = UserRepository()

    fun refreshAndRouteIfNeeded(context: Context, onValid: () -> Unit = {}) {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        val cached = DataStoreManager.user

        if (firebaseUid.isNullOrEmpty() || cached?.uid.isNullOrEmpty()) {
            forceSignOut(context)
            return
        }

        userRepository.getUserProfile(firebaseUid) { profile ->
            if (profile == null) {
                forceSignOut(context)
                return@getUserProfile
            }
            profile.resolveRole()
            if (!profile.isActive) {
                forceSignOut(context)
                return@getUserProfile
            }
            if (profile.role != cached!!.role) {
                DataStoreManager.user = profile
                GlobalFunction.gotoMainActivity(context)
                return@getUserProfile
            }
            DataStoreManager.user = profile
            onValid()
        }
    }

    fun forceSignOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        DataStoreManager.user = null
        GlobalFunction.startActivity(context, SignInActivity::class.java)
    }
}
