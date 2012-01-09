package pl.elka.gis.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

        Set<Vertex> mCenters;
        int mCentralsCount;
        int mLongest = Integer.MAX_VALUE;

        public Result(int centralsCount) {
            mCentralsCount = centralsCount;
            mCenters = new HashSet<Vertex>(centralsCount);
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
    }

    public void resolve(int centralsCount, ProgressCallback callback) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + " start");

        mResultCase = checkResultCase(centralsCount);

        switch (mResultCase) {
            case NO_VERTEXES :
            case NO_CENTRALS :
            case INSUFFICIENT_VERTEXES :
            case TOO_MANY_SUBGRAPHS :
                Log.d(LOG_TAG, Log.getCurrentMethodName() + " Calculation error: " + mResultCase.name());
                // callback.calculationError(mResultCase.name());
                return;
            case CENTRALS_EQUALS_SUBGRAPHS :
            case DEFAULT :
            default :
                int centralsLeft = initializeCenters(centralsCount);

                Log.d(LOG_TAG, Log.getCurrentMethodName() + " centrals: " + centralsCount);

                Set<Vertex> vertexes = mGraph.getVertexes();
                Vertex[] center = new Vertex[vertexes.size()];
                Vertex[] result = new Vertex[vertexes.size()];
                int[] d = new int[vertexes.size()];
                Arrays.fill(d, Integer.MAX_VALUE);
                boolean[] notAvailable = new boolean[vertexes.size()];

                for (Vertex v : vertexes) {
                    if (v.isCenter()) {
                        notAvailable[v.getId() - 1] = true;
                        center[v.getId() - 1] = v;
                        result[v.getId() - 1] = v;
                        d[v.getId() - 1] = 0;
                    }
                }

                mResult.mLongest = findCentral(Integer.MAX_VALUE, vertexes, notAvailable, centralsLeft, d, center, result);

                for (int i = 0; i < result.length; i++) {
                    mResult.mCenters.add(result[i]);
                }
                // callback.calculationFinished();

                StringBuilder sb = new StringBuilder();
                for (Vertex v : mResult.mCenters) {
                    ;
                    sb.append(v.getId());
                    sb.append(" ");
                }

                Log.d(LOG_TAG, Log.getCurrentMethodName() + " Case: " + mResultCase.name() + " Longest: " + mResult.mLongest
                        + " Centers: " + sb.toString());

                return;
        }
    }

    private String printCentrals(Vertex[] vertexes) {
        StringBuilder sb = new StringBuilder();
        for (Vertex v : vertexes) {
            sb.append(v.getId());
            sb.append(" ");
        }

        return sb.toString();
    }

    // FIXME: just temporary on arrays in order to check if it's working

    private int findCentral(int currentLongest, final Set<Vertex> vertexes, boolean[] notAvailable, int centralsLeft, int[] d, Vertex[] center, Vertex[] result) {
        Log.d(LOG_TAG, Log.getCurrentMethodName() + " centrals: " + centralsLeft + " " + Arrays.toString(notAvailable) + " "
                + Arrays.toString(d));

        if (centralsLeft == 0) {
            return findMax(d);
        }

        for (Vertex v : vertexes) {
            if (notAvailable[v.getId() - 1])
                continue;

            // mResult.mCenters.add(v);
            int[] tmpD = Arrays.copyOf(d, d.length);
            Vertex[] tmpCenter = Arrays.copyOf(center, center.length);

            countDijkstra(v, vertexes, tmpD, tmpCenter, notAvailable);
            notAvailable[v.getId() - 1] = true;
            int pom = findCentral(currentLongest, vertexes, notAvailable, centralsLeft - 1, tmpD, tmpCenter, result);
            if (pom < currentLongest) {
                Log.d(LOG_TAG, Log.getCurrentMethodName() + " RESULT: centrals: " + printCentrals(tmpCenter) + " d: "
                        + Arrays.toString(tmpD));

                for (int i = 0; i < tmpCenter.length; i++) {
                    result[i] = tmpCenter[i];
                }
                currentLongest = pom;
            }
            notAvailable[v.getId() - 1] = false;

        }

        return currentLongest;
    }

    private int findMax(int[] d) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < d.length; i++) {
            max = Math.max(max, d[i]);
        }

        return max;
    }

    private void countDijkstra(final Vertex central, final Set<Vertex> vertexes, int[] d, Vertex[] center, final boolean[] notAvailable) {
        // Log.d(LOG_TAG, Log.getCurrentMethodName() + " v: " + central.getId());

        int[] pom_d = new int[vertexes.size()];
        Arrays.fill(pom_d, Integer.MAX_VALUE);
        boolean[] taken = Arrays.copyOf(notAvailable, notAvailable.length);
        d[central.getId() - 1] = 0;
        pom_d[central.getId() - 1] = 0;
        center[central.getId() - 1] = central;

        while (notEmpty(taken)) {

            // Log.d(LOG_TAG, Log.getCurrentMethodName() + "while");

            int minIdx = findMinIndex(pom_d, taken);
            if (pom_d[minIdx] == Integer.MAX_VALUE)
                break;

            Vertex u = null;
            for (Vertex ver : vertexes) {
                if (ver.getId() - 1 == minIdx) {
                    u = ver;
                    taken[minIdx] = true;
                    break;
                }
            }
            // Log.d(LOG_TAG, Log.getCurrentMethodName() + "u:" + u.getId());

            Set<Vertex> neighbours = u.getNeighbours();
            for (Vertex v : neighbours) {
                int newDistance = d[u.getId() - 1] + Vertex.distance(u, v);
                if (d[v.getId() - 1] > newDistance) {
                    d[v.getId() - 1] = newDistance;
                    pom_d[v.getId() - 1] = newDistance;
                    center[v.getId() - 1] = central;
                }
            }
        }
    }

    private boolean notEmpty(boolean[] taken) {
        for (int i = 0; i < taken.length; i++) {
            if (!taken[i])
                return true;
        }

        return false;
    }

    private int findMinIndex(int[] pom_d, boolean[] taken) {
        int min = Integer.MAX_VALUE, idx = 0;

        for (int i = 0; i < pom_d.length; i++) {
            if (!taken[i] && pom_d[i] <= min) {
                min = pom_d[i];
                idx = i;
            }
        }

        return idx;
    }

    private int initializeCenters(int centralsCount) {
        Log.d(LOG_TAG, Log.getCurrentMethodName());

        Set<Vertex> vertexes = mGraph.getVertexes();
        Set<Vertex> pomVertexes = new HashSet<Vertex>();
        mResult = new Result(centralsCount);

        for (Vertex v : vertexes) {
            if (v.hasNeighbours()) {
                v.setShortestPathLength(Integer.MAX_VALUE);
                pomVertexes.add(v);
            } else {
                v.setShortestPathLength(0);
                v.setNearestCenter(v);
                mResult.mCenters.add(v);
                centralsCount--;
            }
        }

        return centralsCount;
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