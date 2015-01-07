package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.ScatterPlot;
import edu.cmu.causality.chartBuilder.ScatterPlotXml;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;
import nu.xom.Element;

import javax.swing.*;
import java.awt.*;

/**
 * Command that creates a scatterplot
 *
 * @author mattheweasterday
 */
public class CreateScatterplotCommand extends AbstractCommand {

    private static final String MY_NAME = "createScatterPlotCommand";
    private static final String X = "x";
    private static final String Y = "y";
    private final JDesktopPane DESKTOP;
    private final int x;
    private final int y;
    private final ScatterPlot SCATTERPLOT;
    private ScatterPlotChartFrame SCATTERPLOT_FRAME;

    /**
     * Constructor
     *
     * @param pane                  interface element
     * @param experimentName        name of experiment sample created from
     * @param sampleName            name of sample scatterplot created from
     * @param includeRegressionLine if yes, include regression on plot
     * @param responseVar           the y-axix variable
     * @param predictorVar          the x-axis variable
     * @param x                     position of the scatterplot
     * @param y                     position of the scatterplot
     */
    public CreateScatterplotCommand(JDesktopPane pane,
                                    String experimentName,
                                    String sampleName,
                                    String truename,
                                    boolean includeRegressionLine,
                                    String responseVar,
                                    String predictorVar,
                                    int x,
                                    int y) {
        DESKTOP = pane;
        SCATTERPLOT = new ScatterPlot(
                //(SemSample) CausalityLabModel.getModel().getSample(experimentName, sampleName),
                experimentName,
                sampleName,
                truename,
                includeRegressionLine,
                responseVar,
                predictorVar);
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor for parser.
     *
     * @param pane        interface element
     * @param scatterPlot the created scatterplot
     * @param x           x position of scatterplot
     * @param y           y position of scatterplot
     */
    public CreateScatterplotCommand(JDesktopPane pane,
                                    ScatterPlot scatterPlot,
                                    int x,
                                    int y) {
        DESKTOP = pane;
        SCATTERPLOT = scatterPlot;
        this.x = x;
        this.y = y;
    }

    /**
     * Runs the moves by creating a scatterplot
     */
    public void justDoIt() {
        SCATTERPLOT_FRAME = new ScatterPlotChartFrame(SCATTERPLOT);
        DESKTOP.add(SCATTERPLOT_FRAME);
        SCATTERPLOT_FRAME.setLocation(new Point(x, y));
        SCATTERPLOT_FRAME.setVisible(true);
        SCATTERPLOT_FRAME.pack();
    }


    /**
     * Undoes the moves by deleting the scatterplot
     */
    public void undo() {
        SCATTERPLOT_FRAME.dispose();
    }


    /**
     * String representation of the moves for display in the moves history
     *
     * @return string
     */
    public String toString() {
        return "Scatterplot created";
    }


    /**
     * Name of the moves used in the xml representation
     *
     * @return "createScatterplotCommand
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * Attributes used in xml representation of moves
     *
     * @return attribute array used in xml representation of moves
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[2];
        Point p = SCATTERPLOT_FRAME.getLocation();

        att[0] = new Attribute(X, Integer.toString(p.x));
        att[1] = new Attribute(Y, Integer.toString(p.y));
        return att;
    }


    /**
     * @return scatterplot element
     */
    protected Element[] renderChildren() {
        Element[] e = new Element[1];
        e[0] = ScatterPlotXml.renderScatterPlot(SCATTERPLOT);
        return e;
    }
}
