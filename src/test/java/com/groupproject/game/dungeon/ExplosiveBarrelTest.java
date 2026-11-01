package com.groupproject.game.dungeon;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.entity.enemy.BossTank;
import com.groupproject.game.entity.enemy.MeleeEnemy;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho tính năng Thùng Phi Nổ (Explosive Barrel).
 *
 * <p>WHY: Đảm bảo tính toàn vẹn của thùng phi nổ:
 * - Thuộc tính vật lý, máu tối đa (20 HP), khả năng cản đạn và cản đường.
 * - Nhận sát thương từ vũ khí và kích hoạt bộc phá khi HP giảm về 0.
 * - Tác động sát thương diện rộng (AoE) lên cả người chơi lẫn kẻ địch (bao gồm cả Boss Tank).
 * - Kích hoạt phản ứng nổ dây chuyền (Chain Reaction) lên các thùng gỗ và thùng phi lân cận.
 * - Tỷ lệ sinh ngẫu nhiên an toàn trong Dungeon thường và Boss Arena.
 */
public class ExplosiveBarrelTest {

    @BeforeEach
    void setUp() {
        Tile.setExplosionListener(null);
    }

    @AfterEach
    void tearDown() {
        Tile.setExplosionListener(null);
    }

    @Test
    @DisplayName("Verify explosive barrel tile properties and initial stats")
    void testExplosiveBarrelTileProperties() {
        Tile tile = new Tile(5, 5, TileType.EXPLOSIVE_BARREL);

        assertTrue(tile.isSolid(), "Explosive barrel must be solid to block locomotion");
        assertTrue(tile.blocksProjectiles(), "Explosive barrel must block projectiles");
        assertTrue(tile.isDestructible(), "Explosive barrel must be destructible");
        assertTrue(tile.isExplosiveBarrel(), "isExplosiveBarrel() must return true");
        assertEquals(20, tile.getMaxHealth(), "Explosive barrel max health should be 20 HP");
        assertEquals(20, tile.getHealth(), "Explosive barrel starting health should be 20 HP");
        assertEquals(TileType.EXPLOSIVE_BARREL, tile.getType());

        assertEquals(5 * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2.0, tile.getCenterX());
        assertEquals(5 * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2.0, tile.getCenterY());
    }

    @Test
    @DisplayName("Verify taking damage reduces HP and triggers detonation callback when destroyed")
    void testExplosiveBarrelTakeDamageAndDetonate() {
        List<String> explosionLog = new ArrayList<>();
        Tile.setExplosionListener((col, row, cx, cy, radius, dmg) -> {
            explosionLog.add("EXPLODED at (" + col + "," + row + ") R=" + radius + " DMG=" + dmg);
        });

        Tile tile = new Tile(3, 4, TileType.EXPLOSIVE_BARREL);

        // Đòn bắn thứ nhất: 12 sát thương (chưa đủ phá hủy)
        boolean destroyedFirst = tile.takeDamage(12);
        assertFalse(destroyedFirst, "Barrel should survive 12 damage");
        assertEquals(8, tile.getHealth(), "Remaining HP should be 8");
        assertEquals(TileType.EXPLOSIVE_BARREL, tile.getType(), "Type should remain EXPLOSIVE_BARREL");
        assertTrue(explosionLog.isEmpty(), "No explosion should occur yet");

        // Đòn bắn thứ hai: 10 sát thương (phá hủy hoàn toàn và kích nổ)
        boolean destroyedSecond = tile.takeDamage(10);
        assertTrue(destroyedSecond, "Barrel should be destroyed by lethal damage");
        assertEquals(0, tile.getHealth());
        assertEquals(TileType.FLOOR, tile.getType(), "Destroyed barrel must revert to walkable FLOOR");
        assertEquals(1, explosionLog.size(), "Detonation callback must be triggered exactly once");
        assertTrue(explosionLog.get(0).contains("EXPLODED at (3,4)"));
    }

    @Test
    @DisplayName("Verify barrel explosion in PlayingState damages player, enemies, and Boss Tank")
    void testExplosionDamagesPlayerAndEnemies() {
        GameStateManager gsm = new GameStateManager();
        PlayingState playingState = new PlayingState(gsm);
        playingState.init();

        // Đặt người chơi gần tâm nổ
        playingState.getPlayer().reset(100.0, 100.0);
        int initialPlayerHP = playingState.getPlayer().getCurrentHealth();

        // Thêm 1 quái thường và 1 Boss Tank trong tầm nổ
        MeleeEnemy minion = new MeleeEnemy(120.0, 100.0, 50);
        int initialMinionHP = minion.getCurrentHealth();

        playingState.getCombatTargets().clear();
        playingState.getCombatTargets().add(minion);
        playingState.spawnBossForTesting(110.0, 120.0, 500, 72.0, "Boss Tank");
        TargetDummy boss = playingState.getBossDummy();
        int initialBossHP = boss.getCurrentHealth();

        // Kích nổ thùng phi tại tọa độ (105, 105), bán kính 110px, sát thương tâm 65
        playingState.onTileExploded(2, 2, 105.0, 105.0, 110.0, 65);

        // 1. Kiểm tra sát thương lên người chơi
        assertTrue(playingState.getPlayer().getCurrentHealth() < initialPlayerHP,
                "Player must take explosion damage when in blast radius");

        // 2. Kiểm tra sát thương lên quái thường
        assertTrue(minion.getCurrentHealth() < initialMinionHP,
                "Minion must take explosion damage when in blast radius");

        // 3. Kiểm tra sát thương lên Boss Tank và hiển thị thanh máu Boss
        assertTrue(boss.getCurrentHealth() < initialBossHP,
                "Boss Tank must take explosion damage when in blast radius");
        assertTrue(playingState.isBossHealthBarRevealed(),
                "Boss health bar must be revealed when Boss takes explosion damage");
    }

    @Test
    @DisplayName("Verify explosive barrel chain reaction detonates adjacent barrels")
    void testBarrelChainReaction() {
        GameStateManager gsm = new GameStateManager();
        PlayingState playingState = new PlayingState(gsm);
        playingState.init();

        DungeonMap map = playingState.getDungeonMap();
        // Đặt 2 thùng phi nổ liền kề nhau
        int col1 = 10;
        int row1 = 10;
        int col2 = 11;
        int row2 = 10;

        map.getTile(col1, row1).setType(TileType.EXPLOSIVE_BARREL);
        map.getTile(col2, row2).setType(TileType.EXPLOSIVE_BARREL);

        assertEquals(TileType.EXPLOSIVE_BARREL, map.getTile(col1, row1).getType());
        assertEquals(TileType.EXPLOSIVE_BARREL, map.getTile(col2, row2).getType());

        // Bắn phát nổ thùng thứ nhất
        map.getTile(col1, row1).takeDamage(999);

        // Thùng thứ nhất bị phá hủy và chuyển thành sàn
        assertEquals(TileType.FLOOR, map.getTile(col1, row1).getType(),
                "First barrel must detonate and turn into FLOOR");

        // Nhờ phản ứng nổ dây chuyền (chain reaction), thùng thứ hai cũng phải bị phá hủy và chuyển thành FLOOR
        assertEquals(TileType.FLOOR, map.getTile(col2, row2).getType(),
                "Second barrel must detonate in chain reaction and turn into FLOOR");
    }

    @Test
    @DisplayName("Verify procedural generation and boss stage instantiate explosive barrels")
    void testProceduralAndBossSpawnsExplosiveBarrels() {
        DungeonMap map = new DungeonMap();
        map.generateProceduralDungeon();

        int barrelCount = 0;
        for (int c = 0; c < DungeonMap.COLS; c++) {
            for (int r = 0; r < DungeonMap.ROWS; r++) {
                if (map.getTile(c, r).getType() == TileType.EXPLOSIVE_BARREL) {
                    barrelCount++;
                }
            }
        }
        assertTrue(barrelCount >= 3,
                "Procedural dungeon should spawn explosive barrels across combat rooms (found " + barrelCount + ")");

        // Kiểm tra Boss stage
        map.generateBossStage();
        int bossBarrelCount = 0;
        for (int c = 0; c < DungeonMap.COLS; c++) {
            for (int r = 0; r < DungeonMap.ROWS; r++) {
                if (map.getTile(c, r).getType() == TileType.EXPLOSIVE_BARREL) {
                    bossBarrelCount++;
                }
            }
        }
        assertTrue(bossBarrelCount >= 3,
                "Boss stage must spawn at least 3 explosive barrels (found " + bossBarrelCount + ")");
    }
}
