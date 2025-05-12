package dummyInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.paint.Color;

public abstract class Enemy {
    protected int x, y;
    protected List<GridGraph.Node> currentPath = new ArrayList<>();
    protected EnemyBehavior behavior;
    protected Set<GridGraph.Node> lastExploredNodes = new HashSet<>();
    private String name;
    private Color color;
    private int activationRange = Integer.MAX_VALUE;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.behavior = new DefaultBehavior();
        this.name = "Enemy";
        this.color = Color.RED;
    }

    public void updatePath(GridGraph graph, GridGraph.Node target) {
        if (target == null) {
            currentPath.clear();
            lastExploredNodes.clear();
            return;
        }

        // Check if player is in activation range
        GridGraph.Node start = graph.getNode(y, x);
        if (start != null && isPlayerInRange(start, target)) {
            List<GridGraph.Node> path = behavior.calculatePath(graph, start, target);
            if (path != null) {
                this.currentPath = path;
                // No explored nodes available with this method
                this.lastExploredNodes.clear();
            } else {
                this.currentPath.clear();
                this.lastExploredNodes.clear();
            }
        } else {
            this.currentPath.clear();
            this.lastExploredNodes.clear();
        }
    }

    private boolean isPlayerInRange(GridGraph.Node start, GridGraph.Node target) {
        if (activationRange == Integer.MAX_VALUE) return true;
        
        // Calculate Manhattan distance
        int distance = Math.abs(start.row - target.row) + Math.abs(start.col - target.col);
        return distance <= activationRange;
    }

    public void move() {
        if (!currentPath.isEmpty()) {
            GridGraph.Node next = currentPath.get(0);
            if (next != null) {
                this.x = next.col;
                this.y = next.row;
                currentPath.remove(0);
            }
        }
    }

    // Behavior management
    public void setBehavior(EnemyBehavior behavior) {
        this.behavior = behavior;
        if (behavior instanceof LazyBehavior) {
            this.activationRange = 5;
        } else {
            this.activationRange = Integer.MAX_VALUE;
        }
    }

    public EnemyBehavior getBehavior() {
        return behavior;
    }

    // Visualization accessors
    public Set<GridGraph.Node> getLastExploredNodes() {
        return new HashSet<>(lastExploredNodes);
    }

    public List<GridGraph.Node> getCurrentPath() {
        return new ArrayList<>(currentPath);
    }

    // Position accessors
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Appearance customization
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    // Debug information
    public String getBehaviorName() {
        return behavior.getClass().getSimpleName().replace("Behavior", "");
    }

    @Override
    public String toString() {
        return String.format("%s [%s] at (%d,%d)", name, getBehaviorName(), x, y);
    }
}