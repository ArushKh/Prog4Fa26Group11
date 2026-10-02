package assignment;
import java.awt.Point;
import assignment.Piece.PieceType;
import assignment.Board.Action;
import assignment.Board.Result;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;


/*
 * Any comments and methods here are purely descriptions or suggestions.
 * This is your test file. Feel free to change this as much as you want.
 */

public class TetrisTest {

    // This will run ONCE before all other tests. It can be useful to setup up
    // global variables and anything needed for all of the tests.
    private TetrisBrain brain;
    private TetrisBoard board;

    @BeforeAll
    static void setupAll() {
    }

    // This will run before EACH test.
    @BeforeEach
    void setupEach() {
        brain = new TetrisBrain();
        board = new TetrisBoard(10, 20);
    }

    @Test
    void testNextMoveEmptyBoard() {
        Piece tPiece = new TetrisPiece(PieceType.T);
        board.nextPiece(tPiece, new Point(4, 10));
        Action firstMove = brain.nextMove(board);
        assertNotNull("Brain should return a non-null action", firstMove);
        assertTrue("First move should be a valid action",
            firstMove == Action.CLOCKWISE || 
            firstMove == Action.COUNTERCLOCKWISE ||
            firstMove == Action.LEFT ||
            firstMove == Action.RIGHT ||
            firstMove == Action.DROP);
    }

    @Test
    public void testNextMoveWithHoles() {
        // create a board with a hole - fill bottom row except x=5
        for (int x = 0; x < 10; x++) {
            if (x != 5) {
                Piece square = new TetrisPiece(PieceType.SQUARE);
                board.nextPiece(square, new Point(x, 0));
                board.move(Action.DROP);
            }
        }
        
        // add another layer - hole still at x = 5
        for (int x = 0; x < 10; x++) {
            if (x != 5) {
                Piece square = new TetrisPiece(PieceType.SQUARE);
                board.nextPiece(square, new Point(x, 1));
                board.move(Action.DROP);
            }
        }
        
        // set next piece to be a stick that could fill the hole
        Piece stick = new TetrisPiece(PieceType.STICK);
        board.nextPiece(stick, new Point(3, 10));
        Action firstMove = brain.nextMove(board);
        assertNotNull("Brain should return a non-null action for hole-filling", firstMove);
        
        // execute all moves until the piece is placed
        int moveCount = 0;
        while (board.getCurrentPiece() != null && moveCount < 50) {
            Action move = brain.nextMove(board);
            if (move == null) break;
            board.move(move);
            moveCount++;
        }
        
        // holeshould be filled
        assertTrue("Brain should have attempted to fill the hole",board.getColumnHeight(5) > 0);
    }
        



    // You can test execute critter here. You may want to make additional tests and
    // your own testing harness. See spec section 2.5 for more details.
    @Test
    void testTetrisPiece() {

    }

    // Test load species. You may want to make more tests for different cases here.
    @Test
    void testTetrisBoard() {

    }

}
