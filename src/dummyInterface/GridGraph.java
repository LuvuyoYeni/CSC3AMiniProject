package dummyInterface;

import java.util.*;

public class GridGraph {
    private final int rows, cols;
    private final Node[][] nodes;
    private final Map<Node, Set<Node>> adjacencyList = new HashMap<>();
    private final Set<Node> walls = new HashSet<>();

    public GridGraph(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        nodes = new Node[rows][cols];

        // Initialize all nodes
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                nodes[row][col] = new Node(row, col);
                adjacencyList.put(nodes[row][col], new HashSet<>());
            }
        }

        // Create initial edges (8-directional movement)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Node current = getNode(row, col);
                // 8-directional movement
                int[][] directions = {
                    {-1,-1}, {-1,0}, {-1,1},
                    {0,-1},          {0,1},
                    {1,-1},  {1,0},  {1,1}
                };
                
                for (int[] dir : directions) {
                    int newRow = row + dir[0];
                    int newCol = col + dir[1];
                    
                    if (isInBounds(newRow, newCol)) {
                        Node neighbor = getNode(newRow, newCol);
                        adjacencyList.get(current).add(neighbor);
                        adjacencyList.get(neighbor).add(current);
                    }
                }
            }
        }
    }

    public Node getNode(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return null;
        }
        return nodes[row][col];
    }

    public Set<Node> getNeighbors(Node node) {
        if (node == null || walls.contains(node)) {
            return new HashSet<>();
        }
        Set<Node> neighbors = new HashSet<>(adjacencyList.getOrDefault(node, new HashSet<>()));
        neighbors.removeIf(walls::contains);
        return neighbors;
    }

    public boolean isWall(Node node) {
        return walls.contains(node);
    }

    public void setWall(Node node, boolean isWall) {
        if (node == null) return;
        if (isWall) {
            walls.add(node);
        } else {
            walls.remove(node);
        }
    }

    public Set<Node> getAllNodes() {
        Set<Node> allNodes = new HashSet<>();
        for (Node[] row : nodes) {
            Collections.addAll(allNodes, row);
        }
        return allNodes;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public static class Node {
        public final int row, col;
        public int distance; // For Dijkstra's algorithm

        public Node(int row, int col) {
            this.row = row;
            this.col = col;
            this.distance = Integer.MAX_VALUE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return row == node.row && col == node.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
}