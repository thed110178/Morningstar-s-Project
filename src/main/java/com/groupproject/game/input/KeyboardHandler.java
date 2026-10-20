package com.groupproject.game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles all keyboard inputs smoothly by recording key state changes.
 *
 * <p>WHY: Default OS KeyListeners trigger repetitive keyTyped events with an annoying initial
 * delay (typematic delay) before repeating. By tracking raw {@code keyPressed} and {@code keyReleased}
 * states in boolean arrays, continuous locomotion (holding W/A/S/D) remains completely fluid without
 * hitching. In addition, the triple-array mechanism ({@code keys}, {@code keysJustPressed},
 * {@code keysCantPress}) reliably differentiates between holding a key down and pressing it
 * once (e.g. Pause Menu toggle, Dash, Weapon Reload).
 */
public class KeyboardHandler implements KeyListener {

    /**
     * Array tracking raw held-down state of keys up to keyCode 511.
     */
    private final boolean[] keys = new boolean[512];

    /**
     * Array indicating keys that transitioned to pressed state during the current frame.
     */
    private final boolean[] keysJustPressed = new boolean[512];

    /**
     * Lockout array preventing {@code keysJustPressed} from remaining true across multiple ticks
     * while the user continues to hold down the physical key.
     */
    private final boolean[] keysCantPress = new boolean[512];

    /**
     * Synchronizes key state transitions at the start of each game tick.
     *
     * <p>WHY: Ensures {@link #isKeyPressedOnce(int)} returns true for exactly ONE logic frame
     * per physical key strike.
     */
    public void update() {
        for (int i = 0; i < keys.length; i++) {
            if (keysCantPress[i]) {
                if (!keys[i]) {
                    // Physical key has been released, unlock for next press
                    keysCantPress[i] = false;
                }
            } else if (keysJustPressed[i]) {
                // Key was just consumed in previous frame, activate lock
                keysCantPress[i] = true;
                keysJustPressed[i] = false;
            }
            if (!keysCantPress[i] && keys[i]) {
                // Key is held down and not locked, register single-press event
                keysJustPressed[i] = true;
            }
        }
    }

    /**
     * Checks if a key is currently held down.
     * Ideal for continuous actions like WASD player movement.
     *
     * @param keyCode The virtual key code from {@link KeyEvent}.
     * @return {@code true} if currently pressed, {@code false} otherwise.
     */
    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    /**
     * Checks if a key was pressed down in this exact frame (fires only once per physical press).
     * Ideal for discrete actions like Menu Selection, ESC pause, or Weapon Swap.
     *
     * @param keyCode The virtual key code from {@link KeyEvent}.
     * @return {@code true} if pressed once during this frame tick.
     */
    public boolean isKeyPressedOnce(int keyCode) {
        if (keyCode >= 0 && keyCode < keysJustPressed.length) {
            return keysJustPressed[keyCode];
        }
        return false;
    }

    /**
     * Trả về mã keyCode của phím đầu tiên được nhấn xuống trong khung hình này (nếu có).
     * Phục vụ tính năng bắt phím để cài đặt phím tùy chỉnh (Keybind Rebinding).
     *
     * @return Mã keyCode nếu có phím được nhấn một lần, hoặc -1 nếu không có phím nào.
     */
    public int getAnyKeyPressedOnce() {
        for (int i = 0; i < keysJustPressed.length; i++) {
            if (keysJustPressed[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Unused - we rely on raw key codes rather than typed characters
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = false;
        }
    }
}
