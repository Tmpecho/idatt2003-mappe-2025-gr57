package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller;

import edu.ntnu.idi.idatt.boardgame.common.action.Action;
import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.common.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.common.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board.SnakesAndLaddersBoard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SnakesAndLaddersController extends GameController {
  private final int numberOfPlayers;
  private final List<PlayerColor> playerColors = List.of(
          PlayerColor.RED,
          PlayerColor.BLUE,
          PlayerColor.GREEN,
          PlayerColor.YELLOW,
          PlayerColor.ORANGE,
          PlayerColor.PURPLE
  );

  public SnakesAndLaddersController(int numberOfPlayers) {
    super(new SnakesAndLaddersBoard(), new Dice(2));
    this.numberOfPlayers = numberOfPlayers;
    initialize(numberOfPlayers);
  }

  @Override
  protected Map<Integer, Player> createPlayers(int numberOfPlayers) {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers).forEach(playerId -> {
      PlayerColor color = playerColors.get((playerId - 1) % playerColors.size());
      Player player = new Player(playerId, "Player " + playerId, color);
      players.put(playerId, player);
    });
    return players;
  }

  public void rollDice() {
    Action roll = new RollAction(gameBoard, currentPlayer, dice);
    roll.execute();
    notifyObservers(currentPlayer.getName() + " is now at tile " + currentPlayer.getPosition());
    if (isGameOver()) {
      onGameFinish();
    } else {
      currentPlayer = getNextPlayer();
    }
  }

  @Override
  protected boolean isGameOver() {
    return currentPlayer.getPosition() == gameBoard.getBoardSize();
  }

  @Override
  protected void onGameFinish() {
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  @Override
  public void saveGameState(String filePath) {
    JsonObject gameState = new JsonObject();
    gameState.addProperty("currentTurn", currentPlayer.getId());

    JsonArray playersArray = new JsonArray();
    for (Player player : players.values()) {
      JsonObject playerObj = new JsonObject();
      playerObj.addProperty("id", player.getId());
      playerObj.addProperty("position", player.getPosition());
      // playerObj.addProperty("name", player.getName()); \\ name
      // playerObj.addProperty("color", player.getColor().name()); \\ color 
      playersArray.add(playerObj);
    }
    gameState.add("players", playersArray);

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String jsonOutput = gson.toJson(gameState);

    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(jsonOutput);
    } catch (IOException e) {
      System.err.println("Error writing game state to file: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void loadGameState(String filePath) {
    try (FileReader reader = new FileReader(filePath)) {
      JsonObject gameState = JsonParser.parseReader(reader).getAsJsonObject();
      int currentTurnId = gameState.get("currentTurn").getAsInt();
      currentPlayer = players.get(currentTurnId);

      JsonArray playersArray = gameState.get("players").getAsJsonArray();
      for (JsonElement elem : playersArray) {
        JsonObject playerObj = elem.getAsJsonObject();
        int id = playerObj.get("id").getAsInt();
        int position = playerObj.get("position").getAsInt();
        // String name = playerObj.get("name").getAsString(); // name
        // PlayerColor color = PlayerColor.valueOf(playerObj.get("color").getAsString()); // color 
        Player player = players.get(id);
        if (player != null) {
          gameBoard.setPlayerPosition(player, position);
          // player.setName(name); \\ update color
          // player.setColor(color); \\ update name
        }
      }
      notifyObservers("Game state loaded. Current turn: " + currentPlayer.getName());
    } catch (IOException e) {
      System.err.println("Error reading game state from file: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
        System.err.println("Error parsing game state file: " + e.getMessage());
        e.printStackTrace();
    }
  }
}