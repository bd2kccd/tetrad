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

import edu.cmu.tetrad.cluster.ClusterUtils;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the MIMBuild
 * algorithm.
 *
 * @author Ricardo Silva
 */
public class MimBuildRunner extends AbstractMimRunner implements GraphSource {
    static final long serialVersionUID = 23L;
    private DataSet dataSet;
    private Graph fullGraph;
    private ICovarianceMatrix covMatrix;

    //============================CONSTRUCTORS===========================//

    public MimBuildRunner(DataWrapper dataWrapper,
                          MeasurementModelWrapper mmWrapper,
                          MimBuildParams params) {
        super(dataWrapper, mmWrapper.getClusters(), params);
        this.dataSet = (DataSet) getData();
        setClusters(mmWrapper.getClusters());
        params.setClusters(mmWrapper.getClusters());
    }

    public MimBuildRunner(DataWrapper dataWrapper,
                          BuildPureClustersRunner mmWrapper,
                          MimBuildParams params) {
        super(dataWrapper, mmWrapper.getClusters(), params);
        this.dataSet = (DataSet) getData();
        setClusters(mmWrapper.getClusters());
        params.setClusters(mmWrapper.getClusters());
    }

    public MimBuildRunner(DataWrapper dataWrapper,
                          MeasurementModelWrapper mmWrapper,
                          MimBuildParams params,
                          KnowledgeBoxModel knowledgeBoxModel) {
        super(dataWrapper, mmWrapper.getClusters(), params);
        this.dataSet = (DataSet) getData();
        setClusters(mmWrapper.getClusters());
        params.setClusters(mmWrapper.getClusters());
        params.setKnowledge(knowledgeBoxModel.getKnowledge());
    }

    public MimBuildRunner(MeasurementModelWrapper mmWrapper,
                          DataWrapper dataWrapper,
                          MimBuildParams params) {
        super(mmWrapper, mmWrapper.getClusters(), params);
        this.dataSet = (DataSet) dataWrapper.getDataModelList().get(0);
        setClusters(mmWrapper.getClusters());
        params.setClusters(mmWrapper.getClusters());
    }

    public MimBuildRunner(MeasurementModelWrapper mmWrapper,
                          MimBuildParams params,
                          KnowledgeBoxModel knowledgeBoxModel) {
        super(mmWrapper, mmWrapper.getClusters(), params);
        this.dataSet = (DataSet) getData();
        setClusters(mmWrapper.getClusters());
        getParams().setClusters(mmWrapper.getClusters());
        params.setKnowledge(knowledgeBoxModel.getKnowledge());
    }

//    public MimBuildRunner(DataWrapper dataWrapper, MimBuildParams params) {
//        super(dataWrapper, params.getClusters(), params);
//        this.dataSet = (DataSet) getData();
//        setClusters(params.getClusters());
//    }

    public MimBuildRunner(DataWrapper dataWrapper, MimBuildParams params, KnowledgeBoxModel knowledgeBox) {
        super(dataWrapper, params.getClusters(), params);
        this.dataSet = (DataSet) getData();
        setClusters(params.getClusters());
        params.setKnowledge(knowledgeBox.getKnowledge());
    }

    public MimBuildRunner(BuildPureClustersRunner pureClustersRunner,
                          MimBuildParams params) {
        super(pureClustersRunner, params);

        if (getData() instanceof CovarianceMatrix) {
            CovarianceMatrix cov = (CovarianceMatrix) getData();
            this.dataSet = DataUtils.choleskySimulation(cov);
        }
        else {
            this.dataSet = (DataSet) getData();
        }

        setClusters(params.getClusters());
    }

    public MimBuildRunner(BuildPureClustersRunner bpcRunner, KnowledgeBoxModel knowledgeBox, MimBuildParams params) {
        super(bpcRunner, params);
        this.dataSet = (DataSet) getData();
        setClusters(params.getClusters());
        params.setKnowledge(knowledgeBox.getKnowledge());
    }

    public MimBuildRunner(PurifyRunner runner, MimBuildParams params) {
        super(runner, params);
        this.dataSet = (DataSet) getData();
        setClusters(params.getClusters());
    }

    public MimBuildRunner(PurifyRunner runner, KnowledgeBoxModel knowledgeBox, MimBuildParams params) {
        super(runner, params);
        this.dataSet = (DataSet) getData();
        setClusters(params.getClusters());
        params.setKnowledge(knowledgeBox.getKnowledge());
    }

    public MimBuildRunner(PurifyRunner runner, DataWrapper dataWrapper, MimBuildParams params) {
        super(runner, params);
        this.dataSet = (DataSet) dataWrapper.getSelectedDataModel();
        setClusters(params.getClusters());
    }

    public MimBuildRunner(PurifyRunner runner, DataWrapper dataWrapper, KnowledgeBoxModel knowledgeBox, MimBuildParams params) {
        super(runner, params);
        this.dataSet = (DataSet) dataWrapper.getSelectedDataModel();
        setClusters(params.getClusters());
        params.setKnowledge(knowledgeBox.getKnowledge());
    }


    public MimBuildRunner(MimBuildRunner runner, MimBuildParams params) {
        super(runner, params);
        this.dataSet = (DataSet) getData();
        setClusters(params.getClusters());
    }

    public MimBuildRunner(MimBuildRunner runner, KnowledgeBoxModel knowledgeBox, MimBuildParams params) {
        super(runner, params);
        this.dataSet = (DataSet) getData();
        setClusters(params.getClusters());
        params.setKnowledge(knowledgeBox.getKnowledge());
    }

    public ICovarianceMatrix getCovMatrix() {
        return this.covMatrix;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MimBuildRunner serializableInstance() {
        DataSet dataSet = DataUtils.discreteSerializableInstance();
        DataWrapper dataWrapper = new DataWrapper(dataSet);
        MeasurementModelWrapper mmWrapper = new MeasurementModelWrapper(dataWrapper, MimBuildParams.serializableInstance());
        return new MimBuildRunner(mmWrapper, DataWrapper.serializableInstance(),
                MimBuildParams.serializableInstance());
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() throws Exception {
        DataSet data = this.dataSet;

        Mimbuild3 mimbuild = new Mimbuild3();
        mimbuild.setAlpha(getParams().getAlpha());
        mimbuild.setKnowledge(getParams().getKnowledge());

        Clusters clusters = getParams().getClusters();

        List<List<Node>> partition = ClusterUtils.clustersToPartition(clusters, data.getVariables());

        System.out.println(partition);

        List<String> latentNames = new ArrayList<String>();

        for (int i = 0; i < clusters.getNumClusters(); i++) {
            latentNames.add(clusters.getClusterName(i));
        }

        CovarianceMatrix cov = new CovarianceMatrix(data);

        Graph structureGraph = mimbuild.search(partition, latentNames, cov);
        TetradLogger.getInstance().log("details", "Structure graph = " + structureGraph);
        TetradLogger.getInstance().log("details", "P = " + mimbuild.getpValue());
        GraphUtils.circleLayout(structureGraph, 200, 200, 150);
        GraphUtils.fruchtermanReingoldLayout(structureGraph);

        System.out.println("Latent covs = " + mimbuild.getLatentsCov());
        System.out.println("Latent covs = " + mimbuild.getCovMatrix());

        Graph fullGraph = mimbuild.getFullGraph();
        GraphUtils.circleLayout(fullGraph, 200, 200, 150);
        GraphUtils.fruchtermanReingoldLayout(fullGraph);

        setResultGraph(fullGraph);
        setFullGraph(fullGraph);
        setClusters(MimUtils.convertToClusters(structureGraph));

        setClusters(ClusterUtils.partitionToClusters(mimbuild.getClustering()));

        setStructureGraph(structureGraph);

        getParams().getMimIndTestParams().setLatentVarNames(new ArrayList<String>(latentNames));

        this.covMatrix = mimbuild.getCovMatrix();
    }

    private void setFullGraph(Graph fullGraph) {
        this.fullGraph = fullGraph;
    }

    //===========================PRIVATE METHODS==========================//

    public Graph getGraph() {
        return getResultGraph();
    }

    public SemPm getSemPm() {
        Graph graph = getResultGraph();
        return new SemPm(graph);
    }

    public Graph getFullGraph() {
        return fullGraph;
    }
}



