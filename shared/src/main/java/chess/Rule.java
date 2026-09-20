package chess;

import java.util.Collection;
import java.util.HashSet;

public class Rule {
    private final boolean canMove;
    private final ChessPosition myPosition;
    private final int[][] movements;

    public Rule(boolean canMove, ChessPosition myPosition, int[][] movements) {
        this.canMove = canMove;
        this.myPosition = myPosition;
        this.movements = movements;
    }

    private boolean validBoardPosition(ChessPosition myPosition) {
        // Currently only checks if its on the board. Make sure it checks if there is a piece there/in the way. Maybe in another function?
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        if (row>=1 && row<=8){
            if (column>=1 && column<=8) {
                return true;
            }
        }
        return false;
    }

    private void movesHelper(Collection<ChessMove> validMovements, ChessBoard board, ChessPosition currentPosition, int[] posChange) {
        ChessPosition endPosition = new ChessPosition(currentPosition.getRow() + posChange[0], currentPosition.getColumn() + posChange[1]);
        if (!validBoardPosition(endPosition)) {
            return;
        }

        ChessPiece movingPiece = board.getPiece(myPosition);
        ChessPiece occupyingPiece = board.getPiece(endPosition);

        if (occupyingPiece != null) {
            if (occupyingPiece.getTeamColor() != movingPiece.getTeamColor()) {
                validMovements.add(new ChessMove(myPosition, endPosition, null));
            }
            return;
        }

        validMovements.add(new ChessMove(myPosition, endPosition, null));

        if (canMove) {
            movesHelper(validMovements, board, endPosition, posChange);
        }
    }


    public Collection<ChessMove> hashedMoves(ChessBoard board) {
        Collection<ChessMove> validMovements = new HashSet<ChessMove>();

        if (board.getPiece(myPosition).getPieceType() == ChessPiece.PieceType.PAWN) {
            pawnMoves(validMovements, board);
            return validMovements;
        }

        for (int[] movement:movements) {
            movesHelper(validMovements, board, myPosition, movement);
        }

        return validMovements;
    }


    private void pawnMoves(Collection<ChessMove> validMoves, ChessBoard board) {
        ChessPiece pawn = board.getPiece(myPosition);
        ChessGame.TeamColor color = pawn.getTeamColor();
        int rowChange; int startingRow;
        if (color == ChessGame.TeamColor.WHITE) {
            rowChange = 1; startingRow = 2;
        } else {
            rowChange = -1; startingRow = 7;
        }

        int nextRow = myPosition.getRow() + rowChange;
        int column = myPosition.getColumn();

        if (validBoardPosition(new ChessPosition(nextRow, column))) {
            ChessPosition forward = new ChessPosition(nextRow, column);
            if (board.getPiece(forward) == null) { //essentially, space is opem
                addPawnMove(validMoves, myPosition, forward);

                if (myPosition.getRow() == startingRow) {
                    int twoRows = myPosition.getRow() + (2 * rowChange);
                    ChessPosition doubleForward = new ChessPosition(twoRows, column);
                    if (validBoardPosition(doubleForward) == true && board.getPiece(doubleForward) == null) {
                        validMoves.add(new ChessMove(myPosition, doubleForward, null));
                    }
                }
            }
        }

        for (int columnChange : new int[]{-1, 1}) { //capture up and over only
            int captureColumn = column + columnChange;
            ChessPosition capturePosition = new ChessPosition(nextRow, captureColumn);

            if (!validBoardPosition(capturePosition)) {
                continue;
            }

            ChessPiece target = board.getPiece(capturePosition);
            if (target != null && target.getTeamColor() != color) {
                addPawnMove(validMoves, myPosition, capturePosition);
            }
        }
    }

    private void addPawnMove(Collection<ChessMove> validMoves, ChessPosition startPosition, ChessPosition endPosition
    ) {
        if (endPosition.getRow() == 1 || endPosition.getRow() == 8) {
            validMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
            validMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
            validMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
        } else {
            validMoves.add(new ChessMove(startPosition, endPosition, null));
        }
    }
}
