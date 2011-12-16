package pl.elka.gis.model;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ResultSet {

    private Set<GVertex> mCentralVertexSet;
    private int mLongestPath; // we can count that as well from mLongestPathVertexSet
    private Set<GVertex> mLongestPathVertexSet;// need vertexes that are on the longest path for painting
    private Set<GEdge> mLongestPathEdgesSet;// need vertexes that are on the longest path for painting

    public ResultSet() {
        mCentralVertexSet = new LinkedHashSet<GVertex>();
        mLongestPathVertexSet = new LinkedHashSet<GVertex>();
        mLongestPathEdgesSet = new LinkedHashSet<GEdge>();
    }

    public void clear() {
        mCentralVertexSet = new LinkedHashSet<GVertex>();
        mLongestPathVertexSet = new LinkedHashSet<GVertex>();
        mLongestPathEdgesSet = new LinkedHashSet<GEdge>();
        mLongestPath = Integer.MAX_VALUE;
    }

    public Set<GVertex> getCentralVertexSet() {
        return mCentralVertexSet;
    }

    public void setCentralVertexSet(Set<GVertex> mCentralVertexSet) {
        this.mCentralVertexSet = mCentralVertexSet;
    }

    public int getLongestPath() {
        return mLongestPath;
    }

    public void setLongestPath(int mLongestPath) {
        this.mLongestPath = mLongestPath;
    }

    public Set<GVertex> getLongestPathVertexSet() {
        return mLongestPathVertexSet;
    }

    public void setLongestPathVertexSet(Set<GVertex> mLongestPathVertexSet) {
        this.mLongestPathVertexSet = mLongestPathVertexSet;
    }

    public Set<GEdge> getLongestPathEdgesSet() {
        return mLongestPathEdgesSet;
    }

    public void setLongestPathEdgesSet(Set<GEdge> mLongestPathEdgesSet) {
        this.mLongestPathEdgesSet = mLongestPathEdgesSet;
    }

    public String getCentersSetAsString() {
        if (mCentralVertexSet.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<GVertex> iterator = mCentralVertexSet.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            sb.append(v.getVertexId() + ", ");
        }
        sb.delete(sb.length() - 2, sb.length()); // remove last ", " chars
        return sb.toString();
    }

    public String getLongestPathVertexSetAsString() {
        if (mLongestPathVertexSet.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<GVertex> iterator = mLongestPathVertexSet.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            sb.append(v.getVertexId() + " - ");
        }
        sb.delete(sb.length() - 3, sb.length()); // remove last " - " chars
        return sb.toString();
    }

    public String getLongestPathEdgesSetAsString() {
        if (mLongestPathEdgesSet.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<GEdge> iterator = mLongestPathEdgesSet.iterator(); iterator.hasNext();) {
            GEdge e = iterator.next();
            sb.append(e.getStartingVertexId() + " - " + e.getEndingVertexId() + ", ");
        }
        sb.delete(sb.length() - 2, sb.length()); // remove last ", " chars
        return sb.toString();
    }
}
