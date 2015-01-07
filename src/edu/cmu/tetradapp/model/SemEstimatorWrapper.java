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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.Unmarshallable;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a SemEstimator for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 */
public class SemEstimatorWrapper implements SessionModel, GraphSource, Unmarshallable {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private SemEstimator semEstimator;

    private boolean multipleResults = false;

    private List<SemEstimator> multipleResultList = new ArrayList<SemEstimator>();
    private SemEstimatorParams params;

    //==============================CONSTRUCTORS==========================//

    /**
     * Private constructor for serialization only. Problem is, for the real
     * constructors, I'd like to call the degrees of freedom check, which
     * pops up a dialog. This is irritating when running unit tests.
     * jdramsey 8/29/07
     */
    private SemEstimatorWrapper(DataSet dataSet, SemPm semPm, SemEstimatorParams params) {
        this.semEstimator = new SemEstimator(dataSet, semPm, getOptimizer());
        this.params = params;
    }

    public SemEstimatorWrapper(DataWrapper dataWrapper,
                               SemPmWrapper semPmWrapper, SemEstimatorParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException("Data wrapper must not be null.");
        }

        if (semPmWrapper == null) {
            throw new NullPointerException(
                    "OldSem PM Wrapper must not be null.");
        }

        DataModel dataModel = dataWrapper.getDataModelList();

        this.params = params;

        if (dataModel instanceof DataModelList) {
            multipleResults = true;

            for (DataModel model : (DataModelList) dataModel) {
                if (model instanceof DataSet) {
                    DataSet dataSet = new ColtDataSet((ColtDataSet) model);
                    SemPm semPm = semPmWrapper.getSemPm();
                    SemEstimator estimator = new SemEstimator(dataSet, semPm, getOptimizer());
                    if (!degreesOfFreedomCheck(semPm)) return;
                    estimator.estimate();

                    getMultipleResultList().add(estimator);
                } else if (model instanceof ICovarianceMatrix) {
                    ICovarianceMatrix covMatrix = new CovarianceMatrix((ICovarianceMatrix) model);
                    SemPm semPm = semPmWrapper.getSemPm();
                    SemEstimator estimator = new SemEstimator(covMatrix, semPm, getOptimizer());
                    if (!degreesOfFreedomCheck(semPm)) return;
                    estimator.estimate();

                    getMultipleResultList().add(estimator);
                } else {
                    throw new IllegalArgumentException("Data must consist of continuous data sets or covariance matrices.");
                }
            }

            this.semEstimator = getMultipleResultList().get(0);
//        }
//        else if (dataModel instanceof DataSet) {
//            //            checkVarNameEquality(dataModel, semPmWrapper.getSemIm());
//            DataSet dataSet =
//                    (DataSet) dataWrapper.getSelectedDataModel();
//            SemPm semPm = semPmWrapper.getSemPm();
//            this.semEstimator = new SemEstimator(dataSet, semPm);
//            if (!degreesOfFreedomCheck(semPm)) return;
//            this.semEstimator.estimate();
//            getMultipleResultList().add(this.semEstimator);
//        } else if (dataModel instanceof ICovarianceMatrix) {
//            //            checkVarNameEquality(dataModel, semPmWrapper.getSemIm());
//            ICovarianceMatrix covMatrix = (ICovarianceMatrix) dataModel;
//            SemPm semPm = semPmWrapper.getSemPm();
//            this.semEstimator = new SemEstimator(covMatrix, semPm);
//            if (!degreesOfFreedomCheck(semPm)) return;
//            this.semEstimator.estimate();
//            getMultipleResultList().add(this.semEstimator);
        } else {
            throw new IllegalArgumentException("Data must consist of continuous data sets or covariance matrices.");
        }

        log();
    }

    public SemEstimatorWrapper(DataWrapper dataWrapper,
                               SemImWrapper semImWrapper, SemEstimatorParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (semImWrapper == null) {
            throw new NullPointerException();
        }

        DataSet dataSet =
                (DataSet) dataWrapper.getSelectedDataModel();
        SemPm semPm = semImWrapper.getSemIm().getSemPm();

        this.semEstimator = new SemEstimator(dataSet, semPm, getOptimizer());
        if (!degreesOfFreedomCheck(semPm)) return;
        this.semEstimator.estimate();

        this.params = params;

        log();
    }

    private boolean degreesOfFreedomCheck(SemPm semPm) {
        if (semPm.getDof() < 1) {
            int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
                    "This model has nonpositive degrees of freedom (DOF = " +
                            semPm.getDof() + "). " +
                            "\nEstimation will be uninformative. Are you sure you want to proceed?",
                    "Please confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ret != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        return true;
    }

    public SemEstimatorWrapper(DataWrapper dataWrapper,
                               SemPmWrapper semPmWrapper,
                               SemImWrapper semImWrapper,
                               SemEstimatorParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (semPmWrapper == null) {
            throw new NullPointerException();
        }

        if (semImWrapper == null) {
            throw new NullPointerException();
        }

        DataSet dataSet =
                (DataSet) dataWrapper.getSelectedDataModel();
        SemPm semPm = semPmWrapper.getSemPm();
        SemIm semIm = semImWrapper.getSemIm();

        this.semEstimator = new SemEstimator(dataSet, semPm, getOptimizer());
        if (!degreesOfFreedomCheck(semPm)) return;
        this.semEstimator.setTrueSemIm(semIm);
        this.semEstimator.estimate();

        this.params = params;

        log();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemEstimatorWrapper serializableInstance() {
        DataSet dataSet = DataUtils.continuousSerializableInstance();
        return new SemEstimatorWrapper(dataSet, SemPm.serializableInstance(), SemEstimatorParams.serializableInstance());
    }

    //============================PUBLIC METHODS=========================//

    public SemEstimator getSemEstimator() {
        return this.semEstimator;
    }

    public void setSemEstimator(SemEstimator semEstimator) {
        this.semEstimator = semEstimator;
    }

    public SemIm getEstimatedSemIm() {
        return semEstimator.getEstimatedSem();
    }

    public String getSemOptimizerType() {
        return getParams().getSemOptimizerType();
    }

    public void setSemOptimizerType(String type) {
        getParams().setSemOptimizerType(type);
    }

    public Graph getGraph() {
        return semEstimator.getEstimatedSem().getSemPm().getGraph();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //=============================== Private methods =======================//

    private void log() {
        TetradLogger.getInstance().log("info", "EM-Estimated Bayes IM:");
        TetradLogger.getInstance().log("im", "" + getEstimatedSemIm());
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

//        if (semEstimator == null) {
//            throw new NullPointerException();
//        }
    }

    /**
     * @serial
     */
    public boolean isMultipleResults() {
        return multipleResults;
    }

    /**
     * @serial
     */
    public List<SemEstimator> getMultipleResultList() {
        return multipleResultList;
    }

    public void setMultipleResultList(List<SemEstimator> multipleResultList) {
        this.multipleResultList = multipleResultList;
    }

    public SemEstimatorParams getParams() {
        return params;
    }

    private SemOptimizer getOptimizer() {
        SemOptimizer optimizer;
        String type = getParams().getSemOptimizerType();

        if ("Regression".equals(type)) {
            optimizer = new SemOptimizerRegression();
        } else if ("EM".equals(type)) {
            optimizer = new SemOptimizerEm();
        } else if ("CDS".equals(type)) {
            optimizer = new SemOptimizerCdsGls();
        } else if ("Random Search".equals(type)) {
            optimizer = new SemOptimizerScattershot();
        } else if ("RICF".equals(type)) {
            optimizer = new SemOptimizerRicf();
        } else if ("Powell".equals(type)) {
            optimizer = new SemOptimizerNrPowell();
        } else if ("Uncmin".equals(type)) {
            optimizer = new SemOptimizerUncmin();
        } else {
            optimizer = null;
//            throw new IllegalArgumentException("Unexpected optimizer " +
//                    "type: " + type);
        }

        return optimizer;
    }
}



