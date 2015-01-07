package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.chartBuilder.Histogram;
import edu.cmu.causality.chartBuilder.HistogramXml;
import edu.cmu.causality.sample.BayesSample;
import edu.cmu.command.AbstractCommand;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.editor.EditorWindow;
import edu.cmu.tetradapp.editor.HistogramView;
import edu.cmu.tetradapp.util.DesktopController;
import nu.xom.Attribute;
import nu.xom.Element;

import javax.swing.*;
import java.awt.*;

/**
 * Command for creating histograms from the sample editor
 *
 * @author mattheweasterday
 */
public class CreateHistogramCommand extends AbstractCommand {

    private static final String MY_NAME = "createHistogramCommand";
    private static final String X = "x";
    private static final String Y = "y";

    private final JDesktopPane desktop;
    private JInternalFrame histogramGraph;
    private final Histogram histogram;
    private final int x;
    private final int y;

    /**
     * Constructor.
     *
     * @param pane           Interface element that holds editor
     * @param experimentName name of the experiment sample created from
     * @param sampleName     name of the sample histogram will be made from
     * @param varsToChart    the variables that will be charted
     * @param condVars       the variables that will be conditioned on
     * @param condState      the states of the conditioned variables
     * @param x              the x position of the chart
     * @param y              the y position of the chart
     */
    public CreateHistogramCommand(
            JDesktopPane pane,
            String experimentName,
            String sampleName,
            String[] varsToChart,
            String[] condVars,
            String[] condState,
            int x,
            int y) {

        desktop = pane;

        String truename = "True"; // todo jdramsey
        histogram = new Histogram(experimentName, sampleName, truename, varsToChart, condVars, condState);

        this.x = x;
        this.y = y;
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     */
    public CreateHistogramCommand(int x, int y, JDesktopPane desktop, Histogram histogram) {
        this.x = x;
        this.y = y;
        this.desktop = desktop;
        this.histogram = histogram;
    }

    /**
     * Executes the moves by creating and displaying a histogram.
     */
    public void justDoIt() {
//        histogram.get

        BayesSample sample = (BayesSample) CausalityLabModel.getModel().getSample(histogram.getExperimentName(), histogram.getSampleName());
        DataSet dataSet = sample.getDataSet();
        edu.cmu.tetrad.data.Histogram _histogram = new edu.cmu.tetrad.data.Histogram(dataSet);

        JPanel component = createHistogramPanel(null, _histogram);

//        histogramGraph = new JInternalFrame();
//        histogramGraph.setContentPane(component);
//        histogramGraph.pack();



        this.histogramGraph = new EditorWindow(component, "Histogram", null, /*"Close",*/ false, null);
//        desktop.add(this.histogramGraph);
//        DesktopController.getInstance().addEditorWindow(editorWindow, JLayeredPane.PALETTE_LAYER);
//        setLocation(editorWindoaw, index);
//        editorWindow.setVisible(true);
//        editorWindow.pack();


        /* Create histogram */
//        this.histogramGraph = new HistogramChartFrame(histogram);
        desktop.add(this.histogramGraph);
        this.histogramGraph.setLocation(new Point(x, y));
        this.histogramGraph.setVisible(true);
        this.histogramGraph.pack();
    }

    /**
     * Creates a dialog that is showing the histogram for the given node (if null
     * one is selected for you)
     */
    private JPanel createHistogramPanel(Node selected, edu.cmu.tetrad.data.Histogram histogram) {
        histogram.setTarget(selected == null ? null : selected.getName());
        HistogramView view = new HistogramView(histogram);

        Box box = Box.createHorizontalBox();
        box.add(view);
        box.add(Box.createHorizontalStrut(5));
        box.add(Box.createHorizontalGlue());

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(15));
        vBox.add(box);
        vBox.add(Box.createVerticalStrut(5));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(vBox, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Undoes the moves by deleting the histogram window.
     */
    public void undo() {
        histogramGraph.dispose();
    }

    /**
     * A string representation of the moves used for display in the exercise
     * history.
     *
     * @return "Histogram created"
     */
    public String toString() {
        return "Histogram created";
    }

    /**
     * The name of the moves used in the xml representation.
     *
     * @return "createHistogramCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * @return The attributes of the xml representation of the moves
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[2];
        Point p = histogramGraph.getLocation();

        att[0] = new Attribute(X, Integer.toString(p.x));
        att[1] = new Attribute(Y, Integer.toString(p.y));
        return att;
    }

    /**
     * An xml representation of a child node for the xml for the moves.
     *
     * @return an xml representatino of the histogram used by the moves
     */
    protected Element[] renderChildren() {
        Element[] e = new Element[1];
        e[0] = HistogramXml.renderHistogram(histogram);
        return e;
    }
}
