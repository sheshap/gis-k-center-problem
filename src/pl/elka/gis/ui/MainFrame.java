package pl.elka.gis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import pl.elka.gis.logic.GraphResolver;
import pl.elka.gis.model.Graph;
import pl.elka.gis.ui.components.CalculationProgressDialog;
import pl.elka.gis.ui.components.GraphPainterPanel;
import pl.elka.gis.ui.components.ProgressCallback;
import pl.elka.gis.utils.AppConstants;
import pl.elka.gis.utils.FilePickingUtils;
import pl.elka.gis.utils.Log;
import pl.elka.gis.utils.WindowUtilities;

/**
 * @author pasu
 * @author Andrzej Makarewicz
 */
public class MainFrame extends JFrame implements ProgressCallback {

    private static final String LOG_TAG = MainFrame.class.getSimpleName();
    private static final String APP_NAME = "K-graph center solver";
    private GraphGenerationFrame mGraphGenerationFrame;
    private CalculationProgressDialog mProgressDialog;
    private boolean mGraphCalculationInProgress;
    private JLabel mStatusLabel;
    private String mLastStatusText;
    private GraphPainterPanel mGraphPanel;
    private ScrollPane mScrollPane;
    private Graph mGraph;
    private GraphResolver mGraphResolver;
    private JMenuItem[] mMenuItems;

    public MainFrame() {
        super(APP_NAME);
        WindowUtilities.setNativeLookAndFeel();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 8, screenSize.height / 8);
        setSize(screenSize.width * 3 / 4, screenSize.height * 3 / 4);
        setPreferredSize(new Dimension(screenSize.width * 3 / 4, screenSize.height * 3 / 4));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        prepareMenu();

        mScrollPane = new ScrollPane();

        mLastStatusText = "Idle";
        mStatusLabel = new JLabel(mLastStatusText);
        mStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        Container con = getContentPane();
        con.add(mStatusLabel, BorderLayout.SOUTH);
        con.add(mScrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private GraphPainterPanel getOrCreateGraphPanel() {
        if (mGraphPanel == null) {
            mGraphPanel = new GraphPainterPanel();
            mGraphPanel.setPreferredSize(new Dimension(AppConstants.MAX_X_Y_VALUE, AppConstants.MAX_X_Y_VALUE));
        }
        return mGraphPanel;
    }

    private boolean isGraphLoaded() {
        return mGraph != null;
    }

    private void setGraphOptions() {
        boolean isGraphLoaded = isGraphLoaded();

        mMenuItems[MENU_CLOSE].setEnabled(isGraphLoaded);
        mMenuItems[MENU_COUNT].setEnabled(isGraphLoaded);
        mMenuItems[MENU_STATS].setEnabled(isGraphLoaded);
        mMenuItems[MENU_PROGRESS].setEnabled(isGraphLoaded && mGraphCalculationInProgress);
    }

    private static final int MENU_OPEN = 0, MENU_CLOSE = 1, MENU_EXIT = 2, MENU_GENERATE = 3, MENU_COUNT = 4, MENU_STATS = 5,
            MENU_PROGRESS = 6;

    private void prepareMenu() {
        mMenuItems = new JMenuItem[7];

        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        mMenuItems[MENU_OPEN] = new JMenuItem("Open");
        mMenuItems[MENU_OPEN].setMnemonic('O');
        file.add(mMenuItems[MENU_OPEN]);
        mMenuItems[MENU_CLOSE] = new JMenuItem("Close");
        mMenuItems[MENU_CLOSE].setMnemonic('W');
        file.add(mMenuItems[MENU_CLOSE]);
        mMenuItems[MENU_EXIT] = new JMenuItem("Exit");
        mMenuItems[MENU_EXIT].setMnemonic('X');
        file.add(mMenuItems[MENU_EXIT]);

        JMenu actions = new JMenu("Actions");
        actions.setMnemonic('F');

        mMenuItems[MENU_GENERATE] = new JMenuItem("Generate graph");
        mMenuItems[MENU_GENERATE].setMnemonic('G');
        actions.add(mMenuItems[MENU_GENERATE]);
        mMenuItems[MENU_COUNT] = new JMenuItem("Count graph");
        mMenuItems[MENU_COUNT].setMnemonic('C');
        actions.add(mMenuItems[MENU_COUNT]);
        mMenuItems[MENU_STATS] = new JMenuItem("Statistics");
        mMenuItems[MENU_STATS].setMnemonic('S');
        actions.add(mMenuItems[MENU_STATS]);
        mMenuItems[MENU_PROGRESS] = new JMenuItem("Show progress");
        mMenuItems[MENU_PROGRESS].setMnemonic('P');
        actions.add(mMenuItems[MENU_PROGRESS]);

        setGraphOptions();

        mMenuItems[MENU_OPEN].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mGraphCalculationInProgress) {
                    JOptionPane
                            .showMessageDialog(MainFrame.this, "Calculation in progress.\nPlease wait until finished.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                File f = FilePickingUtils.openFileChooser(MainFrame.this);
                if (f != null) {
                    try {
                        mGraph = Graph.fromFile(f);
                        mGraphResolver = new GraphResolver(mGraph);

                        mLastStatusText = String.format("Graph: %s, vertexes: %d, edges: %d", f.getName(), mGraph
                                .getVertexes()
                                .size(), mGraph.getEdges().size());
                        mStatusLabel.setText(mLastStatusText);

                        GraphPainterPanel graphPanel = getOrCreateGraphPanel();
                        graphPanel.setGraph(mGraph);
                        mScrollPane.add(graphPanel);
                        graphPanel.revalidate();

                        setGraphOptions();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mMenuItems[MENU_CLOSE].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mScrollPane.removeAll();
                mGraphPanel = null;
                mGraph = null;

                mLastStatusText = "Idle";
                mStatusLabel.setText(mLastStatusText);

                setGraphOptions();
            }
        });

        mMenuItems[MENU_EXIT].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // actions menu
        mMenuItems[MENU_GENERATE].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mGraphGenerationFrame != null) {
                    mGraphGenerationFrame.dispose(); // dispose and recreate
                }
                mGraphGenerationFrame = new GraphGenerationFrame(MainFrame.this.getLocationOnScreen());
            }
        });
        mMenuItems[MENU_COUNT].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isGraphLoaded()) {
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
                        mGraphResolver.resolve(centersCount, MainFrame.this);

                    } catch (NumberFormatException ex) {
                        JOptionPane
                                .showMessageDialog(MainFrame.this, "Wrong centers number.", "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
        mMenuItems[MENU_STATS].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isGraphLoaded()) {
                    JOptionPane.showMessageDialog(MainFrame.this, "No graph loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                showStatsDialog(MainFrame.this, mGraph);
            }
        });
        mMenuItems[MENU_PROGRESS].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isGraphLoaded()) {
                    JOptionPane.showMessageDialog(MainFrame.this, "No graph loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (mProgressDialog != null) {
                    mProgressDialog.setVisible(true);
                } else {
                    mProgressDialog = new CalculationProgressDialog(MainFrame.this, "Calculation progress", mGraph, 1);
                    mProgressDialog.setVisible(true);
                }
            }
        });
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        bar.add(file);
        bar.add(actions);
    }

    private void startGraphProcessing() {
        if (mGraphGenerationFrame != null) {
            mGraphGenerationFrame.dispose();
        }
        mGraphCalculationInProgress = true;
        mProgressDialog = new CalculationProgressDialog(this, "Calculation progress", mGraph, 1);
        mProgressDialog.setVisible(true);
    }

    private float mProgress = 0.0f;

    @Override
    public void updateProgress(float progressValue) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + progressValue);

        if (mProgressDialog != null) {
            mProgressDialog.updateProgress(progressValue);
        }
        mStatusLabel.setText("Progress " + progressValue + "%");
    }

    @Override
    public void increaseProgress(float progressToAdd) {
        Log.d(LOG_TAG, Log.getCurrentMethodName());

        mProgress += progressToAdd;
        if (mProgress > 100) {
            mProgress = 100;
        }
        updateProgress(mProgress);
    }

    @Override
    public void calculationError(String errorMessage) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + errorMessage);

        if (mProgressDialog != null) {
            mProgressDialog.dispose();
        }
        JOptionPane.showMessageDialog(MainFrame.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
        mGraphCalculationInProgress = false;
        mStatusLabel.setText(mLastStatusText);
        setGraphOptions();
    }

    @Override
    public void calculationFinished() {
        Log.d(LOG_TAG, Log.getCurrentMethodName());

        if (mProgressDialog != null) {
            mProgressDialog.dispose();
        }
        // mGraphPanel.refreshGraph();

        mGraphCalculationInProgress = false;
        mStatusLabel.setText(mLastStatusText);

        setGraphOptions();
        showStatsDialog(MainFrame.this, mGraph);
    }

    private static void showStatsDialog(Component parentComponent, Graph graph) {
        if (parentComponent == null)
            return;

        if (graph == null) {
            JOptionPane.showMessageDialog(parentComponent, "Error showing graph statistics.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Vertexes: ").append(graph.getVertexes().size()).append("\n");
        sb.append("Edges: ").append(graph.getEdges().size()).append("\n");
        sb.append("Subgraphs: ").append(graph.getSubgraphsCount() + 1).append("\n");

        JOptionPane.showMessageDialog(parentComponent, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
}
