package edu.cmu.causality.experimentalSetup;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;


/**
 * @author Matthew Easterday
 */
public class TestExperimentalSetupVariablesStudied extends junit.framework.TestCase {

    private ExperimentalSetup studiedVariables;

    public TestExperimentalSetupVariablesStudied(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Dag graph = new Dag();
        graph.addNode(new GraphNode("education"));
        graph.addNode(new GraphNode("income"));
        graph.addNode(new GraphNode("happiness"));
        GraphNode node = new GraphNode("Latent");
        node.setNodeType(NodeType.LATENT);
        graph.addNode(node);
        graph.addDirectedEdge(graph.getNode("education"), graph.getNode("income"));
        graph.addDirectedEdge(graph.getNode("education"), graph.getNode("happiness"));
        graph.addDirectedEdge(graph.getNode("Latent"), graph.getNode("education"));

        studiedVariables = new ExperimentalSetup("experiment 1", graph);
    }

    public void test() {
        boolean except = false;

        assertTrue(studiedVariables.isVariableStudied("education"));
        assertTrue(studiedVariables.isVariableStudied("income"));
        assertTrue(studiedVariables.isVariableStudied("happiness"));


        studiedVariables.getVariable("education").setStudied(false);
        assertTrue(!studiedVariables.isVariableStudied("education"));
        assertTrue(studiedVariables.getNumVariablesStudied() == 2);

        try {
            studiedVariables.isVariableStudied("foo");
        } catch (IllegalArgumentException e) {
            except = true;
        }
        assertTrue(except);

        except = false;
        try {
            studiedVariables.getVariable("foo").setStudied(false);
        } catch (IllegalArgumentException e) {
            except = true;
        }
        assertTrue(except);
    }

    public void testNames() {
        String[] names;
        studiedVariables.getVariable("education").setStudied(false);
        names = studiedVariables.getNamesOfStudiedVariables();
        assertTrue(studiedVariables.getNumVariablesStudied() == 2);
        assertTrue(names.length == 2);
        assertTrue(((names[0]).equals("happiness") && (names[1]).equals("income")) ||
                (names[1]).equals("happiness") && (names[0]).equals("income"));
    }
}
