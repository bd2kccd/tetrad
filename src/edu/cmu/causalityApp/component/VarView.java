package edu.cmu.causalityApp.component;

import edu.cmu.causality.experimentalSetup.manipulation.Locked;
import edu.cmu.causality.experimentalSetup.manipulation.Manipulation;
import edu.cmu.causality.experimentalSetup.manipulation.None;
import edu.cmu.causality.experimentalSetup.manipulation.Randomized;
import edu.cmu.causalityApp.util.ImageUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * This class is the visual representation of a variable
 */
public class VarView implements ViewComponent {

    static private Image RANDOMIZED_IMAGE;
    static private Image LOCKED_IMAGE;
    static private Image IGNORED_IMAGE;

    // a boolean flag to indicate if this varview object is made to be displayed in the
    private boolean experimentSetupWizardFlag;

    private double mean,
            stdDev;

    private boolean visible = true;
    private Pos pos = new Pos();

    /**
     * Immutable name
     */
    private String name = "";

    /**
     * Immutable latent
     */
    private boolean latent = false;
    private Manipulation intervention;

    /**
     * Preferred width of the variable view.
     */
    public static final int PREFERED_WIDTH = 50;

    /**
     * Preferred height of the variable view.
     */
    public static final int PREFERED_HEIGHT = 26;

    private final Dimension size = new Dimension(PREFERED_WIDTH, PREFERED_HEIGHT);
    private Shape shape;
    private Font font;
    private boolean calculatedSizeOnPaint = false;

    private boolean selected = false;

    /**
     * Color scheme of the CSR course.
     */
    private static final Color BACKGROUND = new Color(204, 204, 255);
    private static final Color HIGHLIGHT_BACKGROUND = new Color(255, 204, 102);
    private static final Color BORDER = new Color(153, 153, 204);

    /**
     * Creates a new studied VarView with no manipulations and the given name and latency.
     *
     * @param name   The name of the variable.
     * @param latent Whether the variable is latent.
     */
    public VarView(String name, boolean latent) {
        this(name, latent, new None(), true);
    }

    /**
     * Creates a new VarView from the given values.
     *
     * @param name         The name of the variable.
     * @param latent       Whether the variable is latent.
     * @param intervention What intervention is applied to the variable.
     * @param studied      Whether the variable is studied.
     */
    public VarView(String name, boolean latent, Manipulation intervention, boolean studied) {
        assert (name != null);
        assert (name.length() > 0);

        this.name = name;
        this.latent = latent;
        this.intervention = intervention;
        this.visible = studied;
        this.experimentSetupWizardFlag = false;

        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();
        calculateSize(g);
    }


    /**
     * Creates a new VarView from the given values.
     *
     * @param name         The name of the variable.
     * @param latent       Whether the variable is latent.
     * @param intervention What intervention is applied to the variable.
     * @param studied      Whether the variable is studied.
     */
    private VarView(String name, boolean latent, Manipulation intervention, boolean studied, double mean, double stdDev, boolean experimentSetupWizardFlag) {
        assert (name != null);
        assert (name.length() > 0);

        this.name = name;
        this.latent = latent;
        this.intervention = intervention;
        this.visible = studied;
        this.mean = mean;
        this.stdDev = stdDev;
        this.experimentSetupWizardFlag = experimentSetupWizardFlag;

        Graphics g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();
        calculateSize(g);
    }

    public VarView(String name, boolean latent, Manipulation intervention, boolean studied, double mean, double stdDev) {
        this(name, latent, intervention, studied, mean, stdDev, false);
    }

    /**
     * Returns whether the variable represented by this VarView is latent.
     */
    public boolean isLatent() {
        return latent;
    }

    /**
     * Returns the name of the variable represented by this VarView.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the image of a randomized intervention - a die.
     */
    Image getRandomizedImage() {
        if (RANDOMIZED_IMAGE == null) {
            RANDOMIZED_IMAGE = ImageUtils.getImage(this, "random_new2.gif");
        }
        return RANDOMIZED_IMAGE;
    }

    /**
     * @return the image of a locked intervention - a lock.
     */
    Image getLockedImage() {
        if (LOCKED_IMAGE == null) {
            LOCKED_IMAGE = ImageUtils.getImage(this, "lock_new2.gif");
        }
        return LOCKED_IMAGE;
    }

    /**
     * @return the image indicating a variable is ignored in an experiment.
     */
    Image getIgnoredImage() {
        if (IGNORED_IMAGE == null) {
            IGNORED_IMAGE = ImageUtils.getImage(this, "ignored_gray.gif");
        }
        return IGNORED_IMAGE;
    }

////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Mutable Properties ////////////////////////////////////

    /**
     * Returns the visibility of this var (if it's in or out of an experiment).
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return the Pos coordinates of this variable view.
     */
    public Pos getPos() {
        return pos;
    }

    /**
     * Sets a temporary (feedback) position of this Var. Fires a VarMoveEvent.
     */
    public void setTempPos(Pos pos) {
        if (pos.equals(this.pos)) return;
        this.pos = pos;
    }

    /**
     * Sets the final (non-feedback) position of this Var. Fires a VarMoveDoneEvent
     */
    public void setFinalPos(int x, int y) {
        setFinalPos(new Pos(x, y));
    }

    /**
     * Sets the final (non-feedback) position of this Var. Fires a VarMoveDoneEvent
     */
    public void setFinalPos(Pos pos) {
        if (pos == null) {
            throw new IllegalArgumentException("pos is null");
        }
        if (!pos.equals(this.pos) || !pos.equals(this.lastFinalPos)) {
            this.pos = pos;
            lastFinalPos = pos;
        }
    }

    private Pos lastFinalPos;

    /**
     * Returns the name of the variable represented by this VarView.
     */
    public String toString() {
        return getName();
    }

    /**
     * Determines whether the given point is contained in this VarView.
     *
     * @param point The point to check.
     */
    public boolean contains(Pos point) {
        int x = point.getX() - pos.getX();
        boolean inXrange = (x >= 0) && (x <= getSize().width);
        int y = point.getY() - pos.getY();
        boolean inYrange = (y >= 0) && (y <= getSize().height);
        return inXrange && inYrange;
    }


    public void setFlag(boolean newFlag) {
        experimentSetupWizardFlag = newFlag;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    /**
     * @return the dimensions of this VarView.
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * @return the shape of this VarView.
     */
    public Shape getShape() {
        if (latent) {
            shape = new Ellipse2D.Float(pos.getX(), pos.getY(), size.width, size.height);
        } else {
            shape = new Rectangle(pos.getPoint(), getSize());
        }
        return shape;
    }

    /**
     * Renders this view.
     */
    public void paintComponent(Graphics2D g) {
        if (!calculatedSizeOnPaint) {
            calculateSize(g);
            calculatedSizeOnPaint = true;
        }

        int textWidth;

        /** Draw box */
        shape = null;
        g.setColor(getShapeColor());
        g.fill(getShape());
        g.setColor(BORDER);
        Stroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        g.setStroke(stroke);
        g.draw(shape);

        /** Draw var name */
        g.setColor(getTextColor());
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        textWidth = metrics.stringWidth(name);
        int textHeight = metrics.getAscent() - metrics.getDescent();
        int x = pos.getX() + (size.width - textWidth) / 2;
        int y = pos.getY() + size.height / 2 + textHeight / 2;
        g.drawString(name, x, y);

        if (!isVisible()) {
            x = (int) (getPos().getX() + getSize().getWidth() / 2 - 40);
            y = (int) (getPos().getY() + getSize().getHeight() / 2 - 22);

            g.drawImage(getIgnoredImage(), x, y, null);
            /*
            g.setColor(Color.gray);
            stroke = new BasicStroke(.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		    g.setStroke(stroke);
            g.draw(new Line2D.Double (pos.getX(), pos.getY(), pos.getX() + getSize().width, pos.getY() + getSize().height));
            */
        }

        paintIntervention(g);
    }

    private void paintIntervention(Graphics2D g) {
        int x = getPos().getX() - 32;
        int y = (int) (getPos().getY() + getSize().getHeight() / 2 - 7);

        if (intervention instanceof Locked) {
            g.drawImage(getLockedImage(), x, y, null);

            /** Paint locked value */
            g.setFont(g.getFont().deriveFont(10f));
            g.drawString(((Locked) intervention).getLockedAtValue(), x, y + 25);
        } else if (intervention instanceof Randomized) {
            g.drawImage(getRandomizedImage(), x, y, null);
            if (experimentSetupWizardFlag) {
                g.setFont(new Font("Arial", Font.PLAIN, 13));
                System.out.println("theName: " + name);
                g.drawString("mean:" + mean, x + 50 + name.length() * 9, y + 2);
                g.drawString("std deviation:" + stdDev, x + 50 + name.length() * 9, y + 15);
            }

        }
    }

    private void calculateSize(Graphics g) {
        /** Find largest font size for text given available space */
        int FONT_SIZE = 16;
        font = new Font("Sanserif", Font.PLAIN, FONT_SIZE);

        FontMetrics metrics = g.getFontMetrics(font);
        size.width = metrics.stringWidth(name) + 5;
    }

    private Color getTextColor() {
        return isVisible() ? Color.black : Color.gray;
    }

    private Color getShapeColor() {
        Color color = (isSelected() ? HIGHLIGHT_BACKGROUND : BACKGROUND);
        if (!isVisible()) color = translucent(color, 100);
        return color;
    }

    private static Color translucent(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * @return if this variable is selected or not.
     */
    private boolean isSelected() {
        return selected;
    }

    /**
     * Select or unselect this variable.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
