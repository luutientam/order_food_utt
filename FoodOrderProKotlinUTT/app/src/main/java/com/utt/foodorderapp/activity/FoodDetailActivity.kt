package com.utt.foodorderapp.activity

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.MoreImageAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.database.FoodDatabase.Companion.getInstance
import com.utt.foodorderapp.databinding.ActivityFoodDetailBinding
import com.utt.foodorderapp.event.ReloadListCartEvent
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.utils.GlideUtils.loadUrl
import com.utt.foodorderapp.utils.GlideUtils.loadUrlBanner
import org.greenrobot.eventbus.EventBus

class FoodDetailActivity : BaseActivity() {

    private var mActivityFoodDetailBinding: ActivityFoodDetailBinding? = null
    private var mFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityFoodDetailBinding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(mActivityFoodDetailBinding!!.root)
        getDataIntent()
        initToolbar()
        setDataFoodDetail()
        initListener()
    }

    private fun getDataIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            mFood = bundle[AppConfig.KEY_INTENT_FOOD_OBJECT] as Food?
        }
    }

    private fun initToolbar() {
        mActivityFoodDetailBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityFoodDetailBinding!!.toolbar.imgCart.visibility = View.VISIBLE
        mActivityFoodDetailBinding!!.toolbar.tvTitle.text = getString(R.string.food_detail_title)
        mActivityFoodDetailBinding!!.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun setDataFoodDetail() {
        if (mFood == null) {
            return
        }
        loadUrlBanner(mFood!!.banner, mActivityFoodDetailBinding!!.imageFood)
        if (mFood!!.sale <= 0) {
            mActivityFoodDetailBinding!!.tvSaleOff.visibility = View.GONE
            mActivityFoodDetailBinding!!.tvPrice.visibility = View.GONE
            val strPrice: String = "" + mFood!!.price + AppConfig.CURRENCY
            mActivityFoodDetailBinding!!.tvPriceSale.text = strPrice
        } else {
            mActivityFoodDetailBinding!!.tvSaleOff.visibility = View.VISIBLE
            mActivityFoodDetailBinding!!.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + mFood!!.sale + "%"
            mActivityFoodDetailBinding!!.tvSaleOff.text = strSale
            val strPriceOld: String = "" + mFood!!.price + AppConfig.CURRENCY
            mActivityFoodDetailBinding!!.tvPrice.text = strPriceOld
            mActivityFoodDetailBinding!!.tvPrice.paintFlags = mActivityFoodDetailBinding!!.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = "" + mFood!!.realPrice + AppConfig.CURRENCY
            mActivityFoodDetailBinding!!.tvPriceSale.text = strRealPrice
        }
        mActivityFoodDetailBinding!!.tvFoodName.text = mFood!!.name
        mActivityFoodDetailBinding!!.tvFoodDescription.text = mFood!!.description
        displayListMoreImages()
        setStatusButtonAddToCart()
    }

    private fun displayListMoreImages() {
        if (mFood!!.images == null || mFood!!.images!!.isEmpty()) {
            mActivityFoodDetailBinding!!.tvMoreImageLabel.visibility = View.GONE
            return
        }
        mActivityFoodDetailBinding!!.tvMoreImageLabel.visibility = View.VISIBLE
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivityFoodDetailBinding!!.rcvImages.layoutManager = gridLayoutManager
        val moreImageAdapter = MoreImageAdapter(mFood!!.images)
        mActivityFoodDetailBinding!!.rcvImages.adapter = moreImageAdapter
    }

    private fun setStatusButtonAddToCart() {
        if (isFoodInCart()) {
            mActivityFoodDetailBinding!!.tvAddToCart.setBackgroundResource(R.drawable.bg_gray_shape_corner_6)
            mActivityFoodDetailBinding!!.tvAddToCart.text = getString(R.string.added_to_cart)
            mActivityFoodDetailBinding!!.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary))
            mActivityFoodDetailBinding!!.toolbar.imgCart.visibility = View.GONE
        } else {
            mActivityFoodDetailBinding!!.tvAddToCart.setBackgroundResource(R.drawable.bg_green_shape_corner_6)
            mActivityFoodDetailBinding!!.tvAddToCart.text = getString(R.string.add_to_cart)
            mActivityFoodDetailBinding!!.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.white))
            mActivityFoodDetailBinding!!.toolbar.imgCart.visibility = View.VISIBLE
        }
    }

    private fun isFoodInCart(): Boolean {
        val list = getInstance(this)!!.foodDAO()!!.checkFoodInCart(mFood!!.id)
        return list != null && list.isNotEmpty()
    }

    private fun initListener() {
        mActivityFoodDetailBinding!!.tvAddToCart.setOnClickListener { onClickAddToCart() }
        mActivityFoodDetailBinding!!.toolbar.imgCart.setOnClickListener { onClickAddToCart() }
    }

    private fun onClickAddToCart() {
        if (isFoodInCart()) {
            return
        }
        @SuppressLint("InflateParams") val viewDialog = layoutInflater.inflate(R.layout.layout_bottom_sheet_cart, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(viewDialog)
        val imgFoodCart = viewDialog.findViewById<ImageView>(R.id.img_food_cart)
        val tvFoodNameCart = viewDialog.findViewById<TextView>(R.id.tv_food_name_cart)
        val tvFoodPriceCart = viewDialog.findViewById<TextView>(R.id.tv_food_price_cart)
        val tvSubtractCount = viewDialog.findViewById<TextView>(R.id.tv_subtract)
        val tvCount = viewDialog.findViewById<TextView>(R.id.tv_count)
        val tvAddCount = viewDialog.findViewById<TextView>(R.id.tv_add)
        val tvCancel = viewDialog.findViewById<TextView>(R.id.tv_cancel)
        val tvAddCart = viewDialog.findViewById<TextView>(R.id.tv_add_cart)
        loadUrl(mFood!!.image, imgFoodCart)
        tvFoodNameCart.text = mFood!!.name
        val totalPrice = mFood!!.realPrice
        val strTotalPrice: String = "" + totalPrice + AppConfig.CURRENCY
        tvFoodPriceCart.text = strTotalPrice
        mFood!!.count = 1
        mFood!!.totalPrice = totalPrice
        tvSubtractCount.setOnClickListener {
            val count = tvCount.text.toString().toInt()
            if (count <= 1) {
                return@setOnClickListener
            }
            val newCount = tvCount.text.toString().toInt() - 1
            tvCount.text = newCount.toString()
            val totalPrice1 = mFood!!.realPrice * newCount
            val strTotalPrice1: String = "" + totalPrice1 + AppConfig.CURRENCY
            tvFoodPriceCart.text = strTotalPrice1
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice1
        }
        tvAddCount.setOnClickListener {
            val newCount = tvCount.text.toString().toInt() + 1
            tvCount.text = newCount.toString()
            val totalPrice2 = mFood!!.realPrice * newCount
            val strTotalPrice2: String = "" + totalPrice2 + AppConfig.CURRENCY
            tvFoodPriceCart.text = strTotalPrice2
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice2
        }
        tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        tvAddCart.setOnClickListener {
            val selectedFood = mFood ?: return@setOnClickListener
            getInstance(this@FoodDetailActivity)!!.foodDAO()!!.insertFood(selectedFood)
            bottomSheetDialog.dismiss()
            setStatusButtonAddToCart()
            EventBus.getDefault().post(ReloadListCartEvent())
        }
        bottomSheetDialog.show()
    }
}