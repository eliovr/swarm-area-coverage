package swarmdynamiccleaning;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.json.simple.JSONObject;

/**
 *
 * @author Elio Ventocilla
 */
public class GridCell extends Atom{

    public static final byte BLANK = 0, PHERMONE = 1, WALL = 2, LEAK = 3;

    private static final double MIN_OPACITY = 0.2;

    public static final int CELL_SIZE = 5;

    private Rectangle cell;

    private byte state;

    private Point2D position;

    private double amount;

    private ICellWorld world;

    private boolean visited;

    public GridCell(double size, Point2D position, ICellWorld world){
        this.position = position;
        this.cell = Utils.drawRectangle(position, size);
        this.amount = 0.0;
        this.world = world;
        this.visited = false;
        setState(BLANK);
    }

    @Override
    public void act(){
        switch (this.state){
            case PHERMONE:
                if (visited && amount > 0){
                    amount = (1.0 - world.evaporationRate()) * amount;
//                    amount = amount - world.evaporationRate() ;
//
//                    if (amount < 0) amount = 0;

                    cell.setOpacity(amount > MIN_OPACITY? amount : MIN_OPACITY);
                }
                break;
            case LEAK:
                double extra = amount - 1.0;
                if (extra > 0.0){
                    world.spread(neigbourhoodArea(), extra);
                    amount = 1.0;
                }
                break;
        }
    }

    @Override
    public Circle collisionBounds(){
        double radious = CELL_SIZE/2;
        return Utils.drawCircle(new Point2D(position.getX()+radious, position.getY()+radious), radious);
    }

    private Circle neigbourhoodArea(){
        double radious = CELL_SIZE;
        double centerX = (CELL_SIZE/2) + position.getX();
        double centerY = (CELL_SIZE/2) + position.getY();

        return Utils.drawCircle(new Point2D(centerX, centerY), radious);
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
                cell.setFill(Color.BLACK);
                cell.setOpacity(1.0);
                break;
            case BLANK:
                cell.setOpacity(0.0);
                amount = 0.0;
                break;
            case PHERMONE:
                cell.setFill(Color.LIGHTSEAGREEN);
                break;
            case LEAK:
                cell.setFill(Color.GRAY);
                break;
        }
    }

    /** Gives the percentage of whatever it has (leak or pheromones).
     * Percentage levels are between 0 and 1 regardless of whether the amount is
     * greater than 1 or lower than 0, and that's why we use the cell opacity which,
     * for our convenience, will always be between 0 and 1.
     */
    public double getAmountPercentage(){
//        return cell.getOpacity();
        return amount < 1.0? amount : 1.0;
    }

    public void setAmount(double amount){
        this.amount = amount;
        cell.setOpacity(amount);
    }

    public void addAmount(double amount){
        this.amount += amount;
        cell.setOpacity(this.amount);
    }

    /** Subtracts the given amount from the current amount.
     * If the result is lower than 0 then it transforms the cell into a pheromone (assuming it's leak),
     * and sets the absolute value of the result into the amount. In other words, if
     * the "cleaning product" is more than the "contamination" then what remains is the former.
     */
    public void cleanAmount(double amount){
        this.amount -= amount;
        if (this.amount < 0){
            setState(PHERMONE);
            this.amount = Math.abs(this.amount);
        }
        cell.setOpacity(this.amount);
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("x", position.getX());
        obj.put("y", position.getY());
        return obj;
    }

    /**
     * @return the position
     */
    public Point2D getPosition() {
        return position;
    }

    /**
     * @return the visited
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
