package chess.engine.pieces;

import chess.engine.board.Board;

public class BoolPieceSnapshot extends PieceSnapshot {
    public boolean bool;

    public BoolPieceSnapshot(int x, int y, ChessPiece captured) {
        super(x, y, captured);
    }

    @Override
    public void restoreSnapshot(Board board) {
        super.restoreSnapshot(board);
        if (captured instanceof BoolPiece) {
            ((BoolPiece) captured).setBool(bool);
        }
    }
}
