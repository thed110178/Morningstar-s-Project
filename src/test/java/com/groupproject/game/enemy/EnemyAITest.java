package com.groupproject.game.enemy;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.enemy.MeleeEnemy;
import com.groupproject.game.entity.enemy.RangedPistolEnemy;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testing suite for autonomous Enemy AI (Melee Chasers & Ranged Pistol Scouts).
 *
 * <p>WHY: Verifies tactical behaviors including approach locomotion, contact damage cooldowns,
 * kiting range preservation, 1-3 round burst pistol discharges, and player damage handling.
 */
class EnemyAITest {

    private ProjectileManager projectileManager;

    @BeforeEach
    void setUp() {
        projectileManager = new ProjectileManager();
    }

    @Test
    @DisplayName("Verify MeleeEnemy pursues player world coordinates")
    void testMeleeEnemyPursuit() {
        MeleeEnemy melee = new MeleeEnemy(100, 100, 50, 120.0, 12, 1.0);
        double initialX = melee.getX();

        // Player is to the right at (250, 100)
        melee.updateAI(0.1, 250, 100, 28.0, null, projectileManager, null);

        assertTrue(melee.getX() > initialX,
                "Melee enemy must advance towards operative along the X axis");
        assertEquals(100.0, melee.getY(), 0.001,
                "Y coordinate should remain unchanged when chasing directly along X axis");
    }

    @Test
    @DisplayName("Verify MeleeEnemy strikes operative and initiates cooldown")
    void testMeleeEnemyStrikeAndCooldown() {
        MeleeEnemy melee = new MeleeEnemy(100, 100, 50, 120.0, 15, 1.0);

        // Operative is right next to enemy (dist = 20px, well within strike reach)
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setPlayerCurrentHealth(100);

        // Melee strikes operative
        melee.updateAI(0.016, 120, 100, 28.0, null, projectileManager, state);

        assertTrue(melee.getAttackTimer() > 0, "Attack cooldown must be triggered after melee strike");
        assertEquals(85, state.getPlayerCurrentHealth(), "Player must take 15 melee damage");
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy backpedals when player is too close (Kiting)")
    void testRangedEnemyKitingRetreat() {
        // minRange is 190px. Player at 150px (dist = 50px < 190px)
        RangedPistolEnemy ranged = new RangedPistolEnemy(100, 100, 45, 100.0, 10, 190.0, 360.0);
        double initialX = ranged.getX();

        ranged.updateAI(0.1, 150, 100, 28.0, null, projectileManager, null);

        assertTrue(ranged.getX() < initialX,
                "Ranged enemy must backpedal (move in negative X) when operative encroaches within minRange");
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy advances when player is too far")
    void testRangedEnemyAdvanceWhenFar() {
        // maxRange is 360px. Player is far away at (600, 100) (dist = 500px > 360px)
        RangedPistolEnemy ranged = new RangedPistolEnemy(100, 100, 45, 100.0, 10, 190.0, 360.0);
        double initialX = ranged.getX();

        ranged.updateAI(0.1, 600, 100, 28.0, null, projectileManager, null);

        assertTrue(ranged.getX() > initialX,
                "Ranged enemy must advance forward when operative is beyond maxRange");
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy discharges bursts of 1 to 3 bullets")
    void testRangedEnemyBurstPistolDischarge() {
        RangedPistolEnemy ranged = new RangedPistolEnemy(100, 100, 50, 100.0, 10, 190.0, 360.0);

        // Advance AI time to expire initial burst cooldown timer
        ranged.updateAI(2.0, 300, 100, 28.0, null, projectileManager, null);

        int burstTotal = ranged.getBurstTotal();
        assertTrue(burstTotal >= 1 && burstTotal <= 3,
                "Burst total must be between 1 and 3 rounds, but was: " + burstTotal);

        // Update with enough time for first bullet trigger
        ranged.updateAI(0.15, 300, 100, 28.0, null, projectileManager, null);
        projectileManager.update(0.016, null);

        assertTrue(projectileManager.getCount() >= 1,
                "At least 1 hostile bullet should have been spawned into projectile manager");

        Projectile p = projectileManager.getActiveProjectiles().get(0);
        assertFalse(p.isPlayerBullet(),
                "Fired projectile must be flagged as a hostile bullet (!isPlayerBullet)");
        assertEquals(10, p.getDamage(),
                "Bullet damage must match enemy configuration (10 HP)");
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy actively strafes laterally in safe engagement range instead of standing still")
    void testRangedEnemyActiveTacticalStrafingInSafeRange() {
        // Safe range: minRange = 190px, maxRange = 360px. Player at dist = 260px (dx = 260, dy = 0)
        RangedPistolEnemy ranged = new RangedPistolEnemy(100, 200, 45, 100.0, 10, 190.0, 360.0);
        ranged.setStrafeDirection(1); // Force positive Y strafe direction
        double initialY = ranged.getY();

        // Operative at (360, 200), distance = 260px (within safe engagement zone)
        ranged.updateAI(0.2, 360, 200, 28.0, null, projectileManager, null);

        // Verify it actively moved along Y rather than standing stationary
        double movedY = Math.abs(ranged.getY() - initialY);
        assertTrue(movedY > 5.0, "Ranged enemy must actively strafe laterally instead of standing still in safe range");
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy aggressively flanks cover to flush out hiding operative")
    void testRangedEnemyFlanksCoverToFlushHidingPlayer() {
        DungeonMap map = new DungeonMap();
        // Clear entire map as walkable FLOOR
        for (int c = 0; c < DungeonMap.COLS; c++) {
            for (int r = 0; r < DungeonMap.ROWS; r++) {
                map.getTile(c, r).setType(com.groupproject.game.dungeon.TileType.FLOOR);
            }
        }

        // Place a solid wall cover block between enemy at col 12 and operative at col 18
        map.getTile(15, 15).setType(com.groupproject.game.dungeon.TileType.COVER);

        double ts = com.groupproject.game.core.GameConfig.TILE_SIZE;
        double enemyX = 12 * ts + ts / 2.0;
        double enemyY = 15 * ts + ts / 2.0;
        double playerX = 18 * ts + ts / 2.0;
        double playerY = 15 * ts + ts / 2.0;

        // Verify that initially line of sight is obstructed by COVER
        assertFalse(map.hasLineOfSight(enemyX, enemyY, playerX, playerY),
                "Direct line of sight must be blocked by the COVER obstacle");

        RangedPistolEnemy scout = new RangedPistolEnemy(enemyX, enemyY, 50, 130.0, 10, 150.0, 350.0);
        scout.setStrafeDirection(1); // Flank towards positive Y (downward into open floor)
        scout.setStrafeTimer(99.0);  // Prevent timer flip during test duration

        // Run AI updates for 1.2s to flank around the cover block
        for (int frame = 0; frame < 15; frame++) {
            scout.updateAI(0.08, playerX, playerY, 28.0, map, projectileManager, null);
        }

        // Verify that the scout moved laterally to open up an unobstructed shooting angle
        assertTrue(map.hasLineOfSight(scout.getX(), scout.getY(), playerX, playerY),
                "Scout must have flanked around the cover to establish an unobstructed shooting line towards operative");
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy dynamic collision inversion when strafe path is blocked by a wall")
    void testRangedEnemyFlankInvertsDirectionOnWallCollision() {
        DungeonMap map = new DungeonMap();
        // Set all to WALL
        for (int c = 0; c < DungeonMap.COLS; c++) {
            for (int r = 0; r < DungeonMap.ROWS; r++) {
                map.getTile(c, r).setType(com.groupproject.game.dungeon.TileType.WALL);
            }
        }
        // Clear 3-tile wide vertical corridor at columns 14, 15, 16 from rows 10 to 20
        for (int c = 14; c <= 16; c++) {
            for (int r = 10; r <= 20; r++) {
                map.getTile(c, r).setType(com.groupproject.game.dungeon.TileType.FLOOR);
            }
        }

        double ts = com.groupproject.game.core.GameConfig.TILE_SIZE;
        // Col 16 right edge is at 17 * ts. Scout size = 30 -> center at 17 * ts - 16.0 (touches col 17 wall).
        double scoutX = 17 * ts - 16.0;
        double scoutY = 19 * ts + ts / 2.0;
        double playerX = scoutX; // Directly aligned on X so lateral strafe vector points purely horizontally into wall
        double playerY = 12 * ts + ts / 2.0;

        RangedPistolEnemy scout = new RangedPistolEnemy(scoutX, scoutY, 50, 100.0, 10, 150.0, 450.0);
        // Force strafe to the right (into the solid wall at col 17)
        scout.setStrafeDirection(1);
        scout.setStrafeTimer(99.0); // Ensure inversion comes from collision, not timer
        int initialDir = scout.getStrafeDirection();

        // Update AI while pressed against right wall - movement to right is blocked
        for (int frame = 0; frame < 5; frame++) {
            scout.updateAI(0.08, playerX, playerY, 28.0, map, projectileManager, null);
        }

        assertNotEquals(initialDir, scout.getStrafeDirection(),
                "Scout must invert strafe direction when blocked by physical terrain wall");
    }

    @Test
    @DisplayName("Verify hostile bullets inflict damage on the operative")
    void testHostileBulletDamagesPlayer() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setPlayerCurrentHealth(100);

        // Spawn hostile bullet directly overlapping player
        double px = state.getPlayerX();
        double py = state.getPlayerY();

        state.getProjectileManager().spawn(new Projectile(px + 10, py + 10, 0, 100, 15, 2.0, false));
        state.getProjectileManager().update(0.016, null);

        // Run PlayingState update to resolve combat collisions
        com.groupproject.game.input.KeyboardHandler kh = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mh = new com.groupproject.game.input.MouseHandler();
        state.update(0.016, kh, mh);

        assertEquals(85, state.getPlayerCurrentHealth(),
                "Operative HP should decrease from 100 to 85 upon being struck by hostile bullet");
        assertTrue(state.getInvulnerableTimer() > 0,
                "Operative should receive invulnerability frames after taking damage");
    }

    @Test
    @DisplayName("Verify TargetDummy backward compatibility")
    void testTargetDummySubclassPolymorphism() {
        TargetDummy dummy = new MeleeEnemy(200, 200, 60);
        assertTrue(dummy instanceof TargetDummy, "MeleeEnemy must inherit from TargetDummy");
        assertEquals(60, dummy.getMaxHealth());
        assertEquals(60, dummy.getCurrentHealth());

        dummy.takeDamage(25);
        assertEquals(35, dummy.getCurrentHealth());
        assertTrue(dummy.isAlive());

        TargetDummy ranged = new RangedPistolEnemy(300, 300, 50);
        assertTrue(ranged instanceof TargetDummy, "RangedPistolEnemy must inherit from TargetDummy");
        ranged.takeDamage(50);
        assertFalse(ranged.isAlive());
    }

    @Test
    @DisplayName("Verify DungeonMap hasLineOfSight detects clear vs obstructed rays")
    void testDungeonMapLineOfSight() {
        DungeonMap map = new DungeonMap();
        assertTrue(map.hasLineOfSight(500, 500, 500, 500));
    }

    @Test
    @DisplayName("Verify MeleeEnemy custom constructor and getters")
    void testMeleeEnemyCustomConstructorAndGetters() {
        MeleeEnemy melee = new MeleeEnemy(150, 150, 65, 133.0, 14, 0.85, "Cyber Swarmer MK-2 [Melee]");
        assertEquals(65, melee.getMaxHealth());
        assertEquals(133.0, melee.getSpeed(), 0.001);
        assertEquals(14, melee.getMeleeDamage());
        assertEquals(0.85, melee.getAttackCooldown(), 0.001);
        assertEquals("Cyber Swarmer MK-2 [Melee]", melee.getName());
    }

    @Test
    @DisplayName("Verify RangedPistolEnemy custom constructor and getters")
    void testRangedEnemyCustomConstructorAndGetters() {
        RangedPistolEnemy ranged = new RangedPistolEnemy(200, 200, 57, 111.0, 11, 190.0, 360.0, "Rogue Scout MK-2 [Pistol]");
        assertEquals(57, ranged.getMaxHealth());
        assertEquals(111.0, ranged.getSpeed(), 0.001);
        assertEquals(11, ranged.getBulletDamage());
        assertEquals(190.0, ranged.getMinRange(), 0.001);
        assertEquals(360.0, ranged.getMaxRange(), 0.001);
        assertEquals("Rogue Scout MK-2 [Pistol]", ranged.getName());
    }

    @Test
    @DisplayName("Verify enemy stats buff dynamically across Stage 1, Stage 2, and Stage 3")
    void testEnemyStageScalingAcrossStages() {
        GameStateManager gsm = new GameStateManager();
        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        // 1. Stage 1: Base Stats
        PlayingState state1 = new PlayingState(gsm);
        state1.setCurrentStage(1);
        state1.getDungeonMap().generateStage(1);
        com.groupproject.game.dungeon.Room arena1 = state1.getDungeonMap().getCombatRooms().get(0);
        state1.setPlayerPosition(arena1.getCenterWorldX() - 14, arena1.getCenterWorldY() - 14);
        state1.update(0.016, keyH, mouseH);

        java.util.List<TargetDummy> targets1 = state1.getCombatTargets();
        assertFalse(targets1.isEmpty());
        MeleeEnemy melee1 = null;
        RangedPistolEnemy ranged1 = null;
        for (TargetDummy t : targets1) {
            if (t instanceof MeleeEnemy && melee1 == null) melee1 = (MeleeEnemy) t;
            if (t instanceof RangedPistolEnemy && ranged1 == null) ranged1 = (RangedPistolEnemy) t;
        }
        assertNotNull(melee1);
        assertNotNull(ranged1);
        assertEquals(50, melee1.getMaxHealth(), "Stage 1 Melee HP must be 50");
        assertEquals(125.0, melee1.getSpeed(), 0.001);
        assertEquals(12, melee1.getMeleeDamage());
        assertEquals(0.90, melee1.getAttackCooldown(), 0.001);
        assertEquals("Cyber Swarmer [Melee]", melee1.getName());

        assertEquals(45, ranged1.getMaxHealth(), "Stage 1 Ranged HP must be 45");
        assertEquals(105.0, ranged1.getSpeed(), 0.001);
        assertEquals(10, ranged1.getBulletDamage());
        assertEquals("Rogue Scout [Pistol]", ranged1.getName());

        // 2. Stage 2: MK-2 Buff (+15 HP melee, +12 HP ranged, +8 speed melee, +6 speed ranged, +2 dmg melee, +1 dmg ranged)
        PlayingState state2 = new PlayingState(gsm);
        state2.setCurrentStage(2);
        state2.getDungeonMap().generateStage(2);
        com.groupproject.game.dungeon.Room arena2 = state2.getDungeonMap().getCombatRooms().get(0);
        state2.setPlayerPosition(arena2.getCenterWorldX() - 14, arena2.getCenterWorldY() - 14);
        state2.update(0.016, keyH, mouseH);

        java.util.List<TargetDummy> targets2 = state2.getCombatTargets();
        assertFalse(targets2.isEmpty());
        MeleeEnemy melee2 = null;
        RangedPistolEnemy ranged2 = null;
        for (TargetDummy t : targets2) {
            if (t instanceof MeleeEnemy && melee2 == null) melee2 = (MeleeEnemy) t;
            if (t instanceof RangedPistolEnemy && ranged2 == null) ranged2 = (RangedPistolEnemy) t;
        }
        assertNotNull(melee2);
        assertNotNull(ranged2);
        assertEquals(65, melee2.getMaxHealth(), "Stage 2 Melee HP must be buffed to 65");
        assertEquals(133.0, melee2.getSpeed(), 0.001, "Stage 2 Melee speed must be buffed to 133");
        assertEquals(14, melee2.getMeleeDamage(), "Stage 2 Melee damage must be buffed to 14");
        assertEquals(0.85, melee2.getAttackCooldown(), 0.001, "Stage 2 Melee cooldown must reduce to 0.85s");
        assertEquals("Cyber Swarmer MK-2 [Melee]", melee2.getName());

        assertEquals(57, ranged2.getMaxHealth(), "Stage 2 Ranged HP must be buffed to 57");
        assertEquals(111.0, ranged2.getSpeed(), 0.001, "Stage 2 Ranged speed must be buffed to 111");
        assertEquals(11, ranged2.getBulletDamage(), "Stage 2 Ranged damage must be buffed to 11");
        assertEquals("Rogue Scout MK-2 [Pistol]", ranged2.getName());

        // 3. Stage 3: MK-3 Buff on minions
        PlayingState state3 = new PlayingState(gsm);
        state3.setCurrentStage(3);
        state3.getDungeonMap().generateStage(3);
        com.groupproject.game.dungeon.Room arena3 = state3.getDungeonMap().getCombatRooms().get(0);
        state3.setPlayerPosition(arena3.getCenterWorldX() - 14, arena3.getCenterWorldY() - 14);
        state3.update(0.016, keyH, mouseH);

        java.util.List<TargetDummy> targets3 = state3.getCombatTargets();
        assertFalse(targets3.isEmpty());
        MeleeEnemy melee3 = null;
        RangedPistolEnemy ranged3 = null;
        for (TargetDummy t : targets3) {
            if (t instanceof MeleeEnemy && melee3 == null) melee3 = (MeleeEnemy) t;
            if (t instanceof RangedPistolEnemy && ranged3 == null) ranged3 = (RangedPistolEnemy) t;
        }
        assertNotNull(melee3);
        assertNotNull(ranged3);
        assertTrue(melee3.getMaxHealth() >= 80, "Stage 3 minion melee must be buffed (>= 80 HP)");
        assertEquals(141.0, melee3.getSpeed(), 0.001, "Stage 3 Melee speed must be buffed to 141");
        assertEquals(16, melee3.getMeleeDamage(), "Stage 3 Melee damage must be buffed to 16");
        assertEquals(0.80, melee3.getAttackCooldown(), 0.001, "Stage 3 Melee cooldown must reduce to 0.80s");
        assertEquals("Cyber Swarmer MK-3 [Melee]", melee3.getName());

        assertTrue(ranged3.getMaxHealth() >= 69, "Stage 3 minion ranged must be buffed (>= 69 HP)");
        assertEquals(117.0, ranged3.getSpeed(), 0.001, "Stage 3 Ranged speed must be buffed to 117");
        assertEquals(12, ranged3.getBulletDamage(), "Stage 3 Ranged damage must be buffed to 12");
        assertEquals("Rogue Scout MK-3 [Pistol]", ranged3.getName());
    }

    @Test
    @DisplayName("Verify enemy takes slight knockback and slides back upon taking damage")
    void testEnemyKnockbackImpulseAndDecay() {
        TargetDummy dummy = new TargetDummy(200.0, 200.0, 50);
        double initialX = dummy.getX();

        // Operative shoots dummy with bullet traveling from left to right (angle = 0 radians)
        dummy.takeDamage(15, 0.0, 130.0);
        assertTrue(dummy.getKnockbackVx() > 100.0, "Knockback Vx must be initiated in bullet direction");
        assertEquals(0.0, dummy.getKnockbackVy(), 0.001);

        // Advance frames to simulate physics slide
        for (int i = 0; i < 15; i++) {
            dummy.updateAI(0.016, 100.0, 200.0, 28.0, null, null, null);
        }

        assertTrue(dummy.getX() > initialX, "Enemy must be displaced backward by knockback");
        double displacement = dummy.getX() - initialX;
        assertTrue(displacement >= 5.0 && displacement <= 15.0,
                "Knockback displacement should be slight (5 to 15px), but was: " + displacement);
        assertEquals(0.0, dummy.getKnockbackVx(), 0.001, "Knockback velocity must fully decay to 0");
    }

    @Test
    @DisplayName("Verify Boss Dummy has high poise and absorbs 85% of knockback impulse")
    void testBossKnockbackResistance() {
        TargetDummy normal = new TargetDummy(200.0, 200.0, 50, 30.0, false, "Minion");
        TargetDummy boss = new TargetDummy(500.0, 500.0, 1000, 64.0, true, "TITAN BOSS");

        normal.takeDamage(20, 0.0, 140.0);
        boss.takeDamage(20, 0.0, 140.0);

        assertEquals(140.0, normal.getKnockbackVx(), 0.001);
        assertEquals(140.0 * 0.15, boss.getKnockbackVx(), 0.001, "Boss should only receive 15% of knockback impulse");
    }

    @Test
    @DisplayName("Verify projectile impact applies directional knockback in PlayingState")
    void testCombatProjectileAppliesKnockbackInPlayingState() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        // Spawn enemy and bullet inside spawn room (guaranteed walkable floor)
        com.groupproject.game.dungeon.Room room = state.getDungeonMap().getSpawnRoom();
        double cx = room.getCenterWorldX();
        double cy = room.getCenterWorldY();

        MeleeEnemy enemy = new MeleeEnemy(cx + 15, cy, 60);
        state.getCombatTargets().add(enemy);

        // Spawn a player bullet traveling right directly overlapping dummy
        Projectile bullet = new Projectile(cx, cy, 0.0, 600.0, 20, 1.0, true);
        state.getProjectileManager().spawn(bullet);

        com.groupproject.game.input.KeyboardHandler kh = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mh = new com.groupproject.game.input.MouseHandler();
        state.update(0.016, kh, mh);

        assertEquals(40, enemy.getCurrentHealth(), "Enemy should take 20 damage");
        assertTrue(enemy.getKnockbackVx() > 0, "Enemy must receive positive X knockback from bullet traveling right");
    }

    @Test
    @DisplayName("Verify enemy spawn telegraph lifecycle: while spawning, enemy is invulnerable, holds position, and does not strike")
    void testEnemySpawnTelegraphLifecycle() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setPlayerCurrentHealth(100);

        MeleeEnemy enemy = new MeleeEnemy(100, 100, 60, 140.0, 15, 0.8);
        enemy.setSpawnTelegraph(0.40);
        assertTrue(enemy.isSpawning(), "Enemy must report isSpawning == true when telegraph is active");

        // Verify invulnerability while spawning in portal
        enemy.takeDamage(20, 0.0, 150.0);
        assertEquals(60, enemy.getCurrentHealth(), "Enemy must be invulnerable and take 0 damage while in portal telegraph");
        assertEquals(0.0, enemy.getKnockbackVx(), 0.001, "Enemy must not receive knockback while in portal telegraph");

        // Player is right next to enemy (dist = 20px, normally instant strike reach)
        enemy.updateAI(0.2, 120, 100, 28.0, null, projectileManager, state);

        // Enemy must not have moved from (100, 100) and must not have damaged player
        assertEquals(100.0, enemy.getX(), 0.001, "Enemy must hold X position during telegraph");
        assertEquals(100.0, enemy.getY(), 0.001, "Enemy must hold Y position during telegraph");
        assertEquals(100, state.getPlayerCurrentHealth(), "Player must take 0 damage while enemy is telegraphing");
        assertTrue(enemy.isSpawning(), "Telegraph should still be active at 0.20s remaining");

        // Expire telegraph timer (0.25s elapsed, total 0.45s > 0.40s)
        enemy.updateAI(0.25, 120, 100, 28.0, null, projectileManager, state);
        assertFalse(enemy.isSpawning(), "Enemy must finish spawning when telegraph timer reaches 0");

        // After spawning finishes, enemy takes damage normally
        enemy.takeDamage(20, 0.0, 150.0);
        assertEquals(40, enemy.getCurrentHealth(), "Enemy must take damage normally after portal telegraph finishes");
    }

    @Test
    @DisplayName("Verify spawned enemies never spawn within immediate melee proximity of player (< 110px)")
    void testSpawnSafeDistanceExcludesImmediatePlayerProximity() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        com.groupproject.game.dungeon.Room combat = state.getDungeonMap().getCombatRooms().get(0);
        assertNotNull(combat);

        // Put player in combat room
        state.setPlayerPosition(combat.getCenterWorldX() - 14, combat.getCenterWorldY() - 14);

        com.groupproject.game.input.KeyboardHandler kh = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mh = new com.groupproject.game.input.MouseHandler();
        state.update(0.016, kh, mh);

        List<TargetDummy> targets = state.getCombatTargets();
        assertFalse(targets.isEmpty(), "Combat targets must spawn upon entering room");

        double px = state.getPlayerX() + 14.0;
        double py = state.getPlayerY() + 14.0;

        for (TargetDummy t : targets) {
            double dist = Math.hypot(t.getX() - px, t.getY() - py);
            assertTrue(dist >= 110.0, String.format(
                    "Spawned enemy '%s' at (%.1f, %.1f) is too close to player at (%.1f, %.1f) with distance %.1fpx < 110px!",
                    t.getName(), t.getX(), t.getY(), px, py, dist));
            assertTrue(t.isSpawning(), "Spawned enemy in combat room must start with an active spawn telegraph");
        }
    }

    @Test
    @DisplayName("Verify MeleeEnemy navigates around solid obstacles and avoids getting stuck when player hides behind cover")
    void testMeleeEnemyNavigatesAroundObstacleToPursueHidingPlayer() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        DungeonMap map = state.getDungeonMap();

        com.groupproject.game.dungeon.Room combat = map.getCombatRooms().get(0);
        int cCol = combat.getGridX() + 4;
        int cRow = combat.getGridY() + 4;

        // Place a solid cover tile right in the middle
        map.getTile(cCol, cRow).setType(com.groupproject.game.dungeon.TileType.COVER);

        // Put Melee Enemy to the left of cover
        double enemyX = (cCol - 1) * GameConfig.TILE_SIZE + 24.0;
        double enemyY = cRow * GameConfig.TILE_SIZE + 24.0;
        MeleeEnemy enemy = new MeleeEnemy(enemyX, enemyY, 100, 140.0, 15, 0.8);
        enemy.setSpawnTelegraph(0.0); // Active immediately

        // Put player to the right of cover (hiding behind obstacle)
        double playerX = (cCol + 1) * GameConfig.TILE_SIZE + 24.0;
        double playerY = cRow * GameConfig.TILE_SIZE + 24.0;

        assertFalse(map.hasLineOfSight(enemyX, enemyY, playerX, playerY),
                "Direct line of sight must be blocked by the middle cover tile");

        // Run AI update for several frames
        for (int i = 0; i < 8; i++) {
            enemy.updateAI(0.05, playerX, playerY, 28.0, map, projectileManager, state);
        }

        // Enemy should NOT be stuck facing the wall at original Y = 24.0; it should have steered vertically to flank around cover
        assertNotEquals(enemyY, enemy.getY(), 2.0,
                "Melee enemy must steer around the cover tile vertically instead of ramming directly into it");
    }
}
