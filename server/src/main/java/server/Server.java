package server;

import io.javalin.*;
import io.javalin.http.Context;

import java.util.*;
import com.google.gson.Gson;

import datamodels.*;
import services.ServiceActions;

public class Server {

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
        context.result("{}");
    }

    private void register(Context context) {
        var serializer=new Gson();
        String reqJson=context.body();
        var user=serializer.fromJson(reqJson, UserData.class);

        // call to the service and register
        var authData=serviceActions.registerUser(user.username(), user.password(), user.email());
        context.result(serializer.toJson(authData));
    }

    private void login(Context context) {
        var serializer = new Gson();
        String reqJson = context.body();
        var user = serializer.fromJson(reqJson, UserData.class);

        var authData = serviceActions.loginUser(user.username(), user.password());
        context.result(serializer.toJson(authData));
    }

    private void logout(Context context) {
        var data = context.header("authorization");

        serviceActions.logoutUser(data);
        context.status(200).result("{}");
    }

    private void listGames(Context context) {
        var serializer = new Gson();
        var data = context.header("authorization");

        List<GameData> gameList = serviceActions.listGame(data);
        var returnData = String.format("{ \"games\": %s }", serializer.toJson(gameList));
        context.result(returnData);
    }

    private void createGame(Context context) {
    var serializer = new Gson();
        var header = context.header("authorization");
        String reqJson = context.body();
        var data = serializer.fromJson(reqJson, GameData.class);

        int gameID = serviceActions.createGame(header, data);
        var returnData = String.format("{\"gameID\": %d }", gameID);
        context.result(returnData);
    }

    private void joinGame(Context context) {
        var serializer = new Gson();
        var header = context.header("authorization");
        String reqJson = context.body();
        var data = serializer.fromJson(reqJson, JoinData.class);

        serviceActions.joinGame(header, data);
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
