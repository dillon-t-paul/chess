package chess;

import java.util.ArrayList;
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
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.playerTurn = TeamColor.WHITE; // Set starting playetr to white
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
        this.playerTurn = team;
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
        ChessPiece currentPiece = board.getPiece(startPosition);
        if (currentPiece == null || board == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = currentPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            ChessPosition endPosition = move.getEndPosition();
            ChessPiece targetPiece = board.getPiece(endPosition);
            board.rmPiece(startPosition);
            board.addPiece(endPosition, currentPiece);

            if (!isInCheck(currentPiece.getTeamColor())) {
                legalMoves.add(move);
            }
            board.addPiece(startPosition, currentPiece);
            board.addPiece(endPosition, targetPiece);
        }
        return legalMoves;
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
        if (board == null || startPosition == null || targetPosition == null) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> allowedMoves = validMoves(startPosition);
        if (allowedMoves == null || !allowedMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        ChessPiece currentPiece = board.getPiece(startPosition);
        TeamColor currentColor = currentPiece.getTeamColor();
        if (currentColor != getTeamTurn()) {
            throw new InvalidMoveException();
        }

        if (move.getPromotionPiece() != null) {
            ChessPiece promotedPiece = new ChessPiece(currentColor, move.getPromotionPiece());
            board.rmPiece(startPosition);
            board.addPiece(targetPosition, promotedPiece);
        } else {
            board.rmPiece(startPosition);
            board.addPiece(targetPosition, currentPiece);
        }

        if (currentColor == TeamColor.WHITE) { setTeamTurn(TeamColor.BLACK);
        } else { setTeamTurn(TeamColor.WHITE); }
    }

    private ChessPosition kingPosition(TeamColor teamColor) {
        for (int row=1; row<=8; row ++) {
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);

                if (currentPiece != null && currentPiece.getTeamColor()==teamColor && currentPiece.getPieceType()== ChessPiece.PieceType.KING) {
                    return currentPosition;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingsPosition = kingPosition(teamColor);
        for (int row=1; row<=8; row++){
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);

                if (currentPiece != null && currentPiece.getTeamColor()!=teamColor) {
                    Collection<ChessMove> possibleMoves = currentPiece.pieceMoves(board, currentPosition);
                    for (ChessMove move : possibleMoves) {
                        if (move.getEndPosition().equals(kingsPosition)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        for (int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                Collection<ChessMove> validMovements = validMoves(currentPosition);
                if (board.getPiece(currentPosition)!=null && board.getPiece(currentPosition).getTeamColor()==teamColor) {
                    if (!validMovements.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
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
