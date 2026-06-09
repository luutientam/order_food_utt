package com.utt.foodorderapp.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Định dạng tiền tệ chuẩn Việt Nam.
 *
 * Trong app, giá được lưu theo đơn vị NGHÌN đồng (ví dụ: price = 32 nghĩa là
 * 32.000đ). Các hàm bên dưới quy đổi sang số tiền đầy đủ và thêm dấu phân cách
 * hàng nghìn ("." theo chuẩn VN) kèm hậu tố "đ".
 *
 * Ví dụ: format(32) -> "32.000đ", format(1234) -> "1.234.000đ".
 */
object MoneyUtils {

    private const val UNIT = 1000L
    private const val SUFFIX = "đ"

    private val groupingFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = '.' }
        DecimalFormat("#,###", symbols)
    }

    /** Giá lưu theo đơn vị nghìn -> chuỗi tiền đầy đủ, ví dụ 32 -> "32.000đ". */
    @JvmStatic
    fun format(valueInThousands: Int): String =
        groupingFormat.format(valueInThousands.toLong() * UNIT) + SUFFIX

    /** Bản Long cho các tổng tiền lớn (doanh thu, thu nhập...), cũng theo đơn vị nghìn. */
    @JvmStatic
    fun format(valueInThousands: Long): String =
        groupingFormat.format(valueInThousands * UNIT) + SUFFIX

    /** Số tiền đầy đủ (đồng) -> chuỗi có phân cách, ví dụ 32000 -> "32.000đ". */
    @JvmStatic
    fun formatExact(amount: Long): String = groupingFormat.format(amount) + SUFFIX

    /** Chỉ phần số (không hậu tố), dùng khi cần ghép vào câu/format string khác. */
    @JvmStatic
    fun number(valueInThousands: Int): String =
        groupingFormat.format(valueInThousands.toLong() * UNIT)
}
