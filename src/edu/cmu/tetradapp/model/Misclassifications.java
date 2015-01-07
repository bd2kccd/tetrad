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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TextTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.*;


/**
 * Compares a target workbench with a reference workbench by counting errors of
 * omission and commission.  (for edge presence only, not orientation).
 *
 * @author Joseph Ramsey
 * @author Erin Korber (added remove latents functionality July 2004)
 */
public final class Misclassifications implements SessionModel {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private GraphComparisonParams params;

    /**
     * The target workbench.
     *
     * @serial Cannot be null.
     */
    private Graph targetGraph;

    /**
     * The workbench to which the target workbench is being compared.
     *
     * @serial Cannot be null.
     */
    private Graph referenceGraph;

    /**
     * The true DAG, if available. (May be null.)
     */
    private Graph trueGraph;

    private NumberFormat nf;
    private List<Node> nodes;

    //=============================CONSTRUCTORS==========================//

    /**
     * Compares the results of a Pc to a reference workbench by counting errors
     * of omission and commission. The counts can be retrieved using the methods
     * <code>countOmissionErrors</code> and <code>countCommissionErrors</code>.
     */
    public Misclassifications(SessionModel model1, SessionModel model2,
                              GraphComparisonParams params) {
        if (params == null) {
            throw new NullPointerException("Params must not be null");
        }

        // Need to be able to construct this object even if the models are
        // null. Otherwise the interface is annoying.
        if (model2 == null) {
            model2 = new DagWrapper(new Dag());
        }

        if (model1 == null) {
            model1 = new DagWrapper(new Dag());
        }

        if (!(model1 instanceof GraphSource) ||
                !(model2 instanceof GraphSource)) {
            throw new IllegalArgumentException("Must be graph sources.");
        }

        this.params = params;

        String referenceName = this.params.getReferenceGraphName();

        if (referenceName == null) {
            throw new IllegalArgumentException("Must specify a reference graph.");
//            this.referenceGraph = ((GraphSource) model1).getGraph();
//            this.targetGraph = ((GraphSource) model2).getGraph();
//            this.params.setReferenceGraphName(model1.getName());
        } else if (referenceName.equals(model1.getName())) {
            this.referenceGraph = ((GraphSource) model1).getGraph();
            this.targetGraph = ((GraphSource) model2).getGraph();
        } else if (referenceName.equals(model2.getName())) {
            this.referenceGraph = ((GraphSource) model2).getGraph();
            this.targetGraph = ((GraphSource) model1).getGraph();
        } else {
            throw new IllegalArgumentException(
                    "Neither of the supplied session " + "models is named '" +
                            referenceName + "'.");
        }

//        this.referenceGraph = GraphUtils.replaceNodes(referenceGraph, nodes);
        this.targetGraph = GraphUtils.replaceNodes(targetGraph, this.referenceGraph.getNodes());

        Set<Node> _nodes = new HashSet<Node>(this.referenceGraph.getNodes());
        _nodes.addAll(this.targetGraph.getNodes());
        this.nodes = new ArrayList<Node>(_nodes);


        TetradLogger.getInstance().log("info", "Graph Comparison");
        TetradLogger.getInstance().log("comparison", getComparisonString());

        this.nf = NumberFormatUtil.getInstance().getNumberFormat();
    }

    public Misclassifications(GraphWrapper referenceGraph,
                              AbstractAlgorithmRunner algorithmRunner,
                              GraphComparisonParams params) {
        this(referenceGraph, (SessionModel) algorithmRunner,
                params);
    }

    public Misclassifications(GraphWrapper referenceWrapper,
                              GraphWrapper targetWrapper, GraphComparisonParams params) {
        this(referenceWrapper, (SessionModel) targetWrapper,
                params);
    }

    public Misclassifications(DagWrapper referenceGraph,
                              AbstractAlgorithmRunner algorithmRunner,
                              GraphComparisonParams params) {
        this(referenceGraph, (SessionModel) algorithmRunner,
                params);
    }

    public Misclassifications(DagWrapper referenceWrapper,
                              GraphWrapper targetWrapper, GraphComparisonParams params) {
        this(referenceWrapper, (SessionModel) targetWrapper,
                params);
    }

    public Misclassifications(Graph referenceGraph, Graph targetGraph) {
        this.referenceGraph = referenceGraph;
        this.targetGraph = targetGraph;

        TetradLogger.getInstance().log("info", "Graph Comparison");
        TetradLogger.getInstance().log("comparison", getComparisonString());

        this.nf = NumberFormatUtil.getInstance().getNumberFormat();

        this.targetGraph = GraphUtils.replaceNodes(targetGraph, this.referenceGraph.getNodes());

        Set<Node> _nodes = new HashSet<Node>(this.referenceGraph.getNodes());
        _nodes.addAll(this.targetGraph.getNodes());
        this.nodes = new ArrayList<Node>(_nodes);
    }

    public Misclassifications(Graph referenceGraph, Graph targetGraph,
                              Graph trueGraph) {
        this.referenceGraph = referenceGraph;
        this.targetGraph = targetGraph;
        this.trueGraph = trueGraph;

        TetradLogger.getInstance().log("info", "Graph Comparison");
        TetradLogger.getInstance().log("comparison", getComparisonString());

        this.nf = NumberFormatUtil.getInstance().getNumberFormat();

        this.targetGraph = GraphUtils.replaceNodes(targetGraph, this.referenceGraph.getNodes());

        Set<Node> _nodes = new HashSet<Node>(this.referenceGraph.getNodes());
        _nodes.addAll(this.targetGraph.getNodes());
        this.nodes = new ArrayList<Node>(_nodes);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Misclassifications serializableInstance() {
        DagWrapper wrapper1 = DagWrapper.serializableInstance();
        wrapper1.setName("Ref");
        DagWrapper wrapper2 = DagWrapper.serializableInstance();
        GraphComparisonParams graphComparisonParams = GraphComparisonParams.serializableInstance();
        graphComparisonParams.setReferenceGraphName("Ref");
        return new Misclassifications(wrapper1, wrapper2, graphComparisonParams);
    }

    //==============================PUBLIC METHODS========================//

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComparisonString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Comparing " + params.getTargetGraphName() + " to " + params.getReferenceGraphName());
        builder.append("\n\nEndpoint Misclassification:\n");
        builder.append(endpointMisclassification(getNodes(), targetGraph, referenceGraph));
        builder.append("\nEdge Misclassifications:\n");
        builder.append(edgeMisclassifications(getNodes(), targetGraph, referenceGraph));
        return builder.toString();
    }

    private String endpointMisclassification(List<Node> _nodes, Graph estGraph, Graph refGraph) {
        this.nf = NumberFormatUtil.getInstance().getNumberFormat();

        int[][] counts = new int[4][4];

        for (int i = 0; i < _nodes.size(); i++) {
            for (int j = 0; j < _nodes.size(); j++) {
                if (i == j) continue;

                Endpoint endpoint1 = refGraph.getEndpoint(_nodes.get(i), _nodes.get(j));
                Endpoint endpoint2 = estGraph.getEndpoint(_nodes.get(i), _nodes.get(j));

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

        return table2.toString();

//        println("\n" + name);
//        println(table2.toString());
//        println("");
    }

    private String edgeMisclassifications(List<Node> _nodes, Graph estGraph, Graph trueGraph) {
        this.nf = NumberFormatUtil.getInstance().getNumberFormat();

        StringBuilder builder = new StringBuilder();

        Node a = new GraphNode("a");
        Node b = new GraphNode("b");

        List<Edge> trueEdgeTypes = new ArrayList<Edge>();

        trueEdgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.TAIL));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.CIRCLE));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.ARROW));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.CIRCLE));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.ARROW));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.TAIL));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.ARROW));
        trueEdgeTypes.add(new Edge(a, b, Endpoint.NULL, Endpoint.NULL));

        List<Edge> estEdgeTypes = new ArrayList<Edge>();

        estEdgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.TAIL));
        estEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.CIRCLE));
        estEdgeTypes.add(new Edge(a, b, Endpoint.CIRCLE, Endpoint.ARROW));
        estEdgeTypes.add(new Edge(a, b, Endpoint.TAIL, Endpoint.ARROW));
        estEdgeTypes.add(new Edge(a, b, Endpoint.ARROW, Endpoint.ARROW));
        estEdgeTypes.add(new Edge(a, b, Endpoint.NULL, Endpoint.NULL));

        int[][] counts = new int[8][6];
        Graph graph = new EdgeListGraph(_nodes);
        graph.fullyConnect(Endpoint.TAIL);

        for (int m = 0; m < 8; m++) {
            for (int n = 0; n < 6; n++) {
                for (Edge fullEdge : graph.getEdges()) {
                    if (m == 3 || m == 5) {
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

        TextTable table2 = new TextTable(9, 7);

        table2.setToken(1, 0, "---");
        table2.setToken(2, 0, "o-o");
        table2.setToken(3, 0, "o->");
        table2.setToken(4, 0, "<-o");
        table2.setToken(5, 0, "-->");
        table2.setToken(6, 0, "<--");
        table2.setToken(7, 0, "<->");
        table2.setToken(8, 0, "null");
        table2.setToken(0, 1, "---");
        table2.setToken(0, 2, "o-o");
        table2.setToken(0, 3, "o->");
        table2.setToken(0, 4, "-->");
        table2.setToken(0, 5, "<->");
        table2.setToken(0, 6, "null");

        // Need the sum of cells except the null-null cell.
        int sum = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                if (i == 7 && j == 5) continue;
                sum += counts[i][j];
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                if (i == 7 && j == 5) table2.setToken(i + 1, j + 1, "XXXXX");
                else table2.setToken(i + 1, j + 1, nf.format(counts[i][j] / (double) sum));
            }
        }

        builder.append("\n" + table2.toString());

//        println("\n" + name);
//        println(table2.toString());
//        println("");

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

        for (int i = 0; i < 7; i++) {
            _sum += counts[i][0];
        }

        _counts[1][0] = _sum;
        _sum = 0;

        for (int i = 0; i < 5; i++) {
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

        _counts[1][1] = counts[7][5];

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

//        out.println("Null\n");
//
        builder.append("\n" + table3);

        return builder.toString();
    }


    private int getIndex(Endpoint endpoint) {
        if (endpoint == Endpoint.CIRCLE) return 0;
        if (endpoint == Endpoint.ARROW) return 1;
        if (endpoint == Endpoint.TAIL) return 2;
        if (endpoint == null) return 3;
        throw new IllegalArgumentException();
    }

    //============================PRIVATE METHODS=========================//

    //This removes the latent nodes in G and connects nodes that were formerly
    //adjacent to the latent node with an undirected edge (edge type doesnt matter).
    private static Graph removeLatent(Graph g) {
        Graph result = new EdgeListGraph(g);
        result.setGraphConstraintsChecked(false);

        List<Node> allNodes = g.getNodes();
        LinkedList<Node> toBeRemoved = new LinkedList<Node>();

        for (Node curr : allNodes) {
            if (curr.getNodeType() == NodeType.LATENT) {
                List<Node> adj = result.getAdjacentNodes(curr);

                for (int i = 0; i < adj.size(); i++) {
                    Node a = adj.get(i);
                    for (int j = i + 1; j < adj.size(); j++) {
                        Node b = adj.get(j);

                        if (!result.isAdjacentTo(a, b)) {
                            result.addEdge(Edges.undirectedEdge(a, b));
                        }
                    }
                }

                toBeRemoved.add(curr);
            }
        }

        result.removeNodes(toBeRemoved);
        return result;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (params == null) {
            throw new NullPointerException();
        }

        if (targetGraph == null) {
            throw new NullPointerException();
        }

        if (referenceGraph == null) {
            throw new NullPointerException();
        }
    }

    public Graph getTrueGraph() {
        return trueGraph;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public GraphComparisonParams getParams() {
        return params;
    }


    public List<Node> getNodes() {
        return nodes;
    }
}
