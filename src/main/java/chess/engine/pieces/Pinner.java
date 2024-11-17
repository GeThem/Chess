package chess.engine.pieces;

import chess.engine.board.Board;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public abstract class Pinner extends ChessPiece {

    public Pinner(int x, int y, PieceType type, Object tag) {
        super(x, y, type, tag);
    }

    abstract protected Stream<Point> generatePossibleDirs();

    public void fillAttack(Board board) {
        generatePossibleDirs().forEach(d -> {
            for (var m : checkDir(d.x, d.y, board, true)) {
                board.setBool(m, true);
            }
        });
    }

    public void setPins(Board board) {
        for (var d : generatePossibleDirs().toArray()) {
            var dir = (Point)d;
            if (lookForPins(dir.x, dir.y, board)) {
                break;
            }
        }
    }

    @Override
    protected void generateMoves(Board board) {
        possibleMoves = new ArrayList<>();
        generatePossibleDirs().forEach(dir -> {
            var a = checkDir(dir.x, dir.y, board, false);
            if (!a.isEmpty() && board.get(a.getLast()) != null && board.get(a.getLast()).getType().team == type.team) {
                a.removeLast();
            }
            possibleMoves.addAll(a);
        });
        if (pinnedBy != null) {
            legalMoves = pinnedBy.allowedMoves;
        } else {
            legalMoves = possibleMoves;
        }
    }

    public ArrayList<Point> checkDir(int dirX, int dirY, Board board, boolean ignoreKing) {
        ArrayList<Point> currDir = new ArrayList<>();
        for (int i = 1; ; i++) {
            int newX = this.x + dirX * i, newY = this.y + dirY * i;
            if (newX > board.getWidth() - 1 || newX < 0 || newY > board.getWidth() - 1 || newY < 0) {
                return currDir;
            }
            currDir.add(new Point(newX, newY));
            if (ignoreKing) {
                var p = board.get(newX, newY);
                if (p != null && p.type.piece == Piece.king && p.getType().team != type.team) {
                    continue;
                }
            }
            if (!super.isValidMove(newX, newY, board)) {
                return currDir;
            }
            if (board.get(newX, newY) != null) {
                return currDir;
            }
        }
    }

    private boolean lookForPins(int dirX, int dirY, Board board) {
        allowedMoves.clear();
        allowedMoves.add(new Point(x, y));
        boolean lookingForPin = false;
        ChessPiece possiblePin = null;
        for (int i = 1; ; i++) {
            int newX = this.x + dirX * i, newY = this.y + dirY * i;
            if (newX > board.getWidth() - 1 || newX < 0 || newY > board.getWidth() - 1 || newY < 0) {
                return false;
            }
            if (!super.isValidMove(newX, newY, board))
            {
                return false;
            }

            if (!lookingForPin) {
                allowedMoves.add(new Point(newX, newY));
                possiblePin = board.get(newX, newY);
                if (possiblePin != null) {
                    if (possiblePin.getType().piece == Piece.king) {
                        possiblePin.setPinner(this);
                        return true;
                    }
                    lookingForPin = true;
                }
            } else {
                if (board.get(newX, newY) == null) {
                    allowedMoves.add(new Point(newX, newY));
                    continue;
                }
                if (board.get(newX, newY).type.piece != Piece.king) {
                    return false;
                }
                possiblePin.setPinner(this);
                return true;
            }
        }
    }
}
