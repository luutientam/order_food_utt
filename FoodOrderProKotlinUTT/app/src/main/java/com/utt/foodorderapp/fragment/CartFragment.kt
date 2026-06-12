package com.utt.foodorderapp.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.AddressBookActivity
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.adapter.CartAdapter
import com.utt.foodorderapp.adapter.CartAdapter.IClickListener
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.FragmentCartBinding
import com.utt.foodorderapp.data.remote.SePayApiService
import com.utt.foodorderapp.data.repository.AddressRepository
import com.utt.foodorderapp.event.ReloadListCartEvent
import com.utt.foodorderapp.model.Address
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.model.Promotion
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user
import com.utt.foodorderapp.presentation.cart.CartViewModel
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.utils.GlideUtils
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import com.utt.foodorderapp.utils.MoneyUtils

class CartFragment : BaseFragment() {

    private var mFragmentCartBinding: FragmentCartBinding? = null
    private var mCartAdapter: CartAdapter? = null
    private var mListFoodCart: MutableList<Food>? = null
    private var mAmount = 0
    private var selectedDiscount = 0
    private var appliedPromotionCode: String? = null
    private var availablePromotions: MutableList<Promotion> = ArrayList()
    private val sePayApiService by lazy {
        SePayApiService(
                accountNumber = AppConfig.SEPAY_ACCOUNT_NUMBER,
                bankCode = AppConfig.SEPAY_BANK_CODE,
                qrBaseUrl = AppConfig.SEPAY_QR_URL
        )
    }
    private var sePayDialog: AlertDialog? = null
    private var sePayOrderRef: DatabaseReference? = null
    private var sePayOrderListener: ValueEventListener? = null
    private val sePayTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var sePayTimeoutRunnable: Runnable? = null
    private lateinit var cartViewModel: CartViewModel
    private val addressRepository = AddressRepository()

    // Tham chiếu form đang mở (để fill khi pick xong địa chỉ)
    private var activeNameInput: EditText? = null
    private var activePhoneInput: EditText? = null
    private var activeAddressInput: EditText? = null

    private val pickAddressLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        val extras = data.extras ?: return@registerForActivityResult
        val picked = BundleCompat.getSerializable(extras, AddressBookActivity.EXTRA_RESULT_ADDRESS, Address::class.java)
                ?: return@registerForActivityResult
        applyPickedAddress(picked)
    }

    private fun applyPickedAddress(picked: Address) {
        activeNameInput?.setText(picked.recipientName ?: "")
        activePhoneInput?.setText(picked.phone ?: "")
        activeAddressInput?.setText(picked.fullAddress ?: "")
    }

    private fun autoFillDefaultAddress() {
        val uid = user?.uid ?: return
        val nameInput = activeNameInput ?: return
        val phoneInput = activePhoneInput ?: return
        val addressInput = activeAddressInput ?: return
        if (!isEmpty(addressInput.text.toString())) return
        addressRepository.getDefaultAddress(uid) { def ->
            if (def == null) return@getDefaultAddress
            if (isEmpty(nameInput.text.toString())) nameInput.setText(def.recipientName ?: "")
            if (isEmpty(phoneInput.text.toString())) phoneInput.setText(def.phone ?: "")
            if (isEmpty(addressInput.text.toString())) addressInput.setText(def.fullAddress ?: "")
        }
    }

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
        val latestItems = (cartViewModel.cartState.value as? UiState.Success<List<Food>>)?.data ?: emptyList()
        if (mListFoodCart == null) {
            mListFoodCart = ArrayList()
        }
        mListFoodCart!!.clear()
        mListFoodCart!!.addAll(latestItems)
        if (mCartAdapter == null) {
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
        } else {
            mCartAdapter!!.notifyDataSetChanged()
        }
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
            val strZero: String = MoneyUtils.format(0)
            mFragmentCartBinding!!.tvTotalPrice.text = strZero
            mAmount = 0
            return
        }
        var totalPrice = 0
        for (food in listFoodCart) {
            totalPrice += food.totalPrice
        }
        val strTotalPrice: String = MoneyUtils.format(totalPrice)
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
                    if (position >= 0 && mListFoodCart != null && position < mListFoodCart!!.size) {
                        mListFoodCart!!.removeAt(position)
                        mCartAdapter!!.notifyItemRemoved(position)
                    }
                    calculateTotalPrice()
                    EventBus.getDefault().post(ReloadListCartEvent())
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
        val tvPickSavedAddress = viewDialog.findViewById<TextView>(R.id.tv_pick_saved_address)
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

        activeNameInput = edtNameOrder
        activePhoneInput = edtPhoneOrder
        activeAddressInput = edtAddressOrder
        autoFillDefaultAddress()
        tvPickSavedAddress.setOnClickListener {
            val ctx = activity ?: return@setOnClickListener
            val intent = Intent(ctx, AddressBookActivity::class.java)
            intent.putExtra(AddressBookActivity.EXTRA_PICK_MODE, true)
            pickAddressLauncher.launch(intent)
        }

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
        bottomSheetDialog.setOnDismissListener {
            activeNameInput = null
            activePhoneInput = null
            activeAddressInput = null
        }
        tvCancelOrder.setOnClickListener { bottomSheetDialog.dismiss() }
        tvCreateOrder.setOnClickListener {
            val strName = edtNameOrder.text.toString().trim { it <= ' ' }
            val strPhone = edtPhoneOrder.text.toString().trim { it <= ' ' }
            val strAddress = edtAddressOrder.text.toString().trim { it <= ' ' }
            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(activity, getString(R.string.message_enter_infor_order))
            } else if (!strPhone.matches(Regex("^0\\d{9}$"))) {
                showToastMessage(activity, getString(R.string.message_invalid_phone))
                edtPhoneOrder.requestFocus()
            } else {
                // Cả tiền mặt lẫn online đều tạo đơn ở trạng thái CHƯA THANH TOÁN.
                // Với online (SePay), sau khi tạo đơn sẽ mở mã QR và chờ webhook
                // xác nhận -> cập nhật ĐÃ THANH TOÁN (xử lý trong submitOrder).
                val paymentType = if (rdbPaymentOnline.isChecked) AppConfig.TYPE_PAYMENT_ONLINE else AppConfig.TYPE_PAYMENT_CASH
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
                // Mã không còn hợp lệ tại thời điểm đặt: KHÔNG âm thầm tính giá đầy đủ.
                // Bỏ mã, cập nhật lại giá và báo cho người dùng; giữ form để họ xem lại.
                selectedDiscount = 0
                appliedPromotionCode = null
                tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
                tvPriceOrder.text = "${MoneyUtils.format(mAmount)}"
                showToastMessage(activity, getString(R.string.msg_promotion_revoked))
                return@addOnSuccessListener
            }
            val discount = calculateDiscountAmount(promotion)
            selectedDiscount = discount
            submitOrder(name, phone, address, currentUser.email, currentUser.uid, discount, code,
                    bottomSheetDialog, paymentType, paymentStatus, paymentTransactionId)
        }.addOnFailureListener {
            // Lỗi mạng khi kiểm tra mã: KHÔNG đặt đơn giá đầy đủ. Báo lỗi để người dùng thử lại.
            tvPriceOrder.text = "${MoneyUtils.format((mAmount - selectedDiscount).coerceAtLeast(0))}"
            showToastMessage(activity, getString(R.string.msg_promotion_check_failed))
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
        hideSoftKeyboard(requireActivity())
        bottomSheetDialog.dismiss()
        if (paymentType == AppConfig.TYPE_PAYMENT_ONLINE) {
            // Tạo đơn "chờ thanh toán" rồi mở mã QR SePay; webhook sẽ xác nhận.
            cartViewModel.createPendingOrder(order) { ok ->
                if (ok) {
                    // Giỏ đã được xoá khi tạo đơn — cập nhật lại badge giỏ hàng.
                    EventBus.getDefault().post(ReloadListCartEvent())
                    showSePayPaymentDialog(order)
                } else {
                    showToastMessage(activity, AppConfig.GENERIC_ERROR)
                }
            }
        } else {
            cartViewModel.submitOrder(order)
        }
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
                        "${MoneyUtils.format(best.maxDiscountAmount)}"
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
            "${it.code} - ${it.title} (${it.discountPercent}%, max ${MoneyUtils.format(it.maxDiscountAmount)})"
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

    /**
     * Mở hộp thoại thanh toán SePay (VietQR) cho [order] vừa được tạo ở trạng thái
     * "chờ thanh toán". Hiển thị mã QR (nội dung CK = mã đơn) và LẮNG NGHE realtime
     * node /booking/{id}; khi webhook SePay cập nhật paymentStatus = ĐÃ THANH TOÁN,
     * hộp thoại tự đóng và báo thành công.
     */
    private fun showSePayPaymentDialog(order: Order) {
        val ctx = activity ?: return
        val amountVnd = order.amount.toLong() * 1000L
        val content = SePayApiService.contentForOrder(order.id)

        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.dialog_sepay_payment, null, false)
        val ivQr = view.findViewById<ImageView>(R.id.iv_sepay_qr)
        val tvAmount = view.findViewById<TextView>(R.id.tv_sepay_amount)
        val tvBank = view.findViewById<TextView>(R.id.tv_sepay_bank)
        val tvAccount = view.findViewById<TextView>(R.id.tv_sepay_account)
        val tvHolder = view.findViewById<TextView>(R.id.tv_sepay_holder)
        val tvContent = view.findViewById<TextView>(R.id.tv_sepay_content)
        val tvStatus = view.findViewById<TextView>(R.id.tv_sepay_status)
        val tvCancel = view.findViewById<TextView>(R.id.tv_sepay_cancel)
        val tvClose = view.findViewById<TextView>(R.id.tv_sepay_paid)

        tvAmount.text = MoneyUtils.format(order.amount)
        tvBank.text = AppConfig.SEPAY_BANK_CODE
        tvAccount.text = AppConfig.SEPAY_ACCOUNT_NUMBER
        tvHolder.text = AppConfig.SEPAY_ACCOUNT_HOLDER
        tvContent.text = content
        tvStatus.text = getString(R.string.sepay_waiting)
        tvCancel.text = getString(R.string.sepay_cancel_order)
        tvClose.text = getString(R.string.sepay_close)
        GlideUtils.loadUrl(sePayApiService.buildQrUrl(amountVnd, content), ivQr)

        // Chạm để sao chép nhanh số tài khoản / nội dung
        tvAccount.setOnClickListener { copyToClipboard(AppConfig.SEPAY_ACCOUNT_NUMBER) }
        tvContent.setOnClickListener { copyToClipboard(content) }

        val dialog = AlertDialog.Builder(ctx)
                .setView(view)
                .setCancelable(false)
                .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        sePayDialog = dialog

        // Lắng nghe realtime: webhook SePay sẽ đặt paymentStatus = PAID
        val orderRef = ControllerApplication.getInstance()
                .bookingDatabaseReference.child(order.id.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val current = snapshot.getValue(Order::class.java) ?: return
                if (current.paymentStatus == Order.PAYMENT_STATUS_PAID) {
                    stopSePayOrderListener()
                    showToastMessage(activity, getString(R.string.msg_online_payment_success))
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) { /* giữ nguyên trạng thái chờ */ }
        }
        orderRef.addValueEventListener(listener)
        sePayOrderRef = orderRef
        sePayOrderListener = listener

        // "Hủy đơn": chuyển đơn sang trạng thái ĐÃ HỦY rồi đóng
        tvCancel.setOnClickListener {
            orderRef.child("status").setValue(Order.STATUS_CANCEL)
            stopSePayOrderListener()
            dialog.dismiss()
            showToastMessage(activity, getString(R.string.msg_online_payment_cancelled))
        }

        // "Đóng": để đơn ở trạng thái chờ; webhook vẫn tự xác nhận, xem ở Lịch sử
        tvClose.setOnClickListener {
            stopSePayOrderListener()
            dialog.dismiss()
            showToastMessage(activity, getString(R.string.sepay_keep_pending))
        }

        dialog.setOnDismissListener {
            stopSePayOrderListener()
            if (sePayDialog === dialog) sePayDialog = null
        }
        dialog.show()

        // Hết thời gian chờ: không treo vô hạn. Sau SEPAY_TIMEOUT_MS vẫn chưa thanh toán
        // thì báo cho người dùng và để nút "Đóng" nổi bật — đơn vẫn ở Lịch sử, webhook
        // vẫn tự xác nhận sau nếu tiền về.
        sePayTimeoutRunnable = Runnable {
            tvStatus.text = getString(R.string.sepay_timeout)
        }
        sePayTimeoutHandler.postDelayed(sePayTimeoutRunnable!!, SEPAY_TIMEOUT_MS)
    }

    private fun stopSePayOrderListener() {
        sePayTimeoutRunnable?.let { sePayTimeoutHandler.removeCallbacks(it) }
        sePayTimeoutRunnable = null
        val listener = sePayOrderListener ?: return
        sePayOrderRef?.removeEventListener(listener)
        sePayOrderListener = null
        sePayOrderRef = null
    }

    private fun copyToClipboard(text: String) {
        val ctx = activity ?: return
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("sepay", text))
        showToastMessage(activity, getString(R.string.sepay_copied))
    }

    private fun applyPromotionCode(code: String, tvDiscountValue: TextView, tvPriceOrder: TextView) {
        ControllerApplication[requireActivity()].promotionDatabaseReference.child(code).get().addOnSuccessListener { snapshot ->
            val promotion = snapshot.getValue(Promotion::class.java)
            if (promotion == null || !promotion.isActive) {
                selectedDiscount = 0
                appliedPromotionCode = null
                tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
                tvPriceOrder.text = "${MoneyUtils.format(mAmount)}"
                return@addOnSuccessListener
            }
            if (mAmount < promotion.minOrderAmount) {
                selectedDiscount = 0
                appliedPromotionCode = null
                tvDiscountValue.text = getString(R.string.msg_promotion_not_meet_condition)
                tvPriceOrder.text = "${MoneyUtils.format(mAmount)}"
                return@addOnSuccessListener
            }
            val discount = calculateDiscountAmount(promotion)
            selectedDiscount = discount
            appliedPromotionCode = code
            val finalAmount = (mAmount - discount).coerceAtLeast(0)
            tvDiscountValue.text = "${getString(R.string.discount_amount)}: -${MoneyUtils.format(discount)}"
            tvPriceOrder.text = "${MoneyUtils.format(finalAmount)}"
            showToastMessage(activity, getString(R.string.msg_promotion_applied))
        }.addOnFailureListener {
            selectedDiscount = 0
            appliedPromotionCode = null
            tvDiscountValue.text = getString(R.string.msg_promotion_invalid)
            tvPriceOrder.text = "${MoneyUtils.format(mAmount)}"
        }
    }

    private fun getStringListFoodsOrder(): String {
        if (mListFoodCart == null || mListFoodCart!!.isEmpty()) {
            return ""
        }
        var result = ""
        for (food in mListFoodCart!!) {
            result = if (isEmpty(result)) {
                ("- " + food.name + " (" + MoneyUtils.format(food.realPrice) + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count)
            } else {
                (result + "\n" + ("- " + food.name + " (" + MoneyUtils.format(food.realPrice) + ") "
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
        stopSePayOrderListener()
        sePayDialog?.dismiss()
        sePayDialog = null
        mCartAdapter = null
        mFragmentCartBinding = null
    }

    private fun observeViewModel() {
        cartViewModel.cartState.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                initDataFoodCart()
            }
        }
        cartViewModel.orderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Unit
                is UiState.Success -> {
                    clearCart()
                    cartViewModel.loadCart()
                    EventBus.getDefault().post(ReloadListCartEvent())
                    showToastMessage(activity, getString(R.string.msg_order_success))
                }
                is UiState.Error -> {
                    showToastMessage(activity, state.message)
                }
            }
        }
        cartViewModel.loadCart()
    }

    companion object {
        /** Thời gian chờ thanh toán SePay trước khi báo hết hạn (3 phút). */
        private const val SEPAY_TIMEOUT_MS = 3 * 60 * 1000L
    }
}