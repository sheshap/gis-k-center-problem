package pl.elka.gis.model.generator;

import java.util.Set;
import java.util.Vector;

import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;

/**
 * used during loading/saving graph data to application
 * 
 * @author pasu
 */
public class DataValidator {

    public static boolean isValidFilename(String name) {
        // TODO validate filename (no '/', no ':' etc)
        return true;
    }

    public static boolean isValidGraphData(Vector<GVertex> vertexes, Set<GEdge> edges) {
        // TODO validate
        return true;
    }
}
