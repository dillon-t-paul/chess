package dataaccess.sql;

import chess.ChessGame;
import dataaccess.DataAccessException;
import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLDataAccessTest {
    private SQLDataAccess dataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        dataAccess.clear();
    }

    @Test
    void registerAndRetrieveUser() throws DataAccessException {
        UserData user = new UserData("Alice", "hashed-password", "alice@example.com");

        dataAccess.registerUser(user);

        assertEquals(user, dataAccess.getUserData("Alice"));
        assertNull(dataAccess.getUserData("Missing"));
    }

    @Test
    void duplicateUsernameIsRejected() throws DataAccessException {
        UserData user = new UserData("Alice", "hashed-password", "alice@example.com");
        dataAccess.registerUser(user);

        assertThrows(
                DataAccessException.class,
                () -> dataAccess.registerUser(user)
        );
    }

    @Test
    void createRetrieveAndRemoveAuthToken() throws DataAccessException {
        UserData user = new UserData("Alice", "hashed-password", "alice@example.com");
        dataAccess.registerUser(user);

        AuthData auth = new AuthData("Alice", "token-123");
        dataAccess.createAuthData(auth);

        assertEquals(auth, dataAccess.getAuthData("token-123"));
        assertNull(dataAccess.getAuthData("invalid-token"));

        dataAccess.removeAuthData("token-123");

        assertNull(dataAccess.getAuthData("token-123"));
    }

    @Test
    void duplicateAuthTokenIsRejected() throws DataAccessException {
        dataAccess.registerUser(
                new UserData("Alice", "hashed-password", "alice@example.com")
        );

        AuthData auth = new AuthData("Alice", "token-123");
        dataAccess.createAuthData(auth);

        assertThrows(
                DataAccessException.class,
                () -> dataAccess.createAuthData(auth)
        );
    }

    @Test
    void createRetrieveListAndUpdateGames() throws DataAccessException {
        dataAccess.registerUser(
                new UserData("Alice", "hashed-password", "alice@example.com")
        );
        dataAccess.registerUser(
                new UserData("Bob", "hashed-password", "bob@example.com")
        );

        GameData newGame =
                new GameData(null, null, null, "Practice", new ChessGame());

        int gameID = dataAccess.createGame(newGame);

        GameData storedGame = dataAccess.getGameData(gameID);
        assertNotNull(storedGame);
        assertEquals("Practice", storedGame.gameName());
        assertNull(storedGame.whiteUsername());
        assertNull(storedGame.blackUsername());
        assertNotNull(storedGame.game());

        List<GameData> games = dataAccess.listGames();
        assertEquals(1, games.size());
        assertEquals(gameID, games.get(0).gameID());

        GameData updatedGame = new GameData(
                gameID,
                "Alice",
                "Bob",
                "Practice",
                storedGame.game()
        );
        dataAccess.updateGame(updatedGame);

        GameData retrievedUpdate = dataAccess.getGameData(gameID);
        assertEquals("Alice", retrievedUpdate.whiteUsername());
        assertEquals("Bob", retrievedUpdate.blackUsername());
    }

    @Test
    void missingGameReturnsNullAndCannotBeUpdated() throws DataAccessException {
        assertNull(dataAccess.getGameData(999));

        GameData missingGame =
                new GameData(999, null, null, "Missing", new ChessGame());

        assertThrows(
                DataAccessException.class,
                () -> dataAccess.updateGame(missingGame)
        );
    }

    @Test
    void clearRemovesDataAndResetsGameIDs() throws DataAccessException {
        dataAccess.registerUser(
                new UserData("Alice", "hashed-password", "alice@example.com")
        );

        AuthData auth = new AuthData("Alice", "token-123");
        dataAccess.createAuthData(auth);

        int oldGameID = dataAccess.createGame(
                new GameData(null, null, null, "Practice", new ChessGame())
        );

        dataAccess.clear();

        assertNull(dataAccess.getUserData("Alice"));
        assertNull(dataAccess.getAuthData("token-123"));
        assertNull(dataAccess.getGameData(oldGameID));
        assertTrue(dataAccess.listGames().isEmpty());

        int newGameID = dataAccess.createGame(
                new GameData(null, null, null, "After Clear", new ChessGame())
        );
        assertEquals(1, newGameID);
    }
}
