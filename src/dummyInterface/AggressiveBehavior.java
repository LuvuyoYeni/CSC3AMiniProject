package dummyInterface;

import java.util.List;

public class AggressiveBehavior implements EnemyBehavior {
    @Override
    public List<GridGraph.Node> calculatePath(GridGraph graph, GridGraph.Node start, GridGraph.Node target) {
        return Pathfinding.astar(graph, start, target);
    }
}