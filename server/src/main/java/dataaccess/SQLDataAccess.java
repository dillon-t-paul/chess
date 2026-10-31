package dataaccess;

import chess.ChessGame;
import datamodels.*;

public class SQLDataAccess implements DataAccess {


    @Override
    public void createUser(UserData userData) {
        // Register UserData (String username, String password, String email) in database
    }

    @Override
    public UserData getUserData(String username) {
        // Query the database for UserData using PK (username)
        // return UserData object or null if user doesn't exist
    }

    @Override
    public void createAuth(AuthData authData) {
        // register AuthData (String username, String authToken) in database
    }

    @Override
    public AuthData checkAuth(String authToken) {
        // Query the database for AuthData using PK authToken
        // Return the AuthData object, or null if it is invalid
    }

    @Override
    public void revokeAuth(String authToken) {
        // Query database for AuthData object using authToken.
        // Delete it
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