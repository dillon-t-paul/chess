package dataaccess;

import chess.ChessGame;
import datamodels.*;

public class sqlAccess {

    public void createUser(UserData userData) {
        // Register UserData (String username, String password, String email) in database
    }

    public UserData getUserData(String username) {
        // Query the database for UserData using PK (username)
        // return UserData object or null if user doesn't exist
    }

    public void createAuth(AuthData authData) {
        // register AuthData (String username, String authToken) in database
    }

    public AuthData checkAuth(String authToken) {
        // Query the database for AuthData using PK authToken
        // Return the AuthData object, or null if it is invalid
    }

    public void revokeAuth(String authToken) {
        // Query database for AuthData object using authToken.
        // Delete it
    }

    public GameData listGames() {
        // Query database for all GameData objects
        // Return a list of GameData objects or null if db is empty
    }

    public GameData getGame(Integer gameID) {
        // Query database for GameData using the PK of gameID
        // Return GameData object when found, or null if not found
    }

    public void createGame(GameData gameData) {
        // Add new GameData object (Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) to db
    }

    public void joinGame(ChessGame.TeamColor teamColor, Integer gameID) {
        // Query database for GameData based on gameID PK
        // Check if teamColor is available. Throw error if not.
        // Update GameData with user in the teamColor slot
    }

    public void clearDatabase() {
        // Clear all UserData, AuthData, and GameData objects
    }

    public void verifyClearedDB() {
        // Query db for UserData, AuthData, and GameData.
        // Verify all queries return null
    }
}