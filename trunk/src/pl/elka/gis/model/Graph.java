package pl.elka.gis.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import pl.elka.gis.utils.Log;

/**
 * @author Andrzej Makarewicz
 */
public class Graph {

    private static final String LOG_TAG = Graph.class.getSimpleName();
    //
    private final Set<Vertex> mVertexes;
    //
    private int mSubgraphsCount = -1;

    public Graph(Set<Vertex> vertexes) {
        if (vertexes == null)
            vertexes = Collections.emptySet();

        mVertexes = vertexes;
    }

    public Set<Vertex> getVertexes() {
        return Collections.unmodifiableSet(mVertexes);
    }

    public int getSubgraphsCount() {
        maybeCountSubgraphs();
        return mSubgraphsCount;
    }

    private void maybeCountSubgraphs() {
        if (mSubgraphsCount == -1)
            countSubgraphs();
    }

    private void countSubgraphs() {
        Log.d(LOG_TAG, Log.getCurrentMethodName());

        if (mVertexes.isEmpty()) {
            mSubgraphsCount = 0;
            return;
        }

        boolean[] marked = new boolean[mVertexes.size()];

        for (Vertex v : mVertexes) {
            if (!marked[v.getId()]) {
                mSubgraphsCount++;
                marked[v.getId()] = true;
                visitAllNeighbours(v, marked);
            }
        }
    }

    private void visitAllNeighbours(Vertex v, boolean[] marked) {
        Set<Vertex> neighbours = v.getNeighbours();
        for (Vertex n : neighbours) {
            if (!marked[n.getId()]) {
                marked[n.getId()] = true;
                visitAllNeighbours(n, marked);
            }
        }
    }

    public static Graph fromFile(File file) throws FileNotFoundException {
        Log.d(LOG_TAG, Log.getCurrentMethodName());

        if (file == null || !file.exists())
            throw new NullPointerException();

        Scanner scanner = new Scanner(file);

        int vertexCount = scanner.nextInt();
        int edgeCount = scanner.nextInt();

        if (vertexCount == 0)
            return null;

        Vertex[] vertexes = new Vertex[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            int x = scanner.nextInt(), y = scanner.nextInt();
            vertexes[i] = new Vertex(i, x, y);
        }

        for (int i = 0; i < edgeCount; i++) {
            int v1Id = scanner.nextInt(), v2Id = scanner.nextInt();
            Vertex v1 = vertexes[v1Id], v2 = vertexes[v2Id];
            Vertex.setAsNeighbours(v1, v2);
        }

        Set<Vertex> vertexesSet = new HashSet<Vertex>(vertexCount);
        for (Vertex v : vertexes) {
            vertexesSet.add(v);
        }

        return new Graph(vertexesSet);
    }
}
