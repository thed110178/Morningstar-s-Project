package com.groupproject.game.core;

/**
 * 2D Orthographic-like Camera with smooth linear interpolation (Lerp) tracking
 * and world boundary clamping.
 *
 * <p>WHY: A rigid camera that snaps instantly to the player's coordinate creates jarring,
 * nauseating visual stutter whenever the player rapidly taps WASD or switches directions.
 * By implementing a Linear Interpolation (Lerp) factor of 0.1, the camera smoothly glides
 * toward the focal point. Clamping ensures the camera never scrolls into void areas outside
 * the defined game world bounds.
 */
public class Camera {

    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private final int viewportWidth;
    private final int viewportHeight;

    /**
     * Interpolation smoothing factor between current position and target position.
     * Higher values (e.g. 0.5) follow tightly; lower values (e.g. 0.05) feel heavier and cinematic.
     */
    private final double lerpFactor = 0.1;

    private double maxWorldWidth = GameConfig.WORLD_WIDTH;
    private double maxWorldHeight = GameConfig.WORLD_HEIGHT;

    /**
     * Initializes camera dimensions matching the current screen resolution.
     *
     * @param viewportWidth  Horizontal width of the visible window.
     * @param viewportHeight Vertical height of the visible window.
     */
    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = (viewportWidth > 0) ? viewportWidth : GameConfig.SCREEN_WIDTH;
        this.viewportHeight = (viewportHeight > 0) ? viewportHeight : GameConfig.SCREEN_HEIGHT;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Thiết lập kích thước thế giới tùy biến cho các phân cảnh đặc thù (như The Range).
     */
    public void setCustomWorldBounds(double worldWidth, double worldHeight) {
        this.maxWorldWidth = Math.max(viewportWidth, worldWidth);
        this.maxWorldHeight = Math.max(viewportHeight, worldHeight);
        clamp();
    }

    /**
     * Snap camera lập tức vào tâm mục tiêu mà không chờ hiệu ứng Lerp.
     */
    public void snapTo(double focusX, double focusY) {
        targetX = focusX - (viewportWidth / 2.0);
        targetY = focusY - (viewportHeight / 2.0);
        x = targetX;
        y = targetY;
        clamp();
    }

    /**
     * Updates camera position smoothly following a focal target (typically the player).
     *
     * @param focusX Target entity center X in world coordinates.
     * @param focusY Target entity center Y in world coordinates.
     */
    public void update(double focusX, double focusY) {
        // Center the camera viewport around the target coordinates
        targetX = focusX - (viewportWidth / 2.0);
        targetY = focusY - (viewportHeight / 2.0);

        // Smooth Lerp tracking formula: pos = pos + (target - pos) * alpha
        x += (targetX - x) * lerpFactor;
        y += (targetY - y) * lerpFactor;

        // Clamp within world boundaries so the camera doesn't reveal out-of-bounds space
        clamp();
    }

    /**
     * Restricts camera coordinates within the world bounds [0, WORLD_DIMENSION - VIEWPORT].
     */
    private void clamp() {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > maxWorldWidth - viewportWidth) {
            x = Math.max(0, maxWorldWidth - viewportWidth);
        }
        if (y > maxWorldHeight - viewportHeight) {
            y = Math.max(0, maxWorldHeight - viewportHeight);
        }
    }

    /**
     * Converts a screen-space pixel X coordinate (e.g. mouse cursor) to world-space X.
     */
    public double screenToWorldX(int screenX) {
        return screenX + x;
    }

    /**
     * Converts a screen-space pixel Y coordinate (e.g. mouse cursor) to world-space Y.
     */
    public double screenToWorldY(int screenY) {
        return screenY + y;
    }

    /**
     * Converts a world-space coordinate X to screen-space pixel X for rendering on canvas.
     */
    public int worldToScreenX(double worldX) {
        return (int) Math.round(worldX - x);
    }

    /**
     * Converts a world-space coordinate Y to screen-space pixel Y for rendering on canvas.
     */
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
