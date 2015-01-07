///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 3002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //                                           f
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.regression.RegressionCovariance;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.*;
import edu.cmu.tetradapp.workbench.GraphWorkbench;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.MarshalledObject;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;
import static java.lang.StrictMath.pow;

/**
 * Tests the PC search.
 *
 * @author Joseph Ramsey
 */
public class TestHippocampus extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestHippocampus(String name) {
        super(name);
    }

    private enum IndTestType {RoiMeans, RoiMeansConditionalCorr, Voxelwise}

    //    double gSum = 0.0;
//    int gCount = 0;
//    double gMin = Double.POSITIVE_INFINITY;
//    double gMax = Double.NEGATIVE_INFINITY;
    int numDependent = 0;
    int numTests = 0;

    public void test1() {
//        RandomUtil.getInstance().setSeed(1949392L);

        int sampleSize = 1000;
        int v = 50;
        int o = 5;
        double fanout = 10; // The average number of nodes in B connected to a node in A for A->B.
        double probInterEdge = fanout / v;
        double probIntraEdge = fanout / v;
        double probIntraTwoCycleGivenEdge = 0.;

        System.out.println("Sample size " + sampleSize);
        System.out.println("# voxels per ROI = " + v);
        System.out.println("# voxels per move " + o);
        System.out.println("Fanout = " + fanout);
        System.out.println("% 2 cycles in ROI = " + probIntraTwoCycleGivenEdge);

        IndTestType testType = IndTestType.RoiMeans;
        double alpha;

        if (testType == IndTestType.RoiMeans) {
            alpha = 1e-15;
        } else {
            alpha = .05;
        }

        System.out.println("Test type = " + testType);

        System.out.println("alpha = " + alpha);

        // Still using this range for variances.
        double varLow = 1;
        double varHigh = 2;

        System.out.println("Coefficients in N(.4, .1) restricted to [.2, .6]");
        System.out.println("Variances in (1, 2)");


//        for (int problemNumber : new int[]{7}) {
//        for (int problemNumber : new int[]{1, 2, 3, 5}) {
        for (int problemNumber : new int[]{8}) {
            Map<List<Node>, Map<String, int[]>> tables = new LinkedHashMap<List<Node>, Map<String, int[]>>();
            List<List<Node>> problems = null;
            String[] _graphs = null;

            System.out.println("\n\nPROBLEM # " + problemNumber + "\n");

            if (problemNumber == 1) {
                String[] fakeNames = {"A", "B", "C"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "C"));
                problems.add(problem(roiNodes, "A", "C", "B"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = new String[]{"A-->B,B-->C", "A<--B,B-->C", "A-->B,B<--C"};
                    boolean[][] indep = {{false, false, true}, {true, true, false}};

                    _graphs = graphs;

                    for (int j = 0; j < graphs.length; j++) {
                        String graphSpec = graphs[j];

                        int[][] trueVoxellation = constructRois(v, v, v);
                        String[] trueNames = {"A", "B", "C"};

                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation;

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "B");
                        fakeVoxellations.put("LL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellations.put("LN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "C");
                        fakeVoxellations.put("LR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "C", "B");
                        fakeVoxellations.put("NL", fakeVoxellation);

                        fakeVoxellation = trueVoxellation;
                        fakeVoxellations.put("NN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "C");
                        fakeVoxellations.put("NR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "B");
                        fakeVoxellations.put("RL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellations.put("RN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "C");
                        fakeVoxellations.put("RR", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphSpec, counts);
                    }
                }
            } else if (problemNumber == 2) {
                String[] fakeNames = {"A", "B", "C", "D"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "D"));
                problems.add(problem(roiNodes, "A", "D", "B"));
                problems.add(problem(roiNodes, "A", "D", "C"));
                problems.add(problem(roiNodes, "A", "D", "B", "C"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"A-->B,A-->C,B-->D,C-->D"};
                    _graphs = graphs;

                    boolean[][] indep = {{false}, {false}, {false}, {true}};

                    for (int j = 0; j < graphs.length; j++) {
                        int[][] trueVoxellation = constructRois(v, v, v, v);

                        String[] trueNames = {"A", "B", "C", "D"};
                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation;

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "C");
                        fakeVoxellations.put("LL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "D");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "D");
                        fakeVoxellations.put("LM", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "D");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "D");
                        fakeVoxellations.put("LR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "C");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "C");
                        fakeVoxellations.put("ML", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "C");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "D");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "D");
                        fakeVoxellations.put("MM", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "C");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "D");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "D");
                        fakeVoxellations.put("MR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "A", "C");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "C");
                        fakeVoxellations.put("RL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "A", "C");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "D");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "D", "D");
                        fakeVoxellations.put("RM", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "A", "C");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "D");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "D");
                        fakeVoxellations.put("RR", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);

                    }
                }
            }

            if (problemNumber == 3) {
                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                String[] fakeNames = {"A", "B", "C", "X"};

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "C"));
                problems.add(problem(roiNodes, "A", "C", "B"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"A-->B,B-->C,X", "A<--B,B-->C,X", "A-->B,B<--C,X"};

                    boolean[][] indep = {{false, false, true}, {true, true, false}};

                    _graphs = graphs;

                    for (int j = 0; j < graphs.length; j++) {
                        int[][] trueVoxellation = constructRois(v, v, v, v);
                        String[] trueNames = {"A", "B", "C", "X"};

                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "B");

//                    fakeVoxellations.put("Tr", trueVoxellation);
                        fakeVoxellations.put("Mix", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);
                    }
                }
            }
            if (problemNumber == 4) {
                String[] fakeNames = {"A", "B", "C", "X", "Y", "Z"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "C"));
                problems.add(problem(roiNodes, "A", "C", "B"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"A-->B,B-->C,X,Y,Z", "A<--B,B-->C,X,Y,Z",
                            "A-->B,B<--C,X,Y,Z"};
                    boolean[][] indep = {{false, false, true}, {true, true, false}};


                    _graphs = graphs;
                    for (int j = 0; j < graphs.length; j++) {
                        int[][] trueVoxellation = constructRois(v, v, v, v, v, v);
                        String[] trueNames = {"A", "B", "C", "X", "Y", "Z"};

                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation;

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "X");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("LL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "X");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("LN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "X");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Z");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("LR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("NL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("NN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Z");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("NR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("RL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("RN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Z");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("RR", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);

                    }
                }
            }
            if (problemNumber == 5) {
                String[] fakeNames = {"A", "B", "C", "X", "Y", "Z"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "C"));
                problems.add(problem(roiNodes, "A", "C", "B"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"A-->B,B-->C,X-->A,Y-->B,Z-->C", "A<--B,B-->C,X-->A,Y-->B,Z-->C",
                            "A-->B,B<--C,X-->A,Y-->B,Z-->C"};
                    boolean[][] indep = {{false, false, true}, {true, true, false}};

                    _graphs = graphs;
                    for (int j = 0; j < graphs.length; j++) {
                        int[][] trueVoxellation = constructRois(v, v, v, v, v, v);

                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation;
                        String[] trueNames = {"A", "B", "C", "X", "Y", "Z"};

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "X");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("LL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "X");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("LN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "X");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Z");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("LR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("NL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("NN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Z");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("NR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("RL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("RN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "X", "Y");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "Z");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "X", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Y", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "Z", "C");
                        fakeVoxellations.put("RR", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);

                    }
                }

            } else if (problemNumber == 6) {
                String[] fakeNames = {"I", "A", "B", "C", "D", "E"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "E"));
                problems.add(problem(roiNodes, "A", "B", "E"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"I,A-->B,B-->C,C-->D,D-->E,I->A,I->B,I->C,I->D,I->E"};
                    boolean[][] indep = {{false, false, true}, {true, true, false}};

                    _graphs = graphs;
                    for (int j = 0; j < graphs.length; j++) {
                        int[][] trueVoxellation = constructRois(1, v, v, v, v, v);

                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation;
                        String[] trueNames = {"I", "A", "B", "C", "D", "E"};

                        fakeVoxellation = move(trueNames, trueVoxellation, v, "C", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, v, "D", "B");
                        fakeVoxellations.put("LL", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[i], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);

                    }

                }
            } else if (problemNumber == 7) {
                String[] fakeNames = {"I", "A", "B", "C"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "C", "I"));
                problems.add(problem(roiNodes, "A", "C", "B", "I"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"I,I-->A,I-->B,I-->C,A-->B,B-->C",
                            "I,I-->A,I-->B,I-->C,A<--B,B-->C",
                            "I,I-->A,I-->B,I-->C,A-->B,B<--C"};

                    boolean[][] indep = {{false, false, true}, {true, true, false}};

                    _graphs = graphs;
                    for (int j = 0; j < graphs.length; j++) {
                        int[][] trueVoxellation = constructRois(1, v, v, v);
                        String[] trueNames = {"I", "A", "B", "C"};

                        Map<String, int[][]> fakeVoxellations = new TreeMap<String, int[][]>();

                        int[][] fakeVoxellation;

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "B");
                        fakeVoxellations.put("LL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellations.put("LN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "A");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "C");
                        fakeVoxellations.put("LR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "C", "B");
                        fakeVoxellations.put("NL", fakeVoxellation);

                        fakeVoxellation = trueVoxellation;
                        fakeVoxellations.put("NN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellations.put("RN", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "C", "B");
                        fakeVoxellations.put("RL", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "B", "C");
                        fakeVoxellations.put("NR", fakeVoxellation);

                        fakeVoxellation = move(trueNames, trueVoxellation, o, "A", "B");
                        fakeVoxellation = move(trueNames, fakeVoxellation, o, "B", "C");
                        fakeVoxellations.put("RR", fakeVoxellation);

//                    printVoxellations(fakeVoxellations);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);

                    }
                }
            } else if (problemNumber == 8) {
                String[] fakeNames = {"A", "Bp", "C"};

                Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

                for (String name : fakeNames) {
                    roiNodes.put(name, new ContinuousVariable(name));
                }

                problems = new ArrayList<List<Node>>();
                problems.add(problem(roiNodes, "A", "C", "Bp"));

                for (int i = 0; i < problems.size(); i++) {
                    tables.put(problems.get(i), new HashMap<String, int[]>());

                    String[] graphs = {"A-->B,B-->C"};
                    _graphs = graphs;

                    boolean[][] indep = {{true}};

                    for (int j = 0; j < graphs.length; j++) {
                        String[] trueNames = {"A", "B", "C"};
                        int[][] trueVoxellation = constructRois(v, v, v);
                        int[][] fakeVoxellation = delete(trueNames, trueVoxellation, v / 2, "B");

                        Map<String, int[][]> fakeVoxellations = new HashMap<String, int[][]>();
                        fakeVoxellations.put("True", trueVoxellation);
                        fakeVoxellations.put("Mod", fakeVoxellation);

                        int[] counts = runTest(problems.get(i), graphs[j], indep[i][j], trueNames, fakeNames,
                                trueVoxellation, fakeVoxellations, roiNodes,
                                alpha, sampleSize, probInterEdge, probIntraEdge, probIntraTwoCycleGivenEdge,
                                testType, varLow, varHigh);

                        tables.get(problems.get(i)).put(graphs[j], counts);

                    }
                }
            }


            System.out.println("Problem # " + problemNumber);

            for (List<Node> nodes : problems) {
                Node x = nodes.get(0);
                Node y = nodes.get(1);
                List<Node> rest = new ArrayList<Node>(nodes);
                rest.remove(x);
                rest.remove(y);

                for (String graph : _graphs) {
                    System.out.print(independenceFact(x, y, rest) + "\t" + graph + "\t");

                    int[] counts = tables.get(nodes).get(graph);

                    for (int i = 0; i < counts.length; i++) {
                        System.out.print((counts[i] * 10) + "\t");
                    }

                    System.out.println();
                }
            }
        }

//        System.out.println("gavg = " + (gSum / gCount));
//        System.out.println("gmin = " + gMin);
//        System.out.println("gmax = " + gMax);
//        System.out.println("ratio = " + (numDependent / (double) numTests));
//        System.out.println("Interpolation = " + (gMin + 0.1 * (gMax - gMin)));
    }

    public void test1a() {
        String[] fakeNames = {"PRC", "PHC", "ENT", "CA32DG", "CA1", "SUB"};

        Map<String, Node> roiNodes = new LinkedHashMap<String, Node>();

        for (String name : fakeNames) {
            roiNodes.put(name, new ContinuousVariable(name));
        }

        String graph = "PRC-->ENT1,PNC-->ENT1,ENT1-->DG,DG-->CA3,CA3-->CA2,CA2-->CA2P,CA2P-->CA1," +
                "CA1-->SUB,SUB-->ENT2";

        int v = 50;

        int[][] trueVoxellation = constructRois(1, v, v, v, v, v, v, v, v, v, v, 0);
        String[] trueNames = {"PRC", "PHC", "ENT1", "DG", "CA3", "CA2", "CA2P", "CA1", "SUB", "ENT2", "OTHER"};

        int[][] fakeVoxellations;

        fakeVoxellations = move(trueNames, trueVoxellation, v, "", "A");

        Graph g = GraphUtils.randomDag(100, 100, false);

        try {
            Graph g2 = (Graph) new MarshalledObject(g).get();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test2() {
        String graphSpec = "X1-->X2,X2-->X3,X3-->X4,X4-->X5,X1-->X5";
//        String graphSpec = "A-->X1,B-->X1,X1-->X2,X2-->X3,X3-->X4,X4-->X5,X5-->X1";
//        String graphSpec = "X1-->X2,X2-->X3,X3-->X4,X4<--X5";
//        String graphSpec = "X1-->X2,X2-->X3,X3-->X4,X4-->X5";

        int sampleSize = 400;
        int v = 30;
        double fanout = 10; // The average number of nodes in B connected to a node in A for A->B.
        double probInterEdge = fanout / v;
        double probIntraEdge = fanout / v;
        double probIntraTwoCycleGivenEdge = .25;
        double varLow = 1.;
        double varHigh = 2;

        int[][] trueVoxellation = constructRois(v, v, v, v, v);
        String[] trueNames = {"X1", "X2", "X3", "X4", "X5"};

        Graph trueGraph = GraphConverter.convert(graphSpec);

        System.out.println("True graph " + trueGraph);

        int[][] fakeVoxellation = trueVoxellation;
        String[] fakeNames = {"X1", "X2", "X3", "X4", "X5"};

        Map<String, Node> roiNodes = new HashMap<String, Node>();

        for (String name : fakeNames) {
            roiNodes.put(name, new ContinuousVariable(name));
        }

        List<Node> _roiNodes = new ArrayList<Node>();
        for (String s : fakeNames) _roiNodes.add(roiNodes.get(s));

        List<Node> trueVars = new ArrayList<Node>();
        for (String name : trueNames) {
            trueVars.add(trueGraph.getNode(name));
        }

        int numVoxels = numVoxels(trueVoxellation);

        Graph detailGraph = constructDetailGraph(trueVoxellation, trueGraph, trueVars, probInterEdge, probIntraEdge,
                probIntraTwoCycleGivenEdge, numVoxels);
        List<Node> detailVars = detailGraph.getNodes();

        List<Graph> detailGraphs = new ArrayList<Graph>();
        detailGraphs.add(detailGraph);

        for (int i = 1; i < 10; i++) {
            Graph detailGraph2 = constructDetailGraph(trueVoxellation, trueGraph, trueVars, probInterEdge, probIntraEdge,
                    probIntraTwoCycleGivenEdge, numVoxels);
            detailGraphs.add(detailGraph2);
        }

        List<SemIm> ims = new ArrayList<SemIm>();
        List<DataSet> dataSets = new ArrayList<DataSet>();

        for (Graph graph : detailGraphs) {
            SemIm im = parameterizeSem(varLow, varHigh, 0.4, 0.1, graph);
            // Simulate data from the SEM.
            DataSet data = im.simulateData(sampleSize, false);

            ims.add(im);
            dataSets.add(data);
        }


        CovarianceMatrix cov0 = new CovarianceMatrix(dataSets.get(0));

        Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

        for (int i = 0; i < fakeVoxellation.length; i++) {
            nodeMap.put(_roiNodes.get(i), listVars(fakeVoxellation[i], detailVars));
        }

        IndependenceTest test = new IndTestMultiFisherZ(nodeMap, cov0, 1e-4, dataSets.get(0));

        Pc pc = new Pc(test);
        System.out.println("PC voxelwise" + pc.search());

        Ccd ccd = new Ccd(test);
        System.out.println("CCD voxelwise single data set" + ccd.search());

        List<DataSet> roiDatasets = new ArrayList<DataSet>();

        for (DataSet data : dataSets) {
//            double[][] aggregates = calcRoiMeans(sampleSize, data, fakeVoxellation);
//        double[][] aggregates = greatestVariance(sampleSize, data, fakeVoxellation);
            double[][] aggregates = calcFirstPrincipleComponent(sampleSize, data, fakeVoxellation);
            DataSet roiDataset = ColtDataSet.makeContinuousData(_roiNodes, aggregates);
            roiDatasets.add(roiDataset);
        }

        IndependenceTest test1 = new IndTestFisherZ(roiDatasets.get(0), .05);
        Pc pc2 = new Pc(test1);
        System.out.println("PC ROI means single data set alpha 0.05" + pc2.search());

        test1 = new IndTestFisherZ(roiDatasets.get(0), .001);
        pc2 = new Pc(test1);
        System.out.println("PC ROI means single data set alpha 0.001" + pc2.search());

        test1 = new IndTestFisherZ(roiDatasets.get(0), .0001);
        pc2 = new Pc(test1);
        System.out.println("PC ROI means single data set alpha 0.0001" + pc2.search());

        Ges ges = new Ges(roiDatasets.get(0));
        ges.setPenaltyDiscount(1);
        System.out.println("GES ROI means" + ges.search());

        IndependenceTest test2 = new IndTestFisherZConcatenateResiduals(roiDatasets, .05);

        Pc pc3 = new Pc(test2);
        System.out.println("PC ROI means concatenate residuals" + pc3.search());

        Images images = new Images(roiDatasets);
        images.setPenaltyDiscount(1);
        System.out.println("Images ROI means discount 1" + ges.search());

        images = new Images(roiDatasets);
        images.setPenaltyDiscount(5);
        System.out.println("Images ROI means penalty discount 5" + images.search());

        images = new Images(roiDatasets);
        images.setPenaltyDiscount(10);
        System.out.println("Images ROI means penalty discount 10" + images.search());

        images = new Images(roiDatasets);
        images.setPenaltyDiscount(20);
        System.out.println("Images ROI sums penalty discount 20" + images.search());

        images = new Images(roiDatasets);
        images.setPenaltyDiscount(30);
        System.out.println("Images ROI means penalty discount 30" + images.search());

        images = new Images(roiDatasets);
        images.setPenaltyDiscount(40);
        System.out.println("Images ROI means penalty discount 40" + images.search());

        images = new Images(roiDatasets);
        images.setPenaltyDiscount(50);
        System.out.println("Images ROI means penalty discount 50" + images.search());
    }

    public void testNormalCdf() {
        System.out.println("x\tCDF(x)");

//        for (int i = 0; i <= 20; i++) {
//            System.out.println(i + "\t" + RandomUtil.getInstance().normalCdf(0, 1, (double) i));
//        }

        for (double r = 0.0; r <= 1e-18; r += 1e-19) {
            double _z = sqrt(100) * 0.5 * (log(1.0 + r) - log(1.0 - r));
            double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));
            System.out.println(r + "\t" + p);
        }
    }

    double gAvg = .5;

    public void test7() {
        int numDays = 2;
        double alpha = .001;
        double discount = 1;

        DataReader reader = new DataReader();
        reader.setVariablesSupplied(false);

        try {
//            int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

            int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

//            int[] days = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
            int[] _days = {
                    14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 32, 35, 36, 37, 38, 39,
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                    50, 51, 53, 54, 56, 57, 58, 59,
                    60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                    70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                    80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                    91, 92, 94, 95, 96, 97, 98, 99,
                    100, 101, 102, 103, 104
            };
//            int[] days = {26, 27, 28, 29, 30, 32, 40, 41, 42, 43, 44, 45};
//            int[] days = {32};

            List<Node> nodes = new ArrayList<Node>();

            String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

            for (int i = 0; i < varIndices.length; i++) {
                Node node = new ContinuousVariable(names[varIndices[i] - 1]);
                nodes.add(node);
                System.out.println(node);
            }

            Map<Edge, Integer> countsVoxelwise = new HashMap<Edge, Integer>();
            Map<Edge, Integer> countsRoiMeans = new HashMap<Edge, Integer>();

            for (int r = 0; r < _days.length / numDays; r++) {
                int[] days = new int[numDays];

                for (int j = 0; j < numDays; j++) {
                    days[j] = _days[r * numDays + j];
                }

                System.out.println();
                System.out.println("r = " + r + " days = " + Arrays.toString(days));

                System.out.println("alpha = " + alpha);

                List<DataSet> datasets = new ArrayList<DataSet>();

                for (int i : varIndices) {
                    List<DataSet> d = new ArrayList<DataSet>();

                    NumberFormat nf = new DecimalFormat("000");

                    for (int j = 0; j < days.length; j++) {
                        DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                                "sub" + nf.format(days[j]) + "_mtl_" + i + ".txt"));

                        System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                        d.add(f);
                    }

                    DataSet c = DataUtils.concatenateData(d);
                    datasets.add(c);
                }

                DataSet data = DataUtils.collectVariables(datasets);

                System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                CovarianceMatrix cov = new CovarianceMatrix(data);

                System.out.println("Calculated cov");


                Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

                for (int i = 0; i < varIndices.length; i++) {
                    nodeMap.put(nodes.get(i), datasets.get(i).getVariables());
                }

                IndTestMultiFisherZ test = new IndTestMultiFisherZ(nodeMap, cov, alpha, data);
                test.setVerbose(true);

                List<Node> nodes1 = new ArrayList<Node>(nodeMap.keySet());
                double _gAvg = searchForCutoff(test, nodes1);

                double cutoff = discount * _gAvg;

                test = new IndTestMultiFisherZ(nodeMap, cov, alpha, data);
                test.setVerbose(true);

                Fas fas2 = new Fas(test);
                fas2.setDepth(2);
                Graph graph = fas2.search();
                System.out.println("r = " + r + " days = " + Arrays.toString(days));
                System.out.println("Voxelwise FAS " + graph);

                System.out.println("Gavg = " + test.gAvg());
                System.out.println("Percent dependent = " + test.percentDependent());

                List<Double> gValues = test.gValues();

                double[] _gValues = new double[gValues.size()];
                for (int i = 0; i < gValues.size(); i++) _gValues[i] = gValues.get(i);

                for (Edge edge : graph.getEdges()) {
                    increment(countsVoxelwise, edge);
                }

                double[][] aggregates = calcRoiMeans2(datasets);
                DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);

                IndependenceTest test2 = new IndTestFisherZ(roiDataset, 1e-13);
                Fas fas = new Fas(test2);
                Graph g2 = fas.search();
                System.out.println("ROI means " + g2);
//
//            Ccd ccd2 = new Ccd(test2);
//            System.out.println("ROI means " + ccd2.search());

                for (Edge edge : g2.getEdges()) {
                    increment(countsRoiMeans, edge);
                }

            }

            Graph complete = new EdgeListGraph(nodes);
            complete.fullyConnect(Endpoint.TAIL);

            System.out.println("Voxelwise:");

            List<Edge> allEdges = complete.getEdges();
            Collections.sort(allEdges);

            for (Edge edge : allEdges) {
                Integer integer = countsVoxelwise.get(edge);
                System.out.println(edge + "\t" + (integer == null ? 0 : integer));
            }

            System.out.println("\nRoi Means:");

            for (Edge edge : allEdges) {
                Integer integer = countsRoiMeans.get(edge);
                System.out.println(edge + "\t" + (integer == null ? 0 : integer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double searchForCutoff(IndTestMultiFisherZ test, List<Node> nodes1) {
        double _gAvg = 0;

        FOR1:
        for (int i = 0; i < nodes1.size(); i++) {
            for (int j = i + 1; j < nodes1.size(); j++) {
                Node x = nodes1.get(i);
                Node y = nodes1.get(j);

                test.isIndependent(x, y);

                double _gAvgNew = test.gAvg();
                if (Math.abs(_gAvg - _gAvgNew) < 0.0001) break FOR1;
                _gAvg = _gAvgNew;
            }
        }

        FOR2:
        for (int i = 0; i < nodes1.size(); i++) {
            for (int j = i + 1; j < nodes1.size(); j++) {
                for (int k = 0; k < nodes1.size(); k++) {
                    if (k == i || k == j) continue;

                    Node x = nodes1.get(i);
                    Node y = nodes1.get(j);
                    Node z = nodes1.get(k);

                    test.isIndependent(x, y, z);

                    double _gAvgNew = test.gAvg();
                    if (Math.abs(_gAvg - _gAvgNew) < 0.0001) break FOR2;
                    _gAvg = _gAvgNew;
                }
            }
        }

        return _gAvg;
    }

    private double searchForCutoff2(IndependenceTest test, List<Node> nodes1) {
        List<Double> pValues = new ArrayList<Double>();

        FOR1:
        for (int i = 0; i < nodes1.size(); i++) {
            for (int j = i + 1; j < nodes1.size(); j++) {
                Node x = nodes1.get(i);
                Node y = nodes1.get(j);

                test.isIndependent(x, y);

                double p = test.getPValue();

                pValues.add(p);
            }
        }

        FOR2:
        for (int i = 0; i < nodes1.size(); i++) {
            for (int j = i + 1; j < nodes1.size(); j++) {
                for (int k = 0; k < nodes1.size(); k++) {
                    if (k == i || k == j) continue;

                    Node x = nodes1.get(i);
                    Node y = nodes1.get(j);
                    Node z = nodes1.get(k);

                    test.isIndependent(x, y, z);

                    double p = test.getPValue();

                    pValues.add(p);
                }
            }
        }

        return StatUtils.fdrCutoff(0.001, pValues, false);
    }


    public void test8() {
        double beta = 1.3e-10;

        int[] _days = {
                14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 32, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                50, 51, 53, 54, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 94, 95, 96, 97, 98, 99,
                100, 101, 102, 103, 104
        };

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};
        int numDays = 10;

//        for (int numDays : new int[]{3, 4, 5, 6, 8, 9, 10, 15, 20, 25, 30, 40, 50, 60, 70, 84}) {
//        for (int numDays : new int[]{30}) {
        for (int r = 0; r < _days.length / numDays; r++) {
            int[] days = new int[numDays];

            for (int j = 0; j < numDays; j++) {
                days[j] = _days[r * numDays + j];
            }


//            double alpha = beta * (1.0 / (numDays * 518));
            double alpha = .001; ////1 / (numDays * 518);

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);

            try {
                int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

//                int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

                List<Node> nodes = new ArrayList<Node>();

                for (int i = 0; i < varIndices.length; i++) {
                    Node node = new ContinuousVariable(names[varIndices[i] - 1]);
                    nodes.add(node);
                    System.out.println(node);
                }

                Map<Edge, Integer> countsVoxelwise = new HashMap<Edge, Integer>();
                Map<Edge, Integer> countsRoiMeans = new HashMap<Edge, Integer>();

//                int[] days = new int[numDays];
//
//                for (int j = 0; j < numDays; j++) {
//                    days[j] = _days[j + 35];
//                }


                System.out.println();
                System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

                System.out.println("alpha = " + alpha);

                List<DataSet> datasets = new ArrayList<DataSet>();

                for (int i : varIndices) {
                    List<DataSet> d = new ArrayList<DataSet>();

                    NumberFormat nf = new DecimalFormat("000");

                    for (int j = 0; j < days.length; j++) {
                        DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                                "sub" + nf.format(days[j]) + "_mtl_" + i + ".txt"));

                        System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                        d.add(f);
                    }

                    DataSet c = DataUtils.concatenateData(d);
                    datasets.add(c);
                }

                if (true) {

                    DataSet data = DataUtils.collectVariables(datasets);

                    System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                    CovarianceMatrix cov = new CovarianceMatrix(data);

                    System.out.println("Calculated cov");


                    Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

                    for (int i = 0; i < varIndices.length; i++) {
                        nodeMap.put(nodes.get(i), datasets.get(i).getVariables());
                    }

                    IndTestMultiFisherZ test = new IndTestMultiFisherZ(nodeMap, cov, alpha, data);
                    test.setVerbose(true);

                    Fas fas2 = new Fas(test);
                    fas2.setDepth(2);
                    Graph graph = fas2.search();
                    int numIndep = fas2.getNumIndependenceJudgements();
                    System.out.println("# indep = " + numIndep);
                    System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));
                    System.out.println("Voxelwise FAS " + graph);

                    saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/" +
                            "voxelwise." + days.length + ".png");


                    System.out.println("Gavg = " + test.gAvg());
                    System.out.println("Percent dependent = " + test.percentDependent());

                    List<Double> gValues = test.gValues();

                    double[] _gValues = new double[gValues.size()];
                    for (int i = 0; i < gValues.size(); i++) _gValues[i] = gValues.get(i);

                    for (Edge edge : graph.getEdges()) {
                        increment(countsVoxelwise, edge);
                    }
                }

                if (true) {
                    double[][] aggregates = calcRoiMeans2(datasets);
                    DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);


                    IndependenceTest test2 = new IndTestFisherZ(roiDataset, alpha);
                    double _cutoff = searchForCutoff2(test2, test2.getVariables());
                    System.out.println("_cutoff = " + _cutoff);
                    test2 = new IndTestFisherZ(roiDataset, _cutoff);

                    Fas fas = new Fas(test2);
                    Graph g2 = fas.search();
                    System.out.println("ROI mean " + g2);

                    saveImage(g2, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/upward." +
                            "roimean." + days.length + ".png");

                    for (Edge edge : g2.getEdges()) {
                        increment(countsRoiMeans, edge);
                    }
                }

                Graph complete = new EdgeListGraph(nodes);
                complete.fullyConnect(Endpoint.TAIL);

                Graph ground = new EdgeListGraph(complete.getNodes());

                ground.addUndirectedEdge(ground.getNode("Lprc"), ground.getNode("Lent"));
                ground.addUndirectedEdge(ground.getNode("Lphc"), ground.getNode("Lent"));
                ground.addUndirectedEdge(ground.getNode("Lent"), ground.getNode("LCA32DG"));
                ground.addUndirectedEdge(ground.getNode("LCA32DG"), ground.getNode("LCA1"));
                ground.addUndirectedEdge(ground.getNode("LCA1"), ground.getNode("Lsub"));
                ground.addUndirectedEdge(ground.getNode("Lsub"), ground.getNode("Lent"));
                ground.addUndirectedEdge(ground.getNode("Lent"), ground.getNode("LCA1"));


                System.out.println("\nVoxelwise:");

                List<Edge> allEdges = complete.getEdges();
                Collections.sort(allEdges);
                int numAgreeVoxelwise = 0;

                for (Edge edge : allEdges) {
                    Integer integer = countsVoxelwise.get(edge) == null ? 0 : countsVoxelwise.get(edge);
                    System.out.println(edge + "\t" + (integer == null ? 0 : integer));
                    Node node1 = edge.getNode1();
                    Node node2 = edge.getNode2();
                    Edge edge1 = ground.getEdge(node1, node2);
                    numAgreeVoxelwise += ((integer == 1) == (edge1 != null) ? 1 : 0);
                }

                System.out.println("\nAgree Voxelwise = " + numAgreeVoxelwise);

                System.out.println("\nRoi Means:");

                int numAgreeRoimeans = 0;

                for (Edge edge : allEdges) {
                    Integer integer = countsRoiMeans.get(edge) == null ? 0 : countsRoiMeans.get(edge);
                    System.out.println(edge + "\t" + (integer == null ? 0 : integer));
                    Node node1 = edge.getNode1();
                    Node node2 = edge.getNode2();
                    Edge edge1 = ground.getEdge(node1, node2);
                    numAgreeRoimeans += ((integer == 1) == (edge1 != null) ? 1 : 0);
                }

                System.out.println("\nAgree Roimeans " + numAgreeRoimeans);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static double fisherMethodP(List<Double> p) {
        double tf = 0;

        for (double _p : p) {
            tf += -2.0 * Math.log(_p);
        }
        return 1.0 - ProbUtils.chisqCdf(tf, 2 * p.size());
    }

    public void test8a() {
        double beta = 1.3e-10;

        int[] _days = {
                14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 32, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                50, 51, 53, 54, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 94, 95, 96, 97, 98, 99,
                100, 101, 102, 103, 104
        };

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};
        int numDays = 84;

        int[] days = new int[numDays];

        for (int j = 0; j < numDays; j++) {
            days[j] = _days[j];
        }


        double alpha = beta * (1.0 / (numDays * 518));

        DataReader reader = new DataReader();
        reader.setVariablesSupplied(false);

        try {
            int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

//                int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

            List<Node> nodes = new ArrayList<Node>();

            for (int i = 0; i < varIndices.length; i++) {
                Node node = new ContinuousVariable(names[varIndices[i] - 1]);
                nodes.add(node);
                System.out.println(node);
            }

            System.out.println();
            System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

            System.out.println("alpha = " + alpha);

            List<DataSet> datasets = new ArrayList<DataSet>();

            for (int i : varIndices) {
                List<DataSet> d = new ArrayList<DataSet>();

                NumberFormat nf = new DecimalFormat("000");

                for (int j = 0; j < days.length; j++) {
                    DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                            "sub" + nf.format(days[j]) + "_mtl_" + i + ".txt"));

                    System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                    d.add(f);
                }

                DataSet c = DataUtils.concatenateData(d);
                datasets.add(c);
            }

            double[][] aggregates = calcRoiMeans2(datasets);
            DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);


//            double[][] aggregates = calcRoiMeans2(datasets);
//            DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);
//            FileWriter writer = new FileWriter("")
//
//            DataWriter.writeRectangularData(roiDataset, out, '\t');

            Lingam lingam = new Lingam();
            Graph graph = lingam.search(roiDataset);

            System.out.println(graph);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test9() {
//        double beta = 1.3e-10;
        double beta = 3e-10;
        int numDays = 10;

        int[] _days = {
                14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 32, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                50, 51, 53, 54, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 94, 95, 96, 97, 98, 99,
                100, 101, 102, 103, 104
        };

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

//        for (int numDays : new int[]{3, 4, 5, 6, 8, 9, 10, 15, 20, 25, 30, 40, 50, 60, 70, 84}) {
//        for (int numDays : new int[]{30}) {
        int numGroups = _days.length / numDays;

        int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

//        int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        List<Node> nodes = new ArrayList<Node>();

        for (int i = 0; i < varIndices.length; i++) {
            Node node = new ContinuousVariable(names[varIndices[i] - 1]);
            nodes.add(node);
            System.out.println(node);
        }


        Graph complete = new EdgeListGraph(nodes);
        complete.fullyConnect(Endpoint.TAIL);

        List<Edge> completeEdges = complete.getEdges();

        for (Edge edge : new ArrayList<Edge>(completeEdges)) {
            Node n1 = edge.getNode1();
            Node n2 = edge.getNode2();

            if (n1.getName().startsWith("L") && n2.getName().startsWith("R")) {
                completeEdges.remove(edge);
            }
            if (n1.getName().startsWith("R") && n2.getName().startsWith("L")) {
                completeEdges.remove(edge);
            }
        }

        Collections.sort(completeEdges);

//                Graph complete = new EdgeListGraph(nodes);
//                complete.fullyConnect(Endpoint.TAIL);

        Graph ground = new EdgeListGraph(complete.getNodes());

        ground.addUndirectedEdge(ground.getNode("Lprc"), ground.getNode("Lent"));
        ground.addUndirectedEdge(ground.getNode("Lphc"), ground.getNode("Lent"));
        ground.addUndirectedEdge(ground.getNode("Lent"), ground.getNode("LCA32DG"));
        ground.addUndirectedEdge(ground.getNode("LCA32DG"), ground.getNode("LCA1"));
        ground.addUndirectedEdge(ground.getNode("LCA1"), ground.getNode("Lsub"));
        ground.addUndirectedEdge(ground.getNode("Lsub"), ground.getNode("Lent"));
        ground.addUndirectedEdge(ground.getNode("Lent"), ground.getNode("LCA1"));
//
//        ground.addUndirectedEdge(ground.getNode("Rprc"), ground.getNode("Rent"));
//        ground.addUndirectedEdge(ground.getNode("Rphc"), ground.getNode("Rent"));
//        ground.addUndirectedEdge(ground.getNode("Rent"), ground.getNode("RCA32DG"));
//        ground.addUndirectedEdge(ground.getNode("RCA32DG"), ground.getNode("RCA1"));
//        ground.addUndirectedEdge(ground.getNode("RCA1"), ground.getNode("Rsub"));
//        ground.addUndirectedEdge(ground.getNode("Rsub"), ground.getNode("Rent"));
//        ground.addUndirectedEdge(ground.getNode("Rent"), ground.getNode("RCA1"));

//                int[] days = new int[numDays];
//
//                for (int j = 0; j < numDays; j++) {
//                    days[j] = _days[j + 35];
//                }


        int[][] countsPcVoxelwise = new int[completeEdges.size()][numGroups];
        int[][] countsGesVoxelwise = new int[completeEdges.size()][numGroups];
        int[][] countsRoimeans = new int[completeEdges.size()][numGroups];

        for (int r = 0; r < 1; r++) {
            int[] days = new int[numDays];

            for (int j = 0; j < numDays; j++) {
                days[j] = _days[r * numDays + j];
            }


//            double alpha = beta * (1.0 / (numDays * 518));
            double alpha = 0.001;

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);

            try {
                System.out.println();
                System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

                System.out.println("alpha = " + alpha);

                List<DataSet> datasets = new ArrayList<DataSet>();

                for (int i : varIndices) {
                    List<DataSet> d = new ArrayList<DataSet>();

                    NumberFormat nf = new DecimalFormat("000");

                    for (int j = 0; j < days.length; j++) {
                        DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                                "sub" + nf.format(days[j]) + "_mtl_" + i + ".txt"));

                        System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                        d.add(f);
                    }

                    DataSet c = DataUtils.concatenateData(d);
                    datasets.add(c);
                }

                String side = "left";

                for (int i = 0; i < datasets.size(); i++) {
                    List<Node> _nodes = datasets.get(i).getVariables();
                    for (Node node : _nodes) {
                        node.setName((i + 1) + node.getName());
                    }
                }

                DataSet data = DataUtils.collectVariables(datasets);

                System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                CovarianceMatrix cov = new CovarianceMatrix(data);

                System.out.println("Calculated cov");

                if (false) {
                    Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

                    for (int i = 0; i < varIndices.length; i++) {
                        nodeMap.put(nodes.get(i), datasets.get(i).getVariables());
                    }

                    IndTestMultiFisherZ test = new IndTestMultiFisherZ(nodeMap, cov, alpha, data);
                    test.setVerbose(true);

                    Pc pc = new Pc(test);
                    pc.setDepth(2);
                    Graph graph = pc.search();
//                    graph = GraphUtils.undirectedGraph(graph);

                    System.out.println(graph);

                    saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/" + side + ".pc." +
                            "voxelwize." + beta + "." + days.length + "." + (r + 1) + ".png");

                    for (Edge edge : completeEdges) {
                        boolean inGround = ground.containsEdge(edge);
                        boolean inGraph = graph.containsEdge(edge);
                        boolean equals = inGround == inGraph;
                        if (equals) {
                            int index = completeEdges.indexOf(edge);
                            countsPcVoxelwise[index][r] = 1;
                        }
                    }
                }

                if (true) {

                    Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

                    for (int i = 0; i < varIndices.length; i++) {
                        nodeMap.put(nodes.get(i), datasets.get(i).getVariables());
                    }

                    GesMulti ges = new GesMulti(nodeMap, cov);
//                    ges.setKnowledge(knowledge);
                    ges.setVerbose(true);
                    ges.setPenaltyDiscount(0.01);
                    Graph graph = ges.search();

//                    Ges ges = new Ges(data);
//                    ges.setPenaltyDiscount(100.0);
//                    ges.setVerbose(true);
//
//                    Graph graph = ges.search();
//
//                    List<Node> rois = new ArrayList<Node>(nodeMap.keySet());
//                    Graph g2 = new EdgeListGraph(rois);
//
//                    for (int i = 0; i < rois.size(); i++) {
//                        for (int j = i + 1; j < rois.size(); j++) {
//                            List<Node> r1 = nodeMap.get(rois.get(i));
//                            List<Node> r2 = nodeMap.get(rois.get(j));
//                            int count = 0;
//                            int total = r1.size() * r2.size();
//
//                            for (Node n1 : r1) {
//                                for (Node n2 : r2) {
//                                    if (graph.isAdjacentTo(n1, n2)) {
//                                        count++;
//                                    }
//                                }
//                            }
//
//                            System.out.println(rois.get(i) + " " + rois.get(j) +
//                                    " count " + count + " total " + total + " ratio = " + (count / (double) total));
//
//                            if (count > 0) {
//                                g2.addUndirectedEdge(rois.get(i), rois.get(j));
//                            }
//                        }
//                    }
//
//                    graph = g2;

                    graph = GraphUtils.undirectedGraph(graph);

                    System.out.println(graph);

                    saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/" + side + ".ges." +
                            "voxelwize." + (r + 1) + "." + days.length + ".png");

                    for (Edge edge : completeEdges) {
                        boolean inGround = ground.containsEdge(edge);
                        boolean inGraph = graph.containsEdge(edge);
                        boolean equals = inGround == inGraph;
                        if (equals) {
                            int index = completeEdges.indexOf(edge);
                            countsGesVoxelwise[index][r] = 1;
                        }
                    }
                }


                if (false) {
                    double[][] aggregates = calcRoiMeans2(datasets);
                    DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);


                    IndependenceTest test2 = new IndTestFisherZ(roiDataset, alpha);
                    double _cutoff = searchForCutoff2(test2, test2.getVariables());
                    System.out.println("_cutoff = " + _cutoff);
                    test2 = new IndTestFisherZ(roiDataset, _cutoff);

                    Fas fas = new Fas(test2);
                    Graph graph = fas.search();
                    System.out.println("ROI mean " + graph);

                    saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/" + side + "." +
                            "roimean." + (r + 1) + "." + days.length + ".png");

                    for (Edge edge : completeEdges) {
                        boolean inGround = ground.containsEdge(edge);
                        boolean inGraph = graph.containsEdge(edge);
                        boolean equals = inGround == inGraph;
                        if (equals) {
                            int index = completeEdges.indexOf(edge);
                            countsRoimeans[index][r] = 1;
                        }
                    }
                }


                System.out.println("\nPC Voxelwise:");

                for (int i = 0; i < completeEdges.size(); i++) {
                    System.out.print(completeEdges.get(i) + "\t");

                    for (int g = 0; g < numGroups; g++) {
                        int k = countsPcVoxelwise[i][g];
                        System.out.print(k + "\t");
                    }

                    System.out.println();
                }

                System.out.print("\t");

                for (int g = 0; g < numGroups; g++) {
                    int sum = 0;

                    for (int i = 0; i < completeEdges.size(); i++) {
                        sum += countsPcVoxelwise[i][g];
                    }

                    System.out.print(sum + "\t");
                }

                System.out.println();

                System.out.println("\nGES Voxelwise:");

                for (int i = 0; i < completeEdges.size(); i++) {
                    System.out.print(completeEdges.get(i) + "\t");

                    for (int g = 0; g < numGroups; g++) {
                        int k = countsGesVoxelwise[i][g];
                        System.out.print(k + "\t");
                    }

                    System.out.println();
                }

                System.out.print("\t");

                for (int g = 0; g < numGroups; g++) {
                    int sum = 0;

                    for (int i = 0; i < completeEdges.size(); i++) {
                        sum += countsGesVoxelwise[i][g];
                    }

                    System.out.print(sum + "\t");
                }

                System.out.println();

                System.out.println("\nRoi Means:");

                for (int i = 0; i < completeEdges.size(); i++) {
                    System.out.print(completeEdges.get(i) + "\t");

                    for (int g = 0; g < numGroups; g++) {
                        int k = countsRoimeans[i][g];
                        System.out.print(k + "\t");
                    }

                    System.out.println();
                }

                System.out.print("\t");

                for (int g = 0; g < numGroups; g++) {
                    int sum = 0;

                    for (int i = 0; i < completeEdges.size(); i++) {
                        sum += countsRoimeans[i][g];
                    }

                    System.out.print(sum + "\t");
                }

                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void test10() {
        System.out.println(Double.MIN_VALUE);

//        double beta = 1.3e-10;
//        double beta = 1e-10;
//        double beta = 2e-10;

        double beta = 1e-8;
        int numDays = 10;

        int[] _days = {
                14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 32, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                50, 51, 53, 54, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 94, 95, 96, 97, 98, 99,
                100, 101, 102, 103, 104
        };

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

//        for (int numDays : new int[]{3, 4, 5, 6, 8, 9, 10, 15, 20, 25, 30, 40, 50, 60, 70, 84}) {
//        for (int numDays : new int[]{30}) {
        int numGroups = _days.length / numDays;

        int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

//        int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        List<Node> nodes = new ArrayList<Node>();

        for (int i = 0; i < varIndices.length; i++) {
            Node node = new ContinuousVariable(names[varIndices[i] - 1]);
            nodes.add(node);
            System.out.println(node);
        }


        Graph complete = new EdgeListGraph(nodes);
        complete.fullyConnect(Endpoint.TAIL);

        List<Edge> completeEdges = complete.getEdges();

        for (Edge edge : new ArrayList<Edge>(completeEdges)) {
            Node n1 = edge.getNode1();
            Node n2 = edge.getNode2();

            if (n1.getName().startsWith("L") && n2.getName().startsWith("R")) {
                completeEdges.remove(edge);
            }
            if (n1.getName().startsWith("R") && n2.getName().startsWith("L")) {
                completeEdges.remove(edge);
            }
        }

        Collections.sort(completeEdges);

//        Graph ground = getGround(complete);

//        for (int r = 0; r < numGroups; r++) {
        for (int r = 0; r < 1; r++) {
            int[] days = new int[numDays];

            for (int j = 0; j < numDays; j++) {
                days[j] = _days[r * numDays + j];
            }

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);

            try {
                System.out.println();
                System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

                List<DataSet> datasets = new ArrayList<DataSet>();

                for (int i : varIndices) {
                    List<DataSet> d = new ArrayList<DataSet>();

                    NumberFormat nf = new DecimalFormat("000");

                    for (int j = 0; j < days.length; j++) {
                        DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                                "sub" + nf.format(days[j]) + "_mtl_" + i + ".txt"));

                        System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                        d.add(f);
                    }

                    DataSet c = DataUtils.concatenateData(d);
                    datasets.add(c);
                }

                for (int i = 0; i < datasets.size(); i++) {
                    List<Node> _nodes = datasets.get(i).getVariables();
                    for (Node node : _nodes) {
                        node.setName((i + 1) + node.getName());
                    }
                }

                DataSet data = DataUtils.collectVariables(datasets);

                System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                CovarianceMatrix cov = new CovarianceMatrix(data);

                System.out.println("Calculated cov");

                if (true) {
                    Map<Node, List<Integer>> nodeIndices = new HashMap<Node, List<Integer>>();
                    int index = 0;

                    for (int i = 0; i < varIndices.length; i++) {
                        List<Integer> _indices = new ArrayList<Integer>();

                        for (int j = 0; j < datasets.get(i).getVariables().size(); j++) {
                            _indices.add(index++);
                        }

                        nodeIndices.put(nodes.get(i), _indices);
                    }

                    Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

                    for (int i = 0; i < varIndices.length; i++) {
                        nodeMap.put(nodes.get(i), datasets.get(i).getVariables());
                    }

//                    Map<Node, TetradMatrix> coords = new HashMap<Node, TetradMatrix>();
//                    List<TetradMatrix> _coords = new ArrayList<TetradMatrix>();
//
//                    for (int i = 0; i < varIndices.length; i++) {
//                        coords.put(nodes.get(i), loadCoords(i));
//                        _coords.add(loadCoords(i));
//                    }

                    GesMulti ges = new GesMulti(nodeMap, cov);
                    ges.setPenaltyDiscount(0.01);

                    Graph graph = ges.search();

                    System.out.println(graph);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void test10a() {
        File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/roiwisegesdump.txt");
        PrintWriter out = null;

        List<int[][][]> all3Ds = new ArrayList<int[][][]>();

        try {
            out = new PrintWriter(new FileWriter(file));

            double alpha = 0.05;

            int numDays = 10;

            int[] _days = {
                    14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 32, 35, 36, 37, 38, 39,
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                    50, 51, 53, 54, 56, 57, 58, 59,
                    60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                    70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                    80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                    91, 92, 94, 95, 96, 97, 98, 99,
                    100, 101, 102, 103, 104
            };

            String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

            for (int r = 0; r < 1; r++) {
                int[] days = new int[numDays];

                for (int j = 0; j < numDays; j++) {
                    days[j] = _days[r * numDays + j];
                }

                DataReader reader = new DataReader();
                reader.setVariablesSupplied(false);

                System.out.println();
                System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

                List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6}, 0);
//                List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});

                DataSet data = (DataSet) ret.get(0);
                Map<Node, List<Node>> nodeMap = (Map<Node, List<Node>>) ret.get(1);
                Map<Node, TetradMatrix> coords = (Map<Node, TetradMatrix>) ret.get(2);

                System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                CovarianceMatrix cov = new CovarianceMatrix(data);
                TetradMatrix _cov = cov.getMatrix();

                System.out.println("Calculated cov");

                if (true) {
                    GesMulti ges = new GesMulti(nodeMap, cov);
                    ges.setVerbose(true);
                    ges.setPenaltyDiscount(1.1);

                    Graph graph = ges.search();

                    System.out.println("graph = " + graph);

                    saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/left" + "." +
                            "gesvoxelwise." + (r + 1) + "." + days.length + ".png");

                    DagInPatternIterator iterator = new DagInPatternIterator(graph);

                    Graph dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();

//                    Node prc = dag.getNode("Lprc");
//                    Node phc = dag.getNode("Lphc");
//                    Node lca32dg = dag.getNode("LCA32DG");
//                    Node ca1 = dag.getNode("LCA1");
//                    Node ent = dag.getNode("Lent");
//                    Node sub = dag.getNode("Lsub");
//
//                    Graph dag2 = new EdgeListGraph(dag.getNodes());
//
//                    dag2.addDirectedEdge(prc, ent);
//                    dag2.addDirectedEdge(phc, ent);
//                    dag2.addDirectedEdge(ent, lca32dg);
//                    dag2.addDirectedEdge(lca32dg, ca1);
//                    dag2.addDirectedEdge(ca1, sub);
//                    dag2.addDirectedEdge(lca32dg, sub);
//                    dag2.addDirectedEdge(sub, ent);

                    System.out.println("dag  " + dag);
//                    System.out.println("dag2 = " + dag2);

                    for (Node child : dag.getNodes()) {
                        List<Node> parents = dag.getParents(child);
                        printOutScoresAndPlots(nodeMap, child, parents, data.getNumRows(), _cov, cov, data.getVariables(),
                                coords, alpha, out, all3Ds);
                        out.flush();
                    }
                }

                out.flush();
                out.close();

//                tryClustering3Ds(all3Ds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test10b() {
        try {
            File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/roiwisegesdump.txt");

            PrintWriter out = new PrintWriter(new FileWriter(file));

            int numDays = 10;

            int[] _days = {
                    14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 32, 35, 36, 37, 38, 39,
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                    50, 51, 53, 54, 56, 57, 58, 59,
                    60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                    70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                    80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                    91, 92, 94, 95, 96, 97, 98, 99,
                    100, 101, 102, 103, 104
            };

            for (int r = 0; r < 1; r++) {
                int[] days = new int[numDays];

                for (int j = 0; j < numDays; j++) {
                    days[j] = _days[r * numDays + j];
                }

                DataReader reader = new DataReader();
                reader.setVariablesSupplied(false);

                System.out.println();
                System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

//                List<Object> ret = getJitteredDataSet(days, new int[]{3, 4}, 0);
                List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6}, 0);

                DataSet data = (DataSet) ret.get(0);
                Map<Node, List<Node>> nodeMap = (Map<Node, List<Node>>) ret.get(1);
                Map<Node, TetradMatrix> coords = (Map<Node, TetradMatrix>) ret.get(2);

                System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                CovarianceMatrix cov = new CovarianceMatrix(data);

                System.out.println("Calculated cov");

                Ges ges = new Ges(cov);

                ges.setVerbose(true);
                ges.setPenaltyDiscount(20);
                Graph voxelGraph = ges.search();

//                IndependenceTest test = new IndTestFisherZ(cov, 1e-25);
//                Pc pc = new Pc(test);
//                pc.setVerbose(true);
//                pc.setDepth(1);
//                Graph voxelGraph = pc.search();

                Graph roiGraph = new EdgeListGraph(new ArrayList<Node>(nodeMap.keySet()));
                List<Node> metaNodes = roiGraph.getNodes();

                int[][] counts = new int[metaNodes.size()][metaNodes.size()];

                for (Edge edge : voxelGraph.getEdges()) {
                    Node node1 = edge.getNode1();
                    Node node2 = edge.getNode2();

                    for (int i = 0; i < metaNodes.size(); i++) {
                        for (int j = i; j < metaNodes.size(); j++) {
                            Node mnode1 = metaNodes.get(i);
                            Node mnode2 = metaNodes.get(j);

                            if (nodeMap.get(mnode1).contains(node1) && nodeMap.get(mnode2).contains(node2)
                                    || nodeMap.get(mnode2).contains(node1) && nodeMap.get(mnode1).contains(node2)) {
                                if (i == j) {
                                    if (!roiGraph.isAdjacentTo(mnode1, mnode2)) {
                                        roiGraph.addUndirectedEdge(mnode1, mnode2);
                                    }
                                    counts[i][i]++;
                                } else {
                                    if (!roiGraph.isAdjacentTo(mnode1, mnode2)) {
                                        roiGraph.addUndirectedEdge(mnode1, mnode2);
                                    }
                                    counts[i][j]++;
                                    counts[j][i]++;
                                }
                            }
                        }
                    }
                }

                System.out.println(roiGraph);
                System.out.println(MatrixUtils.toString(counts));

                for (Node nodeA : metaNodes) {
                    for (Node nodeB : metaNodes) {
                        if (nodeA == nodeB) continue;
                        if (counts[metaNodes.indexOf(nodeA)][metaNodes.indexOf(nodeB)] < 20) continue;

                        List<Node> aList = nodeMap.get(nodeA);
                        List<Node> bList = nodeMap.get(nodeB);

                        List<Integer> _iIndices = new ArrayList<Integer>();
                        List<Integer> _mIndices = new ArrayList<Integer>();

                        for (int i = 0; i < aList.size(); i++) {
                            for (int j = 0; j < bList.size(); j++) {
                                Node x = aList.get(i);
                                Node y = bList.get(j);

                                if (voxelGraph.isAdjacentTo(x, y)) {
                                    int ii = aList.indexOf(x);
                                    int ij = aList.indexOf(y);
                                    int jj = bList.indexOf(y);
                                    int ji = bList.indexOf(x);

                                    if (ii != -1 && jj != -1) {
                                        _iIndices.add(ii);
                                        _mIndices.add(jj);
                                    }

                                    if (ij != -1 && ji != -1) {
                                        _iIndices.add(ij);
                                        _mIndices.add(ji);
                                    }
                                }
                            }
                        }

                        int[][][] X = threeDView(_iIndices, coords.get(nodeA));
                        int[][][] Y = threeDView(_mIndices, coords.get(nodeB));

                        // Print views
                        String projection = "Axial";

                        TestHippocampusUtils.printChart(X, coords.get(nodeA), 0, 1, "" + nodeA, projection, nodeA + " --- " + nodeB,
                                false, false, out, 0);
                        TestHippocampusUtils.printChart(Y, coords.get(nodeB), 0, 1, "" + nodeB, projection, nodeA + " --- " + nodeB,
                                false, false, out, 0);

                        projection = "Coronal";

                        TestHippocampusUtils.printChart(X, coords.get(nodeA), 0, 2, "" + nodeA, projection, nodeA + " --- " + nodeB,
                                true, false, out, 0);
                        TestHippocampusUtils.printChart(Y, coords.get(nodeB), 0, 2, "" + nodeB, projection, nodeA + " --- " + nodeB,
                                true, false, out, 0);

                        projection = "Saggital";

                        TestHippocampusUtils.printChart(X, coords.get(nodeA), 1, 2, "" + nodeA, projection, nodeA + " --- " + nodeB,
                                true, false, out, 0);
                        TestHippocampusUtils.printChart(Y, coords.get(nodeB), 1, 2, "" + nodeB, projection, nodeA + " --- " + nodeB,
                                true, false, out, 0);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // OK you can change this one. Copy of 10a.
    public void test10c() {
        File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/roiwisegesdump.txt");
        PrintWriter out = null;

        List<int[][][]> all3Ds = new ArrayList<int[][][]>();

        try {
            out = new PrintWriter(new FileWriter(file));

            double alpha = 0.05;

            int numDays = 10;

            int[] _days = {
                    14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 32, 35, 36, 37, 38, 39,
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                    50, 51, 53, 54, 56, 57, 58, 59,
                    60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                    70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                    80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                    91, 92, 94, 95, 96, 97, 98, 99,
                    100, 101, 102, 103, 104
            };

            String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

            for (int r = 0; r < 1; r++) {
                int[] days = new int[numDays];

                for (int j = 0; j < numDays; j++) {
                    days[j] = _days[r * numDays + j];
                }

                DataReader reader = new DataReader();
                reader.setVariablesSupplied(false);

                System.out.println();
                System.out.println("# days = " + days.length + "days = " + Arrays.toString(days));

                List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6}, 0);
//                List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});

                DataSet data = (DataSet) ret.get(0);
                Map<Node, List<Node>> nodeMap = (Map<Node, List<Node>>) ret.get(1);
                Map<Node, TetradMatrix> coords = (Map<Node, TetradMatrix>) ret.get(2);

                System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

                CovarianceMatrix cov = new CovarianceMatrix(data);
                TetradMatrix _cov = cov.getMatrix();

                System.out.println("Calculated cov");

                if (true) {
                    GesMulti ges = new GesMulti(nodeMap, cov);
                    ges.setVerbose(true);
                    ges.setPenaltyDiscount(1.1);

                    Graph graph = ges.search();

                    System.out.println("graph = " + graph);

                    saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/left" + "." +
                            "gesvoxelwise." + (r + 1) + "." + days.length + ".png");

                    DagInPatternIterator iterator = new DagInPatternIterator(graph);

                    Graph dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();
//                    dag = iterator.next();

                    Node prc = dag.getNode("Lprc");
                    Node phc = dag.getNode("Lphc");
                    Node lca32dg = dag.getNode("LCA32DG");
                    Node ca1 = dag.getNode("LCA1");
                    Node ent = dag.getNode("Lent");
                    Node sub = dag.getNode("Lsub");

                    Graph dag2 = new EdgeListGraph(dag.getNodes());

                    dag2.addDirectedEdge(prc, ent);
                    dag2.addDirectedEdge(phc, ent);
                    dag2.addDirectedEdge(ent, lca32dg);
                    dag2.addDirectedEdge(lca32dg, ca1);
                    dag2.addDirectedEdge(ent, ca1);
                    dag2.addDirectedEdge(ca1, sub);
//                    dag2.addDirectedEdge(lca32dg, sub);
                    dag2.addDirectedEdge(sub, ent);

                    System.out.println("dag  " + dag);
                    System.out.println("dag2 = " + dag2);

                    for (Node child : dag2.getNodes()) {
                        List<Node> parents = dag2.getParents(child);
                        printOutScoresAndPlots2(nodeMap, child, parents, data.getNumRows(), _cov, cov, data.getVariables(),
                                coords, alpha, out, all3Ds);
                        out.flush();
                    }
                }

                out.flush();
                out.close();

//                tryClustering3Ds(all3Ds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Don't change
    private void printOutScoresAndPlots(Map<Node, List<Node>> nodeMap, Node child, List<Node> parents, int n, TetradMatrix cov,
                                        CovarianceMatrix _cov, List<Node> dataVars, Map<Node, TetradMatrix> coords, double alpha,
                                        PrintWriter out, List<int[][][]> all3Ds) {

        List<Node> allX = new ArrayList<Node>();

        for (Node parent : parents) {
            allX.addAll(nodeMap.get(parent));
        }

        try {
            for (int parent = 0; parent < parents.size(); parent++) {
                Node _parent = parents.get(parent);
                List<Node> x = nodeMap.get(_parent);
                List<Node> y = nodeMap.get(child);

                TetradVector W = new TetradVector(y.size());

                TetradMatrix Cxx = subMatrix2(dataVars, cov, allX, allX);
                TetradMatrix Cxy = subMatrix2(dataVars, cov, allX, y);

                TetradMatrix B = Cxx.inverse().times(Cxy);

                for (int v = 0; v < y.size(); v++) {
                    W.set(v, B.getColumn(v).dotProduct(Cxy.getColumn(v)));
                }

                int[] indicesY = new int[y.size()];

                for (int l = 0; l < y.size(); l++) {
                    indicesY[l] = dataVars.indexOf(y.get(l));
                }

                TetradVector D = cov.diag().viewSelection(indicesY);

                TetradVector R = D.minus(W);

                String regressionString = regressionString(child, Collections.singletonList(_parent));

                List<Integer> _iIndices = new ArrayList<Integer>();
                List<Integer> _jIndices = new ArrayList<Integer>();

                RegressionCovariance regression = new RegressionCovariance(_cov);

                for (int i = 0; i < x.size(); i++) {
                    List<Node> regressors = new ArrayList<Node>(allX);
                    regressors.remove(x.get(i));

                    RegressionResult result = regression.regress(x.get(i), regressors);
                    double r2 = result.getRSquared();

                    for (int j = 0; j < y.size(); j++) {
                        double b = B.get(i, j);

                        double se = R.get(j);

                        double s2xk = cov.get(j, j);

                        double sb = se / sqrt((1.0 - r2) * s2xk * (n - 1));

                        double t = b / sb;

                        int params = B.columns();

                        double c = new TDistribution(n - params).cumulativeProbability(abs(t));
                        double _p = 2 * (1 - c);
//                        double _p = 2 * (1 - ProbUtils.tCdf(abs(t), n - params));

                        if (_p < alpha) {
                            _iIndices.add(i);
                            _jIndices.add(j);
                        }
                    }
                }

//                    Printing out stuff for Ruben.First print out dependent voxels.
//
//                    2. Second file, contain a list of all the dependencies between voxels.
//
//                    10-- 50
//                    30-- 2

                int[][][] X = threeDView(_iIndices, coords.get(_parent));
                int[][][] Y = threeDView(_jIndices, coords.get(child));

                all3Ds.add(X);
                all3Ds.add(Y);

                List<int[][][]> cubes = new ArrayList<int[][][]>();
                cubes.add(X);
                cubes.add(Y);

//        int thresholdX = tryClustering3D(X);
//        int thresholdY = tryClustering3D(Y);

                int _threshold = TestHippocampusUtils.tryClustering3Ds(cubes);

                out.println("Threshold = " + _threshold);


                int[][][] Cx = getC(X);
                int[][][] Cy = getC(Y);

                System.out.println("_iIndices.size() = " + x.size() + " coords.rows() = " + coords.get(_parent).rows());
                System.out.println("_jIndices.size() = " + y.size() + " coords.rows() = " + coords.get(child).rows());

                out.println("\n\n" + child + " vs " + _parent + " for " + regressionString);

                for (int g = 0; g < _iIndices.size(); g++) {
                    int i = getIndex(_iIndices, Cx, g, coords.get(_parent));
                    int j = getIndex(_jIndices, Cy, g, coords.get(child));

                    if (i == -1 || j == -1) throw new IllegalArgumentException();

                    out.println(i + " -- " + j);
                }

                out.println();


//                    1. First file, containing info of both ROIs and all their voxels.
//                    Example:
//
//                    ROI_LABEL voxel_LABEL COORDINATES#Dependencies
//                    ENT 10 - 80 50 38 6
//                    CA1 50 - 70 15 90 2


                printDependencies(_parent, _parent + " for " + regressionString, X, Cx, out);
                printDependencies(child, child + " for " + regressionString, Y, Cy, out);

                // Print views
                String projection = "Axial";

                TestHippocampusUtils.printChart(X, coords.get(_parent), 0, 1, "" + _parent, projection, regressionString,
                        false, false, out, _threshold);
                TestHippocampusUtils.printChart(Y, coords.get(child), 0, 1, "" + child, projection, regressionString,
                        false, false, out, _threshold);

                projection = "Coronal";

                TestHippocampusUtils.printChart(X, coords.get(_parent), 0, 2, "" + _parent, projection, regressionString,
                        true, false, out, _threshold);
                TestHippocampusUtils.printChart(Y, coords.get(child), 0, 2, "" + child, projection, regressionString,
                        true, false, out, _threshold);

                projection = "Saggital";

                TestHippocampusUtils.printChart(X, coords.get(_parent), 1, 2, "" + _parent, projection, regressionString,
                        true, false, out, _threshold);
                TestHippocampusUtils.printChart(Y, coords.get(child), 1, 2, "" + child, projection, regressionString,
                        true, false, out, _threshold);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Don't change. 2014/11/5  2014/11/13
    public void test10d() {

//        These are the dependencies for which we want data.
//        Using individual-ROI k-means.
//
//
//        sub_||_ent | {CA1}
//        sub_||_CA1 | {ent}
//        ent_||_CA23DG | {}
//        CA1_||_CA23DG | {ent}
//        ent_||_CA1 | {}
//
//        And we need this extra one, for the discussion section
//
//        sub_||_CA23DG | {ent}


        // alpha = 0.001 correlations for region 1 alpha = 5 p1(k) for T^2 k, then FDR(p1, alpha)
        double alpha = 1e-14;

        int[] days = {
                14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
//                24, 25, 26, 27, 28, 29, 30, 32, 35, 36,
//                37, 38, 39, 40, 41, 42, 43, 44, 45, 46,
//                47, 48, 49, 50, 51, 53, 54, 56, 57, 58,
//                59, 60, 61, 62, 63, 64, 65, 66, 67, 68,
//                69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
//                79, 80, 81, 82, 83, 84, 85, 86, 87, 88,
//                89, 91, 92, 94, 95, 96, 97, 98, 99, 100,
//                101, 102, 103, 104
        };


        // Jitter of 0, goes from 0 to 2.
        for (int jitter = 0; jitter <= 2; jitter++) {
            List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6}, jitter);

            DataSet data = (DataSet) ret.get(0);
            Map<Node, List<Node>> nodeMap = (Map<Node, List<Node>>) ret.get(1);
            Map<Node, TetradMatrix> coords = (Map<Node, TetradMatrix>) ret.get(2);

//        List<Node> nodes = data.getVariables();

            System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

            CovarianceMatrix cov = new CovarianceMatrix(data);
//            TetradMatrix _cov = cov.getMatrix();

            IndTestMultiFisherZ3 test = new IndTestMultiFisherZ3(nodeMap, coords, cov, alpha);
            test.setVerbose(true);

            Set<Node> nodes = nodeMap.keySet();

            Node prc = getNode(nodes, "Lprc");
            Node phc = getNode(nodes, "Lphc");
            Node ca32dg = getNode(nodes, "LCA32DG");
            Node ca1 = getNode(nodes, "LCA1");
            Node ent = getNode(nodes, "Lent");
            Node sub = getNode(nodes, "Lsub");

//        sub_||_ent | {CA1}
//        sub_||_CA1 | {ent}
//        ent_||_CA23DG | {}
//        CA1_||_CA23DG | {ent}
//        ent_||_CA1 | {}
//
//        And we need this extra one, for the discussion section
//
//        sub_||_CA23DG | {ent}

            boolean verbose = true;

            try {
                File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/" +
                        "roiwise.communication.4ROIs.alpha." + alpha + ".jitter." + jitter + ".2014.11.5.Joe.txt");
                PrintWriter out = new PrintWriter(new FileWriter(file));

                List<int[][][]> all3Ds = new ArrayList<int[][][]>();

                out.println("\n\n\n====================INDEPENDENCE TEST==================\n\n\n");

                TestHippocampusUtils.printOutMaps(sub, ent, list(ca1), nodeMap, cov, out, alpha, coords, all3Ds, verbose);
                TestHippocampusUtils.printOutMaps(sub, ca1, list(ent), nodeMap, cov, out, alpha, coords, all3Ds, verbose);
                TestHippocampusUtils.printOutMaps(ent, ca32dg, list(), nodeMap, cov, out, alpha, coords, all3Ds, verbose);
                TestHippocampusUtils.printOutMaps(ca1, ca32dg, list(ent), nodeMap, cov, out, alpha, coords, all3Ds, verbose);
                TestHippocampusUtils.printOutMaps(ent, ca1, list(), nodeMap, cov, out, alpha, coords, all3Ds, verbose);
                TestHippocampusUtils.printOutMaps(sub, ca32dg, list(ent), nodeMap, cov, out, alpha, coords, all3Ds, verbose);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private Node getNode(Set<Node> nodes, String lprc) {
        for (Node node : nodes) {
            if (node.getName().equals(lprc)) {
                return node;
            }
        }

        return null;
    }

    private List<Node> list(Node... n) {
        List<Node> list = new ArrayList<Node>();

        for (Node node : n) {
            list.add(node);
        }

        return list;
    }

    // Cah change
    private void printOutScoresAndPlots2(Map<Node, List<Node>> nodeMap, Node child, List<Node> parents, int n, TetradMatrix cov,
                                         CovarianceMatrix _cov, List<Node> dataVars, Map<Node, TetradMatrix> coords, double alpha,
                                         PrintWriter out, List<int[][][]> all3Ds) {

//        List<Node> allX = new ArrayList<Node>();
//
//        for (Node parent : parents) {
//            allX.addAll(nodeMap.get(parent));
//        }

        try {
            for (int parent = 0; parent < parents.size(); parent++) {
                Node _parent = parents.get(parent);
                List<Node> x = nodeMap.get(_parent);
                List<Node> y = nodeMap.get(child);

                TetradVector W = new TetradVector(y.size());

                TetradMatrix Cxx = subMatrix2(dataVars, cov, x, x);
                TetradMatrix Cxy = subMatrix2(dataVars, cov, x, y);

                System.out.println("1 parent " + 0);

                TetradMatrix B = Cxx.inverse().times(Cxy);

                System.out.println("2");

                for (int v = 0; v < y.size(); v++) {
                    W.set(v, B.getColumn(v).dotProduct(Cxy.getColumn(v)));
                }

                int[] indicesY = new int[y.size()];

                for (int l = 0; l < y.size(); l++) {
                    indicesY[l] = dataVars.indexOf(y.get(l));
                }

                TetradVector D = cov.diag().viewSelection(indicesY);

                TetradVector R = D.minus(W);

                String regressionString = regressionString(child, Collections.singletonList(_parent));

                List<Integer> _iIndices = new ArrayList<Integer>();
                List<Integer> _jIndices = new ArrayList<Integer>();

                RegressionCovariance regression = new RegressionCovariance(_cov);

                for (int i = 0; i < x.size(); i++) {
                    List<Node> regressors = new ArrayList<Node>(x);
                    regressors.remove(x.get(i));

                    System.out.println("3 i = " + i);

                    RegressionResult result = regression.regress(x.get(i), regressors);

                    System.out.println("4");

                    double r2 = result.getRSquared();

                    for (int j = 0; j < y.size(); j++) {
                        double b = B.get(i, j);

                        double se = R.get(j);

                        double s2xk = cov.get(j, j);

                        double sb = se / sqrt((1.0 - r2) * s2xk * (n - 1));

                        double t = b / sb;

                        int params = B.columns();

                        double c = new TDistribution(n - params).cumulativeProbability(abs(t));
                        double _p = 2 * (1 - c);
//                        double _p = 2 * (1 - ProbUtils.tCdf(abs(t), n - params));

                        if (_p < alpha) {
                            _iIndices.add(i);
                            _jIndices.add(j);
                        }
                    }
                }

//                    Printing out stuff for Ruben.First print out dependent voxels.
//
//                    2. Second file, contain a list of all the dependencies between voxels.
//
//                    10-- 50
//                    30-- 2

                int[][][] X = threeDView(_iIndices, coords.get(_parent));
                int[][][] Y = threeDView(_jIndices, coords.get(child));

                all3Ds.add(X);
                all3Ds.add(Y);

                int[][][] Cx = getC(X);
                int[][][] Cy = getC(Y);

                System.out.println("_iIndices.size() = " + x.size() + " coords.rows() = " + coords.get(_parent).rows());
                System.out.println("_jIndices.size() = " + y.size() + " coords.rows() = " + coords.get(child).rows());

                out.println("\n\n" + child + " vs " + _parent + " for " + regressionString);

                for (int g = 0; g < _iIndices.size(); g++) {
                    int i = getIndex(_iIndices, Cx, g, coords.get(_parent));
                    int j = getIndex(_jIndices, Cy, g, coords.get(child));

                    if (i == -1 || j == -1) throw new IllegalArgumentException();

                    out.println(i + " -- " + j);
                }

                out.println();


//                    1. First file, containing info of both ROIs and all their voxels.
//                    Example:
//
//                    ROI_LABEL voxel_LABEL COORDINATES#Dependencies
//                    ENT 10 - 80 50 38 6
//                    CA1 50 - 70 15 90 2


                printDependencies(_parent, _parent + " for " + regressionString, X, Cx, out);
                printDependencies(child, child + " for " + regressionString, Y, Cy, out);

                // Print views
                String projection = "Axial";

                TestHippocampusUtils.printChart(X, coords.get(_parent), 0, 1, "" + _parent, projection, regressionString,
                        false, false, out, 0);
                TestHippocampusUtils.printChart(Y, coords.get(child), 0, 1, "" + child, projection, regressionString,
                        false, false, out, 0);

                projection = "Coronal";

                TestHippocampusUtils.printChart(X, coords.get(_parent), 0, 2, "" + _parent, projection, regressionString,
                        true, false, out, 0);
                TestHippocampusUtils.printChart(Y, coords.get(child), 0, 2, "" + child, projection, regressionString,
                        true, false, out, 0);

                projection = "Saggital";

                TestHippocampusUtils.printChart(X, coords.get(_parent), 1, 2, "" + _parent, projection, regressionString,
                        true, false, out, 0);
                TestHippocampusUtils.printChart(Y, coords.get(child), 1, 2, "" + child, projection, regressionString,
                        true, false, out, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String regressionString(Node child, List<Node> parents) {
        String regressionString = child + " | ";

        if (parents.size() == 0) regressionString += " EMPTY";
        else {
            for (int i = 0; i < parents.size(); i++) {
                regressionString += parents.get(i).toString();
                if (i < parents.size() - 1) regressionString += ", ";
            }
        }
        return regressionString;
    }

    // A 3D enumeration of the voxels.
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

    // Tallies indices 0 through k if dependent, or  nonzero through k if dependent, as a 3D map.
    static int[][][] threeDView(List<Integer> indices, TetradMatrix coords) {
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

        for (int g = 0; g < coords.rows(); g++) {
            TetradVector coord = coords.getRow(g);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2] = 0;
        }

        for (int g = 0; g < indices.size(); g++) {
            int index = indices.get(g);
            TetradVector coord = coords.getRow(index);
            X[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2]++;
        }

        return X;
    }

    private static int getIndex(List<Integer> iIndices, int[][][] C, int g, TetradMatrix coords) {
        int min0 = min(coords, 0);
        int min1 = min(coords, 1);
        int min2 = min(coords, 2);
        TetradVector coord = coords.getRow(iIndices.get(g));
        return C[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2];
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


    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static TetradMatrix subMatrix2(List<Node> nodes, TetradMatrix m, List<Node> x, List<Node> y) {

        // Create index array for the given variables.
        int[] indicesX = new int[x.size()];

        for (int i = 0; i < x.size(); i++) {
            indicesX[i] = nodes.indexOf(x.get(i));
        }

        int[] indicesY = new int[y.size()];

        for (int i = 0; i < y.size(); i++) {
            indicesY[i] = nodes.indexOf(y.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        TetradMatrix submatrix = m.getSelection(indicesX, indicesY);

        return submatrix;
    }


    private Graph getGround(Graph complete) {
        Graph ground = new EdgeListGraph(complete.getNodes());

//        ground.addUndirectedEdge(ground.getNode("Lprc"), ground.getNode("Lent"));
//        ground.addUndirectedEdge(ground.getNode("Lphc"), ground.getNode("Lent"));
//        ground.addUndirectedEdge(ground.getNode("Lent"), ground.getNode("LCA32DG"));
//        ground.addUndirectedEdge(ground.getNode("LCA32DG"), ground.getNode("LCA1"));
//        ground.addUndirectedEdge(ground.getNode("LCA1"), ground.getNode("Lsub"));
//        ground.addUndirectedEdge(ground.getNode("Lsub"), ground.getNode("Lent"));
//        ground.addUndirectedEdge(ground.getNode("Lent"), ground.getNode("LCA1"));
//
//        ground.addUndirectedEdge(ground.getNode("Rprc"), ground.getNode("Rent"));
//        ground.addUndirectedEdge(ground.getNode("Rphc"), ground.getNode("Rent"));
//        ground.addUndirectedEdge(ground.getNode("Rent"), ground.getNode("RCA32DG"));
//        ground.addUndirectedEdge(ground.getNode("RCA32DG"), ground.getNode("RCA1"));
//        ground.addUndirectedEdge(ground.getNode("RCA1"), ground.getNode("Rsub"));
//        ground.addUndirectedEdge(ground.getNode("Rsub"), ground.getNode("Rent"));
//        ground.addUndirectedEdge(ground.getNode("Rent"), ground.getNode("RCA1"));

        return ground;
    }


    public void testSaveRoiMeans() {
        int[] _days = {
                14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 32, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                50, 51, 53, 54, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 94, 95, 96, 97, 98, 99,
                100, 101, 102, 103, 104
        };

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

        List<DataSet> datasets = new ArrayList<DataSet>();

        List<Node> _names = new ArrayList<Node>();
        for (String name : names) _names.add(new ContinuousVariable(name));

        int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};


        try {
            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);

            for (int j = 0; j < _days.length; j++) {
                DataSet maxvar = new ColtDataSet(518, _names);

                List<DataSet> d = new ArrayList<DataSet>();

                for (int i : varIndices) {
                    NumberFormat nf = new DecimalFormat("000");

                    File infile = new File("/Users/josephramsey/Documents/dropbox" +
                            "_stuff/mtl_data_regions/" +
                            "sub" + nf.format(_days[j]) + "_mtl_" + i + ".txt");
                    DataSet f = reader.parseTabular(infile);

                    System.out.println("var " + i + " " + _days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                    TetradMatrix m = f.getDoubleData();

                    int max = -1;
                    double maxVar = 0.0;

                    for (int i2 = 0; i2 < m.columns(); i2++) {
                        double[] row = m.getColumn(i2).toArray();
                        double var = StatUtils.variance(row);

                        if (var > maxVar) {
                            maxVar = var;
                            max = i2;
                        }
                    }

                    for (int j2 = 0; j2 < m.rows(); j2++) {
                        maxvar.setDouble(j2, i - 1, m.get(j2, max));
                    }

                    d.add(f);

                }

                NumberFormat nf = new DecimalFormat("000");

                File dir = new File("/Users/josephramsey/Documents/dropbox" +
                        "_stuff/mtl_data_regions_maxvar/");
                dir.mkdirs();
                File outfile = new File(dir,
                        "sub" + nf.format(_days[j]) + ".maxvar__mtl_.txt");

                DataWriter.writeRectangularData(maxvar, new PrintWriter(outfile), '\t');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testSaveMaxVar2() {
        int[] _days = {
                14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 32, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                50, 51, 53, 54, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                91, 92, 94, 95, 96, 97, 98, 99,
                100, 101, 102, 103, 104
        };

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

        List<DataSet> datasets = new ArrayList<DataSet>();

        List<Node> _names = new ArrayList<Node>();
        for (String name : names) _names.add(new ContinuousVariable(name));

        int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        DataSet maxvar = null;

        try {
            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);

            for (int i : varIndices) {
                List<DataSet> d = new ArrayList<DataSet>();

                for (int j = 0; j < 10; j++) {
                    NumberFormat nf = new DecimalFormat("000");

                    File infile = new File("/Users/josephramsey/Documents/dropbox" +
                            "_stuff/mtl_data_regions/" +
                            "sub" + nf.format(_days[j]) + "_mtl_" + i + ".txt");
                    DataSet f = reader.parseTabular(infile);

                    System.out.println("var " + i + " " + _days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                    d.add(f);
                }

                DataSet c = DataUtils.concatenateData(d);

                if (maxvar == null) {
                    maxvar = new ColtDataSet(c.getNumRows(), _names);
                }

                int max = -1;
                double maxVar = 0.0;

                TetradMatrix m = c.getDoubleData();

                for (int i2 = 0; i2 < m.columns(); i2++) {
                    double[] row = m.getColumn(i2).toArray();
                    double var = StatUtils.variance(row);

                    if (var > maxVar) {
                        maxVar = var;
                        max = i2;
                    }
                }

                for (int j2 = 0; j2 < m.rows(); j2++) {
                    maxvar.setDouble(j2, i - 1, m.get(j2, max));
                }

                NumberFormat nf = new DecimalFormat("000");

                File dir = new File("/Users/josephramsey/Documents/dropbox" +
                        "_stuff/mtl_data_regions_maxvar2/");
                dir.mkdirs();
                File outfile = new File(dir,
                        "first.ten.maxvar_mtl_.txt");

                DataWriter.writeRectangularData(maxvar, new PrintWriter(outfile), '\t');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test12() {

        // alpha = 0.001 correlations for region 1 alpha = 5 p1(k) for T^2 k, then FDR(p1, alpha)
        double alpha = 3.042011e-14;

        int[] days = {
                14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
//                24, 25, 26, 27, 28, 29, 30, 32, 35, 36,
//                37, 38, 39, 40, 41, 42, 43, 44, 45, 46,
//                47, 48, 49, 50, 51, 53, 54, 56, 57, 58,
//                59, 60, 61, 62, 63, 64, 65, 66, 67, 68,
//                69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
//                79, 80, 81, 82, 83, 84, 85, 86, 87, 88,
//                89, 91, 92, 94, 95, 96, 97, 98, 99, 100,
//                101, 102, 103, 104
        };


        // Jitter of 2, goes from 0 to 2.
        List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6}, 2);

        DataSet data = (DataSet) ret.get(0);
        Map<Node, List<Node>> nodeMap = (Map<Node, List<Node>>) ret.get(1);
        Map<Node, TetradMatrix> coords = (Map<Node, TetradMatrix>) ret.get(2);

//        List<Node> nodes = data.getVariables();

        System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

        CovarianceMatrix cov = new CovarianceMatrix(data);

        System.out.println("Calculated cov");

        if (true) {
            IndTestMultiFisherZ3 test = new IndTestMultiFisherZ3(nodeMap, coords, cov, alpha);
            test.setVerbose(true);
//            test.setThreshold(5);

            Pc pc = new Pc(test);
            pc.setDepth(2);
            Graph graph = pc.search();
            graph = GraphUtils.undirectedGraph(graph);

            System.out.println(graph);

//            List<int[][][]> all3D = test.getAll3D();

//            tryClustering3Ds(all3D);

            saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/shifted" + ".pc." +
                    "voxelwize." + alpha + ".png");

        }

    }

    public void testHippocampus12a() {

        int[] days = {
                14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
//                24, 25, 26, 27, 28, 29, 30, 32, 35, 36,
//                37, 38, 39, 40, 41, 42, 43, 44, 45, 46,
//                47, 48, 49, 50, 51, 53, 54, 56, 57, 58,
//                59, 60, 61, 62, 63, 64, 65, 66, 67, 68,
//                69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
//                79, 80, 81, 82, 83, 84, 85, 86, 87, 88,
//                89, 91, 92, 94, 95, 96, 97, 98, 99, 100,
//                101, 102, 103, 104
        };


        List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3, 4, 5, 6}, 0);
//        List<Object> ret = getJitteredDataSet(days, new int[]{1, 2, 3});
        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

        DataSet data = (DataSet) ret.get(0);  // Shifted concat
        Map<Node, List<Node>> nodeMap = (Map<Node, List<Node>>) ret.get(1); // Node map
        Map<Node, TetradMatrix> coords = (Map<Node, TetradMatrix>) ret.get(2); // node coords

//        List<Node> nodes = data.getVariables();

        System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

        CovarianceMatrix cov = new CovarianceMatrix(data);

        System.out.println("Calculated cov");

        File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/multifisherzdump.txt");
        PrintWriter out = null;
        List<int[][][]> all3D = new ArrayList<int[][][]>();
        int threshold = 7;
        boolean verbose = true;

        try {
            out = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Node> nodes = new ArrayList<Node>(nodeMap.keySet());
        Knowledge knowledge = new Knowledge();

        Map<Node, List<Node>> communicationRegions = new HashMap<Node, List<Node>>();

        Map<String, Set<Node>> nodesOutOf = new HashMap<String, Set<Node>>();

        for (String s : names) {
            nodesOutOf.put(s, new HashSet<Node>());
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);
                List<Node> z = Collections.emptyList();

                double alpha = 1e-14;

                List<int[][][]> maps = TestHippocampusUtils.get3DMaps(x, y, z, nodeMap, cov, out, alpha, coords);

                List<Node> listx = TestHippocampusUtils.getThresholdedList(nodeMap.get(x), coords.get(x), threshold, maps.get(0));
                List<Node> listy = TestHippocampusUtils.getThresholdedList(nodeMap.get(y), coords.get(y), threshold, maps.get(1));

                ContinuousVariable var1 = new ContinuousVariable(names[i] + "." + names[j]);
                ContinuousVariable var2 = new ContinuousVariable(names[j] + "." + names[i]);

                System.out.println("New " + var1 + " " + var2);

                communicationRegions.put(var1, listx);
                communicationRegions.put(var2, listy);

                nodesOutOf.get(names[i]).add(var1);
                nodesOutOf.get(names[j]).add(var2);

//                threshold = 0;
//
//                TestHippocampusUtils.printOutMaps(x, y, z, thresholdedNodeMap, cov, out, alpha, coords, all3D, threshold, verbose);
//                knowledge.setEdgeForbidden(var1.getName(), var2.getName(), true);
//                knowledge.setEdgeForbidden(var2.getName(), var1.getName(), true);

            }
        }

        for (String s : names) {
            List<Node> _nodes = new ArrayList<Node>(nodesOutOf.get(s));
            System.out.println("_nodes = " + _nodes);

            for (int i = 0; i < _nodes.size(); i++) {
                for (int j = 0; j < _nodes.size(); j++) {
                    if (i == j) continue;
                    System.out.println("i = " + i + " j = " + j);
                    System.out.println(_nodes.get(i).getName() + " " + _nodes.get(j).getName());
                    knowledge.setEdgeForbidden(_nodes.get(i).getName(), _nodes.get(j).getName(), true);
                }
            }
        }

        DataSet roiMeans = calcRoiMeans(communicationRegions, data);

        System.out.println(roiMeans);
        double alpha = 1e-5;

//        IndTestFisherZ test0 = new IndTestFisherZ(roiMeans, alpha);
////        IndependenceTest test0 = new IndTestConditionalCorrelation(roiMeans, alpha);
//        Pc pc0 = new Pc(test0);
//        pc0.setDepth(1);
////        pc0.setKnowledge(knowledge);
//        pc0.setVerbose(true);
//
//        Graph graph0 = pc0.search();
//
//        System.out.println(graph0);

        Ges ges = new Ges(roiMeans);
        ges.setVerbose(true);
        ges.setPenaltyDiscount(5);
        Graph graph0 = ges.search();

        System.out.println(graph0);


//        System.out.println("GES voxelwise");
//
//        GesMulti ges = new GesMulti(communicationRegions, cov);
//        ges.setVerbose(true);
//        ges.setPenaltyDiscount(2);
//        ges.setKnowledge(knowledge);
//
//        Graph graph = ges.search();
//
//        System.out.println(graph);


//        System.out.println("\nPC voxelwise");
//
//        IndTestMultiFisherZ2 test = new IndTestMultiFisherZ2(communicationRegions, cov, alpha);
//
//        Pc pc = new Pc(test);
//        pc.setVerbose(true);
//
//        Graph graph2 = pc.search();
//
//        System.out.println(graph2);
    }

    private DataSet calcRoiMeans(Map<Node, List<Node>> communicationRegions, DataSet data) {
        List<Node> nodes = new ArrayList<Node>(communicationRegions.keySet());

        DataSet means = new ColtDataSet(data.getNumRows(), nodes);
        List<Node> toRemove = new ArrayList<Node>();

        for (int j = 0; j < nodes.size(); j++) {
            Node node = nodes.get(j);
            List<Node> regNodes = communicationRegions.get(node);

            if (regNodes.isEmpty()) toRemove.add(node);

            int[] cols = new int[regNodes.size()];

            for (int j2 = 0; j2 < regNodes.size(); j2++) {
                cols[j2] = data.getColumn(regNodes.get(j2));
            }

            for (int i = 0; i < data.getNumRows(); i++) {
                double sum = 0.0;

                for (int col : cols) {
                    sum += data.getDouble(i, col);
                }

                double avg = sum / cols.length;

                means.setDouble(i, j, avg);
            }
        }

        for (Node node : toRemove) {
            means.removeColumn(node);
        }

        return means;
    }


//    public void test56() {
//        List<Object> ret = getShiftedConcatenatedDataSet();
//
////        System.out.println(dataSet);
//    }


    // jittered
    public List<Object> getJitteredDataSet(int[] days, int[] varIndices, int jitter) {
        try {

            String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

            List<DataSet> datasets = new ArrayList<DataSet>();

//            int[] varIndices = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3};

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);

            List<Node> nodes = new ArrayList<Node>();

            for (int i = 0; i < names.length; i++) {
                Node node = new ContinuousVariable(names[i]);
                nodes.add(node);
                System.out.println(node);
            }

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
            List<Node> voxels = new ArrayList<Node>();

            boolean nodeMapConstructed = false;

            for (int day = 0; day < days.length; day++) {
                List<DataSet> list = new ArrayList<DataSet>();

                if (!nodeMapConstructed) {
                    nodeMap = new HashMap<Node, List<Node>>();
                }

                for (int varIndex : varIndices) {
                    NumberFormat nf = new DecimalFormat("000");

                    DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                            "sub" + nf.format(days[day]) + "_mtl_" + varIndex + ".txt"));

                    System.out.println("var " + varIndex + " " + days[day] + " " + f.getNumRows() + " x " + f.getNumColumns());

                    list.add(f);

                    List<Node> vars = f.getVariables();

                    for (Node node : vars) node.setName("D" + varIndex + "." + node.getName());

                    if (!nodeMapConstructed) {
                        nodeMap.put(nodes.get(varIndex - 1), f.getVariables());
                        voxels.addAll(f.getVariables());
                    }
                }

                nodeMapConstructed = true;

                DataSet c = DataUtils.collectVariables(list);
                datasets.add(c);
            }

            List<TetradMatrix> coords = new ArrayList<TetradMatrix>();
            Map<Node, TetradMatrix> _coords = new HashMap<Node, TetradMatrix>();

            for (int i = 0; i < varIndices.length; i++) {
                coords.add(loadCoords(i));
                _coords.put(nodes.get(i), coords.get(i));
            }

            // We're going to make a list of cubes of voxel indices. Assuming that indices
            // for all voxels do not overlap between ROIs.            q
            List<int[][][]> cubes = new ArrayList<int[][][]>();

            // Shift the voxels in some random direction for each day.
            for (int d : days) {
                if (jitter == 0) {
                    int xBump = 0;
                    int yBump = 0;
                    int zBump = 0;
                    cubes.add(getCube(coords, xBump, yBump, zBump));
                } else if (jitter == 1) {
                    int xBump = RandomUtil.getInstance().nextInt(2);
                    int yBump = RandomUtil.getInstance().nextInt(2);
                    int zBump = RandomUtil.getInstance().nextInt(2);
                    cubes.add(getCube(coords, xBump, yBump, zBump));
                } else if (jitter == 2) {
                    int xBump = RandomUtil.getInstance().nextInt(3) - 1;
                    int yBump = RandomUtil.getInstance().nextInt(3) - 1;
                    int zBump = RandomUtil.getInstance().nextInt(3) - 1;
                    cubes.add(getCube(coords, xBump, yBump, zBump));
                }
            }

            // We will need to compute to total sample size
            int rows = days.length * 518;

            // We will need to make a data set that's long enough.
            DataSet shiftedConcat = new ColtDataSet(rows, voxels);

            // Now we need to go through each variable
            int index = 0;

            // Get the minimums.
            int min0 = min(coords, 0);
            int min1 = min(coords, 1);
            int min2 = min(coords, 2);

            for (int i = 0; i < coords.size(); i++) {
                TetradMatrix m2 = coords.get(i);
                for (int j = 0; j < m2.rows(); j++) {
                    int var = index++;

                    // Gets its coordinate in the base cube.
                    TetradVector coord = m2.getRow(j);

                    // For each day, copy the data from the shifted cube for that day into the long data set.
                    for (int ii = 0; ii < days.length; ii++) {

                        // Get the shifted variable.
                        int[][][] shiftedCube = cubes.get(ii);
                        int shiftedVarIndex = shiftedCube
                                [(int) coord.get(0) - min0 + 2]
                                [(int) coord.get(1) - min1 + 2]
                                [(int) coord.get(2) - min2 + 2];

//                        int shiftedVarIndex = var;

                        for (int row = 0; row < 518; row++) {
                            if (shiftedVarIndex == -1) {
                                double datum = datasets.get(ii).getDouble(row, var);
                                shiftedConcat.setDouble(ii * 518 + row, var, datum);
                            } else {
                                double datum = datasets.get(ii).getDouble(row, shiftedVarIndex);
                                shiftedConcat.setDouble(ii * 518 + row, var, datum);
                            }
                        }
                    }
                }
            }


            List<Object> ret = new ArrayList<Object>();
            ret.add(shiftedConcat);
            ret.add(nodeMap);
            ret.add(_coords);

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int[][][] getCube(List<TetradMatrix> coords, int xBump, int yBump, int zBump) {
        int min0 = min(coords, 0);
        int min1 = min(coords, 1);
        int min2 = min(coords, 2);
        int max0 = max(coords, 0);
        int max1 = max(coords, 1);
        int max2 = max(coords, 2);

        int[][][] X = new int[max0 - min0 + 4][max1 - min1 + 4][max2 - min2 + 4];

        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                for (int m = 0; m < X[0][0].length; m++) {
                    X[i][j][m] = -1;
                }
            }
        }

        int index = 0;

        for (int i = 0; i < coords.size(); i++) {
            TetradMatrix m2 = coords.get(i);
            for (int j = 0; j < m2.rows(); j++) {
                TetradVector coord = m2.getRow(j);
                X[(int) coord.get(0) - min0 + xBump + 2]
                        [(int) coord.get(1) - min1 + yBump + 2]
                        [(int) coord.get(2) - min2 + zBump + 2] = index++;
            }
        }

        return X;

    }

    static int min(List<TetradMatrix> m, int col) {
        int min = Integer.MAX_VALUE;

        for (TetradMatrix m2 : m) {
            for (int i = 0; i < m2.rows(); i++) {
                if (m2.get(i, col) < min) min = (int) m2.get(i, col);
            }
        }

        return min;
    }

    static int max(List<TetradMatrix> m, int col) {
        int max = Integer.MIN_VALUE;

        for (TetradMatrix m2 : m) {
            for (int i = 0; i < m2.rows(); i++) {
                if (m2.get(i, col) > max) max = (int) m2.get(i, col);
            }
        }

        return max;
    }


    public void test20() {
        System.out.println(2e-10 * (1. / 5180));
    }

    private TetradMatrix loadCoords(int i) {
        String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/mtl_data";
        String base = "sub014_mtl__voxlocs_";

        int index = i + 1;

        String path = dir + "/" + base + index + ".txt";

        DataReader reader = new DataReader();
        reader.setDelimiter(DelimiterType.WHITESPACE);
        reader.setVariablesSupplied(false);

        DataSet dataSet = null;

        try {
            dataSet = reader.parseTabular(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataSet.getDoubleData();
    }


    private int min(TetradVector column) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < column.size(); i++) {
            if (column.get(i) < min) min = (int) column.get(i);
        }

        return min;
    }

    public void testLoadCoords() {
        TetradMatrix coords = loadCoords(0);

        System.out.println(coords);
    }


    public void testSaveImage() {
        Graph graph = GraphUtils.randomDag(5, 5, false);

        saveImage(graph, "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/graphs/test");
    }

    public void saveImage(Graph graph, String path) {

        if (graph.getNode("Lsub") != null) {
            graph.getNode("Lsub").setCenter(300, 285);
        }
        if (graph.getNode("Lprc") != null) {
            graph.getNode("Lprc").setCenter(90, 105);
        }
        if (graph.getNode("LCA1") != null) {
            graph.getNode("LCA1").setCenter(405, 180);
        }
        if (graph.getNode("LCA32DG") != null) {
            graph.getNode("LCA32DG").setCenter(300, 90);
        }
        if (graph.getNode("Lent") != null) {
            graph.getNode("Lent").setCenter(195, 180);
        }
        if (graph.getNode("Lphc") != null) {
            graph.getNode("Lphc").setCenter(105, 285);
        }
        if (graph.getNode("RCA1") != null) {
            graph.getNode("RCA1").setCenter(870, 210);
        }
        if (graph.getNode("Rsub") != null) {
            graph.getNode("Rsub").setCenter(765, 315);
        }
        if (graph.getNode("Rprc") != null) {
            graph.getNode("Rprc").setCenter(555, 135);
        }
        if (graph.getNode("RCA32DG") != null) {
            graph.getNode("RCA32DG").setCenter(765, 120);
        }
        if (graph.getNode("Rent") != null) {
            graph.getNode("Rent").setCenter(660, 210);
        }
        if (graph.getNode("Rphc") != null) {
            graph.getNode("Rphc").setCenter(570, 315);
        }

        GraphWorkbench comp = new GraphWorkbench(graph);

        File file = new File(path);

        // Create the image.
        Dimension size = comp.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_BYTE_INDEXED);
        Graphics graphics = image.getGraphics();
        comp.paint(graphics);

        // Write the image to file.
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public void test5() {
        DataReader reader = new DataReader();
        reader.setVariablesSupplied(false);

        try {
            int[] varIndices = {1, 2, 3, 4, 5, 6};
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

//            int[] days = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
            int[][] dayLists = {
                    {20, 21, 22, 23, 24, 25, 26, 27, 28, 29},
                    {40, 41, 42, 43, 44, 45, 46, 47, 48, 49},
                    {60, 61, 62, 63, 64, 65, 66, 67, 68, 69},
                    {70, 71, 72, 73, 74, 75, 76, 77, 78, 79},
                    {80, 81, 82, 83, 84, 85, 86, 87, 88, 89}
            };

            String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};

            List<DataSet> roiDataSets = new ArrayList<DataSet>();

            List<Node> nodes = new ArrayList<Node>();

            for (int i = 0; i < varIndices.length; i++) {
                Node node = new ContinuousVariable(names[varIndices[i] - 1]);
                nodes.add(node);
            }

            for (int[] days : dayLists) {
                List<DataSet> datasets = new ArrayList<DataSet>();

                for (int i : varIndices) {
                    List<DataSet> d = new ArrayList<DataSet>();

                    NumberFormat nf = new DecimalFormat("000");

                    for (int j = 0; j < days.length; j++) {
                        DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                                "sub" + nf.format(days[j]) + "_mtl_" + (i + 1) + ".txt"));

                        System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                        d.add(f);
                    }

                    DataSet c = DataUtils.concatenateData(d);
                    datasets.add(c);
                }

                double[][] aggregates = calcRoiMeans2(datasets);
                DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);
                roiDataSets.add(roiDataset);
            }

            ALPHA:
//            for (double alpha : new double[]{1e-12}) {
            for (double alpha : new double[]{5e-2, 1e-2, 5e-3, 1e-3, 5e-4, 1e-4, 5e-5, 1e-5, 5e-6, 1e-6, 5e-7, 1e-7, 5e-8, 1e-8,
                    5e-9, 1e-9, 5e-10, 1e-10, 5e-11, 1e-11, 5e-12, 1e-12, 5e-13, 1e-13, 5e-14, 1e-14, 5e-15, 1e-15,
                    5e-16, 1e-16, 5e-17, 1e-17, 5e-18, 1e-18, 5e-19, 1e-19, 5e-20, 1e-20, 5e-21, 1e-21, 5e-22, 1e-22,
                    5e-23, 1e-23, 5e-24, 1e-24, 5e-25, 1e-25}) {
                Map<Edge, Integer> counts = new HashMap<Edge, Integer>();


                for (DataSet roiDataset : roiDataSets) {
                    IndependenceTest test2 = new IndTestFisherZ(roiDataset, alpha);
                    Pc pc2 = new Pc(test2);
                    pc2.setDepth(-1);

                    Graph g = pc2.search();
                    g = GraphUtils.undirectedGraph(g);

                    for (Edge edge : g.getEdges()) {
                        increment(counts, edge);
                    }
                }

//                Graph all = new EdgeListGraph(nodes);
//
//                for (Edge edge : counts.keySet()) {
//                    if (counts.get(edge) >= 5) {
//                        all.addEdge(edge);
//                    }
//                }
//
//                System.out.println("alpha = " + alpha);
//                System.out.println(all);

                boolean allAbove = true;

                for (Edge edge : counts.keySet()) {
                    Integer count = counts.get(edge);
                    if (count > 0 && count < 2) {
                        continue ALPHA;
                    }
                }

                if (allAbove) {
                    System.out.println("alpha = " + alpha);
                    System.out.println(counts);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test4() {

        int[] varIndices = {1, 2, 3, 4, 5, 6};

        DataReader reader = new DataReader();
        reader.setVariablesSupplied(false);

        String[] names = {"Lsub", "LCA1", "LCA32DG", "Lent", "Lprc", "Lphc", "Rsub", "RCA1", "RCA32DG", "Rent", "Rprc", "Rphc"};
        List<Node> nodes = new ArrayList<Node>();

        for (int i = 0; i < varIndices.length; i++) {
            Node node = new ContinuousVariable(names[varIndices[i] - 1]);
            nodes.add(node);
            System.out.println(node);
        }

        try {
//            int[] varIndices = {1, 2, 3, 4};
//            int[] varIndices = {7, 8, 9, 10, 11, 12};
//            int[] varIndices = {1, 2, 3};

            List<DataSet> datasets = new ArrayList<DataSet>();

//            int[] days = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
            int[] days = {
//                    14, 15, 16, 17, 18, 19,
//                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                    30, 32, 35, 36, 37, 38, 39
//                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
//                    50, 51, 53, 54, 56, 57, 58, 59,
//                    60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
//                    70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
//                    80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
//                    91, 92, 94, 95, 96, 97, 98, 99,
//                    100, 101, 102, 103, 104
            };
//            int[] days = {26, 27, 28, 29, 30, 32, 40, 41, 42, 43, 44, 45};
//            int[] days = {32};

            double alpha = .01;
            double g = .24103477789064484 * 0.8;


            for (int i : varIndices) {
                List<DataSet> d = new ArrayList<DataSet>();

                NumberFormat nf = new DecimalFormat("000");

                for (int j = 0; j < days.length; j++) {
                    DataSet f = reader.parseTabular(new File("/Users/josephramsey/Documents/dropbox_stuff/mtl_data_regions/" +
                            "sub" + nf.format(days[j]) + "_mtl_" + i + ".txt"));

                    System.out.println("var " + i + " " + days[j] + " " + f.getNumRows() + " x " + f.getNumColumns());

                    d.add(f);
                }

                DataSet c = DataUtils.concatenateData(d);
                datasets.add(c);
            }

            DataSet data = DataUtils.collectVariables(datasets);

            System.out.println("Total:  " + data.getNumRows() + " x " + data.getNumColumns());

            CovarianceMatrix cov = new CovarianceMatrix(data);

            System.out.println("Calculated cov");

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

            for (int i = 0; i < varIndices.length; i++) {
                nodeMap.put(nodes.get(i), datasets.get(i).getVariables());
            }

            // 5e-11, 0.01 6 data sets depth 1
            // 1e-10, 0.01 10 data sets depth 1
            // 1e-12, 0.01 10 data sets depth 1

            // 1e-5
            IndTestMultiFisherZ test = new IndTestMultiFisherZ(nodeMap, cov, alpha, data);
            test.setVerbose(true);


//            test.isIndependent(test.getVariable("Lprc"), test.getVariable("LCA32DG"));
//            test.isIndependent(test.getVariable("Lprc"), test.getVariable("LCA32DG"), test.getVariable("Lent"));
//            test.isIndependent(test.getVariable("Lprc"), test.getVariable("LCA32DG"), test.getVariable("Lent"), test.getVariable("LCA1"));
//            test.isIndependent(test.getVariable("Lprc"), test.getVariable("LCA32DG"), test.getVariable("Lent"), test.getVariable("Lsub"));
////
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("Lsub"));
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("Lsub"), test.getVariable("Lent"));
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("Lsub"), test.getVariable("LCA1"));
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("Lsub"), test.getVariable("Lent"), test.getVariable("LCA1"));
////
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("LCA1"));
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("LCA1"), test.getVariable("Lent"));
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("LCA1"), test.getVariable("Lsub"));
//            test.isIndependent(test.getVariable("LCA32DG"), test.getVariable("LCA1"), test.getVariable("Lent"), test.getVariable("Lsub"));
//
//            test.isIndependent(test.getVariable("Lent"), test.getVariable("LCA1"));
//            test.isIndependent(test.getVariable("Lent"), test.getVariable("LCA1"), test.getVariable("LCA32DG"));
//            test.isIndependent(test.getVariable("Lent"), test.getVariable("LCA1"), test.getVariable("Lsub"));
//            test.isIndependent(test.getVariable("Lent"), test.getVariable("LCA1"), test.getVariable("LCA32DG"), test.getVariable("Lsub"));

//            Cpc pc = new Cpc(test);
//            Graph graph = pc.search();
//            System.out.println("Voxelwise " + graph);

//            Pc pc = new Pc(test);
//            Graph graph = pc.search();
//            System.out.println("Voxelwise " + graph);


            Ccd ccd = new Ccd(test);
            Graph graph = ccd.search();
            System.out.println("Voxelwise " + graph);

            System.out.println("Gavg = " + test.gAvg());

//            double[][] aggregates = calcRoiMeans2(datasets);
//            DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);
////            IndependenceTest test2 = new IndTestFisherZ(roiDataset, 1e-15);
//            IndependenceTest test2 = new IndTestFisherZ(roiDataset, 1e-12);

//            Fas pc2 = new Fas(test2);
//            pc2.setDepth(-1);
//            System.out.println("ROI means " + pc2.search());


            double[][] aggregates = calcRoiMeans2(datasets);
            DataSet roiDataset = ColtDataSet.makeContinuousData(nodes, aggregates);
//
//            IndependenceTest test2 = new IndTestFisherZ(roiDataset, 1e-12);
//            Pc pc2 = new Pc(test2);
//            System.out.println("ROI means " + pc2.search());
//
//            Ccd ccd2 = new Ccd(test2);
//            System.out.println("ROI means " + ccd2.search());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void increment(Map<Edge, Integer> counts, Edge edge) {
        if (counts.get(edge) == null) {
            counts.put(edge, 0);
        }

        counts.put(edge, counts.get(edge) + 1);
    }

    public void addToList(Map<Edge, List<Integer>> counts, Edge edge, int i) {
        if (counts.get(edge) == null) {
            counts.put(edge, new ArrayList<Integer>());
        }

        counts.get(edge).add(i);
    }

    public static String independenceFact(Node x, Node y, List<Node> condSet) {
        StringBuilder sb = new StringBuilder();

        sb.append("I(");
        sb.append(x.getName());
        sb.append(", ");
        sb.append(y.getName());

        Iterator it = condSet.iterator();

        if (it.hasNext()) {
            sb.append("|");
            sb.append(it.next());
        }

        while (it.hasNext()) {
            sb.append(", ");
            sb.append(it.next());
        }

        sb.append(")");

        return sb.toString();
    }


    public void test3() {
//        String graphSpec = "X1-->X2,X2-->X3,X3-->X4,X4-->X5,X1-->X5";
        String graphSpec = "X1-->X2,X2-->X3,X3-->X4,X4<--X5";

        int sampleSize = 1000;
        int v = 50;
        double fanout = 10; // The average number of nodes in B connected to a node in A for A->B.
        double probInterEdge = fanout / v;
        double probIntraEdge = fanout / v;
        double probIntraTwoCycleGivenEdge = .5;
        double varLow = 1.;
        double varHigh = 2;

        int[][] trueVoxellation = constructRois(v, v, v, v, v);
        String[] trueNames = {"X1", "X2", "X3", "X4", "X5"};

        Graph trueGraph = GraphConverter.convert(graphSpec);

        System.out.println("True graph " + trueGraph);

        int[][] fakeVoxellation = trueVoxellation;
        String[] fakeNames = {"X1", "X2", "X3", "X4", "X5"};

        Map<String, Node> roiNodes = new HashMap<String, Node>();

        for (String name : fakeNames) {
            roiNodes.put(name, new ContinuousVariable(name));
        }

        List<Node> _roiNodes = new ArrayList<Node>();
        for (String s : fakeNames) _roiNodes.add(roiNodes.get(s));

        List<Node> trueVars = new ArrayList<Node>();
        for (String name : trueNames) {
            trueVars.add(trueGraph.getNode(name));
        }

        int numVoxels = numVoxels(trueVoxellation);

        Graph detailGraph = constructDetailGraph(trueVoxellation, trueGraph, trueVars, probInterEdge, probIntraEdge,
                probIntraTwoCycleGivenEdge, numVoxels);
        List<Node> detailVars = detailGraph.getNodes();

        List<Graph> detailGraphs = new ArrayList<Graph>();
        detailGraphs.add(detailGraph);

//        for (int i = 1; i < 10; i++) {
//            Graph detailGraph2 = constructDetailGraph(trueVoxellation, trueGraph, trueVars, probInterEdge, probIntraEdge,
//                    probIntraTwoCycleGivenEdge, numVoxels);
//            detailGraphs.add(detailGraph2);
//        }

        List<SemIm> ims = new ArrayList<SemIm>();
        List<DataSet> dataSets = new ArrayList<DataSet>();

        for (Graph graph : detailGraphs) {
            SemIm im = parameterizeSem(varLow, varHigh, 0.4, 0.1, graph);
            // Simulate data from the SEM.
            DataSet data = im.simulateData(sampleSize, false);

            ims.add(im);
            dataSets.add(data);
        }


        CovarianceMatrix cov0 = new CovarianceMatrix(dataSets.get(0));

        Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

        for (int i = 0; i < fakeVoxellation.length; i++) {
            nodeMap.put(_roiNodes.get(i), listVars(fakeVoxellation[i], detailVars));
        }

        IndependenceTest test = new IndTestMultiFisherZ(nodeMap, cov0, .0000001, dataSets.get(0));

        Pc pc = new Pc(test);
        System.out.println("PC voxelwise" + pc.search());


        List<DataSet> roiDatasets = new ArrayList<DataSet>();

        for (DataSet data : dataSets) {
            double[][] aggregates = calcRoiMeans(sampleSize, data, fakeVoxellation);
//        double[][] aggregates = greatestVariance(sampleSize, data, fakeVoxellation);
//        double[][] aggregates = calcFirstPrincipleComponent(sampleSize, data, voxellation);
            DataSet roiDataset = ColtDataSet.makeContinuousData(_roiNodes, aggregates);
            roiDatasets.add(roiDataset);
        }

        IndependenceTest test2 = new IndTestFisherZ(roiDatasets.get(0), .001);

        Fas pc2 = new Fas(test2);
        pc2.setDepth(0);
        System.out.println("Correlation" + pc2.search());

        IndependenceTest test3 = new IndTestPartialCorrelation(roiDatasets.get(0), .00001);

        Fas pc3 = new Fas(test3);
        pc3.setDepth(0);
        System.out.println("Partial correlation " + pc3.search());
    }

    /**
     * Moves the first numVoxels voxels from fromROI to toROI, where the ROIs are numbered as in given[0]...given[n].
     */
    private int[][] move(String[] trueNames, int[][] given, int numVoxels, String fromRoi, String toRoi) {
        int fromIndex = index(trueNames, fromRoi);
        int toIndex = index(trueNames, toRoi);

        int[][] revised = new int[given.length][];

        for (int i = 0; i < given.length; i++) {
            revised[i] = new int[given[i].length];
            System.arraycopy(given[i], 0, revised[i], 0, given[i].length);
        }

        if (fromIndex == toIndex) {
            return revised;
        }

        int[] transfer = new int[numVoxels];
//        int[] givenFrom = shuffle(Arrays.copyOf(given[fromIndex], given[fromIndex].length));
        int[] givenFrom = given[fromIndex];

        for (int i = 0; i < transfer.length; i++) {
            transfer[i] = givenFrom[i];
        }

        revised[fromIndex] = new int[givenFrom.length - numVoxels];

        for (int i = 0; i < givenFrom.length - numVoxels; i++) {
            revised[fromIndex][i] = givenFrom[i + numVoxels];
        }

        revised[toIndex] = new int[given[toIndex].length + numVoxels];

        for (int i = 0; i < numVoxels; i++) {
            revised[toIndex][given[toIndex].length + i] = transfer[i];
        }

        for (int i = 0; i < given[toIndex].length; i++) {
            revised[toIndex][i] = given[toIndex][i];
        }

        return revised;
    }

    /**
     * Moves the first numVoxels voxels from fromROI to toROI, where the ROIs are numbered as in given[0]...given[n].
     */
    private int[][] delete(String[] trueNames, int[][] given, int numVoxels, String fromRoi) {
        int fromIndex = index(trueNames, fromRoi);

        int[][] revised = new int[given.length][];

        for (int i = 0; i < given.length; i++) {
            revised[i] = new int[given[i].length];

            if (i != fromIndex) {
                System.arraycopy(given[i], 0, revised[i], 0, given[i].length);
            } else {
                System.arraycopy(given[i], 0, revised[i], 0, given[i].length - numVoxels);
            }
        }

        return revised;
    }

    private int index(String[] trueNames, String fromRoi) {
        for (int i = 0; i < trueNames.length; i++) {
            if (fromRoi.equals(trueNames[i])) {
                return i;
            }
        }

        return -1;
    }

    int[] shuffle(int[] arr) {
        List<Integer> list = new ArrayList<Integer>();

        for (int i = 1; i <= arr.length; i++) {
            list.add(i);
        }

        Collections.shuffle(list);

        int[] arr2 = new int[arr.length];

        for (int i = 0; i < list.size(); i++) {
            arr2[i] = list.get(i);
        }

        return arr2;
    }

    public int[] runTest(List<Node> problem, String graphSpec, boolean independent, String[] trueVarNames, String[] fakeNames,
                         int[][] trueVoxellation, Map<String, int[][]> fakeVoxellations, Map<String, Node> roiNodes,
                         double alpha, int sampleSize,
                         double probInterEdge, double probIntraEdge, double probIntraTwoCycleGivenEdge, IndTestType testType,
                         double varLow, double varHigh) {

        System.out.println();
        System.out.println();
        System.out.println("======================================================================");

        // Construct the true metagraph and the true detail graph.
        Graph trueGraph = GraphConverter.convert(graphSpec);

        List<Node> trueVars = new ArrayList<Node>();
        for (String name : trueVarNames) {
            trueVars.add(trueGraph.getNode(name));
        }

        int numVoxels = numVoxels(trueVoxellation);

        Graph detailGraph = constructDetailGraph(trueVoxellation, trueGraph, trueVars, probInterEdge, probIntraEdge,
                probIntraTwoCycleGivenEdge, numVoxels);
        List<Node> detailVars = detailGraph.getNodes();

//        System.out.println(detailGraph);

        // Construct the voxellations of the true graph.
        System.out.println("Sample size = " + sampleSize);
        System.out.println("Alpha = " + alpha);
        System.out.println("True graph = " + graphSpec);
        System.out.println("True var order = " + trueVars);

        Node x = problem.get(0);
        Node y = problem.get(1);
        List<Node> rest = new ArrayList<Node>(problem);
        rest.remove(x);
        rest.remove(y);

        System.out.println(independenceFact(x, y, rest));

        System.out.println();

        for (String name : fakeVoxellations.keySet()) {
            System.out.print(name + "\t");
        }

        System.out.println();

        List<String> names = new ArrayList<String>(fakeVoxellations.keySet());
        int[] counts = new int[names.size()];

        for (int run = 0; run < 10; run++) {

            // Parameterize the SEM, without changing the edges in the detail graph.
            SemIm im = parameterizeSem(varLow, varHigh, 0.4, 0.1, detailGraph);

            // Simulate data from the SEM.
            DataSet data = im.simulateData(sampleSize, false);
            CovarianceMatrix cov = null;

            if (testType == IndTestType.Voxelwise) {
                cov = new CovarianceMatrix(data);
            }

            List<Node> _roiNodes = new ArrayList<Node>();
            for (String s : fakeNames) _roiNodes.add(roiNodes.get(s));


            // For each voxellation, calculate the independence.
            for (int f = 0; f < names.size(); f++) {
                int[][] voxellation = fakeVoxellations.get(names.get(f));

                if (testType == IndTestType.Voxelwise) {
                    Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

                    for (int i = 0; i < voxellation.length; i++) {
                        nodeMap.put(_roiNodes.get(i), listVars(voxellation[i], detailVars));
                    }

//                    MultiGes ges = new MultiGes(nodeMap, cov);
//                    ges.setPenaltyDiscount(1);
//                    Graph g = ges.search();
//
//                    System.out.println(g);

                    IndTestMultiFisherZ test = new IndTestMultiFisherZ(nodeMap, cov, alpha, data);
                    boolean indep = test.isIndependent(x, y, rest);
                    int count = (indep == independent) ? 1 : 0;
                    counts[f] += count;
                    System.out.print(count + "\t");

                    if (!indep) numDependent++;
                    numTests++;
                } else if (testType == IndTestType.RoiMeans) {
                    double[][] aggregates = calcRoiMeans(sampleSize, data, voxellation);
                    DataSet roiDataset = ColtDataSet.makeContinuousData(_roiNodes, aggregates);
                    IndependenceTest test = new IndTestFisherZ(roiDataset, alpha);
                    boolean indep = test.isIndependent(x, y, rest);

                    int count = (indep == independent) ? 1 : 0;
                    counts[f] += count;
                    System.out.print(count + "\t");
                } else if (testType == IndTestType.RoiMeansConditionalCorr) {
                    double[][] means = calcRoiMeans(sampleSize, data, voxellation);
                    DataSet roiDataset = ColtDataSet.makeContinuousData(_roiNodes, means);
                    IndependenceTest test = new IndTestConditionalCorrelation(roiDataset, alpha);
                    boolean indep = test.isIndependent(x, y, rest);
                    int count = (indep == independent) ? 1 : 0;
                    counts[f] += count;
                    System.out.print(count + "\t");
                }
            }

            System.out.println();

        }

        System.out.println();

        for (int i = 0; i < names.size(); i++) {
            System.out.print((counts[i] * 10) + "\t");
        }

        System.out.println();

        System.out.println();

        return counts;
    }


    private SemIm parameterizeSem(double varLow, double varHigh, double coefMean, double coefStd, Graph detailGraph) {
        SemImInitializationParams params = new SemImInitializationParams();
//        params.setVarRange(varLow, varHigh);

        SemPm pm = new SemPm(detailGraph);
        SemIm im = new SemIm(pm, params);

        for (Parameter param : im.getSemPm().getParameters()) {
            if (param.getType() == ParamType.COEF) {
                double d = RandomUtil.getInstance().nextNormal(coefMean, coefStd);
                if (d < coefMean - 2 * coefStd) d = coefMean - 2 * coefStd;
                if (d > coefMean + 2 * coefStd) d = coefMean + 2 * coefStd;
//                if (d < 0) d = 0;

                im.setEdgeCoef(param.getNodeA(), param.getNodeB(), d);
            } else if (param.getType() == ParamType.VAR) {
                double d = RandomUtil.getInstance().nextUniform(varLow, varHigh);
                im.setErrVar(param.getNodeA(), d);
            }
        }

        return im;
    }

    private double[][] calcRoiMeans(int sampleSize, DataSet data, int[][] voxellation) {
        double[][] means = new double[sampleSize][voxellation.length];

        // Calculate means over voxels in each ROI.
        for (int q = 0; q < sampleSize; q++) {
            for (int s = 0; s < voxellation.length; s++) {
                double sum = 0.0;

                for (int h = 0; h < voxellation[s].length; h++) {
                    int column = voxellation[s][h];
                    sum += data.getDouble(q, column);
                }

                means[q][s] = sum / voxellation[s].length;
            }
        }

        return means;
    }

    private double[][] calcRoiMeans2(List<DataSet> datasets) {
        int sampleSize = datasets.get(0).getNumRows();
        double[][] means = new double[sampleSize][datasets.size()];

        // Calculate means over voxels in each ROI.
        for (int q = 0; q < sampleSize; q++) {
            for (int s = 0; s < datasets.size(); s++) {
                double sum = 0.0;

                for (int h = 0; h < datasets.get(s).getNumColumns(); h++) {
                    sum += datasets.get(s).getDouble(q, h);
                }

                means[q][s] = sum / datasets.size();
            }
        }

        return means;
    }

    private double[][] calcFirstPrincipleComponent(int sampleSize, DataSet data, int[][] voxellation) {
        double[][] sums = new double[sampleSize][voxellation.length];

        for (int s = 0; s < voxellation.length; s++) {
            RealMatrix m = new BlockRealMatrix(sampleSize, voxellation[s].length);

            for (int i = 0; i < sampleSize; i++) {
                for (int j = 0; j < voxellation[s].length; j++) {
                    m.setEntry(i, j, data.getDouble(i, voxellation[s][j]));
                }
            }

            SingularValueDecomposition d = new SingularValueDecomposition(m);

            RealMatrix s1 = d.getU();

            double[] c = s1.getColumn(0);

            for (int i = 0; i < sampleSize; i++) {
                sums[i][s] = c[i];
            }
        }

        return sums;
    }

    private double[][] greatestVariance(int sampleSize, DataSet data, int[][] voxellation) {
        double[][] sums = new double[sampleSize][voxellation.length];
        TetradMatrix m = data.getDoubleData();

        // Calculate sums over voxels in each ROI.
        for (int s = 0; s < voxellation.length; s++) {
            double maxVar = -1;
            int index = -1;

            for (int h = 0; h < voxellation[s].length; h++) {
                double[] col = m.getColumn(voxellation[s][h]).toArray();
                double var = StatUtils.variance(col);
                if (var > maxVar) {
                    maxVar = var;
                    index = h;
                }
            }

            for (int q = 0; q < sampleSize; q++) {
                sums[q][s] = m.get(q, index);
            }
        }

        return sums;
    }

    private void printVoxellations(int[][] voxellation) {
        for (int i = 0; i < voxellation.length; i++) {
            System.out.print(voxellation[i].length + ": \t");

            for (int j = 0; j < voxellation[i].length; j++) {
                System.out.print(voxellation[i][j] + " ");
            }

            System.out.println();
        }

        System.out.println();
    }

    private void printVoxellations(Map<String, int[][]> fakeVoxellations) {
        for (String s : fakeVoxellations.keySet()) {
            System.out.println(s);
            printVoxellations(fakeVoxellations.get(s));
        }
    }

    private List<Node> problem(Map<String, Node> roiNodes, String... names) {
        List<Node> nodes = new ArrayList<Node>();

        for (String name : names) {
            nodes.add(roiNodes.get(name));
        }

        return nodes;
    }

    private int numVoxels(int[][] truth) {
        int maxVoxel = -1;

        for (int i = 0; i < truth.length; i++) {
            for (int j = 0; j < truth[i].length; j++) {
                if (truth[i][j] > maxVoxel) maxVoxel = truth[i][j];
            }
        }

        return maxVoxel + 1;
    }

    private Graph constructDetailGraph(int[][] truth, Graph trueGraph, List<Node> trueVars,
                                       double probInterEdge, double probIntraEdge, double probIntraTwoCycleGivenEdge,
                                       int numVoxels) {
        List<Node> vars = new ArrayList<Node>();

        for (int i = 0; i < (int) (1.5 * numVoxels); i++) {
            vars.add(new ContinuousVariable("X" + i));
        }

        Graph graph = new EdgeListGraph(vars);

        for (Edge edge : trueGraph.getEdges()) {
            int first = trueVars.indexOf(Edges.getDirectedEdgeTail(edge));
            int second = trueVars.indexOf(Edges.getDirectedEdgeHead(edge));

            int numFirst = truth[first].length;
            int numSecond = truth[second].length;

            int numEdges = (int) (numFirst * numSecond * probInterEdge);

            for (int i = 0; i < numEdges; i++) {
                int y = truth[first][RandomUtil.getInstance().nextInt(truth[first].length)];
                int w = truth[second][RandomUtil.getInstance().nextInt(truth[second].length)];

                if (!graph.isAdjacentTo(vars.get(y), vars.get(w))) {
                    graph.addDirectedEdge(vars.get(y), vars.get(w));
                }
            }
        }

        for (Node node : trueGraph.getNodes()) {
            int index = trueVars.indexOf(node);

            for (int i = 0; i < truth[index].length; i++) {
                for (int j = i + 1; j < truth[index].length; j++) {
                    if (RandomUtil.getInstance().nextDouble() < probIntraEdge) {
                        int i1 = i;
                        int j1 = j;

                        if (!graph.isAdjacentTo(vars.get(truth[index][i1]), vars.get(truth[index][j1]))) {
                            graph.addDirectedEdge(vars.get(truth[index][i1]), vars.get(truth[index][j1]));
                        }

                        if (RandomUtil.getInstance().nextDouble() < probIntraTwoCycleGivenEdge) {
                            graph.addDirectedEdge(vars.get(truth[index][j1]), vars.get(truth[index][i1]));
                        }
                    }
                }
            }
        }

        return graph;
    }

    private int[][] constructRois(int... voxelsPerRoi) {
        int[][] rois = new int[voxelsPerRoi.length][];
        int count = 0;

        for (int i = 0; i < voxelsPerRoi.length; i++) {
            rois[i] = shuffle(new int[voxelsPerRoi[i]]);

            for (int j = 0; j < voxelsPerRoi[i]; j++) {
                rois[i][j] = count++;
            }
        }

        return rois;
    }

    public double coefValue(double low, double high, boolean symmetric) {
        double c = RandomUtil.getInstance().nextDouble();
        double value = low + c * (high - low);

        if (symmetric && RandomUtil.getInstance().nextDouble() < 0.5) {
            value *= -1.0;
        }

        return value;
    }

    private List<Node> listVars(int[] indices, List<Node> vars) {
        List<Node> nodes = new ArrayList<Node>();

        for (int i : indices) {
            nodes.add(vars.get(i));
        }

        return nodes;
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

    public void test11() {
        double minDistance = Double.POSITIVE_INFINITY;

        for (int d : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}) {

            A:
            for (double a = 0.; a <= 10.; a += 0.1) {
                for (double b = -2; b < 2; b += .1) {
                    double sum = 0.0;

                    for (double z = 1; z <= 8.0; z += 0.001) {
                        double f1 = 1 - RandomUtil.getInstance().normalCdf(0, 1, z);
                        double f2 = a / pow((z - b), d);

                        sum += pow(f1 - f2, 2.0);
                    }

                    double distance = sqrt(sum);

                    if (distance < minDistance) {
                        minDistance = distance;
                        System.out.println("\nD  " + d + " a = " + a + " b = " + b + " distance = " + minDistance);
                    }
                }
            }
        }
    }

    public void test14() {
        Graph graph = GraphUtils.randomBifactorModel(3, 1, 10, 1, 1, 0);
        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(500, Boolean.FALSE);

        SemEstimator estimator = new SemEstimator(data, pm);
        SemIm fitResult = estimator.estimate();

    }

    // Approximation to the Normal CDF.
    public double polyaApproximation(double z) {
        return 0.5 * (1 + sqrt(1 - exp((-2 / Math.PI) * z * z)));
    }

    public double fisherZ(double r, double N) {
        return 0.5 * sqrt(N) * log((1 + r) / (1 - r));
    }

    public void test25() {
        for (int i = 0; i < 25; i++) {
            double a = RandomUtil.getInstance().nextDouble() * 5 + 2;
            System.out.println(a + " " + (log(a) - log(a - 1)));
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestHippocampus.class);
    }
}



