package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.util.TetradMatrix;
import edu.cmu.tetrad.util.TetradVector;

import java.util.List;

/**
 * Optimizes a DAG SEM with hidden variables using expectation-maximization.
 * IT SHOULD NOT BE USED WITH SEMs THAT ARE NOT DAGS. For DAGs without hidden
 * variables, SemOptimizerRegression should be more efficient.
 *
 * @author Ricardo Silva
 * @author Joseph Ramsey Cleanup, modernization.
 */
public class SemOptimizerEm implements SemOptimizer {
    static final long serialVersionUID = 23L;

    private static final double FUNC_TOLERANCE = 1.0e-6;

    private SemIm semIm;
    private SemGraph graph;

    private TetradMatrix yCov;   // Sample cov.
    private TetradMatrix yCovModel, yzCovModel, zCovModel; // Partitions of the modeled cov.
    private TetradMatrix expectedCov;

    private int numObserved, numLatent;
    private int idxLatent[], idxObserved[];

    private int[][] parents;
    private Node[] errorParent;
    private double[][] nodeParentsCov;
    private double[][][] parentsCov;

    public SemOptimizerEm() {}

    public void optimize(SemIm semIm) {
        boolean showErrors = semIm.getSemPm().getGraph().isShowErrorTerms();
        semIm.getSemPm().getGraph().setShowErrorTerms(true);

        initialize(semIm);
        updateMatrices();
        double score, newScore = scoreSemIm();
        do {
            score = newScore;
            expectation();
            maximization();
            updateMatrices();
            newScore = scoreSemIm();
        } while (newScore > score + FUNC_TOLERANCE);

        semIm.getSemPm().getGraph().setShowErrorTerms(showErrors);
    }

    public TetradMatrix getExpectedCovarianceMatrix() {
        return new TetradMatrix(expectedCov);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemOptimizerEm serializableInstance() {
        return new SemOptimizerEm();
    }

    //==============================PRIVATE METHODS========================//

    private void initialize(SemIm semIm) {
        this.semIm = semIm;
        graph = semIm.getSemPm().getGraph();
        yCov = semIm.getSampleCovar();
        numObserved = 0;
        numLatent = 0;
        List<Node> nodes = graph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.getNodeType() == NodeType.LATENT) {
                numLatent++;
            } else if (node.getNodeType() == NodeType.MEASURED) {
                numObserved++;
            }
        }

        idxLatent = new int[numLatent];
        idxObserved = new int[numObserved];
        int countLatent = 0;
        int countObserved = 0;

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.getNodeType() == NodeType.LATENT) {
                idxLatent[countLatent++] = i;
            } else if (node.getNodeType() == NodeType.MEASURED) {
                idxObserved[countObserved++] = i;
            }
        }

        expectedCov = new TetradMatrix(numObserved + numLatent, numObserved + numLatent);

        for (int i = 0; i < numObserved; i++) {
            for (int j = i; j < numObserved; j++) {
                expectedCov.set(idxObserved[i], idxObserved[j], yCov.get(i, j));
                expectedCov.set(idxObserved[j], idxObserved[i], yCov.get(i, j));
            }
        }

        yCovModel = new TetradMatrix(numObserved, numObserved);
        yzCovModel = new TetradMatrix(numObserved, numLatent);
        zCovModel = new TetradMatrix(numLatent, numLatent);

        parents = new int[numLatent + numObserved][];
        errorParent = new Node[numLatent + numObserved];
        nodeParentsCov = new double[numLatent + numObserved][];
        parentsCov = new double[numLatent + numObserved][][];
        for (Node node : nodes) {
            if (node.getNodeType() == NodeType.ERROR) {
                continue;
            }
            int idx = nodes.indexOf(node);
            List<Node> _parents = graph.getParents(node);
            for (int i = 0; i < _parents.size(); i++) {
                Node nextParent = _parents.get(i);
                if (nextParent.getNodeType() == NodeType.ERROR) {
                    errorParent[idx] = nextParent;
                    _parents.remove(nextParent);
                    break;
                }
            }
            if (_parents.size() > 0) {
                parents[idx] = new int[_parents.size()];
                nodeParentsCov[idx] = new double[_parents.size()];
                parentsCov[idx] = new double[_parents.size()][_parents.size()];
                for (int i = 0; i < _parents.size(); i++) {
                    parents[idx][i] = nodes.indexOf(_parents.get(i));
                }
            } else {
                parents[idx] = null;
            }
        }
    }

    public String toString() {
        return "Sem Optimizer EM";
    }

    private void expectation() {
        TetradMatrix bYZModel = yCovModel.inverse().times(yzCovModel);
        TetradMatrix yzCovPred = yCov.times(bYZModel);
        TetradMatrix zCovModel = yzCovModel.transpose().times(bYZModel);
        TetradMatrix zCovDiff = this.zCovModel.minus(zCovModel);
        TetradMatrix CzPred = yzCovPred.transpose().times(bYZModel);
        TetradMatrix newCz = CzPred.plus(zCovDiff);

        for (int i = 0; i < numLatent; i++) {
            for (int j = i; j < numLatent; j++) {
                expectedCov.set(idxLatent[i], idxLatent[j], newCz.get(i, j));
                expectedCov.set(idxLatent[j], idxLatent[i], newCz.get(j, i));
            }
        }

        for (int i = 0; i < numLatent; i++) {
            for (int j = 0; j < numObserved; j++) {
                double v = yzCovPred.get(j, i);
                expectedCov.set(idxLatent[i], idxObserved[j], v);
                expectedCov.set(idxObserved[j], idxLatent[i], v);
            }
        }
    }

    private void maximization() {
        List<Node> nodes = graph.getNodes();

        for (Node node : graph.getNodes()) {
            if (node.getNodeType() == NodeType.ERROR) {
                continue;
            }

            int idx = nodes.indexOf(node);
            double variance = expectedCov.get(idx, idx);

            if (parents[idx] != null) {
                for (int i = 0; i < parents[idx].length; i++) {
                    int idx2 = parents[idx][i];
                    nodeParentsCov[idx][i] = expectedCov.get(idx, idx2);
                    for (int j = i; j < parents[idx].length; j++) {
                        int idx3 = parents[idx][j];
                        parentsCov[idx][i][j] = expectedCov.get(idx2, idx3);
                        parentsCov[idx][j][i] = expectedCov.get(idx3, idx2);
                    }
                }

                TetradVector coefs = new TetradMatrix(parentsCov[idx]).inverse().times(new TetradVector(nodeParentsCov[idx]));

                for (int i = 0; i < coefs.size(); i++) {
                    int idx2 = parents[idx][i];
                    if (!semIm.getSemPm().getParameter(nodes.get(idx2), node).isFixed()) {
                        semIm.setEdgeCoef(nodes.get(idx2), node, coefs.get(i));
                    }
                }

                variance -= new TetradVector(nodeParentsCov[idx]).dotProduct(coefs);
            }

            if (!semIm.getSemPm().getParameter(errorParent[idx], errorParent[idx]).isFixed()) {
                semIm.setErrCovar(errorParent[idx], variance);
            }
        }
    }

    private void updateMatrices() {
        TetradMatrix impliedCovar = semIm.getImplCovar();
        for (int i = 0; i < numObserved; i++) {
            for (int j = i; j < numObserved; j++) {
                yCovModel.set(i, j, impliedCovar.get(idxObserved[i], idxObserved[j]));
                yCovModel.set(j, i, impliedCovar.get(idxObserved[i], idxObserved[j]));
            }
            for (int j = 0; j < numLatent; j++) {
                yzCovModel.set(i, j, impliedCovar.get(idxObserved[i], idxLatent[j]));
            }
        }
        for (int i = 0; i < numLatent; i++) {
            for (int j = i; j < numLatent; j++) {
                zCovModel.set(i, j, impliedCovar.get(idxLatent[i], idxLatent[j]));
                zCovModel.set(j, i, impliedCovar.get(idxLatent[i], idxLatent[j]));
            }
        }
    }

    private double scoreSemIm() {
        return -semIm.getFml();
    }

}
