package dummyInterface;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.*;

public class ImageProcessor {
    public static GridGraph createGridFromImage(BufferedImage image, int gridSize) {
        // Scale the image to match grid size
        BufferedImage scaledImage = scaleImage(image, gridSize, gridSize);
        
        // Create an empty grid
        GridGraph grid = new GridGraph(gridSize, gridSize);
        int wallCount = 0;

        // Define critical positions that must remain open (player and enemy starts)
        int[][] criticalPositions = {
            {0, 0}, // Player start
            {gridSize - 1, gridSize - 1}, // BFSEnemy
            {0, gridSize - 1}, // DijkstraEnemy
            {gridSize / 2, gridSize / 2} // AStarEnemy
        };

        // Process each pixel to determine potential walls
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                // Skip critical positions
                boolean isCritical = false;
                for (int[] pos : criticalPositions) {
                    if (y == pos[0] && x == pos[1]) {
                        isCritical = true;
                        break;
                    }
                }
                if (isCritical) continue;

                Color pixelColor = new Color(scaledImage.getRGB(x, y));
                float brightness = (pixelColor.getRed() * 0.299f + 
                                   pixelColor.getGreen() * 0.587f + 
                                   pixelColor.getBlue() * 0.114f) / 255f;

                // If pixel is dark, try to add a wall
                if (brightness < 0.5f) {
                    GridGraph.Node node = grid.getNode(y, x);
                    // Temporarily set wall
                    grid.setWall(node, true);
                    // Check if grid remains connected
                    if (!isGridConnected(grid, criticalPositions)) {
                        grid.setWall(node, false); // Revert if it disconnects
                    } else {
                        wallCount++;
                    }
                }
            }
        }

        // Log grid details
        System.out.println("Grid created from image: " + gridSize + "x" + gridSize);
        System.out.println("Walls: " + wallCount + " (" + (wallCount * 100.0 / (gridSize * gridSize)) + "%)");
        System.out.println("Grid connectivity: " + (isGridConnected(grid, criticalPositions) ? "Valid" : "Invalid"));

        return grid;
    }

    private static boolean isGridConnected(GridGraph grid, int[][] criticalPositions) {
        // Run BFS from player start (0,0) to ensure all critical positions are reachable
        GridGraph.Node start = grid.getNode(0, 0);
        if (start == null || grid.isWall(start)) return false;

        Set<GridGraph.Node> reachable = new HashSet<>();
        Queue<GridGraph.Node> queue = new LinkedList<>();
        queue.add(start);
        reachable.add(start);

        while (!queue.isEmpty()) {
            GridGraph.Node current = queue.poll();
            for (GridGraph.Node neighbor : grid.getNeighbors(current)) {
                if (!reachable.contains(neighbor)) {
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Check if all critical positions are reachable
        for (int[] pos : criticalPositions) {
            GridGraph.Node node = grid.getNode(pos[0], pos[1]);
            if (node == null || !reachable.contains(node)) {
                return false;
            }
        }
        return true;
    }

    private static BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, original.getType());
        java.awt.Graphics2D g2d = scaled.createGraphics();
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return scaled;
    }
}