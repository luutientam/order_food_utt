package com.pro.foodorder.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.pro.foodorder.R
import com.pro.foodorder.activity.MainActivity
import com.pro.foodorder.adapter.ContactAdapter
import com.pro.foodorder.adapter.ContactAdapter.ICallPhone
import com.pro.foodorder.constant.AboutUsConfig
import com.pro.foodorder.constant.GlobalFunction.callPhoneNumber
import com.pro.foodorder.databinding.FragmentContactBinding
import com.pro.foodorder.model.Contact

class ContactFragment : BaseFragment() {

    private var mFragmentContactBinding: FragmentContactBinding? = null
    private var mContactAdapter: ContactAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentContactBinding = FragmentContactBinding.inflate(inflater, container, false)

        initUi()
        initListener()

        return mFragmentContactBinding!!.root
    }

    private fun initUi() {
        mFragmentContactBinding?.tvAboutUsTitle?.text = AboutUsConfig.ABOUT_US_TITLE
        mFragmentContactBinding?.tvAboutUsContent?.text = AboutUsConfig.ABOUT_US_CONTENT
        mFragmentContactBinding?.tvAboutUsWebsite?.text = AboutUsConfig.ABOUT_US_WEBSITE_TITLE

        mContactAdapter = ContactAdapter(activity, getListContact(), object : ICallPhone {
            override fun onClickCallPhone() {
                callPhoneNumber(activity!!)
            }
        })
        val layoutManager = GridLayoutManager(activity, 3)
        mFragmentContactBinding?.rcvData?.isNestedScrollingEnabled = false
        mFragmentContactBinding?.rcvData?.isFocusable = false
        mFragmentContactBinding?.rcvData?.layoutManager = layoutManager
        mFragmentContactBinding?.rcvData?.adapter = mContactAdapter
    }

    private fun initListener() {
        mFragmentContactBinding?.layoutWebsite?.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(AboutUsConfig.WEBSITE)
                )
            )
        }
    }

    private fun getListContact(): List<Contact> {
        val contactArrayList: MutableList<Contact> = ArrayList()
        contactArrayList.add(Contact(Contact.FACEBOOK, R.drawable.ic_facebook))
        contactArrayList.add(Contact(Contact.HOTLINE, R.drawable.ic_hotline))
        contactArrayList.add(Contact(Contact.GMAIL, R.drawable.ic_gmail))
        contactArrayList.add(Contact(Contact.SKYPE, R.drawable.ic_skype))
        contactArrayList.add(Contact(Contact.YOUTUBE, R.drawable.ic_youtube))
        contactArrayList.add(Contact(Contact.ZALO, R.drawable.ic_zalo))
        return contactArrayList
    }

    override fun onDestroy() {
        super.onDestroy()
        mContactAdapter!!.release()
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.contact))
        }
    }
}