package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.stream.Stream;

public class Rook extends Pinner {

    public Rook(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
    }

    @Override
    protected Stream<Point> generatePossibleDirs() {
        var stream = Stream.builder();
        for (var dirX : new int[]{-1, 0, 1}) {
            for (var dirY : new int[]{-1, 0, 1}) {
                if (dirX != 0 && dirY != 0 || dirY == 0 && dirX == 0) {
                    continue;
                }
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
        if (dirX != 0 && dirY != 0) {
            return false;
        }
        if (dirX != 0) { dirX /= Math.abs(dirX); }
        if (dirY != 0) { dirY /= Math.abs(dirY); }
        for (int i = 1; ; i++) {
            int newX = this.x + dirX * i, newY = this.y + dirY * i;
            if (newX == x && newY == y && super.isValidMove(newX, newY, board)) {
                return true;
            }
            if (board.get(newX, newY) != null) {
                return false;
            }
        }
    }
}