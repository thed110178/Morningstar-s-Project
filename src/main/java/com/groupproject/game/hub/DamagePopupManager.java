package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quản lý các chỉ số sát thương bay lên (Floating Damage Numbers) khi bắn trúng Dummy trong The Range.
 *
 * <p>WHY: Mang lại phản hồi thị giác chuẩn xác tương tự như hiệu ứng Critical Hit trong chiến trường,
 * nhưng hỗ trợ đầy đủ cả sát thương thường (màu vàng nhạt/trắng) và sát thương chí mạng (màu đỏ rực rỡ "CRIT!").
 */
public class DamagePopupManager {

    public static class DamagePopup {
        private final double x;
        private final double y;
        private final int damage;
        private final boolean isCrit;
        private final double maxDuration;
        private double timer;
        private final double driftX; // Độ lệch ngang nhẹ để các số không đè khít lên nhau

        public DamagePopup(double x, double y, int damage, boolean isCrit) {
            this.x = x;
            this.y = y;
            this.damage = damage;
            this.isCrit = isCrit;
            this.maxDuration = isCrit ? 0.65 : 0.55;
            this.timer = this.maxDuration;
            this.driftX = ThreadLocalRandom.current().nextDouble(-12.0, 12.0);
        }

        public void update(double deltaTime) {
            if (timer > 0) {
                timer -= deltaTime;
            }
        }

        public boolean isFinished() {
            return timer <= 0;
        }

        public void render(Graphics2D g, Camera camera) {
            if (isFinished()) return;

            int screenX = camera.worldToScreenX(x + driftX);
            int screenY = camera.worldToScreenY(y);

            double progress = 1.0 - (timer / maxDuration);
            int alpha = (int) Math.clamp(255 * (1.0 - progress), 0, 255);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Hiệu ứng bay lên trên đầu dummy (bay cao khoảng 35px)
            int textY = screenY - 24 - (int) (32 * Math.sin(progress * Math.PI / 2));
            String text = isCrit ? ("CRIT! " + damage) : ("-" + damage);

            // Pop scale nhẹ ban đầu
            float fontSize = isCrit ? 15.0f : 13.0f;
            if (progress < 0.18) {
                fontSize += (float) (4.0 * (1.0 - progress / 0.18));
            }

            Font font = new Font("SansSerif", Font.BOLD, (int) fontSize);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textX = screenX - (textW / 2);

            // Đổ bóng viền đen 4 góc
            int shadowAlpha = Math.min(230, alpha);
            g2.setColor(new Color(0, 0, 0, shadowAlpha));
            g2.drawString(text, textX + 1, textY + 1);
            g2.drawString(text, textX - 1, textY - 1);
            g2.drawString(text, textX + 1, textY - 1);
            g2.drawString(text, textX - 1, textY + 1);
            g2.drawString(text, textX, textY + 2);

            // Màu chữ nổi bật: Sát thương thường màu vàng kem sáng, chí mạng màu đỏ rực
            Color textColor = isCrit ? new Color(255, 60, 50, alpha) : new Color(255, 230, 110, alpha);
            g2.setColor(textColor);
            g2.drawString(text, textX, textY);

            g2.dispose();
        }
    }

    private final List<DamagePopup> activePopups = new CopyOnWriteArrayList<>();

    public void addPopup(double x, double y, int damage, boolean isCrit) {
        activePopups.add(new DamagePopup(x, y, damage, isCrit));
    }

    public void update(double deltaTime) {
        for (DamagePopup dp : activePopups) {
            dp.update(deltaTime);
        }
        activePopups.removeIf(DamagePopup::isFinished);
    }

    public void render(Graphics2D g, Camera camera) {
        for (DamagePopup dp : activePopups) {
            dp.render(g, camera);
        }
    }

    public void clear() {
        activePopups.clear();
    }

    public int getActiveCount() {
        return activePopups.size();
    }
}
