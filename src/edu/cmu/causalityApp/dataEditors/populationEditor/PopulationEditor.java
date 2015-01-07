package edu.cmu.causalityApp.dataEditors.populationEditor;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.PopulationTableModel;
import edu.cmu.causality.event.SampleChangedEvent;
import edu.cmu.causalityApp.dataEditors.AbstractDatatableEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;

/**
 * This class describes the BayesPopulation Editor window.
 *
 * @author mattheweasterday
 */
public class PopulationEditor extends AbstractDatatableEditor {

    /**
     * Unique string id of this window.
     */
    public static final String MY_NAME = "Population";

    /**
     * Constructor.
     */
    public PopulationEditor(CausalityLabModel model, InternalFrameListener parent) {
        super(MY_NAME, model);
        addInternalFrameListener(parent);
    }

    /**
     * Constructor.
     */
    public PopulationEditor(CausalityLabModel minimodel) {
        super(MY_NAME, minimodel);
    }

    /**
     * @return the label for the toolbar. Currently "Examine population correlation
     *         matrix for :" or "Examine population for :".
     */
    public String getToolbarLabel(CausalityLabModel model) {
        if (model.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            return "Examine population correlation matrix for :";
        }
        return "Examine population for :";
    }

    /**
     * @return the unique string id of the editor.
     */
    public String getEditorName() {
        return MY_NAME;
    }

    //================ModelChangeListener methods ================

    /**
     * Detects that sample has changed. Not applicable to this window.
     */
    public void sampleChanged(SampleChangedEvent scEvent) {
    }

    /**
     * Detects that finance has changed. Not applicable to this window.
     */
    public void financeChanged() {
    }

    //================END ModelChangeListener methods ================

    /**
     * Constructs the data table for population.
     */
    protected JComponent constructDatatable(CausalityLabModel minimodel, String experimentId) {
        JScrollPane scroll;
        JTable frequencies;

        PopulationTableModel popTable = minimodel.getPopulationTableModel(experimentId);

        if (minimodel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            frequencies = new PopulationTable(popTable);
        } else if (minimodel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            frequencies = new PopulationTableSem(popTable);
        } else {
            System.err.println(this.getClass() + " shouldn't get here");
            frequencies = null;
        }

        scroll = new JScrollPane(frequencies);
/*
        scroll.setPreferredSize(new Dimension(
                (int) scroll.getPreferredSize().getWidth(),
                (int) Math.min(300, scroll.getPreferredSize().getHeight())));
        scroll.setMaximumSize(new Dimension(
                (int) scroll.getPreferredSize().getWidth(),
                (int) (
                frequencies.getPreferredSize().getHeight() +
                frequencies.getTableHeader().getPreferredSize().getHeight())));
 */
        return scroll;
    }


}
