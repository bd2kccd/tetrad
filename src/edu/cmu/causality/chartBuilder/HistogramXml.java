package edu.cmu.causality.chartBuilder;

import nu.xom.Attribute;
import nu.xom.Element;


/**
 * This class contains the information necessary to convert a histogram to its
 * xml representation.
 *
 * @author mattheweasterday
 */
public class HistogramXml {
    public static final String HISTOGRAM = "histogram";
    private static final String EXPERIMENTAL_SETUP = "experimentalSetup";
    private static final String SAMPLE = "sample";
    private static final String CHARTED_VARS = "chartedVars";
    private static final String CHARTED_VAR = "chartedVar";
    private static final String NAME = "name";
    private static final String CONDITIONED_VARS = "conditionedVars";
    private static final String CONDITIONED_VAR = "conditionedVar";
    private static final String VALUE = "value";
    private static final String TRUE_MODEL_NAME = "truemodelName";

    /**
     * @return an xml element representation of the histogram given.
     */
    public static Element renderHistogram(Histogram hist) {
        Element h = new Element(HISTOGRAM);
        h.addAttribute(new Attribute(TRUE_MODEL_NAME, hist.getTrueName()));
        h.addAttribute(new Attribute(EXPERIMENTAL_SETUP, hist.getExptName()));
        h.addAttribute(new Attribute(SAMPLE, hist.getSampleName()));

        Element cVar;
        String[] chartedVars = hist.getChartedVarNames();
        Element chVars = new Element(CHARTED_VARS);
        for (String chartedVar : chartedVars) {
            cVar = new Element(CHARTED_VAR);
            cVar.addAttribute(new Attribute(NAME, chartedVar));
            chVars.appendChild(cVar);
        }

        String[] conditionedVars = hist.getConditionedVarNames();
        Element coVars = new Element(CONDITIONED_VARS);
        for (String conditionedVar : conditionedVars) {
            cVar = new Element(CONDITIONED_VAR);
            cVar.addAttribute(new Attribute(NAME, conditionedVar));
            cVar.addAttribute(new Attribute(VALUE, hist.getCondVarState(conditionedVar)));
            coVars.appendChild(cVar);
        }
        h.appendChild(chVars);
        h.appendChild(coVars);
        return h;
    }


}
