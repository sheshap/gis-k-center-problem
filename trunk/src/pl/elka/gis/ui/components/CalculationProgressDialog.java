package pl.elka.gis.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import pl.elka.gis.model.Graph;

public class CalculationProgressDialog extends JDialog {

    private Gauge mGauge;

    public CalculationProgressDialog(JFrame parentFrame, String title, final ProgressCallback progressCallback, Graph graph, int centers) {
        super(parentFrame, title, false);
        Dimension parentSize = parentFrame.getSize();
        int w = parentSize.width / 3, h = parentSize.height / 5;
        setSize(w, h);
        Point locationOnScreen = parentFrame.getLocationOnScreen();
        setLocation(locationOnScreen.x + parentSize.width / 3, locationOnScreen.y + parentSize.height * 2 / 5);

        Container pane = getContentPane();

        mGauge = new Gauge(Color.GREEN);

        JLabel l1 = new JLabel("Vertexes: " + graph.getVertexesCount());
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l2 = new JLabel("Edges: " + graph.getEdgesCount());
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l3 = new JLabel("Centers: " + centers);
        l3.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton stopButton = new JButton("Cancel");
        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (progressCallback != null)
                    progressCallback.calculationStopped();
            }
        });
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(l1);
        pane.add(l2);
        pane.add(l3);
        pane.add(mGauge);
        pane.add(stopButton);
    }

    public void updateProgress(float progress) {
        setTitle(String.format("Calculation progress: %.3f%s", progress, "%"));
        mGauge.setCurrentAmount((int) progress);
    }
}
