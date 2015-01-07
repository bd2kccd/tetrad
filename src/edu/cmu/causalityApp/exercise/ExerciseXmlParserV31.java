package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.sem.SemIm;
import nu.xom.Element;

/**
 * Use this class to parse exercises for the 3.1 version of the lab
 *
 * @author mattheweasterday
 */
class ExerciseXmlParserV31 {

    /**
     * Specifies that this parser works for the old 3.1 version of the lab.
     */
    public static final String VERSION = "3.1";

    /**
     * Takes an xml representation of a 3.1 exercise and creates the exercise.
     *
     * @param exerciseElement the xml
     * @return the exercise
     */
    public static Exercise getExercise(Element exerciseElement) {
        if (!Exercise.EXERCISE.equals(exerciseElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + Exercise.EXERCISE + "' element");
        }

        Element titleElement = exerciseElement.getFirstChildElement(Exercise.TITLE);
        Element windowsElement = exerciseElement.getFirstChildElement(Exercise.WINDOWS);
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

        exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue();
        exerciseElement.getAttribute(Exercise.GOAL).getValue();

        getWindowStatus(correctGraph);
        getWindowStatus(correctManipGraph);
        getWindowStatus(population);
        getWindowStatus(experiment);
        getWindowStatus(sample);
        getWindowStatus(hypGraph);
        getWindowStatus(hypManipGraph);
        getWindowStatus(indep);
        str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue());

        if (bayesImElement != null) {
            BayesXmlParser bayesParser = new BayesXmlParser();
            BayesIm bayesIm = bayesParser.getBayesIm(bayesImElement);

            exercise = new Exercise(exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue(),
//                    exerciseElement.getAttribute(Exercise.GOAL).getValue(),
                    bayesIm,
                    getWindowStatus(correctGraph),
                    getWindowStatus(correctManipGraph),
                    getWindowStatus(population),
                    getWindowStatus(experiment),
                    getWindowStatus(sample),
                    getWindowStatus(hypGraph),
                    getWindowStatus(hypManipGraph),
                    getWindowStatus(indep),
                    str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()), //boolean isIndependencyCalculatedAutomatically)
                    commandsElement);
        } else {
            SemIm semIm = SemXmlParser.getSemIm(semImElement);

            exercise = new Exercise(exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue(),
//                    exerciseElement.getAttribute(Exercise.GOAL).getValue(),
                    semIm,
                    getWindowStatus(correctGraph),
                    getWindowStatus(correctManipGraph),
                    getWindowStatus(population),
                    getWindowStatus(experiment),
                    getWindowStatus(sample),
                    getWindowStatus(hypGraph),
                    getWindowStatus(hypManipGraph),
                    getWindowStatus(indep),
                    //isHypotheticalManipulatedGraphCalculatedAutomatically,
                    str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()), //boolean isIndependencyCalculatedAutomatically)
                    commandsElement);
        }

        exercise.setId(exerciseElement.getAttribute(Exercise.ID).getValue());
        exercise.setTitle(titleElement.getValue());

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

