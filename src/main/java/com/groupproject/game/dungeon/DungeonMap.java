package com.groupproject.game.dungeon;

import com.groupproject.game.core.Camera;
import com.groupproject.game.core.GameConfig;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the discrete 2D grid matrix of terrain tiles, collision checks, rooms, and lockdown doors.
 *
 * <p>WHY: Centralizes procedural map generation, spatial queries, and door state transitions.
 * Supports generating 3 to 5 randomized combat chambers per run connected by procedural corridors,
 * each with automatic blast doors sealing the operative during combat encounters and unlocking upon clearance.
 */
public class DungeonMap {

    public static final int COLS = GameConfig.WORLD_WIDTH / GameConfig.TILE_SIZE; // 80 columns
    public static final int ROWS = GameConfig.WORLD_HEIGHT / GameConfig.TILE_SIZE; // 45 rows

    private final Tile[][] tiles;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Room> combatRooms = new ArrayList<>();

    private Room spawnRoom;
    private Room combatRoom; // First combat room for backward compatibility
    private Room extractRoom;
    private Room shopRoom;

    /**
     * Constructs the dungeon matrix and builds the procedural multi-room layout.
     */
    public DungeonMap() {
        this.tiles = new Tile[COLS][ROWS];
        initializeEmptyWorld();
        generateProceduralDungeon();
    }

    /**
     * Initializes all grid slots to impassable Void terrain by default.
     */
    private void initializeEmptyWorld() {
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                tiles[c][r] = new Tile(c, r, TileType.VOID);
            }
        }
    }

    // Kích thước chuẩn của mỗi phòng và khoảng cách hành lang trên lưới ô
    public static final int ROOM_WIDTH = 14;
    public static final int ROOM_HEIGHT = 10;
    public static final int CORRIDOR_LEN_X = 6;
    public static final int CORRIDOR_LEN_Y = 4;
    public static final int GRID_COLS = 4;
    public static final int GRID_ROWS = 3;
    public static final int OFFSET_X = 3;
    public static final int OFFSET_Y = 4;

    /**
     * 4 hướng kết nối chính giữa các phòng liền kề: 12h (Bắc), 3h (Đông), 6h (Nam), 9h (Tây).
     */
    public enum Direction {
        NORTH(0, -1, "12h"),
        EAST(1, 0, "3h"),
        SOUTH(0, 1, "6h"),
        WEST(-1, 0, "9h");

        public final int dx;
        public final int dy;
        public final String clockName;

        Direction(int dx, int dy, String clockName) {
            this.dx = dx;
            this.dy = dy;
            this.clockName = clockName;
        }

        public Direction opposite() {
            switch (this) {
                case NORTH: return SOUTH;
                case SOUTH: return NORTH;
                case EAST: return WEST;
                case WEST: return EAST;
            }
            return NORTH;
        }
    }

    private static class RoomNode {
        final int gx;
        final int gy;
        final Room room;

        RoomNode(int gx, int gy, Room room) {
            this.gx = gx;
            this.gy = gy;
            this.room = room;
        }
    }

    private static class RoomConnection {
        final RoomNode from;
        final RoomNode to;
        final Direction dir;

        RoomConnection(RoomNode from, RoomNode to, Direction dir) {
            this.from = from;
            this.to = to;
            this.dir = dir;
        }
    }

    private int gridOriginX(int gx) {
        return OFFSET_X + gx * (ROOM_WIDTH + CORRIDOR_LEN_X);
    }

    private int gridOriginY(int gy) {
        return OFFSET_Y + gy * (ROOM_HEIGHT + CORRIDOR_LEN_Y);
    }

    private int currentStage = 1;

    // Kích thước phòng Bossfight cho Stage Boss (Stage 4)
    public static final int BOSS_STAGE = 4;
    public static final int BOSS_ROOM_WIDTH = 28;
    public static final int BOSS_ROOM_HEIGHT = 18;

    /**
     * Sinh bản đồ hầm ngục theo stage được chỉ định (1, 2, 3: Standard Procedural Grid, 4: Grand Boss Arena).
     *
     * @param stage Chỉ số stage trong Biome (1, 2, 3, hoặc 4).
     */
    public void generateStage(int stage) {
        this.currentStage = stage;
        if (stage == BOSS_STAGE) {
            generateBossStage();
        } else {
            generateProceduralDungeon();
        }
    }

    /**
     * Sinh bản đồ hầm ngục tiêu chuẩn theo mô hình lưới các phòng liền kề theo 4 hướng 12h, 3h, 6h, 9h:
     * - 1 Spawn Room (ở ô (0, 1))
     * - 3 đến 5 Combat Rooms bố trí liền kề theo các hướng 12h, 3h, 6h, 9h
     * - 1 Extract Room (ở cuối chuỗi)
     * - Mỗi cặp phòng kề nhau được nối bởi 1 hành lang thẳng tắp duy nhất, bọc tường và lắp blast doors.
     */
    public void generateProceduralDungeon() {
        int numCombat = ThreadLocalRandom.current().nextInt(3, 6);
        boolean success = false;
        int maxAttempts = 60;

        for (int attempt = 0; attempt < maxAttempts && !success; attempt++) {
            rooms.clear();
            combatRooms.clear();
            initializeEmptyWorld();

            RoomNode[][] grid = new RoomNode[GRID_COLS][GRID_ROWS];
            List<RoomNode> combatNodes = new ArrayList<>();
            List<RoomConnection> connections = new ArrayList<>();

            // 1. Đặt Spawn Room tại ô lưới (0, 1)
            int spawnCol = gridOriginX(0);
            int spawnRow = gridOriginY(1);
            spawnRoom = new Room(spawnCol, spawnRow, ROOM_WIDTH, ROOM_HEIGHT, Room.RoomType.SPAWN, "Deployment Staging Room");
            RoomNode spawnNode = new RoomNode(0, 1, spawnRoom);
            grid[0][1] = spawnNode;
            rooms.add(spawnRoom);

            // 2. Đặt Combat Arena 1 tại (1, 1) - luôn ở hướng 3h (Đông) của Spawn
            // để người chơi nhấn D đi thẳng vào buồng chiến đấu đầu tiên
            int c1Col = gridOriginX(1);
            int c1Row = gridOriginY(1);
            Room combat1 = new Room(c1Col, c1Row, ROOM_WIDTH, ROOM_HEIGHT, Room.RoomType.COMBAT, "Combat Arena 1");
            RoomNode c1Node = new RoomNode(1, 1, combat1);
            grid[1][1] = c1Node;
            combatRooms.add(combat1);
            rooms.add(combat1);
            combatNodes.add(c1Node);
            connections.add(new RoomConnection(spawnNode, c1Node, Direction.EAST));

            // 3. Sinh các phòng combat tiếp theo liền kề ở 12h, 3h, 6h, 9h
            boolean failedCombat = false;
            for (int i = 2; i <= numCombat; i++) {
                RoomNode sourceNode = null;
                Direction chosenDir = null;

                List<RoomNode> candidates = new ArrayList<>(combatNodes);
                java.util.Collections.reverse(candidates);

                for (RoomNode cand : candidates) {
                    List<Direction> validDirs = getValidAdjacentDirections(cand, grid);
                    if (!validDirs.isEmpty()) {
                        sourceNode = cand;
                        chosenDir = validDirs.get(ThreadLocalRandom.current().nextInt(validDirs.size()));
                        break;
                    }
                }

                if (sourceNode == null || chosenDir == null) {
                    failedCombat = true;
                    break;
                }

                int nextGx = sourceNode.gx + chosenDir.dx;
                int nextGy = sourceNode.gy + chosenDir.dy;
                int nextCol = gridOriginX(nextGx);
                int nextRow = gridOriginY(nextGy);

                Room nextCombat = new Room(nextCol, nextRow, ROOM_WIDTH, ROOM_HEIGHT, Room.RoomType.COMBAT, "Combat Arena " + i);
                RoomNode nextNode = new RoomNode(nextGx, nextGy, nextCombat);
                grid[nextGx][nextGy] = nextNode;
                combatRooms.add(nextCombat);
                rooms.add(nextCombat);
                combatNodes.add(nextNode);
                connections.add(new RoomConnection(sourceNode, nextNode, chosenDir));
            }

            if (failedCombat) {
                continue;
            }

            // 4. Đặt Extract Room ở ô lân cận theo 12h, 3h, 6h, 9h
            RoomNode extractSource = null;
            Direction extractDir = null;

            List<RoomNode> extractCandidates = new ArrayList<>(combatNodes);
            java.util.Collections.reverse(extractCandidates);

            for (RoomNode cand : extractCandidates) {
                List<Direction> validDirs = getValidAdjacentDirections(cand, grid);
                if (!validDirs.isEmpty()) {
                    extractSource = cand;
                    if (validDirs.contains(Direction.EAST)) {
                        extractDir = Direction.EAST;
                    } else {
                        extractDir = validDirs.get(ThreadLocalRandom.current().nextInt(validDirs.size()));
                    }
                    break;
                }
            }

            if (extractSource == null || extractDir == null) {
                continue;
            }

            int extGx = extractSource.gx + extractDir.dx;
            int extGy = extractSource.gy + extractDir.dy;
            int extCol = gridOriginX(extGx);
            int extRow = gridOriginY(extGy);

            extractRoom = new Room(extCol, extRow, ROOM_WIDTH, ROOM_HEIGHT, Room.RoomType.EXTRACT, "Evacuation Landing Zone");
            RoomNode extNode = new RoomNode(extGx, extGy, extractRoom);
            grid[extGx][extGy] = extNode;
            rooms.add(extractRoom);
            connections.add(new RoomConnection(extractSource, extNode, extractDir));

            // 4.5. Đặt Shop Room ở ô lân cận theo 12h, 3h, 6h, 9h
            // WHY: Cho phép Shop Room xuất hiện ở bất kỳ đâu trong stage (nhánh từ bất kỳ phòng Combat, Extract, hoặc Spawn)
            RoomNode shopSource = null;
            Direction shopDir = null;

            List<RoomNode> shopCandidates = new ArrayList<>();
            shopCandidates.addAll(combatNodes);
            shopCandidates.add(extNode);
            shopCandidates.add(spawnNode);
            // Xáo trộn ngẫu nhiên để vị trí phòng Shop phân tán đa dạng khắp màn chơi, không bị cố định dính liền vào Spawn
            java.util.Collections.shuffle(shopCandidates);

            for (RoomNode cand : shopCandidates) {
                List<Direction> validDirs = getValidAdjacentDirections(cand, grid);
                if (!validDirs.isEmpty()) {
                    shopSource = cand;
                    shopDir = validDirs.get(ThreadLocalRandom.current().nextInt(validDirs.size()));
                    break;
                }
            }

            if (shopSource == null || shopDir == null) {
                continue;
            }

            int shopGx = shopSource.gx + shopDir.dx;
            int shopGy = shopSource.gy + shopDir.dy;
            int shopCol = gridOriginX(shopGx);
            int shopRow = gridOriginY(shopGy);

            shopRoom = new Room(shopCol, shopRow, ROOM_WIDTH, ROOM_HEIGHT, Room.RoomType.SHOP, "Tactical Field Shop");
            RoomNode shopNode = new RoomNode(shopGx, shopGy, shopRoom);
            grid[shopGx][shopGy] = shopNode;
            rooms.add(shopRoom);
            connections.add(new RoomConnection(shopSource, shopNode, shopDir));

            // 5. Chạm trổ tất cả các phòng và bệ / cột bên trong
            for (Room r : rooms) {
                carveRoom(r);
            }

            // Bệ Spawn 4x4 tại tâm Spawn Room
            carvePad(spawnRoom.getGridX() + 5, spawnRoom.getGridY() + 3, 4, 4, TileType.SPAWN_ZONE);

            // Bệ Extract 4x4 tại tâm Extract Room
            carvePad(extractRoom.getGridX() + 5, extractRoom.getGridY() + 3, 4, 4, TileType.EXTRACT_ZONE);


            // 6. Tự động kết nối TẤT CẢ các phòng Combat ở cạnh nhau (theo 4 hướng 12h, 3h, 6h, 9h)
            // để người chơi hoàn toàn tự do trong lối di chuyển qua lại giữa các phòng chiến đấu
            for (int gx = 0; gx < GRID_COLS; gx++) {
                for (int gy = 0; gy < GRID_ROWS; gy++) {
                    RoomNode nodeA = grid[gx][gy];
                    if (nodeA == null || nodeA.room.getRoomType() != Room.RoomType.COMBAT) {
                        continue;
                    }

                    // Kiểm tra phòng combat lân cận bên phải (hướng 3h / EAST)
                    if (gx + 1 < GRID_COLS) {
                        RoomNode nodeB = grid[gx + 1][gy];
                        if (nodeB != null && nodeB.room.getRoomType() == Room.RoomType.COMBAT) {
                            addUniqueConnection(connections, nodeA, nodeB, Direction.EAST);
                        }
                    }

                    // Kiểm tra phòng combat lân cận bên dưới (hướng 6h / SOUTH)
                    if (gy + 1 < GRID_ROWS) {
                        RoomNode nodeC = grid[gx][gy + 1];
                        if (nodeC != null && nodeC.room.getRoomType() == Room.RoomType.COMBAT) {
                            addUniqueConnection(connections, nodeA, nodeC, Direction.SOUTH);
                        }
                    }
                }
            }

            // 7. Chạm trổ tất cả các hành lang thẳng tắp theo 4 hướng và lắp đặt blast doors
            for (RoomConnection conn : connections) {
                connectAdjacentRooms(conn.from.room, conn.to.room, conn.dir);
            }

            // 8. Sinh đồng thời cả Cover Cứng (Tactical Pillar) và Cover Tạm Thời (Destructible Crate)
            // Tuyệt đối không sinh tại lối ra vào hoặc tâm phòng để tránh tắc nghẽn gameplay
            for (Room cr : combatRooms) {
                spawnSolidCovers(cr);
                int coverCount = ThreadLocalRandom.current().nextInt(3, 6);
                spawnRandomDestructibleCovers(cr, coverCount);
            }

            this.combatRoom = combatRooms.get(0);
            success = true;
        }
    }

    private void addUniqueConnection(List<RoomConnection> connections, RoomNode from, RoomNode to, Direction dir) {
        for (RoomConnection c : connections) {
            if ((c.from == from && c.to == to) || (c.from == to && c.to == from)) {
                return;
            }
        }
        connections.add(new RoomConnection(from, to, dir));
    }

    private List<Direction> getValidAdjacentDirections(RoomNode node, RoomNode[][] grid) {
        List<Direction> validDirs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            int nx = node.gx + dir.dx;
            int ny = node.gy + dir.dy;
            if (nx >= 0 && nx < GRID_COLS && ny >= 0 && ny < GRID_ROWS && grid[nx][ny] == null) {
                validDirs.add(dir);
            }
        }
        return validDirs;
    }

    private void carvePad(int startCol, int startRow, int width, int height, TileType type) {
        for (int c = startCol; c < startCol + width; c++) {
            for (int r = startRow; r < startRow + height; r++) {
                if (isValidGrid(c, r)) {
                    tiles[c][r].setType(type);
                }
            }
        }
    }

    /**
     * Nối 2 phòng liền kề bằng 1 hành lang thẳng tắp theo đúng hướng dir (12h, 3h, 6h, 9h).
     */
    private void connectAdjacentRooms(Room from, Room to, Direction dir) {
        if (dir == Direction.EAST) {
            int exitCol = from.getGridX() + ROOM_WIDTH - 1;
            int entryCol = to.getGridX();
            int startRow = from.getGridY() + 3;
            int endRow = from.getGridY() + 6;

            for (int c = exitCol; c <= entryCol; c++) {
                for (int r = startRow; r <= endRow; r++) {
                    tiles[c][r].setType(TileType.FLOOR);
                }
            }

            for (int r = startRow; r <= endRow; r++) {
                if (from.getRoomType() == Room.RoomType.COMBAT) {
                    installDoorTile(from, exitCol, r);
                } else if (from.getRoomType() == Room.RoomType.SHOP) {
                    installShopDoorTile(from, exitCol, r);
                }

                if (to.getRoomType() == Room.RoomType.COMBAT) {
                    installDoorTile(to, entryCol, r);
                } else if (to.getRoomType() == Room.RoomType.SHOP) {
                    installShopDoorTile(to, entryCol, r);
                }
            }

            for (int c = exitCol; c <= entryCol; c++) {
                if (tiles[c][startRow - 1].getType() == TileType.VOID) {
                    tiles[c][startRow - 1].setType(TileType.WALL);
                }
                if (tiles[c][endRow + 1].getType() == TileType.VOID) {
                    tiles[c][endRow + 1].setType(TileType.WALL);
                }
            }

        } else if (dir == Direction.WEST) {
            connectAdjacentRooms(to, from, Direction.EAST);

        } else if (dir == Direction.SOUTH) {
            int exitRow = from.getGridY() + ROOM_HEIGHT - 1;
            int entryRow = to.getGridY();
            int startCol = from.getGridX() + 5;
            int endCol = from.getGridX() + 8;

            for (int r = exitRow; r <= entryRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    tiles[c][r].setType(TileType.FLOOR);
                }
            }

            for (int c = startCol; c <= endCol; c++) {
                if (from.getRoomType() == Room.RoomType.COMBAT) {
                    installDoorTile(from, c, exitRow);
                } else if (from.getRoomType() == Room.RoomType.SHOP) {
                    installShopDoorTile(from, c, exitRow);
                }

                if (to.getRoomType() == Room.RoomType.COMBAT) {
                    installDoorTile(to, c, entryRow);
                } else if (to.getRoomType() == Room.RoomType.SHOP) {
                    installShopDoorTile(to, c, entryRow);
                }
            }

            for (int r = exitRow; r <= entryRow; r++) {
                if (tiles[startCol - 1][r].getType() == TileType.VOID) {
                    tiles[startCol - 1][r].setType(TileType.WALL);
                }
                if (tiles[endCol + 1][r].getType() == TileType.VOID) {
                    tiles[endCol + 1][r].setType(TileType.WALL);
                }
            }

        } else if (dir == Direction.NORTH) {
            connectAdjacentRooms(to, from, Direction.SOUTH);
        }
    }

    /**
     * Installs an open door tile at the given grid coordinates and registers it with the room.
     */
    private void installDoorTile(Room room, int col, int row) {
        if (isValidGrid(col, row)) {
            Tile doorTile = tiles[col][row];
            doorTile.setType(TileType.DOOR_OPEN); // Default open until lockdown triggers
            room.addDoorTile(doorTile);
        }
    }

    /**
     * Lắp đặt cửa cổng Shop luôn mở (DOOR_SHOP) tại vị trí chỉ định và đăng ký vào shopRoom.
     */
    private void installShopDoorTile(Room room, int col, int row) {
        if (isValidGrid(col, row)) {
            Tile doorTile = tiles[col][row];
            doorTile.setType(TileType.DOOR_SHOP); // Luôn mở, nhận diện đặc trưng của Shop
            room.addDoorTile(doorTile);
        }
    }

    /**
     * Carves a rectangular room by setting boundary walls and interior floor tiles.
     */
    private void carveRoom(Room room) {
        int x = room.getGridX();
        int y = room.getGridY();
        int w = room.getWidthInTiles();
        int h = room.getHeightInTiles();

        for (int c = x; c < x + w; c++) {
            for (int r = y; r < y + h; r++) {
                if (c == x || c == x + w - 1 || r == y || r == y + h - 1) {
                    tiles[c][r].setType(TileType.WALL);
                } else {
                    tiles[c][r].setType(TileType.FLOOR);
                }
            }
        }
    }

    /**
     * Carves a solid cover barrier within a room.
     */
    private void carvePillar(int startCol, int startRow, int width, int height) {
        for (int c = startCol; c < startCol + width; c++) {
            for (int r = startRow; r < startRow + height; r++) {
                if (isValidGrid(c, r)) {
                    tiles[c][r].setType(TileType.COVER);
                }
            }
        }
    }

    /**
     * Validates whether a grid index falls within the map matrix boundaries.
     */
    public boolean isValidGrid(int col, int row) {
        return col >= 0 && col < COLS && row >= 0 && row < ROWS;
    }

    /**
     * Returns the Tile object located at specific grid column and row.
     */
    public Tile getTile(int col, int row) {
        if (isValidGrid(col, row)) {
            return tiles[col][row];
        }
        return null;
    }

    /**
     * Performs an ultra-fast $O(1)$ spatial check to determine if an axis-aligned bounding box
     * (AABB) can move to the specified world position without colliding with walls, cover, or closed doors.
     */
    public boolean isAreaWalkable(double worldX, double worldY, double width, double height) {
        int minCol = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int maxCol = (int) Math.floor((worldX + width - 0.001) / GameConfig.TILE_SIZE);
        int minRow = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        int maxRow = (int) Math.floor((worldY + height - 0.001) / GameConfig.TILE_SIZE);

        for (int c = minCol; c <= maxCol; c++) {
            for (int r = minRow; r <= maxRow; r++) {
                if (!isValidGrid(c, r)) {
                    return false;
                }
                if (tiles[c][r].isSolid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines whether an entity's axis-aligned bounding box (AABB) overlaps any EXTRACT_ZONE tile.
     */
    public boolean isIntersectsExtractZone(double worldX, double worldY, double width, double height) {
        int minCol = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int maxCol = (int) Math.floor((worldX + width - 0.001) / GameConfig.TILE_SIZE);
        int minRow = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        int maxRow = (int) Math.floor((worldY + height - 0.001) / GameConfig.TILE_SIZE);

        for (int c = minCol; c <= maxCol; c++) {
            for (int r = minRow; r <= maxRow; r++) {
                if (isValidGrid(c, r) && tiles[c][r].getType() == TileType.EXTRACT_ZONE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a single world point falls directly upon an EXTRACT_ZONE tile.
     */
    public boolean isOnExtractZone(double worldX, double worldY) {
        int col = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int row = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        return isValidGrid(col, row) && tiles[col][row].getType() == TileType.EXTRACT_ZONE;
    }

    /**
     * Determines whether an area blocks or absorbs flying projectiles.
     */
    public boolean isAreaBlockingProjectiles(double worldX, double worldY, double width, double height) {
        int minCol = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int maxCol = (int) Math.floor((worldX + width - 0.001) / GameConfig.TILE_SIZE);
        int minRow = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        int maxRow = (int) Math.floor((worldY + height - 0.001) / GameConfig.TILE_SIZE);

        for (int c = minCol; c <= maxCol; c++) {
            for (int r = minRow; r <= maxRow; r++) {
                if (!isValidGrid(c, r)) {
                    return true;
                }
                if (tiles[c][r].blocksProjectiles()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether a direct, unobstructed line-of-sight exists between two world points.
     *
     * <p>WHY: Enables tactical ranged AI entities to determine whether they can fire at the player
     * without having bullets immediately absorbed by solid walls, doors, or destructible cover.
     *
     * @param x1 Origin world X coordinate.
     * @param y1 Origin world Y coordinate.
     * @param x2 Target world X coordinate.
     * @param y2 Target world Y coordinate.
     * @return True if no projectile-blocking tiles obstruct the ray; false otherwise.
     */
    public boolean hasLineOfSight(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.hypot(dx, dy);
        if (dist <= 0.001) return true;

        // Sample along ray with step size half a tile (16px) for fast, robust raymarching
        double stepSize = GameConfig.TILE_SIZE / 2.0;
        int steps = (int) Math.ceil(dist / stepSize);
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            double cx = x1 + dx * t;
            double cy = y1 + dy * t;
            if (isAreaBlockingProjectiles(cx - 2, cy - 2, 4, 4)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Renders visible tiles using Viewport Culling.
     */
    public void render(Graphics2D g, Camera camera) {
        int startCol = Math.max(0, (int) Math.floor(camera.getX() / GameConfig.TILE_SIZE));
        int endCol = Math.min(COLS - 1, (int) Math.ceil((camera.getX() + camera.getViewportWidth()) / GameConfig.TILE_SIZE));

        int startRow = Math.max(0, (int) Math.floor(camera.getY() / GameConfig.TILE_SIZE));
        int endRow = Math.min(ROWS - 1, (int) Math.ceil((camera.getY() + camera.getViewportHeight()) / GameConfig.TILE_SIZE));

        for (int c = startCol; c <= endCol; c++) {
            for (int r = startRow; r <= endRow; r++) {
                Tile tile = tiles[c][r];
                int screenX = camera.worldToScreenX(tile.getWorldX());
                int screenY = camera.worldToScreenY(tile.getWorldY());
                tile.render(g, screenX, screenY);
            }
        }

        renderRoomLabels(g, camera);
    }

    /**
     * Displays floating tactical room banners inside each chamber with lockdown warnings.
     */
    private void renderRoomLabels(Graphics2D g, Camera camera) {
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        double camCenterX = camera.getX() + camera.getViewportWidth() / 2.0;
        double camCenterY = camera.getY() + camera.getViewportHeight() / 2.0;

        for (Room room : rooms) {
            double rx = room.getCenterWorldX();
            double ry = room.getCenterWorldY();
            double distSq = (camCenterX - rx) * (camCenterX - rx) + (camCenterY - ry) * (camCenterY - ry);

            // Chỉ hiển thị nhãn của phòng khi camera thực sự ở gần buồng phòng đó (<= 480px)
            // Ngăn chặn hoàn toàn việc nhãn của phòng khác bị lọt mép vào màn hình
            if (distSq > 480 * 480) {
                continue;
            }

            int screenX = camera.worldToScreenX(room.getCenterWorldX() - 110);
            int screenY = camera.worldToScreenY(room.getGridY() * GameConfig.TILE_SIZE + 28);

            if (room.getRoomType() == Room.RoomType.SPAWN) {
                    g.setColor(new Color(60, 220, 160, 200));
                    g.drawString("[ " + room.getName() + " ]", screenX, screenY);
                } else if (room.getRoomType() == Room.RoomType.COMBAT) {
                    if (room.isLocked()) {
                        g.setColor(new Color(255, 60, 60, 240));
                        g.drawString("[ ! COMBAT LOCKDOWN ! ]", screenX, screenY);
                    } else if (room.isCleared()) {
                        g.setColor(new Color(60, 220, 120, 200));
                        g.drawString("[ CLEARED - DOORS OPEN ]", screenX, screenY);
                    } else {
                        g.setColor(new Color(255, 140, 60, 200));
                        g.drawString("[ " + room.getName() + " ]", screenX, screenY);
                    }
                } else if (room.getRoomType() == Room.RoomType.SHOP) {
                    g.setColor(new Color(255, 215, 60, 220));
                    g.drawString("[ TACTICAL FIELD SHOP ]", screenX, screenY);

                    // Hiển thị biển hiệu cổng vào Shop [ ★ SHOP (LUÔN MỞ) ★ ] ngay trên ngưỡng cửa
                    if (!room.getDoorTiles().isEmpty()) {
                        double avgDoorX = 0, avgDoorY = 0;
                        for (Tile dt : room.getDoorTiles()) {
                            avgDoorX += dt.getWorldX() + GameConfig.TILE_SIZE / 2.0;
                            avgDoorY += dt.getWorldY() + GameConfig.TILE_SIZE / 2.0;
                        }
                        avgDoorX /= room.getDoorTiles().size();
                        avgDoorY /= room.getDoorTiles().size();

                        int doorScreenX = camera.worldToScreenX(avgDoorX);
                        int doorScreenY = camera.worldToScreenY(avgDoorY - 14);

                        renderShopArchwayBadge(g, doorScreenX, doorScreenY, "★ CỬA SHOP (LUÔN MỞ) ★");
                    }
                } else {
                    g.setColor(new Color(190, 100, 255, 200));
                    g.drawString("[ " + room.getName() + " ]", screenX, screenY);
                }
        }
    }

    private void renderShopArchwayBadge(Graphics2D g, int centerX, int centerY, String text) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int pad = 8;
        int boxW = textW + pad * 2;
        int boxH = 18;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        g.setColor(new Color(25, 20, 10, 230));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.setColor(new Color(255, 215, 60, 220));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);
        g.drawString(text, boxX + pad, boxY + 13);
    }

    public Room getSpawnRoom() {
        return spawnRoom;
    }

    /**
     * Returns the primary / first combat arena for backward compatibility.
     */
    public Room getCombatRoom() {
        return combatRoom;
    }

    /**
     * Returns the complete list of procedural combat chambers (3 to 5 rooms).
     */
    public List<Room> getCombatRooms() {
        return combatRooms;
    }

    public Room getExtractRoom() {
        return extractRoom;
    }

    public Room getShopRoom() {
        return shopRoom;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * Sinh bản đồ cho Stage 3 (Bossfight Stage / Grand Arena):
     * - 1 Spawn Room chuẩn ở phía Tây (cột 4, hàng 17, 12x10 tiles)
     * - 1 Tactical Shop Room ở phía Bắc Spawn Room (cột 4, hàng 4, 12x10 tiles)
     * - 1 Phòng Combat to (Grand Boss Arena, 28x18 tiles) ở trung tâm bản đồ
     * - Hành lang rộng kết nối thẳng từ Spawn Room sang Boss Arena có lắp blast door
     * - 4 cột trụ che chắn bố trí tại 4 góc phòng Boss
     * - Ban đầu CHƯA có bệ extract riêng; bệ extract sẽ xuất hiện ngay tại tâm phòng khi clear.
     */
    public void generateBossStage() {
        rooms.clear();
        combatRooms.clear();
        initializeEmptyWorld();

        // 1. Đặt Spawn Room ở Tây
        int spawnCol = 4;
        int spawnRow = 17;
        int spawnW = 12;
        int spawnH = 10;
        spawnRoom = new Room(spawnCol, spawnRow, spawnW, spawnH, Room.RoomType.SPAWN, "Deployment Staging Room");
        rooms.add(spawnRoom);
        carveRoom(spawnRoom);

        // Bệ Spawn 4x4 tại tâm Spawn Room
        carvePad(spawnCol + 4, spawnRow + 3, 4, 4, TileType.SPAWN_ZONE);

        // 2. Đặt Shop Room ở phía Bắc Spawn Room để người chơi mua đạn/súng trước khi đối đầu Boss
        int shopCol = 4;
        int shopRow = 4;
        int shopW = 12;
        int shopH = 10;
        shopRoom = new Room(shopCol, shopRow, shopW, shopH, Room.RoomType.SHOP, "Tactical Field Shop");
        rooms.add(shopRoom);
        carveRoom(shopRoom);

        // Nối hành lang thẳng từ Spawn Room lên Shop Room (hướng NORTH / SOUTH)
        int shopCorridorStartCol = spawnCol + 4;
        int shopCorridorEndCol = spawnCol + 7;
        int shopExitRow = shopRow + shopH - 1;
        int spawnEntryRow = spawnRow;

        for (int r = shopExitRow; r <= spawnEntryRow; r++) {
            for (int c = shopCorridorStartCol; c <= shopCorridorEndCol; c++) {
                tiles[c][r].setType(TileType.FLOOR);
            }
        }
        for (int r = shopExitRow; r <= spawnEntryRow; r++) {
            if (tiles[shopCorridorStartCol - 1][r].getType() == TileType.VOID) {
                tiles[shopCorridorStartCol - 1][r].setType(TileType.WALL);
            }
            if (tiles[shopCorridorEndCol + 1][r].getType() == TileType.VOID) {
                tiles[shopCorridorEndCol + 1][r].setType(TileType.WALL);
            }
        }

        // 3. Đặt Boss Arena to ở giữa bản đồ
        int bossCol = 24;
        int bossRow = 13;
        int bossW = BOSS_ROOM_WIDTH;
        int bossH = BOSS_ROOM_HEIGHT;
        Room bossRoom = new Room(bossCol, bossRow, bossW, bossH, Room.RoomType.COMBAT, "Grand Boss Arena");
        bossRoom.setTotalPhases(1);
        combatRooms.add(bossRoom);
        rooms.add(bossRoom);
        carveRoom(bossRoom);

        // 4 Cột trụ che chắn chiến thuật ở 4 góc xa của Boss Arena (không bao quanh lối vào)
        carvePillar(bossCol + 8, bossRow + 2, 2, 2);
        carvePillar(bossCol + bossW - 8, bossRow + 2, 2, 2);
        carvePillar(bossCol + 8, bossRow + bossH - 4, 2, 2);
        carvePillar(bossCol + bossW - 8, bossRow + bossH - 4, 2, 2);

        // Sinh ngẫu nhiên 8 ô cover tạm thời (thùng gỗ che chắn) trong Grand Boss Arena
        spawnRandomDestructibleCovers(bossRoom, 8);

        // 4. Nối hành lang thẳng từ Spawn Room sang Boss Arena (hướng EAST)
        int exitCol = spawnCol + spawnW - 1;
        int entryCol = bossCol;
        int corridorStartRow = spawnRow + 3;
        int corridorEndRow = spawnRow + 6;

        for (int c = exitCol; c <= entryCol; c++) {
            for (int r = corridorStartRow; r <= corridorEndRow; r++) {
                tiles[c][r].setType(TileType.FLOOR);
            }
        }

        // Lắp cửa Blast Door ở lối vào Boss Arena
        for (int r = corridorStartRow; r <= corridorEndRow; r++) {
            installDoorTile(bossRoom, entryCol, r);
        }

        // Tường bảo vệ hai bên hành lang nếu gặp VOID
        for (int c = exitCol; c <= entryCol; c++) {
            if (tiles[c][corridorStartRow - 1].getType() == TileType.VOID) {
                tiles[c][corridorStartRow - 1].setType(TileType.WALL);
            }
            if (tiles[c][corridorEndRow + 1].getType() == TileType.VOID) {
                tiles[c][corridorEndRow + 1].setType(TileType.WALL);
            }
        }

        this.combatRoom = bossRoom;
        this.extractRoom = null; // Chưa có phòng sơ tán độc lập, sẽ mở bệ ngay trong phòng sau khi clear
    }

    /**
     * Mở bệ sơ tán (Extract Pad) ngay tại tâm của một căn phòng cụ thể (dành cho Stage 3 Boss Arena).
     *
     * @param room Phòng cần kích hoạt bệ sơ tán.
     */
    public void spawnExtractZoneInRoom(Room room) {
        if (room == null) return;
        int padW = 4;
        int padH = 4;
        int startCol = room.getGridX() + (room.getWidthInTiles() - padW) / 2;
        int startRow = room.getGridY() + (room.getHeightInTiles() - padH) / 2;
        carvePad(startCol, startRow, padW, padH, TileType.EXTRACT_ZONE);
        this.extractRoom = room;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    /**
     * Kiểm tra xem ô gạch (c, r) có nằm trong phạm vi lối ra vào của phòng hay không.
     * Khu vực này bắt buộc phải thông thoáng 100%, tuyệt đối cấm đặt cover cứng hoặc cover tạm thời.
     *
     * @param room Phòng cần kiểm tra.
     * @param c Tọa độ cột lưới.
     * @param r Tọa độ hàng lưới.
     * @return true nếu ô này nằm gần lối ra vào hoặc tâm phòng.
     */
    public boolean isNearDoorway(Room room, int c, int r) {
        int rx = room.getGridX();
        int ry = room.getGridY();
        int rw = room.getWidthInTiles();
        int rh = room.getHeightInTiles();

        int midX = rx + rw / 2;
        int midY = ry + rh / 2;

        // 1. Kiểm tra trực tiếp khoảng cách đến tất cả các ô cửa thực tế đã lắp đặt
        for (Tile door : room.getDoorTiles()) {
            if (Math.abs(c - door.getCol()) + Math.abs(r - door.getRow()) <= 2) {
                return true;
            }
        }

        // 2. Vùng đệm lối vào phía Tây (9h): Cửa nằm ở giữa cạnh Tây
        if (c >= rx && c <= rx + 2 && Math.abs(r - midY) <= 2) {
            return true;
        }

        // 3. Vùng đệm lối vào phía Đông (3h): Cửa nằm ở giữa cạnh Đông
        if (c >= rx + rw - 3 && c <= rx + rw - 1 && Math.abs(r - midY) <= 2) {
            return true;
        }

        // 4. Vùng đệm lối vào phía Bắc (12h): Cửa nằm ở giữa cạnh Bắc
        if (r >= ry && r <= ry + 2 && Math.abs(c - midX) <= 2) {
            return true;
        }

        // 5. Vùng đệm lối vào phía Nam (6h): Cửa nằm ở giữa cạnh Nam
        if (r >= ry + rh - 3 && r <= ry + rh - 1 && Math.abs(c - midX) <= 2) {
            return true;
        }

        // 6. Khu vực tâm phòng: Dành cho giao tranh, bệ sơ tán và hòm tiếp tế
        if (Math.abs(c - midX) <= 1 && Math.abs(r - midY) <= 1) {
            return true;
        }

        return false;
    }

    /**
     * Sinh các ô cover cứng (Permanent Solid Cover) kiên cố tại các vị trí chiến thuật an toàn bên trong phòng.
     * Tuyệt đối không sinh ở gần bất kỳ cửa ra vào nào để tránh cản trở và gây tắc nghẽn gameplay.
     *
     * @param room Phòng chiến đấu cần bố trí cover cứng.
     */
    public void spawnSolidCovers(Room room) {
        if (room == null || room.getRoomType() != Room.RoomType.COMBAT) return;

        int rx = room.getGridX();
        int ry = room.getGridY();
        int rw = room.getWidthInTiles();
        int rh = room.getHeightInTiles();

        // Bố trí các cột trụ chiến thuật tại các góc phòng (cách xa lối đi cửa và tâm phòng)
        int[][] candidatePositions = {
            {rx + 3, ry + 2},
            {rx + rw - 4, ry + 2},
            {rx + 3, ry + rh - 3},
            {rx + rw - 4, ry + rh - 3}
        };

        for (int[] pos : candidatePositions) {
            int c = pos[0];
            int r = pos[1];
            if (isValidGrid(c, r) && tiles[c][r].getType() == TileType.FLOOR) {
                // Bắt buộc kiểm tra không nằm trong phạm vi lối ra vào của phòng
                if (!isNearDoorway(room, c, r)) {
                    tiles[c][r].setType(TileType.COVER);
                }
            }
        }
    }

    /**
     * Sinh các ô cover tạm thời (Destructible Cover) ngẫu nhiên bên trong phòng chiến đấu.
     * Tuyệt đối không sinh tại các ô lối ra vào hoặc khu vực tâm phòng.
     *
     * @param room Phòng cần sinh cover tạm thời.
     * @param count Số lượng ô cover tạm thời cần sinh.
     */
    public void spawnRandomDestructibleCovers(Room room, int count) {
        List<int[]> candidates = new ArrayList<>();
        int rx = room.getGridX();
        int ry = room.getGridY();
        int rw = room.getWidthInTiles();
        int rh = room.getHeightInTiles();

        for (int c = rx + 1; c < rx + rw - 1; c++) {
            for (int r = ry + 1; r < ry + rh - 1; r++) {
                if (isValidGrid(c, r) && tiles[c][r].getType() == TileType.FLOOR) {
                    if (!isNearDoorway(room, c, r)) {
                        candidates.add(new int[]{c, r});
                    }
                }
            }
        }

        java.util.Collections.shuffle(candidates);
        int spawned = 0;
        for (int[] pos : candidates) {
            if (spawned >= count) break;
            tiles[pos[0]][pos[1]].setType(TileType.DESTRUCTIBLE_COVER);
            spawned++;
        }
    }

    /**
     * Gây sát thương lên tất cả các ô cover tạm thời (Destructible Cover) trong vùng AABB chỉ định.
     *
     * @param worldX Tọa độ X bắt đầu của vùng (pixel).
     * @param worldY Tọa độ Y bắt đầu của vùng (pixel).
     * @param width Chiều rộng của vùng (pixel).
     * @param height Chiều cao của vùng (pixel).
     * @param damage Sát thương từ đạn hoặc vũ khí cận chiến.
     * @return true nếu có ít nhất 1 ô cover tạm thời nhận sát thương.
     */
    public boolean damageDestructibleCoverInArea(double worldX, double worldY, double width, double height, int damage) {
        int minCol = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int maxCol = (int) Math.floor((worldX + width - 0.001) / GameConfig.TILE_SIZE);
        int minRow = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        int maxRow = (int) Math.floor((worldY + height - 0.001) / GameConfig.TILE_SIZE);

        boolean damaged = false;
        for (int c = minCol; c <= maxCol; c++) {
            for (int r = minRow; r <= maxRow; r++) {
                if (isValidGrid(c, r) && tiles[c][r].isDestructible()) {
                    tiles[c][r].takeDamage(damage);
                    damaged = true;
                }
            }
        }
        return damaged;
    }

    /**
     * Gây sát thương lên ô cover tạm thời tại tọa độ thế giới xác định.
     */
    public boolean damageDestructibleCoverAt(double worldX, double worldY, int damage) {
        int col = (int) Math.floor(worldX / GameConfig.TILE_SIZE);
        int row = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        if (isValidGrid(col, row) && tiles[col][row].isDestructible()) {
            return tiles[col][row].takeDamage(damage);
        }
        return false;
    }

    /**
     * Resets the dungeon by regenerating a fresh procedural layout with new random rooms and corridors.
     */
    public void reset() {
        for (Room room : rooms) {
            room.reset();
        }
        this.currentStage = 1;
        generateStage(1);
    }
}

