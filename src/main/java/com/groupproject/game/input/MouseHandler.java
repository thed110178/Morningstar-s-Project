package com.groupproject.game.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Handles mouse movement, clicking, and dragging for 360-degree aiming.
 */
public class MouseHandler extends MouseAdapter {

    private int screenX;
    private int screenY;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean leftClickedOnce;
    private boolean rightClickedOnce;

    public void update() {
        leftClickedOnce = false;
        rightClickedOnce = false;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isLeftClickedOnce() {
        return leftClickedOnce;
    }

    public boolean isRightClickedOnce() {
        return rightClickedOnce;
    }

    @Override
    public void mousePressed(MouseEvent e) {
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
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftPressed = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightPressed = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        screenX = e.getX();
        screenY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        screenX = e.getX();
        screenY = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Can be used for switching weapons or zooming
    }
}
