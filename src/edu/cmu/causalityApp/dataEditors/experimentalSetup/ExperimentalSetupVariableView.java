package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.component.VarView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * To change this template use Options | File Templates.
 *
 * @author greg
 */
public class ExperimentalSetupVariableView extends JComponent {
    private String name;
    private CausalityLabModel model;

    private ArrayList<VarView> varViews = new ArrayList<VarView>();

    private static final int PREFERED_WIDTH = 240;
    private static final int PREFERED_HEIGHT = 240;

    ExperimentalSetupVariableView() {
    }

    public ExperimentalSetupVariableView(CausalityLabModel model, String name) {
        this.model = model;
        this.name = name;
        refreshVars();
    }

    public Dimension getPreferredSize() {
        return new Dimension(PREFERED_WIDTH, PREFERED_HEIGHT);
    }

    void refreshViews() {
        refreshVars();
        repaint();
    }

    void refreshVars() {
        ArrayList<VarView> newVarViews = new ArrayList<VarView>();
        String[] vars = model.getExperimentalSetupVariableNames(name);
        for (String varName : vars) {
            VarView view = new VarView(varName, false, model.getExperimentalVariableManipulation(name, varName), model.isVariableInExperimentStudied(name, varName));
            newVarViews.add(view);
        }
        setVarViews(newVarViews);

        setPositions();
    }

    void setPositions() {
        int y = 30;
        int x = 35;
        int vspace = 24; /*(PREFERED_HEIGHT - 30 - varViews.size () * 25) / varViews.size ();*/

        for (VarView view : getVarViews()) {
            view.setFinalPos(x, y);
            y += getVarHeight(view) + vspace;
        }
    }

    /**
     * Returns the height of the variable, or VarView.PREFERRED_HEIGHT if null
     *
     * @param varView The variable whose height is to be determined
     * @return The height of the variable, or VarView.PREFERRED_HEIGHT if null
     */
    int getVarHeight(VarView varView) {
        if (varView == null) return VarView.PREFERED_HEIGHT;
        return varView.getSize().height;
    }

    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        for (VarView view : getVarViews()) {
            view.paintComponent(g);
        }

    }

    public List<VarView> getList() {
        return getVarViews();
    }

    public ArrayList<VarView> getVarViews() {
        return varViews;
    }

    public void setVarViews(ArrayList<VarView> varViews) {
        this.varViews = varViews;
    }
}
