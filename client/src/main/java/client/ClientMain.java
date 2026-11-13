package client;

import chess.*;
import datamodels.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ClientMain {
    private static final int SERVER_PORT=8080;
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AuthData authData;
    private boolean clientRunning = true;
    private List<GameData> currentGames = List.of();

    private ClientMain(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ClientMain client = new ClientMain(new ServerFacade(SERVER_PORT), scanner);
            client.run();
        }
    }

    private void run() {
        while (clientRunning) {
            if (authData == null) {
                runPrelogin();
            } else {
                runPostLogin();
            }
        }
    }

    private void runPrelogin() {
        System.out.println("Welcome to 240 Chess.");

        while (clientRunning && authData == null) {
            System.out.print("[LOGGED_OUT] >>> ");

            if (!scanner.hasNextLine()) {
                clientRunning = false;
                return;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] arguments = input.split("\\s+");
            String command = arguments[0].toLowerCase(Locale.ROOT);

            switch (command) {
                case "help" -> printPreloginHelp();
                case "register" -> register(arguments);
                case "login" -> login(arguments);
                case "quit" -> clientRunning = false;
                default -> System.out.println(
                        "Unknown command. Type help to see the available commands."
                );
            }
        }

        if (authData != null) {
            System.out.println("Logged in as " + authData.username());
        }
    }

    private void runPostLogin() {
        System.out.print("[LOGGED_IN] >>> ");

        if (!scanner.hasNextLine()) {
            clientRunning = false;
            return;
        }

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        String[] arguments = input.split("\\s+");
        String command = arguments[0].toLowerCase(Locale.ROOT);

        switch (command) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout(arguments);
            case "create" -> createGame(input);
            case "list" -> listGames(arguments);
            case "play" -> playGame(arguments);
            case "observe" -> observeGame(arguments);
            case "quit" -> clientRunning = false;
            default -> System.out.println(
                    "Unknown command. Type help to see the available commands."
            );
        }
    }

    private void register(String[] arguments) {
        if (arguments.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }

        try {
            authData = serverFacade.register(arguments[1], arguments[2], arguments[3]);
        } catch (Exception exception) {
            handleRequestFailure(exception);
        }
    }

    private void login(String[] arguments) {
        if (arguments.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }

        try {
            authData = serverFacade.login(arguments[1], arguments[2]);
        } catch (Exception exception) {
            handleRequestFailure(exception);
        }
    }

    private void logout(String[] arguments) {
        if (arguments.length != 1) {
            System.out.println("Usage: logout");
            return;
        }

        try {
            serverFacade.logout(authData.authToken());
            authData = null;
            System.out.println("Logged out.");
        } catch (Exception exception) {
            handleRequestFailure(exception);
        }
    }

    private void createGame (String input) {
        String gameName = input.substring("create".length()).trim();

        if (gameName.isEmpty()) {
            System.out.println("Usage: create <NAME>");
            return;
        }

        try {
            serverFacade.createGame(authData.authToken(), gameName);
            currentGames = List.of();
            System.out.println("Created game: " + gameName);
        } catch (Exception exception) {
            handleRequestFailure(exception);
        }
    }

    private void listGames(String[] arguments) {
        if (arguments.length != 1) {
            System.out.println("Usage: list");
            return;
        }

        try {
            List<GameData> games = serverFacade.listGames(authData.authToken());
            currentGames = List.copyOf(games);
            displayGames(currentGames);
        } catch (Exception exception) {
            handleRequestFailure(exception);
        }
    }

    private void displayGames(List<GameData> games) {
        if (games.isEmpty()) {
            System.out.println("There are no games.");
            return;
        }

        for (int i=0; i < games.size(); i++) {
            GameData game = games.get(i);

            System.out.printf(
                    "%d. %s (White: %s, Black: %s)%n", i + 1, game.gameName(),
                    playerName(game.whiteUsername()), playerName(game.blackUsername())
            );
        }
    }

    private GameData selectGame(String number) {
        if(currentGames.isEmpty()) {
            System.out.println("Use list first to see available games");
            return null;
        }

        int selection;
        try {
            selection = Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            System.out.println("Input a game number, like 1.");
            return null;
        }

        if (selection <1 || selection > currentGames.size()) {
            System.out.println("That game number isn't in the list.");
            return null;
        }
        return currentGames.get(selection-1);
    }

    private void playGame(String[] arguments) {
        if (arguments.length !=3) {
            System.out.println("Usage: play <NUMBER> <WHITE|BLACK>");
            return;
        }
        GameData selectedGame = selectGame(arguments[1]);
        if (selectedGame == null) {
            return;
        }
        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(arguments[2].toUpperCase(Locale.ROOT));
            serverFacade.joinGame(authData.authToken(), color, selectedGame.gameID());
            System.out.println("Joined " + selectedGame.gameName() + " as " + color + ".");
        } catch (IllegalArgumentException exception) {
            System.out.println("Choose WHITE or BLACK");
        } catch (Exception exception) {
            handleRequestFailure(exception);
        }
    }

    private void observeGame(String[] arguments) {
        if (arguments.length != 2) {
            System.out.println("Usage: observe <NUMBER>");
            return;
        }

        GameData selectedGame = selectGame(arguments[1]);
        if (selectedGame == null) {
            return;
        }
        System.out.println("Observing " + selectedGame.gameName() + ".");
    }

    private String playerName(String username) {
        if (username == null) {
            return "open";
        } else {
            return username;
        }
    }


    private void printPreloginHelp() {
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - create an account");
        System.out.println("login <USERNAME> <PASSWORD> - log in");
        System.out.println("quit - exit the client");
        System.out.println("help - show these commands");
    }

    private void printPostloginHelp() {
        System.out.println("create <NAME> - create a game");
        System.out.println("list - list games");
        System.out.println("logout - log out");
        System.out.println("play <NUMBER> <WHITE|BLACK> - join a game");
        System.out.println("observe <NUMBER> - observe a game");
        System.out.println("quit - exit the client");
        System.out.println("help - show these commands");
    }

    private void handleRequestFailure(Exception exception) {
        if (exception instanceof ServerFacade.ServerException serverException) {
            System.out.println(serverException.getMessage());
        } else if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            clientRunning = false;
            System.out.println("The request was interrupted.");
        } else if (exception instanceof IOException) {
            System.out.println(
                    "Could not connect to the Chess server. Make sure it is running."
            );
        } else {
            System.out.println("The request could not be completed.");
        }
    }
}