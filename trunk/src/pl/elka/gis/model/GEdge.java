package pl.elka.gis.model;

@Deprecated
public class GEdge {

    private int mStartingVertexId;
    private int mEndingVertexId;
    private int mEdgeWeight;

    public GEdge(int startVertexId, int endVertexId, int weight) {
        mStartingVertexId = startVertexId;
        mEndingVertexId = endVertexId;
        mEdgeWeight = weight;
    }

    /**
     * checks edges are equal
     * 
     * @param e2
     * @return
     */
    public boolean equals(GEdge e2) {
        if ((mStartingVertexId == e2.getStartingVertexId() && mEndingVertexId == e2.getEndingVertexId())
                || (mStartingVertexId == e2.getEndingVertexId() && mEndingVertexId == e2.getStartingVertexId())) {
            return true;
        }
        return false;
    }

    public int getStartingVertexId() {
        return mStartingVertexId;
    }

    public int getEndingVertexId() {
        return mEndingVertexId;
    }

    public int getEdgeWeight() {
        return mEdgeWeight;
    }
}
