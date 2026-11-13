package ui;

import chess.*;

import java.util.EmptyStackException;

public class BoardPrinter {
    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {
        if (game == null || game.getBoard()==null) {
            System.out.println("This game has no board to display");
            return;
        }

        StringBuilder output = new StringBuilder();
        ChessBoard board = game.getBoard();
        appendFileLabels(output, perspective);

        for (int rowIndex=0; rowIndex<8; rowIndex++) {
            int row;
            if (perspective == ChessGame.TeamColor.WHITE) {
                row = 8- rowIndex;
            } else {
                row = rowIndex + 1;
            }
            output.append("  ").append(row).append(" ");

            for (int columnIndex = 0; columnIndex <8; columnIndex++) {
                int column;
                if (perspective == ChessGame.TeamColor.WHITE) {
                    column = columnIndex + 1;
                } else {
                    column = 8 - columnIndex;
                }
                String background;

                if ((row + column) % 2 == 0) {
                    background = EscapeSequences.SET_BG_COLOR_DARK_GREY;
                } else {
                    background = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
                }

                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);

                output.append(background);
                output.append(pieceSymbol(piece));
                output.append(EscapeSequences.RESET_BG_COLOR);
            }
            output.append("  ").append(row).append(System.lineSeparator());
        }
        appendFileLabels(output, perspective);
        System.out.print(output);
    }

    private static void appendFileLabels(StringBuilder output, ChessGame.TeamColor perspective) {
        String labels;

        if (perspective == ChessGame.TeamColor.WHITE) {
            labels = "     a   b   c  d   e  f   g   h    ";
        } else {
            labels = "     h   g   f  e   d  c   b   a    ";
        }
        output.append(labels).append(System.lineSeparator());
    }

    private static String pieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        String color;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = EscapeSequences.SET_TEXT_COLOR_RED;
        } else {
            color = EscapeSequences.SET_TEXT_COLOR_BLUE;
        }

        String symbol = switch (piece.getPieceType()) {
            case KING -> EscapeSequences.WHITE_KING;
            case QUEEN -> EscapeSequences.WHITE_QUEEN;
            case BISHOP -> EscapeSequences.WHITE_BISHOP;
            case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
            case ROOK -> EscapeSequences.WHITE_ROOK;
            case PAWN -> EscapeSequences.WHITE_PAWN;
            default -> EscapeSequences.EMPTY;
        };

        return color + symbol + EscapeSequences.RESET_TEXT_COLOR;
    }
}
