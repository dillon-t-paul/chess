package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

    private void movesHelper(Collection<ChessMove> validMovements, ChessBoard board, ChessPosition startPosition, int[]direction) {
        ChessPosition endPosition = new ChessPosition(startPosition.getRow() + direction[0], startPosition.getColumn() + direction[1]);
        boolean valid = validBoardPosition(endPosition);
        if (!valid) {
            return;
        }

        validMovements.add(new ChessMove(myPosition, endPosition, null));

        if (canMove) {
            movesHelper(validMovements, board, endPosition, direction);
        }
    }

//    private void validMoves(ChessBoard board, ChessPosition startPoint, Collection<ChessMove> valMov) {
//
//    }


    public Collection<ChessMove> hashedMoves(ChessBoard board) {
        Collection<ChessMove> validMovements = new HashSet<ChessMove>();
        for (int[] movement:movements) {
            movesHelper(validMovements, board, myPosition, movement);
        }
        return validMovements;
    }

    private void makeMovement (ChessBoard board, ChessPosition startPoint, ChessPosition endPoint, List<ChessMove> movements) {

    }
}
