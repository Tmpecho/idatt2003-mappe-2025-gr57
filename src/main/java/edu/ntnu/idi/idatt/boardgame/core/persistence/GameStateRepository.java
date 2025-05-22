package edu.ntnu.idi.idatt.boardgame.core.persistence;

import edu.ntnu.idi.idatt.boardgame.core.persistence.dto.GameStateDto;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for repositories that handle saving and loading game state.
 *
 * @param <T> The type of {@link GameStateDto} this repository handles.
 */
public interface GameStateRepository<T extends GameStateDto> {

  /**
   * Saves the given game state DTO to a file.
   *
   * @param dto  The game state DTO to save.
   * @param file The path to the file where the game state should be saved.
   * @throws IOException if an I/O error occurs during saving.
   */
  void save(T dto, Path file) throws IOException;

  /**
   * Loads a game state DTO from a file.
   *
   * @param file The path to the file from which to load the game state.
   * @return The loaded game state DTO.
   * @throws IOException if an I/O error occurs during loading.
   */
  T load(Path file) throws IOException;
}
