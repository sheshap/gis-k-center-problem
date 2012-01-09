package pl.elka.gis.logic;

import java.util.Arrays;
import java.util.Iterator;
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
import pl.elka.gis.utils.AppConstants;
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
        // do preprocessing
        ResultFlag preprocessResult = preprocessGraph();
        Log.d(LOG_TAG, "preprocessGraph result=" + preprocessResult);
        boolean useFullVersion = true; // the only case we shouldn't use full version is CENTERS_EQUAL_SUBGRAPHS
        // check the result
        switch (preprocessResult) {
            case PREPROCESSING_DONE_CENTERS_MORE_OR_EQUAL_VERTEXES :
                Log.d(LOG_TAG, "CENTERS_MORE_OR_EQUAL_VERTEXES > don't have to do anything more - solution is in result set");
                mCallback.calculationFinished();
                return;
            case PREPROCESSING_DONE_CENTERS_EQUAL_SUBGRAPHS :
                Log.d(LOG_TAG, "CENTERS_EQUAL_SUBGRAPHS > in this case we can simplify the algorythm");
                useFullVersion = false;
                break;
            case PREPROCESSING_ERROR_GENERAL :
                mResultSet.clear();
                mCallback.calculationError("PREPROCESSING_ERROR_GENERAL");
                return;
            case PREPROCESSING_DONE_ERR_NO_CENTERS :
                mResultSet.clear();
                mCallback.calculationError("PREPROCESSING_DONE_ERR_NO_CENTERS");
                return;
            case PREPROCESSING_DONE_ERR_NO_VERTEXES :
                mResultSet.clear();
                mCallback.calculationError("PREPROCESSING_DONE_ERR_NO_VERTEXES");
                return;
            case PREPROCESSING_DONE_ERR_TOO_MANY_SUBGRAPHS :
                mResultSet.clear();
                mCallback.calculationError("PREPROCESSING_DONE_ERR_TOO_MANY_SUBGRAPHS");
                return;
            case PREPROCESSING_DONE_NO_EDGE_CASE_FOUND :
                Log.d(LOG_TAG, "NO_EDGE_CASE_FOUND > don't have to do anything just carry on with algorythm");
                break;
        }
        findCentersAlgorythm(useFullVersion);
        fakeCountCentersAlgorithm(); // FIXME delete this when ready
        mCallback.calculationFinished();
        Log.d(LOG_TAG, "<< run");
    }

    /**
     * this method checks edge cases and returns result
     * 
     * @return
     */
    private ResultFlag preprocessGraph() {
        if (mVertexSet.size() <= 0) {
            return ResultFlag.PREPROCESSING_DONE_ERR_NO_VERTEXES;
        }
        if (mCentersCount <= 0) {
            return ResultFlag.PREPROCESSING_DONE_ERR_NO_CENTERS;
        }
        if (!putCentersToIsolatedVertexes()) {
            return ResultFlag.PREPROCESSING_DONE_ERR_TOO_MANY_SUBGRAPHS;
        }
        if (mVertexSet.size() <= 0) {
            mResultSet.setLongestPath(0);
            return ResultFlag.PREPROCESSING_DONE_CENTERS_MORE_OR_EQUAL_VERTEXES; // and they are located int the result set
        }
        if (mCentersCount <= 0) {
            return ResultFlag.PREPROCESSING_DONE_ERR_TOO_MANY_SUBGRAPHS;
        }
        if (mVertexSet.size() <= mCentersCount) {
            mResultSet.setLongestPath(0);
            mResultSet.getCentralVertexSet().addAll(mVertexSet); // so now just put rest vertexes into result set
            return ResultFlag.PREPROCESSING_DONE_CENTERS_MORE_OR_EQUAL_VERTEXES; // and they are located int the result set
        }
        int subG = 0;
        if ((subG = countSubGraphs()) >= mCentersCount) {
            if (subG == mCentersCount) {
                return ResultFlag.PREPROCESSING_DONE_CENTERS_EQUAL_SUBGRAPHS; // in this case we just need to put 1 center in each
                // subgraph
            }
            return ResultFlag.PREPROCESSING_DONE_ERR_TOO_MANY_SUBGRAPHS;
        }
        return ResultFlag.PREPROCESSING_DONE_NO_EDGE_CASE_FOUND;
    }

    /**
     * this method finds the independent sub-graphs in the graph (finds on how many parts graph is divided) it user search across
     * method
     * 
     * @return number of independent subgraphs
     */
    private int countSubGraphs() {
        Log.d(LOG_TAG, ">> countSubGraphs");
        // make a table of vertexes from set
        GVertex[] vert = mVertexSet.toArray(new GVertex[0]);
        int subGraphCount = 0;
        for (int i = 0; i < vert.length; ++i) {
            if (vert[i] != null) { // increment and check only if vertex is not marked visited
                ++subGraphCount;
                goToVertex(vert[i], vert);
            }
        }
        Log.d(LOG_TAG, "<< countSubGraphs result=" + subGraphCount);
        return subGraphCount;
    }

    private void goToVertex(GVertex v, GVertex[] vs) {
        // each 'marked' vertex is nullified in 'vs' table
        markVisided(v, vs);
        for (Iterator<GVertex> iterator = v.getNeighbours().iterator(); iterator.hasNext();) {
            GVertex gVertex = (GVertex) iterator.next();
            if (!checkWasVisited(gVertex, vs)) {
                goToVertex(gVertex, vs);
            }
        }
    }

    private void markVisided(GVertex v, GVertex[] vs) {
        for (int i = 0; i < vs.length; ++i) {
            if (v != null && vs[i] != null && vs[i].getVertexId() == v.getVertexId()) {
                vs[i] = null; // 'remove' from table - means is checked
            }
        }
    }

    private boolean checkWasVisited(GVertex v, GVertex[] vs) {
        for (int i = 0; i < vs.length; ++i) {
            if (v != null && vs[i] != null && vs[i].getVertexId() == v.getVertexId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * this puts centers to isolated vertexes - it changes the working vertex and centers set (!!!)
     * 
     * @return false if there were not enough centers to put in isolated vertexes otherwise true
     */
    private boolean putCentersToIsolatedVertexes() {
        for (Iterator<GVertex> iterator = mVertexSet.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            if (v.getNeighbours().isEmpty()) {
                if (mCentersCount == 0) {
                    return false; // unable to deploy centers in isolated vertexes - not enough centers
                }
                v.setNearestCenter(v); // center as self
                v.setShortestPath(0); // path to self is 0
                mResultSet.getCentralVertexSet().add(v);
                --mCentersCount;
                Log.d(LOG_TAG, "Isolated verted delete: vertexID=" + v.getVertexId());
                iterator.remove(); // remove this vertex from the set
            }
        }
        mResultSet.setLongestPath(Integer.MAX_VALUE);
        return true;
    }

    /**
     * full version is standard not full version is when number of subgraphs is equal centers
     * 
     * @param fullVersion
     */
    private void findCentersAlgorythm(boolean fullVersion) {
        // we create heap here because original vertexes set might have changed in putCentersToIsolatedVertexes() method
        for (Iterator<GVertex> iterator = mVertexSet.iterator(); iterator.hasNext();) {
            GVertex vert = iterator.next();
            mVertexHeap.add(vert);
        }
        // TODO implement algorithm here
    }

    /**
     * this is only for testing purposes (progress simulation) - delete when not needed anymore
     */
    private void fakeCountCentersAlgorithm() {
        // ////////////////////////////////////////////////////////////////////testing
        int timeX = 1; // for 1 it is about 3 seconds
        for (int j = 0; j < 100; ++j) {
            mCallback.updateProgress(j);
            for (int i = 0; i < 9999999 * timeX; ++i) {
                for (int k = 0; k < 9999999; ++k) {
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
            } while (AppConstants.contains(val, eTable));
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
        // ////////////////////////////////////////////////////////////////////testing
    }
    // concurent modification safe
    // private boolean putCentersToIsolatedVertexes() {
    // boolean[] indexesToRemove = new boolean[mVertexSet.size()];
    // int index = 0;
    // for (Iterator<GVertex> iterator = mVertexSet.iterator(); iterator.hasNext();) {
    // GVertex v = iterator.next();
    // if (v.getNeighbours().isEmpty()) {
    // if (mCentersCount == 0) {
    // return false; // unable to deploy centers in isolated vertexes - not enough centers
    // }
    // v.setNearestCenter(v); // center as self
    // v.setShortestPath(0); // path to self is 0
    // mResultSet.getCentralVertexSet().add(v);
    // --mCentersCount;
    // indexesToRemove[index] = true; // mark this vertex to be removed
    // } else {
    // indexesToRemove[index] = false; // mark this vertex not to be removed
    // }
    // ++index;
    // }
    // index = 0;
    // for (Iterator<GVertex> iterator = mVertexSet.iterator(); iterator.hasNext();) {
    // GVertex v = iterator.next();
    // if (indexesToRemove[index] == true) {
    // Log.d(LOG_TAG, "delete vertexID=" + v.getVertexId());
    // iterator.remove();
    // }
    // ++index;
    // }
    // mResultSet.setLongestPath(Integer.MAX_VALUE);
    // return true;
    // }
}
