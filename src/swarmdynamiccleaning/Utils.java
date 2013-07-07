/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.PolygonBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Elio A
 */
public class Utils {
    
    /** Draw a polygon with the number of given sides. */
    public static Polygon drawPolygon(Point2D pos, double size, int sides) {
        Polygon poly = new Polygon();
        for (int i = 0; i < sides; i++) {
            poly.getPoints().add((pos.getX() + size * Math.cos(i * 2 * Math.PI / sides)));
            poly.getPoints().add((pos.getY() + size * Math.sin(i * 2 * Math.PI / sides)));
        }
        poly.setOpacity(0.5);
        return poly;
    }
    
    /** Draw a rectangle from to given points. */
    public static Polygon drawPolygon(Point2D pos1, Point2D pos2, double size, Color color) {
        double x1 = pos1.getX();
        double y1 = pos1.getY();
        double x2 = pos2.getX();
        double y2 = pos2.getY();
        
        double m1 = (y1 - y2) / (x1 - x2);
        double m2 = - 1 / m1;

        double dx = Math.sqrt( Math.pow(size,2) / (1 + Math.pow(m2,2)) ) / 2;
        double dy = m2 * dx;

        // Without the following comparison, some of the sensor will appear inside the body of the agent.
        double x3 = y1 > y2? x1 - dx : x1 + dx;
        double y3 = y1 > y2? y1 - dy : y1 + dy;
        double x4 = y1 > y2? x2 - dx : x2 + dx;
        double y4 = y1 > y2? y2 - dy : y2 + dy;
        
        return PolygonBuilder.create()
                .points(x1, y1, x2, y2, x4, y4, x3, y3)
                .fill(color)
                .opacity(0.5)
                .build();
    }
    
    public static double randomBetween(int lowest, int highest){
        return new Random().nextInt(highest-lowest) + lowest;
    }
    
    public static double timelineRateFromFps(double baseRate, int baseFps, int newFps){
        return (newFps * baseRate) / baseFps;
    }
    
    public static Group createGrid(double paneHeight, double paneWidth, int cellSize){
        Group grid = new Group();
        double opacity = 0.2;
        
        for (int i = 0; i <= paneHeight/cellSize; i++) {
            Line line = LineBuilder.create()
                .startX(0).startY(i * cellSize)
                .endX(paneWidth).endY(i * cellSize)
                .stroke(Color.BLACK)
                .strokeWidth(0.5)
                .opacity(opacity)
                .build();
            
            grid.getChildren().add(line);
        }
        
        for (int i = 0; i <= paneWidth/cellSize; i++) {
            Line line = LineBuilder.create()
                .startX(i * cellSize).startY(0)
                .endX(i * cellSize).endY(paneHeight)
                .stroke(Color.BLACK)
                .strokeWidth(0.5)
                .opacity(opacity)
                .build();
            
            grid.getChildren().add(line);
        }
        
        return grid;
    }
    
    /** Draw a circle with the given radius at the given position.
     * @param pos Optional parameter. Can be null.
     */
    public static Circle drawCircle(Point2D pos, double radius){
        Circle c = CircleBuilder.create()
                .radius(radius)
                .opacity(0.7)
                .build();
        
        if (pos != null){
            c.setCenterX(pos.getX());
            c.setCenterY(pos.getY());
        }
        
        return c;
    }
    
    public static Rectangle drawRectangle(Point2D position, double size){
        return RectangleBuilder.create()
                .x(position.getX())
                .y(position.getY())
                .width(size)
                .height(size)
                .opacity(0.0)
                .build();
    }
    
    public static boolean collides(Circle c1, Circle c2){
        double dx = c2.getCenterX() - c1.getCenterX();// - (c1.getRadius()/2);
        double dy = c2.getCenterY() - c1.getCenterY();// - (c1.getRadius()/2);
        
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDist = c1.getRadius() + c2.getRadius();
        
        return (distance < minDist);
    }
    
    public static Point2D newPointInLine(Point2D p1, Point2D p2, double stepSize, boolean getAway){
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        
        double mag = Math.sqrt( Math.pow(dx, 2) + Math.pow(dy, 2));
        
        double x3 = getAway? p1.getX() - dx * stepSize / mag : p1.getX() + dx * stepSize / mag;
        double y3 = getAway? p1.getY() - dy * stepSize / mag : p1.getY() + dy * stepSize / mag;
        
        return new Point2D(x3, y3);
    }
    
    public static Point2D newPointInPerimeter(Point2D p1, Point2D center, double stepSize){
        double radious = p1.distance(center);// + (walkDistance/2);
        double deltaX = p1.getX() - center.getX();
        double deltaY = p1.getY() - center.getY();
        
        double t = Math.atan2(deltaY, deltaX);
        
        t = t < 0? t + (Math.PI*2) : t;
        t += 0.1;
        
        double x = center.getX() + radious * Math.cos(t);
        double y = center.getY() + radious * Math.sin(t);
        
        return new Point2D(x, y);
    }
    
    public static Point2D randomPosFrom(Point2D pos, double stepSize){
        Random r = new Random();
        
        double x = r.nextBoolean()? pos.getX() + Utils.randomBetween(0, (int)stepSize) : pos.getX() - Utils.randomBetween(0, (int)stepSize);
        double y = r.nextBoolean()? pos.getY() + Utils.randomBetween(0, (int)stepSize) : pos.getY() - Utils.randomBetween(0, (int)stepSize);
        
        return new Point2D(x, y);
    }
    
    public static void saveWorlds(ArrayList<World> worlds){
        JSONArray jarr = new JSONArray();
        
        for (World world : worlds) {
            jarr.add(world.toJSON());
        }
        
        try {
            FileWriter fw = new FileWriter("worlds.txt", false);
            jarr.writeJSONString(fw);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(SwarmWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ArrayList<World> loadWorlds(){
        ArrayList<World> worlds = new ArrayList<>();
        
        try {
            FileReader fr = new FileReader("worlds.txt");
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine();
            JSONArray jWorlds = (JSONArray)JSONValue.parse(s);
            
            for (Iterator iWorlds = jWorlds.iterator(); iWorlds.hasNext();) {
                JSONObject jWorld = (JSONObject)iWorlds.next();
                JSONArray jWallCells = (JSONArray)jWorld.get("wallCells");

                World world = new World( jWorld.get("name").toString() );

                for (Iterator iCells = jWallCells.iterator(); iCells.hasNext();) {
                    JSONObject jCell = (JSONObject)iCells.next();
                    world.getWallCells().add(new GridCell(SwarmGrid.CELL_SIZE, new Point2D((double)jCell.get("x"), (double)jCell.get("y")), null));
                }
                worlds.add(world);
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(SwarmWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return worlds;
    }
}
