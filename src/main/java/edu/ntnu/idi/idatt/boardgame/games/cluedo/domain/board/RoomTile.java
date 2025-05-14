package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A room described by an ordered list of corner points that form the perimeter.
 *
 * <p>Edges (p[i] → p[i+1]) are walls except those registered via {@link #addDoor}. Coordinates are
 * board squares (row, col), 0-based.
 */
public final class RoomTile extends AbstractCluedoTile {

  /** Name shown on cards / UI */
  private final String roomName;

  /** Ordered perimeter; first == last. */
  private final List<Point> outline;

  /** Set of edges that are open doorways. */
  private final Set<Edge> doorEdges = new HashSet<>();

  private final int minRow, maxRow, minCol, maxCol;


  /**
   * @param roomName logical name (“Kitchen”)
   * @param outlinePerimeter ordered points, first = last (minimum 4)
   */
  public RoomTile(String roomName, List<Point> outlinePerimeter) {
    // we store row/col of an arbitrary interior square
    super(outlinePerimeter.get(0).row(), outlinePerimeter.get(0).col());
    if (outlinePerimeter.size() < 4)
      throw new IllegalArgumentException("Outline requires ≥ 4 points");
    if (!outlinePerimeter.get(0).equals(outlinePerimeter.get(outlinePerimeter.size() - 1)))
      throw new IllegalArgumentException("First and last outline point must match");
    this.roomName = Objects.requireNonNull(roomName);
    this.outline = List.copyOf(outlinePerimeter);

    this.minRow = outlinePerimeter.stream().mapToInt(Point::row).min().orElseThrow();
    this.maxRow = outlinePerimeter.stream().mapToInt(Point::row).max().orElseThrow();
    this.minCol = outlinePerimeter.stream().mapToInt(Point::col).min().orElseThrow();
    this.maxCol = outlinePerimeter.stream().mapToInt(Point::col).max().orElseThrow();
  }

  /**
   * Marks the edge between a room boundary point and an adjacent corridor point as a doorway.
   *
   * @param roomBoundaryPoint The point on the room's boundary.
   * @param adjacentCorridorPoint The adjacent point in the corridor.
   */
  public void addDoor(Point roomBoundaryPoint, Point adjacentCorridorPoint) {
    // Check roomBoundaryPoint is actually on the boundary of this room
    boolean onHorizontalEdge = (roomBoundaryPoint.row() == this.minRow || roomBoundaryPoint.row() == this.maxRow) &&
            (roomBoundaryPoint.col() >= this.minCol && roomBoundaryPoint.col() <= this.maxCol);
    boolean onVerticalEdge = (roomBoundaryPoint.col() == this.minCol || roomBoundaryPoint.col() == this.maxCol) &&
            (roomBoundaryPoint.row() >= this.minRow && roomBoundaryPoint.row() <= this.maxRow);

    if (! (onHorizontalEdge || onVerticalEdge) ) {
      throw new IllegalArgumentException("Room boundary point " + roomBoundaryPoint +
              " is not on the calculated perimeter (minR:" + this.minRow +
              ", maxR:" + this.maxRow + ", minC:" + this.minCol +
              ", maxC:" + this.maxCol + ") of room " + roomName);
    }

    // Check adjacentCorridorPoint is outside the room's bounds
    boolean corridorIsOutside = adjacentCorridorPoint.row() < this.minRow || adjacentCorridorPoint.row() > this.maxRow ||
            adjacentCorridorPoint.col() < this.minCol || adjacentCorridorPoint.col() > this.maxCol;
    if (!corridorIsOutside) {
      throw new IllegalArgumentException("Corridor point " + adjacentCorridorPoint +
              " is not outside room " + roomName + " (bounds: minR:" + this.minRow +
              ", maxR:" + this.maxRow + ", minC:" + this.minCol +
              ", maxC:" + this.maxCol + ")");
    }

    // Edge constructor checks for adjacency between roomBoundaryPoint and adjacentCorridorPoint
    Edge doorEdge = new Edge(roomBoundaryPoint, adjacentCorridorPoint);
    doorEdges.add(doorEdge);
  }


  /** Checks if a player can enter this room from the given corridor coordinates. */
  public boolean canEnterFrom(int corridorRow, int corridorCol) {
    Point corridorPoint = new Point(corridorRow, corridorCol);
    for (Edge door : doorEdges) {
      // A door edge connects a room point and a corridor point.
      // If the given corridorPoint is one of the points in a door edge.
      if (door.a().equals(corridorPoint) || door.b().equals(corridorPoint)) {
        return true;
      }
    }
    return false;
  }

  private List<Edge> perimeterEdges() {
    return IntStream.range(0, outline.size() - 1)
            .mapToObj(i -> new Edge(outline.get(i), outline.get(i + 1)))
            .collect(Collectors.toList());
  }

  /**
   * Checks if the given edge 'e' is one of the main structural perimeter edges
   * (connecting corners) of the room. Not generally used for door validation.
   */
  private boolean isPerimeterEdge(Edge e) {
    return perimeterEdges().contains(e);
  }

  public String getRoomName() {
    return roomName;
  }

  public List<Point> getOutline() {
    return outline;
  }

  public Set<Edge> getDoors() {
    return Set.copyOf(doorEdges);
  }

  public record Point(int row, int col) {}

  /** Undirected edge between two adjacent squares. */
  public record Edge(Point a, Point b) {
    public Edge {
      if (Math.abs(a.row() - b.row()) + Math.abs(a.col() - b.col()) != 1)
        throw new IllegalArgumentException("Edge must connect adjacent squares: " + a + ", " + b);
    }

    boolean adjacentTo(Point p) {
      return p.equals(a) || p.equals(b);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Edge e
              && (e.a.equals(a) && e.b.equals(b) || e.a.equals(b) && e.b.equals(a));
    }

    @Override
    public int hashCode() {
      return a.hashCode() + b.hashCode();
    }
  }
}
