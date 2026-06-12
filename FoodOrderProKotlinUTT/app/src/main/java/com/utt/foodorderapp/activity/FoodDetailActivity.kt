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
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.event.ReloadListCartEvent
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.prefs.DataStoreManager
import com.utt.foodorderapp.utils.CartAnimationUtils
import com.utt.foodorderapp.utils.GlideUtils.loadUrl
import com.utt.foodorderapp.utils.GlideUtils.loadUrlBanner
import org.greenrobot.eventbus.EventBus
import com.utt.foodorderapp.utils.MoneyUtils

class FoodDetailActivity : BaseActivity() {

    private var mActivityFoodDetailBinding: ActivityFoodDetailBinding? = null
    private var mFood: Food? = null
    private val reviewRepository = ReviewRepository()

    // null = chưa kiểm tra; true/false = đã biết user có mua món này chưa (chỉ ai mua rồi mới được đánh giá).
    private var purchasedFoodCache: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityFoodDetailBinding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(mActivityFoodDetailBinding!!.root)
        getDataIntent()
        initToolbar()
        setDataFoodDetail()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        // Cart contents may have changed (e.g. user removed this item from the cart).
        if (mFood != null) {
            setStatusButtonAddToCart()
        }
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
            val strSale = getString(R.string.food_sale_off, mFood!!.sale)
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
        val soldValue = if (sold >= 1000) String.format("%.1f", sold / 1000.0) + "k" else sold.toString()
        mActivityFoodDetailBinding!!.tvFoodSoldDetail.text = getString(R.string.food_sold_count, soldValue)

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
        // The cart icon stays visible at all times so the user can always reach the cart.
        mActivityFoodDetailBinding!!.toolbar.imgCart.visibility = View.VISIBLE
        if (isFoodInCart()) {
            mActivityFoodDetailBinding!!.tvAddToCart.setBackgroundResource(R.drawable.bg_gray_shape_corner_6)
            mActivityFoodDetailBinding!!.tvAddToCart.text = getString(R.string.added_to_cart)
            mActivityFoodDetailBinding!!.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary))
        } else {
            mActivityFoodDetailBinding!!.tvAddToCart.setBackgroundResource(R.drawable.bg_primary_button)
            mActivityFoodDetailBinding!!.tvAddToCart.text = getString(R.string.add_to_cart)
            mActivityFoodDetailBinding!!.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val count = getInstance(this)?.foodDAO()?.listFoodCart?.size ?: 0
        val badge = mActivityFoodDetailBinding!!.toolbar.tvCartBadge
        if (count > 0) {
            badge.visibility = View.VISIBLE
            badge.text = if (count > 99) "99+" else count.toString()
        } else {
            badge.visibility = View.GONE
        }
    }

    private fun openCart() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_OPEN_CART, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    private fun isFoodInCart(): Boolean {
        val list = getInstance(this)!!.foodDAO()!!.checkFoodInCart(mFood!!.id)
        return list != null && list.isNotEmpty()
    }

    private fun initListener() {
        mActivityFoodDetailBinding!!.tvAddToCart.setOnClickListener { onClickAddToCart() }
        mActivityFoodDetailBinding!!.toolbar.imgCart.setOnClickListener { openCart() }
        mActivityFoodDetailBinding!!.tvRateFood.setOnClickListener { onClickRateFood() }
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

    /** Chỉ cho đánh giá món khi user đã mua món đó (có đơn chứa món, chưa bị huỷ). */
    private fun onClickRateFood() {
        val food = mFood ?: return
        val cached = purchasedFoodCache
        if (cached != null) {
            if (cached) showRatingDialog(forFood = true)
            else showToastMessage(this, getString(R.string.msg_review_require_purchase))
            return
        }
        val currentUser = DataStoreManager.user
        val email = currentUser?.email
        val uid = currentUser?.uid
        if (email.isNullOrEmpty() && uid.isNullOrEmpty()) {
            showToastMessage(this, getString(R.string.msg_review_require_purchase))
            return
        }
        showProgressDialog(true)
        ControllerApplication[this].bookingDatabaseReference.get()
                .addOnSuccessListener { snapshot ->
                    showProgressDialog(false)
                    val bought = snapshot.children.any { child ->
                        val order = child.getValue(Order::class.java) ?: return@any false
                        hasBoughtFood(order, food, email, uid)
                    }
                    purchasedFoodCache = bought
                    if (bought) showRatingDialog(forFood = true)
                    else showToastMessage(this, getString(R.string.msg_review_require_purchase))
                }
                .addOnFailureListener {
                    showProgressDialog(false)
                    showToastMessage(this, getString(R.string.msg_get_date_error))
                }
    }

    /** Đã mua = có đơn của user (khớp email/uid), chưa huỷ, và danh sách món trong đơn chứa món này. */
    private fun hasBoughtFood(order: Order, food: Food, email: String?, uid: String?): Boolean {
        val matchesUser = (!email.isNullOrEmpty() && email.equals(order.email, ignoreCase = true)) ||
                (!uid.isNullOrEmpty() && uid == order.customerId)
        if (!matchesUser) return false
        if (order.getStatusValue() == Order.STATUS_CANCEL) return false
        val foods = order.foods ?: return false
        return foods.contains("- ${food.name} (")
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
            // Already added — the CTA now acts as a shortcut to the cart.
            openCart()
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
            EventBus.getDefault().post(ReloadListCartEvent())
            // ShopeeFood-style: fly the food image into the cart icon, then refresh state.
            CartAnimationUtils.flyToCart(
                    this@FoodDetailActivity,
                    mActivityFoodDetailBinding!!.imageFood,
                    mActivityFoodDetailBinding!!.toolbar.imgCart,
                    mActivityFoodDetailBinding!!.imageFood.drawable
            ) {
                setStatusButtonAddToCart()
            }
        }
        bottomSheetDialog.show()
    }
}