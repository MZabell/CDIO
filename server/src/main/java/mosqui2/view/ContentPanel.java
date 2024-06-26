package mosqui2.view;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {
    public ContentPanel(CamFeed feed, Menu menu) {
        setLayout(new BorderLayout());
        add(feed, BorderLayout.WEST);
        add(menu, BorderLayout.EAST);
    }
}
