package pl.elka.gis.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import pl.elka.gis.logic.Controller;
import pl.elka.gis.model.ResultSet;
import pl.elka.gis.model.generator.DataGenerator;
import pl.elka.gis.model.generator.FileHandler;
import pl.elka.gis.ui.components.CalculationProgressDialog;
import pl.elka.gis.ui.components.GraphPaintingPanel;
import pl.elka.gis.ui.components.ProgressCallback;
import pl.elka.gis.utils.FilePickingUtils;
import pl.elka.gis.utils.Log;
import pl.elka.gis.utils.WindowUtilities;

public class MainFrame extends JFrame implements ProgressCallback {

    private static final String LOG_TAG = "MainFrame";
    private static final String APP_NAME = "K-graph center solver";
    private Dimension mScreenSize;
    private Controller mController;
    private boolean mIsGraphLoaded;
    private GraphGenerationFrame mGraphGenerationFrame;
    private CalculationProgressDialog mProgressDialog;
    private boolean mGraphCalculationInProgress;
    private JMenuItem mProgressOption;
    private JLabel mStatusLabel;
    private String mLastStatusText;
    private GraphPaintingPanel mGraphUIPanel;
    private long mGraphCalculateStartTime;
    private long mGraphCalculateEndTime;

    public MainFrame() {
        super(APP_NAME);
        WindowUtilities.setNativeLookAndFeel();
        mScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(mScreenSize.width / 4, mScreenSize.height / 4);
        prepareMenu();
        mGraphUIPanel = new GraphPaintingPanel();
        mLastStatusText = "  Idle";
        mStatusLabel = new JLabel(mLastStatusText);
        Container con = getContentPane();
        mGraphUIPanel.setPreferredSize(new Dimension(DataGenerator.MAX_X_Y_VALUE, DataGenerator.MAX_X_Y_VALUE));
        ScrollPane scroll = new ScrollPane();
        scroll.add(mGraphUIPanel);
        con.add(scroll, BorderLayout.CENTER);
        con.add(mStatusLabel, BorderLayout.SOUTH);
        setSize(mScreenSize.width / 2, mScreenSize.height / 2);
        setPreferredSize(new Dimension(mScreenSize.width / 2, mScreenSize.height / 2));
        mController = new Controller();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void prepareMenu() {
        JMenu file = new JMenu("File");
        file.setMnemonic('F');
        JMenuItem openItem = new JMenuItem("Open");
        openItem.setMnemonic('O');
        file.add(openItem);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        file.add(exitItem);
        JMenu actions = new JMenu("Actions");
        actions.setMnemonic('F');
        JMenuItem generateItem = new JMenuItem("Generate graph");
        generateItem.setMnemonic('G');
        actions.add(generateItem);
        JMenuItem countGraph = new JMenuItem("Count graph");
        countGraph.setMnemonic('C');
        actions.add(countGraph);
        JMenuItem stats = new JMenuItem("Statistics");
        stats.setMnemonic('S');
        actions.add(stats);
        mProgressOption = new JMenuItem("Show progress");
        mProgressOption.setMnemonic('p');
        mProgressOption.setEnabled(false);
        actions.add(mProgressOption);
        // adding action listener to menu items
        // file menu
        openItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mGraphCalculationInProgress) {
                    JOptionPane
                            .showMessageDialog(MainFrame.this, "Calculation in progress.\nPlease wait until finished.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                File f = FilePickingUtils.openFileChooser(MainFrame.this);
                if (f != null) {
                    try {
                        FileHandler.readSourceFileContent(f, mController);
                        mIsGraphLoaded = true;
                        mLastStatusText = "File: " + f.getName() + ", Vertexes=" + mController.getVertexSet().size() + ", Edges="
                                + mController.getEdgesMap().size();
                        mStatusLabel.setText(mLastStatusText);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        exitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        // actions menu
        generateItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mGraphGenerationFrame != null) {
                    mGraphGenerationFrame.dispose(); // dispose and recreate
                }
                mGraphGenerationFrame = new GraphGenerationFrame(MainFrame.this.getLocationOnScreen());
            }
        });
        countGraph.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!mIsGraphLoaded) {
                    JOptionPane.showMessageDialog(MainFrame.this, "No graph loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (mGraphCalculationInProgress) {
                    JOptionPane
                            .showMessageDialog(MainFrame.this, "Calculation in progress.\nPlease wait until finished.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String val = (String) JOptionPane
                        .showInputDialog(MainFrame.this, "Enter centers count:", "Centers count", JOptionPane.PLAIN_MESSAGE, null, null, "3");
                if ((val != null) && (val.length() > 0)) {
                    try {
                        int centersCount = Integer.parseInt(val);
                        mController.setCentersCount(centersCount);
                        startGraphProcessing();
                        mController.countGraphData(centersCount, MainFrame.this);
                        mProgressDialog.setVisible(true);
                    } catch (NumberFormatException ex) {
                        JOptionPane
                                .showMessageDialog(MainFrame.this, "Wrong centers number.", "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
        stats.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!mIsGraphLoaded) {
                    JOptionPane.showMessageDialog(MainFrame.this, "No graph loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                showStatsDialog();
                // print all data in the console
                Log.d(LOG_TAG, mController.getControllerData());
            }
        });
        mProgressOption.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mProgressDialog != null) {
                    mProgressDialog.setVisible(true);
                } else {
                    mProgressDialog = new CalculationProgressDialog(MainFrame.this, "Calculation progress", true, mController
                            .getVertexSet()
                            .size(), mController.getEdgesMap().size(), mController.getCentersCount());
                    mProgressDialog.setVisible(true);
                }
            }
        });
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        bar.add(file);
        bar.add(actions);
    }

    private void showStatsDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vertexes count: " + mController.getVertexSet().size() + "\n");
        sb.append("Edges count: " + mController.getEdgesMap().size() + "\n");
        sb.append("Centers count: " + mController.getCentersCount() + "\n");
        ResultSet lastResult = mController.getResultSet();
        sb.append("Centers in vertexes: " + lastResult.getCentersSetAsString() + "\n");
        sb.append("Longest path vertexes: " + lastResult.getLongestPathVertexSetAsString() + "\n");
        sb.append("Longest path weight: " + lastResult.getLongestPath() + "\n");
        if (mGraphCalculateStartTime != 0 && mGraphCalculateEndTime != 0) {
            long calTime = mGraphCalculateEndTime - mGraphCalculateStartTime;
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.ms");
            Date resultdate = new Date(calTime);
            sb.append("Calculation time: " + sdf.format(resultdate) + "\n");
        }
        JOptionPane.showMessageDialog(MainFrame.this, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startGraphProcessing() {
        if (mGraphGenerationFrame != null) {
            mGraphGenerationFrame.dispose(); // dispose this dialog
        }
        mGraphCalculateStartTime = mGraphCalculateEndTime = 0;
        mGraphCalculationInProgress = true;
        mProgressOption.setEnabled(true);
        mGraphCalculateStartTime = System.currentTimeMillis();
        mProgressDialog = new CalculationProgressDialog(this, "Calculation progress", true, mController.getVertexSet().size(), mController
                .getEdgesMap()
                .size(), mController.getCentersCount());
    }

    @Override
    public void updateProgress(float progressValue) {
        Log.d(LOG_TAG, "updateProgress=" + progressValue);
        if (mProgressDialog != null) {
            mProgressDialog.updateProgress(progressValue);
        }
        mStatusLabel.setText("  Progress " + progressValue + "%");
    }

    @Override
    public void calculationError(String errorMessage) {
        Log.d(LOG_TAG, "calculationError=" + errorMessage);
        mGraphCalculateEndTime = System.currentTimeMillis();
        if (mProgressDialog != null) {
            mProgressDialog.dispose();
        }
        JOptionPane.showMessageDialog(MainFrame.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
        mGraphCalculationInProgress = false;
        mProgressOption.setEnabled(false);
        mStatusLabel.setText(mLastStatusText);
    }

    @Override
    public void calculationFinished() {
        Log.d(LOG_TAG, "calculationFinished");
        mGraphCalculateEndTime = System.currentTimeMillis();
        if (mProgressDialog != null) {
            mProgressDialog.dispose();
        }
        // TODO refresh ui and show resultset as a info dialog
        mGraphCalculationInProgress = false;
        mProgressOption.setEnabled(false);
        mStatusLabel.setText(mLastStatusText);
        showStatsDialog();
    }
}
