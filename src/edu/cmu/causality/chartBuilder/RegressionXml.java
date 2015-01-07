package edu.cmu.causality.chartBuilder;

import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This class contains the information necessary to convert a regression to its
 * xml representation.
 *
 * @author adrian tang
 */
public class RegressionXml {

    private static final String REGRESSION = "regression";
    private static final String EXPERIMENTAL_SETUP = "experimentalSetup";
    private static final String SAMPLE = "sample";
    private static final String RESPONSE_VAR = "responseVariable";
    private static final String PREDICTOR_VAR = "predictorVariable";
    private static final String NAME = "name";

    /**
     * @return an xml element representation of the regression given.
     */
    public static Element renderRegression(RegressionInfo regression) {
        Element sElement = new Element(REGRESSION);
        sElement.addAttribute(new Attribute(EXPERIMENTAL_SETUP, regression.getExptName()));
        sElement.addAttribute(new Attribute(SAMPLE, regression.getSampleName()));
        sElement.addAttribute(new Attribute(RESPONSE_VAR, regression.getResponseVar()));

        String[] predictorVars = regression.getPredictorVars();
        for (String predictorVar : predictorVars) {
            Element pElt = new Element(PREDICTOR_VAR);
            pElt.addAttribute(new Attribute(NAME, predictorVar));

            sElement.appendChild(pElt);
        }

        return sElement;
    }


}
