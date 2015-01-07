package edu.cmu.tetrad.search;

import edu.cmu.tetrad.cluster.KMeans;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradMatrix;
import edu.cmu.tetrad.util.TetradVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.round;

/**
 * Created by josephramsey on 9/23/14.
 */
public class TestHippocampusUtils {
    static boolean printChart(int[][][] cube, TetradMatrix coords, int coord1, int coord2, String roi,
                              String projection, String fact, boolean flipLR, boolean flipUD, PrintWriter out,
                              int threshold) {
        int[][][] cube2 = new int[cube.length][cube[0].length][cube[0][0].length];

        for (int x = 0; x < cube.length; x++) {
            for (int y = 0; y < cube[0].length; y++) {
                for (int z = 0; z < cube[0][0].length; z++) {
                    cube2[x][y][z] = -1;
                }
            }
        }

        for (int x = 0; x < cube.length; x++) {
            for (int y = 0; y < cube[0].length; y++) {
                for (int z = 0; z < cube[0][0].length; z++) {
                    if (cube[x][y][z] >= threshold) {
                        cube2[x][y][z] = cube[x][y][z];
                    } else if (cube[x][y][z] >= 0) {
                        cube2[x][y][z] = 0;
                    }
                }
            }
        }

        int[][] Z = new int[max(cube2)[coord1]][max(cube2)[coord2]];

        int[] _coords = new int[3];
        for (int i = 0; i < 3; i++) _coords[i] = -1;

        String type = "Marginal Max";
//        String type = "Marginal Avg";
//        String type = "Marginal Sum";

        for (int s = 0; s < max(cube2)[coord1]; s++) {
            for (int t = 0; t < max(cube2)[coord2]; t++) {
                _coords[coord1] = s;
                _coords[coord2] = t;

                if ("Marginal Avg".equals(type)) {
                    Z[s][t] = avg(cube2, _coords);
                }
                if ("Marginal Max".equals(type)) {
                    Z[s][t] = max(cube2, _coords);
                }
                if ("Marginal Sum".equals(type)) {
                    Z[s][t] = sum(cube2, _coords);
                }
            }
        }

        System.out.println("\n" + projection + " view. Showing " + roi + " for test " + fact + ". " + type + "\n");

        if (out != null) {
            out.println("\n" + projection + " view. Showing " + roi + " for test " + fact + ". " + type + "\n");
        }

        Z = addAxes(Z, min(coords, coord1), min(coords, coord2));

        if (flipLR) {
            Z = flipLR(Z);
        }

        if (flipUD) {
            Z = flipUD(Z);
        }

        for (int r = 0; r < Z.length; r++) {
            for (int s = 0; s < Z[0].length; s++) {
                int value = Z[r][s];

                if (value == -1) {
                    System.out.print("  " + " ");
                    if (out != null) {
                        out.print("  " + " ");
                    }
                } else if (value == 0) {
                    System.out.print(". " + " ");
                    if (out != null) {
                        out.print(". " + " ");
                    }
                } else if (value >= 10) {
                    System.out.print(new DecimalFormat("00").format(value) + " ");
                    if (out != null) {
                        out.print(new DecimalFormat("00").format(value) + " ");
                    }
                } else {
                    System.out.print(value + " " + " ");
                    if (out != null) {
                        out.print(value + " " + " ");
                    }
                }
            }

            System.out.println();
            if (out != null) {
                out.println();
            }
        }

        System.out.println();
        if (out != null) {
            out.println();
        }

        return true;
    }

    private static int[][] addAxes(int[][] z, int min1, int min2) {
        int[][] Z2 = new int[z.length + 4][z[0].length + 4];

        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                Z2[i + 2][j + 2] = z[i][j];
            }
        }

        for (int i : new int[]{0, 1, Z2.length - 2, Z2.length - 1}) {
            for (int j = 0; j < Z2[0].length; j++) {
                Z2[i][j] = -1;
            }
        }

        for (int i = 0; i < Z2.length; i++) {
            for (int j : new int[]{0, 1, Z2[0].length - 2, Z2[0].length - 1}) {
                Z2[i][j] = -1;
            }
        }

        for (int i = 0; i < z.length; i++) {
            Z2[i + 2][0] = min1 + i;
            Z2[i + 2][Z2[0].length - 1] = min1 + i;
        }

        for (int i = 0; i < z.length; i++) {
            Z2[i + 2][0] = min1 + i;
            Z2[i + 2][Z2[0].length - 1] = min1 + i;
        }

        for (int i = 0; i < z[0].length; i++) {
            Z2[0][i + 2] = min2 + i;
            Z2[Z2.length - 1][i + 2] = min2 + i;
        }

        return Z2;
    }

    private static int sum(int[][][] X, int[] coords) {
        int x = coords[0];
        int y = coords[1];
        int z = coords[2];

        int xlow = x == -1 ? 0 : x;
        int xhigh = x == -1 ? X.length - 1 : x;
        int ylow = y == -1 ? 0 : y;
        int yhigh = y == -1 ? X[0].length - 1 : y;
        int zlow = z == -1 ? 0 : z;
        int zhigh = z == -1 ? X[0][0].length - 1 : z;

        int sum = 0;
        int count = 0;

        for (int i = xlow; i <= xhigh; i++) {
            for (int j = ylow; j <= yhigh; j++) {
                for (int m = zlow; m <= zhigh; m++) {
                    int c = X[i][j][m];

                    if (c >= 0) {
                        sum += c;
                        count++;
                    }
                }
            }
        }

        return count == 0 ? -1 : sum;
    }

    private static int avg(int[][][] X, int[] coords) {
        int x = coords[0];
        int y = coords[1];
        int z = coords[2];

        int xlow = x == -1 ? 0 : x;
        int xhigh = x == -1 ? X.length - 1 : x;
        int ylow = y == -1 ? 0 : y;
        int yhigh = y == -1 ? X[0].length - 1 : y;
        int zlow = z == -1 ? 0 : z;
        int zhigh = z == -1 ? X[0][0].length - 1 : z;

        int sum = 0;
        int count = 0;

        for (int i = xlow; i <= xhigh; i++) {
            for (int j = ylow; j <= yhigh; j++) {
                for (int m = zlow; m <= zhigh; m++) {
                    int c = X[i][j][m];

                    if (c >= 0) {
                        sum += c;
                        count++;
                    }
                }
            }
        }

        return count == 0 ? -1 : (int) round(sum / (double) count);
    }

    private static int max(int[][][] X, int[] coords) {
        int x = coords[0];
        int y = coords[1];
        int z = coords[2];

        int xlow = x == -1 ? 0 : x;
        int xhigh = x == -1 ? X.length - 1 : x;
        int ylow = y == -1 ? 0 : y;
        int yhigh = y == -1 ? X[0].length - 1 : y;
        int zlow = z == -1 ? 0 : z;
        int zhigh = z == -1 ? X[0][0].length - 1 : z;

        int max = 0;
        int count = 0;

        for (int i = xlow; i <= xhigh; i++) {
            for (int j = ylow; j <= yhigh; j++) {
                for (int m = zlow; m <= zhigh; m++) {
                    int c = X[i][j][m];

                    if (c >= 0) {
                        if (c > max) {
                            max = c;
                        }

                        count++;
                    }
                }
            }
        }

        return count == 0 ? -1 : max;
    }

    private static int[] max(int[][][] X) {
        int[] max = new int[3];

        max[0] = X.length;
        max[1] = X[0].length;
        max[2] = X[0][0].length;

        return max;
    }

    // Tallies indices 0 through k if dependent, or  nonzero through k if dependent, as a 3D map.
    static int[][][] threeDView(List<Integer> sortedIndices, int k, TetradMatrix coords, boolean independent, int nonzero) {
        int min0 = min(coords, 0);
        int min1 = min(coords, 1);
        int min2 = min(coords, 2);
        int max0 = max(coords, 0);
        int max1 = max(coords, 1);
        int max2 = max(coords, 2);

        int[][][] X = new int[max0 - min0 + 1][max1 - min1 + 1][max2 - min2 + 1];

        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                for (int m = 0; m < X[0][0].length; m++) {
                    X[i][j][m] = -1;
                }
            }
        }

//        for (int g = 0; g < sortedIndices.size(); g++) {
//            int index = sortedIndices.get(g);
//            TetradVector coord = coords.getRow(index);
//            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2] = 0;
//        }

        for (int g = 0; g < coords.rows(); g++) {
            TetradVector coord = coords.getRow(g);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2] = 0;
        }

        for (int g = independent ? nonzero : 0; g < k; g++) {
            int index = sortedIndices.get(g);
            TetradVector coord = coords.getRow(index);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2]++;
        }

        return X;
    }

    static int[][][] threeDView2(List<Integer> sortedIndices, int k, TetradMatrix coords, boolean independent, int nonzero) {
        int min0 = min(coords, 0);
        int min1 = min(coords, 1);
        int min2 = min(coords, 2);
        int max0 = max(coords, 0);
        int max1 = max(coords, 1);
        int max2 = max(coords, 2);

        int[][][] X = new int[max0 - min0 + 1][max1 - min1 + 1][max2 - min2 + 1];

        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                for (int m = 0; m < X[0][0].length; m++) {
                    X[i][j][m] = -1;
                }
            }
        }

        for (int g = 0; g < sortedIndices.size(); g++) {
            int index = sortedIndices.get(g);
            TetradVector coord = coords.getRow(index);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2] = 0;
        }

        for (int g = independent ? nonzero : 0; g < k; g++) {
            int index = sortedIndices.get(g);
            TetradVector coord = coords.getRow(index);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2]++;
        }

        return X;
    }

    // Makes a 3D matrix in which each coordinate is mapped to its coordinate index--i.e. its index among
    // the list of variables.
    static int[][][] coordIndices(TetradMatrix coords) {
        int min0 = min(coords, 0);
        int min1 = min(coords, 1);
        int min2 = min(coords, 2);
        int max0 = max(coords, 0);
        int max1 = max(coords, 1);
        int max2 = max(coords, 2);

        int[][][] X = new int[max0 - min0 + 1][max1 - min1 + 1][max2 - min2 + 1];

        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                for (int m = 0; m < X[0][0].length; m++) {
                    X[i][j][m] = -1;
                }
            }
        }

        for (int i = 0; i < coords.rows(); i++) {
            TetradVector coord = coords.getRow(i);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2] = i;
        }

        return X;
    }

    static int[][] flipLR(int[][] x) {
        int[][] x2 = new int[x.length][x[0].length];

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                x2[i][j] = x[i][x[0].length - j - 1];
            }
        }

        return x2;
    }

    static int[][] flipUD(int[][] x) {
        int[][] x2 = new int[x.length][x[0].length];

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                x2[i][j] = x[x.length - i - 1][j];
            }
        }

        return x2;
    }

    static int min(TetradMatrix m, int col) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < m.rows(); i++) {
            if (m.get(i, col) < min) min = (int) m.get(i, col);
        }

        return min;
    }

    static int max(TetradMatrix m, int col) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < m.rows(); i++) {
            if (m.get(i, col) > max) max = (int) m.get(i, col);
        }

        return max;
    }

    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static TetradMatrix subMatrix(ICovarianceMatrix m, List<Node> x, List<Node> y, List<Node> z) {
        List<Node> variables = m.getVariables();
        TetradMatrix _covMatrix = m.getMatrix();

        // Create index array for the given variables.
        int[] indices = new int[x.size() + y.size() + z.size()];

        for (int i = 0; i < x.size(); i++) {
            indices[i] = variables.indexOf(x.get(i));
        }

        for (int i = 0; i < y.size(); i++) {
            indices[x.size() + i] = variables.indexOf(y.get(i));
        }

        for (int i = 0; i < z.size(); i++) {
            indices[x.size() + y.size() + i] = variables.indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        TetradMatrix submatrix = _covMatrix.getSelection(indices, indices);

        return submatrix;
    }

    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static TetradMatrix subset(DataSet m, List<Node> x, List<Node> y, List<Node> z) {
        List<Node> variables = new ArrayList<Node>();
        variables.addAll(x);
        variables.addAll(y);
        variables.addAll(z);
        return m.subsetColumns(variables).getDoubleData();
    }

    // DON'T CHANGE. 2014/11/5
    static boolean printOutMaps(Node x, Node y, List<Node> z, Map<Node, List<Node>> nodeMap, CovarianceMatrix cov,
                                PrintWriter out, double alpha, Map<Node, TetradMatrix> coords, List<int[][][]> all3D,
                                boolean verbose) {
        List<Node> aa = nodeMap.get(x);
        List<Node> bb = nodeMap.get(y);

//        int[][] twod = new int[aa.size()][bb.size()];

        List<Node> cc = new ArrayList<Node>();

        for (Node _z : z) {
            cc.addAll(nodeMap.get(_z));
        }

        TetradMatrix submatrix = subMatrix(cov, aa, bb, cc);

        TetradMatrix inverse;
        int rank;

        try {
            inverse = submatrix.inverse();
            rank = inverse.columns();
        } catch (Exception e) {
            SingularValueDecomposition svd
                    = new SingularValueDecomposition(submatrix.getRealMatrix());
            RealMatrix _inverse = svd.getSolver().getInverse();
            inverse = new TetradMatrix(_inverse, _inverse.getRowDimension(), _inverse.getColumnDimension());
            rank = svd.getRank();
        }

        final List<Double> pValues = new ArrayList<Double>();
        List<Integer> _i = new ArrayList<Integer>();
        List<Integer> _m = new ArrayList<Integer>();

        System.out.println("# voxels for " + x.getName() + " = " + aa.size());
        System.out.println("# voxels for " + y.getName() + " = " + bb.size());
        System.out.println("# p values = " + aa.size() * bb.size());

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = cov.getSampleSize() - 1 - rank;

                if (dof < 0) {
                    out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                    dof = 0;
                }

                double z_ = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z_)));

                pValues.add(p);
                _i.add(i);
                _m.add(m);
            }
        }

        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < pValues.size(); i++) {
            indices.add(i);
        }

        Collections.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return pValues.get(o1).compareTo(pValues.get(o2));
            }
        });

        List<Double> pValues2 = new ArrayList<Double>();
        List<Integer> _iIndices = new ArrayList<Integer>();
        List<Integer> _mIndices = new ArrayList<Integer>();

        for (int _y = 0; _y < indices.size(); _y++) {
            pValues2.add(pValues.get(indices.get(_y)));
            _iIndices.add(_i.get(indices.get(_y)));
            _mIndices.add(_m.get(indices.get(_y)));
        }

//        int k = StatUtils.fdr(alpha, pValues2, false);
//        double cutoff = StatUtils.fdrCutoff(alpha, pValues2, false);
        double cutoff = alpha;
        int k = -1;

        for (int i = pValues2.size() - 1; i >= 0; i--) {
            if (pValues2.get(i) < cutoff) {
                k = i;
                break;
            }
        }

        System.out.println("** cutoff = " + cutoff);
        System.out.println("k = " + k);

        int nonzero = -1;

        for (int i = 0; i < pValues2.size(); i++) {
            if (pValues2.get(i) != 0) {
                nonzero = i;
                break;
            }
        }

        boolean dependent = k > nonzero;
        boolean independent = !dependent;

        TetradMatrix xCoords = coords.get(x);
        TetradMatrix yCoords = coords.get(y);

        int[][][] X = threeDView(_iIndices, k, xCoords, independent, nonzero);
        int[][][] Y = threeDView(_mIndices, k, yCoords, independent, nonzero);

        all3D.add(X);
        all3D.add(Y);




        String fact = SearchLogUtils.independenceFact(x, y, z);

        // Printing out stuff for Ruben. First print out dependent voxels.

        List<int[][][]> cubes = new ArrayList<int[][][]>();
        cubes.add(X);
        cubes.add(Y);

//        int thresholdX = tryClustering3D(X);
//        int thresholdY = tryClustering3D(Y);

        int _threshold = tryClustering3Ds(cubes);

        out.println("Threshold = " + _threshold);

//        2.Second file, contain a list of all the dependencies between voxels.
//
//        10 -- 50
//        30 -- 2

        int[][][] Cx = getC(X);
        int[][][] Cy = getC(Y);

        out.println("\n\n" + fact);

        for (int g = independent ? nonzero : 0; g < k; g++) {
            int i = getIndex(_iIndices, Cx, g, coords.get(x));
            int j = getIndex(_mIndices, Cy, g, coords.get(y));

            if (i == -1 || j == -1) throw new IllegalArgumentException();

            out.println(i + " -- " + j);
        }

        out.println();


//        1. First file, containing info of both ROIs and all their voxels.
//        Example:
//
//        ROI_LABEL  voxel_LABEL  COORDINATES  #Dependencies
//        ENT          10         -80 50 38     6
//        CA1          50         -70 15 90     2

        printDependencies(x, fact, X, Cx, out);
        printDependencies(y, fact, Y, Cy, out);


        // OK back to work.
        int xCount = countAboveThreshold(X, _threshold);
        int yCount = countAboveThreshold(Y, _threshold);

        System.out.println("Total above threshold count = " + (xCount + yCount));
        out.println("Total above threshold count = " + (xCount + yCount));

        boolean thresholdIndep = !(xCount > 0 && yCount > 0);

        String projection;

        projection = "Axial";

        printChart(X, xCoords, 0, 1, x.getName(), projection, fact, false, false, out, _threshold);
        printChart(Y, yCoords, 0, 1, y.getName(), projection, fact, false, false, out, _threshold);

        projection = "Coronal";

        printChart(X, xCoords, 0, 2, x.getName(), projection, fact, true, false, out, _threshold);
        printChart(Y, yCoords, 0, 2, y.getName(), projection, fact, true, false, out, _threshold);

        projection = "Saggital";

        printChart(X, xCoords, 1, 2, x.getName(), projection, fact, true, false, out, _threshold);
        printChart(Y, yCoords, 1, 2, y.getName(), projection, fact, true, false, out, _threshold);

//        List<List<Node>> ret2 = getThresholdedLists(alpha, nodeMap, coords, cov, out, threshold, x, y, z);
//        List<Node> listx = ret2.get(0);
//        List<Node> listy = ret2.get(1);

//        if (thresholdIndep) {
        if (independent) {
            if (verbose) {
                System.out.println("Independent");
                out.println("Independent");
            }

            out.flush();
            return true;
        } else {
            if (verbose) {
                System.out.println("Dependent\n");
                out.println("Dependent\n");
            }

            out.flush();
            return false;
        }
    }

    static int tryClustering3D(int[][][] cube) {
        System.out.println("Clustering 3D's");

        List<List<Double>> points = new ArrayList<List<Double>>();

        for (int i = 0; i < cube.length; i++) {
            for (int j = 0; j < cube[0].length; j++) {
                for (int k = 0; k < cube[0][0].length; k++) {
                    if (cube[i][j][k] >= 0) {
                        List<Double> point = new ArrayList<Double>();
                        if (cube[i][j][k] != -1) {
                            point.add((double) cube[i][j][k]);
                            points.add(point);
                        }

                    }
                }
            }
        }

        TetradMatrix matrix = new TetradMatrix(points.size(), 1);

        for (int i = 0; i < points.size(); i++) {
            matrix.set(i, 0, points.get(i).get(0));
        }

        KMeans kmeans = KMeans.randomClusters(2);

        kmeans.cluster(matrix);

        List<List<Integer>> clusters = kmeans.getClusters();

        int[] minima = new int[2];

        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + i);

            List<Integer> cluster = clusters.get(i);

            List<Integer> values = new ArrayList<Integer>();

            for (int p : cluster) values.add((int) matrix.getRow(p).get(0));

//            for (int j = 0; j < cluster.size(); j++) {
//                System.out.println(MatrixUtils.toString(matrix.getRow(cluster.get(j)).toArray()));
//            }

            Collections.sort(values);
            System.out.println("min = " + values.get(0) + " max = " + values.get(cluster.size() - 1));

            minima[i] = values.get(0);
        }

        return Math.max(minima[0], minima[1]);

    }

    static int tryClustering3Ds(List<int[][][]> all3D) {
        System.out.println("Clustering 3D's");

        List<List<Double>> points = new ArrayList<List<Double>>();

        for (int[][][] cube : all3D) {
            for (int i = 0; i < cube.length; i++) {
                for (int j = 0; j < cube[0].length; j++) {
                    for (int k = 0; k < cube[0][0].length; k++) {
                        if (cube[i][j][k] >= 0) {
                            List<Double> point = new ArrayList<Double>();
                            if (cube[i][j][k] != -1) {
                                point.add((double) cube[i][j][k]);
                                points.add(point);
                            }

                        }
                    }
                }
            }
        }

        TetradMatrix matrix = new TetradMatrix(points.size(), 1);

        for (int i = 0; i < points.size(); i++) {
            matrix.set(i, 0, points.get(i).get(0));
        }

        KMeans kmeans = KMeans.randomClusters(2);

        kmeans.cluster(matrix);

        List<List<Integer>> clusters = kmeans.getClusters();

        int[] minima = new int[2];

        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + i);

            List<Integer> cluster = clusters.get(i);

            List<Integer> values = new ArrayList<Integer>();

            for (int p : cluster) values.add((int) matrix.getRow(p).get(0));

//            for (int j = 0; j < cluster.size(); j++) {
//                System.out.println(MatrixUtils.toString(matrix.getRow(cluster.get(j)).toArray()));
//            }

            Collections.sort(values);
            System.out.println("min = " + values.get(0) + " max = " + values.get(cluster.size() - 1));


            minima[i] = values.get(0);
        }

        return Math.max(minima[0], minima[1]);

    }



    static List<int[][][]> get3DMaps(Node x, Node y, List<Node> z, Map<Node, List<Node>> nodeMap, CovarianceMatrix cov,
                                     PrintWriter out, double alpha, Map<Node, TetradMatrix> coords) {
        List<Node> aa = nodeMap.get(x);
        List<Node> bb = nodeMap.get(y);
        List<Node> cc = new ArrayList<Node>();

        for (Node _z : z) {
            cc.addAll(nodeMap.get(_z));
        }

        TetradMatrix submatrix = subMatrix(cov, aa, bb, cc);

        TetradMatrix inverse;
        int rank;

        try {
            inverse = submatrix.inverse();
            rank = inverse.columns();
        } catch (Exception e) {
            SingularValueDecomposition svd
                    = new SingularValueDecomposition(submatrix.getRealMatrix());
            RealMatrix _inverse = svd.getSolver().getInverse();
            inverse = new TetradMatrix(_inverse, _inverse.getRowDimension(), _inverse.getColumnDimension());
            rank = svd.getRank();
        }

        final List<Double> pValues = new ArrayList<Double>();
        List<Integer> _i = new ArrayList<Integer>();
        List<Integer> _m = new ArrayList<Integer>();

//        System.out.println("# voxels for " + x.getName() + " = " + aa.size());
//        System.out.println("# voxels for " + y.getName() + " = " + bb.size());
//        System.out.println("# p values = " + aa.size() * bb.size());

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = cov.getSampleSize() - 1 - rank;

                if (dof < 0) {
                    out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                    dof = 0;
                }

                double z_ = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z_)));

                pValues.add(p);
                _i.add(i);
                _m.add(m);
            }
        }

        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < pValues.size(); i++) {
            indices.add(i);
        }

        Collections.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return pValues.get(o1).compareTo(pValues.get(o2));
            }
        });

        List<Double> pValues2 = new ArrayList<Double>();
        List<Integer> _iIndices = new ArrayList<Integer>();
        List<Integer> _mIndices = new ArrayList<Integer>();

        for (int _y = 0; _y < indices.size(); _y++) {
            pValues2.add(pValues.get(indices.get(_y)));
            _iIndices.add(_i.get(indices.get(_y)));
            _mIndices.add(_m.get(indices.get(_y)));
        }

//        int k = StatUtils.fdr(alpha, pValues2, false);
//        double cutoff = StatUtils.fdrCutoff(alpha, pValues2, false);
        double cutoff = alpha;
        int k = -1;

        for (int i = pValues2.size() - 1; i >= 0; i--) {
            if (pValues2.get(i) < cutoff) {
                k = i;
                break;
            }
        }

        System.out.println("** cutoff = " + cutoff);
        System.out.println("k = " + k);

        int nonzero = -1;

        for (int i = 0; i < pValues2.size(); i++) {
            if (pValues2.get(i) != 0) {
                nonzero = i;
                break;
            }
        }

        boolean dependent = k > nonzero;
        boolean independent = !dependent;

        TetradMatrix xCoords = coords.get(x);
        TetradMatrix yCoords = coords.get(y);

        int[][][] X = threeDView(_iIndices, k, xCoords, independent, nonzero);
        int[][][] Y = threeDView(_mIndices, k, yCoords, independent, nonzero);

        List<int[][][]> ret = new ArrayList<int[][][]>();

        ret.add(X);
        ret.add(Y);

        return ret;
    }

    private static int getIndex(List<Integer> iIndices, int[][][] C, int g, TetradMatrix coords) {
        int min0 = min(coords, 0);
        int min1 = min(coords, 1);
        int min2 = min(coords, 2);
        TetradVector coord = coords.getRow(iIndices.get(g));
        return C[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2];
    }

    private static void printDependencies(Node nx, String fact, int[][][] X, int[][][] C, PrintWriter out) {

        out.println("\n\n" + fact);
//        1. First file, containing info of both ROIs and all their voxels.
//        Example:
//
//        ROI_LABEL  voxel_LABEL  COORDINATES  #Dependencies
//        ENT          10         -80 50 38     6
//        CA1          50         -70 15 90     2

        for (int x = 0; x < X.length; x++) {
            for (int y = 0; y < X[0].length; y++) {
                for (int z = 0; z < X[0][0].length; z++) {
                    if (X[x][y][z] != -1) {
                        out.println(nx.getName() + "\t" + C[x][y][z] + "\t" + x + "\t" + y + "\t" + z + "\t" + X[x][y][z]);
                    }
                }
            }
        }

        out.println();
    }

    private static int[][][] getC(int[][][] X) {
        int[][][] C = new int[X.length][X[0].length][X[0][0].length];

        int index = 0;

        for (int x = 0; x < X.length; x++) {
            for (int y = 0; y < X[0].length; y++) {
                for (int z = 0; z < X[0][0].length; z++) {
                    if (X[x][y][z] != -1) {
                        C[x][y][z] = index++;
                    } else {
                        C[x][y][z] = -1;
                    }
                }
            }
        }

        return C;
    }

    private static int countAboveThreshold(int[][][] X, int threshold) {
        int count = 0;

        for (int x = 0; x < X.length; x++) {
            for (int y = 0; y < X[0].length; y++) {
                for (int z = 0; z < X[0][0].length; z++) {
                    if (X[x][y][z] >= threshold) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    static Map<Node, List<Node>> threshold(int[][][] ints, Map<Node, List<Node>> nodeMap, Node x) {
        List<Node> aa = nodeMap.get(x);

        for (int i = 0; i < aa.size(); i++) {

        }


        return null;
    }

    public static List<Node> getThresholdedList(List<Node> nodes, TetradMatrix coords,
                                                int threshold, int[][][] map) {
        int[][][] indexMap = coordIndices(coords);

        // This is a subset of nodes.
        List<Node> nodes2 = new ArrayList<Node>();

        for (int _x = 0; _x < indexMap.length; _x++) {
            for (int _y = 0; _y < indexMap[0].length; _y++) {
                for (int _z = 0; _z < indexMap[0][0].length; _z++) {
                    boolean aboveThreshold = map[_x][_y][_z] > threshold;

                    if (aboveThreshold) {
                        int m = map[_x][_y][_z];
                        int index = indexMap[_x][_y][_z];
                        nodes2.add(nodes.get(index));
                    }
                }
            }
        }

        return nodes2;
    }
}
