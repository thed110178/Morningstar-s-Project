package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.PlayingState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bia tập bắn di động lạng lách (Strafing Target Dummy) trong Trường bắn bên trái của The Range.
 *
 * <p>WHY: Giúp người chơi luyện tập kỹ năng rê chuột bám đuổi mục tiêu (tracking) và vẩy tâm
 * (flick shot) vào các mục tiêu đang chuyển động linh hoạt.
 *
 * <p>Đặc tính:
 * <ul>
 *   <li>Di chuyển lạng lách qua lại trong vùng quy định với vận tốc và hướng ngẫu nhiên.</li>
 *   <li>Cho phép người chơi điều chỉnh linh hoạt lượng máu (100, 250, 500, 1000 HP).</li>
 *   <li>Tự động hồi sinh với hiệu ứng triệu hồi sau khi bị tiêu diệt.</li>
 * </ul>
 */
public class StrafingTargetDummy extends TargetDummy {

    private double minX, maxX, minY, maxY;
    private double vx, vy;
    private double strafeTimer = 0.0;
    private double strafeDuration = 1.5;
    private double respawnTimer = 0.0;
    private boolean waitingToRespawn = false;

    public StrafingTargetDummy(double x, double y, int maxHealth,
                               double minX, double maxX, double minY, double maxY) {
        super(x, y, maxHealth, 32.0, false, "Strafing Dummy");
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        pickNewStrafeVelocity();
    }

    /**
     * Gieo ngẫu nhiên vận tốc di chuyển lạng lách mới.
     */
    private void pickNewStrafeVelocity() {
        double speed = ThreadLocalRandom.current().nextDouble(90.0, 150.0);
        // Ưu tiên di chuyển theo trục Y (lên xuống) và trục X (qua lại) trong trường bắn trái
        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.strafeDuration = ThreadLocalRandom.current().nextDouble(0.8, 2.2);
        this.strafeTimer = 0.0;
    }

    /**
     * Cấu hình lại mức máu tối đa và hồi đầy máu ngay lập tức.
     */
    public void configureHealth(int newMaxHealth) {
        this.maxHealth = Math.max(1, newMaxHealth);
        this.currentHealth = this.maxHealth;
        this.alive = true;
        this.waitingToRespawn = false;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        // Xử lý cơ chế hồi sinh tự động khi bị hạ gục
        if (!alive) {
            if (!waitingToRespawn) {
                waitingToRespawn = true;
                respawnTimer = 0.65; // Đợi 0.65s trước khi xuất hiện lại
            }
            respawnTimer -= deltaTime;
            if (respawnTimer <= 0) {
                respawn();
            }
            return;
        }

        // Logic di chuyển lạng lách (Strafing movement)
        strafeTimer += deltaTime;
        if (strafeTimer >= strafeDuration) {
            pickNewStrafeVelocity();
        }

        x += vx * deltaTime;
        y += vy * deltaTime;

        // Giới hạn trong phạm vi trường bắn và bật ngược lại khi chạm biên
        double half = size / 2.0;
        if (x - half < minX) {
            x = minX + half;
            vx = Math.abs(vx);
        } else if (x + half > maxX) {
            x = maxX - half;
            vx = -Math.abs(vx);
        }

        if (y - half < minY) {
            y = minY + half;
            vy = Math.abs(vy);
        } else if (y + half > maxY) {
            y = maxY - half;
            vy = -Math.abs(vy);
        }
    }

    /**
     * Hồi sinh tại một vị trí ngẫu nhiên trong trường bắn trái kèm hiệu ứng cảnh báo triệu hồi.
     */
    public void respawn() {
        double half = size / 2.0;
        this.x = ThreadLocalRandom.current().nextDouble(minX + half, maxX - half);
        this.y = ThreadLocalRandom.current().nextDouble(minY + half, maxY - half);
        this.currentHealth = this.maxHealth;
        this.alive = true;
        this.waitingToRespawn = false;
        pickNewStrafeVelocity();
        setSpawnTelegraph(0.8); // Hiệu ứng triệu hồi 0.8 giây
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (!alive) return;
        super.render(g, camera);

        // Vẽ thêm họa tiết nhận diện bia tập di động (Mũi tên chỉ hướng hoặc viền xanh neon tác chiến)
        int screenCX = camera.worldToScreenX(x);
        int screenCY = camera.worldToScreenY(y);
        int half = (int) (size / 2.0);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(60, 220, 255, 180));
        g2.setStroke(new BasicStroke(1.5f));
        // 4 góc khung ngắm tracking
        int cornerLen = 5;
        g2.drawLine(screenCX - half - 2, screenCY - half - 2, screenCX - half - 2 + cornerLen, screenCY - half - 2);
        g2.drawLine(screenCX - half - 2, screenCY - half - 2, screenCX - half - 2, screenCY - half - 2 + cornerLen);

        g2.drawLine(screenCX + half + 2, screenCY - half - 2, screenCX + half + 2 - cornerLen, screenCY - half - 2);
        g2.drawLine(screenCX + half + 2, screenCY - half - 2, screenCX + half + 2, screenCY - half - 2 + cornerLen);

        g2.drawLine(screenCX - half - 2, screenCY + half + 2, screenCX - half - 2 + cornerLen, screenCY + half + 2);
        g2.drawLine(screenCX - half - 2, screenCY + half + 2, screenCX - half - 2, screenCY + half + 2 - cornerLen);

        g2.drawLine(screenCX + half + 2, screenCY + half + 2, screenCX + half + 2 - cornerLen, screenCY + half + 2);
        g2.drawLine(screenCX + half + 2, screenCY + half + 2, screenCX + half + 2, screenCY + half + 2 - cornerLen);

        g2.dispose();
    }
}
