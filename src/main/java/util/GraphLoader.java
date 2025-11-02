package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads graph data from JSON files.
 * Supports both edge-weighted and node-weighted models.
 */
public class GraphLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class Graph {
        private final boolean directed;
        private final int n;
        private final List<Edge> edges;
        private final int source;
        private final String weightModel;

        public Graph(boolean directed, int n, List<Edge> edges, int source, String weightModel) {
            this.directed = directed;
            this.n = n;
            this.edges = edges;
            this.source = source;
            this.weightModel = weightModel;
        }

        public boolean isDirected() {
            return directed;
        }

        public int getN() {
            return n;
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public int getSource() {
            return source;
        }

        public String getWeightModel() {
            return weightModel;
        }
    }

    public static class Edge {
        private final int u;
        private final int v;
        private final int w;

        public Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }

        public int getU() {
            return u;
        }

        public int getV() {
            return v;
        }

        public int getW() {
            return w;
        }
    }

    public static Graph loadGraph(String filePath) throws IOException {
        JsonNode root = mapper.readTree(new File(filePath));
        
        boolean directed = root.get("directed").asBoolean();
        int n = root.get("n").asInt();
        int source = root.get("source").asInt();
        String weightModel = root.get("weight_model").asText();

        List<Edge> edges = new ArrayList<>();
        JsonNode edgesArray = root.get("edges");
        for (JsonNode edgeNode : edgesArray) {
            int u = edgeNode.get("u").asInt();
            int v = edgeNode.get("v").asInt();
            int w = edgeNode.get("w").asInt();
            edges.add(new Edge(u, v, w));
        }

        return new Graph(directed, n, edges, source, weightModel);
    }
}

