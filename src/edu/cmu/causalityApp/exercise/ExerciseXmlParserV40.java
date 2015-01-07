package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.sem.SemIm;
import nu.xom.Element;

/**
 * Use this class to parse exercises for the 4.0 version of the lab
 *
 * @author adrian tang
 */
public class ExerciseXmlParserV40 {

    /**
     * Specifies that this parser works for the version 4.0 of the lab.
     */
    public static final String VERSION = "4.0";

    /**
     * Takes an xml representation of a 3.2 exercise and creates the exercise.
     *
     * @param exerciseElement the xml
     * @return the exercise
     */
    public static Exercise getExercise(Element exerciseElement) {
        if (!Exercise.EXERCISE.equals(exerciseElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + Exercise.EXERCISE + "' element");
        }

        Element windowsElement = exerciseElement.getFirstChildElement(Exercise.WINDOWS);

        //experimental constraints elements
        Element expConstElement = exerciseElement.getFirstChildElement(Exercise.EXPERIMENTAL_CONSTRAINTS);

        Element resourcesElement = null;
        Element intervenableElement = null;
        try {
            resourcesElement = expConstElement.getFirstChildElement(Exercise.RESOURCES);
            intervenableElement = expConstElement.getFirstChildElement(Exercise.INTERVENABLE_STATUSES);
        } catch (java.lang.NullPointerException npe) {
            //do nothing
        }

        Element bayesImElement = exerciseElement.getFirstChildElement("bayesNet");
        Element semImElement = exerciseElement.getFirstChildElement(SemXmlConstants.SEM);
        Element commandsElement = exerciseElement.getFirstChildElement("commands");

        Element correctGraph = windowsElement.getFirstChildElement(Exercise.CORRECT_GRAPH);
        Element correctManipGraph = windowsElement.getFirstChildElement(Exercise.CORRECT_MANIPULATED_GRAPH);
        Element population = windowsElement.getFirstChildElement(Exercise.POPULATION);
        Element experiment = windowsElement.getFirstChildElement(Exercise.EXPERIMENTAL_SETUP);
        Element sample = windowsElement.getFirstChildElement(Exercise.SAMPLE);
        Element hypGraph = windowsElement.getFirstChildElement(Exercise.HYPOTHETICAL_GRAPH);
        Element hypManipGraph = windowsElement.getFirstChildElement(Exercise.HYPOTHETICAL_MANIPULATED_GRAPH);
        Element indep = windowsElement.getFirstChildElement(Exercise.INDEPENDENCIES);

        Exercise exercise;

        Integer total_resource = null;  // = new Integer(Exercise.DEFAULT_RESOURCE_TOTAL);
        Integer resourceObs = null;     //new Integer(Exercise.DEFAULT_RESOURCE_OBS);
        Integer resourceInt = null;     // new Integer(Exercise.DEFAULT_RESOURCE_TOTAL);
        if (resourcesElement != null) {
            total_resource = new Integer(resourcesElement.getAttributeValue(Exercise.RESOURCE_TOTAL));
            resourceObs = new Integer(resourcesElement.getAttributeValue(Exercise.RESOURCE_PER_OBSERVATION));
            resourceInt = new Integer(resourcesElement.getAttributeValue(Exercise.RESOURCE_PER_INTERVENTION));
        }

        if (bayesImElement != null) {
            BayesXmlParser bayesParser = new BayesXmlParser();
            BayesIm bayesIm = bayesParser.getBayesIm(bayesImElement);

            exercise = new Exercise(exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue(),
//                    exerciseElement.getAttribute(Exercise.GOAL).getValue(),
                    null,
                    null,
                    null,
                    null,
                    bayesIm,
                    total_resource,
                    resourceObs,
                    resourceInt,
                    intervenableElement,
                    getWindowStatus(correctGraph),
                    getWindowStatus(correctManipGraph),
                    getWindowStatus(population),
                    getWindowStatus(experiment),
                    getWindowStatus(sample),
                    getWindowStatus(hypGraph),
                    getWindowStatus(hypManipGraph),
                    getWindowStatus(indep),
                    str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()),
                    commandsElement);
        } else {
            SemIm semIm = SemXmlParser.getSemIm(semImElement);

            exercise = new Exercise(exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue(),
                    null,
                    null,
                    null,
                    null,
                    semIm,
                    total_resource,
                    resourceObs,
                    resourceInt,
                    intervenableElement,
                    getWindowStatus(correctGraph),
                    getWindowStatus(correctManipGraph),
                    getWindowStatus(population),
                    getWindowStatus(experiment),
                    getWindowStatus(sample),
                    getWindowStatus(hypGraph),
                    getWindowStatus(hypManipGraph),
                    getWindowStatus(indep),
                    str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()),
                    commandsElement);
        }

        exercise.setId(exerciseElement.getAttribute(Exercise.ID).getValue());
        //exercise.setTitle(exerciseElement.getAttribute(Exercise.TITLE).getValue());

        //for some reason, some of the exercises don't have title in right place!!!
        try {
            exercise.setTitle(exerciseElement.getAttribute(Exercise.TITLE).getValue());
        } catch (java.lang.NullPointerException npe) {
            System.out.println("Title not in right place!!  using id instead");
            exercise.setTitle(exerciseElement.getAttribute(Exercise.ID).getValue());
        }

        return exercise;
    }


    private static boolean str2Boolean(String str) {
        if (str.equals("true")) return true;
        else if (str.equals("false")) return false;
        else throw new IllegalArgumentException("Error in <Include_Student Guess> attribute");
    }

    private static WindowInclusionStatus getWindowStatus(Element element) {
        boolean isIncluded = element.getAttributeValue(Exercise.INCLUDED).equals("yes");
        String hidable = element.getAttributeValue(Exercise.HIDABLE);
        boolean isHidable = (hidable != null) && (hidable.equals("yes"));

        if (isIncluded) {
            if (isHidable) {
                return WindowInclusionStatus.HIDABLE;
            } else {
                return WindowInclusionStatus.NOT_HIDABLE;
            }
        } else {
            return WindowInclusionStatus.NOT_INCLUDED;
        }
    }
}
