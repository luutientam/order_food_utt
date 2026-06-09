package com.utt.foodorderapp.data.remote

import android.os.Handler
import android.os.Looper

/**
 * Dịch vụ ngân hàng GIẢ LẬP (demo) cho luồng thanh toán online.
 *
 * Không gọi API thật — chỉ mô phỏng việc tạo giao dịch và trả về kết quả
 * thành công sau một khoảng trễ ngắn, để demo luồng đặt hàng "đã thanh toán".
 */
class FakeBankApiService {

    /** Kết quả của một giao dịch giả lập. */
    data class PaymentResult(
        val isSuccess: Boolean,
        val transactionId: String?,
        val message: String?
    )

    /**
     * Tạo giao dịch thanh toán giả lập.
     *
     * @param requestId mã yêu cầu (thường là timestamp hiện tại)
     * @param amount    số tiền cần thanh toán (đơn vị: nghìn đồng)
     * @param callback  được gọi trên main thread khi có kết quả
     */
    fun createPayment(requestId: Long, amount: Int, callback: (PaymentResult) -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            val transactionId = "TXN$requestId"
            callback(PaymentResult(isSuccess = true, transactionId = transactionId, message = "Thanh toán thành công"))
        }, SIMULATED_DELAY_MS)
    }

    companion object {
        private const val SIMULATED_DELAY_MS = 1200L
    }
}
