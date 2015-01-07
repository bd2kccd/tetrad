package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.CausalityLabModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

/**
 * This is the wizard which allows the user to select the x and y-axis variables
 * to chart the scatterplot.
 *
 * @author Adrian Tang
 */
public class ScatterPlotBuilder extends ChartBuilder implements ActionListener {
    private final Vector<String> varsList;
    private JTextField xVar, yVar;
    private JCheckBox isIncludeLine;
    private ArrowButton xBn, yBn;
    private JList listbox;
    private final String sampleName;
    private final String truename;

    final private static String INCLUDE_X = "includeX";
    final private static String INCLUDE_Y = "includeY";


    /**
     * Constructor.
     */
    public ScatterPlotBuilder(CausalityLabModel model,
                              String sampleName,
                              int sampleId) {
        super(model.getExperimentNameForSampleId(sampleId),
                sampleId, "Create Scatterplot (SEM Model)");

        this.sampleName = sampleName;
        varsList = getVarNames(model, sampleId);
        this.truename = "True";   // todo jdramsey

        setContentPane(makePage());
        setPreferredSize(new Dimension(400, 220));
    }


    private Vector<String> getVarNames(CausalityLabModel model, int sampleId) {
        String[] varsNames = model.getExperimentalSetupStudiedVariablesNames(
                model.getExperimentNameForSampleId(sampleId));
        Vector<String> varsList = new Vector<String>(varsNames.length);

        Collections.addAll(varsList, varsNames);

        return varsList;
    }

    /**
     * Creates the selection options for the user to select from the various
     * variables to include in the chart
     *
     * @return JPanel chooser pane
     */
    protected JPanel createChooser() {
        JPanel chooser = new JPanel();
        JPanel subPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel xAxisPanel = new JPanel();
        JPanel yAxisPanel = new JPanel();

        xBn = new ArrowButton(this, INCLUDE_X);
        yBn = new ArrowButton(this, INCLUDE_Y);
        xVar = new JTextField(10);
        yVar = new JTextField(10);
        xVar.setEditable(false);
        yVar.setEditable(false);
        xVar.setBackground(Color.white);
        yVar.setBackground(Color.white);
        isIncludeLine = new JCheckBox("Show Regression Line");

        chooser.add(new JLabel("Select x-axis and y-axis variables:"), BorderLayout.NORTH);
        chooser.add(subPanel, BorderLayout.SOUTH);
        subPanel.add(createVarListbox(), BorderLayout.WEST);
        subPanel.add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        subPanel.add(rightPanel, BorderLayout.EAST);
        /* todo:add constraints for variables */

        rightPanel.setLayout(new GridLayout(3, 1));
        rightPanel.add(xAxisPanel);
        rightPanel.add(yAxisPanel);
        rightPanel.add(isIncludeLine);
        xAxisPanel.setLayout(new FlowLayout());
        xAxisPanel.add(xBn);
        xAxisPanel.add(new JLabel("  x-axis"));
        xAxisPanel.add(xVar);
        yAxisPanel.setLayout(new FlowLayout());
        yAxisPanel.add(yBn);
        yAxisPanel.add(new JLabel("  y-axis"));
        yAxisPanel.add(yVar);

        return chooser;
    }

    /**
     * With the user selection, create the scatterplot in a separate window.
     */
    protected void createChart() {

        (new CreateScatterplotCommand(getDesktopPane(),
                getExperimentalSetupName(),
                sampleName,
                truename,
                isIncludeLine.isSelected(),
                yVar.getText(),
                xVar.getText(),
                (int) getLocation().getX() + 15,
                (int) getLocation().getY() + 15)).doIt();

        dispose();
    }

    /**
     * Checks if the x-axis and y-axis variables are selected.
     *
     * @return true if so
     */
    protected boolean isReadyToCreateChart() {
        return (!xVar.getText().equals("") && !yVar.getText().equals(""));
    }

    private JComponent createVarListbox() {
        listbox = new JList(varsList);
        listbox.setVisibleRowCount(5);
        listbox.setFixedCellWidth(100);
        listbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listbox.setSelectedIndex(0);

        JScrollPane scrollPane = new JScrollPane(listbox);
        JLabel listboxTitle = new JLabel(" Variables");
        scrollPane.setColumnHeaderView(listboxTitle);
        listboxTitle.setBackground(Color.DARK_GRAY);

        return scrollPane;
    }

    /**
     * Control the variable selection actions of the include/disclude buttons.
     */
    public void actionPerformed(ActionEvent e) {
        String varName;

        if (varsList.size() != 0)
            varName = (String) listbox.getSelectedValue();
        else varName = "";

        if (e.getActionCommand().equals(INCLUDE_X)) {
            if ((listbox.isSelectionEmpty()) && (xBn.getIsIncluded())) return;
            includeDiscludeVar(xBn.getIsIncluded(), true, varName);
            xBn.toggleInclude();
        } else if (e.getActionCommand().equals(INCLUDE_Y)) {
            if ((listbox.isSelectionEmpty()) && (yBn.getIsIncluded())) return;
            includeDiscludeVar(yBn.getIsIncluded(), false, varName);
            yBn.toggleInclude();
        }
    }

    private void includeDiscludeVar(boolean isInclude, boolean isX, String varName) {
        int selectionIndex = listbox.getSelectedIndex();
        JTextField textfield = isX ? xVar : yVar;

        if (isInclude) {
            textfield.setText(varName);
            varsList.remove(varName);
        } else {
            varsList.add(textfield.getText());
            textfield.setText("");
        }

        if (selectionIndex > 0) selectionIndex--;
        listbox.setListData(varsList);
        if (selectionIndex != -1) listbox.setSelectedIndex(selectionIndex);
    }

    /**
     * Private Inner Class to manipulate the arrow buttons for including/discluding
     * variables for x/y-axis
     */
    public class ArrowButton extends JButton {
        private boolean isInclude;

        /**
         * Constructor. Action associated is including the variable by default.
         */
        public ArrowButton(ScatterPlotBuilder listener, String command) {
            isInclude = true;
            addActionListener(listener);
            setActionCommand(command);
            setText(">");
        }

        /**
         * Toggle the include / disclude function of the button.
         */
        public void toggleInclude() {
            if (isInclude) {
                setText("<");
                isInclude = false;
            } else {
                setText(">");
                isInclude = true;
            }
        }

        /**
         * @return include / disclude status of the button.
         */
        public boolean getIsIncluded() {
            return isInclude;
        }
    }
}
