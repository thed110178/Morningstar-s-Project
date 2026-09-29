package com.groupproject.game.core;

/**
 * Dedicated Multithreaded Game Loop with Fixed Timestep and Frame Pacing.
 * Demonstrates Concurrency principles required by the project specifications.
 */
public class GameLoop implements Runnable {

    private final GamePanel gamePanel;
    private Thread gameThread;
    private volatile boolean running = false;

    private int currentFPS = 0;
    private int currentUPS = 0;

    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "Game-Loop-Worker");
        gameThread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long initialTime = System.nanoTime();
        final double timePerUpdate = 1_000_000_000.0 / GameConfig.TARGET_UPS;
        double delta = 0;

        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            long currentTime = System.nanoTime();
            delta += (currentTime - initialTime) / timePerUpdate;
            initialTime = currentTime;

            // Update game physics and logic at fixed timestep
            while (delta >= 1) {
                double dt = 1.0 / GameConfig.TARGET_UPS;
                gamePanel.update(dt);
                updates++;
                delta--;
            }

            // Render current frame
            gamePanel.repaint();
            frames++;

            // Measure FPS & UPS every second
            if (System.currentTimeMillis() - timer >= 1000) {
                currentFPS = frames;
                currentUPS = updates;
                frames = 0;
                updates = 0;
                timer += 1000;
            }

            // Yield / small sleep to prevent 100% CPU thread starvation
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public int getCurrentFPS() {
        return currentFPS;
    }

    public int getCurrentUPS() {
        return currentUPS;
    }
}
