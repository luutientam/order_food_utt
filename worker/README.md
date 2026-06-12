# Webhook SePay — Cloudflare Worker (miễn phí, không cần thẻ)

Xác nhận thanh toán online cho UTT Food mà **không cần Firebase Blaze / không cần thẻ**.
SePay gọi Worker này → Worker **ghi thẳng vào Realtime Database** → app lắng nghe realtime
nên tự hiện "Thanh toán thành công".

```
App (tạo đơn "chờ TT" + hiện QR)  ──▶  Ngân hàng  ──▶  SePay  ──▶  Cloudflare Worker
                ▲                                                          │  (REST API)
                └──────────────  Realtime DB (/booking/<id>)  ◀────────────┘
                         paymentStatus = 1, paymentTransactionId
```

App **không** chứa secret. Worker giữ 2 bí mật: khóa webhook (khớp SePay) và Database secret (ghi DB).

## 0) Chuẩn bị
- Tài khoản **Cloudflare** (miễn phí, không cần thẻ): https://dash.cloudflare.com/sign-up
- Cài Wrangler: `npm i -g wrangler` rồi `wrangler login`
- **Firebase Database secret**: Firebase Console → ⚙ *Project settings* → *Service accounts*
  → *Database secrets* → *Show/Add* → copy chuỗi secret.
  (Nếu mục này bị ẩn: bấm "Manage" / bật legacy, hoặc nhắn mình để chuyển sang dùng
  service-account token thay thế.)

## 1) Cập nhật thông tin tài khoản nhận tiền (trong app)
`FoodOrderProKotlinUTT/app/src/main/res/values/app_config.xml`:
- `config_sepay_account_number` — số tài khoản nhận tiền
- `config_sepay_bank_code` — mã ngân hàng VietQR (đang để `TPBank`)
- `config_sepay_account_holder` — tên chủ tài khoản (chỉ để hiển thị)

## 2) Đặt các biến bí mật cho Worker
```bash
cd worker
wrangler secret put SEPAY_WEBHOOK_API_KEY   # dán khóa webhook (chuỗi bí mật bạn tự đặt)
wrangler secret put FIREBASE_DB_SECRET      # dán Database secret ở bước 0
```
(`FIREBASE_DB_URL` đã set sẵn trong `wrangler.toml`.)

## 3) Deploy
```bash
wrangler deploy
```
Xong sẽ in ra URL dạng:
```
https://sepay-webhook.<tên-của-bạn>.workers.dev
```
**Copy URL này.**

## 4) Khai báo webhook ở SePay
https://my.sepay.vn → **Webhooks** → *Thêm Webhook*:
- **URL**: dán URL Worker ở bước 3
- **Kiểu xác thực**: `API Key` → giá trị = đúng `SEPAY_WEBHOOK_API_KEY`
  (SePay gửi header `Authorization: Apikey <key>`)
- **Sự kiện**: giao dịch *tiền vào* (incoming); liên kết tài khoản ngân hàng đã cấu hình.

## 5) Kiểm thử
- Đặt 1 đơn → chọn **Thanh toán online (SePay)** → màn QR hiện ra.
- Chuyển đúng **số tiền** và **nội dung** (vd `UTT1718200000000`).
- Vài giây sau đơn tự chuyển *Đã thanh toán*, hộp thoại QR tự đóng.

Worker khớp đơn bằng `UTT<orderId>` trong nội dung CK, đối chiếu số tiền
(`order.amount × 1000`, vì app lưu tiền theo *nghìn đồng*), rồi PATCH `/booking/<orderId>`.

## Chạy thử cục bộ (tùy chọn)
```bash
cp .dev.vars.example .dev.vars   # điền FIREBASE_DB_SECRET thật
wrangler dev
```

## Sự cố thường gặp
- **401 Unauthorized**: API Key ở SePay khác `SEPAY_WEBHOOK_API_KEY` → sửa cho khớp.
- **DB write failed / 502**: `FIREBASE_DB_SECRET` sai hoặc `FIREBASE_DB_URL` sai.
- **"Order not found"**: nội dung CK mất tiền tố `UTT<id>` (ngân hàng cắt ký tự) → chuyển lại đúng nội dung trong QR.
- Xem log realtime: `wrangler tail`.
