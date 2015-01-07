package edu.cmu.causalityApp.exercise;


//import edu.cmu.causalityApp.exercise.ExerciseFileReader;

import java.io.File;
import java.io.InputStream;


/**
 * @author mattheweasterday
 */
public class TestExerciseFileReader extends junit.framework.TestCase {


    public TestExerciseFileReader(String name) {
        super(name);
    }

    public void setUp() throws Exception {

    }

    private File getFile(String fileName) {
        String path = Object.class.getResource(fileName).getPath();
        return new File(path);

    }

    public void testBatesInterventionReadFromFile() {
        assertNotNull(ExerciseFileReader.read(getFile("ODIN_tests/bates_intervention.xml")));
    }

    public void testBatesInterventionReadFromStream() {
        InputStream iStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("ODIN_tests/bates_intervention.xml");
        assertNotNull(ExerciseFileReader.read(iStream));
    }

    public void testJul08SaveFile2FromFile() {
        assertNotNull(ExerciseFileReader.read(getFile("ODIN_tests/Jul08-save-file-2.xml")));
    }

    public void testJul08SaveFile2FromStream() {
        InputStream iStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("ODIN_tests/Jul08-save-file-2.xml");
        assertNotNull(ExerciseFileReader.read(iStream));
    }

    public void testJul08SaveFileFromFile() {
        assertNotNull(ExerciseFileReader.read(getFile("ODIN_tests/Jul08-save-file.xml")));
    }

    public void testJul08SaveFileFromStream() {
        InputStream iStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("ODIN_tests/Jul08-save-file.xml");
        assertNotNull(ExerciseFileReader.read(iStream));
    }

    public void testOct08SaveFile2FromFile() {
        assertNotNull(ExerciseFileReader.read(getFile("ODIN_tests/Oct08-save-file-2.xml")));
    }

    public void testOct08SaveFile2FromStream() {
        InputStream iStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("ODIN_tests/Oct08-save-file-2.xml");
        assertNotNull(ExerciseFileReader.read(iStream));
    }

    public void testOct08SaveFileFromFile() {
        assertNotNull(ExerciseFileReader.read(getFile("ODIN_tests/Oct08-save-file.xml")));
    }

    public void testOct08SaveFileFromStream() {
        InputStream iStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("ODIN_tests/Oct08-save-file.xml");
        assertNotNull(ExerciseFileReader.read(iStream));
    }

    public void testQATstSaveFileFromFile() {
        assertNotNull(ExerciseFileReader.read(getFile("ODIN_tests/qa_tst.xml")));
    }

    public void testQATstSaveFileFromStream() {
        InputStream iStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("ODIN_tests/qa_tst.xml");
        assertNotNull(ExerciseFileReader.read(iStream));
    }
}
