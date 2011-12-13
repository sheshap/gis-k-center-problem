package pl.elka.gis.logic;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.buffer.PriorityBuffer;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

import pl.elka.gis.model.GVertex;
import pl.elka.gis.model.ResultSet;

public class Controller {

    private Set<GVertex> mVertexSet;
    private PriorityBuffer mVertexHeap; // PriorityBuffer instead of deprecated BinaryHeap
    private MultiKeyMap mEdgesMap; // starting and ending vertex as keys
    private ResultSet mResultSet;

    public Controller() {
        mVertexSet = new LinkedHashSet<GVertex>();
        mEdgesMap = new MultiKeyMap();
    }

    public ResultSet countGraphData() {
        // TODO start algorithm here
        return mResultSet;
    }

    public Set<GVertex> getVertexSet() {
        return mVertexSet;
    }

    public MultiKeyMap getEdgesMap() {
        return mEdgesMap;
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
            sb.append("v1=" + key.getKey(0) + ", v2=" + key.getKey(1) + ", weight= " + it.getValue() + "\n");
        }
        return sb.toString();
    }
}
