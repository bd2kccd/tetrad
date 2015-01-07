package edu.cmu.causalityApp.exercise;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Text;

import java.util.*;

/**
 * Use this class to parse exercises for the 4.1 version of the lab
 *
 * @author adrian tang
 */
public class ExerciseXmlParserV42 {

    /**
     * Specifies that this parser works for the version 4.0 of the lab.
     */
    public static final String VERSION = "4.2";

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
        } catch (NullPointerException npe) {
            //do nothing
        }

        Element bayesImElement = exerciseElement.getFirstChildElement("bayesNet");
        Element semImElement = exerciseElement.getFirstChildElement(SemXmlConstants.SEM);
        Element commandsElement = exerciseElement.getFirstChildElement("commands");
        Element essayQuestions = exerciseElement.getFirstChildElement("essayQuestions");
        Element essayAnswers = exerciseElement.getFirstChildElement("essayAnswers");
        Element instructorFeedback = exerciseElement.getFirstChildElement("instructorFeedback");
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
            BayesIm bayesIm = getBayesIm(bayesImElement);

            exercise = new Exercise(exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue(),
                    getEssayQuestions(essayQuestions),
                    getEssayAnswers(essayAnswers),
                    getInstructorFeedback(instructorFeedback),
                    null,
                    bayesIm,
                    total_resource,
                    resourceObs,
                    resourceInt,
                    intervenableElement,
                    ExerciseXmlParserV42.getWindowStatus(correctGraph),
                    ExerciseXmlParserV42.getWindowStatus(correctManipGraph),
                    ExerciseXmlParserV42.getWindowStatus(population),
                    ExerciseXmlParserV42.getWindowStatus(experiment),
                    ExerciseXmlParserV42.getWindowStatus(sample),
                    ExerciseXmlParserV42.getWindowStatus(hypGraph),
                    ExerciseXmlParserV42.getWindowStatus(hypManipGraph),
                    ExerciseXmlParserV42.getWindowStatus(indep),
                    ExerciseXmlParserV42.str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()),
                    commandsElement);
        } else {
            SemIm semIm = getSemIm(semImElement);

            exercise = new Exercise(exerciseElement.getAttribute(Exercise.INSTRUCTIONS).getValue(),
                    getEssayQuestions(essayQuestions),
                    getEssayAnswers(essayAnswers),
                    getInstructorFeedback(instructorFeedback),
                    null,
                    semIm,
                    total_resource,
                    resourceObs,
                    resourceInt,
                    intervenableElement,
                    ExerciseXmlParserV42.getWindowStatus(correctGraph),
                    ExerciseXmlParserV42.getWindowStatus(correctManipGraph),
                    ExerciseXmlParserV42.getWindowStatus(population),
                    ExerciseXmlParserV42.getWindowStatus(experiment),
                    ExerciseXmlParserV42.getWindowStatus(sample),
                    ExerciseXmlParserV42.getWindowStatus(hypGraph),
                    ExerciseXmlParserV42.getWindowStatus(hypManipGraph),
                    ExerciseXmlParserV42.getWindowStatus(indep),
                    ExerciseXmlParserV42.str2Boolean(exerciseElement.getAttribute(Exercise.INCLUDE_STUDENT_GUESS).getValue()),
                    commandsElement);
        }

        exercise.setId(exerciseElement.getAttribute(Exercise.ID).getValue());
        //exercise.setTitle(exerciseElement.getAttribute(Exercise.TITLE).getValue());

        //for some reason, some of the exercises don't have title in right place!!!
        try {
            exercise.setTitle(exerciseElement.getAttribute(Exercise.TITLE).getValue());
        } catch (NullPointerException npe) {
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

    //=========================================================================
    // parse moves history
    //=========================================================================

    //////////////////////////////////////
    // private XML constants
    private final static String EXPSETUP = "expSetup";
    private final static String NAME = "name";
    private final static String IGNORED = "ignored";
    private final static String EXPVARIABLE = "expVariable";
    private final static String MANIPULATION = "manipulation";
    private final static String LOCKEDAT = "lockedAt";

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

    //////////////////////////////////////////////////////
    // Private parsing commands for this version of parser

    /**
     * Convert xml to an experimental setup.
     *
     * @param expElement the xml.
     * @return an experimental setup.
     */
    public static ExperimentalSetup parseStudiedVariables(Element expElement) {
        if (!EXPSETUP.equals(expElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + expElement + "' element");
        }
        int i;
        Element var;
        String manip, varName, ignored;
        String name = expElement.getAttributeValue(NAME);
        Elements vars = expElement.getChildElements(EXPVARIABLE);
        String[] varNames = new String[vars.size()];

        for (i = 0; i < vars.size(); i++) {
            var = vars.get(i);
            varNames[i] = var.getAttributeValue(NAME);
        }

        ExperimentalSetup es = new ExperimentalSetup(name, varNames);

        for (i = 0; i < vars.size(); i++) {
            var = vars.get(i);
            varName = var.getAttributeValue(NAME);
            ignored = var.getAttributeValue(IGNORED);
            if (ignored.equals("yes")) {
                es.getVariable(varName).setStudied(false);
            }
            manip = var.getAttributeValue(MANIPULATION);
            if (ManipulationType.NONE.toString().equals(manip)) {
                //don't have to do anything
            } else if (ManipulationType.RANDOMIZED.toString().equals(manip)) {
                es.getVariable(varName).setRandomized();
            } else if (ManipulationType.LOCKED.toString().equals(manip)) {
                String value = var.getAttributeValue(LOCKEDAT);
                es.getVariable(varName).setLocked(value);
            }
        }
        return es;
    }

    /**
     * Parses an xml representation of a hypothetical graph and renders it into
     * a hypothetical graph.
     *
     * @return the corresponding hypothetical graph.
     */
    public static HypotheticalGraph parseHypotheticalGraphElement(Element hgElement) {
        if (!HYPOTHETICALGRAPH.equals(hgElement.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting '" + HYPOTHETICALGRAPH + "' element");
        }
        int i;
        GraphNode node;
        String type;
        String name = hgElement.getAttributeValue(NAME);
        Dag dag = new Dag();
        List<GraphNode> latents = new ArrayList<GraphNode>();

        //add all the variables
        Elements variables = hgElement.getFirstChildElement(VARIABLES).getChildElements(VARIABLE);
        Element variableE;
        for (i = 0; i < variables.size(); i++) {
            variableE = variables.get(i);
            node = new GraphNode(variableE.getAttributeValue(NAME));
            node.setCenter(
                    Integer.parseInt(variableE.getAttributeValue(CENTERX)),
                    Integer.parseInt(variableE.getAttributeValue(CENTERY)));
            type = variableE.getAttributeValue(TYPE);
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
        Elements edges = hgElement.getFirstChildElement(EDGES).getChildElements(EDGE);
        Element edge;
        for (i = 0; i < edges.size(); i++) {
            edge = edges.get(i);
            Node fromNode = hg.getNode(edge.getAttributeValue(FROM));
            Node toNode = hg.getNode(edge.getAttributeValue(TO));
            hg.addDirectedEdge(fromNode, toNode);
        }

        return hg;
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

        List<DiscreteVariable> variables = getVariables(namesToVars, element0);
        BayesPm bayesPm = makeBayesPm(namesToVars, variables, element1);

        return makeBayesIm(bayesPm, element2);
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
            //String[] categories = new String[numCategories];
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
            //String rowSumTolerance = e1.getAttributeValue("rowSumTolerance");

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


        Dag graph = makeVariables(variablesElement);
        SemIm im = makeEdges(edgesElement, graph);
        im.getSemPm().getGraph().setShowErrorTerms(true);
        setNodeMeans(variablesElement, im);
        addMarginalErrorDistribution(marginalDistributionElement, im);
        addJointErrorDistribution(jointDistributionElement, im);

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

            node = semIm.getSemPm().getGraph().getExogenous(semIm.getSemPm().getGraph().getNode(normal.getAttributeValue(SemXmlConstants.VARIABLE)));
            //can't set mean at this point...
            semIm.setParamValue(node, node, Double.parseDouble(normal.getAttributeValue(SemXmlConstants.VARIANCE)));
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
