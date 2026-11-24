package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.MemoryDataAccess;
import datamodels.AuthData;
import datamodels.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.ServiceActions;
import services.ServiceActions.ServiceException;

import static org.junit.jupiter.api.Assertions.*;

class GameplayServiceTest {
    private MemoryDataAccess dataAccess;
    private ServiceActions service;
    private AuthData white;
    private AuthData black;
    private AuthData observer;
    private int gameID;

    @BeforeEach
    void setUp() throws Exception {
        dataAccess = new MemoryDataAccess();
        service = new ServiceActions(dataAccess);
        white = register("white");
        black = register("black");
        observer = register("observer");
        gameID = service.createGame(white.authToken(), "test game");
        service.joinGame(white.authToken(), ChessGame.TeamColor.WHITE, gameID);
        service.joinGame(black.authToken(), ChessGame.TeamColor.BLACK, gameID);
    }

    @Test
    void gameContextIdentifiesTheAuthenticatedUserAndGame() throws Exception {
        ServiceActions.GameContext context =
                service.getGameContext(white.authToken(), gameID);

        assertEquals("white", context.username());
        assertEquals(gameID, context.game().gameID());
    }

    @Test
    void makeMoveUpdatesTheStoredGameAndAdvancesTheTurn() throws Exception {
        ChessMove move = move(2, 5, 4, 5);

        ServiceActions.MoveResult result =
                service.makeMove(white.authToken(), gameID, move);

        assertEquals(ChessGame.TeamColor.BLACK, result.game().game().getTeamTurn());
        assertNull(result.game().game().getBoard().getPiece(new ChessPosition(2, 5)));
        assertNotNull(result.game().game().getBoard().getPiece(new ChessPosition(4, 5)));
        assertEquals(ChessGame.TeamColor.WHITE, result.game().game().getBoard()
                .getPiece(new ChessPosition(4, 5)).getTeamColor());
        assertFalse(result.inCheck());
        assertFalse(result.checkmate());
        assertFalse(result.stalemate());
        assertEquals(result.game(), dataAccess.getGameData(gameID));
        assertFalse(result.game().game().isGameOver());
    }

    @Test
    void makeMoveRejectsObserversAndPlayersMovingOutOfTurn() {
        assertError(403, () ->
                service.makeMove(observer.authToken(), gameID, move(2, 5, 3, 5)));
        assertError(400, () ->
                service.makeMove(black.authToken(), gameID, move(7, 5, 6, 5)));
    }

    @Test
    void makeMoveRejectsInvalidMoves() {
        assertError(400, () ->
                service.makeMove(white.authToken(), gameID, move(1, 1, 1, 5)));
    }

    @Test
    void resignMarksGameOverAndPreventsFurtherMoves() throws Exception {
        GameData resigned = service.resign(white.authToken(), gameID);

        assertTrue(resigned.game().isGameOver());
        assertTrue(dataAccess.getGameData(gameID).game().isGameOver());
        assertError(400, () ->
                service.makeMove(white.authToken(), gameID, move(2, 5, 3, 5)));
        assertError(400, () ->
                service.resign(black.authToken(), gameID));
    }

    @Test
    void resignRejectsObservers() {
        assertError(403, () -> service.resign(observer.authToken(), gameID));
    }

    @Test
    void leaveClearsAPlayerSlotAndDoesNotChangeTheBoard() throws Exception {
        GameData before = dataAccess.getGameData(gameID);

        GameData after = service.leaveGame(white.authToken(), gameID);

        assertNull(after.whiteUsername());
        assertEquals("black", after.blackUsername());
        assertEquals(before.game(), after.game());
        assertEquals(after, dataAccess.getGameData(gameID));
    }

    @Test
    void invalidTokenAndUnknownGameAreRejected() {
        assertError(401, () ->
                service.getGameContext("bad-token", gameID));
        assertError(400, () ->
                service.getGameContext(white.authToken(), gameID + 1));
    }

    private AuthData register(String username) throws Exception {
        return service.registerUser(
                username,
                "password",
                username + "@example.com"
        );
    }

    private ChessMove move(
            int startRow,
            int startColumn,
            int endRow,
            int endColumn
    ) {
        return new ChessMove(
                new ChessPosition(startRow, startColumn),
                new ChessPosition(endRow, endColumn),
                null
        );
    }

    private void assertError(
            int status,
            org.junit.jupiter.api.function.Executable action
    ) {
        ServiceException exception =
                assertThrows(ServiceException.class, action);

        assertEquals(status, exception.status());
        assertTrue(exception.getMessage().toLowerCase().contains("error"));
    }
}
