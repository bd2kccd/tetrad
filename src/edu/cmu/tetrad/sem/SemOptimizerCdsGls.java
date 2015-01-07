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

package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.RandomUtil;
import pal.math.*;

import java.util.List;

/**
 * Optimizes a SEM using the ConjugateDirectionSearch class in the PAL library.
 *
 * @author Ricardo Silva
 * @author Joseph Ramsey
 */
public class SemOptimizerCdsGls implements SemOptimizer {
    static final long serialVersionUID = 23L;

    /**
     * Absolute tolerance of function value.
     */
    private static final double FUNC_TOLERANCE = 1.0e-4;

    /**
     * Absolute tolerance of each parameter.
     */
    private static final double PARAM_TOLERANCE = 1.0e-3;

    //=========================CONSTRUCTORS============================//

    /**
     * Blank constructor.
     */
    public SemOptimizerCdsGls() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemOptimizerCdsGls serializableInstance() {
        return new SemOptimizerCdsGls();
    }

    //=========================PUBLIC METHODS==========================//

    /**
     * Optimizes the fitting function for the SEM.
     */
    public void optimize(SemIm semIm) {
        if (DataUtils.containsMissingValue(semIm.getSampleCovar())) {
            throw new IllegalArgumentException("Please remove or impute missing values.");
        }

//        new SemOptimizerEm().optimize(semIm);

        // Optimize the semIm. Note that the the covariance matrix of the
        // sample data is made available to the following PalFittingFunction.
        double min = 10000;
        SemIm _sem = null;
        int count = 0;
        boolean improved = false;

        while (++count <= 1 || !improved) {
            SemIm _sem2 = new SemIm(semIm);

            List<Parameter> freeParameters = _sem2.getFreeParameters();

            double[] p = new double[freeParameters.size()];

            for (int i = 0; i < freeParameters.size(); i++) {
                if (freeParameters.get(i).getType() == ParamType.VAR) {
                    p[i] = RandomUtil.getInstance().nextUniform(0, 1);
                } else {
                    p[i] = RandomUtil.getInstance().nextUniform(-1, 1);
                }
            }

            _sem2.setFreeParamValues(p);

            optimize2(_sem2);

            double chisq = _sem2.getChiSquare();

            if (chisq < min) {
                improved = true;
                min = chisq;
                _sem = _sem2;
            }
        }

        for (Parameter param : semIm.getFreeParameters()) {
            try {
                Node nodeA = param.getNodeA();
                Node nodeB = param.getNodeB();

                Node _nodeA = _sem.getVariableNode(nodeA.getName());
                Node _nodeB = _sem.getVariableNode(nodeB.getName());

                double value = _sem.getParamValue(_nodeA, _nodeB);
                semIm.setParamValue(param, value);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public void optimize2(SemIm semIm) {
        ConjugateGradientSearch search = new ConjugateGradientSearch();
        search.defaultStep = 10;
        search.findMinimum(fittingFunction(semIm), semIm.getFreeParamValues(), 3, 5);
    }

    public String toString() {
        return "Sem Optimizer PAL CDS";
    }

    private Function fittingFunction(SemIm sem) {
        return new Function(sem);
    }

    /**
     * Wraps the SEM maximum likelihood fitting function for purposes of being
     * evaluated using the PAL ConjugateDirection optimizer.
     *
     * @author Joseph Ramsey
     */
    static class Function implements MultivariateFunction {

        /**
         * The wrapped Sem.
         */
        private final SemIm sem;

        private List<Parameter> freeParameters;

        /**
         * Constructs a new PalFittingFunction for the given Sem.
         */
        public Function(SemIm sem) {
            this.sem = sem;
            this.freeParameters = sem.getFreeParameters();
        }

        /**
         * Computes the maximum likelihood function value for the given
         * parameter values as given by the optimizer. These values are mapped to
         * parameter values.
         */
        public double evaluate(final double[] parameters) {
            sem.setFreeParamValues(parameters);
            return sem.getFgls();
        }

        /**
         * Returns the number of arguments. Required by the MultivariateFunction
         * interface.
         */
        public int getNumArguments() {
            return this.sem.getNumFreeParams();
        }

        /**
         * Returns the lower bound of argument n. Required by the
         * MultivariateFunction interface.
         */
        public double getLowerBound(final int n) {
            Parameter param = freeParameters.get(n);
            return (param.getType() == ParamType.COEF ||
                    param.getType() == ParamType.COVAR) ? -10000.0 : 0.0;
        }

        /**
         * Returns the upper bound of argument n. Required by the
         * MultivariateFunction interface.
         */
        public double getUpperBound(final int n) {
            return 10000.0;
        }

        public OrthogonalHints getOrthogonalHints() {
            return null;
        }
    }
}








