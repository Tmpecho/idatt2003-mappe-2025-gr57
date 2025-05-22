package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.GameObserver;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The main view for the Cluedo game. It displays the game board and control panel. Implements
 * {@link GameObserver} to react to game state changes.
 */
public final class CluedoView implements GameObserver<GridPos> {

  private final BorderPane root;
  private final CluedoBoardView boardView;
  private final VBox controlPanel;
  private final Button submitAccusationButton;
  private final Button submitSuggestionButton;
  private final Label statusLabel;
  private final Button rollDiceButton;
  private final Button suggestButton;
  private final Button accuseButton;
  private final Button endTurnButton;
  private final ChoiceBox<Suspect> suspectChoice;
  private final ChoiceBox<Weapon> weaponChoice;
  private final ChoiceBox<Room> roomChoice;
  private VBox accusationForm;
  private VBox suggestionForm;
  private final CluedoController controller;
  private final Map<Suspect, CheckBox> suspectNoteBoxes = new HashMap<>();
  private final Map<Weapon, CheckBox> weaponNoteBoxes = new HashMap<>();
  private final Map<Room, CheckBox> roomNoteBoxes = new HashMap<>();
  private VBox notesBox;

  public static final String CONTROL_PANEL_STYLE = "-fx-background-color: #f0f0f0;";
  private static final String NOTES_BOX_STYLE =
      "-fx-border-color: gray; -fx-padding: 5; -fx-background-color: #dadada;";
  private static final double SPACING = 5.0;

  /**
   * Constructs the Cluedo game view.
   *
   * @param controller The {@link CluedoController} managing the game logic.
   */
  public CluedoView(CluedoController controller) {
    this.controller = controller;
    this.root = new BorderPane();
    root.setPadding(new Insets(10));

    CluedoBoard board = (CluedoBoard) controller.getGameBoard();
    boardView =
        new CluedoBoardView(
            board, () -> controller.getCurrentPlayer().getPosition(), controller::onBoardClick);
    ScrollPane scrollPane = new ScrollPane(boardView);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    root.setCenter(scrollPane);

    // Control Panel (Right Side)
    this.controlPanel = new VBox(10);
    controlPanel.setPrefWidth(250);
    controlPanel.setPadding(new Insets(10));
    controlPanel.setStyle(CONTROL_PANEL_STYLE);

    this.statusLabel = new Label("Welcome to Cluedo!");
    statusLabel.setWrapText(true);

    suspectChoice = new ChoiceBox<>(FXCollections.observableArrayList(Suspect.values()));
    weaponChoice = new ChoiceBox<>(FXCollections.observableArrayList(Weapon.values()));
    roomChoice = new ChoiceBox<>(FXCollections.observableArrayList(Room.values()));

    this.suggestButton = new Button("Make Suggestion");
    suggestButton.setOnAction(e -> showSuggestionForm());
    suggestButton.setDisable(true); // Enable only when in a room excluding the "Cluedo" room

    this.accuseButton = new Button("Make Accusation");
    accuseButton.setOnAction(e -> showAccusationForm());
    accuseButton.setDisable(true); // Enable only when in "Cluedo room"

    endTurnButton = new Button("End Turn");
    endTurnButton.setOnAction(
        e -> {
          controller.endTurn();
          controlPanel.getChildren().remove(endTurnButton);
        });

    submitAccusationButton = new Button("Submit Accusation");
    submitAccusationButton.setOnAction(
        e -> {
          Suspect suspect = suspectChoice.getValue();
          Weapon weapon = weaponChoice.getValue();
          Room room = roomChoice.getValue();
          if (suspect != null && weapon != null && room != null) {
            controller.onAccuseButton(suspect, weapon, room);
            hideAccusationForm();
          } else {
            statusLabel.setText("Please select a suspect, weapon and room.");
          }
        });

    submitSuggestionButton = new Button("Submit Suggestion");
    submitSuggestionButton.setOnAction(
        e -> {
          Suspect suspect = suspectChoice.getValue();
          Weapon weapon = weaponChoice.getValue();
          Room room = roomChoice.getValue();

          if (suspect != null && weapon != null && room != null) {
            controller.onSuggestButton(suspect, weapon, room);

            hideSuggestionForm();
            int idx = controlPanel.getChildren().indexOf(notesBox);
            controlPanel.getChildren().add(idx, endTurnButton);

            suggestButton.setDisable(true);
            accuseButton.setDisable(true);
          } else {
            statusLabel.setText("Please select a suspect, weapon and room.");
          }
        });

    this.rollDiceButton = new Button("Roll Dice");
    rollDiceButton.setOnAction(
        e -> {
          controller.onRollButton();
          rollDiceButton.setDisable(true);
        });

    buildNotesBox();

    // push notes to the bottom
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    controlPanel
        .getChildren()
        .addAll(statusLabel, rollDiceButton, suggestButton, accuseButton, spacer, notesBox);
    root.setRight(controlPanel);

    controller.addObserver(this);
    update("Game started. It's " + controller.getCurrentPlayer().getName() + "'s turn.");
  }

  /**
   * Returns the root {@link BorderPane} of this view.
   *
   * @return The root pane.
   */
  public BorderPane getRoot() {
    return root;
  }

  @Override
  public void update(String message) {
    statusLabel.setText(message);
    boardView.highlightTile(controller.getCurrentPlayer().getPosition());

    if (!controller.canAccuse() && controlPanel.getChildren().contains(submitAccusationButton)) {
      hideAccusationForm();
    }

    rollDiceButton.setDisable(!controller.isWaitingForRoll());

    // if we’re mid-suggestion (i.e. EndTurn is visible), keep suggest/accuse off
    if (controlPanel.getChildren().contains(endTurnButton)) {
      suggestButton.setDisable(true);
      accuseButton.setDisable(true);
    } else {
      suggestButton.setDisable(!controller.canSuggest());
      accuseButton.setDisable(!controller.canAccuse());
    }

    refreshNotes();
  }

  @Override
  public void gameFinished(Player<GridPos> winner) {
    statusLabel.setText("Game Over! " + winner.getName() + " wins!");
    // Disable game action buttons
    rollDiceButton.setDisable(true);
    suggestButton.setDisable(true);
    accuseButton.setDisable(true);
  }

  private void buildNotesBox() {
    initializeNotesBox();
    addCategorySection("Suspects", Suspect.values(), this::createSuspectCheckBox);
    addCategorySection("Weapons", Weapon.values(), this::createWeaponCheckBox);
    addCategorySection("Rooms", Room.values(), this::createRoomCheckBox);
  }

  private void initializeNotesBox() {
    notesBox = new VBox(SPACING);
    notesBox.setStyle(NOTES_BOX_STYLE);
    notesBox.getChildren().add(new Label("Notes"));
  }

  private <T extends Enum<T>> void addCategorySection(
      String categoryName, T[] items, Function<T, CheckBox> checkBoxFactory) {
    notesBox.getChildren().add(new Label(categoryName + ":"));
    Arrays.stream(items)
        .map(checkBoxFactory)
        .forEach(checkBox -> notesBox.getChildren().add(checkBox));
  }

  private CheckBox createSuspectCheckBox(Suspect suspect) {
    CheckBox checkBox = createCheckBox(suspect.getName());
    suspectNoteBoxes.put(suspect, checkBox);
    bindCheckBoxToPlayer(
        checkBox,
        (newValue) ->
            ((CluedoPlayer) controller.getCurrentPlayer()).setSuspectNoted(suspect, newValue));
    return checkBox;
  }

  private CheckBox createWeaponCheckBox(Weapon weapon) {
    CheckBox checkBox = createCheckBox(weapon.getName());
    weaponNoteBoxes.put(weapon, checkBox);
    bindCheckBoxToPlayer(
        checkBox,
        (newValue) ->
            ((CluedoPlayer) controller.getCurrentPlayer()).setWeaponNoted(weapon, newValue));
    return checkBox;
  }

  private CheckBox createRoomCheckBox(Room room) {
    CheckBox checkBox = createCheckBox(room.getName());
    roomNoteBoxes.put(room, checkBox);
    bindCheckBoxToPlayer(
        checkBox,
        (newValue) -> ((CluedoPlayer) controller.getCurrentPlayer()).setRoomNoted(room, newValue));
    return checkBox;
  }

  private CheckBox createCheckBox(String name) {
    return new CheckBox(name);
  }

  private void bindCheckBoxToPlayer(CheckBox checkBox, Consumer<Boolean> updateAction) {
    checkBox
        .selectedProperty()
        .addListener((obs, oldValue, newValue) -> updateAction.accept(newValue));
  }

  private void showAccusationForm() {
    accuseButton.setDisable(true);

    accusationForm =
        new VBox(
            5,
            new Label("Suspect:"),
            suspectChoice,
            new Label("Weapon:"),
            weaponChoice,
            new Label("Room:"),
            roomChoice,
            submitAccusationButton);

    int idx = controlPanel.getChildren().indexOf(notesBox);
    controlPanel.getChildren().add(idx, accusationForm);
  }

  private void hideAccusationForm() {
    controlPanel.getChildren().remove(accusationForm);
    accuseButton.setDisable(false);
  }

  private void showSuggestionForm() {
    suggestButton.setDisable(true);

    suggestionForm =
        new VBox(
            5,
            new Label("Suspect:"),
            suspectChoice,
            new Label("Weapon:"),
            weaponChoice,
            new Label("Room:"),
            roomChoice,
            submitSuggestionButton);

    // set roomChoice to the current room and disable the roomChoicebox
    roomChoice.setValue(controller.getRoomOfCurrentPlayer());
    roomChoice.setDisable(true);

    int idx = controlPanel.getChildren().indexOf(notesBox);
    controlPanel.getChildren().add(idx, suggestionForm);
  }

  private void hideSuggestionForm() {
    controlPanel.getChildren().remove(suggestionForm);
    roomChoice.setDisable(false);
  }

  private void refreshNotes() {
    CluedoPlayer currentPlayer = (CluedoPlayer) controller.getCurrentPlayer();
    suspectNoteBoxes.forEach(
        (suspect, checkBox) -> checkBox.setSelected(currentPlayer.isSuspectNoted(suspect)));
    weaponNoteBoxes.forEach(
        (weapon, checkBox) -> checkBox.setSelected(currentPlayer.isWeaponNoted(weapon)));
    roomNoteBoxes.forEach(
        (room, checkBox) -> checkBox.setSelected(currentPlayer.isRoomNoted(room)));
  }
}
