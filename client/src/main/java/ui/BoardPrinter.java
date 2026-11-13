package ui;

import chess.*;

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
            output.append(" ").append(row).append(" ");

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
            output.append(" ").append(row).append(System.lineSeparator());
        }
        appendFileLabels(output, perspective);
        System.out.println(output);
    }

    private static void appendFileLabels(StringBuilder output, ChessGame.TeamColor perspective) {
        output.append("   ");
        for (int columnIndex = 0; columnIndex<8; columnIndex++) {
            int column;

            if (perspective == ChessGame.TeamColor.WHITE) {
                column = columnIndex + 1;
            } else {
                column = 8 - columnIndex;
            }

            char file = (char) ('a' + column - 1);
            output.append(" ").append(file).append(" ");
        }
        output.append(System.lineSeparator());
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

        String symbol;
        switch (piece.getPieceType()) {
            case KING:
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    symbol = EscapeSequences.WHITE_KING;
                } else {
                    symbol = EscapeSequences.BLACK_KING;
                }
                break;

            case QUEEN:
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    symbol = EscapeSequences.WHITE_QUEEN;
                } else {
                    symbol = EscapeSequences.BLACK_QUEEN;
                }
                break;

            case BISHOP:
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    symbol = EscapeSequences.WHITE_BISHOP;
                } else {
                    symbol = EscapeSequences.BLACK_BISHOP;
                }
                break;

            case KNIGHT:
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    symbol = EscapeSequences.WHITE_KNIGHT;
                } else {
                    symbol = EscapeSequences.BLACK_KNIGHT;
                }
                break;

            case ROOK:
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    symbol = EscapeSequences.WHITE_ROOK;
                } else {
                    symbol = EscapeSequences.BLACK_ROOK;
                }
                break;

            case PAWN:
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    symbol = EscapeSequences.WHITE_PAWN;
                } else {
                    symbol = EscapeSequences.BLACK_PAWN;
                }
                break;

            default:
                symbol = EscapeSequences.EMPTY;
                break;
        }

        return color + symbol + EscapeSequences.RESET_TEXT_COLOR;
    }
}
