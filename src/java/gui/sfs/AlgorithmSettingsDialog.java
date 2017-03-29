package gui.sfs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by root on 14.11.16.
 */
public class AlgorithmSettingsDialog extends JDialog {

    public AlgorithmSettingsDialog(JFrame mainFrame, String name, Dialog.ModalityType modalityType) {
        super(mainFrame, name, modalityType);
    }

    public Integer steps;
    public Double q;
    public Double lm;

    public AlgorithmSettingsDialog() {
        super();
    }

    public static void main(String[] args) {
        AlgorithmSettingsDialog a = new AlgorithmSettingsDialog();
        a.startFrame();
    }

    public void startFrame() {
        this.setPreferredSize(new Dimension(450, 350));
        this.pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Computation Settings");

        this.setLayout(new GridLayout(3, 1));
        JPanel up = new JPanel(new GridLayout(1, 2));
        JPanel upLeft = new JPanel(new GridLayout(2, 1));
        upLeft.add(new JLabel("Albedo:"));
        JSlider albedoSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        albedoSlider.setMajorTickSpacing(25);
        albedoSlider.setMinorTickSpacing(10);
        albedoSlider.setPaintTicks(true);
        albedoSlider.setPaintLabels(true);
        upLeft.add(albedoSlider);

        JPanel upRight = new JPanel(new GridLayout(2, 1));
        upRight.add(new JLabel("Smoothness:"));
        JSlider regularSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 10);
        regularSlider.setMajorTickSpacing(25);
        regularSlider.setMinorTickSpacing(10);
        regularSlider.setPaintTicks(true);
        regularSlider.setPaintLabels(true);
        upRight.add(regularSlider);

        up.add(upLeft);
        up.add(upRight);


        JPanel down = new JPanel(new GridLayout(2, 1));
        down.add(new JLabel("Calculation steps"));
        JSlider stepsSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 20);
        stepsSlider.setMajorTickSpacing(25);
        stepsSlider.setMinorTickSpacing(10);
        stepsSlider.setPaintTicks(true);
        stepsSlider.setPaintLabels(true);
        down.add(stepsSlider);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton buttonOk = new JButton("OK");
        JButton buttonCancel = new JButton("Cancel");
        buttonPanel.add(buttonCancel, BorderLayout.WEST);
        buttonPanel.add(buttonOk, BorderLayout.EAST);
        buttonOk.addActionListener(actionEvent -> {
            q = ((double) (albedoSlider.getValue() + 1) / 100);
            lm = ((double) (regularSlider.getValue() + 1) / 100);
            steps = stepsSlider.getValue() + 1;
            endDialog();
        });
        buttonCancel.addActionListener(actionEvent -> {
            steps = -1;
            endDialog();
        });

        this.add(up);
        this.add(down);
        this.add(buttonPanel);
        setVisible(true);
        pack();
    }

    private void endDialog() {
        this.dispose();
    }
}
