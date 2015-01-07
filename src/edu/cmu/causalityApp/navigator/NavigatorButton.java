package edu.cmu.causalityApp.navigator;

import javax.swing.*;
import java.awt.*;

/**
 * This class describes each navigator button on the panel.
 *
 * @author mattheweasterday
 */
class NavigatorButton extends JButton {

    private boolean isHidden;
    private boolean isHidable;
    private ImageIcon icon;
    private ImageIcon rollIcon;
    private ImageIcon downIcon;
    private ImageIcon iconHidden;
    private ImageIcon rollIconHidden;
    private ImageIcon downIconHidden;
    private final ImageIcon iconActive;
    private ImageIcon iconHiddenActive;

    /**
     * Constructor. Use this when the navigator button is visible.
     *
     * @param icon       this icon is the normal icon for this button.
     * @param rollIcon   the rollover icon.
     * @param downIcon   the mouse-down icon.
     * @param activeIcon the icon with a highlight when its associated frame is
     *                   in focus.
     */
    public NavigatorButton(Navigator listener, String command, ImageIcon icon, ImageIcon rollIcon, ImageIcon downIcon, ImageIcon activeIcon) {
        super(icon);
        isHidable = false;
        setHidden(false);
        this.icon = icon;
        this.rollIcon = rollIcon;
        this.downIcon = downIcon;
        this.iconHidden = null;
        this.rollIconHidden = null;
        this.downIconHidden = null;
        this.iconActive = activeIcon;

        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        addActionListener(listener);
        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setActionCommand(command);
        setBackground(Color.WHITE);
        setBorderPainted(false);
        setRolloverEnabled(true);
        setRolloverIcon(rollIcon);
        setPressedIcon(downIcon);
        setVerticalTextPosition(AbstractButton.CENTER);
        setHorizontalTextPosition(AbstractButton.CENTER);
        setToolTipText(command);
    }

    /**
     * Constructor. Use this to create a button which shows a hidden icon.
     */
    public NavigatorButton(Navigator listener, String command, ImageIcon icon, ImageIcon rollIcon, ImageIcon downIcon,
                           ImageIcon iconHidden, ImageIcon rollIconHidden, ImageIcon downIconHidden, ImageIcon iconActive, ImageIcon iconHiddenActive) {
        this(listener, command, icon, rollIcon, downIcon, iconActive);

        isHidable = true;
        setHidden(true);

        this.icon = icon;
        this.rollIcon = rollIcon;
        this.downIcon = downIcon;
        this.iconHidden = iconHidden;
        this.rollIconHidden = rollIconHidden;
        this.downIconHidden = downIconHidden;
        this.iconHiddenActive = iconHiddenActive;

        setIcon(iconHidden);
        //setRolloverEnabled(true);
        setRolloverIcon(rollIconHidden);
        setPressedIcon(downIconHidden);
        setToolTipText(command);
    }

    /**
     * Sets this button to show a hidden icon or not.
     */
    public void setHidden(boolean hidden) {
        isHidden = hidden;
        if (isHidable) {
            if (hidden) {
                setIcon(iconHidden);
                setRolloverIcon(rollIconHidden);
                setPressedIcon(downIconHidden);
            } else {
                setIcon(icon);
                setRolloverIcon(rollIcon);
                setPressedIcon(downIcon);
            }
        }
    }

    /**
     * Sets this button as active or not.
     */
    public void setActive(boolean active) {
        if (active) {
            if (isHidden)
                setIcon(iconHiddenActive);
            else
                setIcon(iconActive);
        } else {
            if ((isHidable) && (isHidden))
                setIcon(iconHidden);
            else
                setIcon(icon);
        }
    }
}