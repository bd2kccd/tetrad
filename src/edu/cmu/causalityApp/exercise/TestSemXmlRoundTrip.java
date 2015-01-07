package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

/**
 * @author mattheweasterday
 */
public class TestSemXmlRoundTrip extends junit.framework.TestCase {

    public TestSemXmlRoundTrip(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        Graph graph = new Dag();

        graph.addNode(new GraphNode("X"));
        graph.addNode(new GraphNode("W"));
        graph.addNode(new GraphNode("Y"));
        graph.addNode(new GraphNode("Z"));

        graph.addDirectedEdge(graph.getNode("X"), graph.getNode("Y"));
        graph.addDirectedEdge(graph.getNode("W"), graph.getNode("Y"));
        graph.addDirectedEdge(graph.getNode("Y"), graph.getNode("Z"));

        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        semIm.setEdgeCoef(graph.getNode("X"), graph.getNode("Y"), 0.5);
        semIm.setEdgeCoef(graph.getNode("W"), graph.getNode("Y"), 0.6);
        semIm.setEdgeCoef(graph.getNode("Y"), graph.getNode("Z"), 0.7);


        SemGraph semGraph = semIm.getSemPm().getGraph();
        semIm.setErrCovar(semGraph.getNode("X"), 1.0);
        semIm.setErrCovar(semGraph.getNode("W"), 1.0);
        semIm.setErrCovar(semGraph.getNode("Y"), 1.0);
        semIm.setErrCovar(semGraph.getNode("Z"), 1.0);

    }


}