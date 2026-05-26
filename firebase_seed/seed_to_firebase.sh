#!/usr/bin/env bash
# Đẩy dữ liệu fake lên Firebase Realtime Database bằng Firebase CLI.
# Dùng "database:update" (PATCH) nên KHÔNG xoá các node khác và gộp thêm vào node đang có.
#
# Yêu cầu:
#   1. Cài Firebase CLI:   npm install -g firebase-tools
#   2. Đăng nhập:          firebase login
#   3. Tài khoản phải có quyền trên project order-food-utt
#
# Chạy:   bash seed_to_firebase.sh
set -euo pipefail

PROJECT="order-food-utt"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/nodes"

if ! command -v firebase >/dev/null 2>&1; then
  echo "Chưa có Firebase CLI. Cài bằng: npm install -g firebase-tools" >&2
  exit 1
fi

for node in categories restaurants food promotions users addresses reviews booking feedback; do
  echo ">> Đang nạp /$node ..."
  firebase database:update "/$node" "$DIR/$node.json" --project "$PROJECT" --confirm
done

echo "Hoàn tất. Mở app để kiểm tra dữ liệu."
