package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import edu.cmu.causalityApp.util.Misc;

import javax.swing.*;
import java.awt.*;


/**
 * This class renders the independency symbol in the cells.
 *
 * @author mattheweasterday
 */
class IndependenceCellRenderer extends CellRenderer {
    private final boolean isStudentGuessEnabled;
    private static final Icon questionMarkIcon = new HiddenIcon();

    /**
     * Constructor.
     */
    public IndependenceCellRenderer(boolean isStudentGuessEnabled) {
        super();
        independentIcon = new IndependenceCellRenderer.IndependenceIcon(Color.black, null);
        dependentIcon = new IndependenceCellRenderer.IndependenceIcon(Color.black, Color.red);
        noneIcon = new IndependenceCellRenderer.IndependenceIcon(noneColor, noneColor);

        this.isStudentGuessEnabled = isStudentGuessEnabled;
    }

    /**
     * Set the value of the table cell.
     */
    public void setValue(Object value) {
        if (value == null) {
            if (isStudentGuessEnabled) {
                setIcon(questionMarkIcon);
            } else {
                setIcon(noneIcon);
            }
        } else if (!(Boolean) value) {
            setIcon(dependentIcon);
        } else {
            setIcon(independentIcon);
        }

        // This changes the background color with regards to whether its row has
        // been selected. When any difference in independencies is found in this
        // row, it will be highlighted by having its row selected.
        if (isSelected) {
            setBackground(Color.yellow);
        } else {
            setBackground(Color.white);
        }

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

