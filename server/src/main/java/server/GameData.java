package server;

import chess.ChessGame;

public record GameData(ChessGame game, String playerWhite, String playerBlack, String gameName, int gameID) {
}
