package com.groupproject.game.entity.weapon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameFontManager;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.entity.projectile.ProjectileManager;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the operative's firearm arsenal, hotkey cycling, and ammunition HUD rendering.
 *
 * <p>WHY: Decouples weapon switching and inventory storage from the player's core locomotion
 * logic. Supports easy expansion for additional weapon slots (e.g. primary assault rifles on slot 1,
 * shotguns on slot 2) while keeping existing weapon mechanics untouched.
 */
public class WeaponManager {

    private final Map<Integer, Weapon> weaponSlots = new HashMap<>();
    private final List<MeleeSlash> activeSlashes = new CopyOnWriteArrayList<>();
    private final com.groupproject.game.entity.item.AmmoInventory ammoInventory = new com.groupproject.game.entity.item.AmmoInventory();

    private Weapon activeWeapon;
    private Weapon previousWeapon;

    private static void playWeaponDraw(Weapon weapon) {
        if (weapon == null) {
            return;
        }

        SoundEffect drawEffect;
        if (weapon instanceof Pistol) {
            drawEffect = SoundEffect.GUN_PISTOL_DRAW;
        } else if (weapon instanceof AssaultRifle) {
            drawEffect = SoundEffect.GUN_RIFLE_DRAW;
        } else if (weapon instanceof SemiCarbineRifle) {
            drawEffect = SoundEffect.GUN_SCR_DRAW;
        } else if (weapon instanceof Shotgun) {
            drawEffect = SoundEffect.GUN_SHOTGUN_DRAW;
        } else if (weapon instanceof SemiAutoShotgun) {
            drawEffect = SoundEffect.GUN_SAS_DRAW;
        } else if (weapon instanceof SMG) {
            drawEffect = SoundEffect.GUN_SMG_DRAW;
        } else if (weapon instanceof SniperRifle) {
            drawEffect = SoundEffect.GUN_SNIPER_DRAW;
        } else {
            return;
        }
        GameSoundManager.play(drawEffect);
    }

    public WeaponManager() {
        // Khởi đầu người chơi chỉ có súng lục phụ (Slot 3) và dao găm (Slot 4)
        // Chưa có vũ khí chính (Slot 1 và Slot 2 sẽ được mở khóa ngẫu nhiên khi nhặt hòm)
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();

        addWeapon(pistol);
        addWeapon(knife);

        // Mặc định ban đầu kích hoạt ô số 3 (Pistol) và phụ là ô số 4 (Knife)
        this.activeWeapon = pistol;
        this.previousWeapon = knife;
    }

    /**
     * Resets loadout to starting setup (Pistol + Knife only) and resets ammo reserves.
     */
    public void reset() {
        ammoInventory.reset();
        weaponSlots.clear();
        activeSlashes.clear();

        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();

        addWeapon(pistol);
        addWeapon(knife);

        this.activeWeapon = pistol;
        this.previousWeapon = knife;
    }

    /**
     * Registers and immediately equips a newly collected primary weapon.
     * Tự động ưu tiên xếp vào Slot 1 nếu Slot 1 đang trống, hoặc Slot 2 nếu Slot 1 đã có súng.
     * Nếu cả 2 slot chính đều đã đầy, thay thế súng đang cầm (hoặc slot 1) và trả về súng cũ để rơi ra đất.
     *
     * @param weapon Vũ khí mới cần trang bị.
     * @return Khẩu súng cũ bị thay thế nếu cả 2 slot đều đã có súng; {@code null} nếu lấp vào slot trống.
     */
    public Weapon equipWeapon(Weapon weapon) {
        if (weapon == null) {
            return null;
        }

        Weapon replacedWeapon = null;
        // Nếu là vũ khí chính (không phải súng lục slot 3 hay dao slot 4)
        if (!weapon.isMelee() && weapon.getSlotIndex() != 3 && weapon.getSlotIndex() != 4) {
            if (!weaponSlots.containsKey(1)) {
                weapon.setSlotIndex(1);
            } else if (!weaponSlots.containsKey(2)) {
                weapon.setSlotIndex(2);
            } else {
                // Cả Slot 1 và Slot 2 đều đã có vũ khí chính:
                // Thay thế vào slot đang cầm (nếu đang cầm slot 1 hoặc slot 2), ngược lại ưu tiên slot 1
                int targetSlot = (activeWeapon != null && activeWeapon.getSlotIndex() == 2) ? 2 : 1;
                replacedWeapon = weaponSlots.get(targetSlot);
                weapon.setSlotIndex(targetSlot);
            }
        }
        addWeapon(weapon);

        // Quản lý previousWeapon và activeWeapon:
        // Nếu khẩu súng cũ đang cầm bị thay thế và ném ra đất (activeWeapon == replacedWeapon),
        // thì previousWeapon TUYỆT ĐỐI KHÔNG ĐƯỢC trỏ vào replacedWeapon (để tránh vũ khí ma)!
        // Thay vào đó, previousWeapon sẽ trỏ vào slot chính còn lại (hoặc Pistol slot 3).
        if (activeWeapon != null && activeWeapon != replacedWeapon && weaponSlots.containsValue(activeWeapon)) {
            previousWeapon = activeWeapon;
        } else {
            int otherSlot = (weapon.getSlotIndex() == 1) ? 2 : 1;
            if (weaponSlots.containsKey(otherSlot)) {
                previousWeapon = weaponSlots.get(otherSlot);
            } else if (weaponSlots.containsKey(3)) {
                previousWeapon = weaponSlots.get(3);
            } else {
                previousWeapon = weaponSlots.get(4);
            }
        }

        // Đảm bảo previousWeapon không bao giờ trùng với súng cũ vừa bị thay thế
        if (previousWeapon == replacedWeapon || (previousWeapon != null && !weaponSlots.containsValue(previousWeapon))) {
            int otherSlot = (weapon.getSlotIndex() == 1) ? 2 : 1;
            if (weaponSlots.containsKey(otherSlot)) {
                previousWeapon = weaponSlots.get(otherSlot);
            } else {
                previousWeapon = weaponSlots.get(3);
            }
        }

        if (activeWeapon != null) {
            if (activeWeapon == replacedWeapon) {
                activeWeapon.cancelReload();
            } else {
                activeWeapon.pauseReload();
            }
        }
        activeWeapon = weapon;
        activeWeapon.resumeReload();
        playWeaponDraw(activeWeapon);
        return replacedWeapon;
    }

    /**
     * Hoán đổi vị trí của 2 vũ khí chính giữa Slot [1] và Slot [2].
     *
     * <p>WHY: Cho phép người chơi tự do sắp xếp vũ khí yêu thích vào ô số 1 hoặc ô số 2
     * thông qua giao diện Balo (Tab) hoặc phím tắt hoán đổi.
     *
     * @return {@code true} nếu hoán đổi thành công; {@code false} nếu cả 2 slot đều trống.
     */
    public boolean swapPrimarySlots() {
        Weapon w1 = weaponSlots.get(1);
        Weapon w2 = weaponSlots.get(2);

        if (w1 == null && w2 == null) {
            return false;
        }

        weaponSlots.remove(1);
        weaponSlots.remove(2);

        if (w1 != null) {
            w1.setSlotIndex(2);
            weaponSlots.put(2, w1);
        }
        if (w2 != null) {
            w2.setSlotIndex(1);
            weaponSlots.put(1, w2);
        }

        return true;
    }

    private com.groupproject.game.entity.buff.PlayerBuffManager boundBuffManager;

    /**
     * Registers a weapon into its assigned slot and binds player's ammo inventory and buff manager.
     */
    public void addWeapon(Weapon weapon) {
        if (weapon != null) {
            weapon.bindAmmoInventory(ammoInventory);
            if (boundBuffManager != null) {
                weapon.bindBuffManager(boundBuffManager);
                boundBuffManager.applyExtendedMagazineToWeapon(weapon);
            }
            weaponSlots.put(weapon.getSlotIndex(), weapon);
        }
    }

    /**
     * Binds the player's buff manager to all existing and future weapons.
     */
    public void bindPlayerBuffManager(com.groupproject.game.entity.buff.PlayerBuffManager bm) {
        this.boundBuffManager = bm;
        for (Weapon w : weaponSlots.values()) {
            if (w != null) {
                w.bindBuffManager(bm);
                if (bm != null) {
                    bm.applyExtendedMagazineToWeapon(w);
                }
            }
        }
        fallbackKnife.bindBuffManager(bm);
    }

    /**
     * Switches the active weapon using its direct slot number (e.g. 3 or 4).
     */
    public void selectSlot(int slotIndex) {
        Weapon selected = weaponSlots.get(slotIndex);
        if (selected != null && selected != activeWeapon) {
            // WHY: Khi đổi súng, quá trình nạp đạn của vũ khí cũ ĐƯỢC TẠM DỪNG (Pause Reload),
            // bảo lưu tiến trình thời gian nạp còn lại và ngắt âm thanh nạp đạn.
            if (activeWeapon != null) {
                activeWeapon.pauseReload();
            }
            previousWeapon = activeWeapon;
            activeWeapon = selected;
            // Nếu vũ khí vừa được chọn trước đó đang nạp dở, tự động TIẾP TỤC nạp tiếp (Resume Reload)
            activeWeapon.resumeReload();
            playWeaponDraw(activeWeapon);
        }
    }

    public void switchWeapon(int slotIndex) {
        selectSlot(slotIndex);
    }

    /**
     * Quick-swaps between the current weapon and the previously equipped weapon (Hotkey [Q]).
     *
     * <p>WHY: Cơ chế chuyển đổi vũ khí tức thời khi bấm phím [Q]. Kiểm tra nghiêm ngặt
     * để đảm bảo vũ khí được chuyển tới BẮT BUỘC phải đang nằm trong túi đồ người chơi (weaponSlots),
     * triệt tiêu hoàn toàn hiện tượng chuyển sang vũ khí "ma" vừa bị ném ra đất.
     */
    public void quickSwap() {
        // 1. Kiểm tra tính hợp lệ của previousWeapon: phải còn nằm trong kho đồ và khác activeWeapon
        if (previousWeapon != null && !weaponSlots.containsValue(previousWeapon)) {
            previousWeapon = null;
        }

        if (previousWeapon != null && previousWeapon != activeWeapon) {
            // WHY: Tạm dừng nạp đạn vũ khí hiện tại và tiếp tục nạp vũ khí được tráo sang (nếu có)
            if (activeWeapon != null) {
                activeWeapon.pauseReload();
            }
            Weapon temp = activeWeapon;
            activeWeapon = previousWeapon;
            previousWeapon = temp;
            activeWeapon.resumeReload();
            playWeaponDraw(activeWeapon);
        } else {
            // 2. Cơ chế phục hồi thông minh: Nếu previousWeapon không còn hợp lệ,
            // ưu tiên đổi giữa Slot 1 và Slot 2 (hoặc súng lục Slot 3)
            int currentSlot = (activeWeapon != null) ? activeWeapon.getSlotIndex() : 1;
            int targetSlot = (currentSlot == 1) ? 2 : 1;
            Weapon fallback = weaponSlots.get(targetSlot);
            if (fallback == null) {
                fallback = weaponSlots.get(3); // Súng lục Slot 3
            }
            if (fallback == null || fallback == activeWeapon) {
                fallback = weaponSlots.get(4); // Dao găm Slot 4
            }
            if (fallback != null && fallback != activeWeapon) {
                selectSlot(fallback.getSlotIndex());
            }
        }
    }
    private final CombatKnife fallbackKnife = new CombatKnife();

    /**
     * Lấy vũ khí cận chiến (thường là Dao găm ở Slot 4).
     */
    public Weapon getMeleeWeapon() {
        Weapon knife = weaponSlots.get(4);
        if (knife != null && knife.isMelee()) {
            return knife;
        }
        for (Weapon w : weaponSlots.values()) {
            if (w.isMelee()) {
                return w;
            }
        }
        return fallbackKnife;
    }

    /**
     * Thực hiện đòn chém dao cận chiến tức thì (Quick-Melee Slash) bằng chuột phải,
     * độc lập với vũ khí hiện đang cầm trên tay (súng trường, shotgun, súng ngắn...).
     * Giúp người chơi phản xạ nhanh và đẩy lùi kẻ địch cận chiến áp sát.
     *
     * @return true nếu thực hiện chém thành công, false nếu dao đang trong thời gian hồi (cooldown).
     */
    public boolean quickMelee(double originX, double originY, double aimAngle, ProjectileManager pm) {
        Weapon knife = getMeleeWeapon();
        return knife.attack(originX, originY, aimAngle, pm, activeSlashes);
    }

    /**
     * Triggers attack on the currently active weapon.
     */
    public boolean attack(double originX, double originY, double aimAngle, ProjectileManager pm) {
        if (activeWeapon != null) {
            if (activeWeapon.canPlayEmptyShot()) {
                activeWeapon.playEmptyShot();
            }
            return activeWeapon.attack(originX, originY, aimAngle, pm, activeSlashes);
        }
        return false;
    }

    /**
     * Triggers magazine reload on the currently active weapon.
     */
    public void reload() {
        if (activeWeapon != null) {
            activeWeapon.reload();
        }
    }

    /**
     * Updates weapon cooldowns, reload timers, and active melee strike animations.
     */
    public void update(double deltaTime) {
        // Cập nhật hồi chiêu cho các vũ khí (cooldownTimer)
        // Nhưng CHỈ vũ khí đang cầm trên tay (activeWeapon) mới được phép nạp đạn.
        // Mọi vũ khí phụ cất trong túi đều bị hủy nạp đạn ngay lập tức, không được nạp ngầm.
        for (Weapon w : weaponSlots.values()) {
            if (w != null) {
                if (w != activeWeapon && w.isReloading()) {
                    w.pauseReload();
                }
                w.update(deltaTime);
            }
        }
        fallbackKnife.update(deltaTime);

        // Update active melee slashes
        for (MeleeSlash slash : activeSlashes) {
            slash.update(deltaTime);
        }
        activeSlashes.removeIf(s -> !s.isActive());
    }

    /**
     * Vẽ vũ khí đang trang bị (nòng súng, lưỡi dao) ở layer DƯỚI nhân vật theo yêu cầu thiết kế.
     *
     * @param g       Đối tượng đồ họa Graphics2D.
     * @param camera  Camera quan sát.
     * @param originX Tọa độ X tâm nhân vật.
     * @param originY Tọa độ Y tâm nhân vật.
     * @param aimAngle Góc ngắm chuột.
     */
    public void renderWeapon(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        if (activeWeapon != null) {
            activeWeapon.render(g, camera, originX, originY, aimAngle);
        }
    }

    /**
     * Vẽ toàn bộ hiệu ứng chém dao cận chiến (Melee Slash VFX) ở layer TRÊN nhân vật.
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param camera Camera quan sát.
     */
    public void renderSlashes(Graphics2D g, Camera camera) {
        for (MeleeSlash slash : activeSlashes) {
            slash.render(g, camera);
        }
    }

    /**
     * Renders equipped weapon on operative and all active melee slash animations.
     * (Giữ nguyên để tương thích ngược)
     */
    public void render(Graphics2D g, Camera camera, double originX, double originY, double aimAngle) {
        renderWeapon(g, camera, originX, originY, aimAngle);
        renderSlashes(g, camera);
    }

    /**
     * Renders a dedicated tactical Weapon HUD at the bottom right corner of the screen.
     */
    /**
     * Renders a dedicated tactical Weapon & Equipment HUD at the bottom right corner of the screen.
     * Cung cấp thông tin trực quan, gọn gàng và dễ hiểu:
     * 1. Tên súng đang cầm kèm Cấp độ (Level).
     * 2. Trạng thái đạn của vũ khí (băng / nạp đạn).
     * 3. Hiển thị rõ số đạn của tất cả các loại trong người (AR, SMG, Shotgun).
     * 4. Các vị trí nút trang bị [1] [2] [3] [4] làm nổi bật vũ khí đang cầm trên tay.
     */
    public void renderHUD(Graphics2D g, int screenWidth, int screenHeight) {
        if (activeWeapon == null) return;

        int hudW = 356;
        int hudH = 132;
        int hudX = screenWidth - hudW - 20;
        int hudY = screenHeight - hudH - 20;

        // 1. Container panel nền tối sci-fi bo góc
        g.setColor(new Color(12, 16, 24, 230));
        g.fillRoundRect(hudX, hudY, hudW, hudH, 10, 10);
        g.setColor(new Color(50, 70, 95));
        g.drawRoundRect(hudX, hudY, hudW, hudH, 10, 10);

        // 2. Dòng 1: Tên vũ khí đang cầm kèm CẤP ĐỘ (Level) và TỈ LỆ CHÍ MẠNG & HỆ SỐ CHÍ MẠNG
        g.setFont(GameFontManager.getBoldFont(12.0f));
        String nameAndLevel = String.format("[%d] %s   Lv.%d",
                activeWeapon.getSlotIndex(),
                activeWeapon.getName().toUpperCase(),
                activeWeapon.getLevel());
        String critInfo = String.format("CRIT: %d%% (x%.2f)",
                activeWeapon.getCritChancePercent(),
                activeWeapon.getCritMultiplier());

        g.setColor(new Color(255, 215, 65)); // Màu vàng kim cho tên và level
        g.drawString(nameAndLevel, hudX + 14, hudY + 22);

        FontMetrics fmTitle = g.getFontMetrics();
        int nameW = fmTitle.stringWidth(nameAndLevel);
        g.setColor(new Color(255, 120, 80)); // Màu đỏ cam nổi bật cho CRIT % và Hệ số chí mạng
        g.drawString(critInfo, hudX + 14 + nameW + 8, hudY + 22);

        // 3. Dòng 2: Hiển thị đạn băng / nạp đạn của vũ khí đang cầm
        g.setFont(GameFontManager.getBoldFont(20.0f));
        if (activeWeapon.isMelee()) {
            g.setColor(new Color(180, 225, 255));
            g.drawString("[MELEE] CẬN CHIẾN", hudX + 14, hudY + 48);
        } else if (activeWeapon.isReloading() || activeWeapon.isReloadPaused()) {
            g.setColor(new Color(255, 130, 40));
            g.drawString("RELOADING...", hudX + 14, hudY + 48);

            // Thanh tiến trình nạp đạn
            int barW = 120;
            int barH = 7;
            int barX = hudX + 175;
            int barY = hudY + 39;
            g.setColor(new Color(35, 45, 60));
            g.fillRect(barX, barY, barW, barH);
            g.setColor(new Color(255, 140, 30));
            g.fillRect(barX, barY, (int) (barW * activeWeapon.getReloadProgress()), barH);
            g.setColor(new Color(200, 220, 240));
            g.drawRect(barX, barY, barW, barH);
        } else {
            if (activeWeapon.hasInfiniteAmmo()) {
                g.setColor(activeWeapon.getCurrentAmmo() <= 2 ? new Color(255, 80, 80) : Color.WHITE);
                g.drawString(String.format("%d / --", activeWeapon.getCurrentAmmo()), hudX + 14, hudY + 48);
            } else if (activeWeapon.getAmmoType() != null) {
                int reserve = ammoInventory != null ? ammoInventory.getAmmo(activeWeapon.getAmmoType()) : 0;
                boolean isOutOfAmmo = (activeWeapon.getCurrentAmmo() <= 0 && reserve <= 0);
                boolean isLowAmmo = (activeWeapon.getCurrentAmmo() <= 2 || reserve <= 5);

                g.setColor(isOutOfAmmo ? new Color(255, 60, 60) : (isLowAmmo ? new Color(255, 150, 50) : Color.WHITE));
                String ammoDisplay = isOutOfAmmo
                        ? "HẾT ĐẠN! [0 / 0]"
                        : String.format("%d / %d", activeWeapon.getCurrentAmmo(), reserve);
                g.drawString(ammoDisplay, hudX + 14, hudY + 48);

                // Loại đạn badge đồng bộ màu chuẩn với hộp đạn rơi bên ngoài
                FontMetrics fm = g.getFontMetrics();
                int ammoTextW = fm.stringWidth(ammoDisplay);
                g.setFont(GameFontManager.getSmallBoldFont(10.0f));
                g.setColor(activeWeapon.getAmmoType().getColor());
                g.drawString("[" + activeWeapon.getAmmoType().name() + "]", hudX + 14 + ammoTextW + 8, hudY + 47);
            } else {
                g.setColor(activeWeapon.getCurrentAmmo() <= 2 ? new Color(255, 80, 80) : Color.WHITE);
                g.drawString(String.format("%d / %d", activeWeapon.getCurrentAmmo(), activeWeapon.getMagazineSize()),
                        hudX + 14, hudY + 48);
            }
        }

        // 4. Dòng 3: Hiển thị kho đạn của tất cả các loại trong người đồng bộ màu với hộp đạn bên ngoài
        int arReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.AR) : 0;
        int smgReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.SMG) : 0;
        int sgReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.SHOTGUN) : 0;
        int srReserve = ammoInventory != null ? ammoInventory.getAmmo(com.groupproject.game.entity.item.AmmoType.SR) : 0;

        int arMax = com.groupproject.game.entity.item.AmmoType.AR.getMaxCapacity();
        int smgMax = com.groupproject.game.entity.item.AmmoType.SMG.getMaxCapacity();
        int sgMax = com.groupproject.game.entity.item.AmmoType.SHOTGUN.getMaxCapacity();
        int srMax = com.groupproject.game.entity.item.AmmoType.SR.getMaxCapacity();

        int ammoBarY = hudY + 62;
        int ammoBadgeW = 76;
        int ammoBadgeH = 18;
        int ammoGap = 8;
        int ammoStartX = hudX + 14;

        renderAmmoPill(g, ammoStartX, ammoBarY, ammoBadgeW, ammoBadgeH, "RIF", arReserve, arMax, com.groupproject.game.entity.item.AmmoType.AR.getColor());
        renderAmmoPill(g, ammoStartX + (ammoBadgeW + ammoGap), ammoBarY, ammoBadgeW, ammoBadgeH, "SMG", smgReserve, smgMax, com.groupproject.game.entity.item.AmmoType.SMG.getColor());
        renderAmmoPill(g, ammoStartX + (ammoBadgeW + ammoGap) * 2, ammoBarY, ammoBadgeW, ammoBadgeH, "SG", sgReserve, sgMax, com.groupproject.game.entity.item.AmmoType.SHOTGUN.getColor());
        renderAmmoPill(g, ammoStartX + (ammoBadgeW + ammoGap) * 3, ammoBarY, ammoBadgeW, ammoBadgeH, "SR", srReserve, srMax, com.groupproject.game.entity.item.AmmoType.SR.getColor());

        // 5. Dòng 4: Các vị trí nút trang bị (Equipment Slots) [1] [2] [3] [4] (Yêu cầu 2)
        int slotY = hudY + 92;
        int chipW = 73;
        int chipH = 28;
        int chipGap = 7;
        int chipStartX = hudX + 14;

        for (int i = 1; i <= 4; i++) {
            int cx = chipStartX + (i - 1) * (chipW + chipGap);
            Weapon w = weaponSlots.get(i);
            boolean isCur = (activeWeapon.getSlotIndex() == i);

            // Nền chip
            if (isCur) {
                g.setColor(new Color(35, 75, 120, 240));
                g.fillRoundRect(cx, slotY, chipW, chipH, 6, 6);
                g.setColor(new Color(255, 215, 60)); // Viền vàng sáng cho slot đang chọn
                g.drawRoundRect(cx, slotY, chipW, chipH, 6, 6);
            } else {
                g.setColor(new Color(20, 26, 36, 180));
                g.fillRoundRect(cx, slotY, chipW, chipH, 6, 6);
                g.setColor(new Color(45, 60, 80));
                g.drawRoundRect(cx, slotY, chipW, chipH, 6, 6);
            }

            // Tên ngắn gọn trang bị
            String keycap = (i == 4) ? "[4 | R-Clk]" : ("[" + i + "]");
            String label;
            if (w == null) {
                label = "Trống";
            } else if (w instanceof AssaultRifle) {
                label = "AR " + w.getLevelDisplay();
            } else if (w instanceof SMG) {
                label = "SMG " + w.getLevelDisplay();
            } else if (w instanceof Shotgun) {
                label = "SG " + w.getLevelDisplay();
            } else if (w instanceof SniperRifle) {
                label = "SR " + w.getLevelDisplay();
            } else if (w instanceof SemiCarbineRifle) {
                label = "SCR " + w.getLevelDisplay();
            } else if (w instanceof SemiAutoShotgun) {
                label = "SAS " + w.getLevelDisplay();
            } else if (w instanceof Pistol) {
                label = "Pistol";
            } else if (w instanceof CombatKnife) {
                label = "Dao Găm";
            } else {
                label = w.getShortDisplayName();
            }

            g.setFont(GameFontManager.getSmallBoldFont(10.0f));
            g.setColor(isCur ? new Color(255, 220, 80) : new Color(130, 150, 175));
            g.drawString(keycap, cx + 5, slotY + 12);

            g.setFont(isCur ? GameFontManager.getSmallBoldFont(10.0f) : GameFontManager.getSmallFont(10.0f));
            g.setColor(isCur ? Color.WHITE : (w == null ? new Color(90, 105, 125) : new Color(185, 205, 230)));
            g.drawString(label, cx + 5, slotY + 24);
        }
    }

    /**
     * Helper vẽ nhãn hiển thị số lượng đạn dự trữ cho từng loại đạn.
     */
    private void renderAmmoPill(Graphics2D g, int x, int y, int w, int h, String name, int current, int max, Color accentColor) {
        g.setColor(new Color(18, 24, 34, 200));
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(new Color(40, 55, 75));
        g.drawRoundRect(x, y, w, h, 4, 4);

        g.setColor(accentColor);
        g.fillOval(x + 5, y + 5, 6, 6);

        g.setFont(GameFontManager.getSmallBoldFont(10.0f));
        g.setColor(new Color(190, 205, 225));
        g.drawString(name + ":", x + 14, y + 13);

        boolean isZero = (current <= 0);
        g.setColor(isZero ? new Color(255, 75, 75) : Color.WHITE);
        String amountText = current + "/" + max;
        g.drawString(amountText, x + 40, y + 13);
    }

    public Weapon getActiveWeapon() {
        return activeWeapon;
    }

    /**
     * Kiểm tra xem vũ khí hiện tại có bị cạn sạch toàn bộ đạn (cả băng lẫn dự trữ) hay không.
     */
    public boolean isCurrentWeaponOutOfAmmo() {
        return activeWeapon != null && activeWeapon.isOutOfAmmo();
    }

    public List<MeleeSlash> getActiveSlashes() {
        return activeSlashes;
    }

    public Weapon getWeaponInSlot(int slotIndex) {
        return weaponSlots.get(slotIndex);
    }

    public Weapon getWeapon(int slotIndex) {
        return weaponSlots.get(slotIndex);
    }

    public Weapon removeWeapon(int slotIndex) {
        Weapon removed = weaponSlots.remove(slotIndex);
        if (removed != null) {
            if (previousWeapon == removed) {
                previousWeapon = null;
            }
            if (activeWeapon == removed) {
                for (int slot : new int[]{1, 2, 3, 4}) {
                    if (weaponSlots.containsKey(slot)) {
                        activeWeapon = weaponSlots.get(slot);
                        break;
                    }
                }
            }
        }
        return removed;
    }

    public Map<Integer, Weapon> getWeaponSlots() {
        return java.util.Collections.unmodifiableMap(weaponSlots);
    }

    public java.util.Collection<Weapon> getAllWeapons() {
        return weaponSlots.values();
    }

    public com.groupproject.game.entity.item.AmmoInventory getAmmoInventory() {
        return ammoInventory;
    }

    /**
     * Thu hồi toàn bộ số đạn còn lại trong băng của vũ khí về kho ba lô dự trữ (AmmoInventory).
     * Hủy bỏ trạng thái nạp đạn nếu đang dở dang và rút đạn trong băng về 0 để tránh nhân bản đạn.
     *
     * @param weapon Vũ khí cần thu hồi đạn trong băng.
     * @return Số lượng đạn dư thừa không thể chứa trong kho ba lô (surplus >= 0).
     */
    public int reclaimAmmoFromWeapon(Weapon weapon) {
        if (weapon == null) return 0;
        weapon.cancelReload();
        com.groupproject.game.entity.item.AmmoType type = weapon.getAmmoType();
        int current = weapon.getCurrentAmmo();
        if (type == null || current <= 0) return 0;

        weapon.setCurrentAmmo(0);
        int added = ammoInventory.addAmmo(type, current);
        return current - added;
    }

    /**
     * Tiếp tế 2 stack đạn cho toàn bộ các chủng loại đạn dự trữ (AR: +60, SMG: +70, Shotgun: +20)
     * và nạp đầy băng đạn cho tất cả súng hiện có trong túi đồ.
     */
    public void resupplyAllWeapons() {
        resupplyAmmoPackage(2);
    }

    /**
     * Tiếp tế gói đạn theo số lượng stack cho mỗi chủng loại đạn và làm đầy băng đạn cho súng.
     *
     * @param stacksPerType Số lượng stack tiếp tế cho mỗi loại (mặc định là 2 stack khi mua tại Shop).
     */
    public void resupplyAmmoPackage(int stacksPerType) {
        ammoInventory.addStacksForAllTypes(stacksPerType);
        for (Weapon w : weaponSlots.values()) {
            if (w != null && !w.isMelee()) {
                w.resupplyAmmo();
            }
        }
    }

    /**
     * Tạo đối tượng vũ khí cụ thể dựa trên tên định danh thô.
     */
    public static Weapon createWeaponByName(String rawName) {
        if (rawName == null) return new Pistol();
        String clean = rawName.trim();
        if (clean.startsWith("[S] ")) {
            clean = clean.substring(4).trim();
        }

        String lower = clean.toLowerCase();
        if (lower.contains("knife")) {
            return new CombatKnife();
        } else if (lower.contains("semi-auto shotgun") || lower.contains("sas")) {
            return new SemiAutoShotgun();
        } else if (lower.contains("semi") || lower.contains("carbine") || lower.contains("scr")) {
            return new SemiCarbineRifle();
        } else if (lower.contains("assault") || lower.contains("ar")) {
            return new AssaultRifle();
        } else if (lower.contains("shotgun")) {
            return new Shotgun();
        } else if (lower.contains("submachine") || lower.contains("smg")) {
            return new SMG();
        } else if (lower.contains("sniper") || lower.contains("sr")) {
            return new SniperRifle();
        } else if (lower.contains("pistol")) {
            return new Pistol();
        }

        return new Pistol();
    }

    /**
     * Khôi phục toàn bộ kho vũ khí từ danh sách dữ liệu lưu (Save Checkpoint).
     */
    public void restoreFromSave(java.util.List<com.groupproject.game.storage.WeaponSaveData> savedWeapons, int activeSlot) {
        weaponSlots.clear();
        activeSlashes.clear();

        if (savedWeapons != null) {
            for (com.groupproject.game.storage.WeaponSaveData data : savedWeapons) {
                Weapon w = createWeaponByName(data.getWeaponName());
                w.setSlotIndex(data.getSlotIndex());
                w.setLevel(data.getLevel());
                w.setStars(data.getDamageStars(), data.getFireRateStars(), data.getAccuracyStars());
                w.setCritStats(data.getCritChance(), data.getCritMultiplier());
                w.setCurrentAmmo(data.getCurrentAmmo());
                w.setSTier(data.isSTier());
                addWeapon(w);
            }
        }

        // Đảm bảo tối thiểu phải có Pistol (Slot 3) và Knife (Slot 4) nếu bị thiếu
        if (!weaponSlots.containsKey(3)) {
            addWeapon(new Pistol());
        }
        if (!weaponSlots.containsKey(4)) {
            addWeapon(new CombatKnife());
        }

        // Kích hoạt slot chỉ định
        if (weaponSlots.containsKey(activeSlot)) {
            this.activeWeapon = weaponSlots.get(activeSlot);
        } else if (weaponSlots.containsKey(1)) {
            this.activeWeapon = weaponSlots.get(1);
        } else {
            this.activeWeapon = weaponSlots.get(3);
        }
        this.previousWeapon = weaponSlots.get(4);
    }
}
