package edu.cmu.causality.independencies;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.sample.BayesSample;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

import java.util.ArrayList;

/**
 * @author mattheweasterday
 */
public class TestIndependencies extends junit.framework.TestCase {
    private ExperimentalSetup expVarStudied;

    public TestIndependencies(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        GraphNode tn;
        Dag m1 = new Dag();


        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("happiness"));
        m1.addNode(new GraphNode("income"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        expVarStudied = new ExperimentalSetup("experiment", m1);

        BayesPm pm = makePM(m1);

        new BayesSample(makeIM(pm), expVarStudied);
    }


    public void testGetStringCombinations() {
        String[] comb;

        //row 0
        comb = Independencies.getStringCombination(0, expVarStudied);
        assertTrue(comb[0].equals("education"));
        assertTrue(comb[1].equals("happiness"));
        assertTrue(comb.length == 2);

        //row 1
        comb = Independencies.getStringCombination(1, expVarStudied);
        assertTrue((comb[0]).equals("education"));
        assertTrue((comb[1]).equals("happiness"));
        assertTrue((comb[2]).equals("income"));
        assertTrue(comb.length == 3);

        //row 2
        comb = Independencies.getStringCombination(2, expVarStudied);
        assertTrue((comb[0]).equals("education"));
        assertTrue((comb[1]).equals("income"));
        assertTrue(comb.length == 2);

        //row 3
        comb = Independencies.getStringCombination(3, expVarStudied);
        assertTrue((comb[0]).equals("education"));
        assertTrue((comb[1]).equals("income"));
        assertTrue((comb[2]).equals("happiness"));
        assertTrue(comb.length == 3);

        //row 4
        comb = Independencies.getStringCombination(4, expVarStudied);
        assertTrue((comb[0]).equals("happiness"));
        assertTrue((comb[1]).equals("income"));
        assertTrue(comb.length == 2);

        //row 5
        comb = Independencies.getStringCombination(5, expVarStudied);
        assertTrue(comb[0].equals("happiness"));
        assertTrue(comb[1].equals("income"));
        assertTrue(comb[2].equals("education"));
        assertTrue(comb.length == 3);

    }


    public void testHiddenCombinations() {
        expVarStudied.getVariable("education").setStudied(false);
        assertFalse(expVarStudied.getVariable("education").isStudied());
        String[] comb;

        assertTrue(Independencies.getNumRows(expVarStudied) == 1);

        //row 0
        comb = Independencies.getStringCombination(0, expVarStudied);
        assertTrue(comb.length == 2);
        assertTrue((comb[0]).equals("happiness"));
        assertTrue((comb[1]).equals("income"));
    }

    public void testTooFewVariables() {
        expVarStudied.getVariable("education").setStudied(false);
        expVarStudied.getVariable("happiness").setStudied(false);

        assertTrue(Independencies.getNumRows(expVarStudied) == 0);
    }


    private BayesPm makePM(Graph aGraph) {
        BayesPm pm = new BayesPm(new Dag(aGraph));
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

    private BayesIm makeIM(BayesPm aPm) {
        int i;
        BayesIm im = new MlBayesIm(aPm);
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



