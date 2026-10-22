package server;

import chess.ChessGame;
import chess.ChessPiece;

public class ServiceActions {

    public void registerUser(String username, String password, String email) throws Exception {

    }

    public void loginUser(String username, String password) throws Exception {

    }

    public void logoutUser(String authToken) throws Exception {

    }

    public void listGame(String authToken) throws Exception {

    }

    public void createGame(String authToken, String gameName) throws Exception {

    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, String gameID) throws Exception {

    }

    public void resetDB() throws Exception {
//        Three calls: Clear UserData, Clear GameData, Clear AuthData
    }
}
