package edu.cmu.causalityApp.dataEditors.independenciesEditor;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * This renders the hidden independencies in the cells as question marks.
 *
 * @author mattheweasterday
 */
class HiddenIndependenceCellRenderer extends DefaultTableCellRenderer {

    private static final Icon questionMarkIcon = new HiddenIndependenceCellRenderer.HiddenIcon();

    /**
     * Constructor.
     */
    public HiddenIndependenceCellRenderer() {
        super();
        setHorizontalAlignment(JLabel.CENTER);

    }

    /**
     * Matt: one thing this does is make the background of some of the independencies cells pink (which i disabled)
     */
    public Component getTableCellRendererComponent
    (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        return this;
    }


    /**
     * Set the icon of this cell to a question mark.
     */
    protected void setValue(Object value) {
        setIcon(questionMarkIcon);
    }


    /**
     * An Icon depicting an Independence
     */
    private static class HiddenIcon implements Icon {

        /**
         * Constructor to create the hidden icon for independency symbol.
         */
        public HiddenIcon() {
        }

        /**
         * Renders the icon.
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            drawHiddenSymbol(c, g);
        }

        /**
         * @return the icon width.
         */
        public int getIconWidth() {
            return 15;
        }

        /**
         * @return the icon height.
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

            //g2.setFont(thisFont);
            g.setColor(Color.ORANGE);
            String qMark = "?";
            FontMetrics metrics = g.getFontMetrics();
            int width = metrics.stringWidth(qMark);
            int height = metrics.getHeight();
            g.drawString(qMark, c.getWidth() / 2 - width / 2, c.getHeight() / 2 + height / 2);
        }
    }
}
