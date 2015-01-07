package edu.cmu.causality;


import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causality.manipulatedGraph.ManipulatedEdgeType;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Matthew Easterday
 */
public class TestCausalityLabModel extends junit.framework.TestCase {

    private CausalityLabModel model;
    private String exptID0, exptID;
    private String hypID;

    /**
     * Default constructor for use of JUNIT class
     */
    public TestCausalityLabModel(String name) {
        super(name);

    }


    /**
     * Setups the initial variables for JUNIT testing
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {

        model = makeMiniModel();

        hypID = "Hypothesis 1";
        assertTrue(model.getNumHypotheticalGraphs() == 0);

        model.addNewHypotheticalGraph(hypID);
        exptID0 = "Experiment 1";
        model.addNewExperiment(exptID0);

        exptID = "Expt bar";
        model.addNewExperiment(exptID);

        model.addHypotheticalGraphEdge(hypID, "education", "happiness");
        model.addHypotheticalGraphEdge(hypID, "education", "income");
        model.setExperimentalVariableStudied(exptID, "happiness", false);
        model.setExperimentalVariableRandomized(exptID, "income");

    }


    public void testGetVariableParameters() {
        List<String> parameters = model.getVariableParameters("education");
        String param;
        for (Object parameter : parameters) {
            param = (String) parameter;
            assertTrue(param.equals("High school") || param.equals("college") || param.equals("none"));
        }
    }


    //***********************************************************************
    //        Test student answer methods
    //***********************************************************************
    public void testSameGraph() {
        String hyp = "foobar";
        model.addNewHypotheticalGraph(hyp);

        //shouldn't pass--no latent
        assertTrue(!model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        //shouldn't pass--missing an edge from latent
        model.addLatentVariableToHypotheticalGraph("blah", hyp);
        assertTrue(!model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        //should pass
        model.addHypotheticalGraphEdge(hyp, "blah", "education");
        assertTrue(!model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        model.addHypotheticalGraphEdge(hyp, "education", "income");
        model.addHypotheticalGraphEdge(hyp, "education", "happiness");
        assertTrue(model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        //shouldn't pass, extra edge from latent
        model.addHypotheticalGraphEdge(hyp, "blah", "income");
        assertTrue(!model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        //should pass,
        model.removeEdgeFromHypotheticalGraph("blah", "income", hyp);
        assertTrue(model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        //shouldn't pass
        model.removeEdgeFromHypotheticalGraph("blah", "education", hyp);
        assertTrue(!model.isHypotheticalGraphSameAsCorrectGraph(hyp));

        //shouldn't pass
        model.addHypotheticalGraphEdge(hyp, "education", "blah");
        assertTrue(!model.isHypotheticalGraphSameAsCorrectGraph(hyp));
    }


    public void testRightManipulatedGraph() {
        String name = "exp foo";
        model.addNewHypotheticalGraph(name);
        model.addLatentVariableToHypotheticalGraph(name, "blah");
        model.addHypotheticalGraphEdge(name, "blah", "education");
        model.addHypotheticalGraphEdge(name, "education", "income");
        model.addHypotheticalGraphEdge(name, "education", "happiness");

        String[] names = model.getExperimentNames();
        for (String name1 : names) {
            System.out.println(name1);
        }

        model.setGuessedManipulatedGraphEdgeBroken(exptID, name, "education", "income");
    }

    //***********************************************************************
    //       TESTS FOR CORRECT GRAPH METHODS
    //***********************************************************************

    /**
     * Tests that the correct graph stores the correct graph names
     */
    public void testGetCorrectGraphVariableNames() {
        String varName;
        List<String> varNames = model.getCorrectGraphVariableNames();
        assertTrue(varNames.size() == 4);

        for (Object varName1 : varNames) {
            varName = (String) varName1;
            if (varName.equalsIgnoreCase("education")) {
                //ok
            } else if (varName.equalsIgnoreCase("income")) {
                //ok
            } else if (varName.equalsIgnoreCase("happiness")) {
                //ok
            } else if (varName.equalsIgnoreCase("Latent")) {
                //ok
            } else {
                //System.out.println(varName);
                assertTrue(false);
            }
        }
    }

    /**
     * Checks that the graph tested has the correct graph edges
     */
    public void testGetCorrectGraphEdges() {
        EdgeInfo edge;
        String edgeString;
        for (EdgeInfo edgeInfo : (model.getCorrectGraphEdges())) {
            edge = edgeInfo;
            edgeString = edge.toString();
            if (edgeString.equalsIgnoreCase("Latent --> education")) {
                //ok
            } else if (edgeString.equalsIgnoreCase("education --> income")) {
                //ok
            } else if (edgeString.equalsIgnoreCase("education --> happiness")) {
                //ok
            } else {
                //System.out.println(varName);
                assertTrue(false);
            }
        }
    }

    /**
     * Tests the isCorrectGraphVariableLatent method
     */
    public void testIsCorrectGraphVariableLatent() {
        boolean except = false;
        assertTrue(model.isCorrectGraphVariableLatent("Latent"));
        assertTrue(!model.isCorrectGraphVariableLatent("education"));
        assertTrue(!model.isCorrectGraphVariableLatent("income"));
        assertTrue(!model.isCorrectGraphVariableLatent("happiness"));

        try {
            model.isCorrectGraphVariableLatent("foo");
        } catch (IllegalArgumentException e) {
            except = true;
        }
        assertTrue(except);
    }

    //***********************************************************************
    //       TESTS FOR EXPERIMENTAL SETUP METHODS
    //***********************************************************************

    /**
     * Tests whether an experiment is properly added or removed after the
     * add and remove experiment commands
     */
    public void testAddRemoveExperiment() {
        String[] experiments = model.getExperimentNames();
        int oldSize = experiments.length;
        model.addNewExperiment("experiment foo");
        experiments = model.getExperimentNames();
        assertTrue(oldSize + 1 == experiments.length);

        model.removeExperiment("experiment foo");
        experiments = model.getExperimentNames();
        assertTrue(oldSize == experiments.length);
    }

    /**
     * Test the variables name from the experimental setup
     */
    public void testGetNamesFromExperimentalSetup() {
        String varName;
        String[] varNames = model.getExperimentalSetupVariableNames(exptID0);
//System.out.println("size: " + varNames.size());
        assertTrue(varNames.length == 3);

        for (String varName1 : varNames) {
            varName = varName1;
            if (varName.equalsIgnoreCase("education")) {
                //ok
            } else if (varName.equalsIgnoreCase("income")) {
                //ok
            } else if (varName.equalsIgnoreCase("happiness")) {
                //ok
            } else if (varName.equalsIgnoreCase("Latent")) {
                assertTrue(false);
            } else {
                //System.out.println(varName);
                assertTrue(false);
            }
        }
    }

    /**
     * Test whether a variable is properly disabled in
     * the experimental setup
     */
    public void testStudiedVariable() {
        model.addNewExperiment("experiment boo");
        assertTrue(model.isVariableInExperimentStudied("experiment boo", "happiness"));
        model.setExperimentalVariableStudied("experiment boo", "happiness", false);
        assertTrue(!model.isVariableInExperimentStudied("experiment boo", "happiness"));

        boolean except = false;
        try {
            model.setExperimentalVariableStudied("experiment boo", "foo", true);
        } catch (IllegalArgumentException e) {
            except = true;
        }
        assertTrue(except);

        model.removeExperiment("experiment boo");
    }

    /**
     * Test whether the changes to the experimental setup get propogated to
     * the correct manipulated graph
     */
    public void testExperimentalSetupPropagatedToCorrectManipulatedGraph() {
        model.addNewExperiment("experiment boo");
        List<String> varNames = model.getCorrectManipulatedGraphVariableNamesForExperiment("experiment boo");
        String varName;
        assertTrue(varNames.size() == 4);

        for (Object varName1 : varNames) {
            varName = (String) varName1;
            if (varName.equalsIgnoreCase("education")) {
                //ok
            } else if (varName.equalsIgnoreCase("income")) {
                //ok
            } else if (varName.equalsIgnoreCase("happiness")) {
                //ok
            } else if (varName.equalsIgnoreCase("Latent")) {
                //ok
            } else {
                //System.out.println(varName);
                assertTrue(false);
            }
        }
        model.setExperimentalVariableStudied("experiment boo", "income", false);
        assertTrue(!model.isVariableInExperimentStudied("experiment boo", "income"));

        model.removeExperiment("experiment boo");
        assertNull(model.getCorrectManipulatedGraphVariableType("income", "experiment boo"));
    }


    //***********************************************************************
    //       TESTS FOR HYPOTHETICAL GRAPH METHODS
    //***********************************************************************
/*
Note really necessary anymore?

    public void testDefaultHypothesis(){
        Set hypotheses = model.getHypotheticalGraphIDs();
        assertTrue(hypotheses.size() == 2);

        Iterator ids = hypotheses.iterator();
        int id;
        List varNames;

        while(ids.hasNext()){
            id = ((Integer) ids.next()).intValue();
            varNames = model.getHypotheticalGraphVariableNames(id);
            assertTrue(varNames.size() > 0);
        }
    }
 */

    /**
     * Test whether a hypothetical graph is properly added or removed after
     * these commands
     */
    public void testAddRemove() {
        String[] hypotheses = model.getHypotheticalGraphNames();
        int oldSize = hypotheses.length;
        model.addNewHypotheticalGraph("hypothesis foo");
        assertTrue(model.getHypotheticalGraphVariableNames("hypothesis foo").size() == 3);

        hypotheses = model.getHypotheticalGraphNames();
        assertTrue(oldSize + 1 == hypotheses.length);

        model.removeHypotheticalGraph("hypothesis foo");
        hypotheses = model.getHypotheticalGraphNames();
        assertTrue(oldSize == hypotheses.length);
    }

    /**
     * Test to see if when you edit a graph if it does the right thing
     */
    public void testEdit() {
        String a = "hypothesis a";
        model.addNewHypotheticalGraph(a);
        model.addHypotheticalGraphEdge(a, "education", "income");

        HypotheticalGraph hg = model.getHypotheticalGraphCopy(a);
        assertTrue(hg.getNumEdges() == 1);
        assertNotNull(hg.getEdge(hg.getNode("education"), hg.getNode("income")));

        hg.addDirectedEdge(hg.getNode("education"), hg.getNode("happiness"));
        model.setHypotheticalGraph(hg, true);

        hg = model.getHypotheticalGraphCopy(a);
        assertTrue(hg.getNumEdges() == 2);
        assertNotNull(hg.getEdge(hg.getNode("education"), hg.getNode("income")));
        assertNotNull(hg.getEdge(hg.getNode("education"), hg.getNode("happiness")));

        model.removeHypotheticalGraph(a);
    }

    /**
     * Test whether a latent variable is latent
     */
    public void testLatents() {
        String varName = "MyLatent";
        List<String> variables;

        model.addNewHypotheticalGraph("hypothesis x");

        model.addLatentVariableToHypotheticalGraph(varName, "hypothesis x");
        variables = model.getHypotheticalGraphVariableNames("hypothesis x");
        assertTrue(variables.contains(varName));

        model.removeLatentVariableFromHypotheticalGraph(varName, "hypothesis x");
        variables = model.getHypotheticalGraphVariableNames("hypothesis x");
        assertTrue(!variables.contains(varName));

        model.removeHypotheticalGraph("hypothesis x");
    }

    /**
     * Tests the isCorrectGraphVariableLatent method
     */
    public void testIsHypotheticalGraphVariableLatent() {
        boolean except = false;

        model.addNewHypotheticalGraph("hypothesis x");

        assertTrue(!model.isHypotheticalGraphVariableLatent("hypothesis x", "education"));
        assertTrue(!model.isHypotheticalGraphVariableLatent("hypothesis x", "income"));
        assertTrue(!model.isHypotheticalGraphVariableLatent("hypothesis x", "happiness"));

        model.addLatentVariableToHypotheticalGraph("aLatent", "hypothesis x");
        assertTrue(model.isHypotheticalGraphVariableLatent("hypothesis x", "aLatent"));

        try {
            model.isHypotheticalGraphVariableLatent("hypothesis x", "foo");
        } catch (IllegalArgumentException e) {
            except = true;
        }
        assertTrue(except);
    }

    /**
     * Test whether an edge can be added or removed successfully to two
     * variables in a graph.
     * Also check that an edge cannot be added to two non-existent variables
     */
    public void testEdges() {
        String[] names = model.getHypotheticalGraphNames();
        assertTrue(model.addHypotheticalGraphEdge(names[0], "income", "happiness"));
        assertTrue(model.removeEdgeFromHypotheticalGraph("income", "happiness", names[0]));
        assertTrue(!model.addHypotheticalGraphEdge(names[0], "blah", "ba"));
    }

    //***********************************************************************
    //    TEST CORRECT MANIPULATED GRAPH
    //***********************************************************************

    /**
     * Verify the variable names of the manipulated graph
     */
    public void testManipulatedGraphVarNames() {
        String varName;
        model.addNewExperiment("experiment boo");
        model.setExperimentalVariableStudied("experiment boo", "income", false);
        List<String> varNames = model.getCorrectManipulatedGraphVariableNamesForExperiment("experiment boo");


        assertTrue(varNames.size() == 4);

        for (Object varName1 : varNames) {
            varName = (String) varName1;
            if (varName.equalsIgnoreCase("education")) {
                //ok
            } else if (varName.equalsIgnoreCase("income")) {
                //ok
            } else if (varName.equalsIgnoreCase("happiness")) {
                //ok
            } else if (varName.equalsIgnoreCase("Latent")) {
                //ok
            } else {
                //System.out.println(varName);
                assertTrue(false);
            }
        }
        model.removeExperiment("experiment boo");
    }

    /**
     * Verify the edges of the correct manipulated graph
     * Also verify that the broken edges are indeed broken
     * <p/>
     * The correct behavior is for
     */
    public void testCorrectManipulatedGraphEdges() {
        Iterator<EdgeInfo> i;
        EdgeInfo edge;
        String edgeString;
        model.addNewExperiment("experiment boo");
        model.setExperimentalVariableStudied("experiment boo", "income", false);
        model.setExperimentalVariableRandomized("experiment boo", "income");
        model.setExperimentalVariableLocked("experiment boo", "happiness", "true");

        List<EdgeInfo> edges = model.getCorrectManipulatedGraphActiveEdgesForExperiment("experiment boo");
        EdgeInfo[] brokenEdges = model.getCorrectManipulatedGraphBrokenEdges("experiment boo");

        assertTrue(edges.size() == 1);
        i = edges.listIterator();
        edge = i.next();
        edgeString = edge.toString();
        assertTrue(edgeString.equalsIgnoreCase("Latent --> education"));

        assertTrue(brokenEdges.length == 2);
        for (int j = 0; j < 2; j++) {
            assertTrue(brokenEdges[j].toString().equals("education --> income") ||
                    brokenEdges[j].toString().equals("education --> happiness"));
            assertTrue((brokenEdges[j].getType() == ManipulatedEdgeType.BROKEN) ||
                    (brokenEdges[j].getType() == ManipulatedEdgeType.FROZEN));
        }
        model.removeExperiment("experiment boo");
    }

    //***********************************************************************
    // Hypothetical Manipulated Graph Method Test
    //***********************************************************************

    /**
     * If there are n experiments, and m hypothethetical graphs, make sure taht
     * there are mxn hypothetical manipualted graphs
     */
    public void testHypotheticalManipulatedGraphsCreated() {
        String i;
        String j;
        List<String> varNames;
        List<EdgeInfo> edges;

        String name;
        String[] names = model.getExperimentNames();
        for (String name1 : names) {
            i = name1;
            String[] namess = model.getHypotheticalGraphNames();
            for (String names1 : namess) {
                j = names1;

                varNames = model.getHypotheticalManipulatedGraphVariableNames(i, j);
                edges = model.getHypotheticalManipulatedGraphActiveEdges(i, j);
                //brokenEdges = model.getHypotheticalManipulatedGraphBrokenEdges(i,j);

                //make sure that manipulation type in manipualted graph is same as in experimental setup
                for (Object varName : varNames) {
                    name = (String) varName;
                    if (name.equals("education")) {
                        assertTrue(
                                model.getHypotheticalManipulatedGraphVariableType(i, j, "education") ==
                                        model.getExperimentalVariableManipulation(i, "education").getType());
                    } else if (name.equals("happiness")) {
                        assertTrue(
                                model.getHypotheticalManipulatedGraphVariableType(i, j, "happiness") ==
                                        model.getExperimentalVariableManipulation(i, "happiness").getType());
                    } else if (name.equals("income")) {
                        assertTrue(
                                model.getHypotheticalManipulatedGraphVariableType(i, j, "income") ==
                                        model.getExperimentalVariableManipulation(i, "income").getType());

                    } else {
                        assertTrue(false);
                    }
                }

                //make sure that all edges are normal and have toNodes with no manipulation
                for (Object edge : edges) {
                    EdgeInfo eInfo = (EdgeInfo) edge;
                    assertTrue(eInfo.getType() == ManipulatedEdgeType.NORMAL);
                    assertTrue(model.getHypotheticalManipulatedGraphVariableType(i, j, eInfo.getToNode()) == ManipulationType.NONE);
                }

                //make sure that broken edges are all broken, and have manipulated toNodes
                /*
                for(k = 0; k < brokenEdges.length; k++){
                    EdgeInfo beInfo = brokenEdges[k];
                    ManipulatedEdgeType eType = beInfo.getType();
                    ManipulationType toVariableType = model.getHypotheticalManipulatedGraphVariableType(i,j, beInfo.getToNode());
                    assertTrue((eType == ManipulatedEdgeType.BROKEN) || (eType == ManipulatedEdgeType.FROZEN));
                    assertTrue(toVariableType== ManipulationType.RANDOMIZED);
                }
                */

            }
        }
    }

    public void testHypotheticalManipulatedGraphVariableNames() {
        String varName;

        List<String> varNames = model.getHypotheticalManipulatedGraphVariableNames(exptID, hypID);

        assertTrue(varNames.size() == 3);

        for (Object varName1 : varNames) {
            varName = (String) varName1;
            if (varName.equalsIgnoreCase("education")) {
                //ok
            } else if (varName.equalsIgnoreCase("income")) {
                //ok
            } else if (varName.equalsIgnoreCase("happiness")) {
                //ok
            } else if (varName.equalsIgnoreCase("Latent")) {
                assertTrue(false);

            } else {
                //System.out.println(varName);
                assertTrue(false);
            }
        }
    }

    public void testHypotheticalManipulatedGraphVariableType() {
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "income") == ManipulationType.RANDOMIZED);
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "education") == ManipulationType.NONE);
    }

    public void testHypotheticalManipulatedGraphEdges() {
        model.setExperimentalVariableStudied(exptID, "happiness", true);
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "income") == ManipulationType.RANDOMIZED);
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "happiness") == ManipulationType.NONE);
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "education") == ManipulationType.NONE);

        List<EdgeInfo> edgesList = model.getHypotheticalManipulatedGraphActiveEdges(exptID, hypID);
        System.out.println(edgesList.size());
        for (Object anEdgesList : edgesList) {
            System.out.println(anEdgesList);
        }
        assertTrue(edgesList.size() == 1);
    }


    public void testHypotheticalManipulatedGraphRemovedEdges() {
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "income") == ManipulationType.RANDOMIZED);
        assertTrue(model.getHypotheticalManipulatedGraphVariableType(exptID, hypID, "education") == ManipulationType.NONE);

        EdgeInfo[] brokenEdgesList = model.getHypotheticalManipulatedGraphBrokenEdges(exptID, hypID);
        assertTrue(brokenEdgesList.length == 1);

        for (int j = 0; j < 1; j++) {
            assertTrue(brokenEdgesList[j].toString().equals("education --> income") || brokenEdgesList[j].toString().equals("education --> happiness"));
            assertTrue(brokenEdgesList[j].getType() == ManipulatedEdgeType.BROKEN);
        }
    }


    //***********************************************************************
    // Tests for Hypothetical manipulated graph guessed
    //***********************************************************************

    public void testHypotheticalManipulatedGraphGuesses() {
        String[] hypIds = model.getHypotheticalGraphNames();
        String[] expIds = model.getExperimentNames();

        for (String hypId : hypIds) {
            for (String expId : expIds) {
                assertNotNull(model.getGuessedManipulatedGraphVariableManipulation(expId, hypId, "education"));//how many possibilitles there are
            }
        }

        boolean test = false;
        try {
            model.getGuessedManipulatedGraphVariableManipulation("blah blah", "blah", "foo");
        } catch (IllegalArgumentException e) {
            test = true;
        }
        assertTrue(test);


        //adding edges
        //removing edges
    }


    //***********************************************************************
    // Tests for BayesPopulation Methods
    //***********************************************************************
    /**
     * Verify the actual names of the variables gotten from the population
     */
    /*
    public void testGetHeader() {
        List header = model.getHeader(exptID);

        assertTrue(((String)header.get(0)).equals("education"));
        assertTrue(((String)header.get(1)).equals("happiness"));
        assertTrue(((String)header.get(2)).equals("income"));
        assertTrue(((String)header.get(3)).equals("%"));
    }
     */

    /**
     * Verify the actual values of the individual probabilities of the
     * variables
     */
    /*
    public void testGetValues() {
        List header = model.getSievedValues(exptID);
        List combiStr;

    }
    */
    public void testCombinations() {
        int i, j;
        /*
        for(i=0; i< 36; i++){
            System.out.println(i + " " +
                    fullPopulationDistribution.getValueAt(i, 0) + " " +
                    fullPopulationDistribution.getValueAt(i, 1) + " " +
                    fullPopulationDistribution.getValueAt(i, 2) + " " +
                    fullPopulationDistribution.getValueAt(i, 3) + " " +
                    fullPopulationDistribution.getValueAt(i, 4)
            );
        }
        */


        //check the education column
        PopulationTableModel population = model.getPopulationTableModel(exptID0);

        for (i = 0; i < 6; i++) {
            assertTrue(population.getValueAt(i, 0).equals("college"));
        }
        for (i = 6; i < 12; i++) {
            assertTrue(population.getValueAt(i, 0).equals("High school"));
        }
        for (i = 12; i < 18; i++) {
            assertTrue(population.getValueAt(i, 0).equals("none"));
        }


        for (i = 0; i < 18; ) {
            for (j = 0; j < 3; j++, i++) {
                assertTrue(population.getValueAt(i, 1).equals("true"));
            }
            for (j = 0; j < 3; j++, i++) {
                assertTrue(population.getValueAt(i, 1).equals("false"));
            }
        }

        for (i = 0; i < 18; ) {
            assertTrue(population.getValueAt(i++, 2).equals("high"));
            assertTrue(population.getValueAt(i++, 2).equals("medium"));
            assertTrue(population.getValueAt(i++, 2).equals("low"));
        }

    }

    public void testProbabilities() {
        PopulationTableModel population = model.getPopulationTableModel(exptID0);
        int frequencyColumn = 3;

        //row 0: Latent = true, Education == college, Happiness = true, Income == High
        assertEquals((Double) population.getValueAt(0, frequencyColumn), .2835, .00001);
        //row 1: Latent = true, Education == college, Happiness = true, Income == medium
        assertEquals((Double) population.getValueAt(1, frequencyColumn), .081, .00001);
        //row 2: Latent = true, Education == college, Happiness = true, Income == low
        assertEquals((Double) population.getValueAt(2, frequencyColumn), .0405, .00001);

        //row 3: Latent = true, Education == college, Happiness = false, Income == High
        assertEquals((Double) population.getValueAt(3, frequencyColumn), .0315, .00001);
        //row 4: Latent = true, Education == college, Happiness = false, Income == medium
        assertEquals((Double) population.getValueAt(4, frequencyColumn), .009, .00001);
        //row 5: Latent = true, Education == college, Happiness = false, Income == low
        assertEquals((Double) population.getValueAt(5, frequencyColumn), .0045, .00001);

        //row 6: Latent = true, Education == High school, Happiness = true, Income == High
        assertEquals((Double) population.getValueAt(6, frequencyColumn), .084, .00001);
        //row 7: Latent = true, Education == High school, Happiness = true, Income == medium
        assertEquals((Double) population.getValueAt(7, frequencyColumn), .084, .00001);
        //row 8: Latent = true, Education == High school, Happiness = true, Income == low
        assertEquals((Double) population.getValueAt(8, frequencyColumn), .042, .00001);

        //row 9: Latent = true, Education == High school, Happiness = false, Income == High
        assertEquals((Double) population.getValueAt(9, frequencyColumn), .056, .00001);
        //row 10: Latent = true, Education == High school, Happiness = false, Income == medium
        assertEquals((Double) population.getValueAt(10, frequencyColumn), .056, .00001);
        //row 11: Latent = true, Education == High school, Happiness = false, Income == low
        assertEquals((Double) population.getValueAt(11, frequencyColumn), .028, .00001);

        //row 12: Latent = true, Education == none, Happiness = true, Income == High
        assertEquals((Double) population.getValueAt(12, frequencyColumn), .004, .00001);
        //row 13: Latent = true, Education == none, Happiness = true, Income == medium
        assertEquals((Double) population.getValueAt(13, frequencyColumn), .016, .00001);
        //row 14: Latent = true, Education == none, Happiness = true, Income == low
        assertEquals((Double) population.getValueAt(14, frequencyColumn), 0.02, .00001);

        //row 15: Latent = true, Education == none, Happiness = false, Income == High
        assertEquals((Double) population.getValueAt(15, frequencyColumn), .016, .00001);
        //row 16: Latent = true, Education == none, Happiness = false, Income == medium
        assertEquals((Double) population.getValueAt(16, frequencyColumn), .064, .00001);
        //row 17: Latent = true, Education == none, Happiness = false, Income == low
        assertEquals((Double) population.getValueAt(17, frequencyColumn), .08, .00001);
    }

    //***********************************************************************
    // Tests for BayesSample Methods
    //***********************************************************************

    public void testDuplicateSampleNames() {
        boolean flip = false;
        String name = "blah";

        String[] expNames = model.getExperimentNames();
        model.makeNewSample(expNames[0], 100, name);

        try {
            model.makeNewSample(expNames[0], 100, name);
        } catch (IllegalArgumentException e) {
            flip = true;
        }
        assertTrue(flip);
    }
    //***********************************************************************
    //
    //***********************************************************************

    /**
     * Test for null experimental ID from the different experimental setups
     */
    public void testGetExperimentalSetupIDs() {
        String[] ids = model.getExperimentNames();
        for (String id : ids) {
            assertNotNull(id);
        }
    }

    private static CausalityLabModel makeMiniModel() {
        Graph graph = makeModel();
        BayesPm pm = makePM(graph);
        MlBayesIm im = makeIM(pm);

        CausalityLabModel.initialize(im, null);

        return CausalityLabModel.getModel();
    }


    /**
     * Creates a test model for JUNIT.
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

    private static BayesPm makePM(Graph aGraph) {
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
