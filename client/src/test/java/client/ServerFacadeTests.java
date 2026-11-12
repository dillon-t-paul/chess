package client;

import dataaccess.DataAccessException;
import dataaccess.sql.SQLDataAccess;
import datamodels.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import server.Server;


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
        AuthData registration = facade.register(
                "player1", "password", "p1@example.com"
        );

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
        AuthData auth = facade.register(
                "player1", "password", "p1@example.com"
        );

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

}
