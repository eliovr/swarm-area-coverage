/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import java.util.ArrayList;
import java.util.Calendar;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 *
 * @author Elio A
 */
public abstract class SwarmWorld implements IAgentWorld, ICellWorld{
    /** Size of each cell of the grid. */
    public static final int LEAK_AMOUNT = 150;
    
    // In charged of the main loop.
    private Timeline swarmLoop;
    // Looping fram within the timeline.
    private KeyFrame oneFrame;
    // List of agents interacting in the game.
    private ArrayList<Agent> agents;
    // Matrix of cells. Cells are that part of the world that can be changed
    // either by the agents or the user.
    private GridCell[][] cells;
    // Background grid. Placed in a different group so that its visibility can be changed independently.
    private Group gridGroup;
    // Each cells node of each cell in the cells matrix goes here.
    private Group cellsGroup;
    // Keeps track of how many cicles have been executed.
    private int iterations;
    // If the simulation is running or not.
    private boolean running;
    // List of worlds that are loaded from the "worlds.txt" file and can be displayed.
    private ArrayList<World> worlds;
    // Number of cells that have been visited by agents.
    private int visitedCells;
    // Number of cells that are walls (used to calculate the filled percentage).
    private int wallCells;
    
    /** Starts the game.
     * Initializes elements if ran for the first time. No need to call anything else 
     * before this but be sure to implement the abstract methods (needed params are taken from them).
     */
    protected void play() {
        running = true;
        // If ran for the first time.
        if (agents == null)
            initializeElements();
        
        swarmLoop.play();
    }
    
    /** Pauses the game. Can be ran again using the play() method. */
    protected void pause(){
        running = false;
        swarmLoop.pause();
    }
    
    /** Stops the game (if running) and re-initializes all elements of the game. */
    protected void refresh(){
        running = false;
        if (swarmLoop == null)
            initSwarmLoop();
        
        swarmLoop.stop();
        initializeElements();
        loadSelectedWorld();
    }
    
    protected boolean isRunning(){
        return running;
    }
    
    /** Initializes all elements. It is also used to reset elements if called for a second time. */
    private void initializeElements(){
        if (this.swarmLoop == null)
            initSwarmLoop();
        
        if (this.gridGroup == null)
            this.gridGroup = Utils.createGrid(getWorldPane().getWidth(), getWorldPane().getHeight(), GridCell.CELL_SIZE);
        
        if (this.cellsGroup == null)
            this.cellsGroup = new Group();
        
        if (this.cells == null)
            this.cells = new GridCell[ (int)(getWorldPane().getWidth()/GridCell.CELL_SIZE) ][ (int)(getWorldPane().getHeight()/GridCell.CELL_SIZE) ];
        
        // Instantiate or resent each one of the cells.
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == null){
                    cells[i][j] = new GridCell( GridCell.CELL_SIZE, new Point2D( i*GridCell.CELL_SIZE, j*GridCell.CELL_SIZE ), this );
                    this.cellsGroup.getChildren().add(cells[i][j].getCell());
                }
                else{
                    cells[i][j].setState(GridCell.BLANK);
                    cells[i][j].setVisited(false);
                }
            }
        }
        
        getWorldPane().getChildren().clear();
        getWorldPane().getChildren().add(gridGroup);
        getWorldPane().getChildren().add(cellsGroup);
        
        initAgents();
        
        iterations = 0;
        visitedCells = 0;
        wallCells = 0;
    }
    
    /** Here is initialized the main loop of the game. 
     * The game starts when the method play() of this class is called.
     */
    private void initSwarmLoop() {
        Duration oneFrameAmt = Duration.millis(1000/getFramesPerSecond());
        oneFrame = new KeyFrame(oneFrameAmt,
            new EventHandler() {

            @Override
            public void handle(Event t) {
//                    long time = Calendar.getInstance().getTimeInMillis();
                    
                    updateAgents();
 
                    updateCells();
                    
                    updateExecutedFrames(++iterations);
                    
                    // Update how much space has been filled.
                    int filledPercentage = (visitedCells * 100) / ((cells.length * cells[0].length)-wallCells);
                    updateFilledSpace(filledPercentage);
                    
//                    time = Calendar.getInstance().getTimeInMillis() - time;
//                    System.out.println("Time: " + time);
            }
        }); 
        
        this.swarmLoop = TimelineBuilder.create()
                .cycleCount(Animation.INDEFINITE)
                .keyFrames(oneFrame)
                .build();
    }
    
    /** Initialize the agents acting in the game. The number of agents is given by the user.
     */
    private void initAgents(){
        int amount = getNumberOfAgents();
        double size = getAgentsSize();
        
        agents = new ArrayList<>(amount);
        
        for (int i = 0; i < amount; i++) {
            Agent agent = new Agent(size, this);
            agents.add(agent);
            
            // Add the agent "body" to the world pane (main display).
            getWorldPane().getChildren().add(agent.getBody());
            
            // Place in a random place.
            agent.getBody().setTranslateX(Utils.randomBetween(200, 400));
            agent.getBody().setTranslateY(Utils.randomBetween(200, 400));
        }
    }
    
    /** Updates the behaviour of the agents.
     * Values are taken from the implementation of some of the abstract methods of this class.
     */
    private void updateAgents() {
        for (Agent agent : agents) {
            // Update the behaviour of the agents.
            agent.setPersonalRange(getPersonalRange());
            agent.setComfortRange(getComfortRange());
            agent.setFlockRange(getFlockRange());
            agent.setStepSize(getStepSize());
            agent.setPheromone(getPheromone()/100);
            agent.setInertia(getInertia()/100);
            agent.setInfluence(getInfluence()/100);
            
            // Live minions!
            agent.act();
        }
    }
    
    /** Update every cell.
     * Each cell can "act" depending on the state they have, for example if a cell represents 
     * pheromones then it should evaporate each iteration. If a cell represents a wall then it 
     * shouldn't do anything.
     */
    private void updateCells(){
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[i].length; j++)
                cells[i][j].act();
    }
    
    private ArrayList<GridCell> getCellsInArea(Circle area){
        ArrayList<GridCell> arrCells = new ArrayList<>();
        
        int x = (int)((area.getCenterX()-area.getRadius())/GridCell.CELL_SIZE);
        int y = (int)((area.getCenterY()-area.getRadius())/GridCell.CELL_SIZE);
        int width = (int)((area.getRadius()*2)/GridCell.CELL_SIZE);
        
        for (int i = x; (i <= width+x) && (i < cells.length); i++) {
            for (int j = y; (j <= width+y) && (j < cells[i].length); j++) {
                if ( Utils.collides(area, cells[i][j].collisionBounds()) )
                    arrCells.add(cells[i][j]);
            }
        }
        
        return arrCells;
    }
    
    /** Checks whether the bounds of the agent collide or not with a wall.
     * At first it would iterate through all those cell with the "wall" state but 
     * if there were too many then the program would become too slow. Going only through
     * those cells that exist around an agent proved to be more efficient.
     * @param agentBounds Collision bounds of the agent.
     * @return true if collides, false otherwise. */
    private boolean collidesWithWall(Circle agentBounds){
        ArrayList<GridCell> arrCells = getCellsInArea(agentBounds);
        
        for (GridCell gridCell : arrCells) {
            if (gridCell.isWall())
                return true;
        }
        
        return false;
    }
    
    protected void updateFps(){
        if (swarmLoop != null)
            swarmLoop.setRate(Utils.timelineRateFromFps(1.0, 6, getFramesPerSecond()));
    }
    
    protected void showHideGrid(){
        gridGroup.setVisible(displayGrid());
    }
    
    public GridCell getCellAt(Point2D pos){
        int x = (int)(pos.getX()/GridCell.CELL_SIZE);
        int y = (int)(pos.getY()/GridCell.CELL_SIZE);
        
        try {
            return cells[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }
    
    protected void addRemoveWallAt(Point2D pos, boolean add){
        GridCell cell = getCellAt(pos);
        if (cell != null)
            if (add && !cell.isWall()){
                wallCells++;
                cell.setState(GridCell.WALL);
            }
            else if (!add && cell.isWall()){
                wallCells--;
                cell.setState(GridCell.BLANK);
            }
    }
    
    protected void addLeakAt(Point2D pos){
        GridCell cell = getCellAt(pos);
        if (cell != null && !cell.isWall()){
            cell.setState(GridCell.LEAK);
            cell.setAmount(LEAK_AMOUNT);
        }
    }
    
    protected boolean isWall(Point2D pos){
        GridCell cell = getCellAt(pos);
        
        if (cell != null)
            return getCellAt(pos).isWall();
        
        return false;
    }
    
    protected void saveWorlds(){
//        if (worlds == null)
//            worlds = new ArrayList<>();
//        
//        worlds.add(new World("test", swarmGrid.getWallCells()));
//        
//        Utils.saveWorlds(worlds);
    }
    
    /** Get existing worlds. 
     * If the worlds hasn't been loaded then load them from the "worlds.txt" file.
     */
    protected ArrayList<World> getWorlds(){
        if (this.worlds == null){
            this.worlds = Utils.loadWorlds();
            this.worlds.add(new World("Empty"));
        }
        
        return worlds;
    }
    
    /** Load the selected world into the screen. */
    private void loadSelectedWorld(){
        World w = getSelectedWorld();
        if (w != null){
//            wallCells = w.getWallCells().size();
            for (GridCell gridCell : w.getWallCells()) {
                addRemoveWallAt(gridCell.getPosition(), true);
            }
        }
    }
    
    @Override
    public ArrayList<Agent> getAgents() {
        return agents;
    }

    @Override
    public boolean isAllowed(Circle area){
        double x = area.getCenterX();
        double y = area.getCenterY();
        
        // Whether the point is within the boundaries of the world pane or not.
        // For some reason the inbuild method .contains() did not give the expected result.
        // The condition takes into account the size of the agents.
        return (x - area.getRadius() > 0 
                && x + area.getRadius() < getWorldPane().getWidth() 
                && y - area.getRadius() > 0 
                && y + area.getRadius() < getWorldPane().getHeight()
                && !collidesWithWall(area));
    }
    
    @Override
    public void dropPheromones(Circle area, double pher){
        // Drop pheromones (or cleaning product) over the following cells...
        for (GridCell cell : getCellsInArea(area)) {
            
            if (!cell.isVisited()){
                cell.setVisited(true);
                visitedCells++;
            }
            
            // if the cell is contaminatedd (leak) then
            if (cell.isLeak()){
                // clean it. The leaked cell will beacome a phermone cell if the 
                // amount of pheromones being placed is greater than the amount of leak in it.
                cell.cleanAmount(pher);
            }
            else {
                // make the cell a pheromone (in case it's blank)
                cell.setState(GridCell.PHERMONE);
                cell.addAmount(pher);
            }
        }
    }
    
    @Override
    public double pheromoneLevelAt(Circle area){
        double pher = 0.0;
        int cellCount = 0;
        
        for (GridCell cell : getCellsInArea(area)) {
            if (cell.isPheromone())
                pher += cell.getAmountPercentage();
            cellCount ++;
        }
        
        return (pher / cellCount);
    }
    
    @Override
    public double leakageLevelAt(Circle area){
        double leak = 0.0;
        int cellCount = 0;
        
        for (GridCell cell : getCellsInArea(area)) {
            if (cell.isLeak())
                leak += cell.getAmountPercentage();
            cellCount ++;
        }
        
        return (leak / cellCount);
    }
    
    @Override
    public void spread(Circle area, double amount) {
        GridCell core = getCellAt( new Point2D(area.getCenterX(), area.getCenterY()) );
        ArrayList<GridCell> arrCells = new ArrayList<>();
        
        // Reduce those cells in the area to those in the area which are not a wall.
        for (GridCell cell : getCellsInArea(area)) {
            if (!cell.isWall() && cell != core)
                arrCells.add(cell);
        }
        
        // Get the amount to be shared among those cells.
        double share = amount/arrCells.size();
        
        for (GridCell cell : arrCells) {
            // If a leak is been spread then...
            if (core.isLeak()){
                if (!cell.isLeak()){
                    cell.setAmount(share);
                }else
                    cell.addAmount(share);
                
                cell.setState(GridCell.LEAK);
            }
            // else pheromones are been spread.
            else if (!cell.isLeak()){
                if (!cell.isPheromone())
                    cell.setAmount(amount);
                else
                    cell.addAmount(share);
                
                cell.setState(GridCell.PHERMONE);
            }
        }
    }

    @Override
    public double evaporationRate(){
        return getEvaporation()/100;
    }
    
    // ====== Methods to be implemented by the controller ===================
    
    protected abstract Pane getWorldPane();
    
    protected abstract int getFramesPerSecond();
    
    protected abstract int getNumberOfAgents();
    
    protected abstract double getAgentsSize();
    
    protected abstract boolean displayGrid();
    
    protected abstract void updateFilledSpace(int percentage);
    
    protected abstract void updateExecutedFrames(int frames);
    
    protected abstract double getPersonalRange();
    
    protected abstract double getComfortRange();
    
    protected abstract double getFlockRange();
    
    protected abstract double getStepSize();
    
    protected abstract double getPheromone();
    
    protected abstract double getEvaporation();
    
    protected abstract double getInfluence();
    
    protected abstract double getInertia();
    
    protected abstract World getSelectedWorld();
    
    // ====================================================================
}
