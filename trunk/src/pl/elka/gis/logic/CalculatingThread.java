package pl.elka.gis.logic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.buffer.PriorityBuffer;
import org.apache.commons.collections.map.MultiKeyMap;

import pl.elka.gis.model.GVertex;
import pl.elka.gis.model.ResultSet;
import pl.elka.gis.ui.components.ProgressCallback;

public class CalculatingThread extends Thread {

    private Set<GVertex> mVertexSet;
    private PriorityBuffer mVertexHeap; // PriorityBuffer instead of deprecated BinaryHeap
    private MultiKeyMap mEdgesMap; // starting and ending vertex as keys
    private ResultSet mResultSet;
    private ProgressCallback mCallback;
    private int mCentersCount;

    public CalculatingThread(ProgressCallback callback, Set<GVertex> pVertexSet, PriorityBuffer pVertexHeap, MultiKeyMap pEdgesMap, ResultSet pResultSet, int pCentersCount) {
        mVertexSet = pVertexSet;
        mVertexHeap = pVertexHeap;
        mEdgesMap = pEdgesMap;
        mResultSet = pResultSet;
        mCallback = callback;
        mCentersCount = pCentersCount;
    }

    public void run() {
        int timeX = 2; // for 1 it is about 3 seconds
        // TODO count everything here
        for (int j = 0; j < 100; ++j) {
            mCallback.updateProgress(j);
            for (int i = 0; i < 99999999 * timeX; ++i) {
                for (int k = 0; k < 999999991; ++k) {
                    // simulate long working thread
                }
            }
        }
        // TODO create fake data
        Set<GVertex> vs = new HashSet<GVertex>();
        Set<GVertex> vs2 = new HashSet<GVertex>();
        GVertex[] arr1 = mVertexSet.toArray(new GVertex[0]);
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < mCentersCount; ++i) {
            int vertIndex = r.nextInt(mVertexSet.size());
            if (!vs.add(arr1[vertIndex])) {
                --i; // if we have the same vert generated then try again until we really have enough
            }
        }
        for (int i = 0; i < mCentersCount + 1; ++i) {
            int vertIndex = r.nextInt(mVertexSet.size());
            vertIndex = r.nextInt(mVertexSet.size());
            vs2.add(arr1[vertIndex]); // we don't need to assure there will be enough vertexes here
        }
        mResultSet.setCentralVertexSet(vs);
        mResultSet.setLongestPathVertexSet(vs2);
        mResultSet.setLongestPath(r.nextInt(1500)); // random path between 0 and 100
        mCallback.calculationFinished();
    }
}
