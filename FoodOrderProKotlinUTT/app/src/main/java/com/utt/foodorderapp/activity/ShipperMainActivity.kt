package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.ShipperViewPagerAdapter
import com.utt.foodorderapp.databinding.ActivityShipperMainBinding

class ShipperMainActivity : BaseActivity() {

    private var binding: ActivityShipperMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShipperMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.viewpager2.isUserInputEnabled = false
        binding!!.viewpager2.adapter = ShipperViewPagerAdapter(this)
        binding!!.viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding!!.bottomNavigation.menu.findItem(R.id.nav_pick_order).isChecked = true
                    1 -> binding!!.bottomNavigation.menu.findItem(R.id.nav_my_order).isChecked = true
                    2 -> binding!!.bottomNavigation.menu.findItem(R.id.nav_account).isChecked = true
                }
            }
        })
        binding!!.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_pick_order -> binding!!.viewpager2.currentItem = 0
                R.id.nav_my_order -> binding!!.viewpager2.currentItem = 1
                R.id.nav_account -> binding!!.viewpager2.currentItem = 2
            }
            true
        }
    }

    fun setToolBar(title: String?) {
        binding!!.toolbar.layoutToolbar.visibility = View.VISIBLE
        binding!!.toolbar.tvTitle.text = title
    }
}
