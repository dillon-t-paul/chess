package dataaccess;

import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameID = new AtomicInteger(1);

    @Override
    public void registerUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUserData(String username) {
        return users.get(username);
    }

    @Override
    public void createAuthData(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void removeAuthData(String authToken) {
        authTokens.remove(authToken);
    }

    @Override
    public void createGame(GameData gameData) {
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public GameData getGameData(int gameID) {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public int nextGameID() {
        return nextGameID.getAndIncrement();
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID.set(1);
    }
}
