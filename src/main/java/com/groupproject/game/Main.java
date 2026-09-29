package com.groupproject.game;

import com.groupproject.game.core.GameWindow;
import javax.swing.SwingUtilities;

/**
 * Application Entry Point.
 */
public class Main {

    public static void main(String[] args) {
        // Ensure GUI initialization runs safely on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.launch();
        });
    }
}
