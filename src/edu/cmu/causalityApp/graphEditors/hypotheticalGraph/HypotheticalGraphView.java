package edu.cmu.causalityApp.graphEditors.hypotheticalGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causalityApp.component.EdgeView;
import edu.cmu.causalityApp.component.Pos;
import edu.cmu.causalityApp.component.VarView;
import edu.cmu.causalityApp.graphEditors.GraphEditor;
import edu.cmu.causalityApp.graphEditors.GraphView;

import java.util.HashMap;
import java.util.List;

/**
 * To change this template use Options | File Templates.
 * <p/>
 * This class is the view component of a Hypothetical Graph.
 *
 * @author greg
 */
public class HypotheticalGraphView extends GraphView {

    /**
     * Name of this hg view.
     */
    protected String name;


    // ========================================================
    //    PUBLIC METHODS
    // ========================================================

    /**
     * Constructor.
     */
    public HypotheticalGraphView(String name, GraphEditor parent, CausalityLabModel model) {
        super(parent, model);
        this.name = name;
        this.parent = parent;

        refreshViews();
        GraphView.GraphInputHandler handler = new GraphView.GraphInputHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addKeyListener(handler);
    }

    /**
     * Refreshes the views of the variables and edges.
     */
    public void refreshViews() {
        final List<String> vars = model.getHypotheticalGraphVariableNames(name);
        final HashMap<String, VarView> varMap = new HashMap<String, VarView>();

        views.clear();

        refreshVars(varMap);
        setPositions(vars, varMap);
        refreshEdges(varMap);
    }

    /**
     * Removes the selected edge or variable from the model and updates the view.
     */
    public void removeSelected() {
        String name = ((VarView) selectedView).getName();
        if (selectedView instanceof VarView &&
                model.isHypotheticalGraphVariableLatent(this.name, name)) {

            model.removeLatentVariableFromHypotheticalGraph(((VarView) selectedView).getName(), this.name);
            selectedView = null;
        } else if (selectedView instanceof EdgeView) {
            EdgeView selectedEdge = (EdgeView) selectedView;
            model.removeEdgeFromHypotheticalGraph(selectedEdge.getFrom().getName(), selectedEdge.getTo().getName(), this.name);
            selectedView = null;
        }
        refreshViews();
        repaint();
    }

    /**
     * @return the Name of the view.
     */
    public String getName() {
        return name;
    }

    /**
     * Adds an edge from and to the given variables in the hypothetical graph
     * corresponding to this view in the given model and refreshes the view.
     *
     * @param fromName The name of the variable that the edge is from
     * @param toName   The name of the variable that the edge goes to
     */
    public void addEdge(String fromName, String toName) {
        if (!model.doesEdgeExist(toName, fromName, name)) {
            model.addHypotheticalGraphEdge(name, fromName, toName);
            refreshViews();
            repaint();
        }
    }

    // ========================================================
    //    PRIVATE METHODS
    // ========================================================

    /**
     * Refreshes the view of all the edges and stores them in an arraylist.
     *
     * @param varMap Hashmap of varviews
     */
    private void refreshEdges(final HashMap<String, VarView> varMap) {
        edgeViews.clear();
        for (EdgeInfo edge : model.getHypotheticalGraphEdges(name)) {
            VarView from = varMap.get(edge.getFromNode());
            VarView to = varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);
        }
    }

    /**
     * Refreshes the view of all variables and stores them in an arraylist and
     * hashmap.
     *
     * @param varMap Reference to the hashmap which stores the varviews
     */
    private void refreshVars(final HashMap<String, VarView> varMap) {
        varViews.clear();
        for (String varName : model.getHypotheticalGraphVariableNames(name)) {
            VarView view = new VarView(varName, model.isHypotheticalGraphVariableLatent(name, varName));
            view.setFinalPos(getHypPos(name, varName));
            varMap.put(varName, view);
            views.add(view);
            varViews.add(view);
        }
    }

    // ========================================================
    //    PROTECTED METHODS
    // ========================================================

    /**
     * Set the position of the variable view in the window.
     */
    protected void setLabFramePos(String varName, Pos pos) {
        setHypPos(name, varName, pos);
    }


}
