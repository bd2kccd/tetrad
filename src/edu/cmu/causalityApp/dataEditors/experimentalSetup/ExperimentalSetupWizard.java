package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.CreateExperimentalSetupCommand;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.command.Command;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * To change this template use Options | File Templates.
 *
 * @author greg
 */
class ExperimentalSetupWizard extends JInternalFrame {
    private final ExperimentalSetup studiedVariables;
    private final CausalityLabModel model;

    public ExperimentalSetupWizard(CausalityLabModel model, String name) {
        super("Experimental Setup Wizard", true, true, true, true);

        this.model = model;

        studiedVariables = model.getEmptyExperimentalSetup(name);

        setContentPane(makePage1());
        pack();
    }


    private JPanel makePage1() {
        JPanel page1 = new JPanel();

        JPanel p1Buttons = new JPanel();
        p1Buttons.setPreferredSize(new Dimension(300, 50));


        page1.setLayout(new BoxLayout(page1, BoxLayout.PAGE_AXIS));

        p1Buttons.setLayout(new FlowLayout());

        // declaring buttons
        JButton cancel = new JButton("Cancel");
        JButton next = new JButton("Next");
        JButton previous = new JButton("Previous");
        JButton finish = new JButton("Finish");

        //first page, so the previous and finish button is disabled
        previous.setEnabled(false);
        finish.setEnabled(false);


        // putting the buttons on the UI
        page1.add(new ObserveVariablesPanel(studiedVariables));
        page1.add(p1Buttons);

        p1Buttons.add(cancel);
        p1Buttons.add(Box.createHorizontalGlue());
        p1Buttons.add(previous);
        p1Buttons.add(next);
        p1Buttons.add(finish);


        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // if bayesian, go to another page2 that doesn't link up to page 3
                // have an if and else loop around here...
                if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
                    setContentPane(makePage2Bayes());
                    pack();
                } else {
                    setContentPane(makePage2Structural());
                    pack();
                }
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        return page1;
    }


    private JPanel makePage2Structural() {

        JPanel page2Structural = new JPanel();
        JPanel p2Buttons = new JPanel();
        page2Structural.setLayout(new BoxLayout(page2Structural, BoxLayout.PAGE_AXIS));
        p2Buttons.setLayout(new FlowLayout());

        // declaring the row of next, cancel, previous and finish buttons
        JButton previous = new JButton("Previous");
        JButton next = new JButton("Next");
        JButton cancel = new JButton("Cancel");
        JButton finish = new JButton("Finish");

        next.setEnabled(false);

        page2Structural.add(new InterveneOnVariablesPanel(studiedVariables, model, next, finish));
        page2Structural.add(p2Buttons);


        // add extra buttons into the UI
        p2Buttons.add(cancel);
        p2Buttons.add(Box.createHorizontalGlue());
        p2Buttons.add(previous);
        p2Buttons.add(next);
        p2Buttons.add(finish);
        p2Buttons.setPreferredSize(new Dimension(500, 50));


        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setContentPane(makePage1());
                pack();
            }
        });

        finish.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // todo : improvements on the final page..
                Command cmd = new CreateExperimentalSetupCommand(
                        CausalityLabModel.getModel(), studiedVariables);
                cmd.doIt();
                dispose();

            }
        });

        next.addActionListener(new ActionListener() {
            /*
            public void actionPerformed(ActionEvent e){
                Command cmd = new CreateExperimentalSetupCommand(
                        CausalityLabModel.getModel(), studiedVariables);
                cmd.doIt();
                dispose();
            }
            */
            public void actionPerformed(ActionEvent e) {

                JPanel page3Layout = new JPanel();
                JLabel page3Title = new JLabel("Randomizing Distribution");
                page3Title.setBorder(new EmptyBorder(6, 10, 0, 0));

                page3Layout.setLayout(new BorderLayout());
                page3Layout.add(page3Title, BorderLayout.NORTH);
                page3Layout.add(makePage3Structural(), BorderLayout.CENTER);

                setContentPane(page3Layout);

                pack();

            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        return page2Structural;
    }

    private JPanel makePage3Structural() {

        JPanel page3Structural = new JPanel();
        JPanel p3Buttons = new JPanel();
        page3Structural.setLayout(new BoxLayout(page3Structural, BoxLayout.PAGE_AXIS));
        p3Buttons.setLayout(new FlowLayout());

        JPanel svp = new SetVariablesPanel(studiedVariables, model);
        page3Structural.add(svp);
        page3Structural.add(p3Buttons);

        JButton previous = new JButton("Previous");
        JButton next = new JButton("Next");
        JButton finish = new JButton("Finish");
        JButton cancel = new JButton("Cancel");


        next.setEnabled(false);

        p3Buttons.add(cancel);
        p3Buttons.add(Box.createHorizontalGlue());
        p3Buttons.add(previous);
        p3Buttons.add(next);
        p3Buttons.add(finish);
        p3Buttons.setPreferredSize(new Dimension(500, 50));


        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setContentPane(makePage2Structural());
                pack();
            }
        });

        finish.addActionListener(new ActionListener() {

            // add the try catch here, check whether all numbers are of the correct number format
            // and put them into the array..

            //  String [] variables = studiedVariables.getVariableNames();


            public void actionPerformed(ActionEvent e) {
                Command cmd = new CreateExperimentalSetupCommand(
                        CausalityLabModel.getModel(), studiedVariables);
                cmd.doIt();
                dispose();
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        return page3Structural;
    }

    private JPanel makePage2Bayes() {

        JPanel page2Bayes = new JPanel();
        JPanel p2Buttons = new JPanel();
        page2Bayes.setLayout(new BoxLayout(page2Bayes, BoxLayout.PAGE_AXIS));
        p2Buttons.setLayout(new FlowLayout());

//        if (model.getModelType().equals(CausalityLabModel.BAYES)) {
//            page2.add(new InterveneOnVariablesPanel(studiedVariables, model));
//        } else if (model.getModelType().equals(CausalityLabModel.SEM)) {
//            page2.add(new InterveneOnVariablesPanel(studiedVariables));
//        }

        JButton previous = new JButton("Previous");
        JButton next = new JButton("Next");
        JButton finish = new JButton("Finish");
        JButton cancel = new JButton("Cancel");

        next.setEnabled(false);

        page2Bayes.add(new InterveneOnVariablesPanel(studiedVariables, model, next, finish));
        page2Bayes.add(p2Buttons);


        next.setEnabled(false);

        p2Buttons.add(cancel);
        p2Buttons.add(Box.createHorizontalGlue());
        p2Buttons.add(previous);
        p2Buttons.add(next);
        p2Buttons.add(finish);
        p2Buttons.setPreferredSize(new Dimension(500, 50));


        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setContentPane(makePage1());
                pack();
            }
        });

        finish.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Command cmd = new CreateExperimentalSetupCommand(
                        CausalityLabModel.getModel(), studiedVariables);
                cmd.doIt();

                dispose();
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        return page2Bayes;
    }

}
