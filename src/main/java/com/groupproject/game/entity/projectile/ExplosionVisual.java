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

        // 1. Outer shockwave blast ring
        g2.setColor(new Color(255, 80, 20, (int) (alpha * 0.7)));
        g2.setStroke(new BasicStroke((float) (5.0 * (1.0 - progress * 0.7))));
        g2.drawOval(screenX - currentRadius, screenY - currentRadius, currentRadius * 2, currentRadius * 2);

        // 2. Fiery expanding core
        int coreRadius = (int) (currentRadius * 0.65);
        g2.setColor(new Color(255, 180, 40, (int) (alpha * 0.85)));
        g2.fillOval(screenX - coreRadius, screenY - coreRadius, coreRadius * 2, coreRadius * 2);

        // 3. Blinding white epicenter
        int whiteRadius = (int) (currentRadius * 0.35);
        g2.setColor(new Color(255, 255, 230, alpha));
        g2.fillOval(screenX - whiteRadius, screenY - whiteRadius, whiteRadius * 2, whiteRadius * 2);

        g2.dispose();
    }
}
