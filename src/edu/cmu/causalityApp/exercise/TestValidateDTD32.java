package edu.cmu.causalityApp.exercise;

import nu.xom.Builder;
import nu.xom.ValidityException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author mattheweasterday
 */
public class TestValidateDTD32 extends junit.framework.TestCase {


    public TestValidateDTD32(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    public void testeyecolor() {
        runt("xmlFiles/3_2_exercises/chain2.xml", false);
        runt("xmlFiles/3_2_exercises/cl31_u1m4ex4.xml", false);
        runt("xmlFiles/3_2_exercises/cl31_u1m4ex6.xml", false);
        runt("xmlFiles/3_2_exercises/eyecolor.xml", false);
        runt("xmlFiles/3_2_exercises/studyhabits.xml", false);

    }

    public void testLocal4DTD2() {
        runt("xmlFiles/3_2_exercises/eyecolorWWWPHIL.xml", false);
    }


    private void runt(String filename, boolean useLocalDTD) {
        try {
            InputStream iStream = this.getClass().getResourceAsStream(filename);
            URL pathToDTD = this.getClass().getResource("xmlFiles/causality3.2.dtd");

            Builder parser = new Builder(true);

            //do one or the other...
            if (useLocalDTD) {
                parser.build(iStream, pathToDTD.toString());
            } else {
                parser.build(iStream);
            }

        } catch (ValidityException ex) {
            System.err.println("Cafe con Leche is invalid today. (Somewhat embarrassing.)");
            System.err.println(ex.toString());
            assertTrue(false);
        } catch (nu.xom.ParsingException ex) {
            System.err.println("Cafe con Leche is malformed today. (How embarrassing!)");
            System.err.println(ex.toString());
            assertTrue(false);
        } catch (IOException ex) {
            System.err.println("Could not connect to Cafe con Leche. The site may be down.");
            System.err.println(ex.toString());
            assertTrue(false);
        }

    }

}
