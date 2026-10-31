package dataaccess;

import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.List;

public interface DataAccess {
    UserData getUserData(String username);
    void registerUser(UserData user);

    AuthData getAuthData(String authToken);
    void createAuthData(AuthData authData);
    void removeAuthData(String authToken);

    int nextGameID();
    GameData getGameData(int gameID);
    List<GameData> listGames();
    void createGame(GameData game);
    void updateGame(GameData game);

    void clear();
}