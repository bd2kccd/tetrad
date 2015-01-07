package edu.cmu.causalityApp.component;

import java.awt.*;

/**
 * This class represents an edge in the process of being drawn.  Instead of
 * only going from a VarView to a VarView, it can alternatively go from a
 * VarView to a point.
 */
public class DrawEdgeView extends EdgeView {
    private Pos toPoint;

    /**
     * @param from The VarView from which the edge originates
     * @throws IllegalArgumentException if from is null
     */
    public DrawEdgeView(VarView from) {
        if (from == null)
            throw new IllegalArgumentException("DrawEdgeView must be from a non-null VarView");
        this.from = from;
        toPoint = null;
    }

    /**
     * Sets the variable to which the edge points
     *
     * @param to The new variable
     */
    public void setTo(VarView to) {
        this.to = to;
    }

    /**
     * Sets the position to which the edge points
     *
     * @param toPoint The new position
     */
    public void setToPoint(Pos toPoint) {
        this.toPoint = toPoint;
    }

    /**
     * Returns the from and to positions between which the edge will draw
     */
    protected PosPair getPoints() {
        return calculateEdge(getShapeFrom());
    }

    private PosPair calculateEdge(Shape s1) {
        // first, determine the two rectangle centers
        Rectangle r1 = s1.getBounds();
        int x1 = (r1.x + r1.width / 2);
        int y1 = (r1.y + r1.height / 2);
        int x2, y2;
        if (toPoint == null) {
            x2 = x1;
            y2 = y1;
        } else {
            x2 = toPoint.getX();
            y2 = toPoint.getY();
        }

        double a = x2 - x1;
        double b = y1 - y2;
        double theta = Math.atan2(b, a);

        // Adjust centers, such as when we have arrows in both directions
        final int offsetSize = 6;
        final double dx = Math.cos(theta) * offsetSize * getOffset();
        final double dy = Math.sin(theta) * offsetSize * getOffset();
        x1 += dy;
        x2 += dy;
        y1 += dx;
        y2 += dx;

        // figure out the "from side" point
        Point pFrom = new Point(x1, y1);
        Point pTo = new Point(x2, y2);
        Point pMid = null;

        while (pFrom.distance(pTo) >= 2 || pMid == null) {
            pMid = new Point((pFrom.x + pTo.x) / 2, (pFrom.y + pTo.y) / 2);

            if (s1.contains((double) pMid.x, (double) pMid.y))
                pFrom = pMid;
            else
                pTo = pMid;
        }
        Pos p1 = new Pos(pMid);

        // now figure out the "to side" point
        pFrom = new Point(x1, y1);
        pTo = new Point(x2, y2);
        pMid = null;

        while (pFrom.distance(pTo) >= 2 || pMid == null) {
            pMid = new Point((pFrom.x + pTo.x) / 2, (pFrom.y + pTo.y) / 2);


            /*if (s2.contains((double) pMid.x, (double) pMid.y))
            pTo = pMid;
            else*/
            pFrom = pMid;
        }

        Pos p2 = new Pos(pMid);
        return new PosPair(p1, p2);
    }

    /**
     * Paint the edge
     *
     * @param g the graphics with which to paint
     */
    public void paintComponent(Graphics2D g) {
        if (to == null) arrowShapes.refreshShapes(getPoints());
        else arrowShapes.refreshShapes(super.getPoints());

        if (getPoints().getLength() < 4) return;

        float strokeWidth = (isSelected() ? 3.0f : 1.5f);

        // draw thicker lines if we are drawing at a small scale to ensure the arrow is visible
        if (g.getTransform().getScaleX() < 0.25) strokeWidth *= 2;

        Stroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        g.setStroke(stroke);
        g.setColor(Color.black);

        arrowShapes.drawArrowBody(g);
        arrowShapes.drawArrowHead(g);
    }

    /**
     * Returns whether or not the edge can be created.  This is true if and only if
     * the from and to variables are different and non-null.
     */
    public boolean creatable() {
        return (from != to) && (to != null);
    }
}
