package edu.cmu.causalityApp;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.finances.FinancesBalanceView;
import edu.cmu.causalityApp.navigator.NavigatorChangeEvent;
import edu.cmu.causalityApp.navigator.NavigatorChangeListener;
import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * This class describes the main toolbar for the Causality Lab. It holds the
 * icons commands at the top of the main screen.
 *
 * @author Adrian Tang
 */
class CausalityLabToolBar extends JToolBar implements ActionListener {
    /**
     * Tooltip for Instruction button.
     */
    final public static String INSTRUCT = "Instructions";

    /**
     * Tooltip for Check Answer button.
     */
    final public static String CHECK_ANSWER = "Check answer";

    /**
     * Tooltip for Show Answer button.
     */
    final public static String SHOW_ANSWER = "Show answer";

    /**
     * Tooltip for Save Exercise button.
     */
    final public static String SAVE_EXERCISE = "Save exercise";

    /**
     * Tooltip for Submit Exercise button.
     */
    final public static String SUBMIT_EXERCISE = "Submit exercise";

    /**
     * Tooltip for Essay button.
     */
    //todo:translation
    final public static String ESSAY = "Essay";

    /**
     * Tooltip for instructor's feedback button.
     */
    //todo:translation
    final public static String FEEDBACK = "FEEDBACK";


    /**
     * Tooltip for Finances button.
     */
    //todo:translation
    final public static String FINANCES = "Finances";


    final private static Color GREY_COLOR = new Color(204, 204, 204);

    private final JButton bn_instruction;
    private final JButton bn_checkAns;
    private final JButton bn_showAns;
    private final JButton bn_save;
    private final JButton bn_submit;
    private final JButton bn_finances;
    private final JButton bn_essay;
    private final JButton bn_feedback;


    private final FinancesBalanceView financesBalanceView;

    private final Vector<NavigatorChangeListener> navigatorListeners = new Vector<NavigatorChangeListener>();

    /**
     * Constructor.
     */
    public CausalityLabToolBar() {
        super();
        setFloatable(false);
        setRollover(true);
        setBackground(GREY_COLOR);

        bn_instruction = makeNavigationButton("toolbar_instruction.gif", "toolbar_instruction_disabled.gif", INSTRUCT, INSTRUCT);
        bn_checkAns = makeNavigationButton("toolbar_checkans.gif", "toolbar_checkans_disabled.gif", CHECK_ANSWER, CHECK_ANSWER);
        bn_showAns = makeNavigationButton("toolbar_showans.gif", "toolbar_showans_disabled.gif", SHOW_ANSWER, SHOW_ANSWER);
        bn_save = makeNavigationButton("toolbar_save.gif", "toolbar_save_disabled.gif", SAVE_EXERCISE, SAVE_EXERCISE);
        bn_submit = makeNavigationButton("toolbar_submit.gif", "toolbar_submit_disabled.gif", SUBMIT_EXERCISE, SUBMIT_EXERCISE);
        bn_essay = makeNavigationButton("essay.gif", "essay_disabled.gif", ESSAY, ESSAY);
        bn_feedback = makeNavigationButton("feedback.gif", "feedback_disabled.gif", FEEDBACK, FEEDBACK);

        bn_finances = makeNavigationButton("toolbar_finances.gif", "toolbar_finances_disabled.gif", FINANCES, FINANCES);


        // the separator's max size is set so that the buttons can be aligned properly
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMaximumSize(new Dimension(1, 40));

        add(bn_instruction);
        add(bn_checkAns);
        add(bn_showAns);
        add(Box.createHorizontalStrut(5));
        add(separator);
        add(Box.createHorizontalStrut(5));
        add(bn_save);
        add(bn_submit);
        add(bn_essay);
        add(bn_feedback);

        add(Box.createHorizontalGlue());

        add(bn_finances);
        add(Box.createHorizontalStrut(5));

        financesBalanceView = new FinancesBalanceView();
        add(financesBalanceView);
        add(Box.createHorizontalStrut(2));

    }

    /**
     * Helper function to make each button.
     */
    private JButton makeNavigationButton(String imageName,
                                         String disabledImageName,
                                         String actionCommand,
                                         String toolTipText) {
        ImageIcon icon = new ImageIcon(ImageUtils.getImage(this, imageName));
        ImageIcon disabledIcon = new ImageIcon(ImageUtils.getImage(this, disabledImageName));
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        JButton button = new JButton(icon);
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setDisabledIcon(disabledIcon);

        button.setMaximumSize(new Dimension(width + 2, height + 2));
        button.setPreferredSize(new Dimension(width + 2, height + 2));
        button.setMinimumSize(new Dimension(width + 2, height + 2));

        button.setEnabled(false);

        return button;
    }

    /**
     * Initialize the finances balance view to show the getModel balance.
     */
    public void initializeFinancesBalanceView(CausalityLabModel model) {
        financesBalanceView.setModel(model);
    }

    /**
     * Add an action listener to this class.
     */
    public void addNavigationListener(NavigatorChangeListener listener) {
        navigatorListeners.add(listener);
    }

    /**
     * Remove an action listener from this class.
     */
    public void removeNavigationListener(NavigatorChangeListener listener) {
        navigatorListeners.remove(listener);
    }

    /**
     * Propogate the event of button click to each Listener, ie CausalityLab and
     * CausalityLabPanel.
     */
    public void actionPerformed(ActionEvent e) {
        for (NavigatorChangeListener navigatorListener : navigatorListeners) {
            (navigatorListener).navigatorChanged(
                    new NavigatorChangeEvent(this, e.getActionCommand()));
        }
    }

    /**
     * Disable the button of the given name.
     *
     * @param bnName one of the final static string of the button names
     */
    public void disableButton(String bnName) {
        if (bnName.equals(INSTRUCT)) {
            bn_instruction.setEnabled(false);
        } else if (bnName.equals(CHECK_ANSWER)) {
            bn_checkAns.setEnabled(false);
        } else if (bnName.equals(SHOW_ANSWER)) {
            bn_showAns.setEnabled(false);
        } else if (bnName.equals(SAVE_EXERCISE)) {
            bn_save.setEnabled(false);
        } else if (bnName.equals(SUBMIT_EXERCISE)) {
            bn_submit.setEnabled(false);
        } else if (bnName.equals(FINANCES)) {
            bn_finances.setEnabled(false);
        } else if (bnName.equals(ESSAY)) {
            bn_essay.setEnabled(false);
        } else if (bnName.equals(FEEDBACK)) {
            bn_feedback.setEnabled(false);
        }
    }


    /**
     * Enable the button of the given name.
     *
     * @param bnName one of the final static string of the button names
     */
    public void enableButton(String bnName) {
        if (bnName.equals(INSTRUCT)) {
            bn_instruction.setEnabled(true);
        } else if (bnName.equals(CHECK_ANSWER)) {
            bn_checkAns.setEnabled(true);
        } else if (bnName.equals(SHOW_ANSWER)) {
            bn_showAns.setEnabled(true);
        } else if (bnName.equals(SAVE_EXERCISE)) {
            bn_save.setEnabled(true);
        } else if (bnName.equals(SUBMIT_EXERCISE)) {
            bn_submit.setEnabled(true);
        } else if (bnName.equals(FINANCES)) {
            bn_finances.setEnabled(true);
        } else if (bnName.equals(ESSAY)) {
            bn_essay.setEnabled(true);
        } else if (bnName.equals(FEEDBACK)) {
            bn_feedback.setEnabled(true);
        }

    }

}
