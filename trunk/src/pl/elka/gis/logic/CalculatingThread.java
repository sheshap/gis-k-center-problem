package pl.elka.gis.logic;

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

    public CalculatingThread(ProgressCallback callback, Set<GVertex> pVertexSet, PriorityBuffer pVertexHeap, MultiKeyMap pEdgesMap, ResultSet pResultSet) {
        mVertexSet = pVertexSet;
        mVertexHeap = pVertexHeap;
        mEdgesMap = pEdgesMap;
        mResultSet = pResultSet;
        mCallback = callback;
    }

    public void run() {
        // TODO count everything here
        for (int j = 0; j < 100; ++j) {
            mCallback.updateProgress(j);
            for (int i = 0; i < 999999991; ++i) {
                for (int k = 0; k < 999999991; ++k) {
                    // simulate long working thread
                }
            }
        }
        // TODO create fake data
        mResultSet = new ResultSet();
        mCallback.calculationFinished();
    }
}
