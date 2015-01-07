package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.ParamType;
import edu.cmu.tetrad.sem.Parameter;
import edu.cmu.tetrad.sem.SemIm;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * This class converts a SemIm into xml.
 *
 * @author mattheweasterday
 */
class SemXmlRenderer {

    /**
     * Converts a Sem Im into xml.
     *
     * @param semIm the instantiated structural equation model to convert
     * @return xml representation
     */
    public static Element getElement(SemIm semIm) {
        Element semElement = new Element(SemXmlConstants.SEM);
        semElement.appendChild(makeVariables(semIm));
        semElement.appendChild(makeEdges(semIm));
        semElement.appendChild(makeMarginalErrorDistribution(semIm));
        semElement.appendChild(makeJointErrorDistribution(semIm));
        return semElement;
    }


    private static Element makeVariables(SemIm semIm) {
        Element variablesElement = new Element(SemXmlConstants.SEM_VARIABLES);
        Element variable;
        Node measuredNode, latentNode;
        for (Node node : semIm.getSemPm().getMeasuredNodes()) {
            measuredNode = node;
            variable = new Element(SemXmlConstants.CONTINUOUS_VARIABLE);
            variable.addAttribute(new Attribute(SemXmlConstants.NAME, measuredNode.getName()));
            variable.addAttribute(new Attribute(SemXmlConstants.IS_LATENT, "no"));
            variable.addAttribute(new Attribute(SemXmlConstants.MEAN, Double.toString(semIm.getMean(measuredNode))));
            variable.addAttribute(new Attribute(SemXmlConstants.X, Integer.toString(measuredNode.getCenterX())));
            variable.addAttribute(new Attribute(SemXmlConstants.Y, Integer.toString(measuredNode.getCenterY())));
            variablesElement.appendChild(variable);
        }
        for (Node node : semIm.getSemPm().getLatentNodes()) {
            latentNode = node;
            variable = new Element(SemXmlConstants.CONTINUOUS_VARIABLE);
            variable.addAttribute(new Attribute(SemXmlConstants.NAME, latentNode.getName()));
            variable.addAttribute(new Attribute(SemXmlConstants.IS_LATENT, "yes"));
            variable.addAttribute(new Attribute(SemXmlConstants.MEAN, Double.toString(semIm.getMean(latentNode))));
            variable.addAttribute(new Attribute(SemXmlConstants.X, Integer.toString(latentNode.getCenterX())));
            variable.addAttribute(new Attribute(SemXmlConstants.Y, Integer.toString(latentNode.getCenterY())));
            variablesElement.appendChild(variable);
        }
        return variablesElement;
    }

    private static Element makeEdges(SemIm semIm) {
        Element edgesElement = new Element(SemXmlConstants.EDGES);
        Parameter param;
        Element edge;

        for (Parameter parameter : semIm.getSemPm().getParameters()) {
            param = parameter;
            if (param.getType() == ParamType.COEF) {
                edge = new Element(SemXmlConstants.EDGE);
                edge.addAttribute(new Attribute(SemXmlConstants.CAUSE_NODE, param.getNodeA().getName()));
                edge.addAttribute(new Attribute(SemXmlConstants.EFFECT_NODE, param.getNodeB().getName()));
                edge.addAttribute(new Attribute(SemXmlConstants.VALUE, Double.toString(semIm.getParamValue(param))));
                edge.addAttribute(new Attribute(SemXmlConstants.FIXED, Boolean.toString(param.isFixed())));
                edgesElement.appendChild(edge);
            }
        }
        return edgesElement;
    }


    private static Element makeMarginalErrorDistribution(SemIm semIm) {
        Element marginalErrorElement = new Element(SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION);
        Element normal;
        Node node;

        for (Node node1 : getExogenousNodes(semIm.getSemPm().getGraph())) {
            node = node1;
            normal = new Element(SemXmlConstants.NORMAL);
            normal.addAttribute(new Attribute(SemXmlConstants.VARIABLE, node.getName()));
            normal.addAttribute(new Attribute(SemXmlConstants.MEAN, "0.0"));
            normal.addAttribute(new Attribute(SemXmlConstants.VARIANCE, Double.toString(semIm.getParamValue(node, node))));
            marginalErrorElement.appendChild(normal);
        }
        return marginalErrorElement;
    }

    private static List<Node> getExogenousNodes(SemGraph graph) {
        List<Node> exogenousNodes = new ArrayList<Node>();

        for (Node node : graph.getNodes()) {
            exogenousNodes.add(graph.getExogenous(node));
        }

        return exogenousNodes;
    }


    private static Element makeJointErrorDistribution(SemIm semIm) {
        Element jointErrorElement = new Element(SemXmlConstants.JOINT_ERROR_DISTRIBUTION);
        Element normal;

        for (Parameter param : semIm.getSemPm().getParameters()) {
            if (param.getType() == ParamType.COVAR) {
                normal = new Element(SemXmlConstants.NORMAL);
                normal.addAttribute(new Attribute(SemXmlConstants.NODE_1, param.getNodeA().getName()));
                normal.addAttribute(new Attribute(SemXmlConstants.NODE_2, param.getNodeB().getName()));
                normal.addAttribute(new Attribute(SemXmlConstants.COVARIANCE, Double.toString(param.getStartingValue())));
                jointErrorElement.appendChild(normal);
            }
        }

        return jointErrorElement;
    }

}
