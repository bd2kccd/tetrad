package edu.cmu.causalityApp.graphEditors;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.component.*;
import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * To change this template use Options | File Templates.
 *
 * @author greg
 */
 public abstract class GraphView extends JComponent {
    private static final int INSET = 4;

    protected GraphEditor parent;
    protected ViewComponent selectedView;  // Tries to implement object orientation?
    protected CausalityLabModel model;
    protected List<ViewComponent> views = new ArrayList<ViewComponent>();
    protected List<ViewComponent> varViews = new ArrayList<ViewComponent>();
    protected List<ViewComponent> edgeViews = new ArrayList<ViewComponent>();
    protected DrawEdgeView drawEdge;
    private boolean drawing;
    protected static final int PREFERED_WIDTH = 300;
    protected static final int PREFERED_HEIGHT = 300;
    protected boolean hidden;
    protected HiddenImage hiddenGraph;

    /**
     * Constructor.
     */
    public GraphView(GraphEditor parent, CausalityLabModel model) {
        this.model = model;
        this.parent = parent;
        selectedView = null;
        drawing = false;
        hidden = false;
    }

    /**
     * @return the preferred dimensions.
     */
    public Dimension getPreferredSize() {
        return calcMinDimension();
    }

    /**
     * @return the minimum dimensions.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * @return the maximum dimensions.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * @return the position of this variable in the hypothetical graph.
     */
    public Pos getHypPos(String name, String varName) {
        return new Pos(
                model.getHypotheticalGraphVariableCenterX(name, varName),
                model.getHypotheticalGraphVariableCenterY(name, varName));
    }

    /**
     * Sets the position of this variable in the hypothetical graph.
     */
    public void setHypPos(String name, String varName, Pos pos) {
        model.setHypotheticalGraphVariableCenter(name, varName, pos.getX(), pos.getY());
    }

    /**
     * Creates an image of this component at size * ratio size, i.e. if ratio is 0.5, then the method
     * creates an image half the size of the component
     *
     * @return the image icon for this component.
     */

    public ImageIcon getThumbnailImage(double ratio) {

        Dimension size = getSize();
        if (size.width == 0.0) {
            size = new Dimension(300, 300);
        }
        //the size of the white & image
        final int imageWidth = (int) (size.width * ratio) + INSET;
        final int imageHeight = (int) (size.height * ratio) + INSET;

        BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.setBackground(Color.white);
        g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
        //g.clearRect(0, 0, 10, 10);
        //paintBorder (g, 0, 0, imageWidth-1, imageHeight-1, borderColor);

        g.translate(INSET / 2, INSET / 2);
        g.scale(ratio, ratio);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        /** Paint */
        paintComponent(g);

        //return bi;
        return new ImageIcon(bi);
    }

    /**
     * Renders the view.
     */
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        paintVars(g);
        paintEdges(g);
        if (drawing) drawEdge.paintComponent(g);
        if (hidden) paintHiddenGraph(g);
    }

    /**
     * Draws the hidden version of this graph.
     */
    protected void paintHiddenGraph(Graphics2D g) {
        hiddenGraph.paintComponent(g);
    }

    /**
     * Draws the variables.
     */
    protected void paintVars(Graphics2D g) {
        for (ViewComponent varView : varViews) {
            VarView view = (VarView) varView;
            view.paintComponent(g);
        }
    }

    /**
     * Draws the edges.
     */
    protected void paintEdges(Graphics2D g) {
        for (ViewComponent edgeView : edgeViews) {
            EdgeView view = (EdgeView) edgeView;
            view.paintComponent(g);
        }
    }

    /**
     * Lays out the vars
     *
     * @param vars   A list containing variable names
     * @param varMap A HashMap
     */
    protected void setPositions(List vars, HashMap varMap) {
        setPositionsCirc(vars, varMap);  // use this line for circle layout
        //setPositionsCol (vars, varMap);   // use this line for column layout
    }

    /**
     * Lays out the vars in a circle
     *
     * @param vars   A list containing variable names
     * @param varMap A HashMap
     */
    protected void setPositionsCirc(List vars, HashMap varMap) {
        int undefined = countUndefined(vars, varMap);
        if (undefined == 0) return;

        int currentUndefined = undefined;
        for (Iterator i = vars.iterator(); i.hasNext() && currentUndefined > 0; ) {
            String varName = (String) i.next();
            VarView varView = (VarView) varMap.get(varName);
            if (varView != null && varView.getPos().isUndefined()) {

                int w = (GraphView.PREFERED_WIDTH - getVarWidth(varView));
                int x = w / 2;
                int h = (GraphView.PREFERED_HEIGHT - getVarHeight(varView));
                int y = h / 2;

                double angle, ratio;
                if (undefined == 1) {
                    // Give it a small random offset so new variables
                    //  don't appear exactly over each other
                    angle = 2 * Math.PI * Math.random();
                    ratio = 0.1;
                } else {
                    // Lay the vars in a "circle"
                    angle = 2 * Math.PI / undefined * currentUndefined;
                    ratio = 0.90;
                }
                x += (int) (w * (ratio * Math.sin(angle)) / 2);
                y -= (int) (h * (ratio * Math.cos(angle)) / 2);

                varView.setFinalPos(x, y);
                setLabFramePos(varName, new Pos(x, y));

                currentUndefined--;
            }
        }
    }

    protected Dimension calcMinDimension() {
        int height = 250;
        int width = 250;

        for (ViewComponent varView : varViews) {
            VarView view = (VarView) varView;

            if (view.getPos().getX() > width)
                width = view.getPos().getX();

            if (view.getPos().getY() > height)
                height = view.getPos().getY();
        }

        return new Dimension(width + 80, height + 80);
    }

    /**
     * @return the number of variables that have not been placed by the user.
     */
    protected int countUndefined(List vars, HashMap varMap) {
        int undefined = 0;
        for (Object var : vars) {
            String varName = (String) var;
            VarView varView = (VarView) varMap.get(varName);
            if (varView != null && varView.getPos().isUndefined()) undefined++;
        }
        return undefined;
    }


    /**
     * Returns the height of the variable, or VarView.PREFERRED_HEIGHT if null
     *
     * @return The height of the variable, or VarView.PREFERRED_HEIGHT if null
     */
    protected int getVarWidth(VarView varView) {
        if (varView == null) return VarView.PREFERED_WIDTH;
        return varView.getSize().width;
    }

    /**
     * Returns the height of the variable, or VarView.PREFERRED_HEIGHT if null
     *
     * @param varView The variable whose height is to be determined
     * @return The height of the variable, or VarView.PREFERRED_HEIGHT if null
     */
    protected int getVarHeight(VarView varView) {
        if (varView == null) return VarView.PREFERED_HEIGHT;
        return varView.getSize().height;
    }

    /**
     * Remove selected component.
     */
    public abstract void removeSelected();

    /**
     * Adds an edge between two nodes.
     */
    public abstract void addEdge(String fromName, String toName);

    /**
     * Selects the topmost view containing the point (x, y)
     *
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     */
    public void selectView(int x, int y) {
        ViewComponent curView;
        if (selectedView != null) selectedView.setSelected(false);
        selectedView = null;
        for (ListIterator i = views.listIterator(views.size()); i.hasPrevious(); ) {
            curView = (ViewComponent) i.previous();
            if (curView.contains(new Pos(x, y))) {
                selectedView = curView;
                curView.setSelected(true);
                return;
            }
        }
    }

    /**
     * Inner class to handle the mouse actions on the editor.
     */
    public class GraphInputHandler extends InputHandler {

        /**
         * Move handler
         */
        private GraphMoveHandler graphMoveHandler = new GraphMoveHandler();
        /**
         * Draw handler
         */
        private GraphDrawHandler graphDrawHandler = new GraphDrawHandler();
        /**
         * Null handler
         */
        private InputHandler graphNullHandler = new InputHandler();

        /**
         * Returns a handler for mouse and keyboard events depending on which mode
         * the window is in (draw, move, or neither)
         *
         * @return The appropriate handler
         */
        private InputHandler getMouseHandler() {

            // Draw edges mode
            if (parent.getEditMode().compareTo(GraphEditor.EDIT_DRAW) == 0) {
                return graphDrawHandler;
            }

            // Move variables mode
            else if (parent.getEditMode().compareTo(GraphEditor.EDIT_MOVE) == 0) {
                return graphMoveHandler;
            }
            return graphNullHandler;
        }

        private InputHandler getKeyHandler() {
            // Draw edges mode
            if (parent.getEditMode().compareTo(GraphEditor.EDIT_DRAW) == 0) {
                return graphDrawHandler;
            }

            // Move variables mode
            else if (parent.getEditMode().compareTo(GraphEditor.EDIT_MOVE) == 0) {
                return graphMoveHandler;
            }
            return graphNullHandler;
        }

        // These methods pass the corresponding event to the appropriate handler
        public void mouseEntered(int x, int y, int ex) {
            getMouseHandler().mouseEntered(x, y, ex);
        }

        public void mouseExited(int x, int y, int ex) {
            getMouseHandler().mouseExited(x, y, ex);
        }

        public void mousePressed(int x, int y, int ex) {
            getMouseHandler().mousePressed(x, y, ex);
        }

        public void mouseReleased(int x, int y, int ex) {
            getMouseHandler().mouseReleased(x, y, ex);
        }

        public void mouseMoved(int x, int y, int ex) {
            getMouseHandler().mouseMoved(x, y, ex);
        }

        public void mouseDragged(int x, int y, int ex) {
            getMouseHandler().mouseDragged(x, y, ex);
        }

        public void mouseClicked(int x, int y, int ex, int cc) {
            getMouseHandler().mouseClicked(x, y, ex, cc);
        }

        public void keyPressed(char keyChar, int keyCode, int ex) {
            getKeyHandler().keyPressed(keyChar, keyCode, ex);
        }

        public void keyReleased(char keyChar, int keyCode, int ex) {
            getKeyHandler().keyReleased(keyChar, keyCode, ex);
        }

        public void keyTyped(int keyCode) {
            getKeyHandler().keyTyped(keyCode);
        }

        private class GraphMoveHandler extends InputHandler {
            private Pos mouseOffset;

            /**
             * Selects the view under the cursor
             *
             * @param x  The x-coordinate of the cursor
             * @param y  The y-coordinate of the cursor
             * @param ex The modifier mask
             */
            public void mousePressed(int x, int y, int ex) {
                selectView(x, y);
                if (selectedView instanceof VarView) {
                    Pos offset = ((VarView) selectedView).getPos();
                    mouseOffset = new Pos(x - offset.getX(), y - offset.getY());
                }
                repaint();
            }

            /**
             * Moves the selected view if it is a variable
             *
             * @param x  The x-coordinate
             * @param y  The y-coordinate
             * @param ex The modifier mask
             */
            public void mouseDragged(int x, int y, int ex) {
                if (!(selectedView instanceof VarView)) return;
                Pos newPos = calculateNewPos(x, y, (VarView) selectedView);
                ((VarView) selectedView).setTempPos(newPos);
                repaint();
            }

            /**
             * Calculates a new position for the variable, restraining it to the bounds of the window
             *
             * @param x       The x-coordinate of the cursor
             * @param y       The y-coordinate of the cursor
             * @param varView The variable to calculate the position of
             * @return The new position
             */
            protected Pos calculateNewPos(int x, int y, VarView varView) {
                x -= mouseOffset.getX();
                y -= mouseOffset.getY();
                /** Check boundaries */
                Dimension size = varView.getSize();
                x = Math.max(32, Math.min(x, getSize().width - size.width - 1));
                y = Math.max(0, Math.min(y, getSize().height - size.height - 1));
                return new Pos(x, y);
            }

            /**
             * Sets the final position for the selected variable
             *
             * @param x  The x-coordinate of the cursor
             * @param y  The y-coordinate of the cursor
             * @param ex The modifier mask
             */
            public void mouseReleased(int x, int y, int ex) {
                if (!(selectedView instanceof VarView)) return;
                Pos newPos = calculateNewPos(x, y, (VarView) selectedView);
                ((VarView) selectedView).setFinalPos(newPos);
                setLabFramePos(((VarView) selectedView).getName(), newPos);
                repaint();
            }

            /**
             * Activated when a key is pressed.
             */
            public void keyPressed(char keyChar, int keyCode, int modifiersEx) {
                System.out.println("Pressed " + keyCode);
            }

            /**
             * Activated when a key is typed. Used for the "Delete" key.
             */
            public void keyTyped(int keyCode) {
                System.out.println("Pressed " + keyCode);
                if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
                    removeSelected();
                }
            }
        }

        /**
         * Inner class to handle the mouse graph drawing capabilities.
         */
        private class   GraphDrawHandler extends InputHandler {

            /**
             * If the cursor is over a variable, begins drawing a new edge
             *
             * @param x  The x-coordinate of the cursor
             * @param y  The y-coordinate of the cursor
             * @param ex The modifier mask
             */
            public void mousePressed(int x, int y, int ex) {
                selectView(x, y);
                if (selectedView instanceof VarView) {
                    drawEdge = new DrawEdgeView((VarView) selectedView);
                    drawing = true;
                } else if (selectedView instanceof EdgeView) {
                    parent.setEditMode(GraphEditor.EDIT_MOVE);
                }
                repaint();
            }

            /**
             * If the cursor is over a variable, sets drawing edge to point to new variable.
             * Otherwise, sets drawing edge to point to cursor position.
             *
             * @param x  The x-coordinate of the cursor
             * @param y  The y-coordinate of the cursor
             * @param ex The modifier mask
             */
            public void mouseDragged(int x, int y, int ex) {
                selectView(x, y);
                if (selectedView instanceof VarView) {
                    drawEdge.setTo((VarView) selectedView);
                } else {
                    if (selectedView != null) {
                        selectedView.setSelected(false);
                        selectedView = null;
                    }
                    drawEdge.setTo(null);
                    drawEdge.setToPoint(new Pos(x, y));
                }
                repaint();
            }

            /**
             * If the drawing edge is creatable, adds the edge to the model and deselects all views
             *
             * @param x  The x-coordinate of the cursor
             * @param y  The y-coordinate of the cursor
             * @param ex The modifier mask
             */
            public void mouseReleased(int x, int y, int ex) {
                drawing = false;
                if (drawEdge != null && drawEdge.creatable())
                    parent.addEdge(drawEdge.getFromName(), drawEdge.getToName());
                repaint();
                if (selectedView != null) {
                    selectedView.setSelected(false);
                    selectedView = null;
                }
            }
        }
    }

    /**
     * Sets the position of the variable in the view.
     */
    abstract protected void setLabFramePos(String varName, Pos pos);


    /**
     * Inner class to render the hidden version of this view.
     */
    protected class HiddenImage extends JComponent {
        private ImageIcon image;

        public HiddenImage(String hiddenImageFile) {
            image = new ImageIcon(ImageUtils.getImage(this, hiddenImageFile));
        }

        public void paintComponent(Graphics2D g) {
            g.drawImage(image.getImage(), 10, 10, null);
        }
    }
}
