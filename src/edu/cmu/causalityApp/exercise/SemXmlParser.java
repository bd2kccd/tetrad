package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.sem.Parameter;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * This class takes an xml element representing a SEM im and converts it to
 * a SemIM
 *
 * @author mattheweasterday
 */
class SemXmlParser {

    /**
     * Takes an xml representation of a SEM IM and reinstantiates the IM
     *
     * @param semImElement the xml of the IM
     * @return the SemIM
     */
    public static SemIm getSemIm(Element semImElement) {
        if (!SemXmlConstants.SEM.equals(semImElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.SEM + "' element"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Element variablesElement = semImElement.getFirstChildElement(SemXmlConstants.SEM_VARIABLES);
        Element edgesElement = semImElement.getFirstChildElement(SemXmlConstants.EDGES);
        Element marginalDistributionElement = semImElement.getFirstChildElement(SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION);
        Element jointDistributionElement = semImElement.getFirstChildElement(SemXmlConstants.JOINT_ERROR_DISTRIBUTION);


        Dag graph = makeVariables(variablesElement);
        SemIm im = makeEdges(edgesElement, graph);
        setNodeMeans(variablesElement, im);
        addMarginalErrorDistribution(marginalDistributionElement, im);
        addJointErrorDistribution(jointDistributionElement, im);

        return im;
    }


    private static Dag makeVariables(Element variablesElement) {
        if (!SemXmlConstants.SEM_VARIABLES.equals(variablesElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.SEM_VARIABLES + "' element"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Element var;
        GraphNode node;
        Integer x, y;
        Dag semGraph = new Dag();
        Elements vars = variablesElement.getChildElements(SemXmlConstants.CONTINUOUS_VARIABLE);

        for (int i = 0; i < vars.size(); i++) {
            var = vars.get(i);
            node = new GraphNode(var.getAttributeValue(SemXmlConstants.NAME));
            if (var.getAttributeValue(SemXmlConstants.IS_LATENT).equals("yes")) { //$NON-NLS-1$
                node.setNodeType(NodeType.LATENT);
            } else {
                node.setNodeType(NodeType.MEASURED);
            }
            x = new Integer(var.getAttributeValue(SemXmlConstants.X));
            y = new Integer(var.getAttributeValue(SemXmlConstants.Y));
            if (x != null) {
                node.setCenterX(x.intValue());
            }
            if (y != null) {
                node.setCenterY(y.intValue());
            }
            semGraph.addNode(node);
        }
        return semGraph;
    }

    private static SemIm makeEdges(Element edgesElement, Dag semGraph) {
        if (!SemXmlConstants.EDGES.equals(edgesElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.EDGES + "' element"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Element edge;
        Node causeNode, effectNode;

        Elements edges = edgesElement.getChildElements(SemXmlConstants.EDGE);

        for (int i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            causeNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.CAUSE_NODE));
            effectNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.EFFECT_NODE));
            semGraph.addDirectedEdge(causeNode, effectNode);
        }

        //SemIm semIm = SemIm.newInstance(new SemPm(semGraph));
        SemIm semIm = new SemIm(new SemPm(semGraph));
        for (int i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            causeNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.CAUSE_NODE));
            effectNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.EFFECT_NODE));
            semIm.setParamValue(causeNode, effectNode, new Double(edge.getAttributeValue(SemXmlConstants.VALUE)).doubleValue());
            //semIm.getSemPm().getParameter(causeNode, effectNode).setFixed(new Boolean(edge.getAttributeValue(SemXmlConstants.FIXED)).booleanValue());

            Parameter covarianceParameter = semIm.getSemPm().getCovarianceParameter(causeNode, effectNode);

            if (covarianceParameter != null) {
                Boolean aBoolean = Boolean.valueOf(edge.getAttributeValue(SemXmlConstants.FIXED));
                covarianceParameter.setFixed(aBoolean);
            }
        }

        return semIm;
    }

    private static void setNodeMeans(Element variablesElement, SemIm im) {
        Elements vars = variablesElement.getChildElements(SemXmlConstants.CONTINUOUS_VARIABLE);

        for (int i = 0; i < vars.size(); i++) {
            Element var = vars.get(i);
            Node node = im.getSemPm().getGraph().getNode(var.getAttributeValue(SemXmlConstants.NAME));

            if (var.getAttributeValue(SemXmlConstants.MEAN) != null) {
                im.setMean(node, Double.parseDouble(var.getAttributeValue(SemXmlConstants.MEAN)));
            } else {
                return;
            }
        }
    }

    private static void addMarginalErrorDistribution(Element marginalDistributionElement, SemIm semIm) {
        if (!SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION.equals(marginalDistributionElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION + "' element"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Element normal;
        Node node;
        Elements normals = marginalDistributionElement.getChildElements(SemXmlConstants.NORMAL);

        for (int i = 0; i < normals.size(); i++) {
            normal = normals.get(i);

            node = semIm.getSemPm().getGraph().getExogenous(semIm.getSemPm().getGraph().getNode(normal.getAttributeValue(SemXmlConstants.VARIABLE)));
            //can't set mean at this point...
            semIm.setParamValue(node, node, new Double(normal.getAttributeValue(SemXmlConstants.VARIANCE)).doubleValue());
        }
    }

    private static void addJointErrorDistribution(Element jointDistributionElement, SemIm semIm) {
        if (!SemXmlConstants.JOINT_ERROR_DISTRIBUTION.equals(jointDistributionElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.JOINT_ERROR_DISTRIBUTION + "' element"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Element normal;
        Node node1, node2;
        Elements normals = jointDistributionElement.getChildElements(SemXmlConstants.NORMAL);

        for (int i = 0; i < normals.size(); i++) {
            normal = normals.get(i);
            node1 = semIm.getSemPm().getGraph().getExogenous(semIm.getSemPm().getGraph().getNode(normal.getAttributeValue(SemXmlConstants.NODE_1)));
            node2 = semIm.getSemPm().getGraph().getExogenous(semIm.getSemPm().getGraph().getNode(normal.getAttributeValue(SemXmlConstants.NODE_2)));
            semIm.setParamValue(node1, node2, Double.parseDouble(normal.getAttributeValue(SemXmlConstants.COVARIANCE)));
        }
    }

}
