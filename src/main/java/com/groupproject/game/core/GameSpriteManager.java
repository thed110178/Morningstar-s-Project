package com.groupproject.game.core;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Trình quản lý và lưu bộ đệm (Cache) toàn bộ tài nguyên hình ảnh đồ họa (Sprites & Textures) của game.
 *
 * <p>WHY: Đảm bảo toàn bộ tài nguyên hình ảnh của game (nhân vật, kẻ địch, vật che chắn, địa hình, hiệu ứng)
 * được nạp một lần duy nhất vào bộ nhớ RAM, hỗ trợ chiến lược nạp đa tầng (Classpath -> Thư mục đĩa cứng)
 * và giữ nguyên độ sắc nét pixel art (Nearest Neighbor interpolation) khi co giãn hoặc xoay theo góc ngắm.
 */
public final class GameSpriteManager {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    // Đường dẫn tài nguyên Classpath
    public static final String RES_PLAYER_GUN = "/player/PlayerGun.png";
    public static final String RES_PLAYER_MELEE = "/player/PlayerMelee.png";
    public static final String RES_ENEMY_GUN = "/enemy/Enemy gun.png";
    public static final String RES_ENEMY_MELEE = "/enemy/Enemy Melee.png";
    public static final String RES_ENEMY_MEDIC = "/enemy/EnemyMedic.png";
    public static final String RES_ENEMY_GRENADIER = "/enemy/EnemyGrenader.png";
    public static final String RES_COVER_STONE = "/cover/Stone.png";
    public static final String RES_COVER_BARREL = "/cover/Wooden barrel.png";
    public static final String RES_TEX_BRICK = "/textures/Grey brick.png";
    public static final String RES_TEX_DIRT = "/textures/Dirt.png";
    public static final String RES_TEX_GRASS = "/textures/Grass.png";
    public static final String RES_EFFECT_SLASH = "/effects/Slash.png";

    // Đường dẫn fallback trên đĩa cứng Material/
    public static final String FILE_PLAYER_GUN = "Material/Player/PlayerGun.png";
    public static final String FILE_PLAYER_MELEE = "Material/Player/PlayerMelee";
    public static final String FILE_ENEMY_GUN = "Material/Enemy/Enemygun/Enemy gun.png";
    public static final String FILE_ENEMY_MELEE = "Material/Enemy/EnemyMelee/Enemy Melee.png";
    public static final String FILE_ENEMY_MEDIC = "Material/Enemy/Medic/EnemyMedic.png";
    public static final String FILE_ENEMY_GRENADIER = "Material/Enemy/Grenader/EnemyGrenader.png";
    public static final String FILE_COVER_STONE = "Material/Cover/Stone.png";
    public static final String FILE_COVER_BARREL = "Material/Cover/Wooden barrel.png";
    public static final String FILE_TEX_BRICK = "Material/Texture/Grey brick.png";
    public static final String FILE_TEX_DIRT = "Material/Texture/Dirt.png";
    public static final String FILE_TEX_GRASS = "Material/Texture/Grass.png";
    public static final String FILE_EFFECT_SLASH = "Material/Effect/Slash.png";

    // Bộ đệm hình ảnh đã pre-scale sẵn kích thước chính xác và tương thích VRAM
    private static BufferedImage preScaledTextureDirt;
    private static BufferedImage preScaledTextureGrass;
    private static BufferedImage preScaledTextureGreyBrick;
    private static BufferedImage preScaledCoverStone;
    private static BufferedImage preScaledCoverBarrel;

    static {
        loadAll();
    }

    private GameSpriteManager() {
        // Utility class
    }

    /**
     * Nạp toàn bộ sprite vào bộ nhớ Cache và tạo trước các bộ đệm ảnh tương thích phần cứng.
     */
    public static synchronized void loadAll() {
        loadSprite(RES_PLAYER_GUN, FILE_PLAYER_GUN);
        loadSprite(RES_PLAYER_MELEE, FILE_PLAYER_MELEE);
        loadSprite(RES_ENEMY_GUN, FILE_ENEMY_GUN);
        loadSprite(RES_ENEMY_MELEE, FILE_ENEMY_MELEE);
        loadSprite(RES_ENEMY_MEDIC, FILE_ENEMY_MEDIC);
        loadSprite(RES_ENEMY_GRENADIER, FILE_ENEMY_GRENADIER);
        loadSprite(RES_COVER_STONE, FILE_COVER_STONE);
        loadSprite(RES_COVER_BARREL, FILE_COVER_BARREL);
        loadSprite(RES_TEX_BRICK, FILE_TEX_BRICK);
        loadSprite(RES_TEX_DIRT, FILE_TEX_DIRT);
        loadSprite(RES_TEX_GRASS, FILE_TEX_GRASS);
        loadSprite(RES_EFFECT_SLASH, FILE_EFFECT_SLASH);

        // Khởi tạo trước bộ đệm pre-scaled tương thích phần cứng cho các texture địa hình tần suất vẽ cao
        preScaledTextureDirt = createPreScaledImage(getTextureDirt(), GameConfig.TILE_SIZE + 1, GameConfig.TILE_SIZE + 1);
        preScaledTextureGrass = createPreScaledImage(getTextureGrass(), GameConfig.TILE_SIZE + 1, GameConfig.TILE_SIZE + 1);
        preScaledTextureGreyBrick = createPreScaledImage(getTextureGreyBrick(), GameConfig.TILE_SIZE + 1, GameConfig.TILE_SIZE + 1);
        preScaledCoverStone = createPreScaledImage(getCoverStoneSprite(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
        preScaledCoverBarrel = createPreScaledImage(getCoverBarrelSprite(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
    }

    /**
     * Chuyển đổi một BufferedImage sang định dạng tương thích với cấu hình đồ họa phần cứng (Hardware Compatible Image).
     *
     * <p>WHY: BufferedImage mặc định từ ImageIO.read() thường có định dạng TYPE_4BYTE_ABGR hoặc TYPE_CUSTOM.
     * Khi vẽ (drawImage) lên màn hình, driver đồ họa phải dùng CPU giải nén và convert từng pixel sang định dạng VRAM.
     * Chuyển đổi trước sang Compatible Image giúp hình ảnh được lưu trữ trực tiếp dưới dạng VRAM Surface,
     * cho phép GPU Direct3D/OpenGL thực hiện sao chép khối pixel (BitBlt) với tốc độ phần cứng nguyên bản.
     */
    public static BufferedImage toCompatibleImage(BufferedImage image) {
        if (image == null) {
            return null;
        }

        GraphicsConfiguration gc = null;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (!ge.isHeadless()) {
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                if (gd != null) {
                    gc = gd.getDefaultConfiguration();
                }
            }
        } catch (Exception ignored) {
            // Môi trường headless (chạy test không có màn hình)
        }

        int transparency = image.getColorModel().hasAlpha() ? Transparency.TRANSLUCENT : Transparency.OPAQUE;

        if (gc != null) {
            BufferedImage compatible = gc.createCompatibleImage(image.getWidth(), image.getHeight(), transparency);
            Graphics2D g2 = compatible.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
            return compatible;
        }

        // Fallback chuẩn cho môi trường headless/test
        int targetType = (transparency == Transparency.TRANSLUCENT)
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        if (image.getType() == targetType) {
            return image;
        }
        BufferedImage fallback = new BufferedImage(image.getWidth(), image.getHeight(), targetType);
        Graphics2D g2 = fallback.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return fallback;
    }

    /**
     * Tạo trước một bản sao hình ảnh tương thích phần cứng với kích thước pixel mong muốn (Pre-scaled Image).
     *
     * <p>WHY: Tránh việc nội suy co giãn hình ảnh (rescaling) 30.000 lần mỗi giây trong vòng lặp game.
     * Thực hiện co giãn một lần duy nhất lúc nạp tài nguyên và lưu vào bộ nhớ đệm giúp lệnh drawImage
     * đạt hiệu năng tối đa 1:1.
     */
    public static BufferedImage createPreScaledImage(BufferedImage src, int targetWidth, int targetHeight) {
        if (src == null) {
            return null;
        }

        GraphicsConfiguration gc = null;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (!ge.isHeadless()) {
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                if (gd != null) {
                    gc = gd.getDefaultConfiguration();
                }
            }
        } catch (Exception ignored) {
        }

        int transparency = src.getColorModel().hasAlpha() ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
        BufferedImage scaled;
        if (gc != null) {
            scaled = gc.createCompatibleImage(targetWidth, targetHeight, transparency);
        } else {
            scaled = new BufferedImage(targetWidth, targetHeight,
                    transparency == Transparency.TRANSLUCENT ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return scaled;
    }

    /**
     * Nạp ảnh với chiến lược đa tầng: Classpath -> Thư mục đĩa cứng dự phòng.
     * Tự động chuyển đổi sang Hardware Compatible Image.
     */
    public static BufferedImage loadSprite(String resourcePath, String fallbackFilePath) {
        if (resourcePath == null) {
            return null;
        }

        if (CACHE.containsKey(resourcePath)) {
            return CACHE.get(resourcePath);
        }

        BufferedImage img = null;

        // 1. Thử tải qua Classpath (trong file JAR)
        try {
            InputStream is = GameSpriteManager.class.getResourceAsStream(resourcePath);
            if (is != null) {
                img = ImageIO.read(is);
                is.close();
            }
        } catch (Exception ignored) {
            // Fallback tiếp
        }

        // 2. Thử tải từ đĩa cứng (thư mục Material/)
        if (img == null && fallbackFilePath != null) {
            try {
                File f = new File(fallbackFilePath);
                if (f.exists() && f.isFile()) {
                    img = ImageIO.read(f);
                }
            } catch (Exception ignored) {
                // Bỏ qua
            }
        }

        // 3. Thử tải từ release/Material/ hoặc release/app/Material/
        if (img == null && fallbackFilePath != null) {
            try {
                File f = new File("release/" + fallbackFilePath);
                if (f.exists() && f.isFile()) {
                    img = ImageIO.read(f);
                }
            } catch (Exception ignored) {
                // Bỏ qua
            }
        }

        if (img != null) {
            img = toCompatibleImage(img);
            CACHE.put(resourcePath, img);
        }

        return img;
    }

    public static BufferedImage getPlayerGunSprite() {
        return loadSprite(RES_PLAYER_GUN, FILE_PLAYER_GUN);
    }

    public static BufferedImage getPlayerMeleeSprite() {
        return loadSprite(RES_PLAYER_MELEE, FILE_PLAYER_MELEE);
    }

    public static BufferedImage getEnemyGunSprite() {
        return loadSprite(RES_ENEMY_GUN, FILE_ENEMY_GUN);
    }

    public static BufferedImage getEnemyMeleeSprite() {
        return loadSprite(RES_ENEMY_MELEE, FILE_ENEMY_MELEE);
    }

    public static BufferedImage getEnemyMedicSprite() {
        return loadSprite(RES_ENEMY_MEDIC, FILE_ENEMY_MEDIC);
    }

    public static BufferedImage getEnemyGrenadierSprite() {
        return loadSprite(RES_ENEMY_GRENADIER, FILE_ENEMY_GRENADIER);
    }

    public static BufferedImage getCoverStoneSprite() {
        return loadSprite(RES_COVER_STONE, FILE_COVER_STONE);
    }

    public static BufferedImage getCoverBarrelSprite() {
        return loadSprite(RES_COVER_BARREL, FILE_COVER_BARREL);
    }

    public static BufferedImage getTextureGreyBrick() {
        return loadSprite(RES_TEX_BRICK, FILE_TEX_BRICK);
    }

    public static BufferedImage getTextureDirt() {
        return loadSprite(RES_TEX_DIRT, FILE_TEX_DIRT);
    }

    public static BufferedImage getTextureGrass() {
        return loadSprite(RES_TEX_GRASS, FILE_TEX_GRASS);
    }

    public static BufferedImage getEffectSlash() {
        return loadSprite(RES_EFFECT_SLASH, FILE_EFFECT_SLASH);
    }

    public static BufferedImage getPreScaledTextureDirt() {
        if (preScaledTextureDirt == null) {
            preScaledTextureDirt = createPreScaledImage(getTextureDirt(), GameConfig.TILE_SIZE + 1, GameConfig.TILE_SIZE + 1);
        }
        return preScaledTextureDirt;
    }

    public static BufferedImage getPreScaledTextureGrass() {
        if (preScaledTextureGrass == null) {
            preScaledTextureGrass = createPreScaledImage(getTextureGrass(), GameConfig.TILE_SIZE + 1, GameConfig.TILE_SIZE + 1);
        }
        return preScaledTextureGrass;
    }

    public static BufferedImage getPreScaledTextureGreyBrick() {
        if (preScaledTextureGreyBrick == null) {
            preScaledTextureGreyBrick = createPreScaledImage(getTextureGreyBrick(), GameConfig.TILE_SIZE + 1, GameConfig.TILE_SIZE + 1);
        }
        return preScaledTextureGreyBrick;
    }

    public static BufferedImage getPreScaledCoverStone() {
        if (preScaledCoverStone == null) {
            preScaledCoverStone = createPreScaledImage(getCoverStoneSprite(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
        }
        return preScaledCoverStone;
    }

    public static BufferedImage getPreScaledCoverBarrel() {
        if (preScaledCoverBarrel == null) {
            preScaledCoverBarrel = createPreScaledImage(getCoverBarrelSprite(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
        }
        return preScaledCoverBarrel;
    }

    /**
     * Vẽ ảnh xoay quanh tâm (centerX, centerY) với góc ngắm (angle).
     * Do sprite gốc hướng lên trên (-PI/2), góc xoay trong hệ tọa độ vẽ là `angle + Math.PI / 2`.
     * Tối ưu hóa biến đổi ma trận AffineTransform trực tiếp, tránh cấp phát và hủy Graphics2D mỗi frame.
     *
     * @param g       Đối tượng đồ họa Graphics2D.
     * @param img     Ảnh cần vẽ.
     * @param centerX Tọa độ X tâm điểm vẽ.
     * @param centerY Tọa độ Y tâm điểm vẽ.
     * @param angle   Góc hướng về phía mục tiêu (radians, 0 rad là hướng sang phải).
     * @param width   Chiều rộng hiển thị.
     * @param height  Chiều cao hiển thị.
     */
    public static void renderRotatedSprite(Graphics2D g, BufferedImage img, int centerX, int centerY,
                                           double angle, int width, int height) {
        if (img == null || g == null) {
            return;
        }

        AffineTransform oldTransform = g.getTransform();
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Dịch chuyển về tâm đối tượng và xoay hướng
        g.translate(centerX, centerY);
        g.rotate(angle + Math.PI / 2.0);

        int drawX = -width / 2;
        int drawY = -height / 2;
        g.drawImage(img, drawX, drawY, width, height, null);

        if (oldHint != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
        }
        g.setTransform(oldTransform);
    }
}
