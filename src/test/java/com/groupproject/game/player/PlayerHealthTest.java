package com.groupproject.game.player;

import com.groupproject.game.entity.item.HealthPickup;
import com.groupproject.game.entity.shop.ShopItem;
import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
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
}
