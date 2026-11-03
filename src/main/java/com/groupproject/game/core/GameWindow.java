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
@SuppressWarnings("serial")
public class GameWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final GamePanel gamePanel;
    private final GameLoop gameLoop;

    private static String displaySettingsFilePath = "save/display_settings.properties";
    private boolean fullScreen = true;
    private java.awt.Rectangle windowedBounds = null;

    public static void setDisplaySettingsFilePath(String path) {
        displaySettingsFilePath = (path != null && !path.trim().isEmpty()) ? path : "save/display_settings.properties";
    }

    public static String getDisplaySettingsFilePath() {
        return displaySettingsFilePath;
    }

    private void loadDisplaySettings() {
        java.io.File file = new java.io.File(displaySettingsFilePath);
        if (file.exists() && file.isFile()) {
            java.util.Properties props = new java.util.Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                props.load(fis);
                fullScreen = Boolean.parseBoolean(props.getProperty("fullscreen", "true"));
            } catch (Exception e) {
                fullScreen = true;
            }
        } else {
            fullScreen = true;
            saveDisplaySettings();
        }
        GameConfig.fullscreen = fullScreen;
    }

    public void saveDisplaySettings() {
        try {
            java.io.File file = new java.io.File(displaySettingsFilePath);
            java.io.File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            java.util.Properties props = new java.util.Properties();
            props.setProperty("fullscreen", Boolean.toString(fullScreen));
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                props.store(fos, "Alone in the zone Display Settings");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Constructs and arranges the window container.
     */
    @SuppressWarnings("this-escape")
    public GameWindow() {
        setTitle(GameConfig.GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Nạp cấu hình chế độ hiển thị (mặc định Toàn màn hình theo nhu cầu trải nghiệm)
        loadDisplaySettings();

        // Thiết lập biểu tượng cho cửa sổ ứng dụng và thanh tác vụ Taskbar
        setupWindowIcon();

        // WHY: Fixed resolution simplifies coordinate calculations for HUD and aiming mechanics
        setResizable(false);
        setFocusTraversalKeysEnabled(false);

        this.gamePanel = new GamePanel();
        this.gameLoop = new GameLoop(gamePanel);

        // Đăng ký bộ chuyển đổi toàn màn hình F11 và hook kiểm tra trạng thái
        this.gamePanel.setFullScreenToggler(this::toggleFullScreen);
        GameConfig.fullScreenToggler = this::toggleFullScreen;
        GameConfig.fullScreenChecker = this::isFullScreen;

        if (fullScreen) {
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            add(gamePanel);
        } else {
            setUndecorated(false);
            add(gamePanel);
            pack();
            setLocationRelativeTo(null);
        }

        // Gracefully terminate the worker thread upon window close request
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    com.groupproject.game.core.GameSoundManager.saveSettings();
                } catch (Exception ignored) {
                }
                try {
                    saveDisplaySettings();
                } catch (Exception ignored) {
                }
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

    /**
     * Chuyển đổi mượt mà giữa chế độ Cửa sổ (Windowed 1280x720) và Toàn màn hình (Borderless Fullscreen).
     */
    public void toggleFullScreen() {
        fullScreen = !fullScreen;
        GameConfig.fullscreen = fullScreen;
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
        saveDisplaySettings();
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
