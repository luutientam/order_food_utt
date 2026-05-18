package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.BundleCompat
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.databinding.ActivityAddFoodBinding
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.model.FoodObject
import com.utt.foodorderapp.model.Image
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import java.util.*
import kotlin.collections.set

class AddFoodActivity : BaseActivity() {

    private var mActivityAddFoodBinding: ActivityAddFoodBinding? = null
    private var isUpdate = false
    private var mFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAddFoodBinding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(mActivityAddFoodBinding!!.root)
        getDataIntent()
        initToolbar()
        initView()
        mActivityAddFoodBinding!!.btnAddOrEdit.setOnClickListener { addOrEditFood() }
    }

    private fun getDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mFood = BundleCompat.getSerializable(bundleReceived, AppConfig.KEY_INTENT_FOOD_OBJECT, Food::class.java)
        }
    }

    private fun initToolbar() {
        mActivityAddFoodBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAddFoodBinding!!.toolbar.imgCart.visibility = View.GONE
        mActivityAddFoodBinding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            mActivityAddFoodBinding!!.toolbar.tvTitle.text = getString(R.string.edit_food)
            mActivityAddFoodBinding!!.btnAddOrEdit.text = getString(R.string.action_edit)
            mActivityAddFoodBinding!!.edtName.setText(mFood!!.name)
            mActivityAddFoodBinding!!.edtDescription.setText(mFood!!.description)
            mActivityAddFoodBinding!!.edtRestaurantName.setText(mFood!!.restaurantName)
            mActivityAddFoodBinding!!.edtCategoryName.setText(mFood!!.categoryName)
            mActivityAddFoodBinding!!.edtPrice.setText(java.lang.String.valueOf(mFood!!.price))
            mActivityAddFoodBinding!!.edtDiscount.setText(java.lang.String.valueOf(mFood!!.sale))
            mActivityAddFoodBinding!!.edtImage.setText(mFood!!.image)
            mActivityAddFoodBinding!!.edtImageBanner.setText(mFood!!.banner)
            mActivityAddFoodBinding!!.chbPopular.isChecked = mFood!!.isPopular
            mActivityAddFoodBinding!!.edtOtherImage.setText(getTextOtherImages())
        } else {
            mActivityAddFoodBinding!!.toolbar.tvTitle.text = getString(R.string.add_food)
            mActivityAddFoodBinding!!.btnAddOrEdit.text = getString(R.string.action_add)
        }
    }

    private fun getTextOtherImages(): String {
        var result = ""
        if (mFood == null || mFood!!.images == null || mFood!!.images!!.isEmpty()) {
            return result
        }
        for (image in mFood!!.images!!) {
            result = if (isEmpty(result)) {
                result + image.url
            } else {
                result + ";" + image.url
            }
        }
        return result
    }

    private fun addOrEditFood() {
        val strName = mActivityAddFoodBinding!!.edtName.text.toString().trim { it <= ' ' }
        val strDescription = mActivityAddFoodBinding!!.edtDescription.text.toString().trim { it <= ' ' }
        val strRestaurantName = mActivityAddFoodBinding!!.edtRestaurantName.text.toString().trim { it <= ' ' }
        val strCategoryName = mActivityAddFoodBinding!!.edtCategoryName.text.toString().trim { it <= ' ' }
        val strPrice = mActivityAddFoodBinding!!.edtPrice.text.toString().trim { it <= ' ' }
        val strDiscount = mActivityAddFoodBinding!!.edtDiscount.text.toString().trim { it <= ' ' }
        val strImage = mActivityAddFoodBinding!!.edtImage.text.toString().trim { it <= ' ' }
        val strImageBanner = mActivityAddFoodBinding!!.edtImageBanner.text.toString().trim { it <= ' ' }
        val isPopular = mActivityAddFoodBinding!!.chbPopular.isChecked
        val strOtherImages = mActivityAddFoodBinding!!.edtOtherImage.text.toString().trim { it <= ' ' }
        val priceValue = strPrice.toIntOrNull()
        val discountValue = strDiscount.toIntOrNull()
        val listImages: MutableList<Image> = ArrayList()
        if (!isEmpty(strOtherImages)) {
            val temp = strOtherImages.split(";".toRegex()).toTypedArray()
            for (strUrl in temp) {
                val url = strUrl.trim()
                if (!isEmpty(url)) {
                    val image = Image(url)
                    listImages.add(image)
                }
            }
        }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDescription)) {
            Toast.makeText(this, getString(R.string.msg_description_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strRestaurantName)) {
            Toast.makeText(this, getString(R.string.hint_restaurant_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strCategoryName)) {
            Toast.makeText(this, getString(R.string.hint_category_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (priceValue == null || priceValue <= 0) {
            Toast.makeText(this, getString(R.string.msg_price_food_invalid), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDiscount)) {
            Toast.makeText(this, getString(R.string.msg_discount_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (discountValue == null || discountValue < 0 || discountValue > 100) {
            Toast.makeText(this, getString(R.string.msg_discount_food_invalid), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImageBanner)) {
            Toast.makeText(this, getString(R.string.msg_image_banner_food_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update food
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["name"] = strName
            map["description"] = strDescription
            map["restaurantName"] = strRestaurantName
            map["categoryName"] = strCategoryName
            map["restaurantId"] = strRestaurantName.hashCode().toLong()
            map["categoryId"] = strCategoryName.hashCode().toLong()
            map["price"] = priceValue!!
            map["sale"] = discountValue!!
            map["image"] = strImage
            map["banner"] = strImageBanner
            map["popular"] = isPopular
            if (listImages.isNotEmpty()) {
                map["images"] = listImages
            }
            ControllerApplication[this].foodDatabaseReference
                    .child(mFood!!.id.toString()).updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                        showProgressDialog(false)
                        Toast.makeText(this@AddFoodActivity,
                                getString(R.string.msg_edit_food_success), Toast.LENGTH_SHORT).show()
                        hideSoftKeyboard(this)
                    }
            return
        }

        // Add food
        showProgressDialog(true)
        val foodId = System.currentTimeMillis()
        val food = FoodObject(
                foodId,
                strName,
                strDescription,
                strRestaurantName.hashCode().toLong(),
                strRestaurantName,
                strCategoryName.hashCode().toLong(),
                strCategoryName,
                priceValue!!,
                discountValue!!,
                strImage,
                strImageBanner,
                isPopular
        )
        if (listImages.isNotEmpty()) {
            food.images = listImages
        }
        ControllerApplication[this].foodDatabaseReference
                .child(foodId.toString()).setValue(food) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    mActivityAddFoodBinding!!.edtName.setText("")
                    mActivityAddFoodBinding!!.edtDescription.setText("")
                    mActivityAddFoodBinding!!.edtPrice.setText("")
                    mActivityAddFoodBinding!!.edtDiscount.setText("")
                    mActivityAddFoodBinding!!.edtImage.setText("")
                    mActivityAddFoodBinding!!.edtImageBanner.setText("")
                    mActivityAddFoodBinding!!.chbPopular.isChecked = false
                    mActivityAddFoodBinding!!.edtOtherImage.setText("")
                    hideSoftKeyboard(this)
                    Toast.makeText(this, getString(R.string.msg_add_food_success), Toast.LENGTH_SHORT).show()
                }
    }
}