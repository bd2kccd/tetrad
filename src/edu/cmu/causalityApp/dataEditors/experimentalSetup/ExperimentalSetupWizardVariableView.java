package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causalityApp.component.VarView;

import java.awt.*;
import java.util.ArrayList;

/**
 * To change this template use Options | File Templates.
 *
 * @author greg
 */
public class ExperimentalSetupWizardVariableView extends ExperimentalSetupVariableView {
    private final ExperimentalSetup studiedVariables;

    public ExperimentalSetupWizardVariableView(ExperimentalSetup studiedVariables) {
        this.studiedVariables = studiedVariables;
        refreshViews();
    }

    void refreshVars() {
        ArrayList<VarView> newVarViews = new ArrayList<VarView>();
        String variables[] = studiedVariables.getVariableNames();
        for (String varName : variables) {
            VarView view = new VarView(varName, false,
                    studiedVariables.getVariable(varName).getManipulation(),
                    studiedVariables.isVariableStudied(varName),
                    studiedVariables.getVariable(varName).getMean(),
                    studiedVariables.getVariable(varName).getStandardDeviation()
            );
            newVarViews.add(view);
        }
        setVarViews(newVarViews);

        setPositions();
    }

    protected void setPositions() {
        int y = 62;
        int x = 60;

        for (VarView view : getVarViews()) {
            view.setFinalPos(x, y);
            y += 50;
        }
    }

    public Dimension getPreferredSize() {
        int width = 200;
        int height = (studiedVariables.getNumVariables() + 1) * 50;
        return new Dimension(width, height);
    }
}
