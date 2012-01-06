package pl.elka.gis.model.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

import pl.elka.gis.logic.Controller;
import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;
import pl.elka.gis.utils.AppConstants;

/**
 * class for reading data from file
 * 
 * @author pasu
 */
public class FileHandler {

    public static void readSourceFileContent(File source, Controller appController) throws FileNotFoundException {
        Set<GVertex> vertexes = appController.getVertexSet();
        Vector<GVertex> supportVertexVector = new Vector<GVertex>(); // it is used for convenient operations on data set (for
                                                                     // counting
        // edges length)
        Vector<GEdge> supportEdgesVector = new Vector<GEdge>(); // convenient method to iterate over edges (for determining
                                                                // vertexes neighbors)
        MultiKeyMap edges = appController.getEdgesMap();
        Scanner scanner = new Scanner(source);
        int lineNum = 0;
        int vertexCount = 0;
        int edgeCount = 0; // don't really need this - just browse to the end of file
        int vx, vy; // vertex x and y coord
        int vId = 1; // vertex ID
        int edge1, edge2; // edge starting vertex, edge ending vertex
        while (scanner.hasNextInt()) {
            if (lineNum == 0) {
                // read count of vertexes and edges
                vertexCount = scanner.nextInt();
                if (scanner.hasNextInt()) {
                    edgeCount = scanner.nextInt();
                }
            } else if (lineNum < vertexCount + 1) {
                // go throught all vertexes
                vx = scanner.nextInt();
                if (scanner.hasNextInt()) {
                    vy = scanner.nextInt();
                    // just fill the support vector now, we will fill real set later
                    supportVertexVector.add(new GVertex(vId, vx, vy));
                    ++vId;
                }
            } else {
                // go through all edges
                edge1 = scanner.nextInt();
                if (scanner.hasNextInt()) {
                    edge2 = scanner.nextInt();
                    edges
                            .put(new MultiKey(new Integer(edge1), new Integer(edge2)), countDistanceBetweenVertexes(supportVertexVector
                                    .elementAt(edge1 - 1), supportVertexVector.elementAt(edge2 - 1)));
                    supportEdgesVector.add(new GEdge(edge1, edge2, 0)); // weight is not important for support vector
                }
            }
            ++lineNum;
        }
        // now we need to fill the vertex set with all data
        // iterate over all edges and add neightbors data to vector of vertexes
        for (Iterator<GEdge> iterator = supportEdgesVector.iterator(); iterator.hasNext();) {
            GEdge gEdge = iterator.next();
            int s = gEdge.getStartingVertexId();
            int e = gEdge.getEndingVertexId();
            supportVertexVector.elementAt(s - 1).getNeighbours().add(supportVertexVector.elementAt(e - 1));
            supportVertexVector.elementAt(e - 1).getNeighbours().add(supportVertexVector.elementAt(s - 1));
        }
        // now copy vector to vertex set
        vertexes.addAll(supportVertexVector);
        // make original copies that won't be changed
        appController.getVertexSetOriginal().addAll(vertexes);
        appController.getEdgesMapOriginal().putAll(edges);
    }

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

    private static Integer countDistanceBetweenVertexes(GVertex v1, GVertex v2) {
        int x1, x2, y1, y2;
        x1 = v1.getCoord().x;
        y1 = v1.getCoord().y;
        x2 = v2.getCoord().x;
        y2 = v2.getCoord().y;
        return new Integer((int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }
}
