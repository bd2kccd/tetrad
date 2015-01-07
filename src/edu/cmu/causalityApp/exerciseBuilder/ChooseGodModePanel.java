package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.exercise.Exercise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the panel which allows the user to choose what mode
 * to build the exercise in:
 *
 * @author mingyang koh
 */
public class ChooseGodModePanel extends JPanel {
    private final Exercise exercise;

    private int mode = 0;

    private final int NOT_GM = 0;
    private final int GM_1 = 1;
    private final int GM_2 = 2;
    private final int GM_3 = 3;


    private boolean buildNow = false;

    final private static String NORMAL_MODE =
            "(Normal Mode) You build true model now and students cannot change it during exercise.";
    final private static String GODMODE_PARAM_EDIT =
            "(Student Editing Mode 1) You build true model now, and students can edit parameters during the exercise.";
    final private static String GODMODE_LATENTS_AND_EDGES =
            "(Student Editing Mode 2) You build true model now, and students can edit parameters and the graph by adding latents and edges.";
    final private static String GODMODE_NO_BUILD =
            "(Student Editing Mode 3) You do not build a true model now, the students will build it from scratch.";

    /**
     * Constructor. Creates the panel.
     */
    public ChooseGodModePanel(Exercise exercise) {
        super();
//        System.out.println("#1 exercise == null: " + (exercise==null));
        this.exercise = exercise;

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JTextArea typeLabel1 = new JTextArea("Please specify if this exercise is going to allow students to create and edit true models." +
                "The normal mode will only allow one fixed true model.");   //$NON-NLS-3$
        typeLabel1.setLineWrap(true);
        typeLabel1.setWrapStyleWord(true);
        typeLabel1.setEditable(false);
        typeLabel1.setBackground(getBackground());

        JLabel typeLabel2 = new JLabel("Mode");
        Font regularFont = typeLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
        Font boldFont = typeLabel2.getFont().deriveFont(Font.BOLD, 12.0f);

        typeLabel1.setFont(regularFont);
        typeLabel2.setFont(boldFont);

        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));
        ButtonGroup group = new ButtonGroup();

        JRadioButton normalButton = new JRadioButton(ChooseGodModePanel.NORMAL_MODE);
        JRadioButton gmParamEditButton = new JRadioButton(ChooseGodModePanel.GODMODE_PARAM_EDIT);
        JRadioButton gmLatentEdgeButton = new JRadioButton(ChooseGodModePanel.GODMODE_LATENTS_AND_EDGES);
        JRadioButton gmNoBuildButton = new JRadioButton(ChooseGodModePanel.GODMODE_NO_BUILD);

        makeComboButton(normalButton, group, comboPanel);
        makeComboButton(gmParamEditButton, group, comboPanel);
        makeComboButton(gmLatentEdgeButton, group, comboPanel);
        makeComboButton(gmNoBuildButton, group, comboPanel);


        normalButton.setFont(regularFont);
        gmParamEditButton.setFont(regularFont);
        gmLatentEdgeButton.setFont(regularFont);
        gmNoBuildButton.setFont(regularFont);

        JScrollPane scrollPane = new JScrollPane(typeLabel1);
        scrollPane.setPreferredSize(new Dimension(100, 40));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane);
        add(Box.createVerticalStrut(10));
        add(typeLabel2);
        add(comboPanel);

    }

    private void makeComboButton(JRadioButton button, ButtonGroup group, JPanel panel) {
        button.setActionCommand(button.getName());
        button.setSelected(true);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (ae.getActionCommand().equals(NORMAL_MODE)) {
                    mode = NOT_GM;
                    buildNow = true;
                } else if (ae.getActionCommand().equals(GODMODE_PARAM_EDIT)) {
                    mode = GM_1;
                    buildNow = true;
                } else if (ae.getActionCommand().equals(GODMODE_LATENTS_AND_EDGES)) {
                    mode = GM_2;
                    buildNow = true;

                } else {
                    mode = GM_3;
                    buildNow = true;

                }

                exercise.setIsGodMode(mode);

                exercise.setBuildNow(buildNow);

            }
        });
        group.add(button);
        panel.add(button);
    }
}
