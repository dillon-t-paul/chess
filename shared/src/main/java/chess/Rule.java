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

    public Collection<ChessMove> hashedMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> movements = new HashSet<ChessMove>();

        return movements;
    }

}
