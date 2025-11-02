package graph.scc;

import util.Metrics;
import util.GraphLoader;

import java.util.*;

/**
 * Implementation of Tarjan's algorithm for finding Strongly Connected Components (SCC).
 * Builds a condensation graph (DAG of components) from the original directed graph.
 */
public class TarjanSCC {
    private List<List<Integer>> graph;
    private int n;
    private int[] disc;
    private int[] low;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int time;
    private List<List<Integer>> sccs;
    private Metrics metrics;

    /**
     * Constructor for TarjanSCC algorithm.
     * @param graph adjacency list representation of the directed graph
     * @param n number of vertices
     */
    public TarjanSCC(List<List<Integer>> graph, int n) {
        this.graph = graph;
        this.n = n;
        this.disc = new int[n];
        this.low = new int[n];
        this.onStack = new boolean[n];
        this.stack = new Stack<>();
        this.time = 0;
        this.sccs = new ArrayList<>();
        this.metrics = new Metrics();
        Arrays.fill(disc, -1);
    }

    /**
     * Find all strongly connected components using Tarjan's algorithm.
     * @return list of SCCs, where each SCC is a list of vertex indices
     */
    public List<List<Integer>> findSCCs() {
        metrics.reset();
        metrics.startTiming();

        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                dfs(i);
            }
        }

        metrics.stopTiming();
        return new ArrayList<>(sccs);
    }

    /**
     * DFS traversal for Tarjan's algorithm.
     * Updates discovery time, low link, and identifies SCC roots.
     */
    private void dfs(int u) {
        metrics.incrementDfsVisits();
        disc[u] = time;
        low[u] = time;
        time++;
        stack.push(u);
        onStack[u] = true;

        // Explore neighbors
        for (int v : graph.get(u)) {
            metrics.incrementEdgeTraversals();
            if (disc[v] == -1) {
                // Unvisited vertex - recurse
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Back edge to vertex in current DFS tree
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        // If u is root of SCC, pop all vertices in this component
        if (low[u] == disc[u]) {
            List<Integer> component = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                component.add(v);
            } while (v != u);
            sccs.add(component);
        }
    }

    /**
     * Get mapping from vertex to its SCC index.
     * @return map from vertex index to SCC index
     */
    public Map<Integer, Integer> getVertexToSCC() {
        Map<Integer, Integer> vertexToSCC = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (int vertex : sccs.get(i)) {
                vertexToSCC.put(vertex, i);
            }
        }
        return vertexToSCC;
    }

    /**
     * Build condensation graph - DAG where each SCC becomes a single vertex.
     * @return adjacency list of the condensation graph
     */
    public List<List<Integer>> buildCondensationGraph() {
        Map<Integer, Integer> vertexToSCC = getVertexToSCC();
        int numSCCs = sccs.size();
        List<Set<Integer>> condensationGraph = new ArrayList<>();
        for (int i = 0; i < numSCCs; i++) {
            condensationGraph.add(new HashSet<>());
        }

        for (int u = 0; u < n; u++) {
            int sccU = vertexToSCC.get(u);
            for (int v : graph.get(u)) {
                int sccV = vertexToSCC.get(v);
                if (sccU != sccV) {
                    condensationGraph.get(sccU).add(sccV);
                }
            }
        }

        List<List<Integer>> result = new ArrayList<>();
        for (Set<Integer> set : condensationGraph) {
            result.add(new ArrayList<>(set));
        }
        return result;
    }

    /**
     * Get metrics for algorithm performance tracking.
     * @return Metrics object with operation counts and timing
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Create TarjanSCC instance from GraphLoader.Graph.
     * @param graph graph loaded from JSON file
     * @return TarjanSCC instance ready to compute SCCs
     */
    public static TarjanSCC fromGraphLoader(GraphLoader.Graph graph) {
        int n = graph.getN();
        List<List<Integer>> adjList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }

        for (GraphLoader.Edge edge : graph.getEdges()) {
            adjList.get(edge.getU()).add(edge.getV());
        }

        return new TarjanSCC(adjList, n);
    }
}

