package dummyInterface;

import java.util.List;

public interface EnemyBehavior {
    List<GridGraph.Node> calculatePath(GridGraph graph, GridGraph.Node start, GridGraph.Node target);
}