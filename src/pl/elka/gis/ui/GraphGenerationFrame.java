package pl.elka.gis.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pl.elka.gis.model.generator.DataGenerator;
import pl.elka.gis.model.generator.DataValidationException;
import pl.elka.gis.utils.AppConstants;

/**
 * graph creation frame
 * 
 * @author Rekin
 */
public class GraphGenerationFrame extends JFrame {

    private LabelAndText[] mLines;
    private DataGenerator mGenerator;
    private JButton mButtonGenerate;
    private JButton mButtonCancel;
    private JLabel mProcessing;

    public GraphGenerationFrame(Point parentTopLeft) {
        super("Graph Generator");
        mGenerator = new DataGenerator();
        initFrameElements();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        int w = 260, h = 270;
        this.setSize(w, h);
        this.setLocation(parentTopLeft.x + w / 4, parentTopLeft.y + h / 4);
        this.setVisible(true);
    }

    private void initFrameElements() {
        mProcessing = new JLabel("Processing...");
        mProcessing.setVisible(false);
        mProcessing.setAlignmentX(0);
        mProcessing.setForeground(Color.RED);
        Font curFont = mProcessing.getFont();
        mProcessing.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 20));
        mLines = new LabelAndText[5];
        mLines[0] = new LabelAndText("Filename: ", 20, -1);// -1 says "don't set text"
        mLines[1] = new LabelAndText("Vertex count: ", 10, mGenerator.getVertexCount());
        mLines[2] = new LabelAndText("Max vertex degree: ", 10, mGenerator.getMaxVertexDegree());
        mLines[3] = new LabelAndText("Edge propability <0,100>: ", 10, mGenerator.getEdgesProbability());
        mLines[4] = new LabelAndText("Min vertex distance: ", 10, mGenerator.getMinVertexCoordDifference());
        Container pane = getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(mProcessing);
        for (int i = 0; i < mLines.length; ++i) {
            pane.add(mLines[i]);
        }
        JPanel bottomButtons = new JPanel(new FlowLayout());
        mButtonGenerate = new JButton("Generate");
        mButtonGenerate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean generationSuccessful = true;
                try {
                    mProcessing.setVisible(true);
                    mGenerator.setVertexesCount(Integer.parseInt(mLines[1].getTextField().getText()));
                    mGenerator.setMaxVertexDegree(Integer.parseInt(mLines[2].getTextField().getText()));
                    mGenerator.setEdgesProbability(Integer.parseInt(mLines[3].getTextField().getText()));
                    mGenerator.setMinVertexCoordDifference(Integer.parseInt(mLines[4].getTextField().getText()));
                    mGenerator.generateData();
                    mGenerator.saveData(mLines[0].getTextField().getText());
                } catch (NumberFormatException e) {
                    generationSuccessful = false;
                    e.printStackTrace();
                } catch (DataValidationException e) {
                    generationSuccessful = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    generationSuccessful = false;
                    e.printStackTrace();
                }
                if (generationSuccessful) {
                    JOptionPane
                            .showMessageDialog(GraphGenerationFrame.this, "Saved generated graph to \""
                                    + mLines[0].getTextField().getText() + "." + AppConstants.DEFAULT_EXTENSION + "\" file.", "Done", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane
                            .showMessageDialog(GraphGenerationFrame.this, "Error generating graph.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                mProcessing.setVisible(false);
            }
        });
        mButtonCancel = new JButton("Cancel");
        mButtonCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GraphGenerationFrame.this.dispose();
            }
        });
        bottomButtons.add(mButtonGenerate);
        bottomButtons.add(mButtonCancel);
        pane.add(bottomButtons);
    }
    private class LabelAndText extends JPanel {

        private JTextField mTextField;

        public LabelAndText(String labelText, int textColumns, int defaultValue) {
            setLayout(new FlowLayout());
            add(new JLabel(labelText));
            mTextField = new JTextField(defaultValue > 0 ? String.valueOf(defaultValue) : "", textColumns);
            add(mTextField);
        }

        public JTextField getTextField() {
            return mTextField;
        }
    }
}
