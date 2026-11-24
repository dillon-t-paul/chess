package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import java.util.*;
import java.util.concurrent.*;

import org.eclipse.jetty.io.ClientConnectionFactory;
import services.GamePlayService;
import services.ServiceException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class ChessWebsocketHandler {
    private static final Gson Gson = new Gson();

    private final GamePlayService gamePlayService;
    private final ConcurrentMap<String, ClientConnection> connectionsById = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Set<String>> connectionsByGame = new ConcurrentHashMap<>();

    public ChessWebsocketHandler(GamePlayService gamePlayService) {
        this.gamePlayService = gamePlayService;
    }

    public void onConnect(WsContext context) {

    }

    public void onMessage(WsMessageContext context) {
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
        String connectionId = getConnectionId(context);
        if (connectionId == null) {
            return;
        }
        ClientConnection connection = connectionsById.remove(connectionId);
        if (connection == null) {
            return;
        }

        Set<String> gameConnections = connectionsByGame.get(connection.gameID());
        if (gameConnections != null) {
            gameConnections.remove(connectionId);
            if (gameConnections.isEmpty()) {
                connectionsByGame.remove(connection.gameID(), gameConnections);
            }
        }
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

            send(context, ServerMessage.loadGame(gameContext.game().game()));
            String notification;
            if (playerColor == null) {
                notification = gameContext.username() + " joined as an observer.";
            } else {
                notification = gameContext.username() + " joined as " + playerColor + ".";
            }

             broadcast(command.getGameID(), connectionId, ServerMessage.notification(notification));
        } catch (ServiceException exception) {
            sendError(context, exception.getMessage());
        } catch (Exception exception) {
            sendError(context, "Error: connection could not be completed");
        }
    }

    private ChessGame.TeamColor getPlayerColor(
            GamePlayService.GameContext gameContext
    ) {
        if (gameContext.username().equals(
                gameContext.game().whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }

        if (gameContext.username().equals(
                gameContext.game().blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }

        return null;
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

            ClientConnection connection = connectionsById.get(id);
            if (connection != null) {
                send(connection.context(), message);
            }
        }
    }

    private String getConnectionId(WsContext context) {
        return context.sessionId();
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
