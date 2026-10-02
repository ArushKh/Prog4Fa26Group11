package assignment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.junit.jupiter.api.Test;

import assignment.Piece.PieceType;
import java.awt.Point;
import java.util.Random;
/*
 * Any comments and methods here are purely descriptions or suggestions.
 * This is your test file. Feel free to change this as much as you want.
 */


 

public class BlackBoxTetrisPieceTest {

    @Test
    void testSomething() {
        // change this
    }

    // check that rotating a piece 4 times returns it to its original orientation
    @Test
    void testRotations() {
        TetrisPiece piece = new TetrisPiece(PieceType.T);
        TetrisPiece r1 = (TetrisPiece) piece.clockwisePiece();
        TetrisPiece r2 = (TetrisPiece) r1.clockwisePiece();
        TetrisPiece r3 = (TetrisPiece) r2.clockwisePiece();
        TetrisPiece r4 = (TetrisPiece) r3.clockwisePiece();

        assertArrayEquals(piece.getBody(), r4.getBody());
        assertArrayEquals(piece.getSkirt(), r4.getSkirt());
        assertTrue(piece.getWidth() == r4.getWidth());
        assertTrue(piece.getHeight() == r4.getHeight());

        TetrisPiece c1 = (TetrisPiece) piece.counterclockwisePiece();
        TetrisPiece c2 = (TetrisPiece) c1.counterclockwisePiece();
        TetrisPiece c3 = (TetrisPiece) c2.counterclockwisePiece();
        TetrisPiece c4 = (TetrisPiece) c3.counterclockwisePiece();

        assertArrayEquals(piece.getBody(), c4.getBody());
        assertArrayEquals(piece.getSkirt(), c4.getSkirt());
        assertTrue(piece.getWidth() == c4.getWidth());
        assertTrue(piece.getHeight() == c4.getHeight());
    }

    // check that rotation indices are correct after multiple rotations
    @Test
    void testRotationIndices() {
        TetrisPiece piece = new TetrisPiece(PieceType.T);
        assertTrue(piece.getRotationIndex() == 0);
        TetrisPiece r1 = (TetrisPiece) piece.clockwisePiece();
        assertTrue(r1.getRotationIndex() == 1);
        TetrisPiece r2 = (TetrisPiece) r1.clockwisePiece();
        assertTrue(r2.getRotationIndex() == 2);
        TetrisPiece r3 = (TetrisPiece) r2.clockwisePiece();
        assertTrue(r3.getRotationIndex() == 3);
        TetrisPiece r4 = (TetrisPiece) r3.clockwisePiece();
        assertTrue(r4.getRotationIndex() == 0);
    }

    // Malcolm's test for all skirts and rotations
    private static final Map<PieceType, Point[][]> ROTATIONS = Map.ofEntries(
    Map.entry(PieceType.STICK, new Point[][] {
        {new Point(0, 2), new Point(1, 2), new Point(2, 2), new Point(3, 2)},
        {new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3)},
        {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1)},
        {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)}
    }),
    Map.entry(PieceType.LEFT_L, new Point[][] {
        {new Point(0, 2), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
        {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
        {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0)},
        {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2)}
    }),
    Map.entry(PieceType.RIGHT_L, new Point[][] {
        {new Point(2, 2), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
        {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0)},
        {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0)},
        {new Point(0, 2), new Point(1, 0), new Point(1, 1), new Point(1, 2)}
    }),
    Map.entry(PieceType.SQUARE, new Point[][] {
        {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)},
        {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)},
        {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)},
        {new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1)}
    }),
    Map.entry(PieceType.RIGHT_DOG, new Point[][] {
        {new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
        {new Point(1, 2), new Point(1, 1), new Point(2, 1), new Point(2, 0)},
        {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)},
        {new Point(0, 2), new Point(0, 1), new Point(1, 1), new Point(1, 0)}
    }),
    Map.entry(PieceType.T, new Point[][] {
        {new Point(1, 2), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
        {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 1)},
        {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 0)},
        {new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(1, 0)}
    }),
    Map.entry(PieceType.LEFT_DOG, new Point[][] {
        {new Point(0, 2), new Point(1, 2), new Point(1, 1), new Point(2, 1)},
        {new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(2, 2)},
        {new Point(0, 1), new Point(1, 1), new Point(1, 0), new Point(2, 0)},
        {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}
    })
    );
    private static final Map<PieceType, int[][]> SKIRTS = Map.ofEntries(
        Map.entry(PieceType.STICK, new int[][] {
            {2, 2, 2, 2},
            {Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE},
            {1, 1, 1, 1},
            {Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE}
        }),
        Map.entry(PieceType.LEFT_L, new int[][] {
            {1, 1, 1},
            {Integer.MAX_VALUE, 0, 2},
            {1, 1, 0},
            {0, 0, Integer.MAX_VALUE},
        }),
        Map.entry(PieceType.RIGHT_L, new int[][] {
            {1, 1, 1},
            {Integer.MAX_VALUE, 0, 0},
            {0, 1, 1},
            {2, 0, Integer.MAX_VALUE}
        }),
        Map.entry(PieceType.SQUARE, new int[][] {
            {0, 0},
            {0, 0},
            {0, 0},
            {0, 0}
        }),
        Map.entry(PieceType.RIGHT_DOG, new int[][] {
            {1, 1, 2},
            {Integer.MAX_VALUE, 1, 0},
            {0, 0, 1},
            {1, 0, Integer.MAX_VALUE}
        }),
        Map.entry(PieceType.T, new int[][] {
            {1, 1, 1},
            {Integer.MAX_VALUE, 0, 1},
            {1, 0, 1},
            {1, 0, Integer.MAX_VALUE}
        }),
        Map.entry(PieceType.LEFT_DOG, new int[][] {
            {2, 1, 1},
            {Integer.MAX_VALUE, 0, 1},
            {1, 0, 0},
            {0, 1, Integer.MAX_VALUE}
        })
    );

    @Test
    void testAllSkirtsAndRotations() {
        Random rand = new Random();

        for (PieceType type : PieceType.values()) {
            int rots = ROTATIONS.get(type).length;
            Comparator<Point> comparator = Comparator.comparingDouble(a -> (a.x + (double) a.y / type.getBoundingBox().height));

            Piece piece = new TetrisPiece(type);
            int rotationIndex = 0;

            for (int i = 0; i < 1000; i++) {
                boolean ccw = rand.nextBoolean();
                for (int j = 0; j < rand.nextInt(10); j++) {
                    if (ccw) piece = piece.counterclockwisePiece();
                    else piece = piece.clockwisePiece();
                    rotationIndex = ((rotationIndex + (ccw ? -1 : 1)) % rots + rots) % rots;

                    assertEquals(rotationIndex, piece.getRotationIndex());

                    Point[] rot = ROTATIONS.get(type)[rotationIndex];
                    Point[] body = piece.getBody();

                    Arrays.sort(rot, comparator);
                    Arrays.sort(body, comparator);
                    assertArrayEquals(rot, body, "Type "+type+" failed at rotation "+rotationIndex+"\nExpected: "+ Arrays.toString(rot)+"\nReceived: "+Arrays.toString(body));

                    int[] skirt = SKIRTS.get(type)[rotationIndex];
                    int[] givenSkirt = piece.getSkirt();
                    assertArrayEquals(skirt, givenSkirt, "Type "+type+" failed at rotation "+rotationIndex+"\nExpected: "+ Arrays.toString(skirt)+"\nReceived: "+Arrays.toString(givenSkirt));
                }
            }
        }
    }

    
}
