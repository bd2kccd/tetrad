package edu.cmu.causalityApp.exercise;

import nu.xom.Attribute;
import nu.xom.Element;

/**
 * Use this class to take exercises and convert them to xml.  Note that you
 * can only create exercises of the latest version.
 *
 * @author mattheweasterday
 */
public class ExerciseXmlRenderer {

    /**
     * Convert exercise to xml.
     *
     * @param exercise the exercise to convert.
     * @param history  a list of all the actions the user did while working.
     * @return an xml representation of the exercise
     */
    public static Element getElement(Exercise exercise, Element history) {
        Element exerciseElm = getElement(exercise);
        if (history != null) {
            exerciseElm.appendChild(history);
        }      //whats history
        return exerciseElm;
    }


    /**
     * Convert exercise to xml.
     *
     * @param exercise the exercise to convert.
     * @return an xml representation of the exercise
     */
    public static Element getElement(Exercise exercise) {
        Element exerciseElement = new Element(Exercise.EXERCISE);
        exerciseElement.addAttribute(new Attribute(Exercise.VERSION, exercise.getVersion()));
        exerciseElement.addAttribute(new Attribute(Exercise.ID, exercise.getId()));
        exerciseElement.addAttribute(new Attribute(Exercise.TITLE, exercise.getTitle()));
        exerciseElement.addAttribute(new Attribute(Exercise.INSTRUCTIONS, exercise.getPrompt()));
        exerciseElement.addAttribute(new Attribute(Exercise.INCLUDE_STUDENT_GUESS, exercise.getIncludedGuessStr()));
        exerciseElement.addAttribute(new Attribute(Exercise.ISGODMODE, "" + exercise.getIsGodMode()));
        exerciseElement.addAttribute(new Attribute(Exercise.BUILDNOW, "" + exercise.getBuildNow()));

        //todo: to render the exercise nicely. for cases when user decides to not create the first exercise
        if (!exercise.getBuildNow()) {
            return exerciseElement;

        }


        //windows
        Element windowsElement = makeWindowsElement(exercise);
        exerciseElement.appendChild(windowsElement);

        // true graph! can be an array?!?!?!
        Element trueGraphElement = makeTrueGraphElement();
        exerciseElement.appendChild(trueGraphElement);

        //experimental constraints
        Element resourcesElement = makeExperimentalConstraintsElement(exercise);
        trueGraphElement.appendChild(resourcesElement);

        if (exercise.isUsingBayesIm()) {
            //BayesNet
            Element bayesImElement = BayesXmlRenderer.getElement(exercise.getBayesModelIm());
            trueGraphElement.appendChild(bayesImElement);
        } else {
            Element semImElement = SemXmlRenderer.getElement(exercise.getSemModelIm());
            trueGraphElement.appendChild(semImElement);
        }

        exerciseElement.appendChild(getEssayQuestionElement(exercise));
        exerciseElement.appendChild(getEssayAnswerElement(exercise));
        exerciseElement.appendChild(getInstructorFeedbackElement(exercise));
        exerciseElement.appendChild(getGradeScoreElement(exercise));

        return exerciseElement;
    }

    private static Element getEssayQuestionElement(Exercise exercise) {
        Element essayQuestionsElement = new Element(Exercise.ESSAYQUESTION);
        String questionElementString = exercise.getEssayQuestion();
        if (questionElementString != null) {
            Element questionElement = new Element(Exercise.QUESTION);
            questionElement.addAttribute(new Attribute(Exercise.QUESTIONTEXT, questionElementString));
            essayQuestionsElement.appendChild(questionElement);
        }
        return essayQuestionsElement;
    }

    private static Element getEssayAnswerElement(Exercise exercise) {
        Element essayAnswersElement = new Element(Exercise.ESSAYANSWER);
        String answerElementString = exercise.getEssayAnswer();
        if (answerElementString != null) {
            Element answerElement = new Element(Exercise.ANSWER);
            answerElement.addAttribute(new Attribute(Exercise.ANSWERTEXT, answerElementString));
            essayAnswersElement.appendChild(answerElement);
        }
        return essayAnswersElement;
    }

    private static Element getInstructorFeedbackElement(Exercise exercise) {
        Element instructorFeedbackElement = new Element(Exercise.INSTRUCTORFEEDBACK);
        String feedbackElementString = exercise.getInstructorFeedback();
        if (feedbackElementString != null) {
            Element feedbackElement = new Element(Exercise.FEEDBACK);
            feedbackElement.addAttribute(new Attribute(Exercise.FEEDBACKTEXT, feedbackElementString));
            instructorFeedbackElement.appendChild(feedbackElement);
        }
        return instructorFeedbackElement;
    }

    private static Element getGradeScoreElement(Exercise exercise) {
        Element gradeScoreElement = new Element(Exercise.GRADESCORE);
        String gradeString = exercise.getGrade();

        if (gradeString != null) {
            Element gradeElement = new Element(Exercise.GRADE);
            gradeElement.addAttribute(new Attribute(Exercise.GRADETEXT, gradeString));
            gradeScoreElement.appendChild(gradeElement);
        }

        return gradeScoreElement;
    }

    //========================================================================
    //
    //  PRIVATE METHODS    
    //
    //========================================================================

    private static Element makeTrueGraphElement() {
        return new Element(Exercise.TRUEGRAPH);
    }


    private static Element makeWindowsElement(Exercise exercise) {
        Element windowsElement = new Element(Exercise.WINDOWS);
        windowsElement.appendChild(makeHidableWindow(Exercise.CORRECT_GRAPH, exercise));
        windowsElement.appendChild(makeHidableWindow(Exercise.CORRECT_MANIPULATED_GRAPH, exercise));
        windowsElement.appendChild(makeHidableWindow(Exercise.POPULATION, exercise));
        windowsElement.appendChild(makeWindow(Exercise.EXPERIMENTAL_SETUP, exercise));
        windowsElement.appendChild(makeWindow(Exercise.SAMPLE, exercise));
        windowsElement.appendChild(makeWindow(Exercise.INDEPENDENCIES, exercise));
        windowsElement.appendChild(makeWindow(Exercise.HYPOTHETICAL_GRAPH, exercise));
        windowsElement.appendChild(makeWindow(Exercise.HYPOTHETICAL_MANIPULATED_GRAPH, exercise));
        return windowsElement;
    }

    private static Element makeHidableWindow(String windowName, Exercise exercise) {
        Element windowElement = new Element(windowName);
        windowElement.addAttribute(new Attribute(Exercise.INCLUDED, exercise.isWindowInLab(windowName) ? "yes" : "no"));
        windowElement.addAttribute(new Attribute(Exercise.HIDABLE, exercise.isWindowHidable(windowName) ? "yes" : "no"));
        return windowElement;
    }

    private static Element makeWindow(String windowName, Exercise exercise) {
        Element windowElement = new Element(windowName);
        windowElement.addAttribute(new Attribute(Exercise.INCLUDED, exercise.isWindowInLab(windowName) ? "yes" : "no"));
        return windowElement;
    }


    private static Element makeExperimentalConstraintsElement(Exercise exercise) {
        Element expConstElement = new Element(Exercise.EXPERIMENTAL_CONSTRAINTS);

        if (exercise.isLimitResource()) {
            Element resourcesElement = new Element(Exercise.RESOURCES);
            resourcesElement.addAttribute(new Attribute(Exercise.RESOURCE_TOTAL, exercise.getResourceTotal().toString()));
            resourcesElement.addAttribute(new Attribute(Exercise.RESOURCE_PER_OBSERVATION, exercise.getResourcePerObs().toString()));
            resourcesElement.addAttribute(new Attribute(Exercise.RESOURCE_PER_INTERVENTION, exercise.getResourcePerInt().toString()));
            expConstElement.appendChild(resourcesElement);
        }

        String[] varNames = exercise.getMeasuredVariableNames();
        Element intervenableStatusesElement = new Element(Exercise.INTERVENABLE_STATUSES);
        for (String varName : varNames) {
            Element intervenableStatusElement = new Element(Exercise.INTERVENABLE_STATUS);
            intervenableStatusElement.addAttribute(new Attribute(Exercise.VARIABLE, varName));
            boolean b = exercise.isVariableIntervenable(varName);

            intervenableStatusElement.addAttribute(new Attribute(Exercise.INTERVENABLE, b ? "yes" : "no"));
            intervenableStatusesElement.appendChild(intervenableStatusElement);
        }
        expConstElement.appendChild(intervenableStatusesElement);

        return expConstElement;
    }


}
