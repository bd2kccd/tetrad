package edu.cmu.causalityApp;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.EssayEditor.EssayEditor;
import edu.cmu.causalityApp.FeedbackEditor.FeedbackEditor;
import edu.cmu.causalityApp.commands.*;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.causalityApp.dataEditors.ExperimentalSetupFocusListener;
import edu.cmu.causalityApp.dataEditors.HypotheticalManipulatedGraphIconMaker;
import edu.cmu.causalityApp.dataEditors.experimentalSetup.ExperimentalSetupEditor;
import edu.cmu.causalityApp.dataEditors.independenciesEditor.IndependenciesEditor;
import edu.cmu.causalityApp.dataEditors.populationEditor.PopulationEditor;
import edu.cmu.causalityApp.dataEditors.sampleEditor.SampleEditor;
import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.causalityApp.exercise.ParserLabPanel;
import edu.cmu.causalityApp.exercise.WindowInclusionStatus;
import edu.cmu.causalityApp.finances.FinancesHistoryEditor;
import edu.cmu.causalityApp.graphEditors.CorrectGraphFocusListener;
import edu.cmu.causalityApp.graphEditors.HypotheticalGraphFocusListener;
import edu.cmu.causalityApp.graphEditors.correctGraph.CorrectGraphEditor;
import edu.cmu.causalityApp.graphEditors.correctManipulatedGraph.CorrectManipulatedGraphEditor;
import edu.cmu.causalityApp.graphEditors.hypotheticalGraph.HypotheticalGraphEditor;
import edu.cmu.causalityApp.graphEditors.hypotheticalManipulatedGraph.HypotheticalManipulatedGraphEditor;
import edu.cmu.causalityApp.instructions.InstructionsEditor;
import edu.cmu.causalityApp.navigator.Navigator;
import edu.cmu.causalityApp.navigator.NavigatorChangeEvent;
import edu.cmu.causalityApp.navigator.NavigatorChangeListener;
import edu.cmu.causalityApp.undo.HistoryInspectorPanel;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.sem.SemIm;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;

/**
 * This class describes the main workspace of the lab, consisting of the left
 * navigator panel and the right workbench area.
 *
 * @author Matthew Easterday
 */
public class CausalityLabPanel extends JSplitPane implements
        NavigatorChangeListener,
        HypotheticalManipulatedGraphIconMaker,
        ExperimentalSetupFocusListener,
        HypotheticalGraphFocusListener,
        CorrectGraphFocusListener,
        InternalFrameListener,
        ShowAnswerPanel,
        ParserLabPanel {

    private String CURRENT_HYPOTHETICAL_GRAPH_FOCUS = null;

    private boolean IS_HIDDEN = true;
    private JDesktopPane desktop;
    private Navigator navigator;
    private CausalityLabModel model;

    private InstructionsEditor instructionsEditor;
    private ExperimentalSetupEditor experimentalSetupEditor;
    private CorrectGraphEditor correctGraphEditor;
    private CorrectManipulatedGraphEditor correctManipulatedGraphEditor;
    private HypotheticalGraphEditor hypotheticalGraphEditor;
    private HypotheticalManipulatedGraphEditor hypotheticalManipulatedGraphEditor;
    private PopulationEditor populationEditor;
    private SampleEditor sampleEditor;
    private IndependenciesEditor independenciesEditor;
    private FinancesHistoryEditor financesHistoryEditor;
    private EssayEditor essayEditor;
    private FeedbackEditor feedbackEditor;
    private CausalityLab parent;

    private boolean showHistoryPane;

    private boolean isOpeningWindow = false;
    private String lastFocusedWindow = null;

    /**
     * Constructor.
     *
     * @param exercise      use null when no exercise has been loaded yet.
     * @param allowFeedback true if user can add feedback to exercise.
     */
    public CausalityLabPanel(CausalityLab parent, Exercise exercise, boolean allowFeedback) {
        super();

        if (parent == null) {
            throw new NullPointerException();
        }

        CausalityLabModel amodel;
        this.parent = parent;

        if (exercise == null) {
            WindowInclusionStatus disclude = WindowInclusionStatus.NOT_INCLUDED;
            setup(null, null, disclude, disclude, disclude, disclude, disclude, disclude, disclude, disclude, allowFeedback);
        } else {
            if (exercise.isUsingBayesIm()) {
                BayesIm im = exercise.getBayesModelIm();
                CausalityLabModel.initialize(im, exercise.getPrompt());

            } else {
                SemIm im = exercise.getSemModelIm();
                CausalityLabModel.initialize(im, exercise.getPrompt());

            }

            CausalityLabModel.getModel().initializeResourcesStatus(
                    exercise.getResourceTotal(),
                    exercise.getResourcePerObs(),
                    exercise.getResourcePerInt());

            amodel = CausalityLabModel.getModel();
            amodel.setStudentGuessEnabled(exercise.getIncludedGuess());
            amodel.setVariableIntervenableStatuses(exercise.getVariableIntervenableStatuses());

            setup(amodel,
                    exercise,
                    exercise.getWindowStatus(Exercise.CORRECT_GRAPH),
                    exercise.getWindowStatus(Exercise.EXPERIMENTAL_SETUP),
                    exercise.getWindowStatus(Exercise.HYPOTHETICAL_GRAPH),
                    exercise.getWindowStatus(Exercise.CORRECT_MANIPULATED_GRAPH),
                    exercise.getWindowStatus(Exercise.HYPOTHETICAL_MANIPULATED_GRAPH),
                    exercise.getWindowStatus(Exercise.POPULATION),
                    exercise.getWindowStatus(Exercise.SAMPLE),
                    exercise.getWindowStatus(Exercise.INDEPENDENCIES),
                    allowFeedback);

        }
    }

    /**
     * @return the singleton causality lab model.
     */
    public CausalityLabModel getModel() {
        return model;
    }

    /**
     * @param model                        May be null.
     * @param exercise                     may be null.
     */
    private void setup(CausalityLabModel model,
                       Exercise exercise,
                       WindowInclusionStatus correctGraphState,
                       WindowInclusionStatus experimentalSetupState,
                       WindowInclusionStatus hypotheticalGraphState,
                       WindowInclusionStatus correctManipulatedGraphState,
                       WindowInclusionStatus hypotheticalManipulatedGraphState,
                       WindowInclusionStatus populationState,
                       WindowInclusionStatus sampleState,
                       WindowInclusionStatus independenciesState,
                       boolean allowFeedback) {
        // Model and exercise may be null. CausalityLabPanel.<init>

        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        this.model = model;

        desktop = new JDesktopPane();

        navigator = new Navigator(correctGraphState,
                experimentalSetupState,
                hypotheticalGraphState,
                correctManipulatedGraphState,
                hypotheticalManipulatedGraphState,
                populationState,
                sampleState,
                independenciesState);
        if (model != null) {
            navigator.addNavigationListener(this);
        }

        //JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigator, desktop);
        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setRightComponent(getDesktop());
        setLeftComponent(navigator);
        setOneTouchExpandable(true);
        setContinuousLayout(true);


        addWindowsToLab(exercise, allowFeedback);
        if (exercise != null && exercise.getBuildNow()) {

            // todo: come back here again!~~~
            // zzzzz correctGraphState is null
            if (correctGraphState.equals(WindowInclusionStatus.HIDABLE)) {
                correctGraphEditor.setHidden(true);
            }
            if (correctManipulatedGraphState.equals(WindowInclusionStatus.HIDABLE)) {
                correctManipulatedGraphEditor.setHidden(true);
            }
            if (populationState.equals(WindowInclusionStatus.HIDABLE)) {
                populationEditor.setHidden(true);
                independenciesEditor.setHidden(true);
            } else if (populationState.equals(WindowInclusionStatus.NOT_INCLUDED)) {
                if (populationEditor != null) {
                    populationEditor.setHidden(true);
                }
                if (populationEditor != null) {
                    independenciesEditor.setHidden(true);
                }
            }
        }
    }

    /**
     * Adds all the editor frames on the lab and setups up their dimensions and
     * actions.
     */
    void addWindowsToLab(Exercise exercise, boolean allowFeedback) {
        if (model == null) {
            return;
        }
        if (exercise.getBuildNow()) {
            // if there are no true models at the start, do not have the instructionsEditor out
            instructionsEditor = new InstructionsEditor();
            correctManipulatedGraphEditor = new CorrectManipulatedGraphEditor(model, this, this.getDesktop());
            hypotheticalManipulatedGraphEditor = new HypotheticalManipulatedGraphEditor(model, this, this.getDesktop());
            experimentalSetupEditor = new ExperimentalSetupEditor(model, this, this, this.getDesktop());
            //changed here!!!!
            correctGraphEditor = new CorrectGraphEditor(model, this, this.getDesktop(), this);
            hypotheticalGraphEditor = new HypotheticalGraphEditor(model, this, this.getDesktop(), this);
            populationEditor = new PopulationEditor(model, this);
            sampleEditor = new SampleEditor(model, this, CausalityLab.isApplication());
            independenciesEditor = new IndependenciesEditor(model, this, this);
            financesHistoryEditor = new FinancesHistoryEditor(this);
            essayEditor = new EssayEditor(exercise);
            feedbackEditor = new FeedbackEditor(exercise);
            setupInternalFrame(essayEditor, 20, 20);
            setupInternalFrame(feedbackEditor, 20, 20);

            //setupInternalFrame(instructionsEditor, 20, 20, 350, 250);
            setupInternalFrame(instructionsEditor, 20, 20);
            setupInternalFrame(experimentalSetupEditor, 50, 50, 350, 350);
            setupInternalFrame(correctGraphEditor, 250, 50);
            setupInternalFrame(correctManipulatedGraphEditor, 250, 50);
            setupInternalFrame(hypotheticalGraphEditor, 450, 50, 350, 350);
            setupInternalFrame(hypotheticalManipulatedGraphEditor, 250, 350);
            setupInternalFrame(populationEditor, 50, 100);
            setupInternalFrame(sampleEditor, 150, 100, 550, 90);
            setupInternalFrame(independenciesEditor, 250, 200);
            setupInternalFrame(financesHistoryEditor, 60, 60, 600, 250);


            populationEditor.setExperimentalSetupFocusListener(this);
            sampleEditor.setExperimentalSetupFocusListener(this);
            independenciesEditor.setExperimentalSetupFocusListener(this);

            populationEditor.setCorrectGraphFocusListener(this);
            sampleEditor.setCorrectGraphFocusListener(this);
            independenciesEditor.setCorrectGraphFocusListener(this);

            instructionsEditor.setVisible(true);


            if (allowFeedback) {
                feedbackEditor.setTextFieldEnabled(true);
                feedbackEditor.enableScoreField();
            }
        } else {
            // if there are no true models built yet....
            correctGraphEditor = new CorrectGraphEditor(model, this, this.getDesktop(), this);
            setupInternalFrame(correctGraphEditor, 250, 50);
        }

    }


    /**
     * Call this method when you want to switch the navigator panel to the history
     * inspector, e.g. when the teacher opens an exercise with student work and
     * wants to replay the students actions.
     */
    public void switchToHistory() {
        showHistoryPane = true;
        setLeftComponent(new HistoryInspectorPanel());
    }


    /**
     * Call this method when you want to switch from the history panel to the navigator
     * panel, e.g. when the teacher has been viewing a students work and now wants to
     * do an exercise.
     */
    public void switchToNavigator() {
        showHistoryPane = false;
        setLeftComponent(navigator);
    }

    /**
     * Use this method to check if the getModel left pane is the history pane. Needed
     * to disable the submit function.
     *
     * @return if the history pane is shown.
     */
    public boolean isHistoryShown() {
        return showHistoryPane;
    }

    //private void initHypPos(int id) {hypPos.add(id, new HashMap());}

    /**
     * @return the editor with the corresponding name.
     */
    public AbstractEditor getEditor(String name) {
        if (name.equals(InstructionsEditor.MY_NAME)) {
            return instructionsEditor;
        } else if (name.equals(ExperimentalSetupEditor.MY_NAME)) {
            return experimentalSetupEditor;
        } else if (name.equals(HypotheticalGraphEditor.MY_NAME)) {
            return hypotheticalGraphEditor;
        } else if (name.equals(CorrectGraphEditor.MY_NAME) || name.equals(CorrectGraphEditor.OLD_NAME)) {
            return correctGraphEditor;
        } else if (name.equals(HypotheticalManipulatedGraphEditor.MY_NAME)) {
            return hypotheticalManipulatedGraphEditor;
        } else if (name.equals(CorrectManipulatedGraphEditor.MY_NAME)) {
            return correctManipulatedGraphEditor;
        } else if (name.equals(PopulationEditor.MY_NAME)) {
            return populationEditor;
        } else if (name.equals(SampleEditor.MY_NAME)) {
            return sampleEditor;
        } else if (name.equals(IndependenciesEditor.MY_NAME)) {
            return independenciesEditor;
        } else if (name.equals(FinancesHistoryEditor.MY_NAME)) {
            return financesHistoryEditor;
        } else if (name.equals(EssayEditor.MY_NAME)) {
            return essayEditor;   // newly added
        } else if (name.equals(FeedbackEditor.MY_NAME)) {
            return feedbackEditor;   // newly added

        } else {
            throw new NullPointerException("Unrecognized editor name: " + name);
        }
    }

    /**
     * When the user clicks on the navigator buttons, this method will run to
     * handle the request.
     */
    public void navigatorChanged(NavigatorChangeEvent e) {
        String command = e.getActionCommand();
        OpenWindowCommand nbc;

        // Navigator Button Commands
        if (command.equals(Navigator.EXP_SETUP)) {
            if (!experimentalSetupEditor.isVisible()) {
                nbc = new OpenWindowCommand(experimentalSetupEditor);
                nbc.doIt();
            } else {
                selectFrame(experimentalSetupEditor);
            }
        } else if (command.equals(Navigator.HYP_GRAPH)) {
            if (!hypotheticalGraphEditor.isVisible()) {
                nbc = new OpenWindowCommand(hypotheticalGraphEditor);
                nbc.doIt();
            } else {
                selectFrame(hypotheticalGraphEditor);
            }
        } else if (command.equals(Navigator.COR_GRAPH)) {
            if (!correctGraphEditor.isVisible()) {
                nbc = new OpenWindowCommand(correctGraphEditor);
                nbc.doIt();
            } else {
                selectFrame(correctGraphEditor);
            }
        } else if (command.equals(Navigator.HYP_MANIP)) {
            if (!hypotheticalManipulatedGraphEditor.isVisible()) {
                nbc = new OpenWindowCommand(hypotheticalManipulatedGraphEditor);
                nbc.doIt();
            } else {
                selectFrame(hypotheticalManipulatedGraphEditor);
            }
        } else if (command.equals(Navigator.COR_MANIP)) {
            if (!correctManipulatedGraphEditor.isVisible()) {
                nbc = new OpenWindowCommand(correctManipulatedGraphEditor);
                nbc.doIt();
            } else {
                selectFrame(correctManipulatedGraphEditor);
            }
        } else if (command.equals(Navigator.POP)) {
            if (!populationEditor.isVisible()) {
                nbc = new OpenWindowCommand(populationEditor);
                nbc.doIt();
            } else {
                selectFrame(populationEditor);
            }
        } else if (command.equals(Navigator.SAMPLE)) {
            if (!sampleEditor.isVisible()) {
                nbc = new OpenWindowCommand(sampleEditor);
                nbc.doIt();
            } else {
                selectFrame(sampleEditor);
            }
        } else if (command.equals(Navigator.COMPARED)) {
            if (!independenciesEditor.isVisible()) {
                nbc = new OpenWindowCommand(independenciesEditor);
                nbc.doIt();
            } else {
                selectFrame(independenciesEditor);
            }

            // ToolBar Button Commands
        } else if (command.equals(CausalityLabToolBar.INSTRUCT)) {
            if (!instructionsEditor.isVisible()) {
                nbc = new OpenWindowCommand(instructionsEditor);
                nbc.doIt();
            } else {
                selectFrame(instructionsEditor);
            }
        } else if (command.equals(CausalityLabToolBar.CHECK_ANSWER)) {
            //check the answer in the model
            boolean isSame;
            isSame = CURRENT_HYPOTHETICAL_GRAPH_FOCUS != null
                    && model.isHypotheticalGraphSameAsCorrectGraph(CURRENT_HYPOTHETICAL_GRAPH_FOCUS);

            String hypGraphName = (CURRENT_HYPOTHETICAL_GRAPH_FOCUS == null) ?
                    null : model.getHypotheticalGraphName(CURRENT_HYPOTHETICAL_GRAPH_FOCUS);

            if (isSame) {
                showAnswer();
            }

            CheckAnswerCommand cac = new CheckAnswerCommand(
                    this,
                    isSame,
                    hypGraphName,
                    this.getNumGuesses());
            cac.doIt();

        } else if ((command.equals(CausalityLabToolBar.SHOW_ANSWER))) {
            ShowAnswerCommand sac = new ShowAnswerCommand(this, IS_HIDDEN, model.getNumberOfGuesses());
            sac.doIt();

        } else if ((command.equals(CausalityLabToolBar.SAVE_EXERCISE))) {
            parent.saveExercise();

        } else if ((command.equals(CausalityLabToolBar.SUBMIT_EXERCISE))) {
            parent.submitExercise();

        } else if ((command.equals(CausalityLabToolBar.FINANCES))) {
            if (!financesHistoryEditor.isVisible()) {
                nbc = new OpenWindowCommand(financesHistoryEditor);
                nbc.doIt();
            } else {
                selectFrame(financesHistoryEditor);
            }
        } else if ((command.equals(CausalityLabToolBar.ESSAY))) {
            if (!essayEditor.isVisible()) {

                nbc = new OpenWindowCommand(essayEditor);
                nbc.doIt();
            } else {

                selectFrame(essayEditor);
            }
        } else if ((command.equals(CausalityLabToolBar.FEEDBACK))) {
            if (!feedbackEditor.isVisible()) {

                nbc = new OpenWindowCommand(feedbackEditor);
                nbc.doIt();
            } else {

                selectFrame(feedbackEditor);
            }
        }
    }

    private void selectFrame(AbstractEditor editor) {
        try {
            editor.setSelected(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //==============================================================================================
    //Show answer interface methods
    //==============================================================================================
    public JComponent asJComponent() {
        return this;
    }

    /**
     * Unhide all the editor frames.
     */
    public void showAnswer() {
        IS_HIDDEN = false;
        navigator.hideIcons(false);
        correctGraphEditor.setHidden(false);
        correctManipulatedGraphEditor.setHidden(false);
        populationEditor.setHidden(false);
        independenciesEditor.setHidden(false);
        IS_HIDDEN = false;
    }

    public void setAnswerMessage(String message) {
        JInternalFrame redoFrame = new JInternalFrame("Show Answer", false);
        JTextArea lazyText = new JTextArea(message);
        redoFrame.add(lazyText);
        redoFrame.add(new JLabel("Cannot show answer yet"));
        redoFrame.setVisible(true);
        getDesktop().add(redoFrame);

        try {
            redoFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            System.out.println("Cannot select the Show Answer window");
        }
    }

    /**
     * @return the number of guesses the student has made.
     */
    public int getNumGuesses() {
        return model.getNumberOfGuesses();
    }

    /**
     * Set the getModel experimental setup as the getModel active one and inform
     * all the experimental setup change listeners.
     */
    void setExperimentalSetupFocus(Object source, String experimentalSetupName) {
        experimentalSetupEditor.setExperimentalSetupFocus(source, experimentalSetupName);
        correctManipulatedGraphEditor.setExperimentalSetupFocus(experimentalSetupName);
        hypotheticalManipulatedGraphEditor.setExperimentalSetupFocus(experimentalSetupName);
        populationEditor.setExperimentalSetupFocus(experimentalSetupName);
        sampleEditor.setExperimentalSetupFocus(experimentalSetupName);
        independenciesEditor.setExperimentalSetupFocus(experimentalSetupName);
    }

    /**
     * Set the getModel hypothetical graph as the getModel active one and inform
     * all the experimental setup change listeners.
     */
    void setHypotheticalGraphFocus(String hypotheticalGraphName) {
        hypotheticalManipulatedGraphEditor.setHypotheticalGraphFocus(hypotheticalGraphName);
        independenciesEditor.setHypotheticalGraphFocus();
    }

    /**
     * Set the getModel correct graph as the getModel active one and inform
     * all the correct graph change listeners.
     */
    void setCorrectGraphFocus(String correctGraphName) {

        if (populationEditor != null)
            populationEditor.setCorrectGraphFocus(correctGraphName);
        if (sampleEditor != null)
            sampleEditor.setCorrectGraphFocus(correctGraphName);
        if (independenciesEditor != null)
            independenciesEditor.setCorrectGraphFocus(correctGraphName);
    }

    /**
     * When the hypothetical graph view has changed, change the focus to the
     * getModel hypothetical graph.
     */
    public void fireHgViewChangedEvent(JInternalFrame source, String hypName) {
        CURRENT_HYPOTHETICAL_GRAPH_FOCUS = hypName;
        //this method is causing  fireESViewChangedEvent to be called
        setHypotheticalGraphFocus(CURRENT_HYPOTHETICAL_GRAPH_FOCUS);
    }

    /**
     * When the correct graph view has changed, change the focus to the
     * getModel correct graph.
     */
    public void fireCgViewChangedEvent(JInternalFrame source, String correctGraphName) {
        setCorrectGraphFocus(correctGraphName);
    }


    /**
     * When the experimental setup view has changed, change the focus to the
     * getModel experimental setup.
     */
    public void fireESViewChangedEvent(JInternalFrame source, String expName) {
        setExperimentalSetupFocus(source, expName);
    }

    /**
     * @return the JDesktopPane.
     */
    public JDesktopPane getDesktop() {
        return desktop;
    }

    /**
     * @return a thumbnail icon of the hypothetical manipulated graph given the
     *         experimental id and hypothetical graph.
     */
    public ImageIcon getHypotheticalManipulatedGraphIcon(String expId, String hypName) {
        if (!model.isValidExperimentName(expId) || !model.isValidHypotheticalGraphName(hypName)) {
            return null;
        }
        return hypotheticalManipulatedGraphEditor.getThumbnail(expId, hypName, 0.4);
    }

    private void setupInternalFrame(JInternalFrame editor, int x, int y) {
        editor.setVisible(false);
        editor.pack();
        editor.setLocation(x, y);
        getDesktop().add(editor);
    }

    private void setupInternalFrame(JInternalFrame editor, int x, int y, int width, int height) {
        setupInternalFrame(editor, x, y);
        editor.setSize(width, height);
    }


    /**
     * Implements InternalFrameListener methods.
     */
    public void internalFrameOpened(InternalFrameEvent e) {

        // if user is opening window, flag this event so that the exercise history
        // does not log the focusing of the window
        isOpeningWindow = true;
    }

    public void internalFrameClosing(InternalFrameEvent e) {
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    /**
     * Logs the focusing of the getModel window, unless in the history viewing mode.
     */
    public void internalFrameActivated(InternalFrameEvent e) {
        if (!isHistoryShown()) {
            navigator.setActiveIcon(e.getInternalFrame().getTitle());

            if ((!isOpeningWindow) &&
                    (lastFocusedWindow != null) &&
                    (!lastFocusedWindow.equals(e.getInternalFrame().getTitle()))) {
                FocusWindowCommand fwc = new FocusWindowCommand(getEditor(e.getInternalFrame().getTitle()));
                fwc.doIt();
            }

            isOpeningWindow = false;
            lastFocusedWindow = e.getInternalFrame().getTitle();
        }
    }

    /**
     * Sell all the icons unactive when the getModel frame loses focus.
     */
    public void internalFrameDeactivated(InternalFrameEvent e) {
        if (!isHistoryShown()) {
            navigator.setAllIconsUnactive();
        }
    }

    /*
        new method to select the feedback editor and make it visible
        this is for the user to automatically see the feedback editor
        when he opens up the project, in both 'replay' mode or 'continue working'
        mode.
    */

    public void setVisibleEditor() {
        try {
            feedbackEditor.setVisible(true);
        } catch (Exception ignored) {
        }
    }
}