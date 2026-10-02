package assignment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import assignment.Board.Action;

public class TetrisBrain implements Brain {
    private static final double holeWeight = -8;
    private static final double varianceWeight =-5;

    private static class BestMove {
        double score;
        List<Board.Action> moves;
        BestMove(double score, List<Board.Action> moves){
            this.score = score;
            this.moves = new ArrayList<>(moves);;
        }
    } 

    // finds the best move
    private BestMove findBestMove(Board board){
        if (board == null){
            System.out.println("Board is null");
            return null;
        }
        if (board.getCurrentPiece() == null){
            System.out.println("No current piece on board");
            return null;
        }
        if (board.getCurrentPiecePosition() == null){
            System.out.println("Current piece position is null");
            return null;
        }
        Piece currentPiece = board.getCurrentPiece();
        BestMove bestMove = null;
        Piece piece = currentPiece;
        // tries all rotations and x positions
        for (int rotations = 0; rotations < 4; rotations++){
            for (int x = -2; x < board.getWidth()+2; x++){
                List<Board.Action> moves = new ArrayList<>();
                for (int r = 0; r < rotations; r++){
                    moves.add(Board.Action.CLOCKWISE);
                }
                Board testBoard = simulatePlacemenet(board, piece, x, moves);
                if (testBoard != null){
                    double score = scoreBoard(testBoard);
                    if (bestMove == null || score > bestMove.score){
                        bestMove = new BestMove(score, new ArrayList<>(moves));
                    }
                }
            }
        }
        return bestMove;
    }
    
    // simulates placing the piece at the given x position 
    private Board simulatePlacemenet(Board board, Piece piece, int x, List<Board.Action> moves){
        if (board == null || piece == null || moves == null){
            System.out.println("One or more arguments are null");
            return null;
        }
        Board testBoard = board.testMove(Board.Action.NOTHING);
        for (Board.Action move: moves){
            if (move == Action.CLOCKWISE || move == Action.COUNTERCLOCKWISE){
                Board newBoard = testBoard.testMove(move);
                if (newBoard.getLastResult() == Board.Result.OUT_BOUNDS){
                    return null;
                }
                testBoard = newBoard;
            }
        }
        Point currentPoint = board.getCurrentPiecePosition();
        int diffX = x - currentPoint.x;
        Board.Action moveX = diffX > 0 ? Board.Action.RIGHT : Board.Action.LEFT;
        // moves the piece to the desired x position
        for (int i = 0; i < Math.abs(diffX); i++){
            testBoard = testBoard.testMove(moveX);
            if (testBoard.getLastResult() == Board.Result.OUT_BOUNDS){
                return null;
            }
            moves.add(moveX);
        }
        testBoard = testBoard.testMove(Board.Action.DROP);
        moves.add(Board.Action.DROP);
        if (testBoard.getLastResult() != Board.Result.PLACE){
            return null;
        }
        return testBoard;
    }

    // scores the board based on holes and variance 
    private double scoreBoard(Board board){
        double holes = calculateHoles(board);
        double variance = calculateVariance(board);
        return holeWeight * holes + varianceWeight * variance;
    }

    // calculates the number of holes
    private double calculateHoles(Board board){
        int holes = 0;
        for (int x = 0; x < board.getWidth(); x++){
            boolean blockFound = false;
            for (int y = board.getHeight()-1; y >= 0; y--){
                if (board.getGrid(x, y) != null){
                    blockFound = true;
                }
                else if (blockFound){
                    holes++;
                }
            }
        }
        return holes;
    }

    // calculates the variance of the column heights
    private double calculateVariance(Board board){
        if (board.getWidth() == 0){
            return 0;
        }
        double totalHeight = 0;
        for (int x = 0; x < board.getWidth(); x++){
            totalHeight += board.getColumnHeight(x);
        }
        double mean = totalHeight/board.getWidth();
        double sumSquaredDiff = 0;
        for (int x= 0; x < board.getWidth(); x++){
            double diff = board.getColumnHeight(x) - mean;
            sumSquaredDiff += diff * diff;
        }
        return sumSquaredDiff/board.getWidth();
    }

    @Override
    public Action nextMove(Board currentBoard) {
        if (currentBoard == null){
            System.out.println("Current board is null");
            return null;
        }
        if (currentBoard.getCurrentPiece() == null){
            System.out.println("No current piece on board");
            return null;
        }
        BestMove bestMove = findBestMove(currentBoard);
        if (bestMove != null && !bestMove.moves.isEmpty()){
            return bestMove.moves.get(0);
        }
        return null;
    }
}
