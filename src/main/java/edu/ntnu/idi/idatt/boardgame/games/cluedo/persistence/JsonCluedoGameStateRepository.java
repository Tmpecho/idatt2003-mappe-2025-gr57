package edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.dto.CluedoGameStateDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Concrete implementation of {@link GameStateRepository} for handling the persistence of {@link
 * CluedoGameStateDto} objects in JSON format. This class provides functionality to save and load
 * game states to and from files using the Gson library for JSON serialization and deserialization.
 *
 * <p>This repository is specifically designed to work with the game state structure defined in
 * {@link CluedoGameStateDto}.
 */
public class JsonCluedoGameStateRepository implements GameStateRepository<CluedoGameStateDto> {

  /** Gson instance for JSON serialization and deserialization. Configured for pretty printing. */
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public void save(CluedoGameStateDto dto, Path file) throws IOException {
    Files.writeString(file, GSON.toJson(dto));
  }

  @Override
  public CluedoGameStateDto load(Path file) throws IOException {
    String json = Files.readString(file);
    return GSON.fromJson(json, CluedoGameStateDto.class);
  }
}
