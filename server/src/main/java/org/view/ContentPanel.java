package org.view;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {
    public ContentPanel(CamFeed feed, Menu menu) {
        setLayout(new BorderLayout());
        add(feed, BorderLayout.WEST);
        //menu.setPreferredSize(feed.getPreferredSize());
        //menu.setOpaque(false);
        add(menu, BorderLayout.EAST);
        //setVisible(true);
    }
}
