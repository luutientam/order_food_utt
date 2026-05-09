package com.pro.foodorder.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.pro.foodorder.constant.AboutUsConfig
import com.pro.foodorder.constant.GlobalFunction.gotoMainActivity
import com.pro.foodorder.constant.GlobalFunction.startActivity
import com.pro.foodorder.databinding.ActivitySplashBinding
import com.pro.foodorder.prefs.DataStoreManager.Companion.user
import com.pro.foodorder.utils.StringUtil.isEmpty

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
        mActivitySplashBinding?.tvAboutUsTitle?.text = AboutUsConfig.ABOUT_US_TITLE
        mActivitySplashBinding?.tvAboutUsSlogan?.text = AboutUsConfig.ABOUT_US_SLOGAN
    }

    private fun goToNextActivity() {
        if (user != null && !isEmpty(user!!.email)) {
            gotoMainActivity(this)
        } else {
            startActivity(this, SignInActivity::class.java)
        }
        finish()
    }
}