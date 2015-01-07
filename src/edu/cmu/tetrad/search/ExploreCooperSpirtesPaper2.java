package edu.cmu.tetrad.search;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;
import edu.pitt.dbmi.algo.bayesian.constraint.inference.BCInference;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.exp;
import static java.lang.Math.floor;
import static java.lang.Math.log;

/**
 * I'm going to try here to read in the simulation data sets that Ruben just made using Smith's code and
 * run them.
 */
public class ExploreCooperSpirtesPaper2 {

    private NumberFormat nf = new DecimalFormat("0.000");
    private Random shuffleRandom;
    private PrintWriter out;


    public void loop1() {
//        long seed = 30402033986L;
        long seed = 8477756103859L;
        RandomUtil.getInstance().setSeed(seed);
        shuffleRandom = new Random(seed);
        long start = System.currentTimeMillis();

        String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/cooper.spirtes.paper";
        String file = "out1.txt";
        try {
            out = new PrintWriter(new File(dir, file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Table describing each data-generating causal Bayesian network (DGCBN).

//        String modelName = "Random";
        String modelName = "Alarm";
//        String modelName = "Hailfinder";
//        String modelName = "Hepar";
//        String modelName = "Win95";
//        String modelName = "Barley";

        int numberOfModels = 500;
        int sampleSize = 1000;
        double chiSquareAlpha = .05;
        boolean rfciUsed = true;
        int depth = -1;
        int numConfounders = 5;

        BayesIm im = getBayesIM(modelName);
        BayesPm pm = im.getBayesPm();
        Dag dag = pm.getDag();

        double[] indegree = new double[dag.getNumNodes()];
        List<Node> nodes = dag.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            indegree[i] = dag.getAdjacentNodes(nodes.get(i)).size();
        }

        double[] states = new double[dag.getNumNodes()];

        for (int i = 0; i < nodes.size(); i++) {
            DiscreteVariable variable = (DiscreteVariable) pm.getVariable(nodes.get(i));
            states[i] = variable.getNumCategories();
        }

        println("Name of model: " + modelName);
        println("Domain of model: ________");
        println("Number of nodes: " + dag.getNumNodes());
        println("Number of arcs: " + dag.getEdges().size());
        println("Mean Indegree: " + nf.format(StatUtils.mean(indegree)));
        println("Std Dev Indegree: " + nf.format(StatUtils.sd(indegree)));
        println("Mean States: " + nf.format(StatUtils.mean(states)));
        println("Std Dev States: " + nf.format(StatUtils.sd(indegree)));
        println("Number of Confounders = " + numConfounders);
        println("");

        println("numberOfModels = " + numberOfModels);
        println("sampleSize = " + sampleSize);
        println("alpha = " + chiSquareAlpha);
        println("RFCI used = " + rfciUsed);
        println("depth = " + depth);
//        print("seed = " + seed);
        println("");

        // Perhaps generate 1000 samples from each DGCBN

        DataSet data = im.simulateData(sampleSize, false);

        List<Node> latents = getLatents(numConfounders, dag);
        removeLatentsFromData(latents, data, true);

        // Perhaps generate about 500 models in ProbRFCI. To get started, maybe just use 50 models.

        List<Graph> pags = new ArrayList<Graph>();

        IndTestProbabilistic test0 = new IndTestProbabilistic(data);

        Fci fci0 = new Fci(test0);
        fci0.setRFCI_Used(rfciUsed);
        fci0.setDepth(depth);

        for (int i = 0; i < numberOfModels; i++) {
            println("Model # " + (i + 1));
            Graph PAG_PROB = fci0.search();
            PAG_PROB = GraphUtils.replaceNodes(PAG_PROB, data.getVariables());

            if (!pags.contains(PAG_PROB)) {
                pags.add(PAG_PROB);
            }
        }

        List<Graph> pagProbs = new ArrayList<Graph>(pags);

//        //  Let PAG-CS be the PAG obtained by RFCI when using a ChiSq test.
//
        IndTestChiSquare test1 = new IndTestChiSquare(data, chiSquareAlpha);
        Fci fci1 = new Fci(test1);
        fci1.setRFCI_Used(rfciUsed);
        fci1.setDepth(depth);
        test1.startRecordingFacts();
        Graph PAG_CS = fci1.search();
        PAG_CS = GraphUtils.replaceNodes(PAG_CS, data.getVariables());

        if (!pags.contains(PAG_CS)) {
            pags.add(PAG_CS);
        }
//
//        // Let PAG-PR be the MAP PAG obtained by RFCI when having BC_Inference always return the test result
//        // that is the most probable
//
        ProbabilisticMAPIndependence test2 = new ProbabilisticMAPIndependence(data);

        Fci fci2 = new Fci(test2);
        fci2.setRFCI_Used(rfciUsed);
        fci2.setDepth(depth);
        Graph PAG_PR = fci2.search();
        PAG_PR = GraphUtils.replaceNodes(PAG_PR, data.getVariables());

        if (!pags.contains(PAG_PR)) {
            pags.add(PAG_PR);
        }
//
//        // Let PAG-True be the PAG obtained from the DGCBN.
//
        IndTestDSep test3 = new IndTestDSep(new EdgeListGraph(dag));
        List<Node> graphNodes = dag.getNodes();
        graphNodes.removeAll(latents);
        test3 = (IndTestDSep) test3.indTestSubset(graphNodes);
        Fci fci3 = new Fci(test3);
        fci3.setRFCI_Used(rfciUsed);
        fci3.setDepth(depth);
        test3.startRecordingFacts();
        Graph PAG_True = fci3.search();
        PAG_True = GraphUtils.replaceNodes(PAG_True, data.getVariables());

        if (!pags.contains(PAG_True)) {
            pags.add(PAG_True);
        }


        // Include PAG-CS, PAG-PR, and PAG-True in the hash table entries for ProbRFCI.

        for (IndependenceFact fact : test1.getFacts()) {
            fact = replaceVars(fact, test0.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        for (IndependenceFact fact : test2.getH().keySet()) {
            fact = replaceVars(fact, test0.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        for (IndependenceFact fact : test3.getFacts()) {
            fact = replaceVars(fact, test0.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        Map<IndependenceFact, Double> H = test0.getH();

        println("\n# constraints in the domain of H: " + H.keySet().size() + "\n");

        // Normalize the probability scores by all the models in the ProbRFCI, including those 500 (or 50) sampled,
        // as well as PAG-CS, PAG-PR, and PAG-True, if they weren't sampled

        Map<Graph, Double> pagLnProbs = getLnProbs(pags, H);
        double lnQTotal = lnQTotal(pagLnProbs);

        Graph PAG_BS = getMaxPag(pagProbs, pagLnProbs);      // "Best sampled" PAG.
//        double maxLnProb = getLnProb(PAG_BS, H) - lnQTotal;

        for (int i = 0; i < pags.size(); i++) {
            Graph pag = pags.get(i);
            double lnQ = pagLnProbs.get(pag);
            double normalizedlnQ = lnQ - lnQTotal;

            print("lnQ = " + (lnQ) + " lnQTotal = " + lnQTotal + " normalized lnQ = " + normalizedlnQ);

            if (pag.equals(PAG_CS)) print("\tPAG_CS");
            if (pag.equals(PAG_PR)) print("\tPAG_PR");
            if (pag.equals(PAG_True)) print("\tPAG_True");
            if (pag.equals(PAG_BS)) print("\tPAG_BS");
            println("");
        }


        // These normalized probabilities can be displayed in a small table:

        TextTable table = new TextTable(5, 3);

        table.setToken(1, 0, "PAG-CS");
        table.setToken(2, 0, "PAG-PR");
        table.setToken(3, 0, "PAG-BS");
        table.setToken(4, 0, "PAG-True");
        table.setToken(0, 1, "NPP");
        table.setToken(0, 2, "RPP");

        double pagCsNpp = (pagLnProbs.get(PAG_CS) - lnQTotal);
        double pagPrNpp = (pagLnProbs.get(PAG_PR) - lnQTotal);
        double pagBsNpp = (pagLnProbs.get(PAG_BS) - lnQTotal);
        double pagTrueNpp = (pagLnProbs.get(PAG_True) - lnQTotal);
//
//        double pagCsNpp = exp(((pagLnProbs.get(PAG_CS) - lnQTotal) / (double) sampleSize));
//        double pagPrNpp = exp(((pagLnProbs.get(PAG_PR) - lnQTotal) / (double) sampleSize));
//        double pagTrueNpp = exp(((pagLnProbs.get(PAG_True) - lnQTotal) / (double) sampleSize));
//
//
        double pagCsRpp = (pagLnProbs.get(PAG_CS));
        double pagPrRpp = (pagLnProbs.get(PAG_PR));
        double pagBsRpp = (pagLnProbs.get(PAG_BS));
        double pagTrueRpp = (pagLnProbs.get(PAG_True));
//
//        double pagCsRpp = exp((pagLnProbs.get(PAG_CS) / (double) sampleSize));
//        double pagPrRpp = exp((pagLnProbs.get(PAG_PR) / (double) sampleSize));
//        double pagTrueRpp = exp((pagLnProbs.get(PAG_True) / (double) sampleSize));

//        NumberFormat nf2 = new DecimalFormat("0.0000");
        NumberFormat nf2 = new DecimalFormat("0.0E0");

        table.setToken(1, 1, nf2.format(pagCsNpp));
        table.setToken(2, 1, nf2.format(pagPrNpp));
        table.setToken(3, 1, nf2.format(pagBsNpp));
        table.setToken(4, 1, nf2.format(pagTrueNpp));

        table.setToken(1, 2, nf2.format(pagCsRpp));
        table.setToken(2, 2, nf2.format(pagPrRpp));
        table.setToken(3, 2, nf2.format(pagBsRpp));
        table.setToken(4, 2, nf2.format(pagTrueRpp));

        println("\n" + table);

        // Misclassification matrix for each DGCBN

        // Generate a matrix for each of PAG-CS and PAG-PR

        // The entries are counts that have been normalized by the total counts over all the
        // cells, except the null-null cell, which likely will dominate the number of counts;
        // we can report it separately in the caption of the table as "fraction of null-null
        // counts was 0.97", for example
        printEndpointMisclassifications("PAG_CS", data, PAG_CS, PAG_True);
        printEndpointMisclassifications("PAG_PR", data, PAG_PR, PAG_True);
        printEndpointMisclassifications("PAG_BS", data, PAG_BS, PAG_True);

        printEdgeMisclassifiations("PAG_CS", data, PAG_CS, PAG_True);
        printEdgeMisclassifiations("PAG_PR", data, PAG_PR, PAG_True);
        printEdgeMisclassifiations("PAG_BS", data, PAG_BS, PAG_True);

//        influenceOfSampleSize(im, latents, test2, PAG_PR);

        int numBins = 10;
        int[] nCountSummary = new int[numBins];
        int[] dCountSummary = new int[numBins];

        List<Node> _nodes = data.getVariables();
        Endpoint end1, end2;

        for (int k = 0; k < 3; k++) {
            if (k == 0) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.CIRCLE;
            } else if (k == 1) {
                end1 = Endpoint.ARROW;
                end2 = Endpoint.ARROW;
            } else if (k == 2) {
                end1 = Endpoint.NULL; // interpreted as null
                end2 = Endpoint.NULL;
            } else {
                throw new IllegalStateException();
            }

            Graph complete = new EdgeListGraph(_nodes);
            complete.fullyConnect(Endpoint.TAIL);

            int[] nCount = new int[numBins];
            int[] dCount = new int[numBins];

            for (Edge _edge : complete.getEdges()) {
                Node n1 = _edge.getNode1();
                Node n2 = _edge.getNode2();

                Edge edge = new Edge(n1, n2, end1, end2);

                double p = edgeFrequency(edge, pagProbs);
                int bin = (int) floor(p * numBins);

                if (bin == numBins) bin = numBins - 1;

                int count;

                if (PAG_True.containsEdge(edge) || ((end1 == Endpoint.NULL && end2 == Endpoint.NULL && !PAG_True.isAdjacentTo(n1, n2)))) {
                    count = 1;
                } else {
                    count = 0;
                }

                nCount[bin] = nCount[bin] + count;
                dCount[bin] = dCount[bin] + 1;

                nCountSummary[bin] = nCountSummary[bin] + count;
                dCountSummary[bin] = dCountSummary[bin] + 1;
            }

            double[] binHeight = new double[numBins];

            for (int i = 0; i < numBins; i++) {
                binHeight[i] = nCount[i] / (double) dCount[i];
            }

            println("\n" + end1 + "-" + end2);

            for (int i = 0; i < numBins; i++) {
                println((i + 1) + "\t" + nCount[i] + "\t" + dCount[i] + "\t" + (Double.isNaN(binHeight[i]) ? "-" : nf.format(binHeight[i])));
            }
        }

        for (int k = 0; k < 2; k++) {
            if (k == 0) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.ARROW;
            } else if (k == 1) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.ARROW;
            } else {
                throw new IllegalStateException();
            }

            Graph complete = new EdgeListGraph(_nodes);
            complete.fullyConnect(Endpoint.TAIL);

            int[] nCount = new int[numBins];
            int[] dCount = new int[numBins];

            for (Edge _edge : complete.getEdges()) {
                Node n1 = _edge.getNode1();
                Node n2 = _edge.getNode2();

                Edge edge = new Edge(n1, n2, end1, end2);
                Edge backEdge = new Edge(n1, n2, end2, end1);

                double p = edgeFrequency(edge, pagProbs);
                int bin = (int) floor(p * numBins);

                if (bin == numBins) bin = numBins - 1;

                int count;

                if (PAG_True.containsEdge(edge)) {
                    count = 1;
                } else {
                    count = 0;
                }

                nCount[bin] = nCount[bin] + count;
                dCount[bin] = dCount[bin] + 1;

                nCountSummary[bin] = nCountSummary[bin] + count;
                dCountSummary[bin] = dCountSummary[bin] + 1;
            }

            double[] binHeight = new double[numBins];

            for (int i = 0; i < numBins; i++) {
                binHeight[i] = nCount[i] / (double) dCount[i];
            }

            println("\n" + end1 + "-" + end2);

            for (int i = 0; i < numBins; i++) {
                println((i + 1) + "\t" + nCount[i] + "\t" + dCount[i] + "\t" +
                        (Double.isNaN(binHeight[i]) ? "-" : nf.format(binHeight[i])));
            }
        }

        double[] binHeightSummary = new double[numBins];

        for (int i = 0; i < numBins; i++) {
            binHeightSummary[i] = nCountSummary[i] / (double) dCountSummary[i];
        }

        println("\nSUMMARY");

        for (int i = 0; i < numBins; i++) {
            println((i + 1) + "\t" + nCountSummary[i] + "\t" + dCountSummary[i] + "\t" +
                    (Double.isNaN(binHeightSummary[i]) ? "-" : nf.format(binHeightSummary[i])));
        }

        println("===Graphs===");
        println("\nTrue DAG = " + dag);
        println("\nTrue " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + PAG_True);
        println("\nChi Square " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + PAG_CS);
        println("\nBest Score " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + PAG_BS);
//        print("normalisedPagLnProb = " + maxLnProb);
//        print("normalisedProb = " + exp(maxLnProb));

        long stop = System.currentTimeMillis();

        println("\nElapsed " + (stop - start) / 1000L + " seconds");
        out.close();
    }

    private void println(String s) {
        out.println(s);
        out.flush();
        System.out.println(s);
    }

    private void print(String s) {
        out.print(s);
        out.flush();
        System.out.print(s);
    }

    private IndependenceFact replaceVars(IndependenceFact fact, List<Node> variables) {
        Node x = null;

        for (Node node : variables) {
            if (fact.getX().getName().equals(node.getName())) {
                x = node;
            }
        }

        Node y = null;

        for (Node node : variables) {
            if (fact.getY().getName().equals(node.getName())) {
                y = node;
            }
        }

        List<Node> z = new ArrayList<Node>();

        for (Node _z : fact.getZ()) {
            for (Node node : variables) {
                if (node.getName().equals(_z.getName())) {
                    z.add(node);
                    break;
                }
            }
        }

        return new IndependenceFact(x, y, z);
    }


    public void loop1a() {
//        long seed = 30402033986L;
//        long seed = 8477756103859L;
//        RandomUtil.getInstance().setSeed(seed);
//        shuffleRandom = new Random(seed);
        long start = System.currentTimeMillis();

        // Table describing each data-generating causal Bayesian network (DGCBN).

//        String modelName = "Random";
        String modelName = "Alarm";
//        String modelName = "Hailfinder";
//        String modelName = "Hepar";
//        String modelName = "Win95";
//        String modelName = "Barley";

        int numberOfModels = 50;
        int sampleSize = 1000;
        double chiSquareAlpha = .05;
        boolean rfciUsed = true;
        int depth = -1;
        int numConfounders = 5;

        BayesIm im = getBayesIM(modelName);
        BayesPm pm = im.getBayesPm();
        Dag dag = pm.getDag();

        double[] indegree = new double[dag.getNumNodes()];
        List<Node> nodes = dag.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            indegree[i] = dag.getAdjacentNodes(nodes.get(i)).size();
        }

        double[] states = new double[dag.getNumNodes()];

        for (int i = 0; i < nodes.size(); i++) {
            DiscreteVariable variable = (DiscreteVariable) pm.getVariable(nodes.get(i));
            states[i] = variable.getNumCategories();
        }

        System.out.println("Name of model: " + modelName);
        System.out.println("Domain of model: ________");
        System.out.println("Number of nodes: " + dag.getNumNodes());
        System.out.println("Number of arcs: " + dag.getEdges().size());
        System.out.println("Mean Indegree: " + nf.format(StatUtils.mean(indegree)));
        System.out.println("Std Dev Indegree: " + nf.format(StatUtils.sd(indegree)));
        System.out.println("Mean States: " + nf.format(StatUtils.mean(states)));
        System.out.println("Std Dev States: " + nf.format(StatUtils.sd(indegree)));
        System.out.println("Number of Confounders = " + numConfounders);
        System.out.println();

        System.out.println("numberOfModels = " + numberOfModels);
        System.out.println("sampleSize = " + sampleSize);
        System.out.println("alpha = " + chiSquareAlpha);
        System.out.println("RFCI used = " + rfciUsed);
        System.out.println("depth = " + depth);
//        System.out.println("seed = " + seed);
        System.out.println();

        // Perhaps generate 1000 samples from each DGCBN

        DataSet data = im.simulateData(sampleSize, false);

        List<Node> latents = getLatents(numConfounders, dag);
        removeLatentsFromData(latents, data, true);

        // Perhaps generate about 500 models in ProbRFCI. To get started, maybe just use 50 models.

        List<Graph> pags = new ArrayList<Graph>();

        IndTestProbabilistic test0 = new IndTestProbabilistic(data);

        Fci fci0 = new Fci(test0);
        fci0.setRFCI_Used(rfciUsed);
        fci0.setDepth(depth);

        for (int i = 0; i < numberOfModels; i++) {
            System.out.println("Model # " + (i + 1));
            Graph PAG_PROB = fci0.search();
            PAG_PROB = GraphUtils.replaceNodes(PAG_PROB, data.getVariables());

            if (!pags.contains(PAG_PROB)) {
                pags.add(PAG_PROB);
            }
        }

        List<Graph> pagProbs = new ArrayList<Graph>(pags);

//        //  Let PAG-CS be the PAG obtained by RFCI when using a ChiSq test.
//
        IndTestChiSquare test1 = new IndTestChiSquare(data, chiSquareAlpha);
        Fci fci1 = new Fci(test1);
        fci1.setRFCI_Used(rfciUsed);
        fci1.setDepth(depth);
        test1.startRecordingFacts();
        Graph PAG_CS = fci1.search();
        PAG_CS = GraphUtils.replaceNodes(PAG_CS, data.getVariables());

        if (!pags.contains(PAG_CS)) {
            pags.add(PAG_CS);
        }
//
//        // Let PAG-PR be the MAP PAG obtained by RFCI when having BC_Inference always return the test result
//        // that is the most probable
//
        ProbabilisticMAPIndependence test2 = new ProbabilisticMAPIndependence(data);

        Fci fci2 = new Fci(test2);
        fci2.setRFCI_Used(rfciUsed);
        fci2.setDepth(depth);
        Graph PAG_PR = fci2.search();
        PAG_PR = GraphUtils.replaceNodes(PAG_PR, data.getVariables());

        if (!pags.contains(PAG_PR)) {
            pags.add(PAG_PR);
        }
//
//        // Let PAG-True be the PAG obtained from the DGCBN.
//
        IndTestDSep test3 = new IndTestDSep(new EdgeListGraph(dag));
        List<Node> graphNodes = dag.getNodes();
        graphNodes.removeAll(latents);
        test3 = (IndTestDSep) test3.indTestSubset(graphNodes);
        Fci fci3 = new Fci(test3);
        fci3.setRFCI_Used(rfciUsed);
        fci3.setDepth(depth);
        test3.startRecordingFacts();
        Graph PAG_True = fci3.search();
        PAG_True = GraphUtils.replaceNodes(PAG_True, data.getVariables());

        if (!pags.contains(PAG_True)) {
            pags.add(PAG_True);
        }


        // Include PAG-CS, PAG-PR, and PAG-True in the hash table entries for ProbRFCI.

        for (IndependenceFact fact : test1.getFacts()) {
            fact = convertVars(fact, test0.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        for (IndependenceFact fact : test2.getH().keySet()) {
            fact = convertVars(fact, test0.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        for (IndependenceFact fact : test3.getFacts()) {
            fact = convertVars(fact, test0.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        Map<IndependenceFact, Double> H = test0.getH();

        System.out.println("\n# constraints in the domain of H: " + H.keySet().size() + "\n");

        // Normalize the probability scores by all the models in the ProbRFCI, including those 500 (or 50) sampled,
        // as well as PAG-CS, PAG-PR, and PAG-True, if they weren't sampled

        Map<Graph, Double> pagLnProbs = getLnProbs(pags, H);
        double lnQTotal = lnQTotal(pagLnProbs);

        Graph PAG_BS = getMaxPag(pagProbs, pagLnProbs);      // "Best sampled" PAG.
//        double maxLnProb = getLnProb(PAG_BS, H) - lnQTotal;

        for (int i = 0; i < pags.size(); i++) {
            Graph pag = pags.get(i);
            double lnQ = pagLnProbs.get(pag);
            double normalizedlnQ = lnQ - lnQTotal;

            System.out.print("lnQ = " + (lnQ) + " lnQTotal = " + lnQTotal + " normalized lnQ = " + normalizedlnQ);

            if (pag.equals(PAG_CS)) System.out.print("\tPAG_CS");
            if (pag.equals(PAG_PR)) System.out.print("\tPAG_PR");
            if (pag.equals(PAG_True)) System.out.print("\tPAG_True");
            if (pag.equals(PAG_BS)) System.out.print("\tPAG_BS");
            System.out.println();
        }


        // These normalized probabilities can be displayed in a small table:

        TextTable table = new TextTable(5, 3);

        table.setToken(1, 0, "PAG-CS");
        table.setToken(2, 0, "PAG-PR");
        table.setToken(3, 0, "PAG-BS");
        table.setToken(4, 0, "PAG-True");
        table.setToken(0, 1, "NPP");
        table.setToken(0, 2, "RPP");

        double pagCsNpp = (pagLnProbs.get(PAG_CS) - lnQTotal);
        double pagPrNpp = (pagLnProbs.get(PAG_PR) - lnQTotal);
        double pagBsNpp = (pagLnProbs.get(PAG_BS) - lnQTotal);
        double pagTrueNpp = (pagLnProbs.get(PAG_True) - lnQTotal);
//
//        double pagCsNpp = exp(((pagLnProbs.get(PAG_CS) - lnQTotal) / (double) sampleSize));
//        double pagPrNpp = exp(((pagLnProbs.get(PAG_PR) - lnQTotal) / (double) sampleSize));
//        double pagTrueNpp = exp(((pagLnProbs.get(PAG_True) - lnQTotal) / (double) sampleSize));
//
//
        double pagCsRpp = (pagLnProbs.get(PAG_CS));
        double pagPrRpp = (pagLnProbs.get(PAG_PR));
        double pagBsRpp = (pagLnProbs.get(PAG_BS));
        double pagTrueRpp = (pagLnProbs.get(PAG_True));
//
//        double pagCsRpp = exp((pagLnProbs.get(PAG_CS) / (double) sampleSize));
//        double pagPrRpp = exp((pagLnProbs.get(PAG_PR) / (double) sampleSize));
//        double pagTrueRpp = exp((pagLnProbs.get(PAG_True) / (double) sampleSize));

//        NumberFormat nf2 = new DecimalFormat("0.0000");
        NumberFormat nf2 = new DecimalFormat("0.0E0");

        table.setToken(1, 1, nf2.format(pagCsNpp));
        table.setToken(2, 1, nf2.format(pagPrNpp));
        table.setToken(3, 1, nf2.format(pagBsNpp));
        table.setToken(4, 1, nf2.format(pagTrueNpp));

        table.setToken(1, 2, nf2.format(pagCsRpp));
        table.setToken(2, 2, nf2.format(pagPrRpp));
        table.setToken(3, 2, nf2.format(pagBsRpp));
        table.setToken(4, 2, nf2.format(pagTrueRpp));

        System.out.println("\n" + table);

        // Misclassification matrix for each DGCBN

        // Generate a matrix for each of PAG-CS and PAG-PR

        // The entries are counts that have been normalized by the total counts over all the
        // cells, except the null-null cell, which likely will dominate the number of counts;
        // we can report it separately in the caption of the table as "fraction of null-null
        // counts was 0.97", for example
        printEndpointMisclassifications("PAG_CS", data, PAG_CS, PAG_True);
        printEndpointMisclassifications("PAG_PR", data, PAG_PR, PAG_True);
        printEndpointMisclassifications("PAG_BS", data, PAG_BS, PAG_True);

        printEdgeMisclassifiations("PAG_CS", data, PAG_CS, PAG_True);
        printEdgeMisclassifiations("PAG_PR", data, PAG_PR, PAG_True);
        printEdgeMisclassifiations("PAG_BS", data, PAG_BS, PAG_True);

//        influenceOfSampleSize(im, latents, test2, PAG_PR);

        int numBins = 10;
        int[] nCountSummary = new int[numBins];
        int[] dCountSummary = new int[numBins];

        List<Node> _nodes = data.getVariables();
        Endpoint end1, end2;

        for (int k = 0; k < 3; k++) {
            if (k == 0) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.CIRCLE;
            } else if (k == 1) {
                end1 = Endpoint.ARROW;
                end2 = Endpoint.ARROW;
            } else if (k == 2) {
                end1 = Endpoint.NULL; // interpreted as null
                end2 = Endpoint.NULL;
            } else {
                throw new IllegalStateException();
            }

            Graph complete = new EdgeListGraph(_nodes);
            complete.fullyConnect(Endpoint.TAIL);

            int[] nCount = new int[numBins];
            int[] dCount = new int[numBins];

            for (Edge _edge : complete.getEdges()) {
                Node n1 = _edge.getNode1();
                Node n2 = _edge.getNode2();

                Edge edge = new Edge(n1, n2, end1, end2);

                double p = edgeFrequency(edge, pagProbs);
                int bin = (int) floor(p * numBins);

                if (bin == numBins) bin = numBins - 1;

                int count;

                if (PAG_True.containsEdge(edge) || ((end1 == Endpoint.NULL && end2 == Endpoint.NULL && !PAG_True.isAdjacentTo(n1, n2)))) {
                    count = 1;
                } else {
                    count = 0;
                }

                nCount[bin] = nCount[bin] + count;
                dCount[bin] = dCount[bin] + 1;

                nCountSummary[bin] = nCountSummary[bin] + count;
                dCountSummary[bin] = dCountSummary[bin] + 1;
            }

            double[] binHeight = new double[numBins];

            for (int i = 0; i < numBins; i++) {
                binHeight[i] = nCount[i] / (double) dCount[i];
            }

            System.out.println("\n" + end1 + "-" + end2);

            for (int i = 0; i < numBins; i++) {
                System.out.println((i + 1) + "\t" + nCount[i] + "\t" + dCount[i] + "\t" + (Double.isNaN(binHeight[i]) ? "-" : nf.format(binHeight[i])));
            }
        }

        for (int k = 0; k < 2; k++) {
            if (k == 0) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.ARROW;
            } else if (k == 1) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.ARROW;
            } else {
                throw new IllegalStateException();
            }

            Graph complete = new EdgeListGraph(_nodes);
            complete.fullyConnect(Endpoint.TAIL);

            int[] nCount = new int[numBins];
            int[] dCount = new int[numBins];

            for (Edge _edge : complete.getEdges()) {
                Node n1 = _edge.getNode1();
                Node n2 = _edge.getNode2();

                Edge edge = new Edge(n1, n2, end1, end2);
                Edge backEdge = new Edge(n1, n2, end2, end1);

                double p = edgeFrequency(edge, pagProbs);
                int bin = (int) floor(p * numBins);

                if (bin == numBins) bin = numBins - 1;

                int count;

                if (PAG_True.containsEdge(edge)) {
                    count = 1;
                } else {
                    count = 0;
                }

                nCount[bin] = nCount[bin] + count;
                dCount[bin] = dCount[bin] + 1;

                nCountSummary[bin] = nCountSummary[bin] + count;
                dCountSummary[bin] = dCountSummary[bin] + 1;
            }

            double[] binHeight = new double[numBins];

            for (int i = 0; i < numBins; i++) {
                binHeight[i] = nCount[i] / (double) dCount[i];
            }

            System.out.println("\n" + end1 + "-" + end2);

            for (int i = 0; i < numBins; i++) {
                System.out.println((i + 1) + "\t" + nCount[i] + "\t" + dCount[i] + "\t" +
                        (Double.isNaN(binHeight[i]) ? "-" : nf.format(binHeight[i])));
            }
        }

        double[] binHeightSummary = new double[numBins];

        for (int i = 0; i < numBins; i++) {
            binHeightSummary[i] = nCountSummary[i] / (double) dCountSummary[i];
        }

        System.out.println("\nSUMMARY");

        for (int i = 0; i < numBins; i++) {
            System.out.println((i + 1) + "\t" + nCountSummary[i] + "\t" + dCountSummary[i] + "\t" +
                    (Double.isNaN(binHeightSummary[i]) ? "-" : nf.format(binHeightSummary[i])));
        }

        System.out.println("===Graphs===");
        System.out.println("\nTrue DAG = " + dag);
        System.out.println("\nTrue " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + PAG_True);
        System.out.println("\nChi Square " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + PAG_CS);
        System.out.println("\nBest Score " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + PAG_BS);
//        System.out.println("normalisedPagLnProb = " + maxLnProb);
//        System.out.println("normalisedProb = " + exp(maxLnProb));

        long stop = System.currentTimeMillis();

        System.out.println("\nElapsed " + (stop - start) / 1000L + " seconds");

    }

    // Influence of Sample Size on the Posterior Probability of PAG-PR
    private void influenceOfSampleSize(BayesIm im, List<Node> latents, ProbabilisticMAPIndependence test2, Graph PAG_PR) {
        for (int _sampleSize : new int[]{1000, 2000, 3000, 4000, 5000, 10000}) {
            DataSet data2 = im.simulateData(_sampleSize, false);
            removeLatentsFromData(latents, data2, false);
            List<Graph> pags2 = new ArrayList<Graph>();

            IndTestProbabilistic test4 = new IndTestProbabilistic(data2);

//            Fci fci4 = new Fci(test4);
//            fci4.setRFCI_Used(rfciUsed);
//            fci4.setDepth(depth);
//
//            for (int i = 0; i < numberOfModels; i++) {
////                System.out.println("Model # " + (i + 1));
//                Graph PAG_PROB2 = fci4.search();
//                PAG_PROB2 = GraphUtils.replaceNodes(PAG_PROB2, data2.getVariables());
//
//                if (!pags.contains(PAG_PROB2)) {
//                    pags.add(PAG_PROB2);
//                }
//            }

            ProbabilisticMAPIndependence test5 = new ProbabilisticMAPIndependence(data2);

//            Fci fci5 = new Fci(test5);
//            fci5.setRFCI_Used(rfciUsed);
//            fci5.setDepth(depth);
//            Graph PAG_PR5 = fci5.search();
            Graph PAG_PR5 = new EdgeListGraph(PAG_PR);
            PAG_PR5 = GraphUtils.replaceNodes(PAG_PR5, data2.getVariables());

            if (!pags2.contains(PAG_PR5)) {
                pags2.add(PAG_PR5);
            }

            for (IndependenceFact fact : test2.getH().keySet()) {
                fact = convertVars(fact, test4.getVariables());
                test4.isIndependent(fact.getX(), fact.getY(), fact.getZ());
            }

            Map<IndependenceFact, Double> H2 = test4.getH();

            double lnProb = getLnProb(PAG_PR5, H2);
            System.out.println("Sample size = " + _sampleSize + " lnProb / n = " + (1. / _sampleSize) * lnProb +
                    " exp(lnProb / n) = " + exp(((1. / _sampleSize) * lnProb)));
//            System.out.println("Sample size = " + _sampleSize + " lnProb = " + exp(((1./_sampleSize) * lnProb)));
//            System.out.println("Sample size = " + _sampleSize + " lnProb = " + lnProb);
        }
    }

    private double edgeFrequency(Edge e, List<Graph> pagProbs) {
        int count = 0;

        if (!pagProbs.get(0).containsNode(e.getNode1())) throw new IllegalArgumentException();
        if (!pagProbs.get(0).containsNode(e.getNode2())) throw new IllegalArgumentException();

        for (Graph pag : pagProbs) {
            if (e.getEndpoint1() == Endpoint.NULL && e.getEndpoint2() == Endpoint.NULL) {
                if (!pag.isAdjacentTo(e.getNode1(), e.getNode2())) count++;
            } else {
                if (pag.containsEdge(e)) count++;
            }
        }

        return count / (double) pagProbs.size();
    }

    private void removeLatentsFromData(List<Node> latents, DataSet data, boolean verbose) {
        for (int j = 0; j < latents.size(); j++) {
            Node n = latents.get(j);
            data.removeColumn(data.getVariable(n.getName()));

            if (verbose) {
                System.out.println("Data: Removing latent " + n + ".");
            }
        }
    }

    private List<Node> getLatents(int numLatents, Dag dag) {
        List<Node> commonCauses = getCommonCauses(dag);
        System.out.println("There are " + commonCauses.size() + " common causes.");

//        Collections.shuffle(commonCauses, shuffleRandom);
        Collections.shuffle(commonCauses);
        List<Node> latents = new ArrayList<Node>();

        for (int i = 0; i < numLatents; i++) {
            Node n = commonCauses.get(i);
            latents.add(n);
            System.out.println("Setting " + n + " to latent.");
        }

        return latents;
    }

    private void printEndpointMisclassifications(String name, DataSet data, Graph PAG_CS, Graph PAG_True) {
        int[][] counts = new int[4][4];
        List<Node> _nodes = data.getVariables();

        for (int i = 0; i < _nodes.size(); i++) {
            for (int j = 0; j < _nodes.size(); j++) {
                if (i == j) continue;

                Endpoint endpoint1 = PAG_True.getEndpoint(_nodes.get(i), _nodes.get(j));
                Endpoint endpoint2 = PAG_CS.getEndpoint(_nodes.get(i), _nodes.get(j));

                int index1 = getIndex(endpoint1);
                int index2 = getIndex(endpoint2);

                counts[index1][index2]++;
            }
        }

        TextTable table2 = new TextTable(5, 5);

        table2.setToken(0, 1, "-o");
        table2.setToken(0, 2, "->");
        table2.setToken(0, 3, "--");
        table2.setToken(0, 4, "NULL");
        table2.setToken(1, 0, "-o");
        table2.setToken(2, 0, "->");
        table2.setToken(3, 0, "--");
        table2.setToken(4, 0, "NULL");

        int sum = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 && j == 3) continue;
                else sum += counts[i][j];
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 && j == 3) table2.setToken(i + 1, j + 1, "XXXXX");
                else table2.setToken(i + 1, j + 1, nf.format(counts[i][j] / (double) sum));
            }
        }

        System.out.println("\n" + name);
        System.out.println(table2);
        System.out.println();
    }

    private void printEdgeMisclassifiations(String name, DataSet data, Graph estGraph, Graph trueGraph) {
        List<Node> _nodes = data.getVariables();

        Node a = new GraphNode("a");
        Node b = new GraphNode("b");

        List<Edge> trueEdgeTypes = new ArrayList<Edge>();

        trueEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.CIRCLE));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.ARROW));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.CIRCLE));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.ARROW));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.TAIL));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.ARROW));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.NULL, Endpoint.NULL));

        List<Edge> estEdgeTypes = new ArrayList<Edge>();

        estEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.CIRCLE));
        estEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.ARROW));
        estEdgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.ARROW));
        estEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.ARROW));
        estEdgeTypes.add(new Edge(a, b, Endpoint.NULL, Endpoint.NULL));

        int[][] counts = new int[7][5];
        Graph graph = new EdgeListGraph(_nodes);
        graph.fullyConnect(Endpoint.TAIL);

        for (int m = 0; m < 7; m++) {
            for (int n = 0; n < 5; n++) {
                for (Edge fullEdge : graph.getEdges()) {
                    if (m == 2 || m == 4) {
                        Node x = fullEdge.getNode1();
                        Node y = fullEdge.getNode2();

                        Edge true1 = trueGraph.getEdge(x, y);
                        if (true1 == null) true1 = new Edge(x, y, Endpoint.NULL, Endpoint.NULL);
                        true1 = true1.reverse();

                        Edge est1 = estGraph.getEdge(x, y);
                        if (est1 == null) est1 = new Edge(x, y, Endpoint.NULL, Endpoint.NULL);

                        Edge trueEdgeType = trueEdgeTypes.get(m);
                        Edge estEdgeType = estEdgeTypes.get(n);

                        Edge trueConvert = new Edge(x, y, trueEdgeType.getEndpoint1(), trueEdgeType.getEndpoint2());
                        Edge estConvert = new Edge(x, y, estEdgeType.getEndpoint1(), estEdgeType.getEndpoint2());

                        boolean equals = true1.equals(trueConvert) && est1.equals(estConvert);// && true1.equals(est1);
                        if (equals) counts[m][n]++;
                    } else {
                        Node x = fullEdge.getNode1();
                        Node y = fullEdge.getNode2();

                        Edge true1 = trueGraph.getEdge(x, y);
                        if (true1 == null) true1 = new Edge(x, y, Endpoint.NULL, Endpoint.NULL);

                        Edge est1 = estGraph.getEdge(x, y);
                        if (est1 == null) est1 = new Edge(x, y, Endpoint.NULL, Endpoint.NULL);

                        Edge trueEdgeType = trueEdgeTypes.get(m);
                        Edge estEdgeType = estEdgeTypes.get(n);

                        Edge trueConvert = new Edge(x, y, trueEdgeType.getEndpoint1(), trueEdgeType.getEndpoint2());
                        Edge estConvert = new Edge(x, y, estEdgeType.getEndpoint1(), estEdgeType.getEndpoint2());

                        boolean equals = true1.equals(trueConvert) && est1.equals(estConvert);// && true1.equals(est1);
                        if (equals) counts[m][n]++;
                    }
                }
            }
        }

        TextTable table2 = new TextTable(8, 6);

        table2.setToken(1, 0, "o-o");
        table2.setToken(2, 0, "o->");
        table2.setToken(3, 0, "<-o");
        table2.setToken(4, 0, "-->");
        table2.setToken(5, 0, "<--");
        table2.setToken(6, 0, "<->");
        table2.setToken(7, 0, "null");
        table2.setToken(0, 1, "o-o");
        table2.setToken(0, 2, "o->");
        table2.setToken(0, 3, "-->");
        table2.setToken(0, 4, "<->");
        table2.setToken(0, 5, "null");

        int sum = 0;

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 6 && j == 4f) continue;
                else sum += counts[i][j];
            }
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 6 && j == 4) table2.setToken(i + 1, j + 1, "XXXXX");
                else table2.setToken(i + 1, j + 1, nf.format(counts[i][j] / (double) sum));
            }
        }

        System.out.println("\n" + name);
        System.out.println(table2);
        System.out.println();
    }

    private int getIndex(Endpoint endpoint) {
        if (endpoint == Endpoint.CIRCLE) return 0;
        if (endpoint == Endpoint.ARROW) return 1;
        if (endpoint == Endpoint.TAIL) return 2;
        if (endpoint == null) return 3;
        throw new IllegalArgumentException();
    }

    private Graph getMaxPag(List<Graph> pags, Map<Graph, Double> pagLnProbs) {
        double maxLnProb = Double.NEGATIVE_INFINITY;
        Graph maxPag = null;

        for (int n = 0; n < pags.size(); n++) {
            Graph _pag = pags.get(n);
            double lnProb = pagLnProbs.get(_pag);

            if (lnProb > maxLnProb) {
                maxPag = _pag;
                maxLnProb = lnProb;
            }
        }

        return maxPag;
    }

    private double getMaxLnProb(List<Graph> pags, Map<Graph, Double> pagLnProbs) {
        double maxLnProb = Double.NEGATIVE_INFINITY;

        for (int n = 0; n < pags.size(); n++) {
            Graph _pag = pags.get(n);
            double lnProb = pagLnProbs.get(_pag);

            if (lnProb > maxLnProb) {
                maxLnProb = lnProb;
            }
        }

        return maxLnProb;
    }

    private IndependenceFact convertVars(IndependenceFact fact, List<Node> variables) {
        Node x = null;

        for (Node node : variables) {
            if (fact.getX().getName().equals(node.getName())) {
                x = node;
            }
        }

        Node y = null;

        for (Node node : variables) {
            if (fact.getY().getName().equals(node.getName())) {
                y = node;
            }
        }

        List<Node> z = new ArrayList<Node>();

        for (Node _z : fact.getZ()) {
            for (Node node : variables) {
                if (node.getName().equals(_z.getName())) {
                    z.add(node);
                    break;
                }
            }
        }

        return new IndependenceFact(x, y, z);
    }

    private Map<Graph, Double> getLnProbs(List<Graph> pags, Map<IndependenceFact, Double> H) {
        Map<Graph, Double> pagLnProb = new HashMap<Graph, Double>();

        for (int i = 0; i < pags.size(); i++) {
            Graph pag = pags.get(i);
            double lnQ = getLnProb(pag, H);
            pagLnProb.put(pag, lnQ);
        }

        return pagLnProb;
    }

    private double getLnProb(Graph pag, Map<IndependenceFact, Double> H) {
        double lnQ = 0;

        for (IndependenceFact fact : H.keySet()) {
            BCInference.OP op;

            if (pag.isDSeparatedFrom(fact.getX(), fact.getY(), fact.getZ())) {
                op = BCInference.OP.independent;
            } else {
                op = BCInference.OP.dependent;
            }

            double p = H.get(fact);

            if (p < 0 || p > 1 || Double.isNaN(p) || Double.isInfinite(p)) {
                throw new IllegalArgumentException("p illegally equals " + p);
            }

            if (op == BCInference.OP.dependent) {
                p = 1.0 - p;
            }

            double v = lnQ + log(p);

            if (Double.isNaN(v) || Double.isInfinite(v)) {
                continue;
            }

            lnQ = v;
        }
        return lnQ;
    }

    private double lnQTotal(Map<Graph, Double> pagLnProb) {
        Set<Graph> pags = pagLnProb.keySet();
        Iterator<Graph> iter = pags.iterator();
        double lnQTotal = pagLnProb.get(iter.next());

        while (iter.hasNext()) {
            Graph pag = iter.next();
            double lnQ = pagLnProb.get(pag);
            lnQTotal = lnXplusY(lnQTotal, lnQ);
        }

        return lnQTotal;
    }

    private List<Node> getCommonCauses(Dag dag) {
        List<Node> commonCauses = new ArrayList<Node>();
        List<Node> nodes = dag.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            List<Node> children = dag.getChildren(node);
            boolean found = children.size() >= 2;

            if (found && !commonCauses.contains(node)) {
                commonCauses.add(node);
            }
        }

        return commonCauses;
    }

    public void loop1b() {
        int numberOfModels = 500;
        int sampleSize = 1000;
        double chiSquareAlpha = .2;
        boolean rfciUsed = true;
        int depth = -1;
        int numLatents = 5;

//        BayesIm im = getRandomBayesIm(50, 50);
        BayesIm im = getBayesIM("Alarm");
//        BayesIm im = getBayesI("Hailfinder");
//        BayesIm im = getBayesIM("Hepar");
//        BayesIm im = getBayesIM("Win95");
//        BayesIm im = getBayesIM("Barley");

        BayesPm pm = im.getBayesPm();
        Dag dag = pm.getDag();

        List<Node> latents = getLatents(numLatents, dag);

        List<Node> vars = dag.getNodes();
        vars.removeAll(latents);

        int numNodes = dag.getNumNodes();
        int numEdges = dag.getNumEdges();

        System.out.println("numberOfModels = " + numberOfModels);
        System.out.println("sampleSize = " + sampleSize);
        System.out.println("numNodes = " + numNodes);
        System.out.println("numEdges = " + numEdges);
        System.out.println("alpha = " + chiSquareAlpha);
        System.out.println("RFCI used = " + rfciUsed);
        System.out.println("depth = " + depth);
        System.out.println("numLatents = " + numLatents);

        DataSet data = im.simulateData(sampleSize, false);

        removeLatentsFromData(latents, data, true);

        IndependenceTest test2 = new IndTestDSep(new EdgeListGraph(dag));
        test2 = test2.indTestSubset(vars);
        Fci fci1 = new Fci(test2);
        fci1.setRFCI_Used(rfciUsed);
        fci1.setDepth(depth);
        fci1.setVerbose(false);
        Graph pag_GOLD = fci1.search();

        IndependenceTest testChiSqaure = new IndTestChiSquare(data, chiSquareAlpha);
        Fci fci2 = new Fci(testChiSqaure);
        fci2.setRFCI_Used(true);
        fci2.setVerbose(false);
        fci2.setDepth(depth);
        Graph pag_CHISQUARE = fci2.search();

        TextTable table = new TextTable(6, 9);
        table.setToken(1, 0, "FP");
        table.setToken(2, 0, "FN");
        table.setToken(3, 0, "Correct");
        table.setToken(4, 0, "Precision");
        table.setToken(5, 0, "Recall");
        Endpoint end1;
        Endpoint end2;

        for (int k = 0; k < 8; k++) {
            if (k == 0) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.ARROW;
            } else if (k == 1) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.ARROW;
            } else if (k == 2) {
                end1 = Endpoint.ARROW;
                end2 = Endpoint.ARROW;
            } else if (k == 3) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.CIRCLE;
            } else if (k == 4) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.CIRCLE;
            } else if (k == 5) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.TAIL;
            } else if (k == 6) {
                end1 = null;
                end2 = null;
            } else if (k == 7) {
                end1 = Endpoint.NULL;
                end2 = Endpoint.NULL;
            } else {
                throw new IllegalStateException();
            }

            Graph graph1 = pag_CHISQUARE;
            Graph graph2 = pag_GOLD;

            graph1 = GraphUtils.replaceNodes(graph1, graph2.getNodes());

            int edgeFp = edgesComplement(graph1, graph2, end1, end2);
            int edgeFn = edgesComplement(graph2, graph1, end1, end2);
            int edgeCorrect = edgesCorrect(graph2, graph1, end1, end2);
            double precision = edgeCorrect / (double) (edgeCorrect + edgeFp);
            double recall = edgeCorrect / (double) (edgeCorrect + edgeFn);

            table.setToken(0, k + 1, edgeString(end1, end2));
            table.setToken(1, k + 1, Integer.toString(edgeFp));
            table.setToken(2, k + 1, Integer.toString(edgeFn));
            table.setToken(3, k + 1, Integer.toString(edgeCorrect));
            table.setToken(4, k + 1, format(nf, precision));
            table.setToken(5, k + 1, format(nf, recall));
        }

        System.out.println("\nSTATS FOR CHI SQUARE " + (rfciUsed ? "RFCI-PAG" : "PAG"));
        System.out.println("\n" + table);

        ProbabilisticMAPIndependence testProbabilistic = new ProbabilisticMAPIndependence(data);

        Fci fci3 = new Fci(testProbabilistic);
        fci3.setRFCI_Used(rfciUsed);
        fci3.setVerbose(false);
        fci3.setDepth(depth);

        int[] fp = new int[8];
        int[] fn = new int[8];
        int[] correct = new int[8];
        double[] prec = new double[8];
        double[] rec = new double[8];

        List<Graph> pags = new ArrayList<Graph>();

        for (int i = 0; i < numberOfModels; i++) {
            System.out.println("Model # " + (i + 1));
            Graph pag_PROB = fci3.search();
            pags.add(pag_PROB);
        }

        Map<IndependenceFact, Double> H = testProbabilistic.getH();

        System.out.println("\n# constraints in the domain of H: " + H.keySet().size());

        double lnQTotal = Double.NaN;
        Map<Graph, Double> pagLnProb = new HashMap<Graph, Double>();

        for (int i = 0; i < numberOfModels; i++) {
//            System.out.println("Model # " + (i + 1));

            Graph pag_PROB = pags.get(i);

            double lnQ = 0;

            for (IndependenceFact fact : H.keySet()) {
                BCInference.OP op;

                if (pag_PROB.isDSeparatedFrom(fact.getX(), fact.getY(), fact.getZ())) {
                    op = BCInference.OP.independent;
                } else {
                    op = BCInference.OP.dependent;
                }

                double p = H.get(fact);

                if (op == BCInference.OP.dependent) {
                    p = 1.0 - p;
                }

                if (p == 0) continue;

                if (p > 1.0) throw new IllegalArgumentException();

                lnQ += log(p);

            }

            pagLnProb.put(pag_PROB, lnQ);

            if (Double.isNaN(lnQTotal)) lnQTotal = lnQ;
            else lnQTotal = lnXplusY(lnQTotal, lnQ);
        }

        Map<Graph, Double> normalizedPagLnProb = new HashMap<Graph, Double>();

        for (int i = 0; i < pags.size(); i++) {
            Graph pag = pags.get(i);
            double lnQ = pagLnProb.get(pag);

            normalizedPagLnProb.put(pag, lnQ - lnQTotal);
        }

        for (int i = 0; i < numberOfModels; i++) {
            for (int k = 0; k < 8; k++) {
                if (k == 0) {
                    end1 = Endpoint.TAIL;
                    end2 = Endpoint.ARROW;
                } else if (k == 1) {
                    end1 = Endpoint.CIRCLE;
                    end2 = Endpoint.ARROW;
                } else if (k == 2) {
                    end1 = Endpoint.ARROW;
                    end2 = Endpoint.ARROW;
                } else if (k == 3) {
                    end1 = Endpoint.TAIL;
                    end2 = Endpoint.CIRCLE;
                } else if (k == 4) {
                    end1 = Endpoint.CIRCLE;
                    end2 = Endpoint.CIRCLE;
                } else if (k == 5) {
                    end1 = Endpoint.TAIL;
                    end2 = Endpoint.TAIL;
                } else if (k == 6) {
                    end1 = null;
                    end2 = null;
                } else if (k == 7) {
                    end1 = Endpoint.NULL;
                    end2 = Endpoint.NULL;
                } else {
                    throw new IllegalStateException();
                }

                Graph pag_PROB = pags.get(i);
                pag_PROB = GraphUtils.replaceNodes(pag_PROB, pag_GOLD.getNodes());

                Graph graph1 = pag_PROB;
                Graph graph2 = pag_GOLD;

                int edgeFp = edgesComplement(graph1, graph2, end1, end2);
                int edgeFn = edgesComplement(graph2, graph1, end1, end2);
                int edgeCorrect = edgesCorrect(graph1, graph2, end1, end2);
                double precision = edgeCorrect / (double) (edgeCorrect + edgeFp);
                double recall = edgeCorrect / (double) (edgeCorrect + edgeFn);

                fp[k] += edgeFp;
                fn[k] += edgeFn;
                correct[k] += edgeCorrect;
                prec[k] += precision;
                rec[k] += recall;
            }
        }

        for (int k = 0; k < 8; k++) {
            if (k == 0) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.ARROW;
            } else if (k == 1) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.ARROW;
            } else if (k == 2) {
                end1 = Endpoint.ARROW;
                end2 = Endpoint.ARROW;
            } else if (k == 3) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.CIRCLE;
            } else if (k == 4) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.CIRCLE;
            } else if (k == 5) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.TAIL;
            } else if (k == 6) {
                end1 = null;
                end2 = null;
            } else if (k == 7) {
                end1 = Endpoint.NULL;
                end2 = Endpoint.NULL;
            } else {
                throw new IllegalStateException();
            }

            double avgFp = fp[k] / (double) numberOfModels;
            double avgFn = fn[k] / (double) numberOfModels;
            double avgCorrect = correct[k] / (double) numberOfModels;
            double avgPrec = prec[k] / (double) numberOfModels;
            double avgRec = rec[k] / (double) numberOfModels;

            table.setToken(0, k + 1, edgeString(end1, end2));
            table.setToken(1, k + 1, format(nf, avgFp));
            table.setToken(2, k + 1, format(nf, avgFn));
            table.setToken(3, k + 1, format(nf, avgCorrect));
            table.setToken(4, k + 1, format(nf, avgPrec));
            table.setToken(5, k + 1, format(nf, avgRec));

        }

        System.out.println("\nSTATS FOR PROBABILISTIC " + (rfciUsed ? "RFCI-PAG" : "PAG") + "S (AVERAGED)");
        System.out.println("\n" + table);

        Graph maxPag = null;
        double maxLnProb = Double.NEGATIVE_INFINITY;

        for (int n = 0; n < pags.size(); n++) {
            Graph _pag = pags.get(n);
            double lnProb = normalizedPagLnProb.get(_pag);

            if (lnProb > maxLnProb) {
                maxPag = _pag;
                maxLnProb = lnProb;
            }
        }

        for (int k = 0; k < 8; k++) {
            if (k == 0) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.ARROW;
            } else if (k == 1) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.ARROW;
            } else if (k == 2) {
                end1 = Endpoint.ARROW;
                end2 = Endpoint.ARROW;
            } else if (k == 3) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.CIRCLE;
            } else if (k == 4) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.CIRCLE;
            } else if (k == 5) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.TAIL;
            } else if (k == 6) {
                end1 = null;
                end2 = null;
            } else if (k == 7) {
                end1 = Endpoint.NULL;
                end2 = Endpoint.NULL;
            } else {
                throw new IllegalStateException();
            }

            Graph graph1 = maxPag;
            Graph graph2 = pag_GOLD;

            graph1 = GraphUtils.replaceNodes(graph1, graph2.getNodes());
            int edgeFp = edgesComplement(graph1, graph2, end1, end2);
            int edgeFn = edgesComplement(graph2, graph1, end1, end2);
            int edgeCorrect = edgesCorrect(graph1, graph2, end1, end2);
            double precision = edgeCorrect / (double) (edgeCorrect + edgeFp);
            double recall = edgeCorrect / (double) (edgeCorrect + edgeFn);

            table.setToken(0, k + 1, edgeString(end1, end2));
            table.setToken(1, k + 1, Integer.toString(edgeFp));
            table.setToken(2, k + 1, Integer.toString(edgeFn));
            table.setToken(3, k + 1, Integer.toString(edgeCorrect));
            table.setToken(4, k + 1, format(nf, precision));
            table.setToken(5, k + 1, format(nf, recall));
        }

        System.out.println("\nSTATS FOR MAX PAG USING PROBABILISTIC FCI");
        System.out.println("\n" + table);

        System.out.println("===Graphs===");
        System.out.println("\nTrue DAG = " + dag);
        System.out.println("\nTrue " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + pag_GOLD);
        System.out.println("\nChi Square " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + pag_CHISQUARE);
        System.out.println("\nProbabilistic " + (rfciUsed ? "RFCI-PAG" : "PAG") + " = " + maxPag);
        System.out.println("normalisedPagLnProb = " + maxLnProb);
        System.out.println("normalisedProb = " + exp(maxLnProb));
    }

    public void loop2() {
        int depth = -1;

        //        Dag dag = GraphUtils.randomDag(100, 100, true);
        //        BayesPm pm = new BayesPm(dag);
        //        BayesIm im = new MlBayesIm(pm, MlBayesIm.RANDOM);


//        BayesIm im = loadBayesIm("alarm.xdsl", true);
//        BayesIm im = loadBayesIm("hailfinder.xdsl", false);
//        BayesIm im = loadBayesIm("Hepar II.xdsl", true);
//        depth = 5;
//        BayesIm im = loadBayesIm("win95pts.xdsl", false);
        BayesIm im = loadBayesIm("barley.xdsl", false);

        for (Node node : im.getVariables()) {
            System.out.print(((DiscreteVariable) node).getNumCategories() + " ");
        }

        BayesPm pm = im.getBayesPm();
        Dag dag = pm.getDag();

        List<Node> latents = getLatents(5, dag);

        List<Node> vars = dag.getNodes();
        vars.removeAll(latents);

        long start1 = System.currentTimeMillis();

        IndTestDSep test2 = new IndTestDSep(new EdgeListGraph(dag));
        test2 = (IndTestDSep) test2.indTestSubset(vars);
        Fci fci2 = new Fci(test2);
        fci2.setRFCI_Used(true);
        fci2.setDepth(depth);
        fci2.setVerbose(false);
        test2.startRecordingFacts();
        Graph pag_GOLD = fci2.search();
        long stop1 = System.currentTimeMillis();

        System.out.println("Dsep took " + (stop1 - start1) + "ms");

        long totalElapsed = 0L;
        int numRuns = 0;

        for (int i = 0; i < numRuns; i++) {
            long start = System.currentTimeMillis();

            DataSet dataSet = im.simulateData(1000, false);

            for (int j = 0; j < 5; j++) {
                Node n = latents.get(j);
                dataSet.removeColumn(dataSet.getVariable(n.getName()));
                System.out.println("Setting " + n + " to latent.");
            }

            IndTestChiSquare test = new IndTestChiSquare(dataSet, 0.1);
            Fci fci = new Fci(test);
            fci.setRFCI_Used(true);
            fci.setDepth(depth);
            test.startRecordingFacts();
            Graph pag_DATA = fci.search();

            pag_DATA = GraphUtils.replaceNodes(pag_DATA, pag_GOLD.getNodes());

            Endpoint end1;
            Endpoint end2;

            for (int k = 0; k < 8; k++) {
                if (k == 0) {
                    end1 = Endpoint.TAIL;
                    end2 = Endpoint.ARROW;
                } else if (k == 1) {
                    end1 = Endpoint.CIRCLE;
                    end2 = Endpoint.ARROW;
                } else if (k == 2) {
                    end1 = Endpoint.ARROW;
                    end2 = Endpoint.ARROW;
                } else if (k == 3) {
                    end1 = Endpoint.TAIL;
                    end2 = Endpoint.CIRCLE;
                } else if (k == 4) {
                    end1 = Endpoint.CIRCLE;
                    end2 = Endpoint.CIRCLE;
                } else if (k == 5) {
                    end1 = Endpoint.TAIL;
                    end2 = Endpoint.TAIL;
                } else if (k == 6) {
                    end1 = null;
                    end2 = null;
                } else if (k == 6) {

                    // Adjacent
                    end1 = null;
                    end2 = Endpoint.TAIL;
                } else {
                    throw new IllegalStateException();
                }

                System.out.println("\n   " + end1 + " --- " + end2);

                Graph graph1 = pag_GOLD;
                Graph graph2 = pag_DATA;

                int edgeFp = edgesComplement(graph1, graph2, end1, end2);
                int edgeFn = edgesComplement(graph2, graph1, end1, end2);
                int edgeCorrect = edgesCorrect(graph2, graph1, end1, end2);
                double precision = edgeCorrect / (double) (edgeCorrect + edgeFp);
                double recall = edgeCorrect / (double) (edgeCorrect + edgeFn);

                System.out.println("FP = " + edgeFp);
                System.out.println("FN = " + edgeFn);
                System.out.println("Correct = " + edgeCorrect);

                System.out.println("Precision = " + precision);
                System.out.println("Recall = " + recall);
            }

            long stop = System.currentTimeMillis();

            long elapsed = stop - start;
            totalElapsed += elapsed;
        }

        System.out.println("Average elapsed run " + totalElapsed / numRuns);
    }

    public static int edgesComplement(Graph graph1, Graph graph2, Endpoint end1, Endpoint end2) {
        graph1 = GraphUtils.replaceNodes(graph1, graph2.getNodes());

        if (end1 == null && end2 == null) {
            int count = 0;

            List<Node> nodes = graph1.getNodes();

            for (int i = 0; i < graph1.getNumNodes(); i++) {
                for (int j = i + 1; j < graph1.getNumNodes(); j++) {
                    Edge edge1 = graph1.getEdge(nodes.get(i), nodes.get(j));
                    Edge edge2 = graph2.getEdge(nodes.get(i), nodes.get(j));

                    if (edge1 == null && edge2 != null) {
                        count++;
                    }
                }
            }

            return count;
        } else if (end1 == Endpoint.NULL && end2 == Endpoint.NULL) {
            int count = 0;

            List<Node> nodes = graph1.getNodes();

            for (int i = 0; i < graph1.getNumNodes(); i++) {
                for (int j = i + 1; j < graph1.getNumNodes(); j++) {
                    Edge edge1 = graph1.getEdge(nodes.get(i), nodes.get(j));
                    Edge edge2 = graph2.getEdge(nodes.get(i), nodes.get(j));

                    if (edge1 != null && edge2 == null) {
                        count++;
                    }
                }
            }

            return count;
        } else {
            int count = 0;

            for (Edge edge1 : graph1.getEdges()) {
                if ((edge1.getEndpoint1() == end1 && edge1.getEndpoint2() == end2)
                        || (edge1.getEndpoint1() == end2 && edge1.getEndpoint2() == end1)) {
                    Edge edge2 = graph2.getEdge(edge1.getNode1(), edge1.getNode2());

                    if (!edge1.equals(edge2)) {
                        count++;
                    }
                }
            }

            return count;
//        } else {
//            int count = 0;
//
//            EDGE:
//            for (Edge edge1 : graph1.getEdges()) {
//                if ((edge1.getEndpoint1() == end1 && edge1.getEndpoint2() == end2)
//                        || (edge1.getEndpoint1() == end2 && edge1.getEndpoint2() == end1)) {
//                    edge1 = reverseEdge(edge1, end1);
//
//                    Edge edge2 = graph2.getEdge(edge1.getNode1(), edge1.getNode2());
//
//                    if (edge2 == null) {
//                        count++;
//                        continue EDGE;
//                    }
//
//                    edge2 = reverseEdge(edge2, end1);
//
//                    for (Endpoint end1a : getEndpoints(edge1.getEndpoint1())) {
//                        for (Endpoint end2a : getEndpoints(edge1.getEndpoint2())) {
//                            for (Endpoint end1b : getEndpoints(edge2.getEndpoint1())) {
//                                for (Endpoint end2b : getEndpoints(edge2.getEndpoint2())) {
//                                    if (end1a == end1b && end2a == end2b) {
////                                        System.out.println("edge1 = " + edge1 + " edge2 = " + edge2);
//                                        continue EDGE;
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    count++;
//
////                    if (!edge1.equals(edge2)) {
////                        count++;
////                    }
//                }
//            }
//
//            return count;
        }
    }

    private static Endpoint[] getEndpoints(Endpoint end1) {
        if (end1 == Endpoint.CIRCLE) {
            return new Endpoint[]{Endpoint.ARROW, Endpoint.TAIL, Endpoint.CIRCLE};
        } else {
            return new Endpoint[]{end1};
        }
    }

    private static Edge reverseEdge(Edge edge1, Endpoint end) {
        if (edge1.getEndpoint2() == end) {
            edge1 = new Edge(edge1.getNode2(), edge1.getNode1(), edge1.getEndpoint2(), edge1.getEndpoint1());
        }
        return edge1;
    }

    public static int edgesCorrect(Graph graph1, Graph graph2, Endpoint end1, Endpoint end2) {
        graph1 = GraphUtils.replaceNodes(graph1, graph2.getNodes());

        if (end1 == null && end2 == null) {
            int count = 0;

            List<Node> nodes = graph1.getNodes();

            for (int i = 0; i < graph1.getNumNodes(); i++) {
                for (int j = i + 1; j < graph1.getNumNodes(); j++) {
                    Edge edge1 = graph1.getEdge(nodes.get(i), nodes.get(j));
                    Edge edge2 = graph2.getEdge(nodes.get(i), nodes.get(j));

                    if (edge1 == null && edge2 == null) {
                        count++;
                    }
                }
            }

            return count;
        }

        if (end1 == Endpoint.NULL && end2 == Endpoint.NULL) {
            int count = 0;

            List<Node> nodes = graph1.getNodes();

            for (int i = 0; i < graph1.getNumNodes(); i++) {
                for (int j = i + 1; j < graph1.getNumNodes(); j++) {
                    Edge edge1 = graph1.getEdge(nodes.get(i), nodes.get(j));
                    Edge edge2 = graph2.getEdge(nodes.get(i), nodes.get(j));

                    if (edge1 != null && edge2 != null) {
                        count++;
                    }
                }
            }

            return count;
        } else {
            int count = 0;

            for (Edge edge1 : graph1.getEdges()) {
                if ((edge1.getEndpoint1() == end1 && edge1.getEndpoint2() == end2)
                        || (edge1.getEndpoint1() == end2 && edge1.getEndpoint2() == end1)) {
                    Edge edge2 = graph2.getEdge(edge1.getNode1(), edge1.getNode2());

                    if (edge1.equals(edge2)) {
                        count++;
                    }
                }
            }

            return count;
//        } else {
//            int count = 0;
//
//            EDGE:
//            for (Edge edge1 : graph1.getEdges()) {
//                if ((edge1.getEndpoint1() == end1 && edge1.getEndpoint2() == end2)
//                        || (edge1.getEndpoint1() == end2 && edge1.getEndpoint2() == end1)) {
//                    edge1 = reverseEdge(edge1, end1);
//
//                    Edge edge2 = graph2.getEdge(edge1.getNode1(), edge1.getNode2());
//
//                    if (edge2 == null) {
//                        count++;
//                        continue EDGE;
//                    }
//
//                    edge2 = reverseEdge(edge2, end1);
//
//                    for (Endpoint end1a : getEndpoints(edge1.getEndpoint1())) {
//                        for (Endpoint end2a : getEndpoints(edge1.getEndpoint2())) {
//                            for (Endpoint end1b : getEndpoints(edge2.getEndpoint1())) {
//                                for (Endpoint end2b : getEndpoints(edge2.getEndpoint2())) {
//                                    if (end1a == end1b && end2a == end2b) {
////                                        System.out.println("edge1 = " + edge1 + " edge2 = " + edge2);
//                                        count++;
//                                        continue EDGE;
//                                    }
//                                }
//                            }
//                        }
//                    }
//
////                    count++;
//
////                    if (!edge1.equals(edge2)) {
////                        count++;
////                    }
//                }
//            }
//
//            return count;
        }
    }

    private String format(NumberFormat nf, double precision) {
        return (Double.isNaN(precision) ? "*" : nf.format(precision));
    }

    // Prints constraints in domain of H and not in domain of H up to a given depth.
    private void printConstraintInDomain(List<Node> variables, Map<IndependenceFact, Double> h) {
        int _depth = 1;
        boolean inDomain = false;
        ChoiceGenerator gen = new ChoiceGenerator(variables.size(), 2);
        int[] choice;

        if (inDomain) {
            System.out.println("\nConstraints in domain of H depth = " + _depth + ":");
        } else {
            System.out.println("\nConstraints NOT in domain of H depth = " + _depth + ":");
        }

        while ((choice = gen.next()) != null) {
            Node x = variables.get(choice[0]);
            Node y = variables.get(choice[1]);

            List<Node> rest = new ArrayList<Node>(variables);
            rest.remove(x);
            rest.remove(y);

            DepthChoiceGenerator gen2 = new DepthChoiceGenerator(rest.size(), _depth);
            int[] choice2;

            while ((choice2 = gen2.next()) != null) {
                List<Node> z = GraphUtils.asList(choice2, rest);

                IndependenceFact fact = new IndependenceFact(x, y, z);

                if (inDomain) {
                    if (h.containsKey(fact)) {
                        System.out.println(fact);
                    }
                } else {
                    if (!h.containsKey(fact)) {
                        System.out.println(fact);
                    }
                }
            }
        }
    }

    private static final int MININUM_EXPONENT = -1022;

    /**
     * Takes ln(x) and ln(y) as input, and returns ln(x + y)
     *
     * @param lnX is natural log of x
     * @param lnY is natural log of y
     * @return natural log of x plus y
     */
    protected double lnXplusY(double lnX, double lnY) {
        double lnYminusLnX, temp;

        if (lnY > lnX) {
            temp = lnX;
            lnX = lnY;
            lnY = temp;
        }

        lnYminusLnX = lnY - lnX;

        if (lnYminusLnX < MININUM_EXPONENT) {
            return lnX;
        } else {
            double w = Math.log1p(exp(lnYminusLnX));
            return w + lnX;
        }
    }

    private BayesIm getRandomBayesIm(int numNodes, int numEdges) {
        Dag dag = GraphUtils.randomDag(numNodes, numEdges, false);
//        Dag dag = new Dag(TestGraph.weightedRandomGraph(numNodes, numEdges));
        BayesPm pm = new BayesPm(dag, 4, 4);
        return new MlBayesIm(pm, MlBayesIm.RANDOM);
    }

    private BayesIm getBayesIM(String type) {
        if ("Random".equals(type)) {
            return getRandomBayesIm(50, 50);
        } else if ("Alarm".equals(type)) {
            return loadBayesIm("alarm.xdsl", true);
        } else if ("Hailfinder".equals(type)) {
            return loadBayesIm("hailfinder.xdsl", false);
        } else if ("Hepar".equals(type)) {
            return loadBayesIm("Hepar II.xdsl", true);
        } else if ("Win95".equals(type)) {
            return loadBayesIm("win95pts.xdsl", false);
        } else if ("Barley".equals(type)) {
            return loadBayesIm("barley.xdsl", false);
        }

//        BayesIm im = loadBayesIm("hailfinder.xdsl", false);
//        BayesIm im = loadBayesIm("Hepar II.xdsl", true);
//        depth = 5;
//        BayesIm im = loadBayesIm("win95pts.xdsl", false);
//        BayesIm im = loadBayesIm("barley.xdsl", false);

        throw new IllegalArgumentException("Not a recogized Bayes IM type.");
    }

    private String edgeString(Endpoint end1, Endpoint end2) {
        if (end1 == null && end2 == null) {
            return "* *";
        }

        StringBuilder buf = new StringBuilder();

        if (end1 == Endpoint.TAIL) {
            buf.append("-");
        } else if (end1 == Endpoint.ARROW) {
            buf.append("<");
        } else if (end1 == Endpoint.CIRCLE) {
            buf.append("o");
        } else if (end1 == Endpoint.NULL) {
            buf.append("*");
        }

        buf.append("-");

        if (end2 == Endpoint.TAIL) {
            buf.append("-");
        } else if (end2 == Endpoint.ARROW) {
            buf.append(">");
        } else if (end2 == Endpoint.CIRCLE) {
            buf.append("o");
        } else if (end2 == Endpoint.NULL) {
            buf.append("*");
        }

        return buf.toString();
    }

    private BayesIm loadBayesIm(String filename, boolean useDisplayNames) {
        try {
            Builder builder = new Builder();
            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/cooper.spirtes.paper/xdsl/");
            File file = new File(dir, filename);
            Document document = builder.build(file);
            //            printDocument(document);

            XdslXmlParser parser = new XdslXmlParser();
            parser.setUseDisplayNames(useDisplayNames);
            return parser.getBayesIm(document.getRootElement());
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printDocument(Document document) {
        Serializer serializer = new Serializer(System.out);

        serializer.setLineSeparator("\n");
        serializer.setIndent(2);

        try {
            serializer.write(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loop3() {
//        double x = 3;
//        double y = 4;
//
        double x = 1e-49;
        double y = 1e-50;

        double lnX = log(x);
        double lnY = log(y);


        double lnXPlusY = log(x + y);

        double lnXPlusY2 = lnXplusY(lnX, lnY);

        System.out.println(lnX + " " + lnY + " " + lnXPlusY + " " + lnXPlusY2);
    }

    public void loop4() {
        int sampleSize = 1000;
        double alpha = 0.05;
        int _depth = -1;

        try {
            File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/cooper.spirtes.paper/" +
                    "comparison_alarm.2to3thousand.txt");
            File dataFile = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/cooper.spirtes.paper/" +
                    "data_alarm.comparison_alarm.2to3thousand.txt");

            PrintWriter out = new PrintWriter(file);
            PrintWriter outData = new PrintWriter(dataFile);

//            BayesIm im = getRandomBayesIm(50, 50);
            BayesIm im = getBayesIM("Alarm");
            DataSet data = im.simulateData(sampleSize, false);

            outData.println(data);
            outData.close();

            Dag dag = im.getDag();
            dag = new Dag(GraphUtils.replaceNodes(dag, data.getVariables()));
            IndTestChiSquare chiSquare = new IndTestChiSquare(data, alpha);
            IndTestProbabilistic probInd = new IndTestProbabilistic(data);

            List<Node> variables = data.getVariables();

            out.println("Variable\tindex");

            for (int i = 0; i < variables.size(); i++) {
                out.println(variables.get(i) + "\t" + (i + 1));
            }

            out.println("\n\n");

            Fci fci3 = new Fci(probInd);
            fci3.setRFCI_Used(true);
            fci3.setDepth(_depth);

            for (int i = 0; i < 50; i++) {
                out.println("Model # " + (i + 1));
                fci3.search();
            }

            Map<IndependenceFact, Double> H = probInd.getH();
            List<IndependenceFact> constraints = new ArrayList<IndependenceFact>(H.keySet());
            Collections.sort(constraints);

            out.println("PV\tPR\tChiSq\tProb\tDSep\tConstraint");

            for (IndependenceFact constraint : constraints) {
                boolean dsepHolds = dag.isDSeparatedFrom(constraint.getX(), constraint.getY(), constraint.getZ());
                boolean chiSqHolds = chiSquare.isIndependent(constraint.getX(), constraint.getY(), constraint.getZ());
                boolean probIndHolds = probInd.isIndependent(constraint.getX(), constraint.getY(), constraint.getZ());

                double PV = chiSquare.getPValue();
                double PR = probInd.getPosterior();

                out.print(nf.format(PV) + "\t" + nf.format(PR) + "\t" + (chiSqHolds ? 1 : 0) + "\t" +
                        (probIndHolds ? 1 : 0) + "\t" + (dsepHolds ? 1 : 0) + "\t");

                out.print((variables.indexOf(constraint.getX()) + 1) + "\t");
                out.print((variables.indexOf(constraint.getY()) + 1) + "\t");
                for (int i = 0; i < constraint.getZ().size(); i++) {
                    out.print(variables.indexOf(constraint.getZ().get(i)) + 1);

                    if (i < constraint.getZ().size() - 1) {
                        out.print("\t");
                    }
                }

                out.println();
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Test m-separataion against d-separation.
    public void loop5() {
        int numLatents = 5;
        int depth = 3;
        boolean rfci_used = true;

        Dag dag = GraphUtils.randomDag(20, 20, false);

        System.out.println(dag);

        List<Node> variables = dag.getNodes();

        List<Node> commonCauses = getCommonCauses(dag);

        Collections.shuffle(commonCauses);

        List<Node> latents = new ArrayList<Node>();

        for (int i = 0; i < numLatents; i++) {
            Node n = commonCauses.get(i);
            latents.add(n);
            System.out.println("Setting " + n + " to latent.");
            variables.remove(n);
        }

        IndependenceTest test2 = new IndTestDSep(new EdgeListGraph(dag));
        test2 = test2.indTestSubset(variables);
        Fci fci = new Fci(test2);
//        fci.setRFCI_Used(rfci_used);
        fci.setDepth(depth);
        Graph pag = fci.search();

        System.out.println(pag);

        ChoiceGenerator gen = new ChoiceGenerator(variables.size(), 2);
        int[] choice;

        while ((choice = gen.next()) != null) {
            Node x = variables.get(choice[0]);
            Node y = variables.get(choice[1]);

            List<Node> rest = new ArrayList<Node>(variables);
            rest.remove(x);
            rest.remove(y);

            DepthChoiceGenerator gen2 = new DepthChoiceGenerator(rest.size(), depth);
            int[] choice2;

            while ((choice2 = gen2.next()) != null) {
                List<Node> z = GraphUtils.asList(choice2, rest);

                IndependenceFact fact = new IndependenceFact(x, y, z);

                boolean mSep = pag.isDConnectedTo(fact.getX(), fact.getY(), fact.getZ());
                boolean dSep = dag.isDConnectedTo(fact.getX(), fact.getY(), fact.getZ());

                if (mSep != dSep) {
                    System.out.println(mSep + " " + dSep + " " + fact);
                }
            }
        }
    }

    public static void main(String[] args) {
        new ExploreCooperSpirtesPaper2().loop1();
    }
}