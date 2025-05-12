package dummyInterface;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class StartMenu {
    private static final int DEFAULT_GRID_SIZE = 15;

    public Scene createMenu(Stage stage, Runnable defaultGameStarter, Consumer<GridGraph> customGridStarter) {
        Label title = new Label("Graph Chase");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        TextArea howToPlay = new TextArea(
                """
                📌 How to Play:
                - Left click to move your player (blue).
                - Right click to toggle walls and block enemies.
                - Avoid being caught by the enemies (red/green).
                - Press 'Reset' to start over if caught.
                
                🖼️ Create Levels from Images:
                - Upload a black & white image to generate a level
                - Dark areas become walls
                - Light areas become paths
                """
        );
        howToPlay.setEditable(false);
        howToPlay.setWrapText(true);
        howToPlay.setStyle("-fx-font-size: 14px;");

        Button startButton = new Button("Start Default Game");
        startButton.setOnAction(e -> defaultGameStarter.run());

        Button uploadButton = new Button("Upload Image for Level");
        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Level Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try {
                    BufferedImage bufferedImage = ImageIO.read(selectedFile);
                    if (bufferedImage != null) {
                        System.out.println("Image loaded successfully: " + 
                                          selectedFile.getName() + " (" + 
                                          bufferedImage.getWidth() + "x" + 
                                          bufferedImage.getHeight() + ")");
                        
                        GridGraph customGrid = ImageProcessor.createGridFromImage(
                            bufferedImage, DEFAULT_GRID_SIZE);
                        
                        if (customGrid != null) {
                            // Close the start menu window
                            stage.close();
                            // Launch game with the custom grid
                            customGridStarter.accept(customGrid);
                        } else {
                            showAlert("Error", "Failed to create grid from image");
                        }
                    } else {
                        showAlert("Error", "Could not read image file");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Could not load image: " + ex.getMessage());
                }
            }
        });

        VBox layout = new VBox(20, title, howToPlay, startButton, uploadButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        return new Scene(layout, 600, 400);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}