package edu.cmu.causalityApp.component;

import edu.cmu.causality.manipulatedGraph.EdgeInfo;
import edu.cmu.causality.manipulatedGraph.ManipulatedEdgeType;
import edu.cmu.causalityApp.util.Misc;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;


/**
 * Visualizes a directed Edge between two variables.
 */
public class EdgeView implements ViewComponent {
    VarView from;
    VarView to;

    private static final Color FROZEN_ICON_COLOR = Color.cyan.darker();
    private static final Color BROKEN_ICON_COLOR = Color.red;
    private static final Color HIGHLIGHT_COLOR = Color.red;
    private static final Color BROKEN_COLOR = Color.lightGray;
    private static final Color FROZEN_COLOR = Color.gray;
    private static final Color NORMAL_COLOR = Color.black;
    private static final Stroke ICON_STROKE = new BasicStroke(2f);

    private boolean selected = false;
    private EdgeInfo info;

    /**
     * Called only from a subclass as the default constructor
     */
    EdgeView() {
    }

    /**
     * @param from The VarView from which the EdgeView originates
     * @param to   The VarView at which the EdgeView terminates
     * @param info The EdgeInfo representing the edge
     * @throws NullPointerException If from or to is null
     * @throws RuntimeException     If from == to
     */
    public EdgeView(VarView from, VarView to, EdgeInfo info) {
        if (from == to) throw new RuntimeException("Direct Cycle: From == To");
        this.from = from;
        this.to = to;
        this.info = info;
    }

    /**
     * Returns whether the edge is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets whether the edge is selected
     *
     * @param selected The new selected value
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the offset of the edgeview.
     */
    int getOffset() {
        return 0;
    }

    /**
     * Draw an X or * for a broken or frozen edge
     */
    void paintEffectIcon(Graphics2D g) {
        Pos mid = getCenter();
        int x = mid.getX();
        int y = mid.getY();
        PosPair pp = getPoints();
        final double length = 8;
        final int dx = (int) (pp.getDx() * length);
        final int dy = (int) (pp.getDy() * length);

        /** Offset the icon so bidirectional edge icons don't collide */
        x -= dx;
        y += dy;

        g.setStroke(ICON_STROKE);
        if (info.getType() == ManipulatedEdgeType.BROKEN) {
            g.setColor(BROKEN_ICON_COLOR);
            if (!from.isVisible() || !to.isVisible())
                g.setColor(BROKEN_ICON_COLOR.brighter());
        } else {
            g.setColor(FROZEN_ICON_COLOR);
            if (!from.isVisible() || !to.isVisible())
                g.setColor(FROZEN_ICON_COLOR.darker());
            g.drawLine(x + dy, y + dx, x - dy, y - dx);
            g.drawLine(x + dx, y - dy, x - dx, y + dy);
        }
        g.drawLine(x + dx + dy, y + dx - dy, x - dy - dx, y - dx + dy);
        g.drawLine(x + dx - dy, y - dx - dy, x - dx + dy, y + dx + dy);
    }

    /**
     * Returns the color of the edge.  This is HIGHLIGHT_COLOR if the edge is selected,
     * BROKEN_COLOR if either from or to is ignored, or if the edge is broken, FROZEN_COLOR
     * if the edge is frozen, or NORMAL_COLOR otherwise.
     */
    Color getColor() {
        if (selected) return HIGHLIGHT_COLOR;
        if (!from.isVisible() || !to.isVisible()) return BROKEN_COLOR;
        if (info.getType() == ManipulatedEdgeType.NORMAL)
            return NORMAL_COLOR;
        else if (info.getType() == ManipulatedEdgeType.BROKEN)
            return BROKEN_COLOR;
        else if (info.getType() == ManipulatedEdgeType.FROZEN)
            return FROZEN_COLOR;
        //Assert.assertShouldNotBeHere();
        return null;
    }

    Shape getShapeFrom() {
        return from.getShape();
    }

    Shape getShapeTo() {
        return to.getShape();
    }

    public String getFromName() {
        return from.getName();
    }

    public String getToName() {
        return to.getName();
    }

    public VarView getFrom() {
        return from;
    }

    public VarView getTo() {
        return to;
    }

    /**
     * Determines whether the given point is contained in the EdgeView
     *
     * @param point The point to check
     */
    public boolean contains(Pos point) {
        return arrowShapes.arrowShape != null && arrowShapes.arrowShape.contains(point.getPoint());
    }

    /**
     * @return The centerpoint of the arrow.
     */
    Pos getCenter() {
        PosPair points = getPoints();
        final int x = (points.getP1().getX() + points.getP2().getX()) / 2;
        final int y = (points.getP1().getY() + points.getP2().getY()) / 2;
        return new Pos(x, y);
    }

    /**
     * @return The pair of endpoints for this arrow.
     */
    PosPair getPoints() {
        return calculateEdge(getShapeFrom(), getShapeTo());
    }

    /**
     * Calculate the edge which goes from the side of one Shape to the
     * side of another non-overlapping Shape along a line which
     * connects their center points.
     * <p/>
     * By David Danks, Joe Ramsey, and Frank Wimberly.
     *
     * @param s1 The "from" Shape.
     * @param s2 The "to" Shape.
     * @return The PointPair for the Edge
     */
    PosPair calculateEdge(Shape s1, Shape s2) {
        //PointPair edge = new PointPair();

        // first, determine the two rectangle centers
        Rectangle r1 = s1.getBounds();
        int x1 = (r1.x + r1.width / 2);
        int y1 = (r1.y + r1.height / 2);
        Rectangle r2 = s2.getBounds();
        int x2 = (r2.x + r2.width / 2);
        int y2 = (r2.y + r2.height / 2);

        double a = x2 - x1;
        double b = y1 - y2;
        double theta = Math.atan2(b, a);

        /** Adjust centers, such as when we have arrows in both directions */
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

            if (s2.contains((double) pMid.x, (double) pMid.y))
                pTo = pMid;
            else
                pFrom = pMid;
        }
        Pos p2 = new Pos(pMid);
        return new PosPair(p1, p2);
    }

    /**
     * Draw the arrow
     * <p/>
     * Also by David Danks, Joe Ramsey, and Frank Wimberly.
     *
     * @param g The Graphics context in which to draw the edge
     */
    public void paintComponent(Graphics2D g) {
        arrowShapes.refreshShapes(getPoints());

        if (getPoints().getLength() < 4) return;

        float strokeWidth = (isSelected() ? 3.0f : 1.5f);

        /** draw thicker lines if we are drawing at a small scale to ensure the arrow is visible */
        if (g.getTransform().getScaleX() < 0.25) strokeWidth *= 2;

        Stroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        g.setStroke(stroke);
        g.setColor(getColor());

        arrowShapes.drawArrowBody(g);
        arrowShapes.drawArrowHead(g);
        if (info.getType() != ManipulatedEdgeType.NORMAL) {
            paintEffectIcon(g);
        }
    }

    ArrowShapes arrowShapes = new ArrowShapes();

    /**
     * Handles shape creation and drawing.
     */
    public static class ArrowShapes {
        public Shape arrowBody, arrowHead, arrowShape;
        double bodyWidth = 4;
        int headHeight = 16;
        int headWidth = 10;

        public ArrowShapes() {
        }

        void refreshShapes(PosPair pp) {
            final Pos p1 = pp.getP1(), p2 = pp.getP2();
            int x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();

            // ...and the arrowhead
            double theta = pp.getAngle();
            final double dx = Math.cos(theta);
            final double dy = Math.sin(theta);

            final double x2noArrow = x2 - dx * headHeight;
            final double y2noArrow = y2 + dy * headHeight;
            final double dyArrow = dy * headWidth / 2;
            final double dxArrow = dx * headWidth / 2;

            final double[] xHeadPoints = {x2, x2noArrow + dyArrow, x2noArrow - dyArrow};
            final double[] yHeadPoints = {y2, y2noArrow + dxArrow, y2noArrow - dxArrow};

            final double dxBody = dx * bodyWidth, dyBody = dy * bodyWidth;
            final double[] xBodyPoints = {x1 - dyBody, x1 + dyBody, x2 + dyBody, x2 - dyBody};
            final double[] yBodyPoints = {y1 - dxBody, y1 + dxBody, y2 + dxBody, y2 - dxBody};

            arrowBody = new Line2D.Double(x1, y1, x2noArrow + dx * 2, y2noArrow - dy * 2);
            arrowHead = Misc.createPolygon(xHeadPoints, yHeadPoints);
            Shape thickBody = Misc.createPolygon(xBodyPoints, yBodyPoints);
            Area arrowArea = new Area(arrowHead);
            arrowArea.add(new Area(thickBody));
            arrowShape = arrowArea;
        }

        void drawArrowHead(Graphics2D g) {
            g.fill(arrowHead);
        }

        void drawArrowBody(Graphics2D g) {
            g.draw(arrowBody);
        }
    }
}
