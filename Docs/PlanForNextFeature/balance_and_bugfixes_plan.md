# Kế hoạch cân bằng & Sửa lỗi (Balance & Bugfixes Plan)

## 1. Mục tiêu công việc
Khắc phục triệt để và cân bằng 3 vấn đề gameplay theo yêu cầu:
1. **Cân bằng độ xuyên Sniper Rifle (SR)**: Súng ngắm quá mạnh ở Lv.1 và quá áp đảo ở các cấp sao cao hơn do số lần xuyên quá nhiều và hao hụt sát thương quá thấp.
2. **Khắc phục trạm nâng cấp nhân vật bị roll lại lựa chọn mỗi khi mở lên đóng lại**: Bảng nâng cấp (`CharacterUpgradeUI`) mỗi lần mở lên đều gọi `rollBuffs`, khiến người chơi có thể reroll vô hạn miễn phí chỉ bằng cách đóng/mở. Cần giữ nguyên 3 lựa chọn của trạm.
3. **Sửa lỗi vũ khí "ma" khi nhặt đổi súng và ấn phím Q**: Khi nhặt đổi súng thay thế súng đang cầm, `WeaponManager.previousWeapon` vẫn giữ tham chiếu tới khẩu súng cũ đã bị ném ra ngoài, dẫn tới việc bấm phím [Q] chuyển sang cây súng "ma" đó và chỉ bắn được đúng 1 viên.

---

## 2. Phân tích nguyên nhân & Giải pháp chi tiết

### Vấn đề 1: Cân bằng độ xuyên Sniper Rifle (`SniperRifle.java`)
- **Nguyên nhân gốc rễ**:
  - `BASE_PIERCES = 2`: Ở Lv.1, đạn bắn xuyên qua 2 mục tiêu và trúng mục tiêu thứ 3 (tối đa 3 mục tiêu). Với sát thương cơ bản 95 và tỉ lệ crit 25%, 1 viên đạn dọn sạch quái theo hàng ngay từ stage 1.
  - `getMaxPierces() = BASE_PIERCES + (accuracyStars - 1)`: Mỗi sao +1 lần xuyên, ở 5★ xuyên qua 6 mục tiêu.
  - `getPierceDamageFalloff() = Math.max(0.08, BASE_FALLOFF - (accuracyStars - 1) * 0.05)` với `BASE_FALLOFF = 0.25`: Ở Lv.1 chỉ giảm 25% sát thương, lên 5★ chỉ giảm 8% sát thương mỗi lần xuyên!
- **Giải pháp cân bằng**:
  - Đặt `BASE_PIERCES = 1`: Ở Lv.1 (1★), đạn chỉ xuyên tối đa 1 lần (trúng mục tiêu 1, xuyên qua trúng mục tiêu 2 thì dừng; tối đa 2 mục tiêu).
  - Khống chế thăng cấp xuyên phá: `public int getMaxPierces() { return BASE_PIERCES + (accuracyStars - 1) / 2; }`
    - 1★: 1 lần xuyên (trúng tối đa 2 mục tiêu).
    - 2★: 1 lần xuyên (trúng tối đa 2 mục tiêu, tối ưu falloff).
    - 3★: 2 lần xuyên (trúng tối đa 3 mục tiêu).
    - 4★: 2 lần xuyên (trúng tối đa 3 mục tiêu).
    - 5★: 3 lần xuyên (trúng tối đa 4 mục tiêu - khống chế trần hợp lý).
  - Tăng độ sụt giảm sát thương mỗi lần xuyên phá:
    - Đặt `BASE_FALLOFF = 0.40` (mục tiêu thứ 2 chỉ nhận 60% DMG ở 1★).
    - `getPierceDamageFalloff() = Math.max(0.20, BASE_FALLOFF - (accuracyStars - 1) * 0.05)`:
      * 1★: -40%
      * 2★: -35%
      * 3★: -30%
      * 4★: -25%
      * 5★: -20% (vẫn duy trì giảm tối thiểu 20%, không để giảm chỉ 8%).
  - Bổ sung helper preview `getPreviewMaxPierces()` và `getPreviewPierceFalloffPercent()` để `WeaponUpgradeModalUI` hiển thị chính xác.
  - Cập nhật các bài test trong `WeaponSystemTest.java`.

---

### Vấn đề 2: Giữ nguyên lựa chọn Buff tại Trạm Nâng Cấp (`CharacterUpgradeStation.java` & `CharacterUpgradeUI.java`)
- **Nguyên nhân gốc rễ**:
  - `CharacterUpgradeUI.open(station, buffManager)` luôn gọi `station.rollBuffs(buffManager)` vô điều kiện mỗi lần mở giao diện.
- **Giải pháp**:
  - Trong `CharacterUpgradeStation.java`: Bổ sung phương thức `ensureBuffsRolled(PlayerBuffManager buffManager)`:
    ```java
    public void ensureBuffsRolled(PlayerBuffManager buffManager) {
        if (offeredBuffs == null || offeredBuffs.isEmpty()) {
            rollBuffs(buffManager);
        }
    }
    ```
  - Trong `CharacterUpgradeUI.java`: Đổi dòng gọi từ `station.rollBuffs(buffManager)` thành `station.ensureBuffsRolled(buffManager)`.
  - 3 buff tại trạm được giữ nguyên khi đóng/mở. Chỉ đổi khi người chơi mua hết buff hoặc bước sang màn mới.

---

### Vấn đề 3: Khắc phục lỗi vũ khí "ma" khi nhặt đổi súng và ấn Q (`WeaponManager.java`)
- **Nguyên nhân gốc rễ**:
  - Trong `WeaponManager.equipWeapon(weapon)`: Khi cả 2 slot chính đã có súng, hệ thống thay thế khẩu súng ở slot đang cầm (ví dụ Slot 1) bằng súng mới.
  - Tuy nhiên, code gán: `previousWeapon = activeWeapon; activeWeapon = weapon; return replacedWeapon;`.
  - Lúc này `previousWeapon` trỏ trực tiếp vào khẩu súng cũ `replacedWeapon` đã bị vứt ra đất/hòm!
  - Khi người chơi bấm phím [Q] (`quickSwap()`), hệ thống chuyển `activeWeapon = previousWeapon`, tức người chơi cầm khẩu súng đã bị vứt ra ngoài. Vì súng này không còn trong `weaponSlots` và không liên kết với kho đạn dự trữ, nó chỉ bắn được 1 viên đạn rồi kẹt cứng.
- **Giải pháp**:
  - Trong `WeaponManager.equipWeapon(weapon)`:
    - Nếu súng cũ bị thay thế (`replacedWeapon != null`), `previousWeapon` KHÔNG ĐƯỢC trỏ vào `replacedWeapon`.
    - Thay vào đó, `previousWeapon` sẽ trỏ tới súng ở slot chính còn lại (Slot 2 nếu vừa nhặt vào Slot 1, hoặc Slot 1 nếu vừa nhặt vào Slot 2), hoặc Slot 3 (Pistol).
  - Trong `WeaponManager.quickSwap()`:
    - Bổ sung kiểm tra hợp lệ: `if (previousWeapon != null && !weaponSlots.containsValue(previousWeapon)) { previousWeapon = null; }`.
    - Nếu `previousWeapon` không hợp lệ hoặc `previousWeapon == activeWeapon`: Fallback chuyển đổi thông minh giữa Slot 1 và Slot 2 (hoặc Slot 3 Pistol). Đảm bảo 100% không bao giờ chuyển sang vũ khí ma!
  - Trong `WeaponManager.removeWeapon(int slotIndex)`:
    - Dọn dẹp an toàn: nếu `removed == previousWeapon`, đặt `previousWeapon = null`.

---

## 3. Các bước thực hiện
1. Cập nhật `SniperRifle.java`: Cân bằng chỉ số `BASE_PIERCES`, `BASE_FALLOFF`, công thức `getMaxPierces()`, `getPierceDamageFalloff()`.
2. Cập nhật `WeaponUpgradeModalUI.java`: Đồng bộ hiển thị preview nâng cấp sao sức xuyên.
3. Cập nhật `CharacterUpgradeStation.java` & `CharacterUpgradeUI.java`: Hiện thực `ensureBuffsRolled` để không bị roll lại khi đóng mở.
4. Cập nhật `WeaponManager.java`: Quản lý `previousWeapon` và `quickSwap()` hợp lệ, loại bỏ hoàn toàn vũ khí ma.
5. Cập nhật và bổ sung Unit Test trong `WeaponSystemTest.java` & `CharacterBuffSystemTest.java`.
6. Chạy `mvn clean test` kiểm tra 244+ unit tests PASS 100%.
7. Đóng gói lại game: `mvn clean package -DskipTests` và chạy `update_zips.ps1`.
8. Cập nhật tài liệu `Docs/Update/26-10-27_01.md`.
