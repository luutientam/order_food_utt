package com.utt.foodorderapp.data.remote

import android.os.Handler
import android.os.Looper
import java.util.UUID

class FakeBankApiService {

    data class PaymentResult(
            val isSuccess: Boolean,
            val transactionId: String?,
            val bankCode: String,
            val message: String
    )

    fun createPayment(orderId: Long, amount: Int, callback: (PaymentResult) -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (amount <= 0) {
                callback(PaymentResult(false, null, "UTTBANK", "Số tiền thanh toán không hợp lệ"))
                return@postDelayed
            }
            val transactionId = "UTT-${orderId}-${UUID.randomUUID().toString().take(8).uppercase()}"
            callback(
                    PaymentResult(
                            isSuccess = true,
                            transactionId = transactionId,
                            bankCode = "UTTBANK",
                            message = "Thanh toán thành công qua API giả lập"
                    )
            )
        }, 1200)
    }
}
