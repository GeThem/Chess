package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.stream.Stream;

public class Bishop extends Pinner {

    public Bishop(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
    }

    @Override
    protected Stream<Point> generatePossibleDirs() {
        var stream = Stream.builder();
        for (var dirX : new int[]{-1, 1}) {
            for (var dirY : new int[]{-1, 1}) {
                stream.add(new Point(dirX, dirY));
            }
        }
        return stream.build().map(x -> (Point)x);
    }

    @Override
    public boolean isValidMove(int x, int y, Board board) {
        if (this.x == x && this.y == y) {
            return false;
        }
        int dirX = x - this.x, dirY = y - this.y;
        if (Math.abs(dirX) != Math.abs(dirY)) {
            return false;
        }
        dirX /= Math.abs(dirX);
        dirY /= Math.abs(dirY);
        for (int i = 1; ; i++) {
            int newX = this.x + dirX * i, newY = this.y + dirY * i;
            if (newX == x && super.isValidMove(newX, newY, board)) {
                return true;
            }
            if (board.get(newX, newY) != null) {
                return false;
            }
        }
    }
}
