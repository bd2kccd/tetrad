package edu.cmu.causality.hypotheticalGraph;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.List;

/**
 * This class renders and parses the hypothetical graph to its xml representation
 * and vice versa.
 *
 * @author mattheweasterday
 */
public class HypotheticalGraphXml {
    private static final String HYPOTHETICALGRAPH = "hypGraph";
    private static final String NAME = "name";
    private static final String VARIABLES = "hypVariables";
    private static final String VARIABLE = "hypVariable";
    private static final String TYPE = "type";
    private static final String CENTERX = "centerX";
    private static final String CENTERY = "centerY";
    private static final String EDGES = "hypEdges";
    private static final String EDGE = "hypEdge";
    private static final String FROM = "causeVar";
    private static final String TO = "effectVar";

    /**
     * Renders the xml element representation of a hypothetical graph.
     *
     * @return the xml element representation of the hypothetical graph.
     */
    public static Element renderHypotheticalGraph(HypotheticalGraph hg) {
        Element hgE = new Element(HYPOTHETICALGRAPH);
        hgE.addAttribute(new Attribute(NAME, hg.getName()));

        Element variablesE = new Element(VARIABLES);
        Element edgesE = new Element(EDGES);
        Element variableE;
        Element edgeE;

        List<Node> nodes = hg.getNodes();
        for (Node node : nodes) {
            variableE = new Element(VARIABLE);
            variableE.addAttribute(new Attribute(NAME, node.getName()));
            variableE.addAttribute(new Attribute(TYPE, node.getNodeType().toString()));
            variableE.addAttribute(new Attribute(CENTERX, Integer.toString(node.getCenterX())));
            variableE.addAttribute(new Attribute(CENTERY, Integer.toString(node.getCenterY())));
            variablesE.appendChild(variableE);

        }

        List<Edge> edges = hg.getEdges();
        for (Edge edge : edges) {
            edgeE = new Element(EDGE);
            edgeE.addAttribute(new Attribute(FROM, edge.getNode1().getName()));
            edgeE.addAttribute(new Attribute(TO, edge.getNode2().getName()));
            edgesE.appendChild(edgeE);
        }

        hgE.appendChild(variablesE);
        hgE.appendChild(edgesE);
        return hgE;
    }


}
