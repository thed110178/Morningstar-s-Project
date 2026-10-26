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
public class GamePanel extends JPanel {

    private final KeyboardHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameStateManager stateManager;

    /**
     * Initializes the panel layout, input listeners, and registers default game states.
     */
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

        // Mặc định kích hoạt cờ spawn trực tiếp vào phòng Boss cho chế độ kiểm thử
        com.groupproject.game.state.PlayingState.setSpawnDirectlyInBossRoom(true);

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

        // Apply rendering quality enhancements
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

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
        gameG.dispose();

        // WHY: Disposing the Graphics2D instance frees native system graphics context resources immediately
        g2d.dispose();
    }

    /**
     * Returns the central state manager instance.
     */
    public GameStateManager getStateManager() {
        return stateManager;
    }
}
