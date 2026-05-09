package com.pro.foodorder.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.foodorder.ControllerApplication
import com.pro.foodorder.R
import com.pro.foodorder.constant.Constant
import com.pro.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.pro.foodorder.databinding.ActivityAddFoodBinding
import com.pro.foodorder.model.Food
import com.pro.foodorder.model.FoodObject
import com.pro.foodorder.model.Image
import com.pro.foodorder.utils.StringUtil.isEmpty
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
            mFood = bundleReceived[Constant.KEY_INTENT_FOOD_OBJECT] as Food?
        }
    }

    private fun initToolbar() {
        mActivityAddFoodBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAddFoodBinding!!.toolbar.imgCart.visibility = View.GONE
        mActivityAddFoodBinding!!.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            mActivityAddFoodBinding!!.toolbar.tvTitle.text = getString(R.string.edit_food)
            mActivityAddFoodBinding!!.btnAddOrEdit.text = getString(R.string.action_edit)
            mActivityAddFoodBinding!!.edtName.setText(mFood!!.name)
            mActivityAddFoodBinding!!.edtDescription.setText(mFood!!.description)
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
        val strPrice = mActivityAddFoodBinding!!.edtPrice.text.toString().trim { it <= ' ' }
        val strDiscount = mActivityAddFoodBinding!!.edtDiscount.text.toString().trim { it <= ' ' }
        val strImage = mActivityAddFoodBinding!!.edtImage.text.toString().trim { it <= ' ' }
        val strImageBanner = mActivityAddFoodBinding!!.edtImageBanner.text.toString().trim { it <= ' ' }
        val isPopular = mActivityAddFoodBinding!!.chbPopular.isChecked
        val strOtherImages = mActivityAddFoodBinding!!.edtOtherImage.text.toString().trim { it <= ' ' }
        val listImages: MutableList<Image> = ArrayList()
        if (!isEmpty(strOtherImages)) {
            val temp = strOtherImages.split(";".toRegex()).toTypedArray()
            for (strUrl in temp) {
                val image = Image(strUrl)
                listImages.add(image)
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
        if (isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDiscount)) {
            Toast.makeText(this, getString(R.string.msg_discount_food_require), Toast.LENGTH_SHORT).show()
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
            map["price"] = strPrice.toInt()
            map["sale"] = strDiscount.toInt()
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
        val food = FoodObject(foodId, strName, strDescription, strPrice.toInt(), strDiscount.toInt(), strImage, strImageBanner, isPopular)
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