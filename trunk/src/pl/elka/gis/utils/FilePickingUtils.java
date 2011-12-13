package pl.elka.gis.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import pl.elka.gis.model.generator.FileHandler;

public class FilePickingUtils {

    public static File openFileChooser(JFrame ownerFrame) {
        String filename = FileHandler.GRAPHS_FOLDER_PATH;
        JFileChooser fc = new JFileChooser(new File(filename));
        // Show open dialog; this method does not return until the dialog is closed
        fc.showOpenDialog(ownerFrame);
        return fc.getSelectedFile();
    }
}
