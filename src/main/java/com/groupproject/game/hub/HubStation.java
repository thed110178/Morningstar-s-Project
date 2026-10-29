package com.groupproject.game.hub;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

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

    private static final BasicStroke STROKE_ACTIVE = new BasicStroke(2.0f);
    private static final BasicStroke STROKE_INACTIVE = new BasicStroke(1.2f);
    private static final Font FONT_PROMPT = new Font("SansSerif", Font.BOLD, 12);

    public void render(Graphics2D g, Camera camera, boolean playerNear) {
        int screenX = camera.worldToScreenX(x);
        int screenY = camera.worldToScreenY(y);
        int halfW = (int) (width / 2.0);
        int halfH = (int) (height / 2.0);

        // Viewport Culling: Bỏ qua hoàn toàn nếu trạm nằm ngoài tầm nhìn camera
        if (screenX + halfW < -60 || screenX - halfW > GameConfig.SCREEN_WIDTH + 60 ||
            screenY + halfH < -60 || screenY - halfH > GameConfig.SCREEN_HEIGHT + 60) {
            return;
        }

        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        // 1. Vẽ bệ đỡ trạm
        g.setColor(new Color(25, 30, 38));
        g.fillRoundRect(screenX - halfW, screenY - halfH, (int) width, (int) height, 8, 8);

        // Viền trạm sáng hơn khi người chơi đứng gần
        if (playerNear) {
            g.setColor(themeColor);
            g.setStroke(STROKE_ACTIVE);
        } else {
            g.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 120));
            g.setStroke(STROKE_INACTIVE);
        }
        g.drawRoundRect(screenX - halfW, screenY - halfH, (int) width, (int) height, 8, 8);

        // 2. Nội dung riêng của từng trạm (vẽ súng, icon nâng cấp, v.v.)
        renderStationContent(g, screenX, screenY, halfW, halfH, playerNear);

        // 3. Nếu người chơi đang đứng gần: Vẽ Tooltip hướng dẫn phím bấm tương tác phía trên
        if (playerNear) {
            String interactKey = com.groupproject.game.input.KeyBindings.getKeyName(
                    com.groupproject.game.input.KeyBindings.getKeyCode(com.groupproject.game.input.KeyBindings.GameAction.INTERACT));
            String prompt = "[" + interactKey + "] " + getActionPrompt();
            g.setFont(FONT_PROMPT);
            FontMetrics pFm = g.getFontMetrics();
            int promptW = pFm.stringWidth(prompt);
            int promptH = pFm.getHeight();

            int tipX = screenX - promptW / 2 - 8;
            int tipY = screenY - halfH - 24;

            // Nền tooltip
            g.setColor(new Color(15, 20, 26, 230));
            g.fillRoundRect(tipX, tipY, promptW + 16, promptH + 4, 6, 6);
            g.setColor(themeColor);
            g.drawRoundRect(tipX, tipY, promptW + 16, promptH + 4, 6, 6);

            // Chữ hướng dẫn
            g.setColor(Color.WHITE);
            g.drawString(prompt, tipX + 8, tipY + promptH - 2);
        }

        g.setStroke(oldStroke);
        g.setColor(oldColor);
        g.setFont(oldFont);
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
