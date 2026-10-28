package service;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    private static final Gson GSON = new Gson();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private Server server;
    private String baseUrl;

    @BeforeEach
    void startServer() {
        server = new Server();
        int port = server.run(0);
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void registerLoginAndLogoutWork() throws Exception {
        HttpResponse<String> registerResponse = send(
                "POST",
                "/user",
                """
                {"username":"Billy","password":"pw","email":"billy@example.com"}
                """,
                null
        );

        assertEquals(200, registerResponse.statusCode());
        JsonObject registered = parseObject(registerResponse.body());
        assertEquals("Billy", registered.get("username").getAsString());
        assertTrue(registered.has("authToken"));

        HttpResponse<String> loginResponse = send(
                "POST",
                "/session",
                """
                {"username":"Billy","password":"pw"}
                """,
                null
        );

        assertEquals(200, loginResponse.statusCode());
        JsonObject loggedIn = parseObject(loginResponse.body());
        String token = loggedIn.get("authToken").getAsString();

        HttpResponse<String> logoutResponse =
                send("DELETE", "/session", null, token);

        assertEquals(200, logoutResponse.statusCode());
    }

    @Test
    void duplicateRegistrationReturnsAnError() throws Exception {
        String requestBody =
                """
                {"username":"Billy","password":"pw","email":"billy@example.com"}
                """;

        HttpResponse<String> firstResponse =
                send("POST", "/user", requestBody, null);
        assertEquals(200, firstResponse.statusCode());

        HttpResponse<String> duplicateResponse =
                send("POST", "/user", requestBody, null);

        assertEquals(403, duplicateResponse.statusCode());
        assertTrue(duplicateResponse.body().contains("Error"));
    }

    @Test
    void createJoinAndListGameWork() throws Exception {
        String billyToken = registerAndGetToken("Billy");
        String bobToken = registerAndGetToken("Bob");

        HttpResponse<String> createResponse = send(
                "POST",
                "/game",
                """
                {"gameName":"Test Game"}
                """,
                billyToken
        );

        assertEquals(200, createResponse.statusCode());
        int gameID = parseObject(createResponse.body())
                .get("gameID")
                .getAsInt();

        HttpResponse<String> joinResponse = send(
                "PUT",
                "/game",
                GSON.toJson(new JoinRequest("BLACK", gameID)),
                bobToken
        );

        assertEquals(200, joinResponse.statusCode());

        HttpResponse<String> listResponse =
                send("GET", "/game", null, billyToken);

        assertEquals(200, listResponse.statusCode());

        JsonArray games = parseObject(listResponse.body())
                .getAsJsonArray("games");

        assertEquals(1, games.size());

        JsonObject game = games.get(0).getAsJsonObject();
        assertEquals(gameID, game.get("gameID").getAsInt());
        assertEquals("Test Game", game.get("gameName").getAsString());
        assertEquals("Bob", game.get("blackUsername").getAsString());
        assertTrue(!game.has("whiteUsername") || game.get("whiteUsername").isJsonNull());
    }

    @Test
    void clearRemovesUsersAndAuthTokens() throws Exception {
        String token = registerAndGetToken("Billy");

        HttpResponse<String> clearResponse =
                send("DELETE", "/db", null, null);

        assertEquals(200, clearResponse.statusCode());

        HttpResponse<String> listResponse =
                send("GET", "/game", null, token);

        assertEquals(401, listResponse.statusCode());

        HttpResponse<String> loginResponse = send(
                "POST",
                "/session",
                """
                {"username":"Billy","password":"pw"}
                """,
                null
        );

        assertEquals(401, loginResponse.statusCode());
    }

    private String registerAndGetToken(String username) throws Exception {
        HttpResponse<String> response = send(
                "POST",
                "/user",
                GSON.toJson(new UserRequest(
                        username,
                        "pw",
                        username + "@example.com"
                )),
                null
        );

        assertEquals(200, response.statusCode());
        return parseObject(response.body())
                .get("authToken")
                .getAsString();
    }

    private HttpResponse<String> send(
            String method,
            String path,
            String body,
            String authToken
    ) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path));

        if (body != null) {
            builder.header("Content-Type", "application/json");
        }

        if (authToken != null) {
            builder.header("authorization", authToken);
        }

        HttpRequest.BodyPublisher bodyPublisher =
                body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body);

        HttpRequest request = builder
                .method(method, bodyPublisher)
                .build();

        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonObject parseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private record UserRequest(String username, String password, String email) {}

    private record JoinRequest(String playerColor, int gameID) {}
}