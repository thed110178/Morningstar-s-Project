package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Quản lý các vệt đường đạn (Recoil Tracer Lines) tại trường bắn Recoil Pattern (Trường bắn bên phải).
 *
 * <p>WHY: Giúp người chơi quan sát trực quan đường bay của từng viên đạn, hình dung rõ ràng
 * độ giật (recoil), độ tản đạn (spread bloom) và mẫu giật (spray pattern) của các loại vũ khí.
 * Mỗi vệt đường đạn được giữ sáng nguyên vẹn trong đúng 3.0 giây, sau đó mờ dần (fade out)
 * trong 0.6 giây cuối cùng trước khi biến mất.
 */
public class RecoilTracerManager {

    public static final double SOLID_DURATION = 3.0; // Giữ sáng rõ trong 3.0 giây
    public static final double FADE_DURATION = 0.6;  // Mờ dần trong 0.6 giây
    public static final double TOTAL_LIFETIME = SOLID_DURATION + FADE_DURATION; // 3.6 giây

    /**
     * Biểu diễn một đường vệt đạn đơn lẻ.
     */
    public static class RecoilTracer {
        private final double startX;
        private final double startY;
        private final double endX;
        private final double endY;
        private final Color baseColor;
        private double remainingTime;

        public RecoilTracer(double startX, double startY, double endX, double endY, Color baseColor) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.baseColor = (baseColor != null) ? baseColor : new Color(255, 215, 60);
            this.remainingTime = TOTAL_LIFETIME;
        }

        public void update(double deltaTime) {
            remainingTime -= deltaTime;
        }

        public boolean isExpired() {
            return remainingTime <= 0.0;
        }

        public double getStartX() {
            return startX;
        }

        public double getStartY() {
            return startY;
        }

        public double getEndX() {
            return endX;
        }

        public double getEndY() {
            return endY;
        }

        public double getRemainingTime() {
            return remainingTime;
        }

        private static final BasicStroke STROKE_GLOW = new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        private static final BasicStroke STROKE_CORE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        public void render(Graphics2D g, Camera camera) {
            if (isExpired()) return;

            int sx = camera.worldToScreenX(startX);
            int sy = camera.worldToScreenY(startY);
            int ex = camera.worldToScreenX(endX);
            int ey = camera.worldToScreenY(endY);

            // Tính toán độ trong suốt alpha: 3 giây đầu giữ 100%, 0.6s cuối giảm dần về 0
            float alphaFactor = 1.0f;
            if (remainingTime < FADE_DURATION) {
                alphaFactor = (float) Math.max(0.0, remainingTime / FADE_DURATION);
            }

            int alphaCore = (int) Math.clamp(230 * alphaFactor, 0, 255);
            int alphaGlow = (int) Math.clamp(90 * alphaFactor, 0, 255);

            java.awt.Stroke oldStroke = g.getStroke();
            Color oldColor = g.getColor();

            // 1. Lớp hào quang dạ quang phát sáng (Glow halo)
            g.setStroke(STROKE_GLOW);
            g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alphaGlow));
            g.drawLine(sx, sy, ex, ey);

            // 2. Lõi đường đạn laser sắc nét (Laser core)
            g.setStroke(STROKE_CORE);
            g.setColor(new Color(255, 255, 255, alphaCore));
            g.drawLine(sx, sy, ex, ey);

            // 3. Đánh dấu điểm va chạm / đích đạn (Impact point marker)
            int pointRadius = 3;
            g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alphaCore));
            g.fillOval(ex - pointRadius, ey - pointRadius, pointRadius * 2, pointRadius * 2);

            g.setStroke(oldStroke);
            g.setColor(oldColor);
        }
    }

    private final List<RecoilTracer> activeTracers = new CopyOnWriteArrayList<>();

    /**
     * Ghi nhận một vệt đạn mới từ họng súng đến vị trí đích.
     */
    public void addTracer(double startX, double startY, double endX, double endY, Color color) {
        activeTracers.add(new RecoilTracer(startX, startY, endX, endY, color));
    }

    /**
     * Cập nhật thời gian tồn tại và loại bỏ các vệt đạn đã mờ hết.
     */
    public void update(double deltaTime) {
        for (RecoilTracer tracer : activeTracers) {
            tracer.update(deltaTime);
        }
        activeTracers.removeIf(RecoilTracer::isExpired);
    }

    /**
     * Vẽ tất cả các vệt đường đạn đang hoạt động.
     */
    public void render(Graphics2D g, Camera camera) {
        for (RecoilTracer tracer : activeTracers) {
            tracer.render(g, camera);
        }
    }

    /**
     * Xóa toàn bộ vệt đạn (phục vụ nút Reset trường bắn).
     */
    public void clear() {
        activeTracers.clear();
    }

    public int getActiveCount() {
        return activeTracers.size();
    }

    public List<RecoilTracer> getActiveTracers() {
        return new ArrayList<>(activeTracers);
    }
}
