package com.utt.foodorderapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utt.foodorderapp.fragment.shipper.ShipperAccountFragment
import com.utt.foodorderapp.fragment.shipper.ShipperOrderFragment

class ShipperViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ShipperOrderFragment.newInstance(true)
            1 -> ShipperOrderFragment.newInstance(false)
            2 -> ShipperAccountFragment()
            else -> ShipperOrderFragment.newInstance(true)
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
