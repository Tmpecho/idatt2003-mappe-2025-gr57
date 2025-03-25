package edu.ntnu.idi.idatt.boardgame.common.action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeAction implements Action {
  private final List<Action> actions = new ArrayList<>();

  public void addAction(Action action) {
    actions.add(action);
  }

  @Override
  public String execute() {
    String sb =
        actions.stream()
            .map(Action::execute)
            .filter(message -> message != null && !message.isEmpty())
            .map(message -> message + "\n")
            .collect(Collectors.joining());
    return sb.trim();
  }
}
