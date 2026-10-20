package com.groupproject.game.entity.projectile;

import com.groupproject.game.core.Camera;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Visual explosion effect spawned upon heavy cannon shell detonation.
 *
 * <p>WHY: Provides punchy visual and spatial feedback showing the shockwave radius,
 * fiery core, and smoke dissipation, helping the operative immediately perceive the blast perimeter.
 */
public class ExplosionVisual {

    private final double x;
    private final double y;
    private final double maxRadius;
    private final double maxDuration;
    private double timer;

    public ExplosionVisual(double x, double y, double maxRadius, double duration) {
        this.x = x;
        this.y = y;
        this.maxRadius = maxRadius;
        this.maxDuration = duration;
        this.timer = duration;
    }

    public void update(double deltaTime) {
        if (timer > 0) {
            timer -= deltaTime;
        }
    }

    public boolean isExpired() {
        return timer <= 0;
    }

    public boolean isFinished() {
        return isExpired();
    }

    public void render(Graphics2D g, Camera camera) {
        if (isExpired()) return;

        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        double progress = 1.0 - (timer / maxDuration); // 0.0 -> 1.0
        int currentRadius = (int) (maxRadius * (0.2 + progress * 0.8));
        int alpha = (int) Math.max(0, 240 * (1.0 - progress));

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Primary outer shockwave blast ring (Sóng chấn động sơ cấp rực lửa viền ngoài)
        g2.setColor(new Color(255, 75, 20, (int) (alpha * 0.75)));
        g2.setStroke(new BasicStroke((float) (6.0 * (1.0 - progress * 0.75))));
        g2.drawOval(screenX - currentRadius, screenY - currentRadius, currentRadius * 2, currentRadius * 2);

        // 2. Secondary inner shockwave ring (Sóng chấn động thứ cấp rực cam)
        int secRadius = (int) (currentRadius * 0.82);
        if (secRadius > 4) {
            g2.setColor(new Color(255, 140, 30, (int) (alpha * 0.60)));
            g2.setStroke(new BasicStroke((float) (3.5 * (1.0 - progress * 0.6))));
            g2.drawOval(screenX - secRadius, screenY - secRadius, secRadius * 2, secRadius * 2);
        }

        // 3. Shrapnel & incandescent spark trails (Chùm mảnh văng bộc phá rực lửa phóng tỏa tròn)
        g2.setColor(new Color(255, 230, 90, (int) (alpha * 0.95)));
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 12; i++) {
            double sparkAngle = i * (Math.PI * 2.0 / 12.0) + (x * 0.05);
            double innerR = currentRadius * 0.55;
            double outerR = currentRadius * (1.02 + 0.18 * Math.sin(i * 1.9 + x));
            int sx1 = (int) (screenX + Math.cos(sparkAngle) * innerR);
            int sy1 = (int) (screenY + Math.sin(sparkAngle) * innerR);
            int sx2 = (int) (screenX + Math.cos(sparkAngle) * outerR);
            int sy2 = (int) (screenY + Math.sin(sparkAngle) * outerR);
            g2.drawLine(sx1, sy1, sx2, sy2);
        }

        // 4. Turbulent billowing fireball lobes (Các cụm lửa cuộn hữu cơ tạo quả cầu lửa khổng lồ)
        int lobeCount = 6;
        int lobeDist = (int) (currentRadius * 0.38);
        int lobeR = (int) (currentRadius * 0.42);
        g2.setColor(new Color(255, 110, 25, (int) (alpha * 0.70)));
        for (int i = 0; i < lobeCount; i++) {
            double a = i * (Math.PI * 2.0 / lobeCount) + progress * 0.8;
            int lx = (int) (screenX + Math.cos(a) * lobeDist);
            int ly = (int) (screenY + Math.sin(a) * lobeDist);
            g2.fillOval(lx - lobeR, ly - lobeR, lobeR * 2, lobeR * 2);
        }

        // 5. Fiery expanding inner core (Lõi cầu lửa vàng cam bộc phá)
        int coreRadius = (int) (currentRadius * 0.60);
        g2.setColor(new Color(255, 175, 35, (int) (alpha * 0.88)));
        g2.fillOval(screenX - coreRadius, screenY - coreRadius, coreRadius * 2, coreRadius * 2);

        // 6. Blinding white-hot epicenter (Tâm nổ siêu nhiệt chớp trắng lóa mắt)
        int whiteRadius = (int) (currentRadius * 0.30);
        g2.setColor(new Color(255, 255, 240, alpha));
        g2.fillOval(screenX - whiteRadius, screenY - whiteRadius, whiteRadius * 2, whiteRadius * 2);

        g2.dispose();
    }
}
