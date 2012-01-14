package pl.elka.gis.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
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
    private final Set<Edge> mEdges;
    //
    private int mSubgraphsCount = -1;

    public Graph(Set<Vertex> vertexes, Set<Edge> edges) {
        if (vertexes == null)
            vertexes = Collections.emptySet();

        mVertexes = vertexes;

        if (edges == null)
            edges = Collections.emptySet();

        mEdges = edges;
    }

    public Set<Vertex> getVertexes() {
        return Collections.unmodifiableSet(mVertexes);
    }

    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(mEdges);
    }

    public int getVertexesCount() {
        return mVertexes.size();
    }

    public int getEdgesCount() {
        return mEdges.size();
    }

    /******************** SUBGRAPHS ********************/

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

        int n = mVertexes.size();

        if (mEdges.isEmpty()) {
            mSubgraphsCount = n;
            return;
        }

        if (mEdges.size() > (n - 1) * (n - 2) / 2) {
            mSubgraphsCount = 1;
            return;
        }

        boolean[] marked = new boolean[n];

        mSubgraphsCount = 0;
        for (Vertex v : mVertexes) {
            Log.d(LOG_TAG, "for vid:" + v.getId() + " marked:" + marked[v.getId() - 1]);

            if (!marked[v.getId() - 1]) {
                mSubgraphsCount++;
                marked[v.getId() - 1] = true;
                visitAllNeighbours(v, marked);
            }
        }
    }

    private void visitAllNeighbours(Vertex v, boolean[] marked) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + " vid:" + v.getId());

        Set<Vertex> neighbours = v.getNeighbours();
        for (Vertex n : neighbours) {
            if (!marked[n.getId() - 1]) {
                marked[n.getId() - 1] = true;
                visitAllNeighbours(n, marked);
            }
        }
    }

    /******************** FILE LOADING ********************/

    public static Graph fromFile(File file) throws FileNotFoundException, InputMismatchException, ArrayIndexOutOfBoundsException,
                                           NoSuchElementException {
        // Log.d(LOG_TAG, Log.getCurrentMethodName());

        if (file == null || !file.exists())
            throw new NullPointerException();

        Scanner scanner = new Scanner(file);

        int vertexCount = scanner.nextInt();
        int edgeCount = scanner.nextInt();

        if (vertexCount == 0) {
            throw new InputMismatchException("No vertexes in graph.");
        }

        Vertex[] vertexes = new Vertex[vertexCount];

        for (int i = 1; i <= vertexCount; i++) {
            int x = scanner.nextInt(), y = scanner.nextInt();
            vertexes[i - 1] = new Vertex(i, x, y);
        }

        Set<Edge> edges = new HashSet<Edge>();

        for (int i = 0; i < edgeCount; i++) {
            int v1Id = scanner.nextInt(), v2Id = scanner.nextInt();
            Vertex v1 = vertexes[v1Id - 1], v2 = vertexes[v2Id - 1];
            if (Vertex.setAsNeighbours(v1, v2))
                edges.add(new Edge(v1, v2));
        }

        Set<Vertex> vertexesSet = new HashSet<Vertex>(vertexCount);
        for (Vertex v : vertexes) {
            vertexesSet.add(v);
        }

        return new Graph(vertexesSet, edges);
    }

    public void toFile(File file) throws IOException {
        if (file == null)
            throw new NullPointerException();

        FileWriter fileWriter = new FileWriter(file);
        PrintWriter out = new PrintWriter(fileWriter);

        out.println(String.format("%d %d", mVertexes.size(), mEdges.size()));

        Vertex[] vertexes = new Vertex[mVertexes.size()];
        for (Vertex v : mVertexes) {
            vertexes[v.getId() - 1] = v;
        }
        for (Vertex v : vertexes) {
            out.println(String.format("%d %d", v.getX(), v.getY()));
        }

        for (Edge e : mEdges) {
            out.println(String.format("%d %d", e.getVertexIds().getLeft(), e.getVertexIds().getRight()));
        }

        out.close();
    }
}
