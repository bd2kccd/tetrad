package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.chartBuilder.RegressionInfo;
import edu.cmu.causality.chartBuilder.RegressionXml;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;
import nu.xom.Element;

import javax.swing.*;
import java.awt.*;

/**
 * Command that creates a regression
 *
 * @author adrian tang
 */
public class CreateRegressionCommand extends AbstractCommand {

    private static final String MY_NAME = "createRegressionCommand";
    private static final String X = "x";
    private static final String Y = "y";
    private final JDesktopPane DESKTOP;
    private final int x;
    private final int y;
    private final RegressionInfo REGRESSION;
    private RegressionChartFrame REGRESSION_FRAME;

    /**
     * Constructor.
     *
     * @param pane           interface element.
     * @param experimentName name of experiment sample created from.
     * @param sampleName     name of sample regression created from.
     * @param responseVar    the y-axix variable.
     * @param predictorVars  the x-axis variable(s).
     * @param x              position of the scatterplot.
     * @param y              position of the scatterplot.
     */
    public CreateRegressionCommand(JDesktopPane pane,
                                   String experimentName,
                                   String sampleName,
                                   String responseVar,
                                   String[] predictorVars,
                                   int x,
                                   int y) {
        DESKTOP = pane;

        REGRESSION = new RegressionInfo(
                experimentName,
                sampleName,
                responseVar,
                predictorVars);
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor for parser.
     */
    public CreateRegressionCommand(JDesktopPane pane,
                                   RegressionInfo regressionInfo,
                                   int x,
                                   int y) {
        DESKTOP = pane;
        REGRESSION = regressionInfo;
        this.x = x;
        this.y = y;
    }


    /**
     * Runs the moves by creating a regression
     */
    public void justDoIt() {
        REGRESSION_FRAME = new RegressionChartFrame(REGRESSION);
        DESKTOP.add(REGRESSION_FRAME);
        REGRESSION_FRAME.setLocation(new Point(x, y));
        REGRESSION_FRAME.setVisible(true);
        REGRESSION_FRAME.pack();
    }


    /**
     * Undoes the moves by deleting the regression
     */
    public void undo() {
        REGRESSION_FRAME.dispose();
    }


    /**
     * String representation of the moves for display in the moves history
     */
    public String toString() {
        return "Regression created";
    }


    /**
     * Name of the moves used in the xml representation
     *
     * @return "createRegressionCommand
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * Attributes used in xml representation of moves
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[2];
        Point p = REGRESSION_FRAME.getLocation();

        att[0] = new Attribute(X, Integer.toString(p.x));
        att[1] = new Attribute(Y, Integer.toString(p.y));
        return att;
    }


    protected Element[] renderChildren() {
        Element[] e = new Element[1];
        e[0] = RegressionXml.renderRegression(REGRESSION);
        return e;
    }
}
