package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.CreateSampleCommand;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causalityApp.dataEditors.sampleEditor.SaveSampleProgressDialog;
import edu.cmu.tetrad.sem.SemIm;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.text.NumberFormat;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class is a container frame for collecting a sample.
 *
 * @author adrian tang
 */
class CollectSampleWizard extends JFrame implements DocumentListener {
    final private static String MY_NAME = "Collect Sample";
    final private static String OK = "Ok";
    final private static String CANCEL = "Cancel";
    final private static NumberFormat nf;

    static {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMinimumFractionDigits(0);
        nf1.setMaximumFractionDigits(0);
        nf = nf1;
    }

    private final CausalityLabModel model;
    private final JDesktopPane desktop;
    private JTextField txtSampleSize;
    private JTextField txtSampleName;
    private JTextField txtTotalMoney;
    private final String expName;
    private final SemIm im;


    /**
     * Constructor.
     */
    public CollectSampleWizard(CausalityLabModel model, JDesktopPane desktop, String expName) {
        super(MY_NAME);

        this.model = model;

        // Get the SEM model, if there is one, since we need to make sure the sample size isn't less than the
        // number of variables.
        if (model.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            im = model.getCorrectGraphSemImCopy();
        } else {
            im = null;
        }

        this.desktop = desktop;
        this.expName = expName;

        getContentPane().add(createMainPage(), BorderLayout.PAGE_START);
        getContentPane().add(getToolBar(), BorderLayout.PAGE_END);

        this.setLocation(350, 300);
        pack();
        setVisible(true);
    }

    /**
     * Creates a JToolbar for use in the window. Sets up the action listeners for
     * the OK and Cancel buttons.
     */
    private JToolBar getToolBar() {
        JToolBar toolbar = new JToolBar();

        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 14));
        JButton okBn = new JButton(OK);
        JButton cancelBn = new JButton(CANCEL);

        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(cancelBn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(okBn);
        toolbar.add(Box.createHorizontalGlue());

        cancelBn.setPreferredSize(new Dimension(70, 26));
        okBn.setPreferredSize(new Dimension(70, 26));

        toolbar.setPreferredSize(toolbar.getMinimumSize());
        toolbar.setMaximumSize(toolbar.getMaximumSize());
        toolbar.setMinimumSize(toolbar.getMinimumSize());

        cancelBn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        okBn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int size = getSampleSize();
                if (size == -1) return;

                if (model.isLimitResource()) {

                    // Check if the given sample size will exceed the resources left
                    if (getTotalCostofSample() > model.getCurrentBalance()) {
                        // todo: translation
                        JOptionPane.showMessageDialog(CollectSampleWizard.this,
                                "You do not have enough money to collect sample of this size",
                                "Error",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }

                String name = txtSampleName.getText();
                CreateSampleCommand csc = new CreateSampleCommand(expName, size, name, getTotalCostofSample());     // HERE?! //
                try {
                    csc.doIt();
                } catch (IllegalArgumentException i) {
                    JOptionPane.showMessageDialog(CollectSampleWizard.this,
                            "There is already a sample with the name " + name + ". Please try again using a different name.",
                            "Error",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                SaveSampleProgressDialog dialog = new SaveSampleProgressDialog(desktop);
                desktop.add(dialog);
                dialog.moveToFront();

                // Throws focus back to the collect sample wizard, and then
                // closes it.
                try {
                    dialog.setSelected(true);
                } catch (PropertyVetoException ignored) {

                }

                dispose();
            }
        });

        return toolbar;
    }

    private JPanel createMainPage() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        Box mainB = Box.createVerticalBox();

        if (model.isLimitResource()) {
            JLabel unitCostL = new JLabel();

            JTextField moneyTxt = new JTextField(10);
            JTextField unitCostTxt = new JTextField(10);
            txtTotalMoney = new JTextField(10);

            moneyTxt.setText(nf.format(model.getCurrentBalance()));
            moneyTxt.setEditable(false);
            moneyTxt.setBackground(Color.LIGHT_GRAY);
            moneyTxt.setMaximumSize(moneyTxt.getPreferredSize());

            unitCostTxt.setEditable(false);
            unitCostTxt.setBackground(Color.LIGHT_GRAY);
            unitCostTxt.setMaximumSize(unitCostTxt.getPreferredSize());

            txtTotalMoney.setEditable(false);
            txtTotalMoney.setBackground(Color.LIGHT_GRAY);
            txtTotalMoney.setMaximumSize(txtTotalMoney.getPreferredSize());

            if (isExptObsOrInt(expName)) {
                unitCostL.setText("Unit cost (observation): ");
                unitCostTxt.setText(nf.format(model.getCostPerObs()));
            } else {
                unitCostL.setText("Unit cost (intervention): ");
                unitCostTxt.setText(nf.format(model.getCostPerIntervention()));
            }

            Box b1 = Box.createHorizontalBox();
            b1.add(new JLabel("Money left: "));
            b1.add(Box.createHorizontalGlue());
            b1.add(moneyTxt);

            Box b2 = Box.createHorizontalBox();
            b2.add(unitCostL);
            b2.add(Box.createHorizontalGlue());
            b2.add(unitCostTxt);

            Box b3 = Box.createHorizontalBox();
            b3.add(new JLabel("Total cost: "));
            b3.add(Box.createHorizontalGlue());
            b3.add(txtTotalMoney);

            mainB.add(b1);
            mainB.add(b2);
            mainB.add(b3);
            mainB.add(Box.createVerticalStrut(10));
        }

        txtSampleSize = new JTextField(10);
        txtSampleName = new JTextField(10);
        txtSampleSize.getDocument().addDocumentListener(this);

        txtSampleSize.setMaximumSize(txtSampleSize.getPreferredSize());
        txtSampleName.setMaximumSize(txtSampleName.getPreferredSize());

        Box b4 = Box.createHorizontalBox();
        b4.add(new JLabel("Enter sample size:"));
        b4.add(Box.createHorizontalGlue());
        b4.add(txtSampleSize);

        Box b5 = Box.createHorizontalBox();
        b5.add(new JLabel("Enter a name for this sample:"));
        b5.add(Box.createHorizontalStrut(5));
        b5.add(txtSampleName);

        mainB.add(b4);
        mainB.add(b5);

        panel.add(mainB);

        return panel;
    }

    /**
     * Checks that the sample size given is not null and is a valid number.
     *
     * @return sample size that the user inputs.
     */
    private int getSampleSize() {
        int size;

        if (txtSampleSize.getText() == null) {
            JOptionPane.showMessageDialog(CollectSampleWizard.this,
                    "You need to enter a sample size.",
                    "Error",
                    JOptionPane.INFORMATION_MESSAGE);

            return -1;
        }

        try {
            size = Integer.parseInt(txtSampleSize.getText());

            if (size < 0 || (im != null && size < im.getVariableNodes().size())) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ne) {
            JOptionPane.showMessageDialog(CollectSampleWizard.this,
                    "That is not a valid number!",
                    "Error",
                    JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }

        return size;
    }

    /**
     * Checks if this experiment is an observational one or interventional one.
     * The experiment is an interventional one if at least one of the variables
     * is locked or randomized in the experiment.
     *
     * @return true if it's an observational one.
     */
    private boolean isExptObsOrInt(String expName) {
        String[] varNames = model.getExperimentalSetupStudiedVariablesNames(expName);

        for (String varName : varNames) {
            ManipulationType type = model.getExperimentalVariableManipulation(expName, varName).getType();
            if ((type.toString().equals(ManipulationType.LOCKED.toString())) ||
                    (type.toString().equals(ManipulationType.RANDOMIZED.toString()))) {

                return false;
            }
        }

        return true;
    }

    /**
     * Help function to calculate the total cost of the sample based on the sample
     * size and the per unit cost.
     */
    private int getTotalCostofSample() {

        if (!model.isLimitResource()) return -1;

        int totalCost;
        int size = Integer.parseInt(txtSampleSize.getText());

        if (isExptObsOrInt(expName)) {
            totalCost = size * model.getCostPerObs();
        } else {
            totalCost = size * model.getCostPerIntervention();
        }
        return totalCost;
    }

    /**
     * When the user updates the sample size, this method automatically updates
     * the sample name (based on a valid sample size) and checks that the total
     * cost is in fact affordable. The total cost will turn red if it is more
     * than the getModel balance.
     */
    public void insertUpdate(DocumentEvent documentEvent) {

        // Make sure the sample size is a valid number
        int size;
        try {
            size = Integer.parseInt(txtSampleSize.getText());
        } catch (NumberFormatException ne) {
            txtTotalMoney.setText("");
            txtSampleName.setText("");
            return;
        }

        // Change the total cost in the textfield
        if (model.isLimitResource()) {
            int totalCost = getTotalCostofSample();
            txtTotalMoney.setText(nf.format(totalCost));

            if (totalCost > model.getCurrentBalance()) {
                txtTotalMoney.setForeground(Color.red);
            } else {
                txtTotalMoney.setForeground(Color.black);
            }
        }

        // Change the sample name if the sample size is a valid number
        txtSampleName.setText("S" + Integer.toString(size));
    }

    public void removeUpdate(DocumentEvent documentEvent) {
        insertUpdate(documentEvent);
    }

    public void changedUpdate(DocumentEvent documentEvent) {
    }
}
