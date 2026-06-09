package com.utt.foodorderapp.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.MoreImageAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.data.repository.ReviewRepository
import com.utt.foodorderapp.database.FoodDatabase.Companion.getInstance
import com.utt.foodorderapp.databinding.ActivityFoodDetailBinding
import com.utt.foodorderapp.event.ReloadListCartEvent
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.prefs.DataStoreManager
import com.utt.foodorderapp.utils.GlideUtils.loadUrl
import com.utt.foodorderapp.utils.GlideUtils.loadUrlBanner
import org.greenrobot.eventbus.EventBus
import com.utt.foodorderapp.utils.MoneyUtils

class FoodDetailActivity : BaseActivity() {

    private var mActivityFoodDetailBinding: ActivityFoodDetailBinding? = null
    private var mFood: Food? = null
    private val reviewRepository = ReviewRepository()

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
            mFood = BundleCompat.getSerializable(bundle, AppConfig.KEY_INTENT_FOOD_OBJECT, Food::class.java)
        }
    }

    private fun initToolbar() {
        mActivityFoodDetailBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityFoodDetailBinding!!.toolbar.imgCart.visibility = View.VISIBLE
        mActivityFoodDetailBinding!!.toolbar.tvTitle.text = getString(R.string.food_detail_title)
        mActivityFoodDetailBinding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setDataFoodDetail() {
        if (mFood == null) {
            return
        }
        loadUrlBanner(mFood!!.banner, mActivityFoodDetailBinding!!.imageFood)
        if (mFood!!.sale <= 0) {
            mActivityFoodDetailBinding!!.tvSaleOff.visibility = View.GONE
            mActivityFoodDetailBinding!!.tvPrice.visibility = View.GONE
            val strPrice: String = MoneyUtils.format(mFood!!.price)
            mActivityFoodDetailBinding!!.tvPriceSale.text = strPrice
        } else {
            mActivityFoodDetailBinding!!.tvSaleOff.visibility = View.VISIBLE
            mActivityFoodDetailBinding!!.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + mFood!!.sale + "%"
            mActivityFoodDetailBinding!!.tvSaleOff.text = strSale
            val strPriceOld: String = MoneyUtils.format(mFood!!.price)
            mActivityFoodDetailBinding!!.tvPrice.text = strPriceOld
            mActivityFoodDetailBinding!!.tvPrice.paintFlags = mActivityFoodDetailBinding!!.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = MoneyUtils.format(mFood!!.realPrice)
            mActivityFoodDetailBinding!!.tvPriceSale.text = strRealPrice
        }
        mActivityFoodDetailBinding!!.tvFoodName.text = mFood!!.name
        mActivityFoodDetailBinding!!.tvFoodDescription.text = mFood!!.description

        // Rating & sold derived from id so the detail looks realistic & distinct.
        val rating = 4.5 + (mFood!!.id % 5) / 10.0
        mActivityFoodDetailBinding!!.tvFoodRatingDetail.text = String.format("%.1f", rating)
        val sold = 80 + (mFood!!.id * 37 % 1900).toInt()
        val soldText = if (sold >= 1000) "Đã bán " + String.format("%.1f", sold / 1000.0) + "k"
        else "Đã bán $sold"
        mActivityFoodDetailBinding!!.tvFoodSoldDetail.text = soldText

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
            mActivityFoodDetailBinding!!.tvAddToCart.setBackgroundResource(R.drawable.bg_primary_button)
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
        mActivityFoodDetailBinding!!.tvRateFood.setOnClickListener { showRatingDialog(forFood = true) }
        mActivityFoodDetailBinding!!.tvRateRestaurant.setOnClickListener { showRatingDialog(forFood = false) }
        mActivityFoodDetailBinding!!.tvViewFoodReviews.setOnClickListener {
            val food = mFood ?: return@setOnClickListener
            val i = Intent(this, ReviewsListActivity::class.java)
            i.putExtra(ReviewsListActivity.EXTRA_FOOD_ID, food.id)
            startActivity(i)
        }
        mActivityFoodDetailBinding!!.tvViewRestaurantReviews.setOnClickListener {
            val food = mFood ?: return@setOnClickListener
            if (food.restaurantId == 0L) {
                showToastMessage(this, getString(R.string.msg_no_restaurant_linked))
                return@setOnClickListener
            }
            val i = Intent(this, ReviewsListActivity::class.java)
            i.putExtra(ReviewsListActivity.EXTRA_RESTAURANT_ID, food.restaurantId)
            startActivity(i)
        }
    }

    private fun showRatingDialog(forFood: Boolean) {
        val food = mFood ?: return
        if (!forFood && food.restaurantId == 0L) {
            showToastMessage(this, getString(R.string.msg_no_restaurant_linked))
            return
        }
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rate_order, null, false)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val edtComment = view.findViewById<EditText>(R.id.edt_review_comment)
        val titleRes = if (forFood) R.string.action_rate_food else R.string.action_rate_restaurant

        AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setView(view)
                .setPositiveButton(R.string.action_submit_rating) { _, _ ->
                    val rating = ratingBar.rating.toInt().coerceIn(1, 5)
                    val comment = edtComment.text.toString().trim()
                    val email = DataStoreManager.user?.email
                    val cb: (com.google.firebase.database.DatabaseError?) -> Unit = { error ->
                        if (error == null) {
                            showToastMessage(this, getString(R.string.msg_rating_success))
                        } else {
                            showToastMessage(this, getString(R.string.msg_rating_failed))
                        }
                    }
                    if (forFood) {
                        reviewRepository.submitFoodReview(food.id, email, rating, comment, cb)
                    } else {
                        reviewRepository.submitRestaurantReview(food.restaurantId, email, rating, comment, cb)
                    }
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
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
        val strTotalPrice: String = MoneyUtils.format(totalPrice)
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
            val strTotalPrice1: String = MoneyUtils.format(totalPrice1)
            tvFoodPriceCart.text = strTotalPrice1
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice1
        }
        tvAddCount.setOnClickListener {
            val newCount = tvCount.text.toString().toInt() + 1
            tvCount.text = newCount.toString()
            val totalPrice2 = mFood!!.realPrice * newCount
            val strTotalPrice2: String = MoneyUtils.format(totalPrice2)
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