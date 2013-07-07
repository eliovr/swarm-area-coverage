/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.json.simple.JSONObject;

/**
 *
 * @author Elio A
 */
public class GridCell extends Atom{
    
    public static final byte BLANK = 0, PHERMONE = 1, WALL = 2, LEAK = 3;
    
    private Node cell;
    
    private double size;
    
    private byte state;
    
    private Point2D position;
    
    private double amount;
    
    private ICellWorld world;
    
    public GridCell(double size, Point2D position, ICellWorld world){
        this.size = size;
        this.position = position;
        this.cell = Utils.drawRectangle(position, size);
        this.amount = 0.0;
        this.world = world;
        setState(BLANK);
    }
    
    @Override
    public void act(){
        switch (this.state){
            case PHERMONE:
                amount = (1.0 - world.evaporationRate()) * amount;
                cell.setOpacity(amount);
                break;
            case LEAK:
                double extra = amount - 1.0;
                if (extra > 1.0){
                    world.spread(position, extra);
                    amount = 1.0;
                }
                cell.setOpacity(amount);
                break;
        }
        
    }
    
    @Override
    public Circle collisionBounds(){
        double radious = size/2;
        return Utils.drawCircle(new Point2D(getPosition().getX()+radious, getPosition().getY()+radious), radious);
    }
    
    public Node getCell() {
        return cell;
    }
    
    public boolean isEmpty() {
        return state == BLANK;
    }
    
    public boolean isPheromone() {
        return state == PHERMONE;
    }
    
    public boolean isWall(){
        return state == WALL;
    }
    
    public boolean isLeak(){
        return state == LEAK;
    }
    
    public void setState(byte state){
        this.state = state;
        
        switch (state){
            case WALL: 
                ((Rectangle)cell).setFill(Color.BLACK);
                cell.setOpacity(1.0);
                break;
            case BLANK:
                cell.setOpacity(0.0);
            case PHERMONE:
                ((Rectangle)cell).setFill(Color.LIGHTSEAGREEN);
                break;
            case LEAK:
                ((Rectangle)cell).setFill(Color.GRAY);
                break;
        }
    }
    
    public double getAmount(){
        return this.amount;
    }
    
    public void setAmount(double amount){
        this.amount = amount;
        cell.setOpacity(amount);
    }
    
    public void addAmount(double amount){
        this.amount += amount;
        this.amount = this.amount < 0 ? 0 : this.amount;
        cell.setOpacity(amount);
    }
    
//    public double getPheromone(){
//        return cell.getOpacity();
//    }
    
    /** Adds pheromone to the cell
     * @param opacity Pheromone or leakage quantity to be added to the cell. 
     * Should be a number between 0 and 1 (exclusive).
     */
//    public void addOpacity(double opacity){
//        cell.setOpacity(cell.getOpacity() + opacity);
//    }
    
    /** Diminish the amount of pheromone in the cell.
     * @param er Represents the evaporation rate (percentage of pheromone that is to fade). 
     * Must be a number between 0 and 1.
     */
//    public void evaporatePheromone(double er){
//        if ( isPheromone() ){
//            double pher = (1.0 - er) * cell.getOpacity();
//            cell.setOpacity(pher);
//        }
//    }
    
    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("x", getPosition().getX());
        obj.put("y", getPosition().getY());
        return obj;
    }

    /**
     * @return the position
     */
    public Point2D getPosition() {
        return position;
    }
}
