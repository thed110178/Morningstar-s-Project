package com.groupproject.game.entity.item;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Thực thể Hộp đạn chiến thuật rơi trên mặt đất (Ammo Pickup Drop Entity).
 *
 * <p>WHY: Hiện thực hóa cơ chế nhặt chiến lợi phẩm trực quan theo phong cách Roguelite Shooter
 * (tương tự Soul Knight / Enter the Gungeon). Khi quái chết (rơi lượng nhỏ) hoặc khi clear phòng
 * (rơi 2 stack), các hộp đạn sẽ lơ lửng trên sàn. Khi người chơi bước vào bán kính tiếp cận (~38px),
 * hộp đạn sẽ tự động được hút/nhặt vào kho đạn dự trữ nếu kho chưa đầy.
 */
public class AmmoPickup {

    private final double x;
    private final double y;
    private final AmmoType type;
    private final int amount;
    private boolean collected = false;

    // Hiệu ứng chuyển động lơ lửng và ánh sáng nhấp nháy
    private double bobbingTimer;
    private static final double PICKUP_RADIUS = 38.0;

    /**
     * Khởi tạo một hộp đạn rơi tại vị trí chỉ định trong không gian thế giới.
     *
     * @param x      Tọa độ tâm X trong thế giới game.
     * @param y      Tọa độ tâm Y trong thế giới game.
     * @param type   Chủng loại đạn (AR, SMG, Shotgun).
     * @param amount Số lượng đạn chứa bên trong hộp.
     */
    public AmmoPickup(double x, double y, AmmoType type, int amount) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.amount = Math.max(1, amount);
        this.bobbingTimer = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2);
    }

    /**
     * Cập nhật hoạt ảnh và kiểm tra va chạm tự động thu thập với operative.
     */
    public void update(double deltaTime, double playerX, double playerY, double playerSize,
                       AmmoInventory inventory, PlayingState state) {
        if (collected) return;

        bobbingTimer += deltaTime * 4.0;

        // Tính khoảng cách Euclidean từ tâm hộp đạn tới tâm operative
        double playerCenterX = playerX + (playerSize / 2.0);
        double playerCenterY = playerY + (playerSize / 2.0);
        double dx = playerCenterX - x;
        double dy = playerCenterY - y;
        double distSq = dx * dx + dy * dy;

        if (distSq <= PICKUP_RADIUS * PICKUP_RADIUS) {
            // Kiểm tra kho dự trữ của loại đạn này có chỗ trống không
            if (inventory != null && !inventory.isFull(type)) {
                int added = inventory.addAmmo(type, amount);
                if (added > 0) {
                    this.collected = true;
                    GameSoundManager.play(SoundEffect.INTERACT, 0.85f);
                    if (state != null) {
                        state.showTacticalBanner(String.format("ĐÃ NHẶT +%d %s [%d/%d]",
                                added, type.getDisplayName().toUpperCase(),
                                inventory.getAmmo(type), type.getMaxCapacity()),
                                getGlowColor(), 2.0);
                    }
                }
            }
        }
    }

    /**
     * Vẽ mô hình hộp đạn 2D lơ lửng với hào quang rực rỡ và nhãn số lượng.
     */
    public void render(Graphics2D g, Camera camera) {
        if (collected) return;

        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        // Viewport Culling: Bỏ qua nếu hộp đạn nằm ngoài tầm nhìn camera
        if (screenX < -40 || screenX > com.groupproject.game.core.GameConfig.SCREEN_WIDTH + 40 ||
            screenY < -40 || screenY > com.groupproject.game.core.GameConfig.SCREEN_HEIGHT + 40) {
            return;
        }

        // Độ cao nhấp nhô lơ lửng theo nhịp sin (-4px đến +4px)
        int hoverOffset = (int) (Math.sin(bobbingTimer) * 4.0);
        int drawY = screenY + hoverOffset;

        // 1. Bóng đen mờ đổ xuống mặt sàn thực tế (không bay)
        g.setColor(new Color(0, 0, 0, 85));
        g.fillOval(screenX - 12, screenY + 6, 24, 10);

        // 2. Vòng hào quang phát sáng neon xung quanh hộp đạn
        Color glow = getGlowColor();
        g.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 55));
        g.fillOval(screenX - 16, drawY - 14, 32, 28);
        g.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 120));
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(screenX - 16, drawY - 14, 32, 28);

        // 3. Khung thân hộp đạn quân sự (Ammo Box Crate)
        int boxW = 20;
        int boxH = 14;
        int boxX = screenX - (boxW / 2);
        int boxY = drawY - (boxH / 2);

        // Thân hộp nền tối
        g.setColor(new Color(25, 30, 40));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 4, 4);

        // Vạch sơn nhận diện chủng loại đạn ở giữa
        g.setColor(glow);
        g.fillRect(boxX + 2, boxY + 4, boxW - 4, 6);

        // Viền kim loại hộp đạn
        g.setColor(new Color(200, 220, 245));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 4, 4);

        // 4. Nhãn số lượng đạn hiển thị phía trên hộp (VD: "+30 AR")
        g.setFont(com.groupproject.game.core.GameFontManager.getSmallBoldFont(10.0f));
        String label = "+" + amount + " " + type.name();
        FontMetrics fm = g.getFontMetrics();
        int labelW = fm.stringWidth(label);
        int textX = screenX - (labelW / 2);
        int textY = drawY - 16;

        // Nền đen bán trong suốt cho nhãn
        g.setColor(new Color(15, 20, 30, 200));
        g.fillRoundRect(textX - 4, textY - 10, labelW + 8, 13, 4, 4);
        g.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 180));
        g.drawRoundRect(textX - 4, textY - 10, labelW + 8, 13, 4, 4);

        // Chữ số lượng
        g.setColor(Color.WHITE);
        g.drawString(label, textX, textY);
    }

    /**
     * Lấy màu sắc hào quang đặc trưng cho từng loại đạn.
     */
    public Color getGlowColor() {
        return type.getColor();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public AmmoType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
