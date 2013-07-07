/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import java.util.ArrayList;
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
    public static final int CELL_SIZE = 5, LEAK_AMOUNT = 150;
    
    // In charged of the main loop.
    private Timeline swarmLoop;
    // Looping fram within the timeline.
    private KeyFrame oneFrame;
    // List of agents interacting in the game.
    private ArrayList<Agent> agents;
    
    private GridCell[][] cells;
    
    private Group gridGroup;
    
    private Group cellsGroup;
    // Background grid. 
//    private SwarmGrid swarmGrid;
    // Keeps track of how many cicles have been executed.
    private int iterations;
    // If the simulation is running or not.
    private boolean running;
    
    private ArrayList<World> worlds;
    
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
    
    /** Initializes all elements. It is also used to reset elements if called for a second time. */
    private void initializeElements(){
//        leaks = new ArrayList<>();
        
        if (this.swarmLoop == null)
            initSwarmLoop();
        
        if (this.gridGroup == null)
            this.gridGroup = Utils.createGrid(getWorldPane().getWidth(), getWorldPane().getHeight(), CELL_SIZE);
        
        if (this.cellsGroup == null)
            this.cellsGroup = new Group();
//        else
//            this.cellsGroup.getChildren().clear();
        
        if (this.cells == null)
            this.cells = new GridCell[(int)(getWorldPane().getWidth()/CELL_SIZE)][(int)(getWorldPane().getHeight()/CELL_SIZE)];
        
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == null){
                    cells[i][j] = new GridCell(CELL_SIZE, new Point2D(i*CELL_SIZE, j*CELL_SIZE), this);
                    this.cellsGroup.getChildren().add(cells[i][j].getCell());
                }
                else
                    cells[i][j].setState(GridCell.BLANK);
            }
        }
        
//        if (swarmGrid == null)
//            swarmGrid = new SwarmGrid(getWorldPane().getWidth(), getWorldPane().getHeight(), displayGrid(), this);
//        else
//            swarmGrid.reset();
        
        getWorldPane().getChildren().clear();
        getWorldPane().getChildren().add(gridGroup);
        getWorldPane().getChildren().add(cellsGroup);
//        getWorldPane().getChildren().add(swarmGrid.getGrid());
        
        initAgents();
        
        iterations = 0;
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
                    
//                    updatePheromones();
//                    
//                    updateLeaks();
                    
                    updateExecutedFrames(++iterations);
                    
//                    time = Calendar.getInstance().getTimeInMillis() - time;
//                    System.out.println("Time: " + time);
            }
        }); 
        
        setGameLoop(TimelineBuilder.create()
                .cycleCount(Animation.INDEFINITE)
                .keyFrames(oneFrame)
                .build());
    }
    
    /** Initialize the agents acting in the game. The number of agents is given by the user.
     */
    private void initAgents(){
        agents = new ArrayList<>(getNumberOfAgents());
        
        for (int i = 0; i < getNumberOfAgents(); i++) {
            Agent agent = new Agent(getAgentsSize(), this);
            agents.add(agent);
            // Add the agent "body" to the world pane (main display).
            getWorldPane().getChildren().add(agent.getBody());
            
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
            
            // Live my minions!
            agent.act();
        }
        // Update how much space has been filled.
//        updateFilledSpace(swarmGrid.getFilledPercentage());
    }
    
    private void updateCells(){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j].act();
            }
        }
    }
    
//    private void updatePheromones() {
//        for (GridCell cell : swarmGrid.getPheromoneCells())
//            cell.act();
//    }
//    
//    private void updateLeaks(){
//        for (GridCell cell : swarmGrid.getLeakCells())
//            cell.act();
//    }
    
    /** Checks whether the bounds of the agent collide or not with a wall.
     * At first it would iterate through all those cell with the "wall" state but 
     * if there were too many then the program would become too slow. Going only through
     * those cells that exist around an agent proved to be more efficient.
     * @param agentBounds Collision bounds of the agent.
     * @return true if collides, false otherwise. */
    private boolean collidesWithWall(Circle agentBounds){
//        GridCell[][] cells = swarmGrid.getCells();
        int x = (int)((agentBounds.getCenterX()-agentBounds.getRadius())/SwarmGrid.CELL_SIZE);
        int y = (int)((agentBounds.getCenterY()-agentBounds.getRadius())/SwarmGrid.CELL_SIZE);
        int width = (int)((agentBounds.getRadius()*2)/SwarmGrid.CELL_SIZE);
        
        for (int i = x; (i <= width+x) && (i < cells.length); i++) {
            for (int j = y; (j <= width+y) && (j < cells[i].length); j++) {
                GridCell cell = cells[i][j];
                if (cell.isWall() && Utils.collides(agentBounds, cell.collisionBounds()) ){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    protected void updateFps(){
        if (swarmLoop != null)
            swarmLoop.setRate(Utils.timelineRateFromFps(1.0, 6, getFramesPerSecond()));
    }
    
    protected void showHideGrid(){
//        swarmGrid.setVisibility(displayGrid());
        gridGroup.setVisible(displayGrid());
    }
    
    public GridCell getCellAt(Point2D pos){
        int x = (int)(pos.getX()/CELL_SIZE);
        int y = (int)(pos.getY()/CELL_SIZE);
        
        try {
            return cells[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }
    
    protected void addRemoveBrick(Point2D pos, boolean add){
        GridCell cell = getCellAt(pos);
        if (cell != null)
            if (add)
                cell.setState(GridCell.WALL);
            else
                cell.setState(GridCell.BLANK);
    }
    
    protected void addLeakage(Point2D pos){
        GridCell cell = getCellAt(pos);
        if (cell != null && !cell.isWall()){
            cell.setState(GridCell.LEAK);
            cell.setAmount(LEAK_AMOUNT);
        }
    }
    
    protected boolean isWall(Point2D pos){
        return getCellAt(pos).isWall();
    }
    
    private void setGameLoop(Timeline loop) {
        this.swarmLoop = loop;
    }
    
    protected void saveWorlds(){
//        if (worlds == null)
//            worlds = new ArrayList<>();
//        
//        worlds.add(new World("test", swarmGrid.getWallCells()));
//        
//        Utils.saveWorlds(worlds);
    }
    
    protected ArrayList<World> getWorlds(){
        if (this.worlds == null){
            this.worlds = Utils.loadWorlds();
            this.worlds.add(new World("Empty"));
        }
        
        return worlds;
    }
    
    private void loadSelectedWorld(){
        World w = getSelectedWorld();
        if (w != null){
            for (GridCell gridCell : w.getWallCells()) {
                addRemoveBrick(gridCell.getPosition(), true);
            }
        }
    }
    
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
    
    protected boolean isRunning(){
        return running;
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
        int x = (int)((area.getCenterX()-area.getRadius())/CELL_SIZE);
        int y = (int)((area.getCenterY()-area.getRadius())/CELL_SIZE);
        int width = (int)((area.getRadius()*2)/CELL_SIZE);
        
        for (int i = x; (i <= width+x) && (i < cells.length); i++) {
            for (int j = y; (j <= width+y) && (j < cells[i].length); j++) {
                GridCell cell = cells[i][j];
                if ( Utils.collides(area, cell.collisionBounds()) ){
                    
                    if (cell.isLeak()){
                        double diff = pher - cell.getAmount();
                        cell.addAmount(-pher);
                        if (cell.getAmount() <= 0){
                            cell.setState(GridCell.PHERMONE);
                            cell.setAmount(diff);
                        }
                    }
                    else {
                        cell.setState(GridCell.PHERMONE);
                        cell.addAmount(pher);
                    }
                    
                }
            }
        }
    }
    
    @Override
    public double pheromoneLevelAt(Circle area){
        double pher = 0.0;
        int cellCount = 0;
        
        int x = (int)((area.getCenterX()-area.getRadius())/SwarmGrid.CELL_SIZE);
        int y = (int)((area.getCenterY()-area.getRadius())/SwarmGrid.CELL_SIZE);
        int width = (int)((area.getRadius()*2)/SwarmGrid.CELL_SIZE);
        
        for (int i = x; (i <= width+x) && (i < cells.length) && (i >= 0); i++) {
            for (int j = y; (j <= width+y) && (j < cells[i].length)  && (j >= 0); j++) {
                GridCell cell = cells[i][j];
                if ( Utils.collides(area, cell.collisionBounds()) ){
                    if (!cell.isLeak())
                        pher += cell.getAmount();
                    cellCount ++;
                }
            }
        }
        
        return (pher / cellCount);
    }
    
    @Override
    public double leakageLevelAt(Circle area){
        double leak = 0.0;
        int cellCount = 0;
        
        int x = (int)((area.getCenterX()-area.getRadius())/SwarmGrid.CELL_SIZE);
        int y = (int)((area.getCenterY()-area.getRadius())/SwarmGrid.CELL_SIZE);
        int width = (int)((area.getRadius()*2)/SwarmGrid.CELL_SIZE);
        
        for (int i = x; (i <= width+x) && (i < cells.length) && (i >= 0); i++) {
            for (int j = y; (j <= width+y) && (j < cells[i].length)  && (j >= 0); j++) {
                GridCell cell = cells[i][j];
                if ( Utils.collides(area, cell.collisionBounds()) ){
                    if (cell.isLeak())
                        leak += cell.getAmount();
                    cellCount ++;
                }
            }
        }
        if (cellCount <= 0)
            return 0.0;
        
        return (leak / cellCount);
    }
    
    @Override
    public void spread(Point2D pos, double amount) {
        ArrayList<GridCell> neighbours = new ArrayList<>();
        
        int x = (int)(pos.getX()/CELL_SIZE);
        int y = (int)(pos.getY()/CELL_SIZE);
        
        if (x > 0){
            if (!cells[x-1][y].isWall()) neighbours.add(cells[x-1][y]);
            if (y > 0)
                if (!cells[x-1][y-1].isWall()) neighbours.add(cells[x-1][y-1]);
            if (y < cells[x].length-1)
                if (!cells[x-1][y+1].isWall()) neighbours.add(cells[x-1][y+1]);
        }
        if (x < cells.length-1){
            if (!cells[x+1][y].isWall()) neighbours.add(cells[x+1][y]);
            if (y < cells[x].length-1)
                if (!cells[x+1][y+1].isWall()) neighbours.add(cells[x+1][y+1]);
            if (y > 0)
                if (!cells[x+1][y-1].isWall()) neighbours.add(cells[x+1][y-1]);
        }
        if (y > 0)
            if (!cells[x][y-1].isWall()) neighbours.add(cells[x][y-1]);
        if (y < cells[x].length-1)
            if (!cells[x][y+1].isWall()) neighbours.add(cells[x][y+1]);
        
        double share = amount/neighbours.size();
        
        for (GridCell neighbour : neighbours) {
            if (cells[x][y].isLeak()){
                if (!neighbour.isLeak()){
                    neighbour.setAmount(share);
                }else
                    neighbour.addAmount(share);
                
                neighbour.setState(GridCell.LEAK);
            }
            else if (!neighbour.isLeak()){
                if (!neighbour.isPheromone())
                    neighbour.setAmount(amount);
                else
                    neighbour.addAmount(share);
                
                neighbour.setState(GridCell.PHERMONE);
            }
        }
    }

    @Override
    public double evaporationRate(){
        return getEvaporation()/100;
    }
}
