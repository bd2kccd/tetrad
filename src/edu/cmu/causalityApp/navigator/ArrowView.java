package edu.cmu.causalityApp.navigator;


import edu.cmu.causalityApp.component.Pos;
import edu.cmu.causalityApp.component.PosPair;
import edu.cmu.causalityApp.util.Misc;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;

/**
 * To change this template use Options | File Templates.
 * <p/>
 * This class describes the arrow view in the navigator panel.
 *
 * @author mattheweasterday
 */
class ArrowView {
    private boolean selected = false;
    private final Component from;
    private final Component to;

    /**
     * Constructor to create an arrow between two components.
     *
     * @param from start component.
     * @param to   end component.
     */
    public ArrowView(Component from, Component to) {
        this.from = from;
        this.to = to;
    }

    /**
     * @return The shape where the arrow begins.
     */
    Shape getShapeFrom() {
        return from.getBounds();
    }

    /**
     * @return The shape where the arrow ends.
     */
    Shape getShapeTo() {
        return to.getBounds();
    }

    /**
     * @return The pair of endpoints for this arrow.
     */
    PosPair getPoints() {
        return calculateEdge(getShapeFrom(), getShapeTo());
    }

    /**
     * Indicates if the arrow should be offset from the center of the to and from shapes.
     * It can be used, for example, to showLab distinct arrows when going in both directions.
     *
     * @return The multiplier for the offset. 0 if none, 1 if standard.
     */
    int getOffset() {
        return 0;
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
        int x1 = r1.x + r1.width / 2;
        int y1 = r1.y + r1.height / 2;
        Rectangle r2 = s2.getBounds();
        int x2 = r2.x + r2.width / 2;
        int y2 = r2.y + r2.height / 2;

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
     * @param g The Graphics context in which to draw the WorkbenchEdge
     */
    public void paint(Graphics2D g) {
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
    }

    /**
     * @return The color of the arrow
     */
    Color getColor() {
        return Color.LIGHT_GRAY;
    }


    private ArrowView.ArrowShapes arrowShapes = new ArrowView.ArrowShapes();

    /**
     * @param arrowShapes An object to customize the handling of shape creation and drawing.
     */
    public void setArrowShapes(ArrowView.ArrowShapes arrowShapes) {
        this.arrowShapes = arrowShapes;
    }

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

        private void drawArrowBody(Graphics2D g) {
            g.draw(arrowBody);
        }
    }

    /**
     * @return if this arrow is selected.
     */
    boolean isSelected() {
        return selected;
    }

    /**
     * Selects or unselects this arrow.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}