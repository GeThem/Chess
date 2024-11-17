package chess.engine.pieces;

import chess.engine.board.Board;

public class PieceSnapshot {
    public int x, y;
    public ChessPiece captured;
    public int turn;

    public PieceSnapshot(int x, int y, ChessPiece captured) {
        this.x = x;
        this.y = y;
        this.captured = captured;
    }

    public void restoreSnapshot(Board board) {
        captured.x = x;
        captured.y = y;
        captured.turn = turn;
    }
}
