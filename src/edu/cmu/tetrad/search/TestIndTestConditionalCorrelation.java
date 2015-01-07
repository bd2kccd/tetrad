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
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradMatrix;
import edu.cmu.tetradapp.workbench.PngWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;


/**
 * Tests the IndTestTimeSeries class.
 *
 * @author Joseph Ramsey
 */
public class TestIndTestConditionalCorrelation extends TestCase {
    public TestIndTestConditionalCorrelation(String name) {
        super(name);
    }

    public void test1() {
        int total = 20;

        for (int N : new int[]{100, 250, 400, 550, 700}) {
            System.out.println("\n\n========SAMPLE SIZE " + N + "\n\n");

            int numEqualKci = 0;
            int numEqualCci = 0;

            for (int i = 0; i < total; i++) {
                System.out.println("Round " + (i + 1));
                GeneralizedSemIm im = makeTestIm1();
//            SemIm im = makeTestIm2();

                DataSet data = im.simulateData(N, false);
//                IndependenceTest test = new IndTestKciMatlab(data, .01);
                IndependenceTest test = new IndTestFisherZ(data, .1);
                Pc pc = new Pc(test);
                Graph graph = pc.search();

                SemGraph _graph = im.getSemPm().getGraph();
                _graph.setShowErrorTerms(false);

                Graph truePattern = SearchGraphUtils.patternForDag(_graph);

//                System.out.println(truePattern);
//                System.out.println(graph);

                boolean equalsKci = graph.equals(truePattern);
                System.out.println("KCI equals " + equalsKci);

                if (equalsKci) {
                    numEqualKci++;
                }

                IndependenceTest test2 = new IndTestConditionalCorrelation(data, .05);
                Pc pc2 = new Pc(test2);
                Graph graph3 = pc2.search();

//                System.out.println(graph3);
//                System.out.println(truePattern);

                boolean equalsCci = graph3.equals(truePattern);
                System.out.println("CCI equals " + equalsCci);

                if (equalsCci) {
                    numEqualCci++;
                }
            }

            double ratioKci = numEqualKci / (double) total;
            double ratioCci = numEqualCci / (double) total;
            NumberFormat nf = new DecimalFormat("0.00");
            System.out.println("Num equal KCI " + numEqualKci + " out of " + total + " = " + nf.format(ratioKci));
            System.out.println("Num equal CCI " + numEqualCci + " out of " + total + " = " + nf.format(ratioCci));
        }
    }

    private GeneralizedSemIm makeTestIm1() {
        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");

        List<Node> nodes = new ArrayList<Node>();

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);
        nodes.add(x4);

        Graph graph = new EdgeListGraph();

        for (Node node : nodes) {
            graph.addNode(node);
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (RandomUtil.getInstance().nextDouble() > 0.5) {
                    graph.addDirectedEdge(nodes.get(i), nodes.get(j));
                }
            }
        }

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);
        List<Node> variablesNodes = pm.getVariableNodes();
        List<Node> errorNodes = pm.getErrorNodes();

        try {
            for (Node node : variablesNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate("TSUM(NEW(a) * $)", pm, node);
                pm.setNodeExpression(node, _template);
            }

            for (Node node : errorNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate("N(0,NEW(d))", pm, node);
                pm.setNodeExpression(node, _template);
            }

            Set<String> parameters = pm.getParameters();

            for (String parameter : parameters) {
                if (parameter.startsWith("a")) {
                    pm.setParameterExpression(parameter, "U(-2,2)");
//                    pm.setParameterExpression(parameter, "Split(-1.5,-.5,.5,1.5)");
                } else if (parameter.startsWith("d")) {
                    pm.setParameterExpression(parameter, "U(.1,.6)");
//                    pm.setParameterExpression(parameter, "U(.6,1.5)");
                }
            }
        } catch (ParseException e) {
            System.out.println(e);
        }

        GeneralizedSemIm im = new GeneralizedSemIm(pm);

        return im;
    }

    private SemIm makeTestIm2() {
        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");

        List<Node> nodes = new ArrayList<Node>();

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);
        nodes.add(x4);

        Graph graph = new EdgeListGraph();

        for (Node node : nodes) {
            graph.addNode(node);
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (RandomUtil.getInstance().nextDouble() > 0.5) {
                    graph.addDirectedEdge(nodes.get(i), nodes.get(j));
                }
            }
        }

        SemPm pm = new SemPm(graph);

        SemImInitializationParams params = new SemImInitializationParams();
        params.setCoefRange(0., 2.0);
        params.setCoefSymmetric(true);
        params.setVarRange(.1, .6);

        return new SemIm(pm, params);
    }

    public void test2() {
        int numSets = 20;

        String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.09.03/pc_matlab/KCI-test-3/testdata3/";
        File dir = new File(_dir);

        if (!dir.exists()) dir.mkdir();

        for (int sampleSize : new int[]{100, 250, 400, 550, 700}) {
            for (int index = 0; index < numSets; index++) {
                SemIm im = makeTestIm2();

                DataSet data = im.simulateData(sampleSize, false);

                File file = new File(dir, "data." + sampleSize + "." + index + ".txt");

                try {
                    saveData(data, file);

                    // Need to save out the graphs too, in Matlab format. Man this is going ot be slow.

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    // Generate some canonical data sets for testing cross-platform.
    public void test3() {
//        if (true) {
//            throw new IllegalArgumentException("Don't overwrite output unless you reall mean it! Change output directory!");
//        }

        int numEqual = 0;
        int total = 20;

        for (int N : new int[]{100, 250, 400, 550, 700}) {
            for (int i = 0; i < total; i++) {
                System.out.println("Round " + (i + 1));
//            GeneralizedSemIm im = makeTestIm1();
                SemIm im = makeTestIm2();

                try {
                    DataSet data = im.simulateData(N, false);

                    String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.09.03/pc_matlab/KCI-test-3/testdata3/";
//                    String dir = "/home/jdramsey/ccitest2/KCI-test-3/testdata/";
                    File file = new File(dir + "data." + N + "." + (i + 1) + ".txt");
                    PrintWriter out = new PrintWriter(file);

                    DataWriter.writeRectangularData(data, out, '\t');

                    File file2 = new File(dir + "graph." + N + "." + (i + 1) + ".txt");
                    File file3 = new File(dir + "graph.tetrad" + N + "." + (i + 1) + ".txt");
                    File file6 = new File(dir + "dag.tetrad" + N + "." + (i + 1) + ".txt");

                    Graph dag = im.getSemPm().getGraph();
                    dag = GraphUtils.replaceNodes(dag, data.getVariables());
                    Graph pattern = SearchGraphUtils.patternForDag(dag);

                    PrintWriter out2 = new PrintWriter(file2);
                    PrintWriter out3 = new PrintWriter(file3);
                    PrintWriter out6 = new PrintWriter(file6);

                    writePatternAsMatrix(data.getVariables(), pattern, out2);

                    out3.println(pattern.toString());
                    out6.println(dag.toString());

                    IndTestKciMatlab test = new IndTestKciMatlab(data, .05);
//            IndependenceTest test = new IndTestFisherZ(data, .1);
                    Pc pc = new Pc(test);
                    Graph pattern2 = pc.search();

                    File file4 = new File(dir + "cci.graph." + N + "." + (i + 1) + ".txt");
                    File file5 = new File(dir + "cci.graph.tetrad" + N + "." + (i + 1) + ".txt");

                    PrintWriter out4 = new PrintWriter(file4);
                    PrintWriter out5 = new PrintWriter(file5);

                    writePatternAsMatrix(data.getVariables(), pattern2, out4);
                    out5.println(pattern2.toString());

                    out.close();
                    out2.close();
                    out3.close();
                    out4.close();
                    out5.close();
                    out6.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        double ratio = numEqual / (double) total;
        NumberFormat nf = new DecimalFormat("0.00");
        System.out.println("Num equal " + numEqual + " out of " + total + " = " + nf.format(ratio));
    }

    // Just to see how fast it runs through all of the tests.
    public void test4() {
        int numEqual = 0;
        int total = 20;

        long start = System.currentTimeMillis();

        long elapsed = 0l;

//        for (int N : new int[]{2000}) {
        for (int N : new int[]{100, 250, 400, 550, 700}) {
            for (int i = 0; i < total; i++) {
                System.out.println("Round " + (i + 1));
//            GeneralizedSemIm im = makeTestIm1();
                SemIm im = makeTestIm2();

                DataSet data = im.simulateData(N, false);

                IndTestConditionalCorrelation test = new IndTestConditionalCorrelation(data, .01);
                PcStable pc = new PcStable(test);

                long start0 = System.currentTimeMillis();
                Graph pattern2 = pc.search();
                long stop0 = System.currentTimeMillis();

                elapsed += stop0 - start0;

                System.out.println(pattern2);
            }
        }

        long stop = System.currentTimeMillis();

        System.out.println((stop - start) + " ms");

        System.out.println("Elapsed = " + elapsed + " ms");

    }

    public void test5() {
        // Read in a specific file and watch specific variable values to compare with the Matlab version.
        try {
//            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.09.03/pc_matlab/KCI-test-3/testdata/";
//            String dir = "/home/jdramsey/ccitest2/KCI-test-3/testdata/";
            String dir = "/home/jdramsey/ccitest2/";
            int N = 1000;

            for (int index = 1; index <= 20; index++) {
//                int index = 6;

                System.out.println("\n\n======INDEX " + index);

                File file = new File(dir + "data." + N + "." + index + ".txt");
                File file2 = new File(dir + "graph." + N + "." + index + ".txt");

                if (!file.exists()) {
                    throw new IllegalArgumentException(file.getAbsolutePath());
                }

                DataReader reader = new DataReader();
                DataSet dataSet = reader.parseTabular(file);

                long start = System.currentTimeMillis();

                IndTestConditionalCorrelation test = new IndTestConditionalCorrelation(dataSet, .0001);
//            IndependenceTest test = new IndTestFisherZ(data, .1);

                List<Node> var = test.getVariables();
                Node x = var.get(0);
                Node y = var.get(1);
                Node z = var.get(2);
                test.isIndependent(x, y, Collections.singletonList(z));


                Pc pc = new Pc(test);
                Graph pattern2 = pc.search();

                long stop = System.currentTimeMillis();

                System.out.println((stop - start) + " ms");

                System.out.println(pattern2);

                writePatternAsMatrix(dataSet.getVariables(), pattern2, System.out);

                System.out.println();
                System.out.println("Truth");

                BufferedReader buf = new BufferedReader(new FileReader(file2));
                String line;
                while ((line = buf.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public void test6() {
        double[] x = new double[]{.5, .6, .7, .8, .9, 1.0};

        for (int i = 0; i < x.length; i++) {
            System.out.println(sin(2 * x[i]));
        }

    }

    public void test7() {
        System.out.println(pow(-1, -1));
    }

    public void test8() {
        int NTHREDS = 100;
        long start = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        for (int i = 0; i < 5000; i++) {
            Runnable worker = new MyRunnable(10000000L + i);
            executor.execute(worker);
        }
        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
//        executor.shutdown();
        try {
            // Wait until all threads are finish
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Finished all threads");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long stop = System.currentTimeMillis();

        System.out.println((stop - start) + " ms");
    }

    public void test9() {
        GeneralizedSemIm im = makeTestIm3(20);
        SemGraph trueGraph = im.getGeneralizedSemPm().getGraph();
        trueGraph.setShowErrorTerms(false);

        DataSet data = im.simulateData(2000, false);

//        Pc pc = new Pc(new IndTestFisherZ(data, 0.05));
//        PcStable pc = new PcStable(new IndTestFisherZ(data, 0.05));
//        PcStable pc = new PcStable(new IndTestConditionalCorrelation(data, 0.05));
        PcStable pc = new PcStable(new IndTestConditionalCorrelation(data, .1));
        pc.setDepth(2);

        Graph graph = pc.search();

        System.out.println(graph);
        graph = GraphUtils.replaceNodes(graph, trueGraph.getNodes());

        int fp = GraphUtils.adjacenciesComplement(graph, trueGraph).size();
        int fn = GraphUtils.adjacenciesComplement(trueGraph, graph).size();

        System.out.println("fp = " + fp);
        System.out.println("fn = " + fn);
    }


    private class MyRunnable implements Runnable {
        private final long countUntil;

        MyRunnable(long countUntil) {
            this.countUntil = countUntil;
        }

        @Override
        public void run() {
            long sum = 0;
            for (long i = 1; i < countUntil; i++) {
                sum += i;
            }
            System.out.println(sum);
        }
    }

    private void writePatternAsMatrix(List<Node> nodes, Graph pattern, PrintStream out2) {
        GraphUtils.replaceNodes(pattern, nodes);

        for (int p = 0; p < 4; p++) {
            for (int q = 0; q < 4; q++) {

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
                    out2.print(1);
                } else {
                    out2.print(0);
                }

                if (q < 4 - 1) out2.print(",");
            }

            out2.println();
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

    private void writeAdjacencyMatrix(List<Node> nodes, Graph graph, PrintWriter out2) {
//        out2.println(nodes);

        graph = GraphUtils.replaceNodes(graph, nodes);

        for (int p = 0; p < nodes.size(); p++) {
            for (int q = 0; q < nodes.size(); q++) {

                Node n1 = nodes.get(p);
                Node n2 = nodes.get(q);

                Edge edge = graph.getEdge(n1, n2);

                out2.print(edge != null ? 1 : 0);

                if (q < nodes.size() - 1) out2.print(",");
            }

            out2.println();
        }

        out2.flush();
    }

    private Graph readPatternAsMatrix(List<Node> nodes, DataSet dataSet) {
//        List<Node> nodes = dataSet.getVariables();

        Graph pattern = new EdgeListGraph(nodes);

        for (int p = 0; p < nodes.size(); p++) {
            for (int q = 0; q < nodes.size(); q++) {
                if (p == q) continue;

                Node n1 = nodes.get(p);
                Node n2 = nodes.get(q);

                int i1 = (int) round(dataSet.getDouble(p, q));
                int i2 = (int) round(dataSet.getDouble(q, p));

//                System.out.println(i1 + " " + i2);

                if (i1 == -1 && i2 == 0) {
                    pattern.addDirectedEdge(n1, n2);
                } else if (i1 == -1 && i2 == -1 && !pattern.isAdjacentTo(n1, n2)) {
                    pattern.addBidirectedEdge(n2, n1);
                } else if (i1 == 1 && i2 == 1 && !pattern.isAdjacentTo(n1, n2)) {
                    pattern.addUndirectedEdge(n1, n2);
                }
            }
        }

        return pattern;
    }

    /**
     * Saves data in the selected data set to a file.
     */
    private void saveData(DataSet dataSet, File file) throws IOException {
        PrintWriter out;

        try {
            out = new PrintWriter(new FileOutputStream(file));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Output file could not be opened: " + file);
        }

        DataWriter.writeRectangularData(dataSet, out, '\t');

        out.close();
    }


    private GeneralizedSemIm makeTestIm3(int size) {
        Graph graph = GraphUtils.randomDag(size, size, false);

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);
        List<Node> variablesNodes = pm.getVariableNodes();
        List<Node> errorNodes = pm.getErrorNodes();

        try {
            for (Node node : variablesNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate("TSUM(NEW(a) * $)", pm, node);
                pm.setNodeExpression(node, _template);
            }

            for (Node node : errorNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate("U(-1,1)", pm, node);
                pm.setNodeExpression(node, _template);
            }

            Set<String> parameters = pm.getParameters();

            for (String parameter : parameters) {
                if (parameter.startsWith("a")) {
                    pm.setParameterExpression(parameter, "U(-1,1)");
                }
            }
        } catch (ParseException e) {
            System.out.println(e);
        }

        GeneralizedSemIm im = new GeneralizedSemIm(pm);

        return im;
    }

    public void test10() {

        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test10out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

            int numVariables = 20;
            int numRuns = 20;
            NumberFormat nf = new DecimalFormat("0.00");

            for (int modelIndex = 1; modelIndex <= 14; modelIndex++) {
                double sumAP1 = 0.0;
                double sumAR1 = 0.0;
                double sumAP2 = 0.0;
                double sumAR2 = 0.0;

                int sumAP1N = 0;
                int sumAR1N = 0;
                int sumAP2N = 0;
                int sumAR2N = 0;


                for (int r = 0; r < numRuns; r++) {

                    GeneralizedSemIm im = makeTestIm4(numVariables, modelIndex);

                    out.println(im);

                    SemGraph gTrue = im.getGeneralizedSemPm().getGraph();
                    gTrue.setShowErrorTerms(false);

                    DataSet data = im.simulateData(1000, false);

                    PcStable pc = new PcStable(new IndTestConditionalCorrelation(data, .05));
                    Graph graph = pc.search();

                    // Goes to report.
                    out.println(graph);

                    graph = GraphUtils.replaceNodes(graph, gTrue.getNodes());

                    int adjFn = adjacenciesComplement(gTrue, graph);
                    int adjFp = adjacenciesComplement(graph, gTrue);
                    int truePosAdj = truePositivesAdj(gTrue, graph);

                    double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
                    double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);

                    if (!Double.isNaN(adjPrecision)) {
                        sumAP1 += adjPrecision;
                        sumAP1N++;
                    }

                    if (!Double.isNaN(adjRecall)) {
                        sumAR1 += adjRecall;
                        sumAR1N++;
                    }

                    out.println("Model # " + modelIndex + " AP (CCI) = " + adjPrecision);
                    out.println("Model # " + modelIndex + " AR (CCI) = " + adjRecall);

                    Pc pc2 = new Pc(new IndTestFisherZ(data, 0.01));
                    Graph graph2 = pc2.search();

                    // Should go to the report.
                    out.println(graph2);

                    graph2 = GraphUtils.replaceNodes(graph2, gTrue.getNodes());

                    int adjFn2 = adjacenciesComplement(gTrue, graph2);
                    int adjFp2 = adjacenciesComplement(graph2, gTrue);
                    int truePosAdj2 = truePositivesAdj(gTrue, graph2);

                    double adjPrecision2 = truePosAdj2 / (double) (truePosAdj2 + adjFp2);
                    double adjRecall2 = truePosAdj2 / (double) (truePosAdj2 + adjFn2);

                    if (!Double.isNaN(adjPrecision2)) {
                        sumAP2 += adjPrecision2;
                        sumAP2N++;
                    }

                    if (!Double.isNaN(adjRecall2)) {
                        sumAR2 += adjRecall2;
                        sumAR2N++;
                    }

                    out.println("Model # " + modelIndex + " AP (Fisher Z) = " + adjPrecision2);
                    out.println("Model # " + modelIndex + " AR (Fisher Z) = " + adjRecall2);
                }

                out.println("Model # " + modelIndex + " Average AP (CCI) = " + nf.format(sumAP1 / sumAP1N));
                out.println("Model # " + modelIndex + " Average AR (CCI) = " + nf.format(sumAR1 / sumAR1N));
                out.println("Model # " + modelIndex + " Average AP (Fisher Z) = " + nf.format(sumAP2 / sumAP2N));
                out.println("Model # " + modelIndex + " Average AR (Fisher Z) = " + nf.format(sumAR2 / sumAR2N));
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test11() {

        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

            long start = System.currentTimeMillis();

            int model = 6;
            int numVariables = 20;
            int N = 1000;
            int numRuns = 1;

            NumberFormat nf = new DecimalFormat("0.00");

            for (int modelIndex = 1; modelIndex <= 14; modelIndex++) {
                double sumAP1 = 0.0;
                double sumAR1 = 0.0;
                double sumAP2 = 0.0;
                double sumAR2 = 0.0;

                double sumEP1 = 0.0;
                double sumER1 = 0.0;
                double sumEP2 = 0.0;
                double sumER2 = 0.0;

                int sumAP1N = 0;
                int sumAR1N = 0;
                int sumAP2N = 0;
                int sumAR2N = 0;

                int sumEP1N = 0;
                int sumER1N = 0;
                int sumEP2N = 0;
                int sumER2N = 0;

                for (int r = 0; r < numRuns; r++) {

                    GeneralizedSemIm im = makeTestIm4(numVariables, modelIndex);

                    out.println(im);

                    SemGraph gTrue = im.getGeneralizedSemPm().getGraph();
                    gTrue.setShowErrorTerms(false);
                    Graph truePattern = SearchGraphUtils.patternForDag(gTrue);

                    DataSet data = im.simulateData(N, false);

                    long start2 = System.currentTimeMillis();
                    PcStable pc = new PcStable(new IndTestConditionalCorrelation(data, .05));
                    Graph graph = pc.search();
                    long stop2 = System.currentTimeMillis();
                    System.out.println("Elapsed (just CCI) " + (stop2 - start2) / 1000L + " seconds");

                    // Goes to report.
                    out.println(graph);

                    graph = GraphUtils.replaceNodes(graph, truePattern.getNodes());

                    int adjFn = adjacenciesComplement(truePattern, graph);
                    int adjFp = adjacenciesComplement(graph, truePattern);
                    int truePosAdj = truePositivesAdj(truePattern, graph);

                    int edgeFn = edgesComplement(truePattern, graph);
                    int edgeFp = edgesComplement(graph, truePattern);
                    int truePosEdges = truePositiveEdges(truePattern, graph);

                    double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
                    double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);

                    double edgePrecision = truePosEdges / (double) (truePosEdges + edgeFp);
                    double edgeRecall = truePosEdges / (double) (truePosEdges + edgeFn);

                    if (!Double.isNaN(adjPrecision)) {
                        sumAP1 += adjPrecision;
                        sumAP1N++;
                    }

                    if (!Double.isNaN(adjRecall)) {
                        sumAR1 += adjRecall;
                        sumAR1N++;
                    }

                    if (!Double.isNaN(edgePrecision)) {
                        sumEP1 += edgePrecision;
                        sumEP1N++;
                    }

                    if (!Double.isNaN(edgeRecall)) {
                        sumER1 += edgeRecall;
                        sumER1N++;
                    }

                    out.println("Model # " + modelIndex + " AP (CCI) = " + adjPrecision);
                    out.println("Model # " + modelIndex + " AR (CCI) = " + adjRecall);

                    PcStable pc2 = new PcStable(new IndTestFisherZ(data, 0.05));
                    Graph graph2 = pc2.search();

                    // Should go to the report.
                    out.println(graph2);

                    graph2 = GraphUtils.replaceNodes(graph2, truePattern.getNodes());

                    int adjFn2 = adjacenciesComplement(truePattern, graph2);
                    int adjFp2 = adjacenciesComplement(graph2, truePattern);
                    int truePosAdj2 = truePositivesAdj(truePattern, graph2);

                    int edgeFn2 = edgesComplement(truePattern, graph2);
                    int edgeFp2 = edgesComplement(graph2, truePattern);
                    int truePosEdges2 = truePositiveEdges(truePattern, graph2);

                    double adjPrecision2 = truePosAdj2 / (double) (truePosAdj2 + adjFp2);
                    double adjRecall2 = truePosAdj2 / (double) (truePosAdj2 + adjFn2);

                    double edgePrecision2 = truePosEdges2 / (double) (truePosEdges2 + edgeFp2);
                    double edgeRecall2 = truePosEdges2 / (double) (truePosEdges2 + edgeFn2);

                    if (!Double.isNaN(adjPrecision2)) {
                        sumAP2 += adjPrecision2;
                        sumAP2N++;
                    }

                    if (!Double.isNaN(adjRecall2)) {
                        sumAR2 += adjRecall2;
                        sumAR2N++;
                    }

                    if (!Double.isNaN(edgePrecision2)) {
                        sumEP2 += edgePrecision2;
                        sumEP2N++;
                    }

                    if (!Double.isNaN(edgeRecall2)) {
                        sumER2 += edgeRecall2;
                        sumER2N++;
                    }

                    out.println("Model # " + modelIndex + " AP (Fisher Z) = " + adjPrecision2);
                    out.println("Model # " + modelIndex + " AR (Fisher Z) = " + adjRecall2);
                }

                out.println("\nAverages");
                out.println("Model # " + modelIndex + " Average AP (CCI) = " + nf.format(sumAP1 / sumAP1N));
                out.println("Model # " + modelIndex + " Average AR (CCI) = " + nf.format(sumAR1 / sumAR1N));
                out.println("Model # " + modelIndex + " Average EP (CCI) = " + nf.format(sumEP1 / sumEP1N));
                out.println("Model # " + modelIndex + " Average ER (CCI) = " + nf.format(sumER1 / sumER1N));

                out.println("Model # " + modelIndex + " Average AP (Fisher Z) = " + nf.format(sumAP2 / sumAP2N));
                out.println("Model # " + modelIndex + " Average AR (Fisher Z) = " + nf.format(sumAR2 / sumAR2N));
                out.println("Model # " + modelIndex + " Average EP (Fisher Z) = " + nf.format(sumEP2 / sumEP2N));
                out.println("Model # " + modelIndex + " Average ER (Fisher Z) = " + nf.format(sumER2 / sumER2N));
            }

            long stop = System.currentTimeMillis();

            System.out.println("Elapsed " + (stop - start) / 1000L + " seconds");

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test12() {

        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test12final/";
//            String _dir = "/home/jdramsey/test12final/";
            File dir = new File(_dir);

            if (!dir.exists()) dir.mkdir();

            int numRuns = 20;
            double alpha = 0.01;
            double gamma = 0.01;
            int numModels = 14;

//            This should be done only once.
            for (int model = 1; model <= numModels; model++) {

                System.out.println("================= MODEL " + model + " =================");

                int numVariables = 5;
                int N = 1000;

                for (int run = 1; run <= numRuns; run++) {
                    File file = new File(dir, "data." + model + "." + run + ".txt");

                    if (file.exists()) continue;

                    GeneralizedSemIm im = makeTestIm4(numVariables, model);

                    DataSet data = im.simulateData(N, false);

                    PrintWriter out1 = new PrintWriter(file);
                    DataWriter.writeRectangularData(data, out1, '\t');

                    File file2 = new File(dir, "graph." + model + "." + run + ".txt");
                    File file3 = new File(dir, "graph.tetrad." + model + "." + run + ".txt");
                    File file6 = new File(dir, "dag.tetrad." + model + "." + run + ".txt");
                    File file7 = new File(dir, "model." + model + "." + run + ".txt");

                    SemGraph dag = im.getSemPm().getGraph();
                    dag.setShowErrorTerms(false);
                    Graph _dag = GraphUtils.replaceNodes(dag, data.getVariables());
                    Graph truePattern = SearchGraphUtils.patternForDag(_dag);

                    PrintWriter out2 = new PrintWriter(file2);
                    PrintWriter out3 = new PrintWriter(file3);
                    PrintWriter out6 = new PrintWriter(file6);
                    PrintWriter out7 = new PrintWriter(file7);

                    writePatternAsMatrix(data.getVariables(), truePattern, out2);

                    out3.println(truePattern.toString());
                    out6.println(dag.toString());

                    out7.println(im);

                    out1.close();
                    out2.close();
                    out3.close();
                    out6.close();
                    out7.close();
                }
            }

            double[][] stats = new double[14][8];


            for (int model = 1; model <= 14; model++) {
                System.out.println("MODEL " + model);

                NumberFormat nf = new DecimalFormat("0.00");

                String[] indTestTypes = new String[]{"fisherz", "drton", "kci", "cci"};
//                String[] indTestTypes = new String[]{"fisherz", "drton"};

//                String indTestType = "fisherz";
//                String indTestType = "cci";
//                    String indTestType = "kci";
//                    String indTestType = "drton";
                for (int type = 0; type < indTestTypes.length; type++) {
//                for (String indTestType : indTestTypes) {
                    String indTestType = indTestTypes[type];
                    double sumAP = 0.0;
                    double sumAR = 0.0;

                    double sumEP = 0.0;
                    double sumER = 0.0;

                    int sumErrors = 0;

                    int sumAPN = 0;
                    int sumARN = 0;

                    int sumEPN = 0;
                    int sumERN = 0;

                    for (int run = 1; run <= numRuns; run++) {
                        System.out.println("\nRun " + run);

                        File file4 = new File(dir, "pattern." + indTestType + "." + model + "." + run + ".txt");
                        Graph pattern;

                        File file3 = new File(dir, "dag.tetrad." + model + "." + run + ".txt");
                        Graph truePattern = GraphUtils.loadGraphTxt(file3);

                        if (!file4.exists()) {
                            File file = new File(dir, "data." + model + "." + run + ".txt");

                            DataReader reader = new DataReader();
                            reader.setVariablesSupplied(true);
                            reader.setDelimiter(DelimiterType.WHITESPACE);

                            DataSet dataSet = reader.parseTabular(file);
//                            long start2 = System.currentTimeMillis();

                            double cutoff = indTestType.equals("drton") ? gamma : alpha;

                            Pc pc = new Pc(getIndependenceTest(indTestType, dataSet, cutoff));
                            pattern = pc.search();

                            Nlo nlo = new Nlo(dataSet, alpha);
                            pattern = nlo.fullOrient4(pattern);
//                            pattern = GraphUtils.bidirectedToUndirected(pattern);
//                            long stop2 = System.currentTimeMillis();
//                            System.out.println("Elapsed (just " + indTestType + ") " + (stop2 - start2) / 1000L + " seconds");

                            PrintWriter out4 = new PrintWriter(file4);
                            out4.println(pattern);
                            out4.close();

                        } else {
                            pattern = GraphUtils.loadGraphTxt(file4);
                        }

                        System.out.println("True pattern = " + truePattern);
                        System.out.println("Pattern = " + pattern);

                        pattern = GraphUtils.replaceNodes(pattern, truePattern.getNodes());
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);

                        int adjFn = adjacenciesComplement(truePattern, pattern);
                        int adjFp = adjacenciesComplement(pattern, truePattern);
                        int truePosAdj = truePositivesAdj(truePattern, pattern);

                        System.out.println("AdjFn = " + adjFn);
                        System.out.println("AdjFp = " + adjFp);
                        System.out.println("TruePosAdj = " + truePosAdj);

//                        int edgeFn = edgesComplement(truePattern, pattern);
//                        int edgeFp = edgesComplement(pattern, truePattern);
//                        int truePosEdges = truePositiveEdges(truePattern, pattern);

                        int edgeFn = arrowsComplement(truePattern, pattern);
                        int edgeFp = arrowsComplement(pattern, truePattern);
                        int truePosEdges = truePositiveArrows(truePattern, pattern);
//
                        double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
                        double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);


                        double edgePrecision = truePosEdges / (double) (truePosEdges + edgeFp);
                        double edgeRecall = truePosEdges / (double) (truePosEdges + edgeFn);

                        System.out.println("edge Precision = " + edgePrecision);
                        System.out.println("edge Recall = " + edgeRecall);

                        sumErrors += adjFn + adjFp;

                        if (!Double.isNaN(adjPrecision)) {
                            sumAP += adjPrecision;
                            sumAPN++;
                        }

                        if (!Double.isNaN(adjRecall)) {
                            sumAR += adjRecall;
                            sumARN++;
                        }

                        if (!Double.isNaN(edgePrecision)) {
                            sumEP += edgePrecision;
                            sumEPN++;
                        }

                        if (!Double.isNaN(edgeRecall)) {
                            sumER += edgeRecall;
                            sumERN++;
                        }

//                    out.println("Model # " + modelIndex + " AP (CCI) = " + adjPrecision);
//                    out.println("Model # " + modelIndex + " AR (CCI) = " + adjRecall);

                    }

                    out.println("\nAverages " + indTestType);
                    out.println("Model # " + model + " Average AP = " + nf.format(sumAP / sumAPN));
                    out.println("Model # " + model + " Average AR = " + nf.format(sumAR / sumARN));
                    out.println("Model # " + model + " Average EP = " + nf.format(sumEP / sumEPN));
                    out.println("Model # " + model + " Average ER = " + nf.format(sumER / sumERN));
                    out.println("Model # " + model + " Average Adj Errors = " + nf.format(sumErrors / (double) numModels));

                    stats[model - 1][type * 2] = sumEP / sumEPN;
                    stats[model - 1][type * 2 + 1] = sumER / sumERN;

                }
            }

            System.out.println(MatrixUtils.toString(stats));

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test12_2() {

        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

//            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test12_2/";
            String _dir = "/home/jdramsey/test12-2final/";
            File dir = new File(_dir);

            if (!dir.exists()) dir.mkdir();

            int numRuns = 100;
            double alpha = 0.01;
            double gamma = 0.01;
            int numModels = 14;
            double[][] stats = new double[5][8];

//            This should be done only once.
            for (int sampleSize : new int[]{100, 250, 400, 550, 700}) {

                System.out.println("================= Saple size " + sampleSize + " =================");

                int numVariables = 5;
                int N = sampleSize;

                for (int run = 1; run <= numRuns; run++) {
                    File file = new File(dir, "data." + sampleSize + "." + run + ".txt");

                    if (file.exists()) continue;

                    GeneralizedSemIm im = makeTestIm1();

                    DataSet data = im.simulateData(N, false);

                    PrintWriter out1 = new PrintWriter(file);
                    DataWriter.writeRectangularData(data, out1, '\t');

                    File file2 = new File(dir, "graph." + sampleSize + "." + run + ".txt");
                    File file3 = new File(dir, "graph.tetrad." + sampleSize + "." + run + ".txt");
                    File file6 = new File(dir, "dag.tetrad." + sampleSize + "." + run + ".txt");
                    File file7 = new File(dir, "model." + sampleSize + "." + run + ".txt");

                    SemGraph dag = im.getSemPm().getGraph();
                    dag.setShowErrorTerms(false);
                    Graph _dag = GraphUtils.replaceNodes(dag, data.getVariables());
                    Graph truePattern = SearchGraphUtils.patternForDag(_dag);

                    PrintWriter out2 = new PrintWriter(file2);
                    PrintWriter out3 = new PrintWriter(file3);
                    PrintWriter out6 = new PrintWriter(file6);
                    PrintWriter out7 = new PrintWriter(file7);

                    writePatternAsMatrix(data.getVariables(), truePattern, out2);

                    out3.println(truePattern.toString());
                    out6.println(dag.toString());

                    out7.println(im);

                    out1.close();
                    out2.close();
                    out3.close();
                    out6.close();
                    out7.close();
                }
            }

            int[] sizes = {100, 250, 400, 550, 700};

            for (int s = 0; s < sizes.length; s++) {
                int sampleSize = sizes[s];

                System.out.println("Sample size " + sampleSize);

                NumberFormat nf = new DecimalFormat("0.00");

                String[] indTestTypes = new String[]{"fisherz", "drton", "cci", "kci"};
//                String[] indTestTypes = new String[]{"fisherz", "drton"};

//                String indTestType = "fisherz";
//                String indTestType = "cci";
//                    String indTestType = "kci";
//                    String indTestType = "drton";
                for (int type = 0; type < indTestTypes.length; type++) {
//                for (String indTestType : indTestTypes) {
                    String indTestType = indTestTypes[type];

                    double sumAP = 0.0;
                    double sumAR = 0.0;

                    double sumEP = 0.0;
                    double sumER = 0.0;

                    int sumErrors = 0;

                    int sumAPN = 0;
                    int sumARN = 0;

                    int sumEPN = 0;
                    int sumERN = 0;

                    for (int run = 1; run <= numRuns; run++) {
                        System.out.println("\nRun " + run);

                        File file4 = new File(dir, "pattern." + indTestType + "." + sampleSize + "." + run + ".txt");
                        Graph pattern;

                        File file3 = new File(dir, "graph.tetrad." + sampleSize + "." + run + ".txt");
                        Graph truePattern = GraphUtils.loadGraphTxt(file3);

                        if (!file4.exists()) {
                            File file = new File(dir, "data." + sampleSize + "." + run + ".txt");

                            DataReader reader = new DataReader();
                            reader.setVariablesSupplied(true);
                            reader.setDelimiter(DelimiterType.WHITESPACE);

                            DataSet dataSet = reader.parseTabular(file);
//                            long start2 = System.currentTimeQMillis();

                            double cutoff = indTestType.equals("drton") ? gamma : alpha;

                            Pc pc = new Pc(getIndependenceTest(indTestType, dataSet, cutoff));
                            pattern = pc.search();
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);
//                            long stop2 = System.currentTimeMillis();
//                            System.out.println("Elapsed (just " + indTestType + ") " + (stop2 - start2) / 1000L + " seconds");

                            PrintWriter out4 = new PrintWriter(file4);
                            out4.println(pattern);
                            out4.close();

                            System.out.println("Pattern = " + pattern);
                        } else {
                            pattern = GraphUtils.loadGraphTxt(file4);
//                            pattern = GraphUtils.bidirectedToUndirected(pattern);
                        }

                        pattern = GraphUtils.replaceNodes(pattern, truePattern.getNodes());
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);

                        int adjFn = adjacenciesComplement(truePattern, pattern);
                        int adjFp = adjacenciesComplement(pattern, truePattern);
                        int truePosAdj = truePositivesAdj(truePattern, pattern);

                        System.out.println("AdjFn = " + adjFn);
                        System.out.println("AdjFp = " + adjFp);
                        System.out.println("TruePosAdj = " + truePosAdj);

                        int edgeFn = edgesComplement(truePattern, pattern);
                        int edgeFp = edgesComplement(pattern, truePattern);
                        int truePosEdges = truePositiveEdges(truePattern, pattern);

//                        int edgeFn = arrowsComplement(truePattern, pattern);
//                        int edgeFp = arrowsComplement(pattern, truePattern);
//                        int truePosEdges = truePositiveArrows(truePattern, pattern);

                        double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
                        double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);

                        System.out.println("adjPrecision = " + adjPrecision);
                        System.out.println("adjRecall = " + adjRecall);

                        double edgePrecision = truePosEdges / (double) (truePosEdges + edgeFp);
                        double edgeRecall = truePosEdges / (double) (truePosEdges + edgeFn);

                        sumErrors += adjFn + adjFp;

                        if (!Double.isNaN(adjPrecision)) {
                            sumAP += adjPrecision;
                            sumAPN++;
                        }

                        if (!Double.isNaN(adjRecall)) {
                            sumAR += adjRecall;
                            sumARN++;
                        }

                        if (!Double.isNaN(edgePrecision)) {
                            sumEP += edgePrecision;
                            sumEPN++;
                        }

                        if (!Double.isNaN(edgeRecall)) {
                            sumER += edgeRecall;
                            sumERN++;
                        }

//                    out.println("Model # " + modelIndex + " AP (CCI) = " + adjPrecision);
//                    out.println("Model # " + modelIndex + " AR (CCI) = " + adjRecall);

                    }

                    out.println("\nAverages " + indTestType);
                    out.println("Model # " + sampleSize + " Average AP = " + nf.format(sumAP / sumAPN));
                    out.println("Model # " + sampleSize + " Average AR = " + nf.format(sumAR / sumARN));
                    out.println("Model # " + sampleSize + " Average EP = " + nf.format(sumEP / sumEPN));
                    out.println("Model # " + sampleSize + " Average ER = " + nf.format(sumER / sumERN));
                    out.println("Model # " + sampleSize + " Average Adj Errors = " + nf.format(sumErrors / (double) numModels));

                    stats[s][type * 2] = sumEP / sumEPN;
                    stats[s][type * 2 + 1] = sumER / sumERN;
                }
            }

            System.out.println(MatrixUtils.toString(stats));

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test12_3() {

        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

//            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test12-3final/";
            String _dir = "/home/jdramsey/test12-3final/";
            File dir = new File(_dir);

            if (!dir.exists()) dir.mkdir();

            int numRuns = 1;
            double alpha = 0.01;
            double gamma = 0.01;
            double[][] stats = new double[5][8];

//            This should be done only once.
            for (int sampleSize : new int[]{2000}) {

                System.out.println("================= Sample size " + sampleSize + " =================");

                int numVariables = 200;
                int N = sampleSize;

                for (int run = 1; run <= numRuns; run++) {
                    File file = new File(dir, "data." + sampleSize + "." + run + ".txt");

                    if (file.exists()) continue;

                    GeneralizedSemIm im = makeTestIm4(numVariables, 10);

                    DataSet data = im.simulateData(N, false);

                    PrintWriter out1 = new PrintWriter(file);
                    DataWriter.writeRectangularData(data, out1, '\t');

                    File file2 = new File(dir, "graph." + sampleSize + "." + run + ".txt");
                    File file3 = new File(dir, "graph.tetrad." + sampleSize + "." + run + ".txt");
                    File file6 = new File(dir, "dag.tetrad." + sampleSize + "." + run + ".txt");
                    File file7 = new File(dir, "model." + sampleSize + "." + run + ".txt");

                    SemGraph dag = im.getSemPm().getGraph();
                    dag.setShowErrorTerms(false);
                    Graph _dag = GraphUtils.replaceNodes(dag, data.getVariables());
                    Graph truePattern = SearchGraphUtils.patternForDag(_dag);

                    PrintWriter out2 = new PrintWriter(file2);
                    PrintWriter out3 = new PrintWriter(file3);
                    PrintWriter out6 = new PrintWriter(file6);
                    PrintWriter out7 = new PrintWriter(file7);

                    writePatternAsMatrix(data.getVariables(), truePattern, out2);

                    out3.println(truePattern.toString());
                    out6.println(dag.toString());

                    out7.println(im);

                    out1.close();
                    out2.close();
                    out3.close();
                    out6.close();
                    out7.close();
                }
            }

            int[] sizes = {2000};

            for (int s = 0; s < sizes.length; s++) {
                int sampleSize = sizes[s];

                System.out.println("Sample size " + sampleSize);

                NumberFormat nf = new DecimalFormat("0.00");

                String[] indTestTypes = new String[]{"fisherz", "drton", "cci"};
//                String[] indTestTypes = new String[]{"fisherz", "drton"};

//                String indTestType = "fisherz";
//                String indTestType = "cci";
//                    String indTestType = "kci";
//                    String indTestType = "drton";
                for (int type = 0; type < indTestTypes.length; type++) {

                    long start = System.currentTimeMillis();

//                for (String indTestType : indTestTypes) {
                    String indTestType = indTestTypes[type];

                    double sumAP = 0.0;
                    double sumAR = 0.0;

                    double sumEP = 0.0;
                    double sumER = 0.0;

                    int sumErrors = 0;

                    int sumAPN = 0;
                    int sumARN = 0;

                    int sumEPN = 0;
                    int sumERN = 0;

                    for (int run = 1; run <= numRuns; run++) {
                        System.out.println("\nRun " + run);

                        File file4 = new File(dir, "pattern." + indTestType + "." + sampleSize + "." + run + ".txt");
                        Graph pattern;

                        File file3 = new File(dir, "graph.tetrad." + sampleSize + "." + run + ".txt");
                        Graph truePattern = GraphUtils.loadGraphTxt(file3);

                        if (!file4.exists()) {
                            File file = new File(dir, "data." + sampleSize + "." + run + ".txt");

                            DataReader reader = new DataReader();
                            reader.setVariablesSupplied(true);
                            reader.setDelimiter(DelimiterType.WHITESPACE);

                            DataSet dataSet = reader.parseTabular(file);
//                            long start2 = System.currentTimeQMillis();

                            double cutoff = indTestType.equals("drton") ? gamma : alpha;

                            PcStable pc = new PcStable(getIndependenceTest(indTestType, dataSet, cutoff));
                            pattern = pc.search();
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);
//                            long stop2 = System.currentTimeMillis();
//                            System.out.println("Elapsed (just " + indTestType + ") " + (stop2 - start2) / 1000L + " seconds");

                            PrintWriter out4 = new PrintWriter(file4);
                            out4.println(pattern);
                            out4.close();

                            System.out.println("Pattern = " + pattern);
                        } else {
                            pattern = GraphUtils.loadGraphTxt(file4);
//                            pattern = GraphUtils.bidirectedToUndirected(pattern);
                        }

                        pattern = GraphUtils.replaceNodes(pattern, truePattern.getNodes());
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);

                        int adjFn = adjacenciesComplement(truePattern, pattern);
                        int adjFp = adjacenciesComplement(pattern, truePattern);
                        int truePosAdj = truePositivesAdj(truePattern, pattern);

                        System.out.println("AdjFn = " + adjFn);
                        System.out.println("AdjFp = " + adjFp);
                        System.out.println("TruePosAdj = " + truePosAdj);

                        int edgeFn = edgesComplement(truePattern, pattern);
                        int edgeFp = edgesComplement(pattern, truePattern);
                        int truePosEdges = truePositiveEdges(truePattern, pattern);

//                        int edgeFn = arrowsComplement(truePattern, pattern);
//                        int edgeFp = arrowsComplement(pattern, truePattern);
//                        int truePosEdges = truePositiveArrows(truePattern, pattern);

                        double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
                        double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);

                        System.out.println("adjPrecision = " + adjPrecision);
                        System.out.println("adjRecall = " + adjRecall);

                        double edgePrecision = truePosEdges / (double) (truePosEdges + edgeFp);
                        double edgeRecall = truePosEdges / (double) (truePosEdges + edgeFn);

                        sumErrors += adjFn + adjFp;

                        if (!Double.isNaN(adjPrecision)) {
                            sumAP += adjPrecision;
                            sumAPN++;
                        }

                        if (!Double.isNaN(adjRecall)) {
                            sumAR += adjRecall;
                            sumARN++;
                        }

                        if (!Double.isNaN(edgePrecision)) {
                            sumEP += edgePrecision;
                            sumEPN++;
                        }

                        if (!Double.isNaN(edgeRecall)) {
                            sumER += edgeRecall;
                            sumERN++;
                        }

//                    out.println("Model # " + modelIndex + " AP (CCI) = " + adjPrecision);
//                    out.println("Model # " + modelIndex + " AR (CCI) = " + adjRecall);

                    }

                    out.println("\nAverages " + indTestType);
                    out.println("Model # " + sampleSize + " Average AP = " + nf.format(sumAP / sumAPN));
                    out.println("Model # " + sampleSize + " Average AR = " + nf.format(sumAR / sumARN));
                    out.println("Model # " + sampleSize + " Average EP = " + nf.format(sumEP / sumEPN));
                    out.println("Model # " + sampleSize + " Average ER = " + nf.format(sumER / sumERN));
//                    out.println("Model # " + sampleSize + " Average Adj Errors = " + nf.format(sumErrors / (double) numModels));

                    stats[s][type * 2] = sumEP / sumEPN;
                    stats[s][type * 2 + 1] = sumER / sumERN;

                    long stop = System.currentTimeMillis();

                    System.out.println("Elapsed time " + (stop - start) / 1000L);
                }
            }

            System.out.println(MatrixUtils.toString(stats));

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void test13() {
        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata_for_joe/";
            File dir = new File(_dir);

            File[] files = dir.listFiles();

            DataSet dataSet = null;

            for (int i = 0; i < 20; i++) {//files.length; i++) {
                File file = files[i];
                File file2 = new File(file, "roidata");
                File[] files2 = file2.listFiles();
                File file3 = files2[0];

                DataReader reader = new DataReader();
                reader.setVariablesSupplied(false);
                reader.setDelimiter(DelimiterType.WHITESPACE);

                DataSet _dataSet = reader.parseTabular(file3);


                if (dataSet == null) {
                    dataSet = _dataSet;
                } else {
                    dataSet = DataUtils.concatenateData(dataSet, _dataSet);
                }
            }

            {
//                Pc pc = new Pc(new IndTestFisherZ(dataSet, 0.001));
                PcStable pc = new PcStable(new IndTestFisherZ(dataSet, 0.001));
//                Cpc pc = new Cpc(new IndTestFisherZ(dataSet, 0.001));
//                PcStable pc = new PcStable(new IndTestConditionalCorrelation(dataSet, .001));
//                pc.setDepth(3);
                Graph graph = pc.search();

                System.out.println(graph);

                Lofs2 lofs = new Lofs2(graph, Collections.singletonList(dataSet));
                lofs.setRule(Lofs2.Rule.R3);
                Graph graph2 = lofs.orient();

//                lofs.setRule(Lofs2.Rule.RSkew);
//                graph = lofs.orie2nt();

                System.out.println("With R3" + graph2);

                graph2 = GraphUtils.replaceNodes(graph2, graph.getNodes());
                int countSame = 0;
                int countBothDirected = 0;

                for (Edge edge1 : graph.getEdges()) {
                    Node n1 = edge1.getNode1();
                    Node n2 = edge1.getNode2();
                    Edge edge2 = graph2.getEdge(n1, n2);

                    if (Edges.isDirectedEdge(edge1) && Edges.isDirectedEdge(edge2)) {
                        countBothDirected++;

                        if (edge1.equals(edge2)) {
                            countSame++;
                        }
                    }
                }

                System.out.println(countSame + " of " + countBothDirected);
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test14() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";
            File _graphDir = new File(graphDir);

            if (!_graphDir.exists()) _graphDir.mkdir();

//            List<DataSet> dataSets = loadDataSets();
//            List<DataSet> _dataSets = new ArrayList<DataSet>();
//
//            for (int m = 0; m < 67; m++) {
//                DataSet dataSet = dataSets.get(m);
//                _dataSets.add(dataSet);
//            }

            List<DataSet> dataSets = loadDataSets();
            List<DataSet> _dataSets = new ArrayList<DataSet>();

            for (int m = 0; m < 67; m++) {
                DataSet dataSet = dataSets.get(m);
                dataSet = DataUtils.standardizeData(dataSet);
                _dataSets.add(dataSet);
            }
//
            DataSet dataSet = DataUtils.concatenateData(_dataSets);

            Graph graph;

//            IndependenceTest test = new IndTestFisherZ(_dataSets.get(0), 1e-5);
            double alpha = .001;
            double percent = 50;
//            IndTestFisherZConcatenateResiduals2 test = new IndTestFisherZConcatenateResiduals2(dataSets, alpha);
            IndTestFisherZFisherPValue test = new IndTestFisherZFisherPValue(dataSets, alpha);
////            IndTestConditionalCorrelation test = new IndTestConditionalCorrelation(dataSets.get(0), alpha);
////            test.setPercent(percent / 100.0);
//
//            IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

            Pc s = new Pc(test);
            s.setVerbose(true);

//            MsPc s = new MsPc(_dataSets, 0.001, percent / 100.0);
            s.setDepth(3);
//            s.setVerbose(false);
            graph = s.search();

            System.out.println(graph);

//            File file = new File(graphDir, "pc.test3." + percent + "." + _dataSets.size() + "." + alpha + ".txt");
            File file = new File(graphDir, "pc.concatstandardized." + _dataSets.size() + "." + alpha + ".txt");

            System.out.println(file.getAbsolutePath());

            writeGraph(file, graph);

            Graph graph3 = new EdgeListGraph(graph);

            List<Node> nodes = new ArrayList<Node>();

            // the brain layout is going to remove the "X's".
            for (Node node : graph3.getNodes()) {
                nodes.add(new GraphNode(node.getName()));
            }

            graph3 = GraphUtils.replaceNodes(graph3, nodes);

            BrainSpecial special = new BrainSpecial(graph3);
            special.doLayout();

            File pngFile2 = new File(file.getAbsoluteFile() + ".brainXY.png");
            System.out.println(pngFile2);

            PngWriter.writePng(graph3, pngFile2);

            for (Node node : graph.getNodes()) {
                if (graph.getAdjacentNodes(node).isEmpty()) {
                    graph.removeNode(node);
                }
            }

            GraphUtils.circleLayout(graph, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(graph);

            File pngFile = new File(file.getAbsoluteFile() + ".png");
            PngWriter.writePng(graph, pngFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test14a() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";
            File _graphDir = new File(graphDir);

            if (!_graphDir.exists()) _graphDir.mkdir();

            List<DataSet> dataSets = loadDataSets();
            List<DataSet> _dataSets = new ArrayList<DataSet>();

            for (int m = 0; m < 67; m++) {
                DataSet dataSet = dataSets.get(m);
                dataSet = DataUtils.standardizeData(dataSet);
                _dataSets.add(dataSet);
            }

//            DataSet dataSet = DataUtils.concatenateData(_dataSets);

            Images images = new Images(dataSets);
//            Images images = new Images(Collections.singletonList(dataSet));
            Graph graph = images.search();

            File file = new File(graphDir, "images67.individuallynonstandardized.txt");

            System.out.println(file.getAbsolutePath());

            writeGraph(file, graph);

            Graph graph3 = new EdgeListGraph(graph);

            List<Node> nodes = new ArrayList<Node>();

            // the brain layout is going to remove the "X's".
            for (Node node : graph3.getNodes()) {
                nodes.add(new GraphNode(node.getName()));
            }

            graph3 = GraphUtils.replaceNodes(graph3, nodes);

            BrainSpecial special = new BrainSpecial(graph3);
            special.doLayout();

            File pngFile2 = new File(file.getAbsoluteFile() + ".brainXY.png");
            System.out.println(pngFile2);

            PngWriter.writePng(graph3, pngFile2);

            for (Node node : graph.getNodes()) {
                if (graph.getAdjacentNodes(node).isEmpty()) {
                    graph.removeNode(node);
                }
            }

            GraphUtils.circleLayout(graph, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(graph);

            File pngFile = new File(file.getAbsoluteFile() + ".png");
            PngWriter.writePng(graph, pngFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test14a1() {
        String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";

        Graph pc = GraphUtils.loadGraphTxt(new File(dir, "pc.concat67.75.0.67.0.001.txt"));
        Graph images = GraphUtils.loadGraphTxt(new File(dir, "images67.concat.txt"));

//        Graph pc = GraphUtils.loadGraphTxt(new File(dir, "cpc.75.0.67.0.001.txt"));
//        Graph images = GraphUtils.loadGraphTxt(new File(dir, "graph.images67.txt"));

        GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison2(pc, images);

        String graph1 = "PC";
        String graph2 = "IMaGES";

        System.out.println("Adjacencies in " + graph1 + " not in " + graph2 + ": " + comparison.getAdjFp());
        System.out.println("Adjacencies in " + graph2 + " not in " + graph1 + ": " + comparison.getAdjFn());
        System.out.println("Adjacencies in both " + graph1 + " and " + graph2 + ": " + comparison.getAdjCorrect());

        System.out.println("Arrowpoints in " + graph1 + " not in " + graph2 + ": " + comparison.getArrowptFp());
        System.out.println("Arrowpoints in " + graph2 + " not in " + graph1 + ": " + comparison.getArrowptFn());
        System.out.println("Arrowpoints in both " + graph1 + " and " + graph2 + ": " + comparison.getArrowptCorrect());


    }

    public void test14b() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";
            File _graphDir = new File(graphDir);

            if (!_graphDir.exists()) _graphDir.mkdir();

            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/";
//            String _dir = "/home/jdramsey/roidata/";
            File dir = new File(_dir);

            File[] files = dir.listFiles();

            int numDataSets = 67;

            List<Graph> graphs = new ArrayList<Graph>();

            for (int k = 1; k <= 60; k++) {
                List<DataSet> _dataSets = new ArrayList<DataSet>();
                double penaltyDiscount = 2.0;

                File file = new File(graphDir, "test" + penaltyDiscount + ".c.graph.images." + k + ".txt");

                System.out.println(file.getAbsolutePath());
                Graph graph = GraphUtils.loadGraphTxt(file);
                graphs.add(graph);
            }

            List<Node> nodes = graphs.get(0).getNodes();

            List<Graph> graphs2 = new ArrayList<Graph>();
            int c = -1;

            for (Graph graph : graphs) {
                if (++c >= 0) {
                    Graph e = GraphUtils.replaceNodes(graph, nodes);
                    e = GraphUtils.undirectedGraph(e);
                    graphs2.add(e);
                }
            }

            graphs = graphs2;

            Map<Edge, Integer> edges = new HashMap<Edge, Integer>();
//            Map<Edge, Integer> edgesReversed = new HashMap<Edge, Integer>();
            int count = 0;

            for (Graph graph : graphs2) {
//                graph = GraphUtils.replaceNodes(graph, graphs.get(0).getNodes());
                System.out.println("# Edges in graph #" + (++count) + " = " + graph.getNumEdges());

                for (Edge edge : graph.getEdges()) {
//                    edge = Edges.undirectedEdge(edge.getNode1(), edge.getNode2());
                    increment(edges, edge);
                }
            }

//            System.out.println("# Edges in the union of all graphs = " + edges.size());
//
//            int[] counts = new int[dataSets.size() + 1];
//            Map<Integer, List<Edge>> edgesAtCount = new HashMap<Integer, List<Edge>>();
//
//            for (int i = 1; i <= dataSets.size(); i++) {
//                edgesAtCount.put(i, new ArrayList<Edge>());
//            }
//
//            for (Edge edge : edges.keySet()) {
//                int _count = edges.get(edge);
//                counts[_count]++;
//                edgesAtCount.get(_count).add(edge);
//            }
//
//            for (int i = 1; i <= dataSets.size(); i++) {
//                System.out.println(i + " " + counts[i]);
//            }

//            Graph topEdges = new EdgeListGraph(graphs.get(0).getNodes());
//
//            for (int i = 1; i <= 46; i++) {
//                List<Edge> _edges = edgesAtCount.get(i);
//                for (Edge edge : _edges) {
//                    topEdges.addEdge(edge);
//                }
//            }

            Graph topEdges = new EdgeListGraph(graphs.get(0).getNodes());

            for (Edge edge : edges.keySet()) {
                int _count = 0;

                for (int i = graphs2.size() - 1; i >= 0; i--) {
//                    for (int i = 0; i < graphs2.size(); i++) {
                    Graph graph = graphs2.get(i);
                    if (graph.containsEdge(edge)) {
                        _count++;

                        if (_count == 10) {
                            topEdges.addEdge(edge);
                        }
                    } else {
                        break;
                    }
                }

                if (_count > 0) {
                    System.out.println("Edge " + edge + " count = " + _count);
                }
            }

            for (Node node : topEdges.getNodes()) {
                if (topEdges.getAdjacentNodes(node).isEmpty()) {
                    topEdges.removeNode(node);
                }
            }

            for (int i = 1; i < graphs2.size(); i++) {
                Graph lastGraph = graphs2.get(i - 1);
                Graph thisGraph = graphs2.get(i);

                List<Edge> lastEdges = lastGraph.getEdges();
                List<Edge> thisEdges = thisGraph.getEdges();

                List<Edge> gained = new ArrayList<Edge>(thisEdges);
                gained.removeAll(lastEdges);

                List<Edge> lost = new ArrayList<Edge>(lastEdges);
                lost.removeAll(thisEdges);

                List<Edge> retained = new ArrayList<Edge>(thisEdges);
                retained.retainAll(lastEdges);

//                System.out.println("Graph #" + i);
//                System.out.println("GAINED:");
//
//                for (int j = 0; j < gained.size(); j++) {
//                    System.out.println(j + ". " + gained.get(j));
//                }
//
//                System.out.println("LOST:");
//
//                for (int j = 0; j < lost.size(); j++) {
//                    System.out.println(j + ". " + lost.get(j));
//                }
//
//                System.out.println("RETAINED:");
//
//                for (int j = 0; j < retained.size(); j++) {
//                    System.out.println(j + ". " + retained.get(j));
//                }
//
//                System.out.println();

                System.out.println(i + " -> " + (i + 1) + ": " + retained.size() + " out of " + thisEdges.size());
            }

            writeGraph(new File(graphDir, "graph.top.edges.txt"), topEdges);
//
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    public void test14c() {
//        try {
//            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
////            String graphDir = "/home/jdramsey/test14/";
//            File _graphDir = new File(graphDir);
//
//            if (!_graphDir.exists()) _graphDir.mkdir();
//
////            List<DataSet> dataSets = loadDataSets();
//
//            double penalty = 1.0;
//
//            // Partition the data sets into groups of 10 and run IMaGES on each.
//
//            String tag = "images";
//
////            for (int partition = 1; partition <= 6; partition++) {
////                List<DataSet> _dataSets = partitionDataSets(dataSets, partition);
////
////
////                Images images = new Images(_dataSets);
////                images.setPenaltyDiscount(penalty);
////
//////                IndependenceTest test = new IndTestFisherZConcatenateResiduals(_dataSets, 1e-10);
//////                Pc images = new Pc(test);
////                Graph graph = images.search();
////                writeGraph(new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt"), graph);
////            }
//
//            List<Graph> graphs = new ArrayList<Graph>();
//
//            for (int partition = 1; partition <= 6; partition++) {
//
//                File file = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
//                Graph graph = GraphUtils.loadGraphTxt(file);
//
//                if (!graphs.isEmpty()) {
//                    graph = GraphUtils.replaceNodes(graph, graphs.get(0).getNodes());
//                }
//
////                graph = GraphUtils.undirectedGraph(graph);
//                graphs.add(graph);
//            }
//
//            Graph intersection = new EdgeListGraph(graphs.get(0).getNodes());
//
//            List<Edge> edges = new ArrayList<Edge>(graphs.get(0).getEdges());
//
//            for (int i = 1; i < 6; i++) {
//                edges.retainAll(graphs.get(i).getEdges());
//            }
//
//            for (int i = 0; i < edges.size(); i++) {
//                intersection.addEdge(edges.get(i));
//            }
//
//            writeFiveGraphFormats(graphDir, intersection, "graph.partition.undirected." + tag + "." + penalty + ".intersection.txt");
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public void test14a2() {

        Graph graph = GraphUtils.randomDag(50, 50, false);

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);
        GeneralizedSemIm im = new GeneralizedSemIm(pm);
        List<DataSet> dataSets = new ArrayList<DataSet>();
        for (int i = 0; i < 10; i++) {
            dataSets.add(im.simulateData(800, false));
        }
        MsPc pc = new MsPc(dataSets, 0.01, .9);
        pc.setDepth(2);
//        Pc pc = new Pc(new IndTestFisherZConcatenateResiduals(dataSets, 0.05));
        Graph _graph = pc.search();
        System.out.println(_graph);
    }

    public void test14c1() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";
            File _graphDir = new File(graphDir);

            double penalty = 1.0;
            double clip = 0.9;

            // Partition the data sets into groups of 10 and run IMaGES on each.

            String tag = "images";

            List<DataSet> dataSets = loadDataSets();

            for (int partition = 1; partition <= 6; partition++) {
//                List<DataSet> _dataSets = partitionDataSets(dataSets, partition, dataSets.size(), 10);
                List<DataSet> _dataSets = partitionDataSets(dataSets, partition, dataSets.size(), 5);

                Images images = new Images(_dataSets);
                images.setPenaltyDiscount(penalty);
                images.setTrimAlpha(clip);

//                IndependenceTest test = new IndTestFisherZConcatenateResiduals(_dataSets, 1e-10);
//                Pc images = new Pc(test);
                Graph graph = images.search();
                writeGraph(new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".clip" + clip + ".txt"), graph);
            }

            List<Graph> graphs = new ArrayList<Graph>();

            for (int partition = 1; partition <= 6; partition++) {

                File file = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".clip" + clip + ".txt");
                Graph graph = GraphUtils.loadGraphTxt(file);

                if (!graphs.isEmpty()) {
                    graph = GraphUtils.replaceNodes(graph, graphs.get(0).getNodes());
                }

                writeFiveGraphFormats(graphDir, graph, "graph.partition." + tag + "." + penalty + "." + partition + ".clip" + clip + ".txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test14cb() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";

            List<DataSet> dataSets = loadDataSets();

            Images images = new Images(dataSets);

            Graph graph = images.search();
            writeFiveGraphFormats(graphDir, graph, "graph.images67.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<DataSet> partitionDataSets(List<DataSet> dataSets, int partition, int max, int partitionSize) {
        List<DataSet> _dataSets = new ArrayList<DataSet>();

        for (int i = (partition - 1) * partitionSize; i < min(partition * partitionSize, max); i++) {
            _dataSets.add(dataSets.get(i));
        }

        return _dataSets;
    }

//    public void test14c2() {
//        try {
//            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
////            String graphDir = "/home/jdramsey/test14/";
//            File _graphDir = new File(graphDir);
//
//            if (!_graphDir.exists()) _graphDir.mkdir();
//
//            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/";
////            String _dir = "/home/jdramsey/roidata/";
//            File dir = new File(_dir);
//
//            File[] files = dir.listFiles();
//
////            List<DataSet> dataSets = loadDataSets();
//
//            double penalty = 1.0;
//
//            // Partition the data sets into groups of 10 and run IMaGES on each.
//
//            String tag = "images";
//
////            for (int partition = 1; partition <= 6; partition++) {
////                List<DataSet> _dataSets = new ArrayList<DataSet>();
////
////                for (int i = partition * 10 - 9; i <= min(partition * 10, 65); i++) {
////                    _dataSets.add(dataSets.get(i));
////                }
////
////
////                Images images = new Images(_dataSets);
////                images.setPenaltyDiscount(penalty);
////                Graph graph = images.search();
////                writeGraph(new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt"), graph);
////            }
//
//            List<Graph> graphs1 = new ArrayList<Graph>();
//
//            for (int partition = 1; partition <= 6; partition++) {
//
//                File file = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
//                Graph graph = GraphUtils.loadGraphTxt(file);
//
//                if (!graphs1.isEmpty()) {
//                    graph = GraphUtils.replaceNodes(graph, graphs1.get(0).getNodes());
//                }
//
//                System.out.println(graph);
//
////                graphs1.add(GraphUtils.undirectedGraph(graph));
//                graphs1.add(graph);
//            }
//
//            Graph consistent = new EdgeListGraph(graphs1.get(0).getNodes());
//
//            Set<Edge> edges = new HashSet<Edge>();
//
//            for (int i = 0; i < 6; i++) {
//                edges.addAll(graphs1.get(i).getEdges());
//            }
//
//            Set<Edge> unconflicted = new HashSet<Edge>();
//
//            for (Edge edge : edges) {
//                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
//                        edge.getEndpoint1());
//                if (!edges.contains(_edge)) {
//                    unconflicted.add(edge);
//                }
//            }
//
//            List<Edge> _unconflicted = new ArrayList<Edge>(unconflicted);
//
//            for (int i = 0; i < _unconflicted.size(); i++) {
//                consistent.addEdge(_unconflicted.get(i));
//            }
//
//            Graph all = new EdgeListGraph(graphs1.get(0).getNodes());
//
//            for (Edge edge : edges) {
//                if (Edges.isDirectedEdge(edge)) {
//                    all.addEdge(edge);
//                }
//            }
//
//            for (Node node : consistent.getNodes()) {
//                if (consistent.getAdjacentNodes(node).isEmpty()) {
//                    consistent.removeNode(node);
//                }
//            }
//
//            System.out.println(consistent);
//
//            writeGraph(new File(graphDir, "graph.partition.undirected." + tag + "." + penalty + ".consistent.txt"), consistent);
//            writeGraph(new File(graphDir, "graph.partition.undirected." + tag + "." + penalty + ".all.txt"), all);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public void test14c3() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";

//            Lofs2.Rule rule = null; //Lofs2.Rule.R3;
            Lofs2.Rule rule = Lofs2.Rule.R3;

            int minGraphs = 1;

            List<DataSet> dataSets = loadDataSets();

            double penalty = 1.0;

            // Partition the data sets into groups of 10 and run IMaGES on each.

            String tag = "images";

//            if (!new File(graphDir, "graph.partition." + tag + "." + penalty + "." + 1 + ".txt").exists()) {
//                for (int partition = 1; partition <= 6; partition++) {
//                    List<DataSet> _dataSets = partitionDataSets(dataSets, partition);
//
//                    Images images = new Images(_dataSets);
//                    images.setPenaltyDiscount(penalty);
//                    Graph graph = images.search();
//                    writeGraph(new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt"), graph);
//                }
//            }

            List<Graph> graphs = new ArrayList<Graph>();

            File file0 = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + 1 + ".txt");
            Graph referenceGraph = GraphUtils.loadGraphTxt(file0);

            List<Node> nodes = referenceGraph.getNodes();

            for (int partition = 1; partition <= 6; partition++) {

                File file = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
                Graph graph = GraphUtils.loadGraphTxt(file);

                graph = GraphUtils.replaceNodes(graph, nodes);

                if (rule != null) {
                    Lofs2 lofs = new Lofs2(graph, partitionDataSets(dataSets, partition, 67, 10));
                    lofs.setRule(rule);
                    lofs.setAlpha(1.0);
                    graph = lofs.orient();
                }

                graph = GraphUtils.replaceNodes(graph, nodes);

//                graphs1.add(GraphUtils.undirectedGraph(graph));
                graphs.add(graph);

                System.out.println(graph);

            }

            Graph all = new EdgeListGraph(nodes);

            Set<Edge> edges = new HashSet<Edge>();

            Map<Edge, Integer> edgeIntegerMap = new HashMap<Edge, Integer>();

            for (int i = 0; i < 6; i++) {
                edges.addAll(graphs.get(i).getEdges());
                for (Edge edge : graphs.get(i).getEdges()) {
                    increment(edgeIntegerMap, edge);
                }
            }

            for (Edge edge : edges) {
                if (edgeIntegerMap.get(edge) >= minGraphs) {
                    all.addEdge(edge);
                }
            }

            int numConflicts = 0;
            int numDirectedEdges = 0;

            for (Edge edge : all.getEdges()) {
                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
                        edge.getEndpoint1());
                if (edgeIntegerMap.containsKey(edge) && edgeIntegerMap.containsKey(_edge)) {
                    numConflicts++;
                }

                numDirectedEdges++;
            }

            numConflicts /= 2;

            numDirectedEdges -= numConflicts;

            System.out.println("Num conflicts = " + numConflicts);
            System.out.println("Num directed adjacencies = " + numDirectedEdges);

            Graph consistent = new EdgeListGraph(graphs.get(0).getNodes());

            for (Edge edge : all.getEdges()) {
                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
                        edge.getEndpoint1());
                if (edge.isDirected() && !edges.contains(_edge)) {
                    consistent.addEdge(edge);
                }
            }

            for (Node node : consistent.getNodes()) {
                if (consistent.getEdges(node).isEmpty()) {
                    consistent.removeNode(node);
                }
            }

            System.out.println(consistent);
            String filename;

            if (rule == null) {
                filename = "graph.partition." + "images" + "." + penalty + "." + minGraphs + ".consistent.txt";
            } else {
                filename = "graph.partition." + rule + "." + penalty + "." + minGraphs + ".consistent.txt";
            }

            File file = new File(graphDir, filename);
            writeGraph(file, consistent);

            for (Node node : consistent.getNodes()) {
                if (consistent.getAdjacentNodes(node).isEmpty()) {
                    consistent.removeNode(node);
                }
            }

            BrainSpecial special = new BrainSpecial(consistent);
            special.doLayout();
            GraphUtils.circleLayout(consistent, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(consistent);

            File pngFile = new File(file.getAbsoluteFile() + ".brainXY.png");
            PngWriter.writePng(consistent, pngFile);

            GraphUtils.circleLayout(consistent, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(consistent);

            File pngFile2 = new File(file.getAbsoluteFile() + ".force.png");
            PngWriter.writePng(consistent, pngFile2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test14c5() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
            String imagesPartitionDir = graphDir + "images.partition";
//            String graphDir = "/home/jdramsey/test14/";

            Lofs2.Rule rule = null; //Lofs2.Rule.R3; //Images
//            Lofs2.Rule rule = Lofs2.Rule.RSkew;

//            int minGraphs = 6;

//            List<DataSet> dataSets = null; //loadDataSets(files);
            List<DataSet> dataSets = loadDataSets();

            double penalty = 1.0;

            // Partition the data sets into groups of 10 and run IMaGES on each.

            String tag = "images";

            if (!new File(imagesPartitionDir, "graph.partition." + tag + "." + penalty + "." + 1 + ".txt").exists()) {
                for (int partition = 1; partition <= 6; partition++) {
                    List<DataSet> _dataSets = partitionDataSets(dataSets, partition, 67, 10);

                    Images images = new Images(_dataSets);
                    images.setPenaltyDiscount(penalty);
                    Graph graph = images.search();
//                    writeGraph(new File(imagesPartitionDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt"), graph);
                    writeFiveGraphFormats(graphDir, graph, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
                }
            }

            List<Graph> graphs = new ArrayList<Graph>();

            File file0 = new File(imagesPartitionDir, "graph.partition." + tag + "." + penalty + "." + 1 + ".txt");
            Graph referenceGraph = GraphUtils.loadGraphTxt(file0);

            List<Node> nodes = referenceGraph.getNodes();

            for (int partition = 1; partition <= 6; partition++) {

                File file = new File(imagesPartitionDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
                Graph graph = GraphUtils.loadGraphTxt(file);

                graph = GraphUtils.replaceNodes(graph, nodes);

                if (rule != null) {
                    Lofs2 lofs = new Lofs2(graph, partitionDataSets(dataSets, partition, 67, 10));
                    lofs.setRule(rule);
                    lofs.setAlpha(1.0);
                    graph = lofs.orient();
                }

                graph = GraphUtils.replaceNodes(graph, nodes);

//                graphs1.add(GraphUtils.undirectedGraph(graph));
                graphs.add(graph);

                System.out.println(graph);

            }

            for (int minGraphs = 1; minGraphs <= 6; minGraphs++) {
                Graph all = new EdgeListGraph(nodes);

                Set<Edge> edges = new HashSet<Edge>();

                Map<Edge, Integer> edgeIntegerMap = new HashMap<Edge, Integer>();

                for (int i = 0; i < 6; i++) {
                    edges.addAll(graphs.get(i).getEdges());
                    for (Edge edge : graphs.get(i).getEdges()) {
                        increment(edgeIntegerMap, edge);
                    }
                }

                for (Edge edge : edges) {
                    if (edgeIntegerMap.get(edge) >= minGraphs) {
                        all.addEdge(edge);
                    }
                }

                int numConflicts = 0;
                int numDirectedEdges = 0;

                for (Edge edge : all.getEdges()) {
                    Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
                            edge.getEndpoint1());
                    if (edgeIntegerMap.containsKey(edge) && edgeIntegerMap.containsKey(_edge)) {
                        numConflicts++;
                    }

                    numDirectedEdges++;
                }

                numConflicts /= 2;

//            for (Edge edge : new HashSet<Edge>(edges)) {
//                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
//                        edge.getEndpoint1());
//                if (edges.contains(_edge)) {
//                     if (Edges.isDirectedEdge(edge) && Edges.isDirectedEdge(_edge)) {
//                        numConflicts++;
//                    }
//
//                     edges.remove(edge);
//                    edges.remove(_edge);
//                }
//
//                if (Edges.isDirectedEdge(edge)) {
//                    numDirectedEdges++;
//                }
//
//            }

                numDirectedEdges -= numConflicts;

                System.out.println("Num conflicts = " + numConflicts);
                System.out.println("Num directed adjacencies = " + numDirectedEdges);

                Graph consistent = new EdgeListGraph(graphs.get(0).getNodes());

                for (Edge edge : all.getEdges()) {
                    Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
                            edge.getEndpoint1());
                    if (edge.isDirected() && !edges.contains(_edge)) {
                        consistent.addEdge(edge);
                    }
                }

                for (Node node : consistent.getNodes()) {
                    if (consistent.getEdges(node).isEmpty()) {
                        consistent.removeNode(node);
                    }
                }

                System.out.println(consistent);
                String filename;

                if (rule == null) {
                    filename = "graph.partition." + "images" + "." + penalty + "." + minGraphs + ".consistent.txt";
                } else {
                    filename = "graph.partition." + rule + "." + penalty + "." + minGraphs + ".consistent.txt";
                }

                writeFiveGraphFormats(graphDir, consistent, filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFiveGraphFormats(String graphDir, Graph graph, String filename) throws FileNotFoundException {
        File file = new File(graphDir, filename);
        writeGraph(file, graph);

        Graph graph2 = new EdgeListGraph(graph);

        for (Node node : graph2.getNodes()) {
            if (graph2.getAdjacentNodes(node).isEmpty()) {
                graph2.removeNode(node);
            }
        }

        BrainSpecial special = new BrainSpecial(graph2);
        special.doLayout();

        File pngFile = new File(file.getAbsoluteFile() + ".brainXY.png");
        PngWriter.writePng(graph2, pngFile);

        GraphUtils.circleLayout(graph2, 200, 200, 150);
        GraphUtils.fruchtermanReingoldLayout(graph2);

        File pngFile2 = new File(file.getAbsoluteFile() + ".force.png");
        PngWriter.writePng(graph2, pngFile2);

        File patternText = new File(file.getAbsoluteFile() + ".pattern.txt");
        PrintWriter out1 = new PrintWriter(patternText);
        writePatternAsMatrix(graph.getNodes(), graph, out1);

        Graph undir = GraphUtils.undirectedGraph(graph);

        File adjText = new File(file.getAbsoluteFile() + ".adj.txt");
        PrintWriter out2 = new PrintWriter(adjText);
        writePatternAsMatrix(graph.getNodes(), undir, out2);


        try {
            DataReader reader = new DataReader();
            reader.setDelimiter(DelimiterType.COMMA);
            reader.setVariablesSupplied(false);
            DataSet dataSet = reader.parseTabular(patternText);
            List<Node> nodes = graph.getNodes();
            Graph pattern = readPatternAsMatrix(nodes, dataSet);
            graph = GraphUtils.replaceNodes(graph, nodes);

            if (!pattern.equals(graph)) {
                System.out.println("NOT EQUAL!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DataReader reader = new DataReader();
            reader.setDelimiter(DelimiterType.COMMA);
            reader.setVariablesSupplied(false);
            DataSet dataSet = reader.parseTabular(adjText);
            List<Node> nodes = graph.getNodes();
            Graph _undir = readPatternAsMatrix(nodes, dataSet);
            _undir = GraphUtils.replaceNodes(_undir, nodes);

            if (!_undir.equals(undir)) {
                System.out.println("NOT EQUAL!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test14c6() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
            File _graphDir = new File(graphDir);

            if (!_graphDir.exists()) _graphDir.mkdir();

            double penalty = 1.0;

            for (int minGraphs1 = 1; minGraphs1 <= 6; minGraphs1++) {
                File dir1 = new File(graphDir, "partition.consistent/partition.consistent.images");

                String filename1 = "graph.partition." + "images" + "." + penalty + "." + minGraphs1 + ".consistent.txt";

                File file1 = new File(dir1, filename1);

                Graph graph1 = GraphUtils.loadGraphTxt(file1);

                for (int minGraphs2 = 1; minGraphs2 <= 6; minGraphs2++) {
                    String filename2 = "partition.consistent/partition.consistent.r3/" +
                            "graph.partition.R3." + penalty + "." + minGraphs1 + ".consistent.txt";

                    File file2 = new File(graphDir, filename2);

                    Graph graph2 = GraphUtils.loadGraphTxt(file2);
                    graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

                    Set<Edge> edges1 = new HashSet<Edge>(graph1.getEdges());
                    Set<Edge> edges2 = new HashSet<Edge>(graph2.getEdges());

                    Set<Edge> both = new HashSet<Edge>(edges1);
                    both.retainAll(edges2);

                    Graph intersection = new EdgeListGraph(graph1.getNodes());

                    for (Edge edge : both) {
                        intersection.addEdge(edge);
                    }

                    writeFiveGraphFormats(graphDir, intersection, "intersection." + minGraphs1 + "." + minGraphs2 + ".txt");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void test14c7() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
            File _graphDir = new File(graphDir);

            if (!_graphDir.exists()) _graphDir.mkdir();

            double penalty = 1.0;
            double clip = 0.9;

            List<Graph> partitionGraph = new ArrayList<Graph>();

            List<DataSet> dataSets = loadDataSets();

            Lofs2.Rule rule = null; //Lofs2.Rule.R3;
//            Lofs2.Rule rule = Lofs2.Rule.R3;

            for (int partition = 1; partition <= 6; partition++) {
                File dir1 = _graphDir; //new File(graphDir, "images.partition");

                String tag = "images";

//                String filename1 = "graph.partition." + "images" + "." + penalty + "." + partition + ".txt";
                String filename1 = "graph.partition." + tag + "." + penalty + "." + partition + ".clip" + clip + ".txt";

                File file1 = new File(dir1, filename1);

                Graph graph = GraphUtils.loadGraphTxt(file1);

                if (rule != null) {
                    Lofs2 lofs = new Lofs2(graph, partitionDataSets(dataSets, partition, 67, 10));
                    lofs.setRule(rule);
                    lofs.setAlpha(1.0);
                    graph = lofs.orient();
                }

                partitionGraph.add(graph);

            }

            Graph firstGraph = partitionGraph.get(0);
            List<Node> nodes = firstGraph.getNodes();
            Graph firstGraphUndirected = GraphUtils.undirectedGraph(firstGraph);
            Set<Edge> undirectedEdges = new HashSet<Edge>(firstGraphUndirected.getEdges());


            for (int i = 1; i < partitionGraph.size(); i++) {
                Graph _graph = partitionGraph.get(i);
                Graph _graphUndirected = GraphUtils.replaceNodes(_graph, nodes);
                _graphUndirected = GraphUtils.undirectedGraph(_graphUndirected);

                undirectedEdges.retainAll(_graphUndirected.getEdges());
            }

            Graph intersectionUndirected = new EdgeListGraph(firstGraphUndirected.getNodes());

            for (Edge edge : undirectedEdges) {
                intersectionUndirected.addEdge(edge);
            }


            List<Graph> undirectedPartitionGraphs = new ArrayList<Graph>();

            for (Graph graph : partitionGraph) {
                graph = GraphUtils.replaceNodes(graph, nodes);
                graph = GraphUtils.undirectedGraph(graph);
                undirectedPartitionGraphs.add(graph);
            }

            Set<Edge> allEdges = new HashSet<Edge>();

            for (Graph graph : undirectedPartitionGraphs) {
                allEdges.addAll(graph.getEdges());
            }

            int index = 0;
            List<Edge> target = new ArrayList<Edge>();
            int minCount1 = 4;
            int minCount2 = minCount1;

            for (Edge edge : allEdges) {//intersectionUndirected.getEdges()) {
//                if (intersectionUndirected.containsEdge(edge)) continue;

                int count = 0;

                for (Graph graph : undirectedPartitionGraphs) {
                    graph = GraphUtils.replaceNodes(graph, nodes);
                    if (graph.containsEdge(edge)) count++;
                }

                System.out.println((++index) + ". " + edge + " " + count);

                if (count >= minCount1) {
                    target.add(edge);
                }
            }

            Graph targetGraph = new EdgeListGraph(nodes);

            for (Edge edge : target) {
                targetGraph.addEdge(edge);
            }

//            writeFiveGraphFormats(graphDir, targetGraph, "intersection.5or6consistent." + penalty + ".txt");

            Graph intersectionConsistent = new EdgeListGraph(nodes);

            for (Edge edge : targetGraph.getEdges()) {
                Edge edge1 = Edges.directedEdge(edge.getNode1(), edge.getNode2());
                Edge edge2 = Edges.directedEdge(edge.getNode2(), edge.getNode1());
                Edge edge3 = Edges.undirectedEdge(edge.getNode1(), edge.getNode2());

                int count1 = 0;
                int count2 = 0;

                for (Graph graph : partitionGraph) {
                    if (graph.containsEdge(edge1)) count1++;
                    if (graph.containsEdge(edge2)) count2++;
                }

                if (count1 >= minCount2) {
                    intersectionConsistent.addEdge(edge1);
                } else if (count2 >= minCount2) {
                    intersectionConsistent.addEdge(edge2);
                } else {
                    intersectionConsistent.addEdge(edge3);
                }
            }

//            writeGraph(new File(graphDir, "intersectionConsistent." +
//                    minCount1 + "." + minCount2 + "." + penalty + ".txt"), intersectionConsistent);
            writeFiveGraphFormats(graphDir, intersectionConsistent, "intersectionConsistent." +
                    minCount1 + "." + minCount2 + "." + penalty + ".clip" + clip + ".txt");


            Map<String, Coord> map = loadMap();

            List<Edge> edges = intersectionConsistent.getEdges();
            Collections.sort(edges);

            for (Edge edge : edges) {
                Node node1 = edge.getNode1();
                Node node2 = edge.getNode2();

                String name1 = node1.getName();
                String name2 = node2.getName();

                Coord coord1 = map.get(name1);
                Coord coord2 = map.get(name2);

                boolean hub1 = intersectionConsistent.getAdjacentNodes(node1).size() >= 5;
                boolean hub2 = intersectionConsistent.getAdjacentNodes(node2).size() >= 5;

                Edge _edge = new Edge(new GraphNode(name1 + (hub1 ? "*" : "")), new GraphNode(name2 + (hub2 ? "*" : "")),
                        edge.getEndpoint1(), edge.getEndpoint2());

                double dx = coord1.getX() - coord2.getX();
                double dy = coord1.getY() - coord2.getY();
                double dz = coord1.getZ() - coord2.getZ();

                double d = sqrt(dx * dx + dy * dy + dz * dz);
                NumberFormat nf = new DecimalFormat("0.0");

                String side1 = coord1.getX() < 0 ? " (L)" : " (R)";
                String side2 = coord2.getX() < 0 ? " (L)" : " (R)";

                if (d > 50 && (hub1 || hub2)) {
                    System.out.println(_edge + " " + nf.format(d) + " " + coord1.getArea() + side1 + "---" + coord2.getArea() + side2);
                }
            }

//            Graph graph = GraphUtils.loadGraphTxt(new File(graphDir, "2014.01.15b/intersection56Consistent.1.0.txt"));

//            Graph dag = SearchGraphUtils.dagFromPattern(intersectionConsistent);
//
//            SemPm semPm = new SemPm(dag);
//            GeneralizedSemPm pm = new GeneralizedSemPm(semPm);
//            int numDataSets = dataSets.size();
//
//            List<SemEstimator> estimators = new ArrayList<SemEstimator>();
//
//            List<double[]> coefs = new ArrayList<double[]>();
//
//            List<String> parameters = new ArrayList<String>(pm.getParameters());
//            Collections.sort(parameters);
//
//            for (int i = 0; i < parameters.size(); i++) {
//                coefs.add(new double[numDataSets]);
//            }

//            int count = 0;
//
//            for (int i = 0; i < numDataSets; i++) {
//                DataSet dataSet = dataSets.get(i);
//                System.out.println(++count);
//
//                SemEstimator estimator = new SemEstimator(dataSet, semPm);
//                estimator.setSemOptimizer(new SemOptimizerRegression());
//                SemIm im = estimator.estimate();
//                GeneralizedSemIm _im = new GeneralizedSemIm(pm, im);
//
//                estimators.add(estimator);
//
//                for (int k = 0; k < parameters.size(); k++) {
//                    coefs.get(k)[i] = _im.getParameterValue(parameters.get(k));
//                }
//            }

//            NumberFormat nf = new DecimalFormat("0.0000");
//
//            for (int i = 0; i < parameters.size(); i++) {
//                String param = parameters.get(i);
//
//                if (!param.startsWith("B")) continue;
//
//                for (Parameter parameter1 : semPm.getParameters()) {
//                    if (parameter1.getName().equals(param)) {
//                        System.out.print(parameter1.getNodeA() + "-->" + parameter1.getNodeB() + ": ");
//                        break;
//                    }
//                }
//
//                double[] _coefs = coefs.get(i);
//                System.out.println("Coef = " + param + " mean = " + nf.format(StatUtils.mean(_coefs))
//                        + " SE = " + nf.format(StatUtils.sd(_coefs)));
//
//            }

//            System.out.println(pm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test14c9() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";

            List<DataSet> dataSets = loadDataSets();

            List<CovarianceMatrix> allCovMatrices = new ArrayList<CovarianceMatrix>();

            for (int m = 0; m < dataSets.size(); m++) {
                DataSet dataSet = dataSets.get(m);
                allCovMatrices.add(new CovarianceMatrix(dataSet));
            }

            List<Graph> graphs = new ArrayList<Graph>();

            for (int k = 1; k <= dataSets.size(); k++) {
//                if (k < 49) continue;

                double penaltyDiscount = 1.0;
                List<CovarianceMatrix> covarianceMatrixes = new ArrayList<CovarianceMatrix>();

                for (int m = 0; m < k; m++) {
                    covarianceMatrixes.add(allCovMatrices.get(m));
                }

                File file = new File(graphDir, "test" + penaltyDiscount + ".graph.images." + covarianceMatrixes.size() + ".txt");

                System.out.println(file.getAbsolutePath());
                Graph graph;

                if (file.exists()) {
                    graph = GraphUtils.loadGraphTxt(file);
                    writeFiveGraphFormats(graphDir, graph, file.getName());
                } else {
                    System.out.println("IMaGES");
                    Images images = Images.covarianceInstance(covarianceMatrixes);
                    images.setPenaltyDiscount(penaltyDiscount);
                    Graph imagesGraph = images.search();
                    System.out.println(imagesGraph);

                    writeGraph(file, imagesGraph);

                    graph = imagesGraph;
                    writeFiveGraphFormats(graphDir, graph, file.getName());
                }

                graphs.add(graph);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test14c10() {
        try {
            List<DataSet> dataSets = loadDataSets();

            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
            String tag = "images1";
            double penalty = 1.0;
            double clip = 0.5;

            for (int i = 0; i < dataSets.size(); i++) {
                List<DataSet> dataSet = Collections.singletonList(dataSets.get(i));

                Images ges = new Images(dataSet);
                ges.setPenaltyDiscount(penalty);
//                ges.setTrimAlpha(clip);
//                Ges ges = new Ges(dataSets.get(i));
//                ges.setPenaltyDiscount(penalty);
                Graph graph = ges.search();
                writeGraph(new File(graphDir, "graph." + tag + "." + penalty + "." + (i + 1) + ".txt"), graph);
            }

//            Images ges = new Images(dataSets);
//            ges.setPenaltyDiscount(penalty);
//            Graph graph = ges.search();
//
//            writeFiveGraphFormats(graphDir, graph, "images." + dataSets.size() + "." + penalty + ".clip" + clip + ".txt");
//            writeFiveGraphFormats(graphDir, graph, "graph.partition.images" + dataSets.size() + "." + penalty + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test14c10b() {
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
        String tag = "images1";
        double penalty = 1.0;

//        try {
//
//            List<DataSet> dataSets = loadDataSets();
//
//            for (int i = 1; i < 2; i++) {//dataSets.size(); i++) {
//                DataSet timeSeries = TimeSeriesUtils.createLagData(dataSets.get(i), 1);
//                List<DataSet> dataSet = Collections.singletonList(timeSeries);
//
//                Images images = new Images(dataSet);
//                images.setPenaltyDiscount(penalty);
//
//                Graph graph = images.search();
//
//                List<Node> concurrentNodes = new ArrayList<Node>();
//
//                for (Node node : timeSeries.getVariables()) {
//                    if (!(node.getName().contains(":"))) concurrentNodes.add(node);
//                }
//
//                Graph concurrentGraph = graph.subgraph(concurrentNodes);
//
//                writeGraph(new File(graphDir, "graph.concurrent" + tag + "." + penalty + "." + (i + 1) + ".txt"), concurrentGraph);
//            }

//            Images ges = new Images(dataSets);
//            ges.setPenaltyDiscount(penalty);
//            Graph graph = ges.search();
//
//            writeFiveGraphFormats(graphDir, graph, "images." + dataSets.size() + "." + penalty + ".clip" + clip + ".txt");
//            writeFiveGraphFormats(graphDir, graph, "graph.partition.images" + dataSets.size() + "." + penalty + ".txt");


//            for (int i = 0; i < 1; i++) {
//                File file = new File(graphDir, "graph.concurrent" + tag + "." + penalty + "." + (i + 1) + ".txt");
//                final Graph graph = GraphUtils.loadGraphTxt(file);
//                writeFiveGraphFormats(graphDir, graph, "graph.concurrent" + tag + "." + penalty + "." + (i + 1) + ".txt");
//
//                List<Node> nodes = graph.getNodes();
//
//                Collections.sort(nodes, new Comparator<Node>() {
//                    @Override
//                    public int compare(Node o1, Node o2) {
//                        return graph.getAdjacentNodes(o2).size() - graph.getAdjacentNodes(o1).size();
//                    }
//                });
//
//                for (Node node : nodes) {
//                    System.out.println(node + " " + graph.getAdjacentNodes(node).size());
//                }
//
//                List<Node> top = new ArrayList<Node>();
//
//                for (Node node : nodes) {
//                    if (graph.getAdjacentNodes(node).size() > 8) {
//                        top.add(node);
//                    }
//                }
//
//                System.out.println(graph.subgraph(top));
//
//                for (Node v : graph.getNodes()) {
//                    if (graph.getAdjacentNodes(v).size() >= 10) {
//                        System.out.println(v);
//                    }
//                }
//            }


//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        try {
//            List<DataSet> dataSets = loadDataSets();
//            double clip = 0.25;
//
//            List<DataSet> timeLagDataSets = new ArrayList<DataSet>();
//
//            for (DataSet d : dataSets) {
//                DataSet timeSeries = TimeSeriesUtils.createLagData(d, 1);
//                timeLagDataSets.add(timeSeries);
//            }
//
//            Images images = new Images(timeLagDataSets);
//            images.setPenaltyDiscount(penalty);
//            images.setTrimAlpha(clip);
//
//            Graph graph2 = images.search();
//
//            List<Node> concurrentNodes = new ArrayList<Node>();
//
//            for (Node node : graph2.getNodes()) {
//                if (!(node.getName().contains(":"))) concurrentNodes.add(node);
//            }
//
//            Graph concurrentGraph = graph2.subgraph(concurrentNodes);
//
//            writeGraph(new File(graphDir, "graph.concurrent.66." + penalty + ".clip" + clip + ".txt"), concurrentGraph);


        File file = new File(graphDir, "graph.concurrent.67." + penalty + ".clip" + 0.0 + ".txt");
        final Graph graph2 = GraphUtils.loadGraphTxt(file);
//            writeFiveGraphFormats(graphDir, graph, "graph.concurrent.66." + penalty + ".clip" + clip + ".txt");

        List<Node> nodes = graph2.getNodes();

        Collections.sort(nodes, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return graph2.getAdjacentNodes(o2).size() - graph2.getAdjacentNodes(o1).size();
            }
        });

        for (Node node : nodes) {
            System.out.println(node + " " + graph2.getAdjacentNodes(node).size());
        }

        List<Node> top = new ArrayList<Node>();

        for (Node node : nodes) {
            if (graph2.getAdjacentNodes(node).size() > 8) {
                top.add(node);
            }
        }

        System.out.println(graph2.subgraph(top));

        for (Node v : graph2.getNodes()) {
            if (graph2.getAdjacentNodes(v).size() >= 10) {
                System.out.println(v);
            }
        }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void test14c10c() {
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
        String tag = "images1";
        double penalty = 1.0;

        List<Edge> previousIntersection = new ArrayList<Edge>();

        List<Edge> intersection2Back = new ArrayList<Edge>();

        Set<Edge> unionIntersection = new HashSet<Edge>();
        List<Edge> intersectionIntersection = new ArrayList<Edge>();

        for (int i = 0; i < 67; i++) {
            File file1 = new File(graphDir, "graph." + tag + "." + penalty + "." + (i + 1) + ".txt");
            final Graph graph1 = GraphUtils.loadGraphTxt(file1);

//            File file2 = new File(graphDir, "graph.concurrent.67." + penalty + ".clip" + 0.0 + ".txt");
            File file2 = new File(graphDir, "images.67.1.0.clip0.5.txt");
//            File file2 = new File(graphDir, "graph." + tag + "." + penalty + "." + (i + 2) + ".txt");
            final Graph graph2 = GraphUtils.loadGraphTxt(file2);

//            List<Edge> edges1 = graph1.getEdges();
//            List<Edge> edges2 = graph2.getEdges();

            List<Edge> edges1 = new ArrayList<Edge>();

            for (Edge edge : graph1.getEdges()) {
                edges1.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
            }

            List<Edge> edges2 = new ArrayList<Edge>();

            for (Edge edge : graph2.getEdges()) {
                edges2.add(Edges.undirectedEdge(edge.getNode1(), edge.getNode2()));
            }

            List<Edge> newEdges = new ArrayList<Edge>(edges2);
            newEdges.removeAll(edges1);

            List<Edge> oldEdges = new ArrayList<Edge>(edges1);
            oldEdges.removeAll(edges2);

            List<Edge> intersection = new ArrayList<Edge>(edges1);
            intersection.retainAll(edges2);

            unionIntersection.addAll(intersection);

            if (intersectionIntersection.isEmpty()) {
                intersectionIntersection.addAll(intersection);
            } else {
                intersectionIntersection.retainAll(intersection);
            }

            int delta = intersection.size() - previousIntersection.size();

            List<Edge> gained = new ArrayList<Edge>(intersection);
            gained.removeAll(previousIntersection);

            List<Edge> lost = new ArrayList<Edge>(previousIntersection);
            lost.removeAll(intersection);

            System.out.println("Graph " + (i + 2) + " # new in images 67 = " + newEdges.size()
                            + " # intersection = " + intersection.size()
                            + " Gained = " + gained.size() + " Lost = " + lost.size()
                   /* + " " + (intersection.size() - intersection2Back.size())*/
                            + " Interesection intersection # = " + intersectionIntersection.size()
            );

//            intersection2Back = new ArrayList<Edge>(previousIntersection);

            previousIntersection = new ArrayList<Edge>(intersection);

        }

        System.out.println("Union intersection # = " + unionIntersection.size());
        System.out.println("Intersection intersection # = " + intersectionIntersection.size());

        for (Edge edge : unionIntersection) {
            int count = 0;

            for (int i = 0; i < 67; i++) {
                File file1 = new File(graphDir, "graph." + tag + "." + penalty + "." + (i + 1) + ".txt");
                Graph graph1 = GraphUtils.loadGraphTxt(file1);


                Node node1 = edge.getNode1();
                Node node2 = edge.getNode2();

                node1 = graph1.getNode(node1.getName());
                node2 = graph1.getNode(node2.getName());

                if (graph1.isAdjacentTo(node1, node2)) {
                    count++;
                }
            }

            System.out.println("Edge " + edge + " count = " + count);
        }
    }

    public void test14c17() {
        try {
            int numGraphs = 67;
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
            String tag = numGraphs + "individually.d";
            double penalty = 1.0;
            double ratio = .5;
            int skip = 0;

            List<Graph> graphsUndirected = new ArrayList<Graph>();
            List<Graph> graphsDirected = new ArrayList<Graph>();

            for (int i = 0; i < numGraphs - skip; i++) {
                File file = new File(graphDir, "final/graph.partition." + tag + "." + penalty + "." + (i + skip + 1) + ".txt");
                Graph graph = GraphUtils.loadGraphTxt(file);

                if (i > 0) {
                    graph = GraphUtils.replaceNodes(graph, graphsUndirected.get(0).getNodes());
                }

                graphsDirected.add(graph);
                graph = GraphUtils.undirectedGraph(graph);
                graphsUndirected.add(graph);
            }

            Set<Edge> edges = new HashSet<Edge>(graphsUndirected.get(0).getEdges());

            for (int i = 1; i < graphsUndirected.size(); i++) {
                edges.addAll(graphsUndirected.get(i).getEdges());
            }

            int c = 0;
            Graph topEdges = new EdgeListGraph(graphsUndirected.get(0).getNodes());

            for (Edge edge : edges) {
                int count = 0;

                for (Graph graph : graphsUndirected) {
                    if (graph.containsEdge(edge)) count++;
                }

                if (count / (double) graphsUndirected.size() >= ratio) {
                    System.out.println((++c) + ". " + edge + " " + count);
                    topEdges.addEdge(edge);
                }
            }

            writeFiveGraphFormats(graphDir, topEdges, "topEdges.txt");
            Graph consistent = new EdgeListGraph(topEdges.getNodes());

            EDGE:
            for (Edge edge : topEdges.getEdges()) {
                Node node1 = edge.getNode1();
                Node node2 = edge.getNode2();

                Edge edge1 = Edges.directedEdge(node1, node2);
                Edge edge2 = Edges.directedEdge(node2, node1);

                int count1 = 0;
                int count2 = 0;

                for (Graph graph : graphsDirected) {
                    Edge _edge = graph.getEdge(node1, node2);
                    if (_edge == null) continue;

                    if (_edge.equals(edge1)) {
                        count1++;
                    } else if (_edge.equals(edge2)) {
                        count2++;
                    }
                }

                if (count1 + count2 > 0) {
                    if (count2 == 0) {
//                    if (count1 > (double) count2) {
                        consistent.removeEdge(edge);
                        consistent.addEdge(edge1);
                    } else if (count1 == 0) {
//                    else if (count2 > (double) count1) {
                        consistent.removeEdge(edge);
                        consistent.addEdge(edge2);
                    } else {
                        consistent.addUndirectedEdge(node1, node2);
                    }
                } else {
                    consistent.addUndirectedEdge(node1, node2);
                }
            }

            writeFiveGraphFormats(graphDir, consistent, "topGraphConsistent." + penalty + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void test14c18() {
        int numGraphs = 67;
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
        String tag = numGraphs + "individually.c";
        double penalty = 1.0;

        List<Graph> gesUndirected = new ArrayList<Graph>();
        List<Graph> gesDirected = new ArrayList<Graph>();

        for (int i = 0; i < numGraphs; i++) {
            File file = new File(graphDir, "ges." + numGraphs + "/graph.partition." + tag + "." + penalty + "." + (i + 1) + ".txt");
            Graph graph = GraphUtils.loadGraphTxt(file);

            if (i > 0) {
                graph = GraphUtils.replaceNodes(graph, gesUndirected.get(0).getNodes());
            }

            gesDirected.add(graph);
            graph = GraphUtils.undirectedGraph(graph);
            gesUndirected.add(graph);
        }

        Graph referenceGraph = GraphUtils.loadGraphTxt(new File(graphDir, "dof2/intersectionConsistent." + penalty + ".txt"));
        referenceGraph = GraphUtils.replaceNodes(referenceGraph, gesUndirected.get(0).getNodes());
        int index = 0;

        for (Edge edge : referenceGraph.getEdges()) {
//            edge = Edges.undirectedEdge(edge.getNode1(), edge.getNode2());

            edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(), edge.getEndpoint1());

            int count = 0;

            for (Graph gesGraph : gesDirected) {
                if (gesGraph.containsEdge(edge)) count++;
            }

            System.out.println((++index) + ". " + edge + " " + count);
        }

    }

    private Map<String, Coord> loadMap() {
        Map<String, Coord> map = new HashMap<String, Coord>();

        try {
            File file = new File("/Users/josephramsey/Documents/proj/tetrad2/docs/notes/extended_power_labels_283.txt");

            BufferedReader in = new BufferedReader(new FileReader(file));

            String line;

            while ((line = in.readLine()) != null) {
//                System.out.println(line);

                String[] tokens = line.split("\t");

                String var = "X" + tokens[0];
                int index = Integer.parseInt(tokens[0]);
                int x = Integer.parseInt(tokens[1]);
                int y = Integer.parseInt(tokens[2]);
                int z = Integer.parseInt(tokens[3]);
                String area = tokens[4];

                map.put(var, new Coord(index, x, y, z, area));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return map;
    }

    private class Coord {
        private int index;
        private int x;
        private int y;
        private int z;
        private String area;

        public Coord(int index, int x, int y, int z, String area) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.z = z;
            this.area = area;
        }

        public int getIndex() {
            return index;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        private String getArea() {
            return area;
        }
    }


    private List<DataSet> loadDataSets() throws IOException {
        return loadRussHeadData();
//        return loadDanaData();
    }

    private List<DataSet> loadRussHeadData() throws IOException {
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";
        File _graphDir = new File(graphDir);

        if (!_graphDir.exists()) _graphDir.mkdir();

        String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata_for_joe/";
//            String _dir = "/home/jdramsey/roidata/";
        File dir = new File(_dir);

        File[] files = dir.listFiles();

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

    public void test14c4() {
        try {
            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
//            String graphDir = "/home/jdramsey/test14/";

            List<DataSet> dataSets = loadDataSets();

            double penalty = 1.0;

            // Partition the data sets into groups of 10 and run IMaGES on each.

            String tag = "test4";

            for (int partition = 1; partition <= 6; partition++) {
                List<DataSet> _dataSets = partitionDataSets(dataSets, partition, 67, 10);

                Images images = new Images(_dataSets);
                images.setPenaltyDiscount(penalty);
                Graph graph = images.search();
                writeFiveGraphFormats(graphDir, graph, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
            }

            List<Graph> graphs = new ArrayList<Graph>();

            Lofs2.Rule rule = null; //Lofs2.Rule.R3;

            File file0 = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + 1 + ".txt");
            Graph referenceGraph = GraphUtils.loadGraphTxt(file0);

            List<Node> nodes = referenceGraph.getNodes();

            for (int partition = 1; partition <= 6; partition++) {

                File file = new File(graphDir, "graph.partition." + tag + "." + penalty + "." + partition + ".txt");
                Graph graph = GraphUtils.loadGraphTxt(file);

                graph = GraphUtils.replaceNodes(graph, nodes);

                if (rule != null) {
                    Lofs2 lofs = new Lofs2(graph, partitionDataSets(dataSets, partition, 67, 10));
                    lofs.setRule(rule);
                    lofs.setAlpha(1.0);
                    graph = lofs.orient();
                }

                graph = GraphUtils.replaceNodes(graph, nodes);

//                graphs1.add(GraphUtils.undirectedGraph(graph));
                graphs.add(graph);

                System.out.println(graph);

            }

            Graph all = new EdgeListGraph(nodes);

            Set<Edge> edges = new HashSet<Edge>();

            Map<Edge, Integer> edgeIntegerMap = new HashMap<Edge, Integer>();

            for (int i = 0; i < 6; i++) {
                edges.addAll(graphs.get(i).getEdges());
                for (Edge edge : graphs.get(i).getEdges()) {
                    increment(edgeIntegerMap, edge);
                }
            }

            for (Edge edge : edges) {
                if (edgeIntegerMap.get(edge) >= 2) {
                    all.addEdge(edge);
                }
            }

            int numConflicts = 0;
            int numDirectedEdges = 0;

            for (Edge edge : all.getEdges()) {
                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
                        edge.getEndpoint1());
                if (edgeIntegerMap.containsKey(edge) && edgeIntegerMap.containsKey(_edge)) {
                    numConflicts++;
                }

                numDirectedEdges++;
            }

            numConflicts /= 2;

//            for (Edge edge : new HashSet<Edge>(edges)) {
//                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
//                        edge.getEndpoint1());
//                if (edges.contains(_edge)) {
//                     if (Edges.isDirectedEdge(edge) && Edges.isDirectedEdge(_edge)) {
//                        numConflicts++;
//                    }
//
//                     edges.remove(edge);
//                    edges.remove(_edge);
//                }
//
//                if (Edges.isDirectedEdge(edge)) {
//                    numDirectedEdges++;
//                }
//
//            }

            numDirectedEdges -= numConflicts;

            System.out.println("Num conflicts = " + numConflicts);
            System.out.println("Num directed adjacencies = " + numDirectedEdges);

            Graph consistent = new EdgeListGraph(graphs.get(0).getNodes());

            for (Edge edge : all.getEdges()) {
                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint2(),
                        edge.getEndpoint1());
                if (edge.isDirected() && !edges.contains(_edge)) {
                    consistent.addEdge(edge);
                }
            }

            for (Node node : consistent.getNodes()) {
                if (consistent.getEdges(node).isEmpty()) {
                    consistent.removeNode(node);
                }
            }

            System.out.println(consistent);

            if (rule == null) {
                writeFiveGraphFormats(graphDir, consistent, "graph.partition." + "images" + "." + penalty + ".consistent.txt");
            } else {
                writeFiveGraphFormats(graphDir, consistent, "graph.partition." + rule + "." + penalty + ".consistent.txt");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test14d() {
        String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";

        List<Graph> graphs1 = new ArrayList<Graph>();

        for (int partition = 1; partition <= 6; partition++) {
            File file = new File(graphDir, "graph.partition." + partition + ".txt");
            Graph graph = GraphUtils.loadGraphTxt(file);

            if (!graphs1.isEmpty()) {
                graph = GraphUtils.replaceNodes(graph, graphs1.get(0).getNodes());
            }

            graphs1.add(graph);
        }

        Graph topEdges = new EdgeListGraph(graphs1.get(0).getNodes());

        Set<Edge> edges = new HashSet<Edge>(graphs1.get(0).getEdges());

        for (int i = 1; i < 6; i++) {
            edges.addAll(graphs1.get(i).getEdges());
        }

        for (Edge edge : edges) {
            int i = 0;

            for (Graph graph : graphs1) {
                if (graph.containsEdge(edge)) {
                    i++;
                }
            }

            if (i >= 5) {
                System.out.println(edge);
                topEdges.addEdge(edge);
            }
        }

        try {
            writeGraph(new File(graphDir, "graph.top.edges.txt"), topEdges);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void testTemp() {
        int numVars = 100;
        int sampleSize = 1000;
        List<Node> vars = new ArrayList<Node>();
        for (int i = 0; i < numVars; i++) {
            vars.add(new ContinuousVariable("X" + i));
        }

        DataSet data = new ColtDataSet(sampleSize, vars);

        for (int j = 0; j < numVars; j++) {
            for (int i = 0; i < sampleSize; i++) {
                data.setDouble(i, j, RandomUtil.getInstance().nextNormal(0, 1.5));
            }
        }

        CorrelationMatrix cov = new CorrelationMatrix(data);

//        System.out.println(cov);

        NumberFormat nf = new DecimalFormat("0.0000");

        for (int i = 0; i < cov.getDimension(); i++) {
            for (int j = 0; j < cov.getDimension(); j++) {
                if (i == j) continue;
                double r = cov.getValue(i, j);

                double fisherZ = Math.sqrt(sampleSize - 3.0) *
                        0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

                double pValue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));

                if (pValue < 0.001) {
                    System.out.println("(" + i + ", " + j + ") r = " + nf.format(r) + " p = " + nf.format(pValue));
                }
            }
        }

//        IndependenceTest test = new IndTestFisherZ(cov, 0.001);
//
//        Pc pc = new Pc(test);
//        Graph graph = pc.search();
//
//        System.out.println(graph);
    }

    public void test14e() {
        try {
            int numGraphs = 67;

            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";

            double penalty = 1.0;

            List<Graph> graphs = new ArrayList<Graph>();

            for (int i = 0; i < numGraphs; i++) {

                String tag = "images1";

//                File file = new File(graphDir, "graph.ges." + penalty + "." + (i + 1) + ".txt");
                File file = new File(graphDir, "graph." + tag + "." + penalty + "." + (i + 1) + ".txt");
                Graph _graph = GraphUtils.loadGraphTxt(file);

                if (!graphs.isEmpty()) {
                    _graph = GraphUtils.replaceNodes(_graph, graphs.get(0).getNodes());
                }

                graphs.add(GraphUtils.undirectedGraph(_graph));
            }

            Graph union = new EdgeListGraph(graphs.get(0).getNodes());
            final Map<Edge, Integer> counts = new HashMap<Edge, Integer>();
            List<Edge> intersectionEdges = new ArrayList<Edge>(graphs.get(0).getEdges());
            Set<Edge> unionEdges = new HashSet<Edge>();

            for (int i = 0; i < numGraphs; i++) {
                List<Edge> _edges = graphs.get(i).getEdges();
                intersectionEdges.retainAll(_edges);
                unionEdges.addAll(_edges);
//                System.out.println(graphs.get(i).getEdges());

                for (Edge edge : _edges) {
                    increment(counts, edge);
                }

//                writeGraph(new File(graphDir, "graph.ges." + penalty + "." + "union.txt"), union);
            }

            List<Edge> _edges = new ArrayList<Edge>(counts.keySet());
            Collections.sort(_edges, new Comparator<Edge>() {
                @Override
                public int compare(Edge o1, Edge o2) {
                    return counts.get(o2) - counts.get(o1);
                }
            });

            Graph topEdges = new EdgeListGraph(graphs.get(0).getNodes());
            double percent = 0;

            for (Edge edge : _edges) {
                if (counts.get(edge) >= percent * graphs.size()) {
//                    union.addEdge(edge);
                    System.out.println(edge + " " + counts.get(edge));
                    topEdges.addEdge(edge);
                }
            }

            writeFiveGraphFormats(graphDir, topEdges, "topEdgesGes" + graphs.size() + "." + penalty + "." + percent + ".txt");

//            System.out.println("INTERSECTION" + union);

//            for (Edge edge : counts.keySet()) {
//                System.out.println("Count for " + edge + " = " + counts.get(edge));
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testTemp2() {
        for (int i = 0; i < 1; i++) {
            Graph graph = GraphUtils.randomDag(100, 100, false);
            Graph pattern = SearchGraphUtils.patternForDag(graph);

            LargeSemSimulator sim = new LargeSemSimulator(graph);
            sim.setCoefRange(.05, 1.5);
            sim.setVarRange(1, 3);
            DataSet dataSet = sim.simulateDataAcyclic(1000);

            Ges ges = new Ges(dataSet);
            ges.setLog(true);
            ges.setStoreGraphs(false);
            ges.setTrueGraph(graph);
            Graph out = ges.search();

            GraphUtils.GraphComparison comparison = GraphUtils.getGraphComparison(out, pattern);
            System.out.println("Adj Cor = " + comparison.getAdjCorrect());
            System.out.println("Adj FP = " + comparison.getAdjFp());
            System.out.println("Adj FN = " + comparison.getAdjFn());
            System.out.println("Arrow Cor = " + comparison.getArrowptCorrect());
            System.out.println("Arrow FP = " + comparison.getArrowptFp());
            System.out.println("Arrow FN = " + comparison.getArrowptFn());
        }

    }

    private void writeGraph(File file, Graph topEdges) throws FileNotFoundException {
        PrintWriter out6 = new PrintWriter(file);
        out6.println(topEdges);
        out6.close();
    }

    public void test15() {
        try {
            DataReader reader = new DataReader();
            reader.setVariablesSupplied(false);
            reader.setDelimiter(DelimiterType.COMMA);

            File file2 = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test12_3/graph.tetrad.2000.1.txt");
            Graph truePattern = GraphUtils.loadGraphTxt(file2);

            File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test12_3/kcigraph.2000.1.txt");
            DataSet dataSet = reader.parseTabular(file);
            Graph pattern = readPatternAsMatrix(truePattern.getNodes(), dataSet);
            System.out.println(pattern);

            int adjFn = adjacenciesComplement(truePattern, pattern);
            int adjFp = adjacenciesComplement(pattern, truePattern);
            int truePosAdj = truePositivesAdj(truePattern, pattern);

            System.out.println("AdjFn = " + adjFn);
            System.out.println("AdjFp = " + adjFp);
            System.out.println("TruePosAdj = " + truePosAdj);

            double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
            double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);

            System.out.println("adjPrecision = " + adjPrecision);
            System.out.println("adjRecall = " + adjRecall);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test16() {

        try {
//            PrintStream out = new PrintStream("/Users/josephramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test11out.txt");
//            PrintStream out = new PrintStream("/home/jdramsey/test10out.txt");
            PrintStream out = System.out;

            String _dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test12final/";
//            String _dir = "/home/jdramsey/test12final/";
            File dir = new File(_dir);

            if (!dir.exists()) dir.mkdir();

            int numRuns = 20;
            double alpha = 0.01;
            double gamma = 0.01;
            int numModels = 14;

//            This should be done only once.
            for (int model = 1; model <= numModels; model++) {

                System.out.println("================= MODEL " + model + " =================");

                int numVariables = 5;
                int N = 1000;

                for (int run = 1; run <= numRuns; run++) {
                    File file = new File(dir, "data." + model + "." + run + ".txt");

                    if (file.exists()) continue;

                    GeneralizedSemIm im = makeTestIm4(numVariables, model);

                    DataSet data = im.simulateData(N, false);

                    PrintWriter out1 = new PrintWriter(file);
                    DataWriter.writeRectangularData(data, out1, '\t');

                    File file2 = new File(dir, "graph." + model + "." + run + ".txt");
                    File file3 = new File(dir, "graph.tetrad." + model + "." + run + ".txt");
                    File file6 = new File(dir, "dag.tetrad." + model + "." + run + ".txt");
                    File file7 = new File(dir, "model." + model + "." + run + ".txt");

                    SemGraph dag = im.getSemPm().getGraph();
                    dag.setShowErrorTerms(false);
                    Graph _dag = GraphUtils.replaceNodes(dag, data.getVariables());
                    Graph truePattern = SearchGraphUtils.patternForDag(_dag);

                    PrintWriter out2 = new PrintWriter(file2);
                    PrintWriter out3 = new PrintWriter(file3);
                    PrintWriter out6 = new PrintWriter(file6);
                    PrintWriter out7 = new PrintWriter(file7);

                    writePatternAsMatrix(data.getVariables(), truePattern, out2);

                    out3.println(truePattern.toString());
                    out6.println(dag.toString());

                    out7.println(im);

                    out1.close();
                    out2.close();
                    out3.close();
                    out6.close();
                    out7.close();
                }
            }

            double[][] stats = new double[14][8];


            for (int model = 1; model <= 14; model++) {
                System.out.println("MODEL " + model);

                NumberFormat nf = new DecimalFormat("0.00");

                String[] indTestTypes = new String[]{"fisherz", "drton", "kci", "cci"};
//                String[] indTestTypes = new String[]{"fisherz", "drton"};

//                String indTestType = "fisherz";
//                String indTestType = "cci";
//                    String indTestType = "kci";
//                    String indTestType = "drton";
                for (int type = 0; type < indTestTypes.length; type++) {
//                for (String indTestType : indTestTypes) {
                    String indTestType = indTestTypes[type];
                    double sumAP = 0.0;
                    double sumAR = 0.0;

                    double sumEP = 0.0;
                    double sumER = 0.0;

                    int sumErrors = 0;

                    int sumAPN = 0;
                    int sumARN = 0;

                    int sumEPN = 0;
                    int sumERN = 0;

                    for (int run = 1; run <= numRuns; run++) {
                        System.out.println("\nRun " + run);

                        File file4 = new File(dir, "pattern." + indTestType + "." + model + "." + run + ".txt");
                        Graph pattern;

                        File file3 = new File(dir, "graph.tetrad." + model + "." + run + ".txt");
                        Graph truePattern = GraphUtils.loadGraphTxt(file3);

                        if (!file4.exists()) {
                            File file = new File(dir, "data." + model + "." + run + ".txt");

                            DataReader reader = new DataReader();
                            reader.setVariablesSupplied(true);
                            reader.setDelimiter(DelimiterType.WHITESPACE);

                            DataSet dataSet = reader.parseTabular(file);
//                            long start2 = System.currentTimeMillis();

                            double cutoff = indTestType.equals("drton") ? gamma : alpha;

                            Cpc pc = new Cpc(getIndependenceTest(indTestType, dataSet, cutoff));
                            pattern = pc.search();
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);
//                            long stop2 = System.currentTimeMillis();
//                            System.out.println("Elapsed (just " + indTestType + ") " + (stop2 - start2) / 1000L + " seconds");

                            PrintWriter out4 = new PrintWriter(file4);
                            out4.println(pattern);
                            out4.close();

                        } else {
                            pattern = GraphUtils.loadGraphTxt(file4);
                        }

                        System.out.println("True pattern = " + truePattern);
                        System.out.println("Pattern = " + pattern);

                        pattern = GraphUtils.replaceNodes(pattern, truePattern.getNodes());
//                        pattern = GraphUtils.bidirectedToUndirected(pattern);

                        int adjFn = adjacenciesComplement(truePattern, pattern);
                        int adjFp = adjacenciesComplement(pattern, truePattern);
                        int truePosAdj = truePositivesAdj(truePattern, pattern);

                        System.out.println("AdjFn = " + adjFn);
                        System.out.println("AdjFp = " + adjFp);
                        System.out.println("TruePosAdj = " + truePosAdj);

                        int edgeFn = edgesComplement2(truePattern, pattern);
                        int edgeFp = edgesComplement2(pattern, truePattern);
                        int truePosEdges = truePositiveEdges(truePattern, pattern);

//                        int edgeFn = arrowsComplement(truePattern, pattern);
//                        int edgeFp = arrowsComplement(pattern, truePattern);
//                        int truePosEdges = truePositiveArrows(truePattern, pattern);

                        double adjPrecision = truePosAdj / (double) (truePosAdj + adjFp);
                        double adjRecall = truePosAdj / (double) (truePosAdj + adjFn);


                        double edgePrecision = truePosEdges / (double) (truePosEdges + edgeFp);
                        double edgeRecall = truePosEdges / (double) (truePosEdges + edgeFn);

                        System.out.println("edge Precision = " + edgePrecision);
                        System.out.println("edge Recall = " + edgeRecall);

                        sumErrors += adjFn + adjFp;

                        if (!Double.isNaN(adjPrecision)) {
                            sumAP += adjPrecision;
                            sumAPN++;
                        }

                        if (!Double.isNaN(adjRecall)) {
                            sumAR += adjRecall;
                            sumARN++;
                        }

                        if (!Double.isNaN(edgePrecision)) {
                            sumEP += edgePrecision;
                            sumEPN++;
                        }

                        if (!Double.isNaN(edgeRecall)) {
                            sumER += edgeRecall;
                            sumERN++;
                        }

//                    out.println("Model # " + modelIndex + " AP (CCI) = " + adjPrecision);
//                    out.println("Model # " + modelIndex + " AR (CCI) = " + adjRecall);

                    }

                    out.println("\nAverages " + indTestType);
                    out.println("Model # " + model + " Average AP = " + nf.format(sumAP / sumAPN));
                    out.println("Model # " + model + " Average AR = " + nf.format(sumAR / sumARN));
                    out.println("Model # " + model + " Average EP = " + nf.format(sumEP / sumEPN));
                    out.println("Model # " + model + " Average ER = " + nf.format(sumER / sumERN));
                    out.println("Model # " + model + " Average Adj Errors = " + nf.format(sumErrors / (double) numModels));

                    stats[model - 1][type * 2] = sumEP / sumEPN;
                    stats[model - 1][type * 2 + 1] = sumER / sumERN;
                }
            }

            System.out.println(MatrixUtils.toString(stats));

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void test17() {
        int n = 100;
        int numTrials = 1000;
        double alpha = 0.05;

        int numIndep = 0;

        for (int g = 0; g < numTrials; g++) {
            TetradMatrix m = new TetradMatrix(n, 2);

            m.set(0, 0, 0);
            m.set(0, 1, 0);
            double sd = 1.0;

            for (int i = 1; i < n; i++) {
//                m.set(i, 0, RandomUtil.getInstance().nextNormal(0, sd));
//                m.set(i, 1, RandomUtil.getInstance().nextNormal(0, sd));
                m.set(i, 0, .9 * m.get(i - 1, 0) + RandomUtil.getInstance().nextNormal(0, sd));
                m.set(i, 1, .8 * m.get(i - 1, 1) + RandomUtil.getInstance().nextNormal(0, sd));
            }

            double x1S = 0., x2S = 0.;

            for (int i = 0; i < n; i++) {
                x1S += m.get(n - i - 1, 0);
                x2S += m.get(n - i - 1, 1);
            }

//            double x1s2 = (1. / (double) n) * x1S * x1S;
//            double x2s2 = (1. / (double) n) * x2S * x2S;
//
            double x1s2 = m.get(n - 0 - 1, 0);
            double x2s2 = m.get(n - 0 - 1, 0);

            for (int i = 1; i < n; i++) {
                x1s2 += 2 * m.get(n - 0 - 1, 0) * m.get(n - i - 1, 0);
                x2s2 += 2 * m.get(n - 0 - 1, 1) * m.get(n - i - 1, 1);
            }

            x1s2 = abs(x1s2);
            x2s2 = abs(x2s2);

            double x1s = sqrt(x1s2);
            double x2s = sqrt(x2s2);
            double x1z = x1S / (x1s * sqrt(n));
//            double x2z = x2S / (sqrt(x2s2) * sqrt(n));

//            System.out.println("x1S = " + x2S + " x1s2 = " + x1s2 + " + x1s = " + x1s +  " x1z " + x1z);

//        System.out.println(m);

            List<Node> nodes = new ArrayList<Node>();
            ContinuousVariable x1 = new ContinuousVariable("X1");
            ContinuousVariable x2 = new ContinuousVariable("X2");
            nodes.add(x1);
            nodes.add(x2);

//            System.out.println(m);
//            for (int i = 1; i < n; i++) {
//                m.set(n - i - 1, 0, m.get(n - i - 1, 0) * x1s);
//                m.set(n - i - 1, 1, m.get(n - i - 1, 1) * x2s);
//            }


            DataSet d = ColtDataSet.makeContinuousData(nodes, m);

            IndependenceTest ind = new IndTestFisherZ(d, alpha);

            boolean indep = ind.isIndependent(x1, x2, new ArrayList<Node>());

            System.out.println(indep + " " + ind.getPValue());

            if (indep) numIndep++;
        }

        System.out.println(numIndep / (double) numTrials);
    }

    private void increment(Map<Edge, Integer> edges, Edge edge) {
        if (!edges.containsKey(edge)) {
            edges.put(edge, 0);
        }

        edges.put(edge, edges.get(edge) + 1);
    }

    private IndependenceTest getIndependenceTest(String type, DataSet dataSet, double alpha) {
        if ("fisherz".equals(type)) {
            return new IndTestFisherZ(dataSet, alpha);
        } else if ("cci".equals(type)) {
            return new IndTestConditionalCorrelation(dataSet, alpha);
        } else if ("kci".equals(type)) {
            return new IndTestKciMatlab(dataSet, alpha);
        } else if ("drton".equals(type)) {
            return new IndTestDrton(dataSet, alpha);
        }

        throw new IllegalArgumentException("Unrecognized type: " + type);
    }


    private int truePositivesAdj(Graph gTrue, Graph g2) {
        int n = 0;

        g2 = GraphUtils.replaceNodes(g2, gTrue.getNodes());

        List<Node> nodes = gTrue.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (gTrue.isAdjacentTo(nodes.get(i), nodes.get(j)) && g2.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                    n++;
                }
            }
        }

        return n;
    }

    private int truePositiveEdges(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        int n = 0;

        for (Edge edge : graph1.getEdges()) {
            if (graph2.containsEdge(edge)) n++;
        }

        return n;
    }

    private int truePositiveEdges2(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        int n = 0;

        for (Edge edge : graph1.getEdges()) {
            if (edge.isDirected() && graph2.containsEdge(edge)) n++;
        }

        return n;
    }

    private int truePositiveArrows(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        List<Node> nodes = graph1.getNodes();
        int n = 0;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                if (graph1.getEndpoint(nodes.get(i), nodes.get(j)) == Endpoint.ARROW
                        && graph2.getEndpoint(nodes.get(i), nodes.get(j)) == Endpoint.ARROW) {
                    n++;
                }
            }
        }


//        for (Edge edge : graph1.getEdges()) {
//            if (graph2.containsEdge(edge)) n++;
//        }

        return n;
    }

    public static int adjacenciesComplement(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        int n = 0;

        List<Node> nodes = graph1.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (graph1.isAdjacentTo(nodes.get(i), nodes.get(j)) && !graph2.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                    n++;
                }
            }
        }

        return n;
    }

    public static int edgesComplement(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        int n = 0;

        for (Edge edge : graph1.getEdges()) {
            if (!graph2.containsEdge(edge)) {
                n++;
            }
        }

        return n;
    }

    public static int edgesComplement2(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        int n = 0;

        for (Edge edge : graph1.getEdges()) {
            if (edge.isDirected() && !graph2.containsEdge(edge)) {
                n++;
            }
        }

        return n;
    }


    public static int arrowsComplement(Graph graph1, Graph graph2) {
        graph2 = GraphUtils.replaceNodes(graph2, graph1.getNodes());

        List<Node> nodes = graph1.getNodes();
        int n = 0;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                if (graph1.getEndpoint(nodes.get(i), nodes.get(j)) == Endpoint.ARROW
                        && graph2.getEndpoint(nodes.get(i), nodes.get(j)) != Endpoint.ARROW) {
                    n++;
                }
            }
        }

//        int n = 0;
//
//        for (Edge edge : graph1.getEdges()) {
//            if (!graph2.containsEdge(edge)) {
//                n++;
//            }
//        }

        return n;
    }

    private GeneralizedSemIm makeTestIm4(int size, int modelIndex) {
        if (modelIndex < 1 || modelIndex > 14) {
            throw new IllegalArgumentException("Model index out of range: " + modelIndex);
        }

//        List<Node> nodes = new ArrayList<Node>();
//
//        for (int i = 0; i < size; i++) {
//            nodes.add(new GraphNode("X" + (i + 1)));
//        }
//
//        Graph graph = new EdgeListGraph(nodes);
//
//        for (int i = 0; i < nodes.size(); i++) {
//            for (int j = i + 1; j < nodes.size(); j++) {
//                if (RandomUtil.getInstance().nextDouble() < 0.8) {
//                    graph.addDirectedEdge(nodes.get(i), nodes.get(j));
//                }
//            }
//        }

        Graph graph = GraphUtils.randomDag(size, size, false);

//        Graph graph;
//
//        do {
//            graph = GraphUtils.randomDag(size, size, false);
//        } while (graph.getNumEdges() < size);

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);
        List<Node> variablesNodes = pm.getVariableNodes();
        List<Node> errorNodes = pm.getErrorNodes();

        try {
            for (Node node : variablesNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate(getConnectionFunction(modelIndex), pm, node);
                pm.setNodeExpression(node, _template);
            }

            for (Node node : errorNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate(getErrorDistribution(), pm, node);
                pm.setNodeExpression(node, _template);
            }

            Set<String> parameters = pm.getParameters();

            for (String parameter : parameters) {
                if (parameter.startsWith("a")) {
                    pm.setParameterExpression(parameter, getCoefDistribution());
                }
            }
        } catch (ParseException e) {
            System.out.println(e);
        }

        GeneralizedSemIm im = new GeneralizedSemIm(pm);

        return im;
    }

    private String getConnectionFunction(int modelIndex) {
        int[] mixture = new int[]{1, 2, 3, 4, 5, 5, 7, 8, 9, 10, 11, 12, 13};


        switch (modelIndex) {
            case 1:
                return "TSUM(NEW(a) * $)";
            case 2:
                return "tanh(TSUM(NEW(a) * $) + ERROR)";
            case 3:
                return "TSUM(NEW(a) * $^2)";
            case 4:
                return "TSUM(NEW(a) * $^3)";
            case 5:
                return "TSUM(NEW(a) * $^(-1))";
            case 6:
                return "TSUM(NEW(a) * abs($))";
            case 7:
                return "TSUM(NEW(a) * signum($) * abs($)^0.5)";
            case 8:
                return "TSUM(NEW(a) * signum($) * abs($)^1.5)";
            case 9:
                return "TSUM(NEW(a) * ln(abs($)))";
            case 10:
                return "TSUM(NEW(a) * ln(cosh($)))";
            case 11:
                return "TPROD(NEW(a) * $)";
            case 12:
                return "TPROD(NEW(a) * $) * ERROR";
            case 13:
                return "TSUM(PI * NEW(a) + cos(PI * NEW(a) * $))";
            case 14:
                int i = RandomUtil.getInstance().nextInt(mixture.length);
                return getConnectionFunction(mixture[i]);
            default:
                throw new IllegalArgumentException("Not a model");
        }
    }

    private String getErrorDistribution() {
        String defaultDistribution = "U(-1,1)";
//        String defaultDistribution = "Mixture(.5,N(-1,.5),.5,N(1,.5))";
//        String defaultDistribution = "U(-2,2)";

        // all positive errors seems to make everything do just about as well.
//        String defaultDistribution = "U(0, 1)";
//        String defaultDistribution = "Beta(2,5)";

        return defaultDistribution;
    }

    private String getCoefDistribution() {
//        String defaultInterval = "U(1,2)";//"Split(-1,-.2,.2,1)"; //   "U(-1, 1)";
//        String defaultInterval = "Split(-2,-1,1,2)"; //   "U(-1, 1)";
        String defaultInterval = "U(-1,1)";

        return defaultInterval;
    }

    public void testKeiData() {
        try {
//            DataReader reader = new DataReader();
//            reader.setVariablesSupplied(true);
//            reader.setDelimiter(DelimiterType.COMMA);
//
////            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14");
//            File dir = new File("/home/jdramsey/Downloads");
//            File file = new File(dir, "data2.csv");
//
//            DataSet data = reader.parseTabular(file);
//
//            DataFilter interpolator = new MeanInterpolator();
//            data = interpolator.filter(data);
//
////            System.out.println(DataUtils.containsMissingValue(data));
//
//            IndependenceTest test = new IndTestConditionalCorrelation(data, 0.001);
//
//            PcStable pc = new PcStable(test);
//            pc.setDepth(3);
//
//            Graph graph = pc.search();

            Graph graph = new EdgeListGraph();

            System.out.println(graph);
//            String graphDir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/test14/";
            String graphDir = "/home/jdramsey/Downloads";
            writeFiveGraphFormats(graphDir, graph, "keidata.pc.cci.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test18() {
        String data = "Index              x           y\n" +
                "1   -0.72948059  1.01209350\n" +
                "2    0.80798715  1.03681935\n" +
                "3   -0.02095235 -0.37993571\n" +
                "4   -1.64513107  2.80567356\n" +
                "5    0.07684120  0.53549655\n" +
                "6    0.43023555  0.42394366\n" +
                "7   -0.42676522 -0.29117311\n" +
                "8   -2.10007622  5.05256527\n" +
                "9   -0.65580699  0.23033719\n" +
                "10  -1.63617591  2.37332487\n" +
                "11  -1.23680958  1.64217631\n" +
                "12  -0.98708873  1.64958398\n" +
                "13   0.28235697 -0.01322421\n" +
                "14  -0.08159946  0.38443774\n" +
                "15  -1.54593292  1.84517283\n" +
                "16   0.82161145  1.67503553\n" +
                "17   0.67867283  0.29451478\n" +
                "18   0.82985465  0.42267041\n" +
                "19   1.00260529  1.27773320\n" +
                "20   0.13351448 -0.12437656\n" +
                "21  -1.09904966  1.64203132\n" +
                "22   0.73016304  0.30991127\n" +
                "23  -1.27354175  1.76457703\n" +
                "24   0.46934600 -0.23096083\n" +
                "25   0.94805730  0.81191566\n" +
                "26   0.97957563  0.14710183\n" +
                "27   0.04757990  0.02270664\n" +
                "28  -1.91368914  4.23463302\n" +
                "29  -0.59444606  0.28346340\n" +
                "30  -0.71910846  0.48055435\n" +
                "31  -1.25986488  0.88790498\n" +
                "32  -0.26204911  0.26060420\n" +
                "33  -2.86116183  7.72689392\n" +
                "34  -0.33872119  0.15511555\n" +
                "35   0.29524601  0.78723474\n" +
                "36  -1.55505998  2.42226687\n" +
                "37   1.31363087  1.99018993\n" +
                "38   0.30416367 -0.30086889\n" +
                "39  -0.40490442  1.17313339\n" +
                "40  -0.21411599  1.00948841\n" +
                "41  -0.28109120  0.01365507\n" +
                "42   0.17603210 -0.14206316\n" +
                "43  -0.19844413 -0.22548862\n" +
                "44   1.28357341  1.76345693\n" +
                "45  -0.75131049  1.05025460\n" +
                "46   1.28333702  1.94343654\n" +
                "47  -0.34022614  0.65501745\n" +
                "48   0.43365272 -0.11294250\n" +
                "49   0.75461100  0.92198555\n" +
                "50   0.46322642  0.27829113\n" +
                "51  -0.16022255 -0.15490153\n" +
                "52  -2.60920107  7.10439922\n" +
                "53  -0.10300395 -0.54715708\n" +
                "54  -0.71776100  0.61004902\n" +
                "55  -1.02983268  1.49005198\n" +
                "56   2.00803519  3.60926848\n" +
                "57   0.04192123  0.36825046\n" +
                "58  -0.46263362  0.08504761\n" +
                "59   1.94847020  4.11896195\n" +
                "60  -1.07745523  0.98709943\n" +
                "61  -0.17589703 -0.65818477\n" +
                "62   0.81287659  1.48322666\n" +
                "63  -0.45400031  0.31150709\n" +
                "64  -0.47554954  0.72039244\n" +
                "65   0.54807911  0.04695733\n" +
                "66   0.19344402  0.27785098\n" +
                "67  -0.50269640 -0.04849118\n" +
                "68  -0.61802126  0.92677808\n" +
                "69   0.31346941  0.10233607\n" +
                "70   0.63577987  0.52803445\n" +
                "71   0.72578604  0.47726866\n" +
                "72  -0.80020174  0.31059430\n" +
                "73  -1.76306378  3.37591020\n" +
                "74  -1.61635258  2.28390644\n" +
                "75  -0.17252515  0.10342195\n" +
                "76   1.05079002  0.98551042\n" +
                "77  -0.19664975 -0.28227379\n" +
                "78   0.76963250  0.75710892\n" +
                "79  -0.41856370  0.17429286\n" +
                "80   0.97880357  1.32522307\n" +
                "81   1.80407499  3.51901515\n" +
                "82  -0.64527851 -0.02947269\n" +
                "83  -0.03990843 -0.33027020\n" +
                "84  -2.61749411  6.49758868\n" +
                "85  -0.11104965 -0.92909214\n" +
                "86  -0.49033442  1.13918307\n" +
                "87   1.14478318  1.09747092\n" +
                "88   0.64945125  0.93815580\n" +
                "89  -0.09521951  0.89221710\n" +
                "90  -0.15796234  0.49929398\n" +
                "91  -0.06598984 -0.12423091\n" +
                "92   1.34827195  1.74975607\n" +
                "93  -1.61475922  3.09393360\n" +
                "94  -1.83002418  3.48332919\n" +
                "95   0.99130390  1.45967080\n" +
                "96  -0.22961622  0.88516120\n" +
                "97   0.17939985  0.02820274\n" +
                "98   0.64887770  0.54387382\n" +
                "99   0.83298219  0.63271454\n" +
                "100 -1.37347477  1.82045830";

        DataReader reader = new DataReader();

        DataSet dataSet = reader.parseTabular(data.toCharArray());

//        System.out.println(dataSet);

        List<String> varnames = dataSet.getVariableNames();

        RealMatrix realMatrix = dataSet.getDoubleData().getRealMatrix();

        Cci cci = new Cci(realMatrix, varnames, 0.05);

//        cci.temp(realMatrix.getColumn(1), realMatrix.getColumn(2));
        boolean indep = cci.independent(realMatrix.getColumn(1), realMatrix.getColumn(2));

        System.out.println(indep);
    }

    public void test19() {
        Node x = new GraphNode("x");
        Node y = new GraphNode("y");
        Node z = new GraphNode("z");

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(x);
        nodes.add(y);
        nodes.add(z);

        Graph graph = new EdgeListGraph(nodes);
//        graph.addDirectedEdge(x, y);

        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);

        DataSet dataSet = im.simulateData(1000, false);

        RealMatrix realMatrix = dataSet.getDoubleData().getRealMatrix();
        List<String> varnames = dataSet.getVariableNames();

        Cci cci = new Cci(realMatrix, varnames, 0.05);

        List<String> cond = new ArrayList<String>();
        cond.add("z");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            boolean indep = cci.isIndependent("x", "y", cond);
            System.out.println(i + " " + indep);
        }

        long stop = System.currentTimeMillis();

        System.out.println(stop - start);
    }

    public static Test suite() {
        return new TestSuite(TestIndTestConditionalCorrelation.class);
    }

    public static void main(String[] args) {
        new TestIndTestConditionalCorrelation("Test1").test18();
    }

}
