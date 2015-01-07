package edu.cmu.causalityApp.exercise;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class reads in an exercise file.
 *
 * @author mattheweasterday
 */
public class ExerciseFileReader {

    /**
     * Creats an exercise from disk.
     *
     * @param file the exercise file.
     * @return the exercise.
     */
    public static Exercise read(File file) {
        if (file == null) {
            throw new NullPointerException("file was null: " + file);
        }
        try {
            InputStream input = new FileInputStream(file);

            return read(input);
        } catch (java.io.IOException e) {
            throw new NullPointerException("got bad file: " + file);
            //e.printStackTrace();
        }
    }

    /**
     * Creates an exercise from the web.
     *
     * @param input the stream, presumably from a servlet containing the exercise.
     * @return the exercise or null if no exercise.
     */
    public static Exercise read(InputStream input) {
        try {
            boolean validate = false;
            Builder parser = new Builder(getXmlReader(), validate);

            System.out.println("in ExerciseFileReader: read function");
            Document doc = parser.build(input);
            System.out.println("after build function");
            System.out.println("ExerciseFileReader doc: " + doc.toXML());

            System.out.println("before declaring exVersion variable");
            String exVersion = doc.getRootElement().getAttributeValue(Exercise.VERSION);
            System.out.println("the version:" + exVersion);
            if (exVersion.equals(ExerciseXmlParserV43.VERSION)) {
                System.out.println("Using parser V4.3");
                return ExerciseXmlParserV43.getExercise(doc.getRootElement());
            } else if (exVersion.equals(ExerciseXmlParserV42.VERSION)) {
                return ExerciseXmlParserV42.getExercise(doc.getRootElement());
            } else if (exVersion.equals(ExerciseXmlParserV41.VERSION)) {
                return ExerciseXmlParserV41.getExercise(doc.getRootElement());
            } else if (exVersion.equals(ExerciseXmlParserV40.VERSION)) {
                return ExerciseXmlParserV40.getExercise(doc.getRootElement());

            } else if (exVersion.equals(ExerciseXmlParserV33.VERSION)) {
                return ExerciseXmlParserV33.getExercise(doc.getRootElement());

            } else if (exVersion.equals(ExerciseXmlParserV32.VERSION)) {
                return ExerciseXmlParserV32.getExercise(doc.getRootElement());

            } else if (exVersion.equals(ExerciseXmlParserV31.VERSION)) {
                return ExerciseXmlParserV31.getExercise(doc.getRootElement());

            } else {
                return ExerciseXmlParserV43.getExercise(doc.getRootElement());
            }

        } catch (ValidityException ex) {
            System.err.println("XML file is not valid in ExerciseFileReader.read");
            System.err.println(ex);
            return null;
        } catch (ParsingException pee) {
            System.err.println("Parse exception in ExerciseFileReader.read");
            System.err.println(pee);
            return null;
        } catch (IOException ie) {
            System.err.println("IOException in ExerciseFileReader.read");
            System.err.println(ie);
            if (ie instanceof java.net.UnknownHostException) {
                System.err.println("The problem may be that you are not connected to the internet");
            }
            ie.printStackTrace();
            return null;
        }
    }

    /**
     * When the lab is offline, we have a problem reading exercises because
     * the <DOCTYPE> tag in the exercise specifies a dtd that exists online. To
     * get around this problem, we need to set the XMLReader used by the XMO
     * parser.  This method returns that reader.
     */
    private static XMLReader getXmlReader() {
        try {
            XMLReader xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            xerces.setFeature("http://apache.org/xml/features/validation/schema", true);
            xerces.setEntityResolver(new CausalityDtdResolver());
            return xerces;
        } catch (SAXException ex) {
            System.out.println("Could not load Xerces.");
            System.out.println(ex.getMessage());
        }
        return null;
    }

    /**
     * This class returns a DTD kept in the jar (for use when the lab is offline).
     */
    private static class CausalityDtdResolver implements EntityResolver {

        /**
         * When the Resolver receives an exercise with a doctype tag like:
         * <pre>
         * <!DOCTYPE exercise PUBLIC
         *      "-//Carnegie Mellon University//DTD Causality Lab Assignment 1.1//EN"
         *      "http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_1_1.dtd">
         * </pre>
         * The first line is the publidId, the second line is the systemId.
         * <p/>
         * For links on how to do this, see:
         * http://www.xom.nu/apidocs/nu/xom/Builder.html#Builder(org.xml.sax.XMLReader
         * <p/>
         * OASIS Open Catalog Standard
         * http://oasis-open.org/committees/entity/spec-2001-08-06.html
         * <p/>
         * SAX EntityResolver interface
         * http://java.sun.com/j2se/1.4.2/docs/api/org/xml/sax/EntityResolver.html
         * <p/>
         * Apache XML Commons entity resolver
         * http://xml.apache.org/commons/components/resolver/
         * <p/>
         * Useful article on the same
         * http://xml.apache.org/commons/components/resolver/resolver-article.html
         *
         * @return InputSource a resolver that reads from internal dtd.
         */
        public InputSource resolveEntity(String publicId, String systemId) {
            System.out.println("in resolveEntity function:");
            System.out.println("systemID =" + systemId);

            // Note: I moved the local copies of the dtds to /resources, since they weren't
            // loading correctly on Linux.

            if (systemId.equals("http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_1_1.dtd")) {
                String dtd = "/resources/dtd/causality3.3.dtd";
                InputStream is = ExerciseFileReader.class.getResourceAsStream(dtd);
                return new InputSource(is);

            } else if (systemId.equals("http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_4_0.dtd")) {
                String dtd = "/resources/dtd/cmu_phil_cl_exercise_4_0.dtd";
                InputStream is = ExerciseFileReader.class.getResourceAsStream(dtd);
                return new InputSource(is);

            } else if (systemId.equals("http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_4_1.dtd")) {
                String dtd = "/resources/dtd/cmu_phil_cl_exercise_4_1.dtd";
                InputStream is = ExerciseFileReader.class.getResourceAsStream(dtd);
                return new InputSource(is);

            } else if (systemId.equals("http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_4_2.dtd")) {
                String dtd = "/resources/dtd/cmu_phil_cl_exercise_4_2.dtd";
                InputStream is = ExerciseFileReader.class.getResourceAsStream(dtd);

                return new InputSource(is);

            } else if (systemId.equals("http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_4_3.dtd")) {
                System.out.println("== 4_3 version");

                String dtd = "/resources/dtd/cmu_phil_cl_exercise_4_3.dtd";
                InputStream is = ExerciseFileReader.class.getResourceAsStream(dtd);

                return new InputSource(is);
            } else {
                String dtd = "/resources/dtd/cmu_phil_cl_exercise_4_3.dtd";

                System.out.println("filePath " + dtd);
                InputStream is = ExerciseFileReader.class.getResourceAsStream(dtd);
                return new InputSource(is);
            }
        }
    }

}


