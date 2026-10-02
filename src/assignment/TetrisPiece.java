package assignment;

import java.awt.*;
import java.util.Arrays;

/**
 * An immutable representation of a tetris piece in a particular rotation.
 * 
 * All operations on a TetrisPiece should be constant time, except for it's
 * initial construction. This means that rotations should also be fast - calling
 * clockwisePiece() and counterclockwisePiece() should be constant time! You may
 * need to do precomputation in the constructor to make this possible.
 */
public final class TetrisPiece implements Piece {

    /**
     * Construct a tetris piece of the given type. The piece should be in it's spawn orientation,
     * i.e., a rotation index of 0.
     * 
     * You may freely add additional constructors, but please leave this one - it is used both in
     * the runner code and testing code.
     */

    private final PieceType type;
    private final int rotationIndex;
    private final Point[] body;
    private final int[] skirt; 
    private final int boundingBoxWidth; 
    private final int boundingBoxHeight;
    private final CircularLinkedList rotations;

    // constructs spawn type - rotation index 0
    public TetrisPiece(PieceType type) {

        // return an invalid piece
        if (type == null) {
            System.err.println("Piece type cannot be null");
            this.type = null;
            this.rotationIndex = -1;
            this.body = new Point[0];
            this.skirt = new int[0];
            this.boundingBoxWidth = 0;
            this.boundingBoxHeight = 0;
            this.rotations = new CircularLinkedList();
            return;
        }
        
        this.type = type;
        this.rotationIndex = 0;
        this.body = Arrays.copyOf(type.getSpawnBody(), type.getSpawnBody().length);
        this.boundingBoxHeight = type.getBoundingBox().height;
        this.boundingBoxWidth = type.getBoundingBox().width;

        // generate skirt for spawn piece
        this.skirt = generateSkirts(this.body, this.boundingBoxWidth);
        
        // generate the circular list w/ all rotations
        this.rotations = new CircularLinkedList();
        generateRotations();
    }

    // constructor for creating rotated pieces
    private TetrisPiece(PieceType type, int rotationIndex, Point[] body, CircularLinkedList rotations) {
        this.type = type;
        this.rotationIndex = rotationIndex;
        this.body = Arrays.copyOf(body, body.length);
        this.boundingBoxHeight = type.getBoundingBox().height;
        this.boundingBoxWidth = type.getBoundingBox().width;
        this.rotations = rotations;
        this.skirt = generateSkirts(this.body, this.boundingBoxWidth);
    }


    @Override
    public PieceType getType() {
        return this.type;
    }

    @Override
    public int getRotationIndex() {
        return this.rotationIndex;
    }

    @Override
    public Piece clockwisePiece() {
        Node current = rotations.findNode(this);
        if (current == null) {
            System.err.println("Current piece not found in rotations list");
        }
        return rotations.getNextNode(current).piece;
    }

    @Override
    public Piece counterclockwisePiece() {
        Node current = rotations.findNode(this);
        if (current == null) {
            System.err.println("Current piece not found in rotations list");
        }
        return rotations.getPrevNode(current).piece;
    }

    @Override
    public int getWidth() {
        return this.boundingBoxWidth;
    }

    @Override
    public int getHeight() {
        return this.boundingBoxHeight;
    }

    @Override
    public Point[] getBody() {
        return Arrays.copyOf(this.body, this.body.length);
    }

    @Override
    public int[] getSkirt() {
        return Arrays.copyOf(this.skirt, this.skirt.length);
    }

    @Override
    public boolean equals(Object other) {
        // Ignore objects which aren't also tetris pieces.
        if(!(other instanceof TetrisPiece)) return false;
        
        TetrisPiece otherPiece = (TetrisPiece) other;

        // bodies might have the same points but in diff orders so we needa check length first
        if (this.body.length != otherPiece.body.length){
            return false;
        }

        if (this.type != otherPiece.type) {
            return false;
        }

        if (this.rotationIndex != otherPiece.rotationIndex) {
            return false;
        }

        return Arrays.equals(this.body, otherPiece.body) && Arrays.equals(this.skirt, otherPiece.skirt) && this.boundingBoxHeight == otherPiece.boundingBoxHeight && this.boundingBoxWidth == otherPiece.boundingBoxWidth;
    }



    // doubly linked circular list to store all 4 rotations    
    static class Node {
        final TetrisPiece piece;
        Node next;
        Node prev;

        public Node(TetrisPiece piece) {
            this.piece = piece;
            this.next = null;
            this.prev = null;
        }
    }
    
    static class CircularLinkedList {
        Node head = null;
        Node tail = null;

        public void add(TetrisPiece piece){
            Node newNode = new Node(piece);
            if (head == null) {
                head = newNode;
                tail = newNode;
                newNode.next = head;
                newNode.prev = tail;
            }
            else {
                tail.next = newNode;
                newNode.prev = tail;
                tail = newNode;
                tail.next = head;
                head.prev = tail;
            }
        }

        public Node getHead() {
            return head;
        }

        public Node getPrevNode(Node node) {
            return node.prev;
        }
        
        public Node getNextNode(Node node) {
            return node.next;
        }

        public Node findNode(TetrisPiece piece){
            if (head == null) return null;
            Node current = head;
            do {
                if (current.piece.equals(piece)) {
                    return current;
                }
                current = current.next;
            } while (current != head);
            return null;
        }
    }

    // makes all 4 rotations and adds them to the circular linked list
    private void generateRotations() {
        // add spawn peice
        rotations.add(this);
        
        Point[] currentBody = Arrays.copyOf(this.body, this.body.length);
        
        for (int i = 1; i < 4; i++) {
            // rotate 90 degrees clockwise
            Point[] newBody = new Point[currentBody.length];
            for (int j = 0; j < currentBody.length; j++) {
                int newX = currentBody[j].y;
                int newY = this.boundingBoxWidth - 1 - currentBody[j].x;
                newBody[j] = new Point(newX, newY);
            }
            
            // create the rotated piece 
            TetrisPiece rotatedPiece = new TetrisPiece(type, i, newBody, rotations);
            rotations.add(rotatedPiece);
            
            currentBody = newBody;
        }
    }

    // generates the skirt for the piece
    private static int[] generateSkirts(Point[] body, int width) {
        int[] skirt = new int[width];
        Arrays.fill(skirt, Integer.MAX_VALUE);
        for (int i = 0; i < body.length; i++) {
            int x = body[i].x;
            int y = body[i].y;
            // replaces skirt in the column if the y value is lower
            if (y < skirt[x]) {
                skirt[x] = y;
            }
        }
        return skirt;
    }

}   
