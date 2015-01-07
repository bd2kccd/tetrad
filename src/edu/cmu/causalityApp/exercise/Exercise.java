package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.sem.SemIm;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.*;

import static java.lang.Boolean.valueOf;

/**
 * Use this class to read in an exercise from a file and configure the
 * causality lab.  Also use this class for creating exercises in the exercise
 * builder.
 *
 * @author mattheweasterday
 */

public class Exercise {

    //=======================================================================
    //
    // XML Constants.  These strings represent all the element and attribute
    // names in the xml representation of an exercise
    //
    //=======================================================================

    public static final String CORRECT_GRAPH = "correctGraph";
    public static final String CORRECT_MANIPULATED_GRAPH = "correctManipulatedGraph";
    public static final String POPULATION = "population";
    public static final String EXPERIMENTAL_SETUP = "experimentalSetup";
    public static final String SAMPLE = "sample";
    public static final String HYPOTHETICAL_GRAPH = "hypotheticalGraph";
    public static final String HYPOTHETICAL_MANIPULATED_GRAPH = "hypotheticalManipulatedGraph";
    public static final String INDEPENDENCIES = "independencies";

    public static final String EXERCISE = "exercise";
    public static final String TRUEGRAPH = "trueGraph";

    public static final String VERSION = "version";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String INSTRUCTIONS = "instructions";

    // new additions - for god mode
    public static final String ISGODMODE = "isGodMode";
    public static final String BUILDNOW = "buildNow";


    public static final String ESSAYQUESTION = "essayQuestions";
    public static final String QUESTION = "question";
    public static final String QUESTIONTEXT = "questionText";

    public static final String ESSAYANSWER = "essayAnswers";
    public static final String ANSWER = "answer";
    public static final String ANSWERTEXT = "answerText";

    public static final String INSTRUCTORFEEDBACK = "instructorFeedback";
    public static final String FEEDBACK = "feedback";
    public static final String FEEDBACKTEXT = "feedbackText";

    public static final String GRADESCORE = "gradeScore";
    public static final String GRADE = "grade";
    public static final String GRADETEXT = "gradeText";

    public static final String GOAL = "goal";
    public static final String WINDOWS = "windows";
    public static final String INCLUDED = "included";
    public static final String HIDABLE = "hidable";
    public static final String INCLUDE_STUDENT_GUESS = "include_student_guess";

    public static final String VARIABLE = "variable";

    public static final String GOAL_FIND_GRAPH = "find-correct-graph";
    public static final String GOAL_FIND_MANIPULATED_GRAPH = "find-correct-manipulated-graph";
    public static final String GOAL_FIND_INDEPENDENCIES = "find-independencies";
    public static final String GOAL_OTHERS = "other-goals";

    //strings for experimental constraints
    public static final String EXPERIMENTAL_CONSTRAINTS = "experimentalConstraints";
    public static final String RESOURCES = "resources";

    public static final String RESOURCE_TOTAL = "total";
    public static final String RESOURCE_PER_OBSERVATION = "cost-per-observation";
    public static final String RESOURCE_PER_INTERVENTION = "cost-per-intervention";

    public static final String INTERVENABLE_STATUSES = "intervenable-statuses";
    public static final String INTERVENABLE_STATUS = "intervenable-status";
    public static final String INTERVENABLE = "intervenable";


    public static final int DEFAULT_RESOURCE_TOTAL = 50000;
    public static final int DEFAULT_RESOURCE_OBS = 10;
    public static final int DEFAULT_RESOURCE_INT = 100;

    private static final String DEFAULT_VERSION = "4.3";
    private String version;
    private String id;
    private String title;
    private String goal;
    private String prompt;

    private String essayQuestion;
    private String essayAnswer;
    private String instructorFeedback;
    private String theScore;

    // for god mode:
    private boolean buildNow = true;
    private int gmode;
    private int currentTrueModelIndex = 0;

    public static final int NOT_GM = 0;


    // all these are changed to arrays
    private final List<BayesIm> bayesModelIm = new ArrayList<BayesIm>();
    private final List<SemIm> semModelIm = new ArrayList<SemIm>();

    private BayesIm currentBayesModelIm;
    private SemIm currentSemModelIm;
    private boolean currentIsUsingBayesIm;
    private boolean currentIsIncludeGuess;
    private String currentExplanation;
    private Element currentInitialCommands;
    private final Map<String, Boolean> currentVariablesIntervenable = new HashMap<String, Boolean>();

    private WindowInclusionStatus correctGraphInclusion;
    private WindowInclusionStatus correctManipulatedGraphInclusion;
    private WindowInclusionStatus populationInclusion;
    private WindowInclusionStatus experimentalSetupInclusion;
    private WindowInclusionStatus sampleInclusion;
    private WindowInclusionStatus hypotheticalGraphInclusion;
    private WindowInclusionStatus hypotheticalManipulatedGraphInclusion;
    private WindowInclusionStatus independenciesInclusion;

    private Integer resource_total;
    private Integer resource_per_obs;
    private Integer resource_per_int;

    /**
     * Constructor.  Makes an empty exercise.
     */
    public Exercise() {
        version = DEFAULT_VERSION;

        correctGraphInclusion = WindowInclusionStatus.NOT_HIDABLE;
        correctManipulatedGraphInclusion = WindowInclusionStatus.NOT_HIDABLE;
        populationInclusion = WindowInclusionStatus.NOT_HIDABLE;
        experimentalSetupInclusion = WindowInclusionStatus.NOT_HIDABLE;
        sampleInclusion = WindowInclusionStatus.NOT_HIDABLE;
        hypotheticalGraphInclusion = WindowInclusionStatus.NOT_HIDABLE;
        hypotheticalManipulatedGraphInclusion = WindowInclusionStatus.NOT_HIDABLE;
        independenciesInclusion = WindowInclusionStatus.NOT_HIDABLE;

        resource_total = null;
        resource_per_obs = null;
        resource_per_int = null;
        this.gmode = 0;
    }

    /**
     * creates an empty exercise - for the instance of God mode but no exercise being created yet.
     */
    public Exercise(String version,
                    String prompt,
                    int mode,
                    boolean buildNow) {

        this.setVersion(version);
        this.setPrompt(prompt);

        this.setIsGodMode(mode);
        this.setBuildNow(valueOf(buildNow));
        this.gmode = mode;

    }


    /**
     * Create an exercise with a BayesIm.
     *
     * @param prompt                     the instructions to the student.
     *                                   //     * @param goal one of the static Goal constants defined by this class.
     * @param modelIm                    the Bayes IM sepecifying the truth.
     * @param resource_total             specifies the total amount of money a student has available
     * @param resource_per_obs           cost of collecting an observational sample
     * @param resource_per_int           cost of collecting a sample with intervention
     * @param correctGraphInclusion      specifies whether window is included and visible
     * @param correctManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param populationInclusion        specifies whether window is included and visible
     * @param experimentalSetupInclusion specifies whether window is included and visible
     * @param sampleInclusion            specifies whether window is included and visible
     * @param hypotheticalGraphInclusion specifies whether window is included and visible
     * @param hypotheticalManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param independenciesInclusion    specifies whether window is included and visible
     */
    public Exercise(String prompt,
                    int mode,
                    boolean buildNow,
                    String essayQuestion,
                    String essayAnswer,
                    String instructorFeedback,
                    String grade,
                    BayesIm modelIm,
                    Integer resource_total,
                    Integer resource_per_obs,
                    Integer resource_per_int,
                    Element intervenableStatuses,
                    WindowInclusionStatus correctGraphInclusion,
                    WindowInclusionStatus correctManipulatedGraphInclusion,
                    WindowInclusionStatus populationInclusion,
                    WindowInclusionStatus experimentalSetupInclusion,
                    WindowInclusionStatus sampleInclusion,
                    WindowInclusionStatus hypotheticalGraphInclusion,
                    WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                    WindowInclusionStatus independenciesInclusion,
                    boolean isIncludeGuess,
                    Element cmds) {
        this(DEFAULT_VERSION,
                prompt,
                mode,
                buildNow,
                essayQuestion,
                essayAnswer,
                instructorFeedback,
                grade,
                resource_total,
                resource_per_obs,
                resource_per_int,
                correctGraphInclusion,
                correctManipulatedGraphInclusion,
                populationInclusion,
                experimentalSetupInclusion,
                sampleInclusion, hypotheticalGraphInclusion,
                hypotheticalManipulatedGraphInclusion,
                independenciesInclusion,
                isIncludeGuess,
                cmds);

        this.setBayesModelIm(modelIm);
        this.parseIntervenableElements(intervenableStatuses);
    }

    /**
     * Create an exercise with a SemIm
     *
     * @param prompt                     the instructions to the student.
     *                                   //     * @param goal one of the static Goal constants defined by this class.
     * @param modelIm                    the Sem IM sepecifying the truth.
     * @param resource_total             specifies the total amount of money a student has available or null.
     * @param resource_per_obs           cost of collecting an observational sample or null.
     * @param resource_per_int           cost of collecting a sample with intervention or null.
     * @param correctGraphInclusion      specifies whether window is included and visible
     * @param correctManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param populationInclusion        specifies whether window is included and visible
     * @param experimentalSetupInclusion specifies whether window is included and visible
     * @param sampleInclusion            specifies whether window is included and visible
     * @param hypotheticalGraphInclusion specifies whether window is included and visible
     * @param hypotheticalManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param independenciesInclusion    specifies whether window is included and visible
     */
    public Exercise(String prompt,
                    int isGodMode,
                    boolean buildNow,
                    String essayQuestion,
                    String essayAnswer,
                    String instructorFeedback,
                    String grade,
                    SemIm modelIm,
                    Integer resource_total,
                    Integer resource_per_obs,
                    Integer resource_per_int,
                    Element intervenableStatuses,
                    WindowInclusionStatus correctGraphInclusion,
                    WindowInclusionStatus correctManipulatedGraphInclusion,
                    WindowInclusionStatus populationInclusion,
                    WindowInclusionStatus experimentalSetupInclusion,
                    WindowInclusionStatus sampleInclusion,
                    WindowInclusionStatus hypotheticalGraphInclusion,
                    WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                    WindowInclusionStatus independenciesInclusion,
                    boolean isIncludeGuess,
                    Element cmds) {
        this(DEFAULT_VERSION,
                prompt,
                isGodMode,
                buildNow,
                essayQuestion,
                essayAnswer,
                instructorFeedback,
                grade,
                resource_total,
                resource_per_obs,
                resource_per_int,
                correctGraphInclusion,
                correctManipulatedGraphInclusion,
                populationInclusion,
                experimentalSetupInclusion,
                sampleInclusion, hypotheticalGraphInclusion,
                hypotheticalManipulatedGraphInclusion,
                independenciesInclusion,
                isIncludeGuess,
                cmds);

        this.setSemModelIm(modelIm);
        this.parseIntervenableElements(intervenableStatuses);
    }

    /**
     * FOR PRE-GOD MODE EXERCISES. IE VERSION 4.3 AND BELOW:
     * Create an exercise with a BayesIm.
     *
     * @param prompt                     the instructions to the student.
     * @param modelIm                    the Bayes IM sepecifying the truth.
     * @param resource_total             specifies the total amount of money a student has available
     * @param resource_per_obs           cost of collecting an observational sample
     * @param resource_per_int           cost of collecting a sample with intervention
     * @param correctGraphInclusion      specifies whether window is included and visible
     * @param correctManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param populationInclusion        specifies whether window is included and visible
     * @param experimentalSetupInclusion specifies whether window is included and visible
     * @param sampleInclusion            specifies whether window is included and visible
     * @param hypotheticalGraphInclusion specifies whether window is included and visible
     * @param hypotheticalManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param independenciesInclusion    specifies whether window is included and visible
     */
    public Exercise(String prompt,
                    String essayQuestion,
                    String essayAnswer,
                    String instructorFeedback,
                    String grade,
                    BayesIm modelIm,
                    Integer resource_total,
                    Integer resource_per_obs,
                    Integer resource_per_int,
                    Element intervenableStatuses,
                    WindowInclusionStatus correctGraphInclusion,
                    WindowInclusionStatus correctManipulatedGraphInclusion,
                    WindowInclusionStatus populationInclusion,
                    WindowInclusionStatus experimentalSetupInclusion,
                    WindowInclusionStatus sampleInclusion,
                    WindowInclusionStatus hypotheticalGraphInclusion,
                    WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                    WindowInclusionStatus independenciesInclusion,
                    boolean isIncludeGuess,
                    Element cmds) {
        this(DEFAULT_VERSION,
                prompt,
                NOT_GM,
                true,
                essayQuestion,
                essayAnswer,
                instructorFeedback,
                grade,
                resource_total,
                resource_per_obs,
                resource_per_int,
                correctGraphInclusion,
                correctManipulatedGraphInclusion,
                populationInclusion,
                experimentalSetupInclusion,
                sampleInclusion, hypotheticalGraphInclusion,
                hypotheticalManipulatedGraphInclusion,
                independenciesInclusion,
                isIncludeGuess,
                cmds);

        this.setBayesModelIm(modelIm);
        this.parseIntervenableElements(intervenableStatuses);
    }

    /**
     * FOR PRE-GOD MODE EXERCISES. IE VERSION 4.3 AND BELOW:
     * Create an exercise with a SemIm
     *
     * @param prompt                     the instructions to the student.
     * @param modelIm                    the Sem IM sepecifying the truth.
     * @param resource_total             specifies the total amount of money a student has available or null.
     * @param resource_per_obs           cost of collecting an observational sample or null.
     * @param resource_per_int           cost of collecting a sample with intervention or null.
     * @param correctGraphInclusion      specifies whether window is included and visible
     * @param correctManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param populationInclusion        specifies whether window is included and visible
     * @param experimentalSetupInclusion specifies whether window is included and visible
     * @param sampleInclusion            specifies whether window is included and visible
     * @param hypotheticalGraphInclusion specifies whether window is included and visible
     * @param hypotheticalManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param independenciesInclusion    specifies whether window is included and visible
     */
    public Exercise(String prompt,
                    String essayQuestion,
                    String essayAnswer,
                    String instructorFeedback,
                    String grade,
                    SemIm modelIm,
                    Integer resource_total,
                    Integer resource_per_obs,
                    Integer resource_per_int,
                    Element intervenableStatuses,
                    WindowInclusionStatus correctGraphInclusion,
                    WindowInclusionStatus correctManipulatedGraphInclusion,
                    WindowInclusionStatus populationInclusion,
                    WindowInclusionStatus experimentalSetupInclusion,
                    WindowInclusionStatus sampleInclusion,
                    WindowInclusionStatus hypotheticalGraphInclusion,
                    WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                    WindowInclusionStatus independenciesInclusion,
                    boolean isIncludeGuess,
                    Element cmds) {
        this(DEFAULT_VERSION,
                prompt,
                NOT_GM,
                true,
                essayQuestion,
                essayAnswer,
                instructorFeedback,
                grade,
                resource_total,
                resource_per_obs,
                resource_per_int,
                correctGraphInclusion,
                correctManipulatedGraphInclusion,
                populationInclusion,
                experimentalSetupInclusion,
                sampleInclusion, hypotheticalGraphInclusion,
                hypotheticalManipulatedGraphInclusion,
                independenciesInclusion,
                isIncludeGuess,
                cmds);

        this.setSemModelIm(modelIm);
        this.parseIntervenableElements(intervenableStatuses);
    }

    /**
     * For the older parsers, v3.3 and below
     * Create an exercise with a BayesIm.
     *
     * @param prompt                     the instructions to the student.
     *                                   //     * @param goal one of the static Goal constants defined by this class.
     * @param modelIm                    the Bayes IM sepecifying the truth.
     * @param correctGraphInclusion      specifies whether window is included and visible
     * @param correctManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param populationInclusion        specifies whether window is included and visible
     * @param experimentalSetupInclusion specifies whether window is included and visible
     * @param sampleInclusion            specifies whether window is included and visible
     * @param hypotheticalGraphInclusion specifies whether window is included and visible
     * @param hypotheticalManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param independenciesInclusion    specifies whether window is included and visible
     */
    public Exercise(String prompt,
                    BayesIm modelIm,
                    WindowInclusionStatus correctGraphInclusion,
                    WindowInclusionStatus correctManipulatedGraphInclusion,
                    WindowInclusionStatus populationInclusion,
                    WindowInclusionStatus experimentalSetupInclusion,
                    WindowInclusionStatus sampleInclusion,
                    WindowInclusionStatus hypotheticalGraphInclusion,
                    WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                    WindowInclusionStatus independenciesInclusion,
                    boolean isIncludeGuess,
                    Element cmds) {
        this(DEFAULT_VERSION,
                prompt,
                NOT_GM,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                correctGraphInclusion,
                correctManipulatedGraphInclusion,
                populationInclusion,
                experimentalSetupInclusion,
                sampleInclusion, hypotheticalGraphInclusion,
                hypotheticalManipulatedGraphInclusion,
                independenciesInclusion,
                isIncludeGuess,
                cmds);

        this.setBayesModelIm(modelIm);
    }

    /**
     * For the older parsers, v3.3 and below
     * Create an exercise with a SemIm
     *
     * @param prompt                     the instructions to the student.
     *                                   //     * @param goal one of the static Goal constants defined by this class.
     * @param modelIm                    the Sem IM sepecifying the truth.
     * @param correctGraphInclusion      specifies whether window is included and visible
     * @param correctManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param populationInclusion        specifies whether window is included and visible
     * @param experimentalSetupInclusion specifies whether window is included and visible
     * @param sampleInclusion            specifies whether window is included and visible
     * @param hypotheticalGraphInclusion specifies whether window is included and visible
     * @param hypotheticalManipulatedGraphInclusion
     *                                   specifies whether window is included and visible
     * @param independenciesInclusion    specifies whether window is included and visible
     */
    public Exercise(String prompt,
//                    String goal,
                    SemIm modelIm,
                    WindowInclusionStatus correctGraphInclusion,
                    WindowInclusionStatus correctManipulatedGraphInclusion,
                    WindowInclusionStatus populationInclusion,
                    WindowInclusionStatus experimentalSetupInclusion,
                    WindowInclusionStatus sampleInclusion,
                    WindowInclusionStatus hypotheticalGraphInclusion,
                    WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                    WindowInclusionStatus independenciesInclusion,
                    boolean isIncludeGuess,
                    Element cmds) {
        this(DEFAULT_VERSION,
                prompt,
                NOT_GM,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                correctGraphInclusion,
                correctManipulatedGraphInclusion,
                populationInclusion,
                experimentalSetupInclusion,
                sampleInclusion, hypotheticalGraphInclusion,
                hypotheticalManipulatedGraphInclusion,
                independenciesInclusion,
                isIncludeGuess,
                cmds);

        this.setSemModelIm(modelIm);
    }



    //=======================================================================
    //
    // GETTERS and SETTERS
    //
    //=======================================================================

    /**
     * Set the exercise id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the exercise id.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the exercise title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the exercise essay question.
     */
    public void setQuestion(String essayQuestion) {
        this.essayQuestion = essayQuestion;
    }

    /**
     * Set the exercise essay answer.
     */
    public void setAnswer(String essayAnswer) {
        this.essayAnswer = essayAnswer;
    }

    /**
     * Set if this exercise is god mode.
     */
    public void setIsGodMode(int isGodMode) {
        System.out.println("set isgodmode here:" + isGodMode);
        this.gmode = isGodMode;
    }

    /**
     * Set if this exercise is god mode.
     */
    public void setBuildNow(boolean buildNow) {
        System.out.println("set build now:" + buildNow);
        this.buildNow = buildNow;
    }

    /**
     * Set the instructor feedback.
     */
    public void setInstructorFeedback(String instructorFeedback) {
        this.instructorFeedback = instructorFeedback;
    }

    /**
     * Set the score given by the grader.
     */
    public void setScore(String theScore) {
//        System.out.println("FROM SETSCORE FN IN EXERCISE.JAVA:" + theScore);
        this.theScore = theScore;
    }


    /**
     * @return the exercise title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the exercise goal.
     */
    public String getGoal() {
        return goal;
    }

    /**
     * @return the exercise prompt.
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * @return the exercise instructor score.
     */
    public String getGrade() {
        return theScore;
    }

    /**
     * @return the exercise Bayes IM model.
     */
    public BayesIm getBayesModelIm() {
        return currentBayesModelIm;
    }

    /**
     * @return the exercise SEM IM model.
     */
    public SemIm getSemModelIm() {
        return currentSemModelIm;
    }

    /**
     * @return a string representing whether or not students' guesses are enabled.
     */
    public String getIncludedGuessStr() {
        return currentIsIncludeGuess ? "true" : "false";
    }

    /**
     * @return whether or not students' guesses are enabled.
     */
    public boolean getIncludedGuess() {
        return currentIsIncludeGuess;
    }

    /**
     * @return if a window is included in the lab.
     */
    public boolean isWindowInLab(String windowName) {
        WindowInclusionStatus window = getWindowStatus(windowName);
        return ((window == WindowInclusionStatus.HIDABLE) || (window == WindowInclusionStatus.NOT_HIDABLE));
    }

    /**
     * @return if a window in the lab is hidable.
     */
    public boolean isWindowHidable(String windowName) {
        WindowInclusionStatus window = getWindowStatus(windowName);
        return (window == WindowInclusionStatus.HIDABLE);
    }

    /**
     * @return if the window inclusion status. It can be NOT_INCLUDED, HIDABLE
     *         or NOT_HIDABLE.
     */
    public WindowInclusionStatus getWindowStatus(String windowName) {
        if (windowName.equals(CORRECT_GRAPH)) {
            return getCorrectGraphInclusion();
        }
        if (windowName.equals(CORRECT_MANIPULATED_GRAPH)) {
            return getCorrectManipulatedGraphInclusion();
        }
        if (windowName.equals(POPULATION)) {
            return getPopulationInclusion();
        }
        if (windowName.equals(EXPERIMENTAL_SETUP)) {
            return getExperimentalSetupInclusion();
        }
        if (windowName.equals(SAMPLE)) {
            return getSampleInclusion();
        }
        if (windowName.equals(HYPOTHETICAL_GRAPH)) {
            return getHypotheticalGraphInclusion();
        }
        if (windowName.equals(HYPOTHETICAL_MANIPULATED_GRAPH)) {
            return getHypotheticalManipulatedGraphInclusion();
        }
        if (windowName.equals(INDEPENDENCIES)) {
            return getIndependenciesInclusion();
        }
        return null;
    }

    /**
     * Set the exercise prompt.
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * Set the exercise to use the Bayes model.
     */
    public void setUsingBayesIm(boolean isUsingBayes) {
        this.currentIsUsingBayesIm = isUsingBayes;
    }

    /**
     * Set the exercise Bayes IM model.
     */
    public void setBayesModelIm(BayesIm modelIm) {
        if (currentVariablesIntervenable.keySet().size() != modelIm.getMeasuredNodes().size()) {
            currentVariablesIntervenable.clear();
        }

        this.currentIsUsingBayesIm = true;
        this.currentBayesModelIm = modelIm;
        bayesModelIm.add(currentTrueModelIndex, modelIm);
    }

    /**
     * Set the exercise SEM IM model.
     */
    public void setSemModelIm(SemIm modelIm) {
        if (currentVariablesIntervenable.keySet().size() != modelIm.getMeasuredNodes().size()) {
            currentVariablesIntervenable.clear();
        }

        this.currentIsUsingBayesIm = false;
        this.currentSemModelIm = modelIm;
        semModelIm.add(currentTrueModelIndex, modelIm);
    }

    /**
     * @return if the exercise is using a Bayes model.
     */
    public boolean isUsingBayesIm() {
        return currentIsUsingBayesIm;
    }

    /**
     * @return the correct graph navigator button inclusion status.
     */
    public WindowInclusionStatus getCorrectGraphInclusion() {
        return correctGraphInclusion;
    }

    /**
     * Set the correct graph navigator button inclusion status.
     */
    public void setCorrectGraphInclusion(WindowInclusionStatus correctGraphInclusion) {
        this.correctGraphInclusion = correctGraphInclusion;
    }

    /**
     * @return the correct manipulated graph navigator button inclusion status.
     */
    public WindowInclusionStatus getCorrectManipulatedGraphInclusion() {
        return correctManipulatedGraphInclusion;
    }

    /**
     * Set the correct manipulated graph navigator button inclusion status.
     */
    public void setCorrectManipulatedGraphInclusion(WindowInclusionStatus correctManipulatedGraphInclusion) {
        this.correctManipulatedGraphInclusion = correctManipulatedGraphInclusion;
    }

    /**
     * @return the population navigator button inclusion status.
     */
    public WindowInclusionStatus getPopulationInclusion() {
        return populationInclusion;
    }

    /**
     * Set the population navigator button inclusion status.
     */
    public void setPopulationInclusion(WindowInclusionStatus populationInclusion) {
        this.populationInclusion = populationInclusion;
    }

    /**
     * @return the experimental setup navigator button inclusion status.
     */
    public WindowInclusionStatus getExperimentalSetupInclusion() {
        return experimentalSetupInclusion;
    }

    /**
     * Set the experimental setup navigator button inclusion status.
     */
    public void setExperimentalSetupInclusion(WindowInclusionStatus experimentalSetupInclusion) {
        this.experimentalSetupInclusion = experimentalSetupInclusion;
    }

    /**
     * @return the sample navigator button inclusion status.
     */
    public WindowInclusionStatus getSampleInclusion() {
        return sampleInclusion;
    }

    /**
     * Set the sample navigator button inclusion status.
     */
    public void setSampleInclusion(WindowInclusionStatus sampleInclusion) {
        this.sampleInclusion = sampleInclusion;
    }

    /**
     * @return the hypothetical graph navigator button inclusion status.
     */
    public WindowInclusionStatus getHypotheticalGraphInclusion() {
        return hypotheticalGraphInclusion;
    }

    /**
     * Set the hypothetical graph navigator button inclusion status.
     */
    public void setHypotheticalGraphInclusion(WindowInclusionStatus hypotheticalGraphInclusion) {
        this.hypotheticalGraphInclusion = hypotheticalGraphInclusion;
    }

    /**
     * @return the hypothetical manipulated graph navigator button inclusion status.
     */
    public WindowInclusionStatus getHypotheticalManipulatedGraphInclusion() {
        return hypotheticalManipulatedGraphInclusion;
    }

    /**
     * Set the hypothetical manipulated graph navigator button inclusion status.
     */
    public void setHypotheticalManipulatedGraphInclusion(WindowInclusionStatus hypotheticalManipulatedGraphInclusion) {
        this.hypotheticalManipulatedGraphInclusion = hypotheticalManipulatedGraphInclusion;
    }

    /**
     * @return the independencies navigator button inclusion status.
     */
    public WindowInclusionStatus getIndependenciesInclusion() {
        return independenciesInclusion;
    }

    /**
     * Set the independencies navigator button inclusion status.
     */
    public void setIndependenciesInclusion(WindowInclusionStatus independenciesInclusion) {
        this.independenciesInclusion = independenciesInclusion;
    }

    /**
     * Set whether student guess is enabled or not.
     */
    public void setStudentGuessInclusion(boolean isIncludeGuess) {
        this.currentIsIncludeGuess = isIncludeGuess;
    }

    /**
     * @return all the initial commands in the exercise history.
     */
    public Element getInitialCommands() {
        return this.currentInitialCommands;
    }

    /**
     * @return the exercise version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the exercise version.
     */
    void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return if the exercise restricts resources.
     */
    public boolean isLimitResource() {
        return resource_total != null &&
                resource_per_obs != null &&
                resource_per_int != null;
    }

    /**
     * @return the total amount of money available to the user in this exercise.
     */
    public Integer getResourceTotal() {
        return resource_total;
    }

    /**
     * Set the total amount of money the user has for the exercise.
     */
    public void setResourceTotal(Integer resource_total) {
        this.resource_total = resource_total;
    }

    /**
     * @return the cost per sample unit in an observational experiment.
     */
    public Integer getResourcePerObs() {
        return resource_per_obs;
    }

    /**
     * Set the cost per sample unit in an observational experiment.
     */
    public void setResourcePerObs(Integer resource_per_obs) {
        if (resource_per_obs == null) {
            return;
        }
        this.resource_per_obs = resource_per_obs;
    }

    /**
     * @return the cost per sample unit in an intervened experiment.
     */
    public Integer getResourcePerInt() {
        return resource_per_int;
    }

    /**
     * Set the cost per sample unit in an intervened experiment.
     */
    public void setResourcePerInt(Integer resource_per_int) {
        this.resource_per_int = resource_per_int;
    }

    /**
     * Set if a particular variable can be intervened at all.
     */
    public void setVariableIntervenable(String varName, boolean isIntervenable) {
        currentVariablesIntervenable.put(varName, isIntervenable);
    }

    /**
     * @return if a variable is intervenable.
     */
    public boolean isVariableIntervenable(String varName) {
        if (currentVariablesIntervenable == null || varName == null) {
            throw new NullPointerException();
        }

        Boolean intervenable = currentVariablesIntervenable.get(varName);
        return intervenable == null ? true : intervenable;
    }

    /**
     * @return a string array of the names of non-latent variables that are in
     *         the exercise.
     */
    public String[] getMeasuredVariableNames() {
        String[] varNames;

        if (isUsingBayesIm()) {
            Vector<String> varNamesVector = new Vector<String>();

            for (int i = 0; i < currentBayesModelIm.getVariableNames().size(); i++) {
                String name = currentBayesModelIm.getVariableNames().get(i);
                if (currentBayesModelIm.getNode(name).getNodeType() != NodeType.LATENT) {
                    varNamesVector.add(name);
                }
            }

            varNames = new String[varNamesVector.size()];
            int i = 0;
            for (Iterator<String> varNameIter = varNamesVector.iterator(); varNameIter.hasNext(); i++) {
                varNames[i] = varNameIter.next();
            }

        } else {
            varNames = new String[currentSemModelIm.getSemPm().getMeasuredNodes().size()];
            int i = 0;
            for (Iterator<Node> varNameIter = currentSemModelIm.getSemPm().getMeasuredNodes().iterator(); varNameIter.hasNext(); i++) {
                Node var = varNameIter.next();
                varNames[i] = var.getName();
            }
        }

        return varNames;
    }

    /**
     * @return a Boolean array containing the intervenable statuses of the
     *         variables in the exercise.
     */
    public Map<String, Boolean> getVariableIntervenableStatuses() {
        Map<String, Boolean> copy = new HashMap<String, Boolean>();

        for (String key : currentVariablesIntervenable.keySet()) {
            copy.put(key, currentVariablesIntervenable.get(key));
        }

        return copy;
    }

    //=======================================================================
    //
    // PRIVATE METHODS
    //
    //=======================================================================

    /*
     * CONSTRUCTOR
     */

    private Exercise(String version,
                     String prompt,
                     int isGodMode,
                     boolean buildNow,
                     String essayQuestion,
                     String essayAnswer,
                     String instructorFeedback,
                     String grade,
                     Integer resource_total,
                     Integer resource_per_obs,
                     Integer resource_per_int,
                     WindowInclusionStatus correctGraphInclusion,
                     WindowInclusionStatus correctManipulatedGraphInclusion,
                     WindowInclusionStatus populationInclusion,
                     WindowInclusionStatus experimentalSetupInclusion,
                     WindowInclusionStatus sampleInclusion,
                     WindowInclusionStatus hypotheticalGraphInclusion,
                     WindowInclusionStatus hypotheticalManipulatedGraphInclusion,
                     WindowInclusionStatus independenciesInclusion,
                     boolean isIncludeGuess,
                     Element cmds) {
        this.setVersion(version);
        this.setPrompt(prompt);
        this.setQuestion(essayQuestion);
        this.setAnswer(essayAnswer);

        this.setIsGodMode(isGodMode);//Integer.valueOf(mode));
        this.setBuildNow(buildNow);

        this.setInstructorFeedback(instructorFeedback);
        this.setScore(grade);

        this.setResourceTotal(resource_total);
        this.setResourcePerObs(resource_per_obs);
        this.setResourcePerInt(resource_per_int);

        this.setCorrectGraphInclusion(correctGraphInclusion);
        this.setCorrectManipulatedGraphInclusion(correctManipulatedGraphInclusion);
        this.setPopulationInclusion(populationInclusion);
        this.setExperimentalSetupInclusion(experimentalSetupInclusion);
        this.setSampleInclusion(sampleInclusion);
        this.setHypotheticalGraphInclusion(hypotheticalGraphInclusion);
        this.setHypotheticalManipulatedGraphInclusion(hypotheticalManipulatedGraphInclusion);
        this.setIndependenciesInclusion(independenciesInclusion);
        this.setStudentGuessInclusion(isIncludeGuess);
        if (cmds == null) {
            System.out.println("In ProblemPosed.java: cmds is null");

        }
        this.currentInitialCommands = cmds;
    }

//    /**
//     * Deep copy.
//     */
//    public Exercise(Exercise exercise) {
//        this.version = exercise.version;
//        this.id = exercise.id;
//        this.title = exercise.title;
//        this.goal = exercise.goal;
//        this.prompt = exercise.prompt;
//=
//        this.essayQuestion = exercise.essayQuestion;
//        this.essayAnswer = exercise.essayAnswer;
//        this.instructorFeedback = exercise.instructorFeedback.;
//        this.theScore = exercise.theScore;
//
//        this.buildNow = exercise.buildNow;
//        this.gmode = exercise.gmode;
//        this.numExercises = exercise.numExercises;
//        this.currentTrueModelIndex = exercise.currentTrueModelIndex
//
//        try {
//            this.bayesModelIm = (ArrayList<BayesIm>) new MarshalledObject(exercise.bayesModelIm).get();
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//
//        private ArrayList<BayesIm> bayesModelIm = new ArrayList<BayesIm>();
//        private ArrayList<SemIm> semModelIm = new ArrayList<SemIm>();
//        private ArrayList<Boolean> isUsingBayesIm = new ArrayList<Boolean>() ;
//        private ArrayList<Boolean> isIncludeGuess = new ArrayList<Boolean>();
//        private ArrayList<Element> initialCommands = new ArrayList<Element>();
//        private ArrayList<Map<String, Boolean>> variablesIntervenable = new ArrayList<Map<String, Boolean>>();
//
//        private BayesIm currentBayesModelIm;
//        private SemIm currentSemModelIm;
//        private boolean currentIsUsingBayesIm;
//        private boolean currentIsIncludeGuess;
//        private String currentExplanation;
//        private Element currentInitialCommands;
//        private Map<String, Boolean> currentVariablesIntervenable;
//
//        private WindowInclusionStatus correctGraphInclusion;
//        private WindowInclusionStatus correctManipulatedGraphInclusion;
//        private WindowInclusionStatus populationInclusion;
//        private WindowInclusionStatus experimentalSetupInclusion;
//        private WindowInclusionStatus sampleInclusion;
//        private WindowInclusionStatus hypotheticalGraphInclusion;
//        private WindowInclusionStatus hypotheticalManipulatedGraphInclusion;
//        private WindowInclusionStatus independenciesInclusion;
//
//        private Integer resource_total;
//        private Integer resource_per_obs;
//        private Integer resource_per_int;
//
//
//        this.setVersion(version);
//        this.setPrompt(prompt);
//        this.setQuestion(essayQuestion);
//        this.setAnswer(essayAnswer);
//
//        this.setIsGodMode(isGodMode);//Integer.valueOf(mode));
//        this.setBuildNow(Boolean.valueOf(buildNow).booleanValue());
//
//        this.setInstructorFeedback(instructorFeedback);
//        this.setScore(grade);
//
//        this.setResourceTotal(resource_total);
//        this.setResourcePerObs(resource_per_obs);
//        this.setResourcePerInt(resource_per_int);
//
//        this.setCorrectGraphInclusion(correctGraphInclusion);
//        this.setCorrectManipulatedGraphInclusion(correctManipulatedGraphInclusion);
//        this.setPopulationInclusion(populationInclusion);
//        this.setExperimentalSetupInclusion(experimentalSetupInclusion);
//        this.setSampleInclusion(sampleInclusion);
//        this.setHypotheticalGraphInclusion(hypotheticalGraphInclusion);
//        this.setHypotheticalManipulatedGraphInclusion(hypotheticalManipulatedGraphInclusion);
//        this.setIndependenciesInclusion(independenciesInclusion);
//        this.setStudentGuessInclusion(isIncludeGuess);
//        if (cmds == null) {
//            System.out.println("In ProblemPosed.java: cmds is null");
//
//        }
//        this.currentInitialCommands = cmds;
//    }


    /**
     * This takes in an Element intervenableStatuses and parses it into the
     * individual status. If this is null (ie it comes from an older version of
     * exercise, this method MUST be called after the model is specified.
     *
     * @param intervenableStatuses an XML element
     */
    private void parseIntervenableElements(Element intervenableStatuses) {
        Elements intervenableStatusElements = intervenableStatuses.getChildElements();

        for (int i = 0; i < intervenableStatusElements.size(); i++) {

            Element intervenableStatus = intervenableStatusElements.get(i);
            String varName = intervenableStatus.getAttribute(VARIABLE).getValue();
            String status = intervenableStatus.getAttribute(INTERVENABLE).getValue();

            this.setVariableIntervenable(varName, status.equals("yes"));
        }
    }


    /**
     * @return essayQuestion
     */

    public String getEssayQuestion() {
        if (essayQuestion == null)
            return "";
        return essayQuestion;
    }

    /**
     * @return essayAnswer
     */
    public String getEssayAnswer() {
        if (essayAnswer == null)
            return "";
        return essayAnswer;
    }

    /**
     * @return isGodMode
     */
    public int getIsGodMode() {
        return gmode;
    }

    /**
     * @return multipleExercises
     */
    public boolean getBuildNow() {
        return buildNow;
    }


    /**
     * @return essayAnswer
     */
    public String getInstructorFeedback() {
        return instructorFeedback;
    }

}
