package mosqui2.view;

import javax.swing.*;
import java.awt.event.ActionListener;

public class Menu extends JPanel {
    public Menu(ActionListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JButton calibrateButton = new JButton("Calibrate");
        calibrateButton.addActionListener(listener);
        add(calibrateButton);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(listener);
        add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(listener);
        add(stopButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(listener);
        add(exitButton);
    }
}
