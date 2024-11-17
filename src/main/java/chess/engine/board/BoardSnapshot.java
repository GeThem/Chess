package chess.engine.board;


import chess.engine.pieces.ChessPiece;
import chess.engine.pieces.PieceSnapshot;
import org.javatuples.Pair;

import java.awt.*;
import java.util.ArrayList;


public class BoardSnapshot {
    public ArrayList<Pair<PieceSnapshot, Point>> state = new ArrayList<>();
    public ChessPiece.Team turn;
    public int gameOver;

    public BoardSnapshot(ArrayList<Pair<PieceSnapshot, Point>> state, ChessPiece.Team turn, int gameOver) {
        this.state = state;
        this.turn = turn;
        this.gameOver = gameOver;
    }
}
