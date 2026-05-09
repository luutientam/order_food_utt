package com.pro.foodorder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pro.foodorder.fragment.admin.AdminAccountFragment
import com.pro.foodorder.fragment.admin.AdminFeedbackFragment
import com.pro.foodorder.fragment.admin.AdminHomeFragment
import com.pro.foodorder.fragment.admin.AdminOrderFragment

class AdminViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminHomeFragment()
            1 -> AdminFeedbackFragment()
            2 -> AdminOrderFragment()
            3 -> AdminAccountFragment()
            else -> AdminHomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}