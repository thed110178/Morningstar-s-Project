package com.groupproject.game.ui;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.player.Player;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;

/**
 * Hệ thống Bản Đồ Chiến Thuật Thu Nhỏ (Minimap / Tactical Radar System).
 *
 * <p>WHY: Hiển thị radar thu nhỏ thời gian thực ở góc trên bên phải màn hình,
 * áp dụng cơ chế khám phá sương mù chiến thuật (Progressive Room Discovery / Fog of War):
 * <ul>
 *   <li>Chỉ hiển thị các căn phòng mà người chơi đã từng bước chân vào (Discovered Room).</li>
 *   <li>Khi người chơi bước vào một căn phòng mới, phòng đó cùng toàn bộ các hành lang thông tới nó
 *       lập tức được mở rộng và hiển thị đầy đủ trên Minimap.</li>
 *   <li>Các căn phòng kề cận thông qua hành lang nhưng người chơi chưa bước vào sẽ hiện khung mờ
 *       kèm ký hiệu bí ẩn "?" để định hướng thám hiểm.</li>
 *   <li>Các căn phòng ở xa chưa có hành lang nối tới sẽ ẩn hoàn toàn trong bóng tối chiến trường.</li>
 *   <li>Định vị thời gian thực chấm tín hiệu Operative nhấp nháy phát quang (Player Blip & Pulse Ring).</li>
 * </ul>
 */
public class MinimapHUD {

    public static final int HUD_WIDTH = 176;
    public static final int HUD_HEIGHT = 122;
    public static final int INNER_WIDTH = 160;  // 80 tile cols * 2.0 px/tile
    public static final int INNER_HEIGHT = 90;  // 45 tile rows * 2.0 px/tile
    public static final double TILE_SCALE = 2.0;

    private double pulseTimer = 0.0;
    private double sweepX = 0.0;

    /**
     * Cập nhật hoạt ảnh nhấp nháy phát quang và vệt quét radar theo thời gian.
     */
    public void update(double deltaTime) {
        pulseTimer += deltaTime;
        sweepX += deltaTime * 50.0;
        if (sweepX > INNER_WIDTH + 30.0) {
            sweepX = -30.0;
        }
    }

    /**
     * Kết xuất toàn bộ giao diện Minimap Radar lên màn hình.
     *
     * @param g            Đối tượng đồ họa Graphics2D.
     * @param dungeonMap   Bản đồ hầm ngục hiện tại chứa danh sách phòng và hành lang.
     * @param player       Chiến binh Operative cần định vị.
     * @param currentStage Chỉ số stage tác chiến hiện tại.
     */
    public void render(Graphics2D g, DungeonMap dungeonMap, Player player, int currentStage) {
        if (dungeonMap == null) return;

        // Bố trí góc trên bên phải màn hình, ngay dưới khung Credits HUD (y=20, h=36 -> y=64)
        int boxX = GameConfig.SCREEN_WIDTH - HUD_WIDTH - 20;
        int boxY = 64;

        int innerX = boxX + 8;
        int innerY = boxY + 24;

        // 1. Khung container chính phong cách Sci-Fi Military Radar
        g.setColor(new Color(10, 14, 22, 235));
        g.fillRoundRect(boxX, boxY, HUD_WIDTH, HUD_HEIGHT, 8, 8);
        g.setColor(new Color(45, 95, 145, 200));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY, HUD_WIDTH, HUD_HEIGHT, 8, 8);
        g.setStroke(new BasicStroke(1.0f));

        // 2. Thanh tiêu đề (Header): Đèn tín hiệu radar ping & Tiêu đề
        int pingAlpha = (int) (160 + 90 * Math.sin(pulseTimer * 5.0));
        pingAlpha = Math.max(70, Math.min(255, pingAlpha));
        g.setColor(new Color(40, 230, 160, pingAlpha));
        g.fillOval(boxX + 10, boxY + 8, 8, 8);
        g.setColor(new Color(20, 120, 80));
        g.drawOval(boxX + 10, boxY + 8, 8, 8);

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(190, 225, 250));
        g.drawString("TACTICAL RADAR", boxX + 24, boxY + 16);

        // Góc phải tiêu đề: nhãn Stage tinh gọn
        String stageTag = String.format("STG 1|%d", currentStage);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fmTag = g.getFontMetrics();
        int tagW = fmTag.stringWidth(stageTag);
        g.setColor(new Color(255, 210, 65, 220));
        g.drawString(stageTag, boxX + HUD_WIDTH - tagW - 10, boxY + 16);

        // 3. Vùng bản đồ nội bộ (Inner Viewport)
        Shape originalClip = g.getClip();
        g.clipRect(innerX, innerY, INNER_WIDTH, INNER_HEIGHT);

        // Nền đen sâu radar
        g.setColor(new Color(6, 10, 16, 245));
        g.fillRect(innerX, innerY, INNER_WIDTH, INNER_HEIGHT);

        // Lưới radar mờ tinh tế (Grid lines mỗi 20px)
        g.setColor(new Color(30, 48, 70, 65));
        for (int lx = innerX + 20; lx < innerX + INNER_WIDTH; lx += 20) {
            g.drawLine(lx, innerY, lx, innerY + INNER_HEIGHT);
        }
        for (int ly = innerY + 18; ly < innerY + INNER_HEIGHT; ly += 18) {
            g.drawLine(innerX, ly, innerX + INNER_WIDTH, ly);
        }

        // 4. Vẽ các hành lang kết nối đã được khám phá
        // QUY TẮC: Hành lang thông tới một phòng đã khám phá thì lập tức được hiển thị
        List<DungeonMap.RoomConnection> connections = dungeonMap.getActiveConnections();
        if (connections != null) {
            for (DungeonMap.RoomConnection conn : connections) {
                if (conn == null) continue;
                boolean isConnectedToDiscovered = (conn.from != null && conn.from.isDiscovered())
                        || (conn.to != null && conn.to.isDiscovered());

                if (isConnectedToDiscovered) {
                    Rectangle bounds = conn.getCorridorTileBounds();
                    int cx = innerX + (int) Math.round(bounds.x * TILE_SCALE);
                    int cy = innerY + (int) Math.round(bounds.y * TILE_SCALE);
                    int cw = (int) Math.round(bounds.width * TILE_SCALE);
                    int ch = (int) Math.round(bounds.height * TILE_SCALE);

                    // Nền hành lang
                    g.setColor(new Color(42, 58, 80, 230));
                    g.fillRect(cx, cy, cw, ch);

                    // Viền hành lang
                    g.setColor(new Color(75, 110, 155, 180));
                    g.drawRect(cx, cy, cw, ch);
                }
            }
        }

        // 5. Vẽ các căn phòng (Rooms)
        List<Room> rooms = dungeonMap.getRooms();
        if (rooms != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            FontMetrics fmRoom = g.getFontMetrics();

            for (Room r : rooms) {
                if (r == null) continue;

                int rx = innerX + (int) Math.round(r.getGridX() * TILE_SCALE);
                int ry = innerY + (int) Math.round(r.getGridY() * TILE_SCALE);
                int rw = (int) Math.round(r.getWidthInTiles() * TILE_SCALE);
                int rh = (int) Math.round(r.getHeightInTiles() * TILE_SCALE);

                if (r.isDiscovered()) {
                    // Căn phòng ĐÃ ĐƯỢC KHÁM PHÁ: Vẽ đầy đủ màu sắc nhận diện và icon
                    Color bgColor;
                    Color borderColor;
                    Color textColor;
                    String icon;

                    switch (r.getRoomType()) {
                        case SPAWN:
                            bgColor = new Color(22, 68, 50, 235);
                            borderColor = new Color(45, 215, 135, 240);
                            textColor = new Color(205, 255, 225);
                            icon = "S";
                            break;

                        case COMBAT:
                            if (r.isLocked()) {
                                bgColor = new Color(110, 22, 28, 245);
                                borderColor = new Color(255, 60, 70, 255);
                                textColor = Color.WHITE;
                                icon = "!";
                            } else if (r.isCleared()) {
                                bgColor = new Color(22, 50, 85, 235);
                                borderColor = new Color(60, 155, 240, 230);
                                textColor = new Color(185, 230, 255);
                                icon = "✔";
                            } else {
                                bgColor = new Color(78, 48, 22, 235);
                                borderColor = new Color(250, 140, 40, 235);
                                textColor = new Color(255, 215, 155);
                                icon = "⚔";
                            }
                            break;

                        case SHOP:
                            bgColor = new Color(85, 72, 18, 235);
                            borderColor = new Color(255, 215, 55, 240);
                            textColor = new Color(255, 240, 130);
                            icon = "Ⓒ";
                            break;

                        case RESET_STAT:
                            bgColor = new Color(18, 68, 88, 235);
                            borderColor = new Color(38, 205, 230, 240);
                            textColor = new Color(185, 248, 255);
                            icon = "⚙";
                            break;

                        case UPGRADE:
                            bgColor = new Color(72, 26, 92, 235);
                            borderColor = new Color(195, 72, 255, 240);
                            textColor = new Color(248, 195, 255);
                            icon = "★";
                            break;

                        case EXTRACT:
                            bgColor = new Color(18, 82, 68, 235);
                            borderColor = new Color(42, 230, 180, 240);
                            textColor = new Color(195, 255, 235);
                            icon = "H";
                            break;

                        default:
                            bgColor = new Color(35, 45, 60, 220);
                            borderColor = new Color(80, 105, 140, 200);
                            textColor = Color.WHITE;
                            icon = "";
                            break;
                    }

                    g.setColor(bgColor);
                    g.fillRoundRect(rx, ry, rw, rh, 3, 3);
                    g.setColor(borderColor);
                    g.drawRoundRect(rx, ry, rw, rh, 3, 3);

                    // Vẽ icon ở tâm phòng
                    if (!icon.isEmpty()) {
                        int iw = fmRoom.stringWidth(icon);
                        int ix = rx + (rw - iw) / 2;
                        int iy = ry + ((rh - fmRoom.getHeight()) / 2) + fmRoom.getAscent();
                        g.setColor(textColor);
                        g.drawString(icon, ix, iy);
                    }

                } else if (dungeonMap.isRoomAdjacentToDiscovered(r)) {
                    // Căn phòng KỀ CẠNH chưa bước vào: Hiện khung mờ bí ẩn và dấu "?"
                    g.setColor(new Color(20, 26, 36, 170));
                    g.fillRoundRect(rx, ry, rw, rh, 3, 3);
                    g.setColor(new Color(85, 105, 135, 180));
                    g.drawRoundRect(rx, ry, rw, rh, 3, 3);

                    String unknownIcon = "?";
                    int iw = fmRoom.stringWidth(unknownIcon);
                    int ix = rx + (rw - iw) / 2;
                    int iy = ry + ((rh - fmRoom.getHeight()) / 2) + fmRoom.getAscent();
                    g.setColor(new Color(165, 180, 200, 190));
                    g.drawString(unknownIcon, ix, iy);

                } else {
                    // Phòng ở xa chưa thông hành lang: Ẩn hoàn toàn trong Fog of War!
                }
            }
        }

        // 6. Vẽ định vị người chơi Operative (Player Blip & Pulse Effect)
        if (player != null) {
            double pTileX = player.getCenterX() / (double) GameConfig.TILE_SIZE;
            double pTileY = player.getCenterY() / (double) GameConfig.TILE_SIZE;
            int px = innerX + (int) Math.round(pTileX * TILE_SCALE);
            int py = innerY + (int) Math.round(pTileY * TILE_SCALE);

            // Vòng phát quang lan tỏa (Pulse Ring)
            double pulseProgress = (pulseTimer % 1.2) / 1.2;
            int pulseRadius = 3 + (int) (pulseProgress * 8.0);
            int pulseAlpha = Math.max(0, (int) ((1.0 - pulseProgress) * 190));
            g.setColor(new Color(255, 255, 75, pulseAlpha));
            g.drawOval(px - pulseRadius, py - pulseRadius, pulseRadius * 2, pulseRadius * 2);

            // Chấm định vị vàng chanh nổi bật
            g.setColor(new Color(255, 245, 60));
            g.fillOval(px - 2, py - 2, 5, 5);
            g.setColor(new Color(40, 35, 5));
            g.drawOval(px - 2, py - 2, 5, 5);
        }

        // 7. Vệt quét sáng radar mờ (Radar sweep line)
        if (sweepX >= 0 && sweepX <= INNER_WIDTH) {
            int sx = innerX + (int) sweepX;
            g.setColor(new Color(80, 190, 255, 30));
            g.fillRect(sx - 3, innerY, 6, INNER_HEIGHT);
            g.setColor(new Color(160, 230, 255, 65));
            g.drawLine(sx, innerY, sx, innerY + INNER_HEIGHT);
        }

        // Khôi phục vùng vẽ clip
        g.setClip(originalClip);

        // 8. Viền bọc ngoài vùng bản đồ inner
        g.setColor(new Color(45, 75, 110, 200));
        g.drawRect(innerX, innerY, INNER_WIDTH, INNER_HEIGHT);
    }
}
