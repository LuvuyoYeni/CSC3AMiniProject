package dummyInterface;

public class DijkstraEnemy extends Enemy {
    public DijkstraEnemy(int startX, int startY) {
        super(startX, startY);
    }

    @Override
    public void updatePath(GridGraph graph, GridGraph.Node target) {
        GridGraph.Node start = graph.getNode(y, x);
        if (start != null && target != null) {
            this.currentPath = Pathfinding.dijkstra(graph, start, target);
        } else {
            this.currentPath.clear();
        }
    }
}