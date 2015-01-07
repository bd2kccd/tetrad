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

import Jama.Matrix;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemOptimizerPalCds;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Joseph Ramsey
 */
public class TestFTFC extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestFTFC(String name) {
        super(name);
    }

    public void testP() {

        int m = 2;
        int p = 6;

        Graph g = new EdgeListGraph();

        List<Node> vars = new ArrayList<Node>();

        for (int i = 0; i < p; i++) {
            GraphNode n = new GraphNode("V" + i);
            vars.add(n);
            g.addNode(n);
        }

        List<Node> l = new ArrayList<Node>();

        for (int j = 0; j < m; j++) {
            Node _l = new GraphNode("L" + j);
            _l.setNodeType(NodeType.LATENT);
            l.add(_l);
            g.addNode(_l);
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                g.addDirectedEdge(l.get(i), vars.get(j));
            }
        }

        SemPm pm = new SemPm(g);

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < 100; i++) {
            SemIm im = new SemIm(pm);

            DataSet data = im.simulateData(1000, false);
            double chiSquare = Double.POSITIVE_INFINITY;

            for (int j = 0; j < 10; j++) {
                SemEstimator est = new SemEstimator(data, pm, new SemOptimizerPalCds());

                SemIm estIm = est.estimate();

                double _chiSquare = estIm.getChiSquare();
                System.out.println(_chiSquare);
                if (_chiSquare < chiSquare) chiSquare = _chiSquare;
            }

            System.out.println(chiSquare);

            sum += chiSquare;
            count++;

            double avg = sum / count;
            System.out.println("Interim Average " + avg);
        }

        double avg = sum / count;
        System.out.println("Average " + avg);
    }

    public void test2() {
        try {
            DataReader reader = new DataReader();
            ICovarianceMatrix cov = reader.parseCovariance(new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2012.11.14/corr.txt"));
//            cov = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V20", "V24", "V25", "V29"});
//            cov = cov.getSubmatrix(new String[]{"V2", "V8", "V13", "V19", "V21", "V26", "V30", "V32"});
//            cov = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V20", "V24", "V25", "V29",
//                    "V2", "V8", "V13", "V19", "V21", "V26", "V30", "V32"});
            cov = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V20", "V24", "V25", "V29",
                    "V2", "V8", "V13", "V19", "V21", "V26", "V30", "V32",
                    "V3", "V9", "V14", "V22", "V27",
                    "V4", "V10", "V15", "V23", "V28",
                    "V5", "V11", "V16", "V31", "V33"
            });

//            for (int i = 0; i < cov.getDimension(); i++) {
//                for (int j = i + 1; j < cov.getDimension(); j++) {
//                    if (cov.getValue(i, j) < 0.1) cov.setValue(i, j, 0.01);
//                }
//            }

            System.out.println(cov);

//            List<Node> vars = cov.getVariables();
//            List<Node> v =s new ArrayList<Node>();
//
//            // 1-indexed.
//            v.add(null);
//            for (Node _var : vars) v.add(_var);
//
//            List<Node> distant = new ArrayList<Node>();
//            distant.add(v.get(1));
//            distant.add(v.get(6));
//            distant.add(v.get(7));
//            distant.add(v.get(12));
//            distant.add(v.get(17));
//            distant.add(v.get(18));
//            distant.add(v.get(20));
//            distant.add(v.get(24));
//            distant.add(v.get(25));
//            distant.add(v.get(29));
//
//            List<Node> uninsightful = new ArrayList<Node>();
//            uninsightful.add(v.get(2));
//            uninsightful.add(v.get(8));
//            uninsightful.add(v.get(13));
//            uninsightful.add(v.get(19));
//            uninsightful.add(v.get(21));
//            uninsightful.add(v.get(26));
//            uninsightful.add(v.get(30));
//            uninsightful.add(v.get(32));
//
//            List<Node> somatizing = new ArrayList<Node>();
//            somatizing.add(v.get(3));
//            somatizing.add(v.get(9));
//            somatizing.add(v.get(14));
//            somatizing.add(v.get(22));
//            somatizing.add(v.get(27));
//
//            List<Node> humorless = new ArrayList<Node>();
//            humorless.add(v.get(4));
//            humorless.add(v.get(10));
//            humorless.add(v.get(15));
//            humorless.add(v.get(23));
//            humorless.add(v.get(28));
//
//            List<Node> rigid = new ArrayList<Node>();
//            rigid.add(v.get(5));
//            rigid.add(v.get(11));
//            rigid.add(v.get(16));
//            rigid.add(v.get(31));
//            rigid.add(v.get(33));
//
//            List<List<Node>> scales = new ArrayList<List<Node>>();
//            scales.add(distant);
//            scales.add(uninsightful);
//            scales.add(somatizing);
//            scales.add(humorless);
//            scales.add(rigid);
//
//            vars = new ArrayList<Node>();
//      q      for (Node aDistant : distant) vars.add(aDistant);
//            for (Node anUninsightful : uninsightful) vars.add(anUninsightful);

            FindOneFactorClusters ftfc = new FindOneFactorClusters(cov, TestType.TETRAD_BOLLEN, .0000001);
            Graph g = ftfc.search();
            System.out.println(g);
//
//            FindTwoFactorClusters ftfc = new FindTwoFactorClusters(cov, TestType.TETRAD_WISHART, .05);
//            Graph g = ftfc.search();
//            System.out.println(g);

//            int[][] pairCounts = ftfc.getPairCounts();
//            int count = 0;
//            int outof = 0;
//            int bound = 3;

//            for (int i = 0; i < pairCounts.length; i++) {
//                for (int j = i + 1; j < pairCounts.length; j++) {
//                    if (pairCounts[i][j] <= bound) {
//                        outof++;
//
//                        for (List<Node> scale : scales) {
//                            if (scale.contains(vars.get(i)) && scale.contains(vars.get(j))) {
//                                System.out.println(vars.get(i) + " und " + vars.get(j));
//                                count++;
//                            }
//                        }
//                    }
//                }
//            }

//            for (int y = 0; y < pairCounts.length; y++) {
//                List<Integer> w = new ArrayList<Integer>();
////                for (int s = 0; s < pairCounts.length; s++) w.add(s);
//
//                for (int i = 0; i < pairCounts.length; i++) {
//                    for (int j = i + 1; j < pairCounts.length; j++) {
//                        if (pairCounts[i][j] >= bound) {
//                            if (i == y) {
//                                w.add(new Integer(j));
//                            }
//                            if (j == y) {
//                                w.add(new Integer(i));
//                            }
//                        }
//                    }
//                }
//
//                System.out.print("&&& " + vars.get(y) + ":");
//                for (int _w : w) System.out.print(vars.get(_w) + " ");
//                System.out.println();
//            }

//            System.out.println(count + " out of " + outof + " " + count / (double) outof + "%");

//            FindOneFactorClusters fofc = new FindOneFactorClusters(cov, TestType.TETRAD_WISHART, 1e-15);
//            Graph g2 = fofc.search();
//            System.out.println(g2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test3() {
        DataReader reader = new DataReader();
        try {
            ICovarianceMatrix cov = reader.parseCovariance(new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2012.11.14/corr.txt"));

//            ICovarianceMatrix cov2 = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V20", "V24", "V25", "V29"});
//            ICovarianceMatrix cov2 = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V2", "V8", "V13", "V19", "V21", "V26"});
//            System.out.println(cov2);
//
//
//            System.out.println(cov2);


            IDeltaSextadTest test = new DeltaSextadTest(cov);

            List<Node> vars = cov.getVariables();

            List<Node> v = new ArrayList<Node>();

            // 1-indexed.
//            v.add(null);
//            for (Node _var : vars) v.add(_var);
//
//            System.out.println(v);
            List<Integer> distant = new ArrayList<Integer>();
            distant.add(1);
            distant.add(6);
            distant.add(7);
            distant.add(12);
            distant.add(17);
            distant.add(18);
            distant.add(20);
            distant.add(24);
            distant.add(25);
            distant.add(29);


            List<Integer> scale = distant;

            IDeltaSextadTest sextadTest = new DeltaSextadTest(cov);

            ChoiceGenerator gen = new ChoiceGenerator(scale.size(), 6);
            int[] choice;

            while ((choice = gen.next()) != null) {
                List<Integer> cluster = new ArrayList<Integer>();

                for (int i = 0; i < choice.length; i++) {
                    cluster.add(scale.get(choice[i]) - 1);
                }

//                double chisq = getClusterChiSquare(cluster, cov);
//
//                int dof = 4;
//                double q = ProbUtils.chisqCdf(chisq, dof);
//                double p = 1.0 - q;

                double p = significance(cluster, cov);

                Node[] n = new Node[6];
                for (int i = 0; i < 6; i++) n[i] = vars.get(scale.get(choice[i]) - 1);
                Sextad sextad = new Sextad(n);
                double sextadP = sextadTest.getPValue(sextad);

                NumberFormat nf = new DecimalFormat("0.0000");

                System.out.println(variablesForIndices(cluster, vars) + " bifactor model p = " + nf.format(p) + " sextad p = " + sextadP);
            }


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private List<Node> variablesForIndices(List<Integer> cluster, List<Node> variables) {
        List<Node> _cluster = new ArrayList<Node>();

        for (int c : cluster) {
            _cluster.add(variables.get(c));
        }

        return _cluster;
    }

    public void test4() {

        // Explore Fisher Z a bit.

        double sampleSize = 600;
        double zSize = 2;
        double r = 0.25;

        double fisherZ = Math.sqrt(sampleSize - zSize - 3.0) *
                0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

        System.out.println(fisherZ);

        double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));

        System.out.println("p = " + p);

    }

    public void test5() {
        try {
            DataReader reader = new DataReader();
            ICovarianceMatrix cov = reader.parseCovariance(new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2012.11.14/cov2.txt"));

            System.out.println(cov);

            ICovarianceMatrix cov2 = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V20", "V24", "V25", "V29",
                    "V2", "V8", "V13", "V19", "V21", "V26", "V30", "V32"});
//            ICovarianceMatrix cov2 = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V2", "V8", "V13", "V19", "V21", "V26"});
            System.out.println(cov2);


//            System.out.println(cov2);
//
//            DeltaSextadTest test = new DeltaSextadTest(cov);
//
            List<Node> vars = cov.getVariables();

            List<Node> v = new ArrayList<Node>();

            // 1-indexed.
            v.add(null);
            for (Node _var : vars) v.add(_var);
//
//            System.out.println(v);
//
            List<Node> distant = new ArrayList<Node>();
            distant.add(v.get(1));
            distant.add(v.get(6));
            distant.add(v.get(7));
            distant.add(v.get(12));
            distant.add(v.get(17));
            distant.add(v.get(18));
//            distant.add(v.get(20));
//            distant.add(v.get(24));
//            distant.add(v.get(25));
//            distant.add(v.get(29));

            List<Node> uninsightful = new ArrayList<Node>();
            uninsightful.add(v.get(2));
            uninsightful.add(v.get(8));
            uninsightful.add(v.get(13));
            uninsightful.add(v.get(19));
            uninsightful.add(v.get(21));
            uninsightful.add(v.get(26));
            uninsightful.add(v.get(30));
            uninsightful.add(v.get(32));

            List<Node> somatizing = new ArrayList<Node>();
            somatizing.add(v.get(3));
            somatizing.add(v.get(9));
            somatizing.add(v.get(14));
            somatizing.add(v.get(22));
            somatizing.add(v.get(27));

            List<Node> humorless = new ArrayList<Node>();
            humorless.add(v.get(4));
            humorless.add(v.get(10));
            humorless.add(v.get(15));
            humorless.add(v.get(23));
            humorless.add(v.get(28));

            List<Node> rigid = new ArrayList<Node>();
            rigid.add(v.get(5));
            rigid.add(v.get(11));
            rigid.add(v.get(16));
            rigid.add(v.get(31));
            rigid.add(v.get(33));

            List<List<Node>> scales = new ArrayList<List<Node>>();
            scales.add(distant);
            scales.add(uninsightful);
            scales.add(somatizing);
            scales.add(humorless);
            scales.add(rigid);

            IDeltaSextadTest test = new DeltaSextadTest(cov);

            List<Node> scale = distant;

            ChoiceGenerator gen = new ChoiceGenerator(scale.size(), 6);
            int[] choice;

            while ((choice = gen.next()) != null) {
                Node[] s = new Node[6];
                for (int i = 0; i < choice.length; i++) s[i] = scale.get(choice[i]);
                Sextad sextad = new Sextad(s[0], s[1], s[2], s[3], s[4], s[5]);
                System.out.println(test.getPValue(sextad));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test6() {
        try {
            DataReader reader = new DataReader();
            ICovarianceMatrix cov = reader.parseCovariance(new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2012.11.14/corr.txt"));
//            ICovarianceMatrix cov2 = cov.getSubmatrix(new String[]{"V1", "V6", "V7", "V12", "V17", "V18", "V20", "V24", "V25", "V29",
//                    "V2", "V8", "V13", "V19", "V21", "V26", "V30", "V32"});
//            System.out.println(cov2);
//
            List<Node> vars = cov.getVariables();
//            List<Node> vars2 = cov2.getVariables();

            IDeltaSextadTest sextadTest = new DeltaSextadTest(cov);

            List<Node> v = new ArrayList<Node>();

            // 1-indexed.
            v.add(null);
            for (Node _var : vars) v.add(_var);
//
//            System.out.println(v);
//
            List<Integer> test = new ArrayList<Integer>();
            test.add(1);
            test.add(6);
            test.add(7);
            test.add(12);
            test.add(17);
            test.add(21);

            List<Integer> _test = new ArrayList<Integer>();
            for (int i = 0; i < test.size(); i++) _test.add(test.get(i) - 1);

            double chisq = getClusterChiSquare(_test, cov);

            int dof = 4;
            double q = ProbUtils.chisqCdf(chisq, dof);
            double p = 1.0 - q;

            Node[] n = new Node[6];
            for (int i = 0; i < 6; i++) n[i] = vars.get(_test.get(i));
            Sextad sextad = new Sextad(n);
            double sextadP = sextadTest.getPValue(sextad);

//                if (p < 0.05) {
            System.out.println(variablesForIndices(_test, vars) + " " + chisq + " " + p + " " + sextadP);
//                }


//            List<Integer> test2 = new ArrayList<Integer>();
//            test2.add(1);
//            test2.add(2);
//            test2.add(3);
//            test2.add(4);
//            test2.add(5);
//            test2.add(6);
//
//            List<Integer> _test2 = new ArrayList<Integer>();
//            for (int i = 0; i < test2.size(); i++) _test2.add(test2.get(i) - 1);
//
//            double chisq2 = getClusterChiSquare(_test2, cov2);
//
//            int dof2 = 4;
//            double q2 = ProbUtils.chisqCdf(chisq2, dof2);
//            double p2 = 1.0 - q2;
//
//            Node[] n2 = new Node[6];
//            for (int i = 0; i < 6; i++) n2[i] = vars2.get(_test2.get(i));
//            Sextad sextad2 = new Sextad(n2);
//            double sextadP2 = sextadTest.getPValue(sextad2);
//
////                if (p < 0.05) {
//            System.out.println(variablesForIndices(_test2, vars2) + " " + chisq2 + " " + p2 + " " + sextadP2);
//                }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private double significance(List<Integer> cluster, DataModel dataModel) {
        double chisq = getClusterChiSquare(cluster, dataModel);
        int dof;

        // From the table of codimensions from "Algebraic factor analysis: tetrads, pentads and beyond" Drton et al.
        // Table 1, for m = 2 factors.
        if (cluster.size() == 5) {
            dof = 1;
        } else if (cluster.size() == 6) {
            dof = 4;
        } else if (cluster.size() == 7) {
            dof = 8;
        } else if (cluster.size() == 8) {
            dof = 13;
        } else if (cluster.size() == 9) {
            dof = 19;
        } else {
            throw new IllegalStateException("Can only do 5, 6, 7, 8, or 9 DOF.");
        }

        double q = ProbUtils.chisqCdf(chisq, dof);
        return 1.0 - q;
    }

    public void test7() {
        Sum t1 = sum(1, 2, 3, 4, 5, 6);
        Sum t2 = sum(1, 2, 4, 3, 5, 6);
        Sum t3 = sum(1, 2, 5, 3, 4, 6);
        Sum t4 = sum(1, 2, 6, 3, 4, 5);
        Sum t5 = sum(1, 3, 4, 2, 5, 6);
        Sum t6 = sum(1, 3, 5, 2, 4, 6);
        Sum t7 = sum(1, 3, 6, 2, 4, 5);
        Sum t8 = sum(1, 4, 5, 2, 3, 6);
        Sum t9 = sum(1, 4, 6, 2, 3, 5);
        Sum t10 = sum(1, 5, 6, 2, 3, 4);

        List<Sum> t = new ArrayList<Sum>();

        t.add(t1);
        t.add(t2);
        t.add(t3);
        t.add(t4);
        t.add(t5);
        t.add(t6);
        t.add(t7);
        t.add(t8);
        t.add(t9);
        t.add(t10);

        PermutationGenerator gen = new PermutationGenerator(t.size());
        int[] perm;
        int y = -1;

        while ((perm = gen.next()) != null) {
//            System.out.println(Arrays.toString(perm));

            Sum sum = new Sum();
            Map<Term, Integer> counts = new HashMap<Term, Integer>();
            Map<Term, Integer> index = new HashMap<Term, Integer>();
            int _index = 0;

            List<Integer> indices2 = new ArrayList<Integer>();
            Set<Term> sofar2 = new HashSet<Term>();
            Sum sum2 = new Sum();

            for (int i = 0; i < perm.length; i++) {
                List<Term> terms = t.get(perm[i]).getTerms();
                if (sum2.getTerms().containsAll(terms)) {
                    indices2.add(i);
                }
                for (Term term : terms) sum2.addTerm(1, term);
            }

            Set<Term> sofar = new HashSet<Term>();

            for (int i = 0; i < perm.length; i++) {
                Sum s = t.get(perm[i]);

                int coef = 1;

                if (!indices2.contains(i)) {
                    coef = -1;
                }

                System.out.print((perm[i] + 1) + ". ");

                for (Term term : s.getTerms()) {
                    sum.addTerm(coef * s.getCoef(term), term);
                    if (index.get(term) == null) index.put(term, ++_index);
                    if (counts.get(term) == null) counts.put(term, 0);
                    counts.put(term, counts.get(term) + 1);
                    System.out.print(index.get(term) + "-" + counts.get(term) + "  ");
                    sofar.add(term);
                }

                System.out.println();
            }

            sum.setIndex(index);
            System.out.println(sum);
            System.out.println();

            int numTerms = sum.getTerms().size();

//            if (++y > 10) break;

            if (sum.getTerms().isEmpty()) {
                System.out.println("Break");
                break;
            }
        }
    }

    public void test8() {
        Term term1 = new Term(1, 5, 4, 2, 6, 3);
        Term term2 = new Term(1, 5, 2, 6, 3, 4);
        Term term3 = new Term(1, 5, 3, 6, 4, 2);

        System.out.println(term1.equals(term3));
    }

    public void test9() {
        Sum t1 = sum(1, 2, 3, 4, 5, 6);
        Sum t2 = sum(1, 2, 4, 3, 5, 6);
        Sum t3 = sum(1, 2, 5, 3, 4, 6);
        Sum t4 = sum(1, 2, 6, 3, 4, 5);
        Sum t5 = sum(1, 3, 4, 2, 5, 6);
        Sum t6 = sum(1, 3, 5, 2, 4, 6);
        Sum t7 = sum(1, 3, 6, 2, 4, 5);
        Sum t8 = sum(1, 4, 5, 2, 3, 6);
        Sum t9 = sum(1, 4, 6, 2, 3, 5);
        Sum t10 = sum(1, 5, 6, 2, 3, 4);

        List<Sum> t = new ArrayList<Sum>();

        t.add(t1);
        t.add(t2);
        t.add(t3);
        t.add(t4);
        t.add(t5);
        t.add(t6);
        t.add(t7);
        t.add(t8);
        t.add(t9);
        t.add(t10);

        Map<Term, Integer> counts = new HashMap<Term, Integer>();

        Set<Term> _allTerms = new HashSet<Term>();

        for (int i = 0; i < t.size(); i++) {
            List<Term> terms = t.get(i).getTerms();
            _allTerms.addAll(terms);

            for (Term term : terms) {
                if (counts.get(term) == null) counts.put(term, 0);
                counts.put(term, counts.get(term) + 1);
            }
        }

        List<Term> allTerms = new ArrayList<Term>(_allTerms);

        System.out.println(allTerms);

        for (Term term : allTerms) {
            System.out.println(term + " " + counts.get(term));
        }

        double[][] arr = new double[allTerms.size()][t.size()];

        for (int i = 0; i < allTerms.size(); i++) {
            for (int j = 0; j < t.size(); j++) {
                boolean contains = t.get(j).getTerms().contains(allTerms.get(i));

                if (contains) {
                    arr[i][j] = t.get(j).getCoef(allTerms.get(i));
                } else {
                    arr[i][j] = 0;
                }
            }
        }

        System.out.println(MatrixUtils.toString(arr));

        Matrix m = new Matrix(arr);

        System.out.println("m rank = " + m.rank());

//        int[] cols = new int[]{0, 1, 2, 3, 4};
//        int[] rows = new int[]{0, 1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10, 11};
//
//        double[][] arr2 = new double[rows.length][cols.length];
//
//        for (int i : rows) {
//            for (int j : cols) {
//                arr2[i][j] = arr[i][j];
//            }
//        }
//
//        Matrix m2 = new Matrix(arr2);

//        System.out.println(MatrixUtils.toString(m.getArray()));
//
//        System.out.println("m2 rank = " + m2.rank());

        ChoiceGenerator gen = new ChoiceGenerator(10, 5);
        int[] cols;

        while ((cols = gen.next()) != null) {
//            int[] cols = new int[]{0, 1, 2, 3, 4};
            int[] rows = new int[]{0, 1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10, 11, 12, 13, 14};

            double[][] arr2 = new double[rows.length][cols.length];

            for (int i : rows) {
                for (int j = 0; j < 5; j++) {
                    arr2[i][j] = arr[i][cols[j]];
                }
            }

            Matrix m2 = new Matrix(arr2);

//            System.out.println(MatrixUtils.toString(m.getArray()));

            System.out.println("m2 rank = " + m2.rank() + " " + MatrixUtils.toString(cols));

        }

    }

    public Sum sum(int n1, int n2, int n3, int n4, int n5, int n6) {
        Sum sum = new Sum();

        for (Term term : positiveTerms(n1, n2, n3, n4, n5, n6)) {
            sum.addTerm(1, term);
        }

        for (Term term : negativeTerms(n1, n2, n3, n4, n5, n6)) {
            sum.addTerm(-1, term);
        }

        return sum;
    }

    public List<Term> positiveTerms(int n1, int n2, int n3, int n4, int n5, int n6) {
        Term term1 = new Term(n1, n4, n2, n5, n3, n6);
        Term term2 = new Term(n1, n5, n2, n6, n3, n4);
        Term term3 = new Term(n1, n6, n2, n4, n3, n5);

        ArrayList<Term> terms = new ArrayList<Term>();

        terms.add(term1);
        terms.add(term2);
        terms.add(term3);

        return terms;
    }

    public List<Term> negativeTerms(int n1, int n2, int n3, int n4, int n5, int n6) {
        Term term4 = new Term(n1, n6, n2, n5, n3, n4);
        Term term5 = new Term(n2, n6, n3, n5, n1, n4);
        Term term6 = new Term(n3, n6, n1, n5, n2, n4);

        ArrayList<Term> terms = new ArrayList<Term>();

        terms.add(term4);
        terms.add(term5);
        terms.add(term6);

        return terms;
    }

    private static class Sum {
        Map<Term, Integer> coefs = new HashMap<Term, Integer>();
        private Map<Term, Integer> index;

        public Sum() {

        }

        public void addTerm(int count, Term term) {
            if (coefs.get(term) == null) {
                coefs.put(term, 0);
            }

            coefs.put(term, coefs.get(term) + count);

            for (Term _term : new HashSet<Term>(coefs.keySet())) {
                if (coefs.get(_term) == 0) {
                    coefs.remove(_term);
                }
            }
        }

        public List<Term> getTerms() {
            return new ArrayList<Term>(coefs.keySet());
        }

        public int getCoef(Term term) {
            if (!coefs.keySet().contains(term)) throw new IllegalArgumentException("Not a term in this sum.");

            return coefs.get(term);
        }

        public String toString() {
            List<Term> terms = getTerms();
            StringBuilder buf = new StringBuilder();

            for (int i = 0; i < terms.size(); i++) {
                Term term = terms.get(i);
                buf.append(getCoef(term) + " * " + (index == null ? term : index.get(term)));

                if (i < terms.size() - 1) buf.append(" + ");
            }

            return buf.toString();
        }

        public void setIndex(Map<Term, Integer> index) {
            this.index = index;
        }
    }

    private static class Term {
        int[] n = new int[6];

        public Term(int n1, int n2, int n3, int n4, int n5, int n6) {
            n[0] = n1;
            n[1] = n2;
            n[2] = n3;
            n[3] = n4;
            n[4] = n5;
            n[5] = n6;
        }

        public int hashCode() {
            return 1;
        }

        public boolean equals(Object o) {
            Term term = (Term) o;

            PermutationGenerator gen = new PermutationGenerator(3);
            int[] perm;

            while ((perm = gen.next()) != null) {
                int[] p = new int[6];

                for (int i = 0; i < 3; i++) {
                    p[2 * i] = 2 * perm[i];
                    p[2 * i + 1] = 2 * perm[i] + 1;
                }

                boolean e1 = (n[0] == term.n[p[0]] && n[1] == term.n[p[1]]) || (n[0] == term.n[p[1]] && n[1] == term.n[p[0]]);
                boolean e2 = (n[2] == term.n[p[2]] && n[3] == term.n[p[3]]) || (n[2] == term.n[p[3]] && n[3] == term.n[p[2]]);
                boolean e3 = (n[4] == term.n[p[4]] && n[5] == term.n[p[5]]) || (n[4] == term.n[p[5]] && n[5] == term.n[p[4]]);

                if (e1 && e2 && e3) {
//                    System.out.println(this + " = " + term);
                    return true;
                }
            }

            return false;
        }

        public String toString() {
            return "<" + n[0] + "," + n[1] + "," + n[2] + "," + n[3] + "," + n[4] + "," + n[5] + ">";
        }

    }

    private double getClusterChiSquare(List<Integer> cluster, DataModel dataModel) {
        SemIm im = estimateModel(cluster, dataModel);
        return im.getChiSquare();
    }

    private SemIm estimateModel(List<Integer> sextet, DataModel dataModel) {
        List<Node> variables = dataModel.getVariables();

        Graph g = new EdgeListGraph();
        Node l1 = new GraphNode("L1");
        l1.setNodeType(NodeType.LATENT);
        Node l2 = new GraphNode("L2");
        l2.setNodeType(NodeType.LATENT);
        g.addNode(l1);
        g.addNode(l2);

        for (int i = 0; i < sextet.size(); i++) {
            Node n = variables.get(sextet.get(i));
            g.addNode(n);
            g.addDirectedEdge(l1, n);
            g.addDirectedEdge(l2, n);
        }

        SemPm pm = new SemPm(g);

        SemEstimator est;

        if (dataModel instanceof DataSet) {
            est = new SemEstimator((DataSet) dataModel, pm, new SemOptimizerPalCds());
        } else {
            est = new SemEstimator((CovarianceMatrix) dataModel, pm, new SemOptimizerPalCds());
        }

        return est.estimate();
    }

    public void testBatchFofc() {

        PrintStream out;

        try {
            out = new PrintStream(new File("/Users/josephramsey/Documents/fofc.results.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        int n = 10;

        for (int numStructuralNodes : new int[]{5, 10, 20, 50, 100, /*200*/}) {
            int sumTruePositives = 0;
            int sumFalsePositives = 0;
            int sumFalseNegatives = 0;

            double sumPrecision = 0.0;
            double sumRecall = 0.0;

            long sumTimes = 0;
            for (int i = 0; i < n; i++) {
                long start = System.currentTimeMillis();

//                int numStructuralNodes = 5;
                int maxStructuralEdges = numStructuralNodes;
                int measurementModelDegree = 6;
                int numLatentMeasuredImpureParents = 0;
                int numMeasuredMeasuredImpureParents = 2 * numStructuralNodes; // (int) (numStructuralNodes * measurementModelDegree * 0.2);
                int numMeasuredMeasuredImpureAssociations = 0;

                Graph trueGraph = GraphUtils.randomSingleFactorModel(numStructuralNodes,
                        maxStructuralEdges, measurementModelDegree,
                        numLatentMeasuredImpureParents,
                        numMeasuredMeasuredImpureParents,
                        numMeasuredMeasuredImpureAssociations);

                SemPm pm = new SemPm(trueGraph);
                SemIm im = new SemIm(pm);
                DataSet data = im.simulateData(1000, false);
                data = DataUtils.shuffleColumns(data);

                FindOneFactorClusters ftfc = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, .0001);

                Graph searchGraph2 = ftfc.search();

                Graph searchGraph = reidentifyVariables(searchGraph2, trueGraph);

                List<List<Node>> clusters = ftfc.getClusters();
                System.out.println(clusters);
                List<String> latentNames = new ArrayList<String>();

                for (Node node : searchGraph.getNodes()) {
                    if (node.getNodeType() == NodeType.LATENT) {
                        System.out.println(node.getName());
                        latentNames.add(node.getName());
                    }
                }

                int truePositives = 0;
                int falsePositives = 0;

                for (String s : latentNames) {
                    if (s.startsWith("_")) {
                        falsePositives++;
                    } else {
                        truePositives++;
                    }
                }

                if (truePositives == 0) {
                    i--;
                    continue;
                }

                int falseNegatives = numStructuralNodes - truePositives;

                double precision = truePositives / (double) (truePositives + falsePositives);
                double recall = truePositives / (double) (truePositives + falseNegatives);

                System.out.println("True positives = " + truePositives);
                System.out.println("False positives = " + falsePositives);
                System.out.println("False negatives = " + falseNegatives);

                System.out.println("Precision = " + precision);
                System.out.println("Recall = " + recall);

                sumTruePositives += truePositives;
                sumFalsePositives += falsePositives;
                sumFalseNegatives += falseNegatives;

                sumPrecision += precision;
                sumRecall += recall;

                double time = (System.currentTimeMillis() - start) / 1000.0;

                System.out.println("time = " + time);

                sumTimes += (System.currentTimeMillis() - start);
            }

            double avgTruePositives = sumTruePositives / (double) n;
            double avgFalsePositives = sumFalsePositives / (double) n;
            double avgFalseNegatives = sumFalseNegatives / (double) n;

            double avgPrecision = sumPrecision / (double) n;
            double avgRecall = sumRecall / (double) n;

            double avgTime = (sumTimes / 1000) / (double) n;

            out.println("-----------------");
            out.println("# latents = " + numStructuralNodes);

            out.println();
            out.println("Average true positives = " + avgTruePositives);
            out.println("Average false positives = " + avgFalsePositives);
            out.println("Average false negatives = " + avgFalseNegatives);
            out.println();

            out.println("Average precision = " + avgPrecision);
            out.println("Average recall = " + avgRecall);
            out.println();

            out.println("Average time = " + avgTime);
        }

        out.close();
    }

    // This reidentifies a variable if all of its members belong to one of the clusters
    // in the original graph.
    private Graph reidentifyVariables(Graph searchGraph, Graph trueGraph) {
        if (trueGraph == null) {
            return searchGraph;
        }

        Graph reidentifiedGraph = new EdgeListGraph();
//        Graph trueGraph = semIm.getSemPm().getGraph();

        for (Node latent : searchGraph.getNodes()) {
            if (latent.getNodeType() != NodeType.LATENT) {
                continue;
            }

            boolean added = false;

            List<Node> searchChildren = searchGraph.getChildren(latent);

            for (Node _latent : trueGraph.getNodes()) {
                if (_latent.getNodeType() != NodeType.LATENT) ;

                List<Node> trueChildren = trueGraph.getChildren(_latent);

                for (Node node2 : new ArrayList<Node>(trueChildren)) {
                    if (node2.getNodeType() == NodeType.LATENT) {
                        trueChildren.remove(node2);
                    }
                }

                boolean containsAll = true;

                for (Node child : searchChildren) {
                    boolean contains = false;

                    for (Node _child : trueChildren) {
                        if (child.getName().equals(_child.getName())) {
                            contains = true;
                            break;
                        }
                    }

                    if (!contains) {
                        containsAll = false;
                        break;
                    }
                }

                if (containsAll) {
                    reidentifiedGraph.addNode(_latent);

                    for (Node child : searchChildren) {
                        if (!reidentifiedGraph.containsNode(child)) {
                            reidentifiedGraph.addNode(child);
                        }

                        reidentifiedGraph.addDirectedEdge(_latent, child);
                    }

                    added = true;
                    break;
                }
            }

            if (!added) {
                reidentifiedGraph.addNode(latent);

                for (Node child : searchChildren) {
                    if (!reidentifiedGraph.containsNode(child)) {
                        reidentifiedGraph.addNode(child);
                    }

                    reidentifiedGraph.addDirectedEdge(latent, child);
                }
            }
        }

        return reidentifiedGraph;
    }

    public void test20() {
        try {
            File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.03.01/regions.txt");
            BufferedReader in = new BufferedReader(new FileReader(file));
            File file2 = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.03.01/regions2.txt");
            PrintStream out = new PrintStream(new FileOutputStream(file2));

            int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;

            String line;

            while ((line = in.readLine()) != null) {
//                System.out.println(line);

                String[] tokens = line.split("\t");

                String index = tokens[0];
                String x = tokens[1];
                String y = tokens[2];
                String z = tokens[3];

                int _x = Integer.parseInt(x);
                int _y = Integer.parseInt(y);
                int _z = Integer.parseInt(z);

                if (_x < minX) minX = _x;
                if (_x > maxX) maxX = _x;
                if (_y < minY) minY = _y;
                if (_y > maxY) maxY = _y;
                if (_z < minZ) minZ = _z;
                if (_z > maxZ) maxZ = _z;

                out.println("V" + index + " (" + x + "," + y + "," + z + ")");
            }

            System.out.println("minx = " + minX);
            System.out.println("maxx = " + maxX);
            System.out.println("miny = " + minY);
            System.out.println("maxy = " + maxY);
            System.out.println("minz = " + minZ);
            System.out.println("maxz = " + maxZ);

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestFTFC.class);
    }
}
