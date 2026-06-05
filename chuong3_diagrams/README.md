# Biểu đồ Chương 3 — App đặt & giao đồ ăn trực tuyến

**40 biểu đồ** (PlantUML), khớp với source code thật trong `FoodOrderProKotlinUTT/`.
Dùng để vẽ lại toàn bộ Chương 3 — các biểu đồ cũ trong file `.docx` đang là của dự án
"Quản lý hiệu thuốc" (sai đề tài).

| Loại | Số lượng |
|------|:---:|
| Use case (1 tổng quát + 14 chi tiết) | 15 |
| Tuần tự (Sequence) | 12 |
| Hoạt động (Activity) | 12 |
| Lớp (Class) | 1 |
| **Tổng** | **40** |

## Cách vẽ trên diagrams.net (draw.io)

1. Mở https://app.diagrams.net → tạo file mới (Blank Diagram).
2. Menu **Arrange** (cột phải) → **Insert** → **Advanced** → **PlantUML…**
3. Mở file `.puml`, copy **một khối từ `@startuml` đến `@enduml`** (= 1 biểu đồ) → dán → **Insert**.
4. Lặp lại cho từng khối. Xuất ảnh: **File → Export as → PNG** (nền trắng, 300 DPI để in báo cáo).

> Mỗi `@startuml … @enduml` là một hình riêng — đừng dán cả file một lần.
> Tiếng Việt có dấu hiển thị tốt. Use case trên draw.io sẽ bố trí ngang (trái→phải).

## Danh sách 40 biểu đồ & mục báo cáo

### `01_bieu_do_usecase.puml` (15)
| Mục | Biểu đồ |
|-----|---------|
| 3.2.3 | Use case tổng quát (3 actor) |
| 3.2.4.1 | Đăng nhập |
| 3.2.4.2 | Đăng ký tài khoản |
| 3.2.4.3 | Quản lý sổ địa chỉ |
| 3.2.4.4 | Đặt hàng |
| 3.2.4.5 | Áp dụng mã khuyến mại |
| 3.2.4.6 | Theo dõi đơn hàng |
| 3.2.4.7 | Đánh giá món ăn |
| 3.2.4.8 | Quản lý đơn hàng (Admin) |
| 3.2.4.9 | Báo cáo doanh thu (Admin) |
| 3.2.4.10 | Quản lý món ăn (Admin) |
| 3.2.4.11 | Quản lý người dùng (Admin) |
| 3.2.4.12 | Quản lý khuyến mại (Admin) |
| 3.2.4.13 | Nhận & giao đơn (Shipper) |
| 3.2.4.14 | Xem thu nhập (Shipper) |

### `02_bieu_do_tuan_tu.puml` (12)
| Mục | Biểu đồ |
|-----|---------|
| 3.2.5.1 | Đăng nhập |
| 3.2.5.2 | Đăng ký tài khoản |
| 3.2.5.3 | Đặt hàng |
| 3.2.5.4 | Áp dụng mã khuyến mại |
| 3.2.5.5 | Theo dõi đơn realtime |
| 3.2.5.6 | Đánh giá món ăn |
| 3.2.5.7 | Admin quản lý món ăn |
| 3.2.5.8 | Shipper nhận & giao đơn |
| 3.2.5.9 | Quản lý người dùng |
| 3.2.5.10 | Quản lý khuyến mại |
| 3.2.5.11 | Báo cáo doanh thu |
| 3.2.5.12 | Đăng xuất |

### `03_bieu_do_hoat_dong.puml` (12)
| Mục | Biểu đồ |
|-----|---------|
| 3.2.6.1 | Đăng nhập |
| 3.2.6.2 | Đặt hàng |
| 3.2.6.3 | Cập nhật trạng thái đơn |
| 3.2.6.4 | Áp dụng voucher |
| 3.2.6.5 | Theo dõi đơn realtime |
| 3.2.6.6 | Đánh giá món ăn |
| 3.2.6.7 | Quản lý món ăn (Admin) |
| 3.2.6.8 | Báo cáo doanh thu (Admin) |
| 3.2.6.9 | Shipper nhận đơn |
| 3.2.6.10 | Shipper giao đơn |
| 3.2.6.11 | Quản lý người dùng (Admin) |
| 3.2.6.12 | Quản lý khuyến mại (Admin) |

### `04_bieu_do_lop.puml` (1)
| Mục | Biểu đồ |
|-----|---------|
| 3.2.7 | Biểu đồ lớp tổng thể |

## Quy ước trong dự án (để mô tả khi bảo vệ)

- **3 actor:** Khách hàng (customer) · Quản trị viên (admin) · Shipper.
- **Trạng thái đơn (Order.status):** 0=Mới đặt · 1=Đang chuẩn bị · 2=Đang giao · 3=Thành công · 4=Đã huỷ · 5=Thất bại.
- **Kiến trúc:** MVVM — View (Activity/Fragment) → Repository → Firebase.
- **Lưu trữ:** Firebase Realtime Database (`/users`, `/food`, `/booking`, `/promotions`, `/reviews`, `/feedback`, `/restaurants`, `/categories`, `/addresses`) + Firebase Storage (ảnh) + Room (giỏ hàng offline).
- **Điểm nhấn kỹ thuật:** đồng bộ realtime đơn hàng, transaction khi shipper nhận đơn (chống tranh chấp), theo dõi vị trí shipper trên Google Maps.
