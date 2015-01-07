package edu.cmu.causalityApp.graphEditors.correctGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.CausalityLab;
import edu.cmu.causalityApp.exerciseBuilder.BayesGraphToolbar;
import edu.cmu.causalityApp.exerciseBuilder.SemGraphToolbar;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetradapp.editor.BayesImEditor;
import edu.cmu.tetradapp.editor.BayesPmEditor;
import edu.cmu.tetradapp.editor.DagEditor;
import edu.cmu.tetradapp.editor.SemImEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author efs
 */
class CorrectGraphBuilderFrame extends JFrame {
    private static final String NEXT = "Next >";
    private static final String PREVIOUS = "< Back";

    /**
     * "Finish".
     */
    private static final String FINISH = "Finish";

    /**
     * "Cancel".
     */
    private static final String CANCEL = "Cancel";

    private final CausalityLabModel model;

    private JComponent CURRENT_PANEL;
    private final JToolBar BUTTON_PANEL;
    private final JButton PREVIOUS_BUTTON;
    private final JButton NEXT_BUTTON;
    private final JButton FINISH_BUTTON;


    private Dag dagGraph = null;
    private BayesPm bayesPm = null;
    private BayesIm bayesIm = null;
    private SemPm semPm = null;
    private SemIm semIm = null;
    private final CorrectGraphEditor cge;
    private final boolean isBayes;


    public CorrectGraphBuilderFrame(CausalityLabModel p_model, CorrectGraphEditor p_cge) {
        super("Student Editing Mode");
        this.model = p_model;
        cge = p_cge;
        if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            isBayes = true;
            bayesPm = new BayesPm(model.getCorrectGraphBayesPmCopy());
            bayesIm = new MlBayesIm(model.getCorrectGraphBayesImCopy());
            dagGraph = new Dag(bayesIm.getDag());

        } else {
            isBayes = false;
            semPm = model.getCorrectGraphSemPmCopy();
            semIm = model.getCorrectGraphSemImCopy();

            SemGraph oldSemGraph = new SemGraph(semPm.getGraph());
            oldSemGraph.setShowErrorTerms(false);

            dagGraph = new Dag(oldSemGraph);
        }


        BUTTON_PANEL = new JToolBar();
        BUTTON_PANEL.setFloatable(false);
        BUTTON_PANEL.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 14));

        PREVIOUS_BUTTON = new JButton(PREVIOUS);
        NEXT_BUTTON = new JButton(NEXT);
        JButton CANCEL_BUTTON = new JButton(CANCEL);
        addNextButtonListener();
        addPreviousButtonListener();

        FINISH_BUTTON = new JButton(FINISH);
        FINISH_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((isBayes && isBayesImValuesComplete()) || !isBayes) {
//                    cge.SetGraphNameWindow(CorrectGraphBuilderFrame.this);
                    setVisible(false);
                    dispose();
                }

            }
        });

        CANCEL_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        PREVIOUS_BUTTON.setEnabled(false);
        if (CausalityLab.getGodMode() == 1)
            FINISH_BUTTON.setEnabled(true);
        else
            FINISH_BUTTON.setEnabled(false);

        BUTTON_PANEL.add(CANCEL_BUTTON);
        BUTTON_PANEL.add(Box.createHorizontalGlue());
        BUTTON_PANEL.add(PREVIOUS_BUTTON);
        BUTTON_PANEL.add(Box.createHorizontalStrut(5));
        BUTTON_PANEL.add(NEXT_BUTTON);
        BUTTON_PANEL.add(Box.createHorizontalStrut(15));
        BUTTON_PANEL.add(FINISH_BUTTON);

        CANCEL_BUTTON.setPreferredSize(new Dimension(70, 26));
        PREVIOUS_BUTTON.setPreferredSize(new Dimension(70, 26));
        FINISH_BUTTON.setPreferredSize(new Dimension(70, 26));
        NEXT_BUTTON.setPreferredSize(new Dimension(70, 26));

        BUTTON_PANEL.setPreferredSize(BUTTON_PANEL.getMinimumSize());
        BUTTON_PANEL.setMaximumSize(BUTTON_PANEL.getMaximumSize());
        BUTTON_PANEL.setMinimumSize(BUTTON_PANEL.getMinimumSize());

        setSize(1000, 200);
        getContentPane().setLayout(new BorderLayout());
        getFirstPanel();
    }


    public boolean IsBayes() {
        return isBayes;
    }

    public BayesIm getBayesIm() {
        return bayesIm;
    }

    public SemIm getSemIm() {
        return semIm;
    }

    /**
     * Checks if all the probability values are entered in for each variable in
     * a bayesian graph.
     *
     * @return true if all the values in entered in.
     */
    private boolean isBayesImValuesComplete() {
        for (int i = 0; i < getBayesIm().getNumNodes(); i++) {
            if (getBayesIm().isIncomplete(i)) {
                return false;
            }
        }
        return true;
    }

    private void addNextButtonListener() {
        if (CausalityLab.getGodMode() == 1)
            addGodMode1NextButtonListener();
        else if (CausalityLab.getGodMode() == 2)
            addGodMode2NextButtonListener();
    }

    private void addGodeMode1PreviousButtonListener() {
        PREVIOUS_BUTTON.addActionListener(null);
        PREVIOUS_BUTTON.setEnabled(false);
        //FINISH_BUTTON.setEnabled(true);
    }

    private void addGodMode1NextButtonListener() {

        NEXT_BUTTON.addActionListener(null);
        NEXT_BUTTON.setEnabled(false);
        //FINISH_BUTTON.setEnabled(true);

    }

    private void getFirstPanel() {
        if (CausalityLab.getGodMode() == 1) {
            if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES))
                setCurrentPanel(getBayesImEditorPanel());
            else
                setCurrentPanel(getSemImEditorPanel());
        } else if (CausalityLab.getGodMode() == 2) {
            if (isBayes)
                setCurrentPanel(getBayesGraphPanel());
            else
                setCurrentPanel(getSemGraphPanel());
        }

    }

    private void addGodMode2NextButtonListener() {
        NEXT_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (CURRENT_PANEL instanceof DagEditor) {
                    if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
                        BayesPmEditor pmEditor = getBayesPmEditorPanel();
                        setCurrentPanel(pmEditor);
                        //pmEditor.setEditingLatentVariablesAllowed(true);
                        //pmEditor.setEditingMeasuredVariablesAllowed(false);
                        FINISH_BUTTON.setEnabled(false);
                        PREVIOUS_BUTTON.setEnabled(true);
                        NEXT_BUTTON.setEnabled(true);

                    } else {
                        SemImEditor imEditor = getSemImEditorPanel();
                        setCurrentPanel(imEditor);
                        imEditor.setEditIntercepts(false);
                        FINISH_BUTTON.setEnabled(true);
                        NEXT_BUTTON.setEnabled(false);
                        PREVIOUS_BUTTON.setEnabled(true);

                    }
                } else if (CURRENT_PANEL instanceof BayesPmEditor) {
                    BayesImEditor imEditor = getBayesImEditorPanel();
                    setCurrentPanel(imEditor);
                    FINISH_BUTTON.setEnabled(true);
                    NEXT_BUTTON.setEnabled(false);
                    PREVIOUS_BUTTON.setEnabled(true);
                }
            }

        });

    }

    private void addGodMode2PreviousButtonListener() {
        PREVIOUS_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (CURRENT_PANEL instanceof DagEditor) {
                } else if (CURRENT_PANEL instanceof BayesPmEditor) {
                    System.out.println("dagGraph" + getDagGraph());
                    setCurrentPanel(getBayesGraphPanel());
                    FINISH_BUTTON.setEnabled(false);
                    NEXT_BUTTON.setEnabled(true);
                    PREVIOUS_BUTTON.setEnabled(false);
                } else if (CURRENT_PANEL instanceof SemImEditor) {
                    setCurrentPanel(getSemGraphPanel());
                    FINISH_BUTTON.setEnabled(true);
                    PREVIOUS_BUTTON.setEnabled(false);
                    NEXT_BUTTON.setEnabled(true);
                } else if (CURRENT_PANEL instanceof BayesImEditor) {
                    BayesPmEditor pmEditor = getBayesPmEditorPanel();
                    setCurrentPanel(pmEditor);
                    //pmEditor.setEditingLatentVariablesAllowed(true);
                    //pmEditor.setEditingMeasuredVariablesAllowed(false);
                    FINISH_BUTTON.setEnabled(false);
                    NEXT_BUTTON.setEnabled(true);
                    PREVIOUS_BUTTON.setEnabled(true);
                }

            }

        });

    }

    /**
     * This controls the actions of the PREVIOUS button, and is use to determine the
     * sequence of dialogs that appear in the wizard.
     */
    private void addPreviousButtonListener() {
        if (CausalityLab.getGodMode() == 1)
            addGodeMode1PreviousButtonListener();
        else if (CausalityLab.getGodMode() == 2)
            addGodMode2PreviousButtonListener();
    }


    /**
     * Set the getModel screen.
     */
    private void setCurrentPanel(JComponent component) {

        if (CURRENT_PANEL != null) {
            remove(CURRENT_PANEL);
        }

        CURRENT_PANEL = component;

        getContentPane().removeAll();
        getContentPane().add(CURRENT_PANEL, BorderLayout.PAGE_START);  //PAGE_START
        getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);
        getContentPane().add(BUTTON_PANEL, BorderLayout.PAGE_END);

        pack();
        setVisible(true);
        this.setVisible(true);
    }


    /**
     * Makes a panel to edit the bayes graph.
     *
     * @return the graph editor for bayes graph.
     */
    //private BayesGraphEditor getBayesGraphPanel(){
    private DagEditor getBayesGraphPanel() {

        semPm = null;
        semIm = null;

        if (getDagGraph() == null) {
            dagGraph = new Dag();
            bayesPm = null;
            bayesIm = null;
        }

        //DagEditor bayesGraphEditor = new DagEditor(bayesGraph);
        System.out.println("the answer to the problem??  " + getDagGraph().getNode("L2"));
        System.out.println("bayes pm dag has l2: " + bayesPm.getDag().getNode("L2"));


        final DagEditor bayesGraphEditor = new DagEditor(getDagGraph());
        if (CausalityLab.getGodMode() == 2) {
            //bayesGraphEditor.getWorkbench().setAddMeasuredVarsAllowed(false);
            //bayesGraphEditor.getWorkbench().setDeleteVariablesAllowed(false);
            bayesGraphEditor.getWorkbench().setAllowEdgeReorientations(true);

            BayesGraphToolbar toolbar = new BayesGraphToolbar(bayesGraphEditor.getWorkbench());
            toolbar.disableAddObserved();

        }

        //todo: find out what this code is used for: after reordering the make exercise frames,
        // this pcl is not used, but code seems to run fine.
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {

                    bayesPm = null;
                    bayesIm = null;
                    FINISH_BUTTON.setEnabled(false);
                    dagGraph = (Dag) bayesGraphEditor.getGraph();

                }
            }
        };

        bayesGraphEditor.addPropertyChangeListener(pcl);

        return bayesGraphEditor;
    }


    private BayesPmEditor getBayesPmEditorPanel() {
        //if(bayesPm == null){bayesPm = new BayesPm(bayesGraph);}

        if (bayesPm == null) {
            bayesPm = new BayesPm(getDagGraph());
        }

        //BayesPmEditor bpe = new BayesPmEditor(bayesPm);
        BayesPmEditor bpe = new BayesPmEditor(bayesPm);
        if (CausalityLab.getGodMode() == 2) {
            //bpe.setEditingLatentVariablesAllowed(true);
        }


        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {
                    //bayesIm = null;

                    bayesIm = null;
                    FINISH_BUTTON.setEnabled(false);
                }
            }
        };

        bpe.addPropertyChangeListener(pcl);
        return bpe;
    }


    private BayesImEditor getBayesImEditorPanel() {

        if (bayesIm == null) {
            bayesIm = new MlBayesIm(bayesPm);
        }

        return new BayesImEditor(bayesIm);
    }


    /**
     * Makes a panel to edit the sem graph
     *
     * @return the SEM DagEditor.
     */
    private DagEditor getSemGraphPanel() {


        bayesPm = null;
        bayesIm = null;

        if (getDagGraph() == null) {
            dagGraph = new Dag();
            semPm = null;
            semIm = null;
        }


//            GraphParams gp = new GraphParams();
//            gp.setInitializationMode(GraphParams.MANUAL);

        DagEditor de = new DagEditor(getDagGraph());
        if (CausalityLab.getGodMode() == 2) {
            //de.getWorkbench().setAddMeasuredVarsAllowed(false);
            //de.getWorkbench().setDeleteVariablesAllowed(false);
            //de.getWorkbench().setAllowEdgeReorientations(true);

            SemGraphToolbar toolbar = new SemGraphToolbar(de.getWorkbench());
            toolbar.disableAddObserved();

        }

        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {
                    semPm = null;
                    semIm = null;
                    FINISH_BUTTON.setEnabled(false);
                }
            }
        };
        de.addPropertyChangeListener(pcl);
        dagGraph = (Dag) de.getGraph();

        return de;
    }


    private SemImEditor getSemImEditorPanel() {

        if (semPm == null) {
            semPm = new SemPm(getDagGraph());

        }

        if (semIm == null) {
            semIm = new SemIm(semPm);
        }


        semIm.getSemPm().getGraph().setShowErrorTerms(true);
        return new SemImEditor(semIm);
    }

    public BayesPm getBayesPm() {
        return bayesPm;
    }

    public SemPm getSemPm() {
        return semPm;
    }


    public Dag getDagGraph() {
        return dagGraph;
    }
}
