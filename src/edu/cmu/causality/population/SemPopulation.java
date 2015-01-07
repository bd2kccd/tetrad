package edu.cmu.causality.population;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradMatrix;

import java.util.Iterator;


/**
 * This is a wrapper for a correlation matrix. The first column are the variables
 * (as are the column headers for the rest of the columns). The rest is the
 * lower triangle of the correlation matrix.
 *
 * @author Matt Easterday
 * @author Joseph Ramsey
 */
public class SemPopulation {

    /**
     * The first column header is "", the other column headers are the variable names.
     *
     * @return a string array of the column headers.
     */
    public static String[] getColumnHeaders(SemIm correctSemIm, VariablesStudied studiedVariables) {
        return createHeaders(correctSemIm, studiedVariables);
    }


    /**
     * The first column header is "", the other column headers are the variable names.
     *
     * @return the column header at a particular column.
     */
    private static String getColumnHeader(int col, SemIm correctSemIm, VariablesStudied studiedVariables) {
        return getColumnHeaders(correctSemIm, studiedVariables)[col];
    }


    /**
     * row n >0, column 0 is name of a variable.
     *
     * @return the name of the variable at a given row.
     */
    private static String getFirstColumn(int row, VariablesStudied studiedVariables) {
        return (studiedVariables.getNamesOfStudiedVariables())[row];
    }

    /**
     * the row, column indices are variables, the cells of the table are how the variables covary.
     *
     * @return the covariance of the two variables in the SEM IM graph.
     */
    public static double getCovariance(SemIm semIm, String var1, String var2) throws IllegalArgumentException {
        int var1Index = -1, var2Index = -1;
        int i = 0;
        Node node;
        for (Iterator nodes = semIm.getVariableNodes().iterator(); nodes.hasNext(); i++) {
            node = (Node) nodes.next();
            if (node.getName().equals(var1)) {
                var1Index = i;
            }
            if (node.getName().equals(var2)) {
                var2Index = i;
            }
        }
        return semIm.getImplCovar().toArray()[var1Index][var2Index];
    }

    /**
     * the row, column indices are variables, the cells of the table are how the variables covary.
     *
     * @return the correlation of the two variables in the SEM IM graph.
     */
    private static double getCorrelation(SemIm semIm, String var1, String var2) throws IllegalArgumentException {

        // I assume this is supposed to return an element of the implied correlation matrix.
        // jdramsey 6/7/2013
        TetradMatrix covariance = semIm.getImplCovar();

        int x = semIm.getVariableNodes().indexOf(semIm.getVariableNode(var1));
        int y = semIm.getVariableNodes().indexOf(semIm.getVariableNode(var2));

        TetradMatrix corrMatrix = MatrixUtils.convertCovToCorr(covariance);
        return corrMatrix.get(x, y);
    }

    /**
     * number of rows is just number of observed variables.
     *
     * @return the number of observed variables.
     */
    public static int getNumRows(ExperimentalSetup studiedVariables) {

        if (studiedVariables == null) {
            return 0;
        }
        return studiedVariables.getNumVariablesStudied();
    }

    /**
     * For SEM models, returns the value in the population table at a given column and row.
     * For Bayes models see CausalityLabModel.getValueAt
     */
    public static Object getValueAt(int row, int col, SemIm correctSemIm, ExperimentalSetup experiment, VariablesStudied studiedVariables) {
        SemIm correctManipulatedSemIm = createCorrectManipulatedSemIm(correctSemIm, experiment);

        if (col == 0) {
            return getFirstColumn(row, studiedVariables);
        } else {
            if (col > row + 1) {
                return null;
            }
            return getCorrelation(
                    correctManipulatedSemIm,
                    getFirstColumn(row, studiedVariables),
                    getColumnHeader(col, correctManipulatedSemIm, studiedVariables));
        }
    }


    /**
     * Returns the measured variables among the studied variables.
     */
    private static String[] createHeaders(
            SemIm correctSemIm,
            VariablesStudied studiedVariables) {

        Node node;
        int j;
        Iterator nodes;
        String[] headers;
        NodeType type;

        headers = new String[getNumViewedColumns(studiedVariables) + 1];
        headers[0] = "";
        for (j = 1, nodes = correctSemIm.getSemPm().getGraph().getNodes().iterator(); nodes.hasNext(); ) {
            node = (Node) nodes.next();
            type = node.getNodeType();
            if (type == NodeType.MEASURED) {
                if (studiedVariables.isVariableStudied(node.getName())) {
                    headers[j++] = node.getName();
                }
            }
        }
        return headers;
    }

    /**
     * Create the correct manipulated graph SEM IM.
     */
    public static SemIm createCorrectManipulatedSemIm(SemIm correctSemIm, ExperimentalSetup experiment) {
        //AbstractManipulatedGraph corManipulatedGraph = new ManipulatedGraph(correctSemIm.getSemPm().getGraph(), experiment);
        return CorrectManipulatedGraphSemIm.createIm(correctSemIm, experiment);
    }

    //------------------PROTECTED AND PRIVATE METHODS ------------------------------------------

    /**
     * Returns the number of columns that will be in the final joint distribution - 1.
     * In otherwords, latent variables will be included if showLatents == true
     * and viewed variables will be included.
     *
     * @return the number of columns that will be in the final joint distribution - 1.
     */
    private static int getNumViewedColumns(VariablesStudied studiedVariables) {
        return studiedVariables.getNumVariablesStudied();
    }
}
