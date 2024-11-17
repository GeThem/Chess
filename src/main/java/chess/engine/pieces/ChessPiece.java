package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.ArrayList;

public abstract class ChessPiece {
    public enum Team {
        white(-1),
        black(1);

        public final int value;
        Team(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Piece {
        rook,
        knight,
        bishop,
        pawn,
        queen,
        king
    }

    protected PieceSnapshot snapshot = null;

    protected ArrayList<Point> legalMoves = null;
    protected ArrayList<Point> possibleMoves = null;

    protected ArrayList<Point> allowedMoves;
    protected int x, y;
    protected int turn;
    protected Object tag;

    protected PieceType type;

    protected ChessPiece pinnedBy;

    public ChessPiece(int x, int y, PieceType type, Object tag) {
        this.x = x;
        this.y = y;
        this.tag = tag;
        this.type = type;
        turn = 0;
        allowedMoves = new ArrayList<>();
    }

    public void setLegalMoves(ArrayList<Point> legalMoves) {
        this.legalMoves = legalMoves;
    }

    public int getTurn() {
        return turn;
    }

    public Object getTag() {
        return tag;
    }

    public ChessPiece getPinner() {
        return pinnedBy;
    }

    public void setPinner(ChessPiece pinner) {
        pinnedBy = pinner;
    }

    public void setTurn(int val) {
        turn = val;
    }

    public void fillAttack(Board board) {
        for (var m : getPossibleMoves(board)) {
            board.setBool(m, true);
        }
    }

    abstract public void setPins(Board board);

    public boolean isValidMove(int x, int y, Board board) {
        return board.get(x, y) == null || board.get(x, y).type.team != type.team;
    }

    public ChessPiece move(Point p, Board board) {
        return move(p.x, p.y, board);
    }

    public ChessPiece move(int x, int y, Board board) {
        clearMoves();
        turn++;
        var retVal = board.get(x, y);
        board.set(this.x, this.y, null);
        this.x = x;
        this.y = y;
        board.set(this);
        return retVal;
    }

    public ChessPiece tryMove(int x, int y, Board board) {
        var retVal = board.get(x, y);
        snapshot = new PieceSnapshot(this.x, this.y, retVal);
        board.silentSet(this.x, this.y, null);
        this.x = x;
        this.y = y;
        board.silentSet(x, y,this);
        return retVal;
    }

    public PieceSnapshot makeSnapshot() {
        var snapshot = new PieceSnapshot(x, y, this);
        snapshot.turn = turn;
        return snapshot;
    }

    public boolean resetMove(Board board) {
        if (snapshot == null) {
            return false;
        }
        board.silentSet(x, y, snapshot.captured);
        x = snapshot.x;
        y = snapshot.y;
        board.silentSet(x, y, this);
        snapshot = null;
        return true;
    }

    public boolean isPinned() {
        return pinnedBy != null;
    }

    public PieceType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void clearMoves() {
        legalMoves = null;
        possibleMoves = null;
    }

    abstract protected void generateMoves(Board board);

    public ArrayList<Point> getLegalMoves(Board board) {
        if (legalMoves == null) {
            generateMoves(board);
        }
        return legalMoves;
    }

    public ArrayList<Point> getPossibleMoves(Board board) {
        if (possibleMoves == null) {
            generateMoves(board);
        }
        return possibleMoves;
    }
}

