package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
    }



    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */


    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;

    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];

    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i=0; i<=1; i++) {
            ChessGame.TeamColor color; int currentRow;
            if (i == 0) { color = ChessGame.TeamColor.WHITE; currentRow = 1;
            } else { color = ChessGame.TeamColor.BLACK; currentRow = 8; }
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.KING));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.QUEEN));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(currentRow, 1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
        }
        for (int j=1; j<=8; j++) {
            addPiece(new ChessPosition(2, j), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, j), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));

        }
    }
}
