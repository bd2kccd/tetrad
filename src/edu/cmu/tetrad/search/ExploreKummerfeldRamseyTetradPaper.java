package edu.cmu.tetrad.search;

import edu.cmu.tetrad.cluster.ClusterUtils;
import edu.cmu.tetrad.data.Clusters;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.GeneralizedSemIm;
import edu.cmu.tetrad.sem.GeneralizedSemPm;
import edu.cmu.tetrad.sem.TemplateExpander;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.CombinationGenerator;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

public class ExploreKummerfeldRamseyTetradPaper {

    public void loop1() {
        Map<String, String> paramMap = new HashMap<String, String>();

        for (int numClusters : new int[]{3, 9}) {
            for (int clusterSize : new int[]{5, 10}) {

                String[] exoErrors = {"Normal(1,s)", "s*(.5-Beta(2,5))"};

                for (int exoErrorType = 0; exoErrorType < exoErrors.length; exoErrorType++) {
                    String exoError = exoErrors[exoErrorType];
                    paramMap.put("s", "U(1,3)");

                    String[] funcs = {"TSUM(NEW(B)*$)", "TSUM(NEW(B)*$+NEW(C)*sin(NEW(T)*$+NEW(A)))",
                            "TSUM(NEW(B)*(.5*$ + .5*(sqrt(abs(NEW(b)*$+NEW(exoErrorType))) ) ) )"};

                    for (int funcType = 0; funcType < funcs.length; funcType++) {
                        String edgeFunc = funcs[funcType];
                        paramMap.put("B", "Split(-1.5,-.5,.5,1.5)");
                        paramMap.put("C", "Split(-1.5,-.5,.5,1.5)");
                        paramMap.put("T", "U(.5,1.5)");
                        paramMap.put("A", "U(0,.25)");
                        paramMap.put("exoErrorType", "U(-.5,.5)");
                        paramMap.put("funcType", "U(1,5)");

                        // Impurities option
                        for (int impuritiesOption = 0; impuritiesOption < 4; impuritiesOption++) {
                            if (clusterSize != 10 && (impuritiesOption == 2 || impuritiesOption == 3)) continue;

                            GeneralizedSemPm pm = null;
                            try {
                                pm = getPm(numClusters, clusterSize, exoError, edgeFunc, paramMap, impuritiesOption);
                            } catch (Exception e) {
                                continue;
                            }
                            GeneralizedSemIm im = new GeneralizedSemIm(pm);

                            for (double alpha : new double[]{.05, .01, .001, .0000001}) {
                                for (int sampleSize : new int[]{50, 200, 1000, 5000}) {
                                    for (int testType = 0; testType < 2; testType++) {
                                        String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad";
                                        String name = "clusters." + numClusters + "." + clusterSize + "." + exoErrorType + "." + funcType + "." +
                                                alpha + "." + sampleSize + "." + testType + "." + impuritiesOption + ".txt";

                                        File file = new File(dir, name);

                                        if (file.exists()) continue;

                                        try {
                                            PrintWriter out = new PrintWriter(file);

                                            for (int e = 0; e < 50; e++) {
                                                DataSet data = im.simulateData(sampleSize, false);

                                                FindOneFactorClusters search;

                                                if (testType == 0) {
                                                    search = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, alpha);
                                                } else {
                                                    search = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, alpha);
                                                }

                                                search.search();
                                                List<List<Node>> clusters = search.getClusters();

                                                System.out.println("Index = " + (e + 1));
                                                System.out.println("Num clusters = " + numClusters);
                                                System.out.println("Exogenous error type = " + exoErrors[exoErrorType]);
                                                System.out.println("Function type = " + funcs[funcType]);
                                                System.out.println("Impurities option = " + (impuritiesOption + 1));
                                                System.out.println("Alpha = " + alpha);
                                                System.out.println("Sample size = " + sampleSize);

                                                out.println(clusters);
                                                out.flush();
                                            }

                                            out.close();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void loop1b() {
        Map<String, String> paramMap = new HashMap<String, String>();

        String[] algTypes = {"SAG.Wishart", "SAG.Delta", "GAP.Wishart", "GAP.Delta", "BPC"};

        for (int algType : new int[]{4}) {
            for (int numClusters : new int[]{3/*, 9*/}) {
                for (int clusterSize : new int[]{/*5, */10}) {
                    String dirname = "test12";
                    String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad/" + dirname + "/";
                    String name = "clusters." + algTypes[algType] + "." + numClusters + "." + clusterSize + ".txt";

                    File file = new File(dir, name);

                    if (file.exists()) continue;

                    try {
                        if (file.exists()) continue;

                        PrintWriter out = new PrintWriter(file);

                        String[] exoErrors = {"Normal(1,s)", "s*(.5-Beta(2,5))"};

                        for (int exoErrorType = 0; exoErrorType < 2; exoErrorType++) {
                            String exoError = exoErrors[exoErrorType];
                            paramMap.put("s", "U(1,3)");

                            String[] funcs = {"TSUM(NEW(B)*$)", "TSUM(NEW(B)*$+NEW(C)*sin(NEW(T)*$+NEW(A)))",
                                    "TSUM(NEW(B)*(.5*$ + .5*(sqrt(abs(NEW(b)*$+NEW(exoErrorType))) ) ) )"};

                            for (int funcType : new int[]{0, 1, 2}) {
                                String edgeFunc = funcs[funcType];
                                paramMap.put("B", "Split(-1.5,-.5,.5,1.5)");
                                paramMap.put("C", "Split(-1.5,-.5,.5,1.5)");
                                paramMap.put("T", "U(.5,1.5)");
                                paramMap.put("A", "U(0,.25)");
                                paramMap.put("exoErrorType", "U(-.5,.5)");
                                paramMap.put("funcType", "U(1,5)");

                                // Impurities option
                                for (int impuritiesOption = 0; impuritiesOption < 4; impuritiesOption++) {
                                    if (clusterSize != 10 && (impuritiesOption == 2 || impuritiesOption == 3)) continue;

                                    GeneralizedSemPm pm;

                                    try {
                                        pm = getPm(numClusters, clusterSize, exoError, edgeFunc, paramMap, impuritiesOption);
                                    } catch (Exception e) {
                                        continue;
                                    }

                                    Graph graph2 = pm.getGraph();
                                    List<List<Node>> trueClusters = ClusterUtils.mimClustering(graph2, graph2.getNodes());

                                    GeneralizedSemIm im = new GeneralizedSemIm(pm);

                                    for (double alpha : new double[]{/*.05, .01, */.001/*, 1e-7*/}) {
                                        for (int sampleSize : new int[]{/*50, 200,*/ 1000/*, 5000*/}) {
//                                            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad";
//                                            String name = "clusters." + algType +"." + numClusters + "." + clusterSize + "." +
//                                                    exoErrorType + "." + funcType + "." + alpha + "." + sampleSize + "." +
//                                                    algType + "." + impuritiesOption + ".txt";
//
//                                            File file = new File(dir, name);


                                            for (int e = 0; e < 1; e++) {
                                                DataSet data = im.simulateData(sampleSize, false);
                                                data = DataUtils.reorderColumns(data);

                                                out.println();
                                                out.println("Algorithm: " + algTypes[algType]);
                                                out.println("Num clusters = " + numClusters);
                                                out.println("Cluster size = " + clusterSize);
                                                out.println("Exogenous error type = " + exoErrors[exoErrorType]);
                                                out.println("Function type = " + funcs[funcType]);
                                                out.println("Impurities option = " + (impuritiesOption + 1));
                                                out.println("Alpha = " + alpha);
                                                out.println("Sample size = " + sampleSize);

//                                                    String[] algTypes = {"FOFC.FourFirst.Wishart", "Fofc.FourFirst.Delta",
//                                                            "FOFC.ThreeFirst.ThreeFirst.Wishart". "_FOFC.ThreeFirst.Delta",
//                                                            "BPC"};

                                                long start = System.currentTimeMillis();

                                                List<List<Node>> clusters;

                                                int fofcDepth = 0;

                                                if (algType == 0) {
                                                    FindOneFactorClusters search;
                                                    search = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, alpha);
                                                    search.setAlgorithm(FindOneFactorClusters.Algorithm.SAG);
                                                    search.setDepth(fofcDepth);
                                                    search.search();
                                                    clusters = search.getClusters();
                                                } else if (algType == 1) {
                                                    FindOneFactorClusters search;
                                                    search = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, alpha);
                                                    search.setAlgorithm(FindOneFactorClusters.Algorithm.SAG);
                                                    search.setDepth(fofcDepth);
                                                    search.search();
                                                    clusters = search.getClusters();
                                                } else if (algType == 2) {
                                                    FindOneFactorClusters search;
                                                    search = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, alpha);
                                                    search.setAlgorithm(FindOneFactorClusters.Algorithm.GAP);
                                                    search.setDepth(fofcDepth);
                                                    search.search();
                                                    clusters = search.getClusters();
                                                } else if (algType == 3) {
                                                    FindOneFactorClusters search;
                                                    search = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, alpha);
                                                    search.setAlgorithm(FindOneFactorClusters.Algorithm.GAP);
                                                    search.setDepth(fofcDepth);
                                                    search.search();
                                                    clusters = search.getClusters();
                                                } else if (algType == 4) {
                                                    BuildPureClusters search;
                                                    TestType testType = TestType.TETRAD_WISHART;
                                                    TestType purifyType = TestType.TETRAD_BASED2;
                                                    search = new BuildPureClusters(data, alpha, testType, purifyType);
                                                    Graph graph = search.search();
                                                    Clusters _clusters = MimUtils.convertToClusters(graph, data.getVariables());

                                                    clusters = new ArrayList<List<Node>>();

                                                    for (int i = 0; i < _clusters.getNumClusters(); i++) {
                                                        List<String> clusterString = _clusters.getCluster(i);

                                                        List<Node> cluster = new ArrayList<Node>();

                                                        for (String s : clusterString) {
                                                            cluster.add(data.getVariable(s));
                                                        }

                                                        clusters.add(cluster);
                                                    }
                                                } else {
                                                    throw new IllegalStateException();
                                                }

                                                long stop = System.currentTimeMillis();

                                                out.println("Elapsed " + (stop - start) + " ms");
                                                out.println();

                                                for (int i = 0; i < clusters.size(); i++) {
                                                    out.println((i + 1) + ". " + clusters.get(i));
                                                }

                                                printMeasures(out, clusters, trueClusters, graph2.getNodes());

                                                out.flush();
                                            }

                                        }
                                    }
                                }
                            }
                        }

                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void loop1c() {
        Map<String, String> paramMap = new HashMap<String, String>();

        String[] algTypes = {"GAP.Wishart", "GAP.Delta", "BPC"};

        for (int algType : new int[]{0, 1}) {
            for (int numClusters : new int[]{3/*, 9*/}) {
                for (int clusterSize : new int[]{/*5, */10}) {

                    for (boolean usePc : new boolean[]{true, false}) {
                        FindOneFactorClusters.AlgType[] gapTypes = {
                                FindOneFactorClusters.AlgType.strict,
                                FindOneFactorClusters.AlgType.lax,
                                FindOneFactorClusters.AlgType.laxWithSpeedup
                        };

                        for (FindOneFactorClusters.AlgType gapAlgType : gapTypes) {
                            for (boolean extraShuffle : new boolean[]{true, false}) {
                                String dirname = "test12";
                                String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad/" + dirname + "/";
                                String name = "clusters." + algTypes[algType] + "." + numClusters + "." + clusterSize +
                                        (usePc ? ".usePc" : ".noPc") + "." + gapAlgType + "." + (extraShuffle ? "shuffle" : "noshuffle") + ".txt";

                                File file = new File(dir, name);

                                if (file.exists()) continue;

                                try {
                                    if (file.exists()) continue;

                                    PrintWriter out = new PrintWriter(file);

                                    String[] exoErrors = {"Normal(1,s)", "s*(.5-Beta(2,5))"};

                                    for (int exoErrorType = 0; exoErrorType < 2; exoErrorType++) {
                                        String exoError = exoErrors[exoErrorType];
                                        paramMap.put("s", "U(1,3)");

                                        String[] funcs = {"TSUM(NEW(B)*$)", "TSUM(NEW(B)*$+NEW(C)*sin(NEW(T)*$+NEW(A)))",
                                                "TSUM(NEW(B)*(.5*$ + .5*(sqrt(abs(NEW(b)*$+NEW(exoErrorType))) ) ) )"};

                                        for (int funcType : new int[]{0, 1, 2}) {
                                            String edgeFunc = funcs[funcType];
                                            paramMap.put("B", "Split(-1.5,-.5,.5,1.5)");
                                            paramMap.put("C", "Split(-1.5,-.5,.5,1.5)");
                                            paramMap.put("T", "U(.5,1.5)");
                                            paramMap.put("A", "U(0,.25)");
                                            paramMap.put("exoErrorType", "U(-.5,.5)");
                                            paramMap.put("funcType", "U(1,5)");

                                            // Impurities option
                                            for (int impuritiesOption = 0; impuritiesOption < 4; impuritiesOption++) {
                                                if (clusterSize != 10 && (impuritiesOption == 2 || impuritiesOption == 3))
                                                    continue;

                                                GeneralizedSemPm pm;

                                                try {
                                                    pm = getPm(numClusters, clusterSize, exoError, edgeFunc, paramMap, impuritiesOption);
                                                } catch (Exception e) {
                                                    continue;
                                                }

                                                Graph graph2 = pm.getGraph();
                                                List<List<Node>> trueClusters = ClusterUtils.mimClustering(graph2, graph2.getNodes());

                                                GeneralizedSemIm im = new GeneralizedSemIm(pm);

                                                for (double alpha : new double[]{/*.05,.01,*/ .001 /*, 1e-7*/}) {
                                                    for (int sampleSize : new int[]{/*50, 200,*/ 1000 /*, 5000*/}) {
//                                            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad";
//                                            String name = "clusters." + algType +"." + numClusters + "." + clusterSize + "." +
//                                                    exoErrorType + "." + funcType + "." + alpha + "." + sampleSize + "." +
//                                                    algType + "." + impuritiesOption + ".txt";
//
//                                            File file = new File(dir, name);


                                                        for (int e = 0; e < 1; e++) {
                                                            DataSet data = im.simulateData(sampleSize, false);
                                                            data = DataUtils.reorderColumns(data);

                                                            out.println();
                                                            out.println("Algorithm: " + algTypes[algType]);
                                                            out.println("Num clusters = " + numClusters);
                                                            out.println("Cluster size = " + clusterSize);
                                                            out.println("Exogenous error type = " + exoErrors[exoErrorType]);
                                                            out.println("Function type = " + funcs[funcType]);
                                                            out.println("Impurities option = " + (impuritiesOption + 1));
                                                            out.println("Alpha = " + alpha);
                                                            out.println("Sample size = " + sampleSize);
                                                            out.println("Use PC = " + usePc);
                                                            out.println("GAP algorithm type = " + gapAlgType);
                                                            out.println("Extra shuffle = " + extraShuffle);

//                                                    String[] algTypes = {"FOFC.FourFirst.Wishart", "Fofc.FourFirst.Delta",
//                                                            "FOFC.ThreeFirst.ThreeFirst.Wishart". "_FOFC.ThreeFirst.Delta",
//                                                            "BPC"};

                                                            long start = System.currentTimeMillis();

                                                            List<List<Node>> clusters;

                                                            int fofcDepth = 0;

                                                            if (algType == 0) {
                                                                FindOneFactorClusters search;
                                                                search = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, alpha);
                                                                search.setAlgorithm(FindOneFactorClusters.Algorithm.GAP);
                                                                search.setDepth(fofcDepth);
                                                                search.setAlgType(gapAlgType);
                                                                search.search();
                                                                search.setExtraShuffle(extraShuffle);
                                                                clusters = search.getClusters();
                                                            } else if (algType == 1) {
                                                                FindOneFactorClusters search;
                                                                search = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, alpha);
                                                                search.setAlgorithm(FindOneFactorClusters.Algorithm.GAP);
                                                                search.setDepth(fofcDepth);
                                                                search.setAlgType(gapAlgType);
                                                                search.setExtraShuffle(extraShuffle);
                                                                search.search();
                                                                clusters = search.getClusters();
                                                            } else {
                                                                throw new IllegalStateException();
                                                            }

                                                            long stop = System.currentTimeMillis();

                                                            out.println("Elapsed " + (stop - start) + " ms");
                                                            out.println();

                                                            for (int i = 0; i < clusters.size(); i++) {
                                                                out.println((i + 1) + ". " + clusters.get(i));
                                                            }

                                                            printMeasures(out, clusters, trueClusters, graph2.getNodes());

                                                            out.flush();
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    out.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    // Peter's measures.
    public void printMeasures(PrintWriter
                                      out, List<List<Node>> foundClusters, List<List<Node>> trueClusters, List<Node> variables) {
        NumberFormat nf = new DecimalFormat("0.0000");

        out.println("\nMeasure 1 = " + nf.format(foundClusters.size() / (double) trueClusters.size()));

        double sum2 = 0.;
        int count2 = 0;

        for (List<Node> cluster : foundClusters) {
            List<Node> cluster1 = cluster;
            cluster1 = GraphUtils.replaceNodes(cluster1, variables);

            List<Node> e = new ArrayList<Node>();

            for (List<Node> c : trueClusters) {
                List<Node> d = new ArrayList<Node>(cluster1);
                d.retainAll(c);
                if (d.size() > e.size()) {
                    e = d;
                }
            }

            double r = e.size() / (double) cluster.size();
            sum2 += r;
            count2++;
        }

        out.println("Measure 2 = " + nf.format(sum2 / count2));

        double sum3 = 0.;
        int count3 = 0;

        for (List<Node> cluster : foundClusters) {
            cluster = GraphUtils.replaceNodes(cluster, variables);

            List<Node> e = new ArrayList<Node>();
            List<Node> t = new ArrayList<Node>();

            for (List<Node> c : trueClusters) {
                List<Node> d = new ArrayList<Node>(cluster);
                d.retainAll(c);
                if (d.size() > e.size()) {
                    e = d;
                    t = c;
                }
            }

            double r = e.size() / (double) t.size();
            sum3 += r;
            count3++;
        }

        out.println("Measure 3 = " + nf.format(sum3 / count3));

    }


    public void loop2() {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("s", "U(1,3)");
        final String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad";

        final Queue<Callable<String>> callables = new ArrayDeque<Callable<String>>();

        for (int _numVars : new int[]{30, 60}) {//, 90}) {
            final int numVars = _numVars;

            for (int _clusterSize : new int[]{5, 10, 15}) {
                final int clusterSize = _clusterSize;
                final int numClusters = numVars / clusterSize;


                GeneralizedSemPm pm = null;
                try {
                    pm = getPm(numClusters, clusterSize, "Normal(1,s)", "TSUM(NEW(B)*$)", paramMap, 0);
                } catch (Exception e) {
                    continue;
                }

                GeneralizedSemIm im = new GeneralizedSemIm(pm);
                final DataSet data = im.simulateData(1000, false);

                Callable<String> callable1 = new Callable<String>() {
                    public String call() {
                        String name = "clusters.fofc." + numVars + "." + clusterSize + ".txt";

                        File file = new File(dir, name);

//                            if (file.exists()) return Thread.currentThread().getName();

                        try {
                            PrintWriter out = new PrintWriter(file);
                            System.out.println("FOFC Start");
                            FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, 0.001);

                            long start = System.currentTimeMillis();

                            Graph graphFofc = fofc.search();

                            long stop = System.currentTimeMillis();

                            Clusters clustersFofc = MimUtils.convertToClusters(graphFofc, data.getVariables());

                            System.out.println("FOFC " + (stop - start) + " " + clustersFofc);
                            out.println("FOFC " + (stop - start) + " " + clustersFofc);
                            out.close();
                            return Thread.currentThread().getName();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };

//                Callable<String> callable2 = new Callable<String>() {
//                    public String call() {
//                        String name = "clusters.bpc." + numVars + "." + clusterSize + ".txt";
//
//                        File file = new File(dir, name);
//
////                            if (file.exists()) return Thread.currentThread().getName();
//
//                        try {
//                            PrintWriter out = new PrintWriter(file);
//                            System.out.println("FOFC Start");
//                            BuildPureClusters bpc = new BuildPureClusters(data, 0.001, TestType.TETRAD_WISHART, TestType.GAUSSIAN_PVALUE);
//
//                            long start = System.currentTimeMillis();
//
//                            Graph graphBpc = bpc.search();
//
//                            long stop = System.currentTimeMillis();
//
//                            Clusters clustersBpc = MimUtils.convertToClusters(graphBpc, data.getVariables());
//
//                            System.out.println("BPC " + (stop - start) + " " + clustersBpc);
//                            out.println("BPC " + (stop - start) + " " + clustersBpc);
//                            out.close();
//                            return Thread.currentThread().getName();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                            return null;
//                        }
//                    }
//                };

                callables.offer(callable1);
//                callables.offer(callable2);
            }
        }

//        class MyCallable implements Callable<String> {
//            private long waitTime;
//
//            public MyCallable(int timeInMillis) {
//                this.waitTime = timeInMillis;
//            }
//
//            @Override
//            public String call() throws Exception {
//                Thread.sleep(waitTime);
//                return Thread.currentThread().getName();
//            }
//        }

//        Queue<MyCallable> callables = new ArrayDeque<MyCallable>();
//        for (int i = 0; i < 200; i++) callables.add(new MyCallable(200));
        ExecutorService executor = Executors.newFixedThreadPool(200);

        FutureTask<String> futureTask = new FutureTask<String>(callables.poll());
        executor.execute(futureTask);
        FutureTask cancellable = null;


        while (true) {
            try {
                cancellable = futureTask;

                String s = futureTask.get(300000L, TimeUnit.MILLISECONDS);

                if (s != null) {
                    Callable<String> callable = callables.poll();
                    if (callable == null) {
                        System.out.println("Really done.");
                        return;
                    }
                    futureTask = new FutureTask<String>(callable);
                    executor.execute(futureTask);

                    System.out.println("FutureTask2 output=" + s);
                }
            } catch (TimeoutException e) {
                cancellable.cancel(true);
                e.printStackTrace();
                System.out.println("Timed out");

                Callable<String> callable = callables.poll();
                if (callable == null) {
                    System.out.println("Really done.");
                    return;
                }
                futureTask = new FutureTask<String>(callable);
                executor.execute(futureTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void loop6() {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("s", "U(1,3)");
        final String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.ramsey.tetrad";

        final Queue<Callable<String>> callables = new ArrayDeque<Callable<String>>();

        for (int _numVars : new int[]{30, 60}) {//, 90}) {
            final int numVars = _numVars;

            for (int _clusterSize : new int[]{5, 10, 15}) {
                final int clusterSize = _clusterSize;
                final int numClusters = numVars / clusterSize;


                GeneralizedSemPm pm = null;
                try {
                    pm = getPm(numClusters, clusterSize, "Normal(1,s)", "TSUM(NEW(B)*$)", paramMap, 0);
                } catch (Exception e) {
                    continue;
                }

                GeneralizedSemIm im = new GeneralizedSemIm(pm);
                final DataSet data = im.simulateData(1000, false);

                Callable<String> callable1 = new Callable<String>() {
                    public String call() {
                        String name = "clusters.fofc." + numVars + "." + clusterSize + ".txt";

                        File file = new File(dir, name);

//                            if (file.exists()) return Thread.currentThread().getName();

                        try {
                            PrintWriter out = new PrintWriter(file);
                            System.out.println("FOFC Start");
                            FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, 0.001);

                            long start = System.currentTimeMillis();

                            Graph graphFofc = fofc.search();

                            long stop = System.currentTimeMillis();

                            Clusters clustersFofc = MimUtils.convertToClusters(graphFofc, data.getVariables());

                            System.out.println("FOFC " + (stop - start) + " " + clustersFofc);
                            out.println("FOFC " + (stop - start) + " " + clustersFofc);
                            out.close();
                            return Thread.currentThread().getName();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };

//                Callable<String> callable2 = new Callable<String>() {
//                    public String call() {
//                        String name = "clusters.bpc." + numVars + "." + clusterSize + ".txt";
//
//                        File file = new File(dir, name);
//
////                            if (file.exists()) return Thread.currentThread().getName();
//
//                        try {
//                            PrintWriter out = new PrintWriter(file);
//                            System.out.println("FOFC Start");
//                            BuildPureClusters bpc = new BuildPureClusters(data, 0.001, TestType.TETRAD_WISHART, TestType.GAUSSIAN_PVALUE);
//
//                            long start = System.currentTimeMillis();
//
//                            Graph graphBpc = bpc.search();
//
//                            long stop = System.currentTimeMillis();
//
//                            Clusters clustersBpc = MimUtils.convertToClusters(graphBpc, data.getVariables());
//
//                            System.out.println("BPC " + (stop - start) + " " + clustersBpc);
//                            out.println("BPC " + (stop - start) + " " + clustersBpc);
//                            out.close();
//                            return Thread.currentThread().getName();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                            return null;
//                        }
//                    }
//                };

                callables.offer(callable1);
//                callables.offer(callable2);
            }
        }

//        class MyCallable implements Callable<String> {
//            private long waitTime;
//
//            public MyCallable(int timeInMillis) {
//                this.waitTime = timeInMillis;
//            }
//
//            @Override
//            public String call() throws Exception {
//                Thread.sleep(waitTime);
//                return Thread.currentThread().getName();
//            }
//        }

//        Queue<MyCallable> callables = new ArrayDeque<MyCallable>();
//        for (int i = 0; i < 200; i++) callables.add(new MyCallable(200));
        ExecutorService executor = Executors.newFixedThreadPool(200);

        FutureTask<String> futureTask = new FutureTask<String>(callables.poll());
        executor.execute(futureTask);
        FutureTask cancellable = null;


        while (true) {
            try {
                cancellable = futureTask;

                String s = futureTask.get(300000L, TimeUnit.MILLISECONDS);

                if (s != null) {
                    Callable<String> callable = callables.poll();
                    if (callable == null) {
                        System.out.println("Really done.");
                        return;
                    }
                    futureTask = new FutureTask<String>(callable);
                    executor.execute(futureTask);

                    System.out.println("FutureTask2 output=" + s);
                }
            } catch (TimeoutException e) {
                cancellable.cancel(true);
                e.printStackTrace();
                System.out.println("Timed out");

                Callable<String> callable = callables.poll();
                if (callable == null) {
                    System.out.println("Really done.");
                    return;
                }
                futureTask = new FutureTask<String>(callable);
                executor.execute(futureTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

   private GeneralizedSemPm getPm(int numClusters, int clusterSize, String exoError, String
            edgeFunc, Map<String, String> paramMap, int impuritiesOption) {
        if (clusterSize != 10 && (impuritiesOption == 2 || impuritiesOption == 3)) {
            throw new IllegalArgumentException();
        }

        Graph g = new EdgeListGraph();

        for (int i = 0; i < numClusters; i++) {
            Node node = new GraphNode("L" + (i + 1));
            node.setNodeType(NodeType.LATENT);
            g.addNode(node);
        }

        List<Node> nodes = g.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                g.addDirectedEdge(nodes.get(i), nodes.get(j));
            }
        }

        Graph graph = GraphUtils.randomMim(g, clusterSize, 0, 0, 0, true);

        addImpurities(numClusters, clusterSize, impuritiesOption, graph);

        GeneralizedSemPm pm = new GeneralizedSemPm(graph);

        List<Node> variablesNodes = pm.getVariableNodes();
        List<Node> errorNodes = pm.getErrorNodes();

        try {
            for (Node node : variablesNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate(edgeFunc, pm, node);
                pm.setNodeExpression(node, _template);
            }

            for (Node node : errorNodes) {
                String _template = TemplateExpander.getInstance().expandTemplate(exoError, pm, node);
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

    private List<Node> getLatents(Graph graph) {
        List<Node> latents = new ArrayList<Node>();

        for (Node node : graph.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) latents.add(node);
        }

        return latents;
    }

    private List<Node> getMeasures(Graph graph) {
        List<Node> latents = new ArrayList<Node>();

        for (Node node : graph.getNodes()) {
            if (node.getNodeType() == NodeType.MEASURED) latents.add(node);
        }

        return latents;
    }

    public void loop3() {
        int numClusters = 9;
        int clusterSize = 10;
        int impuritiesOption = 3;

        Graph g = new EdgeListGraph();

        for (int i = 0; i < numClusters; i++) {
            Node node = new GraphNode("L" + (i + 1));
            node.setNodeType(NodeType.LATENT);
            g.addNode(node);
        }

        List<Node> nodes = g.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                g.addDirectedEdge(nodes.get(i), nodes.get(j));
            }
        }

        Graph graph = GraphUtils.randomMim(g, clusterSize, 0, 0, 0, true);


        addImpurities(numClusters, clusterSize, impuritiesOption, graph);

    }

    private void addImpurities(int numClusters, int clusterSize, int impuritiesOption, Graph graph) {
        if (impuritiesOption == 0) {
            // No impurities.
        } else if (impuritiesOption == 1) {
            // 1MM(cross clusters)+1LM.  ||  This means that every graph has 1 edge between
            // two measures in different clusters, and 1 extra edge from a latent to a measure,
            // and no other impurities.  In general I'm using "MM" to refer to additional
            // measure-measure edge impurities, and "LM" to refer to additional latent-measure edge impurities.

            List<Node> latents = getLatents(graph);

            Collections.shuffle(latents);

            List<Node> n1 = getCluster(graph, latents.get(0));
            List<Node> n2 = getCluster(graph, latents.get(1));

            Collections.shuffle(n1);
            Collections.shuffle(n2);

            graph.addDirectedEdge(n1.get(0), n2.get(0));

            Collections.shuffle(latents);

            Node l3 = latents.get(0);

            List<Node> measures = getMeasures(graph);

            while (true) {
                Collections.shuffle(measures);
                Edge edge = Edges.directedEdge(l3, measures.get(0));

                if (graph.getEdges().contains(edge)) {
                    continue;
                }

                graph.addDirectedEdge(l3, measures.get(0));
                break;
            }

        } else if (impuritiesOption == 2) {
            // *the following are restricted to cluster size 10 cases:* || The second two cases of impurities
            // should only be applied to the graphs with 10 measures per cluster.  These impurity cases involve
            // a large number of impurities that can't be supported very well by smaller clusters.

            // 2 clusters have a fully connected 4-clique among their measures (i.e. there are two such cliques,
            // in different clusters)  || For this type of impurity, randomly select two different clusters,
            // and then for each of those clusters, randomly select 4 measures.  Then for each of those sets of
            // 4 measures, add MM edges until the 4 measures are fully connected in an acyclic clique.

            if (clusterSize != 10) throw new IllegalArgumentException();

            List<Node> latents = getLatents(graph);

            Collections.shuffle(latents);

            List<Node> n1 = getCluster(graph, latents.get(0));
            List<Node> n2 = getCluster(graph, latents.get(1));

            Collections.shuffle(n1);
            Collections.shuffle(n2);

            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 4; j++) {
                    graph.addDirectedEdge(n1.get(i), n1.get(j));
                }
            }

            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 4; j++) {
                    graph.addDirectedEdge(n2.get(i), n2.get(j));
                }
            }
        } else if (impuritiesOption == 3) {
            // 15LM (no more than 7 in any one cluster) || For this type of impurity, randomly add extra edges
            // from latents to measures, keeping track of which measures had been impurified up until that point,
            // and making sure not to impurify more than 7 measures in any given cluster (guaranteeing that every
            // cluster will still have 3 pure measures and can in theory be found by the algorithm).
            // Do this until 15 measures have been impurified.

            if (clusterSize != 10) throw new IllegalArgumentException();
            if (numClusters != 9) throw new IllegalArgumentException();

            List<Node> latents = getLatents(graph);
            List<Node> measures = getMeasures(graph);
            int numAdded = 0;

            ADD:
            while (numAdded < 15) {
                int i = RandomUtil.getInstance().nextInt(latents.size());
                int j = RandomUtil.getInstance().nextInt(measures.size());

                if (!graph.isAdjacentTo(latents.get(i), measures.get(j))) {
                    for (Node l : latents) {
                        List<Node> cluster = getCluster(graph, l);
                        int total = 0;

                        for (Node n : cluster) {
                            if (graph.getParents(n).size() > 1) {
                                total++;
                            }
                        }

                        if (total > 7) continue ADD;
                    }

                    graph.addDirectedEdge(latents.get(i), measures.get(j));
                    numAdded++;
                }
            }
        }
    }

    // Toy example for running max length m in sequence and stopping timed out ones.
    public void loop4() {
        class MyCallable implements Callable<String> {
            private long waitTime;

            public MyCallable(int timeInMillis) {
                this.waitTime = timeInMillis;
            }

            @Override
            public String call() throws Exception {
                Thread.sleep(waitTime);
                return Thread.currentThread().getName();
            }
        }

        Queue<MyCallable> callables = new ArrayDeque<MyCallable>();
        for (int i = 0; i < 200; i++) callables.add(new MyCallable(200));
        ExecutorService executor = Executors.newFixedThreadPool(200);

        FutureTask<String> futureTask = new FutureTask<String>(callables.poll());
        executor.execute(futureTask);
        FutureTask cancellable = null;


        while (true) {
            try {
                cancellable = futureTask;

                String s = futureTask.get(100L, TimeUnit.MILLISECONDS);

                if (s != null) {
                    Callable<String> callable = callables.poll();
                    if (callable == null) {
                        System.out.println("Really done.");
                        return;
                    }
                    futureTask = new FutureTask<String>(callable);
                    executor.execute(futureTask);

                    System.out.println("FutureTask2 output=" + s);
                }
            } catch (TimeoutException e) {
                cancellable.cancel(true);
                e.printStackTrace();
                System.out.println("Timed out");

                Callable<String> callable = callables.poll();
                if (callable == null) {
                    System.out.println("Really done.");
                    return;
                }
                futureTask = new FutureTask<String>(callable);
                executor.execute(futureTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loop5() {
        TetradMatrix d = new TetradMatrix(2, 100);

        d.set(0, 0, RandomUtil.getInstance().nextNormal(0, 0.5));
        d.set(1, 0, RandomUtil.getInstance().nextNormal(0, 0.5));

        for (int i = 1; i < 100; i++) {
//            d.set(i, 0, d.get(i))
        }
    }

    private List<Node> getCluster(Graph graph, Node latent) {
        List<Node> children = graph.getChildren(latent);

        for (Node node : new ArrayList<Node>(children)) {
            if (node.getNodeType() == NodeType.LATENT) {
                children.remove(node);
            }
        }

        return children;
    }

    private void testOMS() {
        Graph g = new EdgeListGraph();
        int numClusters = 4;
        int clusterSize = 10;

        for (int i = 0; i < numClusters; i++) {
            Node node = new GraphNode("L" + (i + 1));
            node.setNodeType(NodeType.LATENT);
            g.addNode(node);
        }

        List<Node> nodes = g.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                g.addDirectedEdge(nodes.get(i), nodes.get(j));
            }
        }

        int numLatentMeasuredImpureParents = 0;
        int numMeasuredMeasuredImpureParents = 0;
        int numMeasuredMeasuredImpureAssociations = 0;

        Graph mim = GraphUtils.randomMim(g, clusterSize, numLatentMeasuredImpureParents,
                numMeasuredMeasuredImpureParents, numMeasuredMeasuredImpureAssociations, true);

        mim.addDirectedEdge(mim.getNode("L1"), mim.getNode("X11"));
        mim.addDirectedEdge(mim.getNode("X11"), mim.getNode("X12"));
//        mim.addBidirectedEdge(mim.getNode("X10"), mim.getNode("X11"));
//        mim.addBidirectedEdge(mim.getNode("X12"), mim.getNode("X11"));
//        mim.addBidirectedEdge(mim.getNode("X33"), mim.getNode("X34"));
//

        System.out.println(mim);

//        System.out.println(((EdgeListGraph) mim).isDConnectedTo(Collections.singletonList(mim.getNode("X1")),
//                Collections.singletonList(mim.getNode("X2")), Collections.singletonList(mim.getNode("L1"))));

//        mim = new EdgeListGraph(mim);
//
//        int n = optimalMeasuredSetSize((EdgeListGraph) mim);
//        int n = optimalMeasuredSetSize2((EdgeListGraph) mim).size();
//        int n = optimalMeasuredSetSize3((EdgeListGraph) mim);
//        int n = optimalMeasuredSetSize4((EdgeListGraph) mim);
        int n = optimalMeasuredSetSize5((EdgeListGraph) mim);
//
        System.out.println("Optimal node size = " + n);
    }

    private void testOMSSpecial() {
        Graph g = new EdgeListGraph();
        int numClusters = 2;
        int clusterSize = 4;

        for (int i = 0; i < numClusters; i++) {
            Node node = new GraphNode("L" + (i + 1));
            node.setNodeType(NodeType.LATENT);
            g.addNode(node);
        }

        List<Node> nodes = g.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                g.addDirectedEdge(nodes.get(i), nodes.get(j));
            }
        }

        int numLatentMeasuredImpureParents = 0;
        int numMeasuredMeasuredImpureParents = 0;
        int numMeasuredMeasuredImpureAssociations = 0;

        Graph mim = GraphUtils.randomMim(g, clusterSize, numLatentMeasuredImpureParents,
                numMeasuredMeasuredImpureParents, numMeasuredMeasuredImpureAssociations, true);

//        mim.addDirectedEdge(mim.getNode("L1"), mim.getNode("X11"));
//        mim.addBidirectedEdge(mim.getNode("X10"), mim.getNode("X11"));
//        mim.addBidirectedEdge(mim.getNode("X12"), mim.getNode("X11"));
//        mim.addBidirectedEdge(mim.getNode("X33"), mim.getNode("X34"));

        mim.removeNode(mim.getNode("X1"));
//        mim.removeNode(mim.getNode("X2"));

        System.out.println(mim);


//        System.out.println(((EdgeListGraph) mim).isDConnectedTo(Collections.singletonList(mim.getNode("X1")),
//                Collections.singletonList(mim.getNode("X2")), Collections.singletonList(mim.getNode("L1"))));

//        mim = new EdgeListGraph(mim);
//
//        int n = optimalMeasuredSetSize((EdgeListGraph) mim);
//        int n = optimalMeasuredSetSize2((EdgeListGraph) mim).size();
//        int n = optimalMeasuredSetSize3((EdgeListGraph) mim);
//        int n = optimalMeasuredSetSize4((EdgeListGraph) mim);
        int n = optimalMeasuredSetSize5((EdgeListGraph) mim);
//
        System.out.println("Optimal node size = " + n);
    }


    private int optimalMeasuredSetSize(EdgeListGraph factorModel) {
        List<Node> M = new ArrayList<Node>();
        List<Node> L = new ArrayList<Node>();

        for (Node node : factorModel.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                L.add(node);
            } else {
                M.add(node);
            }
        }

        int clustered = 0;
        int output = 0;

        // For each subset size of M, in decreasing order...
        FOREACH_M:
        for (int n = M.size(); n >= 4; n--) {

            // for each subset O of M of that size...
            ChoiceGenerator gen = new ChoiceGenerator(M.size(), n);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> O = GraphUtils.asList(choice, M);
                Set<Node> MMinusO = new HashSet<Node>(M);
                MMinusO.removeAll(O);
                System.out.println("Removed " + MMinusO);

                Set<Node> C = new HashSet<Node>();

                // While the set of clustered variables is smaller than O...
                while (C.size() < O.size()) {
                    List<Node> c1 = new ArrayList<Node>(O);
                    c1.removeAll(C);
                    if (c1.size() == 0) break;

                    // Pick an X in O but not in C
                    Set<Node> oMinusC = new HashSet<Node>(O);
                    oMinusC.removeAll(C);

                    Node X = oMinusC.iterator().next();

                    List<Node> OMinusX = new ArrayList<Node>(O);
                    OMinusX.remove(X);

                    clustered = 0;

                    // Pick two other variables q in O. T is the set of these three variables.
                    ChoiceGenerator gen2 = new ChoiceGenerator(OMinusX.size(), 2);
                    int[] choice2;

                    PICKTWO:
                    while ((choice2 = gen2.next()) != null) {
                        List<Node> q = GraphUtils.asList(choice2, OMinusX);
                        List<Node> T = new ArrayList<Node>(q);
                        T.add(X);

                        List<Node> MULMinusT = new ArrayList<Node>(M);
                        MULMinusT.addAll(L);
                        MULMinusT.removeAll(T);

                        // Pick a y in M or L but not in T...this will be the conditioning variable...
                        for (Node y : MULMinusT) {

                            // Find the set of the rest of the variables.
                            List<Node> rest = new ArrayList<Node>(O);
                            rest.remove(y);
                            rest.removeAll(T);

                            List<Node> Y = Collections.singletonList(y);

                            // If T is d-separated from the rest of the variables conditional on Y, add it to C.
                            if (factorModel.isDSeparatedFrom(T, rest, Y)) {
                                C.addAll(T);
//                                System.out.println(T + " _||_ " + rest + " | " + Y);
//                                System.out.println("Clustered " + T + " size(C) = " + C.size());
                                clustered = 1;
                                break PICKTWO;
                            }
                        }

//                        if (clustered == 1) break;
                    }

                    if (clustered == 0) break;   // 0
                }

                if (clustered == 1) break;
            }

            if (clustered == 1) {
                output = n;
                break FOREACH_M;
            }
        }

        return output;
    }

    private Set<Node> optimalMeasuredSetSize2(EdgeListGraph factorModel) {
        List<Node> M = new ArrayList<Node>();
        List<Node> L = new ArrayList<Node>();

        for (Node node : factorModel.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                L.add(node);
            } else {
                M.add(node);
            }
        }

//        Collections.shuffle(M);
//        Collections.shuffle(L);

        Set<Node> D = new HashSet<Node>();

        List<Node> MRest = new ArrayList<Node>(M);

        // Pick a seed of 3 nodes in M.
        ChoiceGenerator gen = new ChoiceGenerator(MRest.size(), 3);
        int[] choice;

        while ((choice = gen.next()) != null) {
            List<Node> Seed = GraphUtils.asList(choice, MRest);

            // rest = the rest of thd nodes (latents included)
            List<Node> rest = new ArrayList<Node>(MRest);
            rest.addAll(L);
            rest.removeAll(Seed);

            // Pick a y among the rest of the nodes.
            for (Node y : rest) {
                List<Node> rest2 = new ArrayList<Node>(rest);
                rest2.remove(y);
                List<Node> Y = Collections.singletonList(y);

                // If seed _||_ rest | y, grow the seed into cluster C.
                if (factorModel.isDSeparatedFrom(Seed, rest2, Y)) {
//                    System.out.println(Seed + " _||_ " + rest2 + " | " + Y);
                    List<Node> C = new ArrayList<Node>(Seed);
                    List<Node> rest3 = new ArrayList<Node>(MRest);
                    rest3.removeAll(C);

                    for (Node o : new ArrayList<Node>(rest3)) {
                        if (C.contains(o)) throw new IllegalArgumentException();

                        C.add(o);
                        rest3.remove(o);

                        if (!factorModel.isDSeparatedFrom(C, rest3, Y)) {
//                            System.out.println("#\t" + C + " DEP " + rest3 + " | " + Y);
                            C.remove(o);
                        } else {
//                            System.out.println("@\t" + C + " _||_ " + rest3 + " | " + Y);
                        }
                    }

                    rest3 = new ArrayList<Node>(MRest);
                    rest3.removeAll(C);

                    M.removeAll(C);

//                    System.out.println(C);

                    D.addAll(C);
                } else {
//                    System.out.println("&\t" + Seed + " DEP " + rest2 + " | " + Y);
                }
            }
        }

        return D;
    }

    private int optimalMeasuredSetSize3(EdgeListGraph factorModel) {
        List<Node> M = new ArrayList<Node>();
        List<Node> L = new ArrayList<Node>();

        for (Node node : factorModel.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                L.add(node);
            } else {
                M.add(node);
            }
        }

        Set<Node> D = optimalMeasuredSetSize2(factorModel);

        System.out.println("D = " + D);

        int clustered = 0;
        int output = D.size();

        List<Node> I = new ArrayList<Node>(M);
        I.removeAll(D);

        System.out.println("I = " + I);
//
        // For each subset size of M, in decreasing order...
        for (int n = 0; n <= I.size(); n++) {

            // for each subset of I of that size...
            ChoiceGenerator gen = new ChoiceGenerator(I.size(), n);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> E = GraphUtils.asList(choice, I);
                Set<Node> O = new HashSet<Node>(M);
                O.removeAll(E);
                System.out.println("Removed " + E);

                Set<Node> C = new HashSet<Node>();

                // While the set of clustered variables is smaller than O...
                while (C.size() < O.size()) {
                    List<Node> c1 = new ArrayList<Node>(O);
                    c1.removeAll(C);
                    if (c1.size() == 0) break;

                    // Pick an X in O but not in C
                    Set<Node> oMinusC = new HashSet<Node>(O);
                    oMinusC.removeAll(C);

                    Node X = oMinusC.iterator().next();

                    List<Node> OMinusX = new ArrayList<Node>(O);
                    OMinusX.remove(X);

                    clustered = 0;

                    // Pick two other variables q in O. T is the set of these three variables.
                    ChoiceGenerator gen2 = new ChoiceGenerator(OMinusX.size(), 2);
                    int[] choice2;

                    while ((choice2 = gen2.next()) != null) {
                        List<Node> q = GraphUtils.asList(choice2, OMinusX);
                        List<Node> T = new ArrayList<Node>(q);
                        T.add(X);

                        List<Node> MULMinusT = new ArrayList<Node>(M);
                        MULMinusT.addAll(L);
                        MULMinusT.removeAll(T);

                        // Pick a y in M or L but not in T...this will be the conditioning variable...
                        for (Node y : MULMinusT) {

                            // Find the set of the rest of the variables.
                            List<Node> rest = new ArrayList<Node>(O);
                            rest.remove(y);
                            rest.removeAll(T);

                            List<Node> Y = Collections.singletonList(y);

                            // If T is d-separated from the rest of the variables conditional on Y, add it to C.
                            if (factorModel.isDSeparatedFrom(T, rest, Y)) {
                                C.addAll(T);
//                                System.out.println(T + " _||_ " + rest + " | " + Y);
//                                System.out.println("Clustered " + T + " size(C) = " + C.size());
                                clustered = 1;
                                break;
                            }
                        }

                        if (clustered == 1) break;
                    }

                    if (clustered == 0) break;
                }

                if (clustered == 1) break;
            }

            if (clustered == 1) {
                output = M.size() - n;
                break;
            }
        }

        return output;
    }

    private int optimalMeasuredSetSize4(EdgeListGraph factorModel) {
        List<Node> M = new ArrayList<Node>();
        List<Node> L = new ArrayList<Node>();

        for (Node node : factorModel.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                L.add(node);
            } else {
                M.add(node);
            }
        }

        Set<Node> impureSet = new HashSet<Node>();
        Set<Node> clusterableSet = new HashSet<Node>();

        for (Node l : L) {
            int pureCount = 0;
            Set<Node> cluster = new HashSet<Node>();

            List<Node> children = factorModel.getChildren(l);
            children.removeAll(L);

            for (Node c : children) {
                if (factorModel.getNodesInTo(c, Endpoint.ARROW).size() > 1) {
                    impureSet.add(c);
                } else {
                    pureCount++;
                    cluster.add(c);
                }
            }

            if (pureCount > 3) {
                clusterableSet.addAll(cluster);
            } else {
                impureSet.addAll(cluster);
            }
        }

        System.out.println("Clusterable " + clusterableSet);
        System.out.println("Impure " + impureSet);

        Set D = impureSet;

        System.out.println("D = " + D);

        int clustered = 0;
        int output = D.size();

        List<Node> I = new ArrayList<Node>(M);
        I.removeAll(clusterableSet);

        System.out.println("I = " + I);

//        For each subset size of M, leaving out a subset of the variables in I...
        for (int n = 0; n <= I.size(); n++) {

            ChoiceGenerator gen = new ChoiceGenerator(I.size(), n);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Node> E = GraphUtils.asList(choice, I);
                Set<Node> O = new HashSet<Node>(M);
                O.removeAll(E);
                System.out.println("Removed " + E);

                Set<Node> C = new HashSet<Node>();

                // While there are unclustered variables...
                while (C.size() < O.size()) {
                    List<Node> c1 = new ArrayList<Node>(O);
                    c1.removeAll(C);
                    if (c1.size() == 0) break;

                    // Pick an unclustered X in O
                    Set<Node> oMinusC = new HashSet<Node>(O);
                    oMinusC.removeAll(C);

                    Node X = oMinusC.iterator().next();

                    List<Node> OMinusX = new ArrayList<Node>(O);
                    OMinusX.remove(X);

                    clustered = 0;

                    // Pick two other variables q in O to form a set T = {q1, q2, X}
                    ChoiceGenerator gen2 = new ChoiceGenerator(OMinusX.size(), 2);
                    int[] choice2;

                    while ((choice2 = gen2.next()) != null) {
                        List<Node> q = GraphUtils.asList(choice2, OMinusX);
                        List<Node> T = new ArrayList<Node>(q);
                        T.add(X);

                        List<Node> MULMinusT = new ArrayList<Node>(M);
                        MULMinusT.addAll(L);
                        MULMinusT.removeAll(T);

                        // Pick a y in M or L but not in T...this will be the conditioning variable...
                        for (Node y : MULMinusT) {

                            List<Node> rest = new ArrayList<Node>(O);
                            rest.remove(y);
                            rest.removeAll(T);

                            List<Node> Y = Collections.singletonList(y);

                            // If T is d-separated from the rest of the observed variables conditional on Y, add it to C.
                            if (factorModel.isDSeparatedFrom(T, T, Y) && factorModel.isDSeparatedFrom(T, rest, Y)) {
                                C.addAll(T);
//                                System.out.println(T + " _||_ " + rest + " | " + Y);
//                                System.out.println("Clustered " + T + " size(C) = " + C.size());
                                clustered = 1;
                                break;
                            }
                        }

                        if (clustered == 1) break;
                    }

                    if (clustered == 0) break;
                }

                if (clustered == 1) break;
            }

            if (clustered == 1) {
                output = M.size() - n;
                break;
            }
        }

        return output;
    }

    private int optimalMeasuredSetSize5(EdgeListGraph factorModel) {
        List<Node> M = new ArrayList<Node>();
        List<Node> L = new ArrayList<Node>();

        for (Node node : factorModel.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                L.add(node);
            } else {
                M.add(node);
            }
        }

        Set<Set<Node>> choices = new HashSet<Set<Node>>();
        Set<Set<Node>> guysUnderLatents = getGuysUnderLatents(factorModel, L);
        Set<Set<Node>> clusters = new HashSet<Set<Node>>();

        choices.add(new HashSet<Node>());

        for (Set<Node> children : guysUnderLatents) {
            Set<Node> cluster = new HashSet<Node>();

            for (Node c : children) {
                if (factorModel.getNodesInTo(c, Endpoint.ARROW).size() > 1) {
                    List<Node> parents = factorModel.getNodesInTo(c, Endpoint.ARROW);
                    parents.removeAll(L);

                    if (parents.size() > 0) {
                        for (Node d : parents) {
                            Edge edge = factorModel.getEdge(d, c);
                            if (Edges.isDirectedEdge(edge)) {
                                Node head = Edges.getDirectedEdgeHead(edge);
                                choices.add(Collections.singleton(head));
                            } else {
                                Set<Node> pair = new HashSet<Node>();
                                pair.add(c);
                                pair.add(d);
                                choices.add(pair);
                            }
                        }
                    }
                    else {
                        choices.add(Collections.singleton(c));
                    }
                } else {
                    cluster.add(c);
                }
            }

            clusters.add(cluster);
        }

        Set<Set<Node>> clustering = new HashSet<Set<Node>>();

        for (Set<Node> choice : choices) {
            Set<Set<Node>> guysUnderLatents1 = getGuysUnderLatents(factorModel, L);

            for (Set<Node> children : guysUnderLatents1) {
                children.removeAll(choice);
                clustering.add(children);
            }

            List<List<Node>> twoClusters = new ArrayList<List<Node>>();

            for (Set<Node> cluster : clustering) {
                if (cluster.size() == 2) {
                    twoClusters.add(new ArrayList<Node>(cluster));
                }
            }

            G:
            for (List<Node> twoCluster : new ArrayList<List<Node>>(twoClusters)) {
                for (List<Node> twoCluster2 : new ArrayList<List<Node>>(twoClusters)) {
                    if (twoCluster == twoCluster2) {
                        continue;
                    }

                    if (factorModel.isDConnectedTo(twoCluster, twoCluster2, new ArrayList<Node>())) {
                        choice.add(twoCluster.get(0));
                        twoClusters.remove(twoCluster);
                        twoClusters.remove(twoCluster2);
                        continue G;
                    }
                }

                choice.add(twoCluster.get(0));
                choice.add(twoCluster.get(1));
                twoClusters.remove(twoCluster);
            }
        }

        List<List<Node>> _choices = new ArrayList<List<Node>>();

        for (Set<Node> choice : choices) {
            _choices.add(new ArrayList<Node>(choice));
        }

        System.out.println("Choice pairs " + _choices);

        int[] dims = new int[_choices.size()];
        for (int i = 0; i < _choices.size(); i++) dims[i] = _choices.get(i).size();

        List<List<Node>> choices0 = new ArrayList<List<Node>>();

        CombinationGenerator gen3 = new CombinationGenerator(dims);
        int[] choice3;
        Set<Integer> seenSizes = new HashSet<Integer>();

        while ((choice3 = gen3.next()) != null) {
            System.out.println("### " + choice3);

            Set<Node> _choice = new HashSet<Node>();

            for (int i = 0; i < choice3.length; i++) {
                _choice.add(_choices.get(i).get(choice3[i]));
            }

            int size = _choice.size();

            if (!seenSizes.contains(size)) {
                choices0.add(new ArrayList<Node>(_choice));
                seenSizes.add(size);
            }
        }

        Collections.sort(choices0, new Comparator<List<Node>>() {
            public int compare(List<Node> o1, List<Node> o2) {
                return o1.size() - o2.size();
            }
        });

        List<List<Node>> allChoices = new ArrayList<List<Node>>();

        for (List<Node> l : new ArrayList<List<Node>>(choices0)) {
            allChoices.add(l);
        }

        System.out.println(allChoices);

        int clustered = 0;

        List<Node> _O = null;

        for (List<Node> choice : allChoices) {
            Set<Node> O = new HashSet<Node>(M);
            O.removeAll(choice);
            _O = new ArrayList<Node>(O);

            Set<Node> C = new HashSet<Node>();

            // While there are unclustered variables...
            while (C.size() < O.size()) {
                List<Node> c1 = new ArrayList<Node>(O);
                c1.removeAll(C);
                if (c1.size() == 0) break;

                // Pick an unclustered X in O
                Set<Node> oMinusC = new HashSet<Node>(O);
                oMinusC.removeAll(C);

                Node X = oMinusC.iterator().next();

                List<Node> OMinusX = new ArrayList<Node>(O);
                OMinusX.remove(X);

//                System.out.println("OMinusX = " + OMinusX);

                clustered = 0;

                // Pick two other variables q in O to form a set T = {q1, q2, X}
//                System.out.println("OMinuxX.size() = " + OMinusX.size());
                ChoiceGenerator gen2 = new ChoiceGenerator(OMinusX.size(), 2);
                int[] choice2;

                while ((choice2 = gen2.next()) != null) {
                    List<Node> q = GraphUtils.asList(choice2, OMinusX);
                    List<Node> T = new ArrayList<Node>(q);
                    T.add(X);

                    List<Node> MULMinusT = new ArrayList<Node>(M);
                    MULMinusT.addAll(L);
                    MULMinusT.removeAll(T);

                    // Pick a y in M or L but not in T...this will be the conditioning variable...
                    for (Node y : MULMinusT) {

                        List<Node> rest = new ArrayList<Node>(O);
                        rest.remove(y);
                        rest.removeAll(T);

                        List<Node> Y = Collections.singletonList(y);

                        // If T is d-separated from the rest of the observed variables conditional on Y, add it to C.
                        if (factorModel.isDSeparatedFrom(T, rest, Y) && factorModel.isDSeparatedFrom(T, T, Y)) {
                            C.addAll(T);
//                                System.out.println(T + " _||_ " + rest + " | " + Y);
//                                System.out.println("Clustered " + T + " size(C) = " + C.size());
                            clustered = 1;
                            break;
                        }
                    }

                    if (clustered == 1) break;
                }

                if (clustered == 0) break;
            }

            if (clustered == 1) break;
        }

        return _O.size();
    }

    private Set<Set<Node>> getGuysUnderLatents(EdgeListGraph factorModel, List<Node> L) {
        Set<Set<Node>> guysUnderLatents = new HashSet<Set<Node>>();

        for (Node l : L) {
            List<Node> children = factorModel.getChildren(l);
            children.removeAll(L);
            guysUnderLatents.add(new HashSet<Node>(children));
        }

        return guysUnderLatents;
    }


    private boolean allConditionallyIndependent(Graph graph, List<Node> t, List<Node> Y) {
        for (int i = 0; i < t.size(); i++) {
            for (int j = i + 1; j < t.size(); j++) {
                Node n1 = t.get(i);
                Node n2 = t.get(j);

                if (graph.isDConnectedTo(n1, n2, Y)) return false;
            }
        }

        return true;
    }


    public static void main(String[] args) {
//        new ExploreKummerfeldRamseyTetradPaper().loop1c();
        new ExploreKummerfeldRamseyTetradPaper().testOMSSpecial();
//        new ExploreKummerfeldRamseyTetradPaper().testOMS();
    }
}