package edu.ntnu.idi.idatt.boardgame.ui.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * View for configuring players in a game. This class provides a user interface for setting up
 * player names, colors, and other game-specific details.
 */
public class PlayerConfigurationView {

  private final VBox root;
  private final String gameType;
  private final BiConsumer<String, List<PlayerSetupDetails>> onStartGame;
  private final Runnable onBack;

  private Spinner<Integer> numPlayersSpinner;
  private VBox playerInputContainer;
  private final List<PlayerInputRow> playerInputRows = new ArrayList<>();
  private Label errorLabel;

  private static final int MIN_PLAYERS_SNL = 2;
  private static final int MAX_PLAYERS_SNL = 6; // Max 6 due to PlayerColor enum size
  private static final int MIN_PLAYERS_CLUEDO = 2;
  private static final int MAX_PLAYERS_CLUEDO = 6;

  /**
   * Constructs the PlayerConfigurationView.
   *
   * @param gameType    The type of game being played (e.g., Snakes and Ladders, Cluedo).
   * @param onStartGame Callback to be executed when the game is started, passing the game type
   *                    string and player setup details.
   * @param onBack      Callback to be executed when the back button is pressed.
   */
  public PlayerConfigurationView(String gameType,
      BiConsumer<String, List<PlayerSetupDetails>> onStartGame, Runnable onBack) {
    this.gameType = gameType;
    this.onStartGame = onStartGame;
    this.onBack = onBack;

    root = new VBox(20);
    root.setPadding(new Insets(25));
    root.setAlignment(Pos.TOP_CENTER);
    root.setStyle("-fx-background-color: #e8f5e9;");

    Label titleLabel = new Label("Configure Players for " + getGameDisplayName(gameType));
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    titleLabel.setStyle("-fx-text-fill: #2e7d32;");

    errorLabel = new Label();
    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    errorLabel.setVisible(false);

    setupNumPlayersSpinner();

    playerInputContainer = new VBox(10);
    playerInputContainer.setAlignment(Pos.TOP_LEFT);
    ScrollPane scrollPane = new ScrollPane(playerInputContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(300);

    Button startGameButton = new Button("Start Game");
    startGameButton.setOnAction(e -> tryStartGame());
    styleButton(startGameButton, "#388e3c", "#2e7d32");

    Button backButton = new Button("Back to Game Selection");
    backButton.setOnAction(e -> this.onBack.run());
    styleButton(backButton, "#546e7a", "#37474f");

    HBox actionButtons = new HBox(20, backButton, startGameButton);
    actionButtons.setAlignment(Pos.CENTER);

    root.getChildren()
        .addAll(titleLabel, errorLabel,
            new HBox(10, new Label("Number of Players:"), numPlayersSpinner),
            scrollPane, actionButtons);

    updatePlayerInputRows(numPlayersSpinner.getValue());
  }

  private String getGameDisplayName(String type) {
    if (ChooseGameView.GAME_SNAKES_AND_LADDERS.equals(type)) {
      return "Snakes and Ladders";
    } else if (ChooseGameView.GAME_CLUEDO.equals(type)) {
      return "Cluedo";
    }
    return "Unknown Game";
  }

  private void setupNumPlayersSpinner() {
    int min = MIN_PLAYERS_SNL;
    int max = MAX_PLAYERS_SNL;
    int initial = MIN_PLAYERS_SNL;

    if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
      min = MIN_PLAYERS_CLUEDO;
      max = MAX_PLAYERS_CLUEDO;
      initial = MIN_PLAYERS_CLUEDO;
    }

    numPlayersSpinner = new Spinner<>(min, max, initial);
    numPlayersSpinner.setPrefWidth(80);
    numPlayersSpinner.valueProperty()
        .addListener((obs, oldValue, newValue) -> updatePlayerInputRows(newValue));
  }

  private void updatePlayerInputRows(int numPlayers) {
    playerInputRows.clear();
    playerInputContainer.getChildren().clear();

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10));

    grid.add(new Label("Player"), 0, 0);
    grid.add(new Label("Name"), 1, 0);
    grid.add(new Label(ChooseGameView.GAME_CLUEDO.equals(gameType) ? "Suspect" : "Color"), 2, 0);

    for (int i = 0; i < numPlayers; i++) {
      PlayerInputRow row = new PlayerInputRow(i + 1, gameType);
      playerInputRows.add(row);

      grid.add(new Label("Player " + (i + 1)), 0, i + 1);
      grid.add(row.getNameField(), 1, i + 1);
      grid.add(row.getChoiceNode(), 2, i + 1);
    }
    playerInputContainer.getChildren().add(grid);
  }

  private void tryStartGame() {
    errorLabel.setVisible(false);
    List<PlayerSetupDetails> detailsList = new ArrayList<>();
    Set<PlayerColor> usedColors = new HashSet<>();
    Set<Suspect> usedSuspects = new HashSet<>();
    Set<String> usedNames = new HashSet<>();

    for (int i = 0; i < playerInputRows.size(); i++) {
      PlayerInputRow row = playerInputRows.get(i);
      String name = row.getNameField().getText().trim();
      if (name.isEmpty()) {
        showError("Player " + (i + 1) + " name cannot be empty.");
        return;
      }
      if (!usedNames.add(name.toLowerCase())) {
        showError(
            "Player names must be unique (case-insensitive). Name '" + name + "' is duplicated.");
        return;
      }

      Optional<PlayerColor> color = Optional.empty();
      Optional<Suspect> suspect = Optional.empty();

      if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
        Suspect selectedSuspect = row.getSuspectChoiceBox().getValue();
        if (selectedSuspect == null) {
          showError("Player " + (i + 1) + " must select a suspect.");
          return;
        }
        if (!usedSuspects.add(selectedSuspect)) {
          showError("Suspect " + selectedSuspect.getName() + " is already chosen. Pick another.");
          return;
        }
        suspect = Optional.of(selectedSuspect);
        color = Optional.of(selectedSuspect.colour());

      } else {
        PlayerColor selectedColor = row.getColorChoiceBox().getValue();
        if (selectedColor == null) {
          showError("Player " + (i + 1) + " must select a color.");
          return;
        }
        if (!usedColors.add(selectedColor)) {
          showError("Color " + selectedColor.name() + " is already chosen. Pick another.");
          return;
        }
        color = Optional.of(selectedColor);
      }
      detailsList.add(new PlayerSetupDetails(name, color, suspect));
    }

    onStartGame.accept(gameType, detailsList);
  }

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
    LoggingNotification.warn("Player Configuration Error", message);
  }


  private void styleButton(Button button, String bgColor, String hoverColor) {
    button.setPrefHeight(40);
    button.setPrefWidth(180);
    button.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
    String baseStyle = String.format(
        "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 5;", bgColor);
    String hoverStyle = String.format(
        "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 5;", hoverColor);
    button.setStyle(baseStyle);
    button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
    button.setOnMouseExited(e -> button.setStyle(baseStyle));
  }

  public VBox getRoot() {
    return root;
  }

  private static class PlayerInputRow {

    private final TextField nameField;
    private ChoiceBox<PlayerColor> colorChoiceBox;
    private ChoiceBox<Suspect> suspectChoiceBox;
    private final String gameType;

    PlayerInputRow(int playerNumber, String gameType) {
      this.gameType = gameType;
      nameField = new TextField("Player " + playerNumber);
      nameField.setPromptText("Enter Name");
      nameField.setPrefWidth(150);

      if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
        suspectChoiceBox = new ChoiceBox<>(
            FXCollections.observableArrayList(Suspect.values()));
        suspectChoiceBox.setPrefWidth(150);
      } else {
        colorChoiceBox = new ChoiceBox<>(
            FXCollections.observableArrayList(PlayerColor.values()));
        colorChoiceBox.setPrefWidth(150);
      }
    }

    TextField getNameField() {
      return nameField;
    }

    ChoiceBox<PlayerColor> getColorChoiceBox() {
      return colorChoiceBox;
    }

    ChoiceBox<Suspect> getSuspectChoiceBox() {
      return suspectChoiceBox;
    }

    Node getChoiceNode() {
      return ChooseGameView.GAME_CLUEDO.equals(gameType) ? suspectChoiceBox : colorChoiceBox;
    }
  }
}