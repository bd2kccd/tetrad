package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.RegressionInfo;

import javax.swing.*;
import java.awt.*;

/**
 * This is the container frame to hold the regression table results.
 *
 * @author Adrian Tang
 */
class RegressionChartFrame extends JInternalFrame {

    /**
     * Constructor.
     */
    public RegressionChartFrame(RegressionInfo interactionChart) {
        super("Regression Analysis", true, true, true, true);

        setPreferredSize(new Dimension(300, 180));
        RegressionChartView mainview = new RegressionChartView(interactionChart);

        setContentPane(mainview);
        pack();
    }
}
