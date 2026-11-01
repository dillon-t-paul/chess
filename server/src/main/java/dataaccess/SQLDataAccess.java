package dataaccess;

import java.sql.*;
import java.util.*;
import com.google.gson.Gson;

import chess.ChessGame;
import datamodels.*;

public class SQLDataAccess implements DataAccess {
    private static final Gson GSON = new Gson();

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
                ON DELETE SET NULL,
            FOREIGN KEY (black_username) REFERENCES users(username)
                ON DELETE SET NULL
            )
            """
    };

    private static final String GAME_COLUMNS =
            "game_id, white_username, black_username, game_name, game_state";

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
            SELECT username, hashed_password, email
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
            statement.setString(1, authToken);
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
    public int createGame(GameData gameData) throws DataAccessException {
        // Add new GameData object (Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) to db
        String sql = """
                INSERT INTO games (white_username, black_username, game_name, game_state)
                VALUES (?, ?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            setNullableString(statement, 1, gameData.whiteUsername());
            setNullableString(statement, 2, gameData.blackUsername());
            statement.setString(3, gameData.gameName());
            statement.setString(4, GSON.toJson(gameData.game()));
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLDataException("Database did not return a game ID");
                }
                return keys.getInt(1);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("unable to create a game", exception);
        }
    }

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        // Query database for GameData using the PK of gameID
        // Return GameData object when found, or null if not found
        String sql = "SELECT " + GAME_COLUMNS + " FROM games WHERE game_id = ?";

        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, gameID);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return readGame(result);
                } else {return null;}
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to retrieve game", exception);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        // Query database for all GameData objects
        // Return a list of GameData objects or null if db is empty
        String sql = "SELECT" + GAME_COLUMNS + "FROM games ORDER BY game_id";
        List<GameData> games = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql);
            var result = statement.executeQuery())
        {
            while (result.next()) {
                games.add(readGame(result));
            }
            return games;
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to list games", exception);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // Query database for GameData based on gameID PK
        // Check if teamColor is available. Throw error if not.
        // Update GameData with user in the teamColor slot
        String sql = """
                UPDATE games
                SET white_username = ?, black_username = ?, game_name = ?, game_state = ?
                WHERE game_id = ?
                """;

        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql))
        {
            setNullableString(statement, 1, game.whiteUsername());
            setNullableString(statement, 2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, GSON.toJson(game.game()));
            statement.setInt(5, game.gameID());

            if (statement.executeUpdate() == 0) {
                throw new DataAccessException("Cannot update game: no game with that ID");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update game", exception);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // Clear all UserData, AuthData, and GameData objects
        try (var connection = DatabaseManager.getConnection();
            var statement = connection.createStatement())
        {
            statement.executeUpdate("DELETE FROM auth_tokens");
            statement.executeUpdate("DELETE FROM games");
            statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("ALTER TABLE games AUTO_INCREMENT = 1");
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to clear database", exception);
        }
    }


    private void setNullableString(java.sql.PreparedStatement statement, int parameter, String value) throws SQLException {
        if (value == null) {
            statement.setNull(parameter, Types.VARCHAR);
        } else {
            statement.setString(parameter, value);
        }
    }

    private GameData readGame(ResultSet result) throws SQLException {
        ChessGame game = GSON.fromJson(result.getString("game_state"), ChessGame.class);

        return new GameData(
                result.getInt("game_id"),
                result.getString("white_username"),
                result.getString("black_username"),
                result.getString("game_name"),
                game
        );
    }
}