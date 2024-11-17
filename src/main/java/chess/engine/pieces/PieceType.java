package chess.engine.pieces;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PieceType {
    public ChessPiece.Team team;
    public ChessPiece.Piece piece;
    public BufferedImage image;

    public PieceType(ChessPiece.Team team, ChessPiece.Piece piece) {
        this.team = team;
        this.piece = piece;
        try {
            var a = getClass().getResource("/" + piece.name() + "_" + team.name() + ".png");
            var b = ImageIO.read(a);
            image = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_ARGB);
            image.getGraphics().drawImage(b, 0, 0, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
