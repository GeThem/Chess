package chess.engine;

import chess.engine.board.Board;
import chess.engine.board.BoardSnapshot;
import chess.engine.pieces.ChessPiece;
import chess.engine.pieces.ChessPieceFactory;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game extends JPanel implements ActionListener {
	private final int WIDTH = 8;
	private final int HEIGHT = 8;
	private int oX = 0;
	private int oY = 0;
	private int TILE_SIZE = 0;
	private Timer timer;
	private BufferedImage backgroundImage;
	private Board board;
	private ChessPiece focused = null, focusedLast = null;
	private Map<ChessPiece.Team, JPopupMenu> popups;
	private TurnState turnState;
	private ArrayList<BoardSnapshot> history;
	private int currSnapshotIndex = -1;

	public enum TurnState {
		movingPiece,
		turningPawn
	}

	public Game() {
		setPreferredSize(new Dimension(800, 600));
		setFocusable(true);

		InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "Redo");
		am.put("Undo", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prevSnapshot();
			}
		});
		am.put("Redo", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextSnapshot();
			}
		});

		addKeyListener(new KeyAdapter() {

		});
		var mouseListener = new MouseListener();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addComponentListener(new onWindowResize());
		addAncestorListener(new FrameListener());

		backgroundImage = drawBackground(135);
		popups = new HashMap<>();
		for (var team : ChessPiece.Team.values()) {
			var popup = new JPopupMenu();
			popup.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
			for (var piece : new String[]{"Queen", "Rook", "Bishop", "Knight"}) {
				var mi = new JMenuItem();
				mi.setIcon(new ImageIcon(ChessPieceFactory.getType(team, ChessPiece.Piece.valueOf(piece.toLowerCase())).image.getScaledInstance(75, 75, Image.SCALE_REPLICATE)));
				mi.addActionListener(Game.this);
				mi.setActionCommand(piece.toLowerCase());
				popup.add(mi);
			}
			popups.put(team, popup);
		}
		history = new ArrayList<>();

		currSnapshotIndex = -1;
		setupGame();

		timer = new Timer(100, this);
		timer.start();
	}

	public void setupGame() {
		board = new Board(WIDTH, HEIGHT, true);
		for (int i = 0; i < 8; i++) {
			board.set(ChessPieceFactory.getPiece(i, 6, ChessPiece.Team.white, ChessPiece.Piece.pawn, null));
		}
		for (int i = 0; i < 3; i++) {
			board.set(ChessPieceFactory.getPiece(i, 7, ChessPiece.Team.white, ChessPiece.Piece.values()[i], null));
			board.set(ChessPieceFactory.getPiece(7 - i, 7, ChessPiece.Team.white, ChessPiece.Piece.values()[i], null));
		}
		board.set(ChessPieceFactory.getPiece(4, 7, ChessPiece.Team.white, ChessPiece.Piece.king, null));
		board.set(ChessPieceFactory.getPiece(3, 7, ChessPiece.Team.white, ChessPiece.Piece.queen, null));

		for (int i = 0; i < 8; i++) {
			board.set(ChessPieceFactory.getPiece(i, 1, ChessPiece.Team.black, ChessPiece.Piece.pawn, null));
		}
		for (int i = 0; i < 3; i++) {
			board.set(ChessPieceFactory.getPiece(i, 0, ChessPiece.Team.black, ChessPiece.Piece.values()[i], null));
			board.set(ChessPieceFactory.getPiece(7 - i, 0, ChessPiece.Team.black, ChessPiece.Piece.values()[i], null));
		}
		board.set(ChessPieceFactory.getPiece(4, 0, ChessPiece.Team.black, ChessPiece.Piece.king, null));
		board.set(ChessPieceFactory.getPiece(3, 0, ChessPiece.Team.black, ChessPiece.Piece.queen, null));

		snapshot();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == null) {
			return;
		}
		popups.get(board.turn).setVisible(false);
		turnState = null;
		ArrayList<ChessPiece> curr = board.getPieces().get(board.turn);
		var c = ChessPieceFactory.getPiece(focusedLast.getX(), focusedLast.getY(), focusedLast.getType().team, ChessPiece.Piece.valueOf(e.getActionCommand()), curr);
		board.set(c);
		nextTurn();
	}

	private void paintWhiteSide(Graphics2D g2d) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				var piece = board.get(i, j);
				if (piece == null) {
					continue;
				}
				if (board.gameOver != 0) {
					if (piece.getType().piece == ChessPiece.Piece.king) {
						if (piece.getType().team.getValue() == board.gameOver) {
							g2d.setPaint(new Color(200, 50, 50));
						} else if (piece.getType().team.getValue() == board.gameOver / 3) {
							g2d.setPaint(Color.YELLOW);
						}
						g2d.fillRect(oX + piece.getX() * TILE_SIZE, oY + piece.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
					}
				}
				if (piece == focused) {
					continue;
				}
				g2d.drawImage(piece.getType().image, oX + piece.getX() * TILE_SIZE, oY + piece.getY() * TILE_SIZE,
						TILE_SIZE, TILE_SIZE, null);
			}
		}
	}

	private void paintBlackSide(Graphics2D g2d) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				var piece = board.get(i, j);
				if (piece == null) {
					continue;
				}
				if (board.gameOver != 0) {
					if (piece.getType().piece == ChessPiece.Piece.king) {
						if (piece.getType().team.getValue() == board.gameOver) {
							g2d.setPaint(new Color(200, 50, 50));
						} else if (piece.getType().team.getValue() == board.gameOver / 3) {
							g2d.setPaint(Color.YELLOW);
						}
					}
					g2d.fillRect(oX + (7 - piece.getX()) * TILE_SIZE, oY + (7 - piece.getY()) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				if (piece == focused) {
					continue;
				}
				g2d.drawImage(piece.getType().image, oX + (7 - piece.getX()) * TILE_SIZE, oY + (7 - piece.getY()) * TILE_SIZE,
						TILE_SIZE, TILE_SIZE, null);
			}
		}

	}

	private void paintPieces(Graphics2D g2d) {
		if (board.turn == ChessPiece.Team.white) {
			paintWhiteSide(g2d);
		} else {
			paintBlackSide(g2d);
		}
		if (focused != null) {
			g2d.drawImage(focused.getType().image,
					getMousePosition().x - TILE_SIZE / 2, getMousePosition().y - TILE_SIZE / 2,
					TILE_SIZE, TILE_SIZE,null);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		var g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		if (board.gameOver == -2) {
			g2d.setPaint(Color.GRAY);
		} else {
			g2d.setPaint(board.turn == ChessPiece.Team.black ? Color.BLACK : Color.WHITE);
		}
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.drawImage(backgroundImage, oX, oY, TILE_SIZE * WIDTH, TILE_SIZE * WIDTH, null);
		paintPieces(g2d);
	}

	public void restart() {
		setupGame();
		repaint();
	}

	public boolean isGameOver() {
		return board.gameOver != 0;
	}

	public void showPopup() {
		var x = focusedLast.getX() + 1;
		var y = focusedLast.getY();
		if (board.turn == ChessPiece.Team.black) {
			x = 9 - x;
			y = 7 - y;
		}
		popups.get(board.turn).show(Game.this, oX + x * TILE_SIZE, y * TILE_SIZE);
	}

	public void snapshot() {
		currSnapshotIndex++;
		for (int i = currSnapshotIndex; i < history.size(); ) {
			history.removeLast();
		}
		if (currSnapshotIndex > 16) {
			currSnapshotIndex--;
			history.removeFirst();
		}
		history.add(board.makeSnapshot());
	}

	public void nextSnapshot() {
		if (history.isEmpty()) {
			return;
		}
		currSnapshotIndex = Math.min(history.size() - 1, currSnapshotIndex + 1);
		board.restoreSnapshot(history.get(currSnapshotIndex));
		updateForMate(board.turn);
		repaint();
	}

	public void prevSnapshot() {
		if (history.isEmpty()) {
			return;
		}
		currSnapshotIndex = Math.max(0, currSnapshotIndex - 1);
		board.restoreSnapshot(history.get(currSnapshotIndex));
		updateForMate(board.turn);
		repaint();
	}

	public void updateForMate(ChessPiece.Team team) {
		ArrayList<ChessPiece> opponent = board.getPieces().get(team == ChessPiece.Team.white ? ChessPiece.Team.black : ChessPiece.Team.white);
		ArrayList<ChessPiece> allies = board.getPieces().get(team);
		ChessPiece king = allies.stream().filter(p -> p.getType().piece == ChessPiece.Piece.king && p.getType().team == team).findFirst().get();
		board.clearBools();
		opponent.forEach(p -> {
			p.fillAttack(board);
			p.setPins(board);
			p.setPinner(null);
		});
		if (!board.getBool(king.getX(), king.getY())) {
			board.gameOver = 0;
			return;
		}
		board.gameOver = team.getValue() * 3;
		board.clearBools();
		opponent.forEach(p -> p.fillAttack(board));
		king.clearMoves();
		king.getLegalMoves(board);
		System.out.printf("King has %d moves%n", king.getLegalMoves(board).size());
		int movesCount = king.getLegalMoves(board).size();
		for (var ally : allies) {
			if (ally.isPinned() || ally == king) {
				continue;
			}
			ArrayList<Point> newLegalMoves = new ArrayList<>();
			for (var move : ally.getLegalMoves(board)) {
				var exclude = ally.tryMove(move.x, move.y, board);
				board.clearBools();
				opponent.stream().filter(p -> p != exclude).forEach(p -> {
					p.fillAttack(board);
				});
				ally.resetMove(board);
				if (board.getBool(king.getX(), king.getY())) {
					continue;
				}
				newLegalMoves.add(move);
				movesCount += 1;
			}
			ally.setLegalMoves(newLegalMoves);
		}
		board.clearBools();
		opponent.forEach(p -> p.fillAttack(board));
		if (movesCount == 0) {
			board.gameOver = team.getValue();
		}
	}

	public void nextTurn() {
		board.switchTurn();
		var nextPieces = board.getPieces().get(board.turn);
		nextPieces.forEach(ChessPiece::clearMoves);
		updateForMate(board.turn);
		int movesCount = 0;
		for (var p : nextPieces) {
			movesCount += p.getLegalMoves(board).size();
		}
		System.out.printf("Mate: %d || Moves: %d%n", board.gameOver, movesCount);
		movesCount = 0;
		for (var p : nextPieces) {
			if (p.getType().piece == ChessPiece.Piece.king) {
				p.clearMoves();
			}
			movesCount += p.getLegalMoves(board).size();
		}
		if (movesCount == 0 && board.gameOver == 0) {
			board.gameOver = -2;
			System.out.println("STALE MATE");
		}
		snapshot();
		repaint();
	}

	public Point getMousePosOnBoard() {
		var x = (getMousePosition().x - oX) / TILE_SIZE;
		var y = (getMousePosition().y - oY) / TILE_SIZE;
		if (board.turn == ChessPiece.Team.black) {
			x = 7 - x;
			y = 7 - y;
		}
		return new Point(x, y);
	}

	public BufferedImage drawBackground(int TILE_SIZE) {
		BufferedImage bg = new BufferedImage(TILE_SIZE * WIDTH, TILE_SIZE * WIDTH, BufferedImage.TYPE_INT_ARGB);
		var g2d = (Graphics2D)bg.getGraphics();
		g2d.setPaint(new Color(238, 238, 210));
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = y % 2; x < WIDTH; x += 2) {
				g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
			}
		}
		g2d.setPaint(new Color(118, 150, 86));
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = (y + 1) % 2; x < WIDTH; x += 2) {
				g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
			}
		}
		return bg;
	}

	public class onWindowResize extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			int SIZE = Math.min(getWidth(), getHeight());
			TILE_SIZE = SIZE / WIDTH;
			oX = (getWidth() - TILE_SIZE * WIDTH) / 2;
			oY = (getHeight() - TILE_SIZE * WIDTH) / 2;
			if (turnState == TurnState.turningPawn) {
				showPopup();
			}
		}
	}

	public class FrameListener implements AncestorListener {
		@Override
		public void ancestorAdded(AncestorEvent event) {

		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {

		}

		@Override
			public void ancestorMoved(AncestorEvent event) {
				if (turnState == TurnState.turningPawn) {
					showPopup();
				}
			}
	}


	public class MouseListener extends MouseAdapter {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if (turnState == TurnState.movingPiece) {
					if (e.getX() < 0 || e.getX() >= getWidth() || e.getY() < 0 || e.getY() >= getHeight()) {
						focused = null;
						turnState = null;
					}
					repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (turnState == TurnState.turningPawn) {
					if (MouseEvent.BUTTON1 == e.getButton()) {
						var mPos = getMousePosOnBoard();
						if (board.get(mPos.x, mPos.y) == focusedLast) {
							showPopup();
						}
						}
					return;
				}
				if (MouseEvent.BUTTON1 == e.getButton()) {
					var mPos = getMousePosOnBoard();
					focused = board.get(mPos.x, mPos.y);
					if (focused != null && focused.getType().team == board.turn) {
						turnState = TurnState.movingPiece;
					} else {
						focused = null;
					}
				} else if (MouseEvent.BUTTON3 == e.getButton()) {
					focused = null;
					turnState = null;
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (focused == null || turnState == TurnState.turningPawn) {
					return;
				}
				turnState = null;
				var mPos = getMousePosOnBoard();
				var x = mPos.x;
				var y = mPos.y;
				if (focused.getLegalMoves(board).stream().anyMatch(b -> b.getX() == x && b.getY() == y)) {
					var p = focused.move(x, y, board);
					focusedLast = focused;
					if (focused.getType().piece == ChessPiece.Piece.pawn && (y == 0 || y == 7)) {
						turnState = TurnState.turningPawn;
						showPopup();
					} else {
						nextTurn();
					}
				}
				focused = null;
				repaint();
			}
	}
}
