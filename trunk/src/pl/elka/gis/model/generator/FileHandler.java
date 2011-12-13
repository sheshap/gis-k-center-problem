package pl.elka.gis.model.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;

/**
 * class for reading data from file
 * 
 * @author pasu
 */
public class FileHandler {

    public static Object readSourceFileContent() {
        return null;
    }

    public static boolean writeFileContent(String filename, Vector<GVertex> vertexes, Set<GEdge> edges) {
        try {
            FileWriter outFile = new FileWriter(filename);
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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
