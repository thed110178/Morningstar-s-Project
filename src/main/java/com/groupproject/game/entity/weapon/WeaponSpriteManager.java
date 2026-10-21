package com.groupproject.game.entity.weapon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Trình quản lý và bộ đệm (Cache) hình ảnh minh họa (Sprite) của toàn bộ các loại vũ khí trong game.
 *
 * <p>WHY: Đảm bảo tài nguyên ảnh 32x32 pixel của các loại vũ khí (AR, Shotgun, SMG, SR Bolt-Action, Pistol, Knife)
 * được nạp nhất quán, tải một lần duy nhất vào bộ nhớ RAM và tái sử dụng cho mọi thành phần hiển thị:
 * <ul>
 *   <li>1. Giao diện ba lô tác chiến PDA (BackpackUI).</li>
 *   <li>2. Vật phẩm dưới mặt đất (Hòm tiếp tế chiến thuật WeaponChest rơi súng nổi lơ lửng).</li>
 *   <li>3. Cửa hàng quân nhu dã chiến (Tactical ShopItem trưng bày trên bệ).</li>
 * </ul>
 *
 * <p>Cơ chế nạp đa tầng (Multi-tier Loading Strategy):
 * <ol>
 *   <li>Ưu tiên đọc từ Classpath/Resources đóng gói bên trong file JAR ({@code /weapons/*.png}).</li>
 *   <li>Fallback đọc trực tiếp từ thư mục file hệ thống ({@code Material/*.png} hoặc {@code release/Material/*.png}).</li>
 *   <li>Dự phòng an toàn tuyệt đối (Safety Vector Fallback): Nếu vì bất kỳ lý do nào không tải được ảnh,
 *       hệ thống tự động chuyển sang vẽ mô hình hình khối Vector sắc nét tương ứng mà không bao giờ gây Crash hoặc để trống.</li>
 * </ol>
 */
public final class WeaponSpriteManager {

    private static final Map<String, BufferedImage> SPRITE_CACHE = new HashMap<>();

    // Danh sách tên file tương ứng cho từng loại vũ khí trong Material/ và resources/weapons/
    public static final String SPRITE_AR = "Auto AR.png";
    public static final String SPRITE_SHOTGUN = "PumpedShotgun.png";
    public static final String SPRITE_SMG = "SMG.png";
    public static final String SPRITE_SNIPER = "SR bolt-action.png";
    public static final String SPRITE_PISTOL = "Pistol.png";
    public static final String SPRITE_KNIFE = "Knife.png";
    public static final String SPRITE_SEMI_CARBINE = "SemiCarbineRifle.png";

    static {
        loadAllSprites();
    }

    private WeaponSpriteManager() {
        // Lớp tiện ích tĩnh (Utility class) - Không cho phép khởi tạo trực tiếp
    }

    /**
     * Nạp toàn bộ 6 sprite vũ khí vào Cache bộ nhớ.
     */
    public static synchronized void loadAllSprites() {
        loadSprite(SPRITE_AR);
        loadSprite(SPRITE_SHOTGUN);
        loadSprite(SPRITE_SMG);
        loadSprite(SPRITE_SNIPER);
        loadSprite(SPRITE_PISTOL);
        loadSprite(SPRITE_KNIFE);
        loadSprite(SPRITE_SEMI_CARBINE);
    }

    /**
     * Tải một file sprite đơn lẻ với chiến lược đa tầng Classpath -> File hệ thống.
     *
     * @param fileName Tên file ảnh (ví dụ: "Auto AR.png")
     * @return {@link BufferedImage} hoặc {@code null} nếu không tìm thấy
     */
    public static BufferedImage loadSprite(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        if (SPRITE_CACHE.containsKey(fileName)) {
            return SPRITE_CACHE.get(fileName);
        }

        BufferedImage image = null;

        // 1. Thử tải qua Classpath (được Maven đóng gói trong root của file JAR)
        try {
            String resourcePath = "/weapons/" + fileName;
            InputStream is = WeaponSpriteManager.class.getResourceAsStream(resourcePath);
            if (is != null) {
                image = ImageIO.read(is);
                is.close();
            }
        } catch (Exception ignored) {
            // Tiếp tục fallback sang file hệ thống
        }

        // 2. Thử tải trực tiếp từ thư mục Material/ trên đĩa cứng
        if (image == null) {
            try {
                File file = new File("Material", fileName);
                if (file.exists() && file.isFile()) {
                    image = ImageIO.read(file);
                }
            } catch (Exception ignored) {
                // Tiếp tục thử các thư mục release dự phòng
            }
        }

        // 3. Thử tải từ release/Material hoặc release/app/Material
        if (image == null) {
            try {
                File file = new File("release/Material", fileName);
                if (file.exists() && file.isFile()) {
                    image = ImageIO.read(file);
                }
            } catch (Exception ignored) {
                // Bỏ qua
            }
        }

        if (image != null) {
            SPRITE_CACHE.put(fileName, image);
        }

        return image;
    }

    /**
     * Lấy Sprite tương ứng với đối tượng vũ khí {@link Weapon}.
     *
     * @param weapon Đối tượng vũ khí
     * @return {@link BufferedImage} hoặc {@code null} nếu không có ảnh
     */
    public static BufferedImage getSprite(Weapon weapon) {
        if (weapon == null) {
            return null;
        }
        if (weapon instanceof AssaultRifle) {
            return getSprite(SPRITE_AR);
        } else if (weapon instanceof Shotgun) {
            return getSprite(SPRITE_SHOTGUN);
        } else if (weapon instanceof SMG) {
            return getSprite(SPRITE_SMG);
        } else if (weapon instanceof SniperRifle) {
            return getSprite(SPRITE_SNIPER);
        } else if (weapon instanceof Pistol) {
            return getSprite(SPRITE_PISTOL);
        } else if (weapon instanceof CombatKnife) {
            return getSprite(SPRITE_KNIFE);
        } else if (weapon instanceof SemiCarbineRifle) {
            return getSprite(SPRITE_SEMI_CARBINE);
        }
        return null;
    }

    /**
     * Lấy Sprite dựa theo tên file đã nạp trong bộ đệm.
     */
    public static BufferedImage getSprite(String fileName) {
        if (fileName == null) {
            return null;
        }
        BufferedImage cached = SPRITE_CACHE.get(fileName);
        if (cached == null) {
            cached = loadSprite(fileName);
        }
        return cached;
    }

    /**
     * Vẽ ảnh minh họa Sprite vũ khí tại tọa độ tâm (centerX, centerY) với kích thước mong muốn.
     * Nếu không có ảnh Sprite hoặc chưa tải được, tự động chuyển sang vẽ hình khối Vector dự phòng sắc nét.
     *
     * @param g2d     Đối tượng đồ họa {@link Graphics2D}
     * @param weapon  Khẩu súng hoặc vũ khí cần vẽ
     * @param centerX Tọa độ X tâm điểm vẽ
     * @param centerY Tọa độ Y tâm điểm vẽ
     * @param width   Chiều rộng hiển thị (thường 32 hoặc 36 pixel)
     * @param height  Chiều cao hiển thị (thường 32 hoặc 36 pixel)
     */
    public static void renderWeaponSprite(Graphics2D g2d, Weapon weapon, int centerX, int centerY, int width, int height) {
        if (weapon == null || g2d == null) {
            return;
        }

        BufferedImage sprite = getSprite(weapon);
        if (sprite != null) {
            // Giữ cho pixel art sắc nét hoàn hảo khi phóng to (Pixel-perfect Nearest Neighbor Scaling)
            Object oldHint = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int drawX = centerX - width / 2;
            int drawY = centerY - height / 2;
            g2d.drawImage(sprite, drawX, drawY, width, height, null);

            if (oldHint != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
            }
        } else {
            // Dự phòng: Vẽ mô hình vector hình học
            renderFallbackVector(g2d, weapon, centerX, centerY);
        }
    }

    /**
     * Vẽ mô hình vector hình học dự phòng khi không tải được ảnh PNG.
     */
    public static void renderFallbackVector(Graphics2D g, Weapon weapon, int centerX, int centerY) {
        if (weapon == null || g == null) {
            return;
        }

        if (weapon instanceof AssaultRifle) {
            // Súng trường tấn công AR: Thân xám đậm, nòng đỏ/nòng súng, băng đạn cong, đầu vàng
            g.setColor(new Color(45, 55, 65));
            g.fillRect(centerX - 18, centerY - 4, 34, 7);
            g.setColor(new Color(255, 30, 35));
            g.fillRect(centerX + 8, centerY - 2, 12, 3);
            g.fillRect(centerX - 6, centerY + 3, 6, 9);
            g.setColor(new Color(255, 215, 60));
            g.fillRect(centerX + 19, centerY - 3, 3, 5);
        } else if (weapon instanceof Shotgun) {
            // Súng shotgun: Báng gỗ nâu, nòng thép đôi xám lớn, điểm ngắm cam
            g.setColor(new Color(139, 69, 19));
            g.fillRect(centerX - 14, centerY - 4, 12, 8);
            g.setColor(new Color(55, 60, 70));
            g.fillRect(centerX - 2, centerY - 5, 24, 9);
            g.setColor(new Color(255, 140, 30));
            g.fillRect(centerX + 21, centerY - 4, 3, 7);
        } else if (weapon instanceof SMG) {
            // Súng tiểu liên SMG: Thân ngắn xám titan, hộp tiếp đạn thẳng dài, nòng ngắn cyan neon
            g.setColor(new Color(40, 50, 62));
            g.fillRect(centerX - 14, centerY - 4, 24, 7);
            g.setColor(new Color(20, 26, 32));
            g.fillRect(centerX - 4, centerY + 3, 5, 10);
            g.setColor(new Color(25, 32, 40));
            g.fillRect(centerX + 10, centerY - 2, 7, 3);
            g.setColor(new Color(0, 230, 255));
            g.fillRect(centerX + 16, centerY - 3, 3, 5);
        } else if (weapon instanceof SniperRifle) {
            // Súng bắn tỉa SR Bolt-action: Thân súng nòng dài 38px, ống ngắm tím neon, muzzle brake
            g.setColor(new Color(40, 46, 54));
            g.fillRect(centerX - 20, centerY - 3, 38, 5);
            g.setColor(new Color(185, 80, 255));
            g.fillRect(centerX - 6, centerY - 7, 14, 4);
            g.setColor(new Color(220, 200, 255));
            g.fillRect(centerX + 16, centerY - 4, 3, 7);
        } else if (weapon instanceof Pistol) {
            // Súng lục Pistol: Thân nhỏ gọn, nòng đen bạc, tay cầm ngắn
            g.setColor(new Color(50, 55, 65));
            g.fillRect(centerX - 8, centerY - 4, 18, 6);
            g.setColor(new Color(25, 30, 35));
            g.fillRect(centerX - 6, centerY + 2, 6, 8);
            g.setColor(new Color(200, 210, 225));
            g.fillRect(centerX + 8, centerY - 3, 3, 4);
        } else if (weapon instanceof CombatKnife) {
            // Dao găm cận chiến: Cán bọc cao su đen, lưỡi dao thép phản quang sáng
            g.setColor(new Color(30, 30, 35));
            g.fillRect(centerX - 12, centerY - 2, 10, 5);
            g.setColor(new Color(220, 230, 245));
            g.fillRect(centerX - 1, centerY - 3, 16, 4);
            g.setColor(new Color(120, 130, 140));
            g.fillRect(centerX - 2, centerY - 5, 3, 8);
        } else if (weapon instanceof SemiCarbineRifle) {
            // Súng trường bán tự động Semi-Carbine: Thân dài màu xanh ô liu quân sự, báng composite, nòng thép thon dài, hộp tiếp đạn 20 viên
            g.setColor(new Color(55, 68, 60));
            g.fillRect(centerX - 16, centerY - 4, 32, 6);
            g.setColor(new Color(35, 42, 38));
            g.fillRect(centerX - 6, centerY + 2, 5, 8);
            g.setColor(new Color(75, 85, 80));
            g.fillRect(centerX + 8, centerY - 3, 10, 3);
            g.setColor(new Color(210, 225, 235));
            g.fillRect(centerX + 18, centerY - 3, 3, 4);
        }
    }
}
