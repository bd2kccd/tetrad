package edu.cmu.causalityApp.graphEditors.correctGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.event.*;
import edu.cmu.causalityApp.CausalityLab;
import edu.cmu.causalityApp.graphEditors.CorrectGraphFocusListener;
import edu.cmu.causalityApp.graphEditors.GraphEditor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

/**
 * This class describes the Correct Graph Editor frame which shows the correct
 * graph in the exercise.
 *
 * @author greg
 */
public class CorrectGraphEditor extends GraphEditor {

    /**
     * Unique name of the graph editor. Used by CausalityLabPanel to
     * identify graph editors.
     */
    public static final String MY_NAME = "True Graph Editor";

    public static final String OLD_NAME = "Correct Graph";

    //Track number of deleted graphs so far
    private final int deletedGraphs;

    //LinkList of all existing corr graph views
    private final List<CorrectGraphView> graphViews;

    //Pointer to CausalityLabPanel to propagate any changes in this window view
    private final CorrectGraphFocusListener cgFocusListener;

    private final String expName = null; // unassigned jdramsey 6/9/13 Why??


    // ========================================================
    //    PUBLIC METHODS
    // ========================================================

    /**
     * Constructor.
     */
    public CorrectGraphEditor(CausalityLabModel model,
                              InternalFrameListener parent,
                              JDesktopPane desktop,
                              CorrectGraphFocusListener listener) {

        super(model, parent, desktop, MY_NAME, true);

        cgFocusListener = listener;
        graphViews = new LinkedList<CorrectGraphView>();
        deletedGraphs = 0;

        setMainView(new JTabbedPane(JTabbedPane.RIGHT));
        getContentPane().add(getMainView(), BorderLayout.CENTER);

        String[] corrNames = new String[] {"True"};

        for (String name : corrNames) {
            CorrectGraphView view = new CorrectGraphView(name, this, model);
            getMainView().add(view, name);
        }

        if (((JTabbedPane) getMainView()).getTabCount() > 0) {
            //todo: look at this later
//            cgFocusListener.fireCgViewChangedEvent(this, corrNames[0]);
        }

        ((JTabbedPane) getMainView()).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                tabChanged();
            }
        });

        model.addModelChangeListener(new ModelChangeListener() {

            public void hypothesisChanged(HypothesisChangedEvent e) {
            }

            public void experimentChanged(ExperimentChangedEvent e) {
            }

            public void sampleChanged(SampleChangedEvent e) {
            }

            public void financeChanged() {
            }
        });
    }

    public void setHidden(boolean hidden) {
        if (hidden) {
            setMainView(new CorrectGraphView.HiddenView(expName, this, getModel()));
            setContentPane(getMainView());
        } else {
            setMainView(new CorrectGraphView(expName, this, getModel()));
            setContentPane(getMainView());
        }
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
            CorrectGraphView cgView =
                    (CorrectGraphView) ((JTabbedPane) getMainView()).getSelectedComponent();
            String correctGraphViewName = cgView.getName();

            cgFocusListener.fireCgViewChangedEvent(this, correctGraphViewName);

            refreshTabs();
            System.out.println("Changing Tabs?");
        } else {
            cgFocusListener.fireCgViewChangedEvent(this, null);
        }
    }

    /**
     * Creates and launches wizard to edit a hyp graph.
     */
    private void makeWizardToEditGraph(CausalityLabModel m) {
        new CorrectGraphBuilderFrame(m, this);
    }

//    public void SetGraphNameWindow(CorrectGraphBuilderFrame cgbf) {
//        String newname = (String) JOptionPane.showInternalInputDialog(getDesktopPane(),
//                "Enter name for edited true model", "Add new model", JOptionPane.PLAIN_MESSAGE, null, null,
//                "TrueModel" + getNextSuggestedHypId());
//        saveThisCorrectModel(cgbf, newname);
//    }

//    void saveThisCorrectModel(CorrectGraphBuilderFrame cgbf, String nameOfModel) {
//        if (cgbf.IsBayes()) {
//            getModel().addNewCorrectBayesIm(cgbf.getBayesIm());
//        } else {
//            getModel().addNewCorrectGraph(cgbf.getDagGraph(), cgbf.getSemIm(), cgbf.getSemPm());
//        }
//        getModel().addTrueModelName(nameOfModel);
//    }


    /**
     * @return the next hypothetical graph id to assign to a new hyp graph.
     */
    private int getNextSuggestedHypId() {
        return (((JTabbedPane) getMainView()).getTabCount() + deletedGraphs + 1);
    }

    /**
     * Refresh the correct graph views of all the tabs on the editor window.
     */
    private void refreshTabs() {
        for (Object graphView : graphViews) {
            CorrectGraphView view = (CorrectGraphView) graphView;
            view.refreshViews();
        }
        repaint();
    }

    /**
     * Create a correct graph view with the name specified.
     */
    private void setNewCorrectGraph(String corrName) {
        CorrectGraphView graphView = new CorrectGraphView(corrName, this, getModel());

        graphView.setName(corrName);
        graphView.refreshViews();
        getMainView().add(graphView, getMainView().getComponentCount());
        graphViews.add(graphView);
        ((JTabbedPane) getMainView()).setSelectedIndex(((JTabbedPane) getMainView()).getTabCount() - 1);
    }


    // ========================================================
    //    PROTECTED METHODS
    // ========================================================

    /**
     * Create the toolbar for this window.
     */
    protected void fillToolbar() {
        int mode = CausalityLab.getGodMode();
        JButton edit = new JButton("Edit");
        getToolbar().add(Box.createHorizontalGlue());

        // NOT GOD MODE
        if (mode == 1) {
            getToolbar().add(edit);
        } else if (mode == 2) {
            getToolbar().add(edit);
        } else if (mode == 3) {
            JButton newGraph = new JButton("New");
            getToolbar().add(newGraph);
            getToolbar().add(edit);

            newGraph.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int goAhead = JOptionPane.showConfirmDialog(getDesktopPane(), "New graph will overwrite" +
                            " previous version of graph: do you want to destroy the getModel version?");
                    if (goAhead == 0) {
                        JOptionPane.showInternalInputDialog(getDesktopPane(),
                                "Enter a name:", "Add new model", JOptionPane.PLAIN_MESSAGE, null, null,
                                "TrueModel" + getNextSuggestedHypId());
                    }
                }
            });
        }


        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (((JTabbedPane) getMainView()).getTabCount() > 0) {
                    makeWizardToEditGraph(getModel());
                }
            }
        });

    }


}
