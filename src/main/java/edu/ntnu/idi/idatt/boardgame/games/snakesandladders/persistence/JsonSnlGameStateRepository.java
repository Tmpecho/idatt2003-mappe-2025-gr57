package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnlGameStateDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link GameStateRepository} implementation for Snakes and Ladders that saves and loads game
 * state to/from JSON files using Gson.
 */
public final class JsonSnlGameStateRepository implements GameStateRepository<SnlGameStateDto> {

  /**
   * Gson instance for JSON serialization and deserialization. Configured for pretty printing.
   */
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public void save(SnlGameStateDto dto, Path file) throws IOException {
    Files.writeString(file, GSON.toJson(dto));
  }

  @Override
  public SnlGameStateDto load(Path file) throws IOException {
    String json = Files.readString(file);
    return GSON.fromJson(json, SnlGameStateDto.class);
  }
}
