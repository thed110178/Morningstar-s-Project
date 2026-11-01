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

        // Thiết lập biểu tượng cho cửa sổ ứng dụng và thanh tác vụ Taskbar
        setupWindowIcon();

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
                // Tự động lưu tiến trình ván đấu checkpoint nếu người chơi đang trong Chiến dịch Campaign
                try {
                    com.groupproject.game.state.GameStateManager gsm = gamePanel.getStateManager();
                    if (gsm != null && gsm.getCurrentStateType() == com.groupproject.game.state.GameStateManager.StateType.PLAYING) {
                        com.groupproject.game.state.GameState current = gsm.getState(com.groupproject.game.state.GameStateManager.StateType.PLAYING);
                        if (current instanceof com.groupproject.game.state.PlayingState ps) {
                            com.groupproject.game.storage.RunSaveManager.saveRun(ps);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[GameWindow] Không thể lưu checkpoint khi đóng cửa sổ: " + ex.getMessage());
                }
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

    /**
     * Nạp và áp dụng biểu tượng game (Player) cho cửa sổ và thanh tác vụ Taskbar.
     *
     * <p>WHY: Giúp trò chơi hiển thị biểu tượng đặc trưng của Chiến binh Operative thay vì
     * biểu tượng tách cà phê Java mặc định trên Windows Taskbar và thanh tiêu đề cửa sổ.
     */
    private void setupWindowIcon() {
        try {
            java.net.URL iconUrl = getClass().getResource("/player/PlayerGun.png");
            if (iconUrl != null) {
                java.awt.image.BufferedImage baseIcon = javax.imageio.ImageIO.read(iconUrl);
                if (baseIcon != null) {
                    // Tạo danh sách đa kích thước (16, 32, 48, 64) để Windows tự chọn độ phân giải tối ưu
                    java.util.List<java.awt.Image> icons = new java.util.ArrayList<>();
                    int[] sizes = {16, 32, 48, 64};
                    for (int s : sizes) {
                        if (s == baseIcon.getWidth() && s == baseIcon.getHeight()) {
                            icons.add(baseIcon);
                        } else {
                            java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(s, s, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                            java.awt.Graphics2D g2d = scaled.createGraphics();
                            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                            g2d.drawImage(baseIcon, 0, 0, s, s, null);
                            g2d.dispose();
                            icons.add(scaled);
                        }
                    }
                    setIconImages(icons);

                    // Thiết lập biểu tượng cho thanh tác vụ Taskbar trên Windows (Java 9+)
                    if (java.awt.Taskbar.isTaskbarSupported()) {
                        java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                        if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                            taskbar.setIconImage(baseIcon);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            // Im lặng an toàn nếu môi trường không hỗ trợ Taskbar
        }
    }
}
