package com.groupproject.game.entity.item;

import com.groupproject.game.core.Camera;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Thực thể vật phẩm Hồi Phục Sinh Lực (Health Pickup) rơi trên mặt đất trong hầm ngục.
 *
 * <p>QUY TẮC TƯƠNG TÁC:
 * 1. Không tự động nhặt khi đi ngang qua.
 * 2. Người chơi phải chủ động bấm phím tương tác [F] khi đứng trong phạm vi để sử dụng.
 * 3. Nếu máu của người chơi đã đầy (100/100 HP), hệ thống từ chối tiêu thụ để tránh lãng phí.
 */
public class HealthPickup {

    /**
     * Kích cỡ và định lượng hồi phục của Hộp Cứu Thương.
     */
    public enum HealthSize {
        SMALL(25, "HỘP SƠ CỨU NHỎ", "+25 HP", new Color(60, 230, 120)),
        LARGE(50, "HỘP CỨU THƯƠNG LỚN", "+50 HP", new Color(255, 215, 60));

        private final int healAmount;
        private final String displayName;
        private final String tag;
        private final Color accentColor;

        HealthSize(int healAmount, String displayName, String tag, Color accentColor) {
            this.healAmount = healAmount;
            this.displayName = displayName;
            this.tag = tag;
            this.accentColor = accentColor;
        }

        public int getHealAmount() {
            return healAmount;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTag() {
            return tag;
        }

        public Color getAccentColor() {
            return accentColor;
        }
    }

    private final double x;
    private final double y;
    private final HealthSize size;
    private boolean used = false;
    private double bobbingTimer = 0.0;

    /**
     * Khởi tạo Hộp Cứu Thương tại tọa độ thế giới chỉ định.
     */
    public HealthPickup(double x, double y, HealthSize size) {
        this.x = x;
        this.y = y;
        this.size = size != null ? size : HealthSize.SMALL;
    }

    public void update(double deltaTime) {
        bobbingTimer += deltaTime * 3.5;
    }

    /**
     * Kiểm tra người chơi có đang đứng trong phạm vi tương tác (bán kính ~45px) hay không.
     */
    public boolean isPlayerInRange(double playerX, double playerY, double playerSize) {
        if (used) return false;
        double pCenterX = playerX + playerSize / 2.0;
        double pCenterY = playerY + playerSize / 2.0;
        double distSq = (x - pCenterX) * (x - pCenterX) + (y - pCenterY) * (y - pCenterY);
        return distSq <= 45.0 * 45.0;
    }

    /**
     * Xử lý hành động bấm phím [F] để sử dụng Hộp Cứu Thương.
     *
     * @param state Trạng thái màn chơi chứa chỉ số máu của người chơi.
     * @return {@code true} nếu sử dụng thành công; {@code false} nếu người chơi đã đầy máu.
     */
    public boolean interact(PlayingState state) {
        if (used || state == null) return false;

        if (state.getPlayerCurrentHealth() >= state.getPlayerMaxHealth()) {
            state.showTacticalBanner("MÁU ĐÃ ĐẠT TỐI ĐA (" + state.getPlayerMaxHealth() + "/" + state.getPlayerMaxHealth() + " HP)! KHÔNG CẦN CỨU THƯƠNG",
                    new Color(255, 180, 50), 2.2);
            return false;
        }

        int heal = size.getHealAmount();
        state.healPlayer(heal);
        used = true;
        state.showTacticalBanner("+ ĐÃ SỬ DỤNG " + size.getDisplayName() + " (+" + heal + " HP)!",
                new Color(60, 240, 140), 2.5);
        return true;
    }

    /**
     * Hiển thị mô hình Hộp Cứu Thương 2D, vòng hào quang ngọc bích chữa lành và gợi ý tương tác phím [F].
     */
    public void render(Graphics2D g, Camera camera, boolean showPrompt) {
        if (used) return;

        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Viewport Culling: Bỏ qua nếu hộp máu nằm ngoài tầm nhìn camera
        if (screenX < -40 || screenX > com.groupproject.game.core.GameConfig.SCREEN_WIDTH + 40 ||
            screenY < -40 || screenY > com.groupproject.game.core.GameConfig.SCREEN_HEIGHT + 40) {
            return;
        }

        // Hiệu ứng nhấp nhô lơ lửng
        int bobOffset = (int) (Math.sin(bobbingTimer) * 3.5);
        int drawY = screenY + bobOffset;

        // 1. Bóng đổ trên mặt đất (Ground Shadow)
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(screenX - 14, screenY + 10, 28, 10);

        // 2. Vòng hào quang xanh lục bích chữa lành (Healing Aura)
        float pulse = (float) (Math.sin(bobbingTimer * 1.5) * 0.3 + 0.7);
        int auraRadius = size == HealthSize.LARGE ? 22 : 18;
        g.setColor(new Color(50, 230, 120, (int) (40 * pulse)));
        g.fillOval(screenX - auraRadius, drawY - auraRadius, auraRadius * 2, auraRadius * 2);

        // 3. Hộp Cứu Thương Kim Loại Quân Sự (Nanite Medkit Case)
        int boxW = size == HealthSize.LARGE ? 28 : 24;
        int boxH = size == HealthSize.LARGE ? 24 : 20;
        int boxX = screenX - boxW / 2;
        int boxY = drawY - boxH / 2;

        // Thân hộp bọc composite trắng ngà / xám quân sự
        g.setColor(new Color(240, 245, 248));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Viền kim loại công nghệ cao (Xanh ngọc lục bảo hoặc Vàng kim cho Hộp Lớn)
        g.setColor(size.getAccentColor());
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setStroke(new BasicStroke(1.0f));

        // Quai xách kim loại
        g.setColor(new Color(120, 130, 145));
        g.drawRoundRect(boxX + boxW / 2 - 4, boxY - 3, 8, 4, 2, 2);

        // 4. Biểu tượng Dấu Thập Y Tế Chữa Lành (Medical Cross)
        g.setColor(new Color(40, 200, 90));
        int crossW = 4;
        int crossL = 10;
        // Thanh ngang
        g.fillRect(screenX - crossL / 2, drawY - crossW / 2, crossL, crossW);
        // Thanh dọc
        g.fillRect(screenX - crossW / 2, drawY - crossL / 2, crossW, crossL);

        // 5. Nhãn chỉ số nhỏ lơ lửng (+25 HP / +50 HP)
        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
        FontMetrics fm = g.getFontMetrics();
        String tag = size.getTag();
        int tagW = fm.stringWidth(tag);
        g.setColor(new Color(15, 25, 20, 200));
        g.fillRoundRect(screenX - tagW / 2 - 4, boxY - 14, tagW + 8, 12, 4, 4);
        g.setColor(size.getAccentColor());
        g.drawString(tag, screenX - tagW / 2, boxY - 4);

        // 6. Khung Gợi Ý Tương Tác Phím [F] Khi Người Chơi Lại Gần
        if (showPrompt) {
            String prompt = "[F] DÙNG " + size.getDisplayName() + " (" + size.getTag() + ")";
            g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(11.0f));
            FontMetrics pfm = g.getFontMetrics();
            int promptW = pfm.stringWidth(prompt);
            int promptPad = 8;
            int cardW = promptW + promptPad * 2;
            int cardH = 24;
            int cardX = screenX - cardW / 2;
            int cardY = boxY - 42;

            g.setColor(new Color(15, 22, 32, 225));
            g.fillRoundRect(cardX, cardY, cardW, cardH, 6, 6);
            g.setColor(new Color(60, 230, 140));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 6, 6);
            g.setStroke(new BasicStroke(1.0f));

            g.setColor(new Color(255, 255, 255));
            g.drawString(prompt, cardX + promptPad, cardY + 16);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public HealthSize getSize() {
        return size;
    }

    public int getHealAmount() {
        return size.getHealAmount();
    }

    public boolean isUsed() {
        return used;
    }
}
