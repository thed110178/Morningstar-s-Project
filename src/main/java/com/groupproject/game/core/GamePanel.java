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

        this.keyHandler = new KeyboardHandler();
        this.mouseHandler = new MouseHandler();
        this.stateManager = new GameStateManager();

        // Register all input adapters on this canvas
        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        // Register foundational application states (State Pattern)
        stateManager.registerState(GameStateManager.StateType.MENU, new MenuState(stateManager));
        stateManager.registerState(GameStateManager.StateType.PLAYING, new PlayingState(stateManager));

        // Set default startup state to the Main Menu
        stateManager.setState(GameStateManager.StateType.MENU);
    }

    /**
     * Propagates logic updates down to input handlers and the active game state.
     *
     * @param deltaTime Elapsed delta time in seconds (typically 1/60s).
     */
    public void update(double deltaTime) {
        keyHandler.update();
        stateManager.update(deltaTime, keyHandler, mouseHandler);
        mouseHandler.update();
    }

    /**
     * Renders game graphics using anti-aliasing and high-quality rendering hints.
     *
     * <p>WHY: Java 2D defaults to low-quality, pixelated font rendering. Injecting
     * {@link RenderingHints} ensures text readability and clean geometric edge smoothing.
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

        // Delegate rendering to the currently active GameState
        stateManager.render(g2d);

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
