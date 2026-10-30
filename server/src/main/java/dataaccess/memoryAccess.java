package dataaccess;

import chess.ChessGame;
import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.HashMap;
import java.util.Map;

public class memoryAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();

    public void registerUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    public UserData getUserData(String username) {
        return users.get(username);
    }

    public void addAuthData(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    public AuthData getAuthData(String authToken) {
        return authTokens.get(authToken);
    }

    public void removeAuthData(String authToken) {
        authTokens.remove(authToken);
    }

    public int createGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
        return gameData.gameID();
    }

    public void joinGame(Integer gameID, String whiteUsername, String blackUsername, String gameName) {
        games.put(gameID, new GameData(gameID, whiteUsername, blackUsername, gameName, null));
    }

    public void updateGame(Integer gameID, ChessGame game) {
        games.compute(gameID, (k, gameState) -> new GameData(gameID, gameState.whiteUsername(), gameState.blackUsername(), gameState.gameName(), game));
    }
}
