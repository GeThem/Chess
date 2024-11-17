package chess.engine.pieces;

import chess.engine.board.Board;
import org.javatuples.Pair;

import java.awt.*;
import java.util.ArrayList;

public class King extends NotPinner implements BoolPiece {
    protected boolean hasMoved = false;
    private ArrayList<Pair<Point, ChessPiece>> castle;

    public King(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
        castle = new ArrayList<>(2);
    }

    @Override
    protected ArrayList<Point> generatePossibleMoves(Board board) {
        ArrayList<Point> moves = new ArrayList<>();
        for (var dirX : new int[]{-1, 0, 1}) {
            for (var dirY : new int[]{-1, 0, 1}) {
                int newX = this.x + dirX, newY = this.y + dirY;
                if (newX > board.getWidth() - 1 || newX < 0 || newY > board.getWidth() - 1 || newY < 0) {
                    continue;
                }
                moves.add(new Point(newX, newY));
            }
        }
        return moves;
    }

    @Override
    public boolean isValidMove(int x, int y, Board board) {
        if (this.y == y && this.x == x || Math.abs(this.x - x) > 1 || Math.abs(this.y - y) > 1) {
            return false;
        }
        return super.isValidMove(x, y, board);
    }

    @Override
    protected void generateMoves(Board board) {
        legalMoves = new ArrayList<>();
        possibleMoves = generatePossibleMoves(board);
        for (var m : possibleMoves) {
            var p = board.get(m);
            if (!board.getBool(m) && (p == null || p.getType().team != type.team)) {
                legalMoves.add(m);
            }
        }
        if (pinnedBy == null) {
            castle.clear();
            if (hasMoved) {
                return;
            }
            if (board.get(0, y) == null || board.get(0, y).getTurn() != turn) {
                return;
            }
            if (board.get(board.getWidth() - 1, y) == null || board.get(board.getWidth() - 1, y).getTurn() != turn) {
                return;
            }
            for (var dirX : new int[]{-1, 1}) {
                for (int i = 1; ; i++) {
                    var newX = x + dirX * i;
                    if (newX < 0 || newX >= board.getWidth()) {
                        break;
                    }
                    if (i <= 2 && board.getBool(newX, y)) {
                        break;
                    }
                    var p = board.get(newX, y);
                    if (p == null) {
                        continue;
                    }
                    if (p.getType().piece == Piece.rook) {
                        castle.add(new Pair<>(new Point(dirX * 2 + x, y), p));
                        legalMoves.add(castle.getLast().getValue0());
                    }
                    break;
                }
            }
            return;
        }
    }

    @Override
    public ChessPiece move(int x, int y, Board board) {
        hasMoved = true;
        castle.forEach(mp -> {
            var move = mp.getValue0();
            if (move.x == x && move.y == y) {
                mp.getValue1().move((this.x - mp.getValue1().getX() < 0) ? x - 1 : x + 1, y, board);
            }
        });
        return super.move(x, y, board);
    }

    @Override
    public void setBool(boolean b) {
        hasMoved = b;
    }

    @Override
    public boolean getBool(boolean b) {
        return hasMoved;
    }

    @Override
    public PieceSnapshot makeSnapshot() {
        var snapshot = new BoolPieceSnapshot(x, y, this);
        snapshot.bool = hasMoved;
        snapshot.turn = turn;
        return snapshot;
    }
}