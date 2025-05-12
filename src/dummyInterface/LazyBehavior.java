package dummyInterface;

import java.util.ArrayList;
import java.util.List;

public class LazyBehavior implements EnemyBehavior {
    private static final int ACTIVATION_DISTANCE = 5;
    
    @Override
    public List<GridGraph.Node> calculatePath(GridGraph graph, GridGraph.Node start, GridGraph.Node target) {
        // Calculate Manhattan distance
        int distance = Math.abs(start.row - target.row) + Math.abs(start.col - target.col);
        
        if (distance <= ACTIVATION_DISTANCE) {
            return Pathfinding.bfs(graph, start, target);
        }
        
        // Stay put if player is far away
        return new ArrayList<>();
    }
}