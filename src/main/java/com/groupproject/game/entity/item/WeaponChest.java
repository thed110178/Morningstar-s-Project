package com.groupproject.game.entity.item;

import com.groupproject.game.core.Camera;
import com.groupproject.game.entity.weapon.AssaultRifle;
import com.groupproject.game.entity.weapon.Shotgun;
import com.groupproject.game.entity.weapon.Weapon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hòm tiếp tế vũ khí chính (Tactical Weapon Chest / Supply Crate).
 *
 * <p>WHY: Phần thưởng roguelite sau khi dọn sạch phòng chiến đấu.
 * Khi người chơi tiến lại gần hòm, nắp hòm sẽ TỰ ĐỘNG BẬT MỞ và làm lộ diện khẩu súng
 * bên trong (nổi lơ lửng với hào quang rực rỡ). Người chơi sử dụng phím tương tác chính [F]
 * để nhặt vũ khí vào trang bị.
 */
public class WeaponChest {

    private final double x;
    private final double y;
    private final double size = 38.0;

    private boolean opened = false;       // Đã bật nắp hòm (tự động khi lại gần)
    private boolean collected = false;    // Người chơi đã bấm [F] để nhặt vào túi đồ
    private Weapon droppedWeapon;
    private double bobbingTimer = 0.0;

    /**
     * Khởi tạo hòm tiếp tế tại tọa độ thế giới.
     */
    public WeaponChest(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Mở nắp hòm và sinh ngẫu nhiên 50/50 vũ khí chính (AR hoặc Shotgun).
     */
    public Weapon open() {
        if (!opened) {
            opened = true;
            boolean isAR = ThreadLocalRandom.current().nextBoolean();
            droppedWeapon = isAR ? new AssaultRifle() : new Shotgun();
        }
        return droppedWeapon;
    }

    /**
     * Cập nhật chuyển động nhấp nhô và tự động mở nắp khi người chơi tiếp cận.
     */
    public void update(double deltaTime, double playerX, double playerY, double playerSize) {
        bobbingTimer += deltaTime * 3.5;

        // Khi người chơi đi ra chỗ hòm: Hòm TỰ ĐỘNG MỞ và hiện ra khẩu súng bên trong
        if (!opened && isPlayerInRange(playerX, playerY, playerSize)) {
            open();
        }
    }

    /**
     * Thu thập vũ khí vào túi đồ khi người chơi ấn phím tương tác chính [F].
     */
    public Weapon collect() {
        if (!opened) {
            open();
        }
        if (!collected) {
            collected = true;
            return droppedWeapon;
        }
        return null;
    }

    /**
     * Kiểm tra người chơi có đang đứng trong phạm vi tương tác (bán kính ~55px).
     */
    public boolean isPlayerInRange(double px, double py, double pSize) {
        double chestCenterX = x + size / 2.0;
        double chestCenterY = y + size / 2.0;
        double playerCenterX = px + pSize / 2.0;
        double playerCenterY = py + pSize / 2.0;

        double distSq = Math.pow(chestCenterX - playerCenterX, 2) + Math.pow(chestCenterY - playerCenterY, 2);
        return distSq <= 58.0 * 58.0;
    }

    /**
     * Vẽ mô hình hòm tiếp tế, hiệu ứng phát quang, vũ khí nổi lơ lửng và lời nhắc [F].
     */
    public void render(Graphics2D g, Camera camera, boolean showPrompt) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Hiệu ứng nhấp nhô nhẹ lơ lửng của vật phẩm thưởng
        double floatOffset = Math.sin(bobbingTimer) * 4.0;
        int renderY = (int) (screenY + floatOffset);

        if (!opened) {
            // --- 1. TRẠNG THÁI CHƯA MỞ (Chờ người chơi tiếp cận) ---
            // Hào quang vàng kim dưới chân
            g.setColor(new Color(255, 215, 60, 45));
            g.fillOval(screenX - 8, renderY + 22, (int) size + 16, 16);

            // Thân hòm quân sự mạ vàng
            g.setColor(new Color(180, 130, 40));
            g.fillRoundRect(screenX, renderY, (int) size, (int) size, 6, 6);

            // Viền kim loại vàng bóng
            g.setColor(new Color(255, 225, 100));
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(screenX, renderY, (int) size, (int) size, 6, 6);

            // Khóa nắp hòm chính diện
            g.setColor(new Color(50, 40, 20));
            g.fillRect(screenX + (int) size / 2 - 4, renderY + (int) size / 2 - 4, 8, 8);
            g.setColor(new Color(255, 255, 200));
            g.drawRect(screenX + (int) size / 2 - 4, renderY + (int) size / 2 - 4, 8, 8);

        } else if (!collected) {
            // --- 2. TRẠNG THÁI ĐÃ TỰ ĐỘNG MỞ & LỘ DIỆN KHẨU SÚNG BÊN TRONG ---
            // Hào quang xung quanh miệng hòm
            g.setColor(new Color(255, 220, 80, 70));
            g.fillOval(screenX - 12, renderY - 14, (int) size + 24, 30);

            // Đáy hòm mở
            g.setColor(new Color(110, 85, 30));
            g.fillRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);
            g.setColor(new Color(60, 50, 25));
            g.drawRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);

            // Nắp hòm mở lật lên trên
            g.setColor(new Color(160, 120, 40));
            g.fillRect(screenX - 3, renderY + 2, (int) size + 6, 8);
            g.setColor(new Color(255, 220, 100));
            g.drawRect(screenX - 3, renderY + 2, (int) size + 6, 8);

            // VẼ MÔ HÌNH VŨ KHÍ NỔI LƠ LỬNG TRÊN MIỆNG HÒM
            int gunCenterY = renderY - 16;
            int gunCenterX = screenX + (int) size / 2;

            if (droppedWeapon instanceof AssaultRifle) {
                // Minh họa Assault Rifle (AR)
                g.setColor(new Color(45, 55, 65));
                g.fillRect(gunCenterX - 18, gunCenterY - 4, 34, 7); // Thân súng
                g.setColor(new Color(25, 30, 35));
                g.fillRect(gunCenterX + 8, gunCenterY - 2, 12, 3);  // Nòng súng
                g.fillRect(gunCenterX - 6, gunCenterY + 3, 6, 9);   // Băng đạn cong
                g.setColor(new Color(255, 215, 60));
                g.fillRect(gunCenterX + 19, gunCenterY - 3, 3, 5);  // Đầu nòng vàng
            } else if (droppedWeapon instanceof Shotgun) {
                // Minh họa Shotgun
                g.setColor(new Color(139, 69, 19));
                g.fillRect(gunCenterX - 14, gunCenterY - 4, 12, 8); // Báng gỗ
                g.setColor(new Color(55, 60, 70));
                g.fillRect(gunCenterX - 2, gunCenterY - 5, 24, 9);  // Nòng đôi to
                g.setColor(new Color(255, 140, 30));
                g.fillRect(gunCenterX + 21, gunCenterY - 4, 3, 7);  // Điểm nòng cam
            }

            // Nhãn tên vũ khí nổi phía trên
            String weaponTitle = "★ " + droppedWeapon.getName().toUpperCase() + " ★";
            renderTag(g, gunCenterX, gunCenterY - 16, weaponTitle, new Color(255, 215, 60));

            // Lời nhắc phím tương tác chính [F] để nhặt súng
            if (showPrompt) {
                String promptText = "[F] NHẶT " + droppedWeapon.getName().toUpperCase();
                renderPrompt(g, gunCenterX, gunCenterY - 38, promptText);
            }

        } else {
            // --- 3. TRẠNG THÁI ĐÃ NHẶT XONG (Hòm rỗng nắp mở) ---
            g.setColor(new Color(90, 70, 25));
            g.fillRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);
            g.setColor(new Color(50, 40, 20));
            g.drawRoundRect(screenX, renderY + 10, (int) size, (int) size - 10, 6, 6);

            // Nắp mở
            g.setColor(new Color(130, 95, 30));
            g.fillRect(screenX - 3, renderY + 2, (int) size + 6, 8);
        }

        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderTag(Graphics2D g, int centerX, int centerY, String text, Color textColor) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int pad = 6;

        int boxW = textW + pad * 2;
        int boxH = 18;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(15, 20, 30, 210));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);
        g.setColor(new Color(60, 80, 110));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 4, 4);

        g.setColor(textColor);
        g.drawString(text, boxX + pad, boxY + 13);
    }

    private void renderPrompt(Graphics2D g, int centerX, int centerY, String text) {
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int pad = 8;

        int boxW = textW + pad * 2;
        int boxH = 22;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(15, 20, 30, 230));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(60, 220, 160)); // Viền dạ quang tương tác chính [F]
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Phím [F] nổi bật màu vàng
        g.setColor(new Color(255, 220, 60));
        g.drawString("[F]", boxX + pad, boxY + 15);

        // Chữ nội dung
        g.setColor(Color.WHITE);
        g.drawString(text.substring(3), boxX + pad + fm.stringWidth("[F] "), boxY + 15);
    }

    public boolean isOpened() {
        return opened;
    }

    public boolean isCollected() {
        return collected;
    }

    public Weapon getDroppedWeapon() {
        return droppedWeapon;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
