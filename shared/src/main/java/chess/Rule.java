package chess;

public class ChessPiece {
    public collection<ChessMove> pieceMoves(Board, Position) {
        return rules.pieceRule(this.type).moves(board, position);
    }
}

public class Rule {
    private final boolean b;
    private final ChessPosition myPosition;
    private final int[][] ints;

    public Rule(boolean b, ChessPosition myPosition, int[][] ints) {
        this.b = b;
        this.myPosition = myPosition;
        this.ints = ints;
    }

}
