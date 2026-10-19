package com.groupproject.game.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Handles mouse movement, clicking, and dragging for 360-degree aiming.
 *
 * <p>WHY: Top-down roguelite shooters (like Soul Knight and POBG) require omnidirectional
 * aiming where the firearm points directly at the cursor, and shooting can occur either continuously
 * (automatic rifles) or on discrete clicks (pistols, shotguns). MouseHandler captures real-time
 * cursor screen coordinates and tracks button states so that game states can calculate aim vectors
 * without querying the OS mouse pointer synchronously inside the render loop.
 */
public class MouseHandler extends MouseAdapter {

    private int screenX;
    private int screenY;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean leftClickedOnce;
    private boolean rightClickedOnce;

    // WHY: Thông số viewport hỗ trợ chuyển đổi tọa độ chuột chính xác khi phóng to / toàn màn hình
    private double viewportOffsetX = 0.0;
    private double viewportOffsetY = 0.0;
    private double viewportScale = 1.0;

    /**
     * Cập nhật thông số biến đổi tọa độ viewport khi phóng to / toàn màn hình.
     */
    public void setViewportTransform(double offsetX, double offsetY, double scale) {
        this.viewportOffsetX = offsetX;
        this.viewportOffsetY = offsetY;
        this.viewportScale = scale > 0 ? scale : 1.0;
    }

    public int transformX(int rawX) {
        return (int) Math.round((rawX - viewportOffsetX) / viewportScale);
    }

    public int transformY(int rawY) {
        return (int) Math.round((rawY - viewportOffsetY) / viewportScale);
    }

    /**
     * Resets single-click pulse flags at the end of each frame.
     *
     * <p>WHY: Prevents single-click actions (such as clicking a menu button or firing a semi-automatic weapon)
     * from triggering repeatedly across subsequent frames.
     */
    public void update() {
        leftClickedOnce = false;
        rightClickedOnce = false;
    }

    /**
     * Resets all mouse input states including held buttons and click pulses.
     *
     * <p>WHY: Called on state transitions (e.g. entering PlayingState from MenuState)
     * to prevent click leakage causing the operative to unintentionally fire a weapon upon spawn.
     */
    public void reset() {
        leftPressed = false;
        rightPressed = false;
        leftClickedOnce = false;
        rightClickedOnce = false;
    }

    /**
     * Returns the horizontal pixel coordinate of the cursor relative to the canvas.
     */
    public int getScreenX() {
        return screenX;
    }

    /**
     * Returns the vertical pixel coordinate of the cursor relative to the canvas.
     */
    public int getScreenY() {
        return screenY;
    }

    /**
     * Checks if the primary (left) mouse button is held down.
     * Useful for automatic sustained gunfire.
     */
    public boolean isLeftPressed() {
        return leftPressed;
    }

    /**
     * Checks if the secondary (right) mouse button is held down.
     */
    public boolean isRightPressed() {
        return rightPressed;
    }

    /**
     * Checks if the primary (left) mouse button was clicked during this frame.
     * Useful for UI button clicks and semi-automatic weapon firing.
     */
    public boolean isLeftClickedOnce() {
        return leftClickedOnce;
    }

    /**
     * Checks if the secondary (right) mouse button was clicked during this frame.
     */
    public boolean isRightClickedOnce() {
        return rightClickedOnce;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        screenX = transformX(e.getX());
        screenY = transformY(e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftPressed = true;
            leftClickedOnce = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightPressed = true;
            rightClickedOnce = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        screenX = transformX(e.getX());
        screenY = transformY(e.getY());
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftPressed = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightPressed = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        screenX = transformX(e.getX());
        screenY = transformY(e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        screenX = transformX(e.getX());
        screenY = transformY(e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Can be hooked into weapon cycling or zoom levels in future extensions
    }
}
