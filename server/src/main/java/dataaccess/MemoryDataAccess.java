package dataaccess;

import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new LinkedHashMap<>();
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
    public int createGame(GameData gameData) {
        int gameID = nextGameID.getAndIncrement();
        GameData storedGame = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        games.put(gameID, storedGame);
        return gameID;
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
