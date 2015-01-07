package edu.cmu.tetrad.search;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.GeneralizedSemPm;
import edu.cmu.tetrad.sem.SemPm;

import java.util.List;

/**
 * Stores a graph with policy variables (randomizations and locks) as parents of measured and latent
 * variables in the graph. The original
 * @author Joseph Ramsey
 */
public class ExperimentalSetup {
    private enum PmType {bayes, sem, generalized}
    private enum EsType {simpleSurgical, simpleSoft, fullExperimental, counterfactualExperimental}

    private PmType pmType;
    private EsType esType = EsType.simpleSurgical;
    private BayesPm bayesPm = null;
    private SemPm semPm = null;
    private GeneralizedSemPm generalizedSemPm = null;

    private Graph gNat;
    private Graph gManip;

    public ExperimentalSetup(BayesPm pm) {
        this.bayesPm = pm;
        this.pmType = PmType.bayes;

        Dag dag = pm.getDag();
        this.gNat = new EdgeListGraph(dag);
        this.gManip = new EdgeListGraph(gNat);
    }

    public ExperimentalSetup(SemPm pm) {
        this.semPm = pm;
        this.pmType = PmType.sem;

        SemGraph graph = pm.getGraph();
        graph.setShowErrorTerms(false);
        this.gNat = new EdgeListGraph(graph);
        this.gManip = new EdgeListGraph(gNat);
    }

    public ExperimentalSetup(GeneralizedSemPm pm) {
        this.generalizedSemPm = pm;
        this.pmType = PmType.generalized;

        SemGraph graph = pm.getGraph();
        graph.setShowErrorTerms(false);
        this.gNat = new EdgeListGraph(graph);
        this.gManip = new EdgeListGraph(gNat);
    }

    private void updateManipulated() {
        if (esType == EsType.simpleSurgical) {
            addSimpleSurgicalEdges(gNat, gManip);
        }
        else if (esType == EsType.simpleSoft) {
            addSimpleSoftEdges(gNat, gManip);
        }
        else if (esType == EsType.fullExperimental) {
            addFullExperimentalEdges(gNat, gManip);
        }
        else if (esType == EsType.counterfactualExperimental) {
            addCounterfactualExperimentalEdges(gNat, gManip);
        }
    }

    private void addSimpleSurgicalEdges(Graph gNat, Graph gManip) {
        removeCausalEdges(gManip);

        EDGE:
        for (Edge edge : gNat.getEdges()) {
            Node to = Edges.getDirectedEdgeHead(edge);
            List<Node> parents = gNat.getParents(to);

            for (Node node : parents) {
                if (node.getNodeType() == NodeType.LOCK) {
                    continue EDGE;
                }

                if (node.getNodeType() == NodeType.RANDOMIZE) {
                    continue EDGE;
                }

                gManip.addEdge(edge);
            }
        }
    }

    private void addSimpleSoftEdges(Graph gNat, Graph gManip) {
        removeCausalEdges(gManip);

        //To change body of created methods use File | Settings | File Templates.
    }

    private void addFullExperimentalEdges(Graph gNat, Graph gManip) {
        removeCausalEdges(gManip);

        //To change body of created methods use File | Settings | File Templates.
    }

    private void addCounterfactualExperimentalEdges(Graph gNat, Graph gManip) {
        removeCausalEdges(gManip);

        //To change body of created methods use File | Settings | File Templates.
    }

    private void removeCausalEdges(Graph manipulatedGraph) {
        for (Edge edge : manipulatedGraph.getEdges()) {
            if (edge.getNode1().getNodeType() == NodeType.LOCK) continue;
            if (edge.getNode1().getNodeType() == NodeType.RANDOMIZE) continue;
            manipulatedGraph.removeEdge(edge);
        }
    }


    public EsType getEsType() {
        return esType;
    }

    public void setEsType(EsType esType) {
        this.esType = esType;
    }

}
