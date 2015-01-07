package edu.cmu.tetrad.search;

import edu.cmu.tetrad.cluster.ClusterUtils;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.GeneralizedSemIm;
import edu.cmu.tetrad.sem.GeneralizedSemPm;
import edu.cmu.tetrad.sem.TemplateExpander;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.CombinationGenerator;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradMatrix;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

public class ExploreKummerfeldRamseyTetradPaper2 {

    public void loop1() {
        for (int sampleSize : new int[]{300, 1000, 3000}) {
            for (int measureSizeOption : new int[]{1, 2}) {
                int clusterSize = -1;
                int numClusters = -1;

                if (measureSizeOption == 1) {
                    clusterSize = 12;
                    numClusters = 4;
                }
                if (measureSizeOption == 2) {
                    clusterSize = 20;
//                    measureSize = 100;
                }

                //l1->l2,l1->l3, l2

//                int numClusters = measureSize / clusterSize;

                for (int impuritiesOption : new int[]{1, 2}) {
                    for (int nonlinearStructuralEdgesOption : new int[]{1, 2}) {
                        for (int nonlinearFactorMeasureEdgesOption : new int[]{1, 2}) {
                            Graph g = getStructuralGraph(numClusters);

                            Graph graph = getMim(g, clusterSize, numClusters);


                            GeneralizedSemPm pm = getPm(
                                    graph,
                                    nonlinearStructuralEdgesOption,
                                    nonlinearFactorMeasureEdgesOption);
                            GeneralizedSemIm im = new GeneralizedSemIm(pm);

                            DataSet dataSet = im.simulateData(sampleSize, false);

                            //Write out data set for R.

                            // Need to pick a directory for these to go in.
                            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulated.data";

                            String datafile = "data." + sampleSize + "." + measureSizeOption + "." + impuritiesOption +
                                    "." + nonlinearStructuralEdgesOption + "." + nonlinearFactorMeasureEdgesOption + ".txt";

                            // Should also save out the _graph.
                            String graphfile = "data." + sampleSize + "." + measureSizeOption + "." + impuritiesOption +
                                    "." + nonlinearStructuralEdgesOption + "." + nonlinearFactorMeasureEdgesOption + ".txt";


                            File file = new File(dir, datafile);

                            try {
                                DataWriter.writeRectangularData(dataSet, new FileWriter(file), '\t');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                FileWriter out2 = new FileWriter(new File(dir, graphfile));
                                PrintWriter out3 = new PrintWriter(out2);
                                out3.print(graph.toString());
                                out3.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public void loop2() {

        String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulated.data";

        File[] files = new File(dir).listFiles();

        for (File file : files) {
            String name = file.getName();

            if (name.startsWith("data")) {

                DataReader reader = new DataReader();
                reader.setVariablesSupplied(true);
                reader.setDelimiter(DelimiterType.WHITESPACE);

                DataSet dataSet = null;

                try {
                    dataSet = reader.parseTabular(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int sampleSize = dataSet.getNumRows();

                // Maybe I should move the searching to a different loop and load in the data sets to do it.
                long start = System.currentTimeMillis();

                TestType type = TestType.TETRAD_WISHART;
                double alpha = 1. / sampleSize;

                FindOneFactorClusters fofc = new FindOneFactorClusters(dataSet, type, alpha);

                fofc.search();
                List<List<Node>> clusters = fofc.getClusters();

                long stop = System.currentTimeMillis();
                long elapsed = stop - start;

                System.out.println(clusters);
                System.out.println(elapsed);

                start = System.currentTimeMillis();

                TestType testType = TestType.TETRAD_WISHART;
                TestType purifyType = TestType.TETRAD_BASED2;
                BuildPureClusters bpc = new BuildPureClusters(dataSet, alpha, testType, purifyType);

                Graph _graph = bpc.search();
                Clusters _clusters = MimUtils.convertToClusters(_graph);

                clusters = new ArrayList<List<Node>>();

                for (int i = 0; i < _clusters.getNumClusters(); i++) {
                    List<String> clusterString = _clusters.getCluster(i);

                    List<Node> cluster = new ArrayList<Node>();

                    for (String s : clusterString) {
                        cluster.add(dataSet.getVariable(s));
                    }

                    clusters.add(cluster);
                }

                stop = System.currentTimeMillis();
                elapsed = stop - start;

                System.out.println(clusters);
                System.out.println(elapsed);

            }
        }
    }

    private GeneralizedSemPm getPm(Graph graph,
                                   int nonlinearStructuralEdgesOption, int nonlinearFactorMeasureEdgesOption) {

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);

        List<Node> variablesNodes = pm.getVariableNodes();
        List<Node> errorNodes = pm.getErrorNodes();

        Map<String, String> paramMap = new HashMap<String, String>();
        String[] funcs = {"TSUM(NEW(B)*$)", "TSUM(NEW(B)*$+NEW(C)*sin(NEW(T)*$+NEW(A)))",
                "TSUM(NEW(B)*(.5*$ + .5*(sqrt(abs(NEW(b)*$+NEW(exoErrorType))) ) ) )"};
        paramMap.put("s", "U(1,3)");
        paramMap.put("B", "Split(-1.5,-.5,.5,1.5)");
        paramMap.put("C", "Split(-1.5,-.5,.5,1.5)");
        paramMap.put("T", "U(.5,1.5)");
        paramMap.put("A", "U(0,.25)");
        paramMap.put("exoErrorType", "U(-.5,.5)");
        paramMap.put("funcType", "U(1,5)");

        String nonlinearStructuralEdgesFunction = funcs[0];
        String nonlinearFactorMeasureEdgesFunction = funcs[0];

        try {
            for (Node node : variablesNodes) {
                if (node.getNodeType() == NodeType.LATENT) {
                    String _template = TemplateExpander.getInstance().expandTemplate(
                            nonlinearStructuralEdgesFunction, pm, node);
                    pm.setNodeExpression(node, _template);
                }
                else {
                    String _template = TemplateExpander.getInstance().expandTemplate(
                            nonlinearFactorMeasureEdgesFunction, pm, node);
                    pm.setNodeExpression(node, _template);
                }
            }

            for (Node node : errorNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate("U(-.5,.5)", pm, node);
                pm.setNodeExpression(node, _template);
            }

            Set<String> parameters = pm.getParameters();

            for (String parameter : parameters) {
                for (String type : paramMap.keySet()) {
                    if (parameter.startsWith(type)) {
                        pm.setParameterExpression(parameter, paramMap.get(type));
                    }
                }
            }
        } catch (ParseException e) {
            System.out.println(e);
        }

        return pm;
    }

    private Graph getMim(Graph structuralGraph, int clusterSize, int numClusters) {

        //        addImpurities(numClusters, clusterSize, impuritiesOption, graph);
        return GraphUtils.randomMim(structuralGraph, clusterSize, 0, 0, 0, true);
    }

    private Graph getStructuralGraph(int numClusters) {
        return GraphUtils.randomDag(numClusters, numClusters, false);
    }


    public static void main(String[] args) {
//        new ExploreKummerfeldRamseyTetradPaper().loop1c();
//        new ExploreKummerfeldRamseyTetradPaper2().testOMSSpecial();
//        new ExploreKummerfeldRamseyTetradPaper().testOMS();
    }
}