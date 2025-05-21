package edu.ntnu.idi.idatt.boardgame.ui.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * Utility for showing non‑blocking, toast‑style log messages.
 *
 * <p>Uses ControlsFX {@link Notifications}. Must be called on the JavaFX Application Thread.
 */
public final class LoggingNotification {

  private LoggingNotification() {}

  /**
   * Shows a notification with sensible defaults for the given log type.
   *
   * @param type severity/category (see {@link LoggingType})
   * @param title short summary
   * @param message detailed message (optional)
   */
  public static void show(LoggingType type, String title, String message) {
    Runnable task = () -> build(type, title, message).show();
    runOnFxThread(task);
  }

  /** Convenience overload for errors.
   * @param title The title of the error notification.
   * @param message The detailed message of the error.
   */
  public static void error(String title, String message) {
    show(LoggingType.ERROR, title, message);
  }

  /** Convenience overload for fatal errors.
   * @param title The title of the fatal error notification.
   * @param message The detailed message of the fatal error.
   */
  public static void fatal(String title, String message) {
    show(LoggingType.FATAL, title, message);
  }

  /** Convenience overload for warnings.
   * @param title The title of the warning notification.
   * @param message The detailed message of the warning.
   */
  public static void warn(String title, String message) {
    show(LoggingType.WARN, title, message);
  }

  /** Convenience overload for info messages.
   * @param title The title of the info notification.
   * @param message The detailed message of the info.
   */
  public static void info(String title, String message) {
    show(LoggingType.INFO, title, message);
  }

  /** Convenience overload for debug messages.
   * @param title The title of the debug notification.
   * @param message The detailed message of the debug information.
   */
  public static void debug(String title, String message) {
    show(LoggingType.DEBUG, title, message);
  }

  private static Notifications build(LoggingType type, String title, String message) {
    Notifications notifications =
            Notifications.create()
                    .title(title)
                    .text(message == null ? "" : message)
                    .hideAfter(Duration.seconds(4))
                    .position(Pos.BOTTOM_RIGHT);

    switch (type) {
      case INFO -> notifications.graphic(Icon.INFO.view());
      case DEBUG -> notifications.graphic(Icon.DEBUG.view());
      case WARN -> notifications.graphic(Icon.WARN.view());
      case ERROR -> notifications.graphic(Icon.ERROR.view()).hideAfter(Duration.seconds(6));
      case FATAL -> notifications.graphic(Icon.FATAL.view()).hideAfter(Duration.seconds(8));
      default -> throw new IllegalStateException("Unexpected value: " + type);
    }
    return notifications;
  }

  private static void runOnFxThread(Runnable r) {
    try {
      if (Platform.isFxApplicationThread()) {
        r.run();
      } else {
        Platform.runLater(r);
      }
    } catch (IllegalStateException e) {
      // (toolkit not initialized) — swallow or log to console
      r.run();
    }
  }

  /** Icon helper. */
  private enum Icon {
    INFO("/icons/info64.png"),
    DEBUG("/icons/debug64.png"),
    WARN("/icons/caution64.png"),
    ERROR("/icons/error64.png"),
    FATAL("/icons/fatal64.png");

    private final String path;

    Icon(String p) {
      this.path = p;
    }

    ImageView view() {
      return new ImageView(path);
    }
  }
}
