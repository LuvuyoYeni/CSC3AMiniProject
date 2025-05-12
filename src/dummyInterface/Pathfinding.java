package dummyInterface;

import java.util.*;

public class Pathfinding {
    public static class PathResult {
        public final List<GridGraph.Node> path;
        public final Set<GridGraph.Node> explored;
        
        public PathResult(List<GridGraph.Node> path, Set<GridGraph.Node> explored) {
            this.path = path;
            this.explored = explored;
        }
    }

    public static PathResult findPathWithTracking(EnemyBehavior behavior, GridGraph graph, 
                                               GridGraph.Node start, GridGraph.Node goal) {
        if (behavior instanceof AggressiveBehavior || behavior instanceof HunterBehavior) {
            return astarWithTracking(graph, start, goal);
        } else {
            return bfsWithTracking(graph, start, goal);
        }
    }

    public static PathResult bfsWithTracking(GridGraph graph, GridGraph.Node start, GridGraph.Node goal) {
        if (graph == null || start == null || goal == null) {
            return new PathResult(Collections.emptyList(), Collections.emptySet());
        }

        Queue<GridGraph.Node> queue = new LinkedList<>();
        Map<GridGraph.Node, GridGraph.Node> cameFrom = new HashMap<>();
        Set<GridGraph.Node> explored = new HashSet<>();
        
        queue.add(start);
        explored.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            GridGraph.Node current = queue.poll();
            
            if (current.equals(goal)) {
                break;
            }

            for (GridGraph.Node neighbor : graph.getNeighbors(current)) {
                if (!explored.contains(neighbor)) {
                    explored.add(neighbor);
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return new PathResult(reconstructPath(cameFrom, start, goal), explored);
    }

    public static PathResult astarWithTracking(GridGraph graph, GridGraph.Node start, GridGraph.Node goal) {
        if (graph == null || start == null || goal == null) {
            return new PathResult(Collections.emptyList(), Collections.emptySet());
        }

        PriorityQueue<GridGraph.Node> queue = new PriorityQueue<>(
            Comparator.comparingInt(node -> node.distance + heuristic(node, goal))
        );
        
        Map<GridGraph.Node, Integer> gScore = new HashMap<>();
        Map<GridGraph.Node, GridGraph.Node> cameFrom = new HashMap<>();
        Set<GridGraph.Node> explored = new HashSet<>();
        
        for (GridGraph.Node node : graph.getAllNodes()) {
            gScore.put(node, Integer.MAX_VALUE);
            node.distance = Integer.MAX_VALUE;
        }
        
        gScore.put(start, 0);
        start.distance = heuristic(start, goal);
        queue.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            GridGraph.Node current = queue.poll();
            explored.add(current);
            
            if (current.equals(goal)) {
                break;
            }

            for (GridGraph.Node neighbor : graph.getNeighbors(current)) {
                int tentativeG = gScore.get(current) + 1;
                if (tentativeG < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    neighbor.distance = tentativeG + heuristic(neighbor, goal);
                    
                    // Update priority queue
                    if (queue.contains(neighbor)) {
                        queue.remove(neighbor);
                    }
                    queue.add(neighbor);
                }
            }
        }

        return new PathResult(reconstructPath(cameFrom, start, goal), explored);
    }

    public static List<GridGraph.Node> bfs(GridGraph graph, GridGraph.Node start, GridGraph.Node goal) {
        return bfsWithTracking(graph, start, goal).path;
    }

    public static List<GridGraph.Node> dijkstra(GridGraph graph, GridGraph.Node start, GridGraph.Node goal) {
        if (graph == null || start == null || goal == null) {
            return Collections.emptyList();
        }

        PriorityQueue<GridGraph.Node> queue = new PriorityQueue<>(
            Comparator.comparingInt(node -> node.distance)
        );
        
        Map<GridGraph.Node, Integer> distances = new HashMap<>();
        Map<GridGraph.Node, GridGraph.Node> cameFrom = new HashMap<>();
        
        for (GridGraph.Node node : graph.getAllNodes()) {
            distances.put(node, Integer.MAX_VALUE);
            node.distance = Integer.MAX_VALUE;
        }
        
        start.distance = 0;
        distances.put(start, 0);
        queue.add(start);
        cameFrom.put(start, null);

        while (!queue.isEmpty()) {
            GridGraph.Node current = queue.poll();
            
            if (current.equals(goal)) {
                break;
            }

            for (GridGraph.Node neighbor : graph.getNeighbors(current)) {
                int newDist = distances.get(current) + 1;
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    neighbor.distance = newDist;
                    cameFrom.put(neighbor, current);
                    
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return reconstructPath(cameFrom, start, goal);
    }

    public static List<GridGraph.Node> astar(GridGraph graph, GridGraph.Node start, GridGraph.Node goal) {
        return astarWithTracking(graph, start, goal).path;
    }

    private static int heuristic(GridGraph.Node a, GridGraph.Node b) {
        // Manhattan distance
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private static List<GridGraph.Node> reconstructPath(Map<GridGraph.Node, GridGraph.Node> cameFrom,
                                                      GridGraph.Node start, GridGraph.Node goal) {
        List<GridGraph.Node> path = new ArrayList<>();
        if (!cameFrom.containsKey(goal)) return path;
        
        GridGraph.Node current = goal;
        while (current != null && !current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        
        return path;
    }

    // Behavior-specific implementations
    public static class BehaviorMethods {
        public static PathResult cautiousPath(GridGraph graph, GridGraph.Node start, GridGraph.Node target) {
            // Create modified graph with wall penalties
            GridGraph modifiedGraph = new GridGraph(graph.getRows(), graph.getCols());
            
            // Copy walls and add danger zones
            for (GridGraph.Node node : graph.getAllNodes()) {
                if (graph.isWall(node)) {
                    modifiedGraph.setWall(modifiedGraph.getNode(node.row, node.col), true);
                    // Mark adjacent nodes as dangerous
                    for (GridGraph.Node neighbor : graph.getNeighbors(node)) {
                        GridGraph.Node modNode = modifiedGraph.getNode(neighbor.row, neighbor.col);
                        modNode.distance += 5; // Penalty
                    }
                }
            }
            
            return astarWithTracking(modifiedGraph, start, target);
        }

        public static PathResult hunterPath(GridGraph graph, GridGraph.Node start, GridGraph.Node target, 
                                         int lastPlayerX, int lastPlayerY) {
            // Predict movement
            int predictedX = target.col + (target.col - lastPlayerX);
            int predictedY = target.row + (target.row - lastPlayerY);
            
            GridGraph.Node predictedTarget = graph.getNode(predictedY, predictedX);
            if (predictedTarget != null && !graph.isWall(predictedTarget)) {
                return astarWithTracking(graph, start, predictedTarget);
            }
            return astarWithTracking(graph, start, target);
        }
    }
}