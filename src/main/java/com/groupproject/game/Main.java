package com.groupproject.game;

import com.groupproject.game.core.GameWindow;
import javax.swing.SwingUtilities;

/**
 * Entry point of the entire application.
 *
 * <p>WHY: Java Swing components are inherently NOT thread-safe. Creating or modifying
 * UI containers outside of the Swing Event Dispatch Thread (EDT) can lead to subtle race
 * conditions, visual glitches, or unexpected deadlocks on different operating systems.
 * Wrapping the startup routine in {@link SwingUtilities#invokeLater(Runnable)} guarantees
 * that window initialization and initial canvas layout calculation execute strictly on the EDT.
 */
public class Main {

    /**
     * Bootstraps the application on the Swing Event Dispatch Thread.
     *
     * @param args Command-line arguments passed to the application (unused).
     */
    public static void main(String[] args) {
        // Enqueue the window creation task to the EDT to ensure thread safety during GUI initialization
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.launch();
        });
    }
}
