package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.IndependenceFact;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradMatrix;
import edu.pitt.dbmi.algo.bayesian.constraint.inference.BCInference;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses BCInference by Cooper and Bui to calculate probabilistic conditional independence judgments.
 *
 * @author Joseph Ramsey 3/2014
 */
public class IndTestProbabilisticVerbose implements IndependenceTest {

    /**
     * Calculates probabilities of independence for conditional independence facts.
     */
    private final BCInference bci;

    /**
     * The data set for which conditional  independence judgments are requested.
     */
    private final DataSet data;
    private final PrintWriter out;

    private List<Node> nodes;

    /**
     * Indices of the nodes.
     */
    private Map<Node, Integer> indices;

    /**
     * A map from independence facts to their probabilities of independence.
     */
    private Map<IndependenceFact, Double> H;
    private double posterior;

    /**
     * Initializes the test using a discrete data sets.
     */
    public IndTestProbabilisticVerbose(DataSet dataSet, PrintWriter out) {
        this.out = out;

        if (!dataSet.isDiscrete()) {
            throw new IllegalArgumentException("Not a discrete data set.");

        }

        this.data = dataSet;

        int[] nodeDimensions = new int[dataSet.getNumColumns() + 2];

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            DiscreteVariable variable = (DiscreteVariable) (dataSet.getVariable(j));
            int numCategories = variable.getNumCategories();
//            out.println("Variable " + variable + " # cat = " + numCategories);
            nodeDimensions[j + 1] = numCategories;
        }

        int[][] cases = new int[dataSet.getNumRows() + 1][dataSet.getNumColumns() + 2];

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                cases[i + 1][j + 1] = dataSet.getInt(i, j) + 1;
            }
        }

        bci = new BCInference(cases, nodeDimensions);

        nodes = dataSet.getVariables();

        indices = new HashMap<Node, Integer>();

        for (int i = 0; i < nodes.size(); i++) {
            indices.put(nodes.get(i), i);
        }

        this.H = new HashMap<IndependenceFact, Double>();
    }

    @Override
    public IndependenceTest indTestSubset(List<Node> vars) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIndependent(Node x, Node y, List<Node> z) {
        Node[] _z = z.toArray(new Node[z.size()]);
        return isIndependent(x, y, _z);
    }

    @Override
    public boolean isIndependent(Node x, Node y, Node... z) {
        IndependenceFact key = new IndependenceFact(x, y, z);

        if (!H.containsKey(key)) {
            double pInd = probConstraint(BCInference.OP.independent, x, y, z);
            H.put(key, pInd);
        }

        double pInd = H.get(key);

        double p = probOp(BCInference.OP.independent, pInd);

        this.posterior = p;

        boolean ind = RandomUtil.getInstance().nextDouble() < p;

        out.print((nodes.indexOf(x) + 1) + " ");
        out.print((nodes.indexOf(y) + 1) + (z.length > 0 ? " " : ""));

        for (int i = 0; i < z.length; i++) {
            out.print(nodes.indexOf(z[i]) + 1);

            if (i < z.length - 1) {
                out.print(" ");
            }
        }

        out.print(",");
        out.print(ind ? 1 : 0 + ",");
        out.print(p);

        out.println();

        if (ind) {
            return true;
        }
        else {
            return false;
        }
    }

    public double probConstraint(BCInference.OP op, Node x, Node y, Node[] z) {

        int _x = indices.get(x) + 1;
        int _y = indices.get(y) + 1;

        int[] _z = new int[z.length + 1];
        _z[0] = z.length;
        for (int i = 0; i < z.length; i++) {
            _z[i + 1] = indices.get(z[i]) + 1;
        }

//        out.println("test " + _x + " _||_ " + _y + " | " + Arrays.toString(_z));

        return bci.probConstraint(op, _x, _y, _z);
    }

    @Override
    public boolean isDependent(Node x, Node y, List<Node> z) {
        Node[] _z = z.toArray(new Node[z.size()]);
        return !isIndependent(x, y, _z);
    }

    @Override
    public boolean isDependent(Node x, Node y, Node... z) {
        return !isIndependent(x, y, z);
    }

    @Override
    public double getPValue() {
        return posterior;
    }

    @Override
    public List<Node> getVariables() {
        return nodes;
    }

    @Override
    public Node getVariable(String name) {
        for (Node node : nodes) {
            if (name.equals(node.getName())) return node;
        }

        return null;
    }

    @Override
    public List<String> getVariableNames() {
        List<String> names = new ArrayList<String>();

        for (Node node : nodes) {
            names.add(node.getName());
        }
        return names;
    }

    @Override
    public boolean determines(List<Node> z, Node y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getAlpha() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlpha(double alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataModel getData() {
        return data;
    }

    @Override
    public ICovarianceMatrix getCov() {
        return null;
    }

    @Override
    public List<DataSet> getDataSets() {
        return null;
    }

    @Override
    public int getSampleSize() {
        return 0;
    }

    @Override
    public List<TetradMatrix> getCovMatrices() {
        return null;
    }

    public Map<IndependenceFact, Double> getH() {
        return new HashMap<IndependenceFact, Double>(H);
    }

    private double probOp(BCInference.OP type, double pInd) {
        double probOp;

        if (BCInference.OP.independent == type) {
            probOp = pInd;
        }
        else {
            probOp = 1.0 - pInd;
        }

        return probOp;
    }

    public double getPosterior() {
        return posterior;
    }

    /**
     * The nodes of the data set.
     */
    public List<Node> getNodes() {
        return nodes;
    }
}
