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

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataModelList;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class ImagesRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener, IGesRunner, Indexable {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;
    private List<ScoredGraph> topGraphs;
    private int index;
    private transient IImages images;
    private Graph graph;

    //============================CONSTRUCTORS============================//

    public ImagesRunner(DataWrapper dataWrapper, GesParams params, KnowledgeBoxModel knowledgeBoxModel) {
        super(dataWrapper, params, knowledgeBoxModel);
    }

    public ImagesRunner(DataWrapper dataWrapper, GesParams params) {
        super(dataWrapper, params, null);
    }

    public ImagesRunner(DataWrapper dataWrapper, GraphWrapper graph, GesParams params) {
        super(dataWrapper, params, null);
        this.graph = graph.getGraph();
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public ImagesRunner(GraphSource graphWrapper, PcSearchParams params, KnowledgeBoxModel knowledgeBoxModel) {
        super(graphWrapper.getGraph(), params, knowledgeBoxModel);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public ImagesRunner(GraphSource graphWrapper, PcSearchParams params) {
        super(graphWrapper.getGraph(), params, null);
    }
    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ImagesRunner serializableInstance() {
        return new ImagesRunner(DataWrapper.serializableInstance(),
                GesParams.serializableInstance(), KnowledgeBoxModel.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() {
        DataModelList list = getDataModelList();

        for (DataModel dataModel : list) {
            if (!(dataModel instanceof DataSet)) {
                throw new IllegalArgumentException("Must provide a list of data sets.");
            }
        }

        List<DataSet> continuousDataSets = new ArrayList<DataSet>();

        for (DataModel dataModel : list) {
            DataSet dataSet = (DataSet) dataModel;

            if (!dataSet.isContinuous()) {
                    throw new IllegalArgumentException("Must provide continuous data sets.");
            }

            continuousDataSets.add(dataSet);
        }

        List<String> names = continuousDataSets.get(0).getVariableNames();

        for (DataSet dataSet : continuousDataSets) {
            if (!dataSet.getVariableNames().equals(names)) {
                throw new IllegalArgumentException("The variable names must be the same " +
                        "for all data sets.");
            }
        }

        for (DataSet dataSet : continuousDataSets) {
            for (Node node : dataSet.getVariables()) {
                int index = dataSet.getVariables().indexOf(node);
                boolean missing = true;

                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    if (dataSet.getDouble(i, index) != 0) {
                        missing = false;
                        break;
                    }
                }

                if (missing) {
                    for (int i = 0; i < dataSet.getNumRows(); i++) {
                        dataSet.setDouble(i, index, Double.NaN);
                    }
                }
            }
        }

        double penalty = ((GesParams) getParams()).getComplexityPenalty();

        GesParams gesParams = (GesParams) getParams();
        GesIndTestParams indTestParams = (GesIndTestParams) gesParams.getIndTestParams();

        if (graph != null) {
            Reorienter reorienter = new Images3Orienter(continuousDataSets);
            reorienter.orient(graph);
            reorienter.setKnowledge(getParams().getKnowledge());
            setResultGraph(graph);
            this.topGraphs = Collections.singletonList(new ScoredGraph(graph, 1.0));
            return;
        }

//        if (graph != null) {
////            Reorienter reorienter = new Images3Orienter(continuousDataSets);
////            reorienter.orient(graph);                 2
////            reorienter.setKnowledge(getParams().getKnowledge());
////            setResultGraph(graph);
//            this.topGraphs = Collections.singletonList(new ScoredGraph(graph, 1.0));
//
//            images = new Images(continuousDataSets);
//            Images images1 = (Images) images;
//
//            double lnL = images1.likelihood(graph);
//
//            Graph empty = new EdgeListGraph(graph);
//            empty.removeEdges(empty.getEdges());
//
//            System.out.println(empty);
//
//            double lnL0 = images1.likelihood(empty);
//            double lr = 2 * (lnL - lnL0);//   / continuousDataSets.size();
//
//            System.out.println("ln L " + lnL);
//                System.out.println("ln L0 " + lnL0);
//            System.out.println("LR = " + lr);
//
//            int nodes = graph.getNumNodes();
//            int edges = graph.getNumEdges();
//
//            int k0 = nodes;
//            int k = nodes + edges;
//
//            int diff = (k - k0) * continuousDataSets.size();
//
//            System.out.println("k0 = " + k0);
//            System.out.println("k = " + k);
//            System.out.println("diff = " + diff);
//
////            return;
//        }

        if (indTestParams.isFirstNontriangular()) {
           images = new ImagesFirstNontriangular(continuousDataSets);
//           ((ImagesFirstNontriangular) images).setPenalty(penalty);
        }
        else {
            images = new Images(continuousDataSets);
            images.setPenaltyDiscount(penalty);
        }

        images.addPropertyChangeListener(this);
        images.setKnowledge(getParams().getKnowledge());    
        images.setNumPatternsToStore(indTestParams.getNumPatternsToSave());

        Graph graph = images.search();
        setResultGraph(graph);
        indTestParams.setPenaltyDiscount(images.getPenaltyDiscount());

        if (getSourceGraph() != null) {
            GraphUtils.arrangeBySourceGraph(graph, getSourceGraph());
        }
        else if (getParams().getKnowledge().isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(graph, getParams().getKnowledge());
        }
        else {
            GraphUtils.circleLayout(graph, 200, 200, 150);
        }

        this.topGraphs = new ArrayList<ScoredGraph>(images.getTopGraphs());
        setIndex(topGraphs.size() - 1);
    }

    public void setIndex(int index) {
        if (index < 0 || index > topGraphs.size()) {
            throw new IllegalArgumentException("Must be in [0, " + (topGraphs.size() - 1) + ".");
        }

        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Graph getGraph() {
        return getTopGraphs().get(getIndex()).getGraph();
    }


    /**
     * @return the names of the triple classifications. Coordinates with
     */
    public List<String> getTriplesClassificationTypes() {
        return new ArrayList<String>();
    }

    /**
     * @param node
     * @return the list of triples corresponding to <code>getTripleClassificationNames</code>.
     */
    public List<List<Triple>> getTriplesLists(Node node) {
        return new ArrayList<List<Triple>>();
    }

    public boolean supportsKnowledge() {
        return true;
    }

    public ImpliedOrientation getMeekRules() {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(getParams().getKnowledge());
        return rules;
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

    public List<ScoredGraph> getTopGraphs() {
        return this.topGraphs;
    }

    public String getBayesFactorsReport(Graph dag) {
        if (images == null) {
            return "Please re-run IMaGES.";
        }
        else {
            return images.logEdgeBayesFactorsString(dag);
        }
    }

    public String getBootstrapEdgeCountsReport(int numBootstraps) {
        if (images == null) {
            return "Please re-run IMaGES.";
        }
        else {
            return images.bootstrapPercentagesString(numBootstraps);
        }
    }

    public GraphScorer getGraphScorer() {
        return images;
    }
    
}



