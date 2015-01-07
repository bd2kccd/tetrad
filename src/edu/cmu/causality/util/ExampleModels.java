package edu.cmu.causality.util;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

import java.util.ArrayList;

/**
 * These are helper functions used by the JUnits to set up the model for testing.
 *
 * @author mattheweasterday
 */
public class ExampleModels {

    /**
     * @return a test Bayes IM.
     */
    public static MlBayesIm makeIm() {
        return makeIM(makePM(makeModel()));
    }

    /**
     * Creates a test model for JUNIT.
     *
     * @return a <code>Graph</code> model for testing.
     */
    private static Graph makeModel() {
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("happiness"));
        m1.addNode(new GraphNode("income"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        return m1;
    }

    /**
     * Makes a Bayes PM given a graph.
     *
     * @return a test Bayes PM.
     */
    private static BayesPm makePM(Graph aGraph) {
        BayesPm pm = new BayesPm(new Dag(aGraph));
        pm.setNumCategories(pm.getDag().getNode("education"), 3);

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


        return pm;
    }

    /**
     * Makes a Bayes IM given a PM
     *
     * @return test Bayes IM.
     */
    private static MlBayesIm makeIM(BayesPm aPm) {
        int i;
        MlBayesIm im = new MlBayesIm(aPm);
        //Latent
        i = im.getNodeIndex(im.getNode("Latent"));
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);

        //education
        i = im.getNodeIndex(im.getNode("education"));
        im.setProbability(i, 0, 0, 0.4);  //latent ==  education == college
        im.setProbability(i, 0, 1, 0.4);  //latent ==  education == high shcool
        im.setProbability(i, 0, 2, 0.2);  //latent ==  education == none
        im.setProbability(i, 1, 0, 0.5);  //latent ==  education == college
        im.setProbability(i, 1, 1, 0.3);  //latent ==  education == high shcool
        im.setProbability(i, 1, 2, 0.2);  //latent ==  education == none

        //happiness
        i = im.getNodeIndex(im.getNode("happiness"));
        im.setProbability(i, 0, 0, 0.9);  //education == college      happiness = true
        im.setProbability(i, 0, 1, 0.1);  //education == college      happiness = false
        im.setProbability(i, 1, 0, 0.6);  //education == high school  happiness = true
        im.setProbability(i, 1, 1, 0.4);  //education == high school  happiness = false
        im.setProbability(i, 2, 0, 0.2);  //education == none         happiness = true
        im.setProbability(i, 2, 1, 0.8);  //education == none         happiness = false

        //income
        i = im.getNodeIndex(im.getNode("income"));
        im.setProbability(i, 0, 0, 0.7); //education == college       income == high
        im.setProbability(i, 0, 1, 0.2); //education == college       income == medium
        im.setProbability(i, 0, 2, 0.1); //education == college       income == low
        im.setProbability(i, 1, 0, 0.4); //education == high school   income == high
        im.setProbability(i, 1, 1, 0.4); //education == high school   income == medium
        im.setProbability(i, 1, 2, 0.2); //education == high school   income == low
        im.setProbability(i, 2, 0, 0.1); //education == none          income == high
        im.setProbability(i, 2, 1, 0.4); //education == none          income == medium
        im.setProbability(i, 2, 2, 0.5); //education == none          income == low

        //System.out.println(im.toString());
        return im;
    }
}
