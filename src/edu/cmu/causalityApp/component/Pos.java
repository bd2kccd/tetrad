/**
 * Coordinate
 * Date: Mar 8, 2002
 * @author juan
 */

package edu.cmu.causalityApp.component;

import java.awt.*;


/**
 * Immutable point object.
 */
public final class Pos {
    private final int x;
    private final int y;

    /**
     * Creates an "undefined" Pos
     */
    public Pos() {
        this(-1, -1);
    }

    /**
     * Creates a Pos with a defined pair of X and Y coordinates.
     *
     * @param x
     * @param y
     */
    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a Pos with a given Point.
     *
     * @param p
     */
    public Pos(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * @return X coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * @return Y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Checks if the x or y coordinate of this Pos is out of bounds.
     *
     * @return true if so.
     */
    public boolean isUndefined() {
        // should move out?
        return (x < 0 || y < 0);
    }

    /**
     * @return A Point representing this coordinate.
     */
    public Point getPoint() {
        return new Point(x, y);
    }

    /**
     * Compares this Pos to another Pos.
     *
     * @param obj
     * @return true if they are the same.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Pos)) return false;
        Pos pos = (Pos) obj;
        return pos.getX() == x && pos.getY() == y;
    }

    /**
     * @return a unique hashcode for this Pos.
     */
    public int hashCode() {
        return ((17 + x) * 37 + y * 19);
    }

    /**
     * @return a string representation of this Pos.
     */
    public String toString() {
        return "(" + x + ", " + y + ")";   //$NON-NLS-3$
    }


//////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Unit Test /////////////////////////////////////////////

    /**
     * Unit test
     */
    public static class Test extends junit.framework.TestCase {
        public Test(String name) {
            super(name);
        }

        public void test() {
            Pos p1 = new Pos(4, 2);
            Pos p2 = new Pos(4, 2);
            Pos p3 = new Pos(4, 8);
            Pos p4 = new Pos();
            Pos p5 = new Pos(8, 2);
            Pos p6 = new Pos(p1.getPoint());

            assertEquals(p1, p2);
            assertTrue(p1 != p3);
            assertTrue(p1 != p5);
            assertTrue(p3 != p4);
            assertEquals(p1, p6);
            assertEquals(p1.getPoint(), p2.getPoint());
            assertTrue(!p1.getPoint().equals(p3.getPoint()));

            /** Test undefined */
            assertTrue(!p1.isUndefined());
            assertTrue(p4.isUndefined());
        }
    }
}
