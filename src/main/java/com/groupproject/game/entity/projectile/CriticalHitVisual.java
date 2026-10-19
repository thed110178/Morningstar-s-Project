package com.groupproject.game.entity.projectile;

import com.groupproject.game.core.Camera;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Visual feedback spawned when an enemy entity takes critical damage.
 *
 * <p>WHY: Provides clean, tactical feedback matching the game's gritty military sci-fi theme.
 * Displays a sleek, punchy "CRIT! [damage]" text indicator in bold crimson red drifting upward
 * above the impacted target without distracting arcade clutter.
 */
public class CriticalHitVisual {

    private final double x;
    private final double y;
    private final int damage;
    private final double maxDuration;
    private double timer;

    public CriticalHitVisual(double x, double y, int damage) {
        this(x, y, damage, 0.42);
    }

    public CriticalHitVisual(double x, double y, int damage, double duration) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.maxDuration = duration;
        this.timer = duration;
    }

    public void update(double deltaTime) {
        if (timer > 0) {
            timer -= deltaTime;
        }
    }

    public boolean isFinished() {
        return timer <= 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public void render(Graphics2D g, Camera camera) {
        if (isFinished()) return;

        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);

        double progress = 1.0 - (timer / maxDuration); // 0.0 -> 1.0
        int alpha = (int) Math.max(0, 255 * (1.0 - progress));

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Upward floating drift animation above the target head
        int textY = screenY - 20 - (int) (26 * Math.sin(progress * Math.PI / 2));
        String text = "CRIT! " + damage;

        // Dynamic scale pop effect: punchy start, settling quickly
        float fontSize = 14.0f;
        if (progress < 0.15) {
            fontSize = (float) (14.0 + 3.0 * (1.0 - progress / 0.15));
        }
        Font font = new Font("SansSerif", Font.BOLD, (int) fontSize);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textX = screenX - (textW / 2);

        // Dark drop shadow outline for crisp contrast against any floor tile
        int shadowAlpha = Math.min(230, alpha);
        g2.setColor(new Color(0, 0, 0, shadowAlpha));
        g2.drawString(text, textX + 1, textY + 1);
        g2.drawString(text, textX - 1, textY - 1);
        g2.drawString(text, textX + 1, textY - 1);
        g2.drawString(text, textX - 1, textY + 1);
        g2.drawString(text, textX, textY + 2);

        // Searing crimson-red text color matching tactical combat theme
        g2.setColor(new Color(255, 60, 50, alpha));
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
