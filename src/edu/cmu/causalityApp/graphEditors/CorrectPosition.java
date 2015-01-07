package edu.cmu.causalityApp.graphEditors;

import edu.cmu.causalityApp.component.Pos;

import java.util.HashMap;

/**
 * This class stores the coordinates of the variables in the correct graph, so
 * that the correct manipulated graph, hypothetical graph and hypothetical
 * manipulated graph can draw the variables in the same way as the correct
 * graph.
 *
 * @author mattheweasterday
 */
public class CorrectPosition {

    private static final CorrectPosition ME = new CorrectPosition();

    /**
     * @return the singleton instance of this class.
     */
    public static CorrectPosition getInstance() {
        return ME;
    }


    private final HashMap<String, Pos> correctPos;

    /**
     * @return the position of this variable as when it was in the correct graph.
     */
    public Pos getCorrectPos(String varName) {
        Pos coords = correctPos.get(varName);
        if (coords == null) return new Pos(-1, -1);
        return coords;
    }

    /**
     * Stores the position of this variable as it was in the correct graph.
     */
    public void setCorrectPos(String varName, Pos pos) {
        correctPos.put(varName, pos);
    }

    /**
     * Private constructor.
     */
    private CorrectPosition() {
        correctPos = new HashMap<String, Pos>();
    }

}
