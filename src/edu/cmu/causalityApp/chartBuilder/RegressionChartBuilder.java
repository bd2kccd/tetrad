package edu.cmu.causalityApp.chartBuilder;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

/**
 * This describes the wizard for running regression on a SEM sampleIt allows the user
 * to select the predictor and response variable(s) for the regression.
 *
 * @author Adrian Tang
 */
public class RegressionChartBuilder extends ChartBuilder implements ActionListener {

    final private static String INCLUDE_RESPONSE = "includeResponse";
    final private static String INCLUDE_PREDICTOR = "includePredictor";
    final private static String DISCLUDE_PREDICTOR = "discludePredictor";

    private final Vector<String> varsList;
    private final Vector<String> predictorVarsList;
    private JTextField responseVar;
    private ArrowButton responseBn;
    private JList listbox;
    private JList predictorVarListbox;
    private final String sampleName;
    //private SemSample sample;

    /**
     * Constructor.
     *
     * @param expName         the name of the experiment in which the sample was collected.
     * @param studiedVarNames a string array of the variable names in that experiment.
     * @param sampleName      name of the sample to run the regression on.
     * @param sampleId        unique id of the sample.
     */
    public RegressionChartBuilder(String expName, String[] studiedVarNames,
                                  //SemSample sample,
                                  String sampleName, int sampleId) {
        super(expName, sampleId, "Run Regression (SEM Model)");

        varsList = getVarNames(studiedVarNames);
        predictorVarsList = new Vector<String>();
        //this.sample = sample;
        this.sampleName = sampleName;

        setContentPane(makePage());
        setPreferredSize(new Dimension(400, 240));
    }

    /**
     * Handles the user interaction of the widgets and stores the user's selection
     * for running the regression.
     */
    public void actionPerformed(ActionEvent e) {
        String varName, predictorName;
        int varSelectionIndex, predictorVarSelectionIndex;

        if (varsList.size() != 0)
            varName = (String) listbox.getSelectedValue();
        else varName = "";

        if (predictorVarsList.size() != 0)
            predictorName = (String) predictorVarListbox.getSelectedValue();
        else predictorName = "";

        // include/disclude response variable
        if (e.getActionCommand().equals(INCLUDE_RESPONSE)) {
            if ((listbox.isSelectionEmpty()) && (responseBn.getIsIncluded())) return;

            if (responseBn.getIsIncluded()) {
                responseVar.setText(varName);
                varsList.remove(varName);
            } else {
                varsList.add(responseVar.getText());
                responseVar.setText("");
            }
            responseBn.toggleInclude();

            // include predictor variable
        } else if (e.getActionCommand().equals(INCLUDE_PREDICTOR)) {
            if (listbox.isSelectionEmpty()) return;
            varsList.remove(varName);
            predictorVarsList.add(varName);

            // disclude predictor variable
        } else {
            if (predictorVarListbox.isSelectionEmpty()) return;
            predictorVarsList.remove(predictorName);
            varsList.add(predictorName);
        }

        // updates expt variables and predictor listbox
        varSelectionIndex = listbox.getSelectedIndex();
        predictorVarSelectionIndex = predictorVarListbox.getSelectedIndex();

        if (varSelectionIndex > 0) varSelectionIndex--;
        listbox.setListData(varsList);
        if (varSelectionIndex != -1) listbox.setSelectedIndex(varSelectionIndex);

        if (predictorVarSelectionIndex > 0) predictorVarSelectionIndex--;
        predictorVarListbox.setListData(predictorVarsList);
        if (predictorVarSelectionIndex != -1) predictorVarListbox.setSelectedIndex(predictorVarSelectionIndex);
    }

    /**
     * Run the regression.
     */
    protected void createChart() {
        // variables to be charted: 1st var is response-variable, 2nd and more var are predictors
        String predictorVars[] = new String[predictorVarsList.size()];

        for (int i = 0; i < predictorVarsList.size(); i++) {
            predictorVars[i] = (predictorVarsList.get(i));
        }

        /* Create the Regression window */
        CreateRegressionCommand createRegressionCommand = new CreateRegressionCommand(
                getDesktopPane(),
                getExperimentalSetupName(),
                sampleName,
                responseVar.getText(),
                predictorVars,
                (int) getLocation().getX() + 15,
                (int) getLocation().getY() + 15
        );

        createRegressionCommand.doIt();
        dispose();
    }

    /**
     * Checks if there is at least a response variable and a predictor variable
     * before running the regression.
     *
     * @return true if so.
     */
    protected boolean isReadyToCreateChart() {
        return (!responseVar.getText().equals("") && !(predictorVarsList.size() == 0));
    }

    /**
     * Set up the UI for the regression builder, which consists of the list of
     * variables in the experiment and the include/disclude buttons.
     */
    protected JPanel createChooser() {
        JPanel container = new JPanel();
        JLabel instructions = new JLabel("Select response and predictor variables:");
        JLabel rspLb = new JLabel("response");
        JLabel pdtrLb = new JLabel("predictor(s)");
        JScrollPane varListbox = (JScrollPane) createVarListbox();
        JScrollPane predictorVarListbox = (JScrollPane) createPredictorVarListbox();

        responseBn = new ArrowButton(this, INCLUDE_RESPONSE);
        ArrowButton predictorInBn = new ArrowButton(this, INCLUDE_PREDICTOR);
        ArrowButton predictorOutBn = new ArrowButton(this, DISCLUDE_PREDICTOR, false);
        responseVar = new JTextField(10);
        responseVar.setEditable(false);
        responseVar.setBackground(Color.white);

        SpringLayout layout = new SpringLayout();
        container.setLayout(layout);
        container.add(instructions);
        container.add(varListbox);
        container.add(rspLb);
        container.add(pdtrLb);
        container.add(predictorVarListbox);
        container.add(responseBn);
        container.add(responseVar);
        container.add(predictorInBn);
        container.add(predictorOutBn);

        layout.putConstraint(SpringLayout.WEST, instructions, 80, SpringLayout.WEST, container);
        layout.putConstraint(SpringLayout.NORTH, instructions, 10, SpringLayout.NORTH, container);

        layout.putConstraint(SpringLayout.WEST, varListbox, 20, SpringLayout.WEST, container);
        layout.putConstraint(SpringLayout.NORTH, varListbox, 10, SpringLayout.SOUTH, instructions);

        layout.putConstraint(SpringLayout.WEST, responseBn, 15, SpringLayout.EAST, varListbox);
        layout.putConstraint(SpringLayout.NORTH, responseBn, 10, SpringLayout.SOUTH, instructions);

        layout.putConstraint(SpringLayout.WEST, rspLb, 10, SpringLayout.EAST, responseBn);
        layout.putConstraint(SpringLayout.NORTH, rspLb, 10, SpringLayout.SOUTH, instructions);

        layout.putConstraint(SpringLayout.WEST, responseVar, 5, SpringLayout.EAST, rspLb);
        layout.putConstraint(SpringLayout.NORTH, responseVar, 10, SpringLayout.SOUTH, instructions);

        layout.putConstraint(SpringLayout.WEST, predictorInBn, 15, SpringLayout.EAST, varListbox);
        layout.putConstraint(SpringLayout.NORTH, predictorInBn, 20, SpringLayout.SOUTH, responseBn);

        layout.putConstraint(SpringLayout.WEST, predictorOutBn, 15, SpringLayout.EAST, varListbox);
        layout.putConstraint(SpringLayout.NORTH, predictorOutBn, 5, SpringLayout.SOUTH, predictorInBn);

        layout.putConstraint(SpringLayout.WEST, pdtrLb, 10, SpringLayout.EAST, predictorInBn);
        layout.putConstraint(SpringLayout.NORTH, pdtrLb, 20, SpringLayout.SOUTH, responseBn);

        layout.putConstraint(SpringLayout.WEST, predictorVarListbox, 5, SpringLayout.EAST, pdtrLb);
        layout.putConstraint(SpringLayout.NORTH, predictorVarListbox, 20, SpringLayout.SOUTH, responseBn);

        layout.putConstraint(SpringLayout.EAST, container, 20, SpringLayout.EAST, responseVar);
        layout.putConstraint(SpringLayout.SOUTH, container, 20, SpringLayout.SOUTH, predictorVarListbox);

        return container;
    }

    /*
     * Helper function to convert a string array of studied variable names to a vector.
     * @return a vector of variables studied in an experiment
     */
    private Vector<String> getVarNames(String[] studiedVarNames) {
        Vector<String> varsList = new Vector<String>(studiedVarNames.length);

        Collections.addAll(varsList, studiedVarNames);

        return varsList;
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

    private JComponent createPredictorVarListbox() {
        predictorVarListbox = new JList();
        predictorVarListbox.setVisibleRowCount(4);
        predictorVarListbox.setFixedCellWidth(100);
        predictorVarListbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        predictorVarListbox.setSelectedIndex(0);

        return new JScrollPane(predictorVarListbox);
    }


    /**
     * Inner Class to manipulate the arrow buttons for including/discluding
     * variables for x/y-axis.
     */
    public class ArrowButton extends JButton {
        private boolean isInclude;

        /**
         * Constructor. Action associated is including the variable by default.
         */
        public ArrowButton(RegressionChartBuilder listener, String command) {
            this(listener, command, true);
        }

        /**
         * Use this constructor to specify if you want the button to include or
         * disclude a selected variable.
         */
        public ArrowButton(RegressionChartBuilder listener, String command, boolean isInclude) {
            this.isInclude = isInclude;
            addActionListener(listener);
            setActionCommand(command);
            if (isInclude) setText(">");
            else setText("<");
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
