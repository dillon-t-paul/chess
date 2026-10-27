package server;

import com.google.gson.JsonParseException;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.*;
import com.google.gson.Gson;

import chess.ChessGame;
import datamodels.*;
import services.ServiceActions;
import services.ServiceActions.ServiceException;

public class Server {
    private static final Gson GSON = new Gson();

    private final Javalin javalin;
    private final ServiceActions serviceActions;

    public Server() {

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        serviceActions = new ServiceActions();

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
    }

    private void clear(Context context) {
        try {
            serviceActions.resetDB();
            sendJson(context, 200, "{}");
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void register(Context context) {
        // call to the service and register
        try {
            UserData user = GSON.fromJson(context.body(), UserData.class);
            if (user == null) {
                throw new ServiceException(400, "Error: bad request");
            }
            var authData = serviceActions.registerUser(user.username(), user.password(), request.email());
            sendJson(context, 200, GSON.toJson(authData));
        } catch (ServiceException exception) {
            sendError(context, exception.status(), exception.getMessage());
        } catch (JsonParseException | IllegalStateException exception) {
            sendError(context, 400, "Error: bad request");
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void login(Context context) {
        try {
            UserData user = GSON.fromJson(context.body(), UserData.class);
            if (user == null) {
                throw new ServiceException(400, "Error: bad request");
            }
            var authData = serviceActions.loginUser(user.username(), user.password());
            sendJson(context, 200, GSON.toJson(authData));
        } catch (ServiceException exception) {
            sendError(context, exception.status(), exception.getMessage());
        } catch (JsonParseException | IllegalStateException exception) {
            sendError(context, 400, "Error: bad request");
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void logout(Context context) {
        try {
            String authToken = context.header("authorization");
            serviceActions.logoutUser(authToken);
            sendJson(context, 200, "{}");
        } catch (ServiceException exception) {
            sendError(context, exception.status(), exception.getMessage());
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void listGames(Context context) {
        try {
            String authToken = context.header("authorization");
            List<GameData> gameList = serviceActions.listGame(authToken);

            var returnData = String.format("{ \"games\": %s }", GSON.toJson(gameList));
            sendJson(context, 200, returnData);

        } catch (ServiceException exception) {
            sendError(context, exception.status(), exception.getMessage());
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void createGame(Context context) {
        var authToken = context.header("authorization");
        String reqJson = context.body();
        var data = GSON.fromJson(reqJson, GameData.class);
        try {
            int gameID = serviceActions.createGame(authToken, data.gameName());
            var gameData = String.format("{\"gameID\": %d }", gameID);
            sendJson(context, 200, gameData);

        } catch (ServiceException exception) {
            sendError(context, exception.status(), exception.getMessage());
        } catch (JsonParseException | IllegalStateException exception) {
            sendError(context, 400, "Error: bad request");
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void joinGame(Context context) {
        var authToken = context.header("authorization");
        String reqJson = context.body();
        var data = GSON.fromJson(reqJson, JoinData.class);
        try {
            serviceActions.joinGame(authToken, data.playerColor(), data.gameID());
            sendJson(context, 200, "{}");

        } catch (ServiceException exception) {
            sendError(context, exception.status(), exception.getMessage());
        } catch (JsonParseException | IllegalStateException exception) {
            sendError(context, 400, "Error: bad request");
        } catch (Exception exception) {
            sendError(context, 500, "Error: internal server error");
        }
    }

    private void sendError(Context context, int status, String message) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", message);
        sendJson(context, status, GSON.toJson(response));
    }

    private void sendJson(Context context, int status, String body) {
        context.status(status).contentType("application/json").result(body);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
