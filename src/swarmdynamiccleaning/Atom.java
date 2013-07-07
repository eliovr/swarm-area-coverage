/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import javafx.scene.shape.Circle;

/**
 *
 * @author Elio A
 */
public abstract class Atom {
    public abstract void act();
    
    public abstract Circle collisionBounds();
}
