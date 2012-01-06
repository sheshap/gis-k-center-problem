package pl.elka.gis.model.generator;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;
import pl.elka.gis.utils.AppConstants;
import pl.elka.gis.utils.Log;

public class DataGenerator {

    private int mVertexesCount = 12;
    private int mMaxVertexDegree = 5;
    private int mEdgesProbability = 50; // percentage of edge occurence probability
    private int mMinVertexCoordDifference = 10; // mininal difference in at least one dimension
    private Vector<GVertex> mVertexes;
    private Set<GEdge> mEdges;
    private Random mRandomGenerator;
    private static final String LOG_TAG = "DataGenerator";

    public void initGenerator() {
        mRandomGenerator = new Random(System.currentTimeMillis());
        mVertexes = new Vector<GVertex>();
        mEdges = new LinkedHashSet<GEdge>();
    }

    public void generateData() throws DataValidationException {
        Log.d(LOG_TAG, ">> generate data.. ");
        initGenerator();
        Log.d(LOG_TAG, "mVertexesCount, mEdgesProbability, mMaxVertexDegree, mMinVertexCoordDifference = " + mVertexesCount
                + ", " + mEdgesProbability + ", " + mMaxVertexDegree + ", " + mMinVertexCoordDifference);
        if (!DataValidator.isValidGraphData(mVertexesCount, mEdgesProbability, mMaxVertexDegree, mMinVertexCoordDifference)) {
            throw new DataValidationException("Graph data is invalid");
        }
        int x, y;
        GVertex vertex;
        for (int i = 0; i < mVertexesCount; ++i) {
            do {
                x = mRandomGenerator.nextInt(AppConstants.MAX_X_Y_VALUE + 1);
                y = mRandomGenerator.nextInt(AppConstants.MAX_X_Y_VALUE + 1);
            } while (!isDistansBetweenPointsSufficient(x, y));
            vertex = new GVertex(i + 1, x, y);
            mVertexes.add(vertex);
        }
        generateEdgesOld();
        Log.d(LOG_TAG, "<< generate data done");
    }

    public void saveData(String filename) throws DataValidationException, IOException {
        if (!DataValidator.isValidFilename(filename)) {
            throw new DataValidationException("File name is invalid");
        }
        FileHandler.writeFileContent(filename, mVertexes, mEdges);
        Log.d(LOG_TAG, "generated data savet to: " + filename + "." + AppConstants.DEFAULT_EXTENSION);
    }

    private boolean isDistansBetweenPointsSufficient(int newX, int newY) {
        GVertex elem;
        for (Iterator<GVertex> iterator = mVertexes.iterator(); iterator.hasNext();) {
            elem = iterator.next();
            if (newX < mMinVertexCoordDifference || newY < mMinVertexCoordDifference
                    || newX > AppConstants.MAX_X_Y_VALUE - mMinVertexCoordDifference
                    || newY > AppConstants.MAX_X_Y_VALUE - mMinVertexCoordDifference) {
                return false; // assure distance from edges
            }
            if (Math.abs(elem.getCoord().x - newX) < mMinVertexCoordDifference
                    && Math.abs(elem.getCoord().y - newY) < mMinVertexCoordDifference) {
                return false; // assure distance between vertexes
            }
        }
        return true;
    }

    /**
     * this method uses propability of edge occurence
     */
    private void generateEdgesOld() {
        Log.d(LOG_TAG, ">> generate edges old");
        Vector<Integer> degrees = new Vector<Integer>(mVertexes.size());
        for (int i = 0; i < mVertexes.size(); ++i) {
            degrees.add(new Integer(0));
        }
        for (int i = 0; i < mVertexes.size() - 1; ++i) {
            for (int j = i + 1; j < mVertexes.size(); ++j) {
                if (degrees.elementAt(i).intValue() >= mMaxVertexDegree) {
                    break;
                }
                boolean generateConnection = mRandomGenerator.nextInt(101) < mEdgesProbability ? true : false;
                if (generateConnection && degrees.elementAt(j).intValue() < mMaxVertexDegree) {
                    if (mEdges.add(new GEdge(i + 1, j + 1, 0))) {
                        // as it is HashSet it will not add duplicates i.e. 1->2 and 2->1
                        // so we only increment degree if edge was really added
                        // but it always be, because we check connections onty with next vertexes not previous
                        degrees.set(i, degrees.elementAt(i) + 1);
                        degrees.set(j, degrees.elementAt(j) + 1);
                    }
                }
            }
        }
    }

    public int getVertexCount() {
        return mVertexesCount;
    }

    public int getMaxVertexDegree() {
        return mMaxVertexDegree;
    }

    public Vector<GVertex> getVertexes() {
        return mVertexes;
    }

    public Set<GEdge> getEdges() {
        return mEdges;
    }

    public int getMinVertexCoordDifference() {
        return mMinVertexCoordDifference;
    }

    public void setVertexesCount(int mVertexesCount) {
        this.mVertexesCount = mVertexesCount;
    }

    public void setMaxVertexDegree(int mMaxVertexDegree) {
        this.mMaxVertexDegree = mMaxVertexDegree;
    }

    public void setMinVertexCoordDifference(int mMinVertexCoordDifference) {
        this.mMinVertexCoordDifference = mMinVertexCoordDifference;
    }

    public int getEdgesProbability() {
        return mEdgesProbability;
    }

    public void setEdgesProbability(int mEdgesProbability) {
        this.mEdgesProbability = mEdgesProbability;
    }
}
