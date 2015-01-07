package edu.cmu.causalityApp.dataEditors.sampleEditor;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.event.SampleChangedEvent;
import edu.cmu.causalityApp.chartBuilder.*;
import edu.cmu.causalityApp.dataEditors.AbstractDatatableEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class describes the Sample Editor window.
 *
 * @author mattheweasterday
 */
public class SampleEditor extends AbstractDatatableEditor {

    /**
     * Unique string id of this window.
     */
    public static final String MY_NAME = "Sample";

    private JFileChooser fileChooser;
    private final JButton saveSampleButton = new JButton("Save sample");
    private final JButton createChartButton;
    private JButton runRegressionButton;
    private SampleCasesTable CURRENT_SAMPLE_TABLE;
    private Map<String, SampleCasesTable> sampleTableMap;
    private SampleFrequenciesTableSem CURRENT_SEM_FREQUENCIES_TABLE;
    private JTabbedPane tab;
    private final HashMap<Component, Integer> tab2SampleId;

    /**
     * Main constructor
     */
    public SampleEditor(CausalityLabModel minimodel, InternalFrameListener frame, boolean isApplication) {
        this(minimodel, isApplication);
        addInternalFrameListener(frame);
    }

    /**
     * Constructor used by demo
     */
    private SampleEditor(CausalityLabModel minimodel, boolean isApplication) {
        super(MY_NAME, minimodel);
        CausalityLabModel.ModelType modelType = minimodel.getModelType();
        if (modelType.equals(CausalityLabModel.ModelType.BAYES)) {
            createChartButton = new JButton("Create histogram");
        } else {
            createChartButton = new JButton("Create scatterplot");
            runRegressionButton = new JButton("Regression");
        }
        JToolBar toolbar = getToolbar();
        makeButtons();

        toolbar.add(Box.createHorizontalStrut(40));
        if (isApplication) {
            fileChooser = new JFileChooser();
            toolbar.add(saveSampleButton);
        }
        toolbar.add(createChartButton);
        if (modelType.equals(CausalityLabModel.ModelType.SEM))
            toolbar.add(runRegressionButton);

        tab2SampleId = new HashMap<Component, Integer>();
    }


    private void makeButtons() {

        saveSampleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showDialog(SampleEditor.this, "Save sample");
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fileChooser.getSelectedFile();
                    try {
                        PrintStream fileOut = new PrintStream(new FileOutputStream(file));
                        if (getCurrentSampleTable() != null) {
                            printBayesSampleCases(fileOut);
                        } else {
                            printSemCases(fileOut);
                        }
                    } catch (java.io.FileNotFoundException fnfe) {
                        // empty
                    }
                }
            }
        });

        createChartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((getTab() != null) && (getTab().getSelectedIndex() >= 0)) {
                    ChartBuilder chartbuilder;
                    int sampleId = getTab2SampleId().get(getTab().getSelectedComponent());
                    if (getModel().getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
//                        String[] vtc = new String[varsToChart.size()];
//                        for (int i = 0; i < varsToChart.size(); i++) {
//                            vtc[i] = varsToChart.get(i);
//                        }
//
//                        String[] cv = new String[condVars.size()];
//                        for (int i = 0; i < condVars.size(); i++) {
//                            cv[i] = condVars.get(i);
//                        }
//
//                        String[] cs = new String[condState.size()];
//                        for (int i = 0; i < condState.size(); i++) {
//                            cs[i] = condState.get(i);
//                        }

                        String[] vtc = new String[0];
                        String[] cv = new String[0];
                        String[] cs = new String[0];

                        CausalityLabModel model = CausalityLabModel.getModel();

                        CreateHistogramCommand chc = new CreateHistogramCommand(
                                getDesktopPane(),
                                model.getExperimentNameForSampleId(sampleId),
                                model.getSampleName(sampleId),
                                vtc,
                                cv,
                                cs,
                                (int) getLocation().getX() + 15,
                                (int) getLocation().getY() + 15);
                        chc.doIt();

//                        chartbuilder = new HistogramBuilder(getModel(), sampleId);
                    } else {
                        chartbuilder = new ScatterPlotBuilder(
                                getModel(),
                                getModel().getSampleName(sampleId),
                                sampleId);

                        getDesktopPane().add(chartbuilder);
                        chartbuilder.setLocation(new Point((int) getLocation().getX() + 15, (int) getLocation().getY() + 15));
                        chartbuilder.setVisible(true);
                        chartbuilder.pack();
                    }
                }
            }
        });

        if (getModel().getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            runRegressionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if ((getTab() != null) && (getTab().getSelectedIndex() >= 0)) {
                        ChartBuilder chartbuilder;
                        int sampleId = getTab2SampleId().get(getTab().getSelectedComponent());
                        chartbuilder = new RegressionChartBuilder(
                                getModel().getExperimentNameForSampleId(sampleId),
                                getModel().getExperimentalSetupStudiedVariablesNames(getModel().getExperimentNameForSampleId(sampleId)),
                                //(SemSample) getModel().getSample(sampleId),
                                getModel().getSampleName(sampleId),
                                sampleId);

                        getDesktopPane().add(chartbuilder);
                        chartbuilder.setLocation(new Point((int) getLocation().getX() + 15, (int) getLocation().getY() + 15));
                        chartbuilder.setVisible(true);
                        chartbuilder.pack();
                    }
                }
            });
        }

    }


    /**
     * @return the unique string id of the editor.
     */
    public String getEditorName() {
        return MY_NAME;
    }


    //================ModelChangeListener methods ================


    /**
     * Detects that sample has changed. Refreshes the data table with the changes.
     */
    public void sampleChanged(SampleChangedEvent scEvent) {
        showDatatableAgain();
    }

    /**
     * Detects that finance has changed. Not applicable to this window.
     */
    public void financeChanged() {
    }

    //================END ModelChangeListener methods ================

    /**
     * @return the label for the toolbar. Currently "Examine samples for :".
     */
    public String getToolbarLabel(CausalityLabModel model) {
        return "Examine samples for :";
    }


    /**
     * Sets up the editor to show the samples (if any) for the given experiement name.  If no samples
     * are taken, the editor says so
     */
    protected JComponent constructDatatable(CausalityLabModel minimodel, String experimentId) {
        JLabel sampleCasesLabel, sampleCaseFrequenciesLabel;
        SampleTable sampleCaseFrequencies;
        JPanel sampleCasesPanel, sampleCaseFrequenciesPanel;
        JPanel panel = new JPanel();
        int[] sampleIds = minimodel.getSampleIds(experimentId);


        if (sampleIds.length == 0) {               //No samples to display
            panel.add(new JLabel("No samples have been collected for this experiment"));
            return panel;
        } else {
            tab = new JTabbedPane(JTabbedPane.RIGHT);
            getTab().setPreferredSize(new Dimension(550, 400));
            for (int sampleId : sampleIds) {

                sampleCaseFrequenciesLabel = new JLabel("Sample Distribution");
                sampleCaseFrequenciesLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                sampleCaseFrequenciesLabel.setHorizontalAlignment(JLabel.CENTER);

                sampleCaseFrequenciesPanel = new JPanel(new BorderLayout());

                if (minimodel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {

                    CURRENT_SAMPLE_TABLE = new SampleCasesTable(minimodel.getSampleCasesTableModel(sampleId));

                    if (sampleTableMap == null) {
                        sampleTableMap = new Hashtable<String, SampleCasesTable>();
                    }


                    sampleCaseFrequencies = new SampleFrequenciesTable(minimodel.getSampleFrequenciesTableModel(sampleId));
                    sampleCaseFrequenciesPanel.add(sampleCaseFrequenciesLabel, BorderLayout.NORTH);
                    sampleCaseFrequenciesPanel.add(new JScrollPane(sampleCaseFrequencies), BorderLayout.CENTER);
                    sampleCaseFrequenciesPanel.setPreferredSize(new Dimension((int) sampleCaseFrequencies.getPreferredSize().getWidth() + 15, HEIGHT));

                    sampleCasesLabel = new JLabel("Sample Cases" + " (n=" + getCurrentSampleTable().getRowCount() + ")");   //$NON-NLS-3$
                    sampleCasesLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                    sampleCasesLabel.setHorizontalAlignment(JLabel.CENTER);

                    sampleCasesPanel = new JPanel(new BorderLayout());
                    sampleCasesPanel.add(sampleCasesLabel, BorderLayout.NORTH);
                    sampleCasesPanel.add(new JScrollPane(getCurrentSampleTable()), BorderLayout.CENTER);

                    sampleCasesPanel.setPreferredSize(new Dimension((int) getCurrentSampleTable().getPreferredSize().getWidth() + 15, HEIGHT));


                    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sampleCasesPanel, sampleCaseFrequenciesPanel);
                    splitPane.setOneTouchExpandable(false);
                    splitPane.setContinuousLayout(true);
                    splitPane.setResizeWeight(0.5);
                    splitPane.setName(minimodel.getSampleName(sampleId));
                    getTab().add(splitPane, minimodel.getSampleName(sampleId));
                    getTab2SampleId().put(splitPane, sampleId);
                    sampleTableMap.put(minimodel.getSampleName(sampleId), CURRENT_SAMPLE_TABLE);

                } else if (minimodel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {

                    CURRENT_SEM_FREQUENCIES_TABLE = new SampleFrequenciesTableSem(minimodel.getSampleFrequenciesTableModel(sampleId));
                    sampleCaseFrequencies = getCurrentSemFrequenciesTable();
                    sampleCaseFrequenciesPanel.add(sampleCaseFrequenciesLabel, BorderLayout.NORTH);
                    sampleCaseFrequenciesPanel.add(new JScrollPane(sampleCaseFrequencies), BorderLayout.CENTER);
                    sampleCaseFrequenciesPanel.setPreferredSize(new Dimension((int) sampleCaseFrequencies.getPreferredSize().getWidth() + 15, HEIGHT));
                    getTab().add(sampleCaseFrequenciesPanel, minimodel.getSampleName(sampleId));
                    getTab2SampleId().put(sampleCaseFrequenciesPanel, sampleId);
                    //System.out.println(sampleIds[i]);
                }
            }
            return getTab();
        }
    }

    /**
     * @return the getModel sample tab.
     */
    JTabbedPane getTab() {
        return tab;
    }

    /**
     * @return the hashmap which maps the tab to the sample id.
     */
    HashMap<Component, Integer> getTab2SampleId() {
        return tab2SampleId;
    }

    /**
     * @return the causality lab model.
     */
    CausalityLabModel getModel() {
        return minimodel;
    }

    /**
     * @return the getModel sample cases table.
     */
    SampleCasesTable getCurrentSampleTable() {
        return CURRENT_SAMPLE_TABLE;
    }

    /**
     * @return the getModel SEM frequencies table.
     */
    SampleFrequenciesTableSem getCurrentSemFrequenciesTable() {
        return CURRENT_SEM_FREQUENCIES_TABLE;
    }

    /*
     * Helper function to print the Bayes sample cases for debugging or saving.
     */
    private void printBayesSampleCases(PrintStream fileOut) {
        int row, col;

        SampleCasesTable sct = sampleTableMap.get(getTab().getSelectedComponent().getName());

        for (col = 1; col < sct.getColumnCount(); col++) {
            fileOut.print(sct.getColumnName(col));
            if (col + 1 < sct.getColumnCount()) {
                fileOut.print("\t");
            }
        }
        fileOut.println();

        for (row = 0; row < sct.getRowCount(); row++) {
            for (col = 1; col < sct.getColumnCount(); col++) {
                fileOut.print(sct.getValueAt(row, col));
                if (col + 1 < sct.getColumnCount()) {
                    fileOut.print("\t");
                }
            }
            fileOut.println();
        }
    }

    /*
     * Helper function to print the SEM sample cases for debugging or saving.
     */
    private void printSemCases(PrintStream fileOut) {
        int row, col;
        for (col = 1; col < getCurrentSemFrequenciesTable().getColumnCount(); col++) {
            fileOut.print(getCurrentSemFrequenciesTable().getColumnName(col));
            if (col + 1 < getCurrentSemFrequenciesTable().getColumnCount()) {
                fileOut.print("\t");
            }
        }
        fileOut.println();

        for (row = 0; row < getCurrentSemFrequenciesTable().getRowCount(); row++) {
            for (col = 1; col < getCurrentSemFrequenciesTable().getColumnCount(); col++) {
                fileOut.print(getCurrentSemFrequenciesTable().getValueAt(row, col));
                if (col + 1 < getCurrentSemFrequenciesTable().getColumnCount()) {
                    fileOut.print("\t");
                }
            }
            fileOut.println();
        }
    }
}