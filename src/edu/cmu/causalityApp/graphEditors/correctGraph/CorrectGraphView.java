package edu.cmu.causalityApp.graphEditors.correctGraph;

import edu.cmu.causality.CausalityLabModel;
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
 * @author greg
 */
public class CorrectGraphView extends GraphView {
    /**
     * Name of this cg view.
     */
    private String name;
    private static String hiddenImageFile = "hiddenSign.gif";
    private ImageIcon hiddenImage = new ImageIcon(ImageUtils.getImage(this, hiddenImageFile));

    public CorrectGraphView(String name, CorrectGraphEditor parent, CausalityLabModel model) {
        super(parent, model);
        this.name = name;

        refreshViews();
    }

    /**
     * Clears all the views and refreshes both the edges and variables.
     */
    void refreshViews() {
        final List<String> vars = model.getCorrectGraphVariableNames();
        final HashMap<String, VarView> varMap = new HashMap<String, VarView>();

        views.clear();
        if (!hidden) {
            refreshVars(varMap);
        } else {
            refreshVarsHiden(varMap);
        }
        // todo: changed here for godmode
        if (vars != null)
            setPositions(vars, varMap);
        if (!hidden) {
            refreshEdges(varMap);
        }
    }

    /**
     * Refreshes the variables.
     */
    void refreshVarsHiden(HashMap<String, VarView> varMap) {
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
     * Refreshes the edge views.
     */
    void refreshEdges(final HashMap<String, VarView> varMap) {
        edgeViews.clear();
        if (model.getCorrectGraphEdges() == null)
            return;
        for (EdgeInfo edge : model.getCorrectGraphEdges()) {
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
    void refreshVars(final HashMap<String, VarView> varMap) {
        int x = -1;
        int y = -1;

        varViews.clear();
        // todo: changed. for god mode
        if (model.getCorrectGraphVariableNames() == null) {
            return;
        }
        for (String varName : model.getCorrectGraphVariableNames()) {
            VarView view = new VarView(varName, model.isCorrectGraphVariableLatent(varName));

            if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
                System.out.println("varName" + varName);
                System.out.println("model" + model);
                System.out.println("pmcopy" + model.getCorrectGraphBayesPmCopy());
                System.out.println("getdag" + model.getCorrectGraphBayesPmCopy().getDag());
                System.out.println("getnode" + model.getCorrectGraphBayesPmCopy().getDag().getNode(varName));
                System.out.println("getcx" + model.getCorrectGraphBayesPmCopy().getDag().getNode(varName).getCenterX());
                x = model.getCorrectGraphBayesPmCopy().getDag().getNode(varName).getCenterX();
                y = model.getCorrectGraphBayesPmCopy().getDag().getNode(varName).getCenterY();

            } else if (model.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
                x = model.getCorrectGraphSemPmCopy().getGraph().getNode(varName).getCenterX();
                y = model.getCorrectGraphSemPmCopy().getGraph().getNode(varName).getCenterY();

            }
            CorrectPosition.getInstance().setCorrectPos(varName, new Pos(x, y));

            view.setFinalPos(CorrectPosition.getInstance().getCorrectPos(varName));
            varMap.put(varName, view);
            views.add(view);
            varViews.add(view);
        }

    }

    /**
     * Renders the view.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hidden) {
            g.drawImage(getHiddenImage().getImage(), (getWidth() - getHiddenImage().getIconWidth()) / 2, (getHeight() - getHiddenImage().getIconHeight()) / 2, null);
        }
    }

    public ImageIcon getHiddenImage() {
        return hiddenImage;
    }
    /* //////////////////////////////////////////////////////////////////////////////////
    */

    /**
     * Inner class to render the hidden correct graph.
     */
    public static class HiddenView extends CorrectGraphView {
        /**
         *
         */
        private static final long serialVersionUID = 5175447387492611162L;


        /**
         * Don't draw any edges for this hidden view.
         */
        void refreshEdges(HashMap<String, VarView> varMap) {
            /** do nothing */
        }

        /**
         * Refreshes the variables.
         */
        void refreshVars(HashMap<String, VarView> varMap) {
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
         * No edges.
         */
        protected void paintEdges(Graphics2D g) {
            /** do nothing */
        }

        /**
         * Renders the view.
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(getHiddenImage().getImage(), (getWidth() - getHiddenImage().getIconWidth()) / 2, (getHeight() - getHiddenImage().getIconHeight()) / 2, null);
        }

        /**
         * Constructor.
         */
        public HiddenView(String name, CorrectGraphEditor parent, CausalityLabModel model) {
            super(name, parent, model);
        }
    }

    /**
     * accessor getName() to retrieve the name of this specific CorrectgraphView
     */

    public String getName() {
        return name;

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
}
