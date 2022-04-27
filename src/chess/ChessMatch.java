package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {
	private Board board;

	public ChessMatch() {
		board = new Board(8, 8);
		initialSetup();
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
		return (ChessPiece)capturedPiece;
	}
	
	private Piece makeMove(Position source, Position target) {
		Piece p = board.removePiece(source);
		Piece capturedPiece = board.removePiece(target);
		board.placePiece(p, target);
		return capturedPiece;
	}
	
	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
		throw new ChessException("Não existe peça na posição de origem");
		}
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("Não existe movimentos possíveis para a peça escolhida");
		}
	}
	
	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("A peça escolhida não pode se mover para a posição de destino");
		}
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
	}

	private void initialSetup() {
		placeNewPiece('C', 1, new Rook(board, Color.BRANCO));
		placeNewPiece('C', 2, new Rook(board, Color.BRANCO));
		placeNewPiece('D', 2, new Rook(board, Color.BRANCO));
		placeNewPiece('E', 2, new Rook(board, Color.BRANCO));
		placeNewPiece('E', 1, new Rook(board, Color.BRANCO));
		placeNewPiece('D', 1, new King(board, Color.BRANCO));

		placeNewPiece('C', 7, new Rook(board, Color.PRETO));
		placeNewPiece('C', 8, new Rook(board, Color.PRETO));
		placeNewPiece('D', 7, new Rook(board, Color.PRETO));
		placeNewPiece('E', 7, new Rook(board, Color.PRETO));
		placeNewPiece('E', 8, new Rook(board, Color.PRETO));
		placeNewPiece('D', 8, new King(board, Color.PRETO));
	}
}