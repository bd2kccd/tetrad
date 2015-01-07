package edu.cmu.causality.experimentalSetup;

import edu.cmu.causality.experimentalSetup.manipulation.Locked;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * Translates between experimental setup and xml.
 *
 * @author mattheweasterday
 */
public class ExperimentalSetupXml {

    private final static String EXPSETUP = "expSetup";
    private final static String NAME = "name";
    private final static String IGNORED = "ignored";
    private final static String EXPVARIABLE = "expVariable";
    private final static String MANIPULTATION = "manipulation";
    private final static String LOCKEDAT = "lockedAt";
    private final static String MEAN = "mean";
    private final static String SD = "sd";

    /**
     * Convert an experimental setup to xml.
     *
     * @param esvs the experimental setup.
     * @return xml representation of the experimental setup.
     */
    public static Element renderStudiedVariables(ExperimentalSetup esvs) {
        Element esElement = new Element(EXPSETUP);
        esElement.addAttribute(new Attribute(NAME, esvs.getName()));
        Element var;

        String[] names = esvs.getVariableNames();
        for (String name : names) {
            var = new Element(EXPVARIABLE);
            var.addAttribute(new Attribute(NAME, name));
            var.addAttribute(new Attribute(IGNORED, esvs.getVariable(name).isStudied() ? "no" : "yes"));
            var.addAttribute(new Attribute(MANIPULTATION, esvs.getVariable(name).getManipulation().getType().toString()));
            if (esvs.getVariable(name).getManipulation() instanceof Locked) {
                Locked l = (Locked) esvs.getVariable(name).getManipulation();
                var.addAttribute(new Attribute(LOCKEDAT, l.getLockedAtValue()));
            }
            ExperimentalSetupVariable currentVar = esvs.getVariable(name);
            var.addAttribute(new Attribute(MEAN, currentVar.getMean() + ""));
            var.addAttribute(new Attribute(SD, currentVar.getStandardDeviation() + ""));
            esElement.appendChild(var);
        }
        return esElement;
    }

}
