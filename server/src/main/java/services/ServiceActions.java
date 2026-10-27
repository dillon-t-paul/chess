package services;

import chess.ChessGame;
import datamodels.AuthData;
import datamodels.GameData;
import datamodels.UserData;

import java.util.List;
import java.util.UUID;

public class ServiceActions {

    public AuthData registerUser(String username, String password, String email) throws Exception {
        UserData newUser = new UserData(username, password, email);
        String authToken = createAuth();
        AuthData userAuth = new AuthData(username, authToken);
        return userAuth;
    }

    public AuthData loginUser(String username, String password) throws Exception {
        String authToken = createAuth();
        AuthData userAuth = new AuthData(username, authToken);
        return userAuth;
    }

    public void logoutUser(String authToken) throws Exception {

    }

    public List<GameData> listGame(String authToken) throws Exception {
        List<GameData> games = new List<GameData>;
        return games;
    }

    public Integer createGame(String authToken, String gameName) throws Exception {
        Integer gameID = 10;
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, String gameID) throws Exception {

    }

    public void resetDB() throws Exception {
//        Three calls: Clear UserData, Clear GameData, Clear AuthData
    }

    private String createAuth() {
        return UUID.randomUUID().toString();
    }
}
