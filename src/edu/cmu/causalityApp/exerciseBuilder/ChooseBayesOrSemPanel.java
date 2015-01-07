package edu.cmu.causalityApp.exerciseBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the panel which allows the user to choose either a Bayes
 * or SEM graph.
 *
 * @author mattheweasterday
 */
public class ChooseBayesOrSemPanel extends JPanel {

    private boolean isBayesSelected = true;
    final private static String BAYES = "Bayes Net (categorical variables)";
    final private static String SEM = "Structural Equation Model (continuous variables / linear functions)";

    /**
     * Constructor. Creates the panel.
     */
    public ChooseBayesOrSemPanel(boolean isBayes) {
        super();
        isBayesSelected = isBayes;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JTextArea typeLabel1 = new JTextArea("Please specify the type of parametric statistical model you want to use to interpret the causal graph.");   //$NON-NLS-3$
        typeLabel1.setLineWrap(true);
        typeLabel1.setWrapStyleWord(true);
        typeLabel1.setEditable(false);
        typeLabel1.setBackground(getBackground());

        JLabel typeLabel2 = new JLabel("Parametric Model:");
        Font regularFont = typeLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
        Font boldFont = typeLabel2.getFont().deriveFont(Font.BOLD, 12.0f);

        typeLabel1.setFont(regularFont);
        typeLabel2.setFont(boldFont);

        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.PAGE_AXIS));
        ButtonGroup group = new ButtonGroup();

        JRadioButton bayesButton = new JRadioButton(BAYES);
        JRadioButton semButton = new JRadioButton(SEM);
        makeComboButton(bayesButton, isBayesSelected, group, comboPanel);
        makeComboButton(semButton, !isBayesSelected, group, comboPanel);
        bayesButton.setFont(regularFont);
        semButton.setFont(regularFont);

        JScrollPane scrollPane = new JScrollPane(typeLabel1);
        scrollPane.setPreferredSize(new Dimension(100, 40));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane);
        add(Box.createVerticalStrut(10));
        add(typeLabel2);
        add(comboPanel);
    }

    /**
     * @return if the user selected the Bayes graph.
     */
    public boolean isBayesSelected() {
        return isBayesSelected;
    }

    private void makeComboButton(JRadioButton button, boolean isSelected, ButtonGroup group, JPanel panel) {
        button.setActionCommand(button.getName());
        button.setSelected(isSelected);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (ae.getActionCommand().equals(BAYES)) {
                    isBayesSelected = true;
                } else if (ae.getActionCommand().equals(SEM)) {
                    isBayesSelected = false;
                }
            }
        });
        group.add(button);
        panel.add(button);
    }
}
