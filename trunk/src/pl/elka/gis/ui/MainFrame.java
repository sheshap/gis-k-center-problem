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

import pl.elka.gis.logic.Controller;
import pl.elka.gis.model.generator.FileHandler;
import pl.elka.gis.utils.FilePickingUtils;
import pl.elka.gis.utils.Log;
import pl.elka.gis.utils.WindowUtilities;

public class MainFrame extends JFrame {

    private static final String LOG_TAG = "MainFrame";
    private static final String APP_NAME = "K-graph center solver";
    private Dimension mScreenSize;
    private Controller mController;

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
                Log.d(LOG_TAG, "Open is pressed");
                File f = FilePickingUtils.openFileChooser(MainFrame.this);
                if (f != null) {
                    try {
                        FileHandler.readSourceFileContent(f, mController);
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
                new GraphGenerationFrame(MainFrame.this.getLocationOnScreen());
            }
        });
        countGraph.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Log.d(LOG_TAG, "Count is pressed");
            }
        });
        stats.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Log.d(LOG_TAG, "Stats is pressed");
                Log.d(LOG_TAG, mController.getControllerData());
            }
        });
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        bar.add(file);
        bar.add(actions);
    }
}
