package swarmdynamiccleaning;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

/**
 *
 * @author Elio Ventocilla
 */
public interface ICellWorld {
    public void spread(Circle area, double amount);

    public double evaporationRate();
}
