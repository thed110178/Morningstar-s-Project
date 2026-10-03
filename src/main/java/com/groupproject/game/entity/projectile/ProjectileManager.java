package com.groupproject.game.entity.projectile;

import com.groupproject.game.core.Camera;
import com.groupproject.game.dungeon.DungeonMap;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle, physics updates, wall collision, and rendering of all active projectiles.
 *
 * <p>WHY: Projectiles are spawned dynamically during weapon discharge and destroyed upon hitting
 * walls or targets. Modifying a standard {@link java.util.ArrayList} while iterating across it
 * triggers {@link java.util.ConcurrentModificationException}. By utilizing staged addition and
 * removal buffer lists ({@code pendingAdd}, {@code pendingRemove}), projectile operations remain
 * strictly thread-safe and deterministic.
 */
public class ProjectileManager {

    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Projectile> pendingAdd = new ArrayList<>();

    /**
     * Spawns a new projectile into the staging buffer to be integrated on the next update tick.
     *
     * @param projectile The bullet to add.
     */
    public synchronized void spawn(Projectile projectile) {
        if (projectile != null) {
            pendingAdd.add(projectile);
        }
    }

    /**
     * Updates positions of all flying bullets and checks for collisions against dungeon walls.
     *
     * @param deltaTime  Elapsed delta time in seconds.
     * @param dungeonMap Reference to the active dungeon terrain for wall collision testing.
     */
    public synchronized void update(double deltaTime, DungeonMap dungeonMap) {
        // Integrate new bullets from staging buffer
        if (!pendingAdd.isEmpty()) {
            projectiles.addAll(pendingAdd);
            pendingAdd.clear();
        }

        // Update positions and verify terrain collisions
        for (Projectile p : projectiles) {
            if (!p.isActive()) continue;

            p.update(deltaTime);

            // Test if the bullet collided with a solid dungeon wall or cover pillar
            if (dungeonMap != null) {
                // Bullet collision box: 4x4 px
                if (!dungeonMap.isAreaWalkable(p.getX() - 2, p.getY() - 2, 4, 4)) {
                    p.deactivate(); // Impact absorbed by terrain
                }
            }
        }

        // Purge expired or impacted projectiles
        projectiles.removeIf(p -> !p.isActive());
    }

    /**
     * Renders all active bullets onto the canvas.
     */
    public synchronized void render(Graphics2D g, Camera camera) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.render(g, camera);
            }
        }
    }

    /**
     * Returns an immutable snapshot list of currently active projectiles.
     */
    public synchronized List<Projectile> getActiveProjectiles() {
        return new ArrayList<>(projectiles);
    }

    /**
     * Clears all projectiles (useful when resetting game state).
     */
    public synchronized void clear() {
        projectiles.clear();
        pendingAdd.clear();
    }

    public synchronized int getCount() {
        return projectiles.size();
    }
}
