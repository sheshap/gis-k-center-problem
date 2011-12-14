package pl.elka.gis.model;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class GVertex {

    private Point mCoord;
    private int mVertexId; // this is vertex ID ranges from 0 to vertex count -1
    private Set<Integer> mNeighboursIds; // set of neighbors of this vertex (contains only IDs)
    private int mShortestPath;
    private GVertex mNearestCenter;

    public GVertex(int id, int x, int y) {
        mVertexId = id;
        mCoord = new Point(x, y);
        mNeighboursIds = new HashSet<Integer>();
    }

    public GVertex(int id, Point coord) {
        mVertexId = id;
        mCoord = new Point(coord);
        mNeighboursIds = new HashSet<Integer>();
    }

    public Point getCoord() {
        return mCoord;
    }

    public int getVertexId() {
        return mVertexId;
    }

    public Set<Integer> getNeighboursIds() {
        return mNeighboursIds;
    }
}