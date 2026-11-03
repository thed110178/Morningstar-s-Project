package com.groupproject.game.core;

import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.MenuState;
import com.groupproject.game.state.PlayingState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Primary rendering canvas and input receiver component.
 *
 * <p>WHY: By extending {@link JPanel} and enabling hardware Double Buffering, graphics are rendered
 * into an off-screen buffer before being blitted onto the monitor in a single operation. This
 * completely eliminates tearing and visible blanking artifacts. Furthermore, GamePanel acts as
 * the central coordinator linking OS inputs (keyboard, mouse) with the {@link GameStateManager}.
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final KeyboardHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameStateManager stateManager;

    /**
     * Initializes the panel layout, input listeners, and registers default game states.
     */
    @SuppressWarnings("this-escape")
    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        setBackground(Color.BLACK);

        // WHY: Double buffering prevents screen flicker by drawing to an off-screen image buffer first
        setDoubleBuffered(true);

        // WHY: Component must be focusable to receive keyboard KeyEvents from the operating system
        setFocusable(true);

        // WHY: Java Swing mặc định tiêu thụ (consume) phím TAB để chuyển focus traversal giữa các component.
        // Tắt focus traversal keys để KeyListener nhận được đầy đủ sự kiện KeyEvent.VK_TAB cho tính năng Balo.
        setFocusTraversalKeysEnabled(false);

        // Khởi tạo và nạp cài đặt âm thanh người dùng ngay từ đầu
        com.groupproject.game.core.GameSoundManager.init();

        this.keyHandler = new KeyboardHandler();
        this.mouseHandler = new MouseHandler();
        this.stateManager = new GameStateManager();
        this.stateManager.setMouseHandler(this.mouseHandler);

        // Register all input adapters on this canvas
        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        // Register foundational application states (State Pattern)
        stateManager.registerState(GameStateManager.StateType.MENU, new MenuState(stateManager));
        stateManager.registerState(GameStateManager.StateType.THE_RANGE, new com.groupproject.game.hub.HubRangeState(stateManager));
        stateManager.registerState(GameStateManager.StateType.PLAYING, new PlayingState(stateManager));

        // Mặc định tắt cờ spawn trực tiếp vào phòng Boss (chỉ bật khi người chơi chọn chế độ Boss Fight trên Menu)
        com.groupproject.game.state.PlayingState.setSpawnDirectlyInBossRoom(false);

        // Set default startup state to the Main Menu
        stateManager.setState(GameStateManager.StateType.MENU);
        mouseHandler.reset();
    }

    private Runnable fullScreenToggler;

    /**
     * Đăng ký callback chuyển đổi chế độ toàn màn hình từ GameWindow.
     */
    public void setFullScreenToggler(Runnable toggler) {
        this.fullScreenToggler = toggler;
    }

    /**
     * Bật / tắt chế độ toàn màn hình.
     */
    public void toggleFullScreen() {
        if (fullScreenToggler != null) {
            fullScreenToggler.run();
        }
    }

    /**
     * Propagates logic updates down to input handlers and the active game state.
     *
     * @param deltaTime Elapsed delta time in seconds (typically 1/60s).
     */
    public void update(double deltaTime) {
        keyHandler.update();

        // WHY: Phím F11 hoặc Alt + Enter để chuyển đổi Toàn màn hình (Fullscreen)
        if (keyHandler.isKeyPressedOnce(java.awt.event.KeyEvent.VK_F11) ||
                (keyHandler.isKeyDown(java.awt.event.KeyEvent.VK_ALT) && keyHandler.isKeyPressedOnce(java.awt.event.KeyEvent.VK_ENTER))) {
            toggleFullScreen();
        }

        // Đồng bộ hóa với luồng render Swing EDT để đảm bảo state và tọa độ camera không bị thay đổi giữa chừng lúc render
        synchronized (stateManager) {
            stateManager.update(deltaTime, keyHandler, mouseHandler);
        }
        mouseHandler.update();
    }

    /**
     * Renders game graphics using anti-aliasing and high-quality rendering hints.
     *
     * <p>WHY: Java 2D defaults to low-quality, pixelated font rendering. Injecting
     * {@link RenderingHints} ensures text readability and clean geometric edge smoothing.
     * Tự động scale và tạo dải viền đen (Letterbox/Pillarbox) khi chơi ở chế độ toàn màn hình,
     * bảo toàn tỷ lệ 16:9 và không gian tọa độ 1280x720 của trò chơi.
     *
     * @param g Base Graphics context supplied by Swing repaint manager.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Áp dụng cấu hình tối ưu hiệu năng render: Ưu tiên tốc độ (VALUE_RENDER_SPEED) thay vì thuật toán nội suy nặng
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        int panelW = getWidth();
        int panelH = getHeight();
        double scaleX = (double) panelW / GameConfig.SCREEN_WIDTH;
        double scaleY = (double) panelH / GameConfig.SCREEN_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        if (scale <= 0.0) scale = 1.0;

        int drawW = (int) Math.round(GameConfig.SCREEN_WIDTH * scale);
        int drawH = (int) Math.round(GameConfig.SCREEN_HEIGHT * scale);
        int offsetX = (panelW - drawW) / 2;
        int offsetY = (panelH - drawH) / 2;

        // Cập nhật thông số viewport chuyển đổi tọa độ cho chuột
        mouseHandler.setViewportTransform(offsetX, offsetY, scale);

        // Nền đen cho khoảng letterbox/pillarbox
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, panelW, panelH);

        // Vẽ nội dung canvas game chuẩn 1280x720
        Graphics2D gameG = (Graphics2D) g2d.create();
        gameG.translate(offsetX, offsetY);
        gameG.scale(scale, scale);
        gameG.setClip(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        // Khóa đồng bộ với Game-Loop-Worker để loại bỏ hoàn toàn hiện tượng map bị xé hình (Tearing)
        synchronized (stateManager) {
            stateManager.render(gameG);
        }

        // Hiển thị khung đo lường thời gian thực số khung hình (FPS) và nhịp cập nhật (UPS) khi được bật trong Cài đặt
        renderPerformanceOverlay(gameG);

        // Ghi nhận một khung hình đã thực sự được render ra màn hình hiển thị để tính FPS thực tế
        GameLoop.recordFrameRendered();

        gameG.dispose();

        // WHY: Disposing the Graphics2D instance frees native system graphics context resources immediately
        g2d.dispose();
    }

    /**
     * Hiển thị bảng HUD chẩn đoán hiệu năng FPS & UPS ở góc dưới bên trái màn hình.
     * Tự động thích ứng màu sắc dựa trên mức độ ổn định khung hình (Xanh >=55, Vàng 30-54, Đỏ <30).
     */
    private void renderPerformanceOverlay(Graphics2D g) {
        if (!GameConfig.showFpsUps) return;

        int fps = GameLoop.getFps();
        int ups = GameLoop.getUps();

        int boxW = 148;
        int boxH = 26;
        int boxX = 20;
        int boxY = GameConfig.SCREEN_HEIGHT - boxH - 16;

        // Khung nền xám tối bo góc viền công nghệ cao
        g.setColor(new Color(10, 15, 24, 215));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(45, 75, 110, 190));
        g.setStroke(new java.awt.BasicStroke(1.0f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Biểu tượng xung nhịp radar
        g.setColor(new Color(0, 220, 255));
        g.fillOval(boxX + 9, boxY + 9, 8, 8);

        // Màu sắc thích ứng theo FPS
        Color fpsColor;
        if (fps >= 55) {
            fpsColor = new Color(60, 240, 140); // Xanh ngọc mượt mà
        } else if (fps >= 30) {
            fpsColor = new Color(255, 205, 55);  // Vàng cảnh báo
        } else {
            fpsColor = new Color(255, 75, 75);   // Đỏ báo động
        }

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        g.setColor(fpsColor);
        g.drawString("FPS: " + fps, boxX + 24, boxY + 17);

        g.setColor(new Color(110, 135, 165));
        g.drawString("|", boxX + 78, boxY + 17);

        g.setColor(new Color(185, 220, 255));
        g.drawString("UPS: " + ups, boxX + 88, boxY + 17);
    }

    /**
     * Returns the central state manager instance.
     */
    public GameStateManager getStateManager() {
        return stateManager;
    }
}
