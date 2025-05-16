package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomTileTest {

    private RoomTile kitchen;
    private final List<RoomTile.Point> kitchenOutline = List.of(
            new RoomTile.Point(1, 1), new RoomTile.Point(1, 5),
            new RoomTile.Point(6, 5), new RoomTile.Point(6, 1),
            new RoomTile.Point(1, 1)
    );

    @BeforeEach
    void setUp() {
        kitchen = new RoomTile("Kitchen", kitchenOutline);
    }

    @Test
    void constructor_setsNameAndOutline() {
        assertEquals("Kitchen", kitchen.getRoomName());
        assertEquals(kitchenOutline, kitchen.getOutline());
        assertFalse(kitchen.isWalkable());
    }

    @Test
    void constructor_throwsForInvalidOutline_tooShort() {
        List<RoomTile.Point> shortOutline = List.of(new RoomTile.Point(0,0), new RoomTile.Point(0,1), new RoomTile.Point(0,0));
        assertThrows(IllegalArgumentException.class, () -> new RoomTile("Test", shortOutline));
    }

    @Test
    void constructor_throwsForInvalidOutline_notClosed() {
        List<RoomTile.Point> openOutline = List.of(
                new RoomTile.Point(0,0), new RoomTile.Point(0,1),
                new RoomTile.Point(1,1), new RoomTile.Point(1,0)
        );
        assertThrows(IllegalArgumentException.class, () -> new RoomTile("Test", openOutline));
    }

    @Test
    void addDoor_validDoor_isAdded() {
        RoomTile.Point roomSide = new RoomTile.Point(6, 3); // On kitchen bottom edge
        RoomTile.Point corridorSide = new RoomTile.Point(7, 3); // Adjacent outside
        kitchen.addDoor(roomSide, corridorSide);
        assertTrue(kitchen.getDoors().contains(new RoomTile.Edge(roomSide, corridorSide)));
    }

    @Test
    void addDoor_throwsIfRoomPointNotOnBoundary() {
        RoomTile.Point roomSideInvalid = new RoomTile.Point(3, 3); // Inside kitchen, not boundary
        RoomTile.Point corridorSide = new RoomTile.Point(7, 3);
        assertThrows(IllegalArgumentException.class, () -> kitchen.addDoor(roomSideInvalid, corridorSide));
    }

    @Test
    void addDoor_throwsIfCorridorPointIsInsideRoom() {
        RoomTile.Point roomSide = new RoomTile.Point(6, 3);
        RoomTile.Point corridorSideInvalid = new RoomTile.Point(5, 3); // Inside kitchen
        assertThrows(IllegalArgumentException.class, () -> kitchen.addDoor(roomSide, corridorSideInvalid));
    }

    @Test
    void addDoor_throwsIfPointsNotAdjacentInEdgeConstructor() {
        RoomTile.Point roomSide = new RoomTile.Point(6, 3);
        RoomTile.Point corridorSideFar = new RoomTile.Point(8, 3); // Not adjacent
        assertThrows(IllegalArgumentException.class, () -> new RoomTile.Edge(roomSide, corridorSideFar));
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

        assertFalse(kitchen.canEnterFrom(7, 4)); // Adjacent, but not this door
        assertFalse(kitchen.canEnterFrom(0, 3)); // Not near a door
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
        RoomTile.Point p1 = new RoomTile.Point(1,1);
        RoomTile.Point p2 = new RoomTile.Point(1,2);
        RoomTile.Edge edge1 = new RoomTile.Edge(p1, p2);
        RoomTile.Edge edge2 = new RoomTile.Edge(p2, p1);

        assertEquals(edge1, edge2);
        assertEquals(edge1.hashCode(), edge2.hashCode());
    }
}
