package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Lớp cơ sở cho các trạm tương tác (Interactive Stations) trong The Range.
 *
 * <p>Mỗi trạm có vị trí thế giới, kích thước, bán kính tương tác và nhãn thông tin.
 * Khi người chơi di chuyển vào bán kính tương tác, trạm sẽ hiển thị huy hiệu hướng dẫn
 * phím tương tác "[E] ..." và phản hồi khi người chơi nhấn phím.
 */
public abstract class HubStation {

    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected double interactionRadius = 55.0;
    protected String title;
    protected Color themeColor;

    public HubStation(double x, double y, double width, double height, String title, Color themeColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.themeColor = (themeColor != null) ? themeColor : new Color(60, 200, 255);
    }

    public boolean isPlayerInRange(double playerX, double playerY) {
        return Math.hypot(playerX - x, playerY - y) <= interactionRadius;
    }

    public abstract String getActionPrompt();

    public abstract void onInteract(HubRangeState hub);

    public void update(double deltaTime) {
        // Mặc định không có logic chạy theo thời gian
    }

    public void render(Graphics2D g, Camera camera, boolean playerNear) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);
        int halfW = (int) (width / 2.0);
        int halfH = (int) (height / 2.0);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ bệ đỡ trạm
        g2.setColor(new Color(25, 30, 38));
        g2.fillRoundRect(screenX - halfW, screenY - halfH, (int) width, (int) height, 8, 8);

        // Viền trạm sáng hơn khi người chơi đứng gần
        if (playerNear) {
            g2.setColor(themeColor);
            g2.setStroke(new java.awt.BasicStroke(2.0f));
        } else {
            g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 120));
            g2.setStroke(new java.awt.BasicStroke(1.2f));
        }
        g2.drawRoundRect(screenX - halfW, screenY - halfH, (int) width, (int) height, 8, 8);

        // 2. Nội dung riêng của từng trạm (vẽ súng, icon nâng cấp, v.v.)
        renderStationContent(g2, screenX, screenY, halfW, halfH, playerNear);

        // 3. Tiêu đề trạm bên dưới
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int titleW = fm.stringWidth(title);
        g2.setColor(new Color(210, 220, 230));
        g2.drawString(title, screenX - titleW / 2, screenY + halfH + 14);

        // 4. Nếu người chơi đang đứng gần: Vẽ Tooltip hướng dẫn phím bấm tương tác
        if (playerNear) {
            String interactKey = com.groupproject.game.input.KeyBindings.getKeyName(
                    com.groupproject.game.input.KeyBindings.getKeyCode(com.groupproject.game.input.KeyBindings.GameAction.INTERACT));
            String prompt = "[" + interactKey + "] " + getActionPrompt();
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics pFm = g2.getFontMetrics();
            int promptW = pFm.stringWidth(prompt);
            int promptH = pFm.getHeight();

            int tipX = screenX - promptW / 2 - 8;
            int tipY = screenY - halfH - 24;

            // Nền tooltip
            g2.setColor(new Color(15, 20, 26, 230));
            g2.fillRoundRect(tipX, tipY, promptW + 16, promptH + 4, 6, 6);
            g2.setColor(themeColor);
            g2.drawRoundRect(tipX, tipY, promptW + 16, promptH + 4, 6, 6);

            // Chữ hướng dẫn
            g2.setColor(Color.WHITE);
            g2.drawString(prompt, tipX + 8, tipY + promptH - 2);
        }

        g2.dispose();
    }

    protected abstract void renderStationContent(Graphics2D g, int sx, int sy, int halfW, int halfH, boolean playerNear);

    public Rectangle getBounds() {
        return new Rectangle((int) (x - width / 2.0), (int) (y - height / 2.0), (int) width, (int) height);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
