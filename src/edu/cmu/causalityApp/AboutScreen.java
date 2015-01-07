package edu.cmu.causalityApp;

import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * This uses a JWindow class to display an About Screen with credits. The About
 * screen can be accessed via Help > About Causality Lab. It simply displays an
 * image containing all the credits.
 *
 * @author adrian tang
 */
class AboutScreen extends JWindow {

    final private static String ABOUT_FILENAME = "clAbout.gif";

    /**
     * Creates an instance of the about screen.
     */
    public static void showAboutScreen() {
        new AboutScreen();
    }

    private AboutScreen() {

        // Load image file
        Image splash = ImageUtils.getImage(this, ABOUT_FILENAME);
        ImagePane image = new ImagePane(splash);
        getContentPane().add(image, BorderLayout.CENTER);

        // Set the about screen to the center of the application window
        Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = image.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));

        // Add mouse listener so that it closes on click
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setVisible(false);
                dispose();
            }
        });

        pack();
        setVisible(true);
    }
}
