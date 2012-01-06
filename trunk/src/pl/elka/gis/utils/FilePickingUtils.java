package pl.elka.gis.utils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

/**
 * @author Andrzej Makarewicz
 */
public class FilePickingUtils {

    private static final FileFilter DEFAULT_FILE_FILTER = new GraphFileFilter();

    public static File openFileChooser(JFrame ownerFrame) {
        JFileChooser fc = new JFileChooser(new File(AppConstants.DEFAULT_FOLDER_PATH));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(DEFAULT_FILE_FILTER);

        switch (fc.showOpenDialog(ownerFrame)) {
            case JFileChooser.APPROVE_OPTION :
                return fc.getSelectedFile();
            default :
                return null;
        }
    }
    private static class GraphFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f != null && (f.isDirectory() || AppConstants.DEFAULT_EXTENSION.equalsIgnoreCase(FileUtils.getExtension(f)));
        }

        @Override
        public String getDescription() {
            return "GPH";
        }
    }
}
