package edu.cmu.causalityApp.exercise;

import edu.cmu.causality.util.ExampleModels;
import nu.xom.Element;


/**
 * @author mattheweasterday
 */
public class TestExerciseXmlRenderer extends junit.framework.TestCase {

    public TestExerciseXmlRenderer(String name) {
        super(name);
    }

    public void test1() {
        Exercise exercise = new Exercise("these are the instructions",
                ExampleModels.makeIm(),
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                false,
                null);
        exercise.setId("foo");
        exercise.setTitle("foo bar");
        Element element = ExerciseXmlRenderer.getElement(exercise);
        assertNotNull(element);
        assertTrue(element.getAttributeValue(Exercise.INSTRUCTIONS).equals("these are the instructions"));
        Element windows = element.getFirstChildElement(Exercise.WINDOWS);
        Element corrGraph = windows.getFirstChildElement(Exercise.CORRECT_GRAPH);
        assertNotNull(corrGraph);
        assertTrue(corrGraph.getAttributeValue(Exercise.INCLUDED).equals("yes"));
        assertTrue(corrGraph.getAttributeValue(Exercise.HIDABLE).equals("no"));
    }


}
