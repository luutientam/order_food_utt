package com.utt.foodorderapp.data.remote

import java.net.URLEncoder

/**
 * Hỗ trợ thanh toán online qua [SePay](https://sepay.vn) bằng VietQR.
 *
 * Luồng CHUẨN (webhook), app KHÔNG giữ API token:
 *  1. App tạo đơn ở trạng thái "chờ thanh toán" và sinh mã QR chuyển khoản
 *     (VietQR) cho tài khoản nhận tiền, đúng số tiền + nội dung = mã đơn ([buildQrUrl]).
 *  2. Người dùng quét QR, chuyển khoản.
 *  3. SePay gọi WEBHOOK (Cloudflare Worker) -> ghi thẳng "đã thanh toán" vào
 *     Realtime Database. App lắng nghe realtime để xác nhận.
 *
 * Endpoint ảnh QR (`qr.sepay.vn/img`) là public, không cần token.
 */
class SePayApiService(
    private val accountNumber: String,
    private val bankCode: String,
    private val qrBaseUrl: String
) {

    /**
     * Tạo URL ảnh QR VietQR của SePay.
     * @param amountVnd số tiền THỰC (đồng), ví dụ 32000
     * @param content  nội dung chuyển khoản = mã đơn (chỉ chữ/số), ví dụ "UTT1718200000000"
     */
    fun buildQrUrl(amountVnd: Long, content: String): String {
        val des = URLEncoder.encode(content, "UTF-8")
        // template=qronly: QR THUẦN (không logo che giữa) -> app ngân hàng quét ổn định hơn
        return "$qrBaseUrl?acc=$accountNumber&bank=$bankCode&amount=$amountVnd&des=$des&template=qronly"
    }

    companion object {
        /** Tiền tố nội dung chuyển khoản để webhook nhận diện đơn. */
        const val CONTENT_PREFIX = "UTT"

        /** Sinh mã nội dung chuyển khoản từ id đơn hàng. */
        fun contentForOrder(orderId: Long): String = "$CONTENT_PREFIX$orderId"
    }
}
