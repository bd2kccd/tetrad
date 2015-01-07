package edu.cmu.tetrad.search;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;
import edu.pitt.dbmi.algo.bayesian.constraint.inference.BCInference;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.*;

/**
 * I'm going to try here to read in the simulation data sets that Ruben just made using Smith's code and
 * run them.
 */
public class ExploreCooperSpirtesPaper {

    private PrintWriter averagesOut;
    private PrintWriter averagesOutTabDelim;
    private Set<Set<Node>> latentSets = new HashSet<Set<Node>>();

    private enum Location {Work, Home}

    private Location location = Location.Home;

    private NumberFormat nf = new DecimalFormat("0.000");
    private Random shuffleRandom;
    private PrintWriter out;
    private double alpha = .05;
    private boolean rfciUsed = true;
    private int depth = 6;
    //    private int numRuns = 5;
    private String label = "experiment6";


    public void experiment() {

////        String modelName = "Alarm";
//        String modelName = "Hailfinder";
////        String modelName = "Barley";
//
////        String modelName = "Random";
////        String modelName = "Hepar";
////        String modelName = "Win95";

        for (String modelName : new String[]{"Alarm", "Hailfinder", "Barley"}) {
            for (int numCases : new int[]{1000, 8000}) {
                for (int numModels : new int[]{1000, 4000}) {
                    long seed = 30402033986L;
                    RandomUtil.getInstance().setSeed(seed);
                    shuffleRandom = new Random(seed);

                    int numRuns;

                    if (modelName.equals("Alarm")) {
                        numRuns = 40;
                    }
                    else if (modelName.equals("Hailfinder")) {
                        numRuns = 10;
                    }
                    else if (modelName.equals("Barley")) {
                        numRuns = 40;
                    }
                    else {
                        numRuns = 40;
                    }

                    latentSets.clear();
                    loop1(seed, modelName, numCases, numModels, numRuns);
                }
            }
        }

//        for (String modelName : new String[]{"Alarm"}) {
//            for (int numCases : new int[]{1000}) {
//                for (int numModels : new int[]{1000}) {
//                    int numRuns = 40;
//                    loop1(seed, modelName, numCases, numModels, numRuns);
//                }
//            }
//        }
    }

    public void
    experiment2() {

////        String modelName = "Alarm";
//        String modelName = "Hailfinder";
////        String modelName = "Barley";
//
////        String modelName = "Random";
////        String modelName = "Hepar";
////        String modelName = "Win95";

        for (int run = 0; run < 5; run++) {
            for (String modelName : new String[]{"Alarm", /*"Hailfinder", "Barley"*/}) {
                for (int numCases : new int[]{50000}) {
//                    for (int numCases : new int[]{8000}) {
                    for (int numModels : new int[]{4000}) {
                        long seed = 30402033986L;
                        RandomUtil.getInstance().setSeed(seed);
                        shuffleRandom = new Random(seed);
//                        int run = 0;
                        printEdgeHistogramStats(seed, modelName, numCases, numModels, run);
                    }
                }
            }
        }

//        for (String modelName : new String[]{"Alarm"}) {
//            for (int numCases : new int[]{1000}) {
//                for (int numModels : new int[]{1000}) {
//                    int numRuns = 40;
//                    loop1(seed, modelName, numCases, numModels, numRuns);
//                }
//            }
//        }
    }

    public void loop1(long seed, String modelName, int numCases, int numModels, int numRuns) {
        long start = System.currentTimeMillis();

        for (int run = 0; run < numRuns; run++) {
            oneRun(seed, modelName, numCases, numModels, run);
        }

        long stop = System.currentTimeMillis();

        println("\nElapsed " + (stop - start) / 1000L + " seconds");

        loop2(modelName, numRuns, numModels, numCases);

        if (out != null) {
            out.close();
        }
    }

    private void oneRun(long seed, String modelName, int numCases, int numModels, int run) {
        boolean set = setOut(modelName, run, numModels, numCases);
        if (!set) return;

        BayesIm im = getBayesIM(modelName);
        BayesPm pm = im.getBayesPm();
        Dag dag = pm.getDag();
        List<Node> dagNodes = dag.getNodes();

        printParameters(seed, modelName, pm, dag, dagNodes, numModels, numCases);

        DataSet data = im.simulateData(numCases, false);

        println("\n\nRound " + (run + 1) + "\n");

        List<Node> latents = getLatents(dag);

        if (latents == null) return;

        DataSet dataWithoutLatents = removeLatentsFromData(latents, data, true);

        List<Graph> pagProbs = new ArrayList<Graph>();
        IndTestProbabilistic test0 = runFciProb(dataWithoutLatents, pagProbs, numModels);
        List<Graph> pags = new ArrayList<Graph>(pagProbs);
        Graph PAG_CS = runPagCs(dataWithoutLatents, test0, pags);
////        Graph PAG_PR = runPagDsep(dag, latents, dataWithoutLatents, test0, pags);
        Graph PAG_True = runPagDsep(dag, latents, dataWithoutLatents, test0, pags);
//
        Map<IndependenceFact, Double> H = getHMap(test0);

        Map<Graph, Double> lnProbs = getLnProbs(pags, H);
        double lnQTotal = lnQTotal(lnProbs);
        Graph PAG_BS = getMaxPag(pagProbs, lnProbs); // "Best sampled" PAG.
        printAllProbs(pags, PAG_CS, PAG_True, lnProbs, lnQTotal, PAG_BS);
        printSmallProbsTable(PAG_CS, PAG_True, lnProbs, lnQTotal, PAG_BS);

        printEndpointMisclassifications("PAG_CS", dataWithoutLatents, PAG_CS, PAG_True);
        printEndpointMisclassifications("PAG_BS", dataWithoutLatents, PAG_BS, PAG_True);

        printEdgeMisclassifications("PAG_CS", dataWithoutLatents, PAG_CS, PAG_True);
        printEdgeMisclassifications("PAG_BS", dataWithoutLatents, PAG_BS, PAG_True);
//
//        influenceOfSampleSize(im, latents, test2, PAG_PR);

        boolean withoutNull = false;

        println("\nPAG_Prob Histograms from edge frequencies WITH null-null");
        printEdgeProbabilityHistograms(dataWithoutLatents, new EdgeFrequency(pagProbs), PAG_True, withoutNull);

        println("\nPAG_CS Histograms P = 1 or 0 for edges WITH null-null");
        printEdgeProbabilityHistograms(dataWithoutLatents, new EdgePresent(PAG_CS), PAG_True, withoutNull);

        withoutNull = true;

        println("\nPAG_Prob Histograms from edge frequencies WITHOUT null-null");
        printEdgeProbabilityHistograms(dataWithoutLatents, new EdgeFrequency(pagProbs), PAG_True, withoutNull);

        println("\nPAG_CS Histograms P = 1 or 0 for edges WITHOUT null-null");
        printEdgeProbabilityHistograms(dataWithoutLatents, new EdgePresent(PAG_CS), PAG_True, withoutNull);

        printMaxEachEdgeGraph(dataWithoutLatents, pagProbs, PAG_True);
    }

    private void printEdgeHistogramStats(long seed, String modelName, int numCases, int numModels, int run) {
        boolean set = setOut(modelName, run, numModels, numCases);
        if (!set) return;

        BayesIm im = getBayesIM(modelName);
        BayesPm pm = im.getBayesPm();
        Dag dag = pm.getDag();
        List<Node> dagNodes = dag.getNodes();

        printParameters(seed, modelName, pm, dag, dagNodes, numModels, numCases);

        DataSet data = im.simulateData(numCases, false);

        println("\n\nRound " + (run + 1) + "\n");

        List<Node> latents = getLatents(dag);

        if (latents == null) return;

        DataSet dataWithoutLatents = removeLatentsFromData(latents, data, true);

        List<Graph> pagProbs = new ArrayList<Graph>();
        IndTestProbabilistic test0 = runFciProb(dataWithoutLatents, pagProbs, numModels);
        List<Graph> pags = new ArrayList<Graph>(pagProbs);
//        Graph PAG_CS = runPagCs(dataWithoutLatents, test0, pags);
////        Graph PAG_PR = runPagDsep(dag, latents, dataWithoutLatents, test0, pags);
        Graph PAG_True = runPagDsep(dag, latents, dataWithoutLatents, test0, pags);

        System.out.println("Pag_true = " + PAG_True);

        List<Node> nodes = PAG_True.getNodes();
        Graph complete = new EdgeListGraph(nodes);
        complete.fullyConnect(Endpoint.TAIL);

        EdgeFrequency frequency = new EdgeFrequency(pagProbs);

        for (Edge _edge : complete.getEdges()) {
            Node a = _edge.getNode1();
            Node b = _edge.getNode2();

            int i1 = nodes.indexOf(a);
            int i2 = nodes.indexOf(b);

            Edge edge = PAG_True.getEdge(a, b);
            if (edge == null) edge = new Edge(a, b, Endpoint.NULL, Endpoint.NULL);

            List<Edge> edgeTypes = new ArrayList<Edge>();

//            1. A o-o B
//            2. A o-> B
//            3. A <-o B
//            4. A --> B
//            5. A <-- B
//            6. A <-> B
//            7. A null B

            edgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.CIRCLE));

            edgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.ARROW));
            edgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.CIRCLE));

            edgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.ARROW));
            edgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.TAIL));

            edgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.ARROW));
            edgeTypes.add(new Edge(a, b, Endpoint.NULL, Endpoint.NULL));

            int edgeType = edgeTypes.indexOf(edge);

            print((i1 + 1) + " " + (i2 + 1) + " " + (edgeType + 1) + " ");

            NumberFormat nf2 = new DecimalFormat("0.0000");

            for (int i = 0; i < edgeTypes.size(); i++) {
                double prob = frequency.getProbability(edgeTypes.get(i));
                print(nf2.format(prob) + " ");
            }

//            if (PAG_True.containsEdge(edge)) {
//                println(edge.toString());
//            }
//            else {
            println("");
//            }
        }

    }

    private void printMaxEachEdgeGraph(DataSet dataWithoutLatents, List<Graph> pagProbs, Graph PAG_True) {
        Graph maxEachEdgeGraph = new EdgeListGraph(dataWithoutLatents.getVariables());
        Graph completeGraph = new EdgeListGraph(dataWithoutLatents.getVariables());
        completeGraph.fullyConnect(Endpoint.TAIL);

        List<Edge> edgeTypes = new ArrayList<Edge>();
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");

        edgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.CIRCLE));
        edgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.ARROW));
        edgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.CIRCLE));
        edgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.ARROW));
        edgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.TAIL));
        edgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.ARROW));
        edgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.TAIL));
        edgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.CIRCLE));
        edgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.TAIL));
        edgeTypes.add(new Edge(a, b, Endpoint.NULL, Endpoint.NULL));


        for (Edge edge : completeGraph.getEdges()) {
            double maxP = -1;
            Edge maxEdge = null;

            for (int i = 0; i < edgeTypes.size(); i++) {
                Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edgeTypes.get(i).getEndpoint1(),
                        edgeTypes.get(i).getEndpoint2());
                double p = new EdgeFrequency(pagProbs).getProbability(_edge);
                if (p > maxP) {
                    maxP = p;
                    maxEdge = _edge;
                }
            }

            if (!(maxEdge.getEndpoint1() == Endpoint.NULL && maxEdge.getEndpoint2() == Endpoint.NULL)) {
                maxEachEdgeGraph.addEdge(maxEdge);
            }
        }

        println("\nMax Each Edge Graph:");

        List<Edge> edges = maxEachEdgeGraph.getEdges();

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            println((i + 1) + ".\t" + edge + "\tTRUE PAG:" + PAG_True.getEdge(edge.getNode1(), edge.getNode2()));
        }

        println("");

        printEndpointMisclassifications("MaxEdgeGraph", dataWithoutLatents, maxEachEdgeGraph, PAG_True);
        printEdgeMisclassifications("MaxEdgeGraph", dataWithoutLatents, maxEachEdgeGraph, PAG_True);
    }

    private void printParameters(long seed, String modelName, BayesPm pm, Dag dag, List<Node> dagNodes,
                                 int numModels, int numCases) {
        double[] indegrees = indegrees(dag, dagNodes);
        double[] states = states(pm, dag, dagNodes);

        println("Name of model: " + modelName);
        println("Domain of model: ________");
        println("Number of nodes: " + dag.getNumNodes());
        println("Number of arcs: " + dag.getEdges().size());
        println("Mean Indegree: " + nf.format(StatUtils.mean(indegrees)));
        println("Std Dev Indegree: " + nf.format(StatUtils.sd(indegrees)));
        println("Mean States: " + nf.format(StatUtils.mean(states)));
        println("Std Dev States: " + nf.format(StatUtils.sd(indegrees)));
        println("");

        println("numberOfModels = " + numModels);
        println("sampleSize = " + numCases);
        println("alpha = " + alpha);
        println("RFCI used = " + rfciUsed);
        println("depth = " + depth);
        print("seed = " + seed);
        println("");
    }

    private void printEdgeProbabilityHistograms(DataSet dataWithoutLatents, EdgeProbabiity frequency, Graph pagTrue, boolean withoutNull) {


        int numBins = 10;
        int[] nCountSummary = new int[numBins];
        int[] dCountSummary = new int[numBins];

        List<Node> _nodes = dataWithoutLatents.getVariables();
        Endpoint end1, end2;

        for (int k = 0; k < 3; k++) {
            if (k == 0) {
                end1 = Endpoint.CIRCLE;
                end2 = Endpoint.CIRCLE;

                if (!withoutNull) continue;
            } else if (k == 1) {
                end1 = Endpoint.ARROW;
                end2 = Endpoint.ARROW;

                if (!withoutNull) continue;
            } else if (k == 2) {
                if (withoutNull) continue;

                end1 = Endpoint.NULL; // interpreted as null
                end2 = Endpoint.NULL;
            } else {
                throw new IllegalStateException();
            }

            Graph complete = new EdgeListGraph(_nodes);
            complete.fullyConnect(Endpoint.TAIL);

            int[] nCount = new int[numBins];
            int[] dCount = new int[numBins];

            int _count = 0;

            for (Edge _edge : complete.getEdges()) {
                Node n1 = _edge.getNode1();
                Node n2 = _edge.getNode2();

                Edge edge = new Edge(n1, n2, end1, end2);

                double p = frequency.getProbability(edge);

                int bin = (int) floor(p * numBins);

                if (bin == numBins) bin = numBins - 1;

                int count;

                if (pagTrue.containsEdge(edge) || ((end1 == Endpoint.NULL && end2 == Endpoint.NULL && !pagTrue.isAdjacentTo(n1, n2)))) {
                    count = 1;
                } else {
                    count = 0;
                }

                nCount[bin] = nCount[bin] + count;
                dCount[bin] = dCount[bin] + 1;

                nCountSummary[bin] = nCountSummary[bin] + count;
                dCountSummary[bin] = dCountSummary[bin] + 1;

                _count++;
            }

//            System.out.println("count = " + _count);

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

                if (!withoutNull) continue;
            } else if (k == 1) {
                end1 = Endpoint.TAIL;
                end2 = Endpoint.ARROW;

                if (!withoutNull) continue;
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

                double p = frequency.getProbability(edge);

                int bin = (int) floor(p * numBins);

                if (bin == numBins) bin = numBins - 1;

                int count;

                if (pagTrue.containsEdge(edge)) {
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

        if (withoutNull) {
            println("\nSUMMARY");

            for (int i = 0; i < numBins; i++) {
                println((i + 1) + "\t" + nCountSummary[i] + "\t" + dCountSummary[i] + "\t" +
                        (Double.isNaN(binHeightSummary[i]) ? "-" : nf.format(binHeightSummary[i])));
            }
        }
    }

    private static interface EdgeProbabiity {
        public double getProbability(Edge edge);
    }

    private static class EdgeFrequency implements EdgeProbabiity {
        private List<Graph> pagProbs;

        public EdgeFrequency(List<Graph> pagProb) {
            this.pagProbs = pagProb;
        }

        @Override
        public double getProbability(Edge e) {
            int count = 0;

            if (!pagProbs.get(0).containsNode(e.getNode1())) throw new IllegalArgumentException();
            if (!pagProbs.get(0).containsNode(e.getNode2())) throw new IllegalArgumentException();

            for (Graph pag : pagProbs) {
                if (e.getEndpoint1() == Endpoint.NULL || e.getEndpoint2() == Endpoint.NULL) {
                    if (!pag.isAdjacentTo(e.getNode1(), e.getNode2())) count++;
                } else {
                    if (pag.containsEdge(e)) count++;
                }
            }

            return count / (double) pagProbs.size();
        }
    }

    private static class EdgePresent implements EdgeProbabiity {

        private final Graph estGraph;

        public EdgePresent(Graph estGraph) {
            this.estGraph = estGraph;
        }

        @Override
        public double getProbability(Edge edge) {
            if (edge.getEndpoint1() == Endpoint.NULL && edge.getEndpoint2() == Endpoint.NULL) {
                return estGraph.isAdjacentTo(edge.getNode1(), edge.getNode2()) ? 0 : 1;
            } else {
                return estGraph.containsEdge(edge) ? 1 : 0;
            }
        }
    }

    private void printSmallProbsTable(Graph PAG_CS, Graph PAG_True, Map<Graph, Double> pagLnProbs, double lnQTotal, Graph PAG_BS) {
        TextTable table = new TextTable(5, 3);

        table.setToken(1, 0, "PAG-CS");
//            table.setToken(2, 0, "PAG-PR");
        table.setToken(2, 0, "PAG-BS");
        table.setToken(3, 0, "PAG-True");
        table.setToken(0, 1, "NPP");
        table.setToken(0, 2, "RPP");

        double pagCsNpp = (pagLnProbs.get(PAG_CS) - lnQTotal);
        double pagBsNpp = (pagLnProbs.get(PAG_BS) - lnQTotal);
        double pagTrueNpp = (pagLnProbs.get(PAG_True) - lnQTotal);

        double pagCsRpp = (pagLnProbs.get(PAG_CS));
        double pagBsRpp = (pagLnProbs.get(PAG_BS));
        double pagTrueRpp = (pagLnProbs.get(PAG_True));

        NumberFormat nf2 = new DecimalFormat("0.0E0");

        table.setToken(1, 1, nf2.format(pagCsNpp));
        table.setToken(2, 1, nf2.format(pagBsNpp));
        table.setToken(3, 1, nf2.format(pagTrueNpp));

        table.setToken(1, 2, nf2.format(pagCsRpp));
        table.setToken(2, 2, nf2.format(pagBsRpp));
        table.setToken(3, 2, nf2.format(pagTrueRpp));

        println("\n" + table);
    }

    private void printAllProbs(List<Graph> pags, Graph PAG_CS, Graph PAG_True, Map<Graph, Double> pagLnProbs, double lnQTotal, Graph PAG_BS) {
        for (int i = 0; i < pags.size(); i++) {
            Graph pag = pags.get(i);
            double lnQ = pagLnProbs.get(pag);
            double normalizedlnQ = lnQ - lnQTotal;

            print("lnQ = " + (lnQ) + " lnQTotal = " + lnQTotal + " normalized lnQ = " + normalizedlnQ);

            if (pag.equals(PAG_CS)) print("\tPAG_CS");
            if (pag.equals(PAG_True)) print("\tPAG_True");
            if (pag.equals(PAG_BS)) print("\tPAG_BS");
            println("");
        }
    }

    private Map<IndependenceFact, Double> getHMap(IndTestProbabilistic test0) {
        Map<IndependenceFact, Double> H = test0.getH();
        println("\n# constraints in the domain of H: " + H.keySet().size() + "\n");
        return H;
    }

    private Graph runPagDsep(Dag dag, List<Node> latents, DataSet dataWithoutLatents, IndTestProbabilistic test0, List<Graph> pags) {
        IndTestDSep test3 = new IndTestDSep(new EdgeListGraph(dag));
        test3.setVerbose(false);
        List<Node> graphNodes = dag.getNodes();
        graphNodes.removeAll(latents);
        test3 = (IndTestDSep) test3.indTestSubset(graphNodes);
        Fci fci3 = new Fci(test3);
        fci3.setRFCI_Used(rfciUsed);
        fci3.setDepth(depth);
        fci3.setVerbose(true);
        test3.startRecordingFacts();
        Graph PAG_True = fci3.search();
        PAG_True = GraphUtils.replaceNodes(PAG_True, dataWithoutLatents.getVariables());

        if (!pags.contains(PAG_True)) {
            pags.add(PAG_True);
        }

        for (IndependenceFact fact : test3.getFacts()) {
            fact = replaceVars(fact, dataWithoutLatents.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }

        return PAG_True;

    }

    private Graph runPagCs(DataSet dataWithoutLatents, IndTestProbabilistic test0, List<Graph> pags) {
        IndTestChiSquare test1 = new IndTestChiSquare(dataWithoutLatents, alpha);
        Fci fci1 = new Fci(test1);
        fci1.setRFCI_Used(rfciUsed);
        fci1.setDepth(depth);
        fci1.setVerbose(false);
        test1.startRecordingFacts();
        Graph PAG_CS = fci1.search();
        PAG_CS = GraphUtils.replaceNodes(PAG_CS, dataWithoutLatents.getVariables());

        if (!pags.contains(PAG_CS)) {
            pags.add(PAG_CS);
        }

        for (IndependenceFact fact : test1.getFacts()) {
            fact = replaceVars(fact, dataWithoutLatents.getVariables());
            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
        }
        return PAG_CS;
    }

//    private Graph runPagPr(DataSet dataWithoutLatents, ProbabilisticIndependence test0, List<Graph> pags) {
//        ProbabilisticMAPIndependence test2 = new ProbabilisticMAPIndependence(dataWithoutLatents);
//
//        Fci fci2 = new Fci(test2);
//        fci2.setRFCI_Used(rfciUsed);
//        fci2.setDepth(depth);
//        fci2.setVerbose(false);
//        Graph PAG_PR = fci2.search();
//        PAG_PR = GraphUtils.replaceNodes(PAG_PR, dataWithoutLatents.getVariables());
//
//        if (!pags.contains(PAG_PR)) {
//            pags.add(PAG_PR);
//        }
//
//        for (IndependenceFact fact : test2.getH().keySet()) {
//            fact = replaceVars(fact, dataWithoutLatents.getVariables());
//            test0.isIndependent(fact.getX(), fact.getY(), fact.getZ());
//        }
//        return PAG_PR;
//    }

    private IndTestProbabilistic runFciProb(DataSet dataWithoutLatents, List<Graph> pags, int numModels) {
        IndTestProbabilistic test0 = new IndTestProbabilistic(dataWithoutLatents);

        Fci fci0 = new Fci(test0);
        fci0.setRFCI_Used(rfciUsed);
        fci0.setDepth(depth);

        for (int i = 0; i < numModels; i++) {
            Graph PAG_PROB = fci0.search();
            PAG_PROB = GraphUtils.replaceNodes(PAG_PROB, dataWithoutLatents.getVariables());

            pags.add(PAG_PROB);
        }
        println("# Models = " + numModels);
        return test0;
    }

    private double[] states(BayesPm pm, Dag dag, List<Node> dagNodes) {
        double[] states = new double[dag.getNumNodes()];

        for (int i = 0; i < dagNodes.size(); i++) {
            DiscreteVariable variable = (DiscreteVariable) pm.getVariable(dagNodes.get(i));
            states[i] = variable.getNumCategories();
        }
        return states;
    }

    private double[] indegrees(Dag dag, List<Node> dagNodes) {
        double[] indegree = new double[dag.getNumNodes()];

        for (int i = 0; i < dagNodes.size(); i++) {
            indegree[i] = dag.getAdjacentNodes(dagNodes.get(i)).size();
        }
        return indegree;
    }

    private boolean setOut(String modelName, int run, int numModels, int numCases) {
        try {
            String dirname = getDir() + "/" + label + "/output." + modelName + "." + numModels + "." + numCases;
            File dir = new File(dirname);
            dir.mkdirs();
            String filename = "run" + (run + 1) + ".txt";
            File file = new File(dir, filename);
            if (file.exists()) return false;
            out = new PrintWriter(file);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void println(String s) {
        if (out == null) return;
        out.println(s);
        out.flush();
        System.out.println(s);
    }

    private void print(String s) {
        out.print(s);
        out.flush();
        System.out.print(s);
    }

//    // Influence of Sample Size on the Posterior Probability of PAG-PR
//    private void influenceOfSampleSize(BayesIm im, List<Node> latents, ProbabilisticMAPIndependence test2, Graph PAG_PR) {
//        for (int _sampleSize : new int[]{1000, 2000, 3000, 4000, 5000, 10000}) {
//            DataSet data2 = im.simulateData(_sampleSize, false);
//            removeLatentsFromData(latents, data2, false);
//            List<Graph> pags2 = new ArrayList<Graph>();
//
//            ProbabilisticIndependence test4 = new ProbabilisticIndependence(data2);
//
////            Fci fci4 = new Fci(test4);
////            fci4.setRFCI_Used(rfciUsed);
////            fci4.setDepth(depth);
////
////            for (int i = 0; i < numberOfModels; i++) {
//////                print("Model # " + (i + 1));
////                Graph PAG_PROB2 = fci4.search();
////                PAG_PROB2 = GraphUtils.replaceNodes(PAG_PROB2, data2.getVariables());
////
////                if (!pags.contains(PAG_PROB2)) {
////                    pags.add(PAG_PROB2);
////                }
////            }
//
//            ProbabilisticMAPIndependence test5 = new ProbabilisticMAPIndependence(data2);
//
////            Fci fci5 = new Fci(test5);
////            fci5.setRFCI_Used(rfciUsed);
////            fci5.setDepth(depth);
////            Graph PAG_PR5 = fci5.search();
//            Graph PAG_PR5 = new EdgeListGraph(PAG_PR);
//            PAG_PR5 = GraphUtils.replaceNodes(PAG_PR5, data2.getVariables());
//
//            if (!pags2.contains(PAG_PR5)) {
//                pags2.add(PAG_PR5);
//            }
//
//            for (IndependenceFact fact : test2.getH().keySet()) {
//                fact = replaceVars(fact, test4.getVariables());
//                test4.isIndependent(fact.getX(), fact.getY(), fact.getZ());
//            }
//
//            Map<IndependenceFact, Double> H2 = getHMap(test4);
//
//            double lnProb = getLnProb(PAG_PR5, H2);
//            println("Sample size = " + _sampleSize + " lnProb / n = " + (1. / _sampleSize) * lnProb +
//                    " exp(lnProb / n) = " + exp(((1. / _sampleSize) * lnProb)));
////            print("Sample size = " + _sampleSize + " lnProb = " + exp(((1./_sampleSize) * lnProb)));
////            print("Sample size = " + _sampleSize + " lnProb = " + lnProb);
//        }
//    }

    private DataSet removeLatentsFromData(List<Node> latents, DataSet data, boolean verbose) {
        DataSet dataWithoutLatents = new ColtDataSet((ColtDataSet) data);

        for (int j = 0; j < latents.size(); j++) {
            Node n = latents.get(j);
            dataWithoutLatents.removeColumn(data.getVariable(n.getName()));
        }

        return dataWithoutLatents;
    }

    private List<Node> getLatents(Dag dag) {
        List<Node> commonCauses = getCommonCauses(dag);
        int numLatents = (int) round(commonCauses.size() / 2.0);
        println("There are " + commonCauses.size() + " accepted common causes.");
        println("Choosing " + numLatents + " of these.");
        List<Node> latents;
        int listCount = 0;

        do {
            Collections.shuffle(commonCauses, shuffleRandom);
            latents = new ArrayList<Node>();

            for (int i = 0; i < numLatents; i++) {
                Node n = commonCauses.get(i);
                latents.add(n);
            }
        } while (this.latentSets.contains(new HashSet<Node>(latents)) && ++listCount < 50000);

        if (!this.latentSets.contains(new HashSet<Node>(latents))) {
            this.latentSets.add(new HashSet<Node>(latents));
            for (Node n : latents){
                println("Setting " + n + " to latent.");
            }
            return latents;
        }
        else {
            return null;
        }
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

        println("\n" + name);
        println(table2.toString());
        println("");
    }

    private void printEdgeMisclassifications(String name, DataSet data, Graph estGraph, Graph trueGraph) {
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

        // Need the sum of cells except the null-null cell.
        int sum = 0;

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 6 && j == 4) continue;
                sum += counts[i][j];
            }
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 6 && j == 4) table2.setToken(i + 1, j + 1, "XXXXX");
                else table2.setToken(i + 1, j + 1, nf.format(counts[i][j] / (double) sum));
            }
        }

        println("\n" + name);
        println(table2.toString());
        println("");

        TextTable table3 = new TextTable(3, 3);

        table3.setToken(1, 0, "Non-Null");
        table3.setToken(2, 0, "Null");
        table3.setToken(0, 1, "Non-Null");
        table3.setToken(0, 2, "Null");

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 6 && j == 4) table2.setToken(i + 1, j + 1, "XXXXX");
                else table2.setToken(i + 1, j + 1, nf.format(counts[i][j] / (double) sum));
            }
        }


        int[][] _counts = new int[2][2];
        int _sum = 0;

        for (int i = 0; i < 6; i++) {
            _sum += counts[i][0];
        }

        _counts[1][0] = _sum;
        _sum = 0;

        for (int i = 0; i < 4; i++) {
            _sum += counts[0][i];
        }

        _counts[0][1] = _sum;
        _sum = 0;

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                _sum += counts[i][j];
            }
        }

        _counts[0][0] = _sum;

        _counts[1][1] = counts[6][4];

        // Now we need the sum of all cells.
        sum = 0;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                sum += _counts[i][j];
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                table3.setToken(i + 1, j + 1, nf.format(_counts[i][j] / (double) sum));
            }
        }

        out.println("Null\n");

        out.println(table3);
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

    private Map<Graph, Double> getLnProbs(List<Graph> pags, Map<IndependenceFact, Double> H) {
        Map<Graph, Double> pagLnProb = new HashMap<Graph, Double>();

        for (int i = 0; i < pags.size(); i++) {
            Graph pag = pags.get(i);
            double lnQ = getLnProb(pag, H);
            pagLnProb.put(pag, lnQ);
        }
        println("# Pags = " + pags.size());

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

            if (p < -0.0001 || p > 1.0001 || Double.isNaN(p) || Double.isInfinite(p)) {
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

            if (node.getName().equals("Scenario")) {
                continue;
            }
            if (node.getName().equals("aks_vgt")) {
                continue;
            }
            if (node.getName().equals("keraks")) {
                continue;
            }
            if (node.getName().equals("ntilg")) {
                continue;
            }
            if (node.getName().equals("jordn")) {
                continue;
            }

            if (found && !commonCauses.contains(node)) {
                commonCauses.add(node);
            }
        }

        return commonCauses;
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

        throw new IllegalArgumentException("Not a recogized Bayes IM type.");
    }

    private BayesIm loadBayesIm(String filename, boolean useDisplayNames) {
        try {
            Builder builder = new Builder();
            File dir = new File(getDir(), "xdsl");
            File file = new File(dir, filename);
            Document document = builder.build(file);
            XdslXmlParser parser = new XdslXmlParser();
            parser.setUseDisplayNames(useDisplayNames);
            return parser.getBayesIm(document.getRootElement());
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loop2(String modelName, int numRuns, int numModels, int numCases) {
        try {
            int numFiles = numRuns;

            String dirname = getDir() + "/" + label + "/output." + modelName + "." + numModels + "." + numCases;
            File dir = new File(dirname);
            List<BufferedReader> in = new ArrayList<BufferedReader>();
            for (int i = 0; i < numFiles; i++) {
                File file = new File(dir, "run" + (i + 1) + ".txt");
                in.add(new BufferedReader(new FileReader(file)));
            }

            averagesOut = new PrintWriter(new File(dir, "averages.txt"));
            averagesOutTabDelim = new PrintWriter(new File(dir, "averagestabdelim.txt"));

            parseMatrix(numRuns, "Small Probs Table", in, 3, 2, "  ", "NPP");
            parseMatrix(numRuns, "PAG_CS Endpoint Misclassification", in, 4, 4, "  ", "PAG_CS", "-o");
            parseMatrix(numRuns, "PAG_BS Endpoint Misclassification", in, 4, 4, "  ", "PAG_BS", "-o");
            parseMatrix(numRuns, "PAG_CS Edge Misclassification", in, 7, 5, "  ", "PAG_CS", "o-o");
            parseMatrix(numRuns, "PAG_CS Null-Null Edge Misclassification", in, 2, 2, "  ", "Null", "Non-Null");
            parseMatrix(numRuns, "PAG_BS Edge Misclassification", in, 7, 5, "  ", "PAG_BS", "o-o");
            parseMatrix(numRuns, "PAG_BS Null-Null Edge Misclassification", in, 2, 2, "  ", "Null", "Non-Null");

//            parseMatrix("PAG_True Histograms from edge frequencies WITH null-null Circle-Circle", in, 10, 4, "\t", "Circle-Circle");
//            parseMatrix("PAG_True Histograms from edge frequencies WITH null-null Arrow-Arrow", in, 10, 4, "\t", "Arrow-Arrow");
            parseMatrix(numRuns, "PAG_True Histograms from edge frequencies WITH null-null Null-Null", in, 10, 4, "\t", "Null-Null");
//            parseMatrix("PAG_True Histograms from edge frequencies WITH null-null Circle-Arrow", in, 10, 4, "\t", "Circle-Arrow");
//            parseMatrix("PAG_True Histograms from edge frequencies WITH null-null Tail-Arrow", in, 10, 4, "\t", "Tail-Arrow");
//            parseMatrix("PAG_True Histograms from edge frequencies WITH null-null SUMMARY", in, 10, 4, "\t", "SUMMARY");

//            parseMatrix("PAG_CS Histograms P = 1 or 0 for edges WITH null-null Circle-Circle", in, 10, 4, "\t", "Circle-Circle");
//            parseMatrix("PAG_CS Histograms P = 1 or 0 for edges WITH null-null Arrow-Arrow", in, 10, 4, "\t", "Arrow-Arrow");
            parseMatrix(numRuns, "PAG_CS Histograms P = 1 or 0 for edges WITH null-null Null-Null", in, 10, 4, "\t", "Null-Null");
//            parseMatrix("PAG_CS Histograms P = 1 or 0 for edges WITH null-null Circle-Arrow", in, 10, 4, "\t", "Circle-Arrow");
//            parseMatrix("PAG_CS Histograms P = 1 or 0 for edges WITH null-null Tail-Arrow", in, 10, 4, "\t", "Tail-Arrow");
//            parseMatrix("PAG_CS Histograms P = 1 or 0 for edges WITH null-null SUMMARY", in, 10, 4, "\t", "SUMMARY");

            parseMatrix(numRuns, "PAG_True Histograms from edge frequencies WITHOUT null-null Circle-Circle", in, 10, 4, "\t", "Circle-Circle");
            parseMatrix(numRuns, "PAG_True Histograms from edge frequencies WITHOUT null-null Arrow-Arrow", in, 10, 4, "\t", "Arrow-Arrow");
            parseMatrix(numRuns, "PAG_True Histograms from edge frequencies WITHOUT null-null Circle-Arrow", in, 10, 4, "\t", "Circle-Arrow");
            parseMatrix(numRuns, "PAG_True Histograms from edge frequencies WITHOUT null-null Tail-Arrow", in, 10, 4, "\t", "Tail-Arrow");
            parseMatrix(numRuns, "PAG_True Histograms from edge frequencies WITHOUT null-null SUMMARY", in, 10, 4, "\t", "SUMMARY");

            parseMatrix(numRuns, "PAG_CS Histograms P = 1 or 0 for edges WITHOUT null-null Circle-Circle", in, 10, 4, "\t", "Circle-Circle");
            parseMatrix(numRuns, "PAG_CS Histograms P = 1 or 0 for edges WITHOUT null-null Arrow-Arrow", in, 10, 4, "\t", "Arrow-Arrow");
            parseMatrix(numRuns, "PAG_CS Histograms P = 1 or 0 for edges WITHOUT null-null Circle-Arrow", in, 10, 4, "\t", "Circle-Arrow");
            parseMatrix(numRuns, "PAG_CS Histograms P = 1 or 0 for edges WITHOUT null-null Tail-Arrow", in, 10, 4, "\t", "Tail-Arrow");
            parseMatrix(numRuns, "PAG_CS Histograms P = 1 or 0 for edges WITHOUT null-null SUMMARY", in, 10, 4, "\t", "SUMMARY");

            parseMatrix(numRuns, "Max Edge Graph Endpoint Misclassification Matrix", in, 4, 4, "  ", "MaxEdgeGraph", "-o");
            parseMatrix(numRuns, "Max Edge Graph Edge Misclassification Matrix", in, 7, 5, "  ", "MaxEdgeGraph", "o-o");

            averagesOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseMatrix(int numRuns, String label, List<BufferedReader> in, int rows, int cols, String delimiter,
                             String... searchStrings) throws Exception {
        averagesOut.println(label);
        averagesOutTabDelim.println(label);

//        for (String s : searchStrings) {
//            averagesPrintln("SEARCHING FOR: " + s);
//            averageTabDelimPrintln("SEARCHING FOR: " + s);
//        }

        int numFiles = numRuns;

        double[][][] all = new double[numFiles][][];

        for (int i = 0; i < numFiles; i++) {
            for (String s : searchStrings) {
                find(s, in.get(i));
            }

            double[][] m = new double[rows][cols];
            String line;

            for (int j = 0; j < rows; j++) {
                line = in.get(i).readLine();
                double[] values = split(line, cols, delimiter);
                m[j] = values;
            }

            all[i] = m;
        }

        double[][] average = average(all);
        double[][] std = std(all);
        int[][] numValues = numValues(all);

        averagesPrintln("\nAverage");
        averageTabDelimPrintln("\nAverage");
        averagesPrintln(MatrixUtils.toString(average));
        averageTabDelimPrintln(toStringTabDelim(average));

        averagesPrintln("\nStd");
        averageTabDelimPrintln("\nStd");
        averagesPrintln(MatrixUtils.toString(std));
        averageTabDelimPrintln(toStringTabDelim(std));

        averagesPrintln("\nNumValues");
        averagesPrintln(MatrixUtils.toString(numValues));
    }

    private String toStringTabDelim(double[][] m) {
        NumberFormat nf = new DecimalFormat("0.000");
        StringBuilder b = new StringBuilder();
        b.append("\n");

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                b.append(nf.format(m[i][j]) + "\t");
            }

            b.append("\n");
        }

        b.append("\n");
        return b.toString();
    }

    private double[][] average(double[][][] all) {
        double[][] average = new double[all[0].length][all[0][0].length];

        for (int i = 0; i < all[0].length; i++) {
            for (int j = 0; j < all[0][0].length; j++) {
                double[] h = new double[all.length];

                for (int k = 0; k < all.length; k++) {
                    double[][] m = all[k];
                    h[k] = m[i][j];
                }

                h = removeNaN(h);

                double avg = StatUtils.mean(h);
                average[i][j] = avg;
            }
        }

        return average;
    }

    private double[] removeNaN(double[] h) {
        int count = 0;

        for (int i = 0; i < h.length; i++) {
            if (!Double.isNaN(h[i])) {
                count++;
            }
        }

        double[] j = new double[count];
        int index = -1;

        for (int i = 0; i < h.length; i++) {
            if (!Double.isNaN(h[i])) {
                j[++index] = h[i];
            }
        }

        return j;
    }

    private int[][] numValues(double[][][] all) {
        int[][] numValues = new int[all[0].length][all[0][0].length];

        for (int i = 0; i < all[0].length; i++) {
            for (int j = 0; j < all[0][0].length; j++) {
                double[] h = new double[all.length];

                for (int k = 0; k < all.length; k++) {
                    double[][] m = all[k];
                    h[k] = m[i][j];
                }

                h = removeNaN(h);

                numValues[i][j] = h.length;
            }
        }

        return numValues;
    }

    private double[][] std(double[][][] all) {
        double[][] std = new double[all[0].length][all[0][0].length];

        for (int i = 0; i < all[0].length; i++) {
            for (int j = 0; j < all[0][0].length; j++) {
                double[] h = new double[all.length];

                for (int k = 0; k < all.length; k++) {
                    double[][] m = all[k];
                    h[k] = m[i][j];
                }

                h = removeNaN(h);

                double sd = StatUtils.sd(h);
                std[i][j] = sd;
            }
        }

        return std;
    }

    private double[] split(String line, int cols, String delimiter) {
        String[] tokens = line.split(delimiter);
        List<Double> _tokens = new ArrayList<Double>();

        for (int i = 0; i < tokens.length; i++) {
            try {
                double v = Double.parseDouble(tokens[i]);
                _tokens.add(v);
            } catch (NumberFormatException e) {
                continue;
            }
        }

        double[] values = new double[cols];
        Arrays.fill(values, Double.NaN);
        for (int i = 0; i < _tokens.size(); i++) {
            if (_tokens.get(i) != null) {
                values[i] = _tokens.get(i);
            }
        }
        return values;
    }

    private void find(String s, BufferedReader in) throws Exception {
        String line;

        while ((line = in.readLine()) != null) {
            if (line.contains(s)) {
                return;
            }
        }

        throw new IllegalArgumentException("Couldn't find: " + s);
    }

    private String getDir() {
        if (location == Location.Home) {
            return "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/cooper.spirtes.paper/";
        } else if (location == Location.Work) {
            return "/home/jdramsey/proj/cooper/";
        }

        throw new IllegalStateException();
    }

    private void averagesPrintln(String s) {
        averagesOut.println(s);
        averagesOut.flush();
//        System.out.println(s);
    }

    private void averageTabDelimPrintln(String s) {
        averagesOutTabDelim.println(s);
        averagesOutTabDelim.flush();
    }

    public static void main(String[] args) {
        new ExploreCooperSpirtesPaper().experiment3();
    }


    public void test1() {
        BayesIm im = getBayesIM("Alarm");
        DataSet data = im.simulateData(8000, false);

        IndTestProbabilistic test0 = new IndTestProbabilistic(data);

        Fci fci0 = new Fci(test0);
        fci0.setRFCI_Used(rfciUsed);
        fci0.setDepth(0);

        Graph g = fci0.search();

        System.out.println(g);

    }

    public void test2() {
        BayesIm im = getBayesIM("Alarm");
        DataSet data = im.simulateData(8000, false);

        System.out.println(data.getVariables());

        Node co = data.getVariable("CO");
        Node stokeVolume = data.getVariable("STROKEVOLUME");

        IndTestProbabilistic test0 = new IndTestProbabilistic(data);
        int sum = 0;
//
        for (int i = 0; i < 1000; i++) {
            boolean _true = test0.isIndependent(co, stokeVolume);
            if (_true) sum++;
        }

        System.out.println(sum);
    }

    // Generate 50,000 cases for Alarm, run ProbFCI, print out a record for each constraint, and print out the
    // data (2 files).
    public void experiment3() {
        String modelName = "Alarm";
        int numCases = 50000;
        long seed = 30402033986L;
        RandomUtil.getInstance().setSeed(seed);

        try {
            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/cooper.spirtes.paper";

            File file = new File(dir, "probfcioutput.txt");

            PrintWriter out = new PrintWriter(new FileWriter(file));

            BayesIm im = getBayesIM(modelName);
            DataSet data = im.simulateData(numCases, false);

            IndTestProbabilisticVerbose test = new IndTestProbabilisticVerbose(data, out);

            List<Node> nodes = test.getNodes();

            for (int i = 0; i < nodes.size(); i++) {
                out.println("Node " + (i + 1) + "\t" + nodes.get(i));
            }

            ProbFci fci = new ProbFci(test);

            fci.search();

            for (int i = 0; i < data.getNumRows(); i++) {
                for (int j = 0; j < data.getNumColumns(); j++) {
                    out.print(data.getInt(i, j) + ",");
                }
                out.println();
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}