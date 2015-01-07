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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Triple;
import edu.cmu.tetrad.search.Ccd;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.SearchGraphUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the CCD algorithm.
 *
 * @author Frank Wimberly after Shane Harwood's PcRunner
 */

public class CcdRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public CcdRunner(DataWrapper dataWrapper, BasicSearchParams params) {
        super(dataWrapper, params, null);
    }
    
//    /**
//     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
//     * contain a DataSet that is either a DataSet or a DataSet or a DataList
//     * containing either a DataSet or a DataSet as its selected model.
//     */
//    public CcdRunner(DataWrapper dataWrapper, KnowledgeBoxModel knowledgeBoxModel, BasicSearchParams params) {
//        super(dataWrapper, params, knowledgeBoxModel);
//    }
//
//    /**
//     * Constucts a wrapper for the given EdgeListGraph.
//     */
//    public CcdRunner(GraphSource graphWrapper, KnowledgeBoxModel knowledgeBoxModel, PcSearchParams params) {
//        super(graphWrapper.getGraph(), params, knowledgeBoxModel);
//    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CcdRunner(GraphSource graphWrapper, PcSearchParams params) {
        super(graphWrapper.getGraph(), params, null);
    }
    
   
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CcdRunner(GraphWrapper graphWrapper, BasicSearchParams params) {
        super(graphWrapper.getGraph(), params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CcdRunner(GraphWrapper graphWrapper, KnowledgeBoxModel knowledgeBoxModel, BasicSearchParams params) {
        super(graphWrapper.getGraph(), params, knowledgeBoxModel);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CcdRunner(DagWrapper dagWrapper, BasicSearchParams params) {
        super(dagWrapper.getDag(), params);
    }
    
    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
//    public CcdRunner(DagWrapper dagWrapper, KnowledgeBoxModel knowledgeBoxModel, BasicSearchParams params) {
//        super(dagWrapper.getDag(), params, knowledgeBoxModel);
//    }

    public CcdRunner(SemGraphWrapper dagWrapper, BasicSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

//    public CcdRunner(SemGraphWrapper dagWrapper, KnowledgeBoxModel knowledgeBoxModel, BasicSearchParams params) {
//        super(dagWrapper.getGraph(), params, knowledgeBoxModel);
//    }

    public CcdRunner(IndependenceFactsModel model, BasicSearchParams params) {
        super(model, params, null);
    }

    public CcdRunner(IndependenceFactsModel model, BasicSearchParams params, KnowledgeBoxModel knowledgeBoxModel) {
        super(model, params, knowledgeBoxModel);
    }

	/**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CcdRunner serializableInstance() {
        return new CcdRunner(DataWrapper.serializableInstance(), BasicSearchParams.serializableInstance());
    }

    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() {
        Knowledge knowledge = getParams().getKnowledge();
        Ccd ccd = new Ccd(getIndependenceTest(), knowledge);
        ccd.setDepth(getParams().getIndTestParams().getDepth());
        Graph graph = ccd.search();

        if (knowledge.isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
        }

        setResultGraph(graph);

        if (getSourceGraph() != null) {
            GraphUtils.arrangeBySourceGraph(graph, getSourceGraph());
        }
        else if (knowledge.isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
        }
        else {
            GraphUtils.circleLayout(graph, 200, 200, 150);
        }

    }

    public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataModel();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        BasicSearchParams params = (BasicSearchParams) getParams();
        IndTestType testType = params.getIndTestType();
        return new IndTestChooser().getTest(dataModel, params, testType);
    }

    public Graph getGraph() {
        return getResultGraph();
    }


    /**
     * @return the names of the triple classifications. Coordinates with <code>getTriplesList</code>
     */
    public List<String> getTriplesClassificationTypes() {
        List<String> names = new ArrayList<String>();
        names.add("Underlines");
        names.add("Dotted Underlines");
        return names;
    }

    /**
     * @return the list of triples corresponding to <code>getTripleClassificationNames</code> for the given
     * node.
     */
    public List<List<Triple>> getTriplesLists(Node node) {
        List<List<Triple>> triplesList = new ArrayList<List<Triple>>();
        Graph graph = getGraph();
        triplesList.add(GraphUtils.getUnderlinedTriplesFromGraph(node, graph));
        triplesList.add(GraphUtils.getDottedUnderlinedTriplesFromGraph(node, graph));
        return triplesList;
    }
}



