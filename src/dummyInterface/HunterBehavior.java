package dummyInterface;

import java.util.List;

public class HunterBehavior implements EnemyBehavior {
    private int lastPlayerX = -1;
    private int lastPlayerY = -1;
    private int predictedX = -1;
    private int predictedY = -1;
    
    @Override
    public List<GridGraph.Node> calculatePath(GridGraph graph, GridGraph.Node start, GridGraph.Node target) {
        // Simple prediction: assume player continues moving in same direction
        if (lastPlayerX >= 0 && lastPlayerY >= 0) {
            int dx = target.col - lastPlayerX;
            int dy = target.row - lastPlayerY;
            predictedX = target.col + dx;
            predictedY = target.row + dy;
        }
        
        lastPlayerX = target.col;
        lastPlayerY = target.row;
        
        // If prediction is valid, use it
        GridGraph.Node predictedTarget = graph.getNode(predictedY, predictedX);
        if (predictedTarget != null && !graph.isWall(predictedTarget)) {
            return Pathfinding.astar(graph, start, predictedTarget);
        }
        
        // Otherwise use current position
        return Pathfinding.astar(graph, start, target);
    }
}