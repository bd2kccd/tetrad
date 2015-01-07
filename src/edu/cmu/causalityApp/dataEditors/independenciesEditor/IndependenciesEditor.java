package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.event.SampleChangedEvent;
import edu.cmu.causalityApp.dataEditors.AbstractDatatableEditor;
import edu.cmu.causalityApp.dataEditors.HypotheticalManipulatedGraphIconMaker;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the independencies editor window.
 *
 * @author mattheweasterday
 */
public class IndependenciesEditor extends AbstractDatatableEditor {

    /**
     * The unique string id of this window.
     */
    public static final String MY_NAME = "Predictions & Results";

    private static final String COMPARE_INDEPENDENCIES = "Compare Independencies";

    private boolean IS_POPULATION_HIDDEN;
    private final HypotheticalManipulatedGraphIconMaker iconMaker;
    private IndependenciesTable independencies;

    /**
     * Constructor.
     */
    public IndependenciesEditor(CausalityLabModel model,
                                HypotheticalManipulatedGraphIconMaker iconMaker,
                                InternalFrameListener frameListener) {
        super(MY_NAME, model);
        this.iconMaker = iconMaker;
        addInternalFrameListener(frameListener);

        JToolBar toolbar = getToolbar();
        JButton compareBn = new JButton(COMPARE_INDEPENDENCIES);
        compareBn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CompareIndependenciesWizard wizard =
                        new CompareIndependenciesWizard(independencies);

                getDesktopPane().add(wizard);
                wizard.setLocation(new Point((int) getLocation().getX() + 15, (int) getLocation().getY() + 15));
                wizard.setVisible(true);
                wizard.pack();
            }
        });

        toolbar.add(Box.createHorizontalStrut(40));
        toolbar.add(compareBn);
    }

    /**
     * Specifies if the population is hidden or not, and then refreshes the
     * data table.
     */
    public void setHidden(boolean hidden) {
        IS_POPULATION_HIDDEN = hidden;
        showDatatableAgain();
    }

    /**
     * @return unique string id of this window.
     */
    public String getEditorName() {
        return MY_NAME;
    }

    // ----------- MODEL CHANGE LISTENER EVENTS -------------------

    /**
     * Detects that sample has changed. Refreshes the data table with the changes.
     */
    public void sampleChanged(SampleChangedEvent scEvent) {
        showDatatableAgain();
    }

    /**
     * Detects that finance has changed. Not applicable to this window.
     */
    public void financeChanged() {
    }

    /**
     * @return the toolbar label on the independencies table. Currently "Examine
     *         Independencies for:".
     */
    public String getToolbarLabel(CausalityLabModel model) {
        return "Examine independencies for :";
    }


    /**
     * Constructs the data table from the independencies table model.
     *
     * @return the independencies data table.
     */
    protected JComponent constructDatatable(CausalityLabModel minimodel, String expName) {
        JScrollPane scroll;

        int HEIGHT = (int) getContentPane().getSize().getHeight();
        int WIDTH = (int) getContentPane().getSize().getWidth();

        if (HEIGHT == 0) {
            HEIGHT = 300;
        }
        if (WIDTH == 0) {
            WIDTH = 400;
        }

        independencies = new IndependenciesTable(
                minimodel.getIndependenciesTableModel(expName, !IS_POPULATION_HIDDEN, true),
                expName, iconMaker, IS_POPULATION_HIDDEN, minimodel.getStudentGuessEnabled());

        scroll = new JScrollPane(independencies);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        HEIGHT -= 27; //scroll.getHorizontalScrollBar().getHeight();
        scroll.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        scroll.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        return scroll;
    }

}
