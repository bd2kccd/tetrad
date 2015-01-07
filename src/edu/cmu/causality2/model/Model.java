package edu.cmu.causality2.model;

import edu.cmu.causality2.model.command.Command;
import edu.cmu.causality2.model.exercise.WorkedOnExercise;
import edu.cmu.causality2.model.experiment.Experiment;
import edu.cmu.causality2.model.experiment.ExperimentalSetup;
import edu.cmu.causality2.model.graph.ManipulatedGraph;
import edu.cmu.causality2.model.population.BayesJoint;
import edu.cmu.causality2.model.sample.Sample;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.util.IM;
import edu.cmu.tetrad.util.TetradMatrix;
import org.apache.commons.math3.stat.descriptive.moment.SemiVariance;

import java.util.*;

/**
 * Stores the basic model of the causality lab.
 */
public class Model {
    private WorkedOnExercise workedOnExercise;

    private List<Node> nodes;

    private LinkedList<IM> ims;
    private LinkedList<ExperimentalSetup> experimentalSetups;
    private LinkedList<Graph> hypotheticalGraph;
    private LinkedList<Sample> samples;

    private Map<Sample, IM> sampleIms;
    private Map<Sample, ExperimentalSetup> sampleExperimentalSetups;

    public Model(WorkedOnExercise exercise) {
        setWorkedOnExercise(exercise);
//        }
    }

    public void setWorkedOnExercise(WorkedOnExercise exercise) {
        ims = new LinkedList<IM>();
        experimentalSetups = new LinkedList<ExperimentalSetup>();
        hypotheticalGraph = new LinkedList<Graph>();
        samples = new LinkedList<Sample>();

        sampleIms = new HashMap<Sample, IM>();
        sampleExperimentalSetups = new HashMap<Sample, ExperimentalSetup>();

        ims.add(exercise.getExercise().getIm());

        this.nodes = getGraph().getNodes();
        this.workedOnExercise = exercise;

        for (Command command : workedOnExercise.getCommands()) {
            command.doIt();
        }

    }

    public WorkedOnExercise getWorkedOnExercise() {
        return workedOnExercise;
    }

    public void addIm(IM im) {
        if (getIm() instanceof BayesIm && !(im instanceof BayesIm)) {
            throw new IllegalArgumentException("Expecting a Bayes IM.");
        }
        else if (getIm() instanceof SemIm && !(im instanceof SemIm)) {
            throw new IllegalArgumentException("Expecting a SEM IM.");
        }

        if (im instanceof BayesIm) {
            BayesIm _im = (BayesIm) im;
            List<Node> nodes = _im.getBayesPm().getDag().getNodes();
            if (!verifyNodes(nodes)) {
                throw new IllegalArgumentException("Nodes wrong.");
            }
        }
        else if (im instanceof SemIm) {
            SemIm _im = (SemIm) im;
            List<Node> nodes = _im.getSemPm().getGraph().getNodes();
            if (!verifyNodes(nodes)) {
                throw new IllegalArgumentException("Nodes wrong.");
            }
        }
        else {
            throw new IllegalArgumentException("Expecting BayesIM or SemIm.");
        }

        ims.add(im);
    }

    public Graph getGraph() {
        Graph graph;

        if (getIm() instanceof BayesIm) {
            graph = ((BayesIm) getIm()).getDag();
        }
        else if (getIm() instanceof SemIm) {
            graph = ((SemIm) getIm()).getSemPm().getGraph();
        }
        else {
            throw new NullPointerException();
        }

        return graph;
    }

    public void addExperimentalSetup(ExperimentalSetup experimentalSetup) {
        verifyNodes(experimentalSetup.getGraph().getNodes());
        experimentalSetups.add(experimentalSetup);
    }

    public Sample drawSample(int sampleSize) {
        Sample sample = getExperiment().drawSample(sampleSize);
        samples.add(sample);
        sampleIms.put(sample, getIm());
        sampleExperimentalSetups.put(sample, getExperimentalSetup());
        return sample;
    }

    public void addHypotheticalGraph(Graph graph) {
        hypotheticalGraph.add(graph);
    }

    public IM getIm() {
        return ims.getLast();
    }

    public ExperimentalSetup getExperimentalSetup() {
        return experimentalSetups.getLast();
    }

    public Experiment getExperiment() {
        return new Experiment(getIm(), getExperimentalSetup());
    }

    public List<Sample> getSamples() {
        List<Sample> samples = new ArrayList<Sample>();

        for (Sample sample : this.samples) {
            if (sampleIms.get(sample) == getIm() && sampleExperimentalSetups.get(sample) == getExperimentalSetup()) {
                samples.add(sample);
            }
        }

        return samples;
    }

    public Graph getManipulatedGraph() {
        IM manipulatedIm = getExperiment().getManipulatedIm();

        if (manipulatedIm instanceof BayesIm) {
            return ((BayesIm)manipulatedIm).getBayesPm().getDag();
        }
        else if (manipulatedIm instanceof SemIm) {
            SemGraph graph = ((SemIm) manipulatedIm).getSemPm().getGraph();
            graph.setShowErrorTerms(false);
            return new Dag(graph);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public BayesJoint getBayesPopulation() {
        BayesIm manipulatedIm = (BayesIm) getExperiment().getManipulatedIm();
        return new BayesJoint(manipulatedIm);
    }

    // Add in the means.
    public CovarianceMatrix getSemPopulation() {
        SemIm manipulatedIm = (SemIm) getExperiment().getManipulatedIm();
        TetradMatrix covariance = manipulatedIm.getImplCovar();
        List<Node> nodes = manipulatedIm.getVariableNodes();
//        int sampleSize = manipulatedIm.getSampleSize();
        CovarianceMatrix cov = new CovarianceMatrix(nodes, covariance, 100000); // This is supposed to be the population.
        return cov;
//        return new CorrelationMatrix(new CovarianceMatrix(cov));
    }

    public Graph getHypotheticalGraph() {
        return hypotheticalGraph.getLast();
    }

    public Graph getManipulatedHypotheticalGraph() {
        return new ManipulatedGraph(getHypotheticalGraph(), getExperimentalSetup());
    }

    public List<Node> getNodes() {
        return nodes;
    }


    //=====================================PRIVATE METHODS======================================//
    private boolean verifyNodes(List<Node> nodes) {
        return new HashSet<Node>(nodes).equals(new HashSet<Node>(getNodes()));
    }
}
