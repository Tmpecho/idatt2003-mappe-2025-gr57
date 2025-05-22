package edu.ntnu.idi.idatt.boardgame.ui.view;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ChooseGameView {

  private final VBox root;
  public static final String GAME_SNAKES_AND_LADDERS = "SNAKES_AND_LADDERS";
  public static final String GAME_CLUEDO = "CLUEDO";

  /**
   * Constructs the view for choosing a game.
   *
   * @param onGameSelected Callback to be executed when a game is selected, passing the game type
   *                       string.
   * @param onBack         Callback to be executed when the back button is pressed.
   */
  public ChooseGameView(Consumer<String> onGameSelected, Runnable onBack) {
    root = new VBox(30);
    root.setPadding(new Insets(25));
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-background-color: #e0e0e0;");

    Label titleLabel = new Label("Choose a Game");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    titleLabel.setStyle("-fx-text-fill: #333333;");

    Button snlButton = new Button("Snakes and Ladders");
    snlButton.setOnAction(e -> onGameSelected.accept(GAME_SNAKES_AND_LADDERS));
    styleGameButton(snlButton);

    Button cluedoButton = new Button("Cluedo");
    cluedoButton.setOnAction(e -> onGameSelected.accept(GAME_CLUEDO));
    styleGameButton(cluedoButton);

    Button backButton = new Button("Back to Main Menu");
    backButton.setOnAction(e -> onBack.run());
    backButton.setPrefWidth(220);
    backButton.setFont(Font.font("Arial", 16));
    backButton.setStyle(
        "-fx-background-color: #78909c; -fx-text-fill: white; -fx-background-radius: 5;");

    VBox buttonContainer = new VBox(15, snlButton, cluedoButton, backButton);
    buttonContainer.setAlignment(Pos.CENTER);

    root.getChildren().addAll(titleLabel, buttonContainer);
  }

  private void styleGameButton(Button button) {
    button.setPrefSize(220, 60);
    button.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 18));
    button.setStyle(
        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
    button.setOnMouseEntered(e -> button.setStyle(
        "-fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 5;"));
    button.setOnMouseExited(e -> button.setStyle(
        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;"));
  }

  /**
   * Gets the root node of this view.
   *
   * @return The root VBox.
   */
  public VBox getRoot() {
    return root;
  }
}
