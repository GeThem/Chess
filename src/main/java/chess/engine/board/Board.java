package chess.engine.board;

import chess.engine.pieces.ChessPiece;
import chess.engine.pieces.PieceSnapshot;
import org.javatuples.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Board {
    private int width, height;
    private ChessPiece[][] values;
    private Boolean[][] bools;
    public ChessPiece.Team turn = ChessPiece.Team.white;
    private Map<ChessPiece.Team, ArrayList<ChessPiece>> pieces;
    public int gameOver = 0;

    public Board(int width, int height, boolean generateBools) {
        this.width = width;
        this.height = height;
        pieces = new HashMap<>() {};
        values = Stream.iterate(0, y -> y + 1).limit(height)
                .map(_ -> Stream.generate(() -> null).limit(width).toArray(ChessPiece[]::new))
                .toArray(ChessPiece[][]::new);
        if (!generateBools) {
            return;
        }
        bools = Stream.iterate(0, y -> y + 1).limit(height)
                .map(_ -> Stream.generate(() -> false).limit(width).toArray(Boolean[]::new))
                .toArray(Boolean[][]::new);
    }

    public void setBool(Point cell, boolean val) {
        bools[cell.y][cell.x] = val;
    }

    public void setBool(int x, int y, boolean val) {
        bools[y][x] = val;
    }

    public void switchTurn() {
        turn = turn == ChessPiece.Team.white ? ChessPiece.Team.black : ChessPiece.Team.white;
        for (var row : values) {
            for (var p : row) {
                if (p != null && p.getType().team != turn) {
                    p.setTurn(p.getTurn() + 1);
                }
            }
        }
    }

    public boolean getBool(Point cell) {
        return bools[cell.y][cell.x];
    }

    public boolean getBool(int x, int y) {
        return bools[y][x];
    }

    public void clearBools() {
        for (var row : bools) {
            Arrays.fill(row, false);
        }
    }

    public int getWidth() {
        return width;
    }

    public ChessPiece get(int x, int y) {
        if (y < 0 || y > values.length - 1 || x > values[0].length - 1 || x < 0)
            return null;
        return values[y][x];
    }

    public ChessPiece get(Point p) {
        return get(p.x, p.y);
    }

    public void set(int x, int y, ChessPiece value) {
        if (value == null) {
            values[y][x] = value;
            return;
        }
        pieces.computeIfAbsent(value.getType().team, k -> new ArrayList<>());
        var cell = values[y][x];
        if (cell != null) {
            pieces.get(cell.getType().team).remove(cell);
        }
        if (!pieces.get(value.getType().team).contains(value)) {
            pieces.get(value.getType().team).add(value);
        }
        values[y][x] = value;
    }

    public void invisibleSet(int x, int y, ChessPiece value) {
        values[y][x] = value;
    }

    public void set(ChessPiece value) {
        if (value != null) {
            set(value.getX(), value.getY(), value);
        }
    }

    public Map<ChessPiece.Team, ArrayList<ChessPiece>> getPieces() {
        return pieces;
    }

    public BoardSnapshot makeSnapshot() {
        ArrayList<Pair<PieceSnapshot, Point>> state = new ArrayList<>();
        for (var row : values) {
            for (var piece : row) {
                if (piece != null) {
                    state.add(new Pair<>(piece.makeSnapshot(), new Point(piece.getX(), piece.getY())));
                }
            }
        }
        return new BoardSnapshot(state, turn, gameOver);
    }

    public void clearBoard() {
        gameOver = 0;
        turn = ChessPiece.Team.white;
        pieces = new HashMap<>();
        for (var row : values) {
            Arrays.fill(row, null);
        }
    }

    public void restoreSnapshot(BoardSnapshot snapshot) {
        clearBoard();
        gameOver = snapshot.gameOver;
        turn = snapshot.turn;
        for (var val : snapshot.state) {
            var p = val.getValue0().captured;
            val.getValue0().restoreSnapshot(this);
            set(p);
            p.clearMoves();
        }
    }
}