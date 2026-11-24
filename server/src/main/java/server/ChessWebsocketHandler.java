package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.javalin.websocket.WsContext;
import java.util.*;
import java.util.concurrent.*;

import org.eclipse.jetty.io.ClientConnectionFactory;
import services.GamePlayService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class ChessWebsocketHandler {
    private static final Gson Gson = new Gson();
    private static final String CONNECTION_ID = "chessConnectionID";

    private final GamePlayService gamePlayService;
    private final ConcurrentMap<String, ClientConnection> connectById = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Set<String>> connectionsByGame = new ConcurrentHashMap<>();

    public ChessWebsocketHandler(GamePlayService gamePlayService) {
        this.gamePlayService = gamePlayService;
    }

    public void onConnect(WsContext context) {
        context.sessionAttribute(CONNECTION_ID, UUID.randomUUID().toString());
    }

    public void onMessage(WsContext context) {
        String connectionId = getConnectionId(context);
        UserGameCommand command;

        try {
            command = Gson.fromJson(context.message(), UserGameCommand.class);
        } catch (JsonParseException exception) {
            sendError(context, "Error: Invalid command");
            return;
        }

        if (command == null || command.getCommandType()==null || command.getGameID()==null) {
            sendError(context, "Error: invalid command");
            return;
        }

        if (command.getCommandType() != UserGameCommand.CommandType.CONNECT) {
            sendError(context, "Error: this command ins't implemented yet.");
            return;
        }

        connect(context, connectionId, command);
    }

    public void onClose(WsContext context) {

    }

    private void connect(WsContext context, String connectionId, UserGameCommand command) {
        if (connectionId==null || connectionId.isBlank()) {
            sendError(context, "Error: connection isn't initialized");
            return;
        }

        if (connectionsById.containsKey(connectionId)) {
            sendError(context, "Error: this connection is already connected");
            return;
        }

        try {
            GamePlayService.GameContext gameContext = gamePlayService.getGameContext(command.getAuthToken(), command.getGameID());
            ChessGame.TeamColor playerColor = getPlayerColor(gameContext);
            ClientConnection connection = new ClientConnection(connectionId, command.getGameID(), gameContext.username(), playerColor, context);

            connectionsById.put(connectionId, connection);
            connectionsByGame.computeIfAbsent(command.getGameID(), ignored -> ConcurrentHashMap.newKeySet()).add(connectionId);
        }
    }

    private void broadcast(Integer gameID, String excludedConnectionId, ServerMessage message) {
        Set<String> connectionIds = connectionsByGame.get(gameID);
        if (connectionIds == null) {
            return;
        }

        for (String id: new ArrayList<>(connectionIds)) {
            if (id.equals(excludedConnectionId)) {
                continue;
            }

            ClientConnection connection = connectionIds.get(id);
            if (connection != null) {
                send(connection.context(), message);
            }
        }
    }

    private String getConnectionId(WsContext context) {
        Object value = context.sessionAttribute(CONNECTION_ID);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private void sendError(WsContext context, String message) {
        ServerMessage error = ServerMessage.error(message);
        send(context, error);
    }

    private void send(WsContext context, ServerMessage message) {
        context.send(Gson.toJson(message));
    }

    private record ClientConnection(String id, Integer gameID, String username, ChessGame.TeamColor playerColor, WsContext context) {

    }

}
