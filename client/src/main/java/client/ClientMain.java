package client;

import chess.*;
import datamodels.*;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {
    private static final int SERVER_PORT=8080;
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AuthData authData;
    private boolean serverRunning = true;

    private ClientMain(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        try (Scanner scanner = new Scanner(System.in)) {
            ClientMain client = new ClientMain(new ServerFacade(SERVER_PORT), scanner);
            client.runPrelogin();
        }
    }

    private void runPrelogin() {
        System.out.println("Welcome to 240 Chess.");

        while (serverRunning && authData == null) {
            System.out.print("[LOGGED_OUT] >>> ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] arguments = input.split("\\s+");
            String command = arguments[0].toLowerCase();

            switch (command) {
                case "help" -> printPreloginHelp();
                case "register" -> register(arguments);
                case "login" -> login(arguments);
                case "quit" -> serverRunning = false;
                default -> System.out.println(
                        "Unknown command. Type help to see the available commands."
                );
            }
        }

        if (authData != null) {
            System.out.println("Logged in as " + authData.username());
            System.out.println("Next, we’ll connect this to the postlogin menu.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - create an account");
        System.out.println("login <USERNAME> <PASSWORD> - log in");
        System.out.println("quit - exit the client");
        System.out.println("help - show these commands");
    }

    private void register(String[] arguments) {
        if (arguments.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }

        try {
            authData = serverFacade.register(
                    arguments[1],
                    arguments[2],
                    arguments[3]
            );
        } catch (ServerFacade.ServerException exception) {
            System.out.println(exception.getMessage());
        } catch (IOException exception) {
            printConnectionError();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            serverRunning = false;
            System.out.println("The request was interrupted.");
        }
    }

    private void login(String[] arguments) {
        if (arguments.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }

        try {
            authData = serverFacade.login(arguments[1], arguments[2]);
        } catch (ServerFacade.ServerException exception) {
            System.out.println(exception.getMessage());
        } catch (IOException exception) {
            printConnectionError();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            serverRunning = false;
            System.out.println("The request was interrupted.");
        }
    }

    private void printConnectionError() {
        System.out.println(
                "Could not connect to the Chess server. Make sure it is running."
        );
    }
}