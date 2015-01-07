package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.util.ImageUtils;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;


/**
 * This is the toolbar for the GraphEditor.  Its tools are as follows:
 * <ul>
 * <li> The 'move' tool, allows the user to select and move items in the
 * workbench workbench.
 * <li> The 'addObserved' tool, allows the user to add new observed variables.
 * <li> The 'addLatent' tool, allows the user to add new latent variables.
 * <li> The 'addDirectedEdge' tool, allows the user to add new directed edges.
 * <li> The 'addUnorientedEdge' tool, allows the user to add new unoriented
 * edges.
 * <li> The 'addHalfdirectedEdge' tool, allows the user to create new half-
 * directed edges.
 * <li> The 'addBidirectedEdge' tool, allows the user to create new bidirected
 * edges.
 * </ul>
 *
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 * @version $Revision: 806 $ $Date: 2013-06-09 14:49:23 -0400 (Sun, 09 Jun 2013) $
 * @see edu.cmu.tetradapp.editor.GraphEditor
 */
public class BayesGraphToolbar extends GraphToolbar implements PropertyChangeListener, Serializable {

    /**
     * The mutually exclusive button group for the buttons.
     */
    private ButtonGroup group;

    /**
     * The panel that the buttons are in.
     */
    private final JPanel buttonsPanel = new JPanel();

    // The buttons in the toolbar.
    private JToggleButton move, addObserved, addLatent, addDirectedEdge;
    private ActionListener addObservedAL;
    private ActionListener addLatentAL;
    private ActionListener addDirectedEdgeAL;

    /**
     * The workbench this toolbar governs.
     */
    private GraphWorkbench workbench;

    /**
     * Constructs a new Graph toolbar governing the modes of the given
     * GraphWorkbench.
     */
    public BayesGraphToolbar(GraphWorkbench workbench) {
        if (workbench == null) {
            throw new NullPointerException();
        }

        this.workbench = workbench;
        group = new ButtonGroup();

        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        Border insideBorder = new MatteBorder(10, 10, 10, 10, getBackground());
        Border outsideBorder = new EtchedBorder();

        buttonsPanel.setBorder(new CompoundBorder(outsideBorder, insideBorder));

        // construct the bottons.
        move = new JToggleButton();
        addObserved = new JToggleButton();
        addLatent = new JToggleButton();
        addDirectedEdge = new JToggleButton();

        ActionListener moveAL = new ActionListener() {

            /**
             * Sets the flowbench tool to MOVE.
             */
            public void actionPerformed(ActionEvent e) {
                move.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.SELECT_MOVE);
            }
        };

        addObservedAL = new ActionListener() {

            /**
             * Sets the flowbench tool to GRAPH.
             */
            public void actionPerformed(ActionEvent e) {
                addObserved.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.ADD_NODE);
                setNodeMode(GraphWorkbench.MEASURED_NODE);
            }
        };

        addLatentAL = new ActionListener() {

            /**
             * Sets the flowbench tool to PM.
             */
            public void actionPerformed(ActionEvent e) {
                addLatent.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.ADD_NODE);
                setNodeMode(GraphWorkbench.LATENT_NODE);
            }
        };

        addDirectedEdgeAL = new ActionListener() {

            /**
             * Sets the flowbench tool to IM.
             */
            public void actionPerformed(ActionEvent e) {
                addDirectedEdge.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.ADD_EDGE);
                setEdgeMode(GraphWorkbench.DIRECTED_EDGE);
            }
        };


        // Adding this listener fixes a previous bug where if you
        // select a button and then move the mouse away from the
        // button without releasing the mouse it would deselect. J
        // Ramsey 11/02/01
        FocusListener focusListener = new FocusAdapter() {

            public void focusGained(FocusEvent e) {

                JToggleButton component = (JToggleButton) e.getComponent();

                component.doClick();
            }
        };

        move.addFocusListener(focusListener);
        addObserved.addFocusListener(focusListener);
        addLatent.addFocusListener(focusListener);
        addDirectedEdge.addFocusListener(focusListener);

        // add listeners
        move.addActionListener(moveAL);
        addObserved.addActionListener(addObservedAL);
        addLatent.addActionListener(addLatentAL);
        addDirectedEdge.addActionListener(addDirectedEdgeAL);

        // add buttons to the toolbar.
        addButton(move, "move");
        addButton(addObserved, "variable");
        addButton(addLatent, "latent");
        addButton(addDirectedEdge, "directed");
        workbench.addPropertyChangeListener(this);
        selectArrowTools();

        buttonsPanel.add(Box.createGlue());

        this.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(buttonsPanel);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Convenience method to set the mode of the workbench.  Placed
     * here because Java will not allow access to the variable
     * 'workbench' from inner classes.
     */
    private void setWorkbenchMode(int mode) {
        workbench.setWorkbenchMode(mode);
    }

    /**
     * Convenience method to set the mode of the workbench.  Placed
     * here because Java will not allow access to the variable
     * 'workbench' from inner classes.
     */
    private void setEdgeMode(int mode) {
        workbench.setEdgeMode(mode);
    }

    /**
     * Convenience method to set the mode of the workbench.  Placed
     * here because Java will not allow access to the variable
     * 'workbench' from inner classes.
     */
    private void setNodeMode(int mode) {
        workbench.setNodeType(mode);
    }

    /**
     * Adds the various buttons to the toolbar, setting their properties
     * appropriately.
     */
    private void addButton(JToggleButton jb, String name) {
        jb.setIcon(new ImageIcon(ImageUtils.getImage(this, name + "Up.gif")));
        jb.setRolloverIcon(new ImageIcon(ImageUtils.getImage(this, name + "Roll.gif")));
        jb.setPressedIcon(new ImageIcon(ImageUtils.getImage(this, name + "Down.gif")));
        jb.setSelectedIcon(new ImageIcon(ImageUtils.getImage(this, name + "Down.gif")));
        jb.setDisabledIcon(new ImageIcon(ImageUtils.getImage(this, name + "Off.gif")));
        jb.setRolloverEnabled(true);
        jb.setBorder(new EmptyBorder(1, 1, 1, 1));
        jb.setSize(100, 50);
        jb.setMinimumSize(new Dimension(100, 50));
        jb.setPreferredSize(new Dimension(100, 50));
        buttonsPanel.add(jb);
        group.add(jb);
    }


    /**
     * Responds to property change events.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ("graph".equals(e.getPropertyName())) {
            selectArrowTools();
        }
    }

    /**
     * For each workbench type, enables the arrow tools which that workbench can use
     * and disables all others.
     */
    private void selectArrowTools() {
        addDirectedEdge.setEnabled(true);
    }


    public void disableAddObserved() {
        addObserved.removeActionListener(addObservedAL);
    }

    public void disableAddLatent() {
        addLatent.removeActionListener(addLatentAL);
    }

    public void disableAddDirectedEdge() {
        addDirectedEdge.removeActionListener(addDirectedEdgeAL);
    }

}

