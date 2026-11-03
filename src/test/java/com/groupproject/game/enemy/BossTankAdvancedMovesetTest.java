package com.groupproject.game.enemy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.dungeon.DungeonMap;
import com.groupproject.game.dungeon.Tile;
import com.groupproject.game.dungeon.TileType;
import com.groupproject.game.entity.enemy.BossTank;
import com.groupproject.game.entity.enemy.BossTank.TankState;
import com.groupproject.game.entity.player.Player;
import com.groupproject.game.state.GameStateManager;
import com.groupproject.game.state.PlayingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Kiểm thử toàn diện các tính năng nâng cấp moveset của BossTank:
 * 1. Mở khóa chiêu Đạn Diệt Bộ Binh (Canister Shot) khi mất 25% máu.
 * 2. Ngăn chặn đòn đâm húc khi người chơi nấp sau cover cứng hoặc tường.
 * 3. Cơ chế sát thương hình nón hitscan và che chắn an toàn sau cover cứng.
 * 4. Bố cục đấu trường Grand Boss Arena thông thoáng cho Boss di chuyển.
 */
public class BossTankAdvancedMovesetTest {

    private DungeonMap dungeonMap;

    @BeforeEach
    public void setUp() {
        dungeonMap = new DungeonMap();
        dungeonMap.generateStage(4); // Stage 4 Grand Boss Arena
    }

    @Test
    @DisplayName("1. Chiêu Đạn Diệt Bộ Binh (Canister Shot) chỉ mở khóa khi Boss mất ít nhất 25% máu (HP <= 75%)")
    public void testCanisterShotUnlockedAt75PercentHealth() {
        BossTank tank = new BossTank(300, 300);
        assertEquals(1250, tank.getMaxHealth());
        assertEquals(1250, tank.getCurrentHealth());

        // 100% máu -> Chưa mở khóa
        assertFalse(tank.isCanisterUnlocked(), "Khi còn 100% HP, chiêu Canister chưa mở khóa");

        // 80% máu (1000 HP) -> Chưa mở khóa (mới mất 20%)
        tank.setCurrentHealth(1000);
        assertFalse(tank.isCanisterUnlocked(), "Khi còn 80% HP (> 75%), chiêu Canister chưa mở khóa");

        // Đúng 75% máu (937 HP) -> Đã mở khóa
        tank.setCurrentHealth(937);
        assertTrue(tank.isCanisterUnlocked(), "Khi HP <= 75%, chiêu Canister bắt buộc mở khóa");

        // 50% máu (625 HP) -> Đã mở khóa
        tank.setCurrentHealth(625);
        assertTrue(tank.isCanisterUnlocked(), "Khi HP <= 50%, chiêu Canister vẫn mở khóa");
    }

    @Test
    @DisplayName("2. Boss phát hiện đường đâm bị chắn bởi cover cứng và TUYỆT ĐỐI KHÔNG chọn đòn đâm")
    public void testBossDoesNotRamWhenPlayerIsBehindSolidCover() {
        BossTank tank = new BossTank(400, 300);
        tank.setCurrentHealth(1100); // Phase 1 (> 75% máu)

        // Đặt người chơi ở bên phải Boss cách 240px
        double playerX = 640.0;
        double playerY = 300.0;

        // Đặt một cột cover cứng (TileType.COVER) chắn ngang giữa Boss và người chơi
        int coverCol = (int) Math.floor(520.0 / GameConfig.TILE_SIZE);
        int coverRow = (int) Math.floor(300.0 / GameConfig.TILE_SIZE);
        Tile t = dungeonMap.getTile(coverCol, coverRow);
        if (t != null) {
            t.setType(TileType.COVER);
        }

        // Kiểm tra hàm phát hiện vật cản đường đâm
        assertTrue(
                tank.isRamPathBlockedBySolidCover(playerX, playerY, dungeonMap),
                "Phải phát hiện đường đâm bị chặn bởi cột cover cứng"
        );

        // Gọi chooseNextAction 30 lần liên tiếp -> Tuyệt đối không bao giờ được chọn RAM_WINDUP!
        for (int i = 0; i < 30; i++) {
            tank.setState(TankState.PATROL_REPOSITION);
            tank.setLastAttackWasRam(false);
            tank.chooseNextAction(240.0, playerX, playerY, dungeonMap);

            assertNotEquals(
                    TankState.RAM_WINDUP, tank.getState(),
                    "Boss TUYỆT ĐỐI KHÔNG được chọn RAM_WINDUP khi người chơi đang nấp sau cover cứng!"
            );
            assertEquals(
                    TankState.MG_BURST, tank.getState(),
                    "Boss phải chuyển sang súng máy và strafe khi đường đâm bị cản"
            );
            assertTrue(tank.isMgStrafing(), "Khi đường đâm bị chặn, súng máy phải bật strafe để lượn tìm góc bắn");
        }
    }

    @Test
    @DisplayName("3. Khi đường đâm thông thoáng (không có cover cứng), Boss vẫn có thể kích hoạt đòn đâm")
    public void testBossCanRamWhenPathIsOpen() {
        BossTank tank = new BossTank(400, 300);
        tank.setCurrentHealth(1100); // Phase 1

        // Đặt người chơi ở góc thông thoáng không có cover cứng
        double playerX = 640.0;
        double playerY = 300.0;

        // Dọn sạch toàn bộ tile giữa Boss và người chơi thành FLOOR
        int startCol = (int) Math.floor(350.0 / GameConfig.TILE_SIZE);
        int endCol = (int) Math.floor(680.0 / GameConfig.TILE_SIZE);
        int row = (int) Math.floor(300.0 / GameConfig.TILE_SIZE);
        for (int c = startCol; c <= endCol; c++) {
            for (int r = row - 1; r <= row + 1; r++) {
                Tile tile = dungeonMap.getTile(c, r);
                if (tile != null) tile.setType(TileType.FLOOR);
            }
        }

        assertFalse(
                tank.isRamPathBlockedBySolidCover(playerX, playerY, dungeonMap),
                "Đường đâm thông thoáng phải trả về false"
        );

        // Kiểm tra trong 50 lần thử, Boss sẽ chọn đòn đâm khi điều kiện thỏa mãn
        boolean ramTriggered = false;
        for (int i = 0; i < 50; i++) {
            tank.setState(TankState.PATROL_REPOSITION);
            tank.setLastAttackWasRam(false);
            tank.chooseNextAction(240.0, playerX, playerY, dungeonMap);
            if (tank.getState() == TankState.RAM_WINDUP) {
                ramTriggered = true;
                break;
            }
        }
        assertTrue(ramTriggered, "Boss phải có khả năng kích hoạt đòn đâm khi đường đâm thông thoáng");
    }

    @Test
    @DisplayName("4. Canister Shot gây 34 HP sát thương tức thì trong hình nón khi người chơi ở góc thoáng")
    public void testCanisterShotDealsInstantDamageInOpenCone() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        Player player = state.getPlayer();
        player.setCurrentHealth(100);

        BossTank tank = new BossTank(200.0, 200.0);
        tank.setCurrentHealth(800); // Đã mất > 25% máu -> mở khóa Canister
        tank.setCanisterAngle(0.0); // Bắn thẳng sang hướng EAST (+X)

        // Đặt người chơi ở (350, 200) -> cự ly 150px, góc lệch 0 độ (nằm trọn trong hình nón 300px góc 70 độ)
        player.setX(350.0);
        player.setY(200.0);

        // Xóa sạch chướng ngại vật ở giữa thành FLOOR
        int c1 = (int) Math.floor(200.0 / GameConfig.TILE_SIZE);
        int c2 = (int) Math.floor(380.0 / GameConfig.TILE_SIZE);
        int r = (int) Math.floor(200.0 / GameConfig.TILE_SIZE);
        for (int c = c1; c <= c2; c++) {
            Tile tile = state.getDungeonMap().getTile(c, r);
            if (tile != null) tile.setType(TileType.FLOOR);
        }

        tank.setState(TankState.CANISTER_FIRE);
        tank.setStateTimer(0.12);

        // Kích hoạt phát bắn Canister Shot
        tank.updateAI(0.05, player.getX(), player.getY(), player.getSize(), state.getDungeonMap(), state.getProjectileManager(), state);

        // Người chơi phải nhận đúng 34 sát thương -> máu còn 66 HP
        assertEquals(66, player.getCurrentHealth(), "Người chơi trong hình nón thoáng phải bị trừ 34 HP");
        assertEquals(TankState.CANISTER_FIRE, tank.getState());
    }

    @Test
    @DisplayName("5. Người chơi nấp sau Cover Cứng hoàn toàn TRÁNH ĐƯỢC sát thương của Canister Shot")
    public void testCanisterShotBlockedBySolidCover() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        Player player = state.getPlayer();
        player.setCurrentHealth(100);

        BossTank tank = new BossTank(200.0, 200.0);
        tank.setCurrentHealth(800);
        tank.setCanisterAngle(0.0); // Hướng nổ sang phải

        player.setX(380.0);
        player.setY(200.0);

        // Đặt một cột cover cứng (TileType.COVER) kiên cố chắn giữa Boss và người chơi
        int coverCol = (int) Math.floor(290.0 / GameConfig.TILE_SIZE);
        int coverRow = (int) Math.floor(200.0 / GameConfig.TILE_SIZE);
        Tile t = state.getDungeonMap().getTile(coverCol, coverRow);
        if (t != null) {
            t.setType(TileType.COVER);
        }

        // Xác thực Line-of-sight bị chặn
        double bx = tank.getX() + Math.cos(0.0) * 54.0;
        double by = tank.getY() + Math.sin(0.0) * 54.0;
        assertTrue(
                tank.isLineOfSightBlockedBySolidCover(bx, by, player.getX(), player.getY(), state.getDungeonMap()),
                "Đường đạn chùm phải bị chặn bởi cột cover cứng"
        );

        tank.setState(TankState.CANISTER_FIRE);
        tank.setStateTimer(0.12);

        // Kích hoạt khai hỏa
        tank.updateAI(0.05, player.getX(), player.getY(), player.getSize(), state.getDungeonMap(), state.getProjectileManager(), state);

        // Máu người chơi phải giữ nguyên 100 HP!
        assertEquals(100, player.getCurrentHealth(), "Người chơi nấp sau cover cứng phải an toàn 100% (0 sát thương)");
    }

    @Test
    @DisplayName("6. Người chơi đứng ngoài tầm bắn (> 300px) hoặc ngoài góc hình nón (> 35 độ) không bị trúng đòn Canister")
    public void testCanisterShotIgnoresPlayerOutsideConeOrRange() {
        GameStateManager gsm = new GameStateManager();
        PlayingState state = new PlayingState(gsm);
        Player player = state.getPlayer();
        player.setCurrentHealth(100);

        BossTank tank = new BossTank(200.0, 200.0);
        tank.setCurrentHealth(800);
        tank.setCanisterAngle(0.0); // Hướng sang phải (EAST)

        // Trường hợp A: Người chơi đứng ở phía sau Boss (hướng WEST, 180 độ)
        player.setX(50.0);
        player.setY(200.0);

        tank.setState(TankState.CANISTER_FIRE);
        tank.setStateTimer(0.12);
        tank.updateAI(0.05, player.getX(), player.getY(), player.getSize(), state.getDungeonMap(), state.getProjectileManager(), state);

        assertEquals(100, player.getCurrentHealth(), "Người chơi sau lưng Boss không bị ảnh hưởng");

        // Trường hợp B: Người chơi đứng thẳng hướng bắn nhưng ở cự ly xa 450px (> 300px range)
        player.setX(650.0);
        player.setY(200.0);

        tank.setState(TankState.CANISTER_FIRE);
        tank.setStateTimer(0.12);
        tank.updateAI(0.05, player.getX(), player.getY(), player.getSize(), state.getDungeonMap(), state.getProjectileManager(), state);

        assertEquals(100, player.getCurrentHealth(), "Người chơi ngoài cự ly 300px không bị ảnh hưởng");
    }

    @Test
    @DisplayName("7. Đấu trường Grand Boss Arena có 4 trụ bố trí cách xa nhau và trục trung tâm rộng rãi")
    public void testGrandBossArenaLayoutHasSpaciousCorridors() {
        int bossCol = 24;
        int bossRow = 13;
        int bossW = DungeonMap.BOSS_ROOM_WIDTH; // 28
        int bossH = DungeonMap.BOSS_ROOM_HEIGHT; // 18

        // Kiểm tra 4 cột trụ mới ở vị trí cách xa nhau
        // Trụ Tây Bắc: bossCol + 6, bossRow + 4
        Tile nw = dungeonMap.getTile(bossCol + 6, bossRow + 4);
        assertTrue(nw != null && nw.getType() == TileType.COVER, "Trụ Tây Bắc phải là COVER");

        // Trụ Đông Bắc: bossCol + bossW - 8 = bossCol + 20
        Tile ne = dungeonMap.getTile(bossCol + bossW - 8, bossRow + 4);
        assertTrue(ne != null && ne.getType() == TileType.COVER, "Trụ Đông Bắc phải là COVER");

        // Trụ Tây Nam: bossCol + 6, bossRow + bossH - 6
        Tile sw = dungeonMap.getTile(bossCol + 6, bossRow + bossH - 6);
        assertTrue(sw != null && sw.getType() == TileType.COVER, "Trụ Tây Nam phải là COVER");

        // Trụ Đông Nam: bossCol + bossW - 8, bossRow + bossH - 6
        Tile se = dungeonMap.getTile(bossCol + bossW - 8, bossRow + bossH - 6);
        assertTrue(se != null && se.getType() == TileType.COVER, "Trụ Đông Nam phải là COVER");

        // Kiểm tra tâm phòng: từ cột 10 đến cột 18 hoàn toàn thông thoáng không có COVER cứng hoặc WALL
        int midRow = bossRow + (bossH / 2);
        for (int c = bossCol + 10; c <= bossCol + 18; c++) {
            Tile midTile = dungeonMap.getTile(c, midRow);
            assertTrue(midTile != null && midTile.getType() != TileType.COVER && midTile.getType() != TileType.WALL,
                    "Trục trung tâm phòng Boss (cột " + c + ") phải thông thoáng không có COVER cứng hoặc WALL");
        }
    }

    @Test
    @DisplayName("8. Thời gian cảnh báo chiêu Canister Shot là 0.75s và góc ngắm khóa cố định ở 0.20s cuối")
    public void testCanisterWindupDurationIs750msAndLocksAt200ms() {
        BossTank tank = new BossTank(300, 300);
        tank.setCurrentHealth(800); // Mở khóa Canister

        // Gọi setState(CANISTER_WINDUP)
        tank.setState(TankState.CANISTER_WINDUP);
        assertEquals(0.75, tank.getCanisterWindupDuration(), 0.001, "Thời gian cảnh báo Canister phải đúng 0.75s (750ms)");
        assertEquals(0.75, tank.getStateTimer(), 0.001, "stateTimer phải bắt đầu từ 0.75s");

        // Khi stateTimer > 0.20s: nòng pháo bám theo người chơi ở góc 0 radian (300, 300) -> (500, 300)
        tank.updateAI(0.10, 500.0, 300.0, 28.0, dungeonMap, null, null);
        assertEquals(0.0, tank.getCanisterAngle(), 0.01, "Nòng pháo phải hướng sang phải 0 radian theo người chơi");

        // Di chuyển người chơi sang góc 90 độ (300, 500) khi timer vẫn > 0.20s (0.75 - 0.20 = 0.55s)
        tank.updateAI(0.10, 300.0, 500.0, 28.0, dungeonMap, null, null);
        assertEquals(Math.PI / 2.0, tank.getCanisterAngle(), 0.05, "Nòng pháo bám theo người chơi ở góc 90 độ");

        // Giảm timer xuống vùng khóa góc (stateTimer = 0.15s <= 0.20s)
        tank.setStateTimer(0.15);
        // Đổi người chơi sang góc 180 độ (-X)
        tank.updateAI(0.05, 100.0, 300.0, 28.0, dungeonMap, null, null);
        // Góc pháo phải BỊ KHÓA, không đổi sang 180 độ mà vẫn giữ nguyên góc cũ
        assertEquals(Math.PI / 2.0, tank.getCanisterAngle(), 0.05, "Ở 0.20s cuối, góc ngắm nón phải bị khóa cố định để người chơi né đòn");
    }

    @Test
    @DisplayName("9. Ưu tiên hỏa lực theo cự ly: Ở gần ưu tiên pháo bộ binh, ở xa ưu tiên pháo nổ đại bác")
    public void testDistanceBasedAttackPriority() {
        BossTank tank = new BossTank(300, 300);
        // HP = 500 (< 50% maxHealth 1250) -> Cả Canister Shot và Cannon Blast đều đã mở khóa!
        tank.setCurrentHealth(500);
        assertTrue(tank.isCanisterUnlocked(), "Canister Shot phải mở khóa");
        assertTrue(tank.isCannonAwakened(), "Cannon Blast phải mở khóa");

        // 1. CỰ LY GẦN (200px <= 320px): Ưu tiên Pháo bộ binh (Canister Shot)
        int nearCanisterCount = 0;
        int nearCannonCount = 0;
        int runs = 60;
        for (int i = 0; i < runs; i++) {
            tank.setState(TankState.PATROL_REPOSITION);
            tank.setLastAttackWasRam(false);
            tank.setLastAttackWasCanister(false);
            tank.chooseNextAction(200.0, 500.0, 300.0, dungeonMap);

            if (tank.getState() == TankState.CANISTER_WINDUP) {
                nearCanisterCount++;
            } else if (tank.getState() == TankState.CANNON_AIMING) {
                nearCannonCount++;
            }
        }
        assertTrue(nearCanisterCount > runs * 0.5,
                "Ở cự ly gần (200px), pháo bộ binh Canister Shot phải chiếm ưu thế áp đảo (> 50%, thực tế: " + nearCanisterCount + "/" + runs + ")");
        assertTrue(nearCanisterCount > nearCannonCount,
                "Ở cự ly gần, số lần bắn pháo bộ binh (" + nearCanisterCount + ") phải vượt trội so với pháo nổ (" + nearCannonCount + ")");

        // 2. CỰ LY XA (450px > 320px): Ưu tiên Pháo nổ đại bác (Cannon Blast), Tuyệt đối KHÔNG dùng Canister
        int farCanisterCount = 0;
        int farCannonCount = 0;
        for (int i = 0; i < runs; i++) {
            tank.setState(TankState.PATROL_REPOSITION);
            tank.setLastAttackWasRam(false);
            tank.setLastAttackWasCanister(false);
            tank.chooseNextAction(450.0, 750.0, 300.0, dungeonMap);

            if (tank.getState() == TankState.CANISTER_WINDUP) {
                farCanisterCount++;
            } else if (tank.getState() == TankState.CANNON_AIMING) {
                farCannonCount++;
            }
        }
        assertEquals(0, farCanisterCount, "Ở cự ly xa (450px), Boss TUYỆT ĐỐI KHÔNG BAO GIỜ chọn pháo bộ binh Canister Shot");
        assertTrue(farCannonCount > runs * 0.5,
                "Ở cự ly xa (450px), pháo nổ đại bác tầm xa phải chiếm ưu thế áp đảo (> 50%, thực tế: " + farCannonCount + "/" + runs + ")");
    }

    @Test
    @DisplayName("10. Khi Boss chuẩn bị dùng Canister Shot (CANISTER_WINDUP), TUYỆT ĐỐI KHÔNG hiển thị vệt cảnh báo đòn đâm (RAM_WINDUP)")
    public void testCanisterWindupDoesNotRenderRamTelegraph() {
        BossTank tank = new BossTank(300, 300);
        tank.setCurrentHealth(800);
        tank.setState(TankState.CANISTER_WINDUP);

        // Render ra Graphics2D thực tế để đảm bảo không bị lỗi crash và render đúng state độc lập
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(800, 600, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        com.groupproject.game.core.Camera camera = new com.groupproject.game.core.Camera(800, 600);

        tank.render(g2, camera);
        g2.dispose();

        assertEquals(TankState.CANISTER_WINDUP, tank.getState(), "Trạng thái phải là CANISTER_WINDUP");
        assertNotEquals(TankState.RAM_WINDUP, tank.getState(), "Trạng thái không được là RAM_WINDUP");
    }
    @Test
    @DisplayName("11. Khi Boss Tank ở dưới ngưỡng 50% máu, Boss VẪN PHẢI sử dụng đòn đâm húc (RAM_WINDUP) khi đường đâm thông thoáng")
    public void testBossTankStillRamsBelow50PercentHealth() {
        BossTank tank = new BossTank(300, 300);
        // HP = 500 / 1250 (< 50% HP) -> Phase 2 thức tỉnh
        tank.setCurrentHealth(500);
        assertTrue(tank.isCannonAwakened(), "Khi HP < 50%, Boss Tank phải ở trạng thái pháo thức tỉnh Phase 2");

        // 1. Kiểm tra ở cự ly xa (400px > 320px) với đường đâm hoàn toàn thông thoáng (map = null hoặc map trống)
        int farRamCount = 0;
        int runs = 100;
        for (int i = 0; i < runs; i++) {
            tank.setState(TankState.PATROL_REPOSITION);
            tank.setLastAttackWasRam(false);
            tank.setLastAttackWasCanister(false);
            // Đường đâm thông thoáng (map = null)
            tank.chooseNextAction(400.0, 700.0, 300.0, null);

            if (tank.getState() == TankState.RAM_WINDUP) {
                farRamCount++;
                assertEquals(0.48, tank.getStateTimer(), 0.001, "Ở Phase 2 (< 50% HP), thời gian lấy đà đâm phải là 0.48s siêu tốc");
            }
        }
        assertTrue(farRamCount > 15,
                "Ở cự ly xa (> 320px) khi dưới 50% máu, Boss VẪN PHẢI sử dụng đòn đâm húc RAM_WINDUP (kỳ vọng > 15/100, thực tế: " + farRamCount + ")");

        // 2. Kiểm tra ở cự ly gần (240px <= 320px) khi đường đâm thông thoáng
        int nearRamCount = 0;
        for (int i = 0; i < runs; i++) {
            tank.setState(TankState.PATROL_REPOSITION);
            tank.setLastAttackWasRam(false);
            tank.setLastAttackWasCanister(false);
            tank.chooseNextAction(240.0, 540.0, 300.0, null);

            if (tank.getState() == TankState.RAM_WINDUP) {
                nearRamCount++;
            }
        }
        assertTrue(nearRamCount > 15,
                "Ở cự ly gần (<= 320px) khi dưới 50% máu, Boss VẪN PHẢI sử dụng đòn đâm húc RAM_WINDUP (kỳ vọng > 15/100, thực tế: " + nearRamCount + ")");
    }
}
