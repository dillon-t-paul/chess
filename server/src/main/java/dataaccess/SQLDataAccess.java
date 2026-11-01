package dataaccess;

import java.sql.SQLException;
import java.sql.Statement;

import chess.ChessGame;
import datamodels.*;

public class SQLDataAccess implements DataAccess {
    private static final String[] CREATE_TABLES = {
            """
            CREATE TABLE IF NOT EXISTS users (
            username VARCHAR(100) PRIMARY KEY,
            hashed_password VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth_tokens (
            auth_token VARCHAR(255) PRIMARY KEY,
            username VARCHAR(100) NOT NULL,
            FOREIGN KEY (username) REFERENCES users(username)
                ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
            game_id INT AUTO_INCREMENT PRIMARY KEY,
            white_username VARCHAR(100) NULL,
            black_username VARCHAR(100) NULL,
            game_name VARCHAR(255) NOT NULL,
            game_state TEXT NOT NULL,
            FOREIGN KEY (white_username) REFERENCES users(username)
                ON DELETE SET NULL
            FOREIGN KEY (black_username) REFERENCES users(username)
                ON DELETE SET NULL
            )
            """
    };

    public SQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        createTables();
    }

    private void createTables() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement())
        {
            for (String sql : CREATE_TABLES) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to create database tables", exception);
        }
    }


    @Override
    public void registerUser(UserData userData) throws DataAccessException {
        // Register UserData (String username, String password, String email) in database
        String sql = """
                INSERT INTO users (username, hashed_password, email)
                VALUES (?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql))
        {
            statement.setString(1, userData.username());
            statement.setString(2, userData.password());
            statement.setString(3, userData.email());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to register user", exception);
        }
    }

    @Override
    public UserData getUserData(String username) throws DataAccessException {
        // Query the database for UserData using PK (username)
        // return UserData object or null if user doesn't exist
        String sql = """
            SELECT username, password_hash, email
            FROM users
            WHERE username = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql))
        {
            statement.setString(1, username);

            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return new UserData(
                        result.getString("username"),
                        result.getString("password_hash"),
                        result.getString("email")
                );
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to retrieve user", exception);
        }
    }

    @Override
    public void createAuthData(AuthData authData) throws DataAccessException {
        // register AuthData (String username, String authToken) in database
        String sql = """
                INSERT INTO auth_tokens (auth_token, username)
                VALUES (?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql))
        {
            statement.setString(1, authData.authToken());
            statement.setString(2, authData.username());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to create auth token", exception);
        }
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        // Query the database for AuthData using PK authToken
        // Return the AuthData object, or null if it is invalid
        String sql = """
                SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?
                """;

        try (var connections = DatabaseManager.getConnection();
            var statement = connections.prepareStatement(sql))
        {
            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return new AuthData(result.getString("username"), result.getString("auth_token"));
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to retrieve auth token", exception);
        }
    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {
        // Query database for AuthData object using authToken.
        // Delete it
        String sql = "DELETE FROM auth_tokens WHERE auth_token = ?";

        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql))
        {
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to remove auth token", exception);
        }
    }

    @Override
    public GameData listGames() {
        // Query database for all GameData objects
        // Return a list of GameData objects or null if db is empty
    }

    @Override
    public GameData getGame(Integer gameID) {
        // Query database for GameData using the PK of gameID
        // Return GameData object when found, or null if not found
    }

    @Override
    public int createGame(GameData gameData) {
        // Add new GameData object (Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) to db
    }

    @Override
    public void joinGame(ChessGame.TeamColor teamColor, Integer gameID) {
        // Query database for GameData based on gameID PK
        // Check if teamColor is available. Throw error if not.
        // Update GameData with user in the teamColor slot
    }

    @Override
    public void clearDatabase() {
        // Clear all UserData, AuthData, and GameData objects
    }

    @Override
    public void verifyClearedDB() {
        // Query db for UserData, AuthData, and GameData.
        // Verify all queries return null
    }
}