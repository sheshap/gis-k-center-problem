package pl.elka.gis.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import pl.elka.gis.logic.Controller;
import pl.elka.gis.model.generator.FileHandler;
import pl.elka.gis.ui.components.CalculationProgressDialog;
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

    public MainFrame() {
        super(APP_NAME);
        WindowUtilities.setNativeLookAndFeel();
        mScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(mScreenSize.width / 2, mScreenSize.height / 2);
        setLocation(mScreenSize.width / 4, mScreenSize.height / 4);
        prepareMenu();
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
        // adding action listener to menu items
        // file menu
        openItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File f = FilePickingUtils.openFileChooser(MainFrame.this);
                if (f != null) {
                    try {
                        FileHandler.readSourceFileContent(f, mController);
                        mIsGraphLoaded = true;
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
                String val = (String) JOptionPane
                        .showInputDialog(MainFrame.this, "Enter centers count:", "Centers count", JOptionPane.PLAIN_MESSAGE, null, null, "3");
                if ((val != null) && (val.length() > 0)) {
                    try {
                        int centersCount = Integer.parseInt(val);
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
                StringBuilder sb = new StringBuilder();
                sb.append("Centers in vertexes: " + "" + "\n");
                sb.append("Longest path vertexes: " + "" + "\n");
                sb.append("Longest path weight: " + "" + "\n");
                JOptionPane.showMessageDialog(MainFrame.this, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
                // print all data in the console
                Log.d(LOG_TAG, mController.getControllerData());
            }
        });
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        bar.add(file);
        bar.add(actions);
    }

    private void startGraphProcessing() {
        if (mGraphGenerationFrame != null) {
            mGraphGenerationFrame.dispose(); // dispose this dialog
        }
        mProgressDialog = new CalculationProgressDialog(this, "Graph calculation progress", true);
        // TODO set location, size of dialog, set unable to close dialog (make invisible? use window listener?)
        // TODO or perhaps use an option "show progress" and create normal dialog?
        // mProgressDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // TODO processing dialog, time counting and such things
    }

    @Override
    public void updateProgress(float progressValue) {
        Log.d(LOG_TAG, "updateProgress=" + progressValue);
        mProgressDialog.updateProgress(progressValue);
    }

    @Override
    public void calculationError(String errorMessage) {
        Log.d(LOG_TAG, "calculationError=" + errorMessage);
        if (mProgressDialog != null) {
            mProgressDialog.dispose();
        }
        JOptionPane.showMessageDialog(MainFrame.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void calculationFinished() {
        Log.d(LOG_TAG, "calculationFinished");
        if (mProgressDialog != null) {
            mProgressDialog.dispose();
        }
        // TODO refresh ui and show resultset as a info dialog
    }
}
