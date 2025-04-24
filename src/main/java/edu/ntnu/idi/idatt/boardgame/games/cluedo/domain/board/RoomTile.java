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
  }

  /** Marks the edge (p1 → p2) as a doorway. Call after construction. */
  public void addDoor(Point p1, Point p2) {
    Edge e = new Edge(p1, p2);
    if (!isPerimeterEdge(e))
      throw new IllegalArgumentException("Edge " + e + " not on room perimeter");
    doorEdges.add(e);
  }

  /** Corridor square (r,c) may enter if its edge to this room is a door. */
  public boolean canEnterFrom(int r, int c) {
    Point corridor = new Point(r, c);
    // look for a perimeter edge adjoining the corridor square
    return perimeterEdges().stream().anyMatch(e -> doorEdges.contains(e) && e.adjacentTo(corridor));
  }

  private List<Edge> perimeterEdges() {
	  return IntStream.range(0, outline.size() - 1)
	      .mapToObj(i -> new Edge(outline.get(i), outline.get(i + 1)))
	      .collect(Collectors.toList());
  }

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
        throw new IllegalArgumentException("Edge must connect adjacent squares");
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
