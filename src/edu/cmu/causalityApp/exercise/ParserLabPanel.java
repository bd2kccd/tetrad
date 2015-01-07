package edu.cmu.causalityApp.exercise;

import edu.cmu.causalityApp.commands.ShowAnswerPanel;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;

import javax.swing.*;

/**
 * @author mykoh
 */
public interface ParserLabPanel extends ShowAnswerPanel {

    /**
     * @param name of the editor
     * @return the editor pane
     */
    public AbstractEditor getEditor(String name);

    /**
     * @return JDesktopPane
     */
    public JDesktopPane getDesktop();

    public JComponent asJComponent();
}
