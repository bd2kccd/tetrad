package edu.cmu.causalityApp.exerciseBuilder;

import javax.swing.*;

/**
 * This describes the toggle icons used by the icons of the different navigator
 * buttons.
 *
 * @author mattheweasterday
 */
class ToggleIcon extends JLabel {
    private boolean showIcon = true;
    private final ImageIcon image;
    private final ImageIcon blankImage;

    /**
     * Constructor.
     *
     * @param image      the navigator button icon.
     * @param blankImage a blank icon to show when a navigator button is not
     *                   included in the exercise.
     */
    public ToggleIcon(ImageIcon image, ImageIcon blankImage) {
        super(image);
        this.image = image;
        this.blankImage = blankImage;
    }

    /**
     * Toggle between the actual icon and the blank icon.
     */
    public void toggle() {
        showIcon = !showIcon;
        this.setIcon(showIcon ? image : blankImage);
    }

    /**
     * @return if this icon is shown or not.
     */
    public boolean isShown() {
        return showIcon;
    }
}
