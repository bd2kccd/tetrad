package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.CausalityLabModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * To change this template use Options | File Templates.
 *
 * @author greg
 */
public class ExperimentalSetupView extends JPanel {
    private final String name;
    private final ExperimentalSetupVariableView view;

    public ExperimentalSetupView(final CausalityLabModel model, final JDesktopPane desktop, String name) {
        this.name = name;
        view = new ExperimentalSetupVariableView(model, name);

        JPanel samplePane = new JPanel();

        JButton newSampleButton = new JButton("Collect data / Run experiment");
        samplePane.add(Box.createHorizontalGlue());
        samplePane.add(newSampleButton);

        newSampleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CollectSampleWizard(model, desktop, getExpName());
            }
        });

        this.setLayout(new BorderLayout());
        this.add(samplePane, BorderLayout.SOUTH);
        this.add(view, BorderLayout.CENTER);
    }

    public String getExpName() {
        return name;
    }

    public ExperimentalSetupVariableView getView() {
        return view;
    }
}