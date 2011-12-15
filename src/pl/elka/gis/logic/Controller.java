package pl.elka.gis.logic;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.buffer.PriorityBuffer;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

import pl.elka.gis.model.GVertex;
import pl.elka.gis.model.ResultSet;
import pl.elka.gis.ui.components.ProgressCallback;
import pl.elka.gis.utils.Log;

public class Controller {

    private Set<GVertex> mVertexSet;
    private PriorityBuffer mVertexHeap; // PriorityBuffer instead of deprecated BinaryHeap
    private MultiKeyMap mEdgesMap; // starting and ending vertex as keys
    private ResultSet mResultSet;
    private int mCentersCount;
    private static final String LOG_TAG = "Controller";

    public Controller() {
        initController();
    }

    public void initController() {
        mVertexSet = new LinkedHashSet<GVertex>();
        mEdgesMap = new MultiKeyMap();
        mResultSet = new ResultSet();
    }

    private void prepareVertexHeap() {
        Comparator<GVertex> vertexComparator = new Comparator<GVertex>() {

            @Override
            public int compare(GVertex o1, GVertex o2) {
                // TODO is this comparator ok? taking x into account then y if needed
                if (o1.getCoord().x < o2.getCoord().x) {
                    return -1;
                } else if (o1.getCoord().x > o2.getCoord().x) {
                    return 1;
                } else {
                    if (o1.getCoord().y < o2.getCoord().y) {
                        return -1;
                    } else if (o1.getCoord().y > o2.getCoord().y) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        };
        mVertexHeap = new PriorityBuffer(vertexComparator);
    }

    public ResultSet countGraphData(int centersCount, ProgressCallback callback) {
        Log.d(LOG_TAG, ">> countGraphData(" + centersCount + ")");
        mCentersCount = centersCount;
        prepareVertexHeap();
        new CalculatingThread(callback, mVertexSet, mVertexHeap, mEdgesMap, mResultSet, mCentersCount).start();
        Log.d(LOG_TAG, "<< countGraphData done - background thread started");
        return mResultSet;
    }

    public Set<GVertex> getVertexSet() {
        return mVertexSet;
    }

    public MultiKeyMap getEdgesMap() {
        return mEdgesMap;
    }

    public ResultSet getResultSet() {
        return mResultSet;
    }

    public int getCentersCount() {
        return mCentersCount;
    }

    public String getControllerData() {
        StringBuilder sb = new StringBuilder();
        sb.append("data set:\n");
        sb.append(mVertexSet.size() + " " + mEdgesMap.size() + "\n");
        sb.append("vertexes:\n");
        for (Iterator<GVertex> iterator = mVertexSet.iterator(); iterator.hasNext();) {
            GVertex vert = iterator.next();
            sb.append("id=" + vert.getVertexId() + ", x=" + vert.getCoord().x + ", y=" + vert.getCoord().y + "\n");
            sb.append("neighbors=");
            for (Iterator<Integer> iterator2 = vert.getNeighboursIds().iterator(); iterator2.hasNext();) {
                int neighborId = iterator2.next().intValue();
                sb.append(" - " + neighborId);
            }
            sb.append("\n");
        }
        sb.append("edges:\n");
        MapIterator it = mEdgesMap.mapIterator();
        while (it.hasNext()) {
            MultiKey key = (MultiKey) it.next();
            sb.append("" + key.getKey(0) + " <-> " + key.getKey(1) + ", weight= " + it.getValue() + "\n");
        }
        return sb.toString();
    }

    public void setCentersCount(int mCentersCount) {
        this.mCentersCount = mCentersCount;
    }
}
