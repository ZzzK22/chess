package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import shared.AuthData;
import shared.ErrorResponses;
import shared.GameData;
import shared.ResponseData;
import ui.EscapeSequences;

import java.util.*;

import static ui.EscapeSequences.*;

public class REPL {
    private static String username = null;
    private static String authToken = null;
    private static boolean loggedIn = false;
    private static ServerFacade serverFacade;
    private static ArrayList<Integer> gameIdMap = new ArrayList<>();
    public static void run (int port){
        serverFacade = new ServerFacade(port);
        Scanner stdin = new Scanner(System.in);
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        drawBoard(board, true);
        while(true) {
            loggedIn = username != null;
            System.out.print("[" + (Optional.ofNullable(username).orElse("LOGGED OUT")) + "] $:: ");
            String received = stdin.nextLine();
            String[] input = received.split(" ", 0);
            String in = input[0];
            if(theyHavePut(in, "quit")){
                if(loggedIn){
                    logout();
                }
                break;
            } else if(theyHavePut(in, "help")){
                if(loggedIn){
                    displayLoggedInOptions();
                } else {
                    displayLoggedOutOptions();
                }
            } else if(theyHavePut(in, "login")){
                if(loggedIn){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("You're already logged in as " + username);
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                if(input.length < 3){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("Must provide username and password!");
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    System.out.println("Usage: login <username> <password>");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                var response = login(input[1], input[2]);
                if(response.error() != null){
                    switch(response.error()){
                        case InvalidUsername, InvalidPassword -> System.out.println("Incorrect username or password!");
                        case MajorError -> System.out.println("Something went wrong internally. Try again later!");
                        default -> System.out.println("Something went wrong! try again later!");
                    }
                } else {
                    authToken = response.data().authToken();
                    username = response.data().username();
                    System.out.println("You are now logged in as " + username);
                }
                continue;
            } else if(theyHavePut(in, "register")){
                if(loggedIn){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("You're already logged in as " + username);
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                if(input.length < 4){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("Must provide username and password and email!");
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    System.out.println("Usage: register <username> <password> <email>");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                var response = register(input[1], input[2], input[3]);
                if(response.error() != null){
                    switch(response.error()){
                        case InvalidUsername -> System.out.println("Username already taken!");
                        case InvalidEmail -> System.out.println("You've already signed up using that email!");
                        case MajorError -> System.out.println("Something went wrong internally. Try again later!");
                        default -> System.out.println("Something went wrong! try again later!");
                    }
                } else {
                    authToken = response.data().authToken();
                    username = response.data().username();
                    System.out.println("You are now logged in as " + username);
                }
            } else if(theyHavePut(in, "logout")){
                if(!loggedIn){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("You're already logged out!");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                var error = logout();
                switch (error) {
                    case null -> System.out.println("Logged out successfully!");
                    case InvalidAuthToken -> System.out.println("Bad authorization");
                    default -> System.out.println("Something went Wrong!");
                }
                username = null;
                authToken = null;
            } else if(theyHavePut(in, "create")) {
                if (!loggedIn) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("You're not logged in!");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                if(input.length < 2){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("Must provide game name!");
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    System.out.println("Usage: create <name>");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                String name = input[1];
                var response = createGame(name);
                if(response.error() != null){
                    System.out.println("Something went wrong!");
                    continue;
                }
                System.out.println("Successfully created game \"" + name + "\".");
            } else if(theyHavePut(in, "list")) {
                if (!loggedIn) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("You're not logged in!");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                var response = listGames();
                if (response.error() != null) {
                    System.out.println("Something went wrong!");
                    continue;
                }
                var games = response.data();
                gameIdMap.clear();
                for (int i = 1; i <= games.size(); i++) {
                    var game = games.get(i - 1);
                    gameIdMap.add(game.gameID());
                    System.out.print(SET_TEXT_COLOR_BLUE);
                    System.out.printf(
                            "%d: gameID%n" +
                                    "    name: %s%n" +
                                    "    white: %s%n" +
                                    "    black: %s%n" +
                                    "%n", i, game.gameName(),
                            Optional.ofNullable(game.whiteUsername()).orElse(""),
                            Optional.ofNullable(game.blackUsername()).orElse(""));
                    System.out.print(RESET_TEXT_COLOR);
                }
            } else if (theyHavePut(in, "join")) {
                if (!loggedIn) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("You're not logged in!");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                if(input.length < 3){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("Must provide gameID and player color!");
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    System.out.println("Usage: join <gameID> (WHITE|BLACK)");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                String idString = input[1];
                int idInteger;
                try {
                    idInteger = Integer.parseInt(idString);
                } catch (NumberFormatException e) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("gameID must be a number!");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                String color = input[2].toLowerCase();
                if(!color.equals("white") && !color.equals("black")){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("color must be either WHITE or BLACK");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                if(idInteger > gameIdMap.size() || idInteger <= 0){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("There's no game with that ID!");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                    continue;
                }
                ErrorResponses error = joinGame(idInteger - 1, color.equals("white"));
                if (error == ErrorResponses.InvalidColor) {
                    System.out.println("That color is already taken!");
                    continue;
                } else if (error != null){
                    System.out.println("Something Went wrong!");
                    continue;
                }
                System.out.println("Successfully joined game!");
            } else {
                System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                System.out.println("Unknown command \"" + in + "\"");
                System.out.print(EscapeSequences.RESET_TEXT_COLOR);
                System.out.println("Type help for options");
            }
        }

    }

    private static ErrorResponses joinGame(int idInteger, boolean isWhite) {
        return serverFacade.joinGame(gameIdMap.get(idInteger), isWhite, authToken);
    }

    private static void getGameIds(){
        gameIdMap.clear();
        for (GameData game : listGames().data()) {
            gameIdMap.add(game.gameID());
        }
    }
    private static ResponseData<ArrayList<GameData>> listGames() {
        return serverFacade.listGames(authToken);
    }

    private static ResponseData<Integer> createGame(String name) {
        return serverFacade.createGame(name, authToken);
    }

    private static ResponseData<AuthData> login(String username, String password){
        return serverFacade.login(username, password);
    }

    private static ResponseData<AuthData> register(String username, String password, String email){
        var response = serverFacade.register(username, password, email);
        if(response.error() != null){
            return new ResponseData<>(response.error(), null);
        }
        return response;
    }

    private static boolean theyHavePut(Object in, Object b){
        return Objects.equals(in, b);
    }

    private static ErrorResponses logout() {
        return serverFacade.logout(authToken);
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
}