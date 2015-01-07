package edu.cmu.causalityApp.dataEditors;

import javax.swing.*;

/**
 * This class creates an icon of the hypothetical manipulated graph.
 *
 * @author mattheweasterday
 */
public interface HypotheticalManipulatedGraphIconMaker {

    /**
     * Constructor.
     *
     * @return the ImageIcon of the hypothetical manipulated graph.
     */
    abstract public ImageIcon getHypotheticalManipulatedGraphIcon(String expName, String hypName);
}
