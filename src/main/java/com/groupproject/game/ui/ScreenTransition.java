package com.groupproject.game.ui;

import com.groupproject.game.core.GameConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Quản lý hiệu ứng chuyển màn hình điện ảnh (Screen Transition) mượt mà cho toàn bộ trò chơi.
 *
 * <p>Áp dụng khi:
 * <ul>
 *   <li>Chuyển đổi giữa các GameState (Menu &lt;-&gt; The Range &lt;-&gt; Playing &lt;-&gt; GameOver).</li>
 *   <li>Chuyển tầng trong Dungeon (Stage 1 -&gt; Stage 2 -&gt; Stage 3 -&gt; Grand Boss Arena).</li>
 * </ul>
 *
 * <p>Cung cấp hiệu ứng Fade Black kết hợp Cyberpunk Grid Scanline & Banner tiêu đề tầng hiện đại,
 * chạy non-blocking 60 FPS và thực thi callback an toàn khi màn hình tối hoàn toàn.
 */
public class ScreenTransition {

    public enum State {
        IDLE,
        FADING_OUT, // Màn hình tối dần / đóng lại
        HOLD,       // Màn hình đen hoàn toàn, kích hoạt callback đổi state/stage
        FADING_IN   // Màn hình mở sáng dần ra
    }

    private State state = State.IDLE;
    private double timer = 0.0;
    private double fadeDuration = 0.35;
    private double holdDuration = 0.15;

    private String overlayTitle = null;
    private String overlaySubtitle = null;
    private Color accentColor = new Color(0, 220, 255);

    private Runnable onHoldAction = null;
    private Runnable onCompleteAction = null;

    public ScreenTransition() {
    }

    /**
     * Khởi động quá trình chuyển cảnh cơ bản (mặc định fade 0.35s, hold 0.15s).
     */
    public void start(Runnable onHold) {
        start(0.35, 0.15, onHold, null, null, new Color(0, 220, 255));
    }

    /**
     * Khởi động chuyển cảnh có tiêu đề và thông điệp hiển thị (ví dụ chuyển Stage).
     */
    public void start(double fadeTime, double holdTime, Runnable onHold, String title, String subtitle) {
        start(fadeTime, holdTime, onHold, title, subtitle, new Color(0, 220, 255));
    }

    /**
     * Khởi động chuyển cảnh tùy chỉnh đầy đủ thông số.
     */
    public void start(double fadeTime, double holdTime, Runnable onHold, String title, String subtitle, Color accent) {
        this.fadeDuration = Math.max(0.05, fadeTime);
        this.holdDuration = Math.max(0.01, holdTime);
        this.onHoldAction = onHold;
        this.overlayTitle = title;
        this.overlaySubtitle = subtitle;
        this.accentColor = (accent != null) ? accent : new Color(0, 220, 255);
        this.timer = 0.0;
        this.state = State.FADING_OUT;
    }

    /**
     * Khởi động quá trình fade-in từ màn hình đen sẵn có sang State mới.
     * Bắt đầu trực tiếp từ HOLD (đen hoàn toàn), thực thi callback đổi State ngay lập tức,
     * và sau đó chuyển sang FADING_IN để cảnh mới mở sáng dần ra.
     */
    public void startFadeInFromBlack(double holdTime, double fadeInTime, Runnable onHold, String title, String subtitle, Color accent) {
        this.fadeDuration = Math.max(0.05, fadeInTime);
        this.holdDuration = Math.max(0.01, holdTime);
        this.onHoldAction = onHold;
        this.overlayTitle = title;
        this.overlaySubtitle = subtitle;
        this.accentColor = (accent != null) ? accent : new Color(0, 220, 255);
        this.timer = 0.0;
        this.state = State.HOLD;
        if (onHoldAction != null) {
            try {
                onHoldAction.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnCompleteAction(Runnable onCompleteAction) {
        this.onCompleteAction = onCompleteAction;
    }

    public boolean isActive() {
        return state != State.IDLE;
    }

    /**
     * Trả về true nếu transition đang ở trạng thái cần chặn thao tác chuột/phím của người chơi.
     */
    public boolean isBlockingInput() {
        return state != State.IDLE;
    }

    public State getState() {
        return state;
    }

    /**
     * Cập nhật tiến trình thời gian của transition.
     */
    public void update(double deltaTime) {
        if (state == State.IDLE) {
            return;
        }

        timer += deltaTime;

        switch (state) {
            case FADING_OUT:
                if (timer >= fadeDuration) {
                    timer = 0.0;
                    state = State.HOLD;
                    if (onHoldAction != null) {
                        try {
                            onHoldAction.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case HOLD:
                if (timer >= holdDuration) {
                    timer = 0.0;
                    state = State.FADING_IN;
                }
                break;

            case FADING_IN:
                if (timer >= fadeDuration) {
                    timer = 0.0;
                    state = State.IDLE;
                    overlayTitle = null;
                    overlaySubtitle = null;
                    if (onCompleteAction != null) {
                        try {
                            onCompleteAction.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        onCompleteAction = null;
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * Kết xuất hiệu ứng chuyển cảnh lên màn hình.
     */
    public void render(Graphics2D g) {
        if (state == State.IDLE) {
            return;
        }

        float alpha = 0.0f;
        switch (state) {
            case FADING_OUT:
                alpha = (float) Math.min(1.0, timer / fadeDuration);
                break;
            case HOLD:
                alpha = 1.0f;
                break;
            case FADING_IN:
                alpha = (float) Math.max(0.0, 1.0 - (timer / fadeDuration));
                break;
            default:
                break;
        }

        int screenW = GameConfig.SCREEN_WIDTH;
        int screenH = GameConfig.SCREEN_HEIGHT;

        // 1. Phủ lớp nền đen mờ/đậm theo tiến độ alpha
        int bgAlphaInt = (int) (alpha * 255);
        if (bgAlphaInt > 0) {
            g.setColor(new Color(6, 9, 14, bgAlphaInt));
            g.fillRect(0, 0, screenW, screenH);
        }

        // 2. Nếu có tiêu đề chuyển cảnh và độ mờ đủ lớn (> 0.35), hiển thị banner công nghệ
        if (alpha > 0.35f && overlayTitle != null && !overlayTitle.isEmpty()) {
            float textAlpha = Math.min(1.0f, (alpha - 0.35f) / 0.65f);
            int textAlphaInt = (int) (textAlpha * 255);

            // Vạch kẻ công nghệ ngang phát sáng
            int lineY1 = screenH / 2 - 42;
            int lineY2 = screenH / 2 + 42;
            int lineHalfW = (int) (220 * textAlpha);

            g.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int) (textAlphaInt * 0.45)));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(screenW / 2 - lineHalfW, lineY1, screenW / 2 + lineHalfW, lineY1);
            g.drawLine(screenW / 2 - lineHalfW, lineY2, screenW / 2 + lineHalfW, lineY2);
            g.setStroke(new BasicStroke(1.0f));

            // Tiêu đề chính
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            FontMetrics fmMain = g.getFontMetrics();
            int titleW = fmMain.stringWidth(overlayTitle);
            int titleX = (screenW - titleW) / 2;
            int titleY = screenH / 2 - 6;

            // Đổ bóng chữ
            g.setColor(new Color(0, 0, 0, (int) (textAlphaInt * 0.9)));
            g.drawString(overlayTitle, titleX + 2, titleY + 2);

            // Màu chữ chính
            g.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), textAlphaInt));
            g.drawString(overlayTitle, titleX, titleY);

            // Phụ đề (Subtitle)
            if (overlaySubtitle != null && !overlaySubtitle.isEmpty()) {
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fmSub = g.getFontMetrics();
                int subW = fmSub.stringWidth(overlaySubtitle);
                int subX = (screenW - subW) / 2;
                int subY = screenH / 2 + 24;

                g.setColor(new Color(210, 230, 255, (int) (textAlphaInt * 0.85)));
                g.drawString(overlaySubtitle, subX, subY);
            }
        }
    }
}
