package com.groupproject.game.core;

import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.dungeon.TileType;
import com.groupproject.game.entity.enemy.MeleeEnemy;
import com.groupproject.game.entity.enemy.RangedPistolEnemy;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.weapon.CombatKnife;
import com.groupproject.game.entity.weapon.MeleeSlash;
import com.groupproject.game.entity.weapon.Pistol;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho hệ thống nạp và vẽ Sprite tập trung (GameSpriteManager).
 */
public class GameSpriteManagerTest {

    @Test
    @DisplayName("Verify that all Material sprites are properly loaded and cached")
    void testAllSpritesLoaded() {
        assertNotNull(GameSpriteManager.getPlayerGunSprite(), "PlayerGun sprite must be loaded");
        assertNotNull(GameSpriteManager.getPlayerMeleeSprite(), "PlayerMelee sprite must be loaded");
        assertNotNull(GameSpriteManager.getEnemyGunSprite(), "Enemy gun sprite must be loaded");
        assertNotNull(GameSpriteManager.getEnemyMeleeSprite(), "Enemy Melee sprite must be loaded");
        assertNotNull(GameSpriteManager.getEnemyMedicSprite(), "Enemy Medic sprite must be loaded");
        assertNotNull(GameSpriteManager.getEnemyGrenadierSprite(), "Enemy Grenadier sprite must be loaded");
        assertNotNull(GameSpriteManager.getCoverStoneSprite(), "Stone cover sprite must be loaded");
        assertNotNull(GameSpriteManager.getCoverBarrelSprite(), "Wooden barrel cover sprite must be loaded");
        assertNotNull(GameSpriteManager.getCoverRedBarrelSprite(), "Red explosive barrel cover sprite must be loaded");
        assertNotNull(GameSpriteManager.getTextureGreyBrick(), "Grey brick wall texture must be loaded");
        assertNotNull(GameSpriteManager.getTextureDirt(), "Dirt floor texture must be loaded");
        assertNotNull(GameSpriteManager.getTextureGrass(), "Grass texture must be loaded");
        assertNotNull(GameSpriteManager.getEffectSlash(), "Slash effect sprite must be loaded");
    }

    @Test
    @DisplayName("Verify pre-scaled textures and hardware compatible image caching")
    void testPreScaledTexturesAndCompatibleImage() {
        assertNotNull(GameSpriteManager.getPreScaledTextureDirt(), "Pre-scaled dirt texture must be loaded");
        assertEquals(49, GameSpriteManager.getPreScaledTextureDirt().getWidth());
        assertEquals(49, GameSpriteManager.getPreScaledTextureDirt().getHeight());

        assertNotNull(GameSpriteManager.getPreScaledTextureGrass(), "Pre-scaled grass texture must be loaded");
        assertEquals(49, GameSpriteManager.getPreScaledTextureGrass().getWidth());
        assertEquals(49, GameSpriteManager.getPreScaledTextureGrass().getHeight());

        assertNotNull(GameSpriteManager.getPreScaledTextureGreyBrick(), "Pre-scaled brick texture must be loaded");
        assertEquals(49, GameSpriteManager.getPreScaledTextureGreyBrick().getWidth());
        assertEquals(49, GameSpriteManager.getPreScaledTextureGreyBrick().getHeight());

        assertNotNull(GameSpriteManager.getPreScaledCoverStone(), "Pre-scaled stone cover must be loaded");
        assertEquals(48, GameSpriteManager.getPreScaledCoverStone().getWidth());
        assertEquals(48, GameSpriteManager.getPreScaledCoverStone().getHeight());

        assertNotNull(GameSpriteManager.getPreScaledCoverBarrel(), "Pre-scaled barrel cover must be loaded");
        assertEquals(48, GameSpriteManager.getPreScaledCoverBarrel().getWidth());
        assertEquals(48, GameSpriteManager.getPreScaledCoverBarrel().getHeight());

        assertNotNull(GameSpriteManager.getPreScaledCoverRedBarrel(), "Pre-scaled red barrel cover must be loaded");
        assertEquals(48, GameSpriteManager.getPreScaledCoverRedBarrel().getWidth());
        assertEquals(48, GameSpriteManager.getPreScaledCoverRedBarrel().getHeight());

        BufferedImage testImg = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImage compatible = GameSpriteManager.toCompatibleImage(testImg);
        assertNotNull(compatible);
    }

    @Test
    @DisplayName("Verify renderRotatedSprite executes safely without throwing exceptions")
    void testRenderRotatedSprite() {
        BufferedImage canvas = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();

        BufferedImage sprite = GameSpriteManager.getPlayerGunSprite();
        assertDoesNotThrow(() -> {
            GameSpriteManager.renderRotatedSprite(g2d, sprite, 50, 50, Math.PI / 4, 32, 32);
            GameSpriteManager.renderRotatedSprite(g2d, null, 50, 50, 0, 32, 32);
        });

        g2d.dispose();
    }

    @Test
    @DisplayName("Verify Player renders correctly with Gun and Melee weapon forms")
    void testPlayerRenderWithSprites() {
        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        Camera camera = new Camera(200, 200);

        Player player = new Player(100, 100);
        Pistol pistol = new Pistol();
        CombatKnife knife = new CombatKnife();

        assertDoesNotThrow(() -> {
            player.render(g2d, camera, pistol);
            player.render(g2d, camera, knife);
            player.render(g2d, camera, null);
        });

        g2d.dispose();
    }

    @Test
    @DisplayName("Verify all Enemies (Melee, Ranged, Medic, Grenadier) render with sprites safely")
    void testEnemiesRenderWithSprites() {
        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        Camera camera = new Camera(200, 200);

        MeleeEnemy melee = new MeleeEnemy(100, 100, 50);
        RangedPistolEnemy ranged = new RangedPistolEnemy(100, 100, 45);
        com.groupproject.game.entity.enemy.MedicEnemy medic = new com.groupproject.game.entity.enemy.MedicEnemy(100, 100, 60);
        com.groupproject.game.entity.enemy.GrenadierEnemy grenadier = new com.groupproject.game.entity.enemy.GrenadierEnemy(100, 100, 75);

        assertDoesNotThrow(() -> {
            melee.render(g2d, camera);
            ranged.render(g2d, camera);
            medic.renderAura(g2d, camera);
            medic.render(g2d, camera);
            grenadier.render(g2d, camera);
        });

        g2d.dispose();
    }

    @Test
    @DisplayName("Verify that all 30 grass pattern textures are properly loaded and pre-scaled to 49x49")
    void testGrassPatternTexturesLoaded() {
        for (int i = 1; i <= 30; i++) {
            assertNotNull(GameSpriteManager.getGrassPattern(i), "Grass pattern " + i + " must be loaded");
            BufferedImage preScaled = GameSpriteManager.getPreScaledGrassPattern(i);
            assertNotNull(preScaled, "Pre-scaled grass pattern " + i + " must be loaded");
            assertEquals(49, preScaled.getWidth(), "Pre-scaled width for pattern " + i + " must be 49");
            assertEquals(49, preScaled.getHeight(), "Pre-scaled height for pattern " + i + " must be 49");
        }
        assertNull(GameSpriteManager.getGrassPattern(0), "Grass pattern 0 is invalid and must return null");
        assertNull(GameSpriteManager.getGrassPattern(31), "Grass pattern 31 is out of range and must return null");
        assertNull(GameSpriteManager.getPreScaledGrassPattern(0), "Pre-scaled grass pattern 0 must return null");
        assertNull(GameSpriteManager.getPreScaledGrassPattern(31), "Pre-scaled grass pattern 31 must return null");
    }

    @Test
    @DisplayName("Verify Tiles render with textures and grass safely")
    void testTilesRenderWithTextures() {
        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();

        Tile wall = new Tile(0, 0, TileType.WALL);
        Tile cover = new Tile(1, 0, TileType.COVER);
        Tile barrel = new Tile(2, 0, TileType.DESTRUCTIBLE_COVER);
        Tile floor = new Tile(3, 0, TileType.FLOOR);
        Tile grassFloor = new Tile(4, 0, TileType.FLOOR);
        grassFloor.setHasGrass(true);
        assertTrue(grassFloor.hasGrass());

        assertDoesNotThrow(() -> {
            wall.render(g2d, 0, 0);
            cover.render(g2d, 48, 0);
            barrel.render(g2d, 96, 0);
            floor.render(g2d, 144, 0);
            grassFloor.render(g2d, 192, 0);

            // Verify rendering with all 30 grass transition patterns
            for (int i = 1; i <= 30; i++) {
                Tile patternTile = new Tile(i, 1, TileType.FLOOR);
                patternTile.setGrassPattern(i);
                assertEquals(i, patternTile.getGrassPattern());
                patternTile.render(g2d, (i % 5) * 48, (i / 5) * 48);
            }
        });

        g2d.dispose();
    }

    @Test
    @DisplayName("Verify MeleeSlash renders with Slash sprite safely")
    void testMeleeSlashRenderWithSprite() {
        BufferedImage canvas = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        Camera camera = new Camera(200, 200);

        MeleeSlash slash = new MeleeSlash(100, 100, Math.PI / 2, 28, 36, 40, 0.25, 600.0);

        assertDoesNotThrow(() -> {
            slash.render(g2d, camera);
        });

        g2d.dispose();
    }
}
