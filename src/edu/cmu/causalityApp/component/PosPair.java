package edu.cmu.causalityApp.component;


/**
 * Immutable
 * <p/>
 * This describes a pair of Pos classes.
 *
 * @author juan
 */
public final class PosPair {
    private final Pos p1;
    private final Pos p2;

    /**
     * Constructor. Use this to represent the pair of two points that is connected
     * by an edge.
     */
    public PosPair(Pos p1, Pos p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * @return the first Pos.
     */
    public Pos getP1() {
        return p1;
    }

    /**
     * @return the second Pos.
     */
    public Pos getP2() {
        return p2;
    }

    /**
     * @return the distance between the two points.
     */
    public double getLength() {
        return p2.getPoint().distance(p1.getPoint());
    }

    private static final double UNDEFINED = Double.MAX_VALUE;
    private double theta = UNDEFINED;
    private double dx = UNDEFINED, dy = UNDEFINED;

    /**
     * @return the angle between the two points.
     */
    public double getAngle() {
        if (theta == UNDEFINED) {
            /** "lazy" instantiation */
            double a = p2.getX() - p1.getX();
            double b = p1.getY() - p2.getY();
            theta = Math.atan2(b, a);
        }
        return theta;
    }

    /**
     * @return The unitary change in x.
     */
    public double getDx() {
        if (dx == UNDEFINED) dx = Math.cos(getAngle());
        return dx;
    }

    /**
     * @return The unitary change in y.
     */
    public double getDy() {
        if (dy == UNDEFINED) dy = Math.sin(getAngle());
        return dy;
    }
}
