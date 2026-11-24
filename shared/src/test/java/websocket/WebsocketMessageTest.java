package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketMessageTest {
    private final Gson gson = new Gson();

    @Test
    void makeMoveCommandIncludesMoveAndCanBeReadBack() {
        ChessMove move = new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null
        );
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.MAKE_MOVE,
                "test-token",
                17,
                move
        );

        JsonObject json = gson.toJsonTree(command).getAsJsonObject();

        assertEquals("MAKE_MOVE", json.get("commandType").getAsString());
        assertEquals("test-token", json.get("authToken").getAsString());
        assertEquals(17, json.get("gameID").getAsInt());
        assertTrue(json.has("move"));
        assertEquals(move, gson.fromJson(json, UserGameCommand.class).getMove());
    }

    @Test
    void loadGameMessageUsesGameField() {
        ChessGame game = new ChessGame();
        ServerMessage message = ServerMessage.loadGame(game);

        JsonObject json = gson.toJsonTree(message).getAsJsonObject();

        assertEquals("LOAD_GAME", json.get("serverMessageType").getAsString());
        assertTrue(json.has("game"));
        assertNotNull(gson.fromJson(json, ServerMessage.class).getGame());
        assertFalse(json.has("message"));
        assertFalse(json.has("errorMessage"));
    }

    @Test
    void notificationAndErrorMessagesUseTheirRequiredFields() {
        JsonObject notification = gson.toJsonTree(
                ServerMessage.notification("A player joined as WHITE.")
        ).getAsJsonObject();
        JsonObject error = gson.toJsonTree(
                ServerMessage.error("Error: invalid command")
        ).getAsJsonObject();

        assertEquals("NOTIFICATION", notification.get("serverMessageType").getAsString());
        assertEquals("A player joined as WHITE.", notification.get("message").getAsString());
        assertFalse(notification.has("game"));
        assertFalse(notification.has("errorMessage"));

        assertEquals("ERROR", error.get("serverMessageType").getAsString());
        assertTrue(error.get("errorMessage").getAsString().toLowerCase().contains("error"));
        assertFalse(error.has("game"));
        assertFalse(error.has("message"));
    }
}