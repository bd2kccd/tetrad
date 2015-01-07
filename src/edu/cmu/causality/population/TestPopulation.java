package edu.cmu.causality.population;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.manipulatedGraph.AbstractManipulatedGraph;
import edu.cmu.causality.manipulatedGraph.ManipulatedGraph;
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
public class TestPopulation extends junit.framework.TestCase {
    private Graph graph;
    private BayesPm pm;
    private MlBayesIm im;
    private ExperimentalSetup exp;
    private ExperimentalSetup expVarStudied;
    private BayesMarginalJoint bayesMarginalJoint;

    public TestPopulation(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        makeModel();
        makePM(graph);
        makeIM(pm);
        makeExperiment(graph);
        makeManipulatedGraph(graph, exp);
        this.bayesMarginalJoint = new BayesMarginalJoint(im, expVarStudied);
    }

    public void testGetRows() {
        assertTrue(bayesMarginalJoint.getNumRows() == 18);
    }

    public void testGetColumns() {
        assertTrue(bayesMarginalJoint.numColumns() == 4);
    }

    public void testGetHeaders() {
        assertTrue(bayesMarginalJoint.createHeaders()[0].equals("education"));
        assertTrue(bayesMarginalJoint.createHeaders()[1].equals("happiness"));
        assertTrue(bayesMarginalJoint.createHeaders()[2].equals("income"));
        assertTrue(bayesMarginalJoint.createHeaders()[3].equals("%"));
    }


    public void testCombinations() {
        int i, j;

        //check the education column

        for (i = 0; i < 6; i++) {
            assertTrue(bayesMarginalJoint.getCase(i)[0].equals("college"));
        }
        for (i = 6; i < 12; i++) {
            assertTrue(bayesMarginalJoint.getCase(i)[0].equals("High school"));
        }
        for (i = 12; i < 18; i++) {
            assertTrue(bayesMarginalJoint.getCase(i)[0].equals("none"));
        }


        for (i = 0; i < 18; ) {
            for (j = 0; j < 3; j++, i++) {
                assertTrue(bayesMarginalJoint.getCase(i)[1].equals("true"));
            }
            for (j = 0; j < 3; j++, i++) {
                assertTrue(bayesMarginalJoint.getCase(i)[1].equals("false"));
            }
        }

        for (i = 0; i < 18; ) {
            int row2 = i++;
            assertTrue(bayesMarginalJoint.getCase(row2)[2].equals("high"));
            int row1 = i++;
            assertTrue(bayesMarginalJoint.getCase(row1)[2].equals("medium"));
            int row = i++;
            assertTrue(bayesMarginalJoint.getCase(row)[2].equals("low"));
        }
    }


    public void testProbabilities() {

        //row 0: Latent = true, Education == college, Happiness = true, Income == High
        assertEquals(bayesMarginalJoint.getProbability(0), .1, .00001);
        //row 1: Latent = true, Education == college, Happiness = true, Income == medium
        assertEquals(bayesMarginalJoint.getProbability(1), .1, .00001);
        //row 2: Latent = true, Education == college, Happiness = true, Income == low
        assertEquals(bayesMarginalJoint.getProbability(2), .1, .00001);

        //row 3: Latent = true, Education == college, Happiness = false, Income == High
        assertEquals(bayesMarginalJoint.getProbability(3), .0, .00001);
        //row 4: Latent = true, Education == college, Happiness = false, Income == medium
        assertEquals(bayesMarginalJoint.getProbability(4), .0, .00001);
        //row 5: Latent = true, Education == college, Happiness = false, Income == low
        assertEquals(bayesMarginalJoint.getProbability(5), .0, .00001);

        //row 6: Latent = true, Education == High school, Happiness = true, Income == High
        assertEquals(bayesMarginalJoint.getProbability(6), .1, .00001);
        //row 7: Latent = true, Education == High school, Happiness = true, Income == medium
        assertEquals(bayesMarginalJoint.getProbability(7), .1, .00001);
        //row 8: Latent = true, Education == High school, Happiness = true, Income == low
        assertEquals(bayesMarginalJoint.getProbability(8), .1, .00001);

        //row 9: Latent = true, Education == High school, Happiness = false, Income == High
        assertEquals(bayesMarginalJoint.getProbability(9), .0, .00001);
        //row 10: Latent = true, Education == High school, Happiness = false, Income == medium
        assertEquals(bayesMarginalJoint.getProbability(10), .0, .00001);
        //row 11: Latent = true, Education == High school, Happiness = false, Income == low
        assertEquals(bayesMarginalJoint.getProbability(11), .0, .00001);

        //row 12: Latent = true, Education == none, Happiness = true, Income == High
        assertEquals(bayesMarginalJoint.getProbability(12), 4.0 / 30.0, .00001);
        //row 13: Latent = true, Education == none, Happiness = true, Income == medium
        assertEquals(bayesMarginalJoint.getProbability(13), 4.0 / 30.0, .00001);
        //row 14: Latent = true, Education == none, Happiness = true, Income == low
        assertEquals(bayesMarginalJoint.getProbability(14), 4.0 / 30.0, .00001);

        //row 15: Latent = true, Education == none, Happiness = false, Income == High
        assertEquals(bayesMarginalJoint.getProbability(15), .0, .00001);
        //row 16: Latent = true, Education == none, Happiness = false, Income == medium
        assertEquals(bayesMarginalJoint.getProbability(16), .0, .00001);
        //row 17: Latent = true, Education == none, Happiness = false, Income == low
        assertEquals(bayesMarginalJoint.getProbability(17), .0, .00001);
    }

    public void testCreateViewedDistribution() {
        expVarStudied.getVariable("income").setStudied(false);
        int i, j;


        //check the education column
        for (j = 0; j < 6; ) {
            for (i = 0; i < 2; i++, j++) {
                assertTrue(bayesMarginalJoint.getCase(i)[0].equals("college"));
            }
            for (i = 2; i < 4; i++, j++) {
                assertTrue(bayesMarginalJoint.getCase(i)[0].equals("High school"));
            }
            for (i = 4; i < 6; i++, j++) {
                assertTrue(bayesMarginalJoint.getCase(i)[0].equals("none"));
            }
        }

        for (i = 0; i < 6; ) {
            int row1 = i++;
            assertTrue((bayesMarginalJoint.getCase(row1)[1]).equals("true"));
            int row = i++;
            assertTrue((bayesMarginalJoint.getCase(row)[1]).equals("false"));
        }

        //row 0: Education == college, Happiness = true
        assertEquals(bayesMarginalJoint.getProbability(0), .3, .00001);
        //row 1:  Education == college, Happiness = false
        assertEquals(bayesMarginalJoint.getProbability(1), .0, .00001);
        //row 2:  Education == High School, Happiness = true
        assertEquals(bayesMarginalJoint.getProbability(2), .3, .00001);
        //row 3:  Education == High School, Happiness = false
        assertEquals(bayesMarginalJoint.getProbability(3), .0, .00001);
        //row 4:  Education == none, Happiness = true
        assertEquals(bayesMarginalJoint.getProbability(4), .4, .00001);
        //row 5:  Education == none, Happiness = false
        assertEquals(bayesMarginalJoint.getProbability(5), .0, .00001);

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
        pm.setNumCategories(pm.getDag().getNode("education"), 3);

        ArrayList varVals = new ArrayList();
        varVals.add("college");
        varVals.add("High school");
        varVals.add("none");
        pm.setCategories(pm.getDag().getNode("education"), varVals);

        varVals = new ArrayList();
        varVals.add("high");
        varVals.add("medium");
        varVals.add("low");
        pm.setCategories(pm.getDag().getNode("income"), varVals);

        varVals = new ArrayList();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("happiness"), varVals);

        varVals = new ArrayList();
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

        //System.out.println(im.toString());
    }

    private void makeExperiment(Graph aGraph) {
        exp = new ExperimentalSetup("experiment", aGraph);
        exp.getVariable("income").setRandomized();
        exp.getVariable("happiness").setLocked("true");
        expVarStudied = exp;
    }

    private void makeManipulatedGraph(Graph aGraph, ExperimentalSetup exp) {
        AbstractManipulatedGraph manipGraph = new ManipulatedGraph(aGraph, exp);
    }
}
