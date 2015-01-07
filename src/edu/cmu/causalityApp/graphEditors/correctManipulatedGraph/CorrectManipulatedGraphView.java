package edu.cmu.causalityApp.graphEditors.correctManipulatedGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.experimentalSetup.manipulation.Manipulation;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causalityApp.component.EdgeView;
import edu.cmu.causalityApp.component.Pos;
import edu.cmu.causalityApp.component.VarView;
import edu.cmu.causalityApp.graphEditors.CorrectPosition;
import edu.cmu.causalityApp.graphEditors.GraphView;
import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

/**
 * This class renders the view of the correct manipulated graph.
 *
 * @author greg
 */
public class CorrectManipulatedGraphView extends GraphView {
    private String expName;

    /**
     * Constructor.
     */
    public CorrectManipulatedGraphView(String expName, CorrectManipulatedGraphEditor parent, CausalityLabModel model) {
        super(parent, model);
        this.expName = expName;
        refreshViews();
    }

    /**
     * Draws the correct manipulated graph.
     */
    public void paintComponent(Graphics graphics) {
        if (getExpName() != null) {
            Graphics2D g = (Graphics2D) graphics;
            if (hidden) paintHiddenGraph(g);
            else {
                paintVars(g);
                paintEdges(g);
                g.setFont(g.getFont().deriveFont(10f));
                g.setColor(Color.black);
                g.drawString(getExpName(), 5, 10);
            }
        }
    }

    /**
     * Clears all the views and refreshes bopth the edges and variables.
     */
    void refreshViews() {
        if (getExpName() == null) return;

        final List<String> vars = model.getCorrectManipulatedGraphVariableNamesForExperiment(getExpName());
        final HashMap<String, VarView> varMap = new HashMap<String, VarView>();

        views.clear();

        refreshVars(varMap);
        setPositions(vars, varMap);
        refreshEdges(varMap);
        parent.pack();
    }

    /**
     * Refreshes the edge views.
     */
    private void refreshEdges(final HashMap<String, VarView> varMap) {
        edgeViews.clear();

        for (EdgeInfo edge : model.getCorrectManipulatedGraphActiveEdgesForExperiment(getExpName())) {
            VarView from = varMap.get(edge.getFromNode());
            VarView to = varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);
        }

        EdgeInfo edges[] = model.getCorrectManipulatedGraphBrokenEdges(getExpName());
        for (EdgeInfo edge : edges) {
            VarView from = varMap.get(edge.getFromNode());
            VarView to = varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);
        }

        EdgeInfo frozenEdges[] = model.getCorrectManipulatedGraphFrozenEdges(getExpName());
        for (EdgeInfo edge : frozenEdges) {
            VarView from = varMap.get(edge.getFromNode());
            VarView to = varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);
        }

    }

    /**
     * Refreshes the variable views.
     */
    protected void refreshVars(final HashMap<String, VarView> varMap) {
        varViews.clear();
        for (String varName : model.getCorrectManipulatedGraphVariableNamesForExperiment(expName)) {
            boolean latent = model.isCorrectGraphVariableLatent(varName);
            boolean studied;
            VarView view;
            Manipulation manip;

            if (!latent) {
                studied = model.isVariableInExperimentStudied(expName, varName);
                manip = model.getExperimentalVariableManipulation(expName, varName);
            } else {
                studied = true;
                manip = new Manipulation(ManipulationType.NONE);
            }

            view = new VarView(varName, latent, manip, studied);
            view.setFinalPos(CorrectPosition.getInstance().getCorrectPos(varName));
            varMap.put(varName, view);
            views.add(view);
            varViews.add(view);
        }
    }

    /**
     * Remove selected edge or variable. Not used in this editor as graph is not
     * editable.
     */
    public void removeSelected() {
    }

    /**
     * Adds an edge. Not used in this editor as graph is not
     * editable.
     */
    public void addEdge(String fromName, String toName) {
    }

    /**
     * Stores the position of the given variable in <code>CorrectPosition</code>.
     */
    protected void setLabFramePos(String varName, Pos pos) {
        CorrectPosition.getInstance().setCorrectPos(varName, pos);
    }

    /**
     * Set the getModel experimental setup.
     */
    public void setExpName(String expName) {
        this.expName = expName;
        if (!hidden) {
            refreshViews();
            repaint();
        }
    }

    /**
     * @return the getModel experimental setup.
     */
    public String getExpName() {
        return this.expName;
    }

    /**
     * Inner class to render the hidden view.
     */
    public static class HiddenView extends CorrectManipulatedGraphView {
        private static String hiddenImageFile = "hiddenSign.gif";
        private ImageIcon hiddenImage = new ImageIcon(ImageUtils.getImage(this, hiddenImageFile));

        /**
         * Refreshes the variables.
         */
        @Override
        protected void refreshVars(HashMap<String, VarView> varMap) {
            varViews.clear();
            for (String varName : model.getCorrectGraphVariableNames()) {
                if (!model.isCorrectGraphVariableLatent(varName)) {
                    VarView view = new VarView(varName, model.isCorrectGraphVariableLatent(varName));

                    view.setFinalPos(CorrectPosition.getInstance().getCorrectPos(varName));
                    varMap.put(varName, view);
                    views.add(view);
                    varViews.add(view);
                }
            }
        }

        /**
         * No edges painted.
         */
        protected void paintEdges(Graphics2D g) {
            /** do nothing */
        }

        /**
         * Draws the hidden view.
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(hiddenImage.getImage(), (getWidth() - hiddenImage.getIconWidth()) / 2, (getHeight() - hiddenImage.getIconHeight()) / 2, null);
        }

        /**
         * Constructor.
         */
        public HiddenView(String expName, CorrectManipulatedGraphEditor parent, CausalityLabModel model) {
            super(expName, parent, model);
        }
    }


}
