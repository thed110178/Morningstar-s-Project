package com.groupproject.game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles all keyboard inputs smoothly by recording key state changes.
 */
public class KeyboardHandler implements KeyListener {

    private final boolean[] keys = new boolean[512];
    private final boolean[] keysJustPressed = new boolean[512];
    private final boolean[] keysCantPress = new boolean[512];

    public void update() {
        for (int i = 0; i < keys.length; i++) {
            if (keysCantPress[i]) {
                if (!keys[i]) {
                    keysCantPress[i] = false;
                }
            } else if (keysJustPressed[i]) {
                keysCantPress[i] = true;
                keysJustPressed[i] = false;
            }
            if (!keysCantPress[i] && keys[i]) {
                keysJustPressed[i] = true;
            }
        }
    }

    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    public boolean isKeyPressedOnce(int keyCode) {
        if (keyCode >= 0 && keyCode < keysJustPressed.length) {
            return keysJustPressed[keyCode];
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
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
