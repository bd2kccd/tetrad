package edu.cmu.causality.chartBuilder;

import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class contains the information necessary to convert a scatterplot to its
 * xml representation.
 *
 * @author mattheweasterday
 */
public class ScatterPlotXml {

    private static final String SCATTERPLOT = "scatterplot";
    private static final String EXPERIMENTAL_SETUP = "experimentalSetup";
    private static final String SAMPLE = "sample";
    private static final String RESPONSE_VAR = "responseVariable";
    private static final String PREDICTOR_VAR = "predictorVariable";
    private static final String INCLUDE_LINE = "includeRegressionLine";
    private static final String YES = "yes";
    private static final String NO = "no";

    /**
     * @return an xml element representation of the scatterplot given.
     */
    public static Element renderScatterPlot(ScatterPlot scatterplot) {
        Element sElement = new Element(SCATTERPLOT);
        sElement.addAttribute(new Attribute(EXPERIMENTAL_SETUP, scatterplot.getExptName()));
        sElement.addAttribute(new Attribute(SAMPLE, scatterplot.getSampleName()));
        sElement.addAttribute(new Attribute(RESPONSE_VAR, scatterplot.getYvar()));
        sElement.addAttribute(new Attribute(PREDICTOR_VAR, scatterplot.getXvar()));
        String hasLine = scatterplot.getIncludeLine() ? YES : NO;
        sElement.addAttribute(new Attribute(INCLUDE_LINE, hasLine));
        return sElement;
    }


}
