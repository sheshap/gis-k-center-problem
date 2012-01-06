package pl.elka.gis.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;

import pl.elka.gis.logic.Controller;
import pl.elka.gis.model.GEdge;
import pl.elka.gis.model.GVertex;
import pl.elka.gis.utils.Log;

public class GraphPaintingPanel extends JPanel {

    private static final String LOG_TAG = "GraphPaintingPanel";
    private Controller mController;
    private static final Color BG_COLOR = Color.BLUE;
    private static final Color VERTEX_COLOR = Color.WHITE;
    private static final Color EDGE_COLOR = Color.GREEN;
    private static final Color CENTER_COLOR = Color.RED;
    private static final Color EDGE_ON_PATH_COLOR = Color.ORANGE;
    private static final Color VERTEX_ID_COLOR = Color.BLACK;
    private static final Font VERTEX_ID_FONT = new Font("Arial", Font.BOLD, 20);
    private static final int VERTEX_DIM = 10; // dimension of vertex on panel in pixels
    private static final int EDGE_DIM = 1; // thickness of edge on panel in pixels
    private Vector<GVertex> mVertexes;
    private Vector<GVertex> mCenters;
    private Vector<GEdge> mEdges;
    private Vector<GEdge> mLongestPathEdges;

    public GraphPaintingPanel() {
        setBackground(BG_COLOR);
    }

    public void setPlainGraphController(Controller pController) {
        Log.d(LOG_TAG, "setPlainGraphController");
        mController = pController;
        mVertexes = new Vector<GVertex>();
        mCenters = new Vector<GVertex>();
        mEdges = new Vector<GEdge>();
        mLongestPathEdges = new Vector<GEdge>();
        mVertexes.addAll(mController.getVertexSet());
        mCenters.addAll(mController.getResultSet().getCentralVertexSet());
        mLongestPathEdges.addAll(mController.getResultSet().getLongestPathEdgesSet());
        MapIterator it = mController.getEdgesMap().mapIterator();
        while (it.hasNext()) {
            MultiKey key = (MultiKey) it.next();
            mEdges.add(new GEdge((Integer) key.getKey(0), (Integer) key.getKey(1), (Integer) it.getValue()));
        }
    }

    public void refreshGraph() {
        Log.d(LOG_TAG, "refreshGraph");
        if (mController == null) {
            Log.d(LOG_TAG, "mController==null - unable to refresh view");
            return;
        }
        Log.d(LOG_TAG, "mVertexes.size() == " + mVertexes.size());
        Log.d(LOG_TAG, "mCenters.size() == " + mCenters.size());
        Log.d(LOG_TAG, "mEdges.size() == " + mEdges.size());
        Log.d(LOG_TAG, "mLongestPathEdges.size() == " + mLongestPathEdges.size());
        // refreshing starts here
        // check if result set has been already created, if no then create it
        if (mCenters.size() == 0) {
            Log.d(LOG_TAG, "mCenters.size() == 0 add them");
            mCenters.addAll(mController.getResultSet().getCentralVertexSet());
            Log.d(LOG_TAG, "mCenters.size() == " + mCenters.size());
        }
        if (mLongestPathEdges.size() == 0) {
            Log.d(LOG_TAG, "mLongestPathEdges.size() == 0 add them");
            mLongestPathEdges.addAll(mController.getResultSet().getLongestPathEdgesSet());
            Log.d(LOG_TAG, "mLongestPathEdges.size() == " + mLongestPathEdges.size());
        }
        revalidate();
        // repaint();
    }

    private GVertex findVertexWithId(int id, Vector<GVertex> vec) {
        for (Iterator<GVertex> iterator = vec.iterator(); iterator.hasNext();) {
            GVertex gVertex = iterator.next();
            if (id == gVertex.getVertexId()) {
                return gVertex;
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (mController == null) {
            return;
        }
        // draw edges
        if (EDGE_DIM > 1) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(EDGE_DIM));
            for (Iterator<GEdge> iterator = mEdges.iterator(); iterator.hasNext();) {
                GEdge e = iterator.next();
                g2.setColor(EDGE_COLOR);
                GVertex v1 = findVertexWithId(e.getStartingVertexId(), mVertexes);
                GVertex v2 = findVertexWithId(e.getEndingVertexId(), mVertexes);
                if (v1 != null && v2 != null) {
                    g2.drawLine(v1.getCoord().x, v1.getCoord().y, v2.getCoord().x, v2.getCoord().y);
                }
            }
        } else {
            for (Iterator<GEdge> iterator = mEdges.iterator(); iterator.hasNext();) {
                GEdge e = iterator.next();
                g.setColor(EDGE_COLOR);
                GVertex v1 = findVertexWithId(e.getStartingVertexId(), mVertexes);
                GVertex v2 = findVertexWithId(e.getEndingVertexId(), mVertexes);
                if (v1 != null && v2 != null) {
                    g.drawLine(v1.getCoord().x, v1.getCoord().y, v2.getCoord().x, v2.getCoord().y);
                }
            }
        }
        // draw longest path edges
        if (EDGE_DIM > 1) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(EDGE_DIM));
            for (Iterator<GEdge> iterator = mLongestPathEdges.iterator(); iterator.hasNext();) {
                GEdge e = iterator.next();
                g2.setColor(EDGE_ON_PATH_COLOR);
                GVertex v1 = findVertexWithId(e.getStartingVertexId(), mVertexes);
                GVertex v2 = findVertexWithId(e.getEndingVertexId(), mVertexes);
                if (v1 != null && v2 != null) {
                    g2.drawLine(v1.getCoord().x, v1.getCoord().y, v2.getCoord().x, v2.getCoord().y);
                }
            }
        } else {
            for (Iterator<GEdge> iterator = mLongestPathEdges.iterator(); iterator.hasNext();) {
                GEdge e = iterator.next();
                g.setColor(EDGE_ON_PATH_COLOR);
                GVertex v1 = findVertexWithId(e.getStartingVertexId(), mVertexes);
                GVertex v2 = findVertexWithId(e.getEndingVertexId(), mVertexes);
                if (v1 != null && v2 != null) {
                    g.drawLine(v1.getCoord().x, v1.getCoord().y, v2.getCoord().x, v2.getCoord().y);
                }
            }
        }
        // draw vertexes
        for (Iterator<GVertex> iterator = mVertexes.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            g.setColor(VERTEX_COLOR);
            g.fillRect(v.getCoord().x - (int) (VERTEX_DIM / 2), v.getCoord().y - (int) (VERTEX_DIM / 2), VERTEX_DIM, VERTEX_DIM);
            g.setColor(VERTEX_ID_COLOR);
            g.setFont(VERTEX_ID_FONT);
            FontMetrics fm = g.getFontMetrics();
            int verIdWidth = fm.stringWidth(String.valueOf(v.getVertexId()));
            g.drawString(String.valueOf(v.getVertexId()), v.getCoord().x - verIdWidth - VERTEX_DIM, v.getCoord().y
                    + (int) (VERTEX_ID_FONT.getSize() / 6));
        }
        // draw centers
        for (Iterator<GVertex> iterator = mCenters.iterator(); iterator.hasNext();) {
            GVertex v = iterator.next();
            g.setColor(CENTER_COLOR);
            g.fillRect(v.getCoord().x - (int) (VERTEX_DIM / 2), v.getCoord().y - (int) (VERTEX_DIM / 2), VERTEX_DIM, VERTEX_DIM);
            g.setColor(VERTEX_ID_COLOR);
            FontMetrics fm = g.getFontMetrics();
            int verIdWidth = fm.stringWidth(String.valueOf(v.getVertexId()));
            g.drawString(String.valueOf(v.getVertexId()), v.getCoord().x - verIdWidth - VERTEX_DIM, v.getCoord().y
                    + (int) (VERTEX_ID_FONT.getSize() / 6));
        }
    }
}
