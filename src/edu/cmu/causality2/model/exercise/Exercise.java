package edu.cmu.causality2.model.exercise;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.util.IM;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores a problem for the lab; student work is recorded in Exercise.
 *
 * @author Joseph Ramsey
 */
public class Exercise implements TetradSerializable {
    static final long serialVersionUID = 23L;

    private IM im;
    private WindowInclusions windowInclusions;
    private String instructions;
    private boolean resourcesLimited;
    private int resourceTotal;
    private int resourcePerObs;
    private int resourcePerInt;
    private Set<String> intervenableVariables;
    private String essayQuestion;

    public Exercise(IM im, WindowInclusions windowInclusions, String instructions, boolean resourcesLimited, int resourceTotal,
                    int resourcePerObs, int resourcePerInt, Set<String> intervenableVariables,
                    String essayQuestion) {
        if (im == null) throw new NullPointerException();
        if (!(im instanceof BayesIm || im instanceof SemIm)) throw new IllegalArgumentException();
        if (windowInclusions == null) throw new NullPointerException();
        if (instructions == null) throw new NullPointerException();
        if (resourceTotal < 0) throw new IllegalArgumentException();
        if (resourcePerObs < 0) throw new IllegalArgumentException();
        if (resourcePerInt < 0) throw new IllegalArgumentException();
        if (intervenableVariables == null) throw new NullPointerException();
        if (essayQuestion == null) throw new NullPointerException();

        this.im = im;
        this.windowInclusions = windowInclusions;
        this.instructions = instructions;
        this.resourcesLimited = resourcesLimited;
        this.resourceTotal = resourceTotal;
        this.resourcePerObs = resourcePerObs;
        this.resourcePerInt = resourcePerInt;
        this.intervenableVariables = intervenableVariables;
        this.essayQuestion = essayQuestion;
    }

    public Exercise(IM im) {
        this(im, new WindowInclusions(), "", false, 100, 5, 5, new HashSet<String>(), "");
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Exercise serializableInstance() {
        return new Exercise(MlBayesIm.serializableInstance(), WindowInclusions.serializableInstance(),
                "Instructions", false, 1000, 10, 10, new HashSet<String>(), "Essay");
    }

    public IM getIm() {
        return im;
    }

    public WindowInclusions getWindowInclusions() {
        return windowInclusions;
    }

    public String getInstructions() {
        return instructions;
    }

    public int getResourceTotal() {
        return resourceTotal;
    }

    public int getResourcePerObs() {
        return resourcePerObs;
    }

    public int getResourcePerInt() {
        return resourcePerInt;
    }

    public Set<String> getIntervenableVariables() {
        return intervenableVariables;
    }

    public String getEssayQuestion() {
        return essayQuestion;
    }
}
