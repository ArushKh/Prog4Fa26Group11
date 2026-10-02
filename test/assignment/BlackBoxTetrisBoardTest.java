package assignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import assignment.Piece.PieceType;
import org.junit.jupiter.api.function.Executable;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/*
 * Any comments and methods here are purely descriptions or suggestions.
 * This is your test file. Feel free to change this as much as you want.
 */

public class BlackBoxTetrisBoardTest {

    @Test
    void testSomething() {
        // change this
    }

    @Test
    public void testBoardInitialization() {
        TetrisBoard board = new TetrisBoard(10, 20);
        
        assertEquals(10, board.getWidth());
        assertEquals(20, board.getHeight());
        assertEquals(0, board.getMaxHeight());
        assertNull(board.getCurrentPiece());
        assertNull(board.getCurrentPiecePosition());
    }
    
    @Test
void testSimpleLeftRightMovement() {
        TetrisBoard board = new TetrisBoard(10, 20);
        Piece square = new TetrisPiece(PieceType.SQUARE);
        
        // Place square in middle of board
        board.nextPiece(square, new Point(4, 10));
        
        // Move right should succeed
        assertEquals(Board.Result.SUCCESS, board.move(Board.Action.RIGHT));
        assertEquals(new Point(5, 10), board.getCurrentPiecePosition());
        
        // Move left should succeed
        assertEquals(Board.Result.SUCCESS, board.move(Board.Action.LEFT));
        assertEquals(new Point(4, 10), board.getCurrentPiecePosition());
        
        // Move left 4 times to reach the wall
        board.move(Board.Action.LEFT); // x=3
        board.move(Board.Action.LEFT); // x=2
        board.move(Board.Action.LEFT); // x=1
        board.move(Board.Action.LEFT); // x=0
        
        // Next left move should fail (hit wall)
        assertEquals(Board.Result.OUT_BOUNDS, board.move(Board.Action.LEFT));
        assertEquals(new Point(0, 10), board.getCurrentPiecePosition(), 
            "Piece should stay at x=0 after hitting left wall");
    }
    

    

}
