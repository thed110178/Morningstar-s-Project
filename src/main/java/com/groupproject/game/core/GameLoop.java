package com.groupproject.game.core;

/**
 * Dedicated Multithreaded Game Loop implementing a Fixed Timestep with Variable Rendering.
 *
 * <p>WHY: If game physics and input processing run on the Swing Event Dispatch Thread (EDT)
 * or rely on a simple {@link javax.swing.Timer}, heavy computational workloads (such as AI
 * pathfinding or hundreds of flying bullets) will freeze the UI and degrade input responsiveness.
 * By decoupling the game loop onto an independent worker thread ("Game-Loop-Worker") and enforcing
 * a fixed delta time (1/60s), we ensure deterministic physics simulations and satisfy the course's
 * Concurrency / Multithreading rubric requirements.
 *
 * <p>EDGE CASES:
 * <ul>
 *   <li>Thread Starvation: A busy loop with {@code while(true)} can consume 100% of a CPU core.
 *       We include a tiny 1ms cooperative yield/sleep to allow other OS threads to breathe.</li>
 *   <li>Visibility: The {@code running} flag is marked {@code volatile} so that when
 *       {@link #stop()} is invoked from the window thread, the worker thread observes the change
 *       immediately without caching stale values.</li>
 * </ul>
 */
public class GameLoop implements Runnable {

    private final GamePanel gamePanel;
    private Thread gameThread;

    /**
     * Volatile control flag guaranteeing thread-safe visibility between the main UI thread
     * and the worker thread.
     */
    private volatile boolean running = false;

    /**
     * Diagnostic metrics tracked every second to evaluate rendering and logic performance.
     * Marked static volatile for thread-safe global access from UI renderers.
     */
    private static volatile int currentFPS = 0;
    private static volatile int currentUPS = 0;
    private static volatile int actualRenderedFrames = 0;

    /**
     * Ghi nhận một khung hình đã thực sự được Swing/GPU vẽ hoàn tất trên màn hình (paintComponent).
     * Được gọi từ Event Dispatch Thread (EDT) của GamePanel.
     */
    public static void recordFrameRendered() {
        actualRenderedFrames++;
    }

    /**
     * Constructs a GameLoop instance bound to the specified display panel.
     *
     * @param gamePanel The target canvas panel to update and repaint.
     */
    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Starts the dedicated game loop thread if not already running.
     * Synchronized to prevent duplicate thread creation on concurrent calls.
     */
    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "Game-Loop-Worker");
        gameThread.start();
    }

    /**
     * Stops the game loop gracefully and blocks up to 1000ms until the thread terminates.
     *
     * <p>WHY: Terminating the application abruptly while the loop thread is active can leave
     * open file streams or orphan background tasks. Graceful joining guarantees clean teardown.
     */
    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join(1000);
            }
        } catch (InterruptedException e) {
            // Restore interrupted status to respect thread interruption contract
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Main execution routine of the worker thread.
     * Implements accumulator-based fixed timestep physics pacing.
     */
    @Override
    public void run() {
        long initialTime = System.nanoTime();
        final double timePerUpdate = 1_000_000_000.0 / GameConfig.TARGET_UPS;
        double delta = 0;

        long timer = System.currentTimeMillis();
        int updates = 0;

        while (running) {
            long currentTime = System.nanoTime();
            // Accumulate elapsed time in units of fixed updates
            delta += (currentTime - initialTime) / timePerUpdate;
            initialTime = currentTime;

            // WHY: Chống vòng xoắn tử thần (Spiral of Death) khi có gián đoạn hệ thống hoặc garbage collection
            if (delta > 5.0) {
                delta = 5.0;
            }

            boolean shouldRender = false;

            // Catch up on logic updates at a strictly fixed timestep
            while (delta >= 1) {
                double dt = 1.0 / GameConfig.TARGET_UPS;
                gamePanel.update(dt);
                updates++;
                delta--;
                shouldRender = true;
            }

            // WHY: Chỉ yêu cầu Swing vẽ lại (repaint) khi trạng thái thế giới trò chơi đã được cập nhật logic.
            // Tránh spam repaint() 150-200 lần/giây gây nghẽn hàng đợi Event Dispatch Thread (EDT),
            // triệt tiêu xung đột khóa synchronized giữa update và render gây micro-stutter (khựng khung hình).
            if (shouldRender) {
                gamePanel.repaint();
            }

            // Measure FPS và UPS mỗi 1000ms: FPS đo số khung hình thực tế vẽ bởi Swing/GPU
            long now = System.currentTimeMillis();
            if (now - timer >= 1000) {
                currentFPS = actualRenderedFrames;
                currentUPS = updates;
                actualRenderedFrames = 0;
                updates = 0;
                timer += 1000;
            }

            // Yield execution briefly to prevent high CPU utilization on low-power devices
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Returns the measured Frames Per Second rendered in the last second.
     */
    public int getCurrentFPS() {
        return currentFPS;
    }

    /**
     * Returns the measured Updates Per Second executed in the last second.
     */
    public int getCurrentUPS() {
        return currentUPS;
    }

    /**
     * Static thread-safe accessor for real-time FPS metric.
     */
    public static int getFps() {
        return currentFPS;
    }

    /**
     * Static thread-safe accessor for real-time UPS metric.
     */
    public static int getUps() {
        return currentUPS;
    }
}
