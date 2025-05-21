package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A room described by an ordered list of corner points that form the perimeter. */
public final class RoomTile extends AbstractCluedoTile {

  private final String roomName;

  /**
   * Set of edges that are open doorways. Each Edge connects a room point and an adjacent corridor
   * point.
   */
  private final Set<Edge> doorEdges = new HashSet<>();

  /** The minimum row index this room occupies. */
  private final int minRow;
  /** The maximum row index this room occupies. */
  private final int maxRow;
  /** The minimum column index this room occupies. */
  private final int minCol;
  /** The maximum column index this room occupies. */
  private final int maxCol;

  /**
   * Creates a room tile with the given name and outline perimeter.
   *
   * @param roomName logical name (“Kitchen”)
   * @param outlinePerimeter ordered points, first = last (minimum 4)
   */
  public RoomTile(String roomName, List<Point> outlinePerimeter) {
    // we store row/col of an arbitrary interior square from the outline
    super(outlinePerimeter.get(0).row(), outlinePerimeter.get(0).col());
    if (outlinePerimeter.size() < 4) {
      throw new IllegalArgumentException("Outline requires ≥ 4 points");
    }
    if (!outlinePerimeter.get(0).equals(outlinePerimeter.get(outlinePerimeter.size() - 1))) {
      throw new IllegalArgumentException("First and last outline point must match");
    }
    this.roomName = Objects.requireNonNull(roomName);

    this.minRow = outlinePerimeter.stream().mapToInt(Point::row).min().orElseThrow();
    this.maxRow = outlinePerimeter.stream().mapToInt(Point::row).max().orElseThrow();
    this.minCol = outlinePerimeter.stream().mapToInt(Point::col).min().orElseThrow();
    this.maxCol = outlinePerimeter.stream().mapToInt(Point::col).max().orElseThrow();
    this.walkable = false; // Room interiors are not "walkable" in the same way corridors are.
  }

  /**
   * Marks the edge between a room boundary point and an adjacent corridor point as a doorway.
   *
   * @param roomBoundaryPoint The point on the room's boundary.
   * @param adjacentCorridorPoint The adjacent point in the corridor.
   * @throws IllegalArgumentException if roomBoundaryPoint is not on the room's perimeter or
   *                                  if adjacentCorridorPoint is not outside the room.
   */
  public void addDoor(Point roomBoundaryPoint, Point adjacentCorridorPoint) {
    // Check roomBoundaryPoint is actually on the boundary of this room
    boolean onHorizontalEdge =
            (roomBoundaryPoint.row() == this.minRow || roomBoundaryPoint.row() == this.maxRow)
                    && (roomBoundaryPoint.col() >= this.minCol && roomBoundaryPoint.col() <= this.maxCol);
    boolean onVerticalEdge =
            (roomBoundaryPoint.col() == this.minCol || roomBoundaryPoint.col() == this.maxCol)
                    && (roomBoundaryPoint.row() >= this.minRow && roomBoundaryPoint.row() <= this.maxRow);

    if (!(onHorizontalEdge || onVerticalEdge)) {
      throw new IllegalArgumentException(
              "Room boundary point "
                      + roomBoundaryPoint
                      + " is not on the calculated perimeter (minR:"
                      + this.minRow
                      + ", maxR:"
                      + this.maxRow
                      + ", minC:"
                      + this.minCol
                      + ", maxC:"
                      + this.maxCol
                      + ") of room "
                      + roomName);
    }

    // Check adjacentCorridorPoint is outside the room's bounds
    boolean corridorIsOutside =
            adjacentCorridorPoint.row() < this.minRow
                    || adjacentCorridorPoint.row() > this.maxRow
                    || adjacentCorridorPoint.col() < this.minCol
                    || adjacentCorridorPoint.col() > this.maxCol;
    if (!corridorIsOutside) {
      throw new IllegalArgumentException(
              "Corridor point "
                      + adjacentCorridorPoint
                      + " is not outside room "
                      + roomName
                      + " (bounds: minR:"
                      + this.minRow
                      + ", maxR:"
                      + this.maxRow
                      + ", minC:"
                      + this.minCol
                      + ", maxC:"
                      + this.maxCol
                      + ")");
    }

    Edge doorEdge = new Edge(roomBoundaryPoint, adjacentCorridorPoint);
    doorEdges.add(doorEdge);
  }

  /** Checks if a player can enter this room from the given corridor coordinates.
   * @param corridorRow The row of the corridor tile.
   * @param corridorCol The column of the corridor tile.
   * @return True if entry is allowed, false otherwise.
   */
  public boolean canEnterFrom(int corridorRow, int corridorCol) {
    // Cluedo has a special rule that you can enter from any adjacent corridor square
    if ("Cluedo".equals(roomName)) {
      return isAdjacentToPerimeter(corridorRow, corridorCol);
    }

    // normal rooms – use the explicit door list
    Point corridorPoint = new Point(corridorRow, corridorCol);
    return doorEdges.stream().anyMatch(door -> corridorMatchesDoor(corridorPoint, door));
  }

  /** Checks if a player can exit this room to the given corridor coordinates.
   * @param corridorRow The row of the corridor tile.
   * @param corridorCol The column of the corridor tile.
   * @return True if exit is allowed, false otherwise.
   */
  public boolean canExitTo(int corridorRow, int corridorCol) {
    // Cluedo has a special rule that you can exit to any adjacent corridor square
    if ("Cluedo".equals(roomName)) {
      return isAdjacentToPerimeter(corridorRow, corridorCol);
    }

    Point corridorPoint = new Point(corridorRow, corridorCol);
    return doorEdges.stream().anyMatch(door -> corridorMatchesDoor(corridorPoint, door));
  }

  /** true if {@code (row,col)} is next to the room’s perimeter but outside it.
   * @param row The row to check.
   * @param col The column to check.
   * @return True if adjacent to perimeter, false otherwise.
   */
  private boolean isAdjacentToPerimeter(int row, int col) {
    boolean left = col == minCol - 1 && row >= minRow && row <= maxRow;
    boolean right = col == maxCol + 1 && row >= minRow && row <= maxRow;
    boolean above = row == minRow - 1 && col >= minCol && col <= maxCol;
    boolean below = row == maxRow + 1 && col >= minCol && col <= maxCol;
    return left || right || above || below;
  }

  /**
   * true when {@code corridorPoint} equals the corridor side of {@code door}. (The *other* endpoint
   * must be inside the room.)
   * @param corridorPoint The point in the corridor.
   * @param door The door edge to check against.
   * @return True if the corridor point matches one side of the door and the other side is inside the room.
   */
  private boolean corridorMatchesDoor(Point corridorPoint, Edge door) {
    if (door.a().equals(corridorPoint)) {
      return isPointInsideRoom(door.b());
    }
    if (door.b().equals(corridorPoint)) {
      return isPointInsideRoom(door.a());
    }
    return false;
  }

  private boolean isPointInsideRoom(Point point) {
    return point.row() >= this.minRow
            && point.row() <= this.maxRow
            && point.col() >= this.minCol
            && point.col() <= this.maxCol;
  }

  /**
   * Gets the name of the room.
   * @return The room name.
   */
  public String getRoomName() {
    return roomName;
  }

  /**
   * Represents a point (coordinate) on the Cluedo board grid.
   * @param row The row index.
   * @param col The column index.
   */
  public record Point(int row, int col) {}

  /**
   * Represents an edge connecting two adjacent {@link Point}s on the board.
   * Typically used to define doorways between a room cell and a corridor cell.
   *
   * @param a The first point of the edge.
   * @param b The second point of the edge.
   * @throws IllegalArgumentException if the points are not adjacent.
   */
  public record Edge(Point a, Point b) {
    /**
     * Constructs an Edge.
     * Ensures that the two points are orthogonally adjacent.
     */
    public Edge {
      if (Math.abs(a.row() - b.row()) + Math.abs(a.col() - b.col()) != 1) {
        throw new IllegalArgumentException("Edge must connect adjacent squares: " + a + ", " + b);
      }
    }

    /**
     * Checks if this edge is adjacent to the given point (i.e., if the point is one of its endpoints).
     * @param p The point to check.
     * @return True if the point is one of the endpoints of this edge, false otherwise.
     */
    boolean adjacentTo(Point p) {
      return p.equals(a) || p.equals(b);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Edge e = (Edge) o;
      return (Objects.equals(a, e.a) && Objects.equals(b, e.b)) ||
              (Objects.equals(a, e.b) && Objects.equals(b, e.a));
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(a) + Objects.hashCode(b);
    }
  }
}
