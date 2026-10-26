package com.groupproject.game.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

    static {
        loadAll();
    }

    private GameSpriteManager() {
        // Utility class
    }

    /**
     * Nạp toàn bộ sprite vào bộ nhớ Cache khi khởi động game.
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
    }

    /**
     * Nạp ảnh với chiến lược đa tầng: Classpath -> Thư mục đĩa cứng dự phòng.
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

    /**
     * Vẽ ảnh xoay quanh tâm (centerX, centerY) với góc ngắm (angle).
     * Do sprite gốc hướng lên trên (-PI/2), góc xoay trong hệ tọa độ vẽ là `angle + Math.PI / 2`.
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

        Graphics2D g2 = (Graphics2D) g.create();
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Dịch chuyển về tâm đối tượng và xoay hướng
        g2.translate(centerX, centerY);
        g2.rotate(angle + Math.PI / 2.0);

        int drawX = -width / 2;
        int drawY = -height / 2;
        g2.drawImage(img, drawX, drawY, width, height, null);

        if (oldHint != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
        }
        g2.dispose();
    }
}
