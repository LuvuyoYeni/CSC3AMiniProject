package dummyInterface;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GraphChaseUI extends Application {
    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private GameLogic gameLogic;
    private boolean gameOver = false;
    private boolean showPaths = false;
    private boolean showExplored = false;
    private boolean showBehaviorInfo = true;
    private Timeline gameLoop;

    @Override
    public void start(Stage primaryStage) {
        StartMenu menu = new StartMenu();
        Scene menuScene = menu.createMenu(primaryStage, 
                () -> launchGame(primaryStage, null), // Default game
                this::launchGameWithCustomGrid // Custom grid game
            );
            primaryStage.setTitle("Graph Chase");
            primaryStage.setScene(menuScene);
            primaryStage.show();
    }
    
    //method to handle custom grid launches
    private void launchGameWithCustomGrid(GridGraph customGrid) {
        if (customGrid == null) {
            System.err.println("Error: Attempted to launch game with null custom grid");
            return;
        }
        
        System.out.println("Launching game with custom grid: " + 
                         customGrid.getRows() + "x" + customGrid.getCols());
        
        Stage gameStage = new Stage();
        gameStage.setTitle("Graph Chase - Custom Level");
        launchGame(gameStage, customGrid);
        gameStage.show();
    }

    private void launchGame(Stage primaryStage, GridGraph customGrid) {
        gameLogic = new GameLogic(GRID_SIZE, GRID_SIZE);
        
        // If custom grid provided, use it
        if (customGrid != null) {
            System.out.println("Setting custom grid in game logic");
            gameLogic.setGraph(customGrid);
        }
        
        gameOver = false;
        showPaths = false;
        showExplored = false;

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawGrid(gc);

        // Game loop - updates every 500ms
        gameLoop = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (!gameOver) {
                gameLogic.moveEnemies();
                if (gameLogic.isGameOver()) {
                    gameOver = true;
                    showGameOverDialog();
                }
                drawGrid(gc);
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();

        // Mouse controls
        canvas.setOnMouseClicked(e -> {
            if (gameOver) return;

            int col = (int) (e.getX() / CELL_SIZE);
            int row = (int) (e.getY() / CELL_SIZE);

            if (e.getButton() == MouseButton.PRIMARY) {
                gameLogic.movePlayer(row, col);
                drawGrid(gc);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                gameLogic.toggleWall(row, col);
                drawGrid(gc);
            }
        });

        // Game controls
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            gameLogic.resetGame();
            gameOver = false;
            drawGrid(gc);
            gameLoop.play();
        });
        
        // Level info label
        Label levelInfo = new Label(customGrid != null ? "Custom Level" : "Default Level");

        // Visualization toggles
        CheckBox pathToggle = new CheckBox("Show Paths");
        pathToggle.setOnAction(e -> {
            showPaths = pathToggle.isSelected();
            drawGrid(gc);
        });

        CheckBox exploredToggle = new CheckBox("Show Explored");
        exploredToggle.setOnAction(e -> {
            showExplored = exploredToggle.isSelected();
            drawGrid(gc);
        });

        CheckBox behaviorToggle = new CheckBox("Show Behaviors");
        behaviorToggle.setSelected(true);
        behaviorToggle.setOnAction(e -> {
            showBehaviorInfo = behaviorToggle.isSelected();
            drawGrid(gc);
        });

        // Difficulty selection
        ComboBox<GameLogic.Difficulty> difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll(GameLogic.Difficulty.values());
        difficultyBox.setValue(GameLogic.Difficulty.MEDIUM);
        difficultyBox.setOnAction(e -> {
            gameLogic.setDifficulty(difficultyBox.getValue());
            gameOver = false;
            drawGrid(gc);
        });

        // Game mode selection
        ComboBox<GameLogic.GameMode> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(GameLogic.GameMode.values());
        modeBox.setValue(GameLogic.GameMode.CHASE);
        modeBox.setOnAction(e -> {
            gameLogic.setMode(modeBox.getValue());
            gameOver = false;
            drawGrid(gc);
        });

        // Behavior selection
        ComboBox<String> behaviorBox = new ComboBox<>();
        behaviorBox.getItems().addAll("Default", "Aggressive", "Cautious", "Hunter", "Lazy");
        behaviorBox.setValue("Default");
        behaviorBox.setOnAction(e -> {
            String selected = behaviorBox.getValue();
            gameLogic.getEnemies().forEach(enemy -> {
                switch (selected) {
                    case "Aggressive": enemy.setBehavior(new AggressiveBehavior()); break;
                    case "Cautious": enemy.setBehavior(new CautiousBehavior()); break;
                    case "Hunter": enemy.setBehavior(new HunterBehavior()); break;
                    case "Lazy": enemy.setBehavior(new LazyBehavior()); break;
                    default: enemy.setBehavior(new DefaultBehavior()); break;
                }
            });
            drawGrid(gc);
        });

        // Timer display (for TIME_TRIAL mode)
        Label timerLabel = new Label();
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (gameLogic.getMode() == GameLogic.GameMode.TIME_TRIAL && !gameOver) {
                int remaining = gameLogic.getRemainingTime();
                timerLabel.setText(String.format("Time: %02d:%02d", remaining / 60, remaining % 60));
                if (remaining <= 0) {
                    gameOver = true;
                    showGameOverDialog(true);
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        // Control panel layout
        HBox topControls = new HBox(10,
            new VBox(5, new Label("Difficulty:"), difficultyBox),
            new VBox(5, new Label("Mode:"), modeBox),
            new VBox(5, new Label("Behavior:"), behaviorBox),
            new VBox(5, levelInfo)
        );

        HBox bottomControls = new HBox(10,
            resetButton, pathToggle, exploredToggle, behaviorToggle, timerLabel
        );

        VBox controls = new VBox(10, topControls, bottomControls);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        root.setCenter(canvas);
        root.setBottom(controls);

        Scene gameScene = new Scene(root);
        primaryStage.setScene(gameScene);
        primaryStage.setMinWidth(GRID_SIZE * CELL_SIZE + 40);
        primaryStage.setMinHeight(GRID_SIZE * CELL_SIZE + 150);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.clearRect(0, 0, GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        GridGraph graph = gameLogic.getGraph();

        // Draw grid cells
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                double x = col * CELL_SIZE;
                double y = row * CELL_SIZE;

                gc.setFill(Color.WHITE);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw walls
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                GridGraph.Node node = graph.getNode(row, col);
                if (node != null && graph.isWall(node)) {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Draw exit (for ESCAPE mode)
        if (gameLogic.getMode() == GameLogic.GameMode.ESCAPE) {
            GridGraph.Node exitNode = graph.getNode(GRID_SIZE - 1, GRID_SIZE - 1);
            gc.setFill(Color.GOLD);
            gc.fillRect(
                exitNode.col * CELL_SIZE + 5,
                exitNode.row * CELL_SIZE + 5,
                CELL_SIZE - 10,
                CELL_SIZE - 10
            );
        }

        // Draw player
        gc.setFill(Color.BLUE);
        gc.fillOval(
            gameLogic.getPlayerX() * CELL_SIZE + 5,
            gameLogic.getPlayerY() * CELL_SIZE + 5,
            CELL_SIZE - 10,
            CELL_SIZE - 10
        );

        // Draw enemies, paths, and explored nodes
        for (Enemy enemy : gameLogic.getEnemies()) {
            // Set color based on behavior
            if (enemy.getBehavior() instanceof AggressiveBehavior) {
                gc.setFill(Color.RED);
            } else if (enemy.getBehavior() instanceof CautiousBehavior) {
                gc.setFill(Color.GREEN);
            } else if (enemy.getBehavior() instanceof HunterBehavior) {
                gc.setFill(Color.PURPLE);
            } else if (enemy.getBehavior() instanceof LazyBehavior) {
                gc.setFill(Color.ORANGE);
            } else {
                gc.setFill(Color.DARKGRAY); // Default
            }

            // Draw enemy
            gc.fillOval(
                enemy.getX() * CELL_SIZE + 5,
                enemy.getY() * CELL_SIZE + 5,
                CELL_SIZE - 10,
                CELL_SIZE - 10
            );

            // Draw behavior info
            if (showBehaviorInfo) {
                gc.setFill(Color.BLACK);
                gc.fillText(enemy.getBehaviorName(), 
                    enemy.getX() * CELL_SIZE + 5,
                    enemy.getY() * CELL_SIZE + 15);
            }

            // Draw explored nodes if enabled
            if (showExplored) {
                gc.setFill(Color.color(0.8, 0.8, 0.8, 0.3));
                for (GridGraph.Node node : enemy.getLastExploredNodes()) {
                    gc.fillRect(
                        node.col * CELL_SIZE + 2,
                        node.row * CELL_SIZE + 2,
                        CELL_SIZE - 4,
                        CELL_SIZE - 4
                    );
                }
            }

            // Draw path if enabled
            if (showPaths) {
                gc.setFill(Color.color(0, 0, 0, 0.2));
                for (GridGraph.Node node : enemy.getCurrentPath()) {
                    gc.fillRect(
                        node.col * CELL_SIZE + CELL_SIZE / 4.0,
                        node.row * CELL_SIZE + CELL_SIZE / 4.0,
                        CELL_SIZE / 2.0,
                        CELL_SIZE / 2.0
                    );
                }
            }
        }
    }

    private void showGameOverDialog() {
        showGameOverDialog(false);
    }

    private void showGameOverDialog(boolean won) {
        gameLoop.stop();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        
        if (won) {
            alert.setHeaderText("You Won!");
            alert.setContentText("You survived the time trial!");
        } else if (gameLogic.getMode() == GameLogic.GameMode.ESCAPE) {
            alert.setHeaderText("You Escaped!");
            alert.setContentText("You reached the exit safely!");
        } else {
            alert.setHeaderText("Game Over");
            alert.setContentText("The enemy caught the player!");
        }
        
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}