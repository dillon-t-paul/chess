package services;

import chess.*;
import datamodels.*;
import dataaccess.DataAccess;

import java.rmi.ServerException;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class ServiceActions {
    private final DataAccess dataAccess;

    public ServiceActions(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData registerUser(String username, String password, String email) throws Exception {
        // First check to make sure inputs are correct
        if (emptyOrNull(username) || emptyOrNull(password) || emptyOrNull(email)) {
            throw new ServiceException(400, "Error: bad request");
        }
        // Then make sure username is available
        if (dataAccess.getUserData(username) != null) {
            throw new ServiceException(403, "Error: already taken");
        }
        // then create a new user object and store it in the dataAccess map.
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        dataAccess.registerUser(new UserData(username, hashedPassword, email));
        return createAuth(username);
    }

    public AuthData loginUser(String username, String password) throws Exception {
        // Need to make sure the inputs are valid
        if (emptyOrNull(username) || emptyOrNull(password)) {
            throw new ServiceException(400, "Error: bad request");
        }
        UserData user = dataAccess.getUserData(username);
        // Pull username, then make sure user exists and credentials match
        if (user == null || !BCrypt.checkpw(password, user.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        // creates authToken for current log in session
        return createAuth(username);
    }

    public void logoutUser(String authToken) throws Exception {
        checkAuth(authToken);
        // First check auth, then remove their auth token, as it is no longer valid post logout.
        dataAccess.removeAuthData(authToken);
    }

    public List<GameData> listGame(String authToken) throws Exception {
        checkAuth(authToken);
        // First check auth, then return an array list of all the values
        return new ArrayList<>(dataAccess.listGames());
    }

    public Integer createGame(String authToken, String gameName) throws Exception {
        checkAuth(authToken);
        if (emptyOrNull(gameName)) {
            throw new ServiceException(400, "Error: bad request");
        }
        // Initialize game, then return just the ID for the json response
        return dataAccess.createGame(new GameData(null, null, null, gameName, new ChessGame()));
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, Integer gameID) throws Exception {
        checkAuth(authToken);
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
        checkAuth(authToken);
        if (gameID == null) {
            throw new ServiceException(400, "Error: bag request");
        }

        GameData game = dataAccess.getGameData(gameID);
        if (game==null) {
            throw new ServiceException(400, "Error: game not found");
        }

        AuthData authData = dataAccess.getAuthData(authToken);
        return new GameContext(authData.username(), game);
    }

    public MoveResult makeMove(String authToken, Integer gameID, ChessMove move) throws Exception {
        GameContext context = getGameContext(authToken, gameID);
        if (move==null) {
            throw new ServiceException(400, "Error: move is required");
        }

        GameData gameData = context.game();
        ChessGame game = gameData.game();
        ChessGame.TeamColor playerColor = playerColor(gameData, context.username());

        if (playerColor == null) {
            throw new ServiceException(403, "Error: Observers can't make moves");
        }
        if (game.isGameOver()) {
            throw new ServiceException(400, "Error: game is over");
        }
        if (game.getTeamTurn() != playerColor) {
            throw new ServiceException(400, "Error: it isn't your turn");
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


    public void resetDB() throws Exception {
//        Three calls: Clear UserData, Clear GameData, Clear AuthData
        dataAccess.clear();
    }

    private AuthData createAuth(String username) throws Exception {
        //Generates a random UUID, then converts it to a string to be used as the authToken
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(username, authToken);
        dataAccess.createAuthData(authData);
        //Stores authToken in dataAccess, then returns an AuthData object with all info
        return authData;
    }

    private void checkAuth(String authToken) throws Exception{
        if (emptyOrNull(authToken)) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (dataAccess.getAuthData(authToken) == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }

    private boolean emptyOrNull (String value) {
        // Check if a value is null first, then if it is blank/empty before handling in methods
        if (value==null || value.isBlank() || value.isEmpty()) {
            return true;
        }
        return false;
    }


    public static class ServiceException extends RuntimeException {
        // Custom exception handler that can be reused a lot in the different methods
        private final int status;

        public ServiceException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }
}
