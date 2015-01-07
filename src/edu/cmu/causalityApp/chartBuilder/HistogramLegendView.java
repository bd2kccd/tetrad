package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.Histogram;

import javax.swing.*;
import java.awt.*;

/**
 * This class draws the legend for a histogram view.
 *
 * @author Adrian Tang
 */
class HistogramLegendView extends JComponent {
    private HistogramChartFrame parent;
    private Histogram histogram;

    /**
     * Constructor.
     */
    public HistogramLegendView(HistogramChartFrame parent, Histogram histogram) {
        this.parent = parent;
        this.histogram = histogram;
    }

    /**
     * Draws the legend for the histogram.
     */
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        int legendWidth = (int) getPreferredSize().getWidth();
        int legendHeight = (int) getPreferredSize().getHeight();

        int numCats = histogram.getNumStates();
        FontMetrics fm = getFontMetrics(g.getFont().deriveFont(10F));
        int lineHeight = (int) (1.2D * (double) fm.getHeight());
        int boxSide = fm.getHeight() - fm.getLeading();
        int allLinesHeight = (numCats + 1) * lineHeight;
        int yOff = Math.max(5, (legendHeight - allLinesHeight) / 2);
        g.translate(0, yOff);
        String varName = histogram.getIndependentsAsString();
        int xOff = Math.max(5, (legendWidth - fm.stringWidth(varName)) / 2);
        g.setColor(Color.BLACK);
        g.drawString(varName, xOff, boxSide - 3);
        int maxCatWidth = 0;
        for (int i = 0; i < numCats; i++) {
            int catWidth = fm.stringWidth(histogram.getStateName(i));
            maxCatWidth = Math.max(catWidth, maxCatWidth);
        }

        int boxPlusCatsWidth = boxSide + 5 + maxCatWidth;
        xOff = Math.max(5, (legendWidth - boxPlusCatsWidth) / 2);
        g.translate(xOff, lineHeight);
        for (int i = 0; i < numCats; i++) {
            int y = i * lineHeight;
            g.setPaint(parent.getStateColor(i));
            g.fillRect(0, y, boxSide, boxSide);
            g.setColor(Color.BLACK);
            g.drawRect(0, y, boxSide, boxSide);
            g.drawString(histogram.getStateName(i), boxSide + 5, (y + boxSide) - 3);
        }
    }

    /**
     * Sets the preferred size of the legend based on the amount of text in the
     * legend box.
     */
    public Dimension getPreferredSize() {
        int numCats = histogram.getNumStates();
        FontMetrics fm = getFontMetrics(getFont().deriveFont(10F));
        int varNameWidth = fm.stringWidth(histogram.getIndependentsAsString());
        int maxCatWidth = 0;
        for (int i = 0; i < numCats; i++) {
            int catWidth = fm.stringWidth(histogram.getStateName(i));
            maxCatWidth = Math.max(catWidth, maxCatWidth);
        }

        int boxSide = fm.getHeight() - fm.getLeading();
        int prefWidth = Math.max(varNameWidth, boxSide + 5 + maxCatWidth) + 10;
        int lineHeight = (int) (1.2D * (double) fm.getHeight());
        int prefHeight = (numCats + 1) * lineHeight + 10;

        return new Dimension(prefWidth + 35, prefHeight);
    }

    /**
     * Set the minimum size of the legend view to the preferred size.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Set the maximum size of the legend view to the preferred size.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
