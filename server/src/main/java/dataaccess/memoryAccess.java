package dataaccess;

import chess.ChessGame;
import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class memoryAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameID = new AtomicInteger(1);

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

    public void createGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
    }

    public GameData getGameData(Integer gameID) {
        return games.get(gameID);
    }

    public List<GameData> getGamesList() {
        return new ArrayList<>(games.values());
    }

    public int nextGameID() {
        return nextGameID.getAndIncrement();
    }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public void resetDB() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID.set(1);
    }
}
