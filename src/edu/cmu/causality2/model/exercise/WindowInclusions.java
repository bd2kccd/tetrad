package edu.cmu.causality2.model.exercise;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * A note as to whether each window is included and if so whether its hidden or not hidden.
 */
public class WindowInclusions implements TetradSerializable {
    static final long serialVersionUID = 23L;

    public WindowInclusions() {
        this(new Status(true, false),
                new Status(true, false),
                new Status(true, false),
                new Status(true, false),
                new Status(true, false),
                new Status(true, false),
                new Status(true, false),
                new Status(true, false));
    }

    // Windows that are "not included" are not shown in the interface. Windows
    // that are "hidable" are shown, but with a strike-through stamp that says
    // "Hidden"; at the end of their work on the exercise, the user may open these
    // windows and examine their contents. Windows that are "not hidable" are
    // shown at the beginning of the the user's work--the user may open these at
    // will.
    public static class Status implements TetradSerializable {
        static final long serialVersionUID = 23L;

        private boolean included = false;
        private boolean hidden = false;

        public Status(boolean incuded, boolean hidden) {
            this.included = incuded;
            this.hidden = hidden;
        }

        public boolean isIncluded() {
            return included;
        }

        public boolean isHidden() {
            return hidden;
        }

        /**
         * Generates a simple exemplar of this class to test serialization.
         *
         * @see edu.cmu.TestSerialization
         * @see edu.cmu.tetradapp.util.TetradSerializableUtils
         */
        public static Status serializableInstance() {
            return new Status(false, false);
        }
    }

    private Status trueGraph;
    private Status manipulatedTrueGraph;
    private Status population;
    private Status experimentalSetup;
    private Status sample;
    private Status hypotheticalGraph;
    private Status manipulatedHypotheticalGraph;
    private Status predictionsAndResults;

    public WindowInclusions(Status trueGraph,//
                            Status manipulatedTrueGraph, //
                            Status population,                //
                            Status experimentalSetup,
                            Status sample,
                            Status hypotheticalGraph,
                            Status manipulatedHypotheticalGraph,
                            Status predictionsAndResults) {
        if (experimentalSetup.isHidden()) throw new IllegalArgumentException("Cannot be hidden.");
        if (sample.isHidden()) throw new IllegalArgumentException("Cannot be hidden.");
        if (hypotheticalGraph.isHidden()) throw new IllegalArgumentException("Cannot be hidden.");
        if (manipulatedHypotheticalGraph.isHidden()) throw new IllegalArgumentException("Cannot be hidden.");
        if (predictionsAndResults.isHidden()) throw new IllegalArgumentException("Cannot be hidden.");

        if (!(trueGraph.included || experimentalSetup.isIncluded() || hypotheticalGraph.isIncluded())) {
            throw new IllegalArgumentException("One of true graph, exp setup, or hyp graph must be included");
        }

        if (!experimentalSetup.isIncluded()) {
            if (manipulatedTrueGraph.isIncluded() || manipulatedHypotheticalGraph.isIncluded()
                    || population.isIncluded() || experimentalSetup.isIncluded()) {
                throw new IllegalArgumentException("If exp setup is not included, none of man true graph, " +
                        "man hyp graph, pop, or exp setup may be included.");
            }
        }

        if (manipulatedHypotheticalGraph.isIncluded() && !hypotheticalGraph.isIncluded()) {
            throw new IllegalArgumentException("If man hyp graph, then hyp graph.");
        }

        if (predictionsAndResults.isIncluded() && !(sample.isIncluded() || hypotheticalGraph.isIncluded())) {
            throw new IllegalArgumentException("If pred & results then both sapoe and hyp graph");
        }

        this.trueGraph = trueGraph;
        this.manipulatedTrueGraph = manipulatedTrueGraph;
        this.population = population;
        this.experimentalSetup = experimentalSetup;
        this.sample = sample;
        this.hypotheticalGraph = hypotheticalGraph;
        this.manipulatedHypotheticalGraph = manipulatedHypotheticalGraph;
        this.predictionsAndResults = predictionsAndResults;
    }

    public Status getTrueGraph() {
        return trueGraph;
    }

    public Status getManipulatedTrueGraph() {
        return manipulatedTrueGraph;
    }

    public Status getPopulation() {
        return population;
    }

    public Status getExperimentalSetup() {
        return experimentalSetup;
    }

    public Status getSample() {
        return sample;
    }

    public Status getHypotheticalGraph() {
        return hypotheticalGraph;
    }

    public Status getManipulatedHypotheticalGraph() {
        return manipulatedHypotheticalGraph;
    }

    public Status getPredictionsAndResults() {
        return predictionsAndResults;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static WindowInclusions serializableInstance() {
        return new WindowInclusions(new Status(false, false),
                new Status(false, false),
                new Status(false, false),
                new Status(false, false),
                new Status(false, false),
                new Status(false, false),
                new Status(false, false),
                new Status(false, false)
        );
    }
}
