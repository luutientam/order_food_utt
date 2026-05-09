package com.utt.foodorderapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utt.foodorderapp.fragment.admin.AdminAccountFragment
import com.utt.foodorderapp.fragment.admin.AdminFeedbackFragment
import com.utt.foodorderapp.fragment.admin.AdminHomeFragment
import com.utt.foodorderapp.fragment.admin.AdminOrderFragment

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