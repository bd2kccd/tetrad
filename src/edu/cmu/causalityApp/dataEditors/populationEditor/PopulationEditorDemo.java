package edu.cmu.causalityApp.dataEditors.populationEditor;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * This class demos the BayesPopulation Editor for debugging.
 *
 * @author mattheweasterday
 */
public class PopulationEditorDemo extends JFrame {

    /**
     * Constructor.
     */
    private PopulationEditorDemo(CausalityLabModel model) {
        super("Test PopulationEditor");

        int inset = 100;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);


        PopulationEditor populationEditor = new PopulationEditor(model);
        populationEditor.setLocation(100, 50);
        populationEditor.setVisible(true);
        populationEditor.pack();

        JDesktopPane desktop = new JDesktopPane();
        desktop.setPreferredSize(new Dimension(screenSize.width - inset * 2, screenSize.height - inset * 2));
        desktop.add(populationEditor);
        setContentPane(desktop);
    }

    /**
     * Main test function.
     */
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    //=========PRIVATE METHODS========================================


    private static void createAndShowGUI() {
        //Use the Java look and feel.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Empty
        }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        //Instantiate the controlling class.
        JFrame frame = new PopulationEditorDemo(makeMiniModel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        //FrameDemo2 demo = new FrameDemo2();

        //Add components to it.
        //Container contentPane = frame.getContentPane();

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null); //center it
        frame.setVisible(true);
    }


    private static CausalityLabModel makeMiniModel() {
        Graph graph = makeModel();
        BayesPm pm = makePM(graph);
        BayesIm im = makeIM(pm);
        CausalityLabModel.initialize(im, null);
        CausalityLabModel model = CausalityLabModel.getModel();
        model.addNewExperiment("experiment too");

        try {
            model.setExperimentalVariableLocked("experiment too", "education", "college");
        } catch (IllegalArgumentException e) {
            System.out.println("populationEditorDemo.java: couldn't lock");
        }

        return model;
    }

    /**
     * Creates a test model for JUNIT.
     *
     * @return a graph
     */
    private static Graph makeModel() {
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("income"));
        m1.addNode(new GraphNode("happiness"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        return m1;
    }

    private static BayesPm makePM(Graph aGraph) {
        BayesPm pm = new BayesPm(new Dag(aGraph));
        pm.setNumCategories(pm.getDag().getNode("education"), 3);

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


        return pm;
    }

    private static BayesIm makeIM(BayesPm aPm) {
        int i;
        BayesIm im = new MlBayesIm(aPm);
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

        //System.out.println(im.toString());
        return im;
    }
}

