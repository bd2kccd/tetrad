package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.IndependenciesTableModel;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.independencies.GuessedIndependencies;
import edu.cmu.causality.independencies.Independencies;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

import java.awt.*;


/**
 * Command for setting student guesses in the independencies window
 *
 * @author mattheweasterday
 */
public class SetIndependenceCommand extends AbstractCommand {

    /**
     * Unique name for this moves.
     */
    private static final String MY_NAME = "setIndependenceCommand";

    /**
     * The xml attributes for this moves.
     */
    private static final String EXPERIMENT = "experimentalSetup";
    private static final String COLUMN = "column";
    private static final String ROWi = "row";
    private static final String SET_TO = "setTo";
    private static final String DEPENDENT = "dependent";
    private static final String INDEPENDENT = "independent";

    private Container editor;
    private String experimentName;
    private String columnName;
    private int row;
    private boolean isIndependent;
    private Boolean wasIndependent;


    /**
     * Constructor.
     *
     * @param independencies table holding the independencies
     * @param isIndependent  true if independent, false if dependent
     * @param row            the index corresponding to the variables that are either
     *                       independent or dependent
     * @param column         the column index of the guesses for a given hypothetesis or
     *                       sample
     */
    public SetIndependenceCommand(
            IndependenciesTable independencies,
            boolean isIndependent,
            int row,
            int column) {

        setEditor(independencies.getParent());
        IndependenciesTableModel im;
        im = (IndependenciesTableModel) independencies.getModel();

        setExperimentName(im.getExperimentName());
        setColumnName(im.getColumnName(column));
        setIndependent(isIndependent);
        setRow(row);
    }

    /**
     * Constructor for parser.
     */
    public SetIndependenceCommand(
            Container editor,
            boolean isIndependent,
            int row,
            String colName,
            String exptName) {

        setEditor(editor);

        setExperimentName(exptName);
        setColumnName(colName);
        setIndependent(isIndependent);
        setRow(row);
    }

    /**
     * Executes moves by setting two variables to be dependent or independent.
     */
    public void justDoIt() {
        CausalityLabModel m = CausalityLabModel.getModel();
        ExperimentalSetup es = m.getEmptyExperimentalSetup(getExperimentName());
        GuessedIndependencies gi = m.getGuessedIndependenciesForColumn(getExperimentName(), getColumnName());

        setWasIndependent(Independencies.isIndependent(getRow(), gi, es));
        Independencies.setIndependent(isIndependent(), getRow(), gi, es);

        getEditor().repaint();
    }


    /**
     * Undoes the moves by setting the two variables to the relation they
     * were before the moves: independent/dependent/null.
     */
    public void undo() {
        CausalityLabModel m = CausalityLabModel.getModel();
        ExperimentalSetup es = m.getEmptyExperimentalSetup(getExperimentName());
        GuessedIndependencies gi;

        gi = m.getGuessedIndependenciesForColumn(getExperimentName(), getColumnName());
        Independencies.setIndependent(getWasIndependent(), getRow(), gi, es);

        getEditor().repaint();
    }

    /**
     * String representation of the moves used for diplay in moves history.
     *
     * @return "Set Independence".
     */
    public String toString() {
        return "Set Independence";
    }

    /**
     * Name of moves.
     *
     * @return "setIndependenceCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * @return attributes in the xml representatino of the moves.
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[4];

        att[0] = new Attribute(EXPERIMENT, getExperimentName());
        att[1] = new Attribute(COLUMN, getColumnName());
        att[2] = new Attribute(ROWi, Integer.toString(getRow()));
        String val = isIndependent() ? INDEPENDENT : DEPENDENT;
        att[3] = new Attribute(SET_TO, val);

        return att;
    }

    private Container getEditor() {
        return editor;
    }

    private void setEditor(Container editor) {
        this.editor = editor;
    }

    private String getExperimentName() {
        return experimentName;
    }

    private void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    private String getColumnName() {
        return columnName;
    }

    private void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    private int getRow() {
        return row;
    }

    private void setRow(int row) {
        this.row = row;
    }

    private boolean isIndependent() {
        return isIndependent;
    }

    private void setIndependent(boolean independent) {
        isIndependent = independent;
    }

    private Boolean getWasIndependent() {
        return wasIndependent;
    }

    private void setWasIndependent(Boolean wasIndependent) {
        this.wasIndependent = wasIndependent;
    }
}
