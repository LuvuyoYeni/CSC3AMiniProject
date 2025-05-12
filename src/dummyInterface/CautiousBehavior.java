package dummyInterface;

import java.util.List;

public class CautiousBehavior implements EnemyBehavior {
    @Override
    public List<GridGraph.Node> calculatePath(GridGraph graph, GridGraph.Node start, GridGraph.Node target) {
        // Modify the graph temporarily to avoid walls
        GridGraph modifiedGraph = new GridGraph(graph.getRows(), graph.getCols());
        
        // Copy walls but add penalty areas around them
        for (GridGraph.Node node : graph.getAllNodes()) {
            if (graph.isWall(node)) {
                modifiedGraph.setWall(modifiedGraph.getNode(node.row, node.col), true);
                // Mark adjacent nodes as dangerous
                for (GridGraph.Node neighbor : graph.getNeighbors(node)) {
                    modifiedGraph.getNode(neighbor.row, neighbor.col).distance += 5; // Penalty
                }
            }
        }
        
        return Pathfinding.astar(modifiedGraph, start, target);
    }
}