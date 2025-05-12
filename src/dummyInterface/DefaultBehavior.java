package dummyInterface;

import java.util.List;

public class DefaultBehavior implements EnemyBehavior {
    @Override
    public List<GridGraph.Node> calculatePath(GridGraph graph, GridGraph.Node start, GridGraph.Node target) {
        return Pathfinding.bfs(graph, start, target);
    }
}