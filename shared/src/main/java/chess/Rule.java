package chess;

import java.util.Collection;
import java.util.HashSet;

public class Rule {
    private final boolean b;
    private final ChessPosition myPosition;
    private final int[][] ints;

    public Rule(boolean b, ChessPosition myPosition, int[][] ints) {
        this.b = b;
        this.myPosition = myPosition;
        this.ints = ints;
    }

    public Collection<ChessMove> hashedMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> move = new HashSet<ChessMove>();

        return move;
    }

}
