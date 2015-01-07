package edu.cmu.causalityApp.dataEditors;


import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.event.ExperimentChangedEvent;
import edu.cmu.causality.event.HypothesisChangedEvent;
import edu.cmu.causality.event.ModelChangeListener;
import edu.cmu.causalityApp.graphEditors.CorrectGraphFocusListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;

/**
 * This is the superclass for the data table editors such as population editor,
 * sample editor and the independencies editor.
 *
 * @author mattheweasterday
 */
public abstract class AbstractDatatableEditor extends AbstractEditor implements ModelChangeListener, ItemListener {

    /**
     * The causality lab model.
     */
    protected final CausalityLabModel minimodel;

    private JComboBox combo;
    private JComboBox combo2;
    private String EXPERIMENT_TO_FOCUS_ON = null;
    private String TRUEGRAPH_TO_FOCUS_ON = null;
    private JComponent DATATABLE_PANEL;
    private final JToolBar TOOLBAR;
    private ExperimentalSetupFocusListener EXPERIMENT_FOCUS_LISTENER;
    private CorrectGraphFocusListener TRUEGRAPH_FOCUS_LISTENER;
    private boolean HIDDEN;

    /**
     * Constructor.
     */
    protected AbstractDatatableEditor(String name, CausalityLabModel model) {
        super(name);
        this.minimodel = model;
        minimodel.addModelChangeListener(this);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        TOOLBAR = makeToolBar();
        contentPane.add(TOOLBAR, BorderLayout.NORTH);

        showDatatable();
    }

    /**
     * Sets up the listener for the getModel experimental setup focus.
     */
    public void setExperimentalSetupFocusListener(ExperimentalSetupFocusListener listener) {
        EXPERIMENT_FOCUS_LISTENER = listener;
    }

    /**
     * Sets up the listener for the getModel true graph focus.
     */
    public void setCorrectGraphFocusListener(CorrectGraphFocusListener listener) {
        TRUEGRAPH_FOCUS_LISTENER = listener;
    }

    /**
     * Set if population is hidden, and refresh the data table.
     */
    public void setHidden(boolean hidden) {
        this.HIDDEN = hidden;
        showDatatableAgain();
    }

    /**
     * Signals a change in the items on the editor, and refreshes the view.
     */
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (!((ComboBoxName) e.getItem()).getExperimentName().equals(EXPERIMENT_TO_FOCUS_ON)
                    && isExperimentName(((ComboBoxName) e.getItem()).getExperimentName())) {
                EXPERIMENT_TO_FOCUS_ON = ((ComboBoxName) e.getItem()).getExperimentName();
                showDatatable();
                if (EXPERIMENT_FOCUS_LISTENER != null) {
                    EXPERIMENT_FOCUS_LISTENER.fireESViewChangedEvent(this, EXPERIMENT_TO_FOCUS_ON);

                }
            } else if (!((ComboBoxName) e.getItem()).getExperimentName().equals(TRUEGRAPH_TO_FOCUS_ON)
                    && isTrueGraphName(((ComboBoxName) e.getItem()).getExperimentName())) {
                TRUEGRAPH_TO_FOCUS_ON = ((ComboBoxName) e.getItem()).getExperimentName();
                showDatatable();
                if (TRUEGRAPH_FOCUS_LISTENER != null) {
                    TRUEGRAPH_FOCUS_LISTENER.fireCgViewChangedEvent(this, TRUEGRAPH_TO_FOCUS_ON);

                }
            }
        }

    }


    public void setIcon(boolean b) throws PropertyVetoException {
        super.setIcon(b);
        if (!b) {
            updateCombo();
            showDatatable();
        }
    }

    // ---------- VIEW CHANGE LISTENER EVENTS -------------------
    public void setExperimentalSetupFocus(String experimentalSetupName) {
        if (experimentalSetupName == null) {
            return;
        }
        if (!experimentalSetupName.equals(EXPERIMENT_TO_FOCUS_ON)) {
            combo.removeItemListener(this);
            EXPERIMENT_TO_FOCUS_ON = experimentalSetupName;
            updateCombo();
            combo.addItemListener(this);
        }
        showDatatable();
    }

    public void setCorrectGraphFocus(String trueGraphName) {
        if (trueGraphName == null) {
            return;
        }
//        if (!trueGraphName.equals(TRUEGRAPH_TO_FOCUS_ON)) {
//            combo2.removeItemListener(this);
//            TRUEGRAPH_TO_FOCUS_ON = trueGraphName;
//            updateCombo2();
//            combo2.addItemListener(this);
//        }
        showDatatable();
    }

    public void setHypotheticalGraphFocus() {
        combo.removeItemListener(this);
        updateCombo();
        combo.addItemListener(this);
        showDatatable();
    }
    //================END =========================================


    public void hypothesisChanged(HypothesisChangedEvent hcEvent) {
    }

    public void experimentChanged(ExperimentChangedEvent ecEvent) {
    }
    //================END =========================================


    protected abstract String getToolbarLabel(CausalityLabModel model);

    //============================================================
    protected abstract JComponent constructDatatable(CausalityLabModel model, String experimentName);

    protected JToolBar getToolbar() {
        return TOOLBAR;
    }

    protected void showDatatableAgain() {
        showDatatable();
    }

    void updateCombo() {
        int i;
        String expId;
        String[] ids = minimodel.getExperimentNames();


        combo.removeAllItems(); //does not cause an item event

        for (i = 0; i < ids.length; i++) {
            expId = ids[i];
            combo.addItem(new ComboBoxName(expId)); //may cause item event
        }
        selectCombo();
    }

    void selectCombo() {
        String expId;
        for (int i = 0; i < combo.getItemCount(); i++) {
            expId = ((ComboBoxName)(combo.getItemAt(i))).getExperimentName();
            if (expId.equals(EXPERIMENT_TO_FOCUS_ON)) {
                combo.setSelectedIndex(i);
            }
        }
    }

//    void updateCombo2() {
//        int i;
//        String trueGraphName;
//        String[] names = minimodel.getTrueModelNames();
//
//
//        combo2.removeAllItems(); //does not cause an item event
//
//        for (i = 0; i < names.length; i++) {
//            trueGraphName = names[i];
//            combo2.addItem(new ComboBoxName(trueGraphName)); //may cause item event
//        }
//        selectCombo2();
//    }

//    void selectCombo2() {
//        String trueGraphName;
//        for (int i = 0; i < combo2.getItemCount(); i++) {
//            trueGraphName = ((ComboBoxName)(combo2.getItemAt(i))).getExperimentName();
//            if (trueGraphName.equals(TRUEGRAPH_TO_FOCUS_ON)) {
//                combo2.setSelectedIndex(i);
//            }
//        }
//    }

    private void showDatatable() {
        if (DATATABLE_PANEL != null) {
            this.getContentPane().remove(DATATABLE_PANEL);
        }

        if (HIDDEN) {
            return;
        }

        DATATABLE_PANEL = constructDatatable(minimodel, EXPERIMENT_TO_FOCUS_ON);
        getContentPane().add(DATATABLE_PANEL, BorderLayout.CENTER);

        if (!this.isIcon()) {
            pack();
        }
    }


    private JToolBar makeToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        //toolBar.setRollover(true);
        toolBar.add(new JLabel(getToolbarLabel(minimodel)));

        combo = new JComboBox();
        updateCombo();
        toolBar.add(combo);
        combo.addItemListener(this);

//        combo2 = new JComboBox();
//        updateCombo2();
//        toolBar.add(combo2);
//        combo2.addItemListener(this);

        return toolBar;
    }

    private boolean isExperimentName(String exptName) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (((ComboBoxName)(combo.getItemAt(i))).getExperimentName().equals(exptName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTrueGraphName(String trueGraphName) {
        for (int i = 0; i < combo2.getItemCount(); i++) {
            if (((ComboBoxName)(combo2.getItemAt(i))).getExperimentName().equals(trueGraphName)) {
                return true;
            }
        }
        return false;
    }


}
