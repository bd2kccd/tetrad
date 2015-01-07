package edu.cmu.causalityApp.chartBuilder;


import edu.cmu.causality.chartBuilder.Histogram;

import javax.swing.*;
import java.awt.*;

/**
 * This is a wrapper class for a histogram view. It holds the histogram view
 * in an internal frame.
 *
 * @author Adrian Tang
 */
class  HistogramChartFrame extends JInternalFrame {

    /**
     * Define the default colors used, defined as red, green, blue values. The
     * first color would be used, then the next. When all the colors are used,
     * the first color would be reused again.
     */
    private static final Color[] defaultColors = {
            new Color(153, 102, 102), new Color(102, 102, 153), new Color(102, 153, 102), new Color(153, 102, 153), new Color(153, 153, 102), new Color(102, 153, 153), new Color(204, 153, 153), new Color(153, 153, 204), new Color(153, 204, 153), new Color(204, 153, 204),
            new Color(204, 204, 153), new Color(153, 204, 204), new Color(255, 204, 204), new Color(204, 204, 255), new Color(204, 255, 204)
    };

    /**
     * Constructor
     */
    public HistogramChartFrame(Histogram histogram) {
        super("Histogram", true, true, true, true);

        if (histogram.getNumStates() > 3)
            setPreferredSize(new Dimension(480, 300));
        else
            setPreferredSize(new Dimension(350, 300));

        JPanel mainview = new JPanel();
        JPanel chartPanel = new JPanel();
        JPanel titleP = new JPanel();

        mainview.setLayout(new BoxLayout(mainview, BoxLayout.Y_AXIS));
        mainview.add(titleP);
        mainview.add(chartPanel);
        mainview.add(Box.createVerticalStrut(5));

        chartPanel.setLayout(new FlowLayout());
        chartPanel.add(new HistogramChartView(this, histogram));
        chartPanel.add(new HistogramLegendView(this, histogram));

        titleP.add(new JLabel(histogram.getTitle()));

        setContentPane(mainview);
        pack();
    }

    /**
     * Given which state to draw, decides which default color to use to draw the
     * histogram bar.
     *
     * @return the default color to use
     */
    public Paint getStateColor(int stateIndex) {
        return defaultColors[stateIndex % defaultColors.length];
    }
}
