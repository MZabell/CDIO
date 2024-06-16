package org.view;

import org.utils.Calibrator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Menu extends JPanel implements ActionListener {
    public Menu() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JButton calibrateButton = new JButton("Calibrate");
        calibrateButton.addActionListener(this);
        add(calibrateButton);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(this);
        add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        add(stopButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(this);
        add(exitButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Calibrate":
                Calibrator.calibrate();
                break;
            case "Start":
                //CamFeed.start();
                break;
            case "Stop":
                //CamFeed.stop();
                break;
            case "Exit":
                System.exit(0);
                break;
        }
    }
}
