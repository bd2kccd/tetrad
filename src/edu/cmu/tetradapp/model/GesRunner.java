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
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Triple;
import edu.cmu.tetrad.search.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class GesRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener, Indexable, IGesRunner {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;
    private Map<Graph, Double> dagsToScores;
    private List<ScoredGraph> topGraphs;
    private int index;
    private List<Map<Graph, Double>> allDagsToScores;
    private Graph trueGraph;

    //============================CONSTRUCTORS============================//

    public GesRunner(DataWrapper dataWrapper, GesParams params) {
        super(dataWrapper, params, null);
    }

    public GesRunner(DataWrapper dataWrapper, GraphWrapper trueGraph, GesParams params) {
        this(dataWrapper, params, null);
        this.trueGraph = trueGraph.getGraph();
    }

    public GesRunner(DataWrapper dataWrapper, GesParams params, KnowledgeBoxModel knowledgeBoxModel) {
    	super(dataWrapper, params, knowledgeBoxModel);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public GesRunner(GraphSource graphWrapper, PcSearchParams params, KnowledgeBoxModel knowledgeBoxModel) {
        super(graphWrapper.getGraph(), params, knowledgeBoxModel);
    }
    
    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static IGesRunner serializableInstance() {
        return new GesRunner(DataWrapper.serializableInstance(),
                GesParams.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    public void execute() {
        Object source = getDataModel();

        GesParams gesParams = (GesParams) getParams();
        GesIndTestParams indTestParams = (GesIndTestParams) gesParams.getIndTestParams();
        double penalty = gesParams.getComplexityPenalty();
        Ges ges;

        if (source instanceof ICovarianceMatrix) {
            ges = new Ges((ICovarianceMatrix) source);
            ges.addPropertyChangeListener(this);
            ges.setAggressivelyPreventCycles(isAggressivelyPreventCycles());
            ges.setKnowledge(getParams().getKnowledge());
            ges.setSamplePrior(((GesParams) getParams()).getCellPrior());
            ges.setStructurePrior(((GesParams) getParams()).getStructurePrior());
            ges.setPenaltyDiscount(penalty);
            ges.setNumPatternsToStore(indTestParams.getNumPatternsToSave());
        } else if (source instanceof DataSet) {
            ges = new Ges((DataSet) source);
            ges.addPropertyChangeListener(this);
            ges.setAggressivelyPreventCycles(isAggressivelyPreventCycles());
            ges.setKnowledge(getParams().getKnowledge());
            ges.setSamplePrior(((GesParams) getParams()).getCellPrior());
            ges.setStructurePrior(((GesParams) getParams()).getStructurePrior());
            ges.setPenaltyDiscount(penalty);
            ges.setNumPatternsToStore(indTestParams.getNumPatternsToSave());
        } else {
            throw new RuntimeException(
                    "GES does not accept this type of data input.");
        }

        ges.setTrueGraph(trueGraph);

        Graph graph = ges.search();
        setResultGraph(graph);

        if (getSourceGraph() != null) {
            GraphUtils.arrangeBySourceGraph(graph, getSourceGraph());
        }
        else if (getParams().getKnowledge().isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(graph, getParams().getKnowledge());
        }
        else {
            GraphUtils.circleLayout(graph, 200, 200, 150);
        }

        this.topGraphs = new ArrayList<ScoredGraph>(ges.getTopGraphs());
        this.allDagsToScores = new ArrayList<Map<Graph, Double>>();

        for (ScoredGraph scoredGraph : topGraphs) {
            Map<Graph, Double> dagsToScores = scoreGraphs(ges, scoredGraph.getGraph());
            this.allDagsToScores.add(dagsToScores);
        }

        setIndex(topGraphs.size() - 1);
    }

    public void setIndex(int index) {
        if (index < 0 || index > topGraphs.size()) {
            throw new IllegalArgumentException("Must be in [0, " + (topGraphs.size() - 1) + ".");
        }

        this.dagsToScores = this.allDagsToScores.get(index);
        this.index = index;
//        firePropertyChange(new PropertyChangeEvent(this, "modelChanged", null, null);
    }

    public int getIndex() {
        return index;
    }

    private Map<Graph, Double> scoreGraphs(Ges ges, Graph graph) {
        Map<Graph, Double> dagsToScores = new HashMap<Graph, Double>();

        if (false) {
            final List<Graph> dags = SearchGraphUtils.generatePatternDags(graph, true);


            for (Graph _graph : dags) {
                double score = ges.scoreGraph(_graph);
                dagsToScores.put(_graph, score);
            }
        }

        return dagsToScores;
    }

    public Graph getGraph() {
        return getTopGraphs().get(getIndex()).getGraph();
//        return getResultGraph();
    }

    /**
     * @return the names of the triple classifications. Coordinates with getTriplesList.
     */
    public List<String> getTriplesClassificationTypes() {
        List<String> names = new ArrayList<String>();
//        names.add("Colliders");
//        names.add("Noncolliders");
        return names;
    }

    /**
     * @return the list of triples corresponding to <code>getTripleClassificationNames</code>
     * for the given node.
     */
    public List<List<Triple>> getTriplesLists(Node node) {
        List<List<Triple>> triplesList = new ArrayList<List<Triple>>();
        Graph graph = getGraph();
//        triplesList.add(GraphUtils.getCollidersFromGraph(node, graph));
//        triplesList.add(GraphUtils.getNoncollidersFromGraph(node, graph));
        return triplesList;
    }

    public boolean supportsKnowledge() {
        return true;
    }

    public ImpliedOrientation getMeekRules() {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(getParams().getKnowledge());
        return rules;
    }

    private boolean isAggressivelyPreventCycles() {
        SearchParams params = getParams();
        if (params instanceof MeekSearchParams) {
            return ((MeekSearchParams) params).isAggressivelyPreventCycles();
        }
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
    }

    private void firePropertyChange(PropertyChangeEvent evt) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(evt);
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (!getListeners().contains(l)) getListeners().add(l);
    }


//    public Map<Graph, Double> getDagsToScores() {
//        return dagsToScores;
//    }

    public List<ScoredGraph> getTopGraphs() {
        return this.topGraphs;
    }

    public GraphScorer getGraphScorer() {
        return null;
    }
}



