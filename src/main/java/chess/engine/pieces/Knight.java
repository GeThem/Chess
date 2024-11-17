package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.ArrayList;

public class Knight extends NotPinner {

    public Knight(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
    }

    @Override
    public boolean isValidMove(int x, int y, Board board) {
        if (this.x == x && this.y == y) {
            return false;
        }
        int a = Math.abs(this.x - x), b = Math.abs(this.y - y);
        return super.isValidMove(x, y, board) && (a == 1 && b == 2 || a == 2 && b == 1);
    }

    @Override
    protected ArrayList<Point> generatePossibleMoves(Board board) {
        ArrayList<Point> moves = new ArrayList<>();
        for (var dirX : new int[]{-1, 1}) {
            for (var dirY : new int[]{-1, 1}) {
                int newX = x + dirX, newY = y + dirY * 2;
                if (newX < board.getWidth() && newX >= 0 && newY < board.getWidth() && newY >= 0) {
                    moves.add(new Point(newX, newY));
                }
                newX = x + 2 * dirX; newY = y + dirY;
                if (newX < board.getWidth() && newX >= 0 && newY < board.getWidth() && newY >= 0) {
                    moves.add(new Point(newX, newY));
                }
            }
        }
        return moves;
    }
}