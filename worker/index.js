/**
 * Webhook SePay cho UTT Food — chạy trên Cloudflare Workers (MIỄN PHÍ, không cần thẻ).
 *
 * Khi khách quét VietQR và chuyển khoản, SePay gọi HTTP POST tới Worker này. Worker:
 *   1. Xác thực header  Authorization: Apikey <SEPAY_WEBHOOK_API_KEY>
 *   2. Lấy mã đơn từ nội dung chuyển khoản  (UTT<orderId>)
 *   3. Đối chiếu số tiền và GHI THẲNG vào Realtime Database (REST API):
 *      /booking/<orderId>  =>  paymentStatus = 1, paymentTransactionId = <ref>
 *
 * App lắng nghe realtime nên tự hiện "Thanh toán thành công".
 *
 * Biến môi trường (đặt bằng `wrangler secret put ...` hoặc trong Dashboard):
 *   - SEPAY_WEBHOOK_API_KEY : khóa bí mật bạn tự đặt (khớp cấu hình webhook ở SePay)
 *   - FIREBASE_DB_URL       : https://order-food-utt-default-rtdb.asia-southeast1.firebasedatabase.app
 *   - FIREBASE_DB_SECRET    : Database secret (Firebase Console > Project settings > Service accounts > Database secrets)
 */

const PAYMENT_STATUS_PAID = 1;

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ success: false, message: "Method not allowed" }, 405);
    }

    // 1) Xác thực — SePay gửi header "Authorization: Apikey <key>"
    const auth = request.headers.get("authorization") || "";
    // [DEBUG tạm] log header nhận được vs mong đợi để dò lệch API key
    console.log("DEBUG auth received =", JSON.stringify(auth));
    console.log("DEBUG auth expected =", JSON.stringify(`Apikey ${env.SEPAY_WEBHOOK_API_KEY || ""}`));
    if (!env.SEPAY_WEBHOOK_API_KEY || auth !== `Apikey ${env.SEPAY_WEBHOOK_API_KEY}`) {
      return json({ success: false, message: "Unauthorized" }, 401);
    }

    // 2) Đọc payload SePay
    let body;
    try {
      body = await request.json();
    } catch (e) {
      return json({ success: false, message: "Invalid JSON" }, 400);
    }

    const transferType = body.transferType; // "in" = tiền vào
    const amount = Number(body.transferAmount) || 0;
    const content = String(body.content || "");
    const reference = String(body.referenceCode || body.id || "");

    if (transferType && transferType !== "in") {
      return json({ success: true, message: "Ignored non-incoming transfer" });
    }

    // 3) Lấy mã đơn từ nội dung: UTT<orderId>
    const normalized = content.toUpperCase().replace(/[^A-Z0-9]/g, "");
    const match = normalized.match(/UTT(\d{10,})/);
    if (!match) {
      return json({ success: true, message: "No order code in content" });
    }
    const orderId = match[1];

    const base = String(env.FIREBASE_DB_URL || "").replace(/\/$/, "");
    const secret = env.FIREBASE_DB_SECRET;
    const orderUrl = `${base}/booking/${orderId}.json?auth=${secret}`;

    // 4) Đọc đơn để đối chiếu
    const getRes = await fetch(orderUrl);
    if (!getRes.ok) {
      return json({ success: false, message: "DB read failed" }, 502);
    }
    const order = await getRes.json();
    if (!order) {
      return json({ success: true, message: "Order not found" });
    }
    if (Number(order.paymentStatus) === PAYMENT_STATUS_PAID) {
      return json({ success: true, message: "Already paid" });
    }

    // amount của đơn lưu theo NGHÌN đồng -> quy đổi sang VND thật để đối chiếu
    const expectedVnd = Number(order.amount) * 1000;
    if (expectedVnd <= 0 || amount < expectedVnd) {
      return json({ success: true, message: "Amount mismatch" });
    }

    // 5) GHI THẲNG trạng thái đã thanh toán vào Realtime DB
    const patchRes = await fetch(orderUrl, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        paymentStatus: PAYMENT_STATUS_PAID,
        paymentTransactionId: reference,
      }),
    });
    if (!patchRes.ok) {
      return json({ success: false, message: "DB write failed" }, 502);
    }

    return json({ success: true });
  },
};

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}
