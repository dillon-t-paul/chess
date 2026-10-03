package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor playerTurn;

    public ChessGame() {
        // Create board for new game, initialize it with reset
        board = new ChessBoard();
        board.resetBoard();
        playerTurn = TeamColor.WHITE; // Set starting playetr to white
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return playerTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        playerTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = getBoard().getPiece(startPosition);
        Collection<ChessMove> possibleMoves =  currentPiece.pieceMoves(board, startPosition);
        return possibleMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition targetPosition = move.getEndPosition();

        if (startPosition == null) {
            throw new InvalidMoveException();
        }
        if (!validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException();
        }

        ChessPiece myPiece = board.getPiece(startPosition);
        TeamColor myColor = myPiece.getTeamColor();
        ChessPiece targetPiece = board.getPiece(targetPosition);

        if (myColor != playerTurn) {
            throw new InvalidMoveException();
        }

        if (targetPiece != null) {
            board.rmPiece(targetPosition);
        }
        board.rmPiece(startPosition);
        board.addPiece(targetPosition, myPiece);

        if (myColor == TeamColor.WHITE) {
            playerTurn = TeamColor.BLACK;
        } else { playerTurn = TeamColor.WHITE; }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return false;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", playerTurn=" + playerTurn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && playerTurn == chessGame.playerTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, playerTurn);
    }
}
