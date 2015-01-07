package edu.cmu.causalityApp.chartBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An abstract class to define standard methods for classes building graphs.
 *
 * @author Adrian Tang
 */
public abstract class ChartBuilder extends JInternalFrame {
    private String exptName;
    private int sampleId;

    /**
     * Constructor.
     *
     * @param expName  name of the currently active experimental setup
     * @param sampleId unique id of the sample to build graph from
     * @param title    the window title, either "histogram" or "scatterplot"
     */
    ChartBuilder(String expName, int sampleId, String title) {
        super(title, true, true, true, true);
        setExptName(expName);
        setSampleId(sampleId);
    }


    /**
     * Creates the main page container for the create histogram wizard.
     *
     * @return JPanel main page
     */
    JPanel makePage() {
        JPanel panel = new JPanel();
        JPanel buttonsP = new JPanel();

        JButton cancelBn = new JButton("Cancel");
        JButton okBn = new JButton("OK");

        buttonsP.setLayout(new FlowLayout());
        buttonsP.add(cancelBn);
        buttonsP.add(Box.createHorizontalGlue());
        buttonsP.add(okBn);
        buttonsP.setPreferredSize(new Dimension(500, 50));

        panel.setLayout(new BorderLayout());
        panel.add(createChooser(), BorderLayout.CENTER);
        panel.add(buttonsP, BorderLayout.SOUTH);

        okBn.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (isReadyToCreateChart()) createChart();
                        else dispose();
                    }
                }
        );

        cancelBn.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                }
        );

        return panel;
    }

    protected abstract void createChart();

    protected abstract boolean isReadyToCreateChart();

    protected abstract JPanel createChooser();

    String getExperimentalSetupName() {
        return exptName;
    }

    /**
     * Set the name of the experimental setup that is currently active.
     */
    private void setExptName(String exptName) {
        this.exptName = exptName;
    }

    /**
     * Set the unique id of the sample selected.
     */
    private void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }
}
