package edu.cmu.causalityApp;

import edu.cmu.causality.ConsoleOutputStream;

import javax.swing.*;

/**
 * This window displays the error messages and System.out.println messages from
 * the Java console. It is meant to allow users to copy this console trace
 * for bug report purposes.
 *
 * @author adrian tang
 */
class BugReportConsole extends JFrame {

    /**
     * Unique ID name for this window.
     */
    private static final String MY_NAME = "Bug Report Console";

    /**
     * Show the bug report console window.
     */
    public static void showScreen() {
        new BugReportConsole();
    }

    /**
     * Private constructor.
     */
    private BugReportConsole() {
        super(MY_NAME);

        add(getMainPane());
        pack();
        setVisible(true);
    }

    private JComponent getMainPane() {
        JTextArea textArea = new JTextArea(
                ConsoleOutputStream.getConsoleOutput());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(50);

        return new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
