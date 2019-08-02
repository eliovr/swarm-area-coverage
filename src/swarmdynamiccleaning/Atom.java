package swarmdynamiccleaning;

import javafx.scene.shape.Circle;

/**
 *
 * @author Elio Ventocilla
 */
public abstract class Atom {
    public abstract void act();

    public abstract Circle collisionBounds();
}
