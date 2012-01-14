package pl.elka.gis.model.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;
import pl.elka.gis.utils.AppConstants;

/**
 * class for reading data from file
 * 
 * @author pasu
 */
public class FileHandler {

    public static void writeFileContent(String filename, Vector<GVertex> vertexes, Set<GEdge> edges) throws IOException {
        FileWriter outFile = new FileWriter(AppConstants.DEFAULT_FOLDER_PATH + filename + "." + AppConstants.DEFAULT_EXTENSION);
        PrintWriter out = new PrintWriter(outFile);
        out.println(vertexes.size() + " " + edges.size());
        GVertex ver;
        GEdge ed;
        for (int i = 0; i < vertexes.size(); i++) {
            ver = vertexes.elementAt(i);
            out.println(ver.getCoord().x + " " + ver.getCoord().y);
        }
        for (Iterator<GEdge> iterator = edges.iterator(); iterator.hasNext();) {
            ed = iterator.next();
            out.println(ed.getStartingVertexId() + " " + ed.getEndingVertexId());
        }
        out.close();
    }
}
