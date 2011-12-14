package pl.elka.gis.logic;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.buffer.PriorityBuffer;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;
import pl.elka.gis.model.ResultSet;
import pl.elka.gis.ui.components.ProgressCallback;
import pl.elka.gis.utils.Log;

public class CalculatingThread extends Thread {

    private static final String LOG_TAG = "CalculatingThread";
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
        Log.d(LOG_TAG, ">> run");
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
        // ////////////////////////////////////////////////////////////////////////////////////////////////////////// create fake
        // data
        Set<GVertex> vs = new LinkedHashSet<GVertex>();
        Set<GVertex> vs2 = new LinkedHashSet<GVertex>();
        Set<GEdge> vs3 = new LinkedHashSet<GEdge>();
        GVertex[] arr1 = mVertexSet.toArray(new GVertex[0]);
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < mCentersCount; ++i) {
            int vertIndex = r.nextInt(mVertexSet.size());
            if (!vs.add(arr1[vertIndex])) {
                --i; // if we have the same vert generated then try again until we really have enough
            }
        }
        int pathEdges = mCentersCount + 1;
        int[] eTable = new int[pathEdges];
        for (int i = 0; i < pathEdges; ++i) {
            eTable[i] = -1; // init with -1 so randomised numbers won't be present
        }
        for (int i = 0; i < pathEdges; ++i) {
            int val = -1;
            do {
                val = r.nextInt(mEdgesMap.size());
                Log.d(LOG_TAG, ">> val=" + val);
            } while (contains(val, eTable));
            eTable[i] = val;
        }
        Arrays.sort(eTable); // now we have sorted, not duplicated elements
        MapIterator it = mEdgesMap.mapIterator();
        int currETableIndex = 0;
        int iteratorLoops = 0;
        while (it.hasNext()) {
            MultiKey key = (MultiKey) it.next();
            if (iteratorLoops == eTable[currETableIndex]) {
                Log.d(LOG_TAG, "multikey add vertexes (iteratorLoops=" + iteratorLoops + "): " + ((Integer) key.getKey(0)) + ", "
                        + ((Integer) key.getKey(1)));
                vs2.add(arr1[(Integer) key.getKey(0) - 1]);
                vs2.add(arr1[(Integer) key.getKey(1) - 1]);
                vs3.add(new GEdge((Integer) key.getKey(0), (Integer) key.getKey(1), 0));
                ++currETableIndex;
                if (currETableIndex == eTable.length) {
                    break; // stop when added all edges
                }
            }
            ++iteratorLoops;
        }
        mResultSet.setCentralVertexSet(vs);
        mResultSet.setLongestPathVertexSet(vs2);
        mResultSet.setLongestPathEdgesSet(vs3);
        mResultSet.setLongestPath(r.nextInt(1500)); // random path between 0 and 100
        // ////////////////////////////////////////////////////////////////////////////////////////////////////////// create fake
        // data
        mCallback.calculationFinished();
        Log.d(LOG_TAG, "<< run");
    }

    private static boolean contains(int val, int[] tab) {
        for (int i = 0; i < tab.length; ++i) {
            if (val == tab[i]) {
                return true;
            }
        }
        return false;
    }
}
