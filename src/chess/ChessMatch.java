package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {
	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();

	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.BRANCO;
		initialSetup();
	}

	public int getTurn() {
		return turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}

	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}

	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturedPiece = makeMove(source, target);
		
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece);
			throw new ChessException("Voc� n�o pode se colocar em check");
		}
		
		check = (testCheck(opponent(currentPlayer))) ? true : false;
		
		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		}else {
			nextTurn();
		}
		
		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece)board.removePiece(source);
		p.increaseMoveCount();
		Piece capturedPiece = board.removePiece(target);
		board.placePiece(p, target);
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		return capturedPiece;
	}
	
	private void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece)board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, source);
		
		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
	}

	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("N�o existe pe�a na posi��o de origem");
		}
		if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
			throw new ChessException("A pe�a escolhida n�o � sua");
		}
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("N�o existe movimentos poss�veis para a pe�a escolhida");
		}
	}

	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("A pe�a escolhida n�o pode se mover para a posi��o de destino");
		}
	}
	
	private Color opponent(Color color) {
		return (color == Color.BRANCO) ? Color.PRETO : Color.BRANCO;
	}
	
	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece)p;
			}
		}
		throw new IllegalStateException("N�o existe rei da cor " + color + " no tabuleiro");
	}
	
	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}
	
	private boolean testCheckMate(Color color) {
		if (!testCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for (Piece p : list) {
			boolean[][] mat = p.possibleMoves();
			for (int i = 0; i < board.getRows(); i++) {
				for (int j = 0; j < board.getColumns(); j++) {
					if (mat[i][j]) {
						Position source = ((ChessPiece)p).getChessPosition().toPosition();
						Position target = new Position(i, j);
						Piece capturedPiece = makeMove(source, target);
						boolean testCheck = testCheck(color);
						undoMove(source, target, capturedPiece);
						if (!testCheck) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}

	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.BRANCO) ? Color.PRETO : Color.BRANCO;
	}

	private void initialSetup() {
		placeNewPiece('A', 1, new Rook(board, Color.BRANCO)); 
		placeNewPiece('A', 2, new Pawn(board, Color.BRANCO));
		placeNewPiece('B', 1, new Knight(board, Color.BRANCO));
		placeNewPiece('B', 2, new Pawn(board, Color.BRANCO));
		placeNewPiece('C', 1, new Bishop(board, Color.BRANCO));
	    placeNewPiece('C', 2, new Pawn(board, Color.BRANCO));
	    placeNewPiece('D', 1, new Queen(board, Color.BRANCO));
	    placeNewPiece('D', 2, new Pawn(board, Color.BRANCO));
	    placeNewPiece('E', 1, new King(board, Color.BRANCO));
	    placeNewPiece('E', 2, new Pawn(board, Color.BRANCO));
		placeNewPiece('F', 1, new Bishop(board, Color.BRANCO));
	    placeNewPiece('F', 2, new Pawn(board, Color.BRANCO));
	    placeNewPiece('G', 1, new Knight(board, Color.BRANCO));
	    placeNewPiece('G', 2, new Pawn(board, Color.BRANCO));
		placeNewPiece('H', 1, new Rook(board, Color.BRANCO));
        placeNewPiece('H', 2, new Pawn(board, Color.BRANCO));

        placeNewPiece('A', 7, new Pawn(board, Color.PRETO));
		placeNewPiece('A', 8, new Rook(board, Color.PRETO));
		placeNewPiece('B', 7, new Pawn(board, Color.PRETO));
		placeNewPiece('B', 8, new Knight(board, Color.PRETO));
		placeNewPiece('C', 7, new Pawn(board, Color.PRETO));
		placeNewPiece('C', 8, new Bishop(board, Color.PRETO));
		placeNewPiece('D', 7, new Pawn(board, Color.PRETO));
		placeNewPiece('D', 8, new Queen(board, Color.PRETO));
		placeNewPiece('E', 7, new Pawn(board, Color.PRETO));
		placeNewPiece('E', 8, new King(board, Color.PRETO));
		placeNewPiece('F', 7, new Pawn(board, Color.PRETO));
		placeNewPiece('F', 8, new Bishop(board, Color.PRETO));
        placeNewPiece('G', 7, new Pawn(board, Color.PRETO));
        placeNewPiece('G', 8, new Knight(board, Color.PRETO));
        placeNewPiece('H', 7, new Pawn(board, Color.PRETO));
        placeNewPiece('H', 8, new Rook(board, Color.PRETO));
	}
}