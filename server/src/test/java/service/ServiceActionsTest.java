package service;

import chess.ChessGame;
import datamodels.AuthData;
import datamodels.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import services.ServiceActions;
import services.ServiceActions.ServiceException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceActionsTest {
    private ServiceActions serviceActions;

    @BeforeEach
    void setUp() {
        serviceActions = new ServiceActions();
    }

    @Test
    void registerCreatesUserAndAuthToken() throws Exception {
        AuthData auth = register("Billy");

        assertEquals("Billy", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void registerRejectsMissingOrBlankFields() {
        assertStatus(400, () ->
                serviceActions.registerUser(null, "password", "example@example.com"));
        assertStatus(400, () ->
                serviceActions.registerUser("Billy", " ", "example@example.com"));
        assertStatus(400, () ->
                serviceActions.registerUser("Billy", "password", null));
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        register("Billy");

        assertStatus(403, () ->
                serviceActions.registerUser("Billy", "different-password", "other@example.com"));
    }

    @Test
    void loginAcceptsCorrectPasswordAndCreatesToken() throws Exception {
        register("Billy");

        AuthData auth = serviceActions.loginUser("Billy", "password");

        assertEquals("Billy", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void loginRejectsUnknownUserAndWrongPassword() throws Exception {
        register("Billy");

        assertStatus(401, () ->
                serviceActions.loginUser("unknown", "password"));
        assertStatus(401, () ->
                serviceActions.loginUser("Billy", "wrong-password"));
    }

    @Test
    void loginRejectsMissingFields() {
        assertStatus(400, () ->
                serviceActions.loginUser(null, "password"));
        assertStatus(400, () ->
                serviceActions.loginUser("Billy", ""));
    }

    @Test
    void logoutInvalidatesAuthToken()  throws Exception{
        AuthData auth = register("Billy");

        serviceActions.logoutUser(auth.authToken());

        assertStatus(401, () ->
                serviceActions.listGame(auth.authToken()));
    }

    @Test
    void listGamesRequiresAuthAndReturnsCreatedGames() throws Exception {
        AuthData auth = register("Billy");

        assertTrue(serviceActions.listGame(auth.authToken()).isEmpty());

        int gameID = serviceActions.createGame(auth.authToken(), "Opening Practice");
        List<GameData> games = serviceActions.listGame(auth.authToken());

        assertEquals(1, games.size());
        assertEquals(gameID, games.get(0).gameID());
        assertEquals("Opening Practice", games.get(0).gameName());
        assertNull(games.get(0).whiteUsername());
        assertNull(games.get(0).blackUsername());
        assertNotNull(games.get(0).game());
    }

    @Test
    void listGamesRejectsInvalidAuthToken() {
        assertStatus(401, () -> serviceActions.listGame("not-a-token"));
    }

    @Test
    void createGameRejectsMissingNameAndInvalidToken() throws Exception {
        AuthData auth = register("Billy");

        assertStatus(400, () ->
                serviceActions.createGame(auth.authToken(), null));
        assertStatus(400, () ->
                serviceActions.createGame(auth.authToken(), " "));
        assertStatus(401, () ->
                serviceActions.createGame("not-a-token", "A Game"));
    }

    @Test
    void joinGameAssignsWhiteAndBlackPlayers() throws Exception {
        AuthData billy = register("Billy");
        AuthData bob = register("Bob");
        int gameID = serviceActions.createGame(billy.authToken(), "Practice");

        serviceActions.joinGame(
                billy.authToken(), ChessGame.TeamColor.WHITE, gameID);
        serviceActions.joinGame(
                bob.authToken(), ChessGame.TeamColor.BLACK, gameID);

        GameData game = serviceActions.listGame(billy.authToken()).get(0);
        assertEquals("Billy", game.whiteUsername());
        assertEquals("Bob", game.blackUsername());
    }

    @Test
    void joinGameRejectsOccupiedColor()  throws Exception{
        AuthData billy = register("Billy");
        AuthData bob = register("Bob");
        int gameID = serviceActions.createGame(billy.authToken(), "Practice");

        serviceActions.joinGame(
                billy.authToken(), ChessGame.TeamColor.WHITE, gameID);

        assertStatus(403, () ->
                serviceActions.joinGame(
                        bob.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    void joinGameRejectsInvalidRequestOrToken() throws Exception {
        AuthData billy = register("billy");
        int gameID = serviceActions.createGame(billy.authToken(), "Practice");

        assertStatus(400, () ->
                serviceActions.joinGame(
                        billy.authToken(), null, gameID));
        assertStatus(400, () ->
                serviceActions.joinGame(
                        billy.authToken(), ChessGame.TeamColor.WHITE, null));
        assertStatus(400, () ->
                serviceActions.joinGame(
                        billy.authToken(), ChessGame.TeamColor.WHITE, 9999));
        assertStatus(401, () ->
                serviceActions.joinGame(
                        "not-a-token", ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    void resetDBClearsUsersTokensAndGames() throws Exception {
        AuthData oldAuth = register("Billy");
        serviceActions.createGame(oldAuth.authToken(), "Practice");

        serviceActions.resetDB();

        assertStatus(401, () ->
                serviceActions.listGame(oldAuth.authToken()));

        // Registering the same username again confirms the user was cleared.
        AuthData newAuth = register("Billy");
        assertTrue(serviceActions.listGame(newAuth.authToken()).isEmpty());
    }

    private AuthData register(String username) throws Exception {
        return serviceActions.registerUser(
                username,
                "password",
                username + "@example.com");
    }

    private void assertStatus(int expectedStatus, Executable action) {
        ServiceException exception =
                assertThrows(ServiceException.class, action);
        assertEquals(expectedStatus, exception.status());
    }
}