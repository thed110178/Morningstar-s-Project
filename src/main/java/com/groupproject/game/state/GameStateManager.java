package com.groupproject.game.state;

import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

/**
 * Context manager orchestrating state transitions using the State Design Pattern.
 *
 * <p>WHY: Centralizes lifecycle control for application states and decouples individual states
 * from each other. Uses {@link EnumMap} for high-performance $O(1)$ state lookups with zero heap
 * allocations during state transitions.
 */
public class GameStateManager {

    /**
     * Enumeration defining all valid application states.
     */
    public enum StateType {
        MENU,
        PLAYING,
        PAUSE,
        UPGRADE,
        GAME_OVER
    }

    /**
     * Map caching registered state instances by their enum key.
     */
    private final Map<StateType, GameState> states = new EnumMap<>(StateType.class);

    /**
     * The currently active state type.
     */
    private StateType currentState;
    private MouseHandler mouseHandler;

    public GameStateManager() {
        // States will be registered via registerState()
    }

    /**
     * Binds mouse handler reference so state transitions can clear residual click states.
     */
    public void setMouseHandler(MouseHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
    }

    /**
     * Registers a new state instance under the specified state type key.
     *
     * @param type  The unique state enum identifier.
     * @param state The concrete GameState implementation.
     */
    public void registerState(StateType type, GameState state) {
        states.put(type, state);
    }

    /**
     * Switches the active state and automatically invokes {@link GameState#init()} on the new state.
     * Also resets mouse click flags to prevent click leakage between states.
     *
     * @param type The state to activate.
     */
    public void setState(StateType type) {
        this.currentState = type;
        if (mouseHandler != null) {
            mouseHandler.reset();
        }
        GameState state = states.get(type);
        if (state != null) {
            state.init();
        }
    }

    /**
     * Retrieves the instance of a registered state without changing the current active state.
     */
    public GameState getState(StateType type) {
        return states.get(type);
    }

    /**
     * Returns the type of the currently active state.
     */
    public StateType getCurrentStateType() {
        return currentState;
    }

    /**
     * Forwards logic updates and user inputs down to the active state.
     */
    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        GameState state = states.get(currentState);
        if (state != null) {
            state.update(deltaTime, keyH, mouseH);
        }
    }

    /**
     * Forwards rendering calls to the active state.
     */
    public void render(Graphics2D g) {
        GameState state = states.get(currentState);
        if (state != null) {
            state.render(g);
        }
    }
}
