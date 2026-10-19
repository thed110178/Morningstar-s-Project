package com.groupproject.game.ui;

import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.SMG;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.entity.weapon.WeaponManager;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * Giao diện Balo chiến thuật (Tactical Backpack / Inventory UI Overlay).
 *
 * <p>WHY: Cho phép người chơi bấm phím [Tab] để tạm dừng quan sát kho trang bị,
 * kiểm tra chi tiết các chỉ số vũ khí (Damage, Ammo, RPM, Chế độ bắn) và đặc biệt
 * là hoán đổi vị trí linh hoạt giữa 2 vũ khí chính ở Slot [1] và Slot [2] thông qua
 * click chuột hoặc phím tắt [SPACE] / [S].
 */
public class BackpackUI {

    // Kích thước bảng Balo
    private final int panelW = 740;
    private final int panelH = 490;

    // Các vùng nhấn chuột (Bounding boxes)
    private final Rectangle closeBtnBounds = new Rectangle();
    private final Rectangle swapBtnBounds = new Rectangle();
    private final Rectangle slot1Bounds = new Rectangle();
    private final Rectangle slot2Bounds = new Rectangle();
    private final Rectangle slot3Bounds = new Rectangle();
    private final Rectangle slot4Bounds = new Rectangle();

    // Hiệu ứng phản hồi tương tác
    private boolean isSwapHovered = false;
    private boolean isCloseHovered = false;
    private String feedbackMessage = "";
    private double feedbackTimer = 0.0;

    /**
     * Cập nhật các tương tác chuột và bàn phím bên trong giao diện Balo.
     *
     * @return {@code true} nếu người chơi yêu cầu đóng Balo (nhấn nút [X] hoặc ESC/TAB).
     */
    public boolean update(KeyboardHandler keyH, MouseHandler mouseH, WeaponManager weaponManager, double deltaTime) {
        if (feedbackTimer > 0) {
            feedbackTimer -= deltaTime;
            if (feedbackTimer <= 0) {
                feedbackMessage = "";
            }
        }

        int mouseX = mouseH.getScreenX();
        int mouseY = mouseH.getScreenY();

        isSwapHovered = swapBtnBounds.contains(mouseX, mouseY);
        isCloseHovered = closeBtnBounds.contains(mouseX, mouseY);

        // 1. Phím tắt hoán đổi nhanh vũ khí [1] & [2]
        if (keyH.isKeyPressedOnce(KeyEvent.VK_SPACE) || keyH.isKeyPressedOnce(KeyEvent.VK_S)) {
            triggerSwap(weaponManager);
        }

        // 2. Phím tắt trang bị trực tiếp vào tay
        if (keyH.isKeyPressedOnce(KeyEvent.VK_1) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD1)) {
            weaponManager.selectSlot(1);
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_2) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD2)) {
            weaponManager.selectSlot(2);
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_3) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD3)) {
            weaponManager.selectSlot(3);
        }
        if (keyH.isKeyPressedOnce(KeyEvent.VK_4) || keyH.isKeyPressedOnce(KeyEvent.VK_NUMPAD4)) {
            weaponManager.selectSlot(4);
        }

        // 3. Tương tác chuột trái
        if (mouseH.isLeftClickedOnce()) {
            if (isCloseHovered) {
                return true; // Yêu cầu đóng Balo
            }

            if (isSwapHovered) {
                triggerSwap(weaponManager);
                return false;
            }

            if (slot1Bounds.contains(mouseX, mouseY)) {
                if (weaponManager.getWeaponInSlot(1) != null) weaponManager.selectSlot(1);
            } else if (slot2Bounds.contains(mouseX, mouseY)) {
                if (weaponManager.getWeaponInSlot(2) != null) weaponManager.selectSlot(2);
            } else if (slot3Bounds.contains(mouseX, mouseY)) {
                weaponManager.selectSlot(3);
            } else if (slot4Bounds.contains(mouseX, mouseY)) {
                weaponManager.selectSlot(4);
            }
        }

        // Đóng balo khi bấm phím ESC (phím TAB / B được toggle trực tiếp từ PlayingState)
        if (keyH.isKeyPressedOnce(KeyEvent.VK_ESCAPE)) {
            return true;
        }

        return false;
    }

    private void triggerSwap(WeaponManager weaponManager) {
        boolean success = weaponManager.swapPrimarySlots();
        if (success) {
            feedbackMessage = "★ ĐÃ HOÁN ĐỔI VỊ TRÍ VŨ KHÍ [1] ⇄ [2] THÀNH CÔNG ★";
        } else {
            feedbackMessage = "CẢ HAI Ô VŨ KHÍ CHÍNH ĐỀU TRỐNG (MỞ HÒM ĐỂ NHẶT SÚNG)";
        }
        feedbackTimer = 2.2;
    }

    /**
     * Vẽ giao diện Balo chiến thuật với phong cách Sci-Fi Cyber Tactical Glassmorphism.
     */
    public void render(Graphics2D g, WeaponManager weaponManager, int screenWidth, int screenHeight) {
        // 1. Phủ mờ toàn màn hình
        g.setColor(new Color(5, 10, 20, 215));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // Tọa độ trung tâm bảng
        int panelX = (screenWidth - panelW) / 2;
        int panelY = (screenHeight - panelH) / 2;

        // Cập nhật vùng nút đóng
        closeBtnBounds.setBounds(panelX + panelW - 44, panelY + 14, 30, 30);

        // 2. Thân bảng điều khiển
        g.setColor(new Color(14, 20, 32, 250));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);

        // Viền đôi dạ quang phong cách Cyberpunk
        g.setColor(new Color(45, 75, 105));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        g.setColor(new Color(60, 220, 160));
        g.drawRoundRect(panelX + 3, panelY + 3, panelW - 6, panelH - 6, 14, 14);

        // 3. Header Tiêu đề Balo
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(new Color(60, 220, 160));
        g.drawString("🎒 BALO CHIẾN THUẬT // TACTICAL BACKPACK", panelX + 25, panelY + 38);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(150, 175, 205));
        g.drawString("QUẢN LÝ TRANG BỊ & SẮP XẾP VŨ KHÍ CHIẾN ĐẤU", panelX + 25, panelY + 56);

        // Nút đóng [X]
        g.setColor(isCloseHovered ? new Color(255, 60, 60) : new Color(50, 65, 85));
        g.fillRoundRect(closeBtnBounds.x, closeBtnBounds.y, closeBtnBounds.width, closeBtnBounds.height, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("X", closeBtnBounds.x + 9, closeBtnBounds.y + 21);

        // Đường gạch ngang phân cách
        g.setColor(new Color(40, 60, 85));
        g.drawLine(panelX + 25, panelY + 68, panelX + panelW - 25, panelY + 68);

        // 4. KHU VỰC VŨ KHÍ CHÍNH (SLOT 1 & SLOT 2)
        int slotW = 270;
        int slotH = 200;
        int slot1X = panelX + 35;
        int slot1Y = panelY + 85;
        int slot2X = panelX + panelW - 35 - slotW;
        int slot2Y = slot1Y;

        slot1Bounds.setBounds(slot1X, slot1Y, slotW, slotH);
        slot2Bounds.setBounds(slot2X, slot2Y, slotW, slotH);

        Weapon activeW = weaponManager.getActiveWeapon();
        Weapon w1 = weaponManager.getWeaponInSlot(1);
        Weapon w2 = weaponManager.getWeaponInSlot(2);

        renderWeaponCard(g, slot1X, slot1Y, slotW, slotH, 1, "VŨ KHÍ CHÍNH 1", w1, activeW == w1 && w1 != null);
        renderWeaponCard(g, slot2X, slot2Y, slotW, slotH, 2, "VŨ KHÍ CHÍNH 2", w2, activeW == w2 && w2 != null);

        // NÚT TRUNG TÂM HOÁN ĐỔI VỊ TRÍ [1] ⇄ [2]
        int swapW = 110;
        int swapH = 68;
        int swapX = panelX + (panelW - swapW) / 2;
        int swapY = slot1Y + (slotH - swapH) / 2;
        swapBtnBounds.setBounds(swapX, swapY, swapW, swapH);

        g.setColor(isSwapHovered ? new Color(255, 215, 60, 40) : new Color(25, 38, 55, 220));
        g.fillRoundRect(swapX, swapY, swapW, swapH, 10, 10);
        g.setColor(isSwapHovered ? new Color(255, 220, 70) : new Color(70, 100, 140));
        g.setStroke(new BasicStroke(isSwapHovered ? 2.5f : 1.5f));
        g.drawRoundRect(swapX, swapY, swapW, swapH, 10, 10);

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(isSwapHovered ? new Color(255, 225, 80) : Color.WHITE);
        drawCenteredString(g, "⇄ ĐỔI VỊ TRÍ", swapX + swapW / 2, swapY + 28);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(isSwapHovered ? Color.WHITE : new Color(180, 200, 220));
        drawCenteredString(g, "[SPACE / S]", swapX + swapW / 2, swapY + 48);

        // 5. KHU VỰC VŨ KHÍ PHỤ & CẬN CHIẾN (SLOT 3 & SLOT 4)
        int subSlotW = 325;
        int subSlotH = 100;
        int slot3X = panelX + 35;
        int slot3Y = panelY + 300;
        int slot4X = panelX + panelW - 35 - subSlotW;
        int slot4Y = slot3Y;

        slot3Bounds.setBounds(slot3X, slot3Y, subSlotW, subSlotH);
        slot4Bounds.setBounds(slot4X, slot4Y, subSlotW, subSlotH);

        Weapon w3 = weaponManager.getWeaponInSlot(3);
        Weapon w4 = weaponManager.getWeaponInSlot(4);

        renderSubCard(g, slot3X, slot3Y, subSlotW, subSlotH, 3, "SÚNG LỤC PHỤ (SIDEARM)", w3, activeW == w3);
        renderSubCard(g, slot4X, slot4Y, subSlotW, subSlotH, 4, "VŨ KHÍ CẬN CHIẾN (MELEE)", w4, activeW == w4);

        // 5.5. KHU VỰC THỐNG KÊ KHO ĐẠN DỰ TRỮ (AMMO RESERVES HUD)
        com.groupproject.game.entity.item.AmmoInventory ammoInv = weaponManager.getAmmoInventory();
        int ammoBarY = panelY + 410;
        int barW = 195;
        int barH = 22;

        // AR Ammo Badge
        int arX = panelX + 35;
        g.setColor(new Color(20, 35, 30, 220));
        g.fillRoundRect(arX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(60, 220, 120));
        g.drawRoundRect(arX, ammoBarY, barW, barH, 6, 6);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("• ĐẠN AR: " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.AR) + " / 150", arX + 12, ammoBarY + 15);

        // SMG Ammo Badge
        int smgX = arX + barW + 25;
        g.setColor(new Color(15, 30, 45, 220));
        g.fillRoundRect(smgX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(0, 210, 255));
        g.drawRoundRect(smgX, ammoBarY, barW, barH, 6, 6);
        g.drawString("• ĐẠN SMG: " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.SMG) + " / 180", smgX + 12, ammoBarY + 15);

        // Shotgun Ammo Badge
        int sgX = smgX + barW + 25;
        g.setColor(new Color(40, 20, 20, 220));
        g.fillRoundRect(sgX, ammoBarY, barW, barH, 6, 6);
        g.setColor(new Color(255, 90, 70));
        g.drawRoundRect(sgX, ammoBarY, barW, barH, 6, 6);
        g.drawString("• ĐẠN SHOTGUN: " + ammoInv.getAmmo(com.groupproject.game.entity.item.AmmoType.SHOTGUN) + " / 50", sgX + 12, ammoBarY + 15);

        // 6. Thông báo phản hồi thao tác hoán đổi
        if (!feedbackMessage.isEmpty() && feedbackTimer > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(new Color(255, 220, 80));
            drawCenteredString(g, feedbackMessage, panelX + panelW / 2, panelY + 400);
        }

        // 7. Chân bảng hướng dẫn thao tác (Footer Guide)
        g.setColor(new Color(40, 60, 85));
        g.drawLine(panelX + 25, panelY + 440, panelX + panelW - 25, panelY + 440);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(160, 185, 215));
        drawCenteredString(g, "[TAB / ESC] Đóng Balo  |  [SPACE / Click ⇄] Đổi vị trí Slot 1 & 2  |  [1, 2, 3, 4] Trang bị lên tay",
                panelX + panelW / 2, panelY + 465);

        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderWeaponCard(Graphics2D g, int x, int y, int w, int h, int slot, String slotTitle,
                                  Weapon weapon, boolean isEquipped) {
        // Nền thẻ
        g.setColor(new Color(20, 30, 45, 230));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(isEquipped ? new Color(60, 220, 160) : new Color(50, 75, 105));
        g.setStroke(new BasicStroke(isEquipped ? 2.2f : 1.2f));
        g.drawRoundRect(x, y, w, h, 12, 12);

        // Tiêu đề Slot
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(255, 200, 60));
        g.drawString(String.format("[%d] %s", slot, slotTitle), x + 15, y + 25);

        if (weapon != null) {
            // Tên vũ khí và cấp độ hiển thị (VD: AR I [0/3], SHOTGUN II [1/3])
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(new Color(255, 220, 75));
            String titleText = weapon.getShortDisplayName().toUpperCase();
            if (!weapon.isMelee()) {
                titleText += " [" + weapon.getUpgradeCount() + "/" + Weapon.MAX_UPGRADE_COUNT + "]";
            }
            g.drawString(titleText, x + 15, y + 48);

            // Minh họa hình dạng vũ khí thu nhỏ
            int iconCenterX = x + w - 45;
            int iconCenterY = y + 42;
            if (weapon instanceof AssaultRifle) {
                g.setColor(new Color(50, 60, 70));
                g.fillRect(iconCenterX - 18, iconCenterY - 4, 34, 7);
                g.setColor(new Color(255, 215, 60));
                g.fillRect(iconCenterX + 16, iconCenterY - 3, 3, 5);
                g.setColor(new Color(30, 35, 40));
                g.fillRect(iconCenterX - 6, iconCenterY + 3, 6, 8);
            } else if (weapon instanceof Shotgun) {
                g.setColor(new Color(139, 69, 19));
                g.fillRect(iconCenterX - 16, iconCenterY - 4, 12, 8);
                g.setColor(new Color(60, 65, 75));
                g.fillRect(iconCenterX - 4, iconCenterY - 5, 24, 9);
            } else if (weapon instanceof SMG) {
                g.setColor(new Color(40, 50, 62));
                g.fillRect(iconCenterX - 14, iconCenterY - 4, 24, 7);
                g.setColor(new Color(20, 26, 32));
                g.fillRect(iconCenterX - 4, iconCenterY + 3, 5, 10);
                g.setColor(new Color(0, 230, 255));
                g.fillRect(iconCenterX + 10, iconCenterY - 3, 3, 5);
            }

            // Thông số chi tiết đo lường bằng sao và chí mạng
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.setColor(new Color(185, 205, 230));
            g.drawString("• Sát thương:", x + 15, y + 68);
            g.drawString("• Tốc ra đạn:", x + 15, y + 84);
            g.drawString("• Chính xác:", x + 15, y + 100);
            g.drawString("• Chí mạng:", x + 15, y + 116);
            String ammoText = "• Băng đạn: " + weapon.getCurrentAmmo() + " / " + weapon.getMagazineSize();
            if (weapon.getAmmoType() != null) {
                ammoText += " [Kho: " + weapon.getReserveAmmo() + "/" + weapon.getAmmoType().getMaxCapacity() + "]";
            }
            g.drawString(ammoText, x + 15, y + 134);

            // Các cột sao hiển thị màu vàng kim và thông số thực tế
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(new Color(255, 225, 75));
            g.drawString(Weapon.formatStars(weapon.getDamageStars()) + " (" + weapon.getDamage() + " DMG)", x + 88, y + 68);
            g.drawString(Weapon.formatStars(weapon.getFireRateStars()) + " (" + weapon.getFormattedRPM() + ")", x + 88, y + 84);
            g.drawString(Weapon.formatStars(weapon.getAccuracyStars()) + " (" + weapon.getFormattedSpread() + ")", x + 88, y + 100);
            g.setColor(new Color(255, 130, 90));
            g.drawString(weapon.getCritChancePercent() + "% (x" + String.format("%.1f", weapon.getCritMultiplier()) + " DMG)", x + 88, y + 116);

            // Nhãn trạng thái trang bị
            if (isEquipped) {
                g.setColor(new Color(25, 70, 50, 220));
                g.fillRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setColor(new Color(60, 220, 160));
                g.drawRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                drawCenteredString(g, "★ ĐANG CẦM TRÊN TAY ★", x + w / 2, y + 175);
            } else {
                g.setColor(new Color(35, 50, 70));
                g.fillRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setColor(new Color(70, 105, 145));
                g.drawRoundRect(x + 15, y + 158, w - 30, 26, 6, 6);
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.setColor(new Color(180, 210, 240));
                drawCenteredString(g, "[ CLICK / PHÍM " + slot + " ĐỂ CẦM ]", x + w / 2, y + 175);
            }
        } else {
            // Ô trống
            g.setFont(new Font("SansSerif", Font.ITALIC, 15));
            g.setColor(new Color(120, 140, 165));
            drawCenteredString(g, "[ Ô TRỐNG ]", x + w / 2, y + 95);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            drawCenteredString(g, "Mở hòm tiếp tế để nhặt súng", x + w / 2, y + 120);
        }
    }

    private void renderSubCard(Graphics2D g, int x, int y, int w, int h, int slot, String slotTitle,
                              Weapon weapon, boolean isEquipped) {
        g.setColor(new Color(20, 30, 45, 230));
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(isEquipped ? new Color(60, 220, 160) : new Color(50, 75, 105));
        g.setStroke(new BasicStroke(isEquipped ? 2.0f : 1.0f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(255, 200, 60));
        g.drawString(String.format("[%d] %s", slot, slotTitle), x + 12, y + 22);

        if (weapon != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(weapon.isMelee() ? Color.WHITE : new Color(255, 220, 75));
            g.drawString(weapon.getShortDisplayName().toUpperCase(), x + 12, y + 40);

            // Badge tỉ lệ chí mạng nổi bật góc phải
            int badgeW = 95;
            int badgeH = 18;
            int badgeX = x + w - badgeW - 12;
            int badgeY = y + 26;
            g.setColor(new Color(45, 20, 20, 220));
            g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
            g.setColor(new Color(255, 95, 80));
            g.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("CRIT: " + weapon.getCritChancePercent() + "%", badgeX + 12, badgeY + 13);

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(180, 205, 230));
            if (weapon.isMelee()) {
                g.drawString(String.format("• Sát thương: %d HP  • Chí mạng: %d%% (x2.0 DMG)",
                        weapon.getDamage(), weapon.getCritChancePercent()), x + 12, y + 57);
                g.drawString(String.format("• Tốc vung: %s  • Loại: Cận chiến (∞)",
                        weapon.getFormattedRPM()), x + 12, y + 73);
            } else {
                String subAmmo = weapon.hasInfiniteAmmo() ? " (∞)" : "";
                g.drawString(String.format("• Sát thương: %d HP  • Chí mạng: %d%% (x2.0 DMG)",
                        weapon.getDamage(), weapon.getCritChancePercent()), x + 12, y + 57);
                g.drawString(String.format("• Băng đạn: %d/%d%s  • Tốc bắn: %s",
                        weapon.getCurrentAmmo(), weapon.getMagazineSize(), subAmmo, weapon.getFormattedRPM()), x + 12, y + 73);
            }

            if (isEquipped) {
                g.setColor(new Color(60, 220, 160));
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                g.drawString("★ ĐANG CẦM", x + 12, y + 89);
            } else {
                g.setColor(new Color(130, 155, 185));
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString("[Click hoặc nhấn " + slot + " để cầm]", x + 12, y + 89);
            }
        }
    }

    private void drawCenteredString(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        g.drawString(text, centerX - textW / 2, baselineY);
    }
}
