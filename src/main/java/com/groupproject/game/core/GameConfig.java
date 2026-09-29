package com.groupproject.game.core;

/**
 * Global game constants and configuration parameters.
 */
public final class GameConfig {

    private GameConfig() {
        // Prevent instantiation
    }

    // Window settings
    public static final String GAME_TITLE = "Soul Knight: Survivor - POBG Edition";
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final boolean FULLSCREEN = false;

    // Performance & Loop settings
    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 60; // Updates per second
    public static final long OPTIMAL_TIME_NS = 1_000_000_000L / TARGET_UPS;

    // World & Tile settings
    public static final int TILE_SIZE = 48; // 48x48 pixel tile
    public static final int WORLD_WIDTH = 3840;  // 80 tiles wide
    public static final int WORLD_HEIGHT = 2160; // 45 tiles high

    // Gameplay balance constants
    public static final double PLAYER_DEFAULT_SPEED = 240.0; // pixels per second
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int PLAYER_MAX_SHIELD = 50;
    public static final int PLAYER_MAX_ENERGY = 200;
}
