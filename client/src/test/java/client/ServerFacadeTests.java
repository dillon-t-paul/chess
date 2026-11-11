package client;

import datamodels.*;
import org.junit.jupiter.api.*;
import server.Server;

import dataaccess.sql.SQLDataAccess;
import dataaccess.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static SQLDataAccess dataAccess;

    @BeforeAll
    public static void init() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        server = new Server(dataAccess);
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
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
    public void sampleTest() {
        Assertions.assertTrue(true);
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

        AuthData storedAuth = dataAccess.getAuthData(auth.authToken());
        assertEquals(auth, storedAuth);
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        facade.register("player1", "password", "p1@example.com");

        ServerFacade.ServerException exception = assertThrows(
                ServerFacade.ServerException.class,
                () -> facade.register("player1", "another-password", "other@example.com")
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

}
