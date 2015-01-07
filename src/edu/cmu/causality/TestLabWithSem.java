package edu.cmu.causality;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

/**
 * @author mattheweasterday
 */
public class TestLabWithSem extends junit.framework.TestCase {

    private CausalityLabModel modelWithSem;
    private String exp1, exp2, exp3;

    public TestLabWithSem(String name) {
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

        CausalityLabModel.initialize(semIm, null);
        modelWithSem = CausalityLabModel.getModel();
        exp1 = "Exp 1";
        exp2 = "Exp 2";
        exp3 = "Exp 3";

        modelWithSem.addNewExperiment(exp1);

        modelWithSem.addNewExperiment(exp2);
        modelWithSem.setExperimentalVariableRandomized(exp2, "Z");

        modelWithSem.addNewExperiment(exp3);
        modelWithSem.setExperimentalVariableRandomized(exp3, "Y");
    }

    public void testPopulation() {
        PopulationTableModel pop;

        pop = modelWithSem.getPopulationTableModel(exp1);
        /*
            X->Y<-W  Y->Z

            implied cov of X & X = 1.0 X
            implied cov of W & W = 1.0 W
            implied cov of Y & Y =
            implied cov of Z & Z =

                 X     W     Y      Z
           X  1.00
           W  0.00  1.00
           Y  0.50  0.60  1.61
           Z  0.35  0.42  1.127  1.7889
        */
        assertEquals(1.0, pop.getCovariance("X", "X"), 0.01);
        assertEquals(0.0, pop.getCovariance("W", "X"), 0.01);
        assertEquals(1.0, pop.getCovariance("W", "W"), 0.01);
        assertEquals(0.5, pop.getCovariance("Y", "X"), 0.01);
        assertEquals(0.6, pop.getCovariance("Y", "W"), 0.01);
        assertEquals(1.61, pop.getCovariance("Y", "Y"), 0.01);
        assertEquals(0.35, pop.getCovariance("Z", "X"), 0.01);
        assertEquals(0.42, pop.getCovariance("Z", "W"), 0.01);
        assertEquals(1.127, pop.getCovariance("Z", "Y"), 0.01);
        assertEquals(1.7889, pop.getCovariance("Z", "Z"), 0.01);

        /*
           X     W     Y      Z
        X  1.00
        W  0.00  1.00
        Y  0.50  0.60  1.61
        Z  0.00  0.00  0.00  1.00
        */
        pop = modelWithSem.getPopulationTableModel(exp2);
        assertEquals(1.00, pop.getCovariance("X", "X"), 0.01);
        assertEquals(0.00, pop.getCovariance("W", "X"), 0.01);
        assertEquals(1.00, pop.getCovariance("W", "W"), 0.01);
        assertEquals(0.50, pop.getCovariance("Y", "X"), 0.01);
        assertEquals(0.60, pop.getCovariance("Y", "W"), 0.01);
        assertEquals(1.61, pop.getCovariance("Y", "Y"), 0.01);
        assertEquals(0.00, pop.getCovariance("Z", "X"), 0.01);
        assertEquals(0.00, pop.getCovariance("Z", "W"), 0.01);
        assertEquals(0.00, pop.getCovariance("Z", "Y"), 0.01);
        assertEquals(1.00, pop.getCovariance("Z", "Z"), 0.01);


        /*
           X     W     Y      Z
        X  1
        W  0.00  1.00
        Y  0.00  0.00  1.00
        Z  0.00  0.00  0.70  1.49
        */
        pop = modelWithSem.getPopulationTableModel(exp3);
        assertEquals(1.00, pop.getCovariance("X", "X"), 0.01);
        assertEquals(0.00, pop.getCovariance("W", "X"), 0.01);
        assertEquals(1.00, pop.getCovariance("W", "W"), 0.01);
        assertEquals(0.00, pop.getCovariance("Y", "X"), 0.01);
        assertEquals(0.00, pop.getCovariance("Y", "W"), 0.01);
        assertEquals(1.00, pop.getCovariance("Y", "Y"), 0.01);
        assertEquals(0.00, pop.getCovariance("Z", "X"), 0.01);
        assertEquals(0.00, pop.getCovariance("Z", "W"), 0.01);
        assertEquals(0.70, pop.getCovariance("Z", "Y"), 0.01);
        assertEquals(1.49, pop.getCovariance("Z", "Z"), 0.01);

    }

}
