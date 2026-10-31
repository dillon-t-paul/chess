package services;

import chess.ChessGame;
import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;
import dataaccess.memoryAccess;

import java.util.*;

public class ServiceActions {
    private final memoryAccess memory;

    public ServiceActions(memoryAccess memory) {
        this.memory = memory;
    }

    public AuthData registerUser(String username, String password, String email) throws Exception {
        // First check to make sure inputs are correct
        if (emptyOrNull(username) || emptyOrNull(password) || emptyOrNull(email)) {
            throw new ServiceException(400, "Error: bad request");
        }
        // Then make sure username is available
        if (memory.getUserData(username) != null) {
            throw new ServiceException(403, "Error: already taken");
        }
        // then create a new user object and store it in the memory map.
        memory.registerUser(new UserData(username, password, email));
        return createAuth(username);
    }

    public AuthData loginUser(String username, String password) throws Exception {
        // Need to make sure the inputs are valid
        if (emptyOrNull(username) || emptyOrNull(password)) {
            throw new ServiceException(400, "Error: bad request");
        }
        UserData user = memory.getUserData(username);
        // Pull username, then make sure user exists and credentials match
        if (user == null || !user.password().equals(password)) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        // creates authToken for current log in session
        return createAuth(username);
    }

    public void logoutUser(String authToken) throws Exception {
        checkAuth(authToken);
        // First check auth, then remove their auth token, as it is no longer valid post logout.
        memory.removeAuthData(authToken);
    }

    public List<GameData> listGame(String authToken) throws Exception {
        checkAuth(authToken);
        // First check auth, then return an array list of all the values
        return new ArrayList<>(memory.getGamesList());
    }

    public Integer createGame(String authToken, String gameName) throws Exception {
        checkAuth(authToken);
        if (emptyOrNull(gameName)) {
            throw new ServiceException(400, "Error: bad request");
        }
        // Use global incrementor to set game ID
        int gameID = memory.nextGameID();
        // Initialize game, then return just the ID for the json response
        memory.createGame(new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, Integer gameID) throws Exception {
        checkAuth(authToken);
        if (teamColor==null || gameID==null) {
            throw new ServiceException(400, "Error: bad request");
        }
        String username = memory.getAuthData(authToken).username();
        GameData game = memory.getGameData(gameID);
        if (game==null) {
            throw new ServiceException(400, "Error: bad Request");
        }

        // First check which team they want, then check if available. If yes, proceed
        // If no, send to 403 error
        if (teamColor == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            // Add user to the game in the correct spot
            games.put(game.gameID(), new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
        } else {
            if (game.whiteUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            games.put(game.gameID(), new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
        }
    }

    public void resetDB() throws Exception {
//        Three calls: Clear UserData, Clear GameData, Clear AuthData
        memory.resetDB();
    }

    private AuthData createAuth(String username) {
        //Generates a random UUID, then converts it to a string to be used as the authToken
        String authToken = UUID.randomUUID().toString();
        memory.user
        authTokens.put(authToken, username);
        //Stores authToken in memory, then returns an AuthData object with all info
        return new AuthData(username, authToken);
    }

    private void checkAuth(String authToken) throws Exception{
        if (emptyOrNull(authToken)) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (!authTokens.containsKey(authToken)) {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }

    private boolean emptyOrNull (String value) {
        // Check if a value is null first, then if it is blank/empty before handling in methods
        if (value==null || value.isBlank() || value.isEmpty()) {
            return true;
        }
        return false;
    }


    public static class ServiceException extends RuntimeException {
        // Custom exception handler that can be reused a lot in the different methods
        private final int status;

        public ServiceException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }
}
