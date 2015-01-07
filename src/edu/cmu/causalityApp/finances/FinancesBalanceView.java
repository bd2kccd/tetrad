package edu.cmu.causalityApp.finances;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.event.*;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class creates a graphical representation of the amount of money left for
 * the student.
 *
 * @author adrian tang
 */
public class FinancesBalanceView extends JComponent implements ModelChangeListener {
    private CausalityLabModel model;
    private boolean isVisible;

    /**
     * Constructor. This view not visible by default, unless the exercise
     * is required to limit resources.
     */
    public FinancesBalanceView() {
        isVisible = false;
    }

    /**
     * Paints the money balance as well as a bar scale representing the money
     * left.
     */
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        g.setPaint(Color.white);
        g.fillRect(0, 0, 125, 40);
        g.setPaint(Color.gray);
        g.drawRect(0, 0, 125, 40);

        if (isVisible) {
            NumberFormat nf1 = NumberFormat.getInstance();
            nf1.setMinimumFractionDigits(0);
            nf1.setMaximumFractionDigits(0);

            g.setPaint(Color.black);
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 12F));
            g.drawString(nf1.format(model.getCurrentBalance()) + " left", 25, 35);

            g.setPaint(Color.blue);
            g.drawRect(10, 10, 100, 10);

            float scale =
                    (float) model.getCurrentBalance().intValue() /
                            (float) model.getTotalInitialBalance().intValue();
            g.fillRect(10, 10, (int) (scale * 100), 10);
        }
    }

    /**
     * Set the model to the main CausalityLabModel and adds an action listener
     * to the main model so that the model can keep track of any changes in this
     * view.
     *
     * @param model singleton CausalityLabModel
     */
    public void setModel(CausalityLabModel model) {
        this.model = model;
        setVisible(model.isLimitResource());

        if (model.isLimitResource())
            model.addModelChangeListener(this);
    }

    /**
     * Specifies if this view is visible or not.
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
        repaint();
    }

    /**
     * Sets the preferred size of the view.
     */
    public Dimension getPreferredSize() {
        return new Dimension(120, 45);
    }

    /**
     * Set the minimum size of the view.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Set the maximum size of the view.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Update the view when the finances are changed.
     */
    public void financeChanged() {
        if (isVisible)
            repaint();
    }


    public void hypothesisChanged(HypothesisChangedEvent hcEvent) {
    }

    public void experimentChanged(ExperimentChangedEvent ecEvent) {
    }

    public void sampleChanged(SampleChangedEvent scEvent) {
    }

}
