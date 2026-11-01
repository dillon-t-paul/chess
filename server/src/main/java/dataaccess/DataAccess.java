package dataaccess;

import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.List;

public interface DataAccess {
    UserData getUserData(String username) throws DataAccessException;
    void registerUser(UserData user) throws DataAccessException;

    AuthData getAuthData(String authToken) throws DataAccessException;
    void createAuthData(AuthData authData) throws DataAccessException;
    void removeAuthData(String authToken) throws DataAccessException;

    GameData getGameData(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    int createGame(GameData game) throws DataAccessException;
    void updateGame(GameData game);

    void clear();
}