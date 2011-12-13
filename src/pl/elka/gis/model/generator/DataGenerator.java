package pl.elka.gis.model.generator;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;

public class DataGenerator {

    private int mMinVertexes = 5;
    private int mMaxVertexes = 50;
    private int mMaxVertexDegree = 5;
    private int mEdgesProbability = 50; // percentage of edge occurence probability
    private final int MAX_X_Y_VALUE = 2000; // max x or y value of vertex coord
    private int mMinVertexCoordDifference = 10; // mininal difference in at least one dimension
    private Vector<GVertex> mVertexes;
    private Set<GEdge> mEdges;
    private Random mRandomGenerator;

    public DataGenerator() {
        initGenerator();
    }

    public void initGenerator() {
        mRandomGenerator = new Random(System.currentTimeMillis());
        mVertexes = new Vector<GVertex>();
        mEdges = new LinkedHashSet<GEdge>();
    }

    public void generateData() throws DataValidationException {
        if (!DataValidator.isValidGraphData(mVertexes, mEdges)) {
            throw new DataValidationException("Graph data is invalid");
        }
        int vertexesNum = mRandomGenerator.nextInt(mMaxVertexes + 1 - mMinVertexes) + mMinVertexes; // +1 because it generates
                                                                                                    // number exclusive the given
                                                                                                    // value
        int x, y;
        GVertex vertex;
        for (int i = 0; i < vertexesNum; ++i) {
            do {
                x = mRandomGenerator.nextInt(MAX_X_Y_VALUE + 1);
                y = mRandomGenerator.nextInt(MAX_X_Y_VALUE + 1);
            } while (!isDistansBetweenPointsSufficient(x, y));
            vertex = new GVertex(i, x, y);
            // generateNeighborsForVertex(vertex);
            mVertexes.add(vertex);
        }
        generateEdges();
    }

    public void saveData(String filename) throws DataValidationException {
        if (!DataValidator.isValidFilename(filename)) {
            throw new DataValidationException("File name is invalid");
        }
        FileHandler.writeFileContent(filename, mVertexes, mEdges);
    }

    private boolean isDistansBetweenPointsSufficient(int newX, int newY) {
        GVertex elem;
        for (Iterator<GVertex> iterator = mVertexes.iterator(); iterator.hasNext();) {
            elem = iterator.next();
            if (Math.abs(elem.getCoord().x - newX) < mMinVertexCoordDifference
                    && Math.abs(elem.getCoord().y - newY) < mMinVertexCoordDifference) {
                return false;
            }
        }
        return true;
    }

    private void generateEdges() {
        int degree;
        for (int i = 0; i < mVertexes.size() - 1; ++i) {
            degree = 0;
            for (int j = i + 1; j < mVertexes.size(); ++j) {
                if (degree >= mMaxVertexDegree) {
                    break;
                }
                boolean generateConnection = mRandomGenerator.nextInt(101) <= mEdgesProbability ? true : false;
                if (generateConnection) {
                    if (mEdges.add(new GEdge(i, j, 0))) {
                        // as it is HashSet it will not add duplicates i.e. 1->2 and 2->1
                        // so we only increment degree if edge was really added
                        // but it always be, because we check connections onty with next vertexes not previous
                        ++degree;
                    }
                }
            }
        }
    }

    // private void generateNeighborsForVertex(GVertex vertex) {
    // int maxVertexId = mVertexes.size() - 1;
    // for (int i = 0; i < mMaxVertexDegree && i <= maxVertexId; ++i) {
    // if (i != vertex.getVertexId()) {
    // boolean generateConnection = mRandomGenerator.nextInt(101) <= mEdgesProbability ? true : false;
    // if (generateConnection) {
    // vertex.getNeighboursIds().add(new Integer(i)); // add connection current -> i
    // mVertexes.elementAt(i).getNeighboursIds().add(vertex.getVertexId()); // update vertex 'i' connection to
    // // current
    // // as it is HashSet it will not add duplicates i.e. 1->2 and 2->1
    // mEdges.add(new GEdge(vertex.getVertexId(), i, countDistanceBetweenVertexes(vertex, mVertexes.elementAt(i))));
    // }
    // }
    // }
    // }
    private int countDistanceBetweenVertexes(GVertex v1, GVertex v2) {
        int x1, x2, y1, y2;
        x1 = v1.getCoord().x;
        y1 = v1.getCoord().y;
        x2 = v2.getCoord().x;
        y2 = v2.getCoord().y;
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    // ///////////////////// getters and setters /////////////////////////////
    public int getMinVertex() {
        return mMinVertexes;
    }

    public void setMinVertex(int mMinVertexes) {
        this.mMinVertexes = mMinVertexes;
    }

    public int getMaxVertex() {
        return mMaxVertexes;
    }

    public void setMaxVertex(int mMaxVertexes) {
        this.mMaxVertexes = mMaxVertexes;
    }

    public int getMaxVertexDegree() {
        return mMaxVertexDegree;
    }

    public void setMaxVertexDegree(int mMaxVertexDegree) {
        this.mMaxVertexDegree = mMaxVertexDegree;
    }

    public Vector<GVertex> getVertexes() {
        return mVertexes;
    }

    public Set<GEdge> getEdges() {
        return mEdges;
    }
}
