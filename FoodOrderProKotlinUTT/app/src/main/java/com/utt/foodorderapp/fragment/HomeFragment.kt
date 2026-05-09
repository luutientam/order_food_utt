package com.utt.foodorderapp.fragment

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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.FoodDetailActivity
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.adapter.FoodGridAdapter
import com.utt.foodorderapp.adapter.FoodPopularAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.getTextSearch
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.FragmentHomeBinding
import com.utt.foodorderapp.listener.IOnClickFoodItemListener
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.presentation.home.HomeViewModel
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import java.util.*

class HomeFragment : BaseFragment() {

    private var mFragmentHomeBinding: FragmentHomeBinding? = null
    private var mListFood: MutableList<Food>? = null
    private var mListFoodPopular: MutableList<Food>? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private var pageChangeCallback: OnPageChangeCallback? = null
    private val mRunnableBanner = Runnable {
        val binding = mFragmentHomeBinding ?: return@Runnable
        if (mListFoodPopular.isNullOrEmpty()) {
            return@Runnable
        }
        if (binding.viewpager2.currentItem == mListFoodPopular!!.size - 1) {
            binding.viewpager2.currentItem = 0
            return@Runnable
        }
        binding.viewpager2.currentItem = binding.viewpager2.currentItem + 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        observeViewModel()
        homeViewModel.loadFoods("")
        initListener()
        return mFragmentHomeBinding!!.root
    }

    override fun initToolbar() {
        (activity as? MainActivity)?.setToolBar(true, getString(R.string.home))
    }

    private fun initListener() {
        val binding = mFragmentHomeBinding ?: return
        binding.edtSearchName.addTextChangedListener(object : TextWatcher {
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
        binding.imgSearch.setOnClickListener { searchFood() }
        binding.edtSearchName.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun displayListFoodPopular() {
        val binding = mFragmentHomeBinding ?: return
        val mFoodPopularAdapter = FoodPopularAdapter(getListFoodPopular(), object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                goToFoodDetail(food)
            }
        })
        binding.viewpager2.adapter = mFoodPopularAdapter
        binding.indicator3.setViewPager(binding.viewpager2)
        pageChangeCallback?.let { binding.viewpager2.unregisterOnPageChangeCallback(it) }
        pageChangeCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        }
        binding.viewpager2.registerOnPageChangeCallback(pageChangeCallback!!)
    }

    private fun displayListFoodSuggest() {
        val binding = mFragmentHomeBinding ?: return
        val gridLayoutManager = GridLayoutManager(activity, 2)
        binding.rcvFood.layoutManager = gridLayoutManager
        val mFoodGridAdapter = FoodGridAdapter(mListFood, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                goToFoodDetail(food)
            }
        })
        binding.rcvFood.adapter = mFoodGridAdapter
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
        homeViewModel.loadFoods(key)
    }

    private fun searchFood() {
        val binding = mFragmentHomeBinding ?: return
        val strKey = binding.edtSearchName.text.toString().trim { it <= ' ' }
        if (mListFood != null) mListFood!!.clear()
        getListFoodFromFirebase(strKey)
        activity?.let { hideSoftKeyboard(it) }
    }

    private fun goToFoodDetail(food: Food) {
        val currentActivity = activity ?: return
        val bundle = Bundle()
        bundle.putSerializable(AppConfig.KEY_INTENT_FOOD_OBJECT, food)
        startActivity(currentActivity, FoodDetailActivity::class.java, bundle)
    }

    override fun onPause() {
        super.onPause()
        mHandlerBanner.removeCallbacks(mRunnableBanner)
    }

    override fun onResume() {
        super.onResume()
        if (mFragmentHomeBinding != null) {
            mHandlerBanner.postDelayed(mRunnableBanner, 3000)
        }
    }

    override fun onDestroyView() {
        mHandlerBanner.removeCallbacks(mRunnableBanner)
        val binding = mFragmentHomeBinding
        if (binding != null && pageChangeCallback != null) {
            binding.viewpager2.unregisterOnPageChangeCallback(pageChangeCallback!!)
        }
        pageChangeCallback = null
        mFragmentHomeBinding = null
        super.onDestroyView()
    }

    private fun observeViewModel() {
        homeViewModel.foodsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Unit
                is UiState.Error -> showToastMessage(activity, getString(R.string.msg_get_date_error))
                is UiState.Success -> {
                    mFragmentHomeBinding?.layoutContent?.visibility = View.VISIBLE
                    mListFood = state.data.toMutableList()
                    displayListFoodPopular()
                    displayListFoodSuggest()
                }
            }
        }
    }
}