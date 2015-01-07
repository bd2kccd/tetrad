package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * This is the cell renderer for the header.
 *
 * @author mattheweasterday
 */
class HeaderRenderer extends JLabel implements TableCellRenderer {
    /**
     * The background color of the header.
     */
    private static final Color headerBackground = new Color(220, 233, 233);

    private final Icon icon;

    /**
     * Constructor.
     *
     * @param icon the icon to be displayed in the header cell. Use null if you
     *             want to just use text in the header cell.
     */
    public HeaderRenderer(Icon icon) {
        super(icon);
        setBackground(headerBackground);
        setHorizontalAlignment(JLabel.CENTER);
        setBorder(BorderFactory.createLineBorder(Color.white, 2));
        setOpaque(true);
        setFont(new Font("sansserif", Font.BOLD, 12));
        setVerticalAlignment(JLabel.BOTTOM);
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        this.icon = icon;
    }

    /**
     * Returns the default table cell renderer.
     *
     * @param table      the <code>JTable</code>
     * @param value      the value to assign to the cell at <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus   true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     * @return the default table cell renderer
     */
    public Component getTableCellRendererComponent
    (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Dimension textSize = getTextSize(value.toString());
        Dimension size;


        int iconWidth = 0;
        int iconHeight = 0;
        if (icon != null) {
            iconWidth = icon.getIconWidth();
            iconHeight = icon.getIconHeight();
        }

        final int width = Math.max(textSize.width, iconWidth);
        final int height = textSize.height + iconHeight + getIconTextGap();
        size = new Dimension(width, height);

        setMinimumSize(size);
        setPreferredSize(size);
        setText(value.toString());

        return this;
    }


    private Dimension getTextSize(String text) {
        if (text == null) return new Dimension(0, 0);
        FontMetrics metrics = getFontMetrics(getFont());
        final int width = metrics.stringWidth(text) + getInsets().left + getInsets().right + 1;
        final int height = metrics.getHeight() + getInsets().top + getInsets().bottom + 1;
        return new Dimension(width, height);
    }
}

