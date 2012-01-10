package pl.elka.gis.model;

import java.awt.Point;
import java.util.LinkedHashSet;
import java.util.Set;

@Deprecated
public class GVertex {

    private Point mCoord;
    private int mVertexId; // this is vertex ID ranges from 0 to vertex count -1
    private Set<GVertex> mNeighboursIds; // set of neighbors of this vertex (contains only IDs)
    private int mShortestPath;
    private GVertex mNearestCenter;

    public GVertex(int id, int x, int y) {
        mVertexId = id;
        mCoord = new Point(x, y);
        // (!) important HashSet unlike the LinkedHashSet can mix order of elements!
        // that's why wy always use LinkedHashSet
        mNeighboursIds = new LinkedHashSet<GVertex>();
    }

    public GVertex(int id, Point coord) {
        mVertexId = id;
        mCoord = new Point(coord);
        mNeighboursIds = new LinkedHashSet<GVertex>();
    }

    public Point getCoord() {
        return mCoord;
    }

    public int getVertexId() {
        return mVertexId;
    }

    public Set<GVertex> getNeighbours() {
        return mNeighboursIds;
    }

    public int getShortestPath() {
        return mShortestPath;
    }

    public void setShortestPath(int mShortestPath) {
        this.mShortestPath = mShortestPath;
    }

    public GVertex getNearestCenter() {
        return mNearestCenter;
    }

    public void setNearestCenter(GVertex mNearestCenter) {
        this.mNearestCenter = mNearestCenter;
    }

    public void setCoord(Point mCoord) {
        this.mCoord = mCoord;
    }
}
