package pl.elka.gis.model;

import java.awt.geom.Point2D;
import java.util.Set;

public class GVertex {

    private Point2D mCoord;
    private int mId;
    private Set<GVertex> mNeighbours;
    private int mShortestPath;
    private GVertex mNearestCenter;
}
