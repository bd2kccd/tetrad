package edu.cmu.causality.hypotheticalGraph;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

/**
 * @author mattheweasterday
 */
public class TestHypotheticalGraph extends junit.framework.TestCase {

    private HypotheticalGraph hg;

    /**
     * Default constructor for use of JUNIT class
     */
    public TestHypotheticalGraph(String name) {
        super(name);
    }


    /**
     * Setups the initial variables for JUNIT testing
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        hg = new HypotheticalGraph("h1", new Dag(), false, false);

        hg.addNode(new GraphNode("education"));
        hg.addNode(new GraphNode("happiness"));
        hg.addNode(new GraphNode("income"));
        GraphNode tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        hg.addNode(tn);

        hg.addDirectedEdge(hg.getNode("education"), hg.getNode("income"));
        hg.addDirectedEdge(hg.getNode("education"), hg.getNode("happiness"));
        hg.addDirectedEdge(hg.getNode("Latent"), hg.getNode("education"));
    }


    public void testCopy() {
        HypotheticalGraph hg2 = hg.copyGraph(true, false);
        assertTrue(hg.getName().equals("h1"));
        assertTrue(hg.getNumEdges() == 3);

        assertTrue(hg.getName().equals(hg2.getName()));
        assertTrue(hg.getNumEdges() == hg2.getNumEdges());
    }
}
