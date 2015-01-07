package edu.cmu.causality.manipulatedGraph;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

/**
 * @author Matthew Easterday
 */
public class TestManipulatedGraph extends junit.framework.TestCase {

    private Graph correctGraph;
    private ExperimentalSetup experiment;


    public TestManipulatedGraph(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        correctGraph = makeModel("Education", "Income", "Happiness");   //$NON-NLS-3$
        experiment = new ExperimentalSetup("experiment 1", correctGraph);
    }

    /**
     * A randomized variable should break the incoming links and leave the outgoing links in tact
     */
    public void testRandomizedType() {
        experiment.getVariable("Education").setRandomized();
        AbstractManipulatedGraph manipulatedGraph;
        manipulatedGraph = new ManipulatedGraph(correctGraph, experiment);

        assertTrue(manipulatedGraph.getManipulationFor("Education") == ManipulationType.RANDOMIZED);
        assertTrue(manipulatedGraph.getManipulationFor("Income") == ManipulationType.NONE);
        assertTrue(manipulatedGraph.getManipulationFor("Happiness") == ManipulationType.NONE);
        assertTrue(manipulatedGraph.getManipulationFor("Latent") == ManipulationType.NONE);
        assertNull(manipulatedGraph.getManipulationFor("foo"));

        EdgeInfo[] brokenEdges = manipulatedGraph.getBrokenEdges();
        assertTrue(brokenEdges.length == 1);
        assertTrue(brokenEdges[0].toString().equals("Latent --> Education"));
        assertTrue(brokenEdges[0].getType() == ManipulatedEdgeType.BROKEN);
    }


    /**
     * A locked variable should break the incoming links and freeze the outgoing links in tact
     */
    public void testLockedType() {
        experiment.getVariable("Education").setLocked("fake value");
        AbstractManipulatedGraph manipulatedGraph;
        manipulatedGraph = new ManipulatedGraph(correctGraph, experiment);

        assertTrue(manipulatedGraph.getManipulationFor("Education") == ManipulationType.LOCKED);
        assertTrue(manipulatedGraph.getManipulationFor("Income") == ManipulationType.NONE);
        assertTrue(manipulatedGraph.getManipulationFor("Happiness") == ManipulationType.NONE);
        assertTrue(manipulatedGraph.getManipulationFor("Latent") == ManipulationType.NONE);
        assertNull(manipulatedGraph.getManipulationFor("foo"));

        EdgeInfo[] brokenEdges = manipulatedGraph.getBrokenEdges();
        EdgeInfo[] frozenEdges = manipulatedGraph.getFrozenEdges();

        assertTrue(brokenEdges.length == 1);
        assertTrue(frozenEdges.length == 2);


        assertTrue((brokenEdges[0].toString().equals("Latent --> Education") &&
                brokenEdges[0].getType() == ManipulatedEdgeType.BROKEN));
        assertTrue((frozenEdges[0].toString().equals("Education --> Income") &&
                frozenEdges[0].getType() == ManipulatedEdgeType.FROZEN));
        assertTrue((frozenEdges[1].toString().equals("Education --> Happiness") &&
                frozenEdges[1].getType() == ManipulatedEdgeType.FROZEN));

    }


    private Graph makeModel(String a, String b, String c) {
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode(a));
        m1.addNode(new GraphNode(b));
        m1.addNode(new GraphNode(c));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode(a), m1.getNode(b));
        m1.addDirectedEdge(m1.getNode(a), m1.getNode(c));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode(a));

        return m1;
    }
}
