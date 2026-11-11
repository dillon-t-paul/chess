package client;

import chess.ChessGame;
import com.google.gson.*;
import datamodels.*;

import java.net.http.HttpClient;
import java.util.List;

public class ServerFacade {
    private final String serverURL;
    private final HttpClient httpClient;
    private final Gson gson;

    public ServerFacade(int port) {
        this.serverURL = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public AuthData register(String username, String Password, String email) {

    }

    public AuthData login(String username, String password) {

    }

    public void logout(String authToken) {

    }

    public List<GameData> listGames(String authToken) {

    }

    public int createGame(String authToken, String gameName) {

    }

    public void joinGame(String authToken, ChessGame.TeamColor color, int gameID) {

    }

    private static class ServerException extends RuntimeException {
        private final int statusCode;

        public ServerException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
