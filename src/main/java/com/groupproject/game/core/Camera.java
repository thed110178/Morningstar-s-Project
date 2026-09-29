package com.groupproject.game.core;

/**
 * 2D Orthographic-like Camera with smooth linear interpolation (Lerp) tracking
 * and world boundary clamping.
 */
public class Camera {

    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private final int viewportWidth;
    private final int viewportHeight;
    private final double lerpFactor = 0.1; // Smooth follow speed

    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Updates camera position smoothly following a target coordinate.
     */
    public void update(double focusX, double focusY) {
        targetX = focusX - (viewportWidth / 2.0);
        targetY = focusY - (viewportHeight / 2.0);

        // Smooth Lerp tracking
        x += (targetX - x) * lerpFactor;
        y += (targetY - y) * lerpFactor;

        // Clamp within world boundaries
        clamp();
    }

    private void clamp() {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > GameConfig.WORLD_WIDTH - viewportWidth) {
            x = GameConfig.WORLD_WIDTH - viewportWidth;
        }
        if (y > GameConfig.WORLD_HEIGHT - viewportHeight) {
            y = GameConfig.WORLD_HEIGHT - viewportHeight;
        }
    }

    public double screenToWorldX(int screenX) {
        return screenX + x;
    }

    public double screenToWorldY(int screenY) {
        return screenY + y;
    }

    public int worldToScreenX(double worldX) {
        return (int) Math.round(worldX - x);
    }

    public int worldToScreenY(double worldY) {
        return (int) Math.round(worldY - y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }
}
