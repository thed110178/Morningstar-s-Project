package com.groupproject.game.state;

import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

/**
 * Manages game states and smooth transitions using the State Pattern.
 */
public class GameStateManager {

    public enum StateType {
        MENU,
        PLAYING,
        PAUSE,
        UPGRADE,
        GAME_OVER
    }

    private final Map<StateType, GameState> states = new EnumMap<>(StateType.class);
    private StateType currentState;

    public GameStateManager() {
        // States will be registered after instantiation
    }

    public void registerState(StateType type, GameState state) {
        states.put(type, state);
    }

    public void setState(StateType type) {
        this.currentState = type;
        GameState state = states.get(type);
        if (state != null) {
            state.init();
        }
    }

    public GameState getState(StateType type) {
        return states.get(type);
    }

    public StateType getCurrentStateType() {
        return currentState;
    }

    public void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH) {
        GameState state = states.get(currentState);
        if (state != null) {
            state.update(deltaTime, keyH, mouseH);
        }
    }

    public void render(Graphics2D g) {
        GameState state = states.get(currentState);
        if (state != null) {
            state.render(g);
        }
    }
}
