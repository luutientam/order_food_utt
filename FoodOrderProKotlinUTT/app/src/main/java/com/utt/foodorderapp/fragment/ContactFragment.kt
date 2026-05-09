package com.utt.foodorderapp.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.adapter.ContactAdapter
import com.utt.foodorderapp.adapter.ContactAdapter.ICallPhone
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.callPhoneNumber
import com.utt.foodorderapp.databinding.FragmentContactBinding
import com.utt.foodorderapp.model.Contact

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
        mFragmentContactBinding?.tvAboutUsTitle?.text = AppConfig.ABOUT_US_TITLE
        mFragmentContactBinding?.tvAboutUsContent?.text = AppConfig.ABOUT_US_CONTENT
        mFragmentContactBinding?.tvAboutUsWebsite?.text = AppConfig.ABOUT_US_WEBSITE_TITLE

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
                    Uri.parse(AppConfig.WEBSITE)
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