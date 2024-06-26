package mosqui2.view;

import javax.swing.*;
import java.awt.*;

// Class for camera feed GUI object
public class CamFeed extends JLayeredPane {

    JLabel statusLabel;

    public CamFeed(Canvas canvas) {
        setPreferredSize(new Dimension(640, 480));
        canvas.setSize(new Dimension(640, 480));
        statusLabel = new JLabel("OFFLINE");
        statusLabel.setBackground(Color.DARK_GRAY);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setSize(statusLabel.getPreferredSize());

        add(canvas, DEFAULT_LAYER);
        add(statusLabel, PALETTE_LAYER);

    }

    public void setStatus(boolean status) {
        if (status) {
            statusLabel.setText("LIVE");
            statusLabel.setBackground(Color.RED);
        } else {
            statusLabel.setText("OFFLINE");
            statusLabel.setBackground(Color.DARK_GRAY);
        }
    }
}
