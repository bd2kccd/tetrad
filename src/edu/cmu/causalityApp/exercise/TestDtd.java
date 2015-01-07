package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author mattheweasterday
 */
public class TestDtd extends junit.framework.TestCase {

    public TestDtd(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    }

    public void testReadAndValidate() {
        try {
            InputStream iStream = this.getClass().getResourceAsStream("xmlFiles/3_0_exercises/test.xml");
            URL pathToDTD = this.getClass().getResource("xmlFiles/exercise.dtd");
//            Builder parser = new Builder(true);
//            parser.build(iStream, pathToDTD.toString());
            Builder parser = new Builder(false);
            parser.build(iStream);

            iStream = this.getClass().getResourceAsStream("xmlFiles/3_0_exercises/test2.xml");
            System.out.println(iStream.toString());

            parser.build(iStream, pathToDTD.toString());

        } catch (ValidityException ex) {
            System.err.println("Cafe con Leche is invalid today. (Somewhat embarrassing.)");
            System.err.println(ex.toString());
            assertTrue(false);
        } catch (ParsingException ex) {
            System.err.println("Cafe con Leche is malformed today. (How embarrassing!)");
            System.err.println(ex.toString());
            assertTrue(false);
        } catch (IOException ex) {
            System.err.println("Could not connect to Cafe con Leche. The site may be down.");
            System.err.println(ex.toString());
            assertTrue(false);
        }
    }

    public void tetValidatedRoundTripWithSem() {
        Exercise exercise1 = new Exercise("these are the instructions",
//                Exercise.GOAL_FIND_GRAPH,
                makeSem(),
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

        //URL path = this.getClass().getResource("xmlFiles/");
        //String filePath = edu.cmu.StringTemp.string(path.getPath() + "testSem.xml");
        Element element = runTheExercise(exercise1, "testSem.xml");
        Exercise exercise2 = ExerciseXmlParserV33.getExercise(element);
        compareExercises(exercise1, exercise2);
        assertNotNull(exercise2.getSemModelIm());
        Node x = exercise2.getSemModelIm().getSemPm().getGraph().getNode("X");
        assertTrue(x.getNodeType() == NodeType.LATENT);
        assertTrue(x.getCenterX() == 5);
    }

    public void tetValidatedRoundTripWithBayes() {
        Exercise exercise1 = new Exercise("these are the instructions",
//                Exercise.GOAL_FIND_GRAPH,
                makeIM(makePM(makeModel())),
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

        Element element = runTheExercise(exercise1, "testBayes.xml");
        Exercise exercise2 = ExerciseXmlParserV33.getExercise(element);
        compareExercises(exercise1, exercise2);
        assertNotNull(exercise2.getBayesModelIm());

        Node x = exercise2.getBayesModelIm().getDag().getNode("Latent");
        assertTrue(x.getNodeType() == NodeType.LATENT);
        assertTrue(x.getCenterX() == 5);
    }


    private Element runTheExercise(Exercise exercise, String fileName) {
        URL path = this.getClass().getResource("xmlFiles/");

        String filePath = path.getPath();
        File file = new File(filePath, fileName);
        //String filePath = edu.cmu.StringTemp.string(path.getPath() + fileName);
        //File file = new File(filePath);

        try {
            ExerciseFileWriter.write(exercise, file);
            InputStream iStream = this.getClass().getResourceAsStream("xmlFiles/" + fileName);
            //BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            //String s;
            //while((s = br.readLine())!=null){ System.out.println(s);}
            URL pathToDTD = this.getClass().getResource("xmlFiles/exercise.dtd");
            Builder parser = new Builder(true);
            return parser.build(iStream, pathToDTD.toString()).getRootElement();

        } catch (ValidityException ex) {
            System.err.println("Cafe con Leche is invalid today. (Somewhat embarrassing.)");
            System.err.println(ex.toString());
            assertTrue(false);
            return null;
        } catch (ParsingException ex) {
            System.err.println("Cafe con Leche is malformed today. (How embarrassing!)");
            System.err.println(ex.toString());
            assertTrue(false);
            return null;
        } catch (IOException ex) {
            System.err.println("Could not connect to Cafe con Leche. The site may be down.");
            System.err.println(ex.toString());
            assertTrue(false);
            return null;
        }
    }

    private void compareExercises(Exercise exercise1, Exercise exercise2) {
        assertTrue(exercise1.getPrompt().equals(exercise2.getPrompt()));
        assertTrue(exercise1.getGoal().equals(exercise2.getGoal()));
        assertTrue(exercise1.getCorrectGraphInclusion() == exercise2.getCorrectGraphInclusion());
        assertTrue(exercise1.getCorrectManipulatedGraphInclusion() == exercise2.getCorrectManipulatedGraphInclusion());
        assertTrue(exercise1.getPopulationInclusion() == exercise2.getPopulationInclusion());
        assertTrue(exercise1.getExperimentalSetupInclusion() == exercise2.getExperimentalSetupInclusion());
        assertTrue(exercise1.getHypotheticalGraphInclusion() == exercise2.getHypotheticalGraphInclusion());
        assertTrue(exercise1.getHypotheticalManipulatedGraphInclusion() == exercise2.getHypotheticalManipulatedGraphInclusion());
        assertTrue(exercise1.getSampleInclusion() == exercise2.getSampleInclusion());
        assertTrue(exercise1.getIndependenciesInclusion() == exercise2.getIndependenciesInclusion());
    }

    /*
    public static void main(String [] args){

        Exercise exercise1 = new Exercise(
                "these are the instructions",
                "goal??",
                makeIM(makePM(makeModel())),
                null, WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                true,
                true);
        Element element = ExerciseXmlRenderer.getElement(exercise1);
        Exercise exercise2 = ExerciseXmlParser.getExercise(element);


        Document doc = new Document(element);
        DocType doctype = new DocType(Exercise.EXERCISE, "exercise.dtd");
        doc.insertChild(doctype, 0);


        try {
            File file = new File("test.xml");
            PrintStream printOut = new PrintStream(new FileOutputStream(file));
            //Serializer serializer = new Serializer(System.out, "ISO-8859-1");
            Serializer serializer = new Serializer(printOut, "ISO-8859-1");
            serializer.setIndent(4);
            serializer.setMaxLength(64);
            serializer.write(doc);
            printOut.flush();
            printOut.close();
        }
        catch (IOException ex) {
            System.err.println(ex);
        }

    }
    */

    /**
     * Creates a test model for JUNIT.
     */
    private static Graph makeModel() {
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("happiness"));
        m1.addNode(new GraphNode("income"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        tn.setCenterX(5);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        return m1;
    }

    private static BayesPm makePM(Graph aGraph) {
        BayesPm pm = new BayesPm(new Dag(aGraph));

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

    private static MlBayesIm makeIM(BayesPm aPm) {
        int i;
        MlBayesIm im = new MlBayesIm(aPm);
        //Latent
        i = im.getNodeIndex(im.getNode("Latent"));
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);

        //education
        i = im.getNodeIndex(im.getNode("education"));
        im.setProbability(i, 0, 0, 0.4);  //latent ==  education == college
        im.setProbability(i, 0, 1, 0.4);  //latent ==  education == high shcool
        im.setProbability(i, 0, 2, 0.2);  //latent ==  education == none
        im.setProbability(i, 1, 0, 0.5);  //latent ==  education == college
        im.setProbability(i, 1, 1, 0.3);  //latent ==  education == high shcool
        im.setProbability(i, 1, 2, 0.2);  //latent ==  education == none

        //happiness
        i = im.getNodeIndex(im.getNode("happiness"));
        im.setProbability(i, 0, 0, 0.9);  //education == college      happiness = true
        im.setProbability(i, 0, 1, 0.1);  //education == college      happiness = false
        im.setProbability(i, 1, 0, 0.6);  //education == high school  happiness = true
        im.setProbability(i, 1, 1, 0.4);  //education == high school  happiness = false
        im.setProbability(i, 2, 0, 0.2);  //education == none         happiness = true
        im.setProbability(i, 2, 1, 0.8);  //education == none         happiness = false

        //income
        i = im.getNodeIndex(im.getNode("income"));
        im.setProbability(i, 0, 0, 0.7); //education == college       income == high
        im.setProbability(i, 0, 1, 0.2); //education == college       income == medium
        im.setProbability(i, 0, 2, 0.1); //education == college       income == low
        im.setProbability(i, 1, 0, 0.4); //education == high school   income == high
        im.setProbability(i, 1, 1, 0.4); //education == high school   income == medium
        im.setProbability(i, 1, 2, 0.2); //education == high school   income == low
        im.setProbability(i, 2, 0, 0.1); //education == none          income == high
        im.setProbability(i, 2, 1, 0.4); //education == none          income == medium
        im.setProbability(i, 2, 2, 0.5); //education == none          income == low

        //System.out.println(im.toString());
        return im;
    }


    SemIm makeSem() {
        Graph graph = new Dag();

        GraphNode g = new GraphNode("X");
        g.setNodeType(NodeType.LATENT);
        g.setCenterX(5);

        graph.addNode(g);
        graph.addNode(new GraphNode("W"));
        graph.addNode(new GraphNode("Y"));
        graph.addNode(new GraphNode("Z"));

        graph.addDirectedEdge(graph.getNode("X"), graph.getNode("Y"));
        graph.addDirectedEdge(graph.getNode("W"), graph.getNode("Y"));
        graph.addDirectedEdge(graph.getNode("Y"), graph.getNode("Z"));

        SemPm semPm = new SemPm(graph);
        //SemIm semIm = SemIm.newInstance(semPm);
        SemIm semIm = new SemIm(semPm);

        Node x = semIm.getSemPm().getGraph().getNode("X");
        Node y = semIm.getSemPm().getGraph().getNode("Y");
        Node w = semIm.getSemPm().getGraph().getNode("W");
        Node z = semIm.getSemPm().getGraph().getNode("Z");

        //semIm.setParamValue(graph.getNode("X"), graph.getNode("Y"), 0.5);
        //semIm.setParamValue(graph.getNode("W"), graph.getNode("Y"), 0.6);
        //semIm.setParamValue(graph.getNode("Y"), graph.getNode("Z"), 0.7);
        semIm.setEdgeCoef(x, y, 0.5);
        semIm.setEdgeCoef(w, y, 0.6);
        semIm.setEdgeCoef(y, z, 0.7);

        //SemGraph semGraph = semIm.getSemPm().getGraph();
        //semIm.setParamValue(semIm.getSemPm().getNodeParameter(semGraph.getNode("X")), 1.0);
        //semIm.setParamValue(semIm.getSemPm().getNodeParameter(semGraph.getNode("W")), 1.0);
        //semIm.setParamValue(semIm.getSemPm().getNodeParameter(semGraph.getExogenous(semGraph.getNode("Y"))), 1.0);
        //semIm.setParamValue(semIm.getSemPm().getNodeParameter(semGraph.getExogenous(semGraph.getNode("Z"))), 1.0);
        semIm.setMean(x, 1.0);
        semIm.setMean(w, 1.0);
        semIm.setErrCovar(y, 1.0);
        semIm.setErrCovar(z, 1.0);


        return semIm;
    }

}
