package edu.cmu.causalityApp;

import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is the initial splash screen appearing before the lab is launched.
 * Besides displaying a splash screen, it allows user to select what language
 * locale to use for the lab.
 *
 * @author Adrian Tang
 */
class SplashScreen extends JFrame {
    final private static String SPLASH_FILENAME = "clsplashV4_0.gif";
    private static boolean isDone = false;

    /**
     * Keeps looping the display of the splash screen until the user clicks Run.
     */
    public static void showSplashScreen() {
        new SplashScreen();
        //wait until user clicked ok..
        while (!isDone) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                // nothing, keep going
            }
        }

    }

    private SplashScreen() {
        try {
            Image splash = ImageUtils.getImage(this, SPLASH_FILENAME);
            ImagePane chooser = new ImagePane(splash);
            getContentPane().add(chooser, BorderLayout.NORTH);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        JButton okButton = new JButton("Run");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                isDone = true;
            }
        });

        getContentPane().add(okButton, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }


}
