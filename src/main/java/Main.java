import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import util.GraphLoader;

import java.util.List;

/**
 * Main entry point for Smart City Scheduling assignment.
 * Demonstrates SCC, topological sorting, and shortest paths in DAGs.
 */
public class Main {
    public static void main(String[] args) {
        try {
            GraphLoader.Graph graph = GraphLoader.loadGraph("tasks (1).json");
            
            System.out.println("=".repeat(60));
            System.out.println("GRAPH INFORMATION");
            System.out.println("=".repeat(60));
            System.out.println("Vertices: " + graph.getN());
            System.out.println("Edges: " + graph.getEdges().size());
            System.out.println("Source: " + graph.getSource());
            System.out.println("Weight model: " + graph.getWeightModel());
            System.out.println();
            
            System.out.println("=".repeat(60));
            System.out.println("1. STRONGLY CONNECTED COMPONENTS (SCC) - TARJAN");
            System.out.println("=".repeat(60));
            TarjanSCC tarjan = TarjanSCC.fromGraphLoader(graph);
            List<List<Integer>> sccs = tarjan.findSCCs();
            
            System.out.println("Number of SCCs found: " + sccs.size());
            System.out.println();
            System.out.println("SCC Details:");
            for (int i = 0; i < sccs.size(); i++) {
                List<Integer> component = sccs.get(i);
                System.out.println("  SCC " + i + ": " + component + " (size: " + component.size() + ")");
            }
            System.out.println();
            System.out.println("SCC Metrics:");
            System.out.println("  DFS visits: " + tarjan.getMetrics().getDfsVisits());
            System.out.println("  Edge traversals: " + tarjan.getMetrics().getEdgeTraversals());
            System.out.println("  Execution time: " + String.format("%.3f", tarjan.getMetrics().getElapsedTimeMs()) + " ms");
            System.out.println();
            
            List<List<Integer>> condensation = tarjan.buildCondensationGraph();
            System.out.println("Condensation graph (DAG of SCCs):");
            for (int i = 0; i < condensation.size(); i++) {
                System.out.println("  SCC " + i + " -> " + condensation.get(i));
            }
            System.out.println();
            
            System.out.println("=".repeat(60));
            System.out.println("2. TOPOLOGICAL SORTING");
            System.out.println("=".repeat(60));
            TopologicalSort topo = new TopologicalSort(condensation, condensation.size());
            
            System.out.println("2.1 Kahn's Algorithm:");
            List<Integer> topoOrderKahn = topo.kahnSort();
            System.out.println("  Topological order of SCCs: " + topoOrderKahn);
            System.out.println("  Metrics:");
            System.out.println("    Queue pushes: " + topo.getMetrics().getQueuePushes());
            System.out.println("    Queue pops: " + topo.getMetrics().getQueuePops());
            System.out.println("    Edge traversals: " + topo.getMetrics().getEdgeTraversals());
            System.out.println("    Execution time: " + String.format("%.3f", topo.getMetrics().getElapsedTimeMs()) + " ms");
            System.out.println();
            
            TopologicalSort topoDfs = new TopologicalSort(condensation, condensation.size());
            System.out.println("2.2 DFS-based Algorithm:");
            List<Integer> topoOrderDfs = topoDfs.dfsSort();
            System.out.println("  Topological order of SCCs: " + topoOrderDfs);
            System.out.println("  Metrics:");
            System.out.println("    DFS visits: " + topoDfs.getMetrics().getDfsVisits());
            System.out.println("    Edge traversals: " + topoDfs.getMetrics().getEdgeTraversals());
            System.out.println("    Execution time: " + String.format("%.3f", topoDfs.getMetrics().getElapsedTimeMs()) + " ms");
            System.out.println();
            
            List<Integer> originalOrder = topo.sortOriginalVertices(sccs, topoOrderKahn);
            System.out.println("Original vertex order (from SCC compression):");
            System.out.println("  " + originalOrder);
            System.out.println();
            
            System.out.println("=".repeat(60));
            System.out.println("3. SHORTEST PATHS IN DAG");
            System.out.println("=".repeat(60));
            DAGShortestPath dagSP = DAGShortestPath.fromGraphLoader(graph);
            
            System.out.println("3.1 Single-Source Shortest Paths:");
            DAGShortestPath.ShortestPathResult shortest = dagSP.shortestPaths(graph.getSource(), originalOrder);
            System.out.println("  Source: " + graph.getSource());
            System.out.println("  Shortest distances:");
            for (int i = 0; i < graph.getN(); i++) {
                if (shortest.getDist()[i] != Integer.MAX_VALUE) {
                    System.out.println("    Vertex " + i + ": " + shortest.getDist()[i]);
                } else {
                    System.out.println("    Vertex " + i + ": unreachable");
                }
            }
            System.out.println("  Metrics:");
            System.out.println("    Edge relaxations: " + dagSP.getMetrics().getRelaxations());
            System.out.println("    Execution time: " + String.format("%.3f", dagSP.getMetrics().getElapsedTimeMs()) + " ms");
            System.out.println();
            
            int target = -1;
            for (int i = 0; i < graph.getN(); i++) {
                if (shortest.getDist()[i] != Integer.MAX_VALUE && i != graph.getSource()) {
                    target = i;
                    break;
                }
            }
            if (target != -1) {
                List<Integer> path = dagSP.reconstructPath(graph.getSource(), target, shortest.getParent());
                System.out.println("  Example shortest path from " + graph.getSource() + " to " + target + ":");
                System.out.println("    Path: " + path);
                System.out.println("    Length: " + shortest.getDist()[target]);
                System.out.println();
            }
            
            System.out.println("3.2 Longest Path (Critical Path):");
            DAGShortestPath dagSPLongest = DAGShortestPath.fromGraphLoader(graph);
            DAGShortestPath.CriticalPathResult critical = dagSPLongest.findCriticalPath(originalOrder);
            System.out.println("  Critical path: " + critical.getPath());
            System.out.println("  Critical path length: " + critical.getLength());
            System.out.println("  Metrics:");
            System.out.println("    Edge relaxations: " + dagSPLongest.getMetrics().getRelaxations());
            System.out.println("    Execution time: " + String.format("%.3f", dagSPLongest.getMetrics().getElapsedTimeMs()) + " ms");
            System.out.println();
            
            System.out.println("=".repeat(60));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
