package edu.cmu.causalityApp.exerciseBuilder;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * This is the file filter used to save or open exercise files.
 *
 * @author mattheweasterday
 */
public class XmlFileFilter extends FileFilter {

    //

    /**
     * Accept all directories and all gif, jpg, tiff, or png files.
     *
     * @param f <code>File</code>
     * @return true if the file's extension is xml.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        return extension != null && extension.equals("xml");
    }

    /**
     * @return the description of this filter. "Just xml files".
     */
    public String getDescription() {
        return "just xml files";
    }


    /*
    * Get the extension of a file.
    */
    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

}

