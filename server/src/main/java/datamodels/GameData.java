package datamodels;

import chess.ChessGame;

public record GameData(Integer gameID, String whiteUser, String blackUser, String gameName, ChessGame game) {
}
