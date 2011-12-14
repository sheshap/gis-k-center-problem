package pl.elka.gis.model.generator;

/**
 * used during loading/saving graph data to application
 * 
 * @author pasu
 */
public class DataValidator {

    public static boolean isValidFilename(String name) {
        if (name.length() == 0 || name.contains("/") || name.contains("\\")) {
            return false;
        }
        return true;
    }

    public static boolean isValidGraphData(int vertexCount, int edgeCreationProbability, int maxDegree, int minVertexesDistance) {
        if (minVertexesDistance >= DataGenerator.MAX_X_Y_VALUE / 4) {
            return false;
        }
        if (vertexCount > 50) {
            return false;
        }
        if (edgeCreationProbability > 100 || edgeCreationProbability < 0) {
            return false;
        }
        if (maxDegree > vertexCount - 1) {
            return false;
        }
        return true;
    }
}
