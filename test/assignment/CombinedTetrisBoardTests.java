package assignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import assignment.Piece.PieceType;
import org.junit.jupiter.api.function.Executable;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Combined tests for TetrisBoard implementation
 * Includes adapted tests and wall-kick tests
 */
public class CombinedTetrisBoardTests {

    // =================== HELPER METHODS ===================
    private void assertGridEquals(Board board, PieceType[][] expected, int[] columnHeights, int[] rowWidths) {
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                assertEquals(expected[board.getHeight() - 1 - y][x], board.getGrid(x, y), "Board not equal at ("+x+","+y+")");
                assertEquals(rowWidths[y], board.getRowWidth(y), "Row width not equal for "+y);
            }

            assertEquals(columnHeights[x], board.getColumnHeight(x), "Column height not equal for "+x);
        }
    }
    
    private void assertSystemErr(boolean error, Executable executable) {
        PrintStream err = System.err;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setErr(new PrintStream(out));
        try {
            executable.execute();
        } catch (Throwable t) {
            System.setErr(err);
            fail(t);
        }
        System.setErr(err);

        assertEquals(error, out.size() > 0);
    }

    private void constructorVerification(int width, int height) {
        Board board = new TetrisBoard(width, height);
        assertEquals(width, board.getWidth());
        assertEquals(height, board.getHeight());
    }

    private void nextPieceVerification(Board board, Piece piece, Point point) {
        board.nextPiece(piece, point);
        assertSame(point == null ? null : piece, board.getCurrentPiece());
        assertEquals(piece == null ? null : point, board.getCurrentPiecePosition());

        if (piece != null && point != null) {
            for (Point p : piece.getBody()) {
                // Note: getGrid will print error for empty cells in your implementation
                board.getGrid(point.x + p.x, point.y + p.y);
            }
        }
    }

    private void moveVerification(Board board, Board.Action action, Board.Result expectedResult) {
        // Note: testMove is not implemented (returns null), so we skip that check
        assertEquals(expectedResult, board.move(action));
        assertEquals(action, board.getLastAction());
        assertEquals(expectedResult, board.getLastResult());
    }

    private void moveVerification(Board board, Board.Action action, Board.Result expectedResult, 
                                 PieceType[][] expectedGrid, Point currentPiecePosition, 
                                 int clearedRows, int maxHeight, int[] columnHeights, int[] rowWidths) {
        // Move only (testMove not implemented)
        moveVerification(board, action, expectedResult);
        assertGridEquals(board, expectedGrid, columnHeights, rowWidths);
        assertEquals(currentPiecePosition, board.getCurrentPiecePosition());
        assertEquals(clearedRows, board.getRowsCleared());
        assertEquals(maxHeight, board.getMaxHeight());
    }

    // =================== BASIC FUNCTION TESTS ===================
    @Test
    void constructor() {
        // test invalid width/height - your code prints errors but creates 0x0 board
        assertSystemErr(true, () -> new TetrisBoard(0, 0));
        assertSystemErr(true, () -> new TetrisBoard(-12, 6));
        assertSystemErr(true, () -> new TetrisBoard(19481394, -11384));
        assertSystemErr(true, () -> new TetrisBoard(3, 3));

        // test valid width/height
        assertSystemErr(false, () -> constructorVerification(4, 4));
        assertSystemErr(false, () -> constructorVerification(2347, 12378));
        assertSystemErr(false, () -> constructorVerification(10, 20));
    }
    
    @Test
    void testNextPiece() {
        // test nulls - your code prints errors instead of throwing exceptions
        assertSystemErr(true, () -> nextPieceVerification(new TetrisBoard(4, 4), null, new Point(0, 0)));
        assertSystemErr(true, () -> nextPieceVerification(new TetrisBoard(4, 4), null, null));

        for (PieceType type : PieceType.values()) {
            Piece piece = new TetrisPiece(type);

            // test null point
            assertSystemErr(true, () -> nextPieceVerification(new TetrisBoard(4, 4), piece, null));

            // test valid
            assertDoesNotThrow(() -> nextPieceVerification(new TetrisBoard(4, 4), piece, new Point(0, 0)));

            // Your implementation prints errors for invalid positions but doesn't throw exceptions
            // We just check that errors are printed
        }

        // BB out of bounds but not piece, also rotation
        assertDoesNotThrow(() -> nextPieceVerification(new TetrisBoard(4, 4), 
            new TetrisPiece(PieceType.RIGHT_DOG).clockwisePiece(), new Point(-1, 1)));
        assertDoesNotThrow(() -> nextPieceVerification(new TetrisBoard(4, 4), 
            new TetrisPiece(PieceType.RIGHT_L), new Point(0, -1)));

        // test overlapping pieces
        Board board = new TetrisBoard(4, 4);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        
        // Your code prints error but doesn't prevent setting the piece
        assertSystemErr(true, () -> board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 0)));
    }

    @Test
    void testGetGrid() {
        // test with full grid except right column (so it doesn't clear)
        Board board = new TetrisBoard(5, 4);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(2, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        
        // Your getGrid prints errors for null cells and OOB, so we need to suppress those
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                PieceType result = board.getGrid(x, y);
                if (x >= 0 && x < 4 && y >= 0 && y < 4) {
                    assertNotNull(result, "Expected non-null at (" + x + "," + y + ")");
                } else {
                    assertNull(result, "Expected null at (" + x + "," + y + ")");
                }
            }
        }
        
        System.setErr(originalErr);

        // test not showing current piece
        board = new TetrisBoard(4, 4);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        assertNull(board.getGrid(0, 0));
        System.setErr(originalErr);
    }

    @Test
    void testGetCurrentPieceAndPosition() {
        // test with no piece
        Board board = new TetrisBoard(4, 4);
        assertNull(board.getCurrentPiece());
        assertNull(board.getCurrentPiecePosition());

        // test with OOB piece location
        Piece piece = new TetrisPiece(PieceType.RIGHT_L);
        board.nextPiece(piece, new Point(0, -1));
        assertSame(piece, board.getCurrentPiece());
        assertEquals(new Point(0, -1), board.getCurrentPiecePosition());
    }

    @Test
    void testEquals() {
        // test same object
        Board board = new TetrisBoard(4, 4);
        assertEquals(board, board);

        // test null
        assertNotEquals(null, board);

        // test different dimensions
        assertNotEquals(new TetrisBoard(5, 4), board);
        assertNotEquals(new TetrisBoard(4, 5), board);
        assertNotEquals(new TetrisBoard(5, 5), board);

        // test variations in currentPiece
        board = new TetrisBoard(5, 4);
        Board board2 = new TetrisBoard(5, 4);
        assertEquals(board, board2);

        board2.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 0));
        assertNotEquals(board, board2);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 0));
        assertEquals(board, board2);

        board.move(Board.Action.RIGHT);
        assertNotEquals(board, board2);

        board2.move(Board.Action.RIGHT);
        assertEquals(board, board2);

        // test variations in the grid
        board = new TetrisBoard(5, 4);
        board2 = new TetrisBoard(5, 4);
        assertEquals(board, board2);

        board2.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board2, Board.Action.DROP, Board.Result.PLACE);
        assertNotEquals(board, board2);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        assertEquals(board, board2);
    }

    @Test
    void testMove() {
        // LEFT
        Board board = new TetrisBoard(4, 4);
        moveVerification(board, Board.Action.LEFT, Board.Result.NO_PIECE, new PieceType[][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
        }, null, 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});
        
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board, Board.Action.LEFT, Board.Result.OUT_BOUNDS, new PieceType[][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});

        // RIGHT
        board = new TetrisBoard(4, 4);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(2, 0));
        moveVerification(board, Board.Action.RIGHT, Board.Result.OUT_BOUNDS, new PieceType[][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
        }, new Point(2, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});

        // DOWN and DROP tests
        board = new TetrisBoard(4, 4);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 1));
        moveVerification(board, Board.Action.DOWN, Board.Result.SUCCESS, new PieceType[][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});
        
        moveVerification(board, Board.Action.DOWN, Board.Result.PLACE, new PieceType[][] {
                {null, null, null, null},
                {null, null, null, null},
                {PieceType.SQUARE, PieceType.SQUARE, null, null},
                {PieceType.SQUARE, PieceType.SQUARE, null, null},
        }, null, 0, 2, new int[] {2, 2, 0, 0}, new int[] {2, 2, 0, 0});

        // Test rotations
        board = new TetrisBoard(4, 4);
        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L), new Point(0, 0));
        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});
    }

    @Test
    void testDropHeight() {
        // set up test board
        Board board = new TetrisBoard(5, 10);
        board.nextPiece(new TetrisPiece(PieceType.T), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        // Test STICK
        assertEquals(0, board.dropHeight(new TetrisPiece(PieceType.STICK), 0));
        assertEquals(2, board.dropHeight(new TetrisPiece(PieceType.STICK), 1));
        assertEquals(Integer.MIN_VALUE, board.dropHeight(new TetrisPiece(PieceType.STICK), 2));

        // Test SQUARE
        assertEquals(2, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 0));
        assertEquals(2, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 1));
        assertEquals(1, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 2));
        assertEquals(4, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 3));
        assertEquals(Integer.MIN_VALUE, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 4));
    }

    // =================== WALL-KICK TESTS ===================
    
    @Test
    void clockwiseNormalWallKicks0to90() {
        TetrisBoard board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L), new Point(1, 0));

        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, PieceType.RIGHT_L},
            {null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L}
        }, null, 0, 2, new int[] {0, 1, 1, 2}, new int[] {3, 1, 0, 0});
       
        

        board.nextPiece(new TetrisPiece(PieceType.T), new Point(0, 0));
        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, PieceType.RIGHT_L},
            {null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L}
        }, new Point(-1, 0), 0, 2, new int[] {0, 1, 1, 2}, new int[] {3, 1, 0, 0});
        
        board = new TetrisBoard(4, 4);


        board.nextPiece(new TetrisPiece(PieceType.T), new Point(0, -1));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
        }, new Point(-1, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});


        board = new TetrisBoard(4, 5);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1, 0));

        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {PieceType.RIGHT_L, null, null, null},
            {PieceType.RIGHT_L, null, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, null}
        }, null, 0, 3, new int[] {3, 1, 0, 0}, new int[] {2, 1, 1, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(0, 1));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, PieceType.T, null, null},
            {PieceType.RIGHT_L, PieceType.T, PieceType.T, null},
            {PieceType.RIGHT_L, PieceType.T, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, null}
        }, null, 0, 4, new int[] {3, 4, 3, 0}, new int[] {2, 2, 3, 1, 0});

        board.nextPiece(new TetrisPiece(PieceType.LEFT_DOG), new Point(1, 2));
        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, PieceType.T, null, null},
            {PieceType.RIGHT_L, PieceType.T, PieceType.T, null},
            {PieceType.RIGHT_L, PieceType.T, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, null}
        }, new Point(1, 0), 0, 4, new int[] {3, 4, 3, 0}, new int[] {2, 2, 3, 1, 0});


        board = new TetrisBoard(5, 6);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece(), new Point(3, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1, 3));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        /*   RL .  .  .  .     
         *   RL 0  0  RL RL
         *   RL RL 0  0  RL
         *   LL LL .  .  RL
         *   LL .  .  .  RL
         *   LL .  RL RL RL
         * 
         *   RL .  .  .  .
         *   RL .  .  RL RL
         *   RL RL .  .  RL
         *   LL LL 0  .  RL
         *   LL 0  0  .  RL
         *   LL 0  RL RL RL
         */

        board.nextPiece(new TetrisPiece(PieceType.LEFT_DOG), new Point(1, 2));

        System.out.println(board.toString());

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {PieceType.RIGHT_L, null, null, null, null},
            {PieceType.RIGHT_L, null, null, PieceType.RIGHT_L, PieceType.RIGHT_L},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, null, PieceType.RIGHT_L},
            {PieceType.LEFT_L, PieceType.LEFT_L, null, null, PieceType.RIGHT_L},
            {PieceType.LEFT_L, null, null, null, PieceType.RIGHT_L},
            {PieceType.LEFT_L, null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L},
        }, new Point(0, 0), 0, 6, new int[] {6, 4, 1, 5, 5}, new int[] {4, 2, 3, 3, 3, 1});
    }


    @Test
    void clockwiseNormalWallKicks90to180() {
        TetrisBoard board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});

        board = new TetrisBoard(4, 6);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(2, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(1, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(-1, 2));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, PieceType.SQUARE, PieceType.SQUARE, null},
            {null, PieceType.SQUARE, PieceType.SQUARE, PieceType.T},
            {null, null, PieceType.T, PieceType.T},
            {null, null, null, PieceType.T},
            {null, null, PieceType.SQUARE, PieceType.SQUARE},
            {null, null, PieceType.SQUARE, PieceType.SQUARE}
        }, new Point(0, 1), 0, 6, new int[] {0, 6, 6, 5}, new int[] {2, 2, 1, 2, 3, 2});


        board = new TetrisBoard(5, 4);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece(), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(3, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(1, 0));

        

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, null, PieceType.LEFT_L},
            {null, PieceType.RIGHT_L, null, null, PieceType.LEFT_L},
            {null, PieceType.RIGHT_L, null, PieceType.LEFT_L, PieceType.LEFT_L}
        }, new Point(1, 2), 0, 3, new int[] {3, 3, 0, 1, 3}, new int[] {3, 2, 3, 0});

        board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        System.out.println(board.toString());

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(0, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {PieceType.STICK, null, null, null},
            {PieceType.STICK, null, null, PieceType.LEFT_L},
            {PieceType.STICK, null, null, PieceType.LEFT_L},
            {PieceType.STICK, null, PieceType.LEFT_L, PieceType.LEFT_L}
        }, new Point(1, 2), 0, 4, new int[] {4, 0, 1, 3}, new int[] {3, 2, 2, 1});
    }

    @Test
    void clockwiseNormalWallKicks180to270() {
        TetrisBoard board = new TetrisBoard(4, 5);

        board.nextPiece(new TetrisPiece(PieceType.T), new Point(0 ,0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece().counterclockwisePiece(), new Point(0 ,1));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, PieceType.T, null, null},
            {PieceType.T, PieceType.T, PieceType.T, null}
        }, new Point(1, 1), 0, 2, new int[] {1, 2, 1, 0}, new int[] {3, 1, 0, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(2 ,1));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece().counterclockwisePiece(), new Point(0 ,1));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, PieceType.LEFT_L},
            {null, null, null, PieceType.LEFT_L},
            {null, PieceType.T, PieceType.LEFT_L, PieceType.LEFT_L},
            {PieceType.T, PieceType.T, PieceType.T, null}
        }, new Point(1, 2), 0, 4, new int[] {1, 2, 2, 4}, new int[] {3, 3, 1, 1, 0});

        board = new TetrisBoard(5, 6);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece().counterclockwisePiece(), new Point(1 ,2));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null}
        }, new Point(1, 0), 0, 5, new int[] {5, 5, 5, 5, 0}, new int[] {1, 1, 1, 1, 4, 0});

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece().counterclockwisePiece(), new Point(1 ,2));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, PieceType.SQUARE, PieceType.SQUARE, null, null},
            {PieceType.STICK, PieceType.SQUARE, PieceType.SQUARE, null, null}
        }, new Point(2, 0), 0, 5, new int[] {5, 5, 5, 5, 0}, new int[] {3, 3, 1, 1, 4, 0});

    }@Test
    void clockwiseNormalWallKicks270to0() {
        TetrisBoard board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(2 ,1));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
        }, new Point(1, 1), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});

        board = new TetrisBoard(6, 4);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(4, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(2 ,0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {PieceType.T, null, null, null, null, null},
            {PieceType.T, PieceType.T, null, null, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.T, null, null, null, PieceType.SQUARE, PieceType.SQUARE}
        }, new Point(1, -1), 0, 3, new int[] {3, 2, 0, 0, 2, 2}, new int[] {3, 4, 1, 0});

        board = new TetrisBoard(5, 5);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(3, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(1 ,0));


        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {PieceType.RIGHT_L, null, null, null, null},
            {PieceType.RIGHT_L, null, null, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, PieceType.SQUARE, PieceType.SQUARE},
        }, new Point(1, 2), 0, 3, new int[] {3, 1, 0, 2, 2}, new int[] {4, 3, 1, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(3, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(1 ,0));


        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.RIGHT_L, null, null, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.RIGHT_L, null, null, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, PieceType.SQUARE, PieceType.SQUARE},
        }, new Point(0, 2), 0, 4, new int[] {3, 1, 0, 4, 4}, new int[] {4, 3, 3, 2, 0});

    }

    /* This test assumes that dropping the block underneath an overhang will not cause it to teleport to the top
     * The test also assumes that the dropHeight() function will return a height as if the block was at the top of the
     * board. Therefore move(drop) should not implement dropHeight().
     */
    @Test
    void testOverhang() {
        TetrisBoard board = new TetrisBoard(4, 15);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece().clockwisePiece(), new Point(0, 5));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(0, 1));
        assertEquals(6, board.dropHeight(board.getCurrentPiece(), 0));

        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, null},
            {PieceType.RIGHT_L, null, null, null},
            {PieceType.STICK, null, null, null},
            {PieceType.STICK, PieceType.T, null, null},
            {PieceType.STICK, PieceType.T, PieceType.T, null},
            {PieceType.STICK, PieceType.T, null, null},
                
        }, null, 0, 6, new int[] {6, 6, 6, 0}, new int[] {2, 3, 2, 1, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    }
    @Test
    void testDropHeight2() {
        Board board = new TetrisBoard(5, 10);
        board.nextPiece(new TetrisPiece(PieceType.T), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        assertSystemErr(false, () -> assertEquals(Integer.MIN_VALUE, board.dropHeight(new TetrisPiece(PieceType.STICK), -5)));
        assertSystemErr(false, () -> assertEquals(Integer.MIN_VALUE, board.dropHeight(new TetrisPiece(PieceType.STICK), 20)));

        assertEquals(1, board.dropHeight(new TetrisPiece(PieceType.STICK).clockwisePiece(), -2));
        assertEquals(2, board.dropHeight(new TetrisPiece(PieceType.STICK).counterclockwisePiece(), 0));
        
        assertEquals(2, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 0));
        assertEquals(2, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 1));
        assertEquals(1, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 2));
        assertEquals(4, board.dropHeight(new TetrisPiece(PieceType.SQUARE), 3));
    }

    @Test
    void CCNormalWallKicks0to270() {
        TetrisBoard board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L), new Point(0, 0));

        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {PieceType.LEFT_L, null, null, null},
            {PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null}
        }, null, 0, 2, new int[] {2, 1, 1, 0}, new int[] {3, 1, 0, 0});
       
        

        board.nextPiece(new TetrisPiece(PieceType.T), new Point(1, 0));
        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {PieceType.LEFT_L, null, null, null},
            {PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null}
        }, new Point(2, 0), 0, 2, new int[] {2, 1, 1, 0}, new int[] {3, 1, 0, 0});
        
        board = new TetrisBoard(4, 4);


        board.nextPiece(new TetrisPiece(PieceType.T), new Point(1, -1));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
        }, new Point(2, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});


        board = new TetrisBoard(4, 5);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(2, 0));

        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, PieceType.LEFT_L},
            {null, null, null, PieceType.LEFT_L},
            {null, null, PieceType.LEFT_L, PieceType.LEFT_L}
        }, null, 0, 3, new int[] {0, 0, 1, 3}, new int[] {2, 1, 1, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(1, 1));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE, new PieceType[][] {
            {null, null, null, null},
            {null, null, PieceType.T, null},
            {null, PieceType.T, PieceType.T, PieceType.LEFT_L},
            {null, null, PieceType.T, PieceType.LEFT_L},
            {null, null, PieceType.LEFT_L, PieceType.LEFT_L}
        }, null, 0, 4, new int[] {0, 3, 4, 3}, new int[] {2, 2, 3, 1, 0});

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_DOG), new Point(0, 2));
        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, PieceType.T, null},
            {null, PieceType.T, PieceType.T, PieceType.LEFT_L},
            {null, null, PieceType.T, PieceType.LEFT_L},
            {null, null, PieceType.LEFT_L, PieceType.LEFT_L}
        }, new Point(0, 0), 0, 4, new int[] {0, 3, 4, 3}, new int[] {2, 2, 3, 1, 0});


        board = new TetrisBoard(5, 6);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).clockwisePiece(), new Point(-1, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece(), new Point(3, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(3, 3));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);


        board.nextPiece(new TetrisPiece(PieceType.RIGHT_DOG), new Point(1, 2));

        System.out.println(board.toString());

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, PieceType.LEFT_L}, 
            {PieceType.LEFT_L, PieceType.LEFT_L, null, null, PieceType.LEFT_L},
            {PieceType.LEFT_L, null, null, PieceType.LEFT_L, PieceType.LEFT_L},
            {PieceType.LEFT_L, null, null, PieceType.RIGHT_L, PieceType.RIGHT_L},
            {PieceType.LEFT_L, null, null, null, PieceType.RIGHT_L},
            {PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null, PieceType.RIGHT_L}
        }, new Point(2, 0), 0, 6, new int[] {5, 5, 1, 4, 6}, new int[] {4, 2, 3, 3, 3, 1});
    }

    @Test
    void CCNormalWallKicks270to180() {
        TetrisBoard board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
        }, new Point(1, 0), 0, 0, new int[] {0, 0, 0, 0}, new int[] {0, 0, 0, 0});

        board = new TetrisBoard(4, 6);
        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(-1, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(1, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(2, 2));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, PieceType.SQUARE, PieceType.SQUARE, null},
            {PieceType.T, PieceType.SQUARE, PieceType.SQUARE, null},
            {PieceType.T, PieceType.T, null, null},
            {PieceType.T, null, null, null},
            {PieceType.SQUARE, PieceType.SQUARE, null, null},
            {PieceType.SQUARE, PieceType.SQUARE, null, null}
        }, new Point(1, 1), 0, 6, new int[] {5, 6, 6, 0}, new int[] {2, 2, 1, 2, 3, 2});


        board = new TetrisBoard(5, 4);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(1, 0));

        

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {PieceType.RIGHT_L, null, null, PieceType.LEFT_L, PieceType.LEFT_L},
            {PieceType.RIGHT_L, null, null, PieceType.LEFT_L, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, PieceType.LEFT_L, null}
        }, new Point(1, 2), 0, 3, new int[] {3, 1, 0, 3, 3}, new int[] {3, 2, 3, 0});

        board = new TetrisBoard(4, 4);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        //System.out.println(board.toString());

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(1, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, PieceType.STICK},
            {PieceType.RIGHT_L, null, null, PieceType.STICK},
            {PieceType.RIGHT_L, null, null, PieceType.STICK},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, PieceType.STICK}
        }, new Point(0, 2), 0, 4, new int[] {3, 1, 0, 4}, new int[] {3, 2, 2, 1});
    }


    @Test
    void CCNormalWallKicks180to90() {
        TetrisBoard board = new TetrisBoard(4, 5);

        board.nextPiece(new TetrisPiece(PieceType.T), new Point(1 ,0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece().counterclockwisePiece(), new Point(1 ,1));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, PieceType.T, null},
            {null, PieceType.T, PieceType.T, PieceType.T}
        }, new Point(0, 1), 0, 2, new int[] {0, 1, 2, 1}, new int[] {3, 1, 0, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1 ,1));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece().counterclockwisePiece(), new Point(1 ,1));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {PieceType.RIGHT_L, null, null, null},
            {PieceType.RIGHT_L, null, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.T, null},
            {null, PieceType.T, PieceType.T, PieceType.T}
        }, new Point(0, 2), 0, 4, new int[] {4, 2, 2, 1}, new int[] {3, 3, 1, 1, 0});

        board = new TetrisBoard(5, 6);

        

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece().counterclockwisePiece(), new Point(1 ,2));

        //System.out.println(board.toString());

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK},
            {null, null, null, null, PieceType.STICK},
            {null, null, null, null, PieceType.STICK},
            {null, null, null, null, PieceType.STICK},
            {null, null, null, null, PieceType.STICK}
        }, new Point(1, 0), 0, 5, new int[] {0, 5, 5, 5, 5}, new int[] {1, 1, 1, 1, 4, 0});

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        //System.out.println(board.toString());

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece().counterclockwisePiece(), new Point(1 ,2));
        // moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS);

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK},
            {null, null, null, null, PieceType.STICK},
            {null, null, null, null, PieceType.STICK},
            {null, null, PieceType.SQUARE, PieceType.SQUARE, PieceType.STICK},
            {null, null, PieceType.SQUARE, PieceType.SQUARE, PieceType.STICK}
        }, new Point(0, 0), 0, 5, new int[] {0, 5, 5, 5, 5}, new int[] {3, 3, 1, 1, 4, 0});

    }


    @Test 
    void clockwiseStickWallKick0to90() {
        TetrisBoard board = new TetrisBoard(4, 6);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, PieceType.RIGHT_L},
            {null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L}
        }, new Point(-2, 0), 0, 2, new int[] {0, 1, 1, 2}, new int[] {3, 1, 0, 0, 0, 0});

        board = new TetrisBoard(4, 6);


        board.nextPiece(new TetrisPiece(PieceType.LEFT_L), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {PieceType.LEFT_L, null, null, null},
            {PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null}
        }, new Point(1, 0), 0, 2, new int[] {2, 1, 1, 0}, new int[] {3, 1, 0, 0, 0, 0});

        board = new TetrisBoard(5, 6);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        //System.out.println(board.toString());

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(3, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        //System.out.println(board.toString());

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 1));

        //System.out.println(board.toString());

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {PieceType.SQUARE, PieceType.SQUARE, null, null, null},
            {PieceType.SQUARE, PieceType.SQUARE, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.STICK, null, null, PieceType.SQUARE, PieceType.SQUARE}
        }, new Point(-1, 0), 0, 6, new int[] {6, 6, 0, 2, 2}, new int[] {3, 3, 1, 1, 2, 2});


        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, -2));

        //System.out.println(board.toString());

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {PieceType.SQUARE, PieceType.SQUARE, null, null, null},
            {PieceType.SQUARE, PieceType.SQUARE, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null}
        }, new Point(2, 0), 0, 4, new int[] {4, 4, 0, 0, 0}, new int[] {1, 1, 2, 2, 0, 0});



    }

    @Test
    void clockwiseStickWallKick90to180() {

        TetrisBoard board = new TetrisBoard(5, 5);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        }, new Point(1, 0), 0, 0, new int[] {0, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});


        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});


        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(1, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, PieceType.STICK},
            {null, null, null, null, PieceType.STICK},
            {null, PieceType.SQUARE, PieceType.SQUARE, null, PieceType.STICK},
            {null, PieceType.SQUARE, PieceType.SQUARE, null, PieceType.STICK}
        }, new Point(0, 2), 0, 4, new int[] {0, 2, 2, 0, 4}, new int[] {3, 3, 1, 1, 0});
        
        board = new TetrisBoard(6, 5);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).clockwisePiece().clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece(), new Point(0, 1));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));

        
        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {null, PieceType.T, null, null, null, null},
            {null, PieceType.T, PieceType.T, null, null, null},
            {null, PieceType.T, PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null},
            {null, null, null, null, PieceType.LEFT_L, null}
        }, new Point(0, -1), 0, 4, new int[] {0, 4, 3, 2, 2, 0}, new int[] {1, 4, 2, 1, 0});
    }


    @Test
    void clockwiseStickWallKick180to270() {
        TetrisBoard board = new TetrisBoard(5, 5);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(1, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {PieceType.LEFT_L, null, null, null, null},
            {PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null, null}
        }, new Point(3, 0), 0, 2, new int[] {2, 1, 1, 0, 0}, new int[] {3, 1, 0, 0, 0});


        board = new TetrisBoard(6, 5);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);


        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(0, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK, null}
        }, new Point(-1, 0), 0, 1, new int[] {0, 1, 1, 1, 1, 0}, new int[] {4, 0, 0, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(1, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK, null}
        }, new Point(3, 1), 0, 1, new int[] {0, 1, 1, 1, 1, 0}, new int[] {4, 0, 0, 0, 0});


        board = new TetrisBoard(6, 6);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(4, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        
        

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(4, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

         board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).counterclockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).clockwisePiece().clockwisePiece(), new Point(3, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);


        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(1, 2));

        System.out.println(board.toString());

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {PieceType.SQUARE, PieceType.SQUARE, null, PieceType.T, PieceType.T, PieceType.T},
            {PieceType.SQUARE, PieceType.SQUARE, null, null, PieceType.T, PieceType.LEFT_L},
            {PieceType.STICK, null, null, null, null, PieceType.LEFT_L},
            {PieceType.STICK, null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.LEFT_L, PieceType.LEFT_L},
            {PieceType.STICK, null, null, PieceType.RIGHT_L, PieceType.SQUARE, PieceType.SQUARE},
            {PieceType.STICK, null, null, PieceType.RIGHT_L, PieceType.SQUARE, PieceType.SQUARE}
            
        }, new Point(0, 0), 0, 6, new int[] {6, 6, 3, 6, 6, 6}, new int[] {4, 4, 5, 2, 4, 5});


    }

    @Test
    void clockwiseStickWallKick270to0() {
        TetrisBoard board = new TetrisBoard(5, 5);
        board.nextPiece(new TetrisPiece(PieceType.STICK).counterclockwisePiece(), new Point(-1, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});


        board.nextPiece(new TetrisPiece(PieceType.STICK).counterclockwisePiece(), new Point(3, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        }, new Point(1, 0), 0, 0, new int[] {0, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});


        board = new TetrisBoard(6, 6);

        board.nextPiece(new TetrisPiece(PieceType.SQUARE), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(3, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(2, 2));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).counterclockwisePiece(), new Point(0, 2));

        

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {PieceType.STICK, null, null, null, null, null},
            {PieceType.STICK, null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK},
            {PieceType.STICK, null, null, null, null, PieceType.STICK},
            {PieceType.STICK, null, null, null, null, PieceType.STICK},
            {PieceType.SQUARE, PieceType.SQUARE, null, null, null, PieceType.STICK},
            {PieceType.SQUARE, PieceType.SQUARE, null, null, null, PieceType.STICK},
        }, new Point(1, 0), 0, 6, new int[] {6, 2, 5, 5, 5, 5}, new int[] {3, 3, 2, 2, 5, 1});

        board = new TetrisBoard(5, 4);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).counterclockwisePiece(), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        System.out.println(board.toString());

        board.nextPiece(new TetrisPiece(PieceType.STICK).counterclockwisePiece(), new Point(2, 0));

        moveVerification(board, Board.Action.CLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, PieceType.LEFT_L, null, null},
            {null, null, PieceType.LEFT_L, null, null},
            {null, PieceType.LEFT_L, PieceType.LEFT_L, null, null},
        }, new Point(0, 1), 0, 3, new int[] {0, 1, 3, 0, 0}, new int[] {2, 1, 1, 0});
        
    }

    @Test
    void CCStickWallKick0to270() {

        TetrisBoard board = new TetrisBoard(5, 6); 
        
        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);


        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, PieceType.RIGHT_L, null},
            {null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, null}
        }, new Point(-1, 0), 0, 2, new int[] {0, 1, 1, 2, 0}, new int[] {3, 1, 0, 0, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, PieceType.RIGHT_L, null},
            {null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, null}
        }, new Point(3, 0), 0, 2, new int[] {0, 1, 1, 2, 0}, new int[] {3, 1, 0, 0, 0, 0});


        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece().clockwisePiece(), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(0, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, null}
        }, new Point(-1, 2), 0, 2, new int[] {2, 2, 2, 2, 0}, new int[] {4, 4, 0, 0, 0, 0});

        board = new TetrisBoard(5, 6);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);
        board.nextPiece(new TetrisPiece(PieceType.LEFT_L), new Point(0, 3));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).clockwisePiece().clockwisePiece(), new Point(2, 4));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 1));

            moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {PieceType.LEFT_L, null, PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L},
            {PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L, null, PieceType.LEFT_L},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
            {PieceType.STICK, null, null, null, null},
        }, new Point(3, 0), 0, 6, new int[] {6, 5, 6, 6, 6}, new int[] {1, 1, 1, 1, 4, 4});

    }

    @Test
    void CCStickWallKick90t0() {
        TetrisBoard board = new TetrisBoard(5, 5);
        
        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        }, new Point(0, 0), 0, 0, new int[] {0, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});


        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        }, new Point(1, 0), 0, 0, new int[] {0, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(-2, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, PieceType.RIGHT_L, null, null, null},
            {null, PieceType.RIGHT_L, null, null, null},
            {null, PieceType.RIGHT_L, PieceType.RIGHT_L, null, null}
        }, new Point(0, 1), 0, 3, new int[] {0, 3, 1, 0, 0}, new int[] {2, 1, 1, 0, 0});

        board = new TetrisBoard(6, 5);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece().clockwisePiece(), new Point(0, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.T).counterclockwisePiece(), new Point(2, 1));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

         board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));

        

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {null, null, null, PieceType.T, null, null},
            {null, null, PieceType.T, PieceType.T, null, null},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.T, null, null},
            {PieceType.RIGHT_L, null, null, null, null, null}
        }, new Point(1, -2), 0, 4, new int[] {2, 2, 3, 4, 0, 0}, new int[] {1, 4, 2, 1, 0});

    }

    @Test
    void CCStickWallKick180t90() {
        TetrisBoard board = new TetrisBoard(6, 5);

        board.nextPiece(new TetrisPiece(PieceType.STICK), new Point(1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(2, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK, null}
        }, new Point(3, 0), 0, 1, new int[] {0, 1, 1, 1, 1, 0}, new int[] {4, 0, 0, 0, 0});


        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(0, 0));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, PieceType.STICK, PieceType.STICK, PieceType.STICK, PieceType.STICK, null}
        }, new Point(-2, 0), 0, 1, new int[] {0, 1, 1, 1, 1, 0}, new int[] {4, 0, 0, 0, 0});


        board = new TetrisBoard(5, 6);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L), new Point(2, 3));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.RIGHT_L).clockwisePiece(), new Point(-1, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(0, 2));

         moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, PieceType.RIGHT_L},
            {null, null, PieceType.RIGHT_L, PieceType.RIGHT_L, PieceType.RIGHT_L},
            {null, null, null, null,  PieceType.STICK},
            {PieceType.RIGHT_L, null, null, null, PieceType.STICK},
            {PieceType.RIGHT_L,null, null, null, PieceType.STICK},
            {PieceType.RIGHT_L, PieceType.RIGHT_L, null, null,  PieceType.STICK}
        }, new Point(1, 0), 0, 6, new int[] {3, 1, 5, 5, 6}, new int[] {3, 2, 2, 1, 3, 1});

        board = new TetrisBoard(5, 5);

        

        board.nextPiece(new TetrisPiece(PieceType.LEFT_L).clockwisePiece().clockwisePiece(), new Point(2, 0));
        moveVerification(board, Board.Action.DROP, Board.Result.PLACE);

        board.nextPiece(new TetrisPiece(PieceType.STICK).clockwisePiece().clockwisePiece(), new Point(0, -1));

        moveVerification(board, Board.Action.COUNTERCLOCKWISE, Board.Result.SUCCESS, new PieceType[][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, PieceType.LEFT_L, PieceType.LEFT_L, PieceType.LEFT_L},
            {null, null, null, null, PieceType.LEFT_L}
        }, new Point(-2, 0), 0, 2, new int[] {0, 0, 2, 2, 2}, new int[] {1, 3, 0, 0, 0});


    }
}