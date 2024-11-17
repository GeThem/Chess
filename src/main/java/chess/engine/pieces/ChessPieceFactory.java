package chess.engine.pieces;

import java.util.ArrayList;

public class ChessPieceFactory {
    private static final ArrayList<PieceType> pieces = new ArrayList<>();

    public static PieceType getType(ChessPiece.Team team, ChessPiece.Piece piece) {
        PieceType type = null;
        for (var val : pieces) {
            if (val.team == team && val.piece == piece) {
                type = val;
                break;
            }
        }
        if (type == null) {
            type = new PieceType(team, piece);
        }
        return type;
    }

    public static ChessPiece getPiece(int x, int y, ChessPiece.Team team, ChessPiece.Piece piece, Object tag) {
        PieceType type = null;
        for (var val : pieces) {
            if (val.team == team && val.piece == piece) {
                type = val;
                break;
            }
        }
        if (type == null) {
            type = new PieceType(team, piece);
        }
        pieces.add(type);
        switch (piece) {
            case king -> {
                return new King(x, y, type, tag);
            }
            case pawn -> {
                return new Pawn(x, y, type, tag);
            }
            case rook -> {
                return new Rook(x, y, type, tag);
            }
            case queen -> {
                return new Queen(x, y, type, tag);
            }
            case bishop -> {
                return new Bishop(x, y, type, tag);
            }
            case knight -> {
                return new Knight(x, y, type, tag);
            }
            case null, default -> {
                return null;
            }
        }
    }
}
