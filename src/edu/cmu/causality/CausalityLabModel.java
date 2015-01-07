package edu.cmu.causality;

import edu.cmu.causality.event.ExperimentChangedEvent;
import edu.cmu.causality.event.HypothesisChangedEvent;
import edu.cmu.causality.event.ModelChangeListener;
import edu.cmu.causality.event.SampleChangedEvent;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.ExperimentalSetupVariable;
import edu.cmu.causality.experimentalSetup.manipulation.Manipulation;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.finances.FinanceTransaction;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causality.independencies.GuessedIndependencies;
import edu.cmu.causality.manipulatedGraph.AbstractManipulatedGraph;
import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causality.manipulatedGraph.GuessedManipulatedGraph;
import edu.cmu.causality.manipulatedGraph.ManipulatedGraph;
import edu.cmu.causality.sample.BayesSample;
import edu.cmu.causality.sample.Sample;
import edu.cmu.causality.sample.SemSample;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import javax.swing.*;
import java.util.*;

/**
 * This is the main model for the lab using a singleton pattern. It interfaces
 * with the rest of the model classes to provide data needed for the gui
 * classes.
 */
public class CausalityLabModel {

    /**
     * The current model. Each time an initialization method is called, this is updated.
     */
    private static CausalityLabModel model = null;

    /**
     * Returns the model instance.
     */
    public static CausalityLabModel getModel() {
        if (model == null) {
            throw new NullPointerException("Not yet initialized.");
        }

        return model;
    }

    /**
     * Creates a singleton CausalityLabModel for the given BayesIm. The singleton instance is accessed
     * through getModel().
     *
     * @param modelIm      a BayesIm that cannot be null.
     * @param instructions May be null.
     */
    public static void initialize(BayesIm modelIm, String instructions) {
        model = new CausalityLabModel(modelIm, instructions);
    }

    /**
     * Creates a singleton CausalityLabModel for the given SemIm. The singleton instance is accessed
     * through getModel().
     *
     * @param modelIm      a SemIm that cannot be null.
     * @param instructions May be null.
     */
    public static void initialize(SemIm modelIm, String instructions) {
        model = new CausalityLabModel(modelIm, instructions);
    }

    /**
     * The lab allows two types of models, discrete Bayes nets and linear Gaussian Structural Equation Models (SEMs).
     */
    public static enum ModelType {
        BAYES, SEM
    }

    /**
     * The model type for this particular CausalityLabModel.
     */
    private ModelType modelType;

    /**
     * The Bayes IM for this model, if the type is BAYES.
     */
    private BayesIm correctBayesIm = null;

    /**
     * The SEM IM for this model, if the type is SEM.
     */
    private SemIm correctSemIm = null;

    /**
     * Keep track of whether this exercise has been submitted at all this instance.
     */
    private boolean isExerciseSubmitted = false;

    /**
     * The ID of the next data sample drawn, just to enumerate them.
     */
    private int nextSampleId = 0;

    /**
     * A list of samples, regenerable by their random seeds.
     */
    private List<SampleSeed> samples;

    /**
     * The users gets to guess hypothetical graphs until they hit on the correct graph. This is the
     * number of such guesses.
     */
    private int numberOfGuesses = 0;

    /**
     * This is the last hypothetical graph guessed.
     */
    private Graph lastGraphGuessed = null;

    /**
     * The instructions that the instructor has typed in.
     */
    private String instructions;

    /**
     * The experimental setups. There may be zero or more of these for the model.
     */
    private List<ExperimentalSetup> experiments;

    /**
     * The hypothetical graphs. There may be zero or more.
     */
    private List<HypotheticalGraph> hypotheticalGraphs;

    /**
     * True if student guesses for independencies are enabled. Used in the IndependenciesTableModel.
     */
    private boolean isStudentGuessesEnabled;

    /**
     * A sequence of guessed independencies from the user. Used in IndependenciesTableModel.
     */
    private List<GuessedIndependenciesHolder> guessedIndependencies;

    /**
     * Which variables can be intervened upon.
     */
    private Map<String, Boolean> variablesIntervenable;

    /**
     * Fields to implement dollar resources allowed for the user. Current dollar balance.
     */
    private Integer currentBalance;

    /**
     * Initial balance allotted.
     */
    private Integer totalInitialBalance;

    /**
     * Dollar cost per sample unit if everything is passively observed.
     */
    private Integer costPerObs;

    /**
     * Dollar cost per sample unit if some variable is intervened upon.
     */
    private Integer costPerIntervention;

    /**
     * A sequential list of financial transactions.
     */
    private List<FinanceTransaction> moneyTransactions;

    /**
     * These must help with communication among the model pieces.
     */
    private Vector<ModelChangeListener> modelChangeListeners;

    /**
     * Constructs a model for the given BayesIm.
     *
     * @param modelIm      a BayesIm which cannot be null.
     * @param instructions May be null.
     */
    public CausalityLabModel(BayesIm modelIm, String instructions) {
        if (modelIm == null) {
            throw new NullPointerException("Null model.");
        }

        initialize(instructions);
        setModelType(ModelType.BAYES);
        this.correctBayesIm = modelIm;
    }

    /**
     * Constructs a model for the given SemIm.
     *
     * @param modelIm      a SemIm which cannot be null.
     * @param instructions May be null.
     */
    public CausalityLabModel(SemIm modelIm, String instructions) {
        if (modelIm == null) {
            throw new NullPointerException("Null model.");
        }

        initialize(instructions);
        setModelType(ModelType.SEM);

        // if there is already the first true model built:
        SemGraph semGraph = modelIm.getSemPm().getGraph();

        // These were previously set to true.
        semGraph.setShowErrorTerms(false);
        this.correctSemIm = modelIm;
    }

    /**
     * Initializes all of the basic fields.
     *
     * @param instructions May be null.
     */
    private void initialize(String instructions) {
        setModelChangeListeners(new Vector<ModelChangeListener>());
        setInstructions(instructions);
        setExperiments(new ArrayList<ExperimentalSetup>());
        setHypotheticalGraphs(new ArrayList<HypotheticalGraph>());
        setSamples(new ArrayList<SampleSeed>());
        setGuessedIndependencies(new ArrayList<GuessedIndependenciesHolder>());
        setMoneyTransactions(new ArrayList<FinanceTransaction>());
    }

    /**
     * Initialize the resources status with the given initial balance, cost of
     * collecting a sample in an observational and intervened experiment.
     */
    public void initializeResourcesStatus(Integer totalInitialBalance,
                                          Integer costPerObs, Integer costPerInt) {
        setTotalInitialBalance(totalInitialBalance);
        setCostPerObs(costPerObs);
        setCostPerIntervention(costPerInt);
        setCurrentBalance(getTotalInitialBalance());
    }

    /**
     * @return the type of graph (BAYES or SEM) that this model deals with.
     */
    public ModelType getModelType() {
        return modelType;
    }

    // ***********************************************************************
    // Test student answer methods
    // ***********************************************************************

    /**
     * @return the number of student guesses made so far.
     */
    public int getNumberOfGuesses() {
        return numberOfGuesses;
    }

    /**
     * Checks if a given hypothetical graph is the same as the correct graph.
     *
     * @param hypotheticalGraphName the name of the given hypothetical graph.
     * @return true if so.
     */
    public boolean isHypotheticalGraphSameAsCorrectGraph(String hypotheticalGraphName) {
        HypotheticalGraph hyp = getHypotheticalGraph(hypotheticalGraphName);
        boolean isCorrect = isGraphsIdentical(getCorrectGraph(), hyp);

        if (getNumberOfGuesses() > 0) {
            if (isGraphsIdentical(getLastGraphGuessed(), hyp)) {
                return isCorrect;
            }
        }

        setLastGraphGuessed(new Dag(hyp));
        setNumberOfGuesses(getNumberOfGuesses() + 1);

        return isCorrect;
    }

    /**
     * Checks to see if two graphs have the same edges (ignoring latents and
     * their edges).
     */
    private boolean isGraphsIdentical(Graph answerGraph, Graph guessedGraph) {
        Node var, var1, var2;
        String varName;
        int i;
        Iterator<Node> variables;

        // make sure the hypothetical graph has same variables as correct graph
        for (variables = answerGraph.getNodes().iterator(); variables.hasNext(); ) {
            var = variables.next();
            if (var.getNodeType() == NodeType.MEASURED) {
                varName = var.getName();
                if (guessedGraph.getNode(varName) == null) {
                    return false;
                }
            }
        }

        // make sure has same number of latents
        int numLatentsInHypGraph = 0, numLatentsInCorrectGraph = 0;

        for (variables = answerGraph.getNodes().iterator(); variables.hasNext(); ) {
            var = variables.next();
            if (var.getNodeType() == NodeType.LATENT) {
                numLatentsInCorrectGraph++;
            }
        }
        for (variables = guessedGraph.getNodes().iterator(); variables
                .hasNext(); ) {
            var = variables.next();
            if (var.getNodeType() == NodeType.LATENT) {
                numLatentsInHypGraph++;
            }
        }

        if (numLatentsInHypGraph != numLatentsInCorrectGraph) {
            return false;

        } else { // SEE IF LATENTS MATCH
            String[] correctOrdering = new String[numLatentsInCorrectGraph];
            for (i = 0, variables = answerGraph.getNodes().iterator(); variables
                    .hasNext(); ) {
                var = variables.next();
                if (var.getNodeType() == NodeType.LATENT) {
                    correctOrdering[i] = var.getName();
                    i++;
                }
            }

            // create the possible guessed orderings
            String[][] orderings = createOrderingsOfLatents(guessedGraph);

            // for each orderings, test if it works
            int falseCount = 0;
            for (i = 0; i < factorial(numLatentsInHypGraph); i++) {
                // does this ordering work?
                if (!isLatentOrderingCorrect(orderings[i], guessedGraph,
                        correctOrdering, answerGraph)) {
                    falseCount++;
                }
            }
            if (falseCount == orderings.length) {
                return false;
            }
        }

        // make sure the hypothetical graph has all the (non-latent) correct
        // graph edges
        for (Edge edge1 : answerGraph.getEdges()) {
            var1 = edge1.getNode1();
            var2 = edge1.getNode2();
            if ((var1.getNodeType() == NodeType.MEASURED)
                    && (var2.getNodeType() == NodeType.MEASURED)) {
                if (!guessedGraph.isDirectedFromTo(guessedGraph.getNode(var1
                        .getName()), guessedGraph.getNode(var2.getName()))) {
                    return false;
                }
            }

        }

        // make sure that the correct graph has all the (non-latent)
        // hypothetical graph edges
        for (Edge edge : guessedGraph.getEdges()) {
            var1 = edge.getNode1();
            var2 = edge.getNode2();
            if ((var1.getNodeType() == NodeType.MEASURED)
                    && (var2.getNodeType() == NodeType.MEASURED)) {
                if (!answerGraph.isDirectedFromTo(answerGraph.getNode(var1
                        .getName()), answerGraph.getNode(var2.getName()))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isLatentOrderingCorrect(String[] guessedOrdering,
                                            Graph guessedGraph, String[] answerOrdering, Graph answerGraph) {
        List<Edge> guessedEdges;
        List<Edge> answerEdges;
        int i;
        Node guessedLatent, answerLatent;

        if (guessedOrdering.length != answerOrdering.length) {
            return false;
        }

        for (i = 0; i < guessedOrdering.length; i++) {
            System.out.println(guessedOrdering[i] + " " + answerOrdering[i]);
        }

        for (i = 0; i < guessedOrdering.length; i++) {
            guessedLatent = guessedGraph.getNode(guessedOrdering[i]);
            answerLatent = answerGraph.getNode(answerOrdering[i]);

            guessedEdges = guessedGraph.getEdges(guessedLatent);
            answerEdges = answerGraph.getEdges(answerLatent);

            if (guessedEdges.size() != answerEdges.size()) {
                return false;
            }

            if (!sameEdgeWithLatent(guessedLatent, guessedEdges, answerLatent,
                    answerEdges)) {
                return false;
            }
        }
        return true;
    }

    private boolean sameEdgeWithLatent(Node guessedLatent, List<Edge> guessedEdges,
                                       Node answerLatent, List<Edge> answerEdges) {
        return allEdgesWithNodeContained(guessedLatent, guessedEdges, answerEdges)
                && allEdgesWithNodeContained(answerLatent, answerEdges, guessedEdges);
    }

    private boolean allEdgesWithNodeContained(Node latent, List<Edge> guessedEdges,
                                              List<Edge> answerEdges) {
        Edge edge, answerEdge;
        Node notLatent, answerNotLatent;
        boolean ok = false;

        for (Edge guessedEdge : guessedEdges) {
            edge = guessedEdge;
            if (edge.getNode1() == latent) {// incoming edge
                notLatent = edge.getNode2();
                for (Object answerEdge1 : answerEdges) {
                    answerEdge = (Edge) answerEdge1;
                    answerNotLatent = answerEdge.getNode2();
                    if (notLatent.getName().equals(answerNotLatent.getName())) {
                        ok = true;
                    }
                }
                if (!ok) {
                    return false;
                }
                ok = false;
            } else { // outgoing edge
                notLatent = edge.getNode1();
                for (Object answerEdge1 : answerEdges) {
                    answerEdge = (Edge) answerEdge1;
                    answerNotLatent = answerEdge.getNode1();
                    if (notLatent.getName().equals(answerNotLatent.getName())) {
                        ok = true;
                    }
                }
                if (!ok) {
                    return false;
                }
                ok = false;
            }
        }
        return true;
    }

    private String[][] createOrderingsOfLatents(Graph guessedGraph) {
        String[] latents;
        int numLatentsInHypGraph = 0, i;
        Iterator<Node> variables;
        Node var;

        for (variables = guessedGraph.getNodes().iterator(); variables
                .hasNext(); ) {
            var = variables.next();
            if (var.getNodeType() == NodeType.LATENT) {
                numLatentsInHypGraph++;
            }
        }

        latents = new String[numLatentsInHypGraph];

        for (i = 0, variables = guessedGraph.getNodes().iterator(); variables
                .hasNext(); ) {
            var = variables.next();
            if (var.getNodeType() == NodeType.LATENT) {
                latents[i] = var.getName();
                i++;
            }
        }

        return createOrderings(latents);
    }

    private String[][] createOrderings(String[] args) {
        String[][] orderings;
        String[][] orderingsLessOne;
        String var;
        String argsLessOne[];
        int i, j, k;
        int ithOrdering;

        // base case
        if (args.length == 1) {
            orderings = new String[1][1];
            orderings[0][0] = args[0];
            return orderings;
        }

        // recursive case
        orderings = new String[factorial(args.length)][args.length];
        ithOrdering = 0;
        for (i = 0; i < args.length; i++) {
            var = args[i];
            argsLessOne = new String[args.length - 1];
            for (j = 0, k = 0; k < args.length; k++) {
                if (k != i) {
                    argsLessOne[j] = args[k];
                    j++;
                }
            }
            orderingsLessOne = createOrderings(argsLessOne);
            for (String[] anOrderingsLessOne : orderingsLessOne) {
                orderings[ithOrdering] = concat(var, anOrderingsLessOne);
                ithOrdering++;
            }
        }
        return orderings;
    }

    private String[] concat(String var, String[] otherVars) {
        String[] order = new String[otherVars.length + 1];
        order[0] = var;
        System.arraycopy(otherVars, 0, order, 1, otherVars.length + 1 - 1);
        return order;
    }

    private int factorial(int arg) {
        if (arg <= 1) {
            return 1;
        } else {
            return arg * factorial(arg - 1);
        }
    }

    /**
     * @return whether or not an exercise was submitted at all.
     */
    public boolean isExerciseSubmitted() {
        return isExerciseSubmitted;
    }

    /**
     * Flag this exercise as being submitted at least once.
     */
    public void setExerciseSubmitted(boolean exerciseSubmitted) {
        isExerciseSubmitted = exerciseSubmitted;
    }

    // ***********************************************************************
    // EVENT METHODS
    // ***********************************************************************

    /**
     * GUI classes must add themselves to the listener list via this method in
     * order to receive events.
     */
    public void addModelChangeListener(ModelChangeListener listener) {
        getModelChangeListeners().add(listener);
    }

    private void hypothesisChanged() {
        HypothesisChangedEvent event = new HypothesisChangedEvent(this);
        ModelChangeListener listener;

        for (Object modelChangeListener : getModelChangeListeners()) {
            listener = (ModelChangeListener) modelChangeListener;
            listener.hypothesisChanged(event);
        }
    }

    private void experimentChanged(String experimentName) {
        ExperimentChangedEvent event = new ExperimentChangedEvent(this
        );
        ModelChangeListener listener;

        for (ModelChangeListener modelChangeListener : getModelChangeListeners()) {
            listener = modelChangeListener;
            listener.experimentChanged(event);
        }
    }

    private void sampleChanged() {
        SampleChangedEvent event = new SampleChangedEvent(this);
        ModelChangeListener listener;

        for (ModelChangeListener modelChangeListener : getModelChangeListeners()) {
            listener = modelChangeListener;
            listener.sampleChanged(event);
        }
    }

    private void financeChanged() {
        ModelChangeListener listener;

        for (Object modelChangeListener : getModelChangeListeners()) {
            listener = (ModelChangeListener) modelChangeListener;
            listener.financeChanged();
        }
    }

    /**
     * Gets the list of possible parameters for a given variable in the correct
     * graph.
     *
     * @param variableName the variable whose parameter you want.
     * @return a list of strings of parameters of the variable.
     */
    public List<String> getVariableParameters(String variableName)
            throws IllegalArgumentException {
        if (!getModelType().equals(ModelType.BAYES)) {
            throw new IllegalArgumentException("Not use a bayes model");
        }
        Node var = getCorrectBayesIm().getBayesPm().getNode(variableName);
        if (var == null) {
            throw new IllegalArgumentException("I don't recognize variable "
                    + variableName);
        }
        List<String> varNames = new ArrayList<String>();
        for (int i = 0; i < getCorrectBayesIm().getBayesPm()
                .getNumCategories(var); i++) {
            varNames.add(getCorrectBayesIm().getBayesPm().getCategory(
                    var, i));
        }
        return varNames;
    }

    // ***********************************************************************
    // Correct GRAPH METHODS
    // ***********************************************************************

    /**
     * Gets the names of the variables in the correct graph.
     *
     * @return a List of Strings of variable names.
     */
    public List<String> getCorrectGraphVariableNames() {
        return getVariableNamesFromGraph(getCorrectGraph());
    }

    public Graph getCorrectGraph() {
        if (getModelType().equals(ModelType.BAYES)) {
            return getCorrectBayesIm().getDag();
        } else {
            SemGraph graph = getCorrectSemIm().getSemPm().getGraph();
            graph.setShowErrorTerms(false);
            return new EdgeListGraph(graph);
        }
    }

    /**
     * Gets all the edges of the correct graph.
     *
     * @return a List of EdgeInfo objects.
     */
    public List<EdgeInfo> getCorrectGraphEdges() {
        if (getCorrectGraph() != null)
            return convertEdgesToEdgeInfo(getCorrectGraph()
                    .getEdges().iterator());
        else
            return null;
    }

    /**
     * Tells whether the given variable in the CORRECT_GRAPH is latent or not.
     *
     * @return true if variable is latent, false otherwise.
     */
    public boolean isCorrectGraphVariableLatent(String variableName)
            throws IllegalArgumentException {
        Node node = getCorrectGraph().getNode(variableName);
        if (node == null) {
            throw new IllegalArgumentException(variableName
                    + " is not a variable in the correct graph");
        }
        return (node.getNodeType() == NodeType.LATENT);
    }

    /**
     * Helper class that converts a List of edges into a List of EdgeInfo
     * objects so that clients (GUIs) using CausalityLabModel don't
     * inadvertantly change an edge.
     *
     * @param edges a List of Edge objects.
     * @return a List of EdgeInfo objects.
     */
    private List<EdgeInfo> convertEdgesToEdgeInfo(Iterator edges) {
        List<EdgeInfo> edgeInfos = new ArrayList<EdgeInfo>();

        for (; edges.hasNext(); ) {
            edgeInfos.add(new EdgeInfo((Edge) edges.next()));
        }

        return edgeInfos;
    }

    /**
     * @return a deep clone of the correct Bayes PM graph.
     * @throws IllegalArgumentException
     */
    public BayesPm getCorrectGraphBayesPmCopy() throws IllegalArgumentException {
        if (!getModelType().equals(ModelType.BAYES)) {
            throw new IllegalArgumentException("Not use a bayes model");
        }
        return new BayesPm(getCorrectBayesIm().getBayesPm());
    }

    /**
     * @return a deep clone of the correct SEM PM graph.
     * @throws IllegalArgumentException
     */
    public SemPm getCorrectGraphSemPmCopy() throws IllegalArgumentException {
        if (!getModelType().equals(ModelType.SEM)) {
            throw new IllegalArgumentException("Not use a sem model");
        }
        return new SemPm(getCorrectSemIm().getSemPm());
    }

    public BayesIm getCorrectGraphBayesImCopy() throws IllegalArgumentException {
        if (!getModelType().equals(ModelType.BAYES)) {
            throw new IllegalArgumentException("Not using a bayes model");
        }
        return new MlBayesIm(getCorrectBayesIm());
    }

    public SemIm getCorrectGraphSemImCopy() throws IllegalArgumentException {
        if (!getModelType().equals(ModelType.SEM)) {
            throw new IllegalArgumentException("Not using a sem model");
        }
        return new SemIm(getCorrectSemIm());
    }

    // ***********************************************************************
    // EXPERIMENTAL SETUP METHODS
    // ***********************************************************************

    /**
     * Adds a new experimental setup with the given name to the model. Note, we
     * should probably make sure that all experiments have unique names!
     *
     * @param name what to call the experiment.
     */
    public void addNewExperiment(String name) {
        addNewExperiment(new ExperimentalSetup(name, getCorrectGraph()));
    }

    /**
     * Adds a new experimental setup.
     *
     * @param studiedVariables the list of studied variables.
     * @throws IllegalArgumentException
     */
    public void addNewExperiment(ExperimentalSetup studiedVariables)
            throws IllegalArgumentException {
        String name = studiedVariables.getName();
        // need to make sure it is legitimate....
        if (getExperiment(name) != null) {
            JOptionPane.showMessageDialog(null,
                    "this experiment name already exists " + name,
                    "Experiment name already exists",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException(
                    "this experiment name already exists " + name);
        }

        getExperiments().add(studiedVariables);
        experimentChanged(name);
    }

    /**
     * Removes the experiment with the given id from the model.
     *
     * @param experimentalSetupName the unique id of the experiment to remove.
     */
    public void removeExperiment(String experimentalSetupName) {
        getExperiments().remove(getExperiment(experimentalSetupName));

        cleanSamples(experimentalSetupName);

        // fire an event that the experiment has changed
        experimentChanged(experimentalSetupName);
    }

    /**
     * @return a list of all the variable names in the Correct graph that are
     *         not error terms or latent variables.
     */
    public String[] getExperimentalSetupVariableNames() {
        List<String> variables = new ArrayList<String>();
        String[] names;

        for (Node var : getCorrectGraph().getNodes()) {
            if (var.getNodeType() == NodeType.MEASURED) {
                variables.add(var.getName());
            } else if (var.getNodeType() == NodeType.LATENT) {
            } else if (var.getNodeType() == NodeType.ERROR) {
            }
        }
        names = new String[variables.size()];

        int j;
        Iterator<String> i;
        for (j = 0, i = variables.iterator(); i.hasNext(); j++) {
            names[j] = i.next();
        }

        return names;
    }

    /**
     * Returns the names of the variables in the experimental setup as a list of
     * Strings.
     *
     * @param experimentalSetupName the unique id of the experimental setup.
     * @return a List of Strings of variable names or null if invalid ID.
     */
    public String[] getExperimentalSetupVariableNames(
            String experimentalSetupName) throws IllegalArgumentException {
        ExperimentalSetup experiment = getExperiment(experimentalSetupName);
        if (experiment == null) {
            throw new IllegalArgumentException("no experiment for this id ("
                    + experimentalSetupName + ")");
        }
        return experiment.getNamesOfStudiedVariables();
    }

    /**
     * @return a list of the existing experimental setup names.
     */
    public String[] getExperimentNames() {
        String[] names = new String[getExperiments().size()];
        for (int i = 0; i < getExperiments().size(); i++) {
            names[i] = (getExperiments().get(i)).getName();
        }
        return names;
    }

    /**
     * @param exptName the experimental setup name.
     * @return the list of names of the variables studied in the given
     *         experimental setup.
     */
    public String[] getExperimentalSetupStudiedVariablesNames(String exptName) {
        return getExperiment(exptName).getNamesOfStudiedVariables();
    }

    /**
     * Gets the type of manipualtion for the given variable in the given
     * experiment.
     *
     * @return the ManipulationType or null for the variable or experiment
     *         doesn't exist
     */
    public Manipulation getExperimentalVariableManipulation(
            String experimentalSetupName, String variableName) {
        ExperimentalSetup experiment = getExperiment(experimentalSetupName);
        return experiment.getVariable(variableName).getManipulation();
    }

    /**
     * Set a given variable in the given experimental setup as being randomized,
     * and send an event to the listeners that the experimental setup has
     * changed.
     *
     * @throws IllegalArgumentException
     */
    public void setExperimentalVariableRandomized(String experimentalSetupName,
                                                  String variableName) throws IllegalArgumentException {
        ExperimentalSetup experiment = getExperiment(experimentalSetupName);
        experiment.getVariable(variableName).setRandomized();
        experimentChanged(experimentalSetupName);
    }

    /**
     * Set a given variable in the given experimental setup as being locked, and
     * send an event to the listeners that the experimental setup has changed.
     *
     * @throws IllegalArgumentException
     */
    public void setExperimentalVariableLocked(String experimentalSetupName,
                                              String variableName, String variableValue)
            throws IllegalArgumentException {
        if (!getModelType().equals(ModelType.BAYES)) {
            throw new IllegalArgumentException(
                    "not using a bayes model, cannot lock at value");
        }
        ExperimentalSetup experiment = getExperiment(experimentalSetupName);
        experiment.getVariable(variableName).setLocked(variableValue);
        experimentChanged(experimentalSetupName);
    }

    /**
     * In the experimental setup you can set a variable as not-studied which
     * removes it from consideration in the independencies.
     *
     * @param isStudied             removes the variable from consideration if true
     * @param variableName          the variable to table/untable
     * @param experimentalSetupName the experiment in which the variable is being changed
     */

    public void setExperimentalVariableStudied(String experimentalSetupName,
                                               String variableName, boolean isStudied) {
        ExperimentalSetupVariable var;
        var = getExperiment(experimentalSetupName).getVariable(variableName);
        if (var == null) {
            throw new IllegalArgumentException(variableName
                    + " is not a valid variable name");
        }
        var.setStudied(isStudied);
        experimentChanged(experimentalSetupName);
    }

    /**
     * @return if a particular variable in the given experimental setup is being
     *         studied in that experiment.
     */
    public boolean isVariableInExperimentStudied(String experimentalSetupName,
                                                 String variableName) {
        return getExperiment(experimentalSetupName).getVariable(variableName)
                .isStudied();
    }

    /**
     * @param experimentName the name of the experimental setup to copy.
     * @return a copy of an experimental setup with the given name.
     */
    public ExperimentalSetup getExperimentalSetupCopy(String experimentName) {
        return new ExperimentalSetup(getExperiment(experimentName));
    }

    /**
     * damon: March 21st 07 this method is modified from the above method, to
     * get the original ExperimentalSetup variable. This function is created for
     * showing the standard dev and mean in the ExperimentalSetupEditor. I tried
     * to use the getExperimentalSetupCopy but for some reason, the mean and std
     * dev is not copied properly over.
     *
     * @param experimentName the name of the original experimental setup
     * @return the real experimental setup with the given name.
     */
    public ExperimentalSetup getRealExperimentalSetup(String experimentName) {
        return getExperiment(experimentName);
    }

    /**
     * @param experimentName name of this experimental setup.
     * @return an empty experimental setup with a given name.
     */
    public ExperimentalSetup getEmptyExperimentalSetup(String experimentName) {
        return new ExperimentalSetup(experimentName, getCorrectGraph());
    }

    /**
     * Helper method gets the experimental setup
     *
     * @param name the unique name of the experiment
     * @return the qualitative experimental setup
     */
    public ExperimentalSetup getExperiment(String name) {
        ExperimentalSetup es;
        for (Object EXPERIMENT : getExperiments()) {
            es = ((ExperimentalSetup) EXPERIMENT);
            if (es.getName().equals(name)) {
                return es;
            }
        }
        return null;
    }

    /**
     * Checks if an experimental setup with the given name exists or not.
     *
     * @return true if so.
     */
    public boolean isValidExperimentName(String name) {
        return (getExperiment(name) != null);
    }

    /**
     * In the experimental setup, some variables can be intervened uqqpon (like
     * salary) and some cannot (like sex). This method sets the variable
     * intervenable statuses for all the variables in the experimental setup.
     */
    public void setVariableIntervenableStatuses(
            Map<String, Boolean> variablesIntervenable) {
        setVariablesIntervenable(variablesIntervenable);
    }

    /**
     * @return if a variable of a given name can be intervened or not in an
     *         experimental setup.
     */
    public boolean getVariableIntervenable(String varName) {
        return getVariablesIntervenable().get(varName) == null ? true
                : getVariablesIntervenable().get(varName);
    }

    // ***********************************************************************
    // HYPOTHETICAL GRAPH METHODS
    // ***********************************************************************

    /**
     * Adds a latent variable to the given hypothetical graph (or does nothing
     * if an invalid hypotheticalGraphID).
     *
     * @param latentName            the name of the new latent variable.
     * @param hypotheticalGraphName the unique id of the hypothetical graph.
     */
    public void addLatentVariableToHypotheticalGraph(String latentName,
                                                     String hypotheticalGraphName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            return;
        }
        graph.addLatentVariable(latentName);
        hypothesisChanged();
    }

    /**
     * Adds a new hypothetical graph with the given name.
     *
     * @param name name of the new hypothetical graph.
     */
    public void addNewHypotheticalGraph(String name) {
        addNewHypotheticalGraph(getEmptyHypotheticalGraph(name));
    }

    /**
     * Adds the new given hypothetical graph.
     */
    public void addNewHypotheticalGraph(HypotheticalGraph hypotheticalGraph) {
        // make sure name is unique
        if (getHypotheticalGraph(hypotheticalGraph.getName()) != null) {
            throw new IllegalArgumentException(
                    "already a hypothesis with this name: "
                            + hypotheticalGraph.getName());
        }
        getHypotheticalGraphs().add(hypotheticalGraph);

        hypothesisChanged();
    }

    /**
     * Adds a directed edge to a given hypothetical graph.
     *
     * @param fromVariableName      the name of the variable the edge starts from.
     * @param toVariableName        the name of the variable the edge ends at.
     * @param hypotheticalGraphName the unique ID of the hypoehtical graph.
     * @return true on success, false otherwise.
     */
    public boolean addHypotheticalGraphEdge(String hypotheticalGraphName,
                                            String fromVariableName, String toVariableName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            return false;
        }
        Node node1 = graph.getNode(fromVariableName);
        Node node2 = graph.getNode(toVariableName);
        if (node1 == null) {
            return false;
        }
        if (node2 == null) {
            return false;
        }

        hypothesisChanged();
        graph.addDirectedEdge(node1, node2);
        return true;
    }

    /**
     * This can be used by the Hypothetical graph editor to hold data while
     * creating a new hypothesis graph.
     */
    public HypotheticalGraph getEmptyHypotheticalGraph(String name) {
        HypotheticalGraph hg = new HypotheticalGraph(name, getCorrectGraph(), false, false);
        hg.removeLatenVariables();
        return hg;
    }

    /**
     * @return a copy of the hypothetical graph with the given name.
     */
    public HypotheticalGraph getHypotheticalGraphCopy(
            String hypotheticalGraphName) {
        HypotheticalGraph hg = getHypotheticalGraph(hypotheticalGraphName);
        return hg.copyGraph(true, false);
    }

    /**
     * Gets a requested hypothetical graph name from the model
     */
    public String getHypotheticalGraphName(String hypotheticalGraphName) {
        HypotheticalGraph hg = getHypotheticalGraph(hypotheticalGraphName);
        if (hg == null) {
            return null;
        }
        return hg.getName();
    }

    /**
     * Gets the unique IDs of all the hypothetical graphs
     *
     * @return a set of Integers?
     */
    public String[] getHypotheticalGraphNames() {
        Iterator<HypotheticalGraph> i;
        int index;
        String names[] = new String[getHypotheticalGraphs().size()];
        for (index = 0, i = getHypotheticalGraphs().iterator(); i.hasNext(); index++) {
            names[index] = (i.next()).getName();
        }
        return names;
    }

    /**
     * Gets the names of the variables in the given hypothetical graph, returns
     * null if invalid id.
     *
     * @param hypotheticalGraphName the unique ID of the hypothetical graph
     * @return a List of Strings of variable names, or null if invalid ID
     */
    public List<String> getHypotheticalGraphVariableNames(String hypotheticalGraphName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        return getVariableNamesFromGraph(graph);
    }

    /**
     * Returns all the edges in a given hypothetical graph
     *
     * @param hypotheticalGraphName the unique ID of the hypothetical graph
     * @return a List of EdgeInfo objects of edges in the hypothetical graph, or
     *         null if invalid ID
     */
    public List<EdgeInfo> getHypotheticalGraphEdges(String hypotheticalGraphName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            return null;
        }
        return convertEdgesToEdgeInfo(graph.getEdges().iterator());
    }

    /**
     * Tells whether there is a directed edge between the two nodes in the given
     * graph
     *
     * @param fromVariableName      the name of the first node
     * @param toVariableName        the name of the second node
     * @param hypotheticalGraphName the graph to look for the edge in
     * @return true if there is an edge between the two nodes
     */
    public boolean doesEdgeExist(String fromVariableName,
                                 String toVariableName, String hypotheticalGraphName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            return false;
        }
        Node fromNode = graph.getNode(fromVariableName);
        Node toNode = graph.getNode(toVariableName);

        return graph.existsDirectedPathFromTo(fromNode, toNode);
    }

    /**
     * @return the number of existing hypothetical graphs.
     */
    public int getNumHypotheticalGraphs() {
        return getHypotheticalGraphs().size();
    }

    /**
     * Checks if a hypothetical graph with a given name exists.
     *
     * @return true if so.
     */
    public boolean isValidHypotheticalGraphName(String name) {
        return (getHypotheticalGraph(name) != null);
    }

    /**
     * Tells whether the given variable in the hypothetical Graph is latent or
     * not
     *
     * @return true if the variable is latent
     */
    public boolean isHypotheticalGraphVariableLatent(
            String hypotheticalGraphName, String variableName) {
        Node node;
        HypotheticalGraph graph;
        graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            throw new IllegalArgumentException(hypotheticalGraphName
                    + " is not a valid hypothetical graph id");
        }
        node = graph.getNode(variableName);
        if (node == null) {
            throw new IllegalArgumentException(variableName
                    + " is not a variable in the correct graph");
        }
        return (node.getNodeType() == NodeType.LATENT);

    }

    /**
     * Removes a hypothetical graph from the list of hypothetical graphs. If the
     * id is invalid, does nothing
     *
     * @param hypotheticalGraphName the unique id of the hypothetical graph
     */
    public void removeHypotheticalGraph(String hypotheticalGraphName) {
        HypotheticalGraph hg = getHypotheticalGraph(hypotheticalGraphName);
        getHypotheticalGraphs().remove(hg);
        hypothesisChanged();
    }

    /**
     * Remove a latent variable from the given hypothetical graph (or does
     * nothing if an invalid hypotheticalGraphID
     *
     * @param latentName            he name of the new latent variable
     * @param hypotheticalGraphName the unique id of the hypothetical graph
     */
    public void removeLatentVariableFromHypotheticalGraph(String latentName,
                                                          String hypotheticalGraphName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            return;
        }
        graph.removeLatentVariable(latentName);
        hypothesisChanged();
    }

    /**
     * Removes an edge from a given hypothetical graph
     *
     * @param fromVariableName      the name of the variable the edge starts from
     * @param toVariableName        the name of the variable the edge goes to
     * @param hypotheticalGraphName the unique ID of the hypothetical graph
     * @return true on success, false on failure
     */
    public boolean removeEdgeFromHypotheticalGraph(String fromVariableName,
                                                   String toVariableName, String hypotheticalGraphName) {
        HypotheticalGraph graph = getHypotheticalGraph(hypotheticalGraphName);
        if (graph == null) {
            return false;
        }
        Node fromNode = graph.getNode(fromVariableName);
        Node toNode = graph.getNode(toVariableName);

        hypothesisChanged();
        return graph.removeEdges(fromNode, toNode);
    }

    /**
     * Edits or replaces a hypothetical graph.
     *
     * @param hypotheticalGraph the new edited hypothetical graph.
     * @param withEdges         if false, copies only the variables.
     */
    public void setHypotheticalGraph(HypotheticalGraph hypotheticalGraph,
                                     boolean withEdges) {
        // make sure id exists
        HypotheticalGraph hg = getHypotheticalGraph(hypotheticalGraph.getName());
        if (hg == null) {
            throw new IllegalArgumentException(
                    "that hypothetical graph doesn't exist : "
                            + hypotheticalGraph.getName());
        }
        getHypotheticalGraphs().remove(hg);
        HypotheticalGraph hgc = hypotheticalGraph.copyGraph(withEdges, false);
        getHypotheticalGraphs().add(hgc);
        hypothesisChanged();
    }

    /**
     * @return the x coordinate of a given variable in a hypothetical graph.
     */
    public int getHypotheticalGraphVariableCenterX(String hypName,
                                                   String varName) {
        return getHypotheticalGraph(hypName).getVariableCenterX(varName);
    }

    /**
     * @return the y coordinate of a given variable in a hypothetical graph.
     */
    public int getHypotheticalGraphVariableCenterY(String hypName,
                                                   String varName) {
        return getHypotheticalGraph(hypName).getVariableCenterY(varName);
    }

    /**
     * Sets the x and y coordinates of a given variable in a hypothetical graph.
     */
    public void setHypotheticalGraphVariableCenter(String hypName,
                                                   String varName, int x, int y) {
        getHypotheticalGraph(hypName).setVariableCenter(varName, x, y);
    }

    private HypotheticalGraph getHypotheticalGraph(String name) {
        HypotheticalGraph hg;
        for (HypotheticalGraph hypotheticalGraph : getHypotheticalGraphs()) {
            hg = (hypotheticalGraph);
            if (hg.getName().equals(name)) {
                return hg;
            }
        }
        return null;
    }

    // ***********************************************************************
    // CORRECT MANIPULATED GRAPH METHODS
    // ***********************************************************************

    /**
     * Gets the variables in the correct manipulated graph
     *
     * @param experimentalSetupName the unique id of the experiment which was used to create a
     *                              given correct manipulated graph
     * @return a List of Strings of variable names
     */
    public List<String> getCorrectManipulatedGraphVariableNamesForExperiment(
            String experimentalSetupName) {
        AbstractManipulatedGraph mGraph = getCorrectManipulatedGraph(experimentalSetupName);
        if (mGraph == null) {
            return null;
        }
        return getVariableNamesFromGraph(mGraph);
    }

    /**
     * Gets the type of manipulation for a given variable, e.g. NONE,
     * RANDOMIZED, LOCKED, DISABLED
     *
     * @param variableName          the variable
     * @param experimentalSetupName the unique id of the experiment used to create a given correct
     *                              manipulated graph
     * @return the type of variable
     */
    public ManipulationType getCorrectManipulatedGraphVariableType(
            String variableName, String experimentalSetupName) {
        AbstractManipulatedGraph mGraph = getCorrectManipulatedGraph(experimentalSetupName);
        if (mGraph == null) {
            return null;
        }
        return mGraph.getManipulationFor(variableName);
    }

    /**
     * Gets the active edges (those not broken by an experimental manipulation)
     * for a given correct manipulated graph. This will still return frozen
     * edges because <code>AbstractManipulatedGraph.freezeEdges</code> does not
     * reset the frozen edge when it adds that edge.
     *
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              correct manipulated graph.
     * @return a List of EdgeInfo objects.
     */
    public List<EdgeInfo> getCorrectManipulatedGraphActiveEdgesForExperiment(
            String experimentalSetupName) {
        AbstractManipulatedGraph mGraph = getCorrectManipulatedGraph(experimentalSetupName);
        if (mGraph == null) {
            return null;
        }
        return convertEdgesToEdgeInfo(mGraph.getEdges().iterator());
    }

    /**
     * Gets the broken edges in the correct manipulated graph.
     *
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              correct manipulated graph.
     * @return a List of EdgeInfo objects.
     */
    public EdgeInfo[] getCorrectManipulatedGraphBrokenEdges(
            String experimentalSetupName) {
        AbstractManipulatedGraph mGraph = getCorrectManipulatedGraph(experimentalSetupName);
        if (mGraph == null) {
            return null;
        }
        return mGraph.getBrokenEdges();
    }

    /**
     * Gets the frozen edges in the correct manipulated graph.
     *
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              correct manipulated graph.
     * @return a List of EdgeInfo objects.
     */
    public EdgeInfo[] getCorrectManipulatedGraphFrozenEdges(
            String experimentalSetupName) {
        AbstractManipulatedGraph mGraph = getCorrectManipulatedGraph(experimentalSetupName);
        if (mGraph == null) {
            return null;
        }
        return mGraph.getFrozenEdges();
    }

    /**
     * @param experimentalSetupName the name of the experimental setup.
     * @return the correct manipulated graph given the experimental setup.
     */
    public AbstractManipulatedGraph getCorrectManipulatedGraph(
            String experimentalSetupName) {
        ExperimentalSetup expQL = getExperiment(experimentalSetupName);
        if (expQL == null) {
            return null;
        }
        return new ManipulatedGraph(getCorrectGraph(), expQL);
    }

    // ***********************************************************************
    // HYPOTHETICAL MANIPULATED GRAPH METHODS
    // ***********************************************************************

    /**
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              hypothetical manipulated graph.
     * @param hypotheticalGraphName the unique id of the hypothetical graph to create the given
     *                              hypothetical manipulated graph.
     * @return the names of the variables in the hypothetical manipulated graph.
     */
    public List<String> getHypotheticalManipulatedGraphVariableNames(
            String experimentalSetupName, String hypotheticalGraphName) {
        AbstractManipulatedGraph hGraph = getHypotheticalManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);
        if (hGraph == null) {
            return null;
        }
        return getVariableNamesFromGraph(hGraph);
    }

    /**
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              hypothetical manipulated graph.
     * @param hypotheticalGraphName the unique id of the hypothetical graph to create the given
     *                              hypothetical manipulated graph.
     * @param var                   name of variable.
     * @return the Manipulation Type of the given variable in the hypothetical
     *         manipulated graph.
     */
    public ManipulationType getHypotheticalManipulatedGraphVariableType(
            String experimentalSetupName, String hypotheticalGraphName,
            String var) {
        AbstractManipulatedGraph hGraph = getHypotheticalManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);
        if (hGraph == null) {
            return null;
        }
        return hGraph.getManipulationFor(var);
    }

    /**
     * Gets the active edges (those not broken by an experimental manipulation)
     * for a given hypothetical manipulated graph. This will still return frozen
     * edges because <code>AbstractManipulatedGraph.freezeEdges</code> does not
     * remove the frozen edge when it adds that edge.
     *
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              hypothetical manipulated graph.
     * @param hypotheticalGraphName the unique id of the hypothetical graph to create the given
     *                              hypothetical manipulated graph.
     * @return a List of EdgeInfo objects.
     */
    public List<EdgeInfo> getHypotheticalManipulatedGraphActiveEdges(
            String experimentalSetupName, String hypotheticalGraphName) {
        AbstractManipulatedGraph hGraph = getHypotheticalManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);
        if (hGraph == null) {
            return null;
        }
        return convertEdgesToEdgeInfo(hGraph.getEdges().iterator());
    }

    /**
     * Gets the broken edges in the hypothetical manipulated graph.
     *
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              hypothetical manipulated graph.
     * @param hypotheticalGraphName the unique id of the hypothetical graph to create the given
     *                              hypothetical manipulated graph.
     * @return a List of EdgeInfo objects.
     */
    public EdgeInfo[] getHypotheticalManipulatedGraphBrokenEdges(
            String experimentalSetupName, String hypotheticalGraphName) {
        AbstractManipulatedGraph graph = getHypotheticalManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);
        if (graph == null) {
            return null;
        }
        return graph.getBrokenEdges();
    }

    /**
     * Gets the frozen edges in the hypothetical manipulated graph.
     *
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              hypothetical manipulated graph.
     * @param hypotheticalGraphName the unique id of the hypothetical graph to create the given
     *                              hypothetical manipulated graph.
     * @return a List of EdgeInfo objects.
     */
    public EdgeInfo[] getHypotheticalManipulatedGraphFrozenEdges(
            String experimentalSetupName, String hypotheticalGraphName) {
        AbstractManipulatedGraph graph = getHypotheticalManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);
        if (graph == null) {
            return null;
        }
        return graph.getFrozenEdges();
    }

    /**
     * @param experimentalSetupName the unique id of the experiment used to create the given
     *                              hypothetical manipulated graph.
     * @param hypotheticalGraphName the unique id of the hypothetical graph to create the given
     *                              hypothetical manipulated graph.
     * @return the hypothetical manipulated graph given the hyp graph and exp
     *         setup.
     */
    public AbstractManipulatedGraph getHypotheticalManipulatedGraph(
            String experimentalSetupName, String hypotheticalGraphName) {
        HypotheticalGraph hypothesis = getHypotheticalGraph(hypotheticalGraphName);
        ExperimentalSetup experiment = getExperiment(experimentalSetupName);
        if ((hypothesis == null) || (experiment == null)) {
            return null;
        }
        return new ManipulatedGraph(hypothesis, experiment);
    }

    // ***********************************************************************
    // GUESSED HYPOTHETICAL MANIPULATED GRAPH METHODS
    // ***********************************************************************

    /**
     * @return the manipulation type of a given variable in the guessed
     *         manipulated graph for a given experimental setup and hypothetical
     *         graph.
     */
    public ManipulationType getGuessedManipulatedGraphVariableManipulation(
            String experimentalSetupName, String hypotheticalGraphName,
            String variable) throws IllegalArgumentException {
        GuessedManipulatedGraph guess = getGuessedManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);

        return guess.getManipulationFor(variable);
    }

    /**
     * Set the edge between two variables as broken in the guessed manipulated
     * graph for a given experimental setup and hypothetical graph.
     *
     * @param experimentalSetupName unique id of an experimental setup.
     * @param hypotheticalGraphName unique id of a hypothetical graph.
     * @param fromVariableName      source variable.
     * @param toVariableName        destination variable
     */
    public void setGuessedManipulatedGraphEdgeBroken(
            String experimentalSetupName, String hypotheticalGraphName,
            String fromVariableName, String toVariableName)
            throws IllegalArgumentException {
        GuessedManipulatedGraph guess = getGuessedManipulatedGraph(
                experimentalSetupName, hypotheticalGraphName);
        guess.setEdgeBroken(fromVariableName, toVariableName);
    }

    /**
     * Gets GuessedManipulatedGraph for corresponding experiment and hypothesis,
     * or creates it if it doesn't exist. Also removes any guesses who's
     * corresponding experiment and hypothesis have been deleted
     *
     * @return the guessed manipulated graph needed.
     * @throws IllegalArgumentException
     */

    private GuessedManipulatedGraph getGuessedManipulatedGraph(
            String experimentalSetupName, String hypotheticalGraphName)
            throws IllegalArgumentException {
        GuessedManipulatedGraph aGraph;
        GuessedManipulatedGraphHolder guessHolder;

        if (!isValidExperimentName(experimentalSetupName)) {
            throw new IllegalArgumentException(experimentalSetupName
                    + " is not a valid experiment id");
        }
        if (!isValidHypotheticalGraphName(hypotheticalGraphName)) {
            throw new IllegalArgumentException(experimentalSetupName
                    + " is not a valid hypothetical id");
        }

        // if the guess hasn't been made, make the guessHolder and return it
        aGraph = new GuessedManipulatedGraph(
                getHypotheticalGraph(hypotheticalGraphName));
        updateGuessedManipulatedGraph(experimentalSetupName,
                hypotheticalGraphName, aGraph);
        guessHolder = new GuessedManipulatedGraphHolder(experimentalSetupName,
                hypotheticalGraphName, aGraph);
        return guessHolder.getGuess();
    }

    private void updateGuessedManipulatedGraph(String experimentalSetupName,
                                               String hypotheticalGraphName, GuessedManipulatedGraph guess) {
        String fromNodeName, toNodeName, variable;
        Edge edge;
        EdgeInfo edge2;
        ManipulationType manipulation;

        // get the hyp and experimental setup
        ExperimentalSetup experiment = this
                .getExperiment(experimentalSetupName);
        HypotheticalGraph hypGraph = this
                .getHypotheticalGraph(hypotheticalGraphName);

        // if there is an extra edge in the guess that is not in the
        // hypothetical graph remove it
        EdgeInfo[] edges = guess.getAllNonLatentEdges();
        for (EdgeInfo edge1 : edges) {
            fromNodeName = edge1.getFromNode();
            toNodeName = edge1.getToNode();
            edge = hypGraph.getEdge(hypGraph.getNode(fromNodeName), hypGraph
                    .getNode(toNodeName));
            if (edge == null) {
                guess.removeEdgeFromGuess(fromNodeName, toNodeName);
            }
        }

        // if there is an edge that is in the hypothetical graph that's not in
        // the guess, add a normal edge
        for (Object o : hypGraph.getEdges()) {
            edge = (Edge) o;
            fromNodeName = edge.getNode1().getName();
            toNodeName = edge.getNode2().getName();

            edge2 = guess.getAnyEdge(fromNodeName, toNodeName);
            if (edge2 == null) {
                guess.setEdge(fromNodeName, toNodeName);
            }
        }

        // make sure it has all the manipulations
        String[] varNames = experiment.getVariableNames();
        int i;
        for (i = 0; i < varNames.length; i++) {
            variable = varNames[i];
            manipulation = experiment.getVariable(variable).getManipulation()
                    .getType();
            if (manipulation == ManipulationType.NONE) {
                guess.setVariableNotManipulated(variable);
            } else if (manipulation == ManipulationType.LOCKED) {
                guess.setVariableLocked(variable);
            } else if (manipulation == ManipulationType.RANDOMIZED) {
                guess.setVariableRandomized(variable);
            }
        }
    }

    /**
     * Removes any guesses who's corresponding experimental setup or
     * hypothetical graph has been deleted
     */

    // ***********************************************************************
    // RESOURCES METHODS
    // ***********************************************************************

    /**
     * @return if resources for the user have been restricted.
     */
    public boolean isLimitResource() {
        // return isLimitResource;
        return getCurrentBalance() != null && getTotalInitialBalance() != null
                && getCostPerObs() != null && getCostPerIntervention() != null;
    }

    /**
     * @return the getModel balance the user has now.
     */
    public Integer getCurrentBalance() {
        return currentBalance;
    }

    /**
     * Sets the getModel balance to a given value.
     *
     * @param currentBalance new getModel balance.
     */
    void setCurrentBalance(int currentBalance) {
        this.currentBalance = currentBalance;
    }

    /**
     * @return the initial total amount of money the user has.
     */
    public Integer getTotalInitialBalance() {
        return totalInitialBalance;
    }

    /**
     * @return the cost of collecting one unit sample in an observational
     *         experimental setup.
     */
    public Integer getCostPerObs() {
        return costPerObs;
    }

    /**
     * @return the cost of collecting one unit sample in an experimental setup
     *         with interventions.
     */
    public Integer getCostPerIntervention() {
        return costPerIntervention;
    }

    /**
     * Adds a given finance transaction done (after collecting sample).
     *
     * @param expName    unique id of that experimental setup.
     * @param sampleName name of the sample collected.
     * @param sampleSize size of the sample.
     * @param expenses   total cost of collecting this sample.
     */
    public void addFinanceTransaction(String expName, String sampleName,
                                      int sampleSize, int expenses) {
        int currentBalance = getCurrentBalance() - expenses;
        FinanceTransaction transaction = new FinanceTransaction(expName,
                sampleName, sampleSize, expenses, currentBalance);
        getMoneyTransactions().add(transaction);

        setCurrentBalance(currentBalance);
        financeChanged();
    }

    /**
     * Removes an existing financial transaction.
     *
     * @param expName    unique id of that experimental setup.
     * @param sampleName name of the sample collected.
     * @param sampleSize size of the sample.
     * @param expenses   total cost of collecting this sample.
     */
    public void removeFinanceTransaction(String expName, String sampleName,
                                         int sampleSize, int expenses) {
        int newBalance = getCurrentBalance() + expenses;
        FinanceTransaction transaction = null;
        boolean found = false;

        for (FinanceTransaction moneyTransaction : getMoneyTransactions()) {
            transaction = moneyTransaction;
            if (transaction.getExpName().equals(expName)
                    && transaction.getSampleName().equals(sampleName)
                    && transaction.getSampleSize() == sampleSize
                    && transaction.getExpenses() == expenses) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new NullPointerException("No such financial transaction: ["
                    + expName + "," + sampleName + "," + sampleSize + ","
                    + expenses + "]");
        }

        getMoneyTransactions().remove(transaction);

        setCurrentBalance(newBalance);
        financeChanged();
    }

    /**
     * @return the the table model of the finance history table.
     */
    public FinanceHistoryTableModel getFinanceHistoryTableModel() {
        return new FinanceHistoryTableModel(this);
    }

    // ***********************************************************************
    // POPULATION METHODS
    // ***********************************************************************

    /**
     * @return the table model of the population table.
     */
    public PopulationTableModel getPopulationTableModel(String experimentName) {
        return new PopulationTableModel(this, experimentName);
    }

    // ***********************************************************************
    // SAMPLE METHODS
    // ***********************************************************************

    /**
     * Creates a SampleSeed to hold the data needed to calculate a sample. THe
     * sample is only instantiated when the view calls getSampleCasesTableModel
     * or getSampleFrequenciesTableModel
     *
     * @param experimentName the unique id of the experiment that determines the population
     *                       distribution which the sample is calculated from
     * @param sampleSize     the number of cases in the sample
     * @param sampleName     the optional name of the sample
     * @return the unique id of the sample
     */
    public int makeNewSample(String experimentName, int sampleSize,
                             String sampleName) throws IllegalArgumentException {
        return makeNewSample(experimentName, sampleSize, sampleName, null);
    }

    /**
     * Same as makeNewSample, but allows you to specify the number used to
     * calculate the sample. this is necessary if you want to see the sample
     * students got when replaying their work.
     *
     * @param experimentName the unique id of the experiment that determines the population
     *                       distribution which the sample is calculated from
     * @param sampleSize     the number of cases in the sample
     * @param sampleName     the optional name of the sample
     * @param sampleSeed     number used to calc sample, or null if you don't care
     * @return the unique id of the sample
     * @throws IllegalArgumentException
     */
    public int makeNewSample(String experimentName, int sampleSize,
                             String sampleName, Long sampleSeed) throws IllegalArgumentException {

        if (isNameUsed(sampleName, experimentName)) {
            throw new IllegalArgumentException("That name has already been "
                    + "used for another sample for this experiment");
        }

        SampleSeed seed = new SampleSeed(getNextSampleId(), experimentName, sampleSize,
                sampleName, sampleSeed);
        getSamples().add(seed);
        sampleChanged();
        return seed.getSampleId();
    }

    /**
     * Gets all of the unique ids of the samples for a given experiment
     *
     * @param experimentName the unique id of an experimenht
     * @return the unique ids of all the samples taken for the given experiment
     */
    public int[] getSampleIds(String experimentName) {
        SampleSeed seed;
        List<Integer> ids = new ArrayList<Integer>();
        Iterator<SampleSeed> i;
        int j;
        int[] result;

        for (i = getSamples().iterator(); i.hasNext(); ) {
            seed = i.next();
            if (seed.getExpName().equals(experimentName)) {
                ids.add(seed.getSampleId());
            }
        }

        result = new int[ids.size()];
        for (j = 0; j < ids.size(); j++) {
            result[j] = ids.get(j);
        }

        return result;
    }

    /**
     * Get the whole set of sample cases corresponding to a particular sample
     * seed
     *
     * @param seed the sample seed to generate the sample from
     * @return the set of all sample based on the sample seed
     */
    private Sample getSample(SampleSeed seed) {
        if (getModelType().equals(ModelType.BAYES)) {
            return new BayesSample(getCorrectBayesIm(), getExperiment(seed
                    .getExpName()), seed.getSampleSize(), seed.getSeed());
        } else {
            return new SemSample(getCorrectGraphSemImCopy(), getExperiment(seed
                    .getExpName()), seed.getSampleSize(), seed.getSeed());
        }
    }

    /**
     * @return the experiment name of the sample with the given sample id.
     */
    public String getExperimentNameForSampleId(int sampleId) {
        return (getSampleSeed(sampleId).getExpName());
    }

    /**
     * @return the sample with the given name in the given experimental setup.
     */
    public Sample getSample(String experimentName, String sampleName) {
        for (SampleSeed seed : samples) {
            if (seed.getExpName().equals(experimentName)
                    && seed.getName().equals(sampleName)) {
                return getSample(seed);
            }
        }
        return null;
    }

    /**
     * @return the name of the sample with the given sample id.
     */
    public String getSampleName(int sampleId) {
        SampleSeed seed = getSampleSeed(sampleId);
        if (seed == null) {
            throw new NullPointerException(
                    "there is no sampleSeed for the sampleId: " + sampleId);
        }
        return seed.getName();
    }

    /**
     * Can only be called if modelType == BAYES .
     *
     * @return the table model for the sample cases table.
     */
    public SampleCasesTableModel getSampleCasesTableModel(int sampleId) {
        if (!getModelType().equals(ModelType.BAYES)) {
            return null;
        }
        SampleSeed seed = getSampleSeed(sampleId);
        if (seed == null) {
            throw new NullPointerException(
                    "there is no sampleSeed for the sampleId: " + sampleId);
        }
        return new SampleCasesTableModel(this, seed);
    }

    /**
     * @return the table model for the sample frequencies table for the given
     *         sample id.
     */
    public AbstractSampleTable getSampleFrequenciesTableModel(int sampleId) {
        SampleSeed seed = getSampleSeed(sampleId);
        if (seed == null) {
            throw new NullPointerException(
                    "there is no sampleSeed for the sampleId: " + sampleId);
        }
        if (getModelType().equals(ModelType.BAYES)) {
            return new BayesSampleFrequenciesTableModel(this, seed);
        } else if (getModelType().equals(ModelType.SEM)) {
            return new SemSampleFrequenciesTableModel(this, seed);
        } else {
            System.err.println(getClass() + " shouldn't get here");
            return null;
        }
    }

    /**
     * Removes all samples from this experimental setup.
     */
    void cleanSamples(String experimentalSetupName) {
        SampleSeed seed;
        List<SampleSeed> seedsToRemove = new ArrayList<SampleSeed>();
        for (SampleSeed sample : getSamples()) {
            seed = (sample);
            if (seed.getExpName().equals(experimentalSetupName)) {
                seedsToRemove.add(seed);
            }
        }
        for (SampleSeed aSeedsToRemove : seedsToRemove) {
            getSamples().remove(aSeedsToRemove);
        }
    }

    /**
     * Deletes sample with the given sample id.
     */
    public void deleteSample(int sampleId) {
        SampleSeed seed = getSampleSeed(sampleId);
        getSamples().remove(seed);
        sampleChanged();
    }

    /**
     * Gets the stored sample information (like the name of the sample and the
     * random number for generating the sample) for the unique sample id.
     *
     * @param sampleId unique id for the SampleSeed.
     * @return the SampleSeed data structure with the info for generating the
     *         sample.
     */
    public SampleSeed getSampleSeed(int sampleId) {
        SampleSeed seed;
        for (SampleSeed sample : getSamples()) {
            seed = sample;
            if (seed.getSampleId() == sampleId) {
                return seed;
            }
        }
        return null;
    }

    private boolean isNameUsed(String name, String experimentName) {
        int[] sampleIds = getSampleIds(experimentName);
        for (int sampleId : sampleIds) {
            if (name.equals(getSampleName(sampleId))) {
                return true;
            }
        }
        return false;
    }

    // ***********************************************************************
    // COMPARED INDEPENDENCIES METHOD
    // ***********************************************************************

    /**
     * @param experimentName the name of the experimental setup.
     * @param showPopulation true if population data is to be shown.
     * @param showSample     true if sample data is to be shown.
     * @return the table model of the independencies table.
     */
    public IndependenciesTableModel getIndependenciesTableModel(
            String experimentName, boolean showPopulation, boolean showSample) {
        return new IndependenciesTableModel(this, experimentName,
                showPopulation, showSample);
    }

    /**
     * @return the guessed independencies for the given column in the given
     *         experimental setup.
     */
    public GuessedIndependencies getGuessedIndependenciesForColumn(
            String experimentName, String columnName) {
        GuessedIndependencies guess;

        guess = getGuessedIndependenceForHypothesis(experimentName, columnName);

        if (guess != null) {
            return guess;
        }

        return getGuessedIndependenceForSample(experimentName, columnName);
    }

    public GuessedIndependencies getGuessedIndependenceForHypothesis(
            String experimentName, String hypotheticalGraphName) {
        GuessedIndependenciesHolder guessHolder;

        cleanIndependencies();

        for (GuessedIndependenciesHolder guessedIndependency : getGuessedIndependencies()) {
            guessHolder = guessedIndependency;
            if (guessHolder instanceof GuessedIndependenciesForHypothesisHolder) {
                if ((guessHolder.getExperimentalSetupName()
                        .equals(experimentName))
                        && (((GuessedIndependenciesForHypothesisHolder) guessHolder)
                        .getHypotheticalGraphName()
                        .equals(hypotheticalGraphName))) {
                    return guessHolder.getGuess();
                }
            }
        }
        if (isValidExperimentName(experimentName)
                && isValidHypotheticalGraphName(hypotheticalGraphName)) {
            guessHolder = new GuessedIndependenciesForHypothesisHolder(getModel(),
                    experimentName, hypotheticalGraphName);
            getGuessedIndependencies().add(guessHolder);
            return guessHolder.getGuess();
        }
        return null;
    }

    public GuessedIndependencies getGuessedIndependenceForSample(
            String experimentId, String sampleName) {
        GuessedIndependenciesHolder guessHolder;

        cleanIndependencies();

        for (GuessedIndependenciesHolder guessedIndependency : getGuessedIndependencies()) {
            guessHolder = guessedIndependency;
            if (guessHolder instanceof GuessedIndependenciesForSampleHolder)
                if ((guessHolder.getExperimentalSetupName()
                        .equals(experimentId))
                        && (((GuessedIndependenciesForSampleHolder) guessHolder)
                        .getSampleName().equals(sampleName))) {
                    return guessHolder.getGuess();
                }
        }
        if (isValidExperimentName(experimentId)
                && isNameUsed(sampleName, experimentId)) {
            guessHolder = new GuessedIndependenciesForSampleHolder(getModel(),
                    experimentId, sampleName);
            getGuessedIndependencies().add(guessHolder);
            return guessHolder.getGuess();
        }
        return null;
    }

    private void cleanIndependencies() {
        List<GuessedIndependenciesHolder> toDelete = new ArrayList<GuessedIndependenciesHolder>();
        GuessedIndependenciesHolder guessHolder;

        for (GuessedIndependenciesHolder guessedIndependency : getGuessedIndependencies()) {
            guessHolder = guessedIndependency;
            if (guessHolder instanceof GuessedIndependenciesForHypothesisHolder) {
                if (!isValidExperimentName(guessHolder
                        .getExperimentalSetupName())
                        || !isValidHypotheticalGraphName(((GuessedIndependenciesForHypothesisHolder) guessHolder)
                        .getHypotheticalGraphName())) {
                    toDelete.add(guessHolder);
                }
            } else if (guessHolder instanceof GuessedIndependenciesForSampleHolder) {
                String experimentName = guessHolder.getExperimentalSetupName();
                String sampleName = ((GuessedIndependenciesForSampleHolder) guessHolder)
                        .getSampleName();

                if (!isValidExperimentName(experimentName)
                        || !isNameUsed(sampleName, experimentName)) {
                    toDelete.add(guessHolder);
                }
            }
        }
        for (GuessedIndependenciesHolder aToDelete : toDelete) {
            getGuessedIndependencies().remove(aToDelete);
        }
    }

    // ***********************************************************************
    // HELPER METHODS
    // ***********************************************************************

    /**
     * Helper method that gets all the variable names from a graph, does nothing
     * if graph is null.
     *
     * @param graph the graph whose variable names we want.
     * @return a List of Strings of variable names.
     */
    private List<String> getVariableNamesFromGraph(Graph graph) {
        Node variable;
        List<String> variableNames = new ArrayList<String>();

        if (graph == null) {
            return null;
        }

        for (Node node : graph.getNodes()) {
            variable = node;
            variableNames.add(variable.getName());
        }

        return variableNames;
    }

    /**
     * Gets the longest parameter name (for the inner classes) of the given
     * variable.
     */
    String getLongestVariableParameter(String varName) {
        String longestParameter = "";
        String parameter;

        if (getModelType().equals(ModelType.BAYES)) {
            Node var = getCorrectBayesIm().getBayesPm().getDag()
                    .getNode(varName);// getGraph().getNode(varName);
            // Node variable = CORRECT_GRAPH_BAYES_PM.getVariable();
            int numParameters = getCorrectBayesIm().getBayesPm()
                    .getNumCategories(var);
            for (int i = 0; i < numParameters; i++) {
                parameter = getCorrectBayesIm().getBayesPm()
                        .getCategory(var, i);
                if (parameter.length() > longestParameter.length()) {
                    longestParameter = parameter;
                }
            }
        } else if (getModelType().equals(ModelType.SEM)) {
            // what is the length???
            // System.out.println(getClass() +
            // " don't know what to return...");
        }
        return longestParameter + "   ";
    }

    /**
     * @return the instructions of the exercise.
     */
    public String getInstructions() {
        if (instructions == null) {
            return null;
        }
        return instructions.replaceAll("0x0a", "\n");
    }

    /**
     * @return the table model of the exercise variable status table.
     */
    public ExerciseVariableStatusTableModel getExerciseVariableStatusTableModel() {
        return new ExerciseVariableStatusTableModel(this);
    }

    /**
     * Enable student guessing for guessed independencies.
     */
    public void setStudentGuessEnabled(boolean includedGuess) {
        setStudentGuessesEnabled(includedGuess);
    }

    /**
     * @return whether or not student guessing for guessed independencies is
     *         enabled.
     */
    public boolean getStudentGuessEnabled() {
        return isStudentGuessesEnabled();
    }

    public BayesIm getCorrectBayesIm() {
        return correctBayesIm;
    }

    public SemIm getCorrectSemIm() {
        return correctSemIm;
    }

    public List<FinanceTransaction> getMoneyTransactions() {
        return moneyTransactions;
    }

    void setMoneyTransactions(List<FinanceTransaction> moneyTransactions) {
        this.moneyTransactions = moneyTransactions;
    }

    void setCurrentBalance(Integer currentBalance) {
        this.currentBalance = currentBalance;
    }

    void setTotalInitialBalance(Integer totalInitialBalance) {
        this.totalInitialBalance = totalInitialBalance;
    }

    void setCostPerObs(Integer costPerObs) {
        this.costPerObs = costPerObs;
    }

    void setCostPerIntervention(Integer costPerIntervention) {
        this.costPerIntervention = costPerIntervention;
    }

    int getNextSampleId() {
        return nextSampleId++;
    }

    void setNumberOfGuesses(int numberOfGuesses) {
        this.numberOfGuesses = numberOfGuesses;
    }

    Graph getLastGraphGuessed() {
        return lastGraphGuessed;
    }

    void setLastGraphGuessed(Graph lastGraphGuessed) {
        this.lastGraphGuessed = lastGraphGuessed;
    }

    void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isStudentGuessesEnabled() {
        return isStudentGuessesEnabled;
    }

    void setStudentGuessesEnabled(boolean studentGuessesEnabled) {
        isStudentGuessesEnabled = studentGuessesEnabled;
    }

    void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    List<ExperimentalSetup> getExperiments() {
        return experiments;
    }

    void setExperiments(List<ExperimentalSetup> experiments) {
        this.experiments = experiments;
    }

    List<HypotheticalGraph> getHypotheticalGraphs() {
        return hypotheticalGraphs;
    }

    void setHypotheticalGraphs(List<HypotheticalGraph> hypotheticalGraphs) {
        this.hypotheticalGraphs = hypotheticalGraphs;
    }

    List<SampleSeed> getSamples() {
        return samples;
    }

    void setSamples(List<SampleSeed> samples) {
        this.samples = samples;
    }

    List<GuessedIndependenciesHolder> getGuessedIndependencies() {
        return guessedIndependencies;
    }

    void setGuessedIndependencies(List<GuessedIndependenciesHolder> guessedIndependencies) {
        this.guessedIndependencies = guessedIndependencies;
    }

    Vector<ModelChangeListener> getModelChangeListeners() {
        return modelChangeListeners;
    }

    void setModelChangeListeners(Vector<ModelChangeListener> modelChangeListeners) {
        this.modelChangeListeners = modelChangeListeners;
    }

    Map<String, Boolean> getVariablesIntervenable() {
        return variablesIntervenable;
    }

    void setVariablesIntervenable(Map<String, Boolean> variablesIntervenable) {
        this.variablesIntervenable = variablesIntervenable;
    }

}
