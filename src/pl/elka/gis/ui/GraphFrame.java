package pl.elka.gis.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.Toolkit;

import javax.swing.JFrame;

import pl.elka.gis.logic.Controller;
import pl.elka.gis.ui.components.GraphPaintingPanel;
import pl.elka.gis.utils.AppConstants;

public class GraphFrame extends JFrame {

    private static final String LOG_TAG = "GraphFrame";
    private GraphPaintingPanel mGraphUIPanel;
    private Controller mController;

    public GraphFrame(Point parentLocation) {
        super("Graph frame");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(parentLocation.x + 100, parentLocation.y + 100);
        mGraphUIPanel = new GraphPaintingPanel();
        Container con = getContentPane();
        mGraphUIPanel.setPreferredSize(new Dimension(AppConstants.MAX_X_Y_VALUE, AppConstants.MAX_X_Y_VALUE));
        ScrollPane scroll = new ScrollPane();
        scroll.add(mGraphUIPanel);
        con.add(scroll, BorderLayout.CENTER);
        setSize(screenSize.width / 4, screenSize.height / 4);
        setPreferredSize(new Dimension(screenSize.width / 4, screenSize.height / 4));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void setPlainGraphController(Controller pController) {
        mController = pController;
        mGraphUIPanel.setPlainGraphController(mController);
    }

    public void refreshGraph() {
        mGraphUIPanel.refreshGraph();
    }
}
