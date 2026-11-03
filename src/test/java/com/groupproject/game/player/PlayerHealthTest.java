package com.groupproject.game.player;

import com.groupproject.game.entity.item.HealthPickup;
import com.groupproject.game.entity.shop.ShopItem;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying Player Health (HP), Damage Resolution, Invulnerability Frames (i-frames),
 * Health Pickups [F] Interaction, and Tactical Shop Medkit Station.
 */
public class PlayerHealthTest {

    private GameStateManager gsm;
    private PlayingState state;

    @BeforeEach
    void setUp() {
        gsm = new GameStateManager();
        state = new PlayingState(gsm);
        state.init();
    }

    @Test
    @DisplayName("Verify operative starts run with 100/100 HP and alive state")
    void testInitialPlayerHealth() {
        assertEquals(100, state.getPlayerMaxHealth());
        assertEquals(100, state.getPlayerCurrentHealth());
        assertFalse(state.isPlayerDead());
        assertEquals(0.0, state.getInvulnerableTimer(), 0.001);
    }

    @Test
    @DisplayName("Verify takeDamage reduces HP and activates i-frames to block consecutive hits")
    void testDamagePlayerAndInvulnerabilityFrames() {
        // 1. Nhận đòn 35 sát thương lần đầu
        state.damagePlayer(35);
        assertEquals(65, state.getPlayerCurrentHealth());
        assertTrue(state.getInvulnerableTimer() > 0.5, "i-frames should be active (~0.8s)");

        // 2. Nhận đòn tiếp theo khi đang bất tử -> Không bị trừ máu
        state.damagePlayer(30);
        assertEquals(65, state.getPlayerCurrentHealth(), "Damage during i-frames must be absorbed");

        // 3. Thời gian bất tử kết thúc -> Nhận sát thương bình thường
        state.setInvulnerableTimer(0.0);
        state.damagePlayer(20);
        assertEquals(45, state.getPlayerCurrentHealth());
    }

    @Test
    @DisplayName("Verify healPlayer restores HP without exceeding maxHealth cap")
    void testHealPlayerCapsAtMaxHealth() {
        state.setPlayerCurrentHealth(60);

        // Hồi 25 HP -> Lên 85
        state.healPlayer(25);
        assertEquals(85, state.getPlayerCurrentHealth());

        // Hồi thêm 30 HP -> Bị chặn trần ở 100 HP
        state.healPlayer(30);
        assertEquals(100, state.getPlayerCurrentHealth());
    }

    @Test
    @DisplayName("Verify player death when HP reaches 0 and prevents further actions")
    void testPlayerDeathAndRestart() {
        state.damagePlayer(150);
        assertEquals(0, state.getPlayerCurrentHealth());
        assertTrue(state.isPlayerDead());

        // Đã tử trận thì không thể hồi phục hoặc nhận sát thương tiếp
        state.healPlayer(50);
        assertEquals(0, state.getPlayerCurrentHealth());

        // Tái xuất phát run mới bằng init()
        state.init();
        assertEquals(100, state.getPlayerCurrentHealth());
        assertFalse(state.isPlayerDead());
    }

    @Test
    @DisplayName("Verify HealthPickup requires manual interaction and prevents consumption at full HP")
    void testHealthPickupInteractionWithFKey() {
        HealthPickup smallMedkit = new HealthPickup(300, 300, HealthPickup.HealthSize.SMALL);
        assertFalse(smallMedkit.isUsed());
        assertEquals(25, smallMedkit.getHealAmount());

        // 1. Khi người chơi đang đầy máu 100 HP -> Từ chối sử dụng
        state.setPlayerCurrentHealth(100);
        boolean usedWhenFull = smallMedkit.interact(state);
        assertFalse(usedWhenFull, "Should not consume medkit when HP is already 100%");
        assertFalse(smallMedkit.isUsed());
        assertEquals(100, state.getPlayerCurrentHealth());

        // 2. Khi người chơi bị thương (60 HP) -> Cho phép sử dụng bằng phím [F]
        state.setPlayerCurrentHealth(60);
        boolean usedWhenDamaged = smallMedkit.interact(state);
        assertTrue(usedWhenDamaged);
        assertTrue(smallMedkit.isUsed());
        assertEquals(85, state.getPlayerCurrentHealth());

        // 3. Test Hộp Cứu Thương Lớn (Large Medkit)
        HealthPickup largeMedkit = new HealthPickup(350, 350, HealthPickup.HealthSize.LARGE);
        assertEquals(50, largeMedkit.getHealAmount());
        state.setPlayerCurrentHealth(70);
        boolean largeUsed = largeMedkit.interact(state);
        assertTrue(largeUsed);
        assertEquals(100, state.getPlayerCurrentHealth(), "70 + 50 capped at 100");

        // 4. Test Băng Gạc Cứu Thương (Bandage) rơi từ kẻ địch
        HealthPickup bandage = new HealthPickup(400, 400, HealthPickup.HealthSize.BANDAGE);
        assertEquals(10, bandage.getHealAmount());
        assertEquals("BĂNG GẠC CỨU THƯƠNG", bandage.getSize().getDisplayName());
        assertEquals("+10 HP", bandage.getSize().getTag());
        state.setPlayerCurrentHealth(75);
        boolean bandageUsed = bandage.interact(state);
        assertTrue(bandageUsed);
        assertTrue(bandage.isUsed());
        assertEquals(85, state.getPlayerCurrentHealth(), "75 + 10 = 85 HP");

        // Khi đầy máu (100 HP), không thể sử dụng Bandage
        HealthPickup fullBandage = new HealthPickup(420, 420, HealthPickup.HealthSize.BANDAGE);
        state.setPlayerCurrentHealth(100);
        assertFalse(fullBandage.interact(state), "Cannot consume Bandage when HP is full");
        assertFalse(fullBandage.isUsed());
    }

    @Test
    @DisplayName("Verify Shop Medkit purchase logic, credits deduction, and full HP guard")
    void testShopHealthItemPurchase() {
        ShopItem healthStation = ShopItem.createHealthItem(200, 200, 75);
        assertTrue(healthStation.isAvailable());
        assertEquals(ShopItem.ShopItemType.HEALTH, healthStation.getType());
        assertEquals(75, healthStation.getPrice());

        state.setPlayerCredits(100);
        state.setPlayerCurrentHealth(100);

        // 1. Máu đang đầy 100 HP -> Không cho mua, bảo toàn 100 Credits
        boolean boughtWhenFull = healthStation.purchase(state);
        assertFalse(boughtWhenFull);
        assertFalse(healthStation.isPurchased());
        assertEquals(100, state.getPlayerCredits());

        // 2. Bị thương (40 HP) và đủ tiền -> Cho phép mua, trừ 75 Credits, hồi +50 HP
        state.setPlayerCurrentHealth(40);
        boolean boughtWhenDamaged = healthStation.purchase(state);
        assertTrue(boughtWhenDamaged);
        assertTrue(healthStation.isPurchased());
        assertEquals(25, state.getPlayerCredits());
        assertEquals(90, state.getPlayerCurrentHealth());

        // 3. Không đủ tiền (cần 75 Credits, chỉ có 25)
        ShopItem secondStation = ShopItem.createHealthItem(250, 200, 75);
        state.setPlayerCurrentHealth(50);
        boolean boughtNoMoney = secondStation.purchase(state);
        assertFalse(boughtNoMoney);
        assertEquals(25, state.getPlayerCredits());
        assertEquals(50, state.getPlayerCurrentHealth());
    }

    @Test
    @DisplayName("Verify Shop randomizes supply slot between Ammo and Health across stages")
    void testShopSupplySlotRandomization() {
        int healthCount = 0;
        int ammoCount = 0;

        for (int i = 0; i < 50; i++) {
            PlayingState testState = new PlayingState(gsm);
            testState.init();
            for (ShopItem item : testState.getShopItems()) {
                if (item.getType() == ShopItem.ShopItemType.HEALTH) {
                    healthCount++;
                } else if (item.getType() == ShopItem.ShopItemType.AMMO) {
                    ammoCount++;
                }
            }
        }

        assertTrue(healthCount > 0, "Health item should appear in shop supply slot across random seeds");
        assertTrue(ammoCount > 0, "Ammo item should appear in shop supply slot across random seeds");
    }

    @Test
    @DisplayName("Verify operative dodge roll trigger, high-speed movement burst, and directional angles")
    void testPlayerDodgeTriggerAndMovement() {
        double startX = state.getPlayerX();
        double startY = state.getPlayerY();

        // 1. Kích hoạt né sang phải (D = true)
        boolean triggered = state.triggerPlayerDodge(false, false, false, true);
        assertTrue(triggered, "Dodge should activate successfully when ready");
        assertTrue(state.isPlayerDodging(), "Player should be in dodging state");
        assertEquals(0.80, state.getPlayerDodgeCooldownTimer(), 0.05, "Cooldown should be initialized to 0.8s");

        // 2. Cập nhật di chuyển trong 0.1s
        state.getPlayer().handleMovement(false, false, false, true, 0.1, state.getDungeonMap());
        double movedX = state.getPlayerX() - startX;
        // Vận tốc né là 560 px/s, trong 0.1s di chuyển 56px (nhanh hơn tốc độ chạy thường 210 px/s * 0.1 = 21px)
        assertEquals(56.0, movedX, 1.0, "Dodge burst should displace at 560 px/s");
        assertEquals(startY, state.getPlayerY(), 0.01, "Y coordinate should remain unchanged during horizontal dodge");

        // 3. Test đứng yên: Chờ lượt né thứ nhất kết thúc (0.25s) và reset hồi chiêu
        state.getPlayer().update(0.25);
        state.getPlayer().setDodgeCooldownTimer(0.0);
        state.getPlayer().setAimAngle(Math.PI / 2.0); // Hướng thẳng xuống dưới Y
        boolean triggeredStationary = state.triggerPlayerDodge(false, false, false, false);
        assertTrue(triggeredStationary);
        assertEquals(0.0, state.getPlayer().getDodgeDirX(), 0.001);
        assertEquals(1.0, state.getPlayer().getDodgeDirY(), 0.001, "Stationary dodge should follow aim angle");
    }

    @Test
    @DisplayName("Verify i-frames completely block damage during dodge roll")
    void testPlayerDodgeInvulnerabilityFrames() {
        assertEquals(100, state.getPlayerCurrentHealth());
        state.setInvulnerableTimer(0.0);

        // 1. Kích hoạt né đòn
        boolean triggered = state.triggerPlayerDodge(true, false, false, false);
        assertTrue(triggered);
        assertTrue(state.isPlayerDodging());
        assertTrue(state.getPlayer().isInvulnerable(), "Player must be invulnerable during dodge");

        // 2. Nhận sát thương cực lớn (80 DMG) trong lúc né -> Bị chặn 100% bởi i-frames
        state.damagePlayer(80);
        assertEquals(100, state.getPlayerCurrentHealth(), "Damage during dodge roll i-frames must be 100% absorbed");

        // 3. Khi thời gian né kết thúc (sau 0.25s) -> Hết i-frames, nhận sát thương bình thường
        state.getPlayer().update(0.25);
        assertFalse(state.isPlayerDodging(), "Dodge duration should expire after 0.22s");
        state.damagePlayer(40);
        assertEquals(60, state.getPlayerCurrentHealth(), "Damage after dodge expiration must apply normally");
    }

    @Test
    @DisplayName("Verify dodge cooldown prevents spamming and resets after cooldown timer expires")
    void testPlayerDodgeCooldown() {
        // 1. Kích hoạt lần 1 thành công
        assertTrue(state.triggerPlayerDodge(false, false, true, false));
        assertTrue(state.isPlayerDodging());

        // 2. Thử kích hoạt lại ngay lập tức -> Thất bại vì đang né và đang hồi chiêu
        assertFalse(state.triggerPlayerDodge(false, false, true, false), "Cannot trigger dodge while dodging or on cooldown");

        // 3. Sau khi hết thời gian lướt (0.25s), vẫn còn thời gian hồi chiêu (~0.55s) -> Vẫn thất bại
        state.getPlayer().update(0.25);
        assertFalse(state.isPlayerDodging());
        assertTrue(state.getPlayerDodgeCooldownTimer() > 0.4, "Cooldown should still be active");
        assertFalse(state.triggerPlayerDodge(false, false, true, false), "Cannot trigger dodge until full 0.8s cooldown completes");

        // 4. Cập nhật thêm 0.6s -> Hồi chiêu kết thúc -> Kích hoạt lần 2 thành công
        state.getPlayer().update(0.60);
        assertEquals(0.0, state.getPlayerDodgeCooldownTimer(), 0.001);
        assertTrue(state.getPlayer().canDodge());
        assertTrue(state.triggerPlayerDodge(false, false, true, false), "Dodge should become available after cooldown");
    }

    @Test
    @DisplayName("Verify SPACE key triggers dodge roll during PlayingState update loop")
    void testPlayerSpaceKeyTriggersDodgeInPlayingState() {
        KeyboardHandler keyH = new KeyboardHandler();
        MouseHandler mouseH = new MouseHandler();

        // Giả lập phím Space được nhấn
        java.awt.Canvas dummyCanvas = new java.awt.Canvas();
        KeyEvent spaceEvent = new KeyEvent(dummyCanvas, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
        keyH.keyPressed(spaceEvent);
        keyH.update();

        assertFalse(state.isPlayerDodging());
        state.update(0.016, keyH, mouseH);

        assertTrue(state.isPlayerDodging(), "Space key press in PlayingState must trigger dodge roll");
        assertTrue(state.getPlayer().isInvulnerable(), "Player must have i-frames active from Space dodge");
    }

    @Test
    @DisplayName("Verify operative death sequence triggers sliding skull animation without instant game over")
    void testPlayerDeathSlidingSkullSequence() {
        double startX = state.getPlayerX();
        double startY = state.getPlayerY();

        // 1. Gây sát thương chí tử hạ gục người chơi (150 HP)
        state.setInvulnerableTimer(0.0);
        state.damagePlayer(150);

        assertTrue(state.isPlayerDead(), "Player must be dead after fatal damage");
        assertEquals(0, state.getPlayerCurrentHealth());
        assertFalse(state.getPlayer().isDeathSequenceComplete(), "Death sequence must NOT be complete immediately");
        assertEquals(0.0, state.getPlayer().getDeathSequenceProgress(), 0.001);

        // 2. Cập nhật chuyển động trượt trong 0.5s -> Tọa độ trượt di chuyển theo quán tính
        state.getPlayer().update(0.5, state.getDungeonMap());
        assertFalse(state.getPlayer().isDeathSequenceComplete());
        assertEquals(0.25, state.getPlayer().getDeathSequenceProgress(), 0.01);
        double displacedDist = Math.hypot(state.getPlayerX() - startX, state.getPlayerY() - startY);
        assertTrue(displacedDist > 5.0, "Skull should slide across the floor due to inertia displacement");

        // 3. Cập nhật thêm 1.6s (tổng 2.1s > 2.0s) -> Hoạt cảnh trượt hoàn tất
        state.getPlayer().update(1.6, state.getDungeonMap());
        assertTrue(state.getPlayer().isDeathSequenceComplete(), "Death sequence must be complete after full duration");
        assertEquals(1.0, state.getPlayer().getDeathSequenceProgress(), 0.001);

        // 4. Test chức năng bỏ qua (Skip) ngay lập tức
        state.getPlayer().reset(startX, startY);
        assertFalse(state.isPlayerDead());
        state.damagePlayer(150);
        assertTrue(state.isPlayerDead());
        assertFalse(state.getPlayer().isDeathSequenceComplete());

        state.getPlayer().completeDeathSequence();
        assertTrue(state.getPlayer().isDeathSequenceComplete(), "completeDeathSequence must finish transition immediately");
    }

    @Test
    @DisplayName("Verify operative death automatically respawns at The Range after death sequence without any keypress")
    void testPlayerDeathAutoRespawnToTheRangeWithoutKeypress() {
        com.groupproject.game.hub.HubRangeState hubRangeState = new com.groupproject.game.hub.HubRangeState(gsm);
        gsm.registerState(GameStateManager.StateType.THE_RANGE, hubRangeState);
        gsm.registerState(GameStateManager.StateType.PLAYING, state);
        gsm.setState(GameStateManager.StateType.PLAYING);

        KeyboardHandler keyH = new KeyboardHandler();
        MouseHandler mouseH = new MouseHandler();

        // 1. Gây sát thương chí mạng tiêu diệt người chơi
        state.setInvulnerableTimer(0.0);
        state.damagePlayer(150);
        assertTrue(state.isPlayerDead());
        assertEquals(GameStateManager.StateType.PLAYING, gsm.getCurrentStateType());

        // 2. Cập nhật 1.0 giây (hoạt cảnh trượt chưa xong): Vẫn ở PLAYING
        state.update(1.0, keyH, mouseH);
        assertFalse(state.getPlayer().isDeathSequenceComplete());
        assertEquals(GameStateManager.StateType.PLAYING, gsm.getCurrentStateType());

        // 3. Cập nhật thêm 1.2 giây (tổng 2.2s > 2.0s hoàn tất hoạt cảnh tử trận)
        // Tuyệt đối không nhấn phím nào (ENTER / R / ESC)
        state.update(1.2, keyH, mouseH);

        // Xác nhận: Hệ thống tự động chuyển tiếp thẳng người chơi về The Range
        assertEquals(GameStateManager.StateType.THE_RANGE, gsm.getCurrentStateType(),
                "Player must auto-respawn in The Range without requiring any key press");
        assertEquals(100, hubRangeState.getPlayer().getCurrentHealth(),
                "Player must respawn in The Range with full health");
    }

    @Test
    @DisplayName("Verify BossTank is pre-spawned and ready in Boss Arena without portal telegraph")
    void testBossPreSpawnInBossStageWithoutPortalTelegraph() {
        // Chuyển thẳng tới Stage 4 (Boss Stage)
        while (state.getCurrentStage() < PlayingState.BOSS_STAGE) {
            state.advanceToNextStage();
        }
        assertEquals(PlayingState.BOSS_STAGE, state.getCurrentStage());

        // Xác nhận BossTank đã được sinh sẵn trong phòng Boss
        com.groupproject.game.entity.enemy.TargetDummy boss = state.getBossDummy();
        assertNotNull(boss, "BossTank must be pre-spawned in Boss Stage arena");
        assertTrue(boss instanceof com.groupproject.game.entity.enemy.BossTank, "Boss dummy must be instance of BossTank");
        assertTrue(boss.isAlive(), "BossTank must be alive");
        assertFalse(boss.isSpawning(), "BossTank must NOT be in portal spawning telegraph mode, it must be fully visible");
    }

    @Test
    @DisplayName("Verify directional projectile tracer beams render without errors and respect yellow/red colors")
    void testProjectileTracerRenderBeamColors() {
        // Đạn thường của người chơi (Player regular bullet)
        com.groupproject.game.entity.projectile.Projectile normalBullet =
                new com.groupproject.game.entity.projectile.Projectile(100, 100, 0.0, 500, 20, 2.0, true, false);
        assertTrue(normalBullet.isPlayerBullet());
        assertFalse(normalBullet.isCritical());
        assertFalse(normalBullet.isPiercing());

        // Đạn bắn tỉa của người chơi (Sniper piercing bullet)
        com.groupproject.game.entity.projectile.Projectile sniperBullet =
                new com.groupproject.game.entity.projectile.Projectile(100, 100, 0.5, 900, 85, 2.0, true, false, 2, 0.25);
        assertTrue(sniperBullet.isPlayerBullet());
        assertFalse(sniperBullet.isCritical());
        assertTrue(sniperBullet.isPiercing());

        // Đạn chí mạng (Critical bullet)
        com.groupproject.game.entity.projectile.Projectile critBullet =
                new com.groupproject.game.entity.projectile.Projectile(100, 100, 0.5, 900, 150, 2.0, true, true, 2, 0.25);
        assertTrue(critBullet.isPlayerBullet());
        assertTrue(critBullet.isCritical());

        // Kết xuất đồ họa thử nghiệm trên BufferedImage Graphics2D
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(400, 400, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        com.groupproject.game.core.Camera camera = new com.groupproject.game.core.Camera(400, 400);

        assertDoesNotThrow(() -> normalBullet.render(g2, camera));
        assertDoesNotThrow(() -> sniperBullet.render(g2, camera));
        assertDoesNotThrow(() -> critBullet.render(g2, camera));
        g2.dispose();
    }

    @Test
    @DisplayName("Verify Campaign Boss extraction displays Demo thank you message and returns to The Range on button press")
    void testCampaignBossExtractionReturnsToTheRange() {
        com.groupproject.game.hub.HubRangeState hubRangeState = new com.groupproject.game.hub.HubRangeState(gsm);
        gsm.registerState(GameStateManager.StateType.THE_RANGE, hubRangeState);
        gsm.registerState(GameStateManager.StateType.PLAYING, state);
        gsm.setState(GameStateManager.StateType.PLAYING);

        // Đảm bảo là chế độ Campaign
        state.setStandaloneBossFight(false);
        assertFalse(state.isStandaloneBossFight());

        // Kích hoạt sơ tán chiến thắng sau khi hạ boss
        state.setIsExtracting(true);
        assertTrue(state.isExtracting());

        KeyboardHandler keyH = new KeyboardHandler();
        MouseHandler mouseH = new MouseHandler();

        // Cập nhật mà chưa bấm nút -> Vẫn ở màn hình victory
        state.update(0.5, keyH, mouseH);
        assertEquals(GameStateManager.StateType.PLAYING, gsm.getCurrentStateType());

        // Người chơi bấm phím ENTER để tiếp tục
        keyH.setKeyPressed(KeyEvent.VK_ENTER, true);
        state.update(0.016, keyH, mouseH);

        // Phải transition về THE_RANGE
        assertTrue(gsm.getScreenTransition().isActive());
        // Chờ transition hoàn tất
        gsm.getScreenTransition().update(1.0);
        assertEquals(GameStateManager.StateType.THE_RANGE, gsm.getCurrentStateType(),
                "Campaign victory extraction must return to The Range on button press");
    }

    @Test
    @DisplayName("Verify Standalone Boss Fight returns to MENU on both win and loss")
    void testStandaloneBossFightWinAndLossReturnsToMenu() {
        com.groupproject.game.state.MenuState menuState = new com.groupproject.game.state.MenuState(gsm);
        gsm.registerState(GameStateManager.StateType.MENU, menuState);
        gsm.registerState(GameStateManager.StateType.PLAYING, state);
        gsm.setState(GameStateManager.StateType.PLAYING);

        // Chế độ Boss Fight độc lập
        state.setStandaloneBossFight(true);
        assertTrue(state.isStandaloneBossFight());

        KeyboardHandler keyH = new KeyboardHandler();
        MouseHandler mouseH = new MouseHandler();

        // 1. Kiểm tra trường hợp THẮNG (Extract)
        state.setIsExtracting(true);
        keyH.setKeyPressed(KeyEvent.VK_SPACE, true);
        state.update(0.016, keyH, mouseH);
        keyH.setKeyPressed(KeyEvent.VK_SPACE, false);

        // Transition về MENU
        assertTrue(gsm.getScreenTransition().isActive());
        gsm.getScreenTransition().update(1.0);
        assertEquals(GameStateManager.StateType.MENU, gsm.getCurrentStateType(),
                "Standalone boss fight victory must return to MENU");

        // 2. Kiểm tra trường hợp THUA (Chết)
        gsm.setState(GameStateManager.StateType.PLAYING);
        state.setStandaloneBossFight(true);
        state.setIsExtracting(false);
        state.setInvulnerableTimer(0.0);
        state.damagePlayer(150);
        assertTrue(state.isPlayerDead());

        // Cập nhật hết chuỗi tử trận (màn hình đen dần -> chữ -> lại đen dần)
        state.update(2.5, keyH, mouseH);
        assertEquals(GameStateManager.StateType.MENU, gsm.getCurrentStateType(),
                "Standalone boss fight loss must return to MENU");
    }

    @Test
    @DisplayName("Verify damagePlayer triggers PLAYER_GOT_HIT sound effect and screen shake")
    void testPlayerDamageTriggersGotHitSfxAndScreenShake() {
        assertEquals(100, state.getPlayerCurrentHealth());
        state.damagePlayer(25);
        assertEquals(75, state.getPlayerCurrentHealth());
        assertTrue(state.getScreenShakeTimer() > 0.0, "Screen shake should be active upon taking damage");
        assertTrue(com.groupproject.game.core.GameSoundManager.isSoundLoaded(com.groupproject.game.core.GameSoundManager.SoundEffect.PLAYER_GOT_HIT),
                "PLAYER_GOT_HIT sound must be loaded and ready");
    }
}
