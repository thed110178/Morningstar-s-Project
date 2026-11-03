package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.core.GameSoundManager;
import com.groupproject.game.core.GameSoundManager.SoundEffect;
import com.groupproject.game.core.GameSpriteManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Represents an individual square terrain unit positioned on the discrete dungeon grid.
 *
 * <p>WHY: Encapsulating grid coordinates, world-space bounds, and tile classifications into
 * a dedicated object ensures the physics system can rapidly query whether an entity's hitbox
 * intersects impassable geography without performing expensive arbitrary polygon checks.
 */
public class Tile {

    private static TileExplosionListener explosionListener;

    public static void setExplosionListener(TileExplosionListener listener) {
        explosionListener = listener;
    }

    public static TileExplosionListener getExplosionListener() {
        return explosionListener;
    }

    private final int gridX;
    private final int gridY;
    private final int worldX;
    private final int worldY;
    private TileType type;
    private int health = 0;
    private int maxHealth = 0;
    private boolean hasGrass = false;
    /**
     * Chỉ số biến thể hoa văn cỏ - đất (1-26 từ Grass_pattern, hoặc 0 nếu dùng texture gốc).
     *
     * <p>WHY: Lưu trữ trước kết quả autotiling được tính toán lúc tạo bản đồ (DungeonMap),
     * giúp quá trình dựng hình mỗi khung hình (render) đạt tốc độ O(1) tuyệt đối bằng cách
     * tra cứu trực tiếp hình ảnh tương thích phần cứng từ GameSpriteManager, không tính toán lại lân cận.
     */
    private int grassPattern = 0;

    /**
     * Constructs a tile at the designated grid coordinates.
     *
     * @param gridX Horizontal column index on the map grid.
     * @param gridY Vertical row index on the map grid.
     * @param type  The classification determining collision behavior and visual style.
     */
    public Tile(int gridX, int gridY, TileType type) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldX = gridX * GameConfig.TILE_SIZE;
        this.worldY = gridY * GameConfig.TILE_SIZE;
        setType(type);
    }

    /**
     * Checks if this tile blocks movement (physical obstruction).
     */
    public boolean isSolid() {
        return type.isSolid();
    }

    /**
     * Checks if this tile blocks or absorbs projectile trajectories.
     *
     * <p>WHY: Walls, cover barriers, and closed doors block projectiles physically.
     * Open doors (DOOR_OPEN) also absorb bullets to prevent operatives from cheesing
     * combat encounters by eliminating hostiles from the outside corridor before entering.
     */
    public boolean blocksProjectiles() {
        return isSolid() || type == TileType.DOOR_OPEN;
    }

    public int getGridX() {
        return gridX;
    }

    public int getCol() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getRow() {
        return gridY;
    }

    public int getWorldX() {
        return worldX;
    }

    public int getWorldY() {
        return worldY;
    }

    public double getCenterX() {
        return worldX + GameConfig.TILE_SIZE / 2.0;
    }

    public double getCenterY() {
        return worldY + GameConfig.TILE_SIZE / 2.0;
    }

    public TileType getType() {
        return type;
    }

    public final void setType(TileType type) {
        this.type = type;
        if (type == TileType.DESTRUCTIBLE_COVER) {
            this.maxHealth = 40;
            this.health = 40;
        } else if (type == TileType.EXPLOSIVE_BARREL) {
            this.maxHealth = 20;
            this.health = 20;
        } else {
            this.health = 0;
            this.maxHealth = 0;
        }
        if (type != TileType.FLOOR && type != TileType.DESTRUCTIBLE_COVER && type != TileType.EXPLOSIVE_BARREL && type != TileType.COVER) {
            this.hasGrass = false;
            this.grassPattern = 0;
        }
    }

    public boolean hasGrass() {
        return hasGrass;
    }

    public void setHasGrass(boolean hasGrass) {
        this.hasGrass = hasGrass;
    }

    public int getGrassPattern() {
        return grassPattern;
    }

    public void setGrassPattern(int grassPattern) {
        this.grassPattern = grassPattern;
    }

    /**
     * Cờ đánh dấu ô này có tiếp xúc với không gian mở (phòng, hành lang) hay không.
     * Dùng cho Void Culling: Các ô tường ngầm trong bóng tối (exposed = false) sẽ không cần vẽ.
     * Mặc định là true để giữ tương thích an toàn nếu chưa chạy thuật toán culling.
     */
    private boolean exposed = true;

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    /**
     * Xác định xem ô này có phải là cover tạm thời có thể phá hủy được hay không.
     */
    public boolean isDestructible() {
        return type == TileType.DESTRUCTIBLE_COVER || type == TileType.EXPLOSIVE_BARREL;
    }

    public boolean isExplosiveBarrel() {
        return type == TileType.EXPLOSIVE_BARREL;
    }

    /**
     * Gây sát thương lên ô cover tạm thời. Khi máu giảm về 0 hoặc thấp hơn,
     * ô này sẽ bị phá hủy và tự động chuyển hóa thành ô sàn thông thường (FLOOR).
     *
     * @param damage Lượng sát thương từ đạn bắn hoặc nhát chém cận chiến.
     * @return true nếu ô cover đã bị phá hủy hoàn toàn.
     */
    public boolean takeDamage(int damage) {
        if (!isDestructible() || damage <= 0) {
            return false;
        }
        boolean wasExplosive = isExplosiveBarrel();
        this.health -= damage;
        if (this.health <= 0) {
            setType(TileType.FLOOR);
            if (wasExplosive) {
                if (explosionListener != null) {
                    explosionListener.onTileExploded(gridX, gridY, getCenterX(), getCenterY(), 110.0, 65);
                } else {
                    GameSoundManager.play(SoundEffect.GRENADIER_EXPLOSION);
                }
            } else {
                GameSoundManager.play(SoundEffect.HIT_CRATE_BREAKING);
            }
            return true;
        }
        GameSoundManager.play(wasExplosive ? SoundEffect.HIT_UNBREAKABLE_WALL : SoundEffect.HIT_CRATE);
        return false;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Renders the tile surface at the given screen-space coordinates.
     *
     * <p>WHY: Decouples tile drawing from absolute world coordinates by accepting pre-calculated
     * screen coordinates from the camera offset, supporting efficient viewport culling.
     *
     * @param g       2D Graphics context.
     * @param screenX Pixel X position on the canvas.
     * @param screenY Pixel Y position on the canvas.
     */
    public void render(Graphics2D g, int screenX, int screenY) {
        int size = GameConfig.TILE_SIZE;
        // Bù giáp mí 1px (drawSize = size + 1) để triệt tiêu hoàn toàn khe hở subpixel khi cửa sổ game co giãn scale
        int drawSize = size + 1;

        // 1. Base surface fill: Tối ưu 1-pass không overdraw cho các ô có texture VRAM (FLOOR, WALL, COVER, BARREL)
        if (type == TileType.FLOOR) {
            BufferedImage floorTex = null;
            if (grassPattern >= 1 && grassPattern <= 30) {
                floorTex = GameSpriteManager.getPreScaledGrassPattern(grassPattern);
            }
            if (floorTex == null) {
                floorTex = hasGrass ? GameSpriteManager.getPreScaledTextureGrass() : GameSpriteManager.getPreScaledTextureDirt();
            }
            if (floorTex != null) {
                g.drawImage(floorTex, screenX, screenY, null);
            } else {
                g.setColor(hasGrass ? new Color(55, 110, 50) : type.getDefaultColor());
                g.fillRect(screenX, screenY, drawSize, drawSize);
            }
        } else if (type == TileType.WALL) {
            BufferedImage brickTex = GameSpriteManager.getPreScaledTextureGreyBrick();
            if (brickTex != null) {
                g.drawImage(brickTex, screenX, screenY, null);
            } else {
                g.setColor(type.getDefaultColor());
                g.fillRect(screenX, screenY, drawSize, drawSize);
                // Beveled 3D top-down wall border
                g.setColor(new Color(65, 75, 100));
                g.drawRect(screenX, screenY, size - 1, size - 1);
                g.setColor(new Color(30, 35, 48));
                g.fillRect(screenX + 4, screenY + 4, size - 8, size - 8);
            }
        } else if (type == TileType.COVER) {
            // Nền sàn bên dưới cột cover
            BufferedImage floorTex = null;
            if (grassPattern >= 1 && grassPattern <= 30) {
                floorTex = GameSpriteManager.getPreScaledGrassPattern(grassPattern);
            }
            if (floorTex == null) {
                floorTex = hasGrass ? GameSpriteManager.getPreScaledTextureGrass() : GameSpriteManager.getPreScaledTextureDirt();
            }
            if (floorTex != null) {
                g.drawImage(floorTex, screenX, screenY, null);
            }
            BufferedImage stoneTex = GameSpriteManager.getPreScaledCoverStone();
            if (stoneTex != null) {
                g.drawImage(stoneTex, screenX, screenY, null);
            } else {
                g.setColor(type.getDefaultColor());
                g.fillRect(screenX, screenY, drawSize, drawSize);
                // Tactical cover block with cross-hatch pattern
                g.setColor(new Color(90, 105, 135));
                g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
                g.drawLine(screenX + 4, screenY + 4, screenX + size - 5, screenY + size - 5);
                g.drawLine(screenX + size - 5, screenY + 4, screenX + 4, screenY + size - 5);
            }
        } else if (type == TileType.DESTRUCTIBLE_COVER) {
            // Nền sàn bên dưới thùng gỗ
            BufferedImage floorTex = null;
            if (grassPattern >= 1 && grassPattern <= 30) {
                floorTex = GameSpriteManager.getPreScaledGrassPattern(grassPattern);
            }
            if (floorTex == null) {
                floorTex = hasGrass ? GameSpriteManager.getPreScaledTextureGrass() : GameSpriteManager.getPreScaledTextureDirt();
            }
            if (floorTex != null) {
                g.drawImage(floorTex, screenX, screenY, null);
            }
            BufferedImage barrelTex = GameSpriteManager.getPreScaledCoverBarrel();
            if (barrelTex != null) {
                g.drawImage(barrelTex, screenX, screenY, null);
            } else {
                g.setColor(type.getDefaultColor());
                g.fillRect(screenX, screenY, drawSize, drawSize);
                // Thùng gỗ che chắn chiến thuật phá hủy được (Tactical Wooden Crate)
                g.setColor(new Color(85, 55, 30));
                g.drawRect(screenX + 1, screenY + 1, size - 3, size - 3);
                g.setColor(new Color(105, 75, 40));
                g.drawLine(screenX + 4, screenY + 4, screenX + size - 5, screenY + size - 5);
                g.drawLine(screenX + size - 5, screenY + 4, screenX + 4, screenY + size - 5);
                g.setColor(new Color(70, 75, 85));
                int bSize = 6;
                g.fillRect(screenX + 2, screenY + 2, bSize, bSize);
                g.fillRect(screenX + size - 2 - bSize, screenY + 2, bSize, bSize);
                g.fillRect(screenX + 2, screenY + size - 2 - bSize, bSize, bSize);
                g.fillRect(screenX + size - 2 - bSize, screenY + size - 2 - bSize, bSize, bSize);
            }
        } else if (type == TileType.EXPLOSIVE_BARREL) {
            // Vẽ lớp nền sàn đất/cỏ trước để các pixel trong suốt (4 góc của hình tròn thùng phi) hòa nhập mượt mà vào sàn phòng, không bị lộ nền đen
            BufferedImage floorTex = null;
            if (grassPattern >= 1 && grassPattern <= 30) {
                floorTex = GameSpriteManager.getPreScaledGrassPattern(grassPattern);
            }
            if (floorTex == null) {
                floorTex = hasGrass ? GameSpriteManager.getPreScaledTextureGrass() : GameSpriteManager.getPreScaledTextureDirt();
            }
            if (floorTex != null) {
                g.drawImage(floorTex, screenX, screenY, null);
            } else {
                g.setColor(hasGrass ? new Color(55, 110, 50) : TileType.FLOOR.getDefaultColor());
                g.fillRect(screenX, screenY, drawSize, drawSize);
            }

            BufferedImage redBarrelTex = GameSpriteManager.getPreScaledCoverRedBarrel();
            if (redBarrelTex != null) {
                g.drawImage(redBarrelTex, screenX, screenY, null);
            } else {
                g.setColor(new Color(200, 30, 30));
                g.fillOval(screenX + 2, screenY + 2, size - 4, size - 4);
                g.setColor(new Color(40, 40, 40));
                g.drawOval(screenX + 2, screenY + 2, size - 4, size - 4);
                g.fillOval(screenX + size / 2 - 4, screenY + size / 2 - 4, 8, 8);
            }
        } else {
            // Các ô chức năng và vùng chiến thuật đặc biệt (SPAWN_ZONE, DOOR_OPEN, DOOR_CLOSED, EXTRACT_ZONE...)
            // Tô lớp nền màu đặc trưng (defaultColor) để không bị lộ nền đen của GamePanel
            g.setColor(type.getDefaultColor());
            g.fillRect(screenX, screenY, drawSize, drawSize);
        }

        // 2. Visual ornamentation based on tile classification
        if (type == TileType.DESTRUCTIBLE_COVER || type == TileType.EXPLOSIVE_BARREL) {
            // Hiệu ứng rạn nứt khi bị nhận sát thương
            if (health < maxHealth) {
                if (type == TileType.DESTRUCTIBLE_COVER) {
                    g.setColor(new Color(25, 15, 10, 220));
                    g.drawLine(screenX + 14, screenY + 8, screenX + 26, screenY + 24);
                    g.drawLine(screenX + 26, screenY + 24, screenX + 20, screenY + 38);
                    if (health <= maxHealth / 2) {
                        g.drawLine(screenX + 34, screenY + 12, screenX + 28, screenY + 28);
                        g.drawLine(screenX + 10, screenY + 30, screenX + 22, screenY + 32);
                    }
                }

                // Thanh HP nhỏ phía trên đỉnh thùng: đỏ cam cho thùng phi nổ, vàng cam cho thùng gỗ
                int barW = 30;
                int barH = 4;
                int barX = screenX + (size - barW) / 2;
                int barY = screenY + 4;
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(barX, barY, barW, barH);
                int fillW = (int) Math.round((barW * health) / (double) maxHealth);
                g.setColor(type == TileType.EXPLOSIVE_BARREL ? new Color(255, 60, 40) : new Color(255, 160, 40));
                g.fillRect(barX, barY, fillW, barH);
                g.setColor(new Color(255, 255, 255, 140));
                g.drawRect(barX, barY, barW, barH);
            }
        } else if (type == TileType.DOOR_CLOSED) {
            // Heavy lockdown blast gate with bright red warning barrier
            g.setColor(new Color(255, 60, 60));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.setStroke(new BasicStroke(2.5f));
            g.drawLine(screenX + 6, screenY + size / 2, screenX + size - 6, screenY + size / 2);
            g.drawLine(screenX + size / 2, screenY + 6, screenX + size / 2, screenY + size - 6);
            g.setStroke(new BasicStroke(1.0f));
            // Warning beacon dot
            g.setColor(new Color(255, 220, 50));
            g.fillOval(screenX + size / 2 - 3, screenY + size / 2 - 3, 6, 6);
        } else if (type == TileType.DOOR_OPEN) {
            // Unlocked green transition threshold
            g.setColor(new Color(50, 220, 120, 140));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.drawLine(screenX + 6, screenY + 8, screenX + size - 6, screenY + 8);
            g.drawLine(screenX + 6, screenY + size - 8, screenX + size - 6, screenY + size - 8);
        } else if (type == TileType.DOOR_SHOP) {
            // Permanent open Tactical Shop doorway with warm amber floor and neon gold portal threshold
            g.setColor(new Color(45, 38, 20));
            g.fillRect(screenX, screenY, size, size);

            // Glowing golden border
            g.setColor(new Color(255, 215, 60, 210));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);

            // Energy threshold scanlines
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(screenX + 5, screenY + 7, screenX + size - 5, screenY + 7);
            g.drawLine(screenX + 5, screenY + size - 7, screenX + size - 5, screenY + size - 7);

            // Holographic diamond credit beacon in center
            int cx = screenX + size / 2;
            int cy = screenY + size / 2;
            g.setColor(new Color(255, 225, 75));
            int[] dx = { cx, cx + 5, cx, cx - 5 };
            int[] dy = { cy - 5, cy, cy + 5, cy };
            g.drawPolygon(dx, dy, 4);
            g.setColor(new Color(255, 250, 180));
            g.fillOval(cx - 2, cy - 2, 4, 4);
            g.setStroke(new BasicStroke(1.0f));
        } else if (type == TileType.DOOR_RESET_STAT) {
            // Ngưỡng cửa Xưởng Quân Khí Dã Chiến: Tấm thép dập gân với sọc chéo cảnh báo Vàng - Đen (Hazard Stripes)
            // Áp dụng Graphics2D clipRect để đảm bảo dải sọc không bao giờ tràn ra ngoài phạm vi ô cửa
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clipRect(screenX, screenY, size, size);

            // Nền thép tấm chịu lực dã chiến
            g2.setColor(new Color(38, 42, 44));
            g2.fillRect(screenX, screenY, size, size);

            // Dải sọc xiên chéo cảnh báo màu Vàng Dã Chiến - Đen quân sự
            g2.setStroke(new BasicStroke(2.5f));
            for (int offset = -size; offset < size * 2; offset += 10) {
                g2.setColor(new Color(230, 185, 30, 210)); // Vàng dã chiến
                g2.drawLine(screenX + offset, screenY, screenX + offset + 12, screenY + size);
            }

            // Khung viền kim loại nẹp 4 cạnh
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(new Color(85, 95, 92));
            g2.drawRect(screenX, screenY, size - 1, size - 1);

            // Nẹp thép gia cố 2 đầu (trên & dưới) chống mài mòn
            g2.setColor(new Color(22, 25, 27));
            g2.fillRect(screenX, screenY, size, 3);
            g2.fillRect(screenX, screenY + size - 3, size, 3);

            g2.dispose();
        } else if (type == TileType.DOOR_UPGRADE) {
            // Ngưỡng cửa Phòng Nâng Cấp Nhân Vật (Bio-Tech Augmentation Lab Gateway)
            // Nền xanh đen sinh học công nghệ cao với dải viền Neon Emerald
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clipRect(screenX, screenY, size, size);

            g2.setColor(new Color(20, 36, 38));
            g2.fillRect(screenX, screenY, size, size);

            // Viền phát quang xanh ngọc Neon
            g2.setColor(new Color(45, 230, 175, 220));
            g2.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);

            // Các vạch quét năng lượng sinh học kép
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(screenX + 5, screenY + 7, screenX + size - 5, screenY + 7);
            g2.drawLine(screenX + 5, screenY + size - 7, screenX + size - 5, screenY + size - 7);

            // Biểu tượng lục giác sinh học ở tâm
            int cx = screenX + size / 2;
            int cy = screenY + size / 2;
            g2.setColor(new Color(60, 255, 200));
            g2.drawOval(cx - 4, cy - 4, 8, 8);
            g2.setColor(new Color(120, 255, 230));
            g2.fillOval(cx - 2, cy - 2, 4, 4);

            g2.dispose();
        } else if (type == TileType.REFORGE_ZONE) {
            // Tấm lót cao su dã chiến bảo dưỡng súng (Gunsmith Maintenance Mat)
            g.setColor(new Color(36, 42, 38));
            g.fillRect(screenX, screenY, size, size);

            // Khung nẹp thép dập viền xanh rêu quân đội
            g.setColor(new Color(70, 85, 72));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);

            // Đinh tán kim loại ở 4 góc tấm lót
            g.setColor(new Color(150, 165, 155));
            g.fillRect(screenX + 4, screenY + 4, 2, 2);
            g.fillRect(screenX + size - 6, screenY + 4, 2, 2);
            g.fillRect(screenX + 4, screenY + size - 6, 2, 2);
            g.fillRect(screenX + size - 6, screenY + size - 6, 2, 2);

            // Vân lưới chống trượt dã chiến
            g.setColor(new Color(50, 60, 52));
            g.drawLine(screenX + 6, screenY + size / 2, screenX + size - 6, screenY + size / 2);
            g.drawLine(screenX + size / 2, screenY + 6, screenX + size / 2, screenY + size - 6);
        } else if (type == TileType.UPGRADE_ZONE) {
            // Sàn bệ công nghệ sinh học Bio-Tech: Màu xanh lục cyan sẫm với mạch vi mạch phát quang
            g.setColor(new Color(24, 38, 48));
            g.fillRect(screenX, screenY, size, size);

            // Viền kim loại titanium xanh
            g.setColor(new Color(50, 160, 190, 160));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);

            // Đường dẫn năng lượng vi mạch ở tâm
            g.setColor(new Color(60, 220, 200, 120));
            g.drawLine(screenX + 4, screenY + 4, screenX + size / 2, screenY + size / 2);
            g.drawLine(screenX + size - 5, screenY + size - 5, screenX + size / 2, screenY + size / 2);
        } else if (type == TileType.SPAWN_ZONE) {
            // Điểm tập kết dã chiến: Vạch định vị góc màu xanh rêu quân đội (Tactical Corner Brackets)
            g.setColor(new Color(55, 75, 60, 140));
            g.drawRect(screenX + 2, screenY + 2, size - 5, size - 5);
            g.setColor(new Color(120, 160, 130));
            // Góc trên-trái
            g.drawLine(screenX + 4, screenY + 4, screenX + 10, screenY + 4);
            g.drawLine(screenX + 4, screenY + 4, screenX + 4, screenY + 10);
            // Góc dưới-phải
            g.drawLine(screenX + size - 5, screenY + size - 5, screenX + size - 11, screenY + size - 5);
            g.drawLine(screenX + size - 5, screenY + size - 5, screenX + size - 5, screenY + size - 11);
        } else if (type == TileType.EXTRACT_ZONE) {
            // BÃI ĐÁP TRỰC THĂNG SƠ TÁN QUÂN SỰ (HELIPAD LZ)
            g.setColor(new Color(38, 42, 40));
            g.fillRect(screenX, screenY, size, size);

            int cx = screenX + size / 2;
            int cy = screenY + size / 2;

            // Vòng tròn sơn vàng quân sự viền quanh Helipad
            g.setColor(new Color(230, 190, 40, 200));
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(screenX + 4, screenY + 4, size - 8, size - 8);

            // Chữ [ H ] màu trắng phản quang nổi bật ở chính giữa bãi đáp
            g.setColor(new Color(245, 245, 240));
            g.setStroke(new BasicStroke(2.2f));
            // Cột đứng trái
            g.drawLine(cx - 5, cy - 7, cx - 5, cy + 7);
            // Thanh ngang
            g.drawLine(cx - 5, cy, cx + 5, cy);
            // Cột đứng phải
            g.drawLine(cx + 5, cy - 7, cx + 5, cy + 7);
            g.setStroke(new BasicStroke(1.0f));

            // Đèn tín hiệu dẫn đường màu hổ phách chớp nhẹ ở 4 góc bãi đáp
            float strobe = (float) (Math.sin(System.currentTimeMillis() * 0.006) * 0.3 + 0.7);
            g.setColor(new Color(255, 180, 30, (int) (220 * strobe)));
            g.fillOval(screenX + 2, screenY + 2, 3, 3);
            g.fillOval(screenX + size - 5, screenY + 2, 3, 3);
            g.fillOval(screenX + 2, screenY + size - 5, 3, 3);
            g.fillOval(screenX + size - 5, screenY + size - 5, 3, 3);
        }
    }
}
