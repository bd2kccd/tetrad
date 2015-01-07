package edu.cmu.causalityApp.exercise;

import edu.cmu.command.ExerciseHistory;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
 * Use this class to save exercises to files.
 *
 * @author mattheweasterday
 */
public class ExerciseFileWriter {


    /**
     * Writes an exercise to a file.
     *
     * @param exercise the exercise to write.
     * @param history  the list of actions the user did working on the exercise.
     * @param file     the file to write the exercise to.
     */
    public static void write(Exercise exercise, ExerciseHistory history, File file) {
        Element exerciseElement;
        if (history == null) {
            exerciseElement = ExerciseXmlRenderer.getElement(exercise);
        } else {
            exerciseElement = ExerciseXmlRenderer.getElement(exercise, history.render());
        }
        write(exerciseElement, file);
    }


    /**
     * Writes an exercise to a file.
     *
     * @param exercise the exercise to write.
     */
    public static void write(Exercise exercise, File file) {
        Element exerciseElement = ExerciseXmlRenderer.getElement(exercise);
        write(exerciseElement, file);
    }


    private static void write(Element exerciseElement, File file) {
        try {
            PrintStream printOut = new PrintStream(new FileOutputStream(file));

            Document document = new Document(exerciseElement);

            DocType doctype = new DocType(
                    "exercise",
                    "-//Carnegie Mellon University//DTD Causality Lab Assignment 4.3//EN",
                    "http://oli.web.cmu.edu/dtd/cmu_phil_cl_exercise_4_3.dtd");
            document.insertChild(doctype, 0);

            Serializer serializer = new Serializer(printOut, "UTF-8"); //ISO-8859-1
            serializer.setLineSeparator("\n");
            serializer.setIndent(4);
            serializer.write(document);
            printOut.close();

        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
}