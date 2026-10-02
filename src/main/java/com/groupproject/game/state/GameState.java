package com.groupproject.game.state;

import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Graphics2D;

/**
 * Base abstract class for all discrete game states implementing the State Design Pattern.
 *
 * <p>WHY: Instead of scattering conditional statements (e.g. {@code if (isMenu) ... else if (isPlaying) ...})
 * across rendering and update loops, the State Pattern encapsulates the behavior, inputs, and graphics
 * of each game phase (Menu, Combat, Upgrade selection, Game Over) into dedicated, independent classes.
 * This directly satisfies the course's OOP Design and SOLID principles (Single Responsibility & Open/Closed).
 */
public abstract class GameState {

    /**
     * Reference to the state manager, enabling states to trigger transitions (e.g. Menu -> Playing).
     */
    protected final GameStateManager gsm;

    /**
     * Constructs a GameState tied to the central state manager context.
     *
     * @param gsm The parent state manager.
     */
    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    /**
     * Initializes or resets state-specific variables.
     * Called whenever this state becomes the active state in {@link GameStateManager#setState}.
     */
    public abstract void init();

    /**
     * Updates state logic and consumes keyboard/mouse inputs for this frame.
     *
     * @param deltaTime Elapsed delta time in seconds since the previous logic tick.
     * @param keyH      Snapshot of active keyboard keys.
     * @param mouseH    Snapshot of active mouse coordinates and buttons.
     */
    public abstract void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH);

    /**
     * Renders all visual elements belonging to this state onto the supplied Graphics context.
     *
     * @param g Hardware-accelerated 2D graphics canvas.
     */
    public abstract void render(Graphics2D g);
}
