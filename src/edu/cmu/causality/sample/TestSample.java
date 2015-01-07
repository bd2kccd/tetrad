package edu.cmu.causality.sample;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.population.BayesMarginalJoint;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

import java.util.ArrayList;

/**
 * @author Matthew Easterday
 */
public class TestSample extends junit.framework.TestCase {
    private Graph graph;
    private BayesPm pm;
    private MlBayesIm im;
    private ExperimentalSetup exp;
    private ExperimentalSetup expVarStudied;

    public TestSample(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        makeModel();
        makePM(graph);
        makeIM(pm);
        makeExperiment(graph, pm);
    }

    public void testGetSampleFrequencies() {
        double count1, count2;
        BayesSample aSample = new BayesSample(im, exp);
        BayesMarginalJoint marginalJoint = new BayesMarginalJoint(im, expVarStudied);

        for (int i = 0; i < aSample.getNumSampleValueCombinations(expVarStudied); i++) {
            count1 = marginalJoint.getProbability(i);
            count2 = aSample.getSampleCaseFrequency(i);
            assertEquals(count1, count2, .05);
        }
    }

    private void makeModel() {
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("happiness"));
        m1.addNode(new GraphNode("income"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        graph = m1;
    }

    private void makePM(Graph aGraph) {
        pm = new BayesPm(new Dag(aGraph));

        ArrayList<String> varVals = new ArrayList<String>();
        varVals.add("college");
        varVals.add("High school");
        varVals.add("none");
        pm.setCategories(pm.getDag().getNode("education"), varVals);

        varVals = new ArrayList<String>();
        varVals.add("high");
        varVals.add("medium");
        varVals.add("low");
        pm.setCategories(pm.getDag().getNode("income"), varVals);

        varVals = new ArrayList<String>();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("happiness"), varVals);

        varVals = new ArrayList<String>();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("Latent"), varVals);


    }

    private void makeIM(BayesPm aPm) {
        int i;
        im = new MlBayesIm(aPm);
        //Latent
        i = im.getNodeIndex(im.getNode("Latent"));
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);

        //education
        i = im.getNodeIndex(im.getNode("education"));
        im.setProbability(i, 0, 0, 0.3);
        im.setProbability(i, 0, 1, 0.3);
        im.setProbability(i, 0, 2, 0.4);
        im.setProbability(i, 1, 0, 0.3);
        im.setProbability(i, 1, 1, 0.3);
        im.setProbability(i, 1, 2, 0.4);

        //happiness
        i = im.getNodeIndex(im.getNode("happiness"));
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);
        im.setProbability(i, 1, 0, 0.5);
        im.setProbability(i, 1, 1, 0.5);
        im.setProbability(i, 2, 0, 0.5);
        im.setProbability(i, 2, 1, 0.5);

        //income
        i = im.getNodeIndex(im.getNode("income"));
        im.setProbability(i, 0, 0, 0.3);
        im.setProbability(i, 0, 1, 0.3);
        im.setProbability(i, 0, 2, 0.4);
        im.setProbability(i, 1, 0, 0.3);
        im.setProbability(i, 1, 1, 0.3);
        im.setProbability(i, 1, 2, 0.4);
        im.setProbability(i, 2, 0, 0.3);
        im.setProbability(i, 2, 1, 0.3);
        im.setProbability(i, 2, 2, 0.4);
    }

    private void makeExperiment(Graph aGraph, BayesPm aPm) {
        exp = new ExperimentalSetup("experiment", aGraph);
        exp.getVariable("happiness").setLocked("true");
        exp.getVariable("income").setRandomized();
        exp.getVariable("happiness").setLocked("true");
        expVarStudied = exp;
    }
}
