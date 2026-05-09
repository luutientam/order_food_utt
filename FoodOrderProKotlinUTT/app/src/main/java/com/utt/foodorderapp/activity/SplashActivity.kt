package com.utt.foodorderapp.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.gotoMainActivity
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.ActivitySplashBinding
import com.utt.foodorderapp.prefs.DataStoreManager
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user
import com.utt.foodorderapp.utils.StringUtil.isEmpty

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private var mActivitySplashBinding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mActivitySplashBinding?.root)

        initUi()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ goToNextActivity() }, 2000)
    }

    private fun initUi() {
        mActivitySplashBinding?.tvAboutUsTitle?.text = AppConfig.ABOUT_US_TITLE
        mActivitySplashBinding?.tvAboutUsSlogan?.text = AppConfig.ABOUT_US_SLOGAN
    }

    private fun goToNextActivity() {
        val hasFirebaseSession = FirebaseAuth.getInstance().currentUser != null
        if (hasFirebaseSession && user != null && !isEmpty(user!!.email)) {
            gotoMainActivity(this)
        } else {
            DataStoreManager.user = null
            startActivity(this, SignInActivity::class.java)
        }
        finish()
    }
}