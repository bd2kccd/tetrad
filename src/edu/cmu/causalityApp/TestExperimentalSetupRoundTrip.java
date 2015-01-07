package edu.cmu.causalityApp;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.ExperimentalSetupXml;
import edu.cmu.causality.experimentalSetup.manipulation.Locked;
import edu.cmu.causality.experimentalSetup.manipulation.None;
import edu.cmu.causality.experimentalSetup.manipulation.Randomized;
import edu.cmu.causalityApp.exercise.ExerciseXmlParserV42;
import nu.xom.Element;


/**
 * @author mattheweasterday
 */
public class TestExperimentalSetupRoundTrip extends junit.framework.TestCase {

    public TestExperimentalSetupRoundTrip(String name) {
        super(name);
    }

    public void testRoundTrip() {
        String[] varNames = {"var1", "var2", "var3", "var4"};   //$NON-NLS-3$ //$NON-NLS-4$
        ExperimentalSetup es = new ExperimentalSetup("exp1", varNames);
        es.getVariable("var2").setRandomized();
        es.getVariable("var3").setLocked("2");
        es.getVariable("var4").setStudied(false);

        Element elm = ExperimentalSetupXml.renderStudiedVariables(es);

        System.out.println(elm.toXML());

        ExperimentalSetup es2 = ExerciseXmlParserV42.parseStudiedVariables(elm);

        assertTrue(es2.getName().equals("exp1"));
        assertTrue(es2.getNumVariables() == 4);
        assertTrue(es2.getVariable("var1").getManipulation() instanceof None);
        assertTrue(es2.getVariable("var2").getManipulation() instanceof Randomized);
        assertTrue(es2.getVariable("var3").getManipulation() instanceof Locked);
        assertTrue(((Locked) es2.getVariable("var3").getManipulation()).getLockedAtValue().equals("2"));
        assertTrue(!es2.getVariable("var4").isStudied());
    }
}
