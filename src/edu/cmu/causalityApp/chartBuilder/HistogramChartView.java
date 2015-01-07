package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.Histogram;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * This is the actual view of a histogram chart. This class draws the visual
 * details of the histogram.
 *
 * @author Adrian Tang
 */
class HistogramChartView extends JComponent {
    private HistogramChartFrame parent;
    private Histogram histogram;

    /**
     * Use this to format any numbers to have 2 decimals.
     */
    private static NumberFormat nf;

    static {
        nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /**
     * Constructor
     *
     * @param parent    the frame that contains this histogram view
     * @param histogram the associated histogram model
     */
    public HistogramChartView(HistogramChartFrame parent, Histogram histogram) {
        this.parent = parent;
        this.histogram = histogram;
    }

    /**
     * Paints the histogram.
     */
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        int chartWidth = parent.getWidth() * 6 / 10;
        int chartHeight = parent.getHeight() * 7 / 10;

        int xMin = 32;
        int xMax = chartWidth - 5;
        int xRange = xMax - xMin;
        //int yMin = 5;
        int yMin = 40;
        int yMax = chartHeight - 5;
        int yRange = yMax - yMin;
        int numCats = histogram.getNumStates();
        int numRangeGroups = histogram.getNumRangeGroups();
        double groupWidth = 1.0D / (double) numRangeGroups;
        double scale = (double) yRange;

        /* draws the axis and division lines */
        if (numRangeGroups > 0) {
            g.setFont(g.getFont().deriveFont(10F));
            g.setStroke(new BasicStroke(1.0F, 0, 2, 0.0F, new float[]{
                    2.0F, 2.0F
            }, 0.0F));
            for (int i = 0; i < numRangeGroups + 1; i++) {
                double bd = (double) i * groupWidth;
                int y = yMax - (int) (scale * bd);
                if (yMin <= y) {
                    g.setPaint(Color.lightGray.darker());
                    g.drawLine(xMin - 2, y, xMax, y);
                    g.setPaint(Color.black);
                    if (i != 0) g.drawString(nf.format(bd), 5, y + 5);
                }
            }
            g.setStroke(new BasicStroke());
        }
        g.setPaint(Color.black);
        g.drawLine(xMin - 2, yMax, xMax, yMax);
        g.drawLine(xMin, yMin, xMin, yMax);

        /* draws the histogram bars */
        int barWidth = xRange / numCats;
        for (int i = 0; i < numCats; i++) {
            int barHeight = (int) (scale * histogram.getStateValue(i));
            int barX = xMin + i * barWidth;
            int barY = yMax - barHeight;
            g.setPaint(parent.getStateColor(i));
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setPaint(Color.black);
            g.drawRect(barX, barY, barWidth, barHeight);
        }

        /* draws the labels for the corresponding experiment and sample names */
        g.setFont(g.getFont().deriveFont(11f));
        g.setColor(Color.black);
        if (histogram.getTrueName() != null) {
            g.drawString(histogram.getTrueName(), 5, 10);
            g.drawString(histogram.getExptName(), 5, 21);
            g.drawString(histogram.getSampleName() + " (n=" + histogram.getSampleSize() + ")", 5, 32);
        } else {
            g.drawString(histogram.getExptName(), 5, 10);
            g.drawString(histogram.getSampleName() + " (n=" + histogram.getSampleSize() + ")", 5, 21);
        }

    }

    /**
     * Sets the preferred size of the histogram proportional to the frame.
     */
    public Dimension getPreferredSize() {
        return new Dimension(parent.getWidth() * 6 / 10, parent.getHeight() * 7 / 10);
    }

    /**
     * Set the minimum size of the histogram to the preferred size.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Set the maximum size of the histogram to the preferred size.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

}
