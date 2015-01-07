package edu.cmu.causalityApp.dataEditors.sampleEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the panel which shows the progress of saving a sample.
 * It uses a ProgressBar to simulate the progress of saving the sample.
 *
 * @author mattheweasterday
 */
public class SaveSampleProgressDialog extends JInternalFrame {

    /**
     * Integer constant for half a second.
     */
    private final static int HALF_SECOND = 500;
    private static boolean SHOW_SAMPLE_CREATED = false;
    private final JProgressBar progressBar;
    private final Timer timer;
    private int ticks = 0;
    private final int max_ticks = 6;
    private final JDesktopPane desktop;

    /**
     * Constructor.
     */
    public SaveSampleProgressDialog(JDesktopPane labFrame) {
        super("Collecting Data");
        this.desktop = labFrame;

        //Create a timer.
        timer = new Timer(HALF_SECOND, new TimerListener());
        progressBar = new JProgressBar(0, max_ticks * HALF_SECOND);
        progressBar.setValue(0);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPane.add(new JLabel("Progress"));
        contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPane.add(progressBar);
        contentPane.add(Box.createRigidArea(new Dimension(0, 10)));
        progressBar.setPreferredSize(new Dimension(500, 30));

        timer.start();
        pack();
        setVisible(true);
    }


    /**
     * The actionPerformed method in this class
     * is called each time the Timer "goes off".
     */
    private class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            ticks++;
            progressBar.setValue(ticks * HALF_SECOND);
            if (ticks > max_ticks) {
                timer.stop();
                ticks = 0;
                SaveSampleProgressDialog.this.setVisible(false);
                sampleSaveDialogDone();
            }
        }
    }


    /**
     * Callback method for SaveSampleProgressDialog.  Called with the progress dialog finishes
     */
    void sampleSaveDialogDone() {
        if (SHOW_SAMPLE_CREATED) {
            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
            JLabel label = new JLabel("<html>The new sample is now stored in the Sample window.<br>Open the sample window in order to see it.</html>");
            final JCheckBox checkbox = new JCheckBox("Don't show this message again");
            checkbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SHOW_SAMPLE_CREATED = !checkbox.isSelected();
                }
            });
            pane.add(label);
            pane.add(checkbox);

            JOptionPane.showInternalMessageDialog(desktop, pane, "Sample Created", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
