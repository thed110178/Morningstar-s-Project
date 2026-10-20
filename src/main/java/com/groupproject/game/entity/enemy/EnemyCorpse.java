package com.groupproject.game.entity.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Thực thể xác kẻ địch (Enemy Corpse) xuất hiện khi mục tiêu bị tiêu diệt.
 *
 * <p>WHY: Thể hiện phản hồi thị giác chân thực cho trận chiến: Khi kẻ địch bị tiêu diệt,
 * để lại một cái xác hình đầu lâu tông màu xám nâu mờ nhạt (faded sepia-brown skull)
 * nhận lực đẩy lùi (knockback impulse) từ đòn kết liễu, trượt văng trên mặt sàn gạch hầm ngục,
 * giảm tốc theo ma sát địa hình (không xuyên tường), và từ từ mờ dần biến mất theo thời gian.
 */
public class EnemyCorpse {

    private double x;
    private double y;
    private final double size;
    private double vx;
    private double vy;
    private double rotation;
    private double angularVelocity;
    private double lifeTime;
    private final double maxLifeTime;
    private final boolean isBoss;

    /**
     * Khởi tạo xác kẻ địch với vị trí tâm, kích thước và xung lực đẩy lùi ban đầu.
     *
     * @param centerX        Tọa độ tâm X của kẻ địch tử trận.
     * @param centerY        Tọa độ tâm Y của kẻ địch tử trận.
     * @param enemySize      Kích thước hitbox của kẻ địch vừa chết.
     * @param knockbackAngle Góc văng đẩy lùi (radian) theo đòn kết liễu.
     * @param knockbackForce Xung lực đẩy lùi (px/s).
     * @param isBoss         Cờ đánh dấu nếu đây là Boss Tank cỡ đại.
     */
    public EnemyCorpse(double centerX, double centerY, double enemySize, double knockbackAngle, double knockbackForce, boolean isBoss) {
        this.isBoss = isBoss;
        // Kích thước đầu lâu: quái thường ~24-26px, Boss ~48-52px
        this.size = isBoss ? Math.max(46.0, enemySize * 0.70) : Math.max(22.0, enemySize * 0.85);
        this.x = centerX - this.size / 2.0;
        this.y = centerY - this.size / 2.0;
        this.maxLifeTime = isBoss ? 5.5 : 4.0;
        this.lifeTime = this.maxLifeTime;

        // Vận tốc trượt ban đầu từ lực knockback (tối thiểu 160 px/s để luôn có độ trượt văng)
        double initialSpeed = Math.max(160.0, knockbackForce);
        this.vx = Math.cos(knockbackAngle) * initialSpeed;
        this.vy = Math.sin(knockbackAngle) * initialSpeed;

        // Xoay nhẹ theo hướng văng và quán tính góc xoay
        this.rotation = knockbackAngle;
        this.angularVelocity = (Math.random() - 0.5) * 6.5;
    }

    /**
     * Cập nhật chuyển động trượt ma sát trên sàn và va chạm tường 2 trục độc lập.
     *
     * @param deltaTime  Thời gian delta trôi qua giữa 2 khung hình (giây).
     * @param dungeonMap Bản đồ hầm ngục kiểm tra va chạm tường và cửa.
     */
    public void update(double deltaTime, DungeonMap dungeonMap) {
        lifeTime -= deltaTime;

        double speed = Math.hypot(vx, vy);
        if (speed > 8.0) {
            // Hệ số ma sát mặt sàn gạch hầm ngục (trượt mượt mà trong ~0.6s - 1.2s rồi dừng hẳn)
            double friction = Math.max(0.0, 1.0 - 5.2 * deltaTime);
            vx *= friction;
            vy *= friction;

            rotation += angularVelocity * deltaTime;
            angularVelocity *= friction;

            double moveX = vx * deltaTime;
            double moveY = vy * deltaTime;

            // Trượt va chạm 2 trục độc lập (Decoupled axis sliding) để đầu lâu không xuyên tường
            if (moveX != 0) {
                double nextX = x + moveX;
                if (dungeonMap.isAreaWalkable(nextX, y, size, size)) {
                    x = nextX;
                } else {
                    vx = -vx * 0.25; // Nảy nhẹ khi đập vào vách tường kiên cố
                }
            }
            if (moveY != 0) {
                double nextY = y + moveY;
                if (dungeonMap.isAreaWalkable(x, nextY, size, size)) {
                    y = nextY;
                } else {
                    vy = -vy * 0.25; // Nảy nhẹ khi đập vào vách tường kiên cố
                }
            }
        } else {
            vx = 0.0;
            vy = 0.0;
            angularVelocity = 0.0;
        }
    }

    /**
     * Kết xuất đồ họa xác đầu lâu tông màu xám nâu dã chiến mờ (Faded Brown Skull) trên mặt sàn.
     *
     * @param g      Đối tượng đồ họa Graphics2D.
     * @param camera Camera thế giới.
     */
    public void render(Graphics2D g, Camera camera) {
        if (isFinished()) return;

        int screenX = camera.worldToScreenX(x + size / 2.0);
        int screenY = camera.worldToScreenY(y + size / 2.0);

        // Tính toán độ mờ (Alpha) động: rõ nét mờ nhẹ lúc đầu (~0.80), mờ dần ở 1.6s cuối
        float alpha = getAlpha();
        if (alpha <= 0.02f) return;

        int alphaInt = (int) (alpha * 255);
        int borderAlpha = Math.min(255, (int) (alpha * 230));
        int shadowAlpha = (int) (alpha * 85);

        AffineTransform oldTx = g.getTransform();
        g.rotate(rotation, screenX, screenY);

        int r = (int) (size / 2.0);

        // 1. Bóng đổ dã chiến trên sàn hầm ngục
        g.setColor(new Color(0, 0, 0, shadowAlpha));
        g.fillOval(screenX - (int) (r * 1.1), screenY - (int) (r * 0.4), (int) (r * 2.2), (int) (r * 0.9));

        // 2. Vòm hộp sọ (Cranium Dome) - Tông màu đầu lâu xám nâu mờ nhạt dã chiến
        Color skullColor = isBoss ? new Color(175, 130, 95, alphaInt) : new Color(165, 138, 108, alphaInt);
        Color skullBorder = new Color(90, 70, 50, borderAlpha);

        g.setColor(skullColor);
        int domeW = (int) (r * 1.85);
        int domeH = (int) (r * 1.60);
        g.fillOval(screenX - domeW / 2, screenY - domeH / 2 - 2, domeW, domeH);
        g.setColor(skullBorder);
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(screenX - domeW / 2, screenY - domeH / 2 - 2, domeW, domeH);

        // 3. Hàm răng xương (Jaw / Maxilla) mở rộng phía dưới
        int jawW = (int) (r * 1.15);
        int jawH = (int) (r * 0.85);
        int jawX = screenX - jawW / 2;
        int jawY = screenY + (int) (r * 0.15);

        g.setColor(skullColor);
        g.fillRoundRect(jawX, jawY, jawW, jawH, 3, 3);
        g.setColor(skullBorder);
        g.drawRoundRect(jawX, jawY, jawW, jawH, 3, 3);

        // Các rãnh răng xương (Teeth notches)
        Color teethNotchColor = new Color(55, 40, 30, borderAlpha);
        g.setColor(teethNotchColor);
        int toothSpacing = Math.max(2, jawW / 4);
        g.fillRect(screenX - toothSpacing, jawY + 2, 1, jawH - 3);
        g.fillRect(screenX, jawY + 2, 1, jawH - 3);
        g.fillRect(screenX + toothSpacing, jawY + 2, 1, jawH - 3);

        // 4. Hai hốc mắt rỗng tối sâu (Hollow Eye Sockets)
        Color socketColor = new Color(30, 20, 15, alphaInt);
        int eyeW = Math.max(3, (int) (r * 0.42));
        int eyeH = Math.max(3, (int) (r * 0.50));
        int eyeOffsetX = (int) (r * 0.38);
        int eyeY = screenY - (int) (r * 0.20);

        g.setColor(socketColor);
        g.fillOval(screenX - eyeOffsetX - eyeW / 2, eyeY, eyeW, eyeH);
        g.fillOval(screenX + eyeOffsetX - eyeW / 2, eyeY, eyeW, eyeH);

        // 5. Hốc mũi rỗng hình tam giác ngược (Nasal Cavity)
        int noseSize = Math.max(2, (int) (r * 0.22));
        int[] nx = {screenX, screenX - noseSize, screenX + noseSize};
        int[] ny = {screenY + (int) (r * 0.25), screenY + (int) (r * 0.05), screenY + (int) (r * 0.05)};
        g.fillPolygon(nx, ny, 3);

        // 6. Chi tiết đặc biệt cho xác Boss Tank: Tấm giáp titan bọc trán và gai cơ khí
        if (isBoss) {
            Color armorColor = new Color(110, 120, 135, alphaInt);
            g.setColor(armorColor);
            g.fillRoundRect(screenX - (int) (r * 0.75), screenY - domeH / 2 - 3, (int) (r * 1.5), 5, 2, 2);
            // Đốm năng lượng tàn lụi trong hốc mắt
            g.setColor(new Color(255, 60, 60, (int) (alpha * 160)));
            g.fillOval(screenX - eyeOffsetX - 1, eyeY + eyeH / 2 - 1, 3, 3);
            g.fillOval(screenX + eyeOffsetX - 1, eyeY + eyeH / 2 - 1, 3, 3);
        }

        g.setStroke(new BasicStroke(1.0f));
        g.setTransform(oldTx);
    }

    /**
     * Tính toán hệ số độ mờ Alpha (0.0f đến 0.80f).
     */
    public float getAlpha() {
        if (lifeTime <= 0) return 0.0f;
        double fadeDuration = 1.6;
        if (lifeTime > fadeDuration) {
            return 0.80f;
        }
        return (float) Math.max(0.0, Math.min(0.80, (lifeTime / fadeDuration) * 0.80));
    }

    public boolean isFinished() {
        return lifeTime <= 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getLifeTime() {
        return lifeTime;
    }

    public double getMaxLifeTime() {
        return maxLifeTime;
    }

    public boolean isBoss() {
        return isBoss;
    }
}
