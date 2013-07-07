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
public class Leak extends Atom{
    private static final double AMOUNT = 0.2;
    
    private ILeakWorld world;
    
    private byte times;
    
    private Point2D position;
    
    private double radious;
    
    public Leak(Point2D position, ILeakWorld world){
        this.times = 10;
        this.radious = 3;
        this.position = position;
        this.world = world;
    }
    
    @Override
    public void act(){
        if (this.times > 0){
            this.world.leakage(collisionBounds(), AMOUNT);
            this.times--;
            this.radious += 15;
        }
    }
    
    @Override
    public Circle collisionBounds(){
        return Utils.drawCircle(new Point2D(position.getX(), position.getY()), radious);
    }
}
