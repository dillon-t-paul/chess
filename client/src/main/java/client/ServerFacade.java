package client;

import chess.ChessGame;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import datamodels.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.Map;

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

    public void logout(String authToken) throws IOException, InterruptedException {
        sendRequest("DELETE", "/session", authToken, null);
    }

    public List<GameData> listGames(String authToken)
            throws IOException, InterruptedException{
        String response = sendRequest("GET", "/game", authToken, null);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        Type gameListType = new TypeToken<List<GameData>>() {}.getType();
        return gson.fromJson(json.get("games"), gameListType);

    }

    public int createGame(String authToken, String gameName)
            throws IOException, InterruptedException {
        String response = sendRequest("POST", "/game", authToken, Map.of("gameName", gameName));
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.get("gameID").getAsInt();
    }

    public void joinGame(String authToken, ChessGame.TeamColor color, int gameID)
            throws IOException, InterruptedException {
        JoinData request = new JoinData(color, gameID);
        sendRequest("PUT", "/game", authToken, request);
    }

    private String sendRequest(String method, String path, String authToken, Object requestBody)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + path))
                .header("Content-Type", "application/json");

        if (authToken != null) {
            requestBuilder.header("authorization", authToken);
        }

        HttpRequest.BodyPublisher body;
        if (requestBody == null) {
            body = HttpRequest.BodyPublishers.noBody();
        } else {
            body = HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody));
        }

        HttpRequest request = requestBuilder.method(method, body).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new ServerException(response.statusCode(),getErrorMessage(response.body()));
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
            // Just a simple fallback if the server response isn't JSON
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
