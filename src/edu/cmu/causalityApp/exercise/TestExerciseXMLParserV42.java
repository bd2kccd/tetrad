package edu.cmu.causalityApp.exercise;

import junit.framework.TestCase;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ValidityException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author mattheweasterday
 */
public class TestExerciseXMLParserV42 extends junit.framework.TestCase {


    public TestExerciseXMLParserV42(String name) {
        super(name);
    }


    public void test1() {
        parseExerciseV42();
        parseExerciseV42();
    }

    private void parseExerciseV42() {

        InputStream iStream = this.getClass().getResourceAsStream("xmlFiles/4_2_exercises/exercise.xml");
        System.out.println(iStream.toString());
        try {
            System.out.println("ExerciseFileReader: uhh not validating dude");

            // FIXME: This line is generating an error in the
            // applet-based deployment.  [WLW 06/28/04]
            Builder parser = new Builder(true);

            Document doc = parser.build(iStream);
            System.out.println("ExerciseFileReader doc: " + doc.toXML());
            ExerciseXmlParserV42.getExercise(doc.getRootElement());

        } catch (ValidityException ex) {
            ex.printStackTrace();
            System.err.println("XML file is not valid in ExerciseFileReader.read");
            System.err.println(ex);
            TestCase.fail();
        } catch (nu.xom.ParsingException exp) {
            exp.printStackTrace();
            System.err.println("Parsing exception in ExerciseFileReader.read");
            System.err.println(exp);
            TestCase.fail();

        } catch (IOException ie) {
            ie.printStackTrace();
            System.err.println("IOException in ExerciseFileReader.read");
            System.err.println(ie);
            if (ie instanceof java.net.UnknownHostException) {
                System.err.println("The problem may be that you are not connected to the internet");
            }
            TestCase.fail();

        }

    }

}
