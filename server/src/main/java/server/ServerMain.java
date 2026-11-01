package server;

import chess.*;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;

public class ServerMain {
    public static void main(String[] args) throws DataAccessException {
        var port = 8080;
        Server server = new Server(new SQLDataAccess());
        server.run(port);
        System.out.println("♕ 240 Chess Server running on port " + port);
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
