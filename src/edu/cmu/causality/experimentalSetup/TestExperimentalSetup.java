package edu.cmu.causality.experimentalSetup;

import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;


/**
 * @author Matthew Easterday
 */
public class  TestExperimentalSetup extends junit.framework.TestCase {

    private ExperimentalSetup experiment;

    public TestExperimentalSetup(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Dag graph = new Dag();
        graph.addNode(new GraphNode("A"));
        graph.addNode(new GraphNode("B"));
        graph.addNode(new GraphNode("C"));
        GraphNode node = new GraphNode("Latent");
        node.setNodeType(NodeType.LATENT);
        graph.addNode(node);
        graph.addDirectedEdge(graph.getNode("A"), graph.getNode("B"));
        graph.addDirectedEdge(graph.getNode("A"), graph.getNode("C"));
        graph.addDirectedEdge(graph.getNode("Latent"), graph.getNode("A"));

        experiment = new ExperimentalSetup("experiment 1", graph);
    }

    /**
     * Make sure that the default manipulation is set to NONE
     */
    public void testDefaultsSetCorrectly() {
        assertTrue(experiment.getNumVariables() == 3);
        assertTrue(experiment.getVariable("A").getManipulation().getType() == ManipulationType.NONE);
        assertTrue(experiment.getVariable("B").getManipulation().getType() == ManipulationType.NONE);
        assertTrue(experiment.getVariable("C").getManipulation().getType() == ManipulationType.NONE);
        //assertTrue(experiment.getManipulationFor("Latent") == ManipulationType.LATENT);

    }

    /**
     * Make sure that if user asks for a variable not in model that it returns null
     */
    public void testVarNotInExperiment() {
        boolean error = false;
        try {
            experiment.getVariable("W").getManipulation();
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }

    /**
     * Make sure that when you set a manipulation, it gets set
     */
    public void testSetManipulator() {
        experiment.getVariable("A").setRandomized();
        assertTrue(experiment.getVariable("A").getManipulation().getType().equals(ManipulationType.RANDOMIZED));
        experiment.getVariable("A").setUnmanipulated();
    }

    /**
     * Make sure that an excpetion is thrown
     */
    public void testSetManipulatorOnAbsentVariable() {
        boolean except = false;

        try {
            experiment.getVariable("W").setRandomized();
        } catch (IllegalArgumentException e) {
            except = true;
        }
        assertTrue(except);
    }

    /**
     * Make sure you can get all the variables in the model
     */
    public void testGetVariables() {
        String[] varNames = experiment.getVariableNames();

        assertTrue(varNames[0].equals("A"));
        assertTrue(varNames[1].equals("B"));
        assertTrue(varNames[2].equals("C"));
        //assertTrue(((String) i.next()).equals("Latent"));
        assertTrue(varNames.length == 3);
    }

    public void testIsValidVariableName() {
        assertTrue(experiment.isValidVariableName("A"));
        assertTrue(!experiment.isValidVariableName("foo"));
    }

    public void testSetName() {
        experiment.setName("Experiment foo");
        assertTrue(experiment.getName().equals("Experiment foo"));
    }
}
