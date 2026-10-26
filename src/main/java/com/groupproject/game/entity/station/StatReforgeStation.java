package com.groupproject.game.entity.station;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.weapon.Weapon;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Bàn Kỹ Thuật Quân Khí Dã Chiến (Field Gunsmith Workbench) đặt tại trung tâm Xưởng Quân Khí.
 *
 * <p>WHY: Cho phép người chơi đầu tư Credits chiến thuật để hiệu chỉnh, tùy biến và cân chỉnh
 * (Gunsmith Calibration) ngẫu nhiên lại toàn bộ cấp độ sao (1★ - 5★) và tỉ lệ phát huy sát thương
 * chí mạng (5% - 50%) của khí tài đang cầm trên tay theo phong cách công binh dã chiến thực tế.
 * Chi phí Credits yêu cầu tăng dần lũy tiến sau mỗi lần hiệu chỉnh để tạo chiều sâu kinh tế Roguelite.
 */
public class StatReforgeStation {

    private final double x;
    private final double y;
    private final double interactionRadius = 65.0;

    private int reforgeCount = 0;
    private int starsReforgeCount = 0;
    private int critReforgeCount = 0;
    private double animTimer = 0.0;
    private double flashTimer = 0.0;

    /**
     * Khởi tạo Bàn Kỹ Thuật Quân Khí tại tọa độ thế giới chỉ định.
     */
    public StatReforgeStation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Chi phí Credits để Reroll Sao Chỉ Số (DMG, RPM, ACC).
     */
    public int getStarsReforgeCost(Weapon weapon) {
        int base = (weapon != null && weapon.getLevel() <= 1) ? 20 : 25;
        int cost = base;
        for (int i = 0; i < starsReforgeCount; i++) {
            cost += 15 + i * 5;
        }
        return cost;
    }

    /**
     * Chi phí Credits để Reroll Tỉ Lệ & Hệ Số Chí Mạng.
     */
    public int getCritReforgeCost(Weapon weapon) {
        int base = 15;
        int cost = base;
        for (int i = 0; i < critReforgeCount; i++) {
            cost += 10 + i * 5;
        }
        return cost;
    }

    /**
     * Tính toán chi phí Credits chung (phục vụ tương thích ngược).
     */
    public int getCurrentCost(Weapon weapon) {
        boolean isLevel1 = (weapon != null && weapon.getLevel() <= 1);
        if (isLevel1) {
            if (reforgeCount <= 0) return 15;
            int cost = 15;
            for (int i = 0; i < reforgeCount; i++) {
                cost += 10 + i * 5;
            }
            return cost;
        } else {
            if (reforgeCount <= 0) return 30;
            int cost = 30;
            for (int i = 0; i < reforgeCount; i++) {
                cost += 20 + i * 5;
            }
            return cost;
        }
    }

    /**
     * Quá tải mặc định tính giá theo vũ khí cấp cao (phục vụ tương thích ngược).
     */
    public int getCurrentCost() {
        return getCurrentCost(null);
    }

    public int getReforgeCount() {
        return reforgeCount;
    }

    public void setReforgeCount(int count) {
        this.reforgeCount = Math.max(0, count);
    }

    public int getStarsReforgeCount() {
        return starsReforgeCount;
    }

    public void setStarsReforgeCount(int count) {
        this.starsReforgeCount = Math.max(0, count);
    }

    public int getCritReforgeCount() {
        return critReforgeCount;
    }

    public void setCritReforgeCount(int count) {
        this.critReforgeCount = Math.max(0, count);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void update(double deltaTime) {
        animTimer += deltaTime * 2.0;
        if (flashTimer > 0) {
            flashTimer -= deltaTime;
        }
    }

    /**
     * Kiểm tra người chơi có đang đứng trong bán kính tương tác của bàn kỹ thuật hay không.
     */
    public boolean isPlayerInRange(double playerX, double playerY, double playerSize) {
        return Math.hypot(playerX - x, playerY - y) <= (interactionRadius + playerSize / 2.0);
    }

    /**
     * Mở Modal UI Xưởng Quân Khí Dã Chiến để người chơi chọn 1 trong 2 mục Reroll.
     *
     * @param state Trạng thái màn chơi chứa ví Credits và kho khí tài của người chơi.
     * @return {@code true} nếu mở Modal thành công; {@code false} nếu không có vũ khí trên tay.
     */
    public boolean interact(PlayingState state) {
        if (state == null) return false;

        Weapon weapon = state.getWeaponManager().getActiveWeapon();
        if (weapon == null) {
            state.showTacticalBanner("KHÔNG TÌM THẤY VŨ KHÍ TRÊN TAY!", new Color(255, 100, 100), 2.0);
            return false;
        }

        // Mở Modal UI Xưởng Quân Khí
        state.openStatReforgeModal(weapon, this);
        return true;
    }

    /**
     * Thực hiện hiệu chỉnh toàn diện (cả sao lẫn chí mạng) trực tiếp — Phục vụ tương thích ngược.
     */
    public boolean executeReforge(PlayingState state, Weapon weapon) {
        if (state == null || weapon == null) return false;
        int cost = getCurrentCost(weapon);
        if (state.getPlayerCredits() < cost) {
            state.showTacticalBanner("KHÔNG ĐỦ CREDITS! (CẦN " + cost + " CREDITS - HIỆN CÓ: "
                    + state.getPlayerCredits() + ")", new Color(255, 75, 75), 2.2);
            return false;
        }

        state.deductCredits(cost);
        Weapon.ReforgeResult result = weapon.reforgeStats();
        reforgeCount++;
        flashTimer = 0.50;
        state.triggerScreenShake(0.12);

        String bannerMsg = String.format("★ HIỆU CHỈNH: %s (Lv.%d) [DMG: %d★ | RPM: %d★ | ACC: %d★] (CHÍ MẠNG: %d%%) [-%d CREDITS] ★",
                weapon.getShortDisplayName().toUpperCase(),
                result.level(),
                result.newDmgStars(),
                result.newFrStars(),
                result.newAccStars(),
                result.newCritPercent(),
                cost);
        state.showTacticalBanner(bannerMsg, new Color(255, 215, 60), 3.4);
        return true;
    }

    /**
     * Thực thi Reroll Sao Chỉ Số (bảo lưu 100% chí mạng).
     */
    public Weapon.ReforgeResult executeReforgeStars(PlayingState state, Weapon weapon) {
        if (state == null || weapon == null) return null;
        if (weapon.getLevel() <= 1) {
            state.showTacticalBanner("VŨ KHÍ LV.1 CHƯA CÓ SAO NÂNG CẤP ĐỂ TÁI CHẾ!", new Color(255, 140, 60), 2.5);
            return null;
        }
        int cost = getStarsReforgeCost(weapon);
        if (state.getPlayerCredits() < cost) return null;

        Weapon.ReforgeResult res = weapon.reforgeStarsOnly();
        if (res == null) return null;

        state.deductCredits(cost);
        starsReforgeCount++;
        reforgeCount++;
        flashTimer = 0.50;
        state.triggerScreenShake(0.12);

        String msg = String.format("★ REROLL SAO: %s (Lv.%d) [DMG: %d★ | RPM: %d★ | ACC: %d★] [-%d CREDITS] ★",
                weapon.getShortDisplayName().toUpperCase(),
                res.level(),
                res.newDmgStars(),
                res.newFrStars(),
                res.newAccStars(),
                cost);
        state.showTacticalBanner(msg, new Color(80, 180, 255), 3.2);
        return res;
    }

    /**
     * Thực thi Reroll Tỉ Lệ & Hệ Số Chí Mạng (bảo lưu 100% sao chỉ số).
     */
    public Weapon.ReforgeResult executeReforgeCrit(PlayingState state, Weapon weapon) {
        if (state == null || weapon == null) return null;
        int cost = getCritReforgeCost(weapon);
        if (state.getPlayerCredits() < cost) return null;

        state.deductCredits(cost);
        Weapon.ReforgeResult res = weapon.reforgeCritOnly();
        critReforgeCount++;
        reforgeCount++;
        flashTimer = 0.50;
        state.triggerScreenShake(0.12);

        String msg = String.format("★ REROLL CHÍ MẠNG: %s [CRIT: %d%% x%.2f] [-%d CREDITS] ★",
                weapon.getShortDisplayName().toUpperCase(),
                res.newCritPercent(),
                weapon.getCritMultiplier(),
                cost);
        state.showTacticalBanner(msg, new Color(255, 140, 60), 3.2);
        return res;
    }

    /**
     * Vẽ đồ họa Bàn Kỹ Thuật Quân Khí Dã Chiến (Field Gunsmith Workbench) phong cách chiến tranh hiện đại.
     */
    public void render(Graphics2D g, Camera camera, double playerX, double playerY, double playerSize,
                       Weapon activeWeapon, int playerCredits) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Bóng đổ bàn làm việc dã chiến trên sàn
        g2.setColor(new Color(15, 18, 16, 140));
        g2.fillRoundRect(screenX - 32, screenY - 18, 64, 38, 8, 8);

        // 2. Thân bàn kỹ thuật gỗ ép khung thép quân đội (Heavy-Duty Workbench)
        int benchW = 58;
        int benchH = 32;
        int bx = screenX - benchW / 2;
        int by = screenY - benchH / 2;

        // Khung thép màu xanh rêu quân đội (Olive Drab Steel Frame)
        g2.setColor(new Color(48, 56, 46));
        g2.fillRoundRect(bx, by, benchW, benchH, 6, 6);

        // Mặt bàn gỗ ép chịu lực dày màu nâu sẫm dã chiến
        g2.setColor(new Color(78, 64, 50));
        g2.fillRect(bx + 3, by + 3, benchW - 6, benchH - 6);

        // Nẹp viền thép kim loại chống mẻ mép bàn
        g2.setColor(new Color(95, 105, 95));
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawRoundRect(bx, by, benchW, benchH, 6, 6);

        // 3. Tấm lót vải/cao su tháo lắp súng màu vàng cát (Khaki Gunsmith Mat)
        g2.setColor(new Color(135, 120, 85));
        g2.fillRect(screenX - 16, screenY - 8, 32, 16);
        g2.setColor(new Color(105, 92, 65));
        g2.drawRect(screenX - 16, screenY - 8, 32, 16);

        // Thân súng trường đang được tháo bảo dưỡng trên tấm lót
        g2.setColor(new Color(32, 35, 38));
        g2.fillRect(screenX - 12, screenY - 2, 24, 4);
        g2.setColor(new Color(85, 90, 95));
        g2.fillRect(screenX - 6, screenY - 4, 8, 2); // Thoi nạp đạn

        // 4. Khối ê-tô cơ khí kẹp nòng súng bằng thép (Steel Bench Vise) bên góc trái bàn
        g2.setColor(new Color(55, 60, 65));
        g2.fillRect(bx + 4, by + 4, 10, 10);
        g2.setColor(new Color(150, 155, 160));
        g2.fillRect(bx + 2, by + 7, 14, 3); // Mỏ kẹp ê-tô
        g2.setColor(new Color(200, 205, 210));
        g2.fillRect(bx + 1, by + 5, 2, 7); // Tay vặn chữ T

        // 5. Hộp đựng dụng cụ cơ khí quân sự màu đỏ dã chiến (Metal Tool Chest) bên góc phải
        g2.setColor(new Color(165, 45, 38));
        g2.fillRoundRect(bx + benchW - 16, by + 4, 12, 9, 2, 2);
        g2.setColor(new Color(220, 215, 180));
        g2.fillRect(bx + benchW - 11, by + 7, 3, 2); // Khóa gài kim loại màu bạc

        // 6. Đèn bàn kỹ thuật dã chiến (Tactical Gooseneck Lamp)
        g2.setColor(new Color(30, 32, 35));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawArc(bx + benchW - 10, by + benchH - 14, 12, 12, 90, 180); // Cổ uốn kim loại
        // Chao đèn xanh quân sự rọi quầng sáng ấm xuống mặt bàn
        g2.setColor(new Color(50, 70, 55));
        g2.fillOval(screenX + 10, screenY + 2, 8, 6);
        // Quầng sáng vàng ấm dã chiến tỏa xuống mặt bàn nhấp nháy thở nhẹ
        int pulseAlpha = (int) (40 + 8 * Math.sin(animTimer));
        g2.setColor(new Color(255, 235, 150, Math.clamp(pulseAlpha, 20, 60)));
        g2.fillOval(screenX - 22, screenY - 14, 44, 28);

        // 7. Hiệu ứng tia lửa cơ khí nhỏ khi vừa hoàn tất hiệu chỉnh
        if (flashTimer > 0) {
            double p = 1.0 - (flashTimer / 0.50);
            int sparkR = (int) (10 + 25 * p);
            int alpha = (int) Math.max(0, 240 * (1.0 - p));
            g2.setColor(new Color(255, 220, 80, alpha));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(screenX - sparkR, screenY - sparkR, sparkR * 2, sparkR * 2);
            // Tia lửa nhỏ tóe ra
            g2.setColor(new Color(255, 245, 160, alpha));
            g2.drawLine(screenX - 8, screenY - 8, screenX - sparkR, screenY - sparkR);
            g2.drawLine(screenX + 8, screenY - 8, screenX + sparkR, screenY - sparkR);
            g2.drawLine(screenX, screenY - 10, screenX, screenY - sparkR - 4);
        }

        // 8. Bảng Kẹp Hồ Sơ Kỹ Thuật Quân Sự (Tactical Clipboard HUD)
        // Chỉ hiển thị bảng nổi khi người chơi đã tiến vào phòng và ở cự ly tiếp cận (<= 140px).
        // Ngăn chặn hoàn toàn việc bảng nổi che tầm nhìn lối vào khi người chơi còn ở ngoài cửa / hành lang.
        double distToPlayer = Math.hypot((playerX + playerSize / 2.0) - x, (playerY + playerSize / 2.0) - y);
        if (distToPlayer <= 140.0) {
            renderGunsmithClipboardHUD(g2, screenX, screenY, isPlayerInRange(playerX, playerY, playerSize),
                    activeWeapon, playerCredits);
        }

        g2.dispose();
    }

    /**
     * Vẽ bảng kẹp hồ sơ kỹ thuật quân sự dã chiến nổi phía trên bàn làm việc.
     */
    private void renderGunsmithClipboardHUD(Graphics2D g, int sx, int sy, boolean inRange,
                                            Weapon weapon, int playerCredits) {
        int cost = getCurrentCost(weapon);
        boolean canAfford = playerCredits >= cost;

        int boxW = inRange ? 320 : 220;
        int boxH = inRange ? 76 : 46;
        int boxX = sx - boxW / 2;
        int boxY = sy - 44 - boxH;

        // 1. Tấm bìa kẹp hồ sơ dã chiến màu xám thép nẹp viền quân sự
        g.setColor(new Color(26, 30, 27, 235));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Viền kim loại dã chiến (xanh rêu / vàng hổ phách cảnh báo)
        Color borderColor = canAfford ? new Color(190, 165, 80) : new Color(225, 75, 75);
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Khối kẹp kim loại mạ bạc ở mép trên bảng hồ sơ
        g.setColor(new Color(160, 165, 170));
        g.fillRoundRect(sx - 16, boxY - 4, 32, 7, 2, 2);
        g.setColor(new Color(80, 85, 90));
        g.drawRoundRect(sx - 16, boxY - 4, 32, 7, 2, 2);

        // Dòng 1: Tiêu đề bảng kỹ thuật
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(245, 215, 80));
        g.drawString("★ BÀN KỸ THUẬT QUÂN KHÍ DÃ CHIẾN", boxX + 12, boxY + 18);

        // Dòng 2: Hướng dẫn [F] và Chi phí Credits
        boolean isLevel1 = (weapon != null && weapon.getLevel() <= 1);
        String actionLabel = isLevel1 ? "[F] ROLL CHÍ MẠNG SÚNG LV.1" : "[F] HIỆU CHỈNH KHÍ TÀI";
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        if (canAfford) {
            g.setColor(new Color(255, 235, 120));
            g.drawString(String.format("%s (%d CREDITS)", actionLabel, cost), boxX + 12, boxY + 36);
        } else {
            g.setColor(new Color(255, 95, 95));
            g.drawString(String.format("%s (%d CREDITS) - THIẾU!", actionLabel, cost), boxX + 12, boxY + 36);
        }

        // Khi lại gần: Hiển thị thông số vũ khí trên tay (DMG, RPM, ACC, Crit) và ghi chú chi phí lũy tiến
        if (inRange) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(215, 225, 215));

            if (weapon != null) {
                String weaponInfo = String.format("• Khí tài: %s (Lv.%d | Crit: %d%% x%.2f | DPS: %d/%d)",
                        weapon.getShortDisplayName(),
                        weapon.getLevel(),
                        weapon.getCritChancePercent(),
                        weapon.getCritMultiplier(),
                        weapon.getNormalDPS(),
                        weapon.getCritDPS());
                g.drawString(weaponInfo, boxX + 12, boxY + 53);
            } else {
                g.drawString("• Chưa trang bị vũ khí trên tay", boxX + 12, boxY + 53);
            }

            g.setColor(new Color(170, 185, 170));
            g.drawString(String.format("• Lần hiệu chỉnh thứ %d (Chi phí tăng lũy tiến)", reforgeCount + 1),
                    boxX + 12, boxY + 68);
        }
    }
}
