package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.ScatterPlot;

import javax.swing.*;
import java.awt.*;

/**
 * This is the container frame to hold the scatterplot view.
 *
 * @author Adrian Tang
 */
class ScatterPlotChartFrame extends JInternalFrame {

    /**
     * Constructor.
     */
    public ScatterPlotChartFrame(ScatterPlot interactionChart) {
        super("Scatterplot", true, true, true, true);

        setPreferredSize(new Dimension(400, 300));

        JPanel mainview = new JPanel();
        JPanel titleP = new JPanel();
        JLabel equation = new JLabel(interactionChart.getEquation());

        Box titleB = Box.createVerticalBox();

        titleB.add(new JLabel(interactionChart.getTitle()));
        titleB.add(Box.createVerticalStrut(10));
        titleB.add(equation);
        titleP.add(titleB);

        equation.setFont(new Font("sansserif", Font.PLAIN, 11));

        mainview.setLayout(new BoxLayout(mainview, BoxLayout.PAGE_AXIS));
        mainview.add(titleP);
        mainview.add(new ScatterPlotChartView(this, interactionChart));

        //titleP.add(new JLabel(interactionChart.getTitle()), BorderLayout.NORTH);
        //titleP.setLayout(new BoxLayout(titleP, BoxLayout.PAGE_AXIS));
        //titleP.add(new JLabel(interactionChart.getConstraints()), BorderLayout.SOUTH);

        setContentPane(mainview);
        pack();
    }
}
