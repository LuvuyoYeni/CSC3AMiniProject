package dummyInterface;

import java.util.*;

public class GameLogic {
    public enum Difficulty {
        EASY,  // Only BFS
        MEDIUM, // BFS + Dijkstra
        HARD   // All 3 algorithms
    }

    public enum GameMode {
        CHASE,      // Default (lose if caught)
        ESCAPE,     // Reach exit node
        TIME_TRIAL  // Survive for X seconds
    }

    private GridGraph graph;
    private int playerX, playerY;
    private boolean gameOver = false;
    private List<Enemy> enemies = new ArrayList<>();
    private Difficulty difficulty = Difficulty.MEDIUM;
    private GameMode mode = GameMode.CHASE;
    private GridGraph.Node exitNode;
    private long gameStartTime;
    private int timeTrialDuration = 120; // 2 minutes in seconds

    public GameLogic(int rows, int cols) {
        this.graph = new GridGraph(rows, cols);
        this.playerX = 0;
        this.playerY = 0;
        initializeEnemies();
        this.gameStartTime = System.currentTimeMillis();
    }

    private void initializeEnemies() {
        enemies.clear();
        int[][] preferredPositions = {
            {graph.getCols() - 1, graph.getRows() - 1}, // Bottom-right
            {0, graph.getRows() - 1},                  // Bottom-left
            {graph.getCols() / 2, graph.getRows() / 2} // Center
        };

        switch (difficulty) {
            case EASY:
                enemies.add(createEnemy(BFSEnemy.class, preferredPositions[0]));
                break;
            case MEDIUM:
                enemies.add(createEnemy(BFSEnemy.class, preferredPositions[0]));
                enemies.add(createEnemy(DijkstraEnemy.class, preferredPositions[1]));
                break;
            case HARD:
                enemies.add(createEnemy(BFSEnemy.class, preferredPositions[0]));
                enemies.add(createEnemy(DijkstraEnemy.class, preferredPositions[1]));
                enemies.add(createEnemy(AStarEnemy.class, preferredPositions[2]));
                break;
        }
    }

    private Enemy createEnemy(Class<? extends Enemy> enemyClass, int[] position) {
        int x = position[0];
        int y = position[1];
        GridGraph.Node node = graph.getNode(y, x);
        if (node != null && !graph.isWall(node)) {
            try {
                return enemyClass.getConstructor(int.class, int.class).newInstance(x, y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Find a valid non-wall position
        for (int row = 0; row < graph.getRows(); row++) {
            for (int col = 0; col < graph.getCols(); col++) {
                node = graph.getNode(row, col);
                if (node != null && !graph.isWall(node)) {
                    try {
                        return enemyClass.getConstructor(int.class, int.class).newInstance(col, row);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Fallback: Force a position to be open
        graph.setWall(graph.getNode(0, 0), false);
        try {
            return enemyClass.getConstructor(int.class, int.class).newInstance(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public GridGraph getGraph() {
        return graph;
    }

    public void setGraph(GridGraph graph) {
        this.graph = graph;
        this.playerX = 0;
        this.playerY = 0;
        if (graph.isWall(graph.getNode(playerY, playerX))) {
            findValidStartPosition();
        }
        initializeEnemies();
        // Validate grid connectivity
        if (!isGridValid()) {
            System.out.println("Warning: Custom grid is invalid, resetting to default");
            this.graph = new GridGraph(graph.getRows(), graph.getCols());
            this.playerX = 0;
            this.playerY = 0;
            initializeEnemies();
        }
    }

    private boolean isGridValid() {
        GridGraph.Node playerNode = graph.getNode(playerY, playerX);
        if (playerNode == null || graph.isWall(playerNode)) {
            return false;
        }

        // Run BFS from player to check if enemies are reachable
        Set<GridGraph.Node> reachable = new HashSet<>();
        Queue<GridGraph.Node> queue = new LinkedList<>();
        queue.add(playerNode);
        reachable.add(playerNode);

        while (!queue.isEmpty()) {
            GridGraph.Node current = queue.poll();
            for (GridGraph.Node neighbor : graph.getNeighbors(current)) {
                if (!reachable.contains(neighbor)) {
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Check if all enemies are in reachable nodes
        for (Enemy enemy : enemies) {
            GridGraph.Node enemyNode = graph.getNode(enemy.getY(), enemy.getX());
            if (!reachable.contains(enemyNode)) {
                return false;
            }
        }
        return true;
    }

    private void findValidStartPosition() {
        for (int row = 0; row < graph.getRows(); row++) {
            for (int col = 0; col < graph.getCols(); col++) {
                if (!graph.isWall(graph.getNode(row, col))) {
                    playerX = col;
                    playerY = row;
                    return;
                }
            }
        }
        playerX = 0;
        playerY = 0;
        graph.setWall(graph.getNode(0, 0), false);
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public List<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        resetGame();
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
        if (mode == GameMode.ESCAPE) {
            exitNode = graph.getNode(graph.getRows() - 1, graph.getCols() - 1);
        }
        resetGame();
    }

    public void movePlayer(int row, int col) {
        if (gameOver) {
            System.out.println("Game over - cannot move");
            return;
        }

        // Bounds checking
        if (row < 0 || row >= graph.getRows() || col < 0 || col >= graph.getCols()) {
            System.out.println("Out of bounds");
            return;
        }

        GridGraph.Node target = graph.getNode(row, col);
        if (target == null) {
            System.out.println("Invalid target node");
            return;
        }

        if (graph.isWall(target)) {
            System.out.println("Target is a wall");
            return;
        }

        playerX = col;
        playerY = row;
        
        checkGameConditions();
    }

    public void toggleWall(int row, int col) {
        GridGraph.Node node = graph.getNode(row, col);
        if (node == null) return;

        // Don't allow walling in player or enemies
        if ((playerX == col && playerY == row) || 
            enemies.stream().anyMatch(e -> e.getX() == col && e.getY() == row)) {
            return;
        }

        graph.setWall(node, !graph.isWall(node));
    }

    public void resetGame() {
        this.graph = new GridGraph(graph.getRows(), graph.getCols());
        this.playerX = 0;
        this.playerY = 0;
        this.gameOver = false;
        this.enemies.clear();
        initializeEnemies();
        this.gameStartTime = System.currentTimeMillis();
        
        if (mode == GameMode.ESCAPE) {
            exitNode = graph.getNode(graph.getRows() - 1, graph.getCols() - 1);
        }
    }

    public void moveEnemies() {
        if (gameOver) return;

        GridGraph.Node playerNode = graph.getNode(playerY, playerX);
        for (Enemy enemy : enemies) {
            enemy.updatePath(graph, playerNode);
            enemy.move();
        }
        
        checkGameConditions();
    }

    private void checkGameConditions() {
        // Check for collisions
        for (Enemy enemy : enemies) {
            if (enemy.getX() == playerX && enemy.getY() == playerY) {
                gameOver = true;
                return;
            }
        }

        // Check win conditions based on game mode
        switch (mode) {
            case ESCAPE:
                if (playerX == exitNode.col && playerY == exitNode.row) {
                    gameOver = true;
                }
                break;
            case TIME_TRIAL:
                long elapsedSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
                if (elapsedSeconds >= timeTrialDuration) {
                    gameOver = true;
                }
                break;
            case CHASE:
            default:
                // No win condition, only lose by being caught
                break;
        }
    }

    public int getRemainingTime() {
        if (mode != GameMode.TIME_TRIAL) return 0;
        long elapsedSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
        return Math.max(0, timeTrialDuration - (int)elapsedSeconds);
    }

    public void setTimeTrialDuration(int seconds) {
        this.timeTrialDuration = seconds;
    }
}