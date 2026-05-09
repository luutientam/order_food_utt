package com.pro.foodorder.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.foodorder.ControllerApplication
import com.pro.foodorder.R
import com.pro.foodorder.activity.FoodDetailActivity
import com.pro.foodorder.activity.MainActivity
import com.pro.foodorder.adapter.FoodGridAdapter
import com.pro.foodorder.adapter.FoodPopularAdapter
import com.pro.foodorder.constant.Constant
import com.pro.foodorder.constant.GlobalFunction.getTextSearch
import com.pro.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.pro.foodorder.constant.GlobalFunction.showToastMessage
import com.pro.foodorder.constant.GlobalFunction.startActivity
import com.pro.foodorder.databinding.FragmentHomeBinding
import com.pro.foodorder.listener.IOnClickFoodItemListener
import com.pro.foodorder.model.Food
import com.pro.foodorder.utils.StringUtil.isEmpty
import java.util.*

class HomeFragment : BaseFragment() {

    private var mFragmentHomeBinding: FragmentHomeBinding? = null
    private var mListFood: MutableList<Food>? = null
    private var mListFoodPopular: MutableList<Food>? = null
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private val mRunnableBanner = Runnable {
        if (mListFoodPopular == null || mListFoodPopular!!.isEmpty()) {
            return@Runnable
        }
        if (mFragmentHomeBinding!!.viewpager2.currentItem == mListFoodPopular!!.size - 1) {
            mFragmentHomeBinding!!.viewpager2.currentItem = 0
            return@Runnable
        }
        mFragmentHomeBinding!!.viewpager2.currentItem = mFragmentHomeBinding!!.viewpager2.currentItem + 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        getListFoodFromFirebase("")
        initListener()
        return mFragmentHomeBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(true, getString(R.string.home))
        }
    }

    private fun initListener() {
        mFragmentHomeBinding!!.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    if (mListFood != null) mListFood!!.clear()
                    getListFoodFromFirebase("")
                }
            }
        })
        mFragmentHomeBinding!!.imgSearch.setOnClickListener { searchFood() }
        mFragmentHomeBinding!!.edtSearchName.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun displayListFoodPopular() {
        val mFoodPopularAdapter = FoodPopularAdapter(getListFoodPopular(), object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                goToFoodDetail(food)
            }
        })
        mFragmentHomeBinding!!.viewpager2.adapter = mFoodPopularAdapter
        mFragmentHomeBinding!!.indicator3.setViewPager(mFragmentHomeBinding!!.viewpager2)
        mFragmentHomeBinding!!.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }

    private fun displayListFoodSuggest() {
        val gridLayoutManager = GridLayoutManager(activity, 2)
        mFragmentHomeBinding!!.rcvFood.layoutManager = gridLayoutManager
        val mFoodGridAdapter = FoodGridAdapter(mListFood, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                goToFoodDetail(food)
            }
        })
        mFragmentHomeBinding!!.rcvFood.adapter = mFoodGridAdapter
    }

    private fun getListFoodPopular(): MutableList<Food>? {
        mListFoodPopular = ArrayList()
        if (mListFood == null || mListFood!!.isEmpty()) {
            return mListFoodPopular
        }
        for (food in mListFood!!) {
            if (food.isPopular) {
                mListFoodPopular?.add(food)
            }
        }
        return mListFoodPopular
    }

    private fun getListFoodFromFirebase(key: String) {
        if (activity == null) {
            return
        }
        ControllerApplication[activity!!].foodDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mFragmentHomeBinding!!.layoutContent.visibility = View.VISIBLE
                mListFood = ArrayList()
                for (dataSnapshot in snapshot.children) {
                    val food = dataSnapshot.getValue(Food::class.java) ?: return
                    if (isEmpty(key)) {
                        mListFood?.add(0, food)
                    } else {
                        if (getTextSearch(food.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                                        .contains(getTextSearch(key).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                            mListFood?.add(0, food)
                        }
                    }
                }
                displayListFoodPopular()
                displayListFoodSuggest()
            }

            override fun onCancelled(error: DatabaseError) {
                showToastMessage(activity, getString(R.string.msg_get_date_error))
            }
        })
    }

    private fun searchFood() {
        val strKey = mFragmentHomeBinding!!.edtSearchName.text.toString().trim { it <= ' ' }
        if (mListFood != null) mListFood!!.clear()
        getListFoodFromFirebase(strKey)
        hideSoftKeyboard(activity!!)
    }

    private fun goToFoodDetail(food: Food) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, food)
        startActivity(activity!!, FoodDetailActivity::class.java, bundle)
    }

    override fun onPause() {
        super.onPause()
        mHandlerBanner.removeCallbacks(mRunnableBanner)
    }

    override fun onResume() {
        super.onResume()
        mHandlerBanner.postDelayed(mRunnableBanner, 3000)
    }
}