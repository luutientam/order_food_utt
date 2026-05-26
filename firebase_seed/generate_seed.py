# -*- coding: utf-8 -*-
"""
Sinh dữ liệu fake cho app Order Food UTT (Firebase Realtime Database).

Khớp đúng cấu trúc node và tên field mà app đọc/ghi:
  /categories /restaurants /food /promotions /users /addresses /reviews /booking /feedback

Quy tắc serialize boolean của Firebase (đã xác minh từ bản export thật):
  isPopular -> "popular", isCompleted -> "completed", isActive -> "active",
  isDefault -> "default", isAdmin -> "admin".
Với /users còn thêm "isActive"/"isAdmin" để thoả mãn database.rules.json.

Ảnh: dùng URL thật từ Wikimedia Commons (upload.wikimedia.org) - vĩnh viễn, hotlink được.

Chạy:  python3 generate_seed.py
"""
import json
import os
import re

OUT_DIR = os.path.dirname(os.path.abspath(__file__))
NODES_DIR = os.path.join(OUT_DIR, "nodes")
os.makedirs(NODES_DIR, exist_ok=True)

CURRENCY = " 000 VNĐ"          # app nối thẳng vào số -> "45 000 VNĐ"
QTY_LABEL = "Số lượng:"        # R.string.quantity


def real_price(price, sale):
    """Khớp Food.realPrice: price - price * sale / 100 (chia nguyên)."""
    if sale <= 0:
        return price
    return price - price * sale // 100


# ---------------------------------------------------------------------------
# Thư viện ảnh thật (Wikimedia Commons). Giá trị là URL thumbnail 330px;
# img() đổi sang kích thước mong muốn.
# ---------------------------------------------------------------------------
IMG = {
    "comtam":   "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/C%C6%A1m_T%E1%BA%A5m%2C_Da_Nang%2C_Vietnam.jpg/330px-C%C6%A1m_T%E1%BA%A5m%2C_Da_Nang%2C_Vietnam.jpg",
    "comga":    "https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Hainanese_Chicken_Rice.jpg/330px-Hainanese_Chicken_Rice.jpg",
    "comrang":  "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Koh_Mak%2C_Thailand%2C_Fried_rice_with_seafood%2C_Thai_fried_rice.jpg/330px-Koh_Mak%2C_Thailand%2C_Fried_rice_with_seafood%2C_Thai_fried_rice.jpg",
    "pho":      "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Bowl_of_Meatball_pho.jpg/330px-Bowl_of_Meatball_pho.jpg",
    "bunbohue": "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/B%C3%BAn_b%C3%B2_Hu%E1%BA%BF_-_Ch%E1%BB%A3_%C4%90%C3%B4ng_Ba_%282024%29_-_img_02.jpg/330px-B%C3%BAn_b%C3%B2_Hu%E1%BA%BF_-_Ch%E1%BB%A3_%C4%90%C3%B4ng_Ba_%282024%29_-_img_02.jpg",
    "buncha":   "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/B%C3%BAn_ch%E1%BA%A3_Vietnamese_food.jpg/330px-B%C3%BAn_ch%E1%BA%A3_Vietnamese_food.jpg",
    "bunrieu":  "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/B%C3%BAn_ri%C3%AAu_%C4%91%E1%BA%B7c_bi%E1%BB%87t.jpg/330px-B%C3%BAn_ri%C3%AAu_%C4%91%E1%BA%B7c_bi%E1%BB%87t.jpg",
    "hutieu":   "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Hu_Tieu_Nam_Vang.jpg/330px-Hu_Tieu_Nam_Vang.jpg",
    "miquang":  "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/M%C3%AC_Qu%E1%BA%A3ng%2C_Da_Nang%2C_Vietnam.jpg/330px-M%C3%AC_Qu%E1%BA%A3ng%2C_Da_Nang%2C_Vietnam.jpg",
    "mitron":   "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Mi_Goreng_GM.jpg/330px-Mi_Goreng_GM.jpg",
    "micay":    "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Jjampong.JPG/330px-Jjampong.JPG",
    "mien":     "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Cooked_dangmyeon.jpg/330px-Cooked_dangmyeon.jpg",
    "chao":     "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/Chinese_rice_congee.jpg/330px-Chinese_rice_congee.jpg",
    "garan":    "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Fried-Chicken-Set.jpg/330px-Fried-Chicken-Set.jpg",
    "burger":   "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/RedDot_Burger.jpg/330px-RedDot_Burger.jpg",
    "khoaitay": "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/French_Fries.JPG/330px-French_Fries.JPG",
    "popcorn":  "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/KFC_Popcorn_Chicken_%2812956064765%29.jpg/330px-KFC_Popcorn_Chicken_%2812956064765%29.jpg",
    "caphe":    "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Viet-coffee.jpg/330px-Viet-coffee.jpg",
    "trasua":   "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Bubble_Tea.png/330px-Bubble_Tea.png",
    "trada":    "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/Iced_Tea_from_flickr.jpg/330px-Iced_Tea_from_flickr.jpg",
    "sinhto":   "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Kiwi_Smoothie.jpg/330px-Kiwi_Smoothie.jpg",
    "banhmi":   "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/B%C3%A1nh_m%C3%AC_th%E1%BB%8Bt_n%C6%B0%E1%BB%9Bng.png/330px-B%C3%A1nh_m%C3%AC_th%E1%BB%8Bt_n%C6%B0%E1%BB%9Bng.png",
    "xoi":      "https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/X%C3%B4i_s%E1%BA%AFn.jpg/330px-X%C3%B4i_s%E1%BA%AFn.jpg",
    "banhbao":  "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Baozi_Chengdu.JPG/330px-Baozi_Chengdu.JPG",
    "hacao":    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Chinese_DimSum_%289023590541%29.jpg/330px-Chinese_DimSum_%289023590541%29.jpg",
    "chagio":   "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Cha_gio.jpg/330px-Cha_gio.jpg",
    "goicuon":  "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Homemade_spring_rolls_%287010969349%29.jpg/330px-Homemade_spring_rolls_%287010969349%29.jpg",
    "banhxeo":  "https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/Banh_Xeo_with_fish_sauce_and_vegetables.jpg/330px-Banh_Xeo_with_fish_sauce_and_vegetables.jpg",
    "flan":     "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/Cremecaramel.jpg/330px-Cremecaramel.jpg",
    "kem":      "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Ice_cream_with_whipped_cream%2C_chocolate_syrup%2C_and_a_wafer_%28cropped%29.jpg/330px-Ice_cream_with_whipped_cream%2C_chocolate_syrup%2C_and_a_wafer_%28cropped%29.jpg",
    "suachua":  "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Joghurt.jpg/330px-Joghurt.jpg",
    "lau":      "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Hot_Pot.jpg/330px-Hot_Pot.jpg",
    "lauthai":  "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Tom_yam_kung_maenam.jpg/330px-Tom_yam_kung_maenam.jpg",
    "ganuong":  "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Chicken_BBQ.jpg/330px-Chicken_BBQ.jpg",
    "tom":      "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Spicy_fried_King_Prawns.jpg/330px-Spicy_fried_King_Prawns.jpg",
    "dauhu":    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/Japanese_SilkyTofu_%28Kinugoshi_Tofu%29.JPG/330px-Japanese_SilkyTofu_%28Kinugoshi_Tofu%29.JPG",
    "pizza":    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Pizza-3007395.jpg/330px-Pizza-3007395.jpg",
    "spaghetti":"https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/Spaghettoni.jpg/330px-Spaghettoni.jpg",
    "mucpasta": "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Babbo-_Squid_Ink_pasta_with_green_chilis%2C_rock_shrimp_and_guanciale.jpg/330px-Babbo-_Squid_Ink_pasta_with_green_chilis%2C_rock_shrimp_and_guanciale.jpg",
    "store":    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Barbieri_-_ViaSophia25668.jpg/330px-Barbieri_-_ViaSophia25668.jpg",
    "bakery":   "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Belgium_2013_%2811620905224%29.jpg/330px-Belgium_2013_%2811620905224%29.jpg",
}


def img(key, w=640):
    # Lấy tên file gốc từ URL thumbnail rồi dựng link Special:FilePath?width=
    # (endpoint chính thức của Wikimedia cho ảnh kích thước tuỳ ý, hotlink ổn định).
    filename = re.search(r"/thumb/[^/]+/[^/]+/([^/]+)/\d+px-", IMG[key]).group(1)
    return f"https://commons.wikimedia.org/wiki/Special:FilePath/{filename}?width={w}"


# ---------------------------------------------------------------------------
# 1) DANH MỤC (categories)  ->  food.categoryId phải == category.id
# ---------------------------------------------------------------------------
CATEGORIES = [
    (1, "Cơm"),
    (2, "Phở & Bún"),
    (3, "Mì & Cháo"),
    (4, "Gà rán & Đồ ăn nhanh"),
    (5, "Đồ uống"),
    (6, "Ăn vặt"),
    (7, "Bánh mì"),
    (8, "Tráng miệng"),
    (9, "Lẩu & Nướng"),
    (10, "Hải sản"),
    (11, "Món chay"),
    (12, "Pizza & Mỳ Ý"),
]
CAT_NAME = dict(CATEGORIES)
categories = {str(cid): {"id": cid, "name": name, "active": True} for cid, name in CATEGORIES}

# ---------------------------------------------------------------------------
# 2) NHÀ HÀNG (restaurants) -> food.restaurantId phải == restaurant.id
# ---------------------------------------------------------------------------
RESTAURANTS = [
    (101, "Cơm Tấm Cô Tư", "Số 12 Triều Khúc, Thanh Xuân, Hà Nội", "0987654321", "comtam"),
    (102, "Phở & Bún Bò Gốc Đa", "Số 8 Nguyễn Trãi, Thanh Xuân, Hà Nội", "0978123456", "pho"),
    (103, "Quán Mì Trộn UTT", "Số 45 Triều Khúc, Thanh Xuân, Hà Nội", "0911222333", "mitron"),
    (104, "Gà Rán Chickita", "Số 120 Nguyễn Xiển, Thanh Xuân, Hà Nội", "0909888777", "garan"),
    (105, "Trà Chanh Bụi Phố", "Số 30 Triều Khúc, Thanh Xuân, Hà Nội", "0966555444", "trasua"),
    (106, "Bánh Mì & Chè Cô Hoa", "Số 25 Khương Đình, Thanh Xuân, Hà Nội", "0944333222", "banhmi"),
    (107, "Lẩu Nướng Hải Sản Biển Đông", "Số 88 Nguyễn Xiển, Thanh Xuân, Hà Nội", "0933121212", "lau"),
    (108, "Pizza Góc Phố Ý", "Số 5 Vũ Trọng Phụng, Thanh Xuân, Hà Nội", "0922343434", "pizza"),
    (109, "Cơm Chay An Lạc", "Số 60 Khương Trung, Thanh Xuân, Hà Nội", "0977565656", "dauhu"),
    (110, "Bún Đậu & Bún Riêu Cô Béo", "Ngõ 105 Triều Khúc, Thanh Xuân, Hà Nội", "0988676767", "bunrieu"),
]
REST_NAME = {rid: name for rid, name, *_ in RESTAURANTS}
restaurants = {
    str(rid): {"id": rid, "name": name, "address": addr, "phone": phone,
               "image": img(tag, 800), "active": True}
    for rid, name, addr, phone, tag in RESTAURANTS
}

# ---------------------------------------------------------------------------
# 3) MÓN ĂN (food)
#    (id, name, restId, catId, price[nghìn], sale[%], popular, img_key, mô tả)
# ---------------------------------------------------------------------------
FOODS = [
    # --- 101 Cơm Tấm Cô Tư (Cơm) ---
    (1001, "Cơm tấm sườn bì chả", 101, 1, 45, 0, True, "comtam", "Cơm tấm dẻo, sườn nướng, bì và chả trứng, chan mắm chua ngọt."),
    (1002, "Cơm tấm sườn trứng ốp la", 101, 1, 42, 10, False, "comtam", "Sườn nướng mật ong kèm trứng ốp lòng đào, dưa leo cà chua."),
    (1003, "Cơm gà xối mỡ", 101, 1, 42, 0, True, "comga", "Đùi gà chiên xối mỡ giòn rụm, cơm vàng ươm kèm gỏi."),
    (1004, "Cơm rang dưa bò", 101, 1, 38, 5, False, "comrang", "Cơm rang săn hạt cùng thịt bò và dưa chua đậm đà."),
    (1005, "Cơm rang hải sản", 101, 1, 48, 0, False, "comrang", "Cơm rang tôm mực, đậu Hà Lan, trứng thơm nức."),
    (1006, "Cơm sườn nướng mật ong", 101, 1, 50, 0, True, "comtam", "Miếng sườn cốt lết to ướp mật ong nướng than hoa."),

    # --- 102 Phở & Bún Bò Gốc Đa (Phở & Bún) ---
    (2001, "Phở bò tái lăn", 102, 2, 40, 0, True, "pho", "Nước dùng ninh xương 12 tiếng, bánh phở mềm, bò tái ngọt."),
    (2002, "Phở gà ta", 102, 2, 38, 0, False, "pho", "Phở gà ta thả vườn, thịt chắc ngọt, nước trong thanh."),
    (2003, "Bún bò Huế đặc biệt", 102, 2, 42, 0, True, "bunbohue", "Bún bò Huế cay nồng sả ớt, giò heo, chả cua, bò bắp."),
    (2004, "Bún chả Hà Nội", 102, 2, 40, 10, True, "buncha", "Chả viên và ba chỉ nướng than hoa, nước chấm chua ngọt."),
    (2005, "Hủ tiếu Nam Vang", 102, 2, 40, 0, False, "hutieu", "Hủ tiếu tôm thịt, gan, trứng cút, nước lèo ngọt thanh."),
    (2006, "Mì Quảng tôm thịt", 102, 2, 42, 0, False, "miquang", "Mì Quảng sợi vàng, tôm thịt, đậu phộng và bánh đa."),

    # --- 103 Quán Mì Trộn UTT (Mì & Cháo) ---
    (3001, "Mì trộn Indomie bò khô", 103, 3, 30, 0, False, "mitron", "Mì Indomie trộn bò khô, trứng lòng đào và rau xanh."),
    (3002, "Mì cay hải sản cấp độ 3", 103, 3, 50, 0, True, "micay", "Mì cay Hàn Quốc hải sản, phô mai kéo sợi, cay tê."),
    (3003, "Cháo sườn sụn", 103, 3, 25, 0, False, "chao", "Cháo sườn sánh mịn, sụn giòn, quẩy nóng và ruốc."),
    (3004, "Miến trộn gà xé", 103, 3, 35, 5, False, "mien", "Miến dong trộn gà xé, nấm hương, hành phi thơm."),
    (3005, "Mì xào giòn hải sản", 103, 3, 45, 0, False, "mitron", "Mì chiên giòn rưới sốt hải sản sánh đậm."),
    (3006, "Cháo gà ác tần thuốc bắc", 103, 3, 40, 0, False, "chao", "Cháo gà ác hầm thuốc bắc bổ dưỡng, ấm bụng."),

    # --- 104 Gà Rán Chickita (Gà rán & Đồ ăn nhanh) ---
    (4001, "Combo gà rán 2 miếng + nước", 104, 4, 55, 0, True, "garan", "2 miếng gà rán giòn rụm kèm 1 nước ngọt mát lạnh."),
    (4002, "Burger gà giòn", 104, 4, 40, 10, False, "burger", "Burger gà chiên giòn, rau tươi, sốt mayonnaise."),
    (4003, "Khoai tây chiên size L", 104, 4, 25, 0, False, "khoaitay", "Khoai tây chiên giòn vàng, rắc muối phô mai."),
    (4004, "Gà popcorn xốt phô mai", 104, 4, 35, 0, True, "popcorn", "Gà viên popcorn chấm xốt phô mai béo ngậy."),
    (4005, "Combo gà rán gia đình 6 miếng", 104, 4, 159, 10, True, "garan", "6 miếng gà, 2 khoai tây, 4 nước - tiệc cho cả nhà."),
    (4006, "Gà sốt cay Hàn Quốc", 104, 4, 60, 0, True, "garan", "Cánh gà chiên phủ sốt cay ngọt kiểu Hàn."),
    (4007, "Hotdog phô mai", 104, 4, 30, 0, False, "burger", "Xúc xích Đức kẹp bánh mì, phô mai chảy, sốt cà."),

    # --- 105 Trà Chanh Bụi Phố (Đồ uống) ---
    (5001, "Trà chanh giã tay", 105, 5, 15, 0, True, "trada", "Trà chanh giã tay mát lạnh, thơm vị chanh tươi."),
    (5002, "Trà sữa trân châu đường đen", 105, 5, 30, 0, True, "trasua", "Trà sữa béo thơm, trân châu đường đen dẻo dai."),
    (5003, "Cà phê sữa đá", 105, 5, 20, 0, False, "caphe", "Cà phê phin đậm đà, sữa đặc béo ngậy."),
    (5004, "Trà đào cam sả", 105, 5, 25, 10, False, "trada", "Trà đào cam sả thanh mát, miếng đào giòn ngọt."),
    (5005, "Sinh tố xoài", 105, 5, 28, 0, False, "sinhto", "Sinh tố xoài chín xay sánh mịn, ngọt tự nhiên."),
    (5006, "Cà phê muối", 105, 5, 25, 0, True, "caphe", "Cà phê muối kem mằn mặn béo, trend không lỗi mốt."),

    # --- 106 Bánh Mì & Chè Cô Hoa (Bánh mì / Ăn vặt / Tráng miệng) ---
    (7001, "Bánh mì pate trứng", 106, 7, 20, 0, True, "banhmi", "Bánh mì giòn vỏ, pate béo, trứng ốp và rau dưa."),
    (7002, "Bánh mì thịt nướng", 106, 7, 25, 0, False, "banhmi", "Bánh mì thịt nướng đậm vị, đồ chua, rau mùi."),
    (7003, "Bánh mì chả cá nóng", 106, 7, 25, 0, False, "banhmi", "Chả cá chiên nóng hổi, tương ớt và rau thơm."),
    (6001, "Xôi xéo hành phi", 106, 6, 18, 0, False, "xoi", "Xôi xéo dẻo thơm, đậu xanh, hành phi và ruốc."),
    (6002, "Bánh bao nhân thịt trứng", 106, 6, 15, 0, False, "banhbao", "Bánh bao nóng nhân thịt, trứng cút, mộc nhĩ."),
    (8001, "Chè khúc bạch hạnh nhân", 106, 8, 22, 0, False, "kem", "Chè khúc bạch mềm mịn, hạnh nhân, nhãn, đá bào."),
    (8002, "Bánh flan caramel", 106, 8, 15, 0, True, "flan", "Bánh flan mềm mịn, lớp caramel đắng nhẹ tan ngay."),
    (8003, "Sữa chua nếp cẩm", 106, 8, 18, 0, False, "suachua", "Sữa chua chua dịu ăn cùng nếp cẩm dẻo bùi."),
    (8004, "Kem ốc quế socola", 106, 8, 20, 0, False, "kem", "Kem tươi vị socola trên ốc quế giòn."),

    # --- 107 Lẩu Nướng Hải Sản Biển Đông (Lẩu & Nướng / Hải sản) ---
    (9001, "Lẩu bò mini 2 người", 107, 9, 135, 0, False, "lau", "Lẩu bò nhúng mini đủ rau nấm, hợp ngày se lạnh."),
    (9002, "Lẩu Thái tom yum hải sản", 107, 9, 159, 0, True, "lauthai", "Lẩu Thái chua cay, tôm mực ngao, sả lá chanh."),
    (9003, "Gà nướng mật ong", 107, 9, 90, 0, True, "ganuong", "Nửa con gà ướp mật ong nướng than vàng óng."),
    (9004, "Sườn nướng BBQ", 107, 9, 95, 10, False, "ganuong", "Sườn heo ướp sốt BBQ nướng mềm, ngọt khói."),
    (10001, "Tôm sú nướng muối ớt", 107, 10, 120, 0, True, "tom", "Tôm sú to nướng muối ớt cay thơm, chắc thịt."),
    (10002, "Mực một nắng nướng sa tế", 107, 10, 110, 0, False, "tom", "Mực một nắng nướng sa tế dai ngọt, đậm đà."),
    (10003, "Ngao hấp sả", 107, 10, 80, 0, False, "tom", "Ngao tươi hấp sả gừng, chấm muối tiêu chanh."),
    (10004, "Cua rang me", 107, 10, 180, 0, True, "tom", "Cua biển rang sốt me chua ngọt, thịt chắc."),

    # --- 108 Pizza Góc Phố Ý (Pizza & Mỳ Ý) ---
    (12001, "Pizza hải sản viền phô mai", 108, 12, 99, 0, True, "pizza", "Pizza hải sản, viền phô mai kéo sợi, sốt cà tươi."),
    (12002, "Pizza bò bằm sốt cà", 108, 12, 89, 10, False, "pizza", "Pizza bò bằm, hành tây, ớt chuông, phô mai mozzarella."),
    (12003, "Mỳ Ý sốt bò bằm", 108, 12, 65, 0, True, "spaghetti", "Spaghetti sốt bolognese bò bằm cà chua đậm đà."),
    (12004, "Mỳ Ý mực sốt mực", 108, 12, 75, 0, False, "mucpasta", "Mỳ Ý sốt mực đen kiểu Ý, mực tươi giòn ngọt."),
    (12005, "Pizza gà nướng BBQ", 108, 12, 95, 0, False, "pizza", "Pizza gà nướng sốt BBQ, hành tây, ngô ngọt."),

    # --- 109 Cơm Chay An Lạc (Món chay) ---
    (11001, "Cơm chay thập cẩm", 109, 11, 35, 0, True, "dauhu", "Cơm chay đầy đủ đậu hũ, rau củ kho, canh nấm."),
    (11002, "Đậu hũ non sốt nấm", 109, 11, 30, 0, False, "dauhu", "Đậu hũ non mềm mịn rưới sốt nấm đông cô."),
    (11003, "Gỏi cuốn chay", 109, 11, 25, 0, False, "goicuon", "Gỏi cuốn rau củ, đậu hũ, chấm tương đậu phộng."),
    (11004, "Nem chay rán giòn", 109, 11, 28, 0, False, "chagio", "Nem chay nhân miến, mộc nhĩ, cà rốt chiên giòn."),
    (11005, "Canh chua chay", 109, 11, 30, 0, False, "lauthai", "Canh chua chay dứa, đậu bắp, cà chua, me thanh."),

    # --- 110 Bún Đậu & Bún Riêu Cô Béo (Phở & Bún / Ăn vặt) ---
    (2007, "Bún riêu cua đồng", 110, 2, 38, 0, True, "bunrieu", "Bún riêu cua đồng, đậu rán, cà chua, mắm tôm tuỳ chọn."),
    (2008, "Bún đậu mắm tôm đầy đủ", 110, 2, 45, 0, True, "bunrieu", "Mẹt bún đậu đầy đủ: đậu rán, chả cốm, nem, lòng."),
    (6003, "Nem rán (chả giò)", 110, 6, 30, 0, True, "chagio", "Nem rán nhân thịt, miến, mộc nhĩ, vỏ giòn rụm."),
    (6004, "Gỏi cuốn tôm thịt", 110, 6, 28, 0, True, "goicuon", "Gỏi cuốn tôm thịt, bún, rau sống, chấm tương."),
    (6005, "Há cảo hấp", 110, 6, 28, 0, False, "hacao", "Há cảo tôm thịt hấp nóng, vỏ mỏng dai."),
    (6006, "Bánh xèo miền Trung", 110, 6, 35, 0, False, "banhxeo", "Bánh xèo giòn nhân tôm thịt giá, cuốn rau chấm mắm."),
]

food = {}
food_lookup = {}  # id -> (name, realPrice)
for fid, name, rid, cid, price, sale, popular, key, desc in FOODS:
    rp = real_price(price, sale)
    food[str(fid)] = {
        "id": fid,
        "name": name,
        "description": desc,
        "image": img(key, 640),
        "banner": img(key, 1280),
        "restaurantId": rid,
        "restaurantName": REST_NAME[rid],
        "categoryId": cid,
        "categoryName": CAT_NAME[cid],
        "price": price,
        "sale": sale,
        "popular": popular,
        "images": [{"url": img(key, 800)}],
    }
    food_lookup[fid] = (name, rp)

# ---------------------------------------------------------------------------
# 4) KHUYẾN MÃI (promotions) - key = mã code
# ---------------------------------------------------------------------------
PROMOS = [
    ("WELCOME20", "Giảm 20% cho đơn đầu tiên", 20, 80, 30, True),
    ("UTT10", "Ưu đãi sinh viên UTT - giảm 10%", 10, 50, 25, True),
    ("LUNCH25", "Giờ vàng ăn trưa - giảm 25%", 25, 120, 50, True),
    ("FREESHIP15", "Giảm 15% tối đa 20k", 15, 100, 20, True),
    ("HUNGRY30", "No bụng cuối tuần - giảm 30%", 30, 200, 70, True),
    ("SALE50OFF", "Flash Sale - giảm 50% (đã kết thúc)", 50, 150, 60, False),
]
promotions = {
    code: {"code": code, "title": title, "discountPercent": pct,
           "minOrderAmount": minamt, "maxDiscountAmount": maxamt, "active": active}
    for code, title, pct, minamt, maxamt, active in PROMOS
}

# ---------------------------------------------------------------------------
# 5) NGƯỜI DÙNG (users) - key = uid
#    Đây là HỒ SƠ trong DB, KHÔNG phải tài khoản Firebase Auth (xem README).
# ---------------------------------------------------------------------------
def user(uid, email, name, phone, role):
    return {
        "uid": uid, "email": email, "name": name, "phone": phone, "role": role,
        "active": True, "isActive": True,
        "admin": role == "admin", "isAdmin": role == "admin",
    }

USERS = [
    user("uid_admin_utt", "admin@admin.com", "Quản trị viên UTT", "0900000001", "admin"),
    user("uid_cust_an", "an.nguyen@gmail.com", "Nguyễn Văn An", "0901111111", "customer"),
    user("uid_cust_binh", "binh.tran@gmail.com", "Trần Thị Bình", "0902222222", "customer"),
    user("uid_cust_cuong", "cuong.le@gmail.com", "Lê Mạnh Cường", "0903333333", "customer"),
    user("uid_cust_dung", "dung.pham@gmail.com", "Phạm Thị Dung", "0904444444", "customer"),
    user("uid_cust_giang", "giang.do@gmail.com", "Đỗ Hương Giang", "0907777777", "customer"),
    user("uid_shipper_hoa", "hoa.shipper@gmail.com", "Vũ Văn Hòa", "0905555555", "shipper"),
    user("uid_shipper_khanh", "khanh.shipper@gmail.com", "Đỗ Quốc Khánh", "0906666666", "shipper"),
]
users = {u["uid"]: u for u in USERS}

# ---------------------------------------------------------------------------
# 6) SỔ ĐỊA CHỈ (addresses) - /addresses/{uid}/{addressId}
# ---------------------------------------------------------------------------
def addr(aid, label, name, phone, full, lat, lng, default):
    return {"id": aid, "label": label, "recipientName": name, "phone": phone,
            "fullAddress": full, "lat": lat, "lng": lng, "default": default}

addresses = {
    "uid_cust_an": {
        "5001": addr(5001, "Nhà", "Nguyễn Văn An", "0901111111",
                     "Số 54 Triều Khúc, Thanh Xuân, Hà Nội", 21.0008, 105.8005, True),
        "5002": addr(5002, "Công ty", "Nguyễn Văn An", "0901111111",
                     "Keangnam Landmark, Phạm Hùng, Nam Từ Liêm, Hà Nội", 21.0173, 105.7838, False),
    },
    "uid_cust_binh": {
        "5003": addr(5003, "Ký túc xá", "Trần Thị Bình", "0902222222",
                     "KTX Mỹ Đình, Nam Từ Liêm, Hà Nội", 21.0227, 105.7642, True),
    },
    "uid_cust_cuong": {
        "5004": addr(5004, "Nhà trọ", "Lê Mạnh Cường", "0903333333",
                     "Ngõ 178 Khương Đình, Thanh Xuân, Hà Nội", 20.9939, 105.8121, True),
    },
    "uid_cust_dung": {
        "5005": addr(5005, "Nhà", "Phạm Thị Dung", "0904444444",
                     "Số 2 Nguyễn Xiển, Thanh Xuân, Hà Nội", 20.9905, 105.8005, True),
    },
    "uid_cust_giang": {
        "5006": addr(5006, "Nhà", "Đỗ Hương Giang", "0907777777",
                     "Số 15 Vũ Trọng Phụng, Thanh Xuân, Hà Nội", 20.9959, 105.8009, True),
    },
}

# ---------------------------------------------------------------------------
# 7) ĐÁNH GIÁ (reviews) - key = id (ms)
# ---------------------------------------------------------------------------
def review(rid, order_id, email, rest_id, food_id, rating, comment):
    return {"id": rid, "orderId": order_id, "userEmail": email, "restaurantId": rest_id,
            "foodId": food_id, "rating": rating, "comment": comment, "createdAt": rid}

REVIEWS = [
    review(1779210000000, 1779120000000, "an.nguyen@gmail.com", 101, 1001, 5, "Cơm tấm ngon, sườn nướng thơm, giao nhanh!"),
    review(1779210100000, 1779120000000, "an.nguyen@gmail.com", 105, 5002, 4, "Trà sữa ổn, trân châu hơi ngọt một chút."),
    review(1779211000000, 1779150000000, "binh.tran@gmail.com", 104, 4001, 5, "Gà rán giòn, combo đáng tiền."),
    review(1779212000000, 1779180000000, "cuong.le@gmail.com", 102, 2003, 5, "Bún bò Huế đậm đà, cay vừa ăn, rất thích!"),
    review(1779213000000, 1779180000000, "cuong.le@gmail.com", 103, 3002, 3, "Mì cay ổn nhưng hơi ít hải sản."),
    review(1779214000000, 1779190000000, "dung.pham@gmail.com", 106, 7001, 5, "Bánh mì pate trứng ngon mà rẻ, ủng hộ tiếp."),
    review(1779215000000, 1779191000000, "giang.do@gmail.com", 108, 12001, 5, "Pizza hải sản viền phô mai cực đỉnh, nóng giòn."),
    review(1779216000000, 1779192000000, "giang.do@gmail.com", 107, 9002, 4, "Lẩu Thái chua cay chuẩn vị, phần hơi ít so với giá."),
    review(1779217000000, 1779193000000, "binh.tran@gmail.com", 110, 6003, 5, "Nem rán giòn tan, chấm nước mắm ngon."),
    review(1779218000000, 1779194000000, "an.nguyen@gmail.com", 109, 11001, 5, "Cơm chay thanh đạm, rau củ tươi, đóng gói đẹp."),
]
reviews = {str(r["id"]): r for r in REVIEWS}

# ---------------------------------------------------------------------------
# 8) ĐƠN HÀNG (booking) - key = id (ms)
# ---------------------------------------------------------------------------
PAYMENT_CASH, PAYMENT_ONLINE = 0, 1
UNPAID, PAID = 0, 1
S_NEW, S_PREPARING, S_DELIVERING, S_SUCCESS, S_CANCEL, S_FAIL = 0, 1, 2, 3, 4, 5


def foods_str(items):
    return "\n".join(
        f"- {food_lookup[fid][0]} ({food_lookup[fid][1]}{CURRENCY}) - {QTY_LABEL} {count}"
        for fid, count in items
    )


def amount_of(items):
    return sum(food_lookup[fid][1] * count for fid, count in items)


def order(oid, name, email, phone, address, items, payment, status,
          customer_id, paid=False, shipper_id=None, shipper_email=None,
          promo=None, discount=0, issue=None,
          dlat=0.0, dlng=0.0, slat=0.0, slng=0.0):
    original = amount_of(items)
    final = max(original - discount, 0)
    o = {
        "id": oid, "name": name, "email": email, "phone": phone, "address": address,
        "amount": final, "originalAmount": original, "discountAmount": discount,
        "promotionCode": promo,
        "foods": foods_str(items),
        "payment": payment,
        "paymentStatus": PAID if paid else UNPAID,
        "completed": status == S_SUCCESS,
        "status": status,
        "customerId": customer_id,
        "shipperId": shipper_id,
        "shipperEmail": shipper_email,
        "issueNote": issue,
        "deliveryLat": dlat, "deliveryLng": dlng,
        "shipperLat": slat, "shipperLng": slng,
    }
    if paid and payment == PAYMENT_ONLINE:
        o["paymentTransactionId"] = f"TXN{oid}"
    return o

ORDERS = [
    order(1779120000000, "Nguyễn Văn An", "an.nguyen@gmail.com", "0901111111",
          "Số 54 Triều Khúc, Thanh Xuân, Hà Nội",
          [(1001, 2), (5002, 1)], PAYMENT_ONLINE, S_SUCCESS, "uid_cust_an",
          paid=True, shipper_id="uid_shipper_hoa", shipper_email="hoa.shipper@gmail.com",
          promo="WELCOME20", discount=24, dlat=21.0008, dlng=105.8005,
          slat=21.0008, slng=105.8005),

    order(1779200000000, "Trần Thị Bình", "binh.tran@gmail.com", "0902222222",
          "KTX Mỹ Đình, Nam Từ Liêm, Hà Nội",
          [(4001, 1), (4003, 1)], PAYMENT_CASH, S_NEW, "uid_cust_binh",
          dlat=21.0227, dlng=105.7642),

    order(1779205000000, "Lê Mạnh Cường", "cuong.le@gmail.com", "0903333333",
          "Ngõ 178 Khương Đình, Thanh Xuân, Hà Nội",
          [(2003, 1), (3002, 1)], PAYMENT_CASH, S_PREPARING, "uid_cust_cuong",
          dlat=20.9939, dlng=105.8121),

    order(1779206000000, "Phạm Thị Dung", "dung.pham@gmail.com", "0904444444",
          "Số 2 Nguyễn Xiển, Thanh Xuân, Hà Nội",
          [(7001, 2), (5001, 2)], PAYMENT_ONLINE, S_DELIVERING, "uid_cust_dung",
          paid=True, shipper_id="uid_shipper_khanh", shipper_email="khanh.shipper@gmail.com",
          dlat=20.9905, dlng=105.8005, slat=20.9950, slng=105.8050),

    order(1779150000000, "Nguyễn Văn An", "an.nguyen@gmail.com", "0901111111",
          "Số 54 Triều Khúc, Thanh Xuân, Hà Nội",
          [(4005, 1)], PAYMENT_CASH, S_CANCEL, "uid_cust_an",
          dlat=21.0008, dlng=105.8005),

    order(1779180000000, "Trần Thị Bình", "binh.tran@gmail.com", "0902222222",
          "KTX Mỹ Đình, Nam Từ Liêm, Hà Nội",
          [(1003, 1), (5004, 1)], PAYMENT_CASH, S_SUCCESS, "uid_cust_binh",
          paid=True, shipper_id="uid_shipper_hoa", shipper_email="hoa.shipper@gmail.com",
          dlat=21.0227, dlng=105.7642, slat=21.0227, slng=105.7642),

    order(1779160000000, "Lê Mạnh Cường", "cuong.le@gmail.com", "0903333333",
          "Ngõ 178 Khương Đình, Thanh Xuân, Hà Nội",
          [(2001, 1)], PAYMENT_CASH, S_FAIL, "uid_cust_cuong",
          shipper_id="uid_shipper_khanh", shipper_email="khanh.shipper@gmail.com",
          issue="Khách không nghe máy, không liên hệ được sau 15 phút.",
          dlat=20.9939, dlng=105.8121),

    order(1779208000000, "Phạm Thị Dung", "dung.pham@gmail.com", "0904444444",
          "Số 2 Nguyễn Xiển, Thanh Xuân, Hà Nội",
          [(3001, 1), (8002, 1), (5003, 1)], PAYMENT_ONLINE, S_NEW, "uid_cust_dung",
          paid=True, dlat=20.9905, dlng=105.8005),

    # Đơn lẩu/hải sản lớn, dùng voucher cuối tuần, đã giao
    order(1779191000000, "Đỗ Hương Giang", "giang.do@gmail.com", "0907777777",
          "Số 15 Vũ Trọng Phụng, Thanh Xuân, Hà Nội",
          [(9002, 1), (10001, 1)], PAYMENT_ONLINE, S_SUCCESS, "uid_cust_giang",
          paid=True, shipper_id="uid_shipper_hoa", shipper_email="hoa.shipper@gmail.com",
          promo="HUNGRY30", discount=70, dlat=20.9959, dlng=105.8009,
          slat=20.9959, slng=105.8009),

    # Đơn pizza đang chờ shipper nhận
    order(1779209000000, "Đỗ Hương Giang", "giang.do@gmail.com", "0907777777",
          "Số 15 Vũ Trọng Phụng, Thanh Xuân, Hà Nội",
          [(12001, 1), (12003, 1)], PAYMENT_CASH, S_PREPARING, "uid_cust_giang",
          dlat=20.9959, dlng=105.8009),

    # Đơn chay đang giao
    order(1779209500000, "Nguyễn Văn An", "an.nguyen@gmail.com", "0901111111",
          "Số 54 Triều Khúc, Thanh Xuân, Hà Nội",
          [(11001, 2), (8003, 1)], PAYMENT_CASH, S_DELIVERING, "uid_cust_an",
          shipper_id="uid_shipper_khanh", shipper_email="khanh.shipper@gmail.com",
          dlat=21.0008, dlng=105.8005, slat=21.0020, slng=105.8030),

    # Đơn bún đậu mới, tiền mặt
    order(1779209800000, "Trần Thị Bình", "binh.tran@gmail.com", "0902222222",
          "KTX Mỹ Đình, Nam Từ Liêm, Hà Nội",
          [(2008, 1), (6003, 1), (5001, 1)], PAYMENT_CASH, S_NEW, "uid_cust_binh",
          dlat=21.0227, dlng=105.7642),
]
booking = {str(o["id"]): o for o in ORDERS}

# ---------------------------------------------------------------------------
# 9) PHẢN HỒI / LIÊN HỆ (feedback) - key = id (ms)
# ---------------------------------------------------------------------------
def feedback_item(fid, name, phone, email, comment, rating, rest_id, food_id):
    return {"id": fid, "name": name, "phone": phone, "email": email, "comment": comment,
            "rating": rating, "restaurantId": rest_id, "foodId": food_id, "createdAt": fid}

FEEDBACK = [
    feedback_item(1779100000000, "Nguyễn Văn An", "0901111111", "an.nguyen@gmail.com",
                  "App dễ dùng, đặt món nhanh. Mong có thêm nhiều quán gần trường!", 5, 0, 0),
    feedback_item(1779101000000, "Trần Thị Bình", "0902222222", "binh.tran@gmail.com",
                  "Giao hàng nhanh, shipper thân thiện.", 5, 104, 4001),
    feedback_item(1779102000000, "Lê Mạnh Cường", "0903333333", "cuong.le@gmail.com",
                  "Đôi lúc app load hơi chậm, mong cải thiện thêm.", 4, 0, 0),
    feedback_item(1779103000000, "Phạm Thị Dung", "0904444444", "dung.pham@gmail.com",
                  "Đồ ăn ngon, đóng gói cẩn thận. Sẽ ủng hộ tiếp.", 5, 102, 2003),
    feedback_item(1779104000000, "Đỗ Hương Giang", "0907777777", "giang.do@gmail.com",
                  "Pizza và lẩu ngon, mong có thêm mã giảm giá cho đơn lớn.", 5, 108, 12001),
    feedback_item(1779105000000, "Vũ Văn Hòa", "0905555555", "hoa.shipper@gmail.com",
                  "Là shipper, mong app hiển thị đường đi rõ hơn ở vùng ngõ nhỏ.", 4, 0, 0),
]
feedback = {str(f["id"]): f for f in FEEDBACK}

# ---------------------------------------------------------------------------
# Xuất file
# ---------------------------------------------------------------------------
NODES = {
    "categories": categories,
    "restaurants": restaurants,
    "food": food,
    "promotions": promotions,
    "users": users,
    "addresses": addresses,
    "reviews": reviews,
    "booking": booking,
    "feedback": feedback,
}


def dump(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


for node_name, data in NODES.items():
    dump(os.path.join(NODES_DIR, f"{node_name}.json"), data)
dump(os.path.join(OUT_DIR, "firebase_seed.json"), NODES)

print("Đã sinh dữ liệu fake:")
print(f"  - {len(categories)} danh mục, {len(restaurants)} nhà hàng, {len(food)} món ăn")
print(f"  - {len(promotions)} khuyến mãi, {len(users)} người dùng")
print(f"  - {len(booking)} đơn hàng, {len(reviews)} đánh giá, {len(feedback)} phản hồi")
print(f"  - File gộp: firebase_seed.json | File theo node: nodes/*.json")
