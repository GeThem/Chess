package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.ArrayList;

public abstract class NotPinner extends ChessPiece {

    public NotPinner(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
    }

    abstract protected ArrayList<Point> generatePossibleMoves(Board board);

    public void fillAttack(Board board) {
        generatePossibleMoves(board).forEach(m -> {
            board.setBool(m, true);
        });
    }

    @Override
    public void setPins(Board board) {
        for (var move : generatePossibleMoves(board)) {
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
    protected void generateMoves(Board board) {
        legalMoves = new ArrayList<>();
        possibleMoves = generatePossibleMoves(board);
        if (pinnedBy == null) {
            for (var m : possibleMoves) {
                var p = board.get(((Point)m).x, ((Point)m).y);
                if (p == null || p.getType().team != type.team) {
                    legalMoves.add(m);
                }
            }
            return;
        }
        for (int i = possibleMoves.size() - 1; i >= 0; i--) {
            var move = (Point)possibleMoves.get(i);
            if (pinnedBy.allowedMoves.stream().anyMatch(x -> x.getX() != move.getX() || x.getY() != move.getY())) {
                continue;
            }
            legalMoves.add(move);
        }
    }
}
