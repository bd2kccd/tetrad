package edu.cmu.causalityApp.exerciseBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * This panel is to instruct the user that he has the option to save the exercise
 * as all the required information has been given.
 *
 * @author Adrian Tang
 */
public class OptionalInstructionsPanel extends JPanel {

    /**
     * Constructor. Creates the panel.
     */
    public OptionalInstructionsPanel() {
        super();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JTextArea instructions1 = new JTextArea("You have specified all the necessary information to create an exercise. You may click 'Finish' to save this exercise.");   //$NON-NLS-3$
        instructions1.setLineWrap(true);
        instructions1.setWrapStyleWord(true);
        instructions1.setEditable(false);
        instructions1.setBackground(getBackground());
        instructions1.setColumns(30);

        JTextArea instructions2 = new JTextArea("There are additional advanced features that you can configure in the exercise. You may click 'Next' to access these features:");
        instructions2.setLineWrap(true);
        instructions2.setWrapStyleWord(true);
        instructions2.setEditable(false);
        instructions2.setBackground(getBackground());
        instructions2.setColumns(30);

        JTextArea heading = new JTextArea("Optional Advanced Features:");
        heading.setBackground(getBackground());
        Font regularFont = heading.getFont().deriveFont(Font.BOLD, 12.0f);
        heading.setFont(regularFont);

        JTextArea instructions3 = new JTextArea("      - show / hide windows" +
                "\n" + "      - limit resources" +
                "\n" + "      - change experimental setup capabilities");   //$NON-NLS-3$
        instructions3.setLineWrap(true);
        instructions3.setWrapStyleWord(true);
        instructions3.setEditable(false);
        instructions3.setBackground(getBackground());
        instructions3.setColumns(30);

        add(instructions1);
        add(Box.createVerticalStrut(10));
        add(instructions2);
        add(Box.createVerticalStrut(10));
        add(heading);
        add(instructions3);
        add(Box.createVerticalStrut(15));


        setPreferredSize(new Dimension(400, 165));
    }
}
