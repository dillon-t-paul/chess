package client;

import chess.ChessGame;
import com.google.gson.*;
import datamodels.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public AuthData register(String username, String password, String email)
            throws IOException, InterruptedException {
        UserData request = new UserData(username, password, email);
        String response = sendRequest("POST", "/user", null, request);
        return gson.fromJson(response, AuthData.class);
    }

    public AuthData login(String username, String password)
            throws IOException, InterruptedException {
        UserData request = new UserData(username, password, null);
        String response = sendRequest("POST", "/session", null, request);
        return gson.fromJson(response, AuthData.class);
    }

    public void logout(String authToken) {

    }

    public List<GameData> listGames(String authToken) {
        return null;

    }

    public int createGame(String authToken, String gameName) {
        return 0;
    }

    public void joinGame(String authToken, ChessGame.TeamColor color, int gameID) {

    }

    private String sendRequest(String method, String path, String authToken, Object requestBody)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + path))
                .header("Content-Type", "application/json");

        if (authToken != null) {
            requestBuilder.header("authorization", authToken);
        }

        HttpRequest.BodyPublisher body = requestBody == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody));

        HttpRequest request = requestBuilder
                .method(method, body)
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 400) {
            throw new ServerException(
                    response.statusCode(),
                    getErrorMessage(response.body())
            );
        }

        return response.body();
    }

    private String getErrorMessage(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (json.has("message")) {
                return json.get("message").getAsString();
            }
        } catch (RuntimeException ignored) {
            // Use a simple fallback if the server response isn't JSON.
        }

        return "The request could not be completed.";
    }

    public static class ServerException extends RuntimeException {
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
