package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnakesAndLaddersBoard;

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
    players.values().forEach(player -> {
      JsonObject playerObj = new JsonObject();
      playerObj.addProperty("id", player.getId());
      playerObj.addProperty("position", player.getPosition());
      playerObj.addProperty("color", player.getColor().name());
      playersArray.add(playerObj);
    });
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
      playersArray.forEach(elem -> {
        JsonObject playerObj = elem.getAsJsonObject();
        int id = playerObj.get("id").getAsInt();
        int position = playerObj.get("position").getAsInt();
        Player player = players.get(id);
        if (player != null) {
          gameBoard.setPlayerPosition(player, position);
        }
      });
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