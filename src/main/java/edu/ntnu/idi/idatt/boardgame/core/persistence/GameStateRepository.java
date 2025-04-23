package edu.ntnu.idi.idatt.boardgame.core.persistence;

import edu.ntnu.idi.idatt.boardgame.core.persistence.dto.GameStateDTO;
import java.io.IOException;
import java.nio.file.Path;

public interface GameStateRepository<T extends GameStateDTO> {
  void save(T dto, Path file) throws IOException;

  T load(Path file) throws IOException;
}
