package com.groupproject.game.enemy;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Room;
import com.groupproject.game.entity.enemy.GrenadierEnemy;
import com.groupproject.game.entity.enemy.MedicEnemy;
import com.groupproject.game.entity.enemy.MeleeEnemy;
import com.groupproject.game.entity.enemy.RangedPistolEnemy;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.entity.projectile.EnemyGrenade;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test suite verifying the behavioral mechanics, support triage, zoning detonation,
 * and procedural stage distribution for Combat Field Medic and Heavy Grenadier.
 */
public class MedicAndGrenadierEnemyTest {

    private GameStateManager gsm;
    private PlayingState playingState;

    @BeforeEach
    void setUp() {
        gsm = new GameStateManager();
        playingState = new PlayingState(gsm);
        playingState.init();
    }

    // ==========================================
    // 1. MEDIC ENEMY TESTS
    // ==========================================

    @Test
    @DisplayName("Verify MedicEnemy inheritance, initial attributes, and support naming")
    void testMedicEnemyInitialProperties() {
        MedicEnemy medic = new MedicEnemy(200, 200, 40);
        assertTrue(medic instanceof TargetDummy, "MedicEnemy must inherit from TargetDummy");
        assertEquals(40, medic.getMaxHealth());
        assertEquals(40, medic.getCurrentHealth());
        assertTrue(medic.isAlive());
        assertFalse(medic.isBoss());
        assertEquals(115.0, medic.getSpeed(), 0.001);
        assertEquals(150.0, medic.getHealRadius(), 0.001);
        assertEquals(8, medic.getHealAmount());
        assertEquals(0.70, medic.getHealInterval(), 0.001);
        assertEquals(240.0, medic.getFleeDistance(), 0.001);
        assertTrue(medic.getName().contains("Medic"), "Name should indicate medical support role");
    }

    @Test
    @DisplayName("Verify MedicEnemy does not deal contact damage or shoot projectiles at the player")
    void testMedicDoesNotAttackPlayer() {
        Player player = playingState.getPlayer();
        player.setCurrentHealth(100);

        MedicEnemy medic = new MedicEnemy(player.getCenterX() + 10, player.getCenterY() + 10, 40);
        playingState.getCombatTargets().add(medic);

        // Update several frames with medic near player
        for (int i = 0; i < 10; i++) {
            medic.updateAI(0.05, player.getCenterX(), player.getCenterY(), player.getSize(),
                    playingState.getDungeonMap(), playingState.getProjectileManager(), playingState);
        }

        // Player health must remain untouched at 100 HP
        assertEquals(100, player.getCurrentHealth(), "Medic must not attack the player");
        assertEquals(0, playingState.getProjectileManager().getCount(), "Medic must not fire any projectiles");
    }

    @Test
    @DisplayName("Verify MedicEnemy restores health to wounded allies within its healing aura")
    void testMedicHealsWoundedAllyInAura() {
        playingState.getCombatTargets().clear();

        // 1. Create wounded raider ally (50 max HP, damaged down to 20 HP)
        MeleeEnemy woundedRaider = new MeleeEnemy(250, 250, 50);
        woundedRaider.takeDamage(30);
        assertEquals(20, woundedRaider.getCurrentHealth());
        playingState.getCombatTargets().add(woundedRaider);

        // 2. Position medic at 290, 250 (distance = 40px <= healRadius 135px)
        MedicEnemy medic = new MedicEnemy(290, 250, 40, 115.0, 135.0, 8, 0.50, 200.0, "Medic Test");
        playingState.getCombatTargets().add(medic);

        // Player is far away at 800, 800 (medic is safe to heal)
        double playerX = 800.0;
        double playerY = 800.0;

        // Update 0.60s (elapsed > 0.50s heal interval -> triggers 1 healing pulse of 8 HP)
        medic.updateAI(0.60, playerX, playerY, 28.0,
                null, playingState.getProjectileManager(), playingState);

        assertEquals(28, woundedRaider.getCurrentHealth(),
                "Wounded ally in aura must receive restorative heal (+8 HP from 20 -> 28)");
        assertTrue(woundedRaider.getHealFlashTimer() > 0, "Ally should flash emerald green outline upon being healed");
    }

    @Test
    @DisplayName("Verify TargetDummy heal method does not overheal beyond maxHealth")
    void testTargetDummyDoesNotOverheal() {
        TargetDummy dummy = new TargetDummy(100, 100, 50);
        dummy.takeDamage(10);
        assertEquals(40, dummy.getCurrentHealth());

        // Heal 25 HP: 40 + 25 = 65 -> capped at 50 maxHealth
        dummy.heal(25);
        assertEquals(50, dummy.getCurrentHealth(), "Current HP must not exceed maxHealth");

        // Further heals on full HP do not modify currentHealth
        dummy.heal(10);
        assertEquals(50, dummy.getCurrentHealth());
    }

    @Test
    @DisplayName("Verify MedicEnemy actively flees when player gets within fleeDistance")
    void testMedicFleesWhenPlayerIsClose() {
        // Player at (200, 200), Medic at (260, 200) -> initial distance = 60px (< fleeDistance 210px)
        double playerX = 200.0;
        double playerY = 200.0;
        MedicEnemy medic = new MedicEnemy(260, 200, 40, 120.0, 135.0, 6, 0.8, 210.0, "Fleeing Medic");

        double initialDistance = Math.hypot(medic.getX() - playerX, medic.getY() - playerY);
        assertEquals(60.0, initialDistance, 0.001);

        // Update 0.2 seconds of AI evasion
        medic.updateAI(0.20, playerX, playerY, 28.0,
                null, playingState.getProjectileManager(), playingState);

        double newDistance = Math.hypot(medic.getX() - playerX, medic.getY() - playerY);
        assertTrue(newDistance > initialDistance,
                "Medic must actively move away from nearby player (initial: " + initialDistance + ", new: " + newDistance + ")");
    }

    @Test
    @DisplayName("Verify MedicEnemy approaches wounded squadmate when player is at safe distance")
    void testMedicApproachesWoundedAllyWhenSafe() {
        playingState.getCombatTargets().clear();

        // Wounded ally at (300, 200)
        MeleeEnemy ally = new MeleeEnemy(300, 200, 60);
        ally.takeDamage(40);
        playingState.getCombatTargets().add(ally);

        // Medic at (100, 200) (distance to ally = 200px > healRadius/2)
        MedicEnemy medic = new MedicEnemy(100, 200, 40, 100.0, 135.0, 6, 0.8, 150.0, "Approaching Medic");
        playingState.getCombatTargets().add(medic);

        // Player is far away at (100, 800) (safe distance > fleeDistance)
        double playerX = 100.0;
        double playerY = 800.0;

        double initialDistToAlly = Math.hypot(medic.getX() - ally.getX(), medic.getY() - ally.getY());
        assertEquals(200.0, initialDistToAlly, 0.001);

        // Update 0.3 seconds of AI navigation toward ally
        medic.updateAI(0.30, playerX, playerY, 28.0,
                null, playingState.getProjectileManager(), playingState);

        double newDistToAlly = Math.hypot(medic.getX() - ally.getX(), medic.getY() - ally.getY());
        assertTrue(newDistToAlly < initialDistToAlly,
                "Medic must close distance toward wounded ally (initial: " + initialDistToAlly + ", new: " + newDistToAlly + ")");
    }


    @Test
    @DisplayName("Verify MedicEnemy self-heals by 4 HP on restorative pulse when wounded")
    void testMedicSelfHealingOnPulse() {
        playingState.getCombatTargets().clear();
        MedicEnemy medic = new MedicEnemy(300, 300, 80);
        medic.takeDamage(30);
        assertEquals(50, medic.getCurrentHealth(), "Medic should have 50 HP after damage");

        // Player is far away
        double playerX = 900.0;
        double playerY = 900.0;

        // Update 0.75s (elapsed > 0.70s heal interval -> triggers 1 pulse with +4 self heal)
        medic.updateAI(0.75, playerX, playerY, 28.0,
                playingState.getDungeonMap(), playingState.getProjectileManager(), playingState);

        assertEquals(54, medic.getCurrentHealth(), "Medic must self-heal +4 HP on restorative pulse");
    }

    @Test
    @DisplayName("Verify MedicEnemy seeks cover when no wounded allies and line of sight is exposed")
    void testMedicSeeksCoverWhenNoWoundedAllies() {
        playingState.getCombatTargets().clear();
        Room room = playingState.getDungeonMap().getSpawnRoom();
        assertNotNull(room, "Spawn room must exist");

        double playerX = room.getCenterWorldX();
        double playerY = room.getCenterWorldY();

        double startX = room.getCenterWorldX() + 35.0;
        double startY = room.getCenterWorldY() + 35.0;
        MedicEnemy medic = new MedicEnemy(startX, startY, 80);
        playingState.getCombatTargets().add(medic);

        // Update several ticks of AI to actively maneuver, seek cover, or kite instead of standing still
        for (int i = 0; i < 15; i++) {
            medic.updateAI(0.05, playerX, playerY, 28.0,
                    playingState.getDungeonMap(), playingState.getProjectileManager(), playingState);
        }

        double movedDist = Math.hypot(medic.getX() - startX, medic.getY() - startY);
        assertTrue(movedDist > 2.0, "Medic must actively maneuver instead of standing motionless (moved: " + movedDist + ")");
    }

    @Test
    @DisplayName("Verify Medic smart steering maneuvers along walls when fleeing from corner")
    void testMedicSmartSteeringAvoidsCornerTrapping() {
        Room room = playingState.getDungeonMap().getSpawnRoom();
        assertNotNull(room, "Spawn room must exist");

        // Đặt medic ở gần góc tường Tây Bắc của phòng Spawn
        double startX = (room.getGridX() + 1.5) * GameConfig.TILE_SIZE;
        double startY = (room.getGridY() + 1.5) * GameConfig.TILE_SIZE;
        MedicEnemy medic = new MedicEnemy(startX, startY, 80);

        // Player áp sát từ hướng Đông Nam (dồn medic vào góc Tây Bắc)
        double playerX = startX + 70.0;
        double playerY = startY + 70.0;

        // Cập nhật AI né chạy
        for (int i = 0; i < 15; i++) {
            medic.updateAI(0.05, playerX, playerY, 28.0,
                    playingState.getDungeonMap(), playingState.getProjectileManager(), playingState);
        }

        // Medic phải bẻ lái thoát hiểm theo các hướng thông thoáng thay vì đứng kẹt cứng tại tọa độ ban đầu
        double movedDist = Math.hypot(medic.getX() - startX, medic.getY() - startY);
        assertTrue(movedDist > 1.5, "Medic must utilize multi-ray steering to maneuver out of corner traps");
    }

    @Test
    @DisplayName("Verify Medic detects dead-end pocket and avoids selecting it as cover")
    void testMedicDeadEndAndPocketDetection() {
        Room room = playingState.getDungeonMap().getSpawnRoom();
        assertNotNull(room);
        DungeonMap map = playingState.getDungeonMap();

        // 1. Center of room has high openness score
        double centerOpenness = new MedicEnemy(room.getCenterWorldX(), room.getCenterWorldY(), 80).calculateOpennessScore(room.getCenterWorldX(), room.getCenterWorldY(), map);
        assertTrue(centerOpenness > 800.0, "Center of room should have high openness clearance: " + centerOpenness);

        // 2. Corner of room (1.2 tile from walls) is identified as a corner/pocket
        double cornerX = (room.getGridX() + 1.2) * GameConfig.TILE_SIZE;
        double cornerY = (room.getGridY() + 1.2) * GameConfig.TILE_SIZE;
        MedicEnemy cornerMedic = new MedicEnemy(cornerX, cornerY, 80);
        assertTrue(cornerMedic.isDeadEndOrPocket(cornerX, cornerY, map), "Corner of room should be detected as dead-end/pocket");

        // 3. Cover search from player at center should NEVER select corner pocket
        java.awt.geom.Point2D.Double coverSpot = cornerMedic.findBestCoverLocation(room.getCenterWorldX(), room.getCenterWorldY(), map);
        if (coverSpot != null) {
            assertFalse(cornerMedic.isDeadEndOrPocket(coverSpot.x, coverSpot.y, map), "Selected cover spot must NEVER be a dead-end pocket");
        }
    }

    @Test
    @DisplayName("Verify Medic executes emergency breakout when cornered by player in dead-end pocket")
    void testMedicEmergencyBreakoutFromCorner() {
        Room room = playingState.getDungeonMap().getSpawnRoom();
        assertNotNull(room);
        DungeonMap map = playingState.getDungeonMap();

        // Set Medic deep in top-right corner pocket
        double cornerX = (room.getGridX() + room.getWidthInTiles() - 1.5) * GameConfig.TILE_SIZE;
        double cornerY = (room.getGridY() + 1.5) * GameConfig.TILE_SIZE;
        MedicEnemy medic = new MedicEnemy(cornerX, cornerY, 80);
        playingState.getCombatTargets().add(medic);

        // Player blocks the corner entrance from below/left (south-west)
        double playerX = cornerX - 45.0;
        double playerY = cornerY + 65.0;

        // Run several AI updates
        for (int i = 0; i < 20; i++) {
            medic.updateAI(0.04, playerX, playerY, 28.0, map, playingState.getProjectileManager(), playingState);
        }

        // Medic must maneuver out of corner pocket towards open room space instead of staying trapped
        double movedDist = Math.hypot(medic.getX() - cornerX, medic.getY() - cornerY);
        assertTrue(movedDist > 1.5, "Medic must actively breakout and maneuver away from corner pocket (moved: " + movedDist + ")");
    }

    // ==========================================
    // 2. GRENADIER ENEMY & ENEMY GRENADE TESTS
    // ==========================================

    @Test
    @DisplayName("Verify GrenadierEnemy inheritance and initial demolitions attributes")
    void testGrenadierEnemyInitialProperties() {
        GrenadierEnemy grenadier = new GrenadierEnemy(200, 200, 55);
        assertTrue(grenadier instanceof TargetDummy, "GrenadierEnemy must inherit from TargetDummy");
        assertEquals(55, grenadier.getMaxHealth());
        assertEquals(55, grenadier.getCurrentHealth());
        assertTrue(grenadier.isAlive());
        assertFalse(grenadier.isBoss());
        assertEquals(95.0, grenadier.getSpeed(), 0.001);
        assertEquals(24, grenadier.getGrenadeDamage());
        assertEquals(78.0, grenadier.getBlastRadius(), 0.001);
        assertEquals(3.0, grenadier.getGrenadeCooldown(), 0.001);
        assertTrue(grenadier.getName().contains("Grenadier"), "Name should indicate grenadier demolitions role");
    }

    @Test
    @DisplayName("Verify GrenadierEnemy lobs EnemyGrenade toward player when cooldown expires")
    void testGrenadierLobsEnemyGrenadeTowardPlayer() {
        ProjectileManager pm = playingState.getProjectileManager();
        pm.clear();

        GrenadierEnemy grenadier = new GrenadierEnemy(200, 200, 55, 95.0, 24, 78.0, 3.0, 175.0, 400.0, "Grenadier");
        grenadier.setGrenadeTimer(0.0); // Cooldown ready immediately

        // Player operative at (350, 200) (distance = 150px within throwing range [90, 460])
        grenadier.updateAI(0.016, 350.0, 200.0, 28.0,
                null, pm, playingState);

        // Advance one tick of ProjectileManager to integrate staging buffer
        pm.update(0.016, null);

        assertEquals(1, pm.getCount(), "Grenadier must discharge 1 grenade into ProjectileManager");
        Projectile p = pm.getActiveProjectiles().get(0);
        assertTrue(p instanceof EnemyGrenade, "Discharged projectile must be instance of EnemyGrenade");
        EnemyGrenade grenade = (EnemyGrenade) p;
        assertEquals(2.0, grenade.getTotalFuse(), 0.001, "Grenade must arm a 2.0-second delay fuse");
        assertTrue(grenade.getFuseTimer() <= 2.0 && grenade.getFuseTimer() >= 1.95, "Fuse timer must be counting down from 2.0s");
        assertEquals(78.0, grenade.getBlastRadius(), 0.001);
        assertEquals(24, grenade.getDamage());
    }

    @Test
    @DisplayName("Verify EnemyGrenade 2.0-second fuse detonation and AoE explosive damage")
    void testEnemyGrenadeTwoSecondDetonationFuse() {
        Player player = playingState.getPlayer();
        player.setCurrentHealth(100);

        // Spawn grenade aimed right at player position (300, 300)
        player.setPosition(300 - player.getSize() / 2.0, 300 - player.getSize() / 2.0);
        EnemyGrenade grenade = new EnemyGrenade(100, 100, 300, 300, 25, 78.0);
        assertFalse(grenade.hasExploded());
        assertTrue(grenade.isActive());

        // 1. Advance 1.0 second: grenade has landed on ground, still counting down
        grenade.update(1.0);
        assertTrue(grenade.isActive(), "Grenade must still be ticking after 1.0s");
        assertFalse(grenade.hasExploded());
        assertTrue(grenade.isLanded());
        assertTrue(grenade.getFuseTimer() <= 1.01 && grenade.getFuseTimer() >= 0.99);

        // 2. Advance another 1.05s (total 2.05s > 2.0s fuse)
        grenade.update(1.05);
        assertFalse(grenade.isActive(), "Grenade must deactivate upon fuse expiration");

        // 3. Trigger detonation payload
        grenade.explode(playingState, playingState.getDungeonMap());
        assertTrue(grenade.hasExploded());

        // Player was inside the 78px blast radius -> must take explosive damage (100 -> 75 HP)
        assertEquals(75, player.getCurrentHealth(),
                "Player operative caught in 2.0s grenade blast must suffer explosive damage (100 -> 75 HP)");
    }

    // ==========================================
    // 3. STAGE DISTRIBUTION PROCEDURAL TESTS
    // ==========================================

    @Test
    @DisplayName("Verify Stage 1 rooms contain Melee and Ranged only (No Medic, No Grenadier)")
    void testStage1HasNoMedicOrGrenadier() {
        playingState.setCurrentStage(1);
        playingState.getDungeonMap().generateStage(1);

        Room arena = playingState.getDungeonMap().getCombatRooms().get(0);
        playingState.setPlayerPosition(arena.getCenterWorldX() - 14, arena.getCenterWorldY() - 14);
        playingState.update(0.016, new com.groupproject.game.input.KeyboardHandler(), new com.groupproject.game.input.MouseHandler());

        List<TargetDummy> targets = playingState.getCombatTargets();
        assertFalse(targets.isEmpty());

        long meleeCount = targets.stream().filter(t -> t instanceof MeleeEnemy).count();
        long rangedCount = targets.stream().filter(t -> t instanceof RangedPistolEnemy).count();
        long medicCount = targets.stream().filter(t -> t instanceof MedicEnemy).count();
        long grenadierCount = targets.stream().filter(t -> t instanceof GrenadierEnemy).count();

        assertTrue(meleeCount > 0, "Stage 1 must have melee enemies");
        assertTrue(rangedCount > 0, "Stage 1 must have ranged enemies");
        assertEquals(0, medicCount, "Stage 1 must NOT contain Medic (Medic starts from Stage 2)");
        assertEquals(0, grenadierCount, "Stage 1 must NOT contain Grenadier (Grenadier starts from Stage 3)");
    }

    @Test
    @DisplayName("Verify Stage 2 rooms spawn MedicEnemy (Kẻ địch 1 xuất hiện từ Stage 2, chưa có Grenadier)")
    void testStage2SpawnsMedicEnemy() {
        playingState.setCurrentStage(2);
        playingState.getDungeonMap().generateStage(2);

        Room arena = playingState.getDungeonMap().getCombatRooms().get(0);
        playingState.setPlayerPosition(arena.getCenterWorldX() - 14, arena.getCenterWorldY() - 14);
        playingState.update(0.016, new com.groupproject.game.input.KeyboardHandler(), new com.groupproject.game.input.MouseHandler());

        List<TargetDummy> targets = playingState.getCombatTargets();
        assertFalse(targets.isEmpty());

        long meleeCount = targets.stream().filter(t -> t instanceof MeleeEnemy).count();
        long rangedCount = targets.stream().filter(t -> t instanceof RangedPistolEnemy).count();
        long medicCount = targets.stream().filter(t -> t instanceof MedicEnemy).count();
        long grenadierCount = targets.stream().filter(t -> t instanceof GrenadierEnemy).count();

        assertTrue(meleeCount > 0, "Stage 2 must retain melee raiders");
        assertTrue(rangedCount > 0, "Stage 2 must retain ranged scouts");
        assertTrue(medicCount > 0, "Stage 2 MUST introduce MedicEnemy as requested!");
        assertEquals(0, grenadierCount, "Stage 2 must NOT contain Grenadier (Grenadier starts from Stage 3)");
    }

    @Test
    @DisplayName("Verify Stage 3 rooms spawn both MedicEnemy and GrenadierEnemy")
    void testStage3SpawnsBothMedicAndGrenadier() {
        playingState.setCurrentStage(3);
        playingState.getDungeonMap().generateStage(3);

        Room arena = playingState.getDungeonMap().getCombatRooms().get(0);
        playingState.setPlayerPosition(arena.getCenterWorldX() - 14, arena.getCenterWorldY() - 14);
        playingState.update(0.016, new com.groupproject.game.input.KeyboardHandler(), new com.groupproject.game.input.MouseHandler());

        List<TargetDummy> targets = playingState.getCombatTargets();
        assertFalse(targets.isEmpty());

        long meleeCount = targets.stream().filter(t -> t instanceof MeleeEnemy).count();
        long rangedCount = targets.stream().filter(t -> t instanceof RangedPistolEnemy).count();
        long medicCount = targets.stream().filter(t -> t instanceof MedicEnemy).count();
        long grenadierCount = targets.stream().filter(t -> t instanceof GrenadierEnemy).count();

        assertTrue(meleeCount > 0, "Stage 3 must retain melee raiders");
        assertTrue(rangedCount > 0, "Stage 3 must retain ranged scouts");
        assertTrue(medicCount > 0, "Stage 3 MUST spawn MedicEnemy!");
        assertTrue(grenadierCount > 0, "Stage 3 MUST spawn GrenadierEnemy as requested!");
    }

    @Test
    @DisplayName("Verify Boss Stage (Stage 4) integrates Medic and Grenadier into Boss minion retinue")
    void testBossStageIntegratesMedicAndGrenadier() {
        playingState.setCurrentStage(PlayingState.BOSS_STAGE);
        playingState.loadBossStageDirectly();
        playingState.skipBossIntro();

        List<TargetDummy> targets = playingState.getCombatTargets();
        assertNotNull(playingState.getBossDummy());

        long minionCount = targets.stream().filter(t -> !t.isBoss()).count();
        assertEquals(8, minionCount, "Boss arena must maintain 8 minions as designed");

        long medicCount = targets.stream().filter(t -> t instanceof MedicEnemy).count();
        long grenadierCount = targets.stream().filter(t -> t instanceof GrenadierEnemy).count();

        assertTrue(medicCount > 0, "Boss minion escort must include Medic support");
        assertTrue(grenadierCount > 0, "Boss minion escort must include Grenadier demolitions pressure");
    }

    @Test
    @DisplayName("Verify Graphics2D rendering of MedicEnemy, GrenadierEnemy, and EnemyGrenade executes without exceptions")
    void testGraphics2DRenderingExecution() {
        BufferedImage image = new BufferedImage(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        Camera camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        camera.update(200, 200);

        MedicEnemy medic = new MedicEnemy(200, 200, 40);
        GrenadierEnemy grenadier = new GrenadierEnemy(240, 200, 55);
        EnemyGrenade grenadeAirborne = new EnemyGrenade(100, 100, 250, 200, 24, 78.0);
        EnemyGrenade grenadeLanded = new EnemyGrenade(100, 100, 250, 200, 24, 78.0);
        grenadeLanded.update(0.60); // Land on floor

        // Render entities - must execute cleanly without NPE or exceptions
        medic.renderAura(g2d, camera);
        medic.render(g2d, camera);
        grenadier.render(g2d, camera);
        grenadeAirborne.render(g2d, camera);
        grenadeLanded.render(g2d, camera);

        g2d.dispose();
    }
}
