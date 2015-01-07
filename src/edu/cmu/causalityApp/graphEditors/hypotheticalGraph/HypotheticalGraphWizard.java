package edu.cmu.causalityApp.graphEditors.hypotheticalGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.CreateHypothesisCommand;
import edu.cmu.causality.EditHypothesisCommand;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causalityApp.graphEditors.GraphEditor;
import edu.cmu.causalityApp.util.ImageUtils;
import edu.cmu.command.Command;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the Hypothetical Graph Wizard window for creating and
 * editing hyp graphs.
 *
 * @author greg
 */
public class HypotheticalGraphWizard extends GraphEditor {

    /**
     * Name of this editor window
     */
    private static final String MY_NAME = "Hypothetical Graph Wizard";

    private JToggleButton move;
    private JToggleButton drawEdge;
    private int latents = 0;
    private HypotheticalGraphEditor editor;
    private boolean isNewGraph;


    // ========================================================
    //    PUBLIC METHODS
    // ========================================================

    /**
     * @return static wizard window for a new hyp graph
     */
    public static HypotheticalGraphWizard makeNewGraph(
            CausalityLabModel model,
            InternalFrameListener parent,
            JDesktopPane desktop,
            HypotheticalGraphEditor editor,
            String name) {
        return new HypotheticalGraphWizard(model, parent, desktop, editor, name, true);
    }

    /**
     * @return static wizard window for editing an existing hyp graph
     */
    public static HypotheticalGraphWizard editExistingGraph(
            CausalityLabModel model,
            InternalFrameListener parent,
            JDesktopPane desktop,
            HypotheticalGraphEditor editor,
            String name) {

        return new HypotheticalGraphWizard(model, parent, desktop, editor, name, false);
    }

    /**
     * @return String ID of this editor window.
     */
    public String getEditorName() {
        return MY_NAME;
    }

    /**
     * Logs the moves object for creating or editing a hyp graph.
     */
    void done(HypotheticalGraphEditor editor) {
        HypotheticalGraph data = ((HypotheticalGraphWizardView) getMainView()).getGraph();
        if (isNewGraph) {
            Command cmd = new CreateHypothesisCommand(data);
            cmd.doIt();
        } else {
            System.out.println(this.getClass() + " not new graph ");
            System.out.println(data);
            Command cmd = new EditHypothesisCommand(data);
            cmd.doIt();
        }

        /*
        HashMap positions = ((HypotheticalGraphWizardView) mainView).getPositions();

        for (Iterator i = positions.keySet().iterator(); i.hasNext(); ) {
        String varName = (String) i.next();
        parent.setHypPos(data.getName(), varName, (Pos) positions.get(varName));
        }

        if(isNewGraph){   editor.setNewHypotheticalGraph(data.getName());
        } else { editor.setOldHypotheticalGraph(data.getName());}
        */
        //editor.editHypotheticalGraph(hypotheticalGraphId, isNewGraph ? "New " + data.getName() : "Edit " + data.getName());
    }

    /**
     * Set the editing mode of the window.
     *
     * @param editMode Includes "Move" to allow mode to move variables
     */
    public void setEditMode(String editMode) {
        super.setEditMode(editMode);
        if (editMode.equals(GraphEditor.EDIT_MOVE)) {
            move.setSelected(true);
            drawEdge.setSelected(false);
        }
    }

    /**
     * Adds an edge from and to the given variables in the hypothetical graph.
     *
     * @param fromName The name of the variable that the edge is from
     * @param toName   The name of the variable that the edge goes to
     */
    public void addEdge(String fromName, String toName) {
        ((HypotheticalGraphWizardView) getMainView()).addEdge(fromName, toName);
    }


    // ========================================================
    //    PRIVATE METHODS
    // ========================================================

    /**
     * Use this constructor when you are going to edit existing hypothesis.
     */
    private HypotheticalGraphWizard(CausalityLabModel model,
                                    InternalFrameListener parent,
                                    JDesktopPane desktop,
                                    HypotheticalGraphEditor editor,
                                    String name,
                                    boolean isNewGraph) {

        super(model, parent, desktop, MY_NAME, true);
        this.editor = editor;
        if (isNewGraph) {
            setMainView(new HypotheticalGraphWizardView(model, this, name));
        } else {
            setMainView(new HypotheticalGraphWizardView(model, this, name, true));
        }
        getContentPane().add(getMainView(), BorderLayout.CENTER);
        setClosable(false);
        setResizable(true);
        setEditMode(GraphEditor.EDIT_MOVE);

        this.isNewGraph = isNewGraph;
    }


    // ========================================================
    //    PROTECTED METHODS
    // ========================================================

    /**
     * Create the toolbar for this window.
     */
    protected void fillToolbar() {
        ImageIcon moveIcon = new ImageIcon(ImageUtils.getImage(this, "move.gif")); //createImageIcon ("images/move.gif");
        move = new JToggleButton(moveIcon);
        ImageIcon addLatentIcon = new ImageIcon(ImageUtils.getImage(this, "add_latent.gif"));
        final JToggleButton addLatent = new JToggleButton(addLatentIcon);
        ImageIcon drawEdgeIcon = new ImageIcon(ImageUtils.getImage(this, "blackedge.gif"));
        drawEdge = new JToggleButton(drawEdgeIcon);
        ImageIcon deleteIcon = new ImageIcon(ImageUtils.getImage(this, "eraser.gif"));
        final JToggleButton delete = new JToggleButton(deleteIcon);
        JButton cancel = new JButton("Cancel");
        JButton done = new JButton("Done");

        getToolbar().add(move);
        getToolbar().add(addLatent);
        getToolbar().add(drawEdge);
        getToolbar().add(delete);
        getToolbar().add(Box.createHorizontalGlue());
        getToolbar().add(cancel);
        getToolbar().add(done);

        move.setSelected(true);
        setEditMode("Move");

        move.setToolTipText("Select / move");
        move.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        drawEdge.setSelected(false);
                        move.setSelected(true);
                        setEditMode(GraphEditor.EDIT_MOVE);
                    }
                }
        );

        drawEdge.setToolTipText("Draw edges");
        drawEdge.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        move.setSelected(false);
                        drawEdge.setSelected(true);
                        setEditMode(GraphEditor.EDIT_DRAW);
                    }
                }
        );

        addLatent.setToolTipText("Add a latent variable");
        addLatent.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        addLatentVar();
                        addLatent.setSelected(false);
                    }
                }
        );

        delete.setToolTipText("Delete selected");
        delete.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        ((HypotheticalGraphWizardView) getMainView()).removeSelected();
                        delete.setSelected(false);
                    }
                }
        );

        cancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        dispose();
                    }
                }
        );

        done.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        done(editor);
                        editor.pack();
                        dispose();
                    }
                }
        );

    }

    /**
     * Adds a latent variable to the hypothetical graph.
     */
    void addLatentVar() {
        String newName = (String) JOptionPane.showInputDialog(getDesktopPane(), "Enter a name:", "Add Latent Var", JOptionPane.PLAIN_MESSAGE, null, null, "L" + (latents + 1));   //$NON-NLS-3$
        while (newName != null && newName.length() > 20) {
            JOptionPane.showMessageDialog(getDesktopPane(), "Variable names cannot be more than 20 characters", "Error", JOptionPane.WARNING_MESSAGE);
            newName = (String) JOptionPane.showInputDialog(getDesktopPane(), "Enter a name:", "Add Latent Var", JOptionPane.PLAIN_MESSAGE, null, null, "L" + (latents + 1));   //$NON-NLS-3$
        }
        if (newName != null) {
            ((HypotheticalGraphWizardView) getMainView()).addLatent(newName);
            latents++;
        }
    }


}
