package client;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.sql.SQLDataAccess;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import server.Server;

import java.util.List;


public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;
    private static SQLDataAccess dataAccess;

    @BeforeAll
    public static void init() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        server = new Server(dataAccess);
        var port = server.run(0);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    void clearData() throws DataAccessException {
        dataAccess.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void registerUser() throws Exception {
        AuthData auth = facade.register("Billy", "1234", "billy@email.com");
        assertEquals("Billy", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
        UserData storedUser = dataAccess.getUserData("Billy");
        assertNotNull(storedUser);
        assertNotEquals("1234", storedUser.password());
        assertEquals(auth, dataAccess.getAuthData(auth.authToken()));
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.register("player1", "another-password", "other@email.com")
        );
        assertEquals(403, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("taken"));
    }

    @Test
    void registerRejectsMissingRequiredField() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.register("player1", "password", "")
        );
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void loginReturnsNewAuthDataForValidCredentials() throws Exception {
        AuthData registration = facade.register("player1", "password", "p1@example.com");
        AuthData login = facade.login("player1", "password");
        assertEquals("player1", login.username());
        assertNotNull(login.authToken());
        assertFalse(login.authToken().isBlank());
        assertNotEquals(registration.authToken(), login.authToken());
        assertEquals(login, dataAccess.getAuthData(login.authToken()));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        facade.register("player1", "password", "p1@example.com");
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.login("player1", "wrong-password")
        );
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void loginRejectsUnknownUsername() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.login("unknown", "password")
        );
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void loginRejectsMissingCredentials() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.login("", "")
        );
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void logoutRemovesAuthToken() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        assertNotNull(dataAccess.getAuthData(auth.authToken()));
        facade.logout(auth.authToken());
        assertNull(dataAccess.getAuthData(auth.authToken()));
    }

    @Test
    void logoutRejectsInvalidAuthToken() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.logout("invalid-token")
        );
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void listGamesReturnsEmptyListWhenThereAreNoGames() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        List<GameData> games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void listGamesReturnsStoredGames() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        int gameID = dataAccess.createGame(new GameData(null, null, null, "First game", new ChessGame()));
        List<GameData> games = facade.listGames(auth.authToken());
        assertEquals(1, games.size());
        assertEquals(gameID, games.get(0).gameID());
        assertEquals("First game", games.get(0).gameName());
        assertNotNull(games.get(0).game());
    }

    @Test
    void listGamesRejectsInvalidAuthToken() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.listGames("invalid-token")
        );
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void createGameStoresGameAndReturnsItsID() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        int gameID = facade.createGame(auth.authToken(), "Test game");
        assertTrue(gameID > 0);
        GameData storedGame = dataAccess.getGameData(gameID);
        assertNotNull(storedGame);
        assertEquals("Test game", storedGame.gameName());
        assertNotNull(storedGame.game());
    }

    @Test
    void createGameRejectsInvalidAuthToken() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.createGame("invalid-token", "Test game")
        );
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void createGameRejectsBlankName() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.createGame(auth.authToken(), "")
        );
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void joinGameAssignsPlayerToRequestedColor() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        int gameID = facade.createGame(auth.authToken(), "Test game");
        facade.joinGame(auth.authToken(), ChessGame.TeamColor.WHITE, gameID);
        GameData game = dataAccess.getGameData(gameID);
        assertNotNull(game);
        assertEquals("player1", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinGameRejectsInvalidAuthToken() {
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.joinGame("invalid-token", ChessGame.TeamColor.WHITE, 1));
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void joinGameRejectsUnknownGameID() throws Exception {
        AuthData auth = facade.register("player1", "password", "p1@example.com");
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.joinGame(auth.authToken(), ChessGame.TeamColor.WHITE, 99999)
        );
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void joinGameRejectsOccupiedColor() throws Exception {
        AuthData firstPlayer = facade.register("player1", "password", "p1@example.com");
        AuthData secondPlayer = facade.register("player2", "password", "p2@example.com");
        int gameID = facade.createGame(firstPlayer.authToken(), "Test game");
        facade.joinGame(firstPlayer.authToken(), ChessGame.TeamColor.WHITE, gameID);
        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.joinGame(secondPlayer.authToken(), ChessGame.TeamColor.WHITE, gameID                )
        );
        assertEquals(403, exception.getStatusCode());
    }
}
