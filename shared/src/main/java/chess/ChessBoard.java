package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] myChessBoard = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        myChessBoard[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return myChessBoard[position.getRow()-1][position.getColumn()-1];
    }


    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        myChessBoard = new ChessPiece[8][8];

        for (int i=0; i<=1; i++) {
            ChessGame.TeamColor color; int currentRow;
            if (i==0) { color = ChessGame.TeamColor.WHITE; currentRow = 1;}
            else { color = ChessGame.TeamColor.BLACK; currentRow = 8;}
            addPiece(new ChessPosition(currentRow,1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
            addPiece(new ChessPosition(currentRow,2), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(currentRow,3), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(currentRow,4), new ChessPiece(color, ChessPiece.PieceType.QUEEN));
            addPiece(new ChessPosition(currentRow,5), new ChessPiece(color, ChessPiece.PieceType.KING));
            addPiece(new ChessPosition(currentRow,6), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(currentRow,7), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(currentRow,8), new ChessPiece(color, ChessPiece.PieceType.ROOK));
        }

        for (int i=1; i<=8; i++) {
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    @Override
    public String toString() {
        return Arrays.deepToString(myChessBoard);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that=(ChessBoard) o;
        return Objects.deepEquals(myChessBoard, that.myChessBoard);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(myChessBoard);
    }
}
