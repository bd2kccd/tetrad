package edu.cmu.causalityApp.graphEditors.hypotheticalManipulatedGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.experimentalSetup.manipulation.Manipulation;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causalityApp.component.EdgeView;
import edu.cmu.causalityApp.component.Pos;
import edu.cmu.causalityApp.component.VarView;
import edu.cmu.causalityApp.graphEditors.GraphView;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * To change this template use Options | File Templates.
 * <p/>
 * This class describes the view of the hypothetical manipulated graph.
 *
 * @author greg
 */
public class HypotheticalManipulatedGraphView extends GraphView {

    private String expName;
    private String hypName;

    /**
     * Constructor.
     *
     * @param expName
     * @param hypName
     * @param parent
     * @param model
     */
    public HypotheticalManipulatedGraphView(String expName, String hypName, HypotheticalManipulatedGraphEditor parent, CausalityLabModel model) {
        //super (expName, parent, model);
        super(parent, model);
        this.expName = expName;
        this.hypName = hypName;
        refreshViews();
    }

    /**
     * Draws this graph view.
     *
     * @param graphics
     */
    public void paintComponent(Graphics graphics) {
        if (hypName != null && (getExpName() != null)) {
            Graphics2D g = (Graphics2D) graphics;
            paintVars(g);
            paintEdges(g);
            g.setFont(g.getFont().deriveFont(10f));
            g.setColor(Color.black);
            g.drawString(getExpName(), 5, 10);
            g.drawString(model.getHypotheticalGraphName(hypName), 5, 20);
        }
    }

    /**
     * Clears all the views and refreshes bopth the edges and variables.
     */
    void refreshViews() {
        if ((hypName == null) || (getExpName() == null)) return;

        final List vars = model.getHypotheticalManipulatedGraphVariableNames(getExpName(), hypName);
        final HashMap varMap = new HashMap();

        views.clear();

        refreshVars(varMap);
        setPositions(vars, varMap);
        refreshEdges(varMap);
        parent.pack();
    }

    /**
     * Refreshes the edge views.
     *
     * @param varMap
     */
    private void refreshEdges(final HashMap varMap) {
        edgeViews.clear();
        for (Iterator i = model.getHypotheticalManipulatedGraphActiveEdges(getExpName(), hypName).listIterator(); i.hasNext(); ) {
            EdgeInfo edge = (EdgeInfo) i.next();

            VarView from = (VarView) varMap.get(edge.getFromNode());
            VarView to = (VarView) varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);
        }

        EdgeInfo edges[] = model.getHypotheticalManipulatedGraphBrokenEdges(getExpName(), hypName);
        for (int i = 0; i < edges.length; i++) {
            EdgeInfo edge = edges[i];

            VarView from = (VarView) varMap.get(edge.getFromNode());
            VarView to = (VarView) varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);

        }

        EdgeInfo frozenEdges[] = model.getHypotheticalManipulatedGraphFrozenEdges(getExpName(), hypName);
        for (int i = 0; i < frozenEdges.length; i++) {
            EdgeInfo edge = frozenEdges[i];

            VarView from = (VarView) varMap.get(edge.getFromNode());
            VarView to = (VarView) varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);

        }
    }

    /**
     * Refreshes the variable views.
     *
     * @param varMap
     */
    private void refreshVars(final HashMap varMap) {
        varViews.clear();
        for (Iterator i = model.getHypotheticalManipulatedGraphVariableNames(expName, hypName).iterator(); i.hasNext(); ) {
            String varName = (String) i.next();

            boolean latent = model.isHypotheticalGraphVariableLatent(hypName, varName);
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
            view.setFinalPos(getHypPos(hypName, varName));
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
     * Adds a latent variable. Not application to this editor.
     *
     * @param newName
     */
    public void addLatent(String newName) {
    }

    /**
     * Adds an edge. Not used in this editor as graph is not
     * editable.
     */
    public void addEdge(String fromName, String toName) {
    }

    /**
     * Sets the position of the variable in this view
     *
     * @param varName
     * @param pos
     */
    protected void setLabFramePos(String varName, Pos pos) {
        setHypPos(hypName, varName, pos);
    }

    /**
     * Sets the experimental setup currently in focus.
     *
     * @param expName
     */
    public void setExpName(String expName) {
        this.expName = expName;
        refreshViews();
        repaint();
    }

    /**
     * @return the name of the experimental setup currently in focus.
     */
    public String getExpName() {
        return this.expName;
    }

    /**
     * Sets the hypothetical graph currently in focus.
     *
     * @param hypName
     */
    public void setHypName(String hypName) {
        this.hypName = hypName;
        refreshViews();
        repaint();
    }

}
