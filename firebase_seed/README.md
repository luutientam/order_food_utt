# Dữ liệu fake cho app Order Food UTT

Bộ dữ liệu mẫu để nạp vào **Firebase Realtime Database** (`order-food-utt`,
vùng `asia-southeast1`) cho mục đích demo / làm đồ án.

## Có gì trong này

| Node | Số lượng | Mô tả |
|------|----------|-------|
| `/categories` | 12 | Cơm, Phở & Bún, Mì & Cháo, Gà rán, Đồ uống, Ăn vặt, Bánh mì, Tráng miệng, Lẩu & Nướng, Hải sản, Món chay, Pizza & Mỳ Ý |
| `/restaurants` | 10 | Quán ăn quanh khu vực UTT (Triều Khúc, Thanh Xuân) |
| `/food` | 64 | Món ăn đa dạng, đã gắn `categoryId` + `restaurantId` để **lọc theo danh mục / theo quán chạy đúng**; có món đủ 3 mức giá |
| `/promotions` | 6 | Mã giảm giá (1 mã đang tắt để test) |
| `/users` | 8 | 1 admin, 5 khách, 2 shipper |
| `/addresses` | 5 user | Sổ địa chỉ giao hàng kèm toạ độ |
| `/reviews` | 10 | Đánh giá món/quán |
| `/booking` | 12 | Đơn hàng đủ trạng thái: mới, đang chuẩn bị, đang giao, thành công, huỷ, thất bại |
| `/feedback` | 6 | Phản hồi từ màn hình liên hệ |

File:
- `firebase_seed.json` — gộp tất cả, dùng để **import ở gốc (root)**.
- `nodes/*.json` — tách từng node, dùng để **import từng node** (an toàn hơn).
- `generate_seed.py` — script sinh lại dữ liệu (sửa rồi chạy `python3 generate_seed.py`).
- `seed_to_firebase.sh` — nạp qua Firebase CLI (không xoá dữ liệu cũ).

## Cách nạp dữ liệu — chọn 1 trong 3

### Cách 1 — Firebase Console, import từng node (khuyến nghị)
Chỉ thay node đó, **không đụng** các node khác (giữ user thật đã đăng ký).

1. Mở https://console.firebase.google.com → project **order-food-utt** → **Realtime Database**.
2. Bấm vào node cần nạp (ví dụ `food`). Nếu chưa có thì tạo: ở gốc bấm **⋮ → Import JSON** cũng được.
3. Bấm **⋮ (More) → Import JSON** → chọn file tương ứng trong `nodes/` (ví dụ `nodes/food.json`).
4. Lặp lại cho: `categories`, `restaurants`, `food`, `promotions`, `users`, `addresses`, `reviews`, `booking`, `feedback`.

> Import tại một node sẽ **ghi đè toàn bộ node đó**, nhưng không ảnh hưởng node khác.

### Cách 2 — Firebase CLI (gộp thêm, không xoá)
```bash
npm install -g firebase-tools
firebase login
bash seed_to_firebase.sh
```
Script dùng `database:update` (PATCH) nên chỉ **thêm/ghi đè các bản ghi trùng id**, giữ nguyên dữ liệu còn lại.

### Cách 3 — Import ở gốc (nhanh nhưng XOÁ SẠCH database)
Realtime Database → node gốc `/` → **Import JSON** → chọn `firebase_seed.json`.

> ⚠️ Cách này **thay thế toàn bộ** database hiện tại. Chỉ dùng khi DB đang trống hoặc bạn muốn reset sạch.

## Lưu ý quan trọng về đăng nhập

`/users` ở đây là **hồ sơ trong database**, KHÔNG phải tài khoản Firebase Authentication.
Các tài khoản fake này hiển thị trong màn quản lý của admin và được tham chiếu trong đơn hàng,
nhưng **chưa đăng nhập được** vì chưa có tài khoản Auth tương ứng.

Để đăng nhập thật theo từng vai trò, làm 1 trong 2:
- **Cách nhanh:** đăng ký bình thường trong app, rồi vào Firebase Console mở `/users/<uid của bạn>`
  và sửa `role` thành `admin` / `shipper` (đồng thời `isAdmin`/`admin` = `true` nếu là admin).
- Hoặc tạo tài khoản trong **Authentication** với đúng email, rồi copy `uid` thật vào key node `/users`.

Tài khoản admin theo quy ước email là `...@admin.com` (xem `config_admin_email_format`).

## Ảnh món ăn

Dùng **ảnh thật** từ Wikimedia Commons qua endpoint chính thức
`https://commons.wikimedia.org/wiki/Special:FilePath/<tên_file>?width=640`
(redirect sang `upload.wikimedia.org`, hotlink ổn định, lấy được mọi kích thước).
App load ảnh bằng Glide nên tự theo redirect.

Muốn đổi ảnh khác: sửa map `IMG` trong `generate_seed.py` rồi chạy lại `python3 generate_seed.py`.

## Đơn vị giá

Giá lưu theo **nghìn đồng**: `price = 45` hiển thị `45 000 VNĐ`. Bộ lọc giá ở màn Home: thấp `0–49k`,
vừa `50–100k`, cao `>100k` — dữ liệu đã có món ở cả ba mức.
