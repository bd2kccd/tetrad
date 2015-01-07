package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.causalityApp.exercise.WindowInclusionStatus;
import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the panel which allows user to select which navigator
 * buttons to hide.
 *
 * @author mattheweasterday
 */
class HideNavigatorIconPanel extends JPanel implements ActionListener {

    /**
     * "Hide true graph".
     */
    private static final String CORRECT_GRAPH_HIIDEN = "Hide true graph";

    /**
     * "Hide true manipulated graph".
     */
    private static final String CORRECT_MANIPULATED_GRAPH_HIIDEN = "Hide true manipulated graph";

    /**
     * "Hide BayesPopulation".
     */
    private static final String POPULATION_HIIDEN = "Hide BayesPopulation";

    // for the hide navigator panel
    private ToggleIcon hiddenCorrectGraphIcon;
    private ToggleIcon hiddenCorrectManipulatedGraphIcon;
    private ToggleIcon hiddenPopulationIcon;

    private final Exercise exercise;

    /**
     * Constructor. Creates the panel.
     */
    public HideNavigatorIconPanel(Exercise exercise) {
        super();
        this.exercise = exercise;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel comboPanel = new JPanel();
        JPanel iconPanel = new JPanel();
        JPanel iconContainerPanel = new JPanel();

        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));

        if (exercise.getCorrectGraphInclusion() != WindowInclusionStatus.NOT_INCLUDED) {
            makeCheckBox(CORRECT_GRAPH_HIIDEN, exercise.isWindowHidable(Exercise.CORRECT_GRAPH), comboPanel);
        }
        if (exercise.getCorrectManipulatedGraphInclusion() != WindowInclusionStatus.NOT_INCLUDED) {
            makeCheckBox(CORRECT_MANIPULATED_GRAPH_HIIDEN, exercise.isWindowHidable(Exercise.CORRECT_MANIPULATED_GRAPH), comboPanel);
        }
        if (exercise.getPopulationInclusion() != WindowInclusionStatus.NOT_INCLUDED) {
            makeCheckBox(POPULATION_HIIDEN, exercise.isWindowHidable(Exercise.POPULATION), comboPanel);
        }

        ImageIcon blank = new ImageIcon(ImageUtils.getImage(this, "blank.gif"));

        ToggleIcon experimentalSetupIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "expt_setup.gif")), blank);
        ToggleIcon sampleIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "sample.gif")), blank);
        ToggleIcon hypotheticalGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "hyp_graph.gif")), blank);
        ToggleIcon hypotheticalManipulatedGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "hyp_manip_graph.gif")), blank);
        ToggleIcon independenciesIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "predict.gif")), blank);
        hiddenCorrectGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "true_graph.gif")), new ImageIcon(ImageUtils.getImage(this, "true_graph_hidden.gif")));
        hiddenCorrectManipulatedGraphIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "true_manip_graph.gif")), new ImageIcon(ImageUtils.getImage(this, "true_manip_graph_hidden.gif")));
        hiddenPopulationIcon = new ToggleIcon(new ImageIcon(ImageUtils.getImage(this, "population.gif")), new ImageIcon(ImageUtils.getImage(this, "population_hidden.gif")));

        if (exercise.getExperimentalSetupInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            experimentalSetupIcon.toggle();
        }
        if (exercise.getSampleInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            sampleIcon.toggle();
        }
        if (exercise.getHypotheticalGraphInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            hypotheticalGraphIcon.toggle();
        }
        if (exercise.getHypotheticalManipulatedGraphInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            hypotheticalManipulatedGraphIcon.toggle();
        }
        if (exercise.getIndependenciesInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            independenciesIcon.toggle();
        }

        if (exercise.getCorrectGraphInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            hiddenCorrectGraphIcon = new ToggleIcon(blank, blank);
        }
        if (exercise.getCorrectManipulatedGraphInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            hiddenCorrectManipulatedGraphIcon = new ToggleIcon(blank, blank);
        }
        if (exercise.getPopulationInclusion() == WindowInclusionStatus.NOT_INCLUDED) {
            hiddenPopulationIcon = new ToggleIcon(blank, blank);
        }

        if (exercise.getCorrectGraphInclusion() == WindowInclusionStatus.HIDABLE) {
            hiddenCorrectGraphIcon.toggle();
        }
        if (exercise.getCorrectManipulatedGraphInclusion() == WindowInclusionStatus.HIDABLE) {
            hiddenCorrectManipulatedGraphIcon.toggle();
        }
        if (exercise.getPopulationInclusion() == WindowInclusionStatus.HIDABLE) {
            hiddenPopulationIcon.toggle();
        }

        iconPanel.setLayout(new GridLayout(4, 3));
        iconPanel.add(hiddenCorrectGraphIcon);
        iconPanel.add(experimentalSetupIcon);
        iconPanel.add(hypotheticalGraphIcon);
        iconPanel.add(hiddenCorrectManipulatedGraphIcon);
        iconPanel.add(new ToggleIcon(blank, blank));
        iconPanel.add(hypotheticalManipulatedGraphIcon);
        iconPanel.add(hiddenPopulationIcon);
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

        JTextArea promptLabel1a = new JTextArea("HidePlease select which window(s) the student will see in the Navigational Panel.");   //$NON-NLS-3$
        promptLabel1a.setLineWrap(true);
        promptLabel1a.setWrapStyleWord(true);
        promptLabel1a.setEditable(false);
        promptLabel1a.setBackground(getBackground());

        JLabel promptLabel2 = new JLabel("HidePreview of Navigational Panel:");   //$NON-NLS-3$

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

        Dimension size = new Dimension(
                Spring.sum(Spring.constant(25), iconContainerPanel2Cons.getConstraint(SpringLayout.EAST)).getValue(),
                Spring.sum(Spring.constant(25), iconContainerPanel2Cons.getConstraint(SpringLayout.SOUTH)).getValue());

        setMinimumSize(size);
        setPreferredSize(size);

    }

    /**
     * Defines the actions performed when the checkboxes to hide the correct
     * graph, correct manipulated graph or the population are checked.
     */
    public void actionPerformed(ActionEvent e) {
        WindowInclusionStatus status;

        if (e.getActionCommand().equals(CORRECT_GRAPH_HIIDEN)) {
            status = exercise.getCorrectGraphInclusion();
            if (status == WindowInclusionStatus.NOT_INCLUDED) {

            } else {
                hiddenCorrectGraphIcon.toggle();
                if (hiddenCorrectGraphIcon.isShown()) {
                    exercise.setCorrectGraphInclusion(WindowInclusionStatus.NOT_HIDABLE);
                } else {
                    exercise.setCorrectGraphInclusion(WindowInclusionStatus.HIDABLE);
                }
            }

        } else if (e.getActionCommand().equals(CORRECT_MANIPULATED_GRAPH_HIIDEN)) {
            status = exercise.getCorrectGraphInclusion();
            if (status == WindowInclusionStatus.NOT_INCLUDED) {

            } else {
                hiddenCorrectManipulatedGraphIcon.toggle();
                if (hiddenCorrectManipulatedGraphIcon.isShown()) {
                    exercise.setCorrectManipulatedGraphInclusion(WindowInclusionStatus.NOT_HIDABLE);
                } else {
                    exercise.setCorrectManipulatedGraphInclusion(WindowInclusionStatus.HIDABLE);
                }
            }

        } else if (e.getActionCommand().equals(POPULATION_HIIDEN)) {
            status = exercise.getPopulationInclusion();
            if (status == WindowInclusionStatus.NOT_INCLUDED) {

            } else {
                hiddenPopulationIcon.toggle();
                if (hiddenPopulationIcon.isShown()) {
                    exercise.setPopulationInclusion(WindowInclusionStatus.NOT_HIDABLE);
                } else {
                    exercise.setPopulationInclusion(WindowInclusionStatus.HIDABLE);
                }
            }
        }
    }


    private void makeCheckBox(String name, boolean isSelected, JPanel panel) {
        JCheckBox box = new JCheckBox(name);
        box.setActionCommand(name);
        box.setSelected(isSelected);
        box.addActionListener(this);

        Font regularFont = box.getFont().deriveFont(Font.PLAIN, 12.0f);
        box.setFont(regularFont);
        //FIXME: Commenting out the god mode stuff for now. Need to find out more.
        // check if it is god mode here: if so, disable the hide true graph button;
//        if(exercise.getIsGodMode() == Exercise.NOT_GM && name.equals(CORRECT_GRAPH_HIIDEN)){
//            box.setEnabled(false);
//        }

        panel.add(box);
    }
}
