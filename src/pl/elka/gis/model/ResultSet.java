package pl.elka.gis.model;

import java.util.Set;

public class ResultSet {

    private Set<GVertex> mCentralVertexSet;
    private int mLongestPath;
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
}
