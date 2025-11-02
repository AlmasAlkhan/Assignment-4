package graph.dagsp;

import util.Metrics;

import java.util.*;

/**
 * Shortest and longest path algorithms for Directed Acyclic Graphs (DAGs).
 * Supports edge-weighted graphs. Uses dynamic programming over topological order.
 */
public class DAGShortestPath {
    private List<List<WeightedEdge>> graph;
    private int n;
    private String weightModel;
    private Metrics metrics;

    /**
     * Represents a weighted edge in the graph.
     */
    public static class WeightedEdge {
        private final int v;
        private final int weight;

        public WeightedEdge(int v, int weight) {
            this.v = v;
            this.weight = weight;
        }

        public int getV() {
            return v;
        }

        public int getWeight() {
            return weight;
        }
    }

    /**
     * Constructor for DAG shortest path algorithms.
     * @param graph adjacency list with weighted edges
     * @param n number of vertices
     * @param weightModel weight model type ("edge" or "node")
     */
    public DAGShortestPath(List<List<WeightedEdge>> graph, int n, String weightModel) {
        this.graph = graph;
        this.n = n;
        this.weightModel = weightModel;
        this.metrics = new Metrics();
    }

    /**
     * Compute single-source shortest paths using DP over topological order.
     * @param source source vertex index
     * @param topoOrder topological order of vertices
     * @return ShortestPathResult with distances and parent pointers
     */
    public ShortestPathResult shortestPaths(int source, List<Integer> topoOrder) {
        metrics.reset();
        metrics.startTiming();

        // Initialize distances: all unreachable except source
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        // Relax edges in topological order
        for (int u : topoOrder) {
            if (dist[u] == Integer.MAX_VALUE) {
                continue; // Skip unreachable vertices
            }

            for (WeightedEdge edge : graph.get(u)) {
                metrics.incrementRelaxations();
                int v = edge.getV();
                int weight = edge.getWeight();
                
                // Relaxation step
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                }
            }
        }

        metrics.stopTiming();
        return new ShortestPathResult(dist, parent);
    }

    /**
     * Compute single-source longest paths (critical path).
     * Uses sign inversion: finds longest by maximizing distances.
     * @param source source vertex index
     * @param topoOrder topological order of vertices
     * @return LongestPathResult with distances and parent pointers
     */
    public LongestPathResult longestPath(int source, List<Integer> topoOrder) {
        metrics.reset();
        metrics.startTiming();

        // Initialize distances: all unreachable except source
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        dist[source] = 0;

        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        // Maximize distances in topological order
        for (int u : topoOrder) {
            if (dist[u] == Integer.MIN_VALUE) {
                continue;
            }

            for (WeightedEdge edge : graph.get(u)) {
                metrics.incrementRelaxations();
                int v = edge.getV();
                int weight = edge.getWeight();
                
                // Maximization step for longest path
                if (dist[u] + weight > dist[v]) {
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                }
            }
        }

        metrics.stopTiming();
        return new LongestPathResult(dist, parent);
    }

    /**
     * Reconstruct path from source to target using parent array.
     * @param source source vertex index
     * @param target target vertex index
     * @param parent parent pointers from shortest/longest path computation
     * @return path as list of vertex indices, or empty list if no path exists
     */
    public List<Integer> reconstructPath(int source, int target, int[] parent) {
        List<Integer> path = new ArrayList<>();
        int current = target;
        
        while (current != -1) {
            path.add(current);
            current = parent[current];
        }
        
        Collections.reverse(path);
        
        if (path.get(0) != source) {
            return new ArrayList<>();
        }
        
        return path;
    }

    /**
     * Find critical path (longest path) in the entire DAG.
     * @param topoOrder topological order of vertices
     * @return CriticalPathResult with path and length
     */
    public CriticalPathResult findCriticalPath(List<Integer> topoOrder) {
        // Compute longest paths from first vertex in topological order
        LongestPathResult longest = longestPath(topoOrder.get(0), topoOrder);
        
        // Find vertex with maximum distance
        int maxDist = Integer.MIN_VALUE;
        int target = -1;
        for (int i = 0; i < n; i++) {
            if (longest.getDist()[i] > maxDist && longest.getDist()[i] != Integer.MIN_VALUE) {
                maxDist = longest.getDist()[i];
                target = i;
            }
        }

        // Reconstruct the critical path
        List<Integer> path = reconstructPath(topoOrder.get(0), target, longest.getParent());
        return new CriticalPathResult(path, maxDist);
    }

    /**
     * Get metrics for algorithm performance tracking.
     * @return Metrics object with operation counts and timing
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Get weight model type.
     * @return weight model string ("edge" or "node")
     */
    public String getWeightModel() {
        return weightModel;
    }

    /**
     * Result container for shortest path computation.
     */
    public static class ShortestPathResult {
        private final int[] dist;
        private final int[] parent;

        public ShortestPathResult(int[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }

        public int[] getDist() {
            return dist;
        }

        public int[] getParent() {
            return parent;
        }
    }

    /**
     * Result container for longest path computation.
     */
    public static class LongestPathResult {
        private final int[] dist;
        private final int[] parent;

        public LongestPathResult(int[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }

        public int[] getDist() {
            return dist;
        }

        public int[] getParent() {
            return parent;
        }
    }

    /**
     * Result container for critical path computation.
     */
    public static class CriticalPathResult {
        private final List<Integer> path;
        private final int length;

        public CriticalPathResult(List<Integer> path, int length) {
            this.path = path;
            this.length = length;
        }

        public List<Integer> getPath() {
            return path;
        }

        public int getLength() {
            return length;
        }
    }

    /**
     * Create DAGShortestPath instance from GraphLoader.Graph.
     * @param graph graph loaded from JSON file
     * @return DAGShortestPath instance ready to compute shortest/longest paths
     */
    public static DAGShortestPath fromGraphLoader(util.GraphLoader.Graph graph) {
        int n = graph.getN();
        List<List<WeightedEdge>> adjList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }

        for (util.GraphLoader.Edge edge : graph.getEdges()) {
            adjList.get(edge.getU()).add(new WeightedEdge(edge.getV(), edge.getW()));
        }

        return new DAGShortestPath(adjList, n, graph.getWeightModel());
    }
}

