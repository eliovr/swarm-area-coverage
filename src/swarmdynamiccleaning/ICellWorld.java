/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

/**
 *
 * @author Elio A
 */
public interface ICellWorld {
    public void spread(Circle area, double amount);
    
    public double evaporationRate();
}
