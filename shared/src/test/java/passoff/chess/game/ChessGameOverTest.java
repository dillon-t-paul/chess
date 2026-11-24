package passoff.chess.game;

import chess.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChessGameOverTest {
    @Test
    void newGameStartsNotOver() {
        ChessGame game = new ChessGame();
        assertFalse(game.isGameOver());
    }

    @Test
    void gameOverPreventsMoves() {
        ChessGame game = new ChessGame();
        ChessPosition start = new ChessPosition(2, 5);
        ChessMove move = new ChessMove(start, new ChessPosition(4,5), null);
        game.setGameOver(true);

        assertThrows(InvalidMoveException.class, () -> game.makeMove(move));
        assertNotNull(game.getBoard().getPiece(start));
        assertTrue(game.isGameOver());
    }
}
