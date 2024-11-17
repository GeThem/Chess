package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.ArrayList;

public class Pawn extends NotPinner implements BoolPiece {
    private boolean movedFirstTime = false;

    public Pawn(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
    }

    public void fillAttack(Board board) {
        for (int i = -1; i <= 1; i += 2) {
            int newY = y + type.team.getValue();
            int newX = x + i;
            if (newX > board.getWidth() - 1 || newX < 0 || newY > board.getWidth() - 1 || newY < 0) {
                continue;
            }
            board.setBool(newX, newY, true);
        }
    }

    @Override
    public void setPins(Board board) {
        for (var move : generatePossibleMoves(board)) {
            if (move.getX() == x) {
                continue;
            }
            var piece = board.get(((Point)move).x, ((Point)move).y);
            if (piece == null) {
                continue;
            }
            if (piece.getType().team == type.team) {
                continue;
            }
            if (piece.getType().piece == Piece.king) {
                piece.setPinner(this);
                allowedMoves.clear();
                allowedMoves.add(new Point(x, y));
                return;
            }
        }
    }

    @Override
    protected ArrayList<Point> generatePossibleMoves(Board board) {
        ArrayList<Point> moves = new ArrayList<>();
        int dirY = type.team.getValue();
        for (var dirX : new int[]{-1, 1}) {
            int newX = x + dirX, newY = y + dirY;
            if (newX > board.getWidth() - 1 || newX < 0 || newY > board.getWidth() - 1 || newY < 0) {
                continue;
            }
            if (board.get(newX, newY) != null) {
                moves.add(new Point(newX, newY));
            }
            var p = board.get(newX, y);
            if (p != null && p.getType().piece == Piece.pawn && p.getTurn() == 0) {
                moves.add(new Point(newX, newY));
            }
        }
        if (board.get(x, y + dirY) != null)
            return moves;
        if (y + dirY < board.getWidth() && y + dirY >= 0) {
            moves.add(new Point(x, y + dirY));
        }
        if (!movedFirstTime && y + dirY * 2 < board.getWidth() && y + dirY * 2 >= 0 && board.get(x, y + dirY * 2) == null) {
            moves.add(new Point(x, y + dirY * 2));
        }
        return moves;
    }

    @Override
    public boolean isValidMove(int x, int y, Board board) {
        if (this.y == y || (y - this.y) * type.team.getValue() < 0) {
            return false;
        }
        if (Math.abs(this.x - x) > 1 || Math.abs(this.y - y) > (2 - turn)) {
            return false;
        }
        if (this.x == x) {
            return board.get(x, y) == null;
        }
        return board.get(x, y) != null && board.get(x, y).type.team != type.team;
    }

    @Override
    public ChessPiece move(int x, int y, Board board) {
        if (!movedFirstTime) {
            movedFirstTime = true;
            turn = -2;
        }
        return super.move(x, y, board);
    }

    @Override
    public void setBool(boolean b) {
        movedFirstTime = b;
    }

    @Override
    public boolean getBool(boolean b) {
        return movedFirstTime;
    }

    @Override
    public PieceSnapshot makeSnapshot() {
        var snapshot = new BoolPieceSnapshot(x, y, this);
        snapshot.bool = movedFirstTime;
        snapshot.turn = turn;
        return snapshot;
    }
}