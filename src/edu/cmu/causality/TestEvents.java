package edu.cmu.causality;


import edu.cmu.causality.event.*;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

import java.util.ArrayList;

/**
 * @author mattheweasterday
 */
public class TestEvents extends junit.framework.TestCase implements ModelChangeListener {

    private CausalityLabModel model;
    private boolean eventFired = false;
    private String exptID;
    private String hypID;

    public TestEvents(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        model = makeModel();
        model.addModelChangeListener(this);
        exptID = "Experiment 1";
        model.addNewExperiment(exptID);
        hypID = "Hypothesis 1";
        model.addNewHypotheticalGraph(hypID);
    }

    public void testNewHypothesisEvent() {
        model.addNewHypotheticalGraph("foo");
        eventCheck();
        model.removeHypotheticalGraph("foo");
        eventCheck();
    }

    public void testAddLatentEvent() {
        model.addLatentVariableToHypotheticalGraph("latent2", hypID);
        eventCheck();
        model.removeLatentVariableFromHypotheticalGraph("latent2", hypID);
        eventCheck();
    }

    public void testAddEdgeEvent() {
        model.addHypotheticalGraphEdge(hypID, "education", "income");
        eventCheck();
        model.removeEdgeFromHypotheticalGraph("education", "income", hypID);
        eventCheck();
    }

    public void testNewExperimentEvent() {
        model.addNewExperiment("foo");
        eventCheck();
        model.removeExperiment("foo");
        eventCheck();
    }

    public void testManipulateVariableEvent() {
        model.setExperimentalVariableRandomized(exptID, "education");
        eventCheck();
    }

    public void testStudyVariableEvent() {
        model.setExperimentalVariableStudied(exptID, "education", false);
        eventCheck();
    }

    public void testNewSampleEvent() {
        int id = model.makeNewSample(exptID, 100, "foo");
        eventCheck();
        model.deleteSample(id);
        eventCheck();
    }


    public void hypothesisChanged(HypothesisChangedEvent hcEvent) {
        eventFired = true;
    }

    public void experimentChanged(ExperimentChangedEvent ecEvent) {
        eventFired = true;
    }

    public void sampleChanged(SampleChangedEvent scEvent) {
        eventFired = true;
    }

    public void financeChanged() {
        eventFired = true;
    }


    private void eventCheck() {
        assertTrue(eventFired);
        eventFired = false;
    }


    private CausalityLabModel makeModel() {
        GraphNode tn;
        Graph m1 = new Dag();
        BayesPm pm;
        MlBayesIm im;


        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("happiness"));
        m1.addNode(new GraphNode("income"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        pm = new BayesPm(new Dag(m1));

        ArrayList<String> varVals = new ArrayList<String>();
        varVals.add("college");
        varVals.add("High school");
        varVals.add("none");
        pm.setCategories(pm.getDag().getNode("education"), varVals);

        varVals = new ArrayList<String>();
        varVals.add("high");
        varVals.add("medium");
        varVals.add("low");
        pm.setCategories(pm.getDag().getNode("income"), varVals);

        varVals = new ArrayList<String>();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("happiness"), varVals);

        varVals = new ArrayList<String>();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("Latent"), varVals);

        int i;
        im = new MlBayesIm(pm);
        //Latent
        i = im.getNodeIndex(im.getNode("Latent"));
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);

        //education
        i = im.getNodeIndex(im.getNode("education"));
        im.setProbability(i, 0, 0, 0.3);
        im.setProbability(i, 0, 1, 0.3);
        im.setProbability(i, 0, 2, 0.4);
        im.setProbability(i, 1, 0, 0.3);
        im.setProbability(i, 1, 1, 0.3);
        im.setProbability(i, 1, 2, 0.4);

        //happiness
        i = im.getNodeIndex(im.getNode("happiness"));
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);
        im.setProbability(i, 1, 0, 0.5);
        im.setProbability(i, 1, 1, 0.5);
        im.setProbability(i, 2, 0, 0.5);
        im.setProbability(i, 2, 1, 0.5);

        //income
        i = im.getNodeIndex(im.getNode("income"));
        im.setProbability(i, 0, 0, 0.3);
        im.setProbability(i, 0, 1, 0.3);
        im.setProbability(i, 0, 2, 0.4);
        im.setProbability(i, 1, 0, 0.3);
        im.setProbability(i, 1, 1, 0.3);
        im.setProbability(i, 1, 2, 0.4);
        im.setProbability(i, 2, 0, 0.3);
        im.setProbability(i, 2, 1, 0.3);
        im.setProbability(i, 2, 2, 0.4);

        CausalityLabModel.initialize(im, null);

        return CausalityLabModel.getModel();
    }
}
