package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoomTileTest {

  private final List<RoomTile.Point> kitchenOutline =
          List.of(
                  new RoomTile.Point(1, 1),
                  new RoomTile.Point(1, 5),
                  new RoomTile.Point(6, 5),
                  new RoomTile.Point(6, 1),
                  new RoomTile.Point(1, 1));
  private RoomTile kitchen;

  @BeforeEach
  void setUp() {
    kitchen = new RoomTile("Kitchen", kitchenOutline);
  }

  @Test
  void constructor_setsNameAndWalkable() {
    assertEquals("Kitchen", kitchen.getRoomName());
    assertFalse(kitchen.isWalkable(), "RoomTile should not be walkable by default in its general area.");
  }

  @Test
  void constructor_throwsForInvalidOutline_tooShort() {
    List<RoomTile.Point> shortOutline =
            List.of(new RoomTile.Point(0, 0), new RoomTile.Point(0, 1), new RoomTile.Point(0, 0));
    assertThrows(IllegalArgumentException.class, () -> new RoomTile("Test", shortOutline));
  }

  @Test
  void constructor_throwsForInvalidOutline_notClosed() {
    List<RoomTile.Point> openOutline =
            List.of(
                    new RoomTile.Point(0, 0), new RoomTile.Point(0, 1),
                    new RoomTile.Point(1, 1), new RoomTile.Point(1, 0));
    assertThrows(IllegalArgumentException.class, () -> new RoomTile("Test", openOutline));
  }

  @Test
  void addDoor_validDoor_isAddedAndAllowsEntry() {
    RoomTile.Point roomSide = new RoomTile.Point(6, 3);
    RoomTile.Point corridorSide = new RoomTile.Point(7, 3);
    kitchen.addDoor(roomSide, corridorSide);
    assertTrue(kitchen.canEnterFrom(corridorSide.row(), corridorSide.col()),
            "Should be able to enter from the corridor side of an added door.");
  }

  @Test
  void addDoor_throwsIfRoomPointNotOnBoundary() {
    // kitchenOutline: minRow=1, maxRow=6, minCol=1, maxCol=5
    RoomTile.Point roomSideInvalid = new RoomTile.Point(3, 3);
    RoomTile.Point corridorSide = new RoomTile.Point(7, 3);
    Exception exception = assertThrows(
            IllegalArgumentException.class, () -> kitchen.addDoor(roomSideInvalid, corridorSide));
    assertTrue(exception.getMessage().contains("is not on the calculated perimeter"));
  }

  @Test
  void addDoor_throwsIfCorridorPointIsInsideRoom() {
    RoomTile.Point roomSide = new RoomTile.Point(6, 3);
    RoomTile.Point corridorSideInvalid = new RoomTile.Point(5, 3);
    Exception exception = assertThrows(
            IllegalArgumentException.class, () -> kitchen.addDoor(roomSide, corridorSideInvalid));
    assertTrue(exception.getMessage().contains("is not outside room"));
  }

  @Test
  void addDoor_throwsIfPointsNotAdjacentInEdgeConstructor() {
    RoomTile.Point roomSide = new RoomTile.Point(6, 3);
    RoomTile.Point corridorSideFar = new RoomTile.Point(8, 3);
    assertThrows(
            IllegalArgumentException.class, () -> new RoomTile.Edge(roomSide, corridorSideFar));
  }

  @Test
  void canEnterFrom_returnsTrueForValidDoorAndCorridorPoint() {
    RoomTile.Point roomSide = new RoomTile.Point(6, 3);
    RoomTile.Point corridorSide = new RoomTile.Point(7, 3);
    kitchen.addDoor(roomSide, corridorSide);

    assertTrue(kitchen.canEnterFrom(corridorSide.row(), corridorSide.col()));
  }

  @Test
  void canEnterFrom_returnsFalseForNonDoorCorridorPoint() {
    RoomTile.Point roomSide = new RoomTile.Point(6, 3);
    RoomTile.Point corridorSide = new RoomTile.Point(7, 3);
    kitchen.addDoor(roomSide, corridorSide);

    assertFalse(kitchen.canEnterFrom(7, 4), "Should not enter from a point not defined as a door's corridor side."); // Adjacent, but not this door
    assertFalse(kitchen.canEnterFrom(0, 3), "Should not enter if not near any door."); // Not near a door
  }

  @Test
  void canEnterFrom_returnsFalseIfNoDoors() {
    assertFalse(kitchen.canEnterFrom(7, 3));
  }

  @Test
  void canExitTo_isSymmetricToCanEnterFrom() {
    RoomTile.Point roomSide = new RoomTile.Point(6, 3);
    RoomTile.Point corridorSide = new RoomTile.Point(7, 3);
    kitchen.addDoor(roomSide, corridorSide);

    assertTrue(kitchen.canExitTo(corridorSide.row(), corridorSide.col()));
    assertFalse(kitchen.canExitTo(7, 4));
  }

  @Test
  void edge_equalsAndHashCode_areOrderIndependent() {
    RoomTile.Point p1 = new RoomTile.Point(1, 1);
    RoomTile.Point p2 = new RoomTile.Point(1, 2);
    RoomTile.Edge edge1 = new RoomTile.Edge(p1, p2);
    RoomTile.Edge edge2 = new RoomTile.Edge(p2, p1);

    assertEquals(edge1, edge2);
    assertEquals(edge1.hashCode(), edge2.hashCode());
  }

  @Test
  void cluedoRoom_canEnterFromAnyAdjacentCorridor() {
    // Outline for a Cluedo special room
    List<RoomTile.Point> cluedoOutline = List.of(
            new RoomTile.Point(10, 10), new RoomTile.Point(10, 12),
            new RoomTile.Point(12, 12), new RoomTile.Point(12, 10),
            new RoomTile.Point(10, 10)
    );
    RoomTile cluedoRoom = new RoomTile("Cluedo", cluedoOutline);
    // minRow=10, maxRow=12, minCol=10, maxCol=12

    assertTrue(cluedoRoom.canEnterFrom(9, 10), "Should enter Cluedo room from (9,10)"); // Above
    assertTrue(cluedoRoom.canEnterFrom(13, 11), "Should enter Cluedo room from (13,11)");// Below
    assertTrue(cluedoRoom.canEnterFrom(11, 9), "Should enter Cluedo room from (11,9)");  // Left
    assertTrue(cluedoRoom.canEnterFrom(10, 13), "Should enter Cluedo room from (10,13)");// Right

    assertFalse(cluedoRoom.canEnterFrom(8, 10), "Should not enter from non-adjacent (8,10)");
    assertFalse(cluedoRoom.canEnterFrom(11, 11), "Should not enter from inside the room (11,11)");
  }

  @Test
  void cluedoRoom_canExitToAnyAdjacentCorridor() {
    List<RoomTile.Point> cluedoOutline = List.of(
            new RoomTile.Point(10, 10), new RoomTile.Point(10, 12),
            new RoomTile.Point(12, 12), new RoomTile.Point(12, 10),
            new RoomTile.Point(10, 10)
    );
    RoomTile cluedoRoom = new RoomTile("Cluedo", cluedoOutline);

    assertTrue(cluedoRoom.canExitTo(9, 10));
    assertTrue(cluedoRoom.canExitTo(13, 11));
    assertTrue(cluedoRoom.canExitTo(11, 9));
    assertTrue(cluedoRoom.canExitTo(10, 13));

    assertFalse(cluedoRoom.canExitTo(8, 10));
  }
}
