package pl.elka.gis.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

import pl.elka.gis.logic.GraphResolver;
import pl.elka.gis.model.Edge;
import pl.elka.gis.model.Graph;
import pl.elka.gis.model.Vertex;

/**
 * @author Andrzej Makarewicz
 * @author pasu
 */
public class GraphPainterPanel extends JPanel {

    private static final String LOG_TAG = GraphPainterPanel.class.getSimpleName();
    //
    private Graph mGraph;
    private GraphResolver.Result mResult;
    //
    private static final Color BG_COLOR = Color.WHITE, VERTEX_COLOR = Color.BLUE, EDGE_COLOR = Color.DARK_GRAY,
            CENTER_COLOR = Color.RED, LONGEST_PATH_COLOR = Color.RED, VERTEX_ID_COLOR = Color.BLACK;
    private static final Font VERTEX_ID_FONT = new Font("Arial", Font.PLAIN, 10);
    private static final int VERTEX_DIM = 10, EDGE_DIM = 2;

    public GraphPainterPanel() {
        setBackground(BG_COLOR);
        reset();
    }

    public void reset() {
        mGraph = null;
        mResult = null;
    }

    public void setGraph(Graph graph) {
        mGraph = graph;
    }

    public void setResult(GraphResolver.Result result) {
        mResult = result;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (mGraph == null)
            return;

        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(EDGE_DIM));
        g2.setColor(EDGE_COLOR);
        Set<Edge> edges = mGraph.getEdges();
        for (Edge e : edges) {
            Pair<Vertex, Vertex> ends = e.getVertexes();
            g2.drawLine(ends.getLeft().getX(), ends.getLeft().getY(), ends.getRight().getX(), ends.getRight().getY());
        }

        // TODO longest path here

        Set<Vertex> vertexes = mGraph.getVertexes();
        g2.setColor(VERTEX_COLOR);
        int diff = VERTEX_DIM / 2;
        for (Vertex v : vertexes) {
            g2.fillOval(v.getX() - diff, v.getY() - diff, VERTEX_DIM, VERTEX_DIM);
        }

        if (mResult != null && mResult.hasCenters()) {
            Set<Vertex> centers = mResult.getCenters();
            g2.setColor(CENTER_COLOR);
            for (Vertex v : centers) {
                g2.fillOval(v.getX() - diff, v.getY() - diff, VERTEX_DIM, VERTEX_DIM);
            }
        }

        g2.setColor(VERTEX_ID_COLOR);
        g2.setFont(VERTEX_ID_FONT);
        FontMetrics fontMetrics = g2.getFontMetrics();
        int height = VERTEX_ID_FONT.getSize();
        for (Vertex v : vertexes) {
            String id = String.valueOf(v.getId());
            int width = fontMetrics.stringWidth(id);
            g2.drawString(id, v.getX() - width / 2, v.getY() - height);
        }

    }
}
