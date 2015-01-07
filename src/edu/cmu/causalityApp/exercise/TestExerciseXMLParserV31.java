package edu.cmu.causalityApp.exercise;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import java.io.File;
import java.io.IOException;

/**
 * @author mattheweasterday
 */
public class TestExerciseXMLParserV31 extends junit.framework.TestCase {


    public TestExerciseXMLParserV31(String name) {
        super(name);
    }


    public void test1() {


        parseExerciseV31("xmlFiles/3_1_exercises/cl31_u5m16ex11.xml");
        parseExerciseV31("xmlFiles/3_1_exercises/cl31_u5m16ex12.xml");
        parseExerciseV31("xmlFiles/3_1_exercises/cl31_u5m16ex13a.xml");
        parseExerciseV31("xmlFiles/3_1_exercises/cl31_u5m16ex13b.xml");
        parseExerciseV31("xmlFiles/3_1_exercises/cl31_u5m16ex13c.xml");
        parseExerciseV31("xmlFiles/3_1_exercises/cl31_u5m16ex13d.xml");
    }

    private void parseExerciseV31(String name) {
        File inputFile = new File(this.getClass().getResource(name).getFile());

        try {
            System.out.println("ExerciseFileReader: uhh not validating dude");

            // FIXME: This line is generating an error in the
            // applet-based deployment.  [WLW 06/28/04]
            Builder parser = new Builder(false);

            Document doc = parser.build(inputFile);
            System.out.println("ExerciseFileReader doc: " + doc.toXML());
            ExerciseXmlParserV31.getExercise(doc.getRootElement());

        } catch (ValidityException ex) {
            System.err.println("XML file is not valid in ExerciseFileReader.read");
            System.err.println(ex);

        } catch (ParsingException exp) {
            System.err.println("Parsing exception in ExerciseFileReader.read");
            System.err.println(exp);

        } catch (IOException ie) {
            System.err.println("IOException in ExerciseFileReader.read");
            System.err.println(ie);
            if (ie instanceof java.net.UnknownHostException) {
                System.err.println("The problem may be that you are not connected to the internet");
            }

        }

    }

}

