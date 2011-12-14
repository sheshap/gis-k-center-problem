package pl.elka.gis.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class CalculationProgressDialog extends JDialog {

    private Gauge mGauge;

    public CalculationProgressDialog(JFrame parentFrame, String title, boolean modal, int vertexC, int edgeC, int centerC) {
        super(parentFrame, title, modal);
        int w = 300, h = 120;
        setLocation(parentFrame.getLocationOnScreen().x + 200, parentFrame.getLocationOnScreen().y + 120);
        setSize(w, h);
        Container pane = getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        mGauge = new Gauge(Color.GREEN);
        JLabel l1 = new JLabel("Vertexes: " + vertexC);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l2 = new JLabel("Edges: " + edgeC);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l3 = new JLabel("Centers: " + centerC);
        l3.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(l1);
        pane.add(l2);
        pane.add(l3);
        pane.add(mGauge);
    }

    public void updateProgress(float progress) {
        setTitle("Calculation progress: " + progress + "%");
        mGauge.setCurrentAmount((int) progress);
    }
}
