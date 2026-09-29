package com.groupproject.game.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Main application window (JFrame container).
 */
public class GameWindow extends JFrame {

    private final GamePanel gamePanel;
    private final GameLoop gameLoop;

    public GameWindow() {
        setTitle(GameConfig.GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        this.gamePanel = new GamePanel();
        this.gameLoop = new GameLoop(gamePanel);

        add(gamePanel);
        pack();
        setLocationRelativeTo(null); // Center window on screen

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameLoop.stop();
            }
        });
    }

    public void launch() {
        setVisible(true);
        gamePanel.requestFocusInWindow();
        gameLoop.start();
    }
}
