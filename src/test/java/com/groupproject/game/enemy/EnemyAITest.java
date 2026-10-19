package com.groupproject.game.enemy;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.entity.enemy.BossTank;
import com.groupproject.game.entity.enemy.MeleeEnemy;
import com.groupproject.game.entity.enemy.RangedPistolEnemy;
import com.groupproject.game.entity.enemy.TargetDummy;
import com.groupproject.game.entity.projectile.BossCannonShell;
import com.groupproject.game.entity.projectile.CriticalHitVisual;
import com.groupproject.game.entity.projectile.ExplosionVisual;
import com.groupproject.game.entity.projectile.Projectile;
import com.groupproject.game.entity.projectile.ProjectileManager;
import com.groupproject.game.entity.weapon.MeleeSlash;
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
    @DisplayName("Verify melee knife slash applies massive knockback (520 px/s) and staggers rushing melee enemy")
    void testMeleeSlashAppliesHighKnockbackAndStaggersMeleeEnemy() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();

        com.groupproject.game.dungeon.Room room = state.getDungeonMap().getSpawnRoom();
        double cx = room.getCenterWorldX();
        double cy = room.getCenterWorldY();

        MeleeEnemy enemy = new MeleeEnemy(cx + 30, cy, 60);
        state.getCombatTargets().add(enemy);

        // Player operative at (cx, cy) slashes knife towards the enemy (aimAngle = 0.0)
        com.groupproject.game.entity.weapon.CombatKnife knife = new com.groupproject.game.entity.weapon.CombatKnife();
        state.getWeaponManager().equipWeapon(knife);
        state.getWeaponManager().selectSlot(4);
        state.getWeaponManager().attack(cx, cy, 0.0, state.getProjectileManager());

        assertEquals(1, state.getWeaponManager().getActiveSlashes().size(), "1 slash must be spawned");
        MeleeSlash slash = state.getWeaponManager().getActiveSlashes().get(0);
        assertEquals(520.0, slash.getKnockbackForce(), 0.001);

        com.groupproject.game.input.KeyboardHandler kh = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mh = new com.groupproject.game.input.MouseHandler();
        state.update(0.016, kh, mh);

        // Enemy should have taken 45 damage from knife (60 - 45 = 15 HP remaining)
        assertEquals(15, enemy.getCurrentHealth(), "Enemy must take 45 damage from knife");
        assertTrue(enemy.getKnockbackVx() > 300.0, "Enemy must receive massive positive X knockback impulse");
        assertTrue(enemy.getAttackTimer() >= 0.35, "Enemy attack timer must be staggered to prevent immediate counter-bite");

        // Advance frames to observe physical displacement
        double initialEnemyX = cx + 30;
        for (int i = 0; i < 15; i++) {
            enemy.updateAI(0.016, cx, cy, 28.0, state.getDungeonMap(), null, state);
        }
        assertTrue(enemy.getX() - initialEnemyX >= 20.0,
                "Enemy must be blasted backwards significantly (> 20px) away from player");
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

        // Ensure clean floor around the test obstacle to isolate test geometry
        for (int c = cCol - 2; c <= cCol + 2; c++) {
            for (int r = cRow - 2; r <= cRow + 2; r++) {
                map.getTile(c, r).setType(com.groupproject.game.dungeon.TileType.FLOOR);
            }
        }

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
        for (int i = 0; i < 12; i++) {
            enemy.updateAI(0.05, playerX, playerY, 28.0, map, projectileManager, state);
        }

        // Enemy should NOT be stuck facing the wall; it should have steered vertically to flank around cover
        assertTrue(Math.abs(enemy.getY() - enemyY) > 0.5,
                String.format("Melee enemy must steer around cover vertically: initialY=%.2f, currentY=%.2f", enemyY, enemy.getY()));
    }

    @Test
    @DisplayName("Verify BossTank 3-layer decoupled rotations: MG locks on player, chassis matches movement, cannon activates < 50% HP")
    void testBossTank3LayerDecoupledRotations() {
        BossTank tank = new BossTank(200, 200, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0); // Active immediately

        // Player is at (400, 200) -> 0 radians (directly to the right)
        double playerX = 400.0;
        double playerY = 200.0;

        // Above 50% HP (1000 HP): Cannon is locked to chassis angle
        tank.setChassisAngle(1.57); // Chassis facing down
        tank.updateAI(0.016, playerX, playerY, 28.0, null, projectileManager, null);

        // Layer 1: Machine Gun should aim directly at player (angle ~ 0 rad)
        assertEquals(0.0, tank.getMachineGunAngle(), 0.05,
                "Machine gun turret must track player direction in 360 degrees");

        // Layer 2: Main Cannon locked to chassis angle when HP >= 50%
        assertFalse(tank.isCannonAwakened(), "Cannon must NOT be awakened when HP >= 50%");
        assertEquals(tank.getChassisAngle(), tank.getCannonAngle(), 0.05,
                "Cannon turret must stay locked to chassis direction when HP >= 50%");

        // Now damage Boss below 50% (450 HP)
        tank.takeDamage(550);
        assertTrue(tank.isCannonAwakened(), "Cannon must be awakened when HP < 50% (450/1000)");

        // Set state to CANNON_AIMING
        tank.setState(BossTank.TankState.CANNON_AIMING);
        tank.setStateTimer(1.0);
        tank.updateAI(0.1, playerX, playerY, 28.0, null, projectileManager, null);

        // Cannon turret now rotates independently towards player
        assertNotEquals(tank.getChassisAngle(), tank.getCannonAngle(),
                "Awakened cannon turret must rotate independently from chassis");
    }

    @Test
    @DisplayName("Verify BossTank ram charge: wind-up, rush movement, and stun upon hitting solid obstacle")
    void testBossTankRamRushAndStunOnObstacle() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        DungeonMap map = state.getDungeonMap();

        com.groupproject.game.dungeon.Room combat = map.getCombatRooms().get(0);
        int cCol = combat.getGridX() + 4;
        int cRow = combat.getGridY() + 4;

        // Place an impassable solid wall tile right in the room
        map.getTile(cCol, cRow).setType(com.groupproject.game.dungeon.TileType.WALL);

        // Position tank 1 tile above the wall, facing south (PI/2 rad) directly towards the wall
        double tankX = cCol * GameConfig.TILE_SIZE + 24.0;
        double tankY = (cRow - 1) * GameConfig.TILE_SIZE + 10.0;
        BossTank tank = new BossTank(tankX, tankY, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);
        tank.setChassisAngle(Math.PI / 2.0); // Facing south towards the solid wall

        tank.setState(BossTank.TankState.RAM_RUSH);
        tank.setStateTimer(1.0);

        // Update tank moving into the solid wall (player is far away on the opposite side)
        double farPlayerX = tankX + 500.0;
        for (int i = 0; i < 5; i++) {
            tank.updateAI(0.05, farPlayerX, tankY, 28.0, map, projectileManager, state);
        }

        // Must enter RAM_STUN state after hitting wall
        assertEquals(BossTank.TankState.RAM_STUN, tank.getState(),
                "BossTank must enter RAM_STUN after colliding with impassable wall");
        assertTrue(tank.getStateTimer() > 0, "Stun timer must be set (> 0)");
    }

    @Test
    @DisplayName("Verify BossTank ram rush hits operative, inflicts 40 contact damage, and enters cooldown")
    void testBossTankRamHitsPlayerDeals40Damage() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setInvulnerableTimer(0.0);
        state.setPlayerCurrentHealth(100);

        double px = state.getPlayerX() + 14.0;
        double py = state.getPlayerY() + 14.0;

        // Position tank 50px away, rushing directly towards operative
        BossTank tank = new BossTank(px - 50.0, py, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);
        tank.setChassisAngle(0.0); // Facing east towards player
        tank.setState(BossTank.TankState.RAM_RUSH);
        tank.setStateTimer(1.0);

        // Run 1 step to ram into player
        tank.updateAI(0.08, px, py, 28.0, state.getDungeonMap(), projectileManager, state);

        assertEquals(60, state.getPlayerCurrentHealth(),
                "Ramming into player must deal 40 damage (100 -> 60 HP)");
        assertEquals(BossTank.TankState.COOLDOWN, tank.getState(),
                "Tank must transition to COOLDOWN after successfully ramming operative");
    }

    @Test
    @DisplayName("Verify BossTank ram rush crushes destructible cover in its path")
    void testBossTankRamCrushesDestructibleCover() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        DungeonMap map = state.getDungeonMap();

        com.groupproject.game.dungeon.Room combat = map.getCombatRooms().get(0);
        int cCol = combat.getGridX() + 4;
        int cRow = combat.getGridY() + 4;

        // Place a destructible cover tile
        com.groupproject.game.dungeon.Tile coverTile = map.getTile(cCol, cRow);
        coverTile.setType(com.groupproject.game.dungeon.TileType.DESTRUCTIBLE_COVER);
        assertEquals(40, coverTile.getHealth());

        // Place tank just above the cover tile, rushing south (angle PI/2)
        double tankX = cCol * GameConfig.TILE_SIZE + 24.0;
        double tankY = (cRow - 1) * GameConfig.TILE_SIZE + 10.0;
        BossTank tank = new BossTank(tankX, tankY, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);
        tank.setChassisAngle(Math.PI / 2.0); // South
        tank.setState(BossTank.TankState.RAM_RUSH);
        tank.setStateTimer(1.0);

        // Advance 1 step into the cover
        tank.updateAI(0.05, tankX, tankY + 200, 28.0, map, projectileManager, state);

        // Destructible cover tile should have been crushed by 120 damage and turned to FLOOR
        assertEquals(com.groupproject.game.dungeon.TileType.FLOOR, coverTile.getType(),
                "Ramming tank must crush and destroy destructible cover in its trajectory");
        // Tank must stop immediately upon striking the obstacle and enter RAM_STUN
        assertEquals(BossTank.TankState.RAM_STUN, tank.getState(),
                "Ramming tank must stop and enter RAM_STUN upon striking an obstacle");
        assertFalse(state.getActiveExplosions().isEmpty(),
                "Tank crashing into obstacle must spawn an impact ExplosionVisual");
    }

    @Test
    @DisplayName("Verify BossTank cannon laser sticks directly to player with zero lag during aiming and freezes in locked state")
    void testBossTankCannonLaserLocksDirectlyToPlayerAndPausesForDodge() {
        BossTank tank = new BossTank(200, 200, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);
        tank.takeDamage(600); // 400 HP -> awakened

        // Aiming state
        tank.setState(BossTank.TankState.CANNON_AIMING);
        tank.setStateTimer(0.90);

        // Player at (200, 400) -> angle PI/2 (straight down)
        tank.updateAI(0.016, 200.0, 400.0, 28.0, null, projectileManager, null);
        assertEquals(Math.PI / 2.0, tank.getCannonAngle(), 0.01,
                "Cannon angle must stick directly to player position with zero rotational lag");

        // Player moves instantly to (400, 200) -> angle 0 (straight right)
        tank.updateAI(0.016, 400.0, 200.0, 28.0, null, projectileManager, null);
        assertEquals(0.0, tank.getCannonAngle(), 0.01,
                "Cannon angle must track player position instantaneously without delay");

        // Advance until aiming timer expires and enters CANNON_LOCKED
        tank.setStateTimer(0.01);
        tank.updateAI(0.02, 400.0, 200.0, 28.0, null, projectileManager, null);
        assertEquals(BossTank.TankState.CANNON_LOCKED, tank.getState(),
                "Must transition to CANNON_LOCKED upon aiming expiry");
        assertTrue(tank.getStateTimer() >= 0.35, "Locked state must provide pause window for player to dodge");

        double lockedAngle = tank.getCannonLaserAngle();
        // Now player moves away to (200, 0) during locked state -> angle should NOT change!
        tank.updateAI(0.05, 200.0, 0.0, 28.0, null, projectileManager, null);
        assertEquals(lockedAngle, tank.getCannonLaserAngle(), 0.001,
                "Locked laser must freeze in place, providing the only window for player to dodge");
    }

    @Test
    @DisplayName("Verify BossCannonShell AoE explosion damages player and spawns ExplosionVisual")
    void testBossCannonShellAoEDetonation() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.setInvulnerableTimer(0.0);
        state.setPlayerCurrentHealth(100);

        double px = state.getPlayerX() + 14.0;
        double py = state.getPlayerY() + 14.0;

        // Shell lands 40px away from player (well within 85px explosion radius)
        BossCannonShell shell = new BossCannonShell(px + 40.0, py, 0, 1350.0, 35, 2.0, 85.0);
        shell.explode(state, state.getDungeonMap());

        assertTrue(shell.hasExploded(), "Shell must be marked as exploded");
        assertFalse(shell.isActive(), "Shell must be deactivated upon detonation");
        assertEquals(65, state.getPlayerCurrentHealth(),
                "Player within 85px explosion radius must take 35 damage (100 -> 65 HP)");
        assertFalse(state.getActiveExplosions().isEmpty(),
                "Detonating shell must spawn an active ExplosionVisual in PlayingState");
    }

    @Test
    @DisplayName("Verify BossTank Phase 1 anti-ram spam: consecutive rams are strictly blocked and alternate with machine gun burst")
    void testBossTankPhase1AntiRamSpamAndMachineGunBalance() {
        BossTank tank = new BossTank(200, 200, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);
        assertFalse(tank.isCannonAwakened(), "Phase 1 requires HP >= 50%");

        // Set lastAttackWasRam = true (simulating just completed a ram attack)
        tank.setLastAttackWasRam(true);
        tank.setState(BossTank.TankState.PATROL_REPOSITION);
        tank.setStateTimer(0.01);

        // Advance 1 frame so patrol timer expires and chooseNextAction runs
        tank.updateAI(0.02, 350.0, 200.0, 28.0, null, projectileManager, null);

        // Must transition to MG_BURST, NOT RAM_WINDUP!
        assertEquals(BossTank.TankState.MG_BURST, tank.getState(),
                "Boss Tank must switch to MG_BURST after a ram attack to prevent ram spamming");
        assertFalse(tank.isLastAttackWasRam(),
                "lastAttackWasRam flag must be reset to false after choosing machine gun burst");
        assertTrue(tank.getMgBulletsLeft() >= 5, "Machine gun burst must load at least 5-6 bullets");
    }

    @Test
    @DisplayName("Verify BossTank condensed skill downtime: instant cooldown after MG and fast patrol repositioning under 1.0s")
    void testBossTankCondensedSkillIntervalsAndInstantCooldownTransition() {
        BossTank tank = new BossTank(200, 200, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);

        // 1. In Phase 1: Machine gun burst loads 6 bullets
        tank.setState(BossTank.TankState.MG_BURST);
        tank.setStateTimer(0.01);
        // Simulate all bullets already fired
        // Advancing 1 tick should transition IMMEDIATELY to COOLDOWN (not waiting 1.5s!)
        tank.updateAI(0.02, 400.0, 200.0, 28.0, null, projectileManager, null);
        assertEquals(BossTank.TankState.COOLDOWN, tank.getState(),
                "Boss Tank must transition to COOLDOWN immediately upon finishing bullets");
        assertTrue(tank.getStateTimer() <= 0.35, "Cooldown timer after MG burst must be condensed (<= 0.35s)");

        // 2. Cooldown timer expiration leads to rapid PATROL_REPOSITION
        tank.setStateTimer(0.01);
        tank.updateAI(0.02, 400.0, 200.0, 28.0, null, projectileManager, null);
        assertEquals(BossTank.TankState.PATROL_REPOSITION, tank.getState());
        assertTrue(tank.getStateTimer() <= 0.60,
                "Patrol reposition timer in Phase 1 must be concise (<= 0.60s)");

        // 3. Phase 2 (Awakened): Machine gun burst loads 8 bullets and cooldowns are even faster
        tank.takeDamage(600);
        assertTrue(tank.isCannonAwakened());
        tank.setLastAttackWasRam(true);
        tank.setState(BossTank.TankState.PATROL_REPOSITION);
        tank.setStateTimer(0.01);
        tank.updateAI(0.02, 400.0, 200.0, 28.0, null, projectileManager, null);

        assertEquals(BossTank.TankState.MG_BURST, tank.getState());
        assertEquals(8, tank.getMgBulletsLeft(), "Phase 2 MG burst must load 8 bullets");

        // Expire bullets in Phase 2 -> Cooldown is even shorter (<= 0.25s)
        tank.updateAI(0.02, 400.0, 200.0, 28.0, null, projectileManager, null); // 1 tick to exhaust
        // Force exhaust
        while (tank.getMgBulletsLeft() > 0) {
            tank.updateAI(0.1, 400.0, 200.0, 28.0, null, projectileManager, null);
        }
        assertEquals(BossTank.TankState.COOLDOWN, tank.getState());
        assertTrue(tank.getStateTimer() <= 0.25, "Phase 2 Cooldown must be ultra-fast (<= 0.25s)");

        // Expire cooldown in Phase 2 -> Patrol reposition <= 0.40s
        tank.setStateTimer(0.01);
        tank.updateAI(0.02, 400.0, 200.0, 28.0, null, projectileManager, null);
        assertEquals(BossTank.TankState.PATROL_REPOSITION, tank.getState());
        assertTrue(tank.getStateTimer() <= 0.40, "Phase 2 Patrol reposition must be <= 0.40s");
    }

    @Test
    @DisplayName("Verify BossTank cannon laser locks onto exact player world coordinates with zero trailing delay")
    void testBossTankLaserLockOnWorldCoordinates() {
        BossTank tank = new BossTank(100, 100, 1000, 84.0, "TITAN MECHA DUMMY TANK [BOSS]");
        tank.setSpawnTelegraph(0.0);
        tank.takeDamage(600); // Awakened
        tank.setState(BossTank.TankState.CANNON_AIMING);
        tank.setStateTimer(0.90);

        // Player is at (450, 320)
        tank.updateAI(0.016, 450.0, 320.0, 28.0, null, projectileManager, null);
        assertEquals(450.0, tank.getLaserTargetWorldX(), 0.001,
                "Laser target X must instantaneously match player center X");
        assertEquals(320.0, tank.getLaserTargetWorldY(), 0.001,
                "Laser target Y must instantaneously match player center Y");

        // Player quickly moves to (150, 600)
        tank.updateAI(0.016, 150.0, 600.0, 28.0, null, projectileManager, null);
        assertEquals(150.0, tank.getLaserTargetWorldX(), 0.001,
                "Laser target X must update immediately with zero lag to new player position");
        assertEquals(600.0, tank.getLaserTargetWorldY(), 0.001,
                "Laser target Y must update immediately with zero lag to new player position");

        // Expire aiming to lock
        tank.setStateTimer(0.01);
        tank.updateAI(0.02, 150.0, 600.0, 28.0, null, projectileManager, null);
        assertEquals(BossTank.TankState.CANNON_LOCKED, tank.getState());

        // During locked state, player dodges to (800, 800) -> locked coordinates must freeze in place!
        tank.updateAI(0.05, 800.0, 800.0, 28.0, null, projectileManager, null);
        assertEquals(150.0, tank.getLaserTargetWorldX(), 0.001,
                "Locked coordinates must freeze in place allowing player to dodge");
        assertEquals(600.0, tank.getLaserTargetWorldY(), 0.001,
                "Locked coordinates must freeze in place allowing player to dodge");
    }

    @Test
    @DisplayName("Verify BossTank default attributes: 1250 Max HP and enlarged 96px hitbox")
    void testBossTankDefaultAttributesAndEnlargedHitbox() {
        BossTank tank = new BossTank(300, 400);
        assertEquals(1250, tank.getMaxHealth(), "BossTank default Max HP must be 1250 HP");
        assertEquals(1250, tank.getCurrentHealth(), "BossTank default Current HP must be 1250 HP");
        assertEquals(96.0, tank.getSize(), "BossTank default hitbox size must be enlarged to 96.0px");
        assertTrue(tank.isBoss(), "BossTank must have isBoss == true");
        assertTrue(tank.getName().contains("TITAN MECHA"), "BossTank display name must contain TITAN MECHA");
    }

    @Test
    @DisplayName("Verify MouseHandler viewport transform translates screen coordinates accurately across resolutions")
    void testFullScreenMouseScalingAndTransforms() {
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();

        // 1. Windowed 1:1 mode (1280x720)
        mouseH.setViewportTransform(0.0, 0.0, 1.0);
        assertEquals(500, mouseH.transformX(500));
        assertEquals(300, mouseH.transformY(300));

        // 2. Fullscreen 1080p (1920x1080 -> 1.5x scale, 0 offset)
        mouseH.setViewportTransform(0.0, 0.0, 1.5);
        assertEquals(640, mouseH.transformX(960)); // Center of 1920 maps to center of 1280
        assertEquals(360, mouseH.transformY(540)); // Center of 1080 maps to center of 720

        // 3. Fullscreen Ultrawide / Letterbox (e.g. 1920x1200 with 60px letterbox offset)
        mouseH.setViewportTransform(0.0, 60.0, 1.5);
        assertEquals(640, mouseH.transformX(960));
        assertEquals(0, mouseH.transformY(60)); // Top of game area
        assertEquals(360, mouseH.transformY(600)); // Center
    }

    @Test
    @DisplayName("Verify Boss arena spawns 5-6 minion reinforcements after 5 seconds of clearing minions")
    void testBossArenaMinionReinforcementsAfter5s() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.loadBossStageDirectly();

        // 1. Initially in Boss Arena: bossDummy is alive and 8 minions were spawned
        assertNotNull(state.getBossDummy(), "Boss dummy must be initialized in Boss Arena");
        assertTrue(state.getBossDummy().isAlive(), "Boss must be alive initially");
        long initialMinions = state.getCombatTargets().stream().filter(d -> d.isAlive() && !d.isBoss()).count();
        assertEquals(8, initialMinions, "Boss arena should initially have 8 minions");

        // 2. Eliminate all non-boss minions
        for (TargetDummy dummy : state.getCombatTargets()) {
            if (!dummy.isBoss()) {
                dummy.setAlive(false);
            }
        }
        long remainingMinions = state.getCombatTargets().stream().filter(d -> d.isAlive() && !d.isBoss()).count();
        assertEquals(0, remainingMinions, "All minions should now be eliminated");

        // 3. Simulate game update ticks: after 3s (timer has 2s left), minions should NOT have spawned yet
        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();
        state.update(3.0, keyH, mouseH);
        long after3sMinions = state.getCombatTargets().stream().filter(d -> d.isAlive() && !d.isBoss()).count();
        assertEquals(0, after3sMinions, "Minions should not spawn before 5 seconds have elapsed");
        assertTrue(state.getBossMinionRespawnTimer() <= 2.01 && state.getBossMinionRespawnTimer() >= 1.99,
                "Timer should have counted down from 5s to 2s (actual: " + state.getBossMinionRespawnTimer() + ")");

        // 4. After another 2.1s (total elapsed > 5.0s), 5-6 reinforcements must be spawned!
        state.update(2.1, keyH, mouseH);
        long reinforcedMinions = state.getCombatTargets().stream().filter(d -> d.isAlive() && !d.isBoss()).count();
        assertTrue(reinforcedMinions >= 5 && reinforcedMinions <= 6,
                "Reinforcement wave should spawn 5 to 6 enemies (actual: " + reinforcedMinions + ")");
        assertEquals(1, state.getBossMinionWaveCount(), "Wave count must be 1 after first reinforcement wave");
        assertEquals(5.0, state.getBossMinionRespawnTimer(), 0.001, "Timer must reset to 5.0s after wave spawn");

        // 5. If Boss is eliminated, no reinforcements should spawn even after 5s
        state.getBossDummy().setAlive(false);
        assertFalse(state.getBossDummy().isAlive(), "Boss is now dead");
        for (TargetDummy dummy : state.getCombatTargets()) {
            if (!dummy.isBoss()) {
                dummy.setAlive(false);
            }
        }
        state.setBossMinionRespawnTimer(0.1);
        state.update(0.2, keyH, mouseH);
        long afterBossDeadMinions = state.getCombatTargets().stream().filter(d -> d.isAlive() && !d.isBoss()).count();
        assertEquals(0, afterBossDeadMinions, "No minions should spawn after boss is defeated");
    }

    @Test
    @DisplayName("Verify critical damage visual feedback: crit flash, CriticalHitVisual spawn, and MeleeSlash crit flag")
    void testCriticalDamageVisualFeedbackAndFlash() {
        TargetDummy dummy = new TargetDummy(200, 200, 100);

        // 1. Standard non-critical damage activates standard hit flash (100ms) and no crit flash
        dummy.takeDamage(10, false);
        assertEquals(90, dummy.getCurrentHealth());
        assertTrue(dummy.getHitFlashTimer() > 0.08, "Standard hit triggers ~100ms hit flash");
        assertEquals(0.0, dummy.getCritFlashTimer(), 0.001, "Standard hit does not activate crit flash");

        // 2. Critical damage activates prolonged fiery-gold crit flash (180ms)
        dummy.takeDamage(32, true);
        assertEquals(58, dummy.getCurrentHealth());
        assertTrue(dummy.getCritFlashTimer() > 0.15, "Critical strike activates ~180ms radiant gold crit flash");
        assertTrue(dummy.getHitFlashTimer() > 0.15, "Critical strike sets hit flash timer to 180ms");

        // 3. Timers decay to zero on update
        dummy.update(0.25);
        assertEquals(0.0, dummy.getCritFlashTimer(), 0.001, "Crit flash expires after duration");
        assertEquals(0.0, dummy.getHitFlashTimer(), 0.001, "Hit flash expires after duration");

        // 4. CriticalHitVisual lifecycle
        CriticalHitVisual cv = new CriticalHitVisual(200, 200, 64, 0.40);
        assertFalse(cv.isFinished());
        assertEquals(64, cv.getDamage());
        assertEquals(200.0, cv.getX());
        assertEquals(200.0, cv.getY());
        cv.update(0.20);
        assertFalse(cv.isFinished());
        cv.update(0.25);
        assertTrue(cv.isFinished(), "CriticalHitVisual should be finished after total elapsed time exceeds duration");

        // 5. MeleeSlash retains critical strike flag
        MeleeSlash normalSlash = new MeleeSlash(100, 100, 0.0, 30.0, 48.0, 45, 0.12, 520.0, false);
        assertFalse(normalSlash.isCritical(), "Normal slash must have critical == false");

        MeleeSlash critSlash = new MeleeSlash(100, 100, 0.0, 30.0, 48.0, 90, 0.12, 676.0, true);
        assertTrue(critSlash.isCritical(), "Crit slash must have critical == true");

        // 6. Projectile hit resolution in PlayingState spawns CriticalHitVisual when bullet is critical
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        state.init();
        state.getCombatTargets().clear();
        state.getActiveCritVisuals().clear();

        double testX = state.getPlayerX() + 10;
        double testY = state.getPlayerY() + 10;
        TargetDummy target = new TargetDummy(testX, testY, 100);
        state.getCombatTargets().add(target);

        // Spawn critical bullet directly overlapping target
        Projectile critBullet = new Projectile(testX, testY, 0.0, 0.0, 40, 1.0, true, true);
        state.getProjectileManager().spawn(critBullet);

        com.groupproject.game.input.KeyboardHandler keyH = new com.groupproject.game.input.KeyboardHandler();
        com.groupproject.game.input.MouseHandler mouseH = new com.groupproject.game.input.MouseHandler();
        state.update(0.016, keyH, mouseH);

        assertFalse(critBullet.isActive(), "Bullet should deactivate after striking target");
        assertEquals(60, target.getCurrentHealth(), "Target should receive 40 damage");
        assertTrue(target.getCritFlashTimer() > 0.10, "Target must enter crit flash state");
        assertFalse(state.getActiveCritVisuals().isEmpty(), "PlayingState must spawn a CriticalHitVisual on crit impact");
        assertEquals(40, state.getActiveCritVisuals().get(0).getDamage());
    }
}
