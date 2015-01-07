///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
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
import edu.cmu.tetrad.search.mb.Mmhc;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.StandardizedSemIm;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradMatrix;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.sqrt;


/**
 * Tests the IndTestTimeSeries class.
 *
 * @author Joseph Ramsey
 */
public class ExploreSanchezGlymourRamsey {
    String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/glymour.sanchez.ramsey.paper/";
//    String graphDir = "/home/jdramsey/proj/sanchezetal/";

    public void test1() {
        try {
//            String graphDir = "/home/jdramsey/test14/";
            File _graphDir = new File(graphDir);

            if (!_graphDir.exists()) _graphDir.mkdir();

            List<DataSet> dataSets = loadDataSets();
//            List<DataSet> _dataSets = new ArrayList<DataSet>();

//            for (int m = 0; m < 67; m++) {
//                DataSet dataSet = dataSets.get(m);
//                dataSet = DataUtils.standardizeData(dataSet);
//                _dataSets.add(dataSet);
//            }

//            for (int i = 0; i < 67; i++) {
//                for (int penalty : new int[]{1, 2, 3, 4, 5}) {
//                    runSingleDataSet(graphDir, dataSets, i, penalty);
//                }
//            }

//            for (boolean standardized : new boolean[]{true, false}) {
//                for (boolean concatenated : new boolean[]{true, false}) {
//                    for (boolean crossvalidation : new boolean[]{false, true}) {
//                        for (int penalty : new int[]{/*1, 2, 3, 4, */5}) {
//                            runImages(graphDir, dataSets, standardized, concatenated, crossvalidation, penalty);
//                        }
//                    }
//                }
//            }

//            for (boolean concatenated : new boolean[]{false, true}) {
//                for (boolean standardized : new boolean[]{true, true}) {
//                    for (boolean fdr : new boolean[]{false, true}) {
//                        for (boolean crossvalidation : new boolean[]{false, true}) {
//                            for (double alpha : new double[]{/*0.00001, 0.0001, 0.001,*/ 0.01, /*0.05*/}) {
//                                runPc(graphDir, dataSets, standardized, concatenated, crossvalidation, fdr, alpha);
//                            }
//                        }
//                    }
//                }
//            }

//            for (boolean standardized : new boolean[]{false, true}) {
//                for (boolean fdr : new boolean[]{false, true}) {
//                    for (double alpha : new double[]{0.05, 0.01, 0.001, 0.0001, 0.00001}) {
//                        for (int dataIndex = 0; dataIndex < dataSets.size(); dataIndex++) {
//                            runSinglePcGraph(graphDir, dataSets, dataIndex, standardized, fdr, alpha);
//                        }
//                    }
//                }
//            }

//            for (boolean concatenated : new boolean[]{true}) {
//                for (boolean standardized : new boolean[]{true, true}) {
//                    for (boolean fdr : new boolean[]{false}) {
//                        for (boolean crossvalidation : new boolean[]{false}) {
//                            for (double alpha : new double[]{/*0.00001, 0.0001, 0.001, */0.1, 0.05}) {
//                                runPc(graphDir, dataSets, standardized, concatenated, crossvalidation, fdr, alpha);
//                            }
//                        }
//                    }
//                }
//            }

////            // Multidata cases.
//            for (boolean standardized : new boolean[] {false, true}) {
//                for (boolean fdr : new boolean[] {false, true}) {
//                    for (double alpha : new double[]{0.05, 0.01, 0.001, 0.0001, 0.00001}) {
//                        List<Graph> graphs = new ArrayList<Graph>();
//
//                        for (int dataIndex = 0; dataIndex < dataSets.size(); dataIndex++) {
//                            Graph graph = loadSinglePcGraph(graphDir, dataIndex, standardized, fdr, alpha);
//                            graphs.add(graph);
//                        }
//
//                        for (double percent : new double[] {0.5, 0.75, 0.9}) {
//                            combineGraphs(graphDir, graphs, percent, standardized, fdr, alpha);
//                        }
//                    }
//                }
//            }

//            // Concatenated data.
//            for (boolean standardized : new boolean[] {true}) {
//                if (standardized) {
//                    List<DataSet> _dataSets = new ArrayList<DataSet>();
//                    for (DataSet d : dataSets) {
//                        d = DataUtils.standardizeData(d);
//                        _dataSets.add(d);
//                    }
//
//                    dataSets = _dataSets;
//                }
//
//                DataSet concatenated = DataUtils.concatenateData(dataSets);
//
//                for (boolean fdr : new boolean[] {false, true}) {
//                    for (double alpha : new double[]{/*0.05, 0.01,*/ 0.001, 0.0001, 0.00001}) {
//                        String name = "pc.cz.concatenated.67." +
//                                (standardized ? "Standardized" + "." : "") +
//                                (fdr ? "fdr" + "." : "") +
//                                alpha + ".txt";
//                        File dir = new File(graphDir + "/results3/");
//                        dir.mkdirs();
//                        File file = new File(dir, name);
//                        System.out.println("DOING " + file.getAbsolutePath());
//                        if (file.exists()) continue;
//
//                        CovarianceMatrix covariance = new CovarianceMatrix(concatenated);
//                        IndTestFisherZ test = new IndTestFisherZ(covariance, alpha);
//
//                        Pc pc = new Pc(test);
//                        pc.setDepth(3);
//                        pc.setFdr(fdr);
//                        pc.setVerbose(true);
//                        Graph graph = pc.search();
//
//                        writeGraph(file, graph);
//
//                        System.out.println(file.getAbsolutePath());
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printCrossValidationsPc() {
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/glymour.sanchez.ramsey.paper/";

        boolean concatenated = false;
        boolean crossvalidation = true;
        boolean fdr = false;

        for (int i = 0; i < 67; i++) {
            boolean standardized = true;
            double alpha = 0.01;

            String name1 = "pc.cr." + (standardized ? "Standardized" + "." : "") +
                    (concatenated ? "concatenated" + "." : "") +
                    (crossvalidation ? "crossvalidation" + "." : "") +
                    (fdr ? "fdr" + "." : "") +
                    "dataset." + (i + 1) + "." +
                    alpha + ".txt";

//            standardized = false;

            String name2 = "pc.cr.single." + (standardized ? "Standardized" + "." : "") +
                    (fdr ? "fdr" + "." : "") +
                    "dataset." + (i + 1) + "." +
                    alpha + ".txt";

            Graph graph1 = GraphUtils.loadGraphTxt(new File(graphDir + "/results4/" + name1));
            Graph graph2 = GraphUtils.loadGraphTxt(new File(graphDir + "/results4/" + name2));

            GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison2(graph1, graph2);

            System.out.println(graph1.getEdges().size() + " " + graph2.getEdges().size() + " " +
                    comparison.getAdjCorrect() + " " + comparison.getAdjFp() + " " +
                    comparison.getAdjFn());

        }

    }

    private Graph combineGraphs(String graphDir, List<Graph> graphs, double percent, boolean standardized,
                                boolean fdr, double alpha) {
        List<Graph> _graphs = new ArrayList<Graph>();
        List<Node> nodes = graphs.get(0).getNodes();

        for (Graph graph : graphs) {
            _graphs.add(GraphUtils.replaceNodes(graph, nodes));
        }

        graphs = _graphs;

        Graph combined = new EdgeListGraph(nodes);
        Graph complete = new EdgeListGraph(nodes);
        complete.fullyConnect(Endpoint.TAIL);

        for (Edge edge : complete.getEdges()) {
            int exists = 0;
            int arrow1 = 0;
            int arrow2 = 0;

            for (Graph g : graphs) {
                if (g.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                    exists++;

                    Edge e = g.getEdge(edge.getNode1(), edge.getNode2());

                    if (e.getEndpoint1() == Endpoint.ARROW) {
                        arrow1++;
                    }
                    if (e.getEndpoint2() == Endpoint.ARROW) {
                        arrow2++;
                    }
                }
            }

            if (exists > graphs.size() * percent) {
                edge = new Edge(edge);

                if (arrow1 > graphs.size() * percent) {
                    edge.setEndpoint1(Endpoint.ARROW);
                }
                if (arrow2 > graphs.size() * percent) {
                    edge.setEndpoint2(Endpoint.ARROW);
                }

                combined.addEdge(edge);

                String name = "pc.multidata.67." +
                        "percent." + percent + "." +
                        (standardized ? "Standardized" + "." : "") +
                        (fdr ? "fdr" + "." : "") +
                        alpha + ".txt";
                File dir = new File(graphDir + "/results/");
                dir.mkdirs();
                File file = new File(dir, name);
                writeGraph(file, combined);
            }
        }

        return combined;
    }

    private void runSinglePcGraph(String graphDir, List<DataSet> dataSets, int dataIndex, boolean standardized, boolean fdr, double alpha) {
        String name = "pc.cr.single." + (standardized ? "Standardized" + "." : "") +
                (fdr ? "fdr" + "." : "") +
                "dataset." + (dataIndex + 1) + "." +
                alpha + ".txt";
        File dir = new File(graphDir + "/results4/");
        dir.mkdirs();
        File file = new File(dir, name);
        if (file.exists()) return;

        DataSet dataSet = dataSets.get(dataIndex);

        if (standardized) {
            dataSet = DataUtils.standardizeData(dataSet);
        }

        IndTestFisherZ test = new IndTestFisherZ(dataSet, alpha);

        Pc pc = new Pc(test);
        pc.setFdr(fdr);
        Graph graph = pc.search();

        System.out.println(file.getAbsolutePath());

        writeGraph(file, graph);
    }

    private Graph loadSinglePcGraph(String graphDir, int dataIndex, boolean standardized, boolean fdr, double alpha) {
        String name = "pc.single." + (standardized ? "Standardized" + "." : "") +
                (fdr ? "fdr" + "." : "") +
                "dataset." + (dataIndex + 1) + "." +
                alpha + ".txt";
        File dir = new File(graphDir + "/results/");
        dir.mkdirs();
        File file = new File(dir, name);

        return GraphUtils.loadGraphTxt(file);
    }

    private void runSingleDataSet(String graphDir, List<DataSet> dataSets, int i, int penalty) {
        String name = "images." + "dataset." + (i + 1) + "." +
                penalty + ".txt";
        File dir = new File(graphDir + "/results8/");
        dir.mkdirs();
        File file = new File(dir, name);
        if (file.exists()) return;

        Images images = new Images(Collections.singletonList(dataSets.get(i)));
        images.setPenaltyDiscount(penalty);
        Graph graph = images.search();

        System.out.println(file.getAbsolutePath());

        writeGraph(file, graph);

    }

    enum PC2Alg {pc, cpc, rfci, mbfspattern, mmhc}

    private void testPcVersions() {
        try {
            List<DataSet> dataSets = loadDataSets();

            for (double alpha : new double[]{1e-7, 1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 5e-2, 1e-1}) {
                for (boolean concatenated : new boolean[]{false, true}) {
                    for (boolean standardized : new boolean[]{true, false}) {
                        for (boolean fdr : new boolean[]{false, true}) {
                            for (PC2Alg alg : new PC2Alg[]{PC2Alg.pc}) {//, PC2Alg.cpc}) {//, PC2Alg.rfci}) {
                                runPc2(graphDir, dataSets, alg, standardized, concatenated, fdr, alpha);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void runPc2(String graphDir, List<DataSet> dataSets, PC2Alg alg, boolean standardized, boolean concatenated,
                        boolean fdr, double alpha) {
        NumberFormat nf = new DecimalFormat("0.00E0");

        String name = alg + "." + (standardized ? "standardized" : "no_standardized") + "." +
                (concatenated ? "concatenated" : "multidata") + "." +
                (fdr ? "fdr" : "no_fdr") + "." +
                nf.format(alpha) + ".txt";

        File dir = new File(graphDir + "/results9/");
        dir.mkdirs();
        File file = new File(dir, name);
        System.out.println("DOING " + file.getAbsolutePath());
        if (file.exists()) return;

        if (standardized) {
            List<DataSet> _dataSets = new ArrayList<DataSet>();
            for (DataSet d : dataSets) {
                _dataSets.add(DataUtils.standardizeData(d));
            }

            dataSets = _dataSets;
        }

        if (concatenated) {
            DataSet concat = DataUtils.concatenateData(dataSets);
            dataSets = Collections.singletonList(concat);
        }

        IndTestFisherZPercentIndependent test = new IndTestFisherZPercentIndependent(dataSets, alpha);
        test.setFdr(fdr);

        FasICov2 fas = new FasICov2(test);
//        FasICov fas = new FasICov(test);
//        fas.setFdr(fdr);

        fas.setVerbose(true);

        Graph graph = null;

        if (alg == PC2Alg.pc) {
            Pc search = new Pc(test);
            search.setDepth(10);
            graph = search.search(fas, test.getVariables());
        } else if (alg == PC2Alg.cpc) {
            Cpc search = new Cpc(test);
            search.setDepth(3);
            graph = search.search(fas, test.getVariables());
        } else if (alg == PC2Alg.rfci) {
            Fci search = new Fci(test);
            search.setRFCI_Used(true);
            search.setDepth(3);
            graph = search.search(fas, test.getVariables());
        } else if (alg == PC2Alg.mbfspattern) {
            Mbfs search = new Mbfs(test, -1);
            search.setDepth(3);
            graph = search.search();
        } else if (alg == PC2Alg.mmhc) {
            Mmhc search = new Mmhc(test);
//            search.setDepth(3);
            graph = search.search();
        }

        System.out.println(graph);
        System.out.println(file.getAbsolutePath());
        writeGraph(file, graph);
    }

    private void runImages(String graphDir, List<DataSet> dataSets, boolean standardized, boolean concatenated, boolean crossvalidation, int penalty) {
        if (standardized) {
            List<DataSet> _dataSets = new ArrayList<DataSet>();
            for (DataSet d : dataSets) {
                _dataSets.add(DataUtils.standardizeData(d));
            }

            dataSets = _dataSets;
        }

        if (crossvalidation) {
            if (concatenated) return;
            else {
                for (int i = 0; i < dataSets.size(); i++) {
                    List<DataSet> _data = new ArrayList<DataSet>();

                    String name = "images." + (standardized ? "Standardized" + "." : "") +
                            (concatenated ? "concatenated" + "." : "") +
                            (crossvalidation ? "crossvalidation" + "." : "") +
                            "dataset." + (i + 1) + "." +
                            penalty + ".txt";
                    File dir = new File(graphDir + "/results/");
                    dir.mkdirs();
                    File file = new File(dir, name);
                    System.out.println("DOING " + file.getAbsolutePath());
                    if (file.exists()) return;

                    for (int j = 0; j < dataSets.size(); j++) {
                        if (i == j) continue;
                        else {
                            _data.add(dataSets.get(i));
                        }
                    }

                    Images images = new Images(_data);
                    images.setPenaltyDiscount(penalty);
                    Graph graph = images.search();

                    System.out.println(file.getAbsolutePath() + " DONE");

                    writeGraph(file, graph);
                }
            }
        } else {
            if (concatenated) {
                dataSets = Collections.singletonList(DataUtils.concatenateData(dataSets));
            }

            String name = "images." + (standardized ? "Standardized" + "." : "") +
                    (concatenated ? "concatenated" + "." : "") +
                    (crossvalidation ? "crossvalidation" + "." : "") +
                    penalty + ".txt";
            File dir = new File(graphDir + "/results/");
            dir.mkdirs();
            File file = new File(dir, name);
            if (file.exists()) return;
            System.out.println("DOING " + file.getAbsolutePath());

            Images images = new Images(dataSets);
            images.setPenaltyDiscount(penalty);
            Graph graph = images.search();

            System.out.println(file.getAbsolutePath());

            writeGraph(file, graph);
        }
    }

    public void test7a() {
        try {
            List<DataSet> dataSets = loadRussData2();

            for (int i = 0; i < dataSets.size(); i++) {
                System.out.println(dataSets.get(i).getNumRows());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test7() {
        try {
            List<DataSet> dataSets = loadRussData2();

            Images images = new Images(dataSets);
//            images.setPenaltyDiscount(3.0);
            Graph graph = images.search();
            List<Node> nodes = graph.getNodes();
//            System.out.println("Images " + graph);

            List<Graph> gesGraphs = new ArrayList<Graph>();

            for (DataSet dataSet : dataSets) {
                Graph _graph = new Images(Collections.singletonList(dataSet)).search();

//                IndTestFisherZ test = new IndTestFisherZ(dataSet, 0.001);
//                Fas3 fas = new Fas3(test);
//                Pc pc = new Pc(test);
//                Graph _graph = pc.search(fas, test.getVariables());


                _graph = GraphUtils.replaceNodes(_graph, nodes);
                System.out.println(_graph);
//                gesGraphs.add(_graph);
                gesGraphs.add(GraphUtils.undirectedGraph(_graph));
            }

            final Map<Edge, Integer> counts = new HashMap<Edge, Integer>();

            for (Graph _graph : gesGraphs) {
                for (Edge edge : _graph.getEdges()) {
                    increment(edge, counts);
                }
            }

            List<Edge> edges = new ArrayList<Edge>(counts.keySet());

            Collections.sort(edges, new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    return counts.get(o2).compareTo(counts.get(o1));
                }
            });

            for (Edge edge : edges) {
                System.out.println(edge + " " + counts.get(edge));
            }

            System.out.println("\n\nCounts for images undirected edges:");

            Graph imagesUndirected = GraphUtils.undirectedGraph(graph);

            for (Edge edge : imagesUndirected.getEdges()) {
                System.out.println(edge + " " + counts.get(edge));
            }

//            System.out.println("\n\nOrienting Images graph using R3 (Nongaussian variables only");
//
//            double alpha = .05;
//
//            final Map<Edge, Integer> counts2 = new HashMap<Edge, Integer>();

//            for (DataSet dataSet : dataSets) {
//                Graph imagesUndirectedNongaussianEdgesOnly = new EdgeListGraph(imagesUndirected.getNodes());
//
//
//                TetradMatrix data = dataSet.getDoubleData();
//                List<Node> _nodes = dataSet.getVariables();
//
////                for (Edge edge : imagesUndirected.getEdges()) {
//////                    Node node1 = edge.getNode1();
//////                    Node node2 = edge.getNode2();
////
//////                    int index1 = _nodes.indexOf(node1);
//////                    int index2 = _nodes.indexOf(node2);
////
//////                    double[] d1 = data.getColumn(index1).toArray();
//////                    double[] d2 = data.getColumn(index2).toArray();
////
//////                    double p1 = new AndersonDarlingTest(d1).getP();
//////                    double p2 = new AndersonDarlingTest(d2).getP();
////
//////                    if (p1 < alpha && p2 < alpha) {
//////                        imagesUndirectedNongaussianEdgesOnly.addEdge(edge);
//////                        System.out.println("a.d. 1 = " + new AndersonDarlingTest(d1).getASquaredStar());
//////                        System.out.println("a.d. 2 = " + new AndersonDarlingTest(d2).getASquaredStar());
//////                    }
////                }
//
//                imagesUndirectedNongaussianEdgesOnly = GraphUtils.replaceNodes(imagesUndirectedNongaussianEdgesOnly, nodes);
//
//                Lofs2 lofs = new Lofs2(imagesUndirectedNongaussianEdgesOnly, Collections.singletonList(dataSet));
//                lofs.setRule(Lofs2.Rule.RSkew);
//                lofs.setAlpha(0.0);
//                Graph g = lofs.orient();
//                g = GraphUtils.replaceNodes(g, nodes);
//
//                System.out.println(g);
//
//                for (Edge edge : g.getEdges()) {
//                    increment(edge, counts2);
//                }
//
//            }
//
//            List<Edge> edges2 = new ArrayList<Edge>(counts2.keySet());
//
//            Collections.sort(edges2, new Comparator<Edge>() {
//                @Override
//                public int compare(Edge o1, Edge o2) {
//                    return counts2.get(o2).compareTo(counts2.get(o1));
//                }
//            });
//
//            for (Edge edge : edges2) {
//                System.out.println(edge + " " + counts2.get(edge) + "; " + edge.reverse() + " " + counts2.get(edge.reverse()));
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void test8() {
        try {
            List<DataSet> dataSets = loadRussData2();
            List<Graph> patterns = new ArrayList<Graph>();

            List<List<Graph>> orientations = new ArrayList<List<Graph>>();

            Images images = new Images(dataSets);
            Graph images1 = images.search();
            patterns.add(images1);
            List<Node> nodes = images1.getNodes();
            orientations.add(getNonGaussianOrientations(images1, dataSets));

            for (double d : new double[]{0.05, 0.01, 0.001, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7}) {
                IndTestFisherZPercentIndependent test = new IndTestFisherZPercentIndependent(dataSets, d);
                test.setPercent(.75);
                Pc pc = new Pc(test);
                Graph pcFDR75 = pc.search();
                patterns.add(pcFDR75);

                pcFDR75 = GraphUtils.replaceNodes(pcFDR75, nodes);

                IndTestFisherZFisherPValue test2 = new IndTestFisherZFisherPValue(dataSets, d);
                test.setPercent(.6);
                pc = new Pc(test2);
                Graph pcFisher50 = pc.search();
                patterns.add(pcFisher50);

                pcFisher50 = GraphUtils.replaceNodes(pcFisher50, nodes);

                orientations.add(getNonGaussianOrientations(pcFDR75, dataSets));
                orientations.add(getNonGaussianOrientations(pcFisher50, dataSets));

            }

            String[] patternNames = new String[]{"Images1",
                    "PC FDR 75 .05", "PC Fisher 50 .05",
                    "PC FDR 75 .01", "PC Fisher 50 .01",
                    "PC FDR 75 .001", "PC Fisher 50 .001",
                    "PC FDR 75 1e-3", "PC Fisher 50 1e-3",
                    "PC FDR 75 1e-4", "PC Fisher 50 1e-4",
                    "PC FDR 75 1e-5", "PC Fisher 50 1e-5",
                    "PC FDR 75 1e-6", "PC Fisher 50 1e-6",
                    "PC FDR 75 1e-7", "PC Fisher 50 1e-7"
            };
            String[] orientationNames = new String[]{"R3", "Skew", "RSkew", "Tanh"};

            System.out.println("\n\nComparing the patterns to one another.");
            System.out.println();

            for (int i = 0; i < patterns.size(); i++) {
                for (int j = i + 1; j < patterns.size(); j++) {

                    Graph pattern1 = patterns.get(i);
                    Graph pattern2 = patterns.get(j);

                    GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison2(pattern1, pattern2);

                    System.out.println("Adjacencies in BOTH " + patternNames[i] + " AND " + patternNames[j] + ": "
                            + comparison.getAdjCorrect());
                    System.out.println("Adjacencies in " + patternNames[i] + " but not in " + patternNames[j] + ": "
                            + comparison.getAdjFp());
                    System.out.println("Adjacencies in " + patternNames[j] + " but not in " + patternNames[i] + ": "
                            + comparison.getAdjFn());

                    System.out.println("\nArrowheads in BOTH " + patternNames[i] + " AND " + patternNames[j] + ": "
                            + comparison.getArrowptCorrect());
                    System.out.println("Arrowheads in " + patternNames[i] + " but not in " + patternNames[j] + ": "
                            + comparison.getArrowptFp());
                    System.out.println("Arrowheads in " + patternNames[j] + " but not in " + patternNames[i] + ": "
                            + comparison.getArrowptFn());
                    System.out.println("Number of adjacencies directed in both graphs: " + getNumDirectedInBoth(pattern1, pattern2));
                    System.out.println("Number of reversed directed edges: " + getNumReversed(pattern1, pattern2));
                    System.out.println();
                }
            }

            System.out.println("\n\nComparing patterns to non-Gaussian orientations of those patterns. (All of the data is used.)");
            System.out.println();

            for (int i = 0; i < orientations.size(); i++) {
                List<Graph> _orientations = orientations.get(i);

                for (int j = 0; j < _orientations.size(); j++) {
                    Graph pattern = patterns.get(i);
                    Graph orientation = _orientations.get(j);

                    GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison2(pattern, orientation);

                    System.out.println("\nArrowheads in BOTH " + patternNames[i] + " AND " + orientationNames[j] + ": "
                            + comparison.getArrowptCorrect());
                    System.out.println("Arrowheads in " + patternNames[i] + " but not in " + orientationNames[j] + ": "
                            + comparison.getArrowptFp());
                    System.out.println("Arrowheads in " + orientationNames[j] + " but not in " + patternNames[i] + ": "
                            + comparison.getArrowptFn());
                    System.out.println("Number of adjacencies directed in both graphs: " + getNumDirectedInBoth(pattern, orientation));
                    System.out.println("Number of reversed directed edges: " + getNumReversed(pattern, orientation));
                    System.out.println();

                }
            }

            System.out.println("\n\nComparing non-Gaussian orientations to one another (All of the data is used.)");
            System.out.println();

            for (int i = 0; i < orientations.size(); i++) {
                List<Graph> _orientations = orientations.get(i);

                System.out.println("================USING ADJACENCIES FROM " + patternNames[i] + "================");

                for (int j = 0; j < _orientations.size(); j++) {
                    for (int k = j + 1; k < _orientations.size(); k++) {
                        Graph graph1 = _orientations.get(k);
                        Graph graph2 = _orientations.get(j);

                        GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison2(graph1, graph2);

                        System.out.println("\nArrowheads in BOTH " + orientationNames[j] + " AND " + orientationNames[k] + ": "
                                + comparison.getArrowptCorrect());
                        System.out.println("Arrowheads in " + orientationNames[j] + " but not in " + orientationNames[k] + ": "
                                + comparison.getArrowptFp());
                        System.out.println("Arrowheads in " + orientationNames[k] + " but not in " + orientationNames[j] + ": "
                                + comparison.getArrowptFn());
                        System.out.println("Number of adjacencies directed in both graphs: " + getNumDirectedInBoth(graph1, graph2));
                        System.out.println("Number of reversed directed edges: " + getNumReversed(graph1, graph2));
                        System.out.println();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int getNumReversed(Graph pattern1, Graph pattern2) {
        int numReversed = 0;

        for (Edge edge : pattern1.getEdges()) {
            if (!Edges.isDirectedEdge(edge)) continue;

            Edge edge2 = pattern2.getEdge(edge.getNode1(), edge.getNode2());

            if (edge2 == null) continue;
            if (!Edges.isDirectedEdge(edge2)) continue;

            if (edge2.equals(edge.reverse())) {
                numReversed++;
            }
        }

        return numReversed;
    }

    private int getNumDirectedInBoth(Graph pattern1, Graph pattern2) {
        int numReversed = 0;

        for (Edge edge : pattern1.getEdges()) {
            if (!Edges.isDirectedEdge(edge)) continue;
            Edge edge2 = pattern2.getEdge(edge.getNode1(), edge.getNode2());
            if (edge2 == null) continue;
            if (!Edges.isDirectedEdge(edge2)) continue;

            numReversed++;
        }

        return numReversed;
    }

    private List<Graph> getNonGaussianOrientations(Graph graph, List<DataSet> dataSets) {
        List<Graph> graphs = new ArrayList<Graph>();

        Lofs2 lofs = new Lofs2(graph, dataSets);
        lofs.setAlpha(0.05);
        lofs.setRule(Lofs2.Rule.R3);
        Graph orient1 = lofs.orient();
        orient1 = GraphUtils.replaceNodes(orient1, graph.getNodes());
        graphs.add(orient1);

        lofs = new Lofs2(graph, dataSets);
        lofs.setRule(Lofs2.Rule.Skew);
        Graph orient2 = lofs.orient();
        orient2 = GraphUtils.replaceNodes(orient2, graph.getNodes());
        graphs.add(orient2);

        lofs = new Lofs2(graph, dataSets);
        lofs.setRule(Lofs2.Rule.RSkew);
        Graph orient3 = lofs.orient();
        orient3 = GraphUtils.replaceNodes(orient3, graph.getNodes());
        graphs.add(orient3);

        lofs = new Lofs2(graph, dataSets);
        lofs.setRule(Lofs2.Rule.Tanh);
        Graph orient4 = lofs.orient();
        orient4 = GraphUtils.replaceNodes(orient4, graph.getNodes());
        graphs.add(orient4);

        return graphs;
    }

    private void increment(Edge edge, Map<Edge, Integer> counts) {
        if (!counts.containsKey(edge)) {
            counts.put(edge, 0);
        }

        counts.put(edge, counts.get(edge) + 1);
    }

    private void writeGraph(File file, Graph graph) {
        try {
            PrintWriter out6 = new PrintWriter(file);
            out6.println(graph);
            out6.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void testCalculation() {
//        double xz = 0.5;
//        double xw = 0.5;
//        double yz = 0.5;
//        double yw = 0.5;

        Graph g = new EdgeListGraph();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");
        Node w = new GraphNode("W");
        g.addNode(x);
        g.addNode(y);
        g.addNode(z);
        g.addNode(w);
        g.addDirectedEdge(x, z);
        g.addDirectedEdge(y, z);
        g.addDirectedEdge(w, x);
        g.addDirectedEdge(w, y);
        SemPm pm = new SemPm(g);
        SemIm im = new SemIm(pm);
        StandardizedSemIm im2 = new StandardizedSemIm(im);
        double xz = im2.getEdgeCoefficient(x, z);
        double xw = im2.getEdgeCoefficient(w, x);
        double yz = im2.getEdgeCoefficient(y, z);
        double yw = im2.getEdgeCoefficient(w, y);

        System.out.println(xz);
        System.out.println(xw);
        System.out.println(yz);
        System.out.println(yw);

//        double xz = RandomUtil.getInstance().nextDouble() * 2 - 1.;
//        double xw = RandomUtil.getInstance().nextDouble() * 2 - 1.;
//        double yz = RandomUtil.getInstance().nextDouble() * 2 - 1.;
//        double yw = RandomUtil.getInstance().nextDouble() * 2 - 1.;

        double a = xw + xz * yz * yw;
        double b = xw * yw;
        double x1 = (b - xz * a) / ((sqrt(1 - xz * xz) * sqrt(1 - a * a)));
        double x2 = (b - xz * yw) / ((sqrt(1 - xz * xz) * sqrt(1 - yw * yw)));

        System.out.println(x1 + " " + x2);

        System.out.println(xz * yz);
        System.out.println(a * yw);
    }

    private List<DataSet> loadDataSets() throws IOException {
        return loadRussHeadData();
//        return loadDanaData();
//        return loadRussData2();
    }

    private List<DataSet> loadRussHeadData() throws IOException {
        String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata_for_joe/";
//        String _dir = "/home/jdramsey/proj/sanchezetal/roidata/";

        System.out.println(_dir);

        File dir = new File(_dir);
        System.out.println("dir = " + dir.getAbsolutePath());

        File[] files = dir.listFiles();

        System.out.println("File = " + files);

        List<DataSet> dataSets = new ArrayList<DataSet>();
        int numDataSets = 67;
        int index = -1;

        for (int i = 0; i < numDataSets; i++) {//files.length; i++) {
            if (++index > files.length - 1) {
                System.out.println("Oops");
                break;
            }

            System.out.println(i + 1);

            File file = files[index];
            File file2 = new File(file, "roidata");
            File[] files2 = file2.listFiles();

            if (files2 == null) {
                i--;
                continue;
            }

            if (files2[0].getName().startsWith(".")) {
                i--;
                continue;
            }

            File file3 = files2[0];

            System.out.println(file3.getAbsolutePath());


            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);
            reader.setDelimiter(DelimiterType.WHITESPACE);

            DataSet _dataSet = reader.parseTabular(file3);

//                _dataSet = DataUtils.standardizeData(_dataSet);

            dataSets.add(_dataSet);
        }
        return dataSets;
    }

    private List<DataSet> loadRussData2() throws IOException {
        String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/data/mtl_datafiles/";

        System.out.println(_dir);

        File dir = new File(_dir);
        System.out.println("dir = " + dir.getAbsolutePath());

        File[] files = dir.listFiles();

        System.out.println("File = " + files);

        List<DataSet> dataSets = new ArrayList<DataSet>();

        for (int i = 0; i < files.length; i++) {//files.length; i++) {
            if (files[i].getAbsolutePath().endsWith(".tet")) continue;

            System.out.println(files[i].getAbsolutePath());

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            reader.setDelimiter(DelimiterType.WHITESPACE);

            DataSet _dataSet = reader.parseTabular(files[i]);
            dataSets.add(_dataSet);
        }
        return dataSets;
    }

    private List<DataSet> loadRussData2b() throws IOException {
        String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/data/mtl_data2/";

        System.out.println(_dir);

        File dir = new File(_dir);
        System.out.println("dir = " + dir.getAbsolutePath());

        File[] files = dir.listFiles();

        System.out.println("File = " + files);

        List<DataSet> dataSets = new ArrayList<DataSet>();

        for (int i = 0; i < files.length; i++) {//files.length; i++) {
            if (files[i].getAbsolutePath().endsWith(".tet")) continue;

            System.out.println(files[i].getAbsolutePath());

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            reader.setDelimiter(DelimiterType.WHITESPACE);

            DataSet _dataSet = reader.parseTabular(files[i]);
            dataSets.add(_dataSet);
        }

        return dataSets;
    }


    private void convertData() {
        try {
            List<DataSet> dataSets = loadRussData2b();

            for (int i = 0; i < dataSets.size(); i++) {
                dataSets.get(i).getVariable(1).setName("Lsub");
                dataSets.get(i).getVariable(2).setName("LCA1");
                dataSets.get(i).getVariable(3).setName("LCA32DG");
                dataSets.get(i).getVariable(4).setName("Lent");
                dataSets.get(i).getVariable(5).setName("Lprc");
                dataSets.get(i).getVariable(6).setName("Lphc");
                dataSets.get(i).getVariable(7).setName("Rsub");
                dataSets.get(i).getVariable(8).setName("RCA1");
                dataSets.get(i).getVariable(9).setName("RCA32DG");
                dataSets.get(i).getVariable(10).setName("Rent");
                dataSets.get(i).getVariable(11).setName("Rprc");
                dataSets.get(i).getVariable(12).setName("Rphc");

                dataSets.get(i).removeColumn(0);
            }


            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/data/mtl_data2.out/";

            File dir = new File(_dir);
            dir.mkdirs();
            System.out.println("dir = " + dir.getAbsolutePath());

            for (int i = 0; i < dataSets.size(); i++) {
                DataSet dataSet = dataSets.get(i);
                File file = new File(_dir, dataSet.getName() + ".txt");
                PrintWriter out = new PrintWriter(file);
                NumberFormatUtil.getInstance().setNumberFormat(new DecimalFormat("0.00000000"));
                DataWriter.writeRectangularData(dataSet, out, '\t');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void countNonGaussian() {
        try {
            List<DataSet> dataSets = loadRussData2b();

            int numGaussian = 0;
            int numNongaussian = 0;
            int numDatasets = 0;
            double sumStat = 0;

            for (DataSet dataSet : dataSets) {
                numDatasets++;
                dataSet = DataUtils.standardizeData(dataSet);
                TetradMatrix data = dataSet.getDoubleData();

                for (int j = 0; j < data.columns(); j++) {
                    double[] column = data.getColumn(j).toArray();
                    double stat = new AndersonDarlingTest(column).getASquaredStar();
                    sumStat += stat;

                    // 5% cutoff for the case where mean and variance are known.
                    if (stat < 2.492) numNongaussian++;
                    else numGaussian++;
                }
            }

            System.out.println("Num data sets = " + numDatasets);
            System.out.println("Num Gaussian = " + numGaussian);
            System.out.println("Num Nongaussian = " + numNongaussian);
            System.out.println("Average a squared star = " + sumStat / dataSets.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private List<DataSet> loadDanaData() throws IOException {
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";
        File _graphDir = new File(graphDir);

        if (!_graphDir.exists()) _graphDir.mkdir();

        String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/dana/Biswal_rs_power_10sub_allrois";
//            String _dir = "/home/jdramsey/roidata/";
        File dir = new File(_dir);

        File[] files = dir.listFiles();

//        for (File file : files) {
//            System.out.println(file);
//        }

        List<DataSet> dataSets = new ArrayList<DataSet>();
        int index = -1;

        for (int i = 0; i < files.length; i++) {//files.length; i++) {
            if (++index > files.length - 1) {
                System.out.println("Oops");
                break;
            }

            File file = files[i];

            System.out.println(file.getAbsolutePath());

            if (file.getName().startsWith(".")) continue;
            if (file.getName().endsWith(".tar")) continue;

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            reader.setDelimiter(DelimiterType.WHITESPACE);

            DataSet _dataSet = reader.parseTabular(file);
            dataSets.add(_dataSet);
        }

        return dataSets;
    }

    private void convertToAdj() {
        File dir = new File(graphDir + "results7/");

        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.getAbsolutePath().endsWith(".adj.txt")) {
                continue;
            }

            if (!file.getAbsolutePath().endsWith(".txt")) {
                continue;
            }

            Graph graph = GraphUtils.loadGraphTxt(file);
            Graph undir = GraphUtils.undirectedGraph(graph);

            try {
                File adjText = new File(file.getAbsoluteFile() + ".adj.txt");
                PrintWriter out2 = new PrintWriter(adjText);
                writePatternAsMatrix(graph.getNodes(), undir, out2);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void writePatternAsMatrix(List<Node> nodes, Graph pattern, PrintWriter out2) {
        pattern = GraphUtils.replaceNodes(pattern, nodes);

        for (int p = 0; p < nodes.size(); p++) {
            for (int q = 0; q < nodes.size(); q++) {

                Node n1 = nodes.get(p);
                Node n2 = nodes.get(q);

                Edge edge = pattern.getEdge(n1, n2);

                if (edge == null) {
                    out2.print(0);
                } else if (Edges.isDirectedEdge(edge)) {
                    if (edge.pointsTowards(n2)) {
                        out2.print(-1);
                    } else {
                        out2.print(0);
                    }
                } else if (Edges.isUndirectedEdge(edge)) {
                    out2.print(1);
                } else if (Edges.isBidirectedEdge(edge)) {
                    out2.print(-1);
                } else {
                    out2.print(0);
                }

                if (q < nodes.size() - 1) out2.print(",");
            }

            out2.println();
        }

        out2.flush();
    }

    public void testConcatRuss() {
        try {
            List<DataSet> dataSets = loadDanaData();

            DataSet dataSet = DataUtils.concatenateData(dataSets);

            System.out.println(dataSet.getNumRows() + " " + dataSet.getNumColumns());

            IndTestFisherZPercentIndependent test = new IndTestFisherZPercentIndependent(dataSets, .001);
//            test.setFdr(fdr);

//            IndependenceTest indep = new IndTestFisherZ(dataSet, 0.001);
            FasICov2 fas = new FasICov2(test);

            Fci fci = new Fci(test);
            fci.setRFCI_Used(true);

            Graph graph = fci.search(fas, test.getVariables());
//
            System.out.println(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        new ExploreSanchezGlymourRamsey().printCrossValidationsPc();
//        new ExploreSanchezGlymourRamsey().test1();
//        new ExploreSanchezGlymourRamsey().convertToAdj();
//        new ExploreSanchezGlymourRamsey().testPcVersions();
//        new ExploreSanchezGlymourRamsey().test7a();
//        new ExploreSanchezGlymourRamsey().test8();
//        new ExploreSanchezGlymourRamsey().testCalculation();
//        new ExploreSanchezGlymourRamsey().convertData();
        new ExploreSanchezGlymourRamsey().testConcatRuss();
//        new ExploreSanchezGlymourRamsey().countNonGaussian();
    }

}
