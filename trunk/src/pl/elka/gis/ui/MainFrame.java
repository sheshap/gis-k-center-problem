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
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private JMenuItem[] mMenuItems;
    private GraphGenerationFrame mGraphGenerationFrame;
    private CalculationProgressDialog mProgressDialog;
    private boolean mGraphCalculationInProgress;
    private JLabel mStatusLabel;
    private String mLastStatusText;
    private GraphPainterPanel mGraphPanel;
    private ScrollPane mScrollPane;
    private Graph mGraph;
    private GraphResolver.Result mResult;
    private ExecutorService mResolvingExecutor;
    private Future mResolvingFuture;

    public MainFrame() {
        super(APP_NAME);
        WindowUtilities.setNativeLookAndFeel();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 8, screenSize.height / 8);
        setMinimumSize(new Dimension(600, 480));
        setSize(screenSize.width * 3 / 4, screenSize.height * 3 / 4);
        setPreferredSize(new Dimension(screenSize.width * 3 / 4, screenSize.height * 3 / 4));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        mResolvingExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), Executors
                .defaultThreadFactory(), new SimpleRejectedExecutionHandler());
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
    private static class SimpleRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Log.d(LOG_TAG, String.format("Executor (%s) rejects runnable (%s)", executor, r));
        }
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

    /******************** MENU AND ACTIONS ********************/
    private void setGraphOptions() {
        boolean isGraphLoaded = isGraphLoaded();
        mMenuItems[MENU_OPEN].setEnabled(!mGraphCalculationInProgress);
        mMenuItems[MENU_CLOSE].setEnabled(isGraphLoaded && !mGraphCalculationInProgress);
        mMenuItems[MENU_COUNT].setEnabled(isGraphLoaded && !mGraphCalculationInProgress);
        mMenuItems[MENU_STATS].setEnabled(isGraphLoaded);
        mMenuItems[MENU_PROGRESS].setEnabled(isGraphLoaded && mGraphCalculationInProgress);
        mMenuItems[MENU_GENERATE].setEnabled(!mGraphCalculationInProgress);
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

            @Override
            public void actionPerformed(ActionEvent e) {
                File f = FilePickingUtils.openFileChooser(MainFrame.this);
                if (f != null) {
                    try {
                        boolean isGraphLoaded = isGraphLoaded();
                        mGraph = Graph.fromFile(f);
                        mLastStatusText = String.format("%s :: vertexes=%d  edges=%d", f.getName(), mGraph.getVertexes().size(), mGraph
                                .getEdges()
                                .size());
                        mStatusLabel.setText(mLastStatusText);
                        GraphPainterPanel graphPanel = getOrCreateGraphPanel();
                        if (isGraphLoaded)
                            graphPanel.reset();
                        graphPanel.setGraph(mGraph);
                        mScrollPane.add(graphPanel);
                        graphPanel.repaint();
                        setGraphOptions();
                    } catch (InputMismatchException e1) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Invalid graph data.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (NoSuchElementException e1) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Invalid graph data.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (ArrayIndexOutOfBoundsException e1) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Invalid graph data.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        mMenuItems[MENU_CLOSE].addActionListener(new ActionListener() {

            @Override
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

            @Override
            public void actionPerformed(ActionEvent e) {
                mResolvingExecutor.shutdownNow();
                System.exit(0);
            }
        });
        mMenuItems[MENU_GENERATE].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mGraphGenerationFrame != null) {
                    mGraphGenerationFrame.dispose(); // dispose and recreate
                }
                mGraphGenerationFrame = new GraphGenerationFrame(MainFrame.this.getLocationOnScreen());
            }
        });
        mMenuItems[MENU_COUNT].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String val = (String) JOptionPane
                        .showInputDialog(MainFrame.this, "Number of centers:", "Count graph", JOptionPane.PLAIN_MESSAGE, null, null, "3");
                if ((val != null) && (val.length() > 0)) {
                    try {
                        final int centersCount = Integer.parseInt(val);
                        onCalculationStarts(centersCount);
                        Runnable resolveRunnable = new Runnable() {

                            @Override
                            public void run() {
                                GraphResolver resolver = new GraphResolver(mGraph);
                                resolver.resolve(centersCount, MainFrame.this);
                            }
                        };
                        mResolvingFuture = mResolvingExecutor.submit(resolveRunnable);
                    } catch (NumberFormatException ex) {
                        JOptionPane
                                .showMessageDialog(MainFrame.this, "The entered number of centers is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        mMenuItems[MENU_STATS].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showStatsDialog(MainFrame.this, mGraph, mResult);
            }
        });
        mMenuItems[MENU_PROGRESS].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mProgressDialog != null) {
                    mProgressDialog.setVisible(true);
                }
            }
        });
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(actions);
        setJMenuBar(bar);
    }

    /******************** CALCULATION CALLBACK ********************/
    private void onCalculationStarts(int centers) {
        onCalculationEnds(false, null);
        mGraphCalculationInProgress = true;
        mProgressDialog = new CalculationProgressDialog(this, "Calculation progress", MainFrame.this, mGraph, centers);
        mProgressDialog.setVisible(true);
        setGraphOptions();
    }
    private float mProgress = 0;

    @Override
    public void updateProgress(float progressValue) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + progressValue);
        if (mProgressDialog != null) {
            mProgressDialog.updateProgress(progressValue);
        }
        mStatusLabel.setText("Progress " + progressValue + "%");
    }

    @Override
    public void increaseProgress(float progressMade) {
        mProgress += progressMade;
        if (mProgress > 100) {
            mProgress = 100;
        }
        updateProgress(mProgress);
    }

    @Override
    public void calculationError(String errorMessage) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + errorMessage);
        onCalculationEnds(true, null);
        JOptionPane.showMessageDialog(MainFrame.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void calculationFinished(GraphResolver.Result result) {
        Log.d(LOG_TAG, Log.getCurrentMethodName());
        onCalculationEnds(true, result);
        showStatsDialog(MainFrame.this, mGraph, mResult);
    }

    @Override
    public void calculationStopped() {
        Log.d(LOG_TAG, Log.getCurrentMethodName());
        mResolvingFuture.cancel(true);
        onCalculationEnds(true, null);
    }

    private void onCalculationEnds(boolean repaint, GraphResolver.Result result) {
        if (mProgressDialog != null) {
            mProgressDialog.dispose();
            mProgressDialog = null;
        }
        mProgress = 0;
        if (repaint) {
            mResult = result;
            mGraphPanel.setResult(result);
            mGraphPanel.repaint();
        }
        mGraphCalculationInProgress = false;
        mStatusLabel.setText(mLastStatusText);
        setGraphOptions();
    }

    /******************** STATS DIALOG ********************/
    private static void showStatsDialog(Component parentComponent, Graph graph, GraphResolver.Result result) {
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
        if (result != null) {
            sb.append("\n");
            sb.append("Centers: ").append(result.getCentersCount()).append("\n");
            sb.append("Longest path: ").append(result.getLongest()).append("\n");
        }
        JOptionPane.showMessageDialog(parentComponent, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
}
