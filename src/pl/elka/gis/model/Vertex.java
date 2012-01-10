package pl.elka.gis.model;

import java.awt.Point;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import pl.elka.gis.utils.AppConstants;

/**
 * @author Andrzej Makarewicz
 */
public class Vertex {

    private final int mId;
    private final Point mPosition;
    //
    private Set<Vertex> mNeighbours;
    //
    private int mShortestPath = Integer.MAX_VALUE;
    private Vertex mNearestCenter;
    private boolean mIsCenter;

    public Vertex(int id, int x, int y) {
        if (id < 0 || x < 0 || x > AppConstants.MAX_X_Y_VALUE || y < 0 || y > AppConstants.MAX_X_Y_VALUE)
            throw new IllegalArgumentException();

        mId = id;
        mPosition = new Point(x, y);
    }

    public int getId() {
        return mId;
    }

    public Point getPosition() {
        return mPosition;
    }

    public int getX() {
        return mPosition.x;
    }

    public int getY() {
        return mPosition.y;
    }

    public boolean hasNeighbours() {
        return mNeighbours != null && !mNeighbours.isEmpty();
    }

    public boolean hasNeighbour(Vertex v) {
        if (!hasNeighbours())
            return false;

        return getOrCreateNeighbours().contains(v);
    }

    public Set<Vertex> getNeighbours() {
        if (hasNeighbours())
            return Collections.unmodifiableSet(mNeighbours);
        else
            return Collections.emptySet();
    }

    private Set<Vertex> getOrCreateNeighbours() {
        if (mNeighbours == null) {
            mNeighbours = new HashSet<Vertex>();
        }
        return mNeighbours;
    }

    public boolean addNeightbour(Vertex vertex) {
        return getOrCreateNeighbours().add(vertex);
    }

    public int getShortestPathLenght() {
        return mShortestPath;
    }

    public void setShortestPathLength(int shortestPath) {
        if (shortestPath < 0 || shortestPath > mShortestPath)
            throw new IllegalArgumentException();

        this.mShortestPath = shortestPath;
    }

    public Vertex getNearestCenter() {
        return mNearestCenter;
    }

    public void setNearestCenter(Vertex nearestCenter) {
        if (nearestCenter == null)
            throw new IllegalArgumentException();

        if (ObjectUtils.equals(this, nearestCenter))
            mIsCenter = true;

        this.mNearestCenter = nearestCenter;
    }

    public boolean isCenter() {
        return mIsCenter;
    }

    public int distance(Vertex v) {
        return distance(this, v);
    }

    public static int distance(Vertex v1, Vertex v2) {
        if (v1 == null || v2 == null)
            return -1;

        if (ObjectUtils.equals(v1, v2))
            return 0;

        int x = Math.abs(v1.getX() - v2.getX());
        int y = Math.abs(v1.getY() - v2.getY());
        return (int) Math.sqrt(x * x + y * y);
    }

    public static boolean setAsNeighbours(Vertex v1, Vertex v2) {
        if (v1 == null || v2 == null || ObjectUtils.equals(v1, v2))
            return false;

        v1.addNeightbour(v2);
        v2.addNeightbour(v1);
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        if (mId != other.mId)
            return false;
        return true;
    }

}
