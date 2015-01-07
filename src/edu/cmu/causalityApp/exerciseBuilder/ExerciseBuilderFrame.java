package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.CausalityLab;
import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetradapp.editor.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This is the wrapper frame for the different dialogs in the Exercise wizard.
 *
 * @author matt and adrian
 */
public class ExerciseBuilderFrame extends JFrame {

    private static final Color BACKGROUND_COLOR = Color.WHITE;

//    private static JFileChooser fileChooser = null;

    private static final String NEXT = "Next >";
    private static final String PREVIOUS = "< Back";

    /**
     * "Finish".
     */
    public static final String FINISH = "Finish";

    /**
     * "Cancel".
     */
    private static final String CANCEL = "Cancel";

    private final Exercise exercise;

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

    /**
     * Use this constructor when creating a new exercise.
     */
    public ExerciseBuilderFrame(ActionListener doneListener) {
        this(doneListener, null);
        CausalityLab causalityLab = (CausalityLab) doneListener;
    }


    /**
     * Use this constructor when editing an exercise.
     *
     * @param exercise if this is null, a new exercise will be created.
     */
    public ExerciseBuilderFrame(ActionListener doneListener, Exercise exercise) {
        super("Make an Exercise");

        // creating a new exercise, its by default set to BayesIM.....
        if (exercise == null) {
            this.exercise = new Exercise();
            this.exercise.setUsingBayesIm(true);
        } else {
            this.exercise = exercise;
            if (this.exercise.isUsingBayesIm()) {
                BayesIm oldBayesIm = this.exercise.getBayesModelIm();
                bayesPm = oldBayesIm.getBayesPm();
                dagGraph = oldBayesIm.getDag();

                bayesIm = new MlBayesIm(bayesPm, oldBayesIm, MlBayesIm.MANUAL);

            } else {
                semIm = this.exercise.getSemModelIm();
                semPm = semIm.getSemPm();

                SemGraph oldSemGraph = semPm.getGraph();
                oldSemGraph.setShowErrorTerms(false);

                dagGraph = new Dag(oldSemGraph);
//                semPm = new SemPm(dagGraph);
//                semPm = new SemPm(oldSemPm);
//                semIm = new SemIm(semPm);
//
//                SemIm semIm = this.semIm;
//
//                for (Node node : oldSemIm.getVariableNodes()) {
//                    semIm.setMean(node, oldSemIm.getMean(node));
//                }
//
//                semIm.setParamValues(oldSemIm.getParamValues());
            }
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
        FINISH_BUTTON.addActionListener(doneListener);

        CANCEL_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        PREVIOUS_BUTTON.setEnabled(false);
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
//        setCurrentPanel(new ExerciseInfoPanel(this.exercise, BACKGROUND_COLOR));
        setCurrentPanel(new ChooseGodModePanel(this.exercise));
    }

    /**
     * @return the getModel exercise being created or edited.
     */
    public Exercise getExercise() {
        //setImModel();
        return exercise;
    }

    /**
     * Helper function to set the appropriate type of model, ie. SEM or Bayesian.
     */
    private void setImModel() {
        if (exercise.isUsingBayesIm()) {
            //exercise.setBayesModelIm(bayesIm);
            exercise.setBayesModelIm(getBayesIm());
        } else {
            //exercise.setSemModelIm(semIm);
            exercise.setSemModelIm(getSemIm());
        }
    }

    private BayesIm getBayesIm() {
        return bayesIm;
    }

    private SemIm getSemIm() {
        return semIm;
    }


    /*
     * Gets the extension of a filepath
     * @param filepath e.g. "somedir/filename.xml"
     * @return the extension, e.g. "xml"
     */
    private String getExtension(String filepath) {
        String ext = null;
        int i = filepath.lastIndexOf('.');
        if (i > 0 && i < filepath.length() - 1) {
            ext = filepath.substring(i + 1).toLowerCase();
        }
        return ext;
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


    /**
     * This controls the actions of the NEXT button, and is use to determine the
     * sequence of dialogs that appear in the wizard.
     */
    private void addNextButtonListener() {
        NEXT_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (CURRENT_PANEL instanceof ChooseGodModePanel) {
//                    if (exercise.getIsGodMode() && !exercise.getBuildNow()) {
                    // if instructor wants to build later
//                       causalityLab.actionPerformed(null);
//                    } else {
                    // if instructor wants to build later
                    setCurrentPanel(getDagPanel());
                    PREVIOUS_BUTTON.setEnabled(true);//                    }

                    // } else if (CURRENT_PANEL instanceof ExerciseInfoPanel) {
                    /*
                    } else {
//                        setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));

                        if(exercise.getIsGodMode() == Exercise.NOT_GM && !exercise.getBuildNow()){
                            setCurrentPanel(new OptionalInstructionsPanel(exercise));
                            PREVIOUS_BUTTON.setEnabled(true);
                            FINISH_BUTTON.setEnabled(true);

                        } else {

                        }
                    }   */
                } else if (CURRENT_PANEL instanceof DagEditor) {
                    setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));

                } else if (CURRENT_PANEL instanceof ChooseBayesOrSemPanel) {
                    exercise.setUsingBayesIm(((ChooseBayesOrSemPanel) CURRENT_PANEL).isBayesSelected());
                    if (exercise.isUsingBayesIm()) {
//                        setCurrentPanel(getBayesGraphPanel());
                        if (dagGraph.getNumNodes() < 1) {
                            JOptionPane.showMessageDialog(ExerciseBuilderFrame.this, "You need to add at least 1 variable");
                        } else {
                            setCurrentPanel(getBayesPmEditorPanel());
                        }
                    } else {
//                        setCurrentPanel(getSemGraphPanel());
                        if (dagGraph.getNumNodes() < 1) {
                            JOptionPane.showMessageDialog(ExerciseBuilderFrame.this, "You need to add at least 1 variable");
                        } else {
                            SemImEditor imEditor = getSemImEditorPanel();
                            setCurrentPanel(imEditor);
                            imEditor.setEditIntercepts(true);
                        }

                    }

//                } else if((CURRENT_PANEL instanceof DagEditor) && (exercise.isUsingBayesIm())){
//                    //if(bayesGraph.getNumNodes() < 1){
//                    if(dagGraph.getNumNodes() < 1){
//                        JOptionPane.showMessageDialog(ExerciseBuilderFrame.this, edu.cmu.StringTemp.message("You need to add at least 1 variable"));
//                    } else {
//                        setCurrentPanel(getBayesPmEditorPanel());
//                    }

                } else if (CURRENT_PANEL instanceof BayesPmEditor) {
                    setCurrentPanel(getBayesImEditorPanel());

                } else if (CURRENT_PANEL instanceof BayesImEditor) {
                    for (int i = 0; i < getBayesIm().getNumNodes(); i++) {
                        if (getBayesIm().isIncomplete(i)) {
                            JOptionPane.showMessageDialog(ExerciseBuilderFrame.this, "You need to fill out all the values for variable" + " " + getBayesIm().getNode(i).getName());
                            return;
                        }
                    }
                    setImModel();
//                    exercise.setDefaultVariableIntervenableStatus();

                    setCurrentPanel2(new ExerciseInfoPanel(exercise));
//                } else if((CURRENT_PANEL instanceof DagEditor) && !(exercise.isUsingBayesIm())){
//                    //if(semGraph.getNumNodes() < 1){
//                    if(dagGraph.getNumNodes() < 1){
//                        JOptionPane.showMessageDialog(ExerciseBuilderFrame.this, edu.cmu.StringTemp.message("You need to add at least 1 variable"));
//                    } else {
//                        setCurrentPanel(getSemPmEditorPanel());
//                    }

                    /*  } else if (CURRENT_PANEL instanceof SemPmEditor) {
                      SemImEditor imEditor = getSemImEditorPanel();
                      setCurrentPanel(imEditor);
                      imEditor.setEditIntercepts(true);
                    */
                } else if (CURRENT_PANEL instanceof SemImEditor) {
                    setImModel();
//                    exercise.setDefaultVariableIntervenableStatus();
                    setCurrentPanel2(new ExerciseInfoPanel(exercise));

                } else if (CURRENT_PANEL instanceof ExerciseInfoPanel) {
                    if (exercise.getPrompt() == null || exercise.getPrompt().equals("")) {
                        JOptionPane.showMessageDialog(ExerciseBuilderFrame.this, "Need to type instructions");
                    } else {
                        setCurrentPanel(new OptionalInstructionsPanel());
                        FINISH_BUTTON.setEnabled(true);
                    }
                } else if (CURRENT_PANEL instanceof OptionalInstructionsPanel) {
                    setCurrentPanel(new NavigatorIconPanel(exercise));

                } else if (CURRENT_PANEL instanceof NavigatorIconPanel) {
                    setCurrentPanel(new HideNavigatorIconPanel(exercise));

                } else if (CURRENT_PANEL instanceof HideNavigatorIconPanel) {
                    setCurrentPanel(new LimitResourcesPanel(exercise));

                } else if (CURRENT_PANEL instanceof LimitResourcesPanel) {
                    if (((LimitResourcesPanel) CURRENT_PANEL).validateFields()) {
                        ((LimitResourcesPanel) CURRENT_PANEL).applyChanges();
                        if (exercise.getIsGodMode() != Exercise.NOT_GM) {
                            setCurrentPanel2(new EssayPanel(exercise));
                        } else {
                            setCurrentPanel(new VariableIntervenablePanel(exercise));
                        }
//                        NEXT_BUTTON.setEnabled(false);
                    }

                } else if (CURRENT_PANEL instanceof VariableIntervenablePanel) {
//                    if (true){
//                        ((LimitResourcesPanel)CURRENT_PANEL).applyChanges();
                    setCurrentPanel2(new EssayPanel(exercise));
                    NEXT_BUTTON.setEnabled(false);


                }

            }
        });
    }

    /**
     * This controls the actions of the PREVIOUS button, and is use to determine the
     * sequence of dialogs that appear in the wizard.
     */
    private void addPreviousButtonListener() {
        PREVIOUS_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (CURRENT_PANEL instanceof ChooseGodModePanel) {
                    // first frame : this case shouldn't occur

                } else if (CURRENT_PANEL instanceof DagEditor) {
//                    setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));
                    PREVIOUS_BUTTON.setEnabled(false);
                    setCurrentPanel(new ChooseGodModePanel(exercise));
                    //setCurrentPanel2(new ExerciseInfoPanel(exercise, BACKGROUND_COLOR));

                } else if (CURRENT_PANEL instanceof ChooseBayesOrSemPanel) {
//                    setCurrentPanel(new ExerciseInfoPanel(exercise, BACKGROUND_COLOR));
//                    PREVIOUS_BUTTON.setEnabled(false);
                    setCurrentPanel(getDagPanel());
                } else if (CURRENT_PANEL instanceof BayesPmEditor) {
//                   setCurrentPanel(getBayesGraphPanel());
                    setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));
                } else if (CURRENT_PANEL instanceof BayesImEditor) {
                    //setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));
                    setCurrentPanel(getBayesPmEditorPanel());
                } else if (CURRENT_PANEL instanceof SemGraphEditor) {
                    setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));

                    /*} else if (CURRENT_PANEL instanceof SemPmEditor) {
//                    setCurrentPanel(getSemGraphPanel());
                  setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));
                    */
                } else if (CURRENT_PANEL instanceof SemImEditor) {
                    setCurrentPanel(new ChooseBayesOrSemPanel(exercise.isUsingBayesIm()));

                } else if (CURRENT_PANEL instanceof ExerciseInfoPanel) {
                    if (exercise.isUsingBayesIm()) {
                        setCurrentPanel(getBayesImEditorPanel());
                    } else {
                        setCurrentPanel(getSemImEditorPanel());
                    }
                } else if (CURRENT_PANEL instanceof OptionalInstructionsPanel) {

                    setCurrentPanel(new ExerciseInfoPanel(exercise));
                    FINISH_BUTTON.setEnabled(false);

                } else if (CURRENT_PANEL instanceof NavigatorIconPanel) {
                    setCurrentPanel(new OptionalInstructionsPanel());

                } else if (CURRENT_PANEL instanceof HideNavigatorIconPanel) {
                    setCurrentPanel(new NavigatorIconPanel(exercise));

                } else if (CURRENT_PANEL instanceof LimitResourcesPanel) {
                    if (((LimitResourcesPanel) CURRENT_PANEL).validateFields()) {
                        ((LimitResourcesPanel) CURRENT_PANEL).applyChanges();
                        setCurrentPanel(new HideNavigatorIconPanel(exercise));
                    }

                } else if (CURRENT_PANEL instanceof VariableIntervenablePanel) {
                    setCurrentPanel(new LimitResourcesPanel(exercise));
//                    NEXT_BUTTON.setEnabled(true);

                } else if (CURRENT_PANEL instanceof EssayPanel) {
                    setCurrentPanel(new VariableIntervenablePanel(exercise));
                    NEXT_BUTTON.setEnabled(true);
                }

            }
        });
    }


    /**
     * Set the getModel screen.
     */
    private void setCurrentPanel(JComponent component) {

        if (CURRENT_PANEL != null) {
            remove(CURRENT_PANEL);
        }

        //JPanel contentPane = new JPanel();
        //contentPane.setBackground(BACKGROUND_COLOR);
        //contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        //panel.setPreferredSize(new Dimension(200,200));
        CURRENT_PANEL = component;
        //contentPane.add(CURRENT_PANEL);
        //contentPane.add(BUTTON_PANEL);

        //setContentPane(contentPane);

        //getContentPane().add(CURRENT_PANEL, BorderLayout.CENTER);
        getContentPane().removeAll();
        getContentPane().add(CURRENT_PANEL, BorderLayout.PAGE_START);  //PAGE_START
        getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);
        getContentPane().add(BUTTON_PANEL, BorderLayout.PAGE_END);

        pack();
        setVisible(true);
        this.setVisible(true);
    }


    /*
        This is a getModel Panel layout for textboxes,
        allows a resize of the given textbox when the
        window size is increased.
     */
    private void setCurrentPanel2(JComponent component) {

        if (CURRENT_PANEL != null) {
            remove(CURRENT_PANEL);
        }

        CURRENT_PANEL = component;

        getContentPane().removeAll();
        getContentPane().add(CURRENT_PANEL, BorderLayout.CENTER);
        getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
        getContentPane().add(BUTTON_PANEL, BorderLayout.PAGE_END);

        pack();
        setVisible(true);
        this.setVisible(true);
    }


    private BayesPmEditor getBayesPmEditorPanel() {
        //if(bayesPm == null){bayesPm = new BayesPm(bayesGraph);}

        if (bayesPm == null) {
            bayesPm = new BayesPm(dagGraph);
        }

        //BayesPmEditor bpe = new BayesPmEditor(bayesPm);
        BayesPmEditor bpe = new BayesPmEditor(bayesPm);

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
//        if(bayesIm == null){
//            bayesIm = new MlBayesIm(bayesPm);
//        }
        //BayesImEditor bie = new BayesImEditor(bayesIm);

        if (bayesIm == null) {
//            bayesImWrapper = new BayesImWrapper(
//                    new BayesPmWrapper(bayesGraph, bayesPm, new BayesPmParams()),
//                    new BayesImParams()
//            );
            bayesIm = new MlBayesIm(bayesPm);
        }

        //return bie;

        return new BayesImEditor(bayesIm);
    }

    /**
     * Makes a panel to edit the dag graph
     *
     * @return the generic DagEditor.
     */
    private DagEditor getDagPanel() {

//            bayesPm = null;
//            bayesIm = null;
//
        if (dagGraph == null) {
            dagGraph = new Dag();
//                semPm = null;
//                semIm = null;
        }


//        GraphParams gp = new GraphParams();
//        gp.setInitializationMode(GraphParams.MANUAL);

        DagEditor de = new DagEditor(dagGraph);

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
        return de;
    }


    private SemImEditor getSemImEditorPanel() {
//        if(semIm == null){
//            //semIm =  new SemIm(semPm);
//            semImWrapper = new SemImWrapper(semPmWrapper);
//        }
        //return new SemImEditor(semIm);


        if (semPm == null) {
            semPm = new SemPm(dagGraph);

        }

        if (semIm == null) {
            semIm = new SemIm(semPm);
        }


        semIm.getSemPm().getGraph().setShowErrorTerms(true);
        //semIm.getSemPm().getGraph().s

        return new SemImEditor(semIm);
    }


}
