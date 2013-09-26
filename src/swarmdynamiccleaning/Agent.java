/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swarmdynamiccleaning;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author Elio A
 */
public class Agent extends Atom{
    private Circle body;
    
    private double size;
    
    private IAgentWorld world;
    
    private double personalRange;
    
    private double comfortRange;
    
    private double flockRange;
    
    private double stepSize;
    
    private double pheromone;
    
    private double confidence;
    
    private double influence;
    
    private double inertia;
    
    private Point2D prevPos;
    
    public Agent (double size, IAgentWorld world){
        this.size = size;
        this.body = Utils.drawCircle(null, size);
        this.world = world;
        
        this.personalRange = 0.0;
        this.comfortRange = 0.0;
        this.flockRange = 0.0;
        this.stepSize = 0.0;
        this.pheromone = 0.0;
        this.confidence = 0.0;
        this.influence = 0.0;
        this.inertia = 0.0;
        
        this.prevPos = null;
    }
    
    @Override
    public void act(){
        Point2D newPos = getPosition();
        // Start by asuming I'm the most confident.
        Agent mostConfident = this;
        // Leave a trace where I am before moving somewhere else.
        world.dropPheromones(collisionBounds(), pheromone);
        
        for (Agent agent : world.getAgents()) {
            if (!agent.equals(this)){
                // This way of measuring coallision only works between agents because
                // it assumes that both objects are the same size.
                // If they're too close then...
                if (newPos.distance(agent.getPosition()) <= getPersonalRange()){
                    // ...get away.
                    newPos = Utils.newPointInLine(newPos, agent.getPosition(), stepSize, true);
                }
                // If they're within a reasonable distance then...
                else if (newPos.distance(agent.getPosition()) <= getComfortRange()){
                    // ...walk around it.
                    newPos = Utils.newPointInPerimeter(newPos, agent.getPosition(), stepSize);
                }
                // If they're getting too far then...
                else if (newPos.distance(agent.getPosition()) <= getFlockRange()){
                    // ...check who's the most confident.
                    if (agent.getConfidence() > mostConfident.getConfidence())
                        mostConfident = agent;
                }
            }
        }
        
        // If there's someone more confident than I am then...
        if (mostConfident != this){
            // ...move towards it.
            newPos = Utils.newPointInLine(newPos, mostConfident.getPosition(), stepSize, false);
            // ...and show the user that I'm not leading.
            body.setFill(Color.BLACK);
        }
        else {
            // ...just show the user I'm leading.
            body.setFill(Color.RED);
        }
        
        // If I'm still at the same position (the newPos is the same as my current position) then...
        if (newPos.distance(getPosition()) <= 0)
            // ...move to a random position.
            newPos = Utils.randomPosFrom(getPosition(), stepSize);
        
        newPos = applyInertia(newPos);
        
        // If the new position is allowed by the rules of the world then...
        if (world.isAllowed(Utils.drawCircle(newPos, size))){
            // ...move to the new position...
            moveTo(newPos);
            
            // ...and set my confidence.
            // Confidence is given by:
            // 1. How much % is without pheromones in the area covered by the agent.
            // 2. Plus a percentage of the confidence of the most confident agent.
            // 3. Just being able to move gives me plus 10% confidence.
            confidence = 
                    ( 1.0 - world.pheromoneLevelAt(Utils.drawCircle(newPos, getPersonalRange())) )+// * 0.3 +
//                    ( world.leakageLevelAt(collisionBounds()) ) +// * 0.6 +
                    ( mostConfident.getConfidence() * influence ) +
                    0.1;
        }
        else{
            // If the position is not allowed then I lose all my confidence.
            setConfidence(0.0);
            prevPos = null;
        }
    }
    
    @Override
    public Circle collisionBounds(){
        return Utils.drawCircle(getPosition(), size);
    }
    
    private void moveTo(Point2D pos){
        prevPos = getPosition();
        body.setTranslateX(pos.getX());
        body.setTranslateY(pos.getY());
    }
    
    /** Returns a new position as the result of the agent striving to move towards the a new position
     * while being influenced by the direction of its last movement.
     * Inertia is given by the following:
     * - A percentage of the distance between the last and the current position of the agent
     * towards the direction of the agents last movement;
     * - And percentage of the step size towards the new position it's striving for.
     */
    private Point2D applyInertia(Point2D newPos){
        Point2D finalPos = newPos;
        if (prevPos != null && inertia > 0.0){
            finalPos = Utils.newPointInLine(getPosition(), prevPos, prevPos.distance(getPosition()) * inertia, true);
//            finalPos = Utils.newPointInLine(getPosition(), prevPos, stepSize * inertia, true);
            finalPos = Utils.newPointInLine(finalPos, newPos, stepSize * (1.0 - inertia), false);
        }
        return finalPos;
    }
    
    public Node getBody() {
        return body;
    }
    
    public Point2D getPosition(){
        return new Point2D(body.getTranslateX(), body.getTranslateY());
    }
    
    // ====== Accesor methods that define the behaviour of the agent ===========
    
    public void setPheromone(double pheromone) {
        this.pheromone = pheromone;
    }

    public void setPersonalRange(double personalRange) {
        this.personalRange = personalRange;
    }

    public void setComfortRange(double comfortRange) {
        this.comfortRange = comfortRange;
    }

    public void setFlockRange(double flockRange) {
        this.flockRange = flockRange;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    public void setInfluence(double influence) {
        this.influence = influence;
    }
    
    public void setInertia(double inertia) {
        this.inertia = inertia;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    private double getPersonalRange() {
        return (size*2) + personalRange;
    }

    private double getComfortRange() {
        return this.getPersonalRange() + comfortRange;
    }

    private double getFlockRange() {
        return this.getComfortRange() + flockRange;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public double getInfluence() {
        return influence;
    }
}
