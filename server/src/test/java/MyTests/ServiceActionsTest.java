package MyTests;

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
        AuthData auth = register("alice");

        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void registerRejectsMissingOrBlankFields() {
        assertStatus(400, () ->
                serviceActions.registerUser(null, "password", "a@example.com"));
        assertStatus(400, () ->
                serviceActions.registerUser("alice", " ", "a@example.com"));
        assertStatus(400, () ->
                serviceActions.registerUser("alice", "password", null));
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        register("alice");

        assertStatus(403, () ->
                serviceActions.registerUser("alice", "other-password", "other@example.com"));
    }

    @Test
    void loginAcceptsCorrectPasswordAndCreatesToken() throws Exception {
        register("alice");

        AuthData auth = serviceActions.loginUser("alice", "password");

        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void loginRejectsUnknownUserAndWrongPassword() throws Exception {
        register("alice");

        assertStatus(401, () ->
                serviceActions.loginUser("unknown", "password"));
        assertStatus(401, () ->
                serviceActions.loginUser("alice", "wrong-password"));
    }

    @Test
    void loginRejectsMissingFields() {
        assertStatus(400, () ->
                serviceActions.loginUser(null, "password"));
        assertStatus(400, () ->
                serviceActions.loginUser("alice", ""));
    }

    @Test
    void logoutInvalidatesAuthToken()  throws Exception{
        AuthData auth = register("alice");

        serviceActions.logoutUser(auth.authToken());

        assertStatus(401, () ->
                serviceActions.listGame(auth.authToken()));
    }

    @Test
    void listGamesRequiresAuthAndReturnsCreatedGames() throws Exception {
        AuthData auth = register("alice");

        assertTrue(serviceActions.listGame(auth.authToken()).isEmpty());

        int gameID = serviceActions.createGame(auth.authToken(), "Opening Practice");
        List<GameData> games = serviceActions.listGame(auth.authToken());

        assertEquals(1, games.size());
        assertEquals(gameID, games.get(0).gameID());
        assertEquals("Opening Practice", games.get(0).gameName());
        assertNull(games.get(0).whiteUser());
        assertNull(games.get(0).blackUser());
        assertNotNull(games.get(0).game());
    }

    @Test
    void listGamesRejectsInvalidAuthToken() {
        assertStatus(401, () -> serviceActions.listGame("not-a-token"));
    }

    @Test
    void createGameRejectsMissingNameAndInvalidToken() throws Exception {
        AuthData auth = register("alice");

        assertStatus(400, () ->
                serviceActions.createGame(auth.authToken(), null));
        assertStatus(400, () ->
                serviceActions.createGame(auth.authToken(), " "));
        assertStatus(401, () ->
                serviceActions.createGame("not-a-token", "A Game"));
    }

    @Test
    void joinGameAssignsWhiteAndBlackPlayers() throws Exception {
        AuthData alice = register("alice");
        AuthData bob = register("bob");
        int gameID = serviceActions.createGame(alice.authToken(), "Practice");

        serviceActions.joinGame(
                alice.authToken(), ChessGame.TeamColor.WHITE, gameID);
        serviceActions.joinGame(
                bob.authToken(), ChessGame.TeamColor.BLACK, gameID);

        GameData game = serviceActions.listGame(alice.authToken()).get(0);
        assertEquals("alice", game.whiteUser());
        assertEquals("bob", game.blackUser());
    }

    @Test
    void joinGameRejectsOccupiedColor()  throws Exception{
        AuthData alice = register("alice");
        AuthData bob = register("bob");
        int gameID = serviceActions.createGame(alice.authToken(), "Practice");

        serviceActions.joinGame(
                alice.authToken(), ChessGame.TeamColor.WHITE, gameID);

        assertStatus(403, () ->
                serviceActions.joinGame(
                        bob.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    void joinGameRejectsInvalidRequestOrToken() throws Exception {
        AuthData alice = register("alice");
        int gameID = serviceActions.createGame(alice.authToken(), "Practice");

        assertStatus(400, () ->
                serviceActions.joinGame(
                        alice.authToken(), null, gameID));
        assertStatus(400, () ->
                serviceActions.joinGame(
                        alice.authToken(), ChessGame.TeamColor.WHITE, null));
        assertStatus(400, () ->
                serviceActions.joinGame(
                        alice.authToken(), ChessGame.TeamColor.WHITE, 9999));
        assertStatus(401, () ->
                serviceActions.joinGame(
                        "not-a-token", ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    void resetDBClearsUsersTokensAndGames() throws Exception {
        AuthData oldAuth = register("alice");
        serviceActions.createGame(oldAuth.authToken(), "Practice");

        serviceActions.resetDB();

        assertStatus(401, () ->
                serviceActions.listGame(oldAuth.authToken()));

        // Registering the same username again confirms the user was cleared.
        AuthData newAuth = register("alice");
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