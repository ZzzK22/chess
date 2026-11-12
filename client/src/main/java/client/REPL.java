package client;

import shared.AuthData;
import shared.ErrorResponses;
import shared.ResponseData;
import ui.EscapeSequences;

import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class REPL {
    private static String username = null;
    private static String authToken = null;
    private static boolean loggedIn = false;
    private static ServerFacade serverFacade;
    public static void run (int port){
        serverFacade = new ServerFacade(port);
        Scanner stdin = new Scanner(System.in);
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
            }
            if(theyHavePut(in, "help")){
                if(loggedIn){
                    displayLoggedInOptions();
                } else {
                    displayLoggedOutOptions();
                }
                continue;
            }
            if(theyHavePut(in, "login")){
                if(input.length < 3){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("Must provide username and password!");
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    System.out.println("Usage: login <username> <password>");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
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
            }
            if(theyHavePut(in, "register")){
                if(input.length < 4){
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
                    System.out.println("Must provide username and password and email!");
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
                    System.out.println("Usage: register <username> <password> <email>");
                    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
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
                continue;
            }
            if(theyHavePut(in, "logout")){
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
                continue;

            }
            System.out.println(in);
        }

    }

    private static ResponseData<AuthData> login(String username, String password){
        var response = serverFacade.login(username, password);
        if(response.error() != null){
            return new ResponseData<>(response.error(), null);
        }

        return response;
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
}
