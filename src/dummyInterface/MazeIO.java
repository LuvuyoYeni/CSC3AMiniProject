package dummyInterface;

import java.io.*;
import java.util.Scanner;

public class MazeIO {
    public static void saveMaze(GridGraph graph, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int row = 0; row < graph.getRows(); row++) {
                for (int col = 0; col < graph.getCols(); col++) {
                    writer.write(graph.isWall(graph.getNode(row, col)) ? "1" : "0");
                }
                writer.write("\n");
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static GridGraph loadMaze(String filename, int rows, int cols) {
        GridGraph graph = new GridGraph(rows, cols);
        try (Scanner scanner = new Scanner(new File(filename))) {
            for (int row = 0; row < rows; row++) {
                String line = scanner.nextLine();
                for (int col = 0; col < cols; col++) {
                    if (line.charAt(col) == '1') {
                        graph.setWall(graph.getNode(row, col), true);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return graph;
    }
}