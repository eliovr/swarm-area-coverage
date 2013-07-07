/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import javafx.geometry.Point2D;

/**
 *
 * @author Elio A
 */
public interface ICellWorld {
    public void spread(Point2D pos, double amount);
    
    public double evaporationRate();
}
