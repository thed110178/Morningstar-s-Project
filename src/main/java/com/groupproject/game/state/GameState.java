package com.groupproject.game.state;

import com.groupproject.game.input.KeyboardHandler;
import com.groupproject.game.input.MouseHandler;
import java.awt.Graphics2D;

/**
 * Base abstract class for all game states (State Design Pattern).
 */
public abstract class GameState {

    protected final GameStateManager gsm;

    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    public abstract void init();

    public abstract void update(double deltaTime, KeyboardHandler keyH, MouseHandler mouseH);

    public abstract void render(Graphics2D g);
}
