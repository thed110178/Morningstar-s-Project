package com.groupproject.game.core;

/**
 * Global game constants, screen specifications, and balancing parameters.
 *
 * <p>WHY: Centralizing all magic numbers into a single immutable configuration class adheres
 * to the Single Source of Truth principle. This prevents scattering hard-coded screen resolutions,
 * target FPS limits, or player movement parameters across different modules, making gameplay
 * balance adjustments and window resizing straightforward to manage.
 *
 * <p>EDGE CASES: Marked as final with a private constructor to prevent accidental instantiation
 * or subclassing, as all members are static constants.
 */
public final class GameConfig {

    private GameConfig() {
        // Prevent instantiation of utility configuration class
    }

    // --- Window and Display Configuration ---

    /**
     * Window title displayed on the operating system frame.
     */
    public static final String GAME_TITLE = "DontKnow - An unplanned top-down roguelite shooter";

    /**
     * Default horizontal resolution matching standard 16:9 HD viewport.
     */
    public static final int SCREEN_WIDTH = 1280;

    /**
     * Default vertical resolution matching standard 16:9 HD viewport.
     */
    public static final int SCREEN_HEIGHT = 720;

    /**
     * Fullscreen toggle flag. Defaulted to false for developer convenience during debugging.
     */
    public static final boolean FULLSCREEN = false;

    // --- Performance and Threading Configuration ---

    /**
     * Target rendering frames per second.
     */
    public static final int TARGET_FPS = 60;

    /**
     * Target logic updates per second (Fixed Timestep).
     *
     * <p>WHY: Keeping UPS fixed at 60 ensures that physics calculations, bullet velocity,
     * and collision checks behave identically regardless of whether the player's monitor
     * runs at 60Hz, 144Hz, or experiences temporary rendering lag.
     */
    public static final int TARGET_UPS = 60;

    /**
     * Nanosecond duration of a single logic update tick (1,000,000,000 / 60 ≈ 16.66ms).
     */
    public static final long OPTIMAL_TIME_NS = 1_000_000_000L / TARGET_UPS;

    // --- World Dimension and Tile Configuration ---

    /**
     * Pixel dimension of a single square terrain tile.
     * Chosen as 48 to balance pixel art granularity with viewport visibility.
     */
    public static final int TILE_SIZE = 48;

    /**
     * Total world map width (80 tiles * 48px = 3840px), representing 3 screens wide.
     */
    public static final int WORLD_WIDTH = 3840;

    /**
     * Total world map height (45 tiles * 48px = 2160px), representing 3 screens high.
     */
    public static final int WORLD_HEIGHT = 2160;

    // --- Gameplay Balancing Parameters ---

    /**
     * Base movement velocity of the player in pixels per second.
     * Scaled by delta time during update to achieve smooth, framerate-independent locomotion.
     */
    public static final double PLAYER_DEFAULT_SPEED = 240.0;

    /**
     * Maximum health pool of the player operative before permadeath.
     */
    public static final int PLAYER_MAX_HEALTH = 100;

    /**
     * Energy shield absorbing incoming projectile damage before depleting health.
     */
    public static final int PLAYER_MAX_SHIELD = 50;

    /**
     * Energy / Mana reserve used for firing special firearms and weapon skills.
     */
    public static final int PLAYER_MAX_ENERGY = 200;
}
