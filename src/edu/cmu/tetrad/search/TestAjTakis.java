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

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradMatrix;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static java.lang.Math.sqrt;

/**
 * Runs tests on AJ and Takis' data.
 *
 * @author Joseph Ramsey
 */
public class TestAjTakis extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestAjTakis(String name) {
        super(name);
    }


    public void test1() {
        try {
            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/aj/Synth_data_main/data";
            String suffix = "undir.txt";
//            String suffix = "directed.txt";
//            String suffix = "partdir.txt";


            String filename = "synth_data_" + suffix;
            DataSet data = loadData(dir, filename);
            List<Node> variables = data.getVariables();

            IndependenceTest test = new IndTestFisherZ(data, 0.01);
            Pc pc = new Pc(test);
            Graph graph = pc.search();
            graph = GraphUtils.replaceNodes(graph, variables);
            graph = GraphUtils.undirectedGraph(graph);

            int[][] _graph = convertGraph(graph, variables);

            String graphName = "true_graph_" + suffix;
            DataSet graphData = loadData(dir, graphName);

            Graph trueGraph = getGraph(graphData);
            trueGraph = GraphUtils.replaceNodes(trueGraph, variables);

            int[][] _trueGraph = convertGraph(trueGraph, variables);

            int tp = 0, fp = 0, tn = 0, fn = 0;

            for (int i = 0; i < variables.size(); i++) {
                for (int j = 0; j < variables.size(); j++) {
                    int k1 = _trueGraph[i][j];
                    int k2 = _graph[i][j];

                    if (k1 == 1 && k2 == 1) {
                        tp++;
                    }
                    else if (k1 == 0 && k2 == 1) {
                        fp++;
                    }
                    else if (k1 == 1 && k2 == 0) {
                        fn++;
                    }
                    else if (k1 == 0 && k2 == 0) {
                        tn++;
                    }
                }
            }

//            System.out.println(trueGraph);

            System.out.println(GraphUtils.graphComparisonString("Est", graph, "True", trueGraph, false));

//            GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison2(graph, trueGraph);
//
//            int fp = comparison.getAdjFp();
//            int fn = comparison.getAdjFn();
//            int tp = comparison.getAdjCorrect();
//            int tn = trueGraph.getNumEdges() - tp;

            double mcc = mcc(tp, fp, tn, fn);

            System.out.println("MCC = " + mcc);

            int hamming = hammingDistance(_graph, _trueGraph);

            System.out.println("Hamming = " + hamming);

//            System.out.println(GraphUtils.graphComparisonString("Found", graph, "True", trueGraph, false));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[][] convertGraph(Graph graph, List<Node> variables) {
        int[][] _graph = new int[variables.size()][variables.size()];

        for (int i = 0; i < variables.size(); i++) {
            for (int j = 0; j < variables.size(); j++) {
                Node ni = variables.get(i);
                Node nj = variables.get(j);
                Edge e = graph.getEdge(ni, nj);

                if (e == null) continue;

                if (e.pointsTowards(nj)) {
                    _graph[i][j] = 1;
                }
                else if (e.pointsTowards(ni)) {
                    _graph[j][i] = 1;
                }
                else {
                    _graph[i][j] = 1;
                    _graph[j][i] = 1;
                }
            }
        }

        return _graph;
    }

    private enum algorithm {PC, PC_STABLE, CPC, GES, CCD, RFCI}
    private enum tests {FisherZ, Mixed}

    public void test3() {
        try {
            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/aj/Synth_data_main/data";
//            String suffix = "undir.txt";
//            String suffix = "directed.txt";
//            String suffix = "partdir.txt";

            String outdir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/aj/out";

            List<Node> nodes = null;

            for (String _suffix : new String[]{"undir.txt", "directed.txt", "partdir.txt"}) {
                String filename = "synth_data_" + _suffix;
                DataSet data1 = loadData(dir, filename);
                TetradMatrix doubleData = data1.getDoubleData();

                for (int i = 0; i < 5; i++) {

                    DataSet data = ColtDataSet.makeData(data1.getVariables(), doubleData.getPart(
                            i * 500, (i + 1) * 500 - 1, 0, data1.getNumColumns() - 1));

                    System.out.println(data.getNumRows() + " x " + data.getNumColumns());
                    List<Node> variables = data.getVariables();

                    double[] alphas = {0.05, 0.01, 0.001, 0.0001};

                    String graphName = "true_graph_" + _suffix;
                    DataSet graphData = loadData(dir, graphName);

                    Graph trueGraph = getGraph(graphData);

                    System.out.println(trueGraph);

                    if (nodes == null) {
                        nodes = trueGraph.getNodes();
                    }

                    trueGraph = GraphUtils.replaceNodes(trueGraph, variables);

                    int[][] _trueGraph = convertGraph(trueGraph, variables);

                    for (algorithm alg : algorithm.values()) {

                        ALPHAS:
                        for (double alpha : alphas) {

                            TEST:
                            for (tests test : tests.values()) {
                                if (alg == algorithm.GES && test == tests.Mixed) continue TEST;

                                String filePrefix = _suffix + "." + i + "." + alg + "." + alpha + "." + test;

                                File[] files = new File(outdir).listFiles();

                                for (File _file : files) {
                                    if (_file.getName().startsWith(filePrefix)) {
                                        continue TEST;
                                    }
                                }

                                IndependenceTest indTest;

                                if (test == tests.FisherZ) {
                                    indTest = new IndTestFisherZ(data, alpha);
                                } else {
                                    indTest = new IndTestMultinomialLogisticRegression(data, alpha);
                                }

                                Graph graph = null;

                                if (alg == algorithm.PC) {
                                    Pc s = new Pc(indTest);
                                    s.setVerbose(false);
                                    graph = s.search();
                                }
                                if (alg == algorithm.PC_STABLE) {
                                    PcStable s = new PcStable(indTest);
                                    s.setVerbose(false);
                                    graph = s.search();
                                }
                                if (alg == algorithm.CPC) {
                                    Cpc s = new Cpc(indTest);
                                    s.setVerbose(false);
                                    graph = s.search();
                                }
                                if (alg == algorithm.GES && test == tests.FisherZ) {
                                    Ges s = new Ges(data);
                                    s.setVerbose(false);
                                    graph = s.search();
                                }
                                if (alg == algorithm.CCD) {
                                    Ccd s = new Ccd(indTest);
                                    s.setVerbose(false);
                                    graph = s.search();
                                }
                                if (alg == algorithm.RFCI) {
                                    Fci s = new Fci(indTest);
                                    s.setRFCI_Used(true);
                                    s.setVerbose(false);
                                    graph = s.search();
                                }

                                int[][] _graph = convertGraph(graph, variables);

                                int tp = 0, fp = 0, tn = 0, fn = 0;

                                for (int _i = 0; _i < variables.size(); _i++) {
                                    for (int _j = 0; _j < variables.size(); _j++) {
                                        int k1 = _trueGraph[_i][_j];
                                        int k2 = _graph[_i][_j];

                                        if (k1 == 1 && k2 == 1) {
                                            tp++;
                                        } else if (k1 == 0 && k2 == 1) {
                                            fp++;
                                        } else if (k1 == 1 && k2 == 0) {
                                            fn++;
                                        } else if (k1 == 0 && k2 == 0) {
                                            tn++;
                                        }
                                    }
                                }

                                double mcc = mcc(tp, fp, tn, fn);
                                int hamming = hammingDistance(_graph, _trueGraph);

                                new File(outdir).mkdirs();
                                NumberFormat nf = new DecimalFormat("0.0000");

                                File outFile = new File(outdir, _suffix + "." + i + "." +
                                        alg + "." + alpha + "." + test + "." + nf.format(mcc) + "." + hamming + ".txt");
                                PrintWriter out = new PrintWriter(outFile);

                                writeAdjacencyMatrix(nodes, _graph, out);
                                out.println(graph);
                                out.println();
                                out.println("MCC = " + nf.format(mcc));
                                out.println("Hamming Distance = " + hamming);
                                out.close();

                                System.out.println(_suffix + "\t" + alg + "\t" + (alg == algorithm.GES ? "-" : alpha) + "\t" + nf.format(mcc) + "\t" + hamming);

                                if (alg == algorithm.GES) break ALPHAS;
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeAdjacencyMatrix(List<Node> nodes, int[][] graph, PrintWriter out2) {
        for (int p = 0; p < nodes.size(); p++) {
            for (int q = 0; q < nodes.size(); q++) {
                out2.print(graph[p][q] + " ");
            }

            out2.println();
        }

        out2.flush();
    }

    public void testParanoia() {
        Graph g = new EdgeListGraph();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");
        g.addNode(x);
        g.addNode(y);
        g.addNode(z);

        g.addDirectedEdge(x, y);
        g.addDirectedEdge(z, y);

        System.out.println(g.getNodes());

        writeAdjacencyMatrix(g.getNodes(), convertGraph(g, g.getNodes()), new PrintWriter(System.out));
    }


    public void test2() {
        System.out.println(mcc(1, 1, 300, 1));
    }

    private double mcc(long tp, long fp, long tn, long fn) {
        double denom = sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
        if (denom == 0) denom = 1;
        return (tp * tn - fp * fn) / denom;
    }

    private int hammingDistance(int[][] g1, int[][] g2) {
        int differences = 0;

        for (int i = 0; i < g1.length; i++) {
            for (int j = 0; j < g1[0].length; j++) {
//                if (j > i && g1[i][j] == 1 && g1[j][i] == 1) {
//                    continue;
//                }
//                if (j > i && g2[i][j] == 1 && g2[j][i] == 1) {
//                    continue;
//                }
                if (g1[i][j] != g2[i][j]) differences++;
            }
        }

        return differences;
    }

    private Graph getGraph(DataSet graphData) {
        List<Node> variables = graphData.getVariables();
        Graph g = new EdgeListGraph(variables);

        for (int i = 0; i < graphData.getNumRows(); i++) {
            for (int j = 0; j < graphData.getNumColumns(); j++) {
                int v1 = graphData.getInt(i, j);
                int v2 = graphData.getInt(j, i);

                if (v1 == 1 && v2 == 1) {
                    if (!g.isAdjacentTo(variables.get(i), variables.get(j))) {
                        g.addUndirectedEdge(variables.get(i), variables.get(j));
                    }
                }
                else if (v1 == 0 && v2 == 1) {
                    if (!g.isAdjacentTo(variables.get(i), variables.get(j))) {
                        g.addDirectedEdge(variables.get(j), variables.get(i));
                    }
                }
                else if (v1 == 1 && v2 == 0) {
                    if (!g.isAdjacentTo(variables.get(i), variables.get(j))) {
                        g.addDirectedEdge(variables.get(i), variables.get(j));
                    }
                }
            }
        }

        return g;
    }

    private DataSet loadData(String dir, String filename) throws IOException {
        File file = new File(dir, filename);
        DataReader reader = new DataReader();
        reader.setVariablesSupplied(false);
        return reader.parseTabular(file);
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestAjTakis.class);
    }
}



