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
 * Main game canvas panel utilizing Double Buffering and hardware rendering hints.
 */
public class GamePanel extends JPanel {

    private final KeyboardHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final GameStateManager stateManager;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true); // Prevent screen tearing & flickering
        setFocusable(true);

        this.keyHandler = new KeyboardHandler();
        this.mouseHandler = new MouseHandler();
        this.stateManager = new GameStateManager();

        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        // Register default states
        stateManager.registerState(GameStateManager.StateType.MENU, new MenuState(stateManager));
        stateManager.registerState(GameStateManager.StateType.PLAYING, new PlayingState(stateManager));

        // Initial state is MENU
        stateManager.setState(GameStateManager.StateType.MENU);
    }

    public void update(double deltaTime) {
        keyHandler.update();
        stateManager.update(deltaTime, keyHandler, mouseHandler);
        mouseHandler.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable high-quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        stateManager.render(g2d);

        g2d.dispose();
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }
}
