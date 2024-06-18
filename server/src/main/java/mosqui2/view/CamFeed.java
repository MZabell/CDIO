package mosqui2.view;

import javax.swing.*;
import java.awt.*;

public class CamFeed extends JLayeredPane {
    public CamFeed(Canvas canvas) {
        setPreferredSize(new Dimension(640, 480));
        canvas.setSize(new Dimension(640, 480));
        JLabel label = new JLabel("LIVE");
        label.setBackground(Color.RED);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setSize(label.getPreferredSize());

        add(canvas, DEFAULT_LAYER);
        add(label, PALETTE_LAYER);

    }
}
