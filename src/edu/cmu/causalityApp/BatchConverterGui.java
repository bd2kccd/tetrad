package edu.cmu.causalityApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * This interface allows you to take old causality lab exercise files in one
 * directory, and convert them to the latest version.
 *
 * @author Matt Easterday
 */
public class BatchConverterGui extends JFrame {

    private final JFileChooser fc;
    private File srcDir;
    private File destDir;
    private JTextField srcText;
    private JTextField destText;
    private JButton destB;

    /**
     * Constructor.  Cretes and shows batch conversion interface.
     */
    private BatchConverterGui() {
        super("Convert causality lab files");
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Set up the content pane.
        layoutStuff(getContentPane());


        //Display the window.
        pack();
        setVisible(true);
    }


    private void layoutStuff(Container pane) {
        int height = 30;

        JLabel src = new JLabel("  Source dir:");
        JLabel dest = new JLabel("  Dest. dir:");
        src.setPreferredSize(new Dimension(80, height));
        dest.setPreferredSize(new Dimension(80, height));

        srcText = new JTextField();
        destText = new JTextField();
        srcText.setPreferredSize(new Dimension(300, height));
        destText.setPreferredSize(new Dimension(300, height));
        srcText.setEnabled(false);
        destText.setEnabled(false);

        JButton srcB = new JButton("...");
        destB = new JButton("...");
        srcB.setMaximumSize(new Dimension(150, height));
        destB.setMaximumSize(new Dimension(150, height));
        destB.setEnabled(false);

        srcB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(BatchConverterGui.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    srcDir = fc.getSelectedFile();
                    srcText.setText(srcDir.getName());
                    destB.setEnabled(true);
                }
            }
        });

        destB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showSaveDialog(BatchConverterGui.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    destDir = fc.getSelectedFile();
                    destText.setText(destDir.getName());
                    try {
                        BatchConvert.copyDirectory(srcDir, destDir);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        sourcePanel.add(src);
        sourcePanel.add(srcText);
        sourcePanel.add(srcB);

        JPanel destPanel = new JPanel();
        destPanel.setLayout(new BoxLayout(destPanel, BoxLayout.X_AXIS));
        destPanel.add(dest);
        destPanel.add(destText);
        destPanel.add(destB);

        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(sourcePanel);
        pane.add(destPanel);
    }

    /**
     * Main method for testing.
     *
     * @param args moves line arguments are ignored.
     */
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BatchConverterGui();
            }
        });
    }


}
