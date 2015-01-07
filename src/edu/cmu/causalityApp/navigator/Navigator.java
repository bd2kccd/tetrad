package edu.cmu.causalityApp.navigator;

import edu.cmu.causalityApp.component.Pos;
import edu.cmu.causalityApp.component.PosPair;
import edu.cmu.causalityApp.exercise.WindowInclusionStatus;
import edu.cmu.causalityApp.util.ImageUtils;
import edu.cmu.causalityApp.util.Misc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class describes the navigator panel on the left side of the lab.
 *
 * @author mattheweasterday
 */
public class Navigator extends JPanel implements ActionListener {

    /**
     * "Correct graph".
     */
    final public static String COR_GRAPH = "Correct graph";

    /**
     * "Experimental setup".
     */
    final public static String EXP_SETUP = "Experimental setup";

    /**
     * "Hypothetical graph".
     */
    final public static String HYP_GRAPH = "Hypothetical graph";

    /**
     * "Hypothetical manipulated graph".
     */
    final public static String HYP_MANIP = "Hypothetical manipulated graph";

    /**
     * "Correct manipulated graph".
     */
    final public static String COR_MANIP = "Correct manipulated graph";

    /**
     * "Population".
     */
    final public static String POP = "Population";

    /**
     * "Sample".
     */
    final public static String SAMPLE = "Sample";

    /**
     * "Compared independencies".
     */
    final public static String COMPARED = "Compared independencies";

    private final Vector<NavigatorChangeListener> navigatorListeners = new Vector<NavigatorChangeListener>();
    private final List<ArrowView> arrows = new ArrayList<ArrowView>();

    private final JComponent correctGraph;
    private final JComponent corManipGraph;
    private final JComponent population;
    private final JComponent experiment;
    private final JComponent hypGraph;
    private final JComponent hypManipGraph;
    private final JComponent sample;
    private final JComponent compared;

    /**
     * Constructor to create a navigator panel where all the buttons are visible.
     */
    private Navigator() {
        this(WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE,
                WindowInclusionStatus.NOT_HIDABLE);
    }

    /**
     * Constructor. Use this to specify the inclusion statuses of each button.
     */
    public Navigator(WindowInclusionStatus correctGraphState,
                     WindowInclusionStatus experimentalSetupState,
                     WindowInclusionStatus hypotheticalGraphState,
                     WindowInclusionStatus correctManipulatedGraphState,
                     WindowInclusionStatus hypotheticalManipulatedGraphState,
                     WindowInclusionStatus populationState,
                     WindowInclusionStatus sampleState,
                     WindowInclusionStatus independenciesState) {
        super();
        //int width = 288;
        int width = 275;
        int height = 570;
        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

        setLayout(new SpringLayout());
        correctGraph = makePossiblyHidableNavigatorIcon(COR_GRAPH, "true_graph.gif", "true_graph_over.gif", "true_graph_down.gif", "true_graph_hidden.gif", "true_graph_hidden_over.gif", "true_graph_hidden_down.gif", "true_graph_active.gif", "true_graph_hidden_active.gif", correctGraphState);   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        corManipGraph = makePossiblyHidableNavigatorIcon(COR_MANIP, "true_manip_graph.gif", "true_manip_graph_over.gif", "true_manip_graph_down.gif", "true_manip_graph_hidden.gif", "true_manip_graph_hidden_over.gif", "true_manip_graph_hidden_down.gif", "true_manip_graph_active.gif", "true_manip_graph_hidden_active.gif", correctManipulatedGraphState);   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        population = makePossiblyHidableNavigatorIcon(POP, "population.gif", "population_over.gif", "population_down.gif", "population_hidden.gif", "population_hidden_over.gif", "population_hidden_down.gif", "population_active.gif", "population_hidden_active.gif", populationState);   //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        experiment = makeUnhidableNavigatorIcon(EXP_SETUP, "expt_setup.gif", "expt_setup_over.gif", "expt_setup_down.gif", "expt_setup_active.gif", experimentalSetupState);   //$NON-NLS-3$ //$NON-NLS-4$
        hypGraph = makeUnhidableNavigatorIcon(HYP_GRAPH, "hyp_graph.gif", "hyp_graph_over.gif", "hyp_graph_down.gif", "hyp_graph_active.gif", hypotheticalGraphState);   //$NON-NLS-3$ //$NON-NLS-4$
        hypManipGraph = makeUnhidableNavigatorIcon(HYP_MANIP, "hyp_manip_graph.gif", "hyp_manip_graph_over.gif", "hyp_manip_graph_down.gif", "hyp_manip_graph_active.gif", hypotheticalManipulatedGraphState);   //$NON-NLS-3$ //$NON-NLS-4$
        sample = makeUnhidableNavigatorIcon(SAMPLE, "sample.gif", "sample_over.gif", "sample_down.gif", "sample_active.gif", sampleState);   //$NON-NLS-3$ //$NON-NLS-4$
        compared = makeUnhidableNavigatorIcon(COMPARED, "predict.gif", "predict_over.gif", "predict_down.gif", "predict_active.gif", independenciesState);   //$NON-NLS-3$ //$NON-NLS-4$

        //if(correctGraph instanceof NavigatorButton){ ((NavigatorButton) correctGraph).addActionListener(this);}


        addIcons(correctGraph, corManipGraph, experiment, hypGraph, hypManipGraph, population, sample, compared);
        System.out.println("-- CALLING ADDPARAMETERIZEDARROWS");

        // if there is not true model built yet, do not show the arrows yet 
//        if(CausalityLab.getBuildNow()) {
        // if there is already a true model built..
        addParameterizedArrows(correctGraphState, experimentalSetupState, hypotheticalGraphState, correctManipulatedGraphState,
                hypotheticalManipulatedGraphState, populationState, sampleState, independenciesState,
                correctGraph, corManipGraph, experiment, hypGraph, hypManipGraph, population, sample, compared);
//        }

        //add(makeToolBar());

        SpringUtilities.makeGrid(this,
                4, 3, //rows, cols
                7, 100, //initialX, initialY
                20, 50);//xPad, yPad
        this.setBackground(new Color(0xE8E8E8));


    }

    private void addIcons(JComponent correctGraph, JComponent corManipGraph, JComponent experiment, JComponent hypGraph,
                          JComponent hypManipGraph, JComponent population, JComponent sample, JComponent compared) {

        add(correctGraph);
        add(experiment);
        add(hypGraph);
        add(corManipGraph);
        add(makeSpacer());
        add(hypManipGraph);
        add(population);
        add(sample);
        add(makeSpacer());
        add(makeSpacer());
        add(compared);
        add(makeSpacer());
    }

    private void addParameterizedArrows(WindowInclusionStatus correctGraphState, WindowInclusionStatus experimentalSetupState,
                                        WindowInclusionStatus hypotheticalGraphState, WindowInclusionStatus correctManipulatedGraphState,
                                        WindowInclusionStatus hypotheticalManipulatedGraphState, WindowInclusionStatus populationState,
                                        WindowInclusionStatus sampleState, WindowInclusionStatus independenciesState,
                                        JComponent correctGraph, JComponent corManipGraph, JComponent experiment, JComponent hypGraph,
                                        JComponent hypManipGraph, JComponent population, JComponent sample, JComponent compared) {
        if (stateIncluded(correctGraphState)) {
            if (stateIncluded(correctManipulatedGraphState)) {
                addArrow(correctGraph, corManipGraph);
            } else if (stateIncluded(populationState)) {
                addArrow(correctGraph, population);
            } else if (stateIncluded(independenciesState)) {
                addArrow(correctGraph, compared);
            }
        }

        if (stateIncluded(correctManipulatedGraphState)) {
            if (stateIncluded(populationState)) {
                addArrow(corManipGraph, population);
            } else if (stateIncluded(independenciesState)) {
                addArrow(corManipGraph, compared);
            }
        }

        if ((stateIncluded(populationState)) && (stateIncluded(independenciesState))) {
            addArrow(population, compared);
        }

        if (stateIncluded(experimentalSetupState)) {
            if (stateIncluded(correctManipulatedGraphState)) {
                addArrow(experiment, corManipGraph);
            } else if (stateIncluded(populationState)) {
                addArrow(experiment, population);
            }
        }

        if ((stateIncluded(experimentalSetupState)) && (stateIncluded(hypotheticalManipulatedGraphState))) {
            addArrow(experiment, hypManipGraph);
        }

        if ((stateIncluded(populationState)) && (stateIncluded(sampleState))) {
            addArrow(population, sample);
        }

        if ((stateIncluded(sampleState)) && (stateIncluded(independenciesState))) {
            addArrow(sample, compared);
        }

        if (stateIncluded(hypotheticalGraphState)) {
            if (stateIncluded(hypotheticalManipulatedGraphState)) {
                addArrow(hypGraph, hypManipGraph);
            } else if (stateIncluded(independenciesState)) {
                addArrow(hypGraph, compared);
            }
        }

        if ((stateIncluded(hypotheticalManipulatedGraphState)) && (stateIncluded(independenciesState))) {
            addArrow(hypManipGraph, compared);
        }
/*      addArrow(corManipGraph, population);
        addArrow(population, sample);
        addArrow(population, compared);
        addArrow(sample, compared);
        addArrow(experiment, corManipGraph);
        addArrow(experiment, hypManipGraph);
        addArrow(hypGraph, hypManipGraph);
        addArrow(hypManipGraph, compared);
    */
    }

    /**
     * Adds the navigation listener.
     */
    public void addNavigationListener(NavigatorChangeListener listener) {
        navigatorListeners.add(listener);
    }

    /**
     * Defines the action performed when the buttons are clicked. The action
     * performed by each button is further defined in the <code>CausalityLabPanel
     * </code>.
     */
    public void actionPerformed(ActionEvent e) {
        for (NavigatorChangeListener navigatorListener : navigatorListeners) {
            (navigatorListener).navigatorChanged(
                    new NavigatorChangeEvent(this, e.getActionCommand()));
        }
    }

    /**
     * Hides all the icons.
     */
    public void hideIcons(boolean hide) {
        hide(correctGraph, hide);
        hide(corManipGraph, hide);
        hide(population, hide);
        hide(experiment, hide);
        hide(hypGraph, hide);
        hide(hypManipGraph, hide);
        hide(sample, hide);
        hide(compared, hide);
    }

    /**
     * Set the active Navigator Button by highlighting it, given the getModel
     * frame in focus.
     */
    public void setActiveIcon(String frameTitle) {
        if (frameTitle.equals("Correct Graph")) ((NavigatorButton) correctGraph).setActive(true);
        if (frameTitle.equals("Correct Manipulated Graph")) ((NavigatorButton) corManipGraph).setActive(true);
        if (frameTitle.equals("Population")) ((NavigatorButton) population).setActive(true);
        if (frameTitle.equals("Experimental Setup")) ((NavigatorButton) experiment).setActive(true);
        if (frameTitle.equals("Hypothesis Graph")) ((NavigatorButton) hypGraph).setActive(true);
        if (frameTitle.equals("Hypothetical Manipulated Graph")) ((NavigatorButton) hypManipGraph).setActive(true);
        if (frameTitle.equals("Sample")) ((NavigatorButton) sample).setActive(true);
        if (frameTitle.equals("Predictions & Results")) ((NavigatorButton) compared).setActive(true);

        //Logger.windowopened(frameTitle);
    }

    /**
     * Unhighlight all the icons.
     */
    public void setAllIconsUnactive() {
        if (correctGraph instanceof NavigatorButton) ((NavigatorButton) correctGraph).setActive(false);
        if (corManipGraph instanceof NavigatorButton) ((NavigatorButton) corManipGraph).setActive(false);
        if (population instanceof NavigatorButton) ((NavigatorButton) population).setActive(false);
        if (experiment instanceof NavigatorButton) ((NavigatorButton) experiment).setActive(false);
        if (hypGraph instanceof NavigatorButton) ((NavigatorButton) hypGraph).setActive(false);
        if (hypManipGraph instanceof NavigatorButton) ((NavigatorButton) hypManipGraph).setActive(false);
        if (sample instanceof NavigatorButton) ((NavigatorButton) sample).setActive(false);
        if (compared instanceof NavigatorButton) ((NavigatorButton) compared).setActive(false);

    }

    private void hide(JComponent navIcon, boolean hide) {
        if (navIcon.getClass() == NavigatorButton.class) {
            ((NavigatorButton) navIcon).setHidden(hide);
        }
    }

    private boolean stateIncluded(WindowInclusionStatus state) {
        return state != WindowInclusionStatus.NOT_INCLUDED;
    }

    /*
    private JToolBar makeToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton instructButton = new JButton(edu.cmu.StringTemp.message("Instructions"));
        JButton checkAnsButton = new JButton(edu.cmu.StringTemp.message("Check my answer"));
        JButton showAnsButton = new JButton(edu.cmu.StringTemp.message("Show answer"));

        instructButton.setActionCommand(INSTRUCT);
        checkAnsButton.setActionCommand(CHECK_ANSWER);
        showAnsButton.setActionCommand(SHOW_ANSWER);

        instructButton.addActionListener(this);
        checkAnsButton.addActionListener(this);
        showAnsButton.addActionListener(this);

        toolbar.add(instructButton);
        toolbar.add(checkAnsButton);
        toolbar.add(showAnsButton);

        return toolbar;
    }
    */

    private JComponent makePossiblyHidableNavigatorIcon(String command, String imageFile, String imageFileRoll, String imageFileDown, String imageHiddenFile, String imageHiddenFileRoll, String imageHiddenFileDown, String imageFileActive, String imageHiddenFileActive, WindowInclusionStatus showState) {
        if (showState == WindowInclusionStatus.HIDABLE) {
            ImageIcon icon, iconRoll, iconDown, iconHidden, iconRollHidden, iconDownHidden, iconActive, iconHiddenActive;
            icon = new ImageIcon(ImageUtils.getImage(this, imageFile));
            iconRoll = new ImageIcon(ImageUtils.getImage(this, imageFileRoll));
            iconDown = new ImageIcon(ImageUtils.getImage(this, imageFileDown));
            iconHidden = new ImageIcon(ImageUtils.getImage(this, imageHiddenFile));
            iconRollHidden = new ImageIcon(ImageUtils.getImage(this, imageHiddenFileRoll));
            iconDownHidden = new ImageIcon(ImageUtils.getImage(this, imageHiddenFileDown));
            iconActive = new ImageIcon(ImageUtils.getImage(this, imageFileActive));
            iconHiddenActive = new ImageIcon(ImageUtils.getImage(this, imageHiddenFileActive));
            return new NavigatorButton(this, command, icon, iconRoll, iconDown, iconHidden, iconRollHidden, iconDownHidden, iconActive, iconHiddenActive);
        } else if (showState == WindowInclusionStatus.NOT_HIDABLE) {
            return makeUnhidableNavigatorIcon(command, imageFile, imageFileRoll, imageFileDown, imageFileActive, showState);
        } else {
            return makeSpacer();
        }
    }

    private JComponent makeUnhidableNavigatorIcon(String command, String imageFile, String imageFileRoll, String imageFileDown, String imageFileActive, WindowInclusionStatus showState) {
        if (showState == WindowInclusionStatus.NOT_HIDABLE) {
            ImageIcon icon, iconRoll, iconDown, iconActive;
            icon = new ImageIcon(ImageUtils.getImage(this, imageFile));
            iconRoll = new ImageIcon(ImageUtils.getImage(this, imageFileRoll));
            iconDown = new ImageIcon(ImageUtils.getImage(this, imageFileDown));
            iconActive = new ImageIcon(ImageUtils.getImage(this, imageFileActive));
            return new NavigatorButton(this, command, icon, iconRoll, iconDown, iconActive);
        } else {
            return makeSpacer();
        }
    }


    private JLabel makeSpacer() {
        //Image image = Misc.makeIcon(this.getClass(), "blank.gif").getImage();
        ImageIcon icon = new ImageIcon(ImageUtils.getImage(this, "blank.gif"));

        return new JLabel(icon);
    }

    /**
     * Adds an arrow to this.
     */
    void addArrow(Component fromNode, Component toNode) {
        if (fromNode == null || toNode == null) return;

        //NavNode fromNode = (NavNode) moduleToNode.get(from);
        //NavNode toNode = (NavNode) moduleToNode.get(to);

        ArrowView arrowView = new ArrowView(fromNode, toNode);
        arrowView.setArrowShapes(new NavigatorEdgeShapes());
        arrowView.setSelected(true);
        arrows.add(arrowView);
    }

    /**
     * Draws all the arrows.
     */
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        Misc.antialiasOn(g2d);
        for (ArrowView arrowView : arrows) {
            arrowView.paint(g2d);
        }
    }

    /**
     * This function is just for testing
     */
    public static void main(String args[]) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JFrame testFrame = new JFrame();
        testFrame.getContentPane().add(new Navigator());
        testFrame.pack();
        testFrame.setVisible(true);
    }


    private class NavigatorEdgeShapes extends ArrowView.ArrowShapes {
        NavigatorEdgeShapes() {
            super();
            this.headHeight = 10;
            this.headWidth = 14;
        }

        protected void refreshShapes(PosPair pp) {
            final Pos p1 = pp.getP1(), p2 = pp.getP2();
            int x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();


            double a = x2 - x1;
            double b = y1 - y2;
            double theta = Math.atan2(b, a);
            //int itheta = (int) ((theta * 360.0) / (2.0 * Math.PI) + 180);
            final double dx = Math.cos(theta);
            final double dy = Math.sin(theta);

            boolean DRAW_CIRCLE = true;
            if (DRAW_CIRCLE) {
                final double ratio = 2.0;
                arrowBody = new Line2D.Double(x1, y1, x2 - dx * ratio, y2 + dy * ratio);
                final double cx = x2 - dx * ratio - ratio;
                final double cy = y2 + dy * ratio - ratio;
                arrowHead = new Ellipse2D.Double(cx, cy, ratio * 2, ratio * 2);
            } else {
                final double x2noArrow = x2 - dx * headHeight;
                final double y2noArrow = y2 + dy * headHeight;
                final double dyArrow = dy * headWidth / 2;
                final double dxArrow = dx * headWidth / 2;

                final double[] xHeadPoints = {x2noArrow + dyArrow, x2, x2noArrow - dyArrow};
                final double[] yHeadPoints = {y2noArrow + dxArrow, y2, y2noArrow - dxArrow};

                arrowBody = new Line2D.Double(x1, y1, x2 - dx * 2, y2 + dy * 2);
                arrowHead = Misc.createPolyline(xHeadPoints, yHeadPoints);
            }


            final double dxBody = dx * bodyWidth, dyBody = dy * bodyWidth;
            final double[] xBodyPoints = {x1 - dyBody, x1 + dyBody, x2 + dyBody, x2 - dyBody};
            final double[] yBodyPoints = {y1 - dxBody, y1 + dxBody, y2 + dxBody, y2 - dxBody};
            Shape thickBody = Misc.createPolygon(xBodyPoints, yBodyPoints);
            Area arrowArea = new Area(arrowHead);
            arrowArea.add(new Area(thickBody));
            arrowShape = arrowArea;


        }

        /**
         * Draws the arrow head.
         */
        protected void drawArrowHead(Graphics2D g) {
            g.draw(arrowHead);
        }
    }
}
