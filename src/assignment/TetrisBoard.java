package assignment;

import java.awt.Point;
import java.util.Objects;

/**
 * Represents a Tetris board -- essentially a 2-d grid of piece types (or nulls). Supports
 * tetris pieces and row clearing.  Does not do any drawing or have any idea of
 * pixels. Instead, just represents the abstract 2-d board.
 */
public final class TetrisBoard implements Board {

    private int width;
    private int height;
    private Piece.PieceType[][] grid;
    private Piece currentPiece;
    private Point currentPiecePosition;
    private Result lastResult;
    private Action lastAction;
    private int rowsCleared;
    private int[] columnHeights;
    private int[] rowWidths;
    private int maxHeight;


    // JTetris will use this constructor
    public TetrisBoard(int width, int height) {
        if (width < 4 || height < 4) {
            System.err.println("width and height must be at least 4");
            this.width = 0;
            this.height = 0;
            this.grid = new Piece.PieceType[0][0];
            this.rowsCleared = 0;
            this.columnHeights = new int[0];
            this.rowWidths = new int[0];
            this.maxHeight = 0;
            return;
        }
        this.width = width;
        this.height = height;
        this.lastAction = Action.NOTHING;
        this.lastResult = null;
        this.grid = new Piece.PieceType[width][height];
        this.rowsCleared = 0;
        this.columnHeights = new int[width];
        this.rowWidths = new int[height];
        this.maxHeight = 0;

    }

    @Override
    public Result move(Action act) { 
        this.rowsCleared = 0;
        // check if there is no current piece
        if (currentPiece == null){
            lastAction = act;
            lastResult = Result.NO_PIECE;
            return Result.NO_PIECE;
        }

        switch (act) {
            case LEFT:
                lastAction = act;
                if (canPlacePiece(currentPiece, new Point(currentPiecePosition.x - 1, currentPiecePosition.y))) {
                    // move left
                    currentPiecePosition.x -= 1; 
                    lastResult = Result.SUCCESS; 
                    return Result.SUCCESS;
                } 
                else {
                    lastResult = Result.OUT_BOUNDS;
                    return Result.OUT_BOUNDS;
                }

            case RIGHT:
                lastAction = act;
                if (canPlacePiece(currentPiece, new Point(currentPiecePosition.x + 1, currentPiecePosition.y))){
                    // move right
                    currentPiecePosition.x += 1;
                    lastResult = Result.SUCCESS;
                    return Result.SUCCESS;
                }
                else{
                    lastResult = Result.OUT_BOUNDS;
                    return Result.OUT_BOUNDS;
                }
            case DOWN:
                lastAction = act;
                // try to move the piece down by 1
                if (canPlacePiece(currentPiece,  new Point(currentPiecePosition.x, currentPiecePosition.y - 1))) {
                    currentPiecePosition.y -= 1;
                    lastResult = Result.SUCCESS;
                    return Result.SUCCESS;
                } 
                else {
                    // place piece if cant move down
                    placePiece(currentPiece, currentPiecePosition);
                    lastResult = Result.PLACE;
                    return Result.PLACE;
                }

            case DROP:
                lastAction = act;
                // int targetY = dropHeight(currentPiece, currentPiecePosition.x);
                // if (targetY == Integer.MIN_VALUE) {
                //     lastResult = Result.OUT_BOUNDS;
                //     return Result.OUT_BOUNDS;
                // }
                // currentPiecePosition.y = targetY;
                while (canPlacePiece(currentPiece, new Point(currentPiecePosition.x, currentPiecePosition.y - 1))) {
                    currentPiecePosition.y -= 1;
                }
                placePiece(currentPiece, currentPiecePosition);
                lastResult = Result.PLACE;
                return Result.PLACE;
           
            case CLOCKWISE:
                lastAction = act;
                Piece cwRotatedPiece = ((TetrisPiece) currentPiece).clockwisePiece();
                lastResult = canRotateOrWallKick(cwRotatedPiece, true) ? Result.SUCCESS : Result.OUT_BOUNDS;
                return lastResult;

            case COUNTERCLOCKWISE:
                lastAction = act;
                Piece ccwRotatedPiece = ((TetrisPiece) currentPiece).counterclockwisePiece();
                lastResult = canRotateOrWallKick(ccwRotatedPiece, false) ? Result.SUCCESS : Result.OUT_BOUNDS;
                return lastResult;
                
            case NOTHING:
                lastAction = act;
                lastResult = Result.SUCCESS;
                return Result.SUCCESS;
   
            default:
                System.err.println("Invalid action");
                return Result.OUT_BOUNDS;
        }
    }

    @Override
    public Board testMove(Action act) { 
        TetrisBoard copy = new TetrisBoard(this.width, this.height);
        // copy grid
        for (int x = 0; x < this.width; x++){
            for (int y = 0; y < this.height; y++){
                copy.grid[x][y] = this.grid[x][y];
            }
        }
        copy.currentPiece = this.currentPiece;
        if (this.currentPiecePosition != null) {
            copy.currentPiecePosition = new Point(this.currentPiecePosition);
        } 
        else {
            copy.currentPiecePosition = null;
        }
        copy.lastAction = this.lastAction;
        copy.lastResult = this.lastResult;
        copy.rowsCleared = this.rowsCleared;

        System.arraycopy(this.columnHeights, 0, copy.columnHeights, 0, this.width);
        System.arraycopy(this.rowWidths, 0, copy.rowWidths, 0, this.height);
        copy.maxHeight = this.maxHeight;
        copy.move(act);
        return copy; }

    @Override
    public Piece getCurrentPiece() {
         return this.currentPiece;
    }

    @Override
    public Point getCurrentPiecePosition() {
        return this.currentPiecePosition; 
    }

    @Override
    public void nextPiece(Piece p, Point spawnPosition) {
        if (p == null || spawnPosition == null) {
            System.err.println("Piece and spawn position cannot be null");
            return;
        }
        if (this.currentPiece != null) {
            System.err.println("Warning: Setting new piece while current piece exists");
        }
        if (!canPlacePiece(p, spawnPosition)) {
            System.err.println("Invalid spawn position");
            return;
        }
        this.currentPiece = p;
        this.currentPiecePosition = spawnPosition;
    }
    @Override
    public boolean equals(Object other) { 
        if(!(other instanceof TetrisBoard)) return false;
        
        TetrisBoard otherBoard = (TetrisBoard) other;
        // check dimensions;
        if (this.width != otherBoard.width || this.height != otherBoard.height) {
            return false;
        }
        // comapre each cell in the grid
        for (int x = 0; x < this.width; x++){
            for (int y=0; y< this.height; y++){
                if (this.grid[x][y] != otherBoard.grid[x][y]){
                    return false;
                }   
            }
        }
        // compare piece + position
        return Objects.equals(this.currentPiece, otherBoard.currentPiece) && Objects.equals(this.currentPiecePosition, otherBoard.currentPiecePosition);
    }    

    @Override
    public Result getLastResult() { 
        return this.lastResult;
    }

    @Override
    public Action getLastAction() { 
        return this.lastAction;
    }

    @Override
    public int getRowsCleared() {
        return this.rowsCleared;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getMaxHeight() {
        return this.maxHeight;
    }

    @Override
    public int dropHeight(Piece piece, int x){
        if (piece == null) {
            System.err.println("Piece is null");
            return Integer.MIN_VALUE;
        }

        int maxDrop = Integer.MIN_VALUE;
        int[] skirt = piece.getSkirt();

        for (int i = 0; i < piece.getWidth(); i ++){
            // skip columns w/ no blocks
            if (skirt[i] == Integer.MAX_VALUE){
                continue;
            }
            int boardX = x + i;

            // checks if an actual block would be out of bounds
            if (boardX < 0 || boardX >= this.width){
                return Integer.MIN_VALUE;
            }
            // calcs drop distance so piece sits on top of column
            int drop = getColumnHeight(boardX) - skirt[i];

            // takes the farthest it can drop across all columns 
            maxDrop = Math.max(drop, maxDrop);
        }
        return maxDrop;
    }

    @Override
    public int getColumnHeight(int x) {
        if (x < 0 || x >= this.width) {
            return 0;
        }
        return this.columnHeights[x];
    }

    @Override
    public int getRowWidth(int y) {
        if (y < 0 || y >= this.height) {
            return 0;
        }
        return this.rowWidths[y];
    }

    @Override
    public Piece.PieceType getGrid(int x, int y) { 
        if (x < 0 || x >= width || y < 0 || y >= height) {
            System.err.println("Coordinates out of bounds");
            return null;
        }
        // if (grid[x][y] == null) {
        //     System.err.println("No piece at the given coordinates"); 
        //     return null;
        // }
        // if (x >= 0 && x < width && y >= 0 && y < height) {
        //     return grid[x][y];
        // }
        // return null;
        return grid[x][y];
     }
    


     // checks if a piece can be placed at a given position
     public boolean canPlacePiece(Piece piece, Point pos) {
        if (piece == null || pos == null){
            return false;
        }
        for (Point p: piece.getBody()){
            int boardX = pos.x + p.x;
            int boardY = pos.y + p.y;
            if (boardX < 0 || boardX >= width){
                return false;
            }
            if (boardY < 0 || boardY >= height){
                return false;
            }
            if (grid[boardX][boardY] != null) {
                return false; // Collision with existing piece
            }
        }
        return true;
     }
     

     // places a piece on the board at a given position
     public void placePiece(Piece piece, Point pos){
        for (Point p: piece.getBody()){
            int boardX = pos.x + p.x;
            int boardY = pos.y + p.y;
            grid[boardX][boardY] = piece.getType();
            rowWidths[boardY]++;
            // update columnHeight if  block is tallest in column
            if (boardY + 1 > columnHeights[boardX]) {
                columnHeights[boardX] = boardY + 1;
                
                // update maxHeight if necessary
                if (columnHeights[boardX] > maxHeight) {
                    maxHeight = columnHeights[boardX];
                }
            }
        }
        this.currentPiece = null;
        this.currentPiecePosition = null;
        clearRows();
     }

     public boolean canRotateOrWallKick(Piece rotatedPiece, boolean isClockwise) {
        // see if rotated piece works (no wall kick needed)
        if (canPlacePiece(rotatedPiece, currentPiecePosition)) {
            currentPiece = rotatedPiece;
            return true;
        }

        // get the correct wallkick table
        Point[][] wallKicks;
        if (isClockwise) {
            wallKicks = (currentPiece.getType() == Piece.PieceType.STICK) ? Piece.I_CLOCKWISE_WALL_KICKS : Piece.NORMAL_CLOCKWISE_WALL_KICKS;
        } 
        else {
            wallKicks = (currentPiece.getType() == Piece.PieceType.STICK) ? Piece.I_COUNTERCLOCKWISE_WALL_KICKS : Piece.NORMAL_COUNTERCLOCKWISE_WALL_KICKS;
        }
        // try each wall kick 
        for (Point kick : wallKicks[currentPiece.getRotationIndex()]) {
            Point newPos = new Point(currentPiecePosition.x + kick.x, currentPiecePosition.y + kick.y);
            if (canPlacePiece(rotatedPiece, newPos)) {
                currentPiece = rotatedPiece;
                currentPiecePosition = newPos;
                return true;
            }
        }
        // all the rotation attempts failed
        return false;
    }

    public void clearRows(){
        int rowsCleared = 0;
        for (int y = 0; y < height; y++) {
            // check if row is full
            if (rowWidths[y] == width) {
                rowsCleared++;
                // shift rows down in the grid
                for (int newY = y; newY < height - 1; newY++) {
                    for (int x = 0; x < width; x++) {
                        grid[x][newY] = grid[x][newY + 1];
                    }
                    // shift rowWidths down as well
                    rowWidths[newY] = rowWidths[newY + 1];
                }
                // clear the top row
                for (int x = 0; x < width; x++) {
                    grid[x][height - 1] = null;
                }
                rowWidths[height - 1] = 0;
                // recheck the same row since rows shifted down
                y--;
            }
        }
        this.rowsCleared = rowsCleared;
        // recalculate column heights and maxHeight
        if (rowsCleared > 0) {
            recalculateColumnHeights();
        }
    }

     private void recalculateColumnHeights(){
        this.maxHeight = 0;
        for (int x = 0; x < this.width; x++){
            int newHeight = 0;
            for (int y = this.height -1; y >= 0; y--){
                if(this.grid[x][y] != null){
                    newHeight = y + 1;
                    break;
                }
            }
            this.columnHeights[x] = newHeight;
            if (newHeight > this.maxHeight) {
                this.maxHeight = newHeight;
            }
        }
    }
}
