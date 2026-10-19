package com.groupproject.game.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Top-level application container (JFrame window).
 *
 * <p>WHY: Manages OS-level window parameters (title, decorations, centering, non-resizable borders)
 * and guarantees clean lifecycle management. Specifically, it hooks into {@link WindowAdapter}
 * to stop the independent {@link GameLoop} thread when the user clicks the close ('X') button,
 * preventing zombie threads from lingering in the background.
 */
public class GameWindow extends JFrame {

    private final GamePanel gamePanel;
    private final GameLoop gameLoop;

    /**
     * Constructs and arranges the window container.
     */
    public GameWindow() {
        setTitle(GameConfig.GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // WHY: Fixed resolution simplifies coordinate calculations for HUD and aiming mechanics
        setResizable(false);
        setFocusTraversalKeysEnabled(false);

        this.gamePanel = new GamePanel();
        this.gameLoop = new GameLoop(gamePanel);

        // Đăng ký bộ chuyển đổi toàn màn hình F11
        this.gamePanel.setFullScreenToggler(this::toggleFullScreen);

        add(gamePanel);

        // WHY: pack() sizes the frame so that the content pane (GamePanel) matches its preferredSize exactly,
        // avoiding window border insets eating into the playable 1280x720 area.
        pack();

        // WHY: Passing null centers the window on the primary monitor
        setLocationRelativeTo(null);

        // Gracefully terminate the worker thread upon window close request
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameLoop.stop();
            }
        });
    }

    private boolean fullScreen = false;
    private java.awt.Rectangle windowedBounds = null;

    /**
     * Chuyển đổi mượt mà giữa chế độ Cửa sổ (Windowed 1280x720) và Toàn màn hình (Borderless Fullscreen).
     */
    public void toggleFullScreen() {
        fullScreen = !fullScreen;
        dispose();
        if (fullScreen) {
            windowedBounds = getBounds();
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            if (windowedBounds != null) {
                setBounds(windowedBounds);
            }
            pack();
            setLocationRelativeTo(null);
        }
        setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    /**
     * Makes the window visible, grabs input focus, and fires up the game loop.
     */
    public void launch() {
        setVisible(true);

        // WHY: Without explicit focus request, key events might be ignored until the user clicks the canvas
        gamePanel.requestFocusInWindow();

        gameLoop.start();
    }
}
