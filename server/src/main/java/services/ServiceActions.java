package services;

import chess.ChessGame;

import java.util.UUID;

public class ServiceActions {

    public void registerUser(String username, String password, String email) throws Exception {

    }

    public String loginUser(String username, String password) throws Exception {
        String authToken = createAuth(username);
        return authToken;
    }

    public void logoutUser(String authToken) throws Exception {

    }

    public list<GameData> listGame(String authToken) throws Exception {
        list<GameData> games = new list<GameData>;
        return games;
    }

    public Integer createGame(String authToken, String gameName) throws Exception {
        Integer gameID = 10;
        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, String gameID) throws Exception {

    }

    public void resetDB() throws Exception {
//        Three calls: Clear UserData, Clear GameData, Clear AuthData
    }

    private String createAuth(String username) {
        String authToken = generateToken();
        return authToken;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
