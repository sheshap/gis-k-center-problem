package pl.elka.gis.model;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Andrzej Makarewicz
 */
public class Edge {

    private final int mVertex1Id, mVertex2Id;
    //
    private Vertex mVertex1, mVertex2;
    private int mWeight;

    public Edge(int vertex1Id, int vertex2Id) {
        if (vertex1Id < 0 || vertex2Id < 0 || vertex1Id == vertex2Id)
            throw new IllegalArgumentException();

        mVertex1Id = vertex1Id;
        mVertex2Id = vertex2Id;
    }

    public boolean setVertexes(final Set<Vertex> vertexes) {
        for (Vertex v : vertexes) {
            if (v.getId() == mVertex1Id) {
                mVertex1 = v;
                if (mVertex2 != null)
                    break;
            } else if (v.getId() == mVertex2Id) {
                mVertex2 = v;
                if (mVertex1 != null)
                    break;
            }
        }

        if (mVertex1 != null && mVertex2 != null) {
            mWeight = Vertex.distance(mVertex1, mVertex2);
            return true;
        }

        return false;
    }

    public Pair<Integer, Integer> getVertexIds() {
        return Pair.of(mVertex1Id, mVertex2Id);
    }

    public Pair<Vertex, Vertex> getVertexes() {
        return Pair.of(mVertex1, mVertex2);
    }

    public int getWeight() {
        return mWeight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (mVertex1Id != other.mVertex1Id && mVertex1Id != other.mVertex2Id)
            return false;
        if (mVertex2Id != other.mVertex2Id && mVertex2Id != other.mVertex1Id)
            return false;
        return true;
    }

}
