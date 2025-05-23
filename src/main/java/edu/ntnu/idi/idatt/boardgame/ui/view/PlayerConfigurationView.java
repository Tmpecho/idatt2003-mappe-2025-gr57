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
import java.util.stream.IntStream;
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

  private static final int MIN_PLAYERS = 2;
  private static final int MAX_PLAYERS = 6;
  private final VBox root;
  private final String gameType;
  private final BiConsumer<String, List<PlayerSetupDetails>> onStartGame;
  private final Runnable onBack;
  private final VBox playerInputContainer;
  private final List<PlayerInputRow> playerInputRows = new ArrayList<>();
  private final Label errorLabel;
  private Spinner<Integer> numPlayersSpinner;

  /**
   * Constructs the PlayerConfigurationView.
   *
   * @param gameType The type of game being played (e.g., Snakes and Ladders, Cluedo).
   * @param onStartGame Callback to be executed when the game is started, passing the game type
   *     string and player setup details.
   * @param onBack Callback to be executed when the back button is pressed.
   */
  public PlayerConfigurationView(
      String gameType, BiConsumer<String, List<PlayerSetupDetails>> onStartGame, Runnable onBack) {
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
        .addAll(
            titleLabel,
            errorLabel,
            new HBox(10, new Label("Number of Players:"), numPlayersSpinner),
            scrollPane,
            actionButtons);

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
    numPlayersSpinner = new Spinner<>(MIN_PLAYERS, MAX_PLAYERS, MIN_PLAYERS);
    numPlayersSpinner.setPrefWidth(80);
    numPlayersSpinner
        .valueProperty()
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
    if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
      grid.add(new Label("Character (Auto-Assigned)"), 1, 0);
    } else {
      grid.add(new Label("Name"), 1, 0);
      grid.add(new Label("Color"), 2, 0);
    }

    IntStream.range(0, numPlayers)
        .forEach(
            i -> {
              PlayerInputRow row = new PlayerInputRow(i + 1, gameType);
              playerInputRows.add(row);
              grid.add(new Label("Player " + (i + 1)), 0, i + 1);
              if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
                grid.add(row.getChoiceNode(), 1, i + 1);
              } else {
                grid.add(row.getNameField(), 1, i + 1);
                grid.add(row.getChoiceNode(), 2, i + 1);
              }
            });
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

      Optional<PlayerColor> color;
      Optional<Suspect> suspect = Optional.empty();

      if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
        Suspect assignedSuspect = row.getAssignedSuspectIfCluedo();
        if (assignedSuspect == null) {
          showError(
              "Error assigning suspect for Player "
                  + (i + 1)
                  + ". Max players might exceed available suspects.");
          return;
        }
        if (!usedSuspects.add(assignedSuspect)) {
          showError(
              "Internal error: Suspect "
                  + assignedSuspect.getName()
                  + " assigned multiple times. This shouldn't happen.");
          return;
        }
        suspect = Optional.of(assignedSuspect);
        color = Optional.of(assignedSuspect.colour());

      } else {
        if (row.getColorChoiceBox() == null || row.getColorChoiceBox().getValue() == null) {
          showError("Player " + (i + 1) + " must select a color.");
          return;
        }
        PlayerColor selectedColor = row.getColorChoiceBox().getValue();
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
    String baseStyle =
        String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 5;", bgColor);
    String hoverStyle =
        String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 5;",
            hoverColor);
    button.setStyle(baseStyle);
    button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
    button.setOnMouseExited(e -> button.setStyle(baseStyle));
  }

  public VBox getRoot() {
    return root;
  }

  private static class PlayerInputRow {

    private final TextField nameField;
    private final ChoiceBox<PlayerColor> colorChoiceBox;
    private final Label cluedoCharacterLabel;
    private final Suspect assignedSuspectIfCluedo;
    private final String gameType;

    PlayerInputRow(int playerNumber, String gameType) {
      this.gameType = gameType;

      if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
        if (playerNumber - 1 < Suspect.values().length) {
          this.assignedSuspectIfCluedo = Suspect.values()[playerNumber - 1];
          nameField = new TextField(this.assignedSuspectIfCluedo.getName());
          nameField.setEditable(false);
          cluedoCharacterLabel =
              new Label(
                  this.assignedSuspectIfCluedo.getName()
                      + " ("
                      + this.assignedSuspectIfCluedo.colour().name()
                      + ")");
        } else {
          this.assignedSuspectIfCluedo = null;
          nameField = new TextField("N/A");
          nameField.setEditable(false);
          cluedoCharacterLabel = new Label("Error: No suspect available");
        }
        this.colorChoiceBox = null;
      } else {
        this.assignedSuspectIfCluedo = null;
        nameField = new TextField("Player " + playerNumber);
        nameField.setPromptText("Enter Name");
        colorChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(PlayerColor.values()));
        colorChoiceBox.setPrefWidth(150);
        this.cluedoCharacterLabel = null;
      }
      nameField.setPrefWidth(150);
    }

    TextField getNameField() {
      return nameField;
    }

    ChoiceBox<PlayerColor> getColorChoiceBox() {
      return colorChoiceBox;
    }

    Suspect getAssignedSuspectIfCluedo() {
      return assignedSuspectIfCluedo;
    }

    Node getChoiceNode() {
      return ChooseGameView.GAME_CLUEDO.equals(gameType) ? cluedoCharacterLabel : colorChoiceBox;
    }
  }
}
