package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import services.GamePlayService;

public class ChessWebsocketHandler {
    private static final Gson Gson = new Gson();

    private final GamePlayService gamePlayService;

    public ChessWebsocketHandler(GamePlayService gamePlayService) {
        this.gamePlayService = gamePlayService;
    }

    public void onConnect(WsContext context) {

    }

    public void onMessage(WsContext context) {

    }

    public void onClose(WsContext context) {

    }

    private void connect() {

    }

    private void broadcast() {

    }



}
