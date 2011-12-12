package pl.elka.gis.logic;

import java.util.Set;

import org.apache.commons.collections.buffer.PriorityBuffer;
import org.apache.commons.collections.map.MultiKeyMap;

import pl.elka.gis.model.GVertex;

public class Controller {

    private Set<GVertex> mVertexSet;
    private PriorityBuffer mVertexHeap; // PriorityBuffer instead of deprecated BinaryHeap
    private MultiKeyMap mEdgesWeightsMap; // starting and ending vertex as keys
}
