package pl.elka.gis.model;

import java.util.Iterator;
import java.util.Set;

public class ResultSet {

    private Set<GVertex> mCentralVertexSet;
    private int mLongestPath; // we can count that as well from mLongestPathVertexSet
    private Set<GVertex> mLongestPathVertexSet;// need vertexes that are on the longest path for painting

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

    public String getCentersSetAsString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<GVertex> iterator = mCentralVertexSet.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            sb.append(v.getVertexId() + ", ");
        }
        sb.delete(sb.length() - 2, sb.length()); // remove last ", " chars
        return sb.toString();
    }

    public String getLongestPathVertexSetAsString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<GVertex> iterator = mLongestPathVertexSet.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            sb.append(v.getVertexId() + " - ");
        }
        sb.delete(sb.length() - 3, sb.length()); // remove last " - " chars
        return sb.toString();
    }
}
