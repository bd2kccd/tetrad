package edu.cmu.causalityApp;

import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.causalityApp.exercise.ExerciseFileReader;
import edu.cmu.causalityApp.exercise.ExerciseFileWriter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This class takes a directory with a bunch of causality lab exercises of
 * verion 3.3 or higher and saves them as exercises of the getModel version in
 * another directory.
 *
 * @author Matt Easterday
 */
class BatchConvert {

    /**
     * Copies all files under srcDir to dstDir. If dstDir does not exist, it
     * will be created.
     */
    public static void copyDirectory(File srcDir, File dstDir) {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }

            String[] children = srcDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".xml"));
                }
            });
            for (String aChildren : children) {
                updateFile(
                        new File(srcDir, aChildren),
                        new File(dstDir, aChildren));
            }
        }
    }

    private static void updateFile(File oldExercise, File newExercise) {
        Exercise ex = ExerciseFileReader.read(oldExercise);
        ExerciseFileWriter.write(ex, newExercise);
    }
}
