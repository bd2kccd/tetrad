package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.RegressionInfo;
import edu.cmu.tetrad.regression.RegressionResult;

import javax.swing.*;
import java.text.DecimalFormat;

/**
 * This is the JPanel view containing the table with the regression results.
 *
 * @author Adrian Tang
 */
class RegressionChartView extends JPanel {
    private RegressionInfo interactionPlot;
    private RegressionInfo.RegressionTableModel regressionTableModel;

    /**
     * Constructor.
     */
    public RegressionChartView(RegressionInfo interactionPlot) {
        this.interactionPlot = interactionPlot;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        regressionTableModel = interactionPlot.getRegressionTableModel();
        add(createExptInfo());
        add(Box.createVerticalStrut(10));
        add(new JLabel(interactionPlot.getEquation()));
        add(createRegressionTable());
        add(Box.createVerticalStrut(10));
    }

    private JPanel createRegressionTable() {
        JPanel regressionPanel = new JPanel();
        JTable regressionTable = new JTable(regressionTableModel);

        regressionPanel.setLayout(new BoxLayout(regressionPanel, BoxLayout.PAGE_AXIS));
        regressionPanel.add(regressionTable.getTableHeader());
        regressionPanel.add(regressionTable);
        return regressionPanel;
    }

    private JPanel createExptInfo() {
        JPanel exptPanel = new JPanel();
        //g.setFont(g.getFont().deriveFont(11f));

        JLabel exptName = new JLabel(interactionPlot.getExptName());
        JLabel sampleName = new JLabel(interactionPlot.getSampleName() + " (n=" + interactionPlot.getSampleSize() + ")");

        RegressionResult regResult = interactionPlot.getRegressionResult();
        JLabel rSquared = new JLabel("R-Squared: " + new DecimalFormat("0.###").format(regResult.getRSquared()));

        double se[] = regResult.getSe();
        double sse = 0;
        for (int i = 0; i < se.length; i++) {
            sse += se[i];
        }

        JLabel sseLabel = new JLabel("SSE: " + new DecimalFormat("0.###").format(sse));

        exptName.setHorizontalAlignment(JLabel.LEFT);
        sampleName.setHorizontalAlignment(JLabel.LEFT);
        rSquared.setHorizontalAlignment(JLabel.LEFT);
        sseLabel.setHorizontalAlignment(JLabel.LEFT);

        exptPanel.setLayout(new BoxLayout(exptPanel, BoxLayout.PAGE_AXIS));
        exptPanel.add(exptName);
        exptPanel.add(sampleName);
        exptPanel.add(rSquared);
        exptPanel.add(sseLabel);
        return exptPanel;
    }

}
