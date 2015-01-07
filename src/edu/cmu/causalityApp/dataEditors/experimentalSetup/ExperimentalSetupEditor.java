package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.DeleteExperimentalSetupCommand;
import edu.cmu.causality.event.*;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causalityApp.component.VarView;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.causalityApp.dataEditors.ExperimentalSetupFocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class describes the Experimental Setup window containing the tabs to all
 * the experimental setups.
 *
 * @author Matthew Easterday
 */
public class ExperimentalSetupEditor extends AbstractEditor implements ModelChangeListener {

    /**
     * Unique string id to this editor window.
     */
    public static final String MY_NAME = "Experimental Setup";

    //private CausalityLabPanel parent;
    private final JDesktopPane desktop;
    private final CausalityLabModel model;
    private final JTabbedPane mainPane;
    private final ExperimentalSetupFocusListener focusListener;


    /*
     * Holds the  experimental setup views
     */
    private final List<ExperimentalSetupView> views = new ArrayList<ExperimentalSetupView>();
    private int deletedSetups = 0;
    private final JButton remButton;


    /**
     * Constructor.
     */
    public ExperimentalSetupEditor(final CausalityLabModel model,
                                   InternalFrameListener frameListener,
                                   ExperimentalSetupFocusListener focusListener,
                                   JDesktopPane parent) {
        super(MY_NAME);

        model.addModelChangeListener(this);
        //parent.addESViewChangeListener (this);
        this.focusListener = focusListener;
        this.desktop = parent;
        this.model = model;
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton newButton = new JButton("New");
        remButton = new JButton("Remove");


        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(newButton);
        toolbar.add(remButton);

        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addExperimentalSetup();
            }
        });

        mainPane = new JTabbedPane(JTabbedPane.RIGHT);

        remButton.setEnabled(false);
        remButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DeleteExperimentalSetupCommand cmd =
                        new DeleteExperimentalSetupCommand(getCurrentlySelectedExperimentalSetupName());
                cmd.doIt();
            }
        });

        String[] names = model.getExperimentNames();

        for (String name : names) {
            ExperimentalSetupView view = new ExperimentalSetupView(model, parent, name);
            view.setName(name);
            views.add(view);
            mainPane.add(view, name);
        }

        mainPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                tabChanged();
            }
        });

        getContentPane().add(mainPane, BorderLayout.CENTER);
        getContentPane().add(toolbar, BorderLayout.NORTH);

        addInternalFrameListener(frameListener);
    }

    /**
     * Performs these actions when a hypothesis has changed. Not applicable to this
     * editor.
     */
    public void hypothesisChanged(HypothesisChangedEvent hcEvent) {
    }

    /**
     * When an experimental setup has been changed (edited), refreshes the view
     * in the editor.
     */
    public void experimentChanged(ExperimentChangedEvent ecEvent) {
        deleteTabs();
        addTabs();
    }

    /**
     * Trigger this change to all the sample change listeners. Not applicable
     * to this editor.
     */
    public void sampleChanged(SampleChangedEvent scEvent) {
    }

    /**
     * Trigger this change to all the finance change listeners. Not applicable
     * to this editor.
     */
    public void financeChanged() {
    }

    /**
     * @return the unique string id of this editor.
     */
    public String getEditorName() {
        return MY_NAME;
    }

    /**
     * Set the selected experimental setup view as the active one and trigger
     * this change to all the experimental setup change listener.
     */
    public void setExperimentalSetupFocus(Object source, String experimentalSetupName) {
        if (experimentalSetupName == null) return;
        ExperimentalSetupView esview = getExperimentalSetupView(experimentalSetupName);
        mainPane.setSelectedComponent(esview);
    }

    private ExperimentalSetupView getExperimentalSetupView(String experimentalSetupId) {
        ExperimentalSetupView esview;
        // uses the getRealExperimentalSetup function instead of the old getExperimentalSetupCopy
        ExperimentalSetup studiedVariables = model.getRealExperimentalSetup(experimentalSetupId);

        for (ExperimentalSetupView view : views) {
            esview = view;
            if (experimentalSetupId.equals(esview.getExpName())) {

                List tempList = esview.getView().getList();

                for (Object aTempList : tempList) {
                    VarView tempView = (VarView) aTempList;
                    String name = tempView.getName();

                    // brings the mean and std dev values from the model to the view for display.
                    // the flag is set to start to display the values.
                    // flag is defaulted to false in the VarView class to block the mean
                    // and std dev from being displayed in the SetVariablesPanel.

                    tempView.setMean(studiedVariables.getVariable(name).getMean());
                    tempView.setStdDev(studiedVariables.getVariable(name).getStandardDeviation());
                    tempView.setFlag(true);
                }
                return esview;
            }
        }
        throw new IllegalArgumentException("This is not a valid experimental setup id");
    }

    private void tabChanged() {
        if (mainPane.getSelectedIndex() >= 0)
            focusListener.fireESViewChangedEvent(this, ((ExperimentalSetupView) mainPane.getSelectedComponent()).getExpName());
        else
            focusListener.fireESViewChangedEvent(this, null);
    }


    private void addExperimentalSetup() {
        String newName = (String) JOptionPane.showInternalInputDialog(getDesktopPane(),
                "Enter a name:", "Add Exp. Setup", JOptionPane.PLAIN_MESSAGE, null, null,
                "Exp-Setup " + (mainPane.getTabCount() + deletedSetups + 1));
        if (newName != null) {
            makeWizard(newName);
        }
    }

    /**
     * Gets the id for the currently selected experimental setup
     * Added so that moves object can figure out which experimental setup is selected to delete from model
     *
     * @return String of the currently selected experimental setup name
     */
    String getCurrentlySelectedExperimentalSetupName() {
        ExperimentalSetupView view = (ExperimentalSetupView) mainPane.getSelectedComponent();
        return view.getExpName();
    }

    /**
     * Adds the tabs with the experimental setup views in the editor.
     */
    void addTabs() {
        int i;
        boolean needToCreate;
        Iterator<ExperimentalSetupView> evs;
        String[] names = model.getExperimentNames();
        for (i = 0; i < names.length; i++) {
            needToCreate = true;

            for (evs = views.iterator(); evs.hasNext(); ) {
                ExperimentalSetupView view = evs.next();
                if (view.getName().equals(names[i])) {
                    needToCreate = false;
                    break;
                }
            }
            if (needToCreate) {
                makeNewExperimentalSetupView(names[i]);
            }
        }
    }

    /**
     * Call this when experimental setup is deleted from the model--checks which view should be deleted and deletes it
     */
    void deleteTabs() {
        Iterator<ExperimentalSetupView> i;
        String id;
        ExperimentalSetupView view;
        ArrayList<ExperimentalSetupView> setupsToRemove = new ArrayList<ExperimentalSetupView>();

        //iterate through views
        for (i = views.iterator(); i.hasNext(); ) {
            //figure out if it's experimental setup has been deleted
            view = i.next();
            id = view.getExpName();
            if (!model.isValidExperimentName(id)) {
                //delete view
                setupsToRemove.add(view);
            }
        }

        for (i = setupsToRemove.iterator(); i.hasNext(); ) {
            removeSetup(i.next());
        }
    }

    private void removeSetup(ExperimentalSetupView view) {
        mainPane.remove(view);
        views.remove(view);
        deletedSetups++;
        if (mainPane.getTabCount() <= 0) {
            remButton.setEnabled(false);
            focusListener.fireESViewChangedEvent(this, null);
        } else {
            focusListener.fireESViewChangedEvent(this, ((ExperimentalSetupView) mainPane.getSelectedComponent()).getExpName());
        }
    }

    private void makeWizard(String name) {
        ExperimentalSetupWizard wizard = new ExperimentalSetupWizard(model, name);
        getDesktopPane().add(wizard);
        wizard.setLocation(new Point((int) getLocation().getX() + 15, (int) getLocation().getY() + 15));
        wizard.setVisible(true);
    }

    /**
     * Makes a new experimental setup view and adds it to a new tab in the
     * editor.
     */
    void makeNewExperimentalSetupView(String expName) {
        ExperimentalSetupView view = new ExperimentalSetupView(model, desktop, expName);
        view.setName(expName);
        views.add(view);
        mainPane.add(view, mainPane.getComponentCount());
        remButton.setEnabled(true);
        mainPane.setSelectedIndex(mainPane.getTabCount() - 1);
        tabChanged();
    }
}
