package services;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccess;
import datamodels.AuthData;
import datamodels.GameData;

import java.util.ArrayList;
import java.util.List;

public class GamePlayService {
    private final DataAccess dataAccess;

    public GamePlayService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public List<GameData> listGame(String authToken) throws Exception {
        requireAuth(authToken);
        // First check auth, then return an array list of all the values
        return new ArrayList<>(dataAccess.listGames());
    }

    public Integer createGame(String authToken, String gameName) throws Exception {
        requireAuth(authToken);
        if (gameName==null || gameName.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }
        // Initialize game, then return just the ID for the json response
        return dataAccess.createGame(new GameData(null, null, null, gameName, new ChessGame()));
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, Integer gameID) throws Exception {
        requireAuth(authToken);
        if (teamColor==null || gameID==null) {
            throw new ServiceException(400, "Error: bad request");
        }
        String username = dataAccess.getAuthData(authToken).username();
        GameData game = dataAccess.getGameData(gameID);
        if (game==null) {
            throw new ServiceException(400, "Error: bad Request");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        // First check which team they want, then check if available. If yes, proceed
        // If no, send to 403 error
        if (teamColor == ChessGame.TeamColor.BLACK) {
            if (blackUsername != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            blackUsername = username;
        } else {
            if (whiteUsername != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            whiteUsername = username;
        }

        GameData updatedGame = new GameData(game.gameID(), whiteUsername, blackUsername, game.gameName(), game.game());
        dataAccess.updateGame(updatedGame);
    }
    public GameContext getGameContext(String authToken, Integer gameID) throws Exception {
        AuthData authData = requireAuth(authToken);
        if (gameID == null) {
            throw new ServiceException(400, "Error: bad request");
        }

        GameData game = dataAccess.getGameData(gameID);
        if (game == null) {
            throw new ServiceException(400, "Error: game not found");
        }

        return new GameContext(authData.username(), game);
    }

    public MoveResult makeMove(String authToken, Integer gameID, ChessMove move)
            throws Exception {
        GameContext context = getGameContext(authToken, gameID);
        if (move == null) {
            throw new ServiceException(400, "Error: move is required");
        }

        GameData gameData = context.game();
        ChessGame game = gameData.game();
        ChessGame.TeamColor playerColor = playerColor(gameData, context.username());

        if (playerColor == null) {
            throw new ServiceException(403, "Error: observers cannot make moves");
        }
        if (game.isGameOver()) {
            throw new ServiceException(400, "Error: game is over");
        }
        if (game.getTeamTurn() != playerColor) {
            throw new ServiceException(400, "Error: it is not your turn");
        }

        try {
            game.makeMove(move);
        } catch (InvalidMoveException exception) {
            throw new ServiceException(400, "Error: invalid move");
        }

        ChessGame.TeamColor nextPlayer = game.getTeamTurn();
        boolean inCheck = game.isInCheck(nextPlayer);
        boolean stalemate = game.isInStalemate(nextPlayer);
        boolean checkmate = !stalemate && game.isInCheckmate(nextPlayer);

        if (stalemate || checkmate) {
            game.setGameOver(true);
        }

        dataAccess.updateGame(gameData);
        String checkedPlayer = usernameForColor(gameData, nextPlayer);
        return new MoveResult(gameData, inCheck, checkmate, stalemate, checkedPlayer);
    }

    public GameData resign(String authToken, Integer gameID) throws Exception {
        GameContext context = getGameContext(authToken, gameID);
        GameData gameData = context.game();

        if (playerColor(gameData, context.username()) == null) {
            throw new ServiceException(403, "Error: observers cannot resign");
        }
        if (gameData.game().isGameOver()) {
            throw new ServiceException(400, "Error: game is over");
        }

        gameData.game().setGameOver(true);
        dataAccess.updateGame(gameData);
        return gameData;
    }

    public GameData leaveGame(String authToken, Integer gameID) throws Exception {
        GameContext context = getGameContext(authToken, gameID);
        GameData gameData = context.game();
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();

        if (context.username().equals(whiteUsername)) {
            whiteUsername = null;
        } else if (context.username().equals(blackUsername)) {
            blackUsername = null;
        } else {
            // Observers leave the connection but do not occupy a player slot.
            return gameData;
        }

        GameData updatedGame = new GameData(
                gameData.gameID(),
                whiteUsername,
                blackUsername,
                gameData.gameName(),
                gameData.game()
        );
        dataAccess.updateGame(updatedGame);
        return updatedGame;
    }

    private AuthData requireAuth(String authToken) throws Exception {
        if (authToken == null || authToken.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        AuthData authData = dataAccess.getAuthData(authToken);
        if (authData == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return authData;
    }

    private ChessGame.TeamColor playerColor(GameData game, String username) {
        if (username.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        if (username.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private String usernameForColor(GameData game, ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE
                ? game.whiteUsername()
                : game.blackUsername();
    }

    public record GameContext(String username, GameData game) { }

    public record MoveResult(
            GameData game,
            boolean inCheck,
            boolean checkmate,
            boolean stalemate,
            String checkedPlayer
    ) { }
}