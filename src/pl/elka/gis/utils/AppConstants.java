package pl.elka.gis.utils;

public class AppConstants {

    public static final int MAX_X_Y_VALUE = 2000; // max x or y value of vertex coord
    public static final String GRAPHS_FOLDER_PATH = "graph_files/";
    public static final String GRAPHS_EXTENSION = ".gph";

    /**
     * checks if given int value exists int the given int table
     * 
     * @param val
     * @param tab
     * @return
     */
    public static boolean contains(int val, int[] tab) {
        for (int i = 0; i < tab.length; ++i) {
            if (val == tab[i]) {
                return true;
            }
        }
        return false;
    }
}
