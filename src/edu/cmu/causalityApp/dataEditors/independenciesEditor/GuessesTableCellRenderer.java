package edu.cmu.causalityApp.dataEditors.independenciesEditor;


import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;

/**
 * This class renders the independence status in a comboBox-like image.
 *
 * @author Adrian Tang
 */
class GuessesTableCellRenderer extends CellRenderer {

    /**
     * Constructor.
     */
    public GuessesTableCellRenderer() {
        super();
        dependentIcon = createGuessIcon("guessedComboDependentIcon.gif");
        independentIcon = createGuessIcon("guessedComboIndependentIcon.gif");
        noneIcon = createGuessIcon("guessedComboIcon.gif");
    }

    private Icon createGuessIcon(String imgName) {
        Image image = ImageUtils.getImage(this, imgName);
        return new ImageIcon(image);
    }

}