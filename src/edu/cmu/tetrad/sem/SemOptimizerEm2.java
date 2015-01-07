package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.util.*;

import java.util.List;

/**
 * Optimizes a DAG SEM with hidden variables using expectation-maximization.
 * IT SHOULD NOT BE USED WITH SEMs THAT ARE NOT DAGS. For DAGs without hidden
 * variables, SemOptimizerRegression should be more efficient.
 *
 * @author Ricardo Silva
 * @author Joseph Ramsey Cleanup, modernization.
 */
public class SemOptimizerEm2 implements SemOptimizer {
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
    private TetradMatrix implCovarC;
    private TetradMatrix implCovarMeasC;
    private TetradMatrix edgeCoef;
    private TetradMatrix errCovar;

    private int sampleSize;
    private double fml;
    private double logDetSample;

    public SemOptimizerEm2() {
    }

    public void optimize(SemIm semIm) {
        boolean showErrors = semIm.getSemPm().getGraph().isShowErrorTerms();
        semIm.getSemPm().getGraph().setShowErrorTerms(true);

        initialize(semIm);
        updateMatrices();
        double score, newScore = scoreSemIm();
        do {
            score = newScore;
            System.out.println(score);
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
    public static SemOptimizerEm2 serializableInstance() {
        return new SemOptimizerEm2();
    }

    public String toString() {
        return "Sem Optimizer EM2";
    }

    //==============================PRIVATE METHODS========================//

    private void initialize(SemIm semIm) {
//        this.semIm = semIm;
        this.edgeCoef = semIm.getEdgeCoef();
        this.errCovar = semIm.getErrCovar();
        this.sampleSize = semIm.getSampleSize();
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
                    edgeCoef.set(idx2, idx, coefs.get(i));
//                    if (!semIm.getSemPm().getParameter(nodes.get(idx2), node).isFixed()) {
//                        semIm.setEdgeCoef(nodes.get(idx2), node, coefs.get(i));
//                    }
                }

                variance -= new TetradVector(nodeParentsCov[idx]).dotProduct(coefs);
            }

            errCovar.set(idx, idx, variance);

//            if (!semIm.getSemPm().getParameter(errorParent[idx], errorParent[idx]).isFixed()) {
//                semIm.setErrCovar(errorParent[idx], variance);
//            }
        }
    }

    private void updateMatrices() {
        computeImpliedCovar();
        TetradMatrix impliedCovar = implCovarC;
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
        return -getFml();
    }


    /**
     * The value of the maximum likelihood function for the getModel the model
     * (Bollen 107). To optimize, this should be minimized.
     */
    public double getFml() {
        if (!Double.isNaN(this.fml)) {
            return this.fml;
        }

        TetradMatrix implCovarMeas; // Do this once.

        try {
            implCovarMeas = implCovarMeas();
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NaN;
        }

        TetradMatrix sampleCovar = sampleCovar();

        double logDetSigma = logDet(implCovarMeas);
        double traceSSigmaInv = traceABInv(sampleCovar, implCovarMeas);
        double logDetSample = logDetSample();
        int pPlusQ = numObserved;

//        System.out.println("Sigma = " + implCovarMeas);
//        System.out.println("Sample = " + sampleCovar);

//        System.out.println("log(det(sigma)) = " + logDetSigma + " trace = " + traceSSigmaInv
//         + " log(det(sample)) = " + logDetSample + " p plus q = " + pPlusQ);

        double fml = logDetSigma + traceSSigmaInv - logDetSample - pPlusQ;

//        System.out.println("FML = " + fml);

        if (Math.abs(fml) < 0) {//1e-14) {
            fml = 0.0;
        }

        this.fml = fml;
        return fml;
    }

    private double traceABInv(TetradMatrix A, TetradMatrix B) {

        // Note that at this point the sem and the sample covar MUST have the
        // same variables in the same order.
        TetradMatrix inverse = null;
        try {
            inverse = B.inverse();
        } catch (Exception e) {
            System.out.println(B);
            e.printStackTrace();
        }
        TetradMatrix product = A.times(inverse);

        double trace = product.trace();

//        double trace = MatrixUtils.trace(product);

        if (trace < -1e-8) {
            throw new IllegalArgumentException("Trace was negative: " + trace);
        }

        return trace;
    }

    private double logDetSample() {
        if (logDetSample == 0.0 && sampleCovar() != null) {
            double det = sampleCovar().det();
            logDetSample = Math.log(det);
        }

        return logDetSample;
    }

    public double getLogLikelihood() {
        TetradMatrix SigmaTheta; // Do this once.

        try {
            SigmaTheta = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        TetradMatrix sStar = sampleCovar();

        double logDetSigmaTheta = logDet(SigmaTheta);
        double traceSStarSigmaInv = traceABInv(sStar, SigmaTheta);
        int pPlusQ = numObserved;

        return -(getSampleSize() / 2.) * pPlusQ * Math.log(2 * Math.PI)
                - (getSampleSize() / 2.) * logDetSigmaTheta
                - (getSampleSize() / 2.) * traceSStarSigmaInv;
    }

//    public double getScore() {
//        TetradMatrix sigmaTheta; // Do this once.
//
//        try {
//            sigmaTheta = implCovarMeas();
//        } catch (Exception e) {
////            e.printStackTrace();
//            return Double.NaN;
//        }
//
//        TetradMatrix s = sampleCovar();
//        TetradMatrix sInv = TetradAlgebra.inverse(s);
//        TetradMatrix prod = TetradAlgebra.times(sigmaTheta, sInv);
//        double trace = TetradAlgebra.trace(prod);
//
//        double detSigmaTheta = TetradAlgebra.det(sigmaTheta);
//        double detS = TetradAlgebra.det(s);
//
//        return Math.log(detSigmaTheta) + trace - Math.log(detS) - getNumFreeParams();
//    }

    public double getFml2() {
        TetradMatrix sigma; // Do this once.

        try {
            sigma = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        TetradMatrix s = sampleCovar();

        TetradMatrix sInv = s.inverse();

        TetradMatrix prod = sigma.times(sInv);
        TetradMatrix identity = TetradAlgebra.identity(s.rows());
//        prod.assign(identity, PlusMult.plusMult(-1));
        prod = prod.minus(identity);
        double trace = prod.times(prod).trace();
        double f = 0.5 * trace;

//        System.out.println(f);

        return f;
    }

    /**
     * The negative  of the log likelihood function for the getModel model, with
     * the constant chopped off. (Bollen 134). This is an alternative, more
     * efficient, optimization function to Fml which produces the same result
     * when minimized.
     */
    public double getTruncLL() {
        // Formula Bollen p. 263.

        TetradMatrix Sigma = implCovarMeas();

        // Using (n - 1) / n * s as in Bollen p. 134 causes sinkholes to open
        // up immediately. Not sure why.
        TetradMatrix S = sampleCovar();
        int n = getSampleSize();
        return -(n - 1) / 2. * (logDet(Sigma) + traceAInvB(Sigma, S));
//        return -(n / 2.0) * (logDet(Sigma) + traceABInv(S, Sigma));
//        return -(logDet(Sigma) + traceABInv(S, Sigma));
//        return -(n - 1) / 2 * (logDet(Sigma) + traceABInv(S, Sigma));
    }

    private TetradMatrix sampleCovar() {
        return yCov;
    }

    private TetradMatrix implCovarMeas () {
        computeImpliedCovar();
        return this.implCovarMeasC;
    }

    /**
     * Computes the implied covariance matrices of the Sem. There are two:
     * <code>implCovar </code> contains the covariances of all the variables and
     * <code>implCovarMeas</code> contains covariance for the measured variables
     * only.
     */
    private void computeImpliedCovar() {

        // Note. Since the sizes of the temp matrices in this calculation
        // never change, we ought to be able to reuse them.
        this.implCovarC = MatrixUtils.impliedCovar(edgeCoef.transpose(), errCovar);

        // Submatrix of implied covar for measured vars only.
        int size = numObserved;
        this.implCovarMeasC = new TetradMatrix(size, size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
//                Node iNode = getMeasuredNodes().get(i);
//                Node jNode = getMeasuredNodes().get(j);
//
//                int _i = getVariableNodes().indexOf(iNode);
//                int _j = getVariableNodes().indexOf(jNode);

                this.implCovarMeasC.set(i, j, this.implCovarC.get(i, j));
            }
        }
    }


    /**
     * Returns BIC score, calculated as chisq - dof. This is equal to getFullBicScore() up to a constant.
     */
    public double getBicScore() {
        int dof = getDof();
        return getChiSquare() - dof * Math.log(getSampleSize());

//        return getChiSquare() + dof * Math.log(getSampleSize());

//        CovarianceMatrix covarianceMatrix = new CovarianceMatrix(getVariableNodes(), getImplCovar(), getSampleSize());
//        Ges ges = new Ges(covarianceMatrix);
//        return -ges.getScore(getSemIm().getGraph());
    }

    public double getAicScore() {
        int dof = getDof();
        return getChiSquare() - 2 * dof;
//
//        return getChiSquare() + dof * Math.log(sampleSize);

//        CovarianceMatrix covarianceMatrix = new CovarianceMatrix(getVariableNodes(), getImplCovar(), getSampleSize());
//        Ges ges = new Ges(covarianceMatrix);
//        return -ges.getScore(getSemIm().getGraph());
    }

//    /**
//     * Returns the BIC score, without subtracting constant terms.
//     */
//    public double getFullBicScore() {
////        int dof = getSemIm().getDof();
//        int sampleSize = getSampleSize();
//        double penalty = getNumFreeParams() * Math.log(sampleSize);
////        double penalty = getSemIm().getDof() * Math.log(sampleSize);
//        double L = getLogLikelihood();
//        return -2 * L + penalty;
//    }

    /**
     * Returns the chi square value for the model.
     */
    public double getChiSquare() {
        return (getSampleSize() - 1) * getFml();
    }

    /**
     * Returns the p-value for the model.
     */
    public double getPValue() {
        double pValue = 1.0 - ProbUtils.chisqCdf(getChiSquare(), getDof());
//        System.out.println("P value = " + pValue);
        return pValue;
//        return (1.0 - ProbUtils.chisqCdf(getChiSquare(), semPm.getDof()));
    }


    public int getSampleSize() {
        return sampleSize;
    }

    public int getDof() {
        return (numObserved * (numObserved + 1)) / 2 - getNumFreeParams();
    }

    public int getNumFreeParams() {
        int countEdges = 0;

        for (int i = 0; i < edgeCoef.rows(); i++) {
            for (int j = 0; j < edgeCoef.columns(); j++) {
                if (edgeCoef.get(i, j) != 0) {
                    countEdges++;
                }
            }
        }

        return countEdges + numObserved + numLatent;
    }

    private double logDet(TetradMatrix matrix2D) {
        return Math.log(matrix2D.det());
    }

    private double traceAInvB(TetradMatrix A, TetradMatrix B) {

        // Note that at this point the sem and the sample covar MUST have the
        // same variables in the same order.
        TetradMatrix inverse = A.inverse();
        TetradMatrix product = inverse.times(B);

        double trace = product.trace();

//        double trace = MatrixUtils.trace(product);

        if (trace < -1e-8) {
            throw new IllegalArgumentException("Trace was negative: " + trace);
        }

        return trace;
    }



    public TetradMatrix getErrCovar() {
        return errCovar;
    }

    public TetradMatrix getImplCovar() {
        return implCovarC;
    }

    public TetradMatrix getImplCovarMeas() {
        return implCovarMeasC;
    }
}
