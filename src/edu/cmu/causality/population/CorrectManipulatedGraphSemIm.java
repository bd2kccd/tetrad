package edu.cmu.causality.population;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.Randomized;
import edu.cmu.causality.manipulatedGraph.ManipulatedGraph;
import edu.cmu.tetrad.graph.DirectedGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.Parameter;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import java.util.List;

/**
 * This class describes the SEM IM graph model of the correct manipulated
 * graph.
 *
 * @author mattheweasterday
 */
public class CorrectManipulatedGraphSemIm {

    /**
     * @return the manipulated SEM IM graph given the correct SEM IM and the
     *         experimental setup.
     */
    public static SemIm createIm(SemIm correctSemIm, ExperimentalSetup experiment) {
        SemGraph tempGraph = new SemGraph(correctSemIm.getSemPm().getGraph());
        tempGraph.setShowErrorTerms(false);

        Graph mGraph = new ManipulatedGraph(new DirectedGraph(tempGraph), experiment);
        if (mGraph == null) {
            throw new NullPointerException("Graph was null");
        }

        SemPm pm = new SemPm(mGraph);
        SemIm im = new SemIm(pm);
        Parameter param;
        double value;
        Node nodeA, nodeB;

        //copy the original sem values to the new sem (if possible)
        //replace with SEMIM retain values
        //SemIm.retainValues(correctSemIm, mGraph);

        for (Parameter parameter : im.getSemPm().getParameters()) {
            param = parameter;
            nodeA = correctSemIm.getSemPm().getGraph().getNode(param.getNodeA().getName());
            nodeB = correctSemIm.getSemPm().getGraph().getNode(param.getNodeB().getName());
            if ((nodeA != null) && (nodeB != null)) {
                try {
                    value = correctSemIm.getParamValue(nodeA, nodeB);
                    im.setParamValue(param, value);
                } catch (IllegalArgumentException e) {
                    if (nodeA == nodeB) { // Y->Z  is now Y  Z  so param E_Z, E_Z should become Z Z
                        //Parameter errorParam = correctSemIm.getSemPm(). getNodeParameter(correctSemIm.getSemPm().getGraph().getExogenous(nodeA));
                        Parameter errorParam = correctSemIm.getSemPm().getVarianceParameter(correctSemIm.getSemPm().getGraph().getExogenous(nodeA));
                        value = correctSemIm.getParamValue(errorParam);
                        im.setParamValue(param, value);
                    }
                }
            }
        }
        //alter the sem values based on the experiment
        List nodes = im.getVariableNodes();
        for (Object ob : nodes) {
            Node node = (Node) ob;


            if (experiment.isValidVariableName(node.getName()) &&
                    (experiment.getVariable(node.getName()).getManipulation() instanceof Randomized)) {
                double stdDev = experiment.getVariable(node.getName()).getStandardDeviation();
                im.setMean(node, experiment.getVariable(node.getName()).getMean());

                im.setErrCovar(node, stdDev * stdDev);


            } else {
                im.setMean(node, correctSemIm.getMean(node));
            }
        }
        return im;

/*
        //change the IM based on locked and randomized variables in the
        //quantitative experimental setup
        List variableNames = experiment.getVariables();
        ManipulationType type;
        String variableName;
        for(Iterator i = variableNames.iterator(); i.hasNext();){
            variableName = (String) i.next();
            type = experiment.getManipulationFor(variableName).getType();
            if(type == ManipulationType.LOCKED){
               setLocked(im, experiment, variableName);
            }else if (type == ManipulationType.RANDOMIZED){
               setRandomized(im, variableName);
            }
        }
        return im;
 */
    }

}
