package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import edu.cmu.causality.independencies.IndependenceResult;
import edu.cmu.causalityApp.util.Misc;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * To change this template use File | Settings | File Templates.
 *
 * @author mykoh
 */
public class SampleIndependenciesCellRenderer extends CellRenderer {
    private final boolean isStudentGuessEnabled;
    private static final Icon questionMarkIcon = new HiddenIcon();

    /**
     * Constructor.
     */
    public SampleIndependenciesCellRenderer(boolean isStudentGuessEnabled) {
        super();
        independentIcon = new SampleIndependenciesCellRenderer.IndependenceIcon(Color.black, null);
        dependentIcon = new SampleIndependenciesCellRenderer.IndependenceIcon(Color.black, Color.red);
        noneIcon = new SampleIndependenciesCellRenderer.IndependenceIcon(noneColor, noneColor);

        this.isStudentGuessEnabled = isStudentGuessEnabled;
    }

    /**
     * Set the value of the table cell.       we know that value is a PValue object...
     */
    public void setValue(Object value) {
        DecimalFormat displayDouble = new DecimalFormat();

        /* StringBuffer pattern = new StringBuffer("#.##");
        // if the hex sign is used, nonsignificant zeros after the dot
        // will not be shown, ie, eg. 0.00 will be shown as 0.0
         whereas, if '0' is used, there will definitely be those digits on display
         */
        String pattern = "0.00";
        displayDouble.applyPattern(pattern);

        if (value == null) {

            if (isStudentGuessEnabled) {
                setIcon(questionMarkIcon);
            } else {
                setIcon(noneIcon);
            }
            //if you remove setText(null) other cells will set the text in this cell--bad!!!!
            setToolTipText(null);
            setText(null);
            //IMPT!!

        } else if (!((IndependenceResult) value).isIndependent()) {
            setIcon(dependentIcon);

            String tempString = displayDouble.format(((IndependenceResult) value).getPValue());

            setToolTipText("(" + tempString + ") : (p-value) the probability that the association in the sample" +
                    " is what we observed or more, assuming independence.");
            // if the double arg for the format function is less than 0.01, then
            // after the format function, tempString will only have 0, not even 0.00
            //String tempString = displayDouble.format(0.005);
            setText("(" + tempString + ")");

        } else if (((IndependenceResult) value).isIndependent()) {
            setIcon(independentIcon);
            String tempString = displayDouble.format(((IndependenceResult) value).getPValue());
            setToolTipText("(" + tempString + ") : (p-value) the probability that the association" +
                    " in the sample is what we observed or more, assuming independence.");

            setText("(" + tempString + ")");

        } else {
            System.err.println("Pvalue " + value);
            System.err.println("is independent: " + ((IndependenceResult) value).isIndependent());
            throw new IllegalArgumentException("PValue must be independent or dependent!!!");

        }

        // This changes the background color with regards to whether its row has
        // been selected. When any difference in independencies is found in this
        // row, it will be highlighted by having its row selected.
        if (isSelected) {
            setBackground(Color.yellow);
        } else {
            setBackground(Color.white);
        }

        this.repaint();
    }


    /**
     * An Icon depicting an Independence
     */
    private static class IndependenceIcon implements Icon {
        private final Color symbolColor;
        private final Color slashColor;

        /**
         * Constructor. Creates the symbol.
         */
        public IndependenceIcon(Color symbolColor, Color slashColor) {
            this.symbolColor = symbolColor;
            this.slashColor = slashColor;
        }

        /**
         * Renders the symbol.
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            drawIndepSymbol(g, x, y, 12);
        }

        /**
         * @return icon width.
         */
        public int getIconWidth() {
            return 15;
        }

        /**
         * @return icon height.
         */
        public int getIconHeight() {
            return 15;
        }

//	private final Color indepNoneColor = new Color(220, 220, 200);

        /**
         * David Danks and Juan Casares
         */
        private void drawIndepSymbol(Graphics g, int x, int y, int ascent) {
            Misc.antialiasOn(g);

            /** independence symbol constants */
            final int INSET = 4;
            final int CENTER = 5;
            final int BASE = 2 * INSET + CENTER;
            final int DROP = 3;

            ((Graphics2D) g).setStroke(new BasicStroke(1.2f));
            g.setColor(symbolColor);

            /** base */
            g.drawLine(x, y + ascent, x + BASE, y + ascent);
            /** left bar */
            g.drawLine(x + INSET, y + DROP, x + INSET, y + ascent);
            /** right bar */
            g.drawLine(x + INSET + CENTER, y + DROP, x + INSET + CENTER, y + ascent);
            /** line through symbol, if needed */
            if (slashColor != null) {
                g.setColor(slashColor);
                g.drawLine(x + BASE, y + DROP, x, y + ascent);
            }
        }
    }


    /**
     * An Icon depicting an Independence
     */
    private static class HiddenIcon implements Icon {

        /**
         * Constructor.
         */
        public HiddenIcon() {
        }

        /**
         * Renders the hidden independence icon.
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            drawHiddenSymbol(c, g);
        }

        /**
         * @return the width of icon.
         */
        public int getIconWidth() {
            return 15;
        }

        /**
         * @return the height of icon.
         */
        public int getIconHeight() {
            return 15;
        }

        /**
         * David Danks and Juan Casares
         */
        private void drawHiddenSymbol(Component c, Graphics g) {
            // Misc.antialiasOn(g);

            ((Graphics2D) g).setStroke(new BasicStroke(1.2f));

            g.setColor(Color.GRAY);
            String qMark = "?";
            FontMetrics metrics = g.getFontMetrics();
            int width = metrics.stringWidth(qMark);
            int height = metrics.getHeight();
            g.drawString(qMark, c.getWidth() / 2 - width / 2, c.getHeight() / 2 + height / 2);
        }
    }
}
