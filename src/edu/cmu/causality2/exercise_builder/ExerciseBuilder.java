package edu.cmu.causality2.exercise_builder;

import edu.cmu.causality2.model.exercise.WorkedOnExercise;
import edu.cmu.causality2.model.exercise.Finances;
import edu.cmu.causality2.model.exercise.Exercise;
import edu.cmu.causality2.model.exercise.WindowInclusions;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.IM;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.PM;
import edu.cmu.tetradapp.editor.*;
import edu.cmu.tetradapp.model.EditorUtils;
import edu.cmu.tetradapp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This is the wrapper frame for the different dialogs in the Exercise wizard.
 *
 * @author matt and adrian
 * @author Joseph Ramsey
 */
public class ExerciseBuilder extends JFrame {
    private JPanel contentPanel;

    // Pieces of the interface that have to be accessed globally.
    private JComponent currentPanel;
    private JToolBar buttonPanel;
    private JButton previousButton;
    private JButton nextButton;
    private JButton saveButton;

    // Most of the fields below are used to construct an Exercise (and by extension, an WorkedOnExercise).
    private ModelType modelType = ModelType.BAYES;

    private Dag dag = null;
    private PM pm = null;
    private IM im;

    private String instructions = "";

    private boolean resourcesLimited = false;
    private int resourceTotal = Finances.DEFAULT_RESOURCE_TOTAL;
    private int resourcePerObs = Finances.DEFAULT_RESOURCE_OBS;
    private int resourcePerInt = Finances.DEFAULT_RESOURCE_INT;
    private Set<String> variablesIntervenable = new HashSet<String>();

    private WindowInclusions.Status trueGraph = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status manipulatedTrueGraph = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status population = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status experimentalSetup = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status sample = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status hypotheticalGraph = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status manipulatedHypotheticalGraph = new WindowInclusions.Status(true, false);
    private WindowInclusions.Status predictionsAndResults = new WindowInclusions.Status(true, false);

    private String essayQuestion = "";

    // Set to false if any change is made. Tracks whether the current version has been saved. Set to false when
    // a change has been made to the exercise.
    private boolean saved = true;

    public static void main(String... args) {
        final ExerciseBuilder builder = new ExerciseBuilder();
        builder.pack();
        builder.setVisible(true);
    }

    /**
     * Use this constructor when editing an exercise.
     */
    public ExerciseBuilder() {
        super("Make an Exercise");

        buttonPanel = new JToolBar();
        buttonPanel.setFloatable(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 14));

        previousButton = new JButton("< Back");
        nextButton = new JButton("Next >");
        JButton exitButton = new JButton("Exit");
        addNextButtonListener();
        addPreviousButtonListener();

        saveButton = new JButton("Save");

        // The save button does something complicated. If it's saving from the opt-out slide, and if no
        // advanced settings have been made, then it just saves. If advanced settings have been made, it
        // first asks whether he user wants to save the advanced settings with the exercise. If so, it just
        // saves. Otherwise, it substitutes default values for all of the advanced settings and saves.
        // If it's saving from the final slide, it just saves. Phew! Hope that does it! jdramsey 9/27/2013
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IM im = getIm();

                String instructions = getInstructions();

                boolean resourcesLimited = isResourcesLimited();
                int resourceTotal = getResourceTotal();
                int resourcePerObs = getResourcePerObs();
                int resourcePerInt = getResourcePerInt();
                Set<String> variablesIntervenable = getVariablesIntervenable();

                WindowInclusions.Status trueGraph = getTrueGraph();
                WindowInclusions.Status manipulatedTrueGraph = getManipulatedTrueGraph();
                WindowInclusions.Status population = getPopulation();
                WindowInclusions.Status experimentalSetup = getExperimentalSetup();
                WindowInclusions.Status sample = getSample();
                WindowInclusions.Status hypotheticalGraph = getHypotheticalGraph();
                WindowInclusions.Status manipulatedHypotheticalGraph = getManipulatedHypotheticalGraph();
                WindowInclusions.Status predictionsAndResults = getPredictionsAndResults();

                String essayQuestion = getEssayQuestion();

                if (currentPanel instanceof OptionalInstructionsPanel) {
                    if (!advancedSettingsAtDefault()) {
                        int ret = JOptionPane.showConfirmDialog(contentPanel,
                                "Some advanced settings have been made. Would you like to save these with the problem?",
                                "", JOptionPane.YES_NO_OPTION);

                        if (ret == JOptionPane.NO_OPTION) {
                            resourcesLimited = false;
                            resourceTotal = Finances.DEFAULT_RESOURCE_TOTAL;
                            resourcePerObs = Finances.DEFAULT_RESOURCE_OBS;
                            resourcePerInt = Finances.DEFAULT_RESOURCE_INT;
                            variablesIntervenable = new HashSet<String>();

                            trueGraph = new WindowInclusions.Status(true, false);
                            manipulatedTrueGraph = new WindowInclusions.Status(true, false);
                            population = new WindowInclusions.Status(true, false);
                            experimentalSetup = new WindowInclusions.Status(true, false);
                            sample = new WindowInclusions.Status(true, false);
                            hypotheticalGraph = new WindowInclusions.Status(true, false);
                            manipulatedHypotheticalGraph = new WindowInclusions.Status(true, false);
                            predictionsAndResults = new WindowInclusions.Status(true, false);

                            essayQuestion = "";
                        }
                    }
                }

                WindowInclusions windowInclusions = new WindowInclusions(
                        trueGraph,
                        manipulatedTrueGraph,
                        population,
                        experimentalSetup,
                        sample,
                        hypotheticalGraph,
                        manipulatedHypotheticalGraph,
                        predictionsAndResults
                );

                Exercise exercise = new Exercise(im, windowInclusions, instructions, resourcesLimited, resourceTotal,
                        resourcePerObs, resourcePerInt, variablesIntervenable, essayQuestion);

//                WorkedOnExercise woe = new WorkedOnExercise(exercise);

                // Select the file to save this to.
                File file = EditorUtils.getSaveFile("exercise", "exc",
                        JOptionUtils.centeringComp(), false, "Save Session As...");

                if (file == null) {
                    return;
                }

                // Save it.
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    ObjectOutputStream objOut = new ObjectOutputStream(out);
                    objOut.writeObject(exercise);
                    out.close();
                    saved = true;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "An error occurred while attempting to save the session.");
                }
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!saved) {
                    int ret = JOptionPane.showConfirmDialog(contentPanel, "The exercise has not been saved. Exit anyway?",
                            "", JOptionPane.YES_NO_OPTION);

                    if (ret == JOptionPane.YES_OPTION) {
                        setVisible(false);
                        dispose();
                    } else {
                        return;
                    }
                }

                setVisible(false);
                dispose();
            }
        });

        JButton loadButton = new JButton("Load Saved Exercise");

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // select a file to open using the file chooser
                JFileChooser chooser = new JFileChooser();
                String sessionSaveLocation = Preferences.userRoot().get("fileSaveLocation", "");
                chooser.setCurrentDirectory(new File(sessionSaveLocation));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int ret1 = chooser.showOpenDialog(JOptionUtils.centeringComp());

                if (!(ret1 == JFileChooser.APPROVE_OPTION)) {
                    return;
                }

                final File file = chooser.getSelectedFile();

                if (file == null) {
                    return;
                }

                Preferences.userRoot().put("fileSaveLocation", file.getParent());

                try {
                    FileInputStream in = new FileInputStream(file);
                    ObjectInputStream objIn = new ObjectInputStream(in);
                    Object o = objIn.readObject();

                    if (o instanceof Exercise || o instanceof WorkedOnExercise) {
                        Exercise exercise;

                        if (o instanceof Exercise) {
                            exercise = (Exercise) o;
                        } else if (o instanceof WorkedOnExercise) {
                            int ret = JOptionPane.showConfirmDialog(contentPanel,
                                    "Just a note: if you load a Worked On Exercise (\".woe\"), only the Exercise from it will be edited;\n" +
                                            "student work will be disgarded. Continue?", "", JOptionPane.YES_NO_OPTION);

                            if (ret != JOptionPane.YES_OPTION) {
                                return;
                            }

                            exercise = ((WorkedOnExercise) o).getExercise();
                        } else {
                            throw new IllegalStateException();
                        }

                        if (exercise.getIm() instanceof BayesIm) {
                            modelType = ModelType.BAYES;
                            im = exercise.getIm();
                            pm = ((BayesIm) im).getBayesPm();
                            dag = ((BayesPm) getPm()).getDag();
                            saved = false;
                        }

                        if (exercise.getIm() instanceof SemIm) {
                            modelType = ModelType.SEM;
                            im = exercise.getIm();
                            pm = ((SemIm) im).getSemPm();

                            SemGraph graph = ((SemPm) getPm()).getGraph();
                            graph.setShowErrorTerms(false);

                            dag = new Dag(graph);
                            saved = false;
                        }

                        WindowInclusions windowInclusions = exercise.getWindowInclusions();

                        trueGraph = windowInclusions.getTrueGraph();
                        manipulatedTrueGraph = windowInclusions.getManipulatedTrueGraph();
                        population = windowInclusions.getPopulation();
                        experimentalSetup = windowInclusions.getExperimentalSetup();
                        sample = windowInclusions.getSample();
                        hypotheticalGraph = windowInclusions.getHypotheticalGraph();
                        manipulatedHypotheticalGraph = windowInclusions.getManipulatedHypotheticalGraph();
                        predictionsAndResults = windowInclusions.getPredictionsAndResults();

                        instructions = exercise.getInstructions();

                        resourceTotal = exercise.getResourceTotal();
                        resourcePerObs = exercise.getResourcePerObs();
                        resourcePerInt = exercise.getResourcePerInt();

                        variablesIntervenable = new HashSet<String>(variablesIntervenable);

                        essayQuestion = exercise.getEssayQuestion();

                        setCurrentPanel(getDagPanel());
                        nextButton.setEnabled(true);
                        previousButton.setEnabled(false);
                        saveButton.setEnabled(false);

                        saved = true;
                    } else {
                        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), "That wasn't an exercise.");
                        return;
                    }

                    in.close();
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "That wasn't a TETRAD session file: " + file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "An error occurred attempting to load the session.");
                }
            }
        });

        nextButton.setEnabled(true);
        previousButton.setEnabled(false);
        saveButton.setEnabled(false);

        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(previousButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(nextButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(exitButton);

        exitButton.setPreferredSize(new Dimension(70, 26));
        previousButton.setPreferredSize(new Dimension(70, 26));
        saveButton.setPreferredSize(new Dimension(70, 26));
        nextButton.setPreferredSize(new Dimension(70, 26));

        buttonPanel.setPreferredSize(buttonPanel.getMinimumSize());
        buttonPanel.setMaximumSize(buttonPanel.getMaximumSize());
        buttonPanel.setMinimumSize(buttonPanel.getMinimumSize());

        contentPanel = new JPanel();

        contentPanel.setPreferredSize(new Dimension(900, 500));

        contentPanel.setLayout(new BorderLayout());

        Box box = Box.createVerticalBox();
        box.add(contentPanel);
        box.add(Box.createVerticalGlue());
        box.add(buttonPanel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(box, BorderLayout.CENTER);

        setCurrentPanel(getDagPanel());

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (size.width - contentPanel.getPreferredSize().width) / 2;
        int y = (size.height - contentPanel.getPreferredSize().height) / 2;
        setLocation(new Point(x, y));
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
        saved = false;
    }

    public Dag getDag() {
        return dag;
    }

    public PM getPm() {
        return pm;
    }

    public IM getIm() {
        return im;
    }

    public String getInstructions() {
        return instructions;
    }

    public boolean isResourcesLimited() {
        return resourcesLimited;
    }

    public int getResourceTotal() {
        return resourceTotal;
    }

    public int getResourcePerObs() {
        return resourcePerObs;
    }

    public int getResourcePerInt() {
        return resourcePerInt;
    }

    public Set<String> getVariablesIntervenable() {
        return variablesIntervenable;
    }

    public WindowInclusions.Status getTrueGraph() {
        return trueGraph;
    }

    public WindowInclusions.Status getManipulatedTrueGraph() {
        return manipulatedTrueGraph;
    }

    public WindowInclusions.Status getPopulation() {
        return population;
    }

    public WindowInclusions.Status getExperimentalSetup() {
        return experimentalSetup;
    }

    public WindowInclusions.Status getSample() {
        return sample;
    }

    public WindowInclusions.Status getHypotheticalGraph() {
        return hypotheticalGraph;
    }

    public WindowInclusions.Status getManipulatedHypotheticalGraph() {
        return manipulatedHypotheticalGraph;
    }

    public WindowInclusions.Status getPredictionsAndResults() {
        return predictionsAndResults;
    }

    public String getEssayQuestion() {
        return essayQuestion;
    }

    public static enum ModelType {
        BAYES, SEM
    }

    /**
     * For each slides it the wizard, this determines the next slide to be displayed.
     */
    private void addNextButtonListener() {
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPanel instanceof DagEditor) {
                    if (dag != null && dag.getNumNodes() < 1) {
                        JOptionPane.showMessageDialog(ExerciseBuilder.this, "You need to add at least 1 variable");
                    } else {
                        setCurrentPanel(new ChooseBayesOrSemPanel());
                        previousButton.setEnabled(true);
                    }
                } else if (currentPanel instanceof ChooseBayesOrSemPanel) {
                    if (getModelType() == ModelType.BAYES) {
                        setCurrentPanel(getBayesPmEditorPanel());
                    } else if (getModelType() == ModelType.SEM) {
                        SemImEditor imEditor = getSemImEditorPanel();
                        setCurrentPanel(imEditor);
                        imEditor.setEditIntercepts(true);
                    }

                } else if (currentPanel instanceof BayesPmEditor) {
                    setCurrentPanel(getBayesImEditorPanel());
                } else if (currentPanel instanceof BayesImEditor) {
                    for (int i = 0; i < ((BayesIm) im).getNumNodes(); i++) {
                        if (((BayesIm) im).isIncomplete(i)) {
                            JOptionPane.showMessageDialog(ExerciseBuilder.this, "You need to fill out all the values for variable" + " " + ((BayesIm) im).getNode(i).getName());
                            return;
                        }
                    }

                    setCurrentPanel(new InstructionsPanel());
                } else if (currentPanel instanceof SemImEditor) {
                    setCurrentPanel(new InstructionsPanel());
                } else if (currentPanel instanceof InstructionsPanel) {
                    if (instructions.equals("")) {
                        JOptionPane.showMessageDialog(ExerciseBuilder.this, "Need to type instructions");
                    } else {
                        setCurrentPanel(new OptionalInstructionsPanel());
                        saveButton.setEnabled(true);
                    }
                } else if (currentPanel instanceof OptionalInstructionsPanel) {
                    setCurrentPanel(new NavigatorIconPanel());
                    saveButton.setEnabled(false);
                } else if (currentPanel instanceof NavigatorIconPanel) {
                    setCurrentPanel(new HideNavigatorIconPanel());
                } else if (currentPanel instanceof HideNavigatorIconPanel) {
                    setCurrentPanel(new LimitResourcesPanel());
                } else if (currentPanel instanceof LimitResourcesPanel) {
                    if (((LimitResourcesPanel) currentPanel).validateFields()) {
                        ((LimitResourcesPanel) currentPanel).applyChanges();
                        setCurrentPanel(new VariableIntervenablePanel());
                    }
                } else if (currentPanel instanceof VariableIntervenablePanel) {
                    setCurrentPanel(new EssayPanel());
                    nextButton.setEnabled(false);
                    saveButton.setEnabled(true);
                }
            }
        });
    }

    /**
     * For each slide in the wizard, this determines the previous slide to be displayed.
     */
    private void addPreviousButtonListener() {
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPanel instanceof ChooseBayesOrSemPanel) {
                    setCurrentPanel(getDagPanel());
                    previousButton.setEnabled(false);
                    saveButton.setEnabled(false);
                } else if (currentPanel instanceof BayesPmEditor) {
                    setCurrentPanel(new ChooseBayesOrSemPanel());
                } else if (currentPanel instanceof BayesImEditor) {
                    setCurrentPanel(getBayesPmEditorPanel());
                } else if (currentPanel instanceof SemGraphEditor) {
                    setCurrentPanel(new ChooseBayesOrSemPanel());
                } else if (currentPanel instanceof SemImEditor) {
                    setCurrentPanel(new ChooseBayesOrSemPanel());
                } else if (currentPanel instanceof InstructionsPanel) {
                    if (im instanceof BayesIm) {
                        setCurrentPanel(getBayesImEditorPanel());
                    } else if (im instanceof SemIm) {
                        setCurrentPanel(getSemImEditorPanel());
                    }
                } else if (currentPanel instanceof OptionalInstructionsPanel) {
                    setCurrentPanel(new InstructionsPanel());
                    saveButton.setEnabled(false);
                } else if (currentPanel instanceof NavigatorIconPanel) {
                    setCurrentPanel(new OptionalInstructionsPanel());
                    saveButton.setEnabled(true);
                } else if (currentPanel instanceof HideNavigatorIconPanel) {
                    setCurrentPanel(new NavigatorIconPanel());
                } else if (currentPanel instanceof LimitResourcesPanel) {
                    if (((LimitResourcesPanel) currentPanel).validateFields()) {
                        ((LimitResourcesPanel) currentPanel).applyChanges();
                        setCurrentPanel(new HideNavigatorIconPanel());
                    }
                } else if (currentPanel instanceof VariableIntervenablePanel) {
                    setCurrentPanel(new LimitResourcesPanel());
                } else if (currentPanel instanceof EssayPanel) {
                    setCurrentPanel(new VariableIntervenablePanel());
                    nextButton.setEnabled(true);
                    saveButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * Returns true just in case all advanced settings are at default. (Make sure you update this
     * if you add more advanced settings!)
     */
    private boolean advancedSettingsAtDefault() {
        return
                !resourcesLimited &&
                        resourceTotal == Finances.DEFAULT_RESOURCE_TOTAL &&
                        resourcePerObs == Finances.DEFAULT_RESOURCE_OBS &&
                        resourcePerInt == Finances.DEFAULT_RESOURCE_INT &&
                        variablesIntervenable.equals(new HashSet<String>()) &&

                        trueGraph == new WindowInclusions.Status(true, false) &&
                        manipulatedTrueGraph == new WindowInclusions.Status(true, false) &&
                        population == new WindowInclusions.Status(true, false) &&
                        experimentalSetup == new WindowInclusions.Status(true, false) &&
                        sample == new WindowInclusions.Status(true, false) &&
                        hypotheticalGraph == new WindowInclusions.Status(true, false) &&
                        manipulatedHypotheticalGraph == new WindowInclusions.Status(true, false) &&
                        predictionsAndResults == new WindowInclusions.Status(true, false) &&

                        "".equals(essayQuestion);
    }

    /**
     * Updates the slide in the wizard.
     */
    private void setCurrentPanel(JComponent component) {
        currentPanel = component;

        getContentPane().removeAll();

        contentPanel.removeAll();

        Box c = Box.createVerticalBox();
        c.add(Box.createVerticalGlue());
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(component);
        b.add(Box.createHorizontalGlue());
        c.add(b);
        c.add(Box.createVerticalGlue());

        contentPanel.add(c, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();

        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().validate();
        getContentPane().repaint();

    }


    private BayesPmEditor getBayesPmEditorPanel() {
        if (getPm() instanceof SemPm) {
            pm = null;
            im = null;
            saved = false;
        }

        if (getPm() == null) {
            pm = new BayesPm(dag);
            saved = false;
        }

        BayesPmEditor bpe = new BayesPmEditor((BayesPm) getPm());

        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {
                    im = null;
                    saveButton.setEnabled(false);
                    saved = false;
                }
            }
        };

        bpe.addPropertyChangeListener(pcl);
        return bpe;
    }


    private BayesImEditor getBayesImEditorPanel() {
        if (im == null) {
            im = new MlBayesIm((BayesPm) getPm());
            saved = false;
        }

        return new BayesImEditor((BayesIm) im);
    }

    /**
     * Makes a panel to edit the dag dag
     *
     * @return the generic DagEditor.
     */
    private DagEditor getDagPanel() {
        if (dag == null) {
            dag = new Dag();
            saved = false;
        }

        final DagEditor de = new DagEditor(dag);

        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {
                    dag = new Dag(de.getGraph());
                    pm = null;
                    im = null;
                    saveButton.setEnabled(false);
                    saved = false;
                }
            }
        };
        de.addPropertyChangeListener(pcl);
        return de;
    }

    private SemImEditor getSemImEditorPanel() {
        if (getPm() instanceof BayesPm) {
            pm = null;
            im = null;
            saved = false;
        }

        if (getPm() == null) {
            pm = new SemPm(dag);
            saved = false;
        }

        if (im == null) {
            im = new SemIm((SemPm) getPm());
            saved = false;
        }

        ((SemIm) im).getSemPm().getGraph().setShowErrorTerms(true);
        return new SemImEditor((SemIm) im);

    }

    private class ChooseBayesOrSemPanel extends JPanel {
        public ChooseBayesOrSemPanel() {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JLabel typeLabel1 = new JLabel("Please specify the type of parametric statistical model you want to use to " +
                    "interpret the causal graph.");

            typeLabel1.setMaximumSize(typeLabel1.getPreferredSize());

            JLabel typeLabel2 = new JLabel("Parametric Model:");
            Font regularFont = typeLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
            Font boldFont = typeLabel2.getFont().deriveFont(Font.BOLD, 12.0f);

            typeLabel1.setFont(regularFont);
            typeLabel2.setFont(boldFont);

            JPanel comboPanel = new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));
            ButtonGroup group = new ButtonGroup();

            JRadioButton bayesButton = new JRadioButton("Bayes Net (categorical variables)");
            JRadioButton semButton = new JRadioButton("Structural Equation Model (continuous variables / linear functions)");
            makeComboButton(bayesButton, modelType, group, comboPanel);
            makeComboButton(semButton, modelType, group, comboPanel);
            bayesButton.setFont(regularFont);
            semButton.setFont(regularFont);

            if (im != null) {
                if (im instanceof BayesIm) {
                    bayesButton.setSelected(true);
                } else if (im instanceof SemIm) {
                    semButton.setSelected(true);
                }
            }

            add(typeLabel1);
            add(Box.createVerticalStrut(10));
            add(typeLabel2);
            add(comboPanel);
            add(Box.createVerticalGlue());
        }


        private void makeComboButton(JRadioButton button, ModelType modelType, ButtonGroup group, JPanel panel) {
            button.setActionCommand(button.getName());
            button.setSelected(modelType == ModelType.BAYES);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (ae.getActionCommand().equals("Bayes Net (categorical variables)")) {
                        setModelType(ModelType.BAYES);
                    } else if (ae.getActionCommand().equals("Structural Equation Model (continuous variables / linear functions)")) {
                        setModelType(ModelType.SEM);
                    }
                }
            });
            group.add(button);
            panel.add(button);
        }
    }

    private class InstructionsPanel extends JPanel implements KeyListener {

        private final JTextArea promptArea;

        /**
         * Constructor. Creates the panel.
         */
        public InstructionsPanel() {
            super();

            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JLabel promptLabel1 = new JLabel("Please specify the instructions in this exercise.");
            JLabel promptLabel2 = new JLabel("This is what the user will see when they click on the instructions button.");
            JLabel promptLabel3 = new JLabel("Instructions:");
            Font regularFont = promptLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
            Font boldFont = promptLabel3.getFont().deriveFont(Font.BOLD, 12.0f);

            promptLabel1.setFont(regularFont);
            promptLabel2.setFont(regularFont);
            promptLabel3.setFont(boldFont);

            promptArea = new JTextArea(10, 30);         // was 5, 30
            if (instructions != null) {
                promptArea.setText(instructions.replaceAll("0x0a", "\n"));
            }

            promptArea.setLineWrap(true);
            promptArea.setWrapStyleWord(true);
            promptArea.setMinimumSize(promptArea.getPreferredSize());
            promptArea.setMaximumSize(promptArea.getPreferredSize());


            promptArea.addKeyListener(this);
            JScrollPane scrollPane =
                    new JScrollPane(promptArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);


            add(promptLabel1);
            add(promptLabel2);
            add(Box.createVerticalStrut(10));
            add(promptLabel3);
            add(scrollPane);

        }

        /**
         * Method for KeyListener. Not used by this panel.
         */
        public void keyTyped(KeyEvent ke) {
        }

        /**
         * Method for KeyListener. Not used by this panel.
         */
        public void keyPressed(KeyEvent ke) {
        }

        /**
         * This action is needed to convert the linefeeds in the instructions to
         * readable and storable representation.
         */
        public void keyReleased(KeyEvent ke) {
            // convert all linefeeds to "0x0a" chars
            instructions = promptArea.getText().replaceAll("\n", "0x0a");
            saved = false;
        }
    }

    private class NavigatorIconPanel extends JPanel {
        private static final String TRUE_GRAPH = "True graph";
        private static final String MANIPULATED_TRUE_GRAPH = "True manipulated graph";
        private static final String POPULATION = "Population";
        private static final String EXPERIMENTAL_SETUP = "Experimental Setup";
        private static final String SAMPLE = "Sample";
        private static final String HYPOTHETICAL_GRAPH = "Hypothetical graph";
        private static final String MANIPULATED_HYPOTHETICAL_GRAPH = "Manipulated hypothetical  graph";
        private static final String PREDICTIONS_AND_RESULTS = "Predictions and Results";

        //for the navigator icon panel
        private ToggleIcon trueGraphIcon;
        private ToggleIcon manipulatedTrueGraphIcon;
        private ToggleIcon populationIcon;
        private ToggleIcon experimentalSetupIcon;
        private ToggleIcon sampleIcon;
        private ToggleIcon hypotheticalGraphIcon;
        private ToggleIcon manipulatedHypothecalGraphIcon;
        private ToggleIcon predictionsAndResultsIcon;

        private JCheckBox CORRECT_GRAPH_BOX;
        private JCheckBox EXPERIMENTAL_SETUP_BOX;
        private JCheckBox HYPOTHETICAL_GRAPH_BOX;

        private NavigatorActionListener navigatorActionListener;

        /**
         * Constructor. Creates the panel.
         */
        public NavigatorIconPanel() {
            super();

            navigatorActionListener = new NavigatorActionListener();
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel comboPanel = new JPanel();
            JPanel iconPanel = new JPanel();
            JPanel iconContainerPanel = new JPanel();

            ////////////////
            // Set up checkbox combo panel
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));

            CORRECT_GRAPH_BOX = makeCheckBox(TRUE_GRAPH, isWindowInLab(trueGraph), comboPanel);

            EXPERIMENTAL_SETUP_BOX = makeCheckBox(EXPERIMENTAL_SETUP, isWindowInLab(experimentalSetup), comboPanel);
            HYPOTHETICAL_GRAPH_BOX = makeCheckBox(HYPOTHETICAL_GRAPH, isWindowInLab(hypotheticalGraph), comboPanel);
            makeCheckBox(MANIPULATED_TRUE_GRAPH, isWindowInLab(manipulatedTrueGraph), comboPanel);
            makeCheckBox(MANIPULATED_HYPOTHETICAL_GRAPH, isWindowInLab(manipulatedHypotheticalGraph), comboPanel);
            makeCheckBox(POPULATION, isWindowInLab(population), comboPanel);
            makeCheckBox(SAMPLE, isWindowInLab(sample), comboPanel);
            makeCheckBox(PREDICTIONS_AND_RESULTS, isWindowInLab(predictionsAndResults), comboPanel);
            comboPanel.add(Box.createVerticalStrut(120));

            ////////////////
            // Set up icon preview panel
            ImageIcon blank = new ImageIcon(ImageUtils.getImage(this, "cl/blank.gif"));
            trueGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/true_graph.gif")), blank);
            manipulatedTrueGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/true_manip_graph.gif")), blank);
            populationIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/population.gif")), blank);
            experimentalSetupIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/expt_setup.gif")), blank);
            sampleIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/sample.gif")), blank);
            hypotheticalGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/hyp_graph.gif")), blank);
            manipulatedHypothecalGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/hyp_manip_graph.gif")), blank);
            predictionsAndResultsIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/predict.gif")), blank);

            if (!isWindowInLab(trueGraph)) {
                trueGraphIcon.toggle();
            }
            if (!isWindowInLab(experimentalSetup)) {
                experimentalSetupIcon.toggle();
            }
            if (!isWindowInLab(hypotheticalGraph)) {
                hypotheticalGraphIcon.toggle();
            }
            if (!isWindowInLab(manipulatedTrueGraph)) {
                manipulatedTrueGraphIcon.toggle();
            }
            if (!isWindowInLab(manipulatedHypotheticalGraph)) {
                manipulatedHypothecalGraphIcon.toggle();
            }
            if (!isWindowInLab(population)) {
                populationIcon.toggle();
            }
            if (!isWindowInLab(sample)) {
                sampleIcon.toggle();
            }
            if (!isWindowInLab(predictionsAndResults)) {
                predictionsAndResultsIcon.toggle();
            }

            iconPanel.setLayout(new GridLayout(4, 3));
            iconPanel.add(trueGraphIcon);
            iconPanel.add(experimentalSetupIcon);
            iconPanel.add(hypotheticalGraphIcon);
            iconPanel.add(manipulatedTrueGraphIcon);
            iconPanel.add(new ToggleIcon(blank, blank));
            iconPanel.add(manipulatedHypothecalGraphIcon);
            iconPanel.add(populationIcon);
            iconPanel.add(sampleIcon);
            iconPanel.add(new ToggleIcon(blank, blank));
            iconPanel.add(new ToggleIcon(blank, blank));
            iconPanel.add(predictionsAndResultsIcon);
            iconPanel.add(new ToggleIcon(blank, blank));

            iconContainerPanel.setBackground(Color.white);
            iconContainerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            iconContainerPanel.add(iconPanel);
            JPanel iconContainerPanel2 = new JPanel();
            iconContainerPanel2.add(iconContainerPanel);
            iconContainerPanel2.setBorder(BorderFactory.createEtchedBorder());

            ////////////////
            // Set up instructions
            JLabel promptLabel1 = new JLabel("Please select which window(s) the student will see in the Navigational Panel.");
            JLabel promptLabel3 = new JLabel("Preview of Navigational Panel:");
            JLabel promptLabel4 = new JLabel("Windows to Display:");

//            JTextArea promptLabel2a = new JTextArea("Windows that are necessary for the users are grayed out and you are not allowed to delete them from the Navigational Panel.");   //$NON-NLS-3$
            JTextArea promptLabel2a = new JTextArea("Some of the windows in the Navigational Panel may be shown but stamped as \"Hidden\"; " +
                    "the user will see these windows but will not be able to open them. Please select which windows should be hidden.");
            promptLabel2a.setLineWrap(true);
            promptLabel2a.setWrapStyleWord(true);
            promptLabel2a.setEditable(false);
            promptLabel2a.setBackground(getBackground());

            promptLabel2a.setMaximumSize(promptLabel2a.getPreferredSize());

            Font regularFont = promptLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
            Font boldFont = promptLabel3.getFont().deriveFont(Font.BOLD, 12.0f);
            promptLabel1.setFont(regularFont);
            promptLabel2a.setFont(regularFont);
            promptLabel3.setFont(boldFont);
            promptLabel4.setFont(boldFont);

            JScrollPane promptLabel2 = new JScrollPane(promptLabel2a);
            promptLabel2.setPreferredSize(new Dimension(450, 50));
            promptLabel2.setBorder(BorderFactory.createEmptyBorder());

            ////////////////
            // Add individual components and layout
            add(promptLabel1);
            add(promptLabel2);
            add(promptLabel3);
            add(promptLabel4);
            add(comboPanel);
            add(iconContainerPanel2);

            SpringLayout layout = new SpringLayout();
            setLayout(layout);

            SpringLayout.Constraints promptLabel1Cons = layout.getConstraints(promptLabel1);
            promptLabel1Cons.setX(Spring.constant(0));
            promptLabel1Cons.setY(Spring.constant(0));

            SpringLayout.Constraints promptLabel2Cons = layout.getConstraints(promptLabel2);
            promptLabel2Cons.setX(Spring.constant(0));
            promptLabel2Cons.setY(Spring.sum(Spring.constant(3), promptLabel1Cons.getConstraint(SpringLayout.SOUTH)));

            SpringLayout.Constraints promptLabel4Cons = layout.getConstraints(promptLabel4);
            promptLabel4Cons.setX(Spring.constant(0));
            promptLabel4Cons.setY(Spring.sum(Spring.constant(15), promptLabel2Cons.getConstraint(SpringLayout.SOUTH)));

            SpringLayout.Constraints comboPanelCons = layout.getConstraints(comboPanel);
            comboPanelCons.setX(Spring.constant(0));
            comboPanelCons.setY(Spring.sum(Spring.constant(5), promptLabel4Cons.getConstraint(SpringLayout.SOUTH)));

            SpringLayout.Constraints promptLabel3Cons = layout.getConstraints(promptLabel3);
            promptLabel3Cons.setX(Spring.sum(Spring.constant(100), promptLabel4Cons.getConstraint(SpringLayout.EAST)));
            promptLabel3Cons.setY(Spring.sum(Spring.constant(15), promptLabel2Cons.getConstraint(SpringLayout.SOUTH)));

            SpringLayout.Constraints iconContainerPanel2Cons = layout.getConstraints(iconContainerPanel2);
            iconContainerPanel2Cons.setX(Spring.sum(Spring.constant(100), promptLabel4Cons.getConstraint(SpringLayout.EAST)));
            iconContainerPanel2Cons.setY(Spring.sum(Spring.constant(5), promptLabel3Cons.getConstraint(SpringLayout.SOUTH)));

            setPreferredSize(new Dimension(600, 600));
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());

            navigatorActionListener.setNavigatorIconPanel(this);
        }

        private boolean isWindowInLab(WindowInclusions.Status experimentalSetup) {
            return experimentalSetup.isIncluded();
        }

        private JCheckBox makeCheckBox(String name, boolean isSelected, JPanel panel) {
            JCheckBox box = new JCheckBox(name);
            box.setActionCommand(name);
            box.setSelected(isSelected);
            box.addActionListener(navigatorActionListener);

            Font regularFont = box.getFont().deriveFont(Font.PLAIN, 12.0f);
            box.setFont(regularFont);

            panel.add(box);

            return box;
        }

    }

    /**
     * This describes the toggle icons used by the icons of the different navigator
     * buttons.
     *
     * @author mattheweasterday
     */
    private class ToggleIcon extends JLabel {
        private boolean showIcon = true;
        private final ImageIcon image;
        private final ImageIcon blankImage;

        /**
         * Constructor.
         *
         * @param image      the navigator button icon.
         * @param blankImage a blank icon to show when a navigator button is not
         *                   included in the exercise.
         */
        public ToggleIcon(ImageIcon image, ImageIcon blankImage) {
            super(image);
            this.image = image;
            this.blankImage = blankImage;
        }

        /**
         * Toggle between the actual icon and the blank icon.
         */
        public void toggle() {
            showIcon = !showIcon;
            this.setIcon(showIcon ? image : blankImage);
        }

        /**
         * @return if this icon is shown or not.
         */
        public boolean isShown() {
            return showIcon;
        }
    }

    /**
     * This is the controller. This is separated out from the view
     * for better unit-testing.
     *
     * @author axshahab
     */
    private class NavigatorActionListener implements ActionListener {
        private NavigatorIconPanel navigatorIconPanel;
        private int selectedCheckboxes;

        public NavigatorActionListener() {
        }

        /**
         * @param navigatorIconPanel the navigatorIconPanel to set
         */
        public void setNavigatorIconPanel(NavigatorIconPanel navigatorIconPanel) {
            this.navigatorIconPanel = navigatorIconPanel;
            selectedCheckboxes = 0;

            //set up the model
            if (navigatorIconPanel.CORRECT_GRAPH_BOX.isSelected()) {
                selectedCheckboxes++;
            }
            if (navigatorIconPanel.EXPERIMENTAL_SETUP_BOX.isSelected()) {
                selectedCheckboxes++;
            }
            if (navigatorIconPanel.HYPOTHETICAL_GRAPH_BOX.isSelected()) {
                selectedCheckboxes++;
            }
        }

        /**
         * Defines the action performed when the navigator button checkboxes are
         * checked.
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(NavigatorIconPanel.TRUE_GRAPH)) {
                if (handleGraphSelection(navigatorIconPanel.CORRECT_GRAPH_BOX)) {
                    navigatorIconPanel.trueGraphIcon.toggle();
                    trueGraph = getStatus(navigatorIconPanel.trueGraphIcon);
                }

            } else if (e.getActionCommand().equals(NavigatorIconPanel.EXPERIMENTAL_SETUP)) {
                if (handleGraphSelection(navigatorIconPanel.EXPERIMENTAL_SETUP_BOX)) {
                    navigatorIconPanel.experimentalSetupIcon.toggle();
                    trueGraph = getStatus(navigatorIconPanel.experimentalSetupIcon);
                }
            } else if (e.getActionCommand().equals(NavigatorIconPanel.HYPOTHETICAL_GRAPH)) {
                if (handleGraphSelection(navigatorIconPanel.HYPOTHETICAL_GRAPH_BOX)) {
                    navigatorIconPanel.hypotheticalGraphIcon.toggle();
                    hypotheticalGraph = getStatus(navigatorIconPanel.hypotheticalGraphIcon);
                }
            } else if (e.getActionCommand().equals(NavigatorIconPanel.MANIPULATED_TRUE_GRAPH)) {
                navigatorIconPanel.manipulatedTrueGraphIcon.toggle();
                manipulatedTrueGraph = getStatus(navigatorIconPanel.manipulatedTrueGraphIcon);
            } else if (e.getActionCommand().equals(NavigatorIconPanel.MANIPULATED_HYPOTHETICAL_GRAPH)) {
                navigatorIconPanel.manipulatedHypothecalGraphIcon.toggle();
                manipulatedHypotheticalGraph = getStatus(navigatorIconPanel.manipulatedHypothecalGraphIcon);
            } else if (e.getActionCommand().equals(NavigatorIconPanel.POPULATION)) {
                navigatorIconPanel.populationIcon.toggle();
                population = getStatus(navigatorIconPanel.populationIcon);
            } else if (e.getActionCommand().equals(NavigatorIconPanel.SAMPLE)) {
                navigatorIconPanel.sampleIcon.toggle();
                manipulatedTrueGraph = getStatus(navigatorIconPanel.manipulatedTrueGraphIcon);
            } else if (e.getActionCommand().equals(NavigatorIconPanel.PREDICTIONS_AND_RESULTS)) {
                navigatorIconPanel.predictionsAndResultsIcon.toggle();
                predictionsAndResults = getStatus(navigatorIconPanel.predictionsAndResultsIcon);
            }

            saved = false;
        }

        private boolean handleGraphSelection(JCheckBox checkBox) {
            selectedCheckboxes = (checkBox.isSelected()) ? selectedCheckboxes + 1 : selectedCheckboxes - 1;
            if (selectedCheckboxes <= 0) {
                selectedCheckboxes++;
                checkBox.setSelected(true);
                JOptionPane.showMessageDialog(navigatorIconPanel,
                        "You must select at least one between the true graph, experimental graph, and hypothetical graph.");
                return false;
            }
            return true;
        }

        private WindowInclusions.Status getStatus(ToggleIcon icon) {
            return icon.isShown() ? new WindowInclusions.Status(true, false) : new WindowInclusions.Status(false, false);
        }
    }

    /**
     * This panel allows user to specify if a given variable in the experiment can
     * be intervened upon.
     *
     * @author jangace
     */
    private class VariableIntervenablePanel extends JPanel {

        /**
         * Constructor. Creates the panel.
         */
        public VariableIntervenablePanel() {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JTextArea instructions = new JTextArea(
                    "Please select the variables that students may intervene upon in this exercise.");
            instructions.setLineWrap(true);
            instructions.setWrapStyleWord(true);
            instructions.setEditable(false);
            instructions.setBackground(getBackground());
            instructions.setColumns(50);

            instructions.setMaximumSize(instructions.getPreferredSize());
            instructions.setMinimumSize(instructions.getPreferredSize());

            JPanel intervene_labels = new JPanel();
            intervene_labels.setLayout(new BoxLayout(intervene_labels, BoxLayout.PAGE_AXIS));
            intervene_labels.add(new JLabel("Student can"));
            intervene_labels.add(new JLabel("intervene upon"));

            JPanel options = new JPanel();
            options.setLayout(new GridLayout(0, 2));
            options.add(new JLabel(" "));
            options.add(intervene_labels);

            List<String> varNames = getMeasuredVariableNames();
            JCheckBox[] checkboxes = createCheckboxes(varNames);
            for (int i = 0; i < varNames.size(); i++) {
                boolean b = variablesIntervenable.contains(varNames.get(i));
                checkboxes[i].setSelected(b);

                options.add(new JLabel(varNames.get(i)));
                options.add(checkboxes[i]);
            }

            options.setMaximumSize(options.getPreferredSize());

            Box b1 = Box.createHorizontalBox();
            b1.add(Box.createHorizontalGlue());
            b1.add(instructions);
            b1.add(Box.createHorizontalGlue());
            add(b1);
            add(Box.createVerticalStrut(10));
            Box b2 = Box.createHorizontalBox();
            b2.add(Box.createHorizontalGlue());
            b2.add(options);
            b2.add(Box.createHorizontalGlue());
            add(b2);
            add(Box.createVerticalStrut(10));
            add(Box.createVerticalGlue());
        }

        private List<String> getMeasuredVariableNames() {
            List<Node> nodes = dag.getNodes();
            List<String> measuredVariableNames = new ArrayList<String>();

            for (Node node : nodes) {
                if (node.getNodeType() == NodeType.MEASURED) {
                    measuredVariableNames.add(node.getName());
                }
            }

            return measuredVariableNames;
        }

        /**
         * Given a string array of variable names in the exercise, create a set of
         * corresponding checkboxes to represent their intervenable status.
         *
         * @param varNames string array of variable names
         * @return an array of JCheckBoxes with index corresponding to the string
         *         array of variable names
         */
        private JCheckBox[] createCheckboxes(List<String> varNames) {
            JCheckBox[] checkboxes = new JCheckBox[varNames.size()];

            for (int i = 0; i < varNames.size(); i++) {
                checkboxes[i] = new JCheckBox();
                checkboxes[i].setSelected(variablesIntervenable.contains(varNames.get(i)));
                checkboxes[i].addActionListener(new CheckBoxListener(varNames.get(i), checkboxes[i]));
            }

            return checkboxes;
        }

        /**                                                                                                       wi
         * ***********************************************************************
         * Private Class action listener for the checkBoxes for conditional
         * variables, to control the enabling and disabling of the combo boxes
         */
        private class CheckBoxListener implements ActionListener {
            private final String varName;
            private final JCheckBox checkbox;

            public CheckBoxListener(String name, JCheckBox checkbox) {
                varName = name;
                this.checkbox = checkbox;
            }

            public void actionPerformed(ActionEvent e) {
                if (variablesIntervenable.contains(varName)) {
                    variablesIntervenable.remove(varName);
                } else {
                    variablesIntervenable.add(varName);
                }

//                variablesIntervenable.add(varName);
                saved = false;
            }
        }
    }

    /**
     * This class describes the panel which allows user to select which navigator
     * buttons to hide.
     *
     * @author mattheweasterday
     */
    private class HideNavigatorIconPanel extends JPanel implements ActionListener {

        /**
         * "Hide true graph".
         */
        private static final String CORRECT_GRAPH_HIDDEN = "Hide true graph";

        /**
         * "Hide true manipulated graph".
         */
        private static final String CORRECT_MANIPULATED_GRAPH_HIDDEN = "Hide true manipulated graph";

        /**
         * "Hide Population".
         */
        private static final String POPULATION_HIDDEN = "Hide Population";

        // for the hide navigator panel
        private ToggleIcon hiddenTrueGraphIcon;
        private ToggleIcon hiddenTrueManipulatedGraphIcon;
        private ToggleIcon hiddenPopulationIcon;

        /**
         * Constructor. Creates the panel.
         */
        public HideNavigatorIconPanel() {
            super();
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel comboPanel = new JPanel();
            JPanel iconPanel = new JPanel();
            JPanel iconContainerPanel = new JPanel();

            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));

            if (trueGraph.isIncluded()) {
                makeCheckBox(CORRECT_GRAPH_HIDDEN, trueGraph.isHidden(), comboPanel);
            }
            if (manipulatedTrueGraph.isIncluded()) {
                makeCheckBox(CORRECT_MANIPULATED_GRAPH_HIDDEN, manipulatedTrueGraph.isHidden(), comboPanel);
            }
            if (population.isIncluded()) {
                makeCheckBox(POPULATION_HIDDEN, population.isHidden(), comboPanel);
            }

            ImageIcon blank = new ImageIcon(ImageUtils.getImage(this, "cl/blank.gif"));

            ToggleIcon experimentalSetupIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/expt_setup.gif")), blank);
            ToggleIcon sampleIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/sample.gif")), blank);
            ToggleIcon hypotheticalGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/hyp_graph.gif")), blank);
            ToggleIcon manipulatedHypotheticalGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/hyp_manip_graph.gif")), blank);
            ToggleIcon predictionsAndResultsIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/predict.gif")), blank);
            hiddenTrueGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/true_graph.gif")), new ImageIcon(ImageUtils.getImage(this, "cl/true_graph_hidden.gif")));
            hiddenTrueManipulatedGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/true_manip_graph.gif")), new ImageIcon(ImageUtils.getImage(this, "cl/true_manip_graph_hidden.gif")));
            hiddenPopulationIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "cl/population.gif")), new ImageIcon(ImageUtils.getImage(this, "cl/population_hidden.gif")));

            if (!experimentalSetup.isIncluded()) {
                experimentalSetupIcon.toggle();
            }
            if (!sample.isIncluded()) {
                sampleIcon.toggle();
            }
            if (!hypotheticalGraph.isIncluded()) {
                hypotheticalGraphIcon.toggle();
            }
            if (!hypotheticalGraph.isIncluded()) {
                manipulatedHypotheticalGraphIcon.toggle();
            }
            if (!predictionsAndResults.isIncluded()) {
                predictionsAndResultsIcon.toggle();
            }
            if (!trueGraph.isIncluded()) {
                hiddenTrueGraphIcon = new ToggleIcon(blank, blank);
            }
            if (!manipulatedTrueGraph.isIncluded()) {
                hiddenTrueManipulatedGraphIcon = new ToggleIcon(blank, blank);
            }
            if (!population.isIncluded()) {
                hiddenPopulationIcon = new ToggleIcon(blank, blank);
            }
            if (trueGraph.isHidden()) {
                hiddenTrueGraphIcon.toggle();
            }
            if (manipulatedTrueGraph.isHidden()) {
                hiddenTrueManipulatedGraphIcon.toggle();
            }
            if (population.isHidden()) {
                hiddenPopulationIcon.toggle();
            }

            iconPanel.setLayout(new GridLayout(4, 3));
            iconPanel.add(hiddenTrueGraphIcon);
            iconPanel.add(experimentalSetupIcon);
            iconPanel.add(hypotheticalGraphIcon);
            iconPanel.add(hiddenTrueManipulatedGraphIcon);
            iconPanel.add(new ToggleIcon(blank, blank));
            iconPanel.add(manipulatedHypotheticalGraphIcon);
            iconPanel.add(hiddenPopulationIcon);
            iconPanel.add(sampleIcon);
            iconPanel.add(new ToggleIcon(blank, blank));
            iconPanel.add(new ToggleIcon(blank, blank));
            iconPanel.add(predictionsAndResultsIcon);
            iconPanel.add(new ToggleIcon(blank, blank));

            iconContainerPanel.setBackground(Color.white);
            iconContainerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            iconContainerPanel.add(iconPanel);
            JPanel iconContainerPanel2 = new JPanel();
            iconContainerPanel2.add(iconContainerPanel);
            iconContainerPanel2.setBorder(BorderFactory.createEtchedBorder());

//            JTextArea promptLabel1a = new JTextArea("Please select which window(s) the student will see in the Navigational Panel.");   //$NON-NLS-3$
            JTextArea promptLabel1a = new JTextArea("Some of the windows in the Navigational Panel may be shown but stamped as \"Hidden\"; " +
                    "the user will see these windows but will not be able to open them. Please select which windows should be hidden.");
            promptLabel1a.setLineWrap(true);
            promptLabel1a.setWrapStyleWord(true);
            promptLabel1a.setEditable(false);
            promptLabel1a.setBackground(getBackground());

            promptLabel1a.setMaximumSize(promptLabel1a.getPreferredSize());

            JLabel promptLabel2 = new JLabel("Preview of Navigational Panel:");   //$NON-NLS-3$

            Font regularFont = promptLabel1a.getFont().deriveFont(Font.PLAIN, 12.0f);
            Font boldFont = promptLabel2.getFont().deriveFont(Font.BOLD, 12.0f);
            promptLabel1a.setFont(regularFont);
            promptLabel2.setFont(boldFont);

            JScrollPane promptLabel1 = new JScrollPane(promptLabel1a);
            promptLabel1.setPreferredSize(new Dimension(500, 60));
            promptLabel1.setBorder(BorderFactory.createEmptyBorder());

            add(promptLabel1);
            add(promptLabel2);
            add(comboPanel);
            add(iconContainerPanel2);

            SpringLayout layout = new SpringLayout();
            setLayout(layout);

            SpringLayout.Constraints promptLabel1Cons = layout.getConstraints(promptLabel1);
            promptLabel1Cons.setX(Spring.constant(0));
            promptLabel1Cons.setY(Spring.constant(0));

            SpringLayout.Constraints comboPanelCons = layout.getConstraints(comboPanel);
            comboPanelCons.setX(Spring.constant(0));
            comboPanelCons.setY(Spring.sum(Spring.constant(20), promptLabel1Cons.getConstraint(SpringLayout.SOUTH)));

            SpringLayout.Constraints promptLabel2Cons = layout.getConstraints(promptLabel2);
            promptLabel2Cons.setX(Spring.sum(Spring.constant(50), comboPanelCons.getConstraint(SpringLayout.EAST)));
            promptLabel2Cons.setY(Spring.sum(Spring.constant(5), promptLabel1Cons.getConstraint(SpringLayout.SOUTH)));

            SpringLayout.Constraints iconContainerPanel2Cons = layout.getConstraints(iconContainerPanel2);
            iconContainerPanel2Cons.setX(Spring.sum(Spring.constant(50), comboPanelCons.getConstraint(SpringLayout.EAST)));
            iconContainerPanel2Cons.setY(Spring.sum(Spring.constant(5), promptLabel2Cons.getConstraint(SpringLayout.SOUTH)));

            setPreferredSize(new Dimension(600, 600));
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());
        }

        /**
         * Defines the actions performed when the checkboxes to hide the correct
         * graph, correct manipulated graph or the population are checked.
         */
        public void actionPerformed(ActionEvent e) {
            WindowInclusions.Status status;

            if (e.getActionCommand().equals(CORRECT_GRAPH_HIDDEN)) {
                status = trueGraph;
                if (!status.isIncluded()) {

                } else {
                    hiddenTrueGraphIcon.toggle();
                    if (hiddenTrueGraphIcon.isShown()) {
                        trueGraph = new WindowInclusions.Status(true, false);
                    } else {
                        trueGraph = new WindowInclusions.Status(true, true);
                    }
                }

            } else if (e.getActionCommand().equals(CORRECT_MANIPULATED_GRAPH_HIDDEN)) {
                status = trueGraph;
                if (!status.isIncluded()) {

                } else {
                    hiddenTrueManipulatedGraphIcon.toggle();
                    if (hiddenTrueManipulatedGraphIcon.isShown()) {
                        manipulatedTrueGraph = new WindowInclusions.Status(true, false);
                    } else {
                        manipulatedTrueGraph = new WindowInclusions.Status(true, true);
                    }
                }

            } else if (e.getActionCommand().equals(POPULATION_HIDDEN)) {
                status = population;
                if (!status.isIncluded()) {

                } else {
                    hiddenPopulationIcon.toggle();
                    if (hiddenPopulationIcon.isShown()) {
                        population = new WindowInclusions.Status(true, false);
                    } else {
                        population = new WindowInclusions.Status(true, true);
                    }
                }
            }

            saved = false;
        }

        private void makeCheckBox(String name, boolean isSelected, JPanel panel) {
            JCheckBox box = new JCheckBox(name);
            box.setActionCommand(name);
            box.setSelected(isSelected);
            box.addActionListener(this);

            Font regularFont = box.getFont().deriveFont(Font.PLAIN, 12.0f);
            box.setFont(regularFont);

            panel.add(box);
        }
    }

    /**
     * This panel allows user to limit the resources for a student in an exercise,
     * and to specify the expected expenditure of each experiment.
     *
     * @author jangace Date: Oct 22, 2005 Time: 10:24:55 PM
     */
    private class LimitResourcesPanel extends JPanel implements ActionListener {
        private final static String LIMIT_RESOURCES = "Limit resources";
        private final JCheckBox checkbox;
        private final JTextField txt_total_money;
        private final JTextField txt_cost_per_obs;
        private final JTextField txt_cost_per_int;

        /**
         * Constructor. Creates the panel.       f
         */
        public LimitResourcesPanel() {
            super();

            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JTextArea instructions = new JTextArea(
                    "Please specify if you want to limit the resources that the student has to work with. You can " +
                            "indicate the total amount of money the student has in an exercise to collect sample data " +
                            "for experiments.");
            instructions.setLineWrap(true);
            instructions.setWrapStyleWord(true);
            instructions.setEditable(false);
            instructions.setBackground(getBackground());
            instructions.setColumns(60);

            instructions.setMaximumSize(instructions.getPreferredSize());

            JLabel label_total_money = new JLabel("Total Money:");
            JLabel label_cost_per_obs = new JLabel(
                    "Cost per subject (Observation):");
            JLabel label_cost_per_int = new JLabel(
                    "Cost per subject (Intervention):");
            JPanel options = new JPanel();

            checkbox = new JCheckBox(LIMIT_RESOURCES);
            checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            checkbox.setActionCommand(LIMIT_RESOURCES);
            checkbox.addActionListener(this);

            checkbox.setSelected(resourcesLimited);

            txt_total_money = new JTextField(9);
            txt_cost_per_obs = new JTextField(9);
            txt_cost_per_int = new JTextField(9);

            options.setLayout(new GridLayout(3, 2));
            options.add(label_total_money);
            options.add(getTextFieldWithDollarSign(txt_total_money));
            options.add(label_cost_per_obs);
            options.add(getTextFieldWithDollarSign(txt_cost_per_obs));
            options.add(label_cost_per_int);
            options.add(getTextFieldWithDollarSign(txt_cost_per_int));

            options.setMaximumSize(options.getPreferredSize());

            add(instructions);
            add(Box.createVerticalStrut(10));
            add(checkbox);
            add(options);
            add(Box.createVerticalStrut(10));
            add(Box.createVerticalGlue());

            populateFields();

            setPreferredSize(getPreferredSize());
        }

        private JPanel getTextFieldWithDollarSign(JTextField txtfield) {
            JPanel panel = new JPanel();

            panel.add(new JLabel("$"));
            panel.add(txtfield);

            return panel;
        }

        /**
         * Controls the action of the 'Limit Resources' checkbox.
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(LIMIT_RESOURCES)) {
                toggleTextFields();
                applyChanges();
            }
        }

        private void toggleTextFields() {
            if (checkbox.isSelected()) {
                txt_total_money.setEnabled(true);
                txt_cost_per_obs.setEnabled(true);
                txt_cost_per_int.setEnabled(true);
                txt_total_money.setBackground(Color.white);
                txt_cost_per_obs.setBackground(Color.white);
                txt_cost_per_int.setBackground(Color.white);
            } else {
                txt_total_money.setEnabled(false);
                txt_cost_per_obs.setEnabled(false);
                txt_cost_per_int.setEnabled(false);
                txt_total_money.setBackground(Color.LIGHT_GRAY);
                txt_cost_per_obs.setBackground(Color.LIGHT_GRAY);
                txt_cost_per_int.setBackground(Color.LIGHT_GRAY);
            }
        }

        private void populateFields() {
            txt_total_money.setText(Integer.toString(resourceTotal));
            txt_cost_per_obs.setText(Integer.toString(resourcePerObs));
            txt_cost_per_int.setText(Integer.toString(resourcePerInt));

            toggleTextFields();
        }

        /**
         * Checks that all the textfields contain valid numbers
         *
         * @return true if so, or if the checkbox is not checked
         */
        public boolean validateFields() {
            if (checkbox.isEnabled()) {
                return true;
            } else {
                return true;
            }
        }

        /**
         * Apply the values in this form into the exercise. This method is done
         * after the fields are validated.
         */
        public void applyChanges() {
            resourceTotal = new Integer(txt_total_money.getText());
            resourcePerObs = new Integer(txt_cost_per_obs.getText());
            resourcePerInt = new Integer(txt_cost_per_int.getText());

            resourcesLimited = checkbox.isSelected();

            saved = false;
        }
    }

    /**
     * This panel is to instruct the user that he has the option to save the exercise
     * as all the required information has been given.
     *
     * @author Adrian Tang
     */
    private class OptionalInstructionsPanel extends JPanel {

        /**
         * Constructor. Creates the panel.
         */
        public OptionalInstructionsPanel() {
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JLabel instructions1 = new JLabel("<html>You have specified all the necessary information to create an exercise.<br>" +
                    "You may click 'Save' to save this exercise.</html>");
//            instructions1.setLineWrap(true);
//            instructions1.setWrapStyleWord(true);
//            instructions1.setEditable(false);
//            instructions1.setBackground(getBackground());
//            instructions1.setColumns(50);

//            instructions1.setMaximumSize(instructions1.getPreferredSize());

            JLabel instructions2 = new JLabel("<html>There are additional advanced features that you can configure in the<br>" +
                    "exercise. You may click 'Next' to access these features:</html>");
//            instructions2.setLineWrap(true);
//            instructions2.setWrapStyleWord(true);
//            instructions2.setEditable(false);
//            instructions2.setBackground(getBackground());
//            instructions2.setColumns(50);

            instructions2.setMaximumSize(instructions2.getPreferredSize());

            JLabel heading = new JLabel("Optional Advanced Features:");
//            heading.setBackground(getBackground());
//            Font regularFont = heading.getFont().deriveFont(Font.BOLD, 12.0f);
//            heading.setFont(regularFont);

//            heading.setMaximumSize(heading.getPreferredSize());

            JLabel instructions3 = new JLabel("<html>      - show / hide windows<br>" +
                    "      - limit resources<br>" +
                    "      - change experimental setup capabilities</html");   //$NON-NLS-3$
//            instructions3.setLineWrap(true);
//            instructions3.setWrapStyleWord(true);
//            instructions3.setEditable(false);
//            instructions3.setBackground(getBackground());
//            instructions3.setColumns(60);

//            instructions3.setMaximumSize(instructions3.getPreferredSize());

            Box b = Box.createVerticalBox();

            Box b1 = Box.createHorizontalBox();
            b1.add(instructions1);
            b1.add(Box.createHorizontalGlue());
            b.add(b1);
            b.add(Box.createVerticalStrut(10));
            Box b2 = Box.createHorizontalBox();
            b2.add(instructions2);
            b2.add(Box.createHorizontalGlue());
            b.add(b2);
            b.add(Box.createVerticalStrut(10));
            Box b3 = Box.createHorizontalBox();
            b3.add(heading);
            b3.add(Box.createHorizontalGlue());
            b.add(b3);
            Box b4 = Box.createHorizontalBox();
            b4.add(instructions3);
            b4.add(Box.createHorizontalGlue());
            b.add(b4);
//            add(Box.createVerticalStrut(15));
            b.add(Box.createVerticalGlue());

            setMaximumSize(new Dimension(400, 165));
//
//            b.setMaximumSize(b.getMinimumSize());

            setLayout(new BorderLayout());
            add(b, BorderLayout.CENTER);
        }
    }

    /**
     * This class describes the panel for users to enter instructions into the exercise
     * This is accessed from the 'make new exercise' option
     *
     * @author mattheweasterday
     */
    private class EssayPanel extends JPanel implements KeyListener {

        private final JTextArea promptArea;

        /**
         * Constructor. Creates the panel.
         */
        public EssayPanel() {
            super();

            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JLabel promptLabel1 = new JLabel("Please specify the essay question you wish the student to answer.");
            JLabel promptLabel2 = new JLabel("The student's answer will be stored for you to view after they submit.");
            JLabel promptLabel3 = new JLabel("Essay question");

            Font regularFont = promptLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
            Font boldFont = promptLabel3.getFont().deriveFont(Font.BOLD, 12.0f);

            promptLabel1.setFont(regularFont);
            promptLabel2.setFont(regularFont);
            promptLabel3.setFont(boldFont);

            promptArea = new JTextArea(10, 10);


            if (essayQuestion != null) {
                promptArea.setText(essayQuestion.replaceAll("\n", "0x0a"));
            }

            promptArea.setLineWrap(true);
            promptArea.setWrapStyleWord(true);
            promptArea.setMinimumSize(promptArea.getPreferredSize());
            promptArea.setMaximumSize(promptArea.getPreferredSize());
            promptArea.addKeyListener(this);

            JScrollPane scrollPane =
                    new JScrollPane(promptArea,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(promptLabel1);
            add(promptLabel2);
            add(Box.createVerticalStrut(10));
            add(promptLabel3);
            add(scrollPane);

        }

        /**
         * Method for KeyListener. Not used by this panel.
         */
        public void keyTyped(KeyEvent ke) {
        }

        /**
         * Method for KeyListener. Not used by this panel.
         */
        public void keyPressed(KeyEvent ke) {
        }

        /**
         * This action is needed to convert the linefeeds in the instructions to
         * readable and storable representation.
         */
        public void keyReleased(KeyEvent ke) {

            // convert all linefeeds to "0x0a" chars
            essayQuestion = (promptArea.getText().replaceAll("\n", "0x0a"));
            saved = false;
        }
    }
}
