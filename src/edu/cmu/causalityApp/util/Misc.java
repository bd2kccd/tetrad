/**
 * Class Misc
 * Created: Mar 19, 2002
 * @author juan
 */

package edu.cmu.causalityApp.util;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * This class describes many miscellaneous functions used by the lab.
 */
public class Misc {

    /**
     * Defines a light aqua color.
     */
    public static final Color lightAqua = new Color(220, 233, 233);

    /**
     * An alternative to java.awt.Polygon where the coordinates are double instead of int
     *
     * @param xpoints Ordered array of x-coordinates
     * @param ypoints Ordered array of y-coordinates
     * @return A closed polygon shape
     * @throws java.lang.IndexOutOfBoundsException
     *          if array is empty
     */
    public static Shape createPolygon(double[] xpoints, double[] ypoints) {
        GeneralPath path = createPolylinePath(xpoints, ypoints);
        path.closePath();
        return path;
    }

    /**
     * @param xpoints Ordered array of x-coordinates
     * @param ypoints Ordered array of y-coordinates
     * @return A closed polygon shape
     * @throws java.lang.IndexOutOfBoundsException
     *          if array is empty
     */
    public static Shape createPolyline(double[] xpoints, double[] ypoints) {
        return createPolylinePath(xpoints, ypoints);
    }

    private static GeneralPath createPolylinePath(double[] xpoints, double[] ypoints) {
        GeneralPath path = new GeneralPath();
        path.moveTo((float) xpoints[0], (float) ypoints[0]);
        for (int i = 1; i < xpoints.length; i++) {
            path.lineTo((float) xpoints[i], (float) ypoints[i]);
        }
        return path;
    }

    /**
     * Turns on anti-aliasing for this <code>Graphics</code>.
     */
    public static void antialiasOn(Graphics g) {
        if (g instanceof Graphics2D) {
            antialiasOn((Graphics2D) g);
        }
    }

    /**
     * Turns on anti-aliasing for this <code>Graphics2D</code>.
     */
    public static void antialiasOn(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
