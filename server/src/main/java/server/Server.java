package server;

import io.javalin.*;
import io.javalin.http.Context;

import java.util.*;
import com.google.gson.Gson;

import chess.ChessGame;
import datamodels.*;
import services.ServiceActions;

public class Server {
    private static final Gson GSON = new Gson();

    private final Javalin javalin;
    private final ServiceActions serviceActions;

    public Server() {

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        serviceActions = new ServiceActions();

        // Register your endpoints and exception handlers here.
        server.delete("/db", this::clear);
        server.post("/user", this::register);
        server.post("/session", this::login);
        server.delete("/session", this::logout);
        server.get("/game", this::listGames);
        server.post("/game", this::createGame);
        server.put("/game", this::joinGame);
    }

    private void clear(Context context) {
        serviceActions.resetDB();
        context.result("{}");
    }

    private void register(Context context) {
        String reqJson=context.body();
        var user=GSON.fromJson(reqJson, UserData.class);

        // call to the service and register
        var authData=serviceActions.registerUser(user.username(), user.password(), user.email());
        context.result(GSON.toJson(authData));
    }

    private void login(Context context) {
        String reqJson = context.body();
        var user = GSON.fromJson(reqJson, UserData.class);

        var authData = serviceActions.loginUser(user.username(), user.password());
        context.result(GSON.toJson(authData));
    }

    private void logout(Context context) {
        var data = context.header("authorization");

        serviceActions.logoutUser(data);
        context.status(200).result("{}");
    }

    private void listGames(Context context) {
        var data = context.header("authorization");

        List<GameData> gameList = serviceActions.listGame(data);
        var returnData = String.format("{ \"games\": %s }", GSON.toJson(gameList));
        context.result(returnData);
    }

    private void createGame(Context context) {
        var header = context.header("authorization");
        String reqJson = context.body();
        var data = GSON.fromJson(reqJson, GameData.class);

        int gameID = serviceActions.createGame(header, data.gameName());
        var returnData = String.format("{\"gameID\": %d }", gameID);
        context.result(returnData);
    }

    private void joinGame(Context context) {
        var header = context.header("authorization");
        String reqJson = context.body();
        var data = GSON.fromJson(reqJson, JoinData.class);

        serviceActions.joinGame(header, data.playerColor(), data.gameID());
        context.result("{}");
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
