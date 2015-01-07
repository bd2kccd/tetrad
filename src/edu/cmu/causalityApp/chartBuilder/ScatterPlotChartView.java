package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.ScatterPlot;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.Vector;

/**
 * This view draws the scatterplot using the information from the ScatterPlot
 * class. It draws the scatterplot line, axes, labels and the statistical values.
 *
 * @author Adrian Tang
 */
class ScatterPlotChartView extends JComponent {
    private ScatterPlotChartFrame parent;
    private ScatterPlot scatterPlot;

    private static NumberFormat nf;

    static {
        nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /**
     * Constructor.
     */
    public ScatterPlotChartView(ScatterPlotChartFrame parent, ScatterPlot scatterPlot) {
        this.parent = parent;
        this.scatterPlot = scatterPlot;
    }

    /**
     * Renders the view.
     */
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        int chartWidth = parent.getWidth() * 8 / 10;
        int chartHeight = parent.getHeight() * 7 / 10;

        int xMin = 30;
        int xMax = chartWidth - 10;
        int xRange = xMax - xMin;
        int yMin = 35;
        int yMax = chartHeight - 18;
        int yRange = yMax - yMin;

        /* draws axis lines */
        g.setStroke(new BasicStroke());
        g.setPaint(Color.black);
        g.drawLine(xMin, yMax, xMax, yMax);
        g.drawLine(xMin, yMin, xMin, yMax);

        /* draws the labels for the corresponding experiment and sample names */
        g.setFont(g.getFont().deriveFont(11f));
        if (scatterPlot.getTrueName() != null) {
            g.drawString(scatterPlot.getTrueName(), 5, 10);
            g.drawString(scatterPlot.getExptName(), 5, 21);
            g.drawString(scatterPlot.getSampleName() + " (n=" + scatterPlot.getSampleSize() + ")", 5, 32);
        } else {
            g.drawString(scatterPlot.getExptName(), 5, 10);
            g.drawString(scatterPlot.getSampleName() + " (n=" + scatterPlot.getSampleSize() + ")", 5, 21);
            //g.drawString(scatterPlot.getEquation(), 120,32);
        }
        /* draws axis labels and scale */
        g.drawString(nf.format(scatterPlot.getYmax()), 2, yMin + 7);
        g.drawString(nf.format(scatterPlot.getYmin()), 2, yMax);
        g.drawString(nf.format(scatterPlot.getXmax()), xMax - 20, yMax + 14);
        g.drawString(nf.format(scatterPlot.getXmin()), 30, yMax + 14);
        g.drawString(scatterPlot.getXvar(), xMin + (xRange / 2) - 10, yMax + 14);
        g.translate(xMin - 7, yMin + (yRange / 2) + 10);
        g.rotate(-Math.PI / 2.0);
        g.drawString(scatterPlot.getYvar(), 0, 0);
        g.rotate(Math.PI / 2.0);
        g.translate(-(xMin - 7), -(yMin + (yRange / 2) + 10));

        /* draws scatterplot of the values */
        Vector pts = scatterPlot.getSievedValues();
        Point2D.Double pt;
        double xDoubleRange = scatterPlot.getXmax() - scatterPlot.getXmin();
        double yDoubleRange = scatterPlot.getYmax() - scatterPlot.getYmin();
        int x, y;

        g.setColor(Color.red);
        for (Object pt1 : pts) {
            pt = (Point2D.Double) pt1;
            x = (int) (((pt.getX() - scatterPlot.getXmin()) / xDoubleRange) * xRange + xMin);
            y = (int) (((scatterPlot.getYmax() - pt.getY()) / yDoubleRange) * yRange + yMin);
            g.fillOval(x - 1, y - 1, 3, 3);
        }

        /* draws best-fit line */
        if (scatterPlot.getIncludeLine()) {
            double coeff = scatterPlot.getRegressionCoeff();
            double zeroIntrpt = scatterPlot.getRegressionZeroIntrpt();

            int y1 = (int) (((scatterPlot.getYmax() -
                    (coeff * scatterPlot.getXmin() + zeroIntrpt)) / yDoubleRange) * yRange + yMin);
            int y2 = (int) (((scatterPlot.getYmax() -
                    (coeff * scatterPlot.getXmax() + zeroIntrpt)) / yDoubleRange) * yRange + yMin);
            g.setColor(Color.BLUE);
            g.drawLine(xMin, y1, xMax, y2);
        }

        /* draws statistical values */
        if (scatterPlot.getIncludeLine()) {
            g.setColor(Color.black);
            g.setFont(g.getFont().deriveFont(11f));
            nf.setMinimumFractionDigits(3);
            nf.setMaximumFractionDigits(3);
            g.drawString("correlation coeff" + nf.format(scatterPlot.getCorrelationCoeff())
                    + "  (p=" + nf.format(scatterPlot.getCorrelationPvalue()) + ")", 100, 21);
        }
    }

    /**
     * @return the preferred dimension of the scatterplot.
     */
    public Dimension getPreferredSize() {
        return new Dimension(parent.getWidth() * 8 / 10 + 10, parent.getHeight() * 7 / 10);
    }

    /**
     * @return the minimum dimension of the scatterplot.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * @return the maximum dimension of the scatterplot.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
