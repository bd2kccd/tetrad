package edu.cmu.causalityApp.graphEditors.hypotheticalGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causalityApp.component.EdgeView;
import edu.cmu.causalityApp.component.Pos;
import edu.cmu.causalityApp.component.VarView;
import edu.cmu.causalityApp.graphEditors.GraphView;
import edu.cmu.tetrad.graph.Edge;

import java.util.*;

/**
 * This class describes the view of the hyp graph that is currently being edited.
 *
 * @author greg
 */
public class HypotheticalGraphWizardView extends GraphView {

    private final HypotheticalGraph data;


    // ========================================================
    //    PUBLIC METHODS
    // ========================================================

    /**
     * Call this constructor to make a view from scratch.
     */
    public HypotheticalGraphWizardView(CausalityLabModel model,
                                       HypotheticalGraphWizard parent,
                                       String name) {
        this(model, parent, model.getEmptyHypotheticalGraph(name));

        refreshViews();
    }

    /**
     * Call this constructor to use a view from the model.
     */
    public HypotheticalGraphWizardView(CausalityLabModel model,
                                       HypotheticalGraphWizard parent,
                                       String hypName,
                                       boolean dummy) {
        this(model, parent, model.getHypotheticalGraphCopy(hypName));
        Iterator<String> i;

        for (i = model.getHypotheticalGraphVariableNames(hypName).iterator(); i.hasNext(); ) {
            String varName = i.next();
            Pos pos = getHypPos(hypName, varName);
            data.setVariableCenter(varName, pos.getX(), pos.getY());
        }

        refreshViews();
    }

    /**
     * @return The corresponding hyp graph model class.
     */
    public HypotheticalGraph getGraph() {
        return data;
    }

    /**
     * Set the position of the variable in this view.
     */
    public void setLabFramePos(String varName, Pos pos) {
        setPos(varName, pos);
    }

    /**
     * Remove selected variable from the model and clear it from the view.
     */
    public void removeSelected() {
        if (selectedView instanceof VarView && data.isVariableLatent(((VarView) selectedView).getName())) {
            data.removeLatentVariable(((VarView) selectedView).getName());
            selectedView = null;
        } else if (selectedView instanceof EdgeView) {
            EdgeView selectedEdge = (EdgeView) selectedView;
            data.removeEdge(data.getNode(selectedEdge.getFrom().getName()), data.getNode(selectedEdge.getTo().getName()));
            selectedView = null;
        }
        refreshViews();
        repaint();
    }

    /**
     * Adds an edge from and to the given variables in the hypothetical graph
     * corresponding to this view in the given model and refreshes the view.
     *
     * @param from The name of the variable that the edge is from
     * @param to   The name of the variable that the edge goes to
     */
    public void addEdge(String from, String to) {
        data.addDirectedEdge(data.getNode(from), data.getNode(to));
        refreshViews();
    }

    /**
     * Add a latent variable in the model and view with the given name.
     */
    public void addLatent(String name) {
        data.addLatentVariable(name);
        refreshViews();
        repaint();
    }

    /**
     * @return Position of the variable with varName.
     */
    Pos getPos(String varName) {
        //return (Pos) positions.get(varName);
        return new Pos(data.getVariableCenterX(varName), data.getVariableCenterY(varName));
    }

    /**
     * Set the position of the variable in the window
     */
    void setPos(String varName, Pos pos) {
        data.setVariableCenter(varName, pos.getX(), pos.getY());
        //positions.put(varName, pos);
    }


    // ========================================================
    //    PRIVATE METHODS
    // ========================================================

    /**
     * Private constructor
     */
    private HypotheticalGraphWizardView(CausalityLabModel model,
                                        HypotheticalGraphWizard parent,
                                        HypotheticalGraph hypGraph) {
        super(parent, model);
        data = hypGraph;

        GraphView.GraphInputHandler handler = new GraphView.GraphInputHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addKeyListener(handler);
    }

    /**
     * Refresh the view of variables and edges in the hyp graph.
     */
    private void refreshViews() {
        //final List vars = data.getVariableNames();
        final String[] vars = data.getVariableNamesFromGraph();
        final List<String> varNames = new ArrayList<String>();
        final HashMap<String, VarView> varMap = new HashMap<String, VarView>();

        views.clear();
        refreshVars(varMap);
        //maybe only do this if positions aren't already set....
        Collections.addAll(varNames, vars);
        setPositions(varNames, varMap);
        refreshEdges(varMap);
    }

    /**
     * Refresh the view of edges in the hyp graph.
     */
    private void refreshEdges(final HashMap<String, VarView> varMap) {
        edgeViews.clear();
        for (Edge edge1 : data.getEdges()) {
            EdgeInfo edge = new EdgeInfo((edge1));
            VarView from = varMap.get(edge.getFromNode());
            VarView to = varMap.get(edge.getToNode());
            final EdgeView view = new EdgeView(from, to, edge);
            views.add(view);
            edgeViews.add(view);
        }
    }

    /**
     * Refresh the view of variables in the hyp graph.
     */
    private void refreshVars(final HashMap<String, VarView> varMap) {
        varViews.clear();
        String[] varNames = data.getVariableNamesFromGraph();

        for (String varName : varNames) {
            VarView view = new VarView(varName, data.isVariableLatent(varName));
            Pos pos = getPos(varName);
            if (pos == null) {
                data.setVariableCenter(varName, -1, -1);
            }
            view.setFinalPos(pos);
            varMap.put(varName, view);
            views.add(view);
            varViews.add(view);
        }
    }


    // ========================================================
    //    PROTECTED METHODS
    // ========================================================

    /**
     * Lays out the vars.
     *
     * @param vars   A list containing variable names
     * @param varMap A HashMap
     */
    protected void setPositions(List vars, HashMap varMap) {
        setPositionsCirc(vars, varMap);  // use this line for circle layout
        //setPositionsCol (vars, varMap);   // use this line for column layout
    }


}
