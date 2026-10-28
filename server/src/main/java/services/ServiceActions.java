package services;

import chess.ChessGame;
import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceActions {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, String> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameID = new AtomicInteger(1);

    public AuthData registerUser(String username, String password, String email) throws Exception {
        if (emptyOrNull(username) || emptyOrNull(password) || emptyOrNull(email)) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (users.containsKey(username)) {
            throw new ServiceException(403, "Error: already taken");
        }
        users.put(username, new UserData(username, password, email));
        return createAuth(username);
    }

    public AuthData loginUser(String username, String password) throws Exception {
        if (emptyOrNull(username) || emptyOrNull(password)) {
            throw new ServiceException(400, "Error: bad request");
        }
        UserData user = users.get(username);
        if (user == null || !user.password().equals(password)) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return createAuth(username);
    }

    public void logoutUser(String authToken) throws Exception {
        checkAuth(authToken);
        authTokens.remove(authToken);
    }

    public List<GameData> listGame(String authToken) throws Exception {
        checkAuth(authToken);
        return new ArrayList<>(games.values());
    }

    public Integer createGame(String authToken, String gameName) throws Exception {
        checkAuth(authToken);
        if (emptyOrNull(gameName)) {
            throw new ServiceException(400, "Error: bad request");
        }
        int gameID = nextGameID.getAndIncrement();
        games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, Integer gameID) throws Exception {
        checkAuth(authToken);
        if (teamColor==null || gameID==null) {
            throw new ServiceException(400, "Error: bad request");
        }
        String username = authTokens.get(authToken);
        GameData game = games.get(gameID);
        if (game==null) {
            throw new ServiceException(400, "Error: bad Request");
        }

        if (teamColor == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }
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
        users.clear();
        authTokens.clear();
        games.clear();
        // Reset counter for game IDs
        nextGameID.set(1);
    }

    private AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        authTokens.put(authToken, username);
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
        if (value==null || value.isBlank() || value.isEmpty()) {
            return true;
        }
        return false;
    }


    public static class ServiceException extends RuntimeException {
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
