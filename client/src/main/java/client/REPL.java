package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import shared.AuthData;
import shared.ErrorResponses;
import shared.GameData;
import ui.EscapeSequences;

import java.util.*;

import static ui.EscapeSequences.*;

public class REPL {
    private static AuthData authData = new AuthData(null, null);
    private static ServerFacade serverFacade;
    private static ArrayList<Integer> gameIdMap = new ArrayList<>();
    private static State state = State.LOGGED_OUT;

    public static void run (int port){
        serverFacade = new ServerFacade(port);
        Scanner stdin = new Scanner(System.in);
        while(true) {
            if(state == State.IN_GAME){
                ChessBoard board = new ChessBoard();
                board.resetBoard();
                drawBoard(board, true);
            }
            System.out.print("[" + (Optional.ofNullable(authData.username()).orElse("LOGGED OUT")) + "] $:: ");
            String received = stdin.nextLine();
            String[] input = received.split(" ", 0);
            String in = input[0];
            if(theyHavePut(in, "quit")){
                if(state != State.LOGGED_OUT){
                    serverFacade.logout(authData.authToken());
                }
                break;
            } else if(theyHavePut(in, "help")) {
                if (state == State.LOGGED_IN) {
                    displayLoggedInOptions();
                } else if(state == State.LOGGED_OUT){
                    displayLoggedOutOptions();
                } else {
                    displayInGameOptions();
                }
            } else if(theyHavePut(in, "exit") && checkStateStatus(state, State.IN_GAME)){
                state = State.LOGGED_IN;
            } else if(theyHavePut(in, "login") && checkStateStatus(state, State.LOGGED_OUT)){
                login(input);
            } else if(theyHavePut(in, "register") && checkStateStatus(state, State.LOGGED_OUT)){
                register(input);
            } else if(theyHavePut(in, "logout") && checkStateStatus(state, State.LOGGED_IN)){
                logout();
            } else if(theyHavePut(in, "create") && checkStateStatus(state, State.LOGGED_IN)) {
                createGame(input);
            } else if(theyHavePut(in, "list") && checkStateStatus(state, State.LOGGED_IN)) {
                listGames();
            } else if (theyHavePut(in, "join") && checkStateStatus(state, State.LOGGED_IN)) {
                joinGame(input);
            } else if (theyHavePut(in, "observe") && checkStateStatus(state, State.LOGGED_IN)) {
                observeGame(input);
            } else {
                printRedAndBlueTextOnSeparateLines("Unknown command \"" + in + "\"", "Type help for options");
            }
        }
    }

    private static void observeGame(String[] input) {
        state = State.IN_GAME;
    }

    private static void joinGame(String[] input) {
        if(input.length < 3){
            printRedAndBlueTextOnSeparateLines("Must provide gameID and player color!", "Usage: join <gameID> (WHITE|BLACK)");
            return;
        }
        int idInteger;
        try {
            idInteger = Integer.parseInt(input[1]);
        } catch (NumberFormatException e) {
            printRedAndBlueTextOnSeparateLines("gameID must be a number!", null);
            return;
        }
        String color = input[2].toLowerCase();
        getGameIds();
        if(!color.equals("white") && !color.equals("black")){
            printRedAndBlueTextOnSeparateLines("color must be either WHITE or BLACK", null);
            return;
        } else if(idInteger > gameIdMap.size() || idInteger <= 0){
            printRedAndBlueTextOnSeparateLines("There's no game with that ID!", null);
            return;
        }
        ErrorResponses error = serverFacade.joinGame(gameIdMap.get(idInteger - 1), color.equals("white"), authData.authToken());
        if (error == ErrorResponses.InvalidColor) {
            System.out.println("That color is already taken!");
        } else if (error != null) {
            System.out.println("Something Went wrong!");
        } else {
            System.out.println("Successfully joined game!");
            state = State.IN_GAME;
        }
    }

    private static void listGames() {
        var response = serverFacade.listGames(authData.authToken());
        if (response.error() != null) {
            printRedAndBlueTextOnSeparateLines("Something went wrong!", null);
            return;
        }
        var games = response.data();
        gameIdMap.clear();
        for (int i = 1; i <= games.size(); i++) {
            var game = games.get(i - 1);
            gameIdMap.add(game.gameID());
            System.out.print(SET_TEXT_COLOR_BLUE);
            System.out.printf("%d: gameID%n" + "    name: %s%n" + "    white: %s%n" + "    black: %s%n" + "%n", i, game.gameName(),
                    Optional.ofNullable(game.whiteUsername()).orElse(""), Optional.ofNullable(game.blackUsername()).orElse(""));
            System.out.print(RESET_TEXT_COLOR);
        }
    }

    private static void createGame(String[] input) {
        if(input.length < 2){
            printRedAndBlueTextOnSeparateLines("Must provide game name!","Usage: create <name>");
            return;
        }
        String name = input[1];
        var response = serverFacade.createGame(name, authData.authToken());
        if(response.error() != null){
            printRedAndBlueTextOnSeparateLines("Something went wrong!", null);
        } else {
            System.out.println("Successfully created game \"" + name + "\".");
        }
    }

    private static void logout() {
        var error = serverFacade.logout(authData.authToken());
        switch (error) {
            case null -> System.out.println("Logged out successfully!");
            case InvalidAuthToken -> printRedAndBlueTextOnSeparateLines("Bad authorization", null);
            default -> printRedAndBlueTextOnSeparateLines("Something went Wrong!", null);
        }
        authData = new AuthData(null, null);
        state = State.LOGGED_OUT;
    }

    private static void register(String[] input) {
        if(input.length < 4){
            printRedAndBlueTextOnSeparateLines("Must provide username and password and email!", "Usage: register <username> <password> <email>");
            return;
        }
        var response = serverFacade.register(input[1], input[2], input[3]);
        if(response.error() != null){
            switch(response.error()){
                case InvalidUsername -> printRedAndBlueTextOnSeparateLines("Username already taken!", null);
                case InvalidEmail -> printRedAndBlueTextOnSeparateLines("You've already signed up using that email!", null);
                case MajorError -> printRedAndBlueTextOnSeparateLines("Something went wrong internally. Try again later!", null);
                default -> printRedAndBlueTextOnSeparateLines("Something went wrong! try again later!", null);
            }
        } else {
            authData = response.data();
            state = State.LOGGED_IN;
            System.out.println("You are now logged in as " + authData.username());
        }
    }

    private static void login(String[] input) {
        if(input.length < 3){
            printRedAndBlueTextOnSeparateLines("Must provide username and password!", "Usage: login <username> <password>");
            return;
        }
        var response = serverFacade.login(input[1], input[2]);
        switch(response.error()){
            case null -> {
                authData = response.data();
                state = State.LOGGED_IN;
                System.out.println("You are now logged in as " + authData.username());
            }
            case InvalidUsername, InvalidPassword -> printRedAndBlueTextOnSeparateLines("Incorrect username or password!", null);
            case MajorError -> printRedAndBlueTextOnSeparateLines("Something went wrong internally. Try again later!", null);
            default -> printRedAndBlueTextOnSeparateLines("Something went wrong! try again later!", null);
        }
    }

    private static void printRedAndBlueTextOnSeparateLines(String redText, String blueText){
        System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        System.out.println(redText);
        if(blueText != null) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
            System.out.println(blueText);
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private static boolean checkStateStatus(State actual, State expected){
        if(actual == expected){
            return true;
        }
        System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        if(expected == State.IN_GAME){
            System.out.println("You're not in a game!");
        } else if(actual == State.LOGGED_OUT){
            System.out.println("You're not logged in!");
        } else if(actual == State.LOGGED_IN){
            System.out.println("You're already logged in!");
        } else if(actual == State.IN_GAME){
            System.out.println("You're in the middle of a game!");
        }
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        return false;
    }

    private static void getGameIds(){
        gameIdMap.clear();
        for (GameData game : serverFacade.listGames(authData.authToken()).data()) {
            gameIdMap.add(game.gameID());
        }
    }

    private static boolean theyHavePut(Object in, Object b){
        return Objects.equals(in, b);
    }

    private static void printColoredOption(String blueText, String blackText){
        System.out.print(SET_TEXT_COLOR_BLUE);
        System.out.print(blueText);
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(" - ");
        System.out.print(blackText);
        System.out.println();
    }

    private static void displayLoggedOutOptions(){
        printColoredOption("register <USERNAME> <PASSWORD> <EMAIL>", "create an account");
        printColoredOption("login <USERNAME> <PASSWORD>", "log in to your account");
        printColoredOption("quit", "quit the program");
        printColoredOption("help", "display this help screen");
    }

    private static void displayLoggedInOptions() {
        printColoredOption("create <NAME>", "create game");
        printColoredOption("list", "list games");
        printColoredOption("join <ID> (WHITE|BLACK)", "join game");
        printColoredOption("observe <ID>", "observe game");
        printColoredOption("logout", "log out of your account");
        printColoredOption("quit", "quit the program");
        printColoredOption("help", "display this help screen");
    }

    private static void displayInGameOptions() {
        printColoredOption("exit", "exit the game (without leaving)");
        printColoredOption("quit", "quit the program");
        printColoredOption("help", "display this help screen");
    }

    private static void drawBoard(ChessBoard board, boolean whitePerspective){
        int direction = whitePerspective ? -1 : 1;
        int start = whitePerspective ? 8 : 1;
        System.out.print(SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE + SET_BG_COLOR_DARK_GREEN);
        String letters = whitePerspective ? "    a  b  c  d  e  f  g  h    " : "    h  g  f  e  d  c  b  a    ";
        System.out.print(letters);
        System.out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        System.out.println();
        for(int i = start; i < 9 && i > 0; i += direction){
            System.out.print(SET_TEXT_COLOR_WHITE + SET_BG_COLOR_DARK_GREEN);
            System.out.print(" " + i + " ");
            for(int j = 9 - start; j < 9 && j > 0; j -= direction){
                char pieceChar = ' ';
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if(piece != null){
                    switch(board.getPiece(new ChessPosition(i, j)).getPieceType()){
                        case PAWN -> pieceChar = 'P';
                        case KNIGHT -> pieceChar = 'N';
                        case KING -> pieceChar = 'K';
                        case QUEEN -> pieceChar = 'Q';
                        case ROOK -> pieceChar = 'R';
                        case BISHOP -> pieceChar = 'B';
                    }
                    if(board.getPiece(new ChessPosition(i, j)).getTeamColor() == ChessGame.TeamColor.WHITE){
                        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    } else {
                        System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    }
                }
                System.out.print((i + j) % 2 == 0 ?
                        EscapeSequences.SET_BG_COLOR_BLACK :
                        EscapeSequences.SET_BG_COLOR_WHITE);
                System.out.print(" " + pieceChar + " ");
            }
            System.out.print(SET_TEXT_COLOR_WHITE + SET_BG_COLOR_DARK_GREEN);
            System.out.print(" " + i + " ");
            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }
        System.out.print(SET_TEXT_COLOR_WHITE + SET_BG_COLOR_DARK_GREEN);
        System.out.print(letters);
        System.out.print(RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR + RESET_BG_COLOR);
        System.out.println();
    }

    private enum State {
        LOGGED_IN,
        LOGGED_OUT,
        IN_GAME,
    }
}