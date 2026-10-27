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
        users.put(username, new UserData(username, password, email));
        return createAuth(username);
    }

    public AuthData loginUser(String username, String password) throws Exception {
        UserData user = users.get(username);
        if (user == null) {
            throw new Exception();
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
        int gameID = nextGameID.getAndIncrement();
        games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, Integer gameID) throws Exception {
        checkAuth(authToken);
        String username = authTokens.get(authToken);
        GameData game = games.get(gameID);

        if (teamColor == ChessGame.TeamColor.BLACK) {
            games.put(game.gameID(), new GameData(game.gameID(), game.whiteUser(), username, game.gameName(), game.game()));
        } else {
            games.put(game.gameID(), new GameData(game.gameID(), username, game.blackUser(), game.gameName(), game.game()));
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
        String username = authTokens.get(authToken);
        if (username == null) {
            throw new Exception();
        }
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
