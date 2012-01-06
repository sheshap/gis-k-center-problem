package pl.elka.gis.utils;

import java.io.File;

/**
 * @author Andrzej Makarewicz
 */
public class FileUtils {

    public static String getExtension(File f) {
        String ext = null;
        if (f != null) {
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1);
            }
        }
        return ext;
    }
}
