package edu.cmu.causalityApp;

import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraphXml;
import edu.cmu.causalityApp.exercise.ExerciseXmlParserV42;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;
import nu.xom.Element;

/**
 * @author mattheweasterday
 */
public class TestHypotheticalGraphXml extends junit.framework.TestCase {

    private HypotheticalGraph hg;

    /**
     * Default constructor for use of JUNIT class
     */
    public TestHypotheticalGraphXml(String name) {
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


    public void testRoundTrip() {
        Element elm = HypotheticalGraphXml.renderHypotheticalGraph(hg);
        System.out.println(elm.toXML());
        HypotheticalGraph hg2 = ExerciseXmlParserV42.parseHypotheticalGraphElement(elm);

        assertTrue(hg2.getName().equals("h1"));
        assertTrue(hg2.getNumEdges() == 3);
        assertTrue(hg2.getNumNodes() == 4);
        assertNotNull(hg2.getNode("Latent"));
        System.out.println(hg2.getNode("Latent").getNodeType());
        assertTrue(hg2.getNode("Latent").getNodeType() == NodeType.LATENT);
    }
}
