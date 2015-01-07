package edu.cmu.causalityApp.exercise;

import edu.cmu.causality.*;
import edu.cmu.causality.chartBuilder.Histogram;
import edu.cmu.causality.chartBuilder.HistogramXml;
import edu.cmu.causality.chartBuilder.RegressionInfo;
import edu.cmu.causality.chartBuilder.ScatterPlot;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causalityApp.chartBuilder.CreateHistogramCommand;
import edu.cmu.causalityApp.chartBuilder.CreateRegressionCommand;
import edu.cmu.causalityApp.chartBuilder.CreateScatterplotCommand;
import edu.cmu.causalityApp.commands.CheckAnswerCommand;
import edu.cmu.causalityApp.commands.FocusWindowCommand;
import edu.cmu.causalityApp.commands.OpenWindowCommand;
import edu.cmu.causalityApp.commands.ShowAnswerCommand;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.causalityApp.dataEditors.CloseWindowCommand;
import edu.cmu.causalityApp.dataEditors.independenciesEditor.IndependenciesEditor;
import edu.cmu.causalityApp.dataEditors.independenciesEditor.SetIndependenceCommand;
import edu.cmu.command.Command;
import edu.cmu.command.StartCommand;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Text;

import javax.swing.*;
import java.util.*;

/**
 * Use this class to parse exercises for the 4.1 version of the lab
 *
 * @author ming yang koh
 */
public class ExerciseXmlParserV43 {

    /**
     * Specifies that this parser works for the version 4.0 of the lab.
     */
    public static final String VERSION = "4.3";

    /**
     * Takes an xml representation of a 3.2 exercise and creates the exercise.
     *
     * @param exerciseElement the xml
     * @return the exercise
     */
    public static Exercise getExercise(Element exerciseElement) {
        System.out.println("in getExercise in ExerciseXmlParserV43");
        if (!Exercise.EXERCISE.equals(exerciseElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + Exercise.EXERCISE + "' element");
        }

        String buildNow = "true";
        String instructions = "";
        int godMode = 0;

        for (int i = 0; i < exerciseElement.getAttributeCount(); i++) {
            Attribute attribute = exerciseElement.getAttribute(i);

            if (attribute.getQualifiedName().equals(Exercise.BUILDNOW)) {
                buildNow = attribute.getValue();
            }

            if (attribute.getQualifiedName().equals(Exercise.INSTRUCTIONS)) {
                instructions = attribute.getValue();
            }

            if (attribute.getQualifiedName().equals(Exercise.ISGODMODE)) {
                godMode = Integer.parseInt(attribute.getValue());
            }
        }

//        boolean gotExercise = exerciseElement.getAttribute(Exercise.BUILDNOW).getValue().equalsIgnoreCase("true");
        boolean gotExercise = buildNow.equalsIgnoreCase("true");

        Exercise exercise;

        // if the exercise opened is a GodMode exercise, and if it doesn't have any of them
        if (gotExercise) {
            Element windowsElement = exerciseElement.getFirstChildElement(Exercise.WINDOWS);

            //experimental constraints elements
            Element trueGraphElement = exerciseElement.getFirstChildElement(Exercise.TRUEGRAPH);

            // HACK. I added this to allow files from July 08 to be loaded. In
            // these files, there is no trueGraph element; everything that is
            // now in the trueGraph element is a child of the exercise element.
            if (trueGraphElement == null) trueGraphElement = exerciseElement;

            Element expConstElement = trueGraphElement.getFirstChildElement(Exercise.EXPERIMENTAL_CONSTRAINTS);

            Element resourcesElement = null;
            Element intervenableElement = null;
            try {
                resourcesElement = expConstElement.getFirstChildElement(Exercise.RESOURCES);
                intervenableElement = expConstElement.getFirstChildElement(Exercise.INTERVENABLE_STATUSES);
            } catch (NullPointerException npe) {
                //do nothing
            }

            System.out.println("!!! " + intervenableElement.toXML());

            Element bayesImElement = trueGraphElement.getFirstChildElement("bayesNet");
            Element semImElement = trueGraphElement.getFirstChildElement(SemXmlConstants.SEM);
            Element commandsElement = exerciseElement.getFirstChildElement("commands");

            Element essayQuestions = exerciseElement.getFirstChildElement("essayQuestions");
            Element essayAnswers = exerciseElement.getFirstChildElement("essayAnswers");
            Element instructorFeedback = exerciseElement.getFirstChildElement("instructorFeedback");
            Element gradeScore = exerciseElement.getFirstChildElement("gradeScore");
            Element correctGraph = windowsElement.getFirstChildElement(Exercise.CORRECT_GRAPH);
            Element correctManipGraph = windowsElement.getFirstChildElement(Exercise.CORRECT_MANIPULATED_GRAPH);
            Element population = windowsElement.getFirstChildElement(Exercise.POPULATION);
            Element experiment = windowsElement.getFirstChildElement(Exercise.EXPERIMENTAL_SETUP);
            Element sample = windowsElement.getFirstChildElement(Exercise.SAMPLE);
            Element hypGraph = windowsElement.getFirstChildElement(Exercise.HYPOTHETICAL_GRAPH);
            Element hypManipGraph = windowsElement.getFirstChildElement(Exercise.HYPOTHETICAL_MANIPULATED_GRAPH);
            Element indep = windowsElement.getFirstChildElement(Exercise.INDEPENDENCIES);

//            Exercise exercise;

            Integer total_resource = null;  // = new Integer(Exercise.DEFAULT_RESOURCE_TOTAL);
            Integer resourceObs = null;     //new Integer(Exercise.DEFAULT_RESOURCE_OBS);
            Integer resourceInt = null;     // new Integer(Exercise.DEFAULT_RESOURCE_TOTAL);
            if (resourcesElement != null) {
                total_resource = new Integer(resourcesElement.getAttributeValue(Exercise.RESOURCE_TOTAL));
                resourceObs = new Integer(resourcesElement.getAttributeValue(Exercise.RESOURCE_PER_OBSERVATION));
                resourceInt = new Integer(resourcesElement.getAttributeValue(Exercise.RESOURCE_PER_INTERVENTION));
            }

            if (bayesImElement != null) {
                BayesIm bayesIm = ExerciseXmlParserV43.getBayesIm(bayesImElement);

                exercise = new Exercise(instructions,
                        godMode,
                        Boolean.parseBoolean(buildNow),
                        ExerciseXmlParserV43.getEssayQuestions(essayQuestions),
                        ExerciseXmlParserV43.getEssayAnswers(essayAnswers),
                        ExerciseXmlParserV43.getInstructorFeedback(instructorFeedback),
                        ExerciseXmlParserV43.getGradeScore(gradeScore),
                        bayesIm,
                        total_resource,
                        resourceObs,
                        resourceInt,
                        intervenableElement,
                        ExerciseXmlParserV43.getWindowStatus(correctGraph),
                        ExerciseXmlParserV43.getWindowStatus(correctManipGraph),
                        ExerciseXmlParserV43.getWindowStatus(population),
                        ExerciseXmlParserV43.getWindowStatus(experiment),
                        ExerciseXmlParserV43.getWindowStatus(sample),
                        ExerciseXmlParserV43.getWindowStatus(hypGraph),
                        ExerciseXmlParserV43.getWindowStatus(hypManipGraph),
                        ExerciseXmlParserV43.getWindowStatus(indep),
                        ExerciseXmlParserV43.str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()),
                        commandsElement);
            } else {
                SemIm semIm = ExerciseXmlParserV43.getSemIm(semImElement);

                exercise = new Exercise(instructions,
                        godMode,
                        Boolean.parseBoolean(buildNow),
                        ExerciseXmlParserV43.getEssayQuestions(essayQuestions),
                        ExerciseXmlParserV43.getEssayAnswers(essayAnswers),
                        ExerciseXmlParserV43.getInstructorFeedback(instructorFeedback),
                        ExerciseXmlParserV43.getGradeScore(gradeScore),
                        semIm,
                        total_resource,
                        resourceObs,
                        resourceInt,
                        intervenableElement,
                        ExerciseXmlParserV43.getWindowStatus(correctGraph),
                        ExerciseXmlParserV43.getWindowStatus(correctManipGraph),
                        ExerciseXmlParserV43.getWindowStatus(population),
                        ExerciseXmlParserV43.getWindowStatus(experiment),
                        ExerciseXmlParserV43.getWindowStatus(sample),
                        ExerciseXmlParserV43.getWindowStatus(hypGraph),
                        ExerciseXmlParserV43.getWindowStatus(hypManipGraph),
                        ExerciseXmlParserV43.getWindowStatus(indep),
                        ExerciseXmlParserV43.str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()),
                        commandsElement);
            }

            exercise.setId(exerciseElement.getAttribute(Exercise.ID).getValue());

            //for some reason, some of the exercises don't have title in right place!!!
            try {
                exercise.setTitle(exerciseElement.getAttribute(Exercise.TITLE).getValue());
            } catch (NullPointerException npe) {
                System.out.println("Title not in right place!!  using id instead");
                exercise.setTitle(exerciseElement.getAttribute(Exercise.ID).getValue());
            }
        } else {
            exercise = new Exercise(VERSION, instructions, godMode,
                    Boolean.parseBoolean(buildNow));
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


    private static String getEssayQuestions(Element essayQuestions) {
        String name = "essayQuestions";

        if (!(name.equals(essayQuestions.getQualifiedName()))) {
            throw new IllegalArgumentException("Expecting '" + name + "' element");
        }
        Element child = essayQuestions.getFirstChildElement("question");

        if (child != null) {
            return (child.getAttributeValue("questionText"));
        } else {
            return null;
        }
    }

    private static String getEssayAnswers(Element essayAnswers) {
        String name = "essayAnswers";

        if (!(name.equals(essayAnswers.getQualifiedName()))) {
            throw new IllegalArgumentException("Expecting '" + name + "' element");
        }
        Element child = essayAnswers.getFirstChildElement("answer");

        if (child != null) {
            return (child.getAttributeValue("answerText"));
        } else {
            return null;
        }
    }

    private static String getInstructorFeedback(Element instructorFeedback) {
        String name = "instructorFeedback";

        if (!(name.equals(instructorFeedback.getQualifiedName()))) {
            throw new IllegalArgumentException("Expecting '" + name + "' element");
        }
        Element child = instructorFeedback.getFirstChildElement("feedback");

        if (child != null) {
            return (child.getAttributeValue("feedbackText"));
        } else {
            return null;
        }
    }

    private static String getGradeScore(Element gradeScore) {
        String name = "gradeScore";

        if (gradeScore == null) {
            return null;
        }
        if (!(name.equals(gradeScore.getQualifiedName()))) {
            throw new IllegalArgumentException("Expecting '" + name + "' element");
        }
        Element child = gradeScore.getFirstChildElement("grade");

        if (child != null) {
            return (child.getAttributeValue("gradeText"));
        } else {
            return null;
        }
    }

    //=========================================================================
    // parse moves history
    //=========================================================================
    /**
     * The xml tag for the element holding the commands.
     */
    private static final String COMMANDS = "commands";

    //////////////////////////////////////
    // private XML constants
    private static final String CheckAnswerCommand_MY_NAME = "checkAnswerCommand";
    private static final String HYPOTHETICAL_GRAPH = "hypotheticalGraph";
    private static final String IS_ANSWER_CORRECT = "isAnswerCorrect";
    private static final String NUMBER_OF_GUESSES = "numberOfGuesses";
    private static final String YES = "yes";

    private static final String CreateExperimentalSetupCommand_MY_NAME = "createExperimentalSetupCommand";
    private final static String EXPSETUP = "expSetup";
    private final static String NAME = "name";
    private final static String IGNORED = "ignored";
    private final static String EXPVARIABLE = "expVariable";
    private final static String MANIPULTATION = "manipulation";
    private final static String LOCKEDAT = "lockedAt";
    private final static String MEAN = "mean";
    private final static String SD = "sd";

    private static final String CreateHistogramCommand_MY_NAME = "createHistogramCommand";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String HISTOGRAM = "histogram";
    private static final String EXPERIMENTAL_SETUP = "experimentalSetup";
    private static final String SAMPLE = "sample";
    private static final String TRUE_MODEL_NAME = "truemodelName";
    private static final String CHARTED_VARS = "chartedVars";
    private static final String CHARTED_VAR = "chartedVar";
    private static final String CONDITIONED_VARS = "conditionedVars";
    private static final String CONDITIONED_VAR = "conditionedVar";
    private static final String VALUE = "value";

    private static final String CreateHypothesisCommand_MY_NAME = "createHypothesisCommand";
    private static final String HYPOTHETICALGRAPH = "hypGraph";
    private static final String VARIABLES = "hypVariables";
    private static final String VARIABLE = "hypVariable";
    private static final String TYPE = "type";
    private static final String CENTERX = "centerX";
    private static final String CENTERY = "centerY";
    private static final String EDGES = "hypEdges";
    private static final String EDGE = "hypEdge";
    private static final String FROM = "causeVar";
    private static final String TO = "effectVar";

    private static final String CreateSampleCommand_MY_NAME = "createSampleCommand";
    private static final String EXP_SETUP_NAME = "experimentalSetupName";
    private static final String SAMPLE_NAME = "sampleName";
    private static final String SAMPLE_SIZE = "sampleSize";
    private static final String SAMPLE_SEED = "sampleSeed";
    private static final String SAMPLE_COST = "sampleCost";

    private static final String CreateScatterplotCommand_MY_NAME = "createScatterPlotCommand";
    private static final String SCATTERPLOT = "scatterplot";
    private static final String RESPONSE_VAR = "responseVariable";
    private static final String PREDICTOR_VAR = "predictorVariable";
    private static final String INCLUDE_LINE = "includeRegressionLine";

    private static final String DeleteExperimentalSetupCommand_MY_NAME = "deleteExperimentalSetupCommand";
    private static final String EXP_NAME = "experimentName";

    private static final String DeleteHypothesisCommand_MY_NAME = "deleteHypothesisCommand";
    private static final String HYP_NAME = "hypothesisName";

    private static final String EditHypothesisCommand_MY_NAME = "editHypothesisCommand";

    static private final String ShowAnswerCommand_MY_NAME = "showAnswerCommand";
    static private final String GUESSES_SO_FAR = "guesseSoFar";
    static private final String ANSWER_HIDDEN = "answerCurrentlyHidden";

    private static final String StartCommand_MY_NAME = "startCommand";

    private static final String FocusWindowCommand_MY_NAME = "focusWindowCommand";
    private static final String EDITOR = "editor";

    private static final String CreateRegressionCommand_MY_NAME = "createRegressionCommand";
    private static final String REGRESSION = "regression";

    private static final String SetIndependenceCommand_MY_NAME = "setIndependenceCommand";
    private static final String EXPERIMENT = "experimentalSetup";
    private static final String COLUMN = "column";
    private static final String ROWi = "row";
    private static final String SET_TO = "setTo";
    private static final String INDEPENDENT = "independent";

    private static final String OpenWindowCommand_MY_NAME = "openWindowCommand";
    private static final String CloseWindowCommand_MY_NAME = "closeWindowCommand";

    /**
     * Takes an xml element that represents a moves and turns in into a
     * <p/>
     * moves.
     *
     * @param element  the xml representation of the moves
     * @param appPanel needed to create some of the commands
     * @return the moves
     */
    public static Command[] parse(Element element, ParserLabPanel appPanel) {
        if (element == null) {
            return null;
        }
        if (!ExerciseXmlParserV43.COMMANDS.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.COMMANDS +
                    "' element");
        }

        Elements commands = element.getChildElements();
        Element cmdE;
        String name;
        List<Command> cmdsList = new ArrayList<Command>();
        Command cmd = null;

        for (int i = 0; i < commands.size(); i++) {
            cmdE = commands.get(i);
            name = cmdE.getQualifiedName();
            if (name.equals(ExerciseXmlParserV43.CheckAnswerCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCheckAnswerCommand(appPanel, cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CloseWindowCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCloseWindowCommand(appPanel, cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CreateExperimentalSetupCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCreateExperimentalSetupCommand(CausalityLabModel.getModel(), cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CreateHistogramCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCreateHistogramCommand(appPanel.getDesktop(), cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CreateHypothesisCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCreateHypothesisCommand(cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CreateSampleCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCreateSampleCommand(cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CreateScatterplotCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCreateScatterplotCommand(appPanel.getDesktop(), cmdE);
            } else if (name.equals(ExerciseXmlParserV43.DeleteExperimentalSetupCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseDeleteExperimentalSetupCommand(cmdE);
            } else if (name.equals(ExerciseXmlParserV43.DeleteHypothesisCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseDeleteHypothesisCommand(cmdE);
            } else if (name.equals(ExerciseXmlParserV43.EditHypothesisCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseEditHypothesisCommand(cmdE);
            } else if (name.equals(ExerciseXmlParserV43.OpenWindowCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseOpenWindowCommand(appPanel, cmdE);
            } else if (name.equals(ExerciseXmlParserV43.ShowAnswerCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseShowAnswerCommand(appPanel, cmdE);
            } else if (name.equals(ExerciseXmlParserV43.SetIndependenceCommand_MY_NAME)) {
                IndependenciesEditor ed;
                ed = (IndependenciesEditor) appPanel.getEditor(IndependenciesEditor.MY_NAME);
                cmd = ExerciseXmlParserV43.parseSetIndependenceCommand(ed, cmdE);
            } else if (name.equals(ExerciseXmlParserV43.StartCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseStartCommand(cmdE);
            } else if (name.equals(ExerciseXmlParserV43.FocusWindowCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseFocusWindowCommand(appPanel, cmdE);
            } else if (name.equals(ExerciseXmlParserV43.CreateRegressionCommand_MY_NAME)) {
                cmd = ExerciseXmlParserV43.parseCreateRegressionCommand(appPanel.getDesktop(), cmdE);
            } else {
                System.err.println("Didn't parse moves: " + name +
                        " in ExerciseXmlParserV43");
            }

            cmdsList.add(cmd);
        }
        Command[] cmds = new Command[commands.size()];
        Iterator<Command> it = cmdsList.iterator();
        for (int i = 0; it.hasNext(); i++) {
            cmds[i] = it.next();
        }

        return cmds;
    }

    //////////////////////////////////////////////////////
    // Private parsing commands for this version of parser

    private static CheckAnswerCommand parseCheckAnswerCommand(ParserLabPanel labPanel, Element element) {
        if (!ExerciseXmlParserV43.CheckAnswerCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CheckAnswerCommand_MY_NAME + "' element");
        }

//        CausalityLabPanel labPanel     = lab.getCausalityLabPanel();
        String hypGraphName = element.getAttributeValue(ExerciseXmlParserV43.HYPOTHETICAL_GRAPH);
        int numGuesses = new Integer(element.getAttributeValue(ExerciseXmlParserV43.NUMBER_OF_GUESSES));
        boolean isCorrect = (element.getAttributeValue(ExerciseXmlParserV43.IS_ANSWER_CORRECT).equals(ExerciseXmlParserV43.YES));

        return new CheckAnswerCommand(labPanel.asJComponent(), isCorrect, hypGraphName, numGuesses);
    }

    private static CreateExperimentalSetupCommand parseCreateExperimentalSetupCommand(CausalityLabModel clm, Element element)
            throws IllegalArgumentException {
        if (!ExerciseXmlParserV43.CreateExperimentalSetupCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CreateExperimentalSetupCommand_MY_NAME + "' element");
        }
        Element esElement = element.getFirstChildElement(ExerciseXmlParserV43.EXPSETUP);
        ExperimentalSetup experiment = ExerciseXmlParserV43.parseStudiedVariables(esElement);

        return new CreateExperimentalSetupCommand(clm, experiment);
    }

    /**
     * Convert xml to an experimental setup.
     *
     * @param expElement the xml.
     * @return an experimental setup.
     */
    private static ExperimentalSetup parseStudiedVariables(Element expElement) {
        if (!ExerciseXmlParserV43.EXPSETUP.equals(expElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + expElement + "' element");
        }
        int i;
        Element var;
        String manip, varName, ignored;
        String name = expElement.getAttributeValue(ExerciseXmlParserV43.NAME);
        Elements vars = expElement.getChildElements(ExerciseXmlParserV43.EXPVARIABLE);
        String[] varNames = new String[vars.size()];
        double[] means = new double[vars.size()];
        double[] sds = new double[vars.size()];

        for (i = 0; i < vars.size(); i++) {
            var = vars.get(i);
            varNames[i] = var.getAttributeValue(ExerciseXmlParserV43.NAME);
            means[i] = Double.parseDouble(var.getAttributeValue(ExerciseXmlParserV43.MEAN));
            sds[i] = Double.parseDouble(var.getAttributeValue(ExerciseXmlParserV43.SD));
        }

        ExperimentalSetup es = new ExperimentalSetup(name, varNames, means, sds);

        for (i = 0; i < vars.size(); i++) {
            var = vars.get(i);
            varName = var.getAttributeValue(ExerciseXmlParserV43.NAME);
            ignored = var.getAttributeValue(ExerciseXmlParserV43.IGNORED);
            if (ignored.equals("yes")) {
                es.getVariable(varName).setStudied(false);
            }
            manip = var.getAttributeValue(ExerciseXmlParserV43.MANIPULTATION);
            if (ManipulationType.NONE.toString().equals(manip)) {
                //don't have to do anything
            } else if (ManipulationType.RANDOMIZED.toString().equals(manip)) {
                es.getVariable(varName).setRandomized();
            } else if (ManipulationType.LOCKED.toString().equals(manip)) {
                String value = var.getAttributeValue(ExerciseXmlParserV43.LOCKEDAT);
                es.getVariable(varName).setLocked(value);
            }
        }
        return es;
    }

    /**
     * Reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param desktop for getting interface references
     * @param element the xml representation of the moves
     */
    private static CreateHistogramCommand parseCreateHistogramCommand(JDesktopPane desktop, Element element) {
        if (!ExerciseXmlParserV43.CreateHistogramCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CreateHistogramCommand_MY_NAME + "' element");
        }
        int x = Integer.parseInt(element.getAttributeValue(ExerciseXmlParserV43.X));
        int y = Integer.parseInt(element.getAttributeValue(ExerciseXmlParserV43.Y));

        Histogram histogram = ExerciseXmlParserV43.parseHistogram(element.getFirstChildElement(HistogramXml.HISTOGRAM));

        return new CreateHistogramCommand(x, y, desktop, histogram);
    }

    /**
     * Parse the given xml element into a histogram.
     *
     * @param element xml element.
     * @return a corresponding histogram represented by that xml element.
     */
    private static Histogram parseHistogram(Element element) {
        if (!ExerciseXmlParserV43.HISTOGRAM.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.HISTOGRAM + "' element");
        }
        String exptName = element.getAttributeValue(ExerciseXmlParserV43.EXPERIMENTAL_SETUP);
        String sampleName = element.getAttributeValue(ExerciseXmlParserV43.SAMPLE);
        String truename = element.getAttributeValue(ExerciseXmlParserV43.TRUE_MODEL_NAME);

        Elements chartedVars = element.getFirstChildElement(ExerciseXmlParserV43.CHARTED_VARS).getChildElements(ExerciseXmlParserV43.CHARTED_VAR);
        String[] varsToChart = new String[chartedVars.size()];
        for (int i = 0; i < chartedVars.size(); i++) {
            varsToChart[i] = chartedVars.get(i).getAttributeValue(ExerciseXmlParserV43.NAME);
        }

        Elements conditionedVars = element.getFirstChildElement(ExerciseXmlParserV43.CONDITIONED_VARS).getChildElements(ExerciseXmlParserV43.CONDITIONED_VAR);
        String[] condVars = new String[conditionedVars.size()];
        String[] condState = new String[conditionedVars.size()];
        for (int i = 0; i < conditionedVars.size(); i++) {
            condVars[i] = conditionedVars.get(i).getAttributeValue(ExerciseXmlParserV43.NAME);
            condState[i] = conditionedVars.get(i).getAttributeValue(ExerciseXmlParserV43.VALUE);
        }

        return new Histogram(exptName, sampleName, truename, varsToChart, condVars, condState);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves.
     */
    private static CreateHypothesisCommand parseCreateHypothesisCommand(Element element) {
        if (!ExerciseXmlParserV43.CreateHypothesisCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CreateHypothesisCommand_MY_NAME + "' element");
        }
        Element elm = element.getFirstChildElement(ExerciseXmlParserV43.HYPOTHETICALGRAPH);
        HypotheticalGraph newGraph = ExerciseXmlParserV43.parseHypotheticalGraphElement(elm);

        return new CreateHypothesisCommand(newGraph);
    }

    /**
     * Parses an xml representation of a hypothetical graph and renders it into
     * a hypothetical graph.
     *
     * @return the corresponding hypothetical graph.
     */
    private static HypotheticalGraph parseHypotheticalGraphElement(Element hgElement) {
        if (!ExerciseXmlParserV43.HYPOTHETICALGRAPH.equals(hgElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.HYPOTHETICALGRAPH + "' element");
        }
        int i;
        GraphNode node;
        String type;
        String name = hgElement.getAttributeValue(ExerciseXmlParserV43.NAME);
        Dag dag = new Dag();
        List<GraphNode> latents = new ArrayList<GraphNode>();

        //add all the variables
        Elements variables = hgElement.getFirstChildElement(ExerciseXmlParserV43.VARIABLES).getChildElements(ExerciseXmlParserV43.VARIABLE);
        Element variableE;
        for (i = 0; i < variables.size(); i++) {
            variableE = variables.get(i);
            node = new GraphNode(variableE.getAttributeValue(ExerciseXmlParserV43.NAME));
            node.setCenter(
                    Integer.parseInt(variableE.getAttributeValue(ExerciseXmlParserV43.CENTERX)),
                    Integer.parseInt(variableE.getAttributeValue(ExerciseXmlParserV43.CENTERY)));
            type = variableE.getAttributeValue(ExerciseXmlParserV43.TYPE);
            if (type.equals(NodeType.LATENT.toString())) {
                latents.add(node);
            } else {
                dag.addNode(node);
            }
        }

        HypotheticalGraph hg = new HypotheticalGraph(name, dag, false, false);
        for (GraphNode latent : latents) {
            node = latent;
            hg.addLatentVariable(node.getName(), node.getCenterX(), node.getCenterY());
        }

        //add all the edges
        Elements edges = hgElement.getFirstChildElement(ExerciseXmlParserV43.EDGES).getChildElements(ExerciseXmlParserV43.EDGE);
        Element edge;
        for (i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            Node fromNode = hg.getNode(edge.getAttributeValue(ExerciseXmlParserV43.FROM));
            Node toNode = hg.getNode(edge.getAttributeValue(ExerciseXmlParserV43.TO));
            hg.addDirectedEdge(fromNode, toNode);
        }

        return hg;
    }


    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves
     */
    private static CreateSampleCommand parseCreateSampleCommand(Element element) {
        if (!ExerciseXmlParserV43.CreateSampleCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CreateSampleCommand_MY_NAME + "' element");
        }
        String experimentalSetupName = element.getAttributeValue(ExerciseXmlParserV43.EXP_SETUP_NAME);
        int sampleSize = Integer.parseInt(element.getAttributeValue(ExerciseXmlParserV43.SAMPLE_SIZE));
        String sampleName = element.getAttributeValue(ExerciseXmlParserV43.SAMPLE_NAME);
        Long sampleSeed = new Long(element.getAttributeValue(ExerciseXmlParserV43.SAMPLE_SEED));
        int sampleCost = Integer.parseInt(element.getAttributeValue(ExerciseXmlParserV43.SAMPLE_COST));

        return new CreateSampleCommand(experimentalSetupName, sampleSize, sampleName, sampleSeed, sampleCost);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves
     */
    private static CreateScatterplotCommand parseCreateScatterplotCommand(JDesktopPane desktop, Element element) {
        if (!ExerciseXmlParserV43.CreateScatterplotCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CreateScatterplotCommand_MY_NAME + "' element");
        }
        ScatterPlot scatterplot = ExerciseXmlParserV43.parseScatterPlot(
                element.getFirstChildElement(ExerciseXmlParserV43.SCATTERPLOT));
        int x = Integer.parseInt(element.getAttributeValue(ExerciseXmlParserV43.X));
        int y = Integer.parseInt(element.getAttributeValue(ExerciseXmlParserV43.Y));

        return new CreateScatterplotCommand(desktop, scatterplot, x, y);
    }

    /**
     * Parse a given scatterplot xml element into the Scatterplot class.
     *
     * @param element xml element of scatterplot.
     * @return the ScatterPlot class containing the relevant information to
     *         create the scatterplot.
     */
    private static ScatterPlot parseScatterPlot(Element element) {
        if (!ExerciseXmlParserV43.SCATTERPLOT.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.SCATTERPLOT + "' element");
        }
        String exptName = element.getAttributeValue(ExerciseXmlParserV43.EXPERIMENTAL_SETUP);
        String sampleName = element.getAttributeValue(ExerciseXmlParserV43.SAMPLE);
        String truename = element.getAttributeValue(ExerciseXmlParserV43.TRUE_MODEL_NAME);
        String responseVar = element.getAttributeValue(ExerciseXmlParserV43.RESPONSE_VAR);
        String predictorVar = element.getAttributeValue(ExerciseXmlParserV43.PREDICTOR_VAR);
        String includeLine = element.getAttributeValue(ExerciseXmlParserV43.INCLUDE_LINE);

        boolean hasLine = (includeLine.equals(ExerciseXmlParserV43.YES));

        return new ScatterPlot(exptName, sampleName, truename, hasLine, responseVar, predictorVar);
    }


    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves.
     */
    private static DeleteExperimentalSetupCommand parseDeleteExperimentalSetupCommand(Element element) {
        if (!ExerciseXmlParserV43.DeleteExperimentalSetupCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.DeleteExperimentalSetupCommand_MY_NAME + "' element");
        }
        String experimentName = element.getAttributeValue(ExerciseXmlParserV43.EXP_NAME);

        return new DeleteExperimentalSetupCommand(experimentName);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves.
     */
    private static DeleteHypothesisCommand parseDeleteHypothesisCommand(Element element) {
        if (!ExerciseXmlParserV43.DeleteHypothesisCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.DeleteHypothesisCommand_MY_NAME + "' element");
        }
        String hypName = element.getAttributeValue(ExerciseXmlParserV43.HYP_NAME);

        return new DeleteHypothesisCommand(hypName);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves.
     */
    private static EditHypothesisCommand parseEditHypothesisCommand(Element element) {
        if (!ExerciseXmlParserV43.EditHypothesisCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.EditHypothesisCommand_MY_NAME + "' element");
        }
        Element elm = element.getFirstChildElement(ExerciseXmlParserV43.HYPOTHETICALGRAPH);
        HypotheticalGraph newGraph = ExerciseXmlParserV43.parseHypotheticalGraphElement(elm);

        return new EditHypothesisCommand(newGraph);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves
     */
    private static ShowAnswerCommand parseShowAnswerCommand(ParserLabPanel panel, Element element) {
        if (!ExerciseXmlParserV43.ShowAnswerCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.ShowAnswerCommand_MY_NAME + "' element");
        }

        boolean is_hidden = (element.getAttributeValue(ExerciseXmlParserV43.ANSWER_HIDDEN)).equals(ExerciseXmlParserV43.YES);
        int num_guess = new Integer(element.getAttributeValue(ExerciseXmlParserV43.GUESSES_SO_FAR));

        return new ShowAnswerCommand(panel, is_hidden, num_guess);
    }


    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves
     */
    private static StartCommand parseStartCommand(Element element) {
        if (!ExerciseXmlParserV43.StartCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.StartCommand_MY_NAME + "' element");
        }

        return new StartCommand();
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param labPanel the labPanel to get the editors
     * @param element  the xml representation of the moves
     */
    private static FocusWindowCommand parseFocusWindowCommand(ParserLabPanel labPanel, Element element) {
        if (!ExerciseXmlParserV43.FocusWindowCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.FocusWindowCommand_MY_NAME + "' element");
        }
        String editorName = element.getAttributeValue(ExerciseXmlParserV43.EDITOR);
        AbstractEditor editor = labPanel.getEditor(editorName);

        return new FocusWindowCommand(editor);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param element the xml representation of the moves
     */
    private static CreateRegressionCommand parseCreateRegressionCommand(JDesktopPane desktop, Element element) {
        if (!ExerciseXmlParserV43.CreateRegressionCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CreateRegressionCommand_MY_NAME + "' element");
        }
        RegressionInfo regression = ExerciseXmlParserV43.parseRegression(
                element.getFirstChildElement(ExerciseXmlParserV43.REGRESSION));
        int x = new Integer(element.getAttributeValue(ExerciseXmlParserV43.X));
        int y = new Integer(element.getAttributeValue(ExerciseXmlParserV43.Y));

        return new CreateRegressionCommand(desktop, regression, x, y);
    }

    /**
     * Parse a given regression xml element into the regressionInfo class.
     *
     * @param element xml element of regression.
     * @return the RegressionInfo class containing the relevant information to
     *         run the regression.
     */
    private static RegressionInfo parseRegression(Element element) {
        if (!ExerciseXmlParserV43.REGRESSION.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.REGRESSION + "' element");
        }
        String exptName = element.getAttributeValue(ExerciseXmlParserV43.EXPERIMENTAL_SETUP);
        String sampleName = element.getAttributeValue(ExerciseXmlParserV43.SAMPLE);
        String responseVar = element.getAttributeValue(ExerciseXmlParserV43.RESPONSE_VAR);

        Elements predictorVarsElts = element.getChildElements(ExerciseXmlParserV43.PREDICTOR_VAR);
        String[] predictorVars = new String[predictorVarsElts.size()];

        for (int i = 0; i < predictorVarsElts.size(); i++) {
            Element predictorVar = predictorVarsElts.get(i);
            predictorVars[i] = predictorVar.getAttributeValue(ExerciseXmlParserV43.NAME);
        }

        return new RegressionInfo(
                exptName,
                sampleName,
                responseVar,
                predictorVars);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param editor  the application, for getting interface references
     * @param element the xml representation of the moves
     */
    private static SetIndependenceCommand parseSetIndependenceCommand(IndependenciesEditor editor, Element element) {
        if (!ExerciseXmlParserV43.SetIndependenceCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.SetIndependenceCommand_MY_NAME + "' element");
        }

        boolean isIndependent = (element.getAttributeValue(ExerciseXmlParserV43.SET_TO).equals(ExerciseXmlParserV43.INDEPENDENT));
        int row = new Integer(element.getAttributeValue(ExerciseXmlParserV43.ROWi));
        String colName = element.getAttributeValue(ExerciseXmlParserV43.COLUMN);
        String exptName = element.getAttributeValue(ExerciseXmlParserV43.EXPERIMENT);

        return new SetIndependenceCommand(editor, isIndependent, row, colName, exptName);
    }

    /**
     * Constructor for reconstructing moves from xml representation.  Used
     * when reading in list of saved commands from exercise file.
     *
     * @param labPanel the panel to get the editors
     * @param element  the xml representation of the moves
     */
    private static OpenWindowCommand parseOpenWindowCommand(ParserLabPanel labPanel, Element element) {
        if (!ExerciseXmlParserV43.OpenWindowCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.OpenWindowCommand_MY_NAME + "' element");
        }
        String editorName = element.getAttributeValue(ExerciseXmlParserV43.EDITOR);
        AbstractEditor editor = labPanel.getEditor(editorName);

        return new OpenWindowCommand(editor);
    }


    /**
     * Extracts the name of the editor from an xml representation of this
     * moves
     *
     * @param element the xml representation
     * @return the name of the editor to close
     */
    private static CloseWindowCommand parseCloseWindowCommand(ParserLabPanel labPanel, Element element) {
        if (!ExerciseXmlParserV43.CloseWindowCommand_MY_NAME.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + ExerciseXmlParserV43.CloseWindowCommand_MY_NAME + "' element");
        }

        String edName = element.getAttributeValue(ExerciseXmlParserV43.EDITOR);
        AbstractEditor ed = labPanel.getEditor(edName);

        return new CloseWindowCommand(ed);
    }

    //==========================================================================
    // Bayes parsing code
    //==========================================================================

    //private static HashMap namesToVars;
    //private static List variables;

    /**
     * Takes an xml representation of a Bayes IM and reinstantiates the IM
     *
     * @param element the xml of the IM
     * @return the BayesIM
     */
    private static BayesIm getBayesIm(Element element) {
        if (!BayesXmlConstants.BAYES_NET.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.BAYES_NET + " element.");
        }

        Elements elements = element.getChildElements();

        Element element0 = elements.get(0);
        Element element1 = elements.get(1);
        Element element2 = elements.get(2);

        HashMap<String, Variable> namesToVars = new HashMap<String, Variable>();

        List<DiscreteVariable> variables = ExerciseXmlParserV43.getVariables(namesToVars, element0);
        BayesPm bayesPm = ExerciseXmlParserV43.makeBayesPm(namesToVars, variables, element1);

        return ExerciseXmlParserV43.makeBayesIm(bayesPm, element2);
    }


    private static List<DiscreteVariable> getVariables(HashMap<String, Variable> namesToVars, Element element0) {
        if (!BayesXmlConstants.BN_VARIABLES.equals(element0.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.BN_VARIABLES + " element.");
        }

        List<DiscreteVariable> variables = new LinkedList<DiscreteVariable>();

        Elements elements = element0.getChildElements();

        for (int i = 0; i < elements.size(); i++) {
            Element e1 = elements.get(i);
            Elements e2Elements = e1.getChildElements();


            if (!BayesXmlConstants.DISCRETE_VARIABLE.equals(e1.getQualifiedName())) {
                throw new IllegalArgumentException("Expecting " +
                        BayesXmlConstants.DISCRETE_VARIABLE + " " + "element.");
            }

            String name = e1.getAttributeValue(BayesXmlConstants.NAME);
            String isLatentVal = e1.getAttributeValue(BayesXmlConstants.LATENT);
            boolean isLatent = (isLatentVal != null) && ((isLatentVal.equals(BayesXmlConstants.YES)));
            Integer x = new Integer(e1.getAttributeValue(BayesXmlConstants.X));
            Integer y = new Integer(e1.getAttributeValue(BayesXmlConstants.Y));

            int numCategories = e2Elements.size();
            List<String> categories = new ArrayList<String>();

            for (int j = 0; j < numCategories; j++) {
                Element e2 = e2Elements.get(j);

                if (!BayesXmlConstants.CATEGORY.equals(e2.getQualifiedName())) {
                    throw new IllegalArgumentException("Expecting " +
                            BayesXmlConstants.CATEGORY + " " + "element.");
                }

                categories.add(e2.getAttributeValue(BayesXmlConstants.NAME));
            }

            DiscreteVariable var = new DiscreteVariable(name, categories);
            if (isLatent) {
                var.setNodeType(NodeType.LATENT);
            }
            var.setCenterX(x.intValue());
            var.setCenterY(y.intValue());
            variables.add(var);
        }

        namesToVars.clear();

        for (DiscreteVariable variable : variables) {
            String name = variable.getName();
            namesToVars.put(name, variable);
        }

        return variables;
    }


    private static BayesPm makeBayesPm(HashMap<String, Variable> namesToVars,
                                       List<DiscreteVariable> variables,
                                       Element element1) {
        if (!BayesXmlConstants.PARENTS.equals(element1.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.PARENTS + " element.");
        }

        Dag graph = new Dag();

        for (DiscreteVariable variable : variables) {
            graph.addNode(variable);
        }

        Elements elements = element1.getChildElements();

        for (int i = 0; i < elements.size(); i++) {
            Element e1 = elements.get(i);

            if (!BayesXmlConstants.PARENTS_FOR.equals(e1.getQualifiedName())) {
                throw new IllegalArgumentException("Expecting " +
                        BayesXmlConstants.PARENTS_FOR + " element.");
            }

            String varName = e1.getAttributeValue(BayesXmlConstants.NAME);
            Variable var = namesToVars.get(varName);

            Elements elements1 = e1.getChildElements();

            for (int j = 0; j < elements1.size(); j++) {
                Element e2 = elements1.get(j);

                if (!BayesXmlConstants.PARENT.equals(e2.getQualifiedName())) {
                    throw new IllegalArgumentException("Expecting " +
                            BayesXmlConstants.PARENT + " element.");
                }

                String parentName = e2.getAttributeValue(BayesXmlConstants.NAME);
                Variable parent = namesToVars.get(parentName);

                graph.addDirectedEdge(parent, var);
            }
        }

        BayesPm bayesPm = new BayesPm(graph);

        for (DiscreteVariable graphVariable : variables) {
            List<String> categories = graphVariable.getCategories();
            bayesPm.setCategories(graphVariable, categories);
        }

        return bayesPm;
    }


    private static BayesIm makeBayesIm(BayesPm bayesPm, Element element2) {
        if (!BayesXmlConstants.CPTS.equals(element2.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.CPTS + " element.");
        }

        MlBayesIm bayesIm = new MlBayesIm(bayesPm);

        Elements elements2 = element2.getChildElements();

        for (int nodeIndex = 0; nodeIndex < elements2.size(); nodeIndex++) {
            Element e1 = elements2.get(nodeIndex);

            if (!BayesXmlConstants.CPT.equals(e1.getQualifiedName())) {
                throw new IllegalArgumentException("Expecting " +
                        BayesXmlConstants.CPT + " element.");
            }

            String numRowsString = e1.getAttributeValue(BayesXmlConstants.NUM_ROWS);
            String numColsString = e1.getAttributeValue(BayesXmlConstants.NUM_COLS);

            int numRows = Integer.parseInt(numRowsString);
            int numCols = Integer.parseInt(numColsString);

            Elements e1Elements = e1.getChildElements();

            if (e1Elements.size() != numRows) {
                throw new IllegalArgumentException("Element cpt claimed " +
                        +numRows + " rows, but there are only "
                        + e1Elements.size() + " rows in the file.");
            }

            for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
                Element e2 = e1Elements.get(rowIndex);

                if (!BayesXmlConstants.ROW.equals(e2.getQualifiedName())) {
                    throw new IllegalArgumentException("Expecting " +
                            BayesXmlConstants.ROW + " element.");
                }

                Text rowNode = (Text) e2.getChild(0);
                String rowString = rowNode.getValue();

                StringTokenizer t = new StringTokenizer(rowString);

                for (int colIndex = 0; colIndex < numCols; colIndex++) {
                    String token = t.nextToken();

                    try {
                        double value = Double.parseDouble(token);
                        bayesIm.setProbability(nodeIndex, rowIndex, colIndex, value);
                    } catch (NumberFormatException e) {
                        // Skip.
                    }
                }

                if (t.hasMoreTokens()) {
                    throw new IllegalArgumentException("Element cpt claimed " +
                            numCols + " columnns , but there are more that that " +
                            "in the file.");
                }
            }
        }

        return bayesIm;
    }

    //==========================================================================
    // Sem parsing code
    //==========================================================================

    /**
     * Takes an xml representation of a SEM IM and reinstantiates the IM
     *
     * @param semImElement the xml of the IM
     * @return the SemIM
     */
    private static SemIm getSemIm(Element semImElement) {
        if (!SemXmlConstants.SEM.equals(semImElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.SEM + "' element");
        }

        Element variablesElement = semImElement.getFirstChildElement(SemXmlConstants.SEM_VARIABLES);
        Element edgesElement = semImElement.getFirstChildElement(SemXmlConstants.EDGES);
        Element marginalDistributionElement = semImElement.getFirstChildElement(SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION);
        Element jointDistributionElement = semImElement.getFirstChildElement(SemXmlConstants.JOINT_ERROR_DISTRIBUTION);


        Dag graph = ExerciseXmlParserV43.makeVariables(variablesElement);
        SemIm im = ExerciseXmlParserV43.makeEdges(edgesElement, graph);
        ExerciseXmlParserV43.setNodeMeans(variablesElement, im);
        ExerciseXmlParserV43.addMarginalErrorDistribution(marginalDistributionElement, im);
        ExerciseXmlParserV43.addJointErrorDistribution(jointDistributionElement, im);

        return im;
    }


    private static Dag makeVariables(Element variablesElement) {
        if (!SemXmlConstants.SEM_VARIABLES.equals(variablesElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.SEM_VARIABLES + "' element");
        }
        Element var;
        GraphNode node;
        Integer x, y;
        Dag semGraph = new Dag();
        Elements vars = variablesElement.getChildElements(SemXmlConstants.CONTINUOUS_VARIABLE);

        for (int i = 0; i < vars.size(); i++) {
            var = vars.get(i);
            node = new GraphNode(var.getAttributeValue(SemXmlConstants.NAME));
            if (var.getAttributeValue(SemXmlConstants.IS_LATENT).equals("yes")) {
                node.setNodeType(NodeType.LATENT);
            } else {
                node.setNodeType(NodeType.MEASURED);
            }
            x = new Integer(var.getAttributeValue(SemXmlConstants.X));
            y = new Integer(var.getAttributeValue(SemXmlConstants.Y));
            node.setCenterX(x.intValue());
            node.setCenterY(y.intValue());
            semGraph.addNode(node);
        }
        return semGraph;
    }

    private static SemIm makeEdges(Element edgesElement, Dag semGraph) {
        if (!SemXmlConstants.EDGES.equals(edgesElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.EDGES + "' element");
        }
        Element edge;
        Node causeNode, effectNode;

        Elements edges = edgesElement.getChildElements(SemXmlConstants.EDGE);

        for (int i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            causeNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.CAUSE_NODE));
            effectNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.EFFECT_NODE));
            semGraph.addDirectedEdge(causeNode, effectNode);
        }

        //SemIm semIm = SemIm.newInstance(new SemPm(semGraph));
        SemIm semIm = new SemIm(new SemPm(semGraph));
        for (int i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            causeNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.CAUSE_NODE));
            effectNode = semGraph.getNode(edge.getAttributeValue(SemXmlConstants.EFFECT_NODE));
            semIm.setParamValue(causeNode, effectNode, Double.parseDouble(edge.getAttributeValue(SemXmlConstants.VALUE)));
            semIm.getSemPm().getCoefficientParameter(causeNode, effectNode).setFixed(Boolean.valueOf(edge.getAttributeValue(SemXmlConstants.FIXED)));
        }

        return semIm;
    }

    private static void setNodeMeans(Element variablesElement, SemIm im) {
        Elements vars = variablesElement.getChildElements(SemXmlConstants.CONTINUOUS_VARIABLE);

        for (int i = 0; i < vars.size(); i++) {
            Element var = vars.get(i);
            Node node = im.getSemPm().getGraph().getNode(var.getAttributeValue(SemXmlConstants.NAME));

            if (var.getAttributeValue(SemXmlConstants.MEAN) != null) {
                im.setMean(node, Double.parseDouble(var.getAttributeValue(SemXmlConstants.MEAN)));
            } else {
                return;
            }
        }
    }

    private static void addMarginalErrorDistribution(Element marginalDistributionElement, SemIm semIm) {
        if (!SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION.equals(marginalDistributionElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION + "' element");
        }

        Element normal;
        Node node;
        Elements normals = marginalDistributionElement.getChildElements(SemXmlConstants.NORMAL);

        for (int i = 0; i < normals.size(); i++) {
            normal = normals.get(i);
            String name = normal.getAttributeValue(SemXmlConstants.VARIABLE);
            SemGraph semGraph = semIm.getSemPm().getGraph();
            semGraph.setShowErrorTerms(true);

            if (name.equals("")) {
                continue;
            }

            node = semGraph.getNode(name);

            if (node.getNodeType() == NodeType.ERROR) {
                node = semGraph.getChildren(node).get(0);
            }

            //can't set mean at this point...
            double covar = Double.parseDouble(normal.getAttributeValue(SemXmlConstants.VARIANCE));
            semIm.setErrCovar(node, covar);
        }
    }

    private static void addJointErrorDistribution(Element jointDistributionElement, SemIm semIm) {
        if (!SemXmlConstants.JOINT_ERROR_DISTRIBUTION.equals(jointDistributionElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + SemXmlConstants.JOINT_ERROR_DISTRIBUTION + "' element");
        }

        Element normal;
        Node node1, node2;
        Elements normals = jointDistributionElement.getChildElements(SemXmlConstants.NORMAL);

        for (int i = 0; i < normals.size(); i++) {
            normal = normals.get(i);
            node1 = semIm.getSemPm().getGraph().getExogenous(semIm.getSemPm().getGraph().getNode(normal.getAttributeValue(SemXmlConstants.NODE_1)));
            node2 = semIm.getSemPm().getGraph().getExogenous(semIm.getSemPm().getGraph().getNode(normal.getAttributeValue(SemXmlConstants.NODE_2)));
            semIm.setParamValue(node1, node2, Double.parseDouble(normal.getAttributeValue(SemXmlConstants.COVARIANCE)));
        }
    }
}
