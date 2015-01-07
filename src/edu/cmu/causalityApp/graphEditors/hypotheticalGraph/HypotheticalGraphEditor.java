package edu.cmu.causalityApp.graphEditors.hypotheticalGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.DeleteHypothesisCommand;
import edu.cmu.causality.event.*;
import edu.cmu.causalityApp.graphEditors.GraphEditor;
import edu.cmu.causalityApp.graphEditors.HypotheticalGraphFocusListener;
import edu.cmu.command.Command;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class describes the Hypothetical Graph Editor window, with each hypothetical
 * graph in each tab.
 *
 * @author greg
 */
public class HypotheticalGraphEditor extends GraphEditor {

    /**
     * Unique name of the graph editor. Used by CausalityLabPanel to
     * identify graph editors.
     */
    public static final String MY_NAME = "Hypothesis Graph";

    //Remove Graph button
    private JButton remGraph;

    //Track number of deleted graphs so far
    private int deletedGraphs;

    //LinkList of all existing hyp graph views
    private final List graphViews;

    //Pointer to CausalityLabPanel to propagate any changes in this window view
    private final HypotheticalGraphFocusListener hgFocusListener;


    // ========================================================
    //    PUBLIC METHODS
    // ========================================================

    /**
     * Constructor.
     */
    public HypotheticalGraphEditor(CausalityLabModel model,
                                   InternalFrameListener parent,
                                   JDesktopPane desktop,
                                   HypotheticalGraphFocusListener listener) {

        super(model, parent, desktop, MY_NAME, true);

        hgFocusListener = listener;
        graphViews = new LinkedList();
        deletedGraphs = 0;

        setMainView(new JTabbedPane(JTabbedPane.RIGHT));
        getContentPane().add(getMainView(), BorderLayout.CENTER);

        String[] hypNames = model.getHypotheticalGraphNames();
        for (int i = 0; i < hypNames.length; i++) {
            String name = hypNames[i];
            HypotheticalGraphView view = new HypotheticalGraphView(name, this, model);
            getMainView().add(view, model.getHypotheticalGraphName(name));
        }

        if (((JTabbedPane) getMainView()).getTabCount() > 0) {
            hgFocusListener.fireHgViewChangedEvent(this, hypNames[0]);
        }

        ((JTabbedPane) getMainView()).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                tabChanged();
            }
        });

        model.addModelChangeListener(new ModelChangeListener() {

            public void hypothesisChanged(HypothesisChangedEvent e) {
                deleteTabs();
                addTabs();
                refreshTabs();
            }

            public void experimentChanged(ExperimentChangedEvent e) {
            }

            public void sampleChanged(SampleChangedEvent e) {
            }

            public void financeChanged() {
            }
        });
    }

    /**
     * @return name of this graph editor.
     */
    public String getEditorName() {
        return MY_NAME;
    }

    /**
     * Add an edge from node with fromName to node with toName.
     */
    public void addEdge(String fromName, String toName) {
    }


    // ========================================================
    //    PRIVATE METHODS
    // ========================================================

    /**
     * When user changes the tab, the CausalityLabPanel is told the frame.
     */
    private void tabChanged() {
        if (((JTabbedPane) getMainView()).getSelectedIndex() >= 0) {
            HypotheticalGraphView hgView =
                    (HypotheticalGraphView) ((JTabbedPane) getMainView()).getSelectedComponent();
            String hypGraphViewName = hgView.getName();

            hgFocusListener.fireHgViewChangedEvent(this, hypGraphViewName);
            //*log* (graph tab changed)
        } else {
            hgFocusListener.fireHgViewChangedEvent(this, null);
        }
    }

    /**
     * Removes the specified hyp. graph view from linked list of stored graph
     * views.
     */
    private void removeGraph(HypotheticalGraphView view) {
        getMainView().remove(view);
        graphViews.remove(view);
        deletedGraphs++;
        if (((JTabbedPane) getMainView()).getTabCount() <= 0) {
            remGraph.setEnabled(false);
            hgFocusListener.fireHgViewChangedEvent(this, null);
        } else {
            HypotheticalGraphView hgView =
                    (HypotheticalGraphView) ((JTabbedPane) getMainView()).getSelectedComponent();
            hgFocusListener.fireHgViewChangedEvent(this, hgView.getName());
        }
    }

    /**
     * Creates and launches wizard to edit a hyp graph.
     */
    private void makeWizardToEditGraph(String hypName) {
        HypotheticalGraphWizard wizard = HypotheticalGraphWizard.editExistingGraph(
                getModel(), getFrameListener(), getDesktop(), this, hypName);
        getDesktop().add(wizard);
        wizard.setLocation(getLocation());
        wizard.pack();
        wizard.setVisible(true);
    }

    /**
     * @return the next hypothetical graph id to assign to a new hyp graph.
     */
    private int getNextSuggestedHypId() {

        return (((JTabbedPane) getMainView()).getTabCount() + deletedGraphs + 1);
    }

    /**
     * Gets the id for the currently selected experimental setup.
     * Added so that moves object can figure out which experimental setup is
     * selected to delete from model.
     *
     * @return the name of the currently selected hypothetical graph.
     */
    private String getCurrentlySelectedHypotheticalGraphName() {
        HypotheticalGraphView view =
                (HypotheticalGraphView) ((JTabbedPane) getMainView()).getSelectedComponent();
        return view.getName();
    }

    /**
     * Get the names of all possible hypothetical graph from the model and
     * create the corresponding hypothetical graph view.
     */
    private void addTabs() {
        int i;
        boolean needToCreate = false;
        Iterator gvs;
        String[] names = getModel().getHypotheticalGraphNames();
        for (i = 0; i < names.length; i++) {
            needToCreate = true;

            for (gvs = graphViews.iterator(); gvs.hasNext(); ) {
                HypotheticalGraphView view = (HypotheticalGraphView) gvs.next();
                if (view.getName().equals(names[i])) {
                    needToCreate = false;
                    break;
                }
            }

            if (needToCreate) {
                setNewHypotheticalGraph(names[i]);
            }
        }
    }

    /**
     * Call this when experimental setup is deleted from the model--checks which
     * view should be deleted and deletes it.
     */
    private void deleteTabs() {
        ArrayList setupsToRemove = new ArrayList();

        //iterate through views
        for (Iterator i = graphViews.iterator(); i.hasNext(); ) {
            //figure out if it's hyp graph has been deleted
            HypotheticalGraphView view = (HypotheticalGraphView) i.next();
            String name = view.getName();
            if (getModel().getHypotheticalGraphName(name) == null) {
                //delete view
                setupsToRemove.add(view);
            }
        }

        for (Iterator i = setupsToRemove.iterator(); i.hasNext(); ) {
            removeGraph((HypotheticalGraphView) i.next());
        }
    }

    /**
     * Refresh the hypothetical graph views of all the tabs on the editor window.
     */
    private void refreshTabs() {
        for (Iterator i = graphViews.iterator(); i.hasNext(); ) {
            HypotheticalGraphView view = (HypotheticalGraphView) i.next();
            view.refreshViews();
        }
        repaint();
    }

    /**
     * Creates and launches the wizard to create a new hyp graph.
     */
    private void makeWizardForNewGraph(String name) {
        HypotheticalGraphWizard wizard = HypotheticalGraphWizard.makeNewGraph(
                getModel(), getFrameListener(), getDesktop(), this, name);
        getDesktop().add(wizard);
        wizard.setLocation(getLocation());
        wizard.pack();
        wizard.setVisible(true);
    }

    /**
     * Create a hypothetical graph view with the name specified.
     */
    private void setNewHypotheticalGraph(String hypName) {
        HypotheticalGraphView graphView = new HypotheticalGraphView(hypName, this, getModel());
        graphView.setName(getModel().getHypotheticalGraphName(hypName));
        graphView.refreshViews();
        getMainView().add(graphView, getMainView().getComponentCount());
        graphViews.add(graphView);
        remGraph.setEnabled(true);
        ((JTabbedPane) getMainView()).setSelectedIndex(((JTabbedPane) getMainView()).getTabCount() - 1);
        tabChanged();
    }


    // ========================================================
    //    PROTECTED METHODS
    // ========================================================

    /**
     * Create the toolbar for this window.
     */
    protected void fillToolbar() {
        remGraph = new JButton("Remove");
        JButton newGraph = new JButton("New");
        JButton edit = new JButton("Edit");

        // remGraph.setEnabled (false);

        getToolbar().add(Box.createHorizontalGlue());
        getToolbar().add(newGraph);
        getToolbar().add(edit);
        getToolbar().add(remGraph);


        newGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newName = (String) JOptionPane.showInternalInputDialog(getDesktopPane(),
                        "Enter a name:", "Add Hyp. Graph", JOptionPane.PLAIN_MESSAGE, null, null,
                        "Hypothesis" + getNextSuggestedHypId());
                if (newName != null) {
                    makeWizardForNewGraph(newName);
                }
            }
        });

        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (((JTabbedPane) getMainView()).getTabCount() > 0) {
                    HypotheticalGraphView hypotheticalGraphView =
                            (HypotheticalGraphView) ((JTabbedPane) getMainView()).getSelectedComponent();
                    makeWizardToEditGraph(hypotheticalGraphView.getName());
                }
            }
        });

        remGraph.setEnabled(false);
        //remGraph.setCommand(new DeleteCurrentHypothesisCommand(model, this));
        remGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String hypName = getCurrentlySelectedHypotheticalGraphName();
                Command cmd = new DeleteHypothesisCommand(hypName);
                cmd.doIt();
            }
        });
    }


    // ARCHIVED CODE
    /*
    public void setOldHypotheticalGraph(String hypName){
        for (Iterator i = graphViews.iterator(); i.hasNext();) {
            HypotheticalGraphView view = (HypotheticalGraphView) i.next();
            if (view.getName().equals(hypName)) {
                view.refreshViews();
                tabChanged();
                break;
            }
        }
    }

    public void editHypotheticalGraph(int hypId, String moves) {
        if (moves.startsWith("New ")) {
            HypotheticalGraphView graphView = new HypotheticalGraphView(hypId, this, model);
            graphView.setName(model.getHypotheticalGraphName(hypId));
            graphView.refreshViews();
            mainView.add(graphView, hypId - deletedGraphs);
            graphViews.add(graphView);
            remGraph.setEnabled(true);
            ((JTabbedPane) mainView).setSelectedIndex(((JTabbedPane) mainView).getTabCount() - 1);
            tabChanged();
        } else if (moves.startsWith("Edit ")) {
            for (Iterator i = graphViews.iterator(); i.hasNext();) {
                HypotheticalGraphView view = (HypotheticalGraphView) i.next();
                if (view.getExpName() == hypId) {
                    view.refreshViews();
                    tabChanged();
                    break;
                }
            }
        } else
            System.err.println("This should never be printed!");
    }
    */
}
