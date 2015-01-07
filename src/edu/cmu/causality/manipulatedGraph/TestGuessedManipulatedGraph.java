package edu.cmu.causality.manipulatedGraph;

import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

/**
 * @author mattheweasterday
 */
public class TestGuessedManipulatedGraph extends junit.framework.TestCase {
    private GuessedManipulatedGraph guess;

    public TestGuessedManipulatedGraph(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        Graph correctGraph = makeModel("Education", "Income", "Happiness");
        guess = new GuessedManipulatedGraph(correctGraph);
    }

    /**
     * A randomized variable should break the incoming links and leave the outgoing links in tact
     */
    public void testRandomizedType() {
        guess.setVariableRandomized("Education");
        assertTrue(guess.getManipulationFor("Education") == ManipulationType.RANDOMIZED);
        assertTrue(guess.getManipulationFor("Income") == ManipulationType.NONE);
        assertTrue(guess.getManipulationFor("Happiness") == ManipulationType.NONE);
        assertTrue(guess.getManipulationFor("Latent") == ManipulationType.NONE);
        assertNull(guess.getManipulationFor("foo"));

        EdgeInfo[] brokenEdges = guess.getBrokenEdges();
        assertTrue(brokenEdges.length == 0);

        guess.setVariableNotManipulated("Education");
        assertTrue(guess.getManipulationFor("Education") == ManipulationType.NONE);
    }

    /**
     * A locked variable should break the incoming links and freeze the outgoing links in tact
     */
    public void testLockedType() {
        guess.setVariableLocked("Education");
        assertTrue(guess.getManipulationFor("Education") == ManipulationType.LOCKED);
        assertTrue(guess.getManipulationFor("Income") == ManipulationType.NONE);
        assertTrue(guess.getManipulationFor("Happiness") == ManipulationType.NONE);
        assertTrue(guess.getManipulationFor("Latent") == ManipulationType.NONE);
        assertNull(guess.getManipulationFor("foo"));

        EdgeInfo[] brokenEdges = guess.getBrokenEdges();
        assertTrue(brokenEdges.length == 0);

        guess.setVariableNotManipulated("Education");
        assertTrue(guess.getManipulationFor("Education") == ManipulationType.NONE);
    }


    public void testGetAllEdges() {
        EdgeInfo[] edges = guess.getAllNonLatentEdges();
        for (EdgeInfo edge : edges) {
            System.out.println(this.getClass() + " " + edge);
        }
        assertTrue(edges.length == 2);

    }

    public void testEdgeType() {
        guess.setEdgeBroken("Education", "Income");
        assertTrue(guess.getManipulationForEdge("Education", "Income") == ManipulatedEdgeType.BROKEN);
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
