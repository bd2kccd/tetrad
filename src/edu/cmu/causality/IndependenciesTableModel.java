package edu.cmu.causality;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.independencies.IndependenceResult;
import edu.cmu.causality.independencies.Independencies;
import edu.cmu.causality.sample.BayesSample;
import edu.cmu.causality.sample.Sample;
import edu.cmu.causality.sample.SemSample;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Superclass for both the calculated Independencies Table Model and the
 * students' guessed independencies table model
 */
public class IndependenciesTableModel extends AbstractTableModel {
    final public static int VARIABLE_NAME_COLUMN = 0;
    final public static int POPULATION_COLUMN = 1;
    final public static int SAMPLE_COLUMN = 2;
    final public static int HYPOTHESIS_COLUMN = 3;
    final public static int HYP_GUESS_COLUMN = 4;
    final public static int SAMPLE_GUESS_COLUMN = 5;

    private final String experimentName;
    private final boolean showPopulation;
    private final boolean showSample;
    private CausalityLabModel causalityLabModel;

    /**
     * Constructor.
     */
    public IndependenciesTableModel(CausalityLabModel causalityLabModel, String experimentName, boolean showPopulation,
                                    boolean showSample) {
        this.causalityLabModel = causalityLabModel;
        this.experimentName = experimentName;
        this.showPopulation = showPopulation;
        this.showSample = showSample;
    }

    /**
     * @return the experimental setup name.
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * @return if the cell with given row and column index is editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        int j, targetColumn = columnIndex;

        if (targetColumn-- == 0) {
            return false;
        }
        if (targetColumn-- == 0) {
            return false;
        }
        if (targetColumn-- == 0) {
            return false;
        }
        if (showPopulation) {
            if (targetColumn-- == 0) {
                return false;
            }
        }

        if (showSample) {
            int[] sampleIds = causalityLabModel.getSampleIds(experimentName);
            for (j = 0; j < sampleIds.length; j++) {
                if (targetColumn-- == 0) {
                    return false;
                }
                if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                    return true;
                }
            }
        }

        String[] hypNames = causalityLabModel.getHypotheticalGraphNames();
        for (j = 0; j < hypNames.length; j++) {
            if (targetColumn-- == 0) {
                return false;
            }
            if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the independence column type of a given column.
     */
    public int getIndependenceColumnType(int col) {
        int j, targetColumn = col;

        if (targetColumn-- == 0) {
            return VARIABLE_NAME_COLUMN;
        }
        if (targetColumn-- == 0) {
            return VARIABLE_NAME_COLUMN;
        }
        if (targetColumn-- == 0) {
            return VARIABLE_NAME_COLUMN;
        }
        if (showPopulation && (targetColumn-- == 0)) {
            return POPULATION_COLUMN;
        }
        if (showSample) {
            int[] sampleIds = causalityLabModel.getSampleIds(experimentName);
            for (j = 0; j < sampleIds.length; j++) {
                if (targetColumn-- == 0) {
                    return SAMPLE_COLUMN;
                }
                if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                    return SAMPLE_GUESS_COLUMN;
                }
            }
        }

        String[] hypNames = causalityLabModel.getHypotheticalGraphNames();
        for (j = 0; j < hypNames.length; j++) {
            if (targetColumn-- == 0) {
                return HYPOTHESIS_COLUMN;
            }
            if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                return HYP_GUESS_COLUMN;
            }
        }
        return -1;
    }

    /**
     * @return the name of the given column.
     */
    public String getColumnName(int col) {
        int j, targetColumn = col;

        if (targetColumn-- == 0) {
            return "Variable 1";
        }
        if (targetColumn-- == 0) {
            return "Variable 2";
        }
        if (targetColumn-- == 0) {
            return "Conditioning set";
        }
        if (showPopulation) {
            if (targetColumn-- == 0) {
                return "Population";
            }
        }

        if (showSample) {
            int[] sampleIds = causalityLabModel.getSampleIds(experimentName);
            for (j = 0; j < sampleIds.length; j++) {
                if (targetColumn-- == 0) {
                    return causalityLabModel.getSampleSeed(sampleIds[j]).getName();
                }
                if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                    return causalityLabModel.getSampleSeed(sampleIds[j]).getName();
                }
            }
        }

        String[] hypNames = causalityLabModel.getHypotheticalGraphNames();
        for (j = 0; j < hypNames.length; j++) {
            if (targetColumn-- == 0) {
                return hypNames[j];
            }
            if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                return hypNames[j];
            }
        }
        return null;
    }

    /**
     * @return the column index of the column of the given name.
     */
    public int getColumnIndexGivenName(String name) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (getColumnName(i).equals(name))
                return i;
        }
        return -1;
    }

    /**
     * @return number of rows.
     */
    public int getRowCount() {
        ExperimentalSetup vars = causalityLabModel.getExperiment(experimentName);
        if (vars == null) {
            return 0;
        }
        return Independencies.getNumRows(vars);
    }

    /**
     * @return number of columns.
     */
    public int getColumnCount() {
        int cols = 3;
        if (showPopulation) {
            cols++;
        }
        if (showSample) {
            int numSamples = causalityLabModel.getSampleIds(experimentName).length;
            cols += (causalityLabModel.isStudentGuessesEnabled()) ? numSamples * 2 : numSamples;
        }
        int numHyps = causalityLabModel.getNumHypotheticalGraphs();
        cols += causalityLabModel.isStudentGuessesEnabled() ? numHyps * 2 : numHyps;

        return cols;
    }

    /**
     * @return the value at the given row and column.
     */
    public Object getValueAt(int row, int column) {
        int i;
        int targetColumn = column;

        // variable 1 column:
        if (targetColumn-- == 0) {
            // System.out.println("causality labMode:" + experimentName);
            return Independencies.getStringCombination(row,
                    causalityLabModel.getExperiment(experimentName))[0];
        }

        if (targetColumn-- == 0) {
            return Independencies.getStringCombination(row,
                    causalityLabModel.getExperiment(experimentName))[1];
        }

        if (targetColumn-- == 0) {
            StringBuilder result = new StringBuilder();
            String[] combo = Independencies.getStringCombination(row,
                    causalityLabModel.getExperiment(experimentName));

            if (combo.length > 2) {
                result.append(combo[2]);
                for (i = 3; i < combo.length; i++) {
                    result.append(", ").append(combo[i]);
                }
            }
            return result.toString();
        }

        if (showPopulation) {
            if (targetColumn-- == 0) {
                if (causalityLabModel.getExperiment(experimentName) == null) {
                    return null;
                }
                return Independencies.isIndependent(row,
                        causalityLabModel.getCorrectManipulatedGraph(experimentName),
                        causalityLabModel.getExperiment(experimentName));
            }
        }

        // SAMPLE INDEPENDENCIES & GUESSES
        SampleSeed seed;
        Sample sample;
        if (showSample) {
            int[] sampleIds = causalityLabModel.getSampleIds(experimentName);
            for (int sampleId : sampleIds) {
                String sampleName = causalityLabModel
                        .getSampleName(sampleId);

                // return the sample independencies
                if (targetColumn-- == 0) {
                    seed = causalityLabModel.getSampleSeed(sampleId);
                    if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
                        sample = new BayesSample(CausalityLabModel.getModel().getCorrectBayesIm(),
                                causalityLabModel.getExperiment(seed.getExpName()), seed
                                .getSampleSize(), seed.getSeed());
                    } else if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
                        sample = new SemSample(CausalityLabModel.getModel().getCorrectSemIm(), causalityLabModel.getExperiment(seed
                                .getExpName()), seed.getSampleSize(), seed
                                .getSeed());
                    } else {
                        throw new NullPointerException();
                    }

                    if (hasStudentGuessForRow(row, sampleName)) {
                        return null;
                    } else {
                        return Independencies.isSampleIndependent(row,
                                sample, causalityLabModel.getExperiment(experimentName));

                    }
                }
                if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                    return Independencies.isIndependent(row,
                            causalityLabModel.getGuessedIndependenceForSample(experimentName,
                                    sampleName),
                            causalityLabModel.getExperiment(experimentName));
                }
            }
        }

        // HYPOTHETICAL MANIPULATED INDPENDENCIES AND GUESSES
        String[] hypNames = causalityLabModel.getHypotheticalGraphNames();
        for (i = 0; i < hypNames.length; i++) {
            if (targetColumn-- == 0) {

                /*
                      * To send the correct data for the checking of the student
                      * guesses If student hasn't entered a value, null value
                      * will be returned to render a question mark
                      */
                if ((causalityLabModel.isStudentGuessesEnabled())
                        && Independencies.isIndependent(row,
                        causalityLabModel.getGuessedIndependenceForHypothesis(
                                experimentName, hypNames[i]),
                        causalityLabModel.getExperiment(experimentName)) == null)
                    return null;

                return Independencies.isIndependent(row,
                        causalityLabModel.getHypotheticalManipulatedGraph(
                                experimentName, hypNames[i]),
                        causalityLabModel.getExperiment(experimentName));
            }
            if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                return
                        Independencies.isIndependent(row,
                                causalityLabModel.getGuessedIndependenceForHypothesis(
                                        experimentName, hypNames[i]),
                                causalityLabModel.getExperiment(experimentName));
            }
        }
        return null;
    }

    /*
       * Returns true if there is a student guess for the given row and sample
       * or false if not (if student guesses are enabled).
       */
    private boolean hasStudentGuessForRow(int row, String sampleName) {
        return (causalityLabModel.isStudentGuessesEnabled())
                && (Independencies.isIndependent(row,
                causalityLabModel.getGuessedIndependenceForSample(experimentName,
                        sampleName), causalityLabModel.getExperiment(experimentName)) == null);

    }

    /**
     * Sets the independence at the cell with the given row and column
     * index.
     */
    public void setIndependence(Boolean isIndependent, int row, int column) {
        int j, targetColumn = column - 3;
        IllegalArgumentException e = new IllegalArgumentException(
                "not a valid column for guesses");

        if (column < 0) {
            throw e;
        }
        if (showPopulation && (targetColumn-- == 0)) {
            throw e;
        }
        if (showSample) {
            int[] sampleIds = causalityLabModel.getSampleIds(experimentName);
            for (j = 0; j < sampleIds.length; j++) {
                String sampleName = causalityLabModel
                        .getSampleName(sampleIds[j]);
                if (targetColumn-- == 0) {
                    throw e;
                }
                if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                    Independencies.setIndependent(isIndependent, row,
                            causalityLabModel.getGuessedIndependenceForSample(experimentName,
                                    sampleName),
                            causalityLabModel.getExperiment(experimentName));
                }
            }
        }

        String[] hypNames = causalityLabModel.getHypotheticalGraphNames();
        for (j = 0; j < hypNames.length; j++) {
            if (targetColumn-- == 0) {
                throw e;
            }
            if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                Independencies
                        .setIndependent(isIndependent, row,
                                causalityLabModel.getGuessedIndependenceForHypothesis(
                                        experimentName, hypNames[j]),
                                causalityLabModel.getExperiment(experimentName));
            }
        }
    }

    /**
     * @return the hypotheical graph name for a given column.
     */
    public String getHypotheticalGraphGraphNameForColumn(int col) {
        int j, targetColumn = col - 3;
        IllegalArgumentException e = new IllegalArgumentException(
                "not a hypothesis column");

        if (col < 0) {
            throw e;
        }
        if (showPopulation && (targetColumn-- == 0)) {
            throw e;
        }
        if (showSample) {
            int[] sampleIds = causalityLabModel.getSampleIds(experimentName);
            for (j = 0; j < sampleIds.length; j++) {
                if (targetColumn-- == 0) {
                    throw e;
                }
                if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                    throw e;
                }
            }
        }

        String[] hypNames = causalityLabModel.getHypotheticalGraphNames();
        for (j = 0; j < hypNames.length; j++) {
            if (targetColumn-- == 0) {
                return hypNames[j];
            }
            if (causalityLabModel.isStudentGuessesEnabled() && (targetColumn-- == 0)) {
                return hypNames[j];
            }
        }
        throw e;
    }

    /**
     * @return the list of row indices with different independencies.
     */
    public Vector<Integer> getRowIndexWithIndepDifferences(Vector columnNames) {
        int[] columns = new int[columnNames.size()];
        Vector<Integer> rows = new Vector<Integer>();

        for (int i = 0; i < columnNames.size(); i++) {
            columns[i] = getColumnIndexGivenName((String) columnNames.get(i));
            if (columns[i] == -1)
                throw new IllegalArgumentException(
                        "No such column name to compare independencies by!");
        }

        for (int row = 0; row < getRowCount(); row++) {
            int numTrue = 0;

            for (int column : columns) {
                Object valueAt = getValueAt(row, column);

                boolean independent;

                if (valueAt instanceof  Boolean) {
                    independent = (Boolean) valueAt;
                }
                else if (valueAt instanceof IndependenceResult) {
                    independent = ((IndependenceResult) valueAt).isIndependent();
                }
                else {
                    throw new IllegalStateException();
                }

                if (independent) numTrue++;
            }

            if (numTrue > 0 && numTrue < columns.length) {
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * @return the number of hypothetical graphs.
     */
    public int getNumOfHypGraph() {
        return causalityLabModel.getNumHypotheticalGraphs();
    }
}
