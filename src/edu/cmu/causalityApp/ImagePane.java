/*
 * Class $(NAME)
 * Author: Mathilde
 * Date: Oct 30, 2002
 * Time: 11:10:18 AM
 */
package edu.cmu.causalityApp;

import javax.swing.*;
import java.awt.*;

/**
 * This is the container class for displaying an image. This is used by the
 * AboutScreen and SplashScreen to display the image.
 *
 * @author adrian tang
 */
class ImagePane extends JComponent {
    private final Image image;

    /**
     * Constructor.
     */
    public ImagePane(Image image) {
        this.image = image;

        // Uses ImageIcon to grab the width and height of the image used
        ImageIcon imgi = new ImageIcon(image);
        setPreferredSize(new Dimension(imgi.getIconWidth(), imgi.getIconHeight()));
    }

    /**
     * Renders the view.
     */
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

}

