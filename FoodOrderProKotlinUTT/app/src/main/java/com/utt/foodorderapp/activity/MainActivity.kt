package com.utt.foodorderapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.MainViewPagerAdapter
import com.utt.foodorderapp.database.FoodDatabase
import com.utt.foodorderapp.data.repository.SessionManager
import com.utt.foodorderapp.databinding.ActivityMainBinding
import com.utt.foodorderapp.event.ReloadListCartEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity() {

    private var mActivityMainBinding: ActivityMainBinding? = null

    companion object {
        const val EXTRA_OPEN_CART = "open_cart"
        private const val TAB_CART = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mActivityMainBinding!!.root)
        mActivityMainBinding!!.viewpager2.isUserInputEnabled = false
        val mainViewPagerAdapter = MainViewPagerAdapter(this)
        mActivityMainBinding!!.viewpager2.adapter = mainViewPagerAdapter
        mActivityMainBinding!!.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> mActivityMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
                    1 -> mActivityMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_cart).isChecked = true
                    2 -> mActivityMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_feedback).isChecked = true
                    3 -> mActivityMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_contact).isChecked = true
                    4 -> mActivityMainBinding!!.bottomNavigation.menu.findItem(R.id.nav_account).isChecked = true
                }
            }
        })
        mActivityMainBinding!!.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    mActivityMainBinding!!.viewpager2.currentItem = 0
                }
                R.id.nav_cart -> {
                    mActivityMainBinding!!.viewpager2.currentItem = 1
                }
                R.id.nav_feedback -> {
                    mActivityMainBinding!!.viewpager2.currentItem = 2
                }
                R.id.nav_contact -> {
                    mActivityMainBinding!!.viewpager2.currentItem = 3
                }
                R.id.nav_account -> {
                    mActivityMainBinding!!.viewpager2.currentItem = 4
                }
            }
            true
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showConfirmExitApp()
            }
        })
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        handleOpenCartIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOpenCartIntent(intent)
    }

    private fun handleOpenCartIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_CART, false) == true) {
            mActivityMainBinding?.viewpager2?.currentItem = TAB_CART
            intent.removeExtra(EXTRA_OPEN_CART)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCartChanged(@Suppress("UNUSED_PARAMETER") event: ReloadListCartEvent?) {
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val binding = mActivityMainBinding ?: return
        val count = FoodDatabase.getInstance(this)?.foodDAO()?.listFoodCart?.size ?: 0
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart)
        badge.isVisible = count > 0
        if (count > 0) {
            badge.maxCharacterCount = 3
            badge.number = count
        }
    }

    override fun onResume() {
        super.onResume()
        SessionManager.refreshAndRouteIfNeeded(this)
        updateCartBadge()
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive { _: MaterialDialog?, _: DialogAction? -> finishAffinity() }
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show()
    }

    fun setToolBar(isHome: Boolean, title: String?) {
        if (isHome) {
            mActivityMainBinding!!.toolbar.layoutToolbar.visibility = View.GONE
            return
        }
        mActivityMainBinding!!.toolbar.layoutToolbar.visibility = View.VISIBLE
        mActivityMainBinding!!.toolbar.tvTitle.text = title
    }
}