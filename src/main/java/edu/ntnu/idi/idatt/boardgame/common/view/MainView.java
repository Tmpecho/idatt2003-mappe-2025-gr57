package edu.ntnu.idi.idatt.boardgame.common.view;

import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller.SnakesAndLaddersController;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.view.SnakesAndLaddersView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * The {@code MainView} class serves as the primary container for the application, featuring a
 * sidebar with game selection and placeholders for save/load functionality.
 */
public class MainView {
  private final BorderPane root;
  private final StackPane contentWrapper;

  /** Constructs a new {@code MainView} and initializes its layout. */
  public MainView() {
    root = new BorderPane();

    contentWrapper = new StackPane();
    contentWrapper.setPadding(new Insets(10));
    root.setCenter(contentWrapper);

    loadMenu();
  }

  /**
   * Returns the root layout node for this view.
   *
   * @return the root {@link BorderPane} for the main layout
   */
  public BorderPane getRoot() {
    return root;
  }

  /**
   * Creates a sidebar with buttons for selecting games, saving/loading, and exiting the
   * application.
   */
  private void loadMenu() {
    VBox sidebar = new VBox(15);
    sidebar.setPadding(new Insets(10));
    sidebar.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    sidebar.setStyle("-fx-background-color: #336699;");

    Label titleLabel = new Label("Board games");
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

    VBox games = new VBox(10);
    addGames(games);

    Region spacer = new Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

    Button saveGameButton = new Button("Save game");
    saveGameButton.setOnAction(e -> System.out.println("Save game not implemented yet."));

    Button loadGameButton = new Button("Load game");
    loadGameButton.setOnAction(e -> System.out.println("Load game not implemented yet."));

    Button exitButton = new Button("Exit");
    exitButton.setOnAction(e -> Platform.exit());

    sidebar
        .getChildren()
        .addAll(
            titleLabel,
            games,
            spacer,
            saveGameButton,
            loadGameButton,
            exitButton);
    root.setLeft(sidebar);
  }

  private void addGames(VBox games) {
    Button snakesAndLaddersButton = new Button("Snakes and Ladders");
    snakesAndLaddersButton.setOnAction(e -> loadSnakesAndLadders());

    Button cluedoButton = new Button("Cluedo");
    cluedoButton.setOnAction(e -> System.out.println("Cluedo not implemented yet."));

    games.getChildren().addAll(snakesAndLaddersButton, cluedoButton);
  }

  /** Loads the Snakes and Ladders game and sets its view in the center of the root layout. */
  private void loadSnakesAndLadders() {
    SnakesAndLaddersController controller = new SnakesAndLaddersController(2);
    SnakesAndLaddersView view = new SnakesAndLaddersView(controller);
    contentWrapper.getChildren().setAll(view.getRoot());
  }
}
