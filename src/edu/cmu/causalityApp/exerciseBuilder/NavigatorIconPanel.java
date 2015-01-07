package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.causalityApp.exercise.WindowInclusionStatus;
import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;

/**
 * This class describes the panel which allows the user to select which navigator
 * buttons to include for the exercise.
 *
 * @author mattheweasterday
 */
public class NavigatorIconPanel extends JPanel {

    /**
     * "True graph".
     */
    public static final String CORRECT_GRAPH = "True graph";

    /**
     * "True manipulated graph".
     */
    public static final String CORRECT_MANIPULATED_GRAPH = "True manipulated graph";

    /**
     * "Population".
     */
    public static final String POPULATION = "Population";

    /**
     * "Experimental setup".
     */
    public static final String EXPERIMENTAL_SETUP = "Experimental Setup";

    /**
     * "Sample".
     */
    public static final String SAMPLE = "Sample";

    /**
     * "Hypothetical graph".
     */
    public static final String HYPOTHETICAL_GRAPH = "Hypothetical graph";

    /**
     * "Hypothetical manipulated graph".
     */
    public static final String HYPOTHETICAL_MANIPULATED_GRAPH = "Hypothetical manipulated graph";

    /**
     * "Independencies".
     */
    public static final String INDEPENDENCIES = "Predictions and Results";

    /**
     * "Include student's guesses".
     */
    public static final String INCLUDE_STUDENT_GUESSES = "Include student guesses";

    //for the navigator icon panel
    final ToggleIcon correctGraphIcon;
    final ToggleIcon correctManipulatedGraphIcon;
    final ToggleIcon populationIcon;
    final ToggleIcon experimentalSetupIcon;
    final ToggleIcon sampleIcon;
    final ToggleIcon hypotheticalGraphIcon;
    final ToggleIcon hypotheticalManipulatedGraphIcon;
    final ToggleIcon independenciesIcon;

    final JCheckBox
            CORRECT_GRAPH_BOX;
    final JCheckBox EXPERIMENTAL_SETUP_BOX;
    final JCheckBox HYPOTHETICAL_GRAPH_BOX;
    final JCheckBox STUDENT_GUESS_BOX;

    private final NavigatorActionListener navigatorActionListener;

    /**
     * Constructor. Creates the panel.
     */
    public NavigatorIconPanel(Exercise exercise) {
        super();

        navigatorActionListener = new NavigatorActionListener(exercise);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (exercise.getCorrectGraphInclusion() == null) {
            exercise.setCorrectGraphInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getExperimentalSetupInclusion() == null) {
            exercise.setExperimentalSetupInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getHypotheticalGraphInclusion() == null) {
            exercise.setHypotheticalGraphInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getCorrectManipulatedGraphInclusion() == null) {
            exercise.setCorrectManipulatedGraphInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getHypotheticalManipulatedGraphInclusion() == null) {
            exercise.setHypotheticalManipulatedGraphInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getPopulationInclusion() == null) {
            exercise.setPopulationInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getSampleInclusion() == null) {
            exercise.setSampleInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }
        if (exercise.getIndependenciesInclusion() == null) {
            exercise.setIndependenciesInclusion(WindowInclusionStatus.NOT_HIDABLE);
        }

        JPanel comboPanel = new JPanel();
        JPanel iconPanel = new JPanel();
        JPanel iconContainerPanel = new JPanel();


        ////////////////
        // Set up checkbox combo panel
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));

        CORRECT_GRAPH_BOX = makeCheckBox(CORRECT_GRAPH, exercise.isWindowInLab(Exercise.CORRECT_GRAPH), comboPanel);

        if (exercise.getIsGodMode() != Exercise.NOT_GM) {
            // for god mode, you must certainly be able to access the true graph
            CORRECT_GRAPH_BOX.setEnabled(false);
        }

        EXPERIMENTAL_SETUP_BOX = makeCheckBox(EXPERIMENTAL_SETUP, exercise.isWindowInLab(Exercise.EXPERIMENTAL_SETUP), comboPanel);
        HYPOTHETICAL_GRAPH_BOX = makeCheckBox(HYPOTHETICAL_GRAPH, exercise.isWindowInLab(Exercise.HYPOTHETICAL_GRAPH), comboPanel);
        JCheckBox CORRECT_MANIPULATED_GRAPH_BOX = makeCheckBox(CORRECT_MANIPULATED_GRAPH, exercise.isWindowInLab(Exercise.CORRECT_MANIPULATED_GRAPH), comboPanel);
        JCheckBox HYPOTHETICAL_MANIPULATED_GRAPH_BOX = makeCheckBox(HYPOTHETICAL_MANIPULATED_GRAPH, exercise.isWindowInLab(Exercise.HYPOTHETICAL_MANIPULATED_GRAPH), comboPanel);
        comboPanel.add(Box.createVerticalStrut(120));
        STUDENT_GUESS_BOX = makeCheckBox(INCLUDE_STUDENT_GUESSES, false, comboPanel);

        if (Exercise.GOAL_FIND_GRAPH.equals(exercise.getGoal())) {
            CORRECT_GRAPH_BOX.setEnabled(false);
            HYPOTHETICAL_GRAPH_BOX.setEnabled(false);

        } else if (Exercise.GOAL_FIND_MANIPULATED_GRAPH.equals(exercise.getGoal())) {
            CORRECT_GRAPH_BOX.setEnabled(false);
            CORRECT_MANIPULATED_GRAPH_BOX.setEnabled(false);
            HYPOTHETICAL_GRAPH_BOX.setEnabled(false);
            HYPOTHETICAL_MANIPULATED_GRAPH_BOX.setEnabled(false);
            EXPERIMENTAL_SETUP_BOX.setEnabled(false);

        } else if (Exercise.GOAL_FIND_INDEPENDENCIES.equals(exercise.getGoal())) {

        } else if (Exercise.GOAL_OTHERS.equals(exercise.getGoal())) {

        }


        ////////////////
        // Set up icon preview panel
        ImageIcon blank = new ImageIcon(ImageUtils.getImage(this, "blank.gif"));
        correctGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "true_graph.gif")), blank);
        correctManipulatedGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "true_manip_graph.gif")), blank);
        populationIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "population.gif")), blank);
        experimentalSetupIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "expt_setup.gif")), blank);
        sampleIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "sample.gif")), blank);
        hypotheticalGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "hyp_graph.gif")), blank);
        hypotheticalManipulatedGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "hyp_manip_graph.gif")), blank);
        independenciesIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "predict.gif")), blank);

        if (!exercise.isWindowInLab(Exercise.CORRECT_GRAPH)) {
            correctGraphIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.EXPERIMENTAL_SETUP)) {
            experimentalSetupIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.HYPOTHETICAL_GRAPH)) {
            hypotheticalGraphIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.CORRECT_MANIPULATED_GRAPH)) {
            correctManipulatedGraphIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.HYPOTHETICAL_MANIPULATED_GRAPH)) {
            hypotheticalManipulatedGraphIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.POPULATION)) {
            populationIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.SAMPLE)) {
            sampleIcon.toggle();
        }
        if (!exercise.isWindowInLab(Exercise.INDEPENDENCIES)) {
            independenciesIcon.toggle();
        }

        iconPanel.setLayout(new GridLayout(4, 3));
        iconPanel.add(correctGraphIcon);
        iconPanel.add(experimentalSetupIcon);
        iconPanel.add(hypotheticalGraphIcon);
        iconPanel.add(correctManipulatedGraphIcon);
        iconPanel.add(new ToggleIcon(blank, blank));
        iconPanel.add(hypotheticalManipulatedGraphIcon);
        iconPanel.add(populationIcon);
        iconPanel.add(sampleIcon);
        iconPanel.add(new ToggleIcon(blank, blank));
        iconPanel.add(new ToggleIcon(blank, blank));
        iconPanel.add(independenciesIcon);
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
        //JLabel promptLabel2a = new JLabel(edu.cmu.StringTemp.message("Windows that are necessary for the users are grayed out and you are not allowed to delete them from the Navigational Panel."));
        JLabel promptLabel3 = new JLabel("Preview of Navigational Panel:");
        JLabel promptLabel4 = new JLabel("Windows to Display:");

        JTextArea promptLabel2a = new JTextArea("Windows that are necessary for the users are grayed out and you are not allowed to delete them from the Navigational Panel.");   //$NON-NLS-3$
        promptLabel2a.setLineWrap(true);
        promptLabel2a.setWrapStyleWord(true);
        promptLabel2a.setEditable(false);
        promptLabel2a.setBackground(getBackground());

        Font regularFont = promptLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
        Font boldFont = promptLabel3.getFont().deriveFont(Font.BOLD, 12.0f);
        promptLabel1.setFont(regularFont);
        promptLabel2a.setFont(regularFont);
        promptLabel3.setFont(boldFont);
        promptLabel4.setFont(boldFont);

        JScrollPane promptLabel2 = new JScrollPane(promptLabel2a);
        promptLabel2.setPreferredSize(new Dimension(450, 40));
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

        Dimension size = new Dimension(
                Spring.sum(Spring.constant(25), iconContainerPanel2Cons.getConstraint(SpringLayout.EAST)).getValue(),
                Spring.sum(Spring.constant(25), iconContainerPanel2Cons.getConstraint(SpringLayout.SOUTH)).getValue());

        setMinimumSize(size);
        setPreferredSize(size);
        navigatorActionListener.setNavigatorIconPanel(this);
    }


    private JCheckBox makeCheckBox(String name, boolean isSelected, JPanel panel) {
        JCheckBox box = new JCheckBox(name);
        //box.setBackground(BACKGROUND_COLOR);
        box.setActionCommand(name);
        box.setSelected(isSelected);
        box.addActionListener(navigatorActionListener);
        //if (name.equals(INCLUDE_STUDENT_GUESSES)) panel.add(Box.createVerticalStrut(45));

        Font regularFont = box.getFont().deriveFont(Font.PLAIN, 12.0f);
        box.setFont(regularFont);

        panel.add(box);


        return box;
    }

}
