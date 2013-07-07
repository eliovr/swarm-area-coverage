/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Elio A
 */
public class World {
    private String name;
    
    private ArrayList<GridCell> wallCells;
    
    public World(String name){
        this.name = name;
        this.wallCells = new ArrayList<>();
    }
    
    public World(String name, ArrayList<GridCell> wallCells){
        this.name = name;
        this.wallCells = wallCells;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the wallCells
     */
    public ArrayList<GridCell> getWallCells() {
        return wallCells;
    }

    /**
     * @param wallCells the wallCells to set
     */
    public void setWallCells(ArrayList<GridCell> wallCells) {
        this.wallCells = wallCells;
    }
    
    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        
        obj.put("name", name);
        for (GridCell gridCell : wallCells) {
            arr.add(gridCell.toJSON());
        }
        obj.put("wallCells", arr);
        return obj;
    }
    
    @Override
    public String toString(){
        return name;
    }
}
