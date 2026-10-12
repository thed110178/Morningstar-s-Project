package com.groupproject.game.dungeon;

import java.awt.Color;

/**
 * Enumeration of all discrete tile classifications within the dungeon grid.
 *
 * <p>WHY: Categorizing terrain tiles through an enum provides type safety, eliminates magic
 * integer IDs, and encapsulates intrinsic properties (solidity for physics, base display color,
 * and semantic role) directly within the type definition.
 */
public enum TileType {

    /**
     * Impassable solid dungeon wall blocking entity locomotion and projectile trajectories.
     */
    WALL(true, new Color(45, 52, 70), "Wall"),

    /**
     * Walkable dungeon ground allowing free movement and bullet flight.
     */
    FLOOR(false, new Color(28, 33, 46), "Floor"),

    /**
     * Permanent solid cover pillar providing indestructible shelter.
     */
    COVER(true, new Color(70, 80, 105), "Tactical Cover"),

    /**
     * Temporary destructible cover crate / barrier absorbing bullets and breakable upon gunfire or melee strikes.
     */
    DESTRUCTIBLE_COVER(true, new Color(130, 95, 55), "Destructible Cover"),

    /**
     * Combat room blast door in locked state (impassable solid barrier).
     * Engaged when hostiles are active inside the combat arena.
     */
    DOOR_CLOSED(true, new Color(180, 40, 40), "Security Door (Locked)"),

    /**
     * Combat room blast door in open state (passable corridor).
     * Disengaged once all room hostiles have been neutralized.
     */
    DOOR_OPEN(false, new Color(40, 150, 90), "Security Door (Unlocked)"),

    /**
     * Designates the safe staging area where the player operative deploys into the mission.
     */
    SPAWN_ZONE(false, new Color(30, 60, 50), "Spawn Pad"),

    /**
     * Extraction zone gateway triggering successful level completion and next stage transition.
     */
    EXTRACT_ZONE(false, new Color(60, 45, 80), "Extraction Portal"),

    /**
     * Void area outside of all rooms and corridors. Impassable.
     */
    VOID(true, new Color(15, 18, 25), "Abyss");

    private final boolean solid;
    private final Color defaultColor;
    private final String displayName;

    /**
     * Constructs a TileType with its physical collision property and aesthetic color.
     *
     * @param solid        Whether entities and projectiles collide with this tile.
     * @param defaultColor Fallback render color before sprite textures are bound.
     * @param displayName  Human-readable debug identifier.
     */
    TileType(boolean solid, Color defaultColor, String displayName) {
        this.solid = solid;
        this.defaultColor = defaultColor;
        this.displayName = displayName;
    }

    /**
     * Returns true if this tile acts as a physical barrier preventing movement.
     */
    public boolean isSolid() {
        return solid;
    }

    /**
     * Returns the base decorative color for this tile.
     */
    public Color getDefaultColor() {
        return defaultColor;
    }

    /**
     * Returns the display name of this tile type.
     */
    public String getDisplayName() {
        return displayName;
    }
}
