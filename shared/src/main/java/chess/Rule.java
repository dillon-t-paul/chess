package chess;

import java.util.Collection;
import java.util.HashMap;

public class Rule {
    private void Rule() {

    }

    private boolean validBoardPosition(ChessPosition endPosition) {
        int row = endPosition.getRow();
        int col = endPosition.getColumn();

        if ((row>=1 && row<=8) && (col>=1 && row<=8)) {
            return true;
        } else {return false; }
    }

//    private Collection<ChessPiece> HashedMoves() {
//
//    }

    private void movesHelper(boolean globalMoves, ChessPosition startPosition, int[] movements) {
        ChessPosition endPosition = new ChessPosition(startPosition.getRow()+movements[0], startPosition.getColumn()+movements[1]);

        if (validBoardPosition(endPosition)){
            if Chess
        }

        if (globalMoves) {
            return movesHelper(globalMoves, endPosition, validMoves);
        }
    }
}
