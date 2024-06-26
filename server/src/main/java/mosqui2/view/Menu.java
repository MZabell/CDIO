package mosqui2.view;

import javax.swing.*;
import java.awt.event.ActionListener;

// Class for menu buttons to provide User Experience
public class Menu extends JPanel {
    public Menu(ActionListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JButton calibrateButton = new JButton("Calibrate");
        calibrateButton.addActionListener(listener);
        add(calibrateButton);

        JButton scanButton = new JButton("Scan");
        scanButton.addActionListener(listener);
        add(scanButton);

        JButton mapButton = new JButton("Map");
        mapButton.addActionListener(listener);
        add(mapButton);

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
