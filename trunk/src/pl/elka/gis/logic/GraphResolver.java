package pl.elka.gis.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import pl.elka.gis.model.Graph;
import pl.elka.gis.model.Vertex;
import pl.elka.gis.ui.components.ProgressCallback;
import pl.elka.gis.utils.Log;

/**
 * @author Andrzej Makarewicz
 */
public class GraphResolver {

    public enum ResultCase {
        DEFAULT, CENTRALS_EQUALS_SUBGRAPHS, NO_VERTEXES, NO_CENTRALS, INSUFFICIENT_VERTEXES, TOO_MANY_SUBGRAPHS, GENERAL_ERROR;
    }

    public static class Result {

        Set<Vertex> centers = null;
        int centersCount = -1, longest = Integer.MAX_VALUE;
        long sum = 0;
        long startTime = 0, endTime = 0;

        public Result() {
        }

        public Result(Result result) {
            this.centers = new HashSet<Vertex>(result.centers);
            this.centersCount = result.centersCount;
            this.longest = result.longest;
            this.sum = result.sum;
        }

        public void setForCenters(int centersCount) {
            this.centersCount = centersCount;
            centers = new HashSet<Vertex>(centersCount);
        }

        public Set<Vertex> getCenters() {
            return centers;
            // if (centers == null)
            // return Collections.emptySet();
            // else
            // return centers;
        }

        public boolean hasCenters() {
            return centers != null && !centers.isEmpty();
        }

        public int getCentersCount() {
            return centersCount;
        }

        public int getLongest() {
            return longest;
        }

        public long getSum() {
            return sum;
        }

        public int getTotalTimeInSeconds() {
            return (int) ((endTime - startTime) / 1000);
        }

        public void setTimes(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private static final String LOG_TAG = GraphResolver.class.getSimpleName();
    //
    private final Graph mGraph;
    //
    private ResultCase mResultCase;
    private Result mResult;

    public GraphResolver(Graph graph) {
        if (graph == null)
            throw new NullPointerException();

        mGraph = graph;
        mResult = new Result();
    }

    public GraphResolver.Result getResult() {
        return mResult;
    }

    public void resolve(int centersCount, ProgressCallback callback) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + " start");

        mResultCase = checkResultCase(centersCount);
        Log.d(LOG_TAG, Log.getCurrentMethodName() + mResultCase.name());

        switch (mResultCase) {
            case NO_VERTEXES :
            case NO_CENTRALS :
            case INSUFFICIENT_VERTEXES :
            case TOO_MANY_SUBGRAPHS :
            case GENERAL_ERROR :
                callback.calculationError(mResultCase.name());
                return;

            case CENTRALS_EQUALS_SUBGRAPHS :
                mResult.centersCount = centersCount;
                mResult.longest = 0;
                mResult.centers = mGraph.getVertexes();
                callback.calculationFinished(mResult);
                return;

            case DEFAULT :
            default :
                long startTime = System.currentTimeMillis();

                int centersLeft = initializeCenters(centersCount);

                Log.d(LOG_TAG, Log.getCurrentMethodName() + "centrals: " + centersCount);

                Set<Vertex> vertexes = new HashSet<Vertex>(mGraph.getVertexes());
                int[] centerId = new int[vertexes.size()];
                int[] d = new int[vertexes.size()];
                Arrays.fill(d, Integer.MAX_VALUE);

                for (Vertex v : vertexes) {
                    if (v.isCenter()) {
                        centerId[v.getId() - 1] = v.getId();
                        d[v.getId() - 1] = 0;
                        vertexes.remove(v);
                    }
                }

                int level = 0;
                if (centersLeft > 2)
                    level = 2;

                float progressDiff = 100 / vertexes.size();
                if (level == 2) {
                    progressDiff /= (vertexes.size() * vertexes.size());
                }

                callback.updateProgress(0);

                try {
                    mResult = findCentral(new Result(mResult), centersLeft, vertexes, d, centerId, callback, progressDiff, level);
                } catch (Exception e) {
                    if (Log.isLoggable()) {
                        e.printStackTrace();
                    }
                    return;
                }

                long endTime = System.currentTimeMillis();

                mResult.setTimes(startTime, endTime);

                callback.calculationFinished(mResult);
        }
    }

    private Result findCentral(Result result, int centersLeft, Set<Vertex> vertexes, int[] d, int[] centerId, ProgressCallback callback, float progressDiff, int level)
                                                                                                                                                                       throws InterruptedException {

        Log.d(LOG_TAG, Log.getCurrentMethodName() + " centers: " + centersLeft + " " + Arrays.toString(d));

        if (Thread.interrupted())
            throw new InterruptedException();

        if (centersLeft == 0) {
            Pair<Integer, Long> res = findMax(d);
            result.longest = res.getLeft();
            result.sum = res.getRight();
            return result;
        }

        Result ret = result;
        for (Vertex v : vertexes) {
            if (Thread.interrupted())
                throw new InterruptedException();

            if (level == 0)
                callback.increaseProgress(progressDiff);

            Result aResult = new Result(result);
            Set<Vertex> aVertexes = new HashSet<Vertex>(vertexes);
            int[] aD = Arrays.copyOf(d, d.length);
            int[] aCenterId = Arrays.copyOf(centerId, centerId.length);

            aResult.centers.add(v);
            aVertexes.remove(v);

            countDijkstra(v, new HashSet<Vertex>(vertexes), aD, aCenterId);

            aResult = findCentral(aResult, centersLeft - 1, aVertexes, aD, aCenterId, callback, progressDiff, level - 1);
            if (aResult.longest < ret.longest || (aResult.longest == ret.longest && aResult.sum < ret.sum)) {
                ret = aResult;
            }
        }

        return ret;
    }

    private void countDijkstra(final Vertex central, Set<Vertex> vertexes, int[] d, int[] centerId) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + " v: " + central.getId());

        int[] aD = new int[d.length];
        Arrays.fill(aD, Integer.MAX_VALUE);

        d[central.getId() - 1] = 0;
        aD[central.getId() - 1] = 0;
        centerId[central.getId() - 1] = central.getId();

        while (!vertexes.isEmpty()) {

            Vertex u = findMinVertex(aD, vertexes);
            if (aD[u.getId() - 1] == Integer.MAX_VALUE)
                break;

            vertexes.remove(u);

            Set<Vertex> neighbours = u.getNeighbours();
            for (Vertex v : neighbours) {
                int newDistance = d[u.getId() - 1] + Vertex.distance(u, v);
                if (d[v.getId() - 1] > newDistance) {
                    d[v.getId() - 1] = newDistance;
                    aD[v.getId() - 1] = newDistance;
                    centerId[v.getId() - 1] = v.getId();
                }
            }

        }
    }

    private Pair<Integer, Long> findMax(int[] d) {
        int max = Integer.MIN_VALUE;
        long sum = 0;
        for (int i = 0; i < d.length; i++) {
            max = Math.max(max, d[i]);
            sum += d[i];
        }

        return Pair.of(max, sum);
    }

    private Vertex findMinVertex(int[] d, Set<Vertex> vertexes) {
        int min = Integer.MAX_VALUE;
        Vertex ret = null;

        for (Vertex v : vertexes) {
            if (d[v.getId() - 1] <= min) {
                min = d[v.getId() - 1];
                ret = v;
            }
        }

        return ret;
    }

    private int initializeCenters(int centersCount) {
        Log.d(LOG_TAG, Log.getCurrentMethodName());

        Set<Vertex> vertexes = mGraph.getVertexes();
        mResult.setForCenters(centersCount);

        for (Vertex v : vertexes) {
            if (v.hasNeighbours()) {
                v.setShortestPathLength(Integer.MAX_VALUE);
            } else {
                v.setShortestPathLength(0);
                v.setNearestCenter(v);
                mResult.centers.add(v);
                centersCount--;
            }
        }

        return centersCount;
    }

    public ResultCase getResultCase() {
        if (mResultCase == null)
            mResultCase = ResultCase.GENERAL_ERROR;

        return mResultCase;
    }

    private ResultCase checkResultCase(int centralsCount) {
        if (mGraph.getVertexes().isEmpty())
            return ResultCase.NO_VERTEXES;

        if (centralsCount <= 0)
            return ResultCase.NO_CENTRALS;

        if (centralsCount > mGraph.getVertexes().size())
            return ResultCase.INSUFFICIENT_VERTEXES;

        if (centralsCount < mGraph.getSubgraphsCount())
            return ResultCase.TOO_MANY_SUBGRAPHS;

        if (centralsCount == mGraph.getVertexes().size())
            return ResultCase.CENTRALS_EQUALS_SUBGRAPHS;

        return ResultCase.DEFAULT;
    }
}
