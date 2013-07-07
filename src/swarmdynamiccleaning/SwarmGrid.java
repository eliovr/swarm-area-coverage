/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.Group;

/**
 *
 * @author Elio A
 */
public class SwarmGrid {
    /** Size of each cell of the grid. */
    public static final int CELL_SIZE = 5;
    
    /** Matrix with all the cells. 
     * This variable represents de cells that have been visited (as well as those that haven't)
     * and those cells that represent a wall (obstacles for the agents). */
    private GridCell[][] cells;
    
    private ArrayList<GridCell> pheromoneCells;
    
    private ArrayList<GridCell> wallCells;
    
    private ArrayList<GridCell> leakCells;
    
    /** The drawn grid goes here. */
    private Group gridGroup;
    
    /** The drawn visited cells go here. */
    private Group pheromoneCellsGroup;
    
    /** The drawn wall cells go here. */
    private Group wallCellsGroup;
    
    private Group leakCellsGroup;
    
    public SwarmGrid(double width, double height, boolean isVisible, ICellWorld world){
        this.gridGroup = Utils.createGrid(width, height, CELL_SIZE);
        
        this.pheromoneCellsGroup = new Group();
        this.wallCellsGroup = new Group();
        this.leakCellsGroup = new Group();
        
        this.gridGroup.setVisible(isVisible);
        initGridCells(width, height, world);
        
        this.pheromoneCells = new ArrayList<>();
        this.wallCells = new ArrayList<>();
        this.leakCells = new ArrayList<>();
    }
    
    /** Clear all cells. Leave them "blank". */
    public void reset() {
        this.pheromoneCells.clear();
        this.wallCells.clear();
        this.leakCells.clear();
        
        this.pheromoneCellsGroup.getChildren().clear();
        this.wallCellsGroup.getChildren().clear();
        this.leakCellsGroup.getChildren().clear();
        
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j].setState(GridCell.BLANK);
            }
        }
    }
    
    /** Initialize the matrix of grid cells (set size and instantiate values). */
    private void initGridCells(double gridWidth, double gridHeight, ICellWorld world){
        this.cells = new GridCell[(int)(gridWidth/CELL_SIZE)][(int)(gridHeight/CELL_SIZE)];
        
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new GridCell(CELL_SIZE, new Point2D(i*CELL_SIZE, j*CELL_SIZE), world);
            }
        }
    }
    
    /** How much percentage of the grid have been filled by the agents. */
    public int getFilledPercentage(){
        double visited = pheromoneCells.size();
        double totalCells = (cells.length*cells[0].length) - wallCells.size();
        
        return (int)((visited/totalCells)*100);
    }
    
    /** Sets the visibility of the grid and the visited cells. 
     * Wall cells are always visible by default.
     */
    public void setVisibility(boolean isVisible){
        this.gridGroup.setVisible(isVisible);
//        this.visitedCellsGroup.setVisible(isVisible);
    }
    
    public void addPheromoneCell(GridCell cell){
        if (!cell.isPheromone()){
            if (cell.isLeak()){
                leakCells.remove(cell);
                leakCellsGroup.getChildren().remove(cell.getCell());
            }
            
            cell.setState(GridCell.PHERMONE);
            pheromoneCells.add(cell);
            pheromoneCellsGroup.getChildren().add(cell.getCell());
        }
    }
    
    public void addLeakedCell(GridCell cell){
        if (!cell.isWall()){
            if (cell.isPheromone()){
                pheromoneCells.remove(cell);
                pheromoneCellsGroup.getChildren().remove(cell.getCell());
            }
            if (!cell.isLeak()){
                cell.setState(GridCell.LEAK);
                leakCells.add(cell);
                leakCellsGroup.getChildren().add(cell.getCell());
            }
        }
    }
    
    /** Makes the given cell a wall. */
    public void newLeak(Point2D pos){
        GridCell cell = getCellAt(pos);
        if (cell != null){
            cell.setAmount(6.0);
            addLeakedCell(cell);
        }
    }
    
    /** Makes the given cell a wall. */
    public void newBrick(Point2D pos){
        GridCell cell = getCellAt(pos);
        if (cell != null)
            newBrick(cell);
    }
    
    private void newBrick(GridCell cell){
        if (!cell.isWall()){
            cell.setState(GridCell.WALL);
            wallCellsGroup.getChildren().add(cell.getCell());
            wallCells.add(cell);
        }
    }
    
    /** Makes the given cell a blank cell (empty space). */
    public void removeBrick(Point2D pos){
        GridCell cell = getCellAt(pos);
        if (cell.isWall()){
            cell.setState(GridCell.BLANK);
            wallCellsGroup.getChildren().remove(cell.getCell());
            wallCells.remove(cell);
        }
    }
    
    /**
     * @return the grid
     */
    public Group getGrid() {
        return new Group(gridGroup, pheromoneCellsGroup, wallCellsGroup, leakCellsGroup);
    }

    public GridCell[][] getCells(){
        return cells;
    }
    
    public ArrayList<GridCell> getPheromoneCells() {
        return pheromoneCells;
    }
    
    public ArrayList<GridCell> getWallCells() {
        return wallCells;
    }
    
    public ArrayList<GridCell> getLeakCells() {
        return leakCells;
    }
    
    public GridCell getCellAt(Point2D pos){
        int x = (int)(pos.getX()/SwarmGrid.CELL_SIZE);
        int y = (int)(pos.getY()/SwarmGrid.CELL_SIZE);
        
        try {
            return cells[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }
}
