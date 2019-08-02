package swarmdynamiccleaning;

import java.util.ArrayList;
import javafx.scene.shape.Circle;

/**
 * Interface between the world and an agent.
 * Defines what an agent can perceive from the world.
 *
 * @author Elio Ventocilla
 */
public interface IAgentWorld {
    abstract ArrayList<Agent> getAgents();

    /** Stated whether the given area is allowed to be visited or not (i.e. false if the area collides with a wall).
     */
    abstract boolean isAllowed(Circle area);

    /** Drop pheromones in the given area.
     * The idea is to go through only those grid cells around the agent so as not to
     * run through all of them.
     * @param area Area where the pheromones should be dropped.
     * @param pher Amount of pheromones to be dropped in that area.
     * Should be a number between 0 and 1. 1 is the maximum amount of pheromones an area can have,
     * more than that will make no difference.
     */
    abstract void dropPheromones(Circle area, double pher);

    /** Level of pheromones found in the given area. */
    abstract double pheromoneLevelAt(Circle area);

    /** Level of leakage found in the given area. */
    abstract double leakageLevelAt(Circle area);
}
