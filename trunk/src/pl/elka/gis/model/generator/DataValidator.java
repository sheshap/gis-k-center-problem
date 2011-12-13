package pl.elka.gis.model.generator;

/**
 * used during loading/saving graph data to application
 * 
 * @author pasu
 */
public class DataValidator {

    public static boolean isValidFilename(String name) {
        if (name.contains(":") || name.contains("/") || name.contains("\\") || name.length() < 2) {
            return false;
        }
        // TODO validate more?
        return true;
    }

    public static boolean isValidGraphData(int vertexCount, int edgeCount, int maxDegree, int minVertexesDistance) {
        if (minVertexesDistance >= DataGenerator.MAX_X_Y_VALUE / 4) {
            return false;
        }
        // if ((vertexCount * (vertexCount - 1)) / 2 <= edgeCount) {
        // return false;
        // }
        // TODO validate some more?
        return true;
    }
}
