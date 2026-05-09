package com.utt.foodorderapp.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.BaseActivity
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.adapter.CartAdapter
import com.utt.foodorderapp.adapter.CartAdapter.IClickListener
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.FragmentCartBinding
import com.utt.foodorderapp.data.remote.FakeBankApiService
import com.utt.foodorderapp.event.ReloadListCartEvent
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.model.Promotion
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user
import com.utt.foodorderapp.presentation.cart.CartViewModel
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class CartFragment : BaseFragment() {

    private var mFragmentCartBinding: FragmentCartBinding? = null
    private var mCartAdapter: CartAdapter? = null
    private var mListFoodCart: MutableList<Food>? = null
    private var mAmount = 0
    private var selectedDiscount = 0
    private var appliedPromotionCode: String? = null
    private var availablePromotions: MutableList<Promotion> = ArrayList()
    private val fakeBankApiService = FakeBankApiService()
    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentCartBinding = FragmentCartBinding.inflate(inflater, container, false)
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        observeViewModel()
        displayListFoodInCart()
        mFragmentCartBinding!!.tvOrderCart.setOnClickListener { onClickOrderCart() }
        return mFragmentCartBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.cart))
        }
    }

    private fun displayListFoodInCart() {
        if (activity == null || mFragmentCartBinding == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentCartBinding!!.rcvFoodCart.layoutManager = linearLayoutManager
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        mFragmentCartBinding!!.rcvFoodCart.addItemDecoration(itemDecoration)
        initDataFoodCart()
    }

    private fun initDataFoodCart() {
        mListFoodCart = ArrayList()
        mListFoodCart = (cartViewModel.cartState.value as? UiState.Success<List<Food>>)?.data?.toMutableList() ?: ArrayList()
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return
        }
        mCartAdapter = CartAdapter(mListFoodCart, object : IClickListener {
            override fun clickDeteteFood(food: Food?, position: Int) {
                deleteFoodFromCart(food, position)
            }

            override fun updateItemFood(food: Food?, position: Int) {
                val selectedFood = food ?: return
                cartViewModel.updateCartItem(selectedFood)
                mCartAdapter!!.notifyItemChanged(position)
                calculateTotalPrice()
            }
        })
        mFragmentCartBinding!!.rcvFoodCart.adapter = mCartAdapter
        calculateTotalPrice()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearCart() {
        mListFoodCart?.clear()
        mCartAdapter?.notifyDataSetChanged()
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val listFoodCart = (cartViewModel.cartState.value as? UiState.Success<List<Food>>)?.data
        if (listFoodCart == null || listFoodCart.isEmpty()) {
            val strZero: String = "" + 0 + AppConfig.CURRENCY
            mFragmentCartBinding!!.tvTotalPrice.text = strZero
            mAmount = 0
            return
        }
        var totalPrice = 0
        for (food in listFoodCart) {
            totalPrice += food.totalPrice
        }
        val strTotalPrice: String = "" + totalPrice + AppConfig.CURRENCY
        mFragmentCartBinding!!.tvTotalPrice.text = strTotalPrice
        mAmount = totalPrice
    }

    private fun deleteFoodFromCart(food: Food?, position: Int) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.confirm_delete_food))
                .setMessage(getString(R.string.message_delete_food))
                .setPositiveButton(getString(R.string.delete)) { _: DialogInterface?, _: Int ->
                    val selectedFood = food ?: return@setPositiveButton
                    cartViewModel.removeCartItem(selectedFood)
                    mListFoodCart?.removeAt(position)
                    mCartAdapter!!.notifyItemRemoved(position)
                    calculateTotalPrice()
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .show()
    }

    private fun onClickOrderCart() {
        if (activity == null) {
            return
        }
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return
        }
        @SuppressLint("InflateParams") val viewDialog: View = layoutInflater.inflate(R.layout.layout_bottom_sheet_order, null, false)
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        bottomSheetDialog.setContentView(viewDialog)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // init ui
        val tvFoodsOrder = viewDialog.findViewById<TextView>(R.id.tv_foods_order)
        val tvPriceOrder = viewDialog.findViewById<TextView>(R.id.tv_price_order)
        val edtNameOrder = viewDialog.findViewById<EditText>(R.id.edt_name_order)
        val edtPhoneOrder = viewDialog.findViewById<EditText>(R.id.edt_phone_order)
        val edtAddressOrder = viewDialog.findViewById<EditText>(R.id.edt_address_order)
        val edtPromotionCode = viewDialog.findViewById<EditText>(R.id.edt_promotion_code)
        val tvApplyPromotion = viewDialog.findViewById<TextView>(R.id.tv_apply_promotion)
        val tvDiscountValue = viewDialog.findViewById<TextView>(R.id.tv_discount_value)
        val tvBestPromotion = viewDialog.findViewById<TextView>(R.id.tv_best_promotion)
        val tvSelectPromotion = viewDialog.findViewById<TextView>(R.id.tv_select_promotion)
        val rdbPaymentOnline = viewDialog.findViewById<RadioButton>(R.id.rdb_payment_online)
        val tvCancelOrder = viewDialog.findViewById<TextView>(R.id.tv_cancel_order)
        val tvCreateOrder = viewDialog.findViewById<TextView>(R.id.tv_create_order)
        selectedDiscount = 0
        appliedPromotionCode = null
        availablePromotions.clear()
        tvDiscountValue.text = getString(R.string.promotion_not_applied)

        // Set data
        tvFoodsOrder.text = getStringListFoodsOrder()
        tvPriceOrder.text = mFragmentCartBinding!!.tvTotalPrice.text.toString()

        // Set listener
        loadAvailablePromotions(edtPromotionCode, tvBestPromotion, tvDiscountValue, tvPriceOrder)
        tvSelectPromotion.setOnClickListener {
            showPromotionPicker(edtPromotionCode, tvDiscountValue, tvPriceOrder)
        }
        tvApplyPromotion.setOnClickListener {
            val code = edtPromotionCode.text.toString().trim().uppercase(Locale.getDefault())
            if (isEmpty(code)) {
                showToastMessage(activity, getString(R.string.msg_promotion_code_required))
                return@setOnClickListener
            }
            applyPromotionCode(code, tvDiscountValue, tvPriceOrder)
        }
        tvCancelOrder.setOnClickListener { bottomSheetDialog.dismiss() }
        tvCreateOrder.setOnClickListener {
            val strName = edtNameOrder.text.toString().trim { it <= ' ' }
            val strPhone = edtPhoneOrder.text.toString().trim { it <= ' ' }
            val strAddress = edtAddressOrder.text.toString().trim { it <= ' ' }
            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(activity, getString(R.string.message_enter_infor_order))
            } else {
                val paymentType = if (rdbPaymentOnline.isChecked) AppConfig.TYPE_PAYMENT_ONLINE else AppConfig.TYPE_PAYMENT_CASH
                if (paymentType == AppConfig.TYPE_PAYMENT_ONLINE) {
                    val expectedAmount = (mAmount - selectedDiscount).coerceAtLeast(0)
                    confirmOnlinePayment(expectedAmount) { transactionId ->
                        submitOrderWithPromotionValidation(
                                strName,
                                strPhone,
                                strAddress,
                                bottomSheetDialog,
                                tvDiscountValue,
                                tvPriceOrder,
                                paymentType,
                                Order.PAYMENT_STATUS_PAID,
                                transactionId
                        )
                    }
                } else {
                    submitOrderWithPromotionValidation(
                            strName,
                            strPhone,
                            strAddress,
                            bottomSheetDialog,
                            tvDiscountValue,
                            tvPriceOrder,
                            paymentType,
                            Order.PAYMENT_STATUS_UNPAID,
                            null
                    )
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun submitOrderWithPromotionValidation(
            name: String,
            phone: String,
            address: String,
            bottomSheetDialog: BottomSheetDialog,
            tvDiscountValue: TextView,
            tvPriceOrder: TextView,
            paymentType: Int,
            paymentStatus: Int,
            paymentTransactionId: String?
    ) {
        val currentUser = user ?: return
        val code = appliedPromotionCode
        if (code.isNullOrEmpty()) {
            submitOrder(name, phone, address, currentUser.email, currentUser.uid, 0, null,
                    bottomSheetDialog, paymentType, paymentStatus, paymentTransactionId)
            return
        }
        ControllerApplication[requireActivity()].promotionDatabaseReference.child(code).get().addOnSuccessListener { snapshot ->
            val promotion = snapshot.getValue(Promotion::class.java)
            if (promotion == null || !promotion.isActive || mAmount < promotion.minOrderAmount) {
                selectedDiscount = 0
                appliedPromotionCode = null
                tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
                tvPriceOrder.text = "${mAmount}${AppConfig.CURRENCY}"
                showToastMessage(activity, getString(R.string.msg_promotion_invalid))
                submitOrder(name, phone, address, currentUser.email, currentUser.uid, 0, null,
                        bottomSheetDialog, paymentType, paymentStatus, paymentTransactionId)
                return@addOnSuccessListener
            }
            val discount = calculateDiscountAmount(promotion)
            selectedDiscount = discount
            submitOrder(name, phone, address, currentUser.email, currentUser.uid, discount, code,
                    bottomSheetDialog, paymentType, paymentStatus, paymentTransactionId)
        }.addOnFailureListener {
            selectedDiscount = 0
            appliedPromotionCode = null
            tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
            tvPriceOrder.text = "${mAmount}${AppConfig.CURRENCY}"
            submitOrder(name, phone, address, currentUser.email, currentUser.uid, 0, null,
                    bottomSheetDialog, paymentType, paymentStatus, paymentTransactionId)
        }
    }

    private fun submitOrder(
            name: String,
            phone: String,
            address: String,
            email: String?,
            customerId: String?,
            discount: Int,
            promotionCode: String?,
            bottomSheetDialog: BottomSheetDialog,
            paymentType: Int,
            paymentStatus: Int,
            paymentTransactionId: String?
    ) {
        val id = System.currentTimeMillis()
        val finalAmount = (mAmount - discount).coerceAtLeast(0)
        val order = Order(id, name, email, phone, address,
                finalAmount, getStringListFoodsOrder(), paymentType, false, Order.STATUS_NEW,
                0.0, 0.0, mAmount, discount, promotionCode, customerId,
                0.0, 0.0, paymentStatus, paymentTransactionId)
        cartViewModel.submitOrder(order)
        hideSoftKeyboard(requireActivity())
        bottomSheetDialog.dismiss()
    }

    private fun loadAvailablePromotions(
            edtPromotionCode: EditText,
            tvBestPromotion: TextView,
            tvDiscountValue: TextView,
            tvPriceOrder: TextView
    ) {
        ControllerApplication[requireActivity()].promotionDatabaseReference.get().addOnSuccessListener { snapshot ->
            val promotions = ArrayList<Promotion>()
            for (child in snapshot.children) {
                val promotion = child.getValue(Promotion::class.java) ?: continue
                if (!promotion.isActive || promotion.code.isNullOrEmpty()) continue
                if (mAmount < promotion.minOrderAmount) continue
                promotions.add(promotion)
            }
            availablePromotions = promotions
            val best = promotions.maxByOrNull { calculateDiscountAmount(it) }
            if (best != null) {
                tvBestPromotion.text = getString(
                        R.string.promotion_best_hint,
                        best.code,
                        best.discountPercent.toString(),
                        "${best.maxDiscountAmount}${AppConfig.CURRENCY}"
                )
                val bestCode = best.code
                if (!bestCode.isNullOrEmpty() && edtPromotionCode.text.toString().trim().isEmpty()) {
                    edtPromotionCode.setText(bestCode)
                    applyPromotionCode(bestCode, tvDiscountValue, tvPriceOrder)
                }
            } else {
                tvBestPromotion.text = getString(R.string.msg_no_promotion_available)
            }
        }.addOnFailureListener {
            tvBestPromotion.text = getString(R.string.msg_no_promotion_available)
        }
    }

    private fun showPromotionPicker(
            edtPromotionCode: EditText,
            tvDiscountValue: TextView,
            tvPriceOrder: TextView
    ) {
        if (availablePromotions.isEmpty()) {
            showToastMessage(activity, getString(R.string.msg_no_promotion_available))
            return
        }
        val sortedPromotions = availablePromotions.sortedByDescending { calculateDiscountAmount(it) }
        val labels = sortedPromotions.map {
            "${it.code} - ${it.title} (${it.discountPercent}%, max ${it.maxDiscountAmount}${AppConfig.CURRENCY})"
        }.toTypedArray()
        AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.action_select_voucher))
                .setItems(labels) { _, which ->
                    val promotion = sortedPromotions[which]
                    val code = promotion.code ?: return@setItems
                    edtPromotionCode.setText(code)
                    applyPromotionCode(code, tvDiscountValue, tvPriceOrder)
                }
                .show()
    }

    private fun calculateDiscountAmount(promotion: Promotion): Int {
        var discount = mAmount * promotion.discountPercent / 100
        if (promotion.maxDiscountAmount > 0 && discount > promotion.maxDiscountAmount) {
            discount = promotion.maxDiscountAmount
        }
        return discount
    }

    private fun confirmOnlinePayment(amount: Int, onPaid: (String) -> Unit) {
        AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.payment_method_online))
                .setMessage(getString(R.string.confirm_online_payment))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    executeFakeBankPayment(amount, onPaid)
                }
                .setNegativeButton(getString(R.string.action_cancel)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    showToastMessage(activity, getString(R.string.msg_online_payment_cancelled))
                }
                .show()
    }

    private fun executeFakeBankPayment(amount: Int, onPaid: (String) -> Unit) {
        (activity as? BaseActivity)?.showProgressDialog(true)
        val requestId = System.currentTimeMillis()
        fakeBankApiService.createPayment(requestId, amount) { result ->
            (activity as? BaseActivity)?.showProgressDialog(false)
            if (!result.isSuccess || result.transactionId.isNullOrEmpty()) {
                showToastMessage(activity, getString(R.string.msg_online_payment_failed, result.message))
                return@createPayment
            }
            showToastMessage(activity, getString(R.string.msg_online_payment_success))
            onPaid(result.transactionId)
        }
    }

    private fun applyPromotionCode(code: String, tvDiscountValue: TextView, tvPriceOrder: TextView) {
        ControllerApplication[requireActivity()].promotionDatabaseReference.child(code).get().addOnSuccessListener { snapshot ->
            val promotion = snapshot.getValue(Promotion::class.java)
            if (promotion == null || !promotion.isActive) {
                selectedDiscount = 0
                appliedPromotionCode = null
                tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
                tvPriceOrder.text = "${mAmount}${AppConfig.CURRENCY}"
                return@addOnSuccessListener
            }
            if (mAmount < promotion.minOrderAmount) {
                selectedDiscount = 0
                appliedPromotionCode = null
                tvDiscountValue.text = getString(R.string.msg_promotion_not_meet_condition)
                tvPriceOrder.text = "${mAmount}${AppConfig.CURRENCY}"
                return@addOnSuccessListener
            }
            val discount = calculateDiscountAmount(promotion)
            selectedDiscount = discount
            appliedPromotionCode = code
            val finalAmount = (mAmount - discount).coerceAtLeast(0)
            tvDiscountValue.text = "${getString(R.string.discount_amount)}: -$discount${AppConfig.CURRENCY}"
            tvPriceOrder.text = "$finalAmount${AppConfig.CURRENCY}"
            showToastMessage(activity, getString(R.string.msg_promotion_applied))
        }.addOnFailureListener {
            selectedDiscount = 0
            appliedPromotionCode = null
            tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
            tvPriceOrder.text = "${mAmount}${AppConfig.CURRENCY}"
        }
    }

    private fun getStringListFoodsOrder(): String {
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return ""
        }
        var result = ""
        for (food in mListFoodCart!!) {
            result = if (isEmpty(result)) {
                ("- " + food.name + " (" + food.realPrice + AppConfig.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count)
            } else {
                (result + "\n" + ("- " + food.name + " (" + food.realPrice + AppConfig.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count))

            }
        }
        return result
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(@Suppress("UNUSED_PARAMETER") event: ReloadListCartEvent?) {
        if (mFragmentCartBinding == null) {
            return
        }
        cartViewModel.loadCart()
    }

    override fun onResume() {
        super.onResume()
        if (::cartViewModel.isInitialized) {
            cartViewModel.loadCart()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        mCartAdapter = null
        mFragmentCartBinding = null
    }

    private fun observeViewModel() {
        cartViewModel.cartState.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                mListFoodCart = state.data.toMutableList()
                if (mCartAdapter == null) {
                    displayListFoodInCart()
                } else {
                    mCartAdapter!!.notifyDataSetChanged()
                    calculateTotalPrice()
                }
            }
        }
        cartViewModel.orderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Unit
                is UiState.Success -> {
                    clearCart()
                    showToastMessage(activity, getString(R.string.msg_order_success))
                }
                is UiState.Error -> {
                    showToastMessage(activity, state.message)
                }
            }
        }
        cartViewModel.loadCart()
    }
}