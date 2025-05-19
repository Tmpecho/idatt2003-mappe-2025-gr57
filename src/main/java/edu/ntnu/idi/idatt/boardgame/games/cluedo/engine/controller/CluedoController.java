package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public final class CluedoController extends GameController<GridPos> {

    private final int numberOfPlayers;
    private final List<PlayerColor> playerColors = List.of(
            PlayerColor.WHITE, // Miss Scarlett
            PlayerColor.RED, // Col. Mustard
            PlayerColor.BLUE, // Mrs. Peacock
            PlayerColor.GREEN, // Rev. Green
            PlayerColor.YELLOW, // Mrs. White
            PlayerColor.PURPLE // Prof. Plum
    );

    private final Map<PlayerColor, String> playerNames = Map.of(
            PlayerColor.WHITE, "Miss Scarlett",
            PlayerColor.RED, "Col. Mustard",
            PlayerColor.BLUE, "Mrs. Peacock",
            PlayerColor.GREEN, "Rev. Green",
            PlayerColor.YELLOW, "Mrs. White",
            PlayerColor.PURPLE, "Prof. Plum");

    public CluedoController(int numberOfPlayers) {
        super(new CluedoBoard(), new Dice(2));
        if (numberOfPlayers < 2 || numberOfPlayers > 6) {
            throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
        }
        this.numberOfPlayers = numberOfPlayers;
        initialize(numberOfPlayers);
    }

    @Override
    protected Map<Integer, Player<GridPos>> createPlayers(int numPlayers) {
        Map<Integer, Player<GridPos>> playersMap = new HashMap<>();
        CluedoBoard board = (CluedoBoard) this.gameBoard;

        IntStream.range(0, numPlayers)
                .forEach(i -> {
                    int playerId = i + 1;
                    PlayerColor color = playerColors.get(i % playerColors.size());
                    String name = playerNames.getOrDefault(color, "Player " + playerId);
                    GridPos startPos = new GridPos(0, 0);
                    Player<GridPos> player = new Player<>(playerId, name, color, startPos);
                    playersMap.put(playerId, player);
                });
        return playersMap;
    }

    public Map<Integer, Player<GridPos>> getPlayers() {
        return players;
    }

    public Player<GridPos> getCurrentPlayer() {
        return currentPlayer;
    }

    // PLACEHOLDERS:
    // TODO: Implement game-specific logic for Cluedo

    @Override
    protected boolean isGameOver() {
        // Game over condition: Correct accusation made or only one player left?
        // Placeholder: Game never ends for now
        return false;
    }

    @Override
    protected void onGameFinish() {
        // Actions when the game ends (e.g., declare winner)
        notifyGameFinished(currentPlayer); // Example notification
    }

    @Override
    protected Player<GridPos> getNextPlayer() {
        // Cycle through players based on ID
        int nextPlayerId = (currentPlayer.getId() % numberOfPlayers) + 1;
        return players.get(nextPlayerId);
    }

    @Override
    public void saveGameState(String filePath) {
        // TODO: Implement Cluedo-specific game state saving
        System.out.println("Cluedo save game state not implemented yet for path: " + filePath);
        // Need a CluedoGameStateDTO, Mapper, and Repository similar to SnL
    }

    @Override
    public void loadGameState(String filePath) {
        // TODO: Implement Cluedo-specific game state loading
        System.out.println("Cluedo load game state not implemented yet from path: " + filePath);
    }

    // --- Cluedo Specific Actions (Examples - Need Implementation) ---

    public void rollDiceAndMove() {
        int roll = dice.roll();
        // TODO: Implement movement logic based on roll
        // Player chooses path, limited by roll, cannot pass through walls (except
        // doors), cannot land on occupied corridor tile.
        notifyObservers(currentPlayer.getName() + " rolled a " + roll + ". Movement logic TBD.");

        // After movement (or if movement ends turn):
        // Check for room entry -> Suggestion?
        // End turn
        currentPlayer = getNextPlayer();
        notifyObservers("It is now " + currentPlayer.getName() + "'s turn.");
    }

    public void makeSuggestion() {
        // TODO: Implement suggestion logic (only in rooms, move suspect/weapon)
        notifyObservers(currentPlayer.getName() + " suggestion logic TBD.");
    }

    public void makeAccusation() {
        // TODO: Implement accusation logic (check against solution, handle win/loss)
        notifyObservers(currentPlayer.getName() + " accusation logic TBD.");
        // if (isGameOver()) onGameFinish();
    }
}
