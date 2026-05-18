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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.FoodDetailActivity
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.adapter.CategoryChipAdapter
import com.utt.foodorderapp.adapter.FoodGridAdapter
import com.utt.foodorderapp.adapter.FoodPopularAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.FragmentHomeBinding
import com.utt.foodorderapp.listener.IOnClickFoodItemListener
import com.utt.foodorderapp.model.Category
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.presentation.home.HomeViewModel

class HomeFragment : BaseFragment() {

    private var mFragmentHomeBinding: FragmentHomeBinding? = null
    private var mListFood: MutableList<Food>? = null
    private var mAllFoods: List<Food> = emptyList()
    private var mListFoodPopular: MutableList<Food>? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val categories: MutableList<Category> = mutableListOf()
    private var categoryAdapter: CategoryChipAdapter? = null
    private var selectedCategoryId: Long = 0L
    private var categoryListener: ValueEventListener? = null
    private var selectedPriceBucket: Int = PRICE_ALL

    companion object {
        private const val PRICE_ALL = 0
        private const val PRICE_LOW = 1   // < 50 (k)
        private const val PRICE_MID = 2   // 50..100
        private const val PRICE_HIGH = 3  // > 100
    }
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
        setupCategoryChips()
        setupPriceChips()
        loadCategoriesFromFirebase()
        homeViewModel.loadFoods("")
        initListener()
        return mFragmentHomeBinding!!.root
    }

    private fun setupPriceChips() {
        val binding = mFragmentHomeBinding ?: return
        val all = binding.chipPriceAll
        val low = binding.chipPriceLow
        val mid = binding.chipPriceMid
        val high = binding.chipPriceHigh
        val chips = listOf(all to PRICE_ALL, low to PRICE_LOW, mid to PRICE_MID, high to PRICE_HIGH)
        chips.forEach { (chip, bucket) ->
            chip.setOnClickListener {
                if (selectedPriceBucket == bucket) return@setOnClickListener
                selectedPriceBucket = bucket
                renderPriceChips()
                applyFilters()
            }
        }
        renderPriceChips()
    }

    private fun renderPriceChips() {
        val binding = mFragmentHomeBinding ?: return
        val ctx = activity ?: return
        val chips = mapOf(
                PRICE_ALL to binding.chipPriceAll,
                PRICE_LOW to binding.chipPriceLow,
                PRICE_MID to binding.chipPriceMid,
                PRICE_HIGH to binding.chipPriceHigh
        )
        chips.forEach { (bucket, chip) ->
            val isSelected = bucket == selectedPriceBucket
            chip.setBackgroundResource(if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
            chip.setTextColor(androidx.core.content.ContextCompat.getColor(ctx,
                    if (isSelected) R.color.white else R.color.textColorPrimary))
        }
    }

    private fun setupCategoryChips() {
        val binding = mFragmentHomeBinding ?: return
        categoryAdapter = CategoryChipAdapter(categories) { selected ->
            selectedCategoryId = selected.id
            applyFilters()
        }
        binding.rcvCategories.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvCategories.adapter = categoryAdapter
    }

    private fun loadCategoriesFromFirebase() {
        val ctx = activity ?: return
        val ref = ControllerApplication[ctx].categoryDatabaseReference
        categoryListener?.let { ref.removeEventListener(it) }
        categoryListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                val all = Category(0L, getString(R.string.category_all), true)
                categories.add(all)
                for (child in snapshot.children) {
                    val item = child.getValue(Category::class.java) ?: continue
                    if (item.isActive) categories.add(item)
                }
                categoryAdapter?.setSelected(selectedCategoryId)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(categoryListener!!)
    }

    private fun applyFilters() {
        var filtered = if (selectedCategoryId == 0L) mAllFoods
        else mAllFoods.filter { it.categoryId == selectedCategoryId }
        filtered = when (selectedPriceBucket) {
            PRICE_LOW -> filtered.filter { it.realPrice in 0..49 }
            PRICE_MID -> filtered.filter { it.realPrice in 50..100 }
            PRICE_HIGH -> filtered.filter { it.realPrice > 100 }
            else -> filtered
        }
        mListFood = filtered.toMutableList()
        displayListFoodSuggest()
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
        categoryListener?.let { listener ->
            val ctx = activity
            if (ctx != null) {
                ControllerApplication[ctx].categoryDatabaseReference.removeEventListener(listener)
            }
        }
        categoryListener = null
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
                    mAllFoods = state.data
                    applyFilters()
                    displayListFoodPopular()
                }
            }
        }
    }
}